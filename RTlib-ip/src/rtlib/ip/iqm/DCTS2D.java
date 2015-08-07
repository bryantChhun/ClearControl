package rtlib.ip.iqm;

import static java.lang.Math.sqrt;

import org.jtransforms.dct.DoubleDCT_2D;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import coremem.ContiguousMemoryInterface;
import net.imglib2.img.basictypeaccess.offheap.DoubleOffHeapAccess;
import net.imglib2.img.basictypeaccess.offheap.ShortOffHeapAccess;
import net.imglib2.img.planar.OffHeapPlanarImg;
import net.imglib2.img.planar.OffHeapPlanarImgFactory;
import net.imglib2.img.planar.PlanarCursor;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import pl.edu.icm.jlargearrays.DoubleLargeArray;

public class DCTS2D	implements
					ImageQualityMetricInterface<UnsignedShortType, ShortOffHeapAccess>
{
	private final Table<Long, Long, DoubleDCT_2D> mDoubleDCT2DCache = HashBasedTable.create();

	private OffHeapPlanarImg<DoubleType, DoubleOffHeapAccess> mDoubleWorkingStack;

	private double mPSFSupportRadius = 3;

	public DCTS2D()
	{
		super();
	}

	private DoubleDCT_2D getDCTForWidthAndHeight(	long pWidth,
													long pHeight)
	{
		DoubleDCT_2D lDoubleDCT_2D = mDoubleDCT2DCache.get(	pWidth,
															pHeight);

		if (lDoubleDCT_2D == null)
		{
			try
			{
				lDoubleDCT_2D = new DoubleDCT_2D(	pHeight,
													pWidth,
													true);
				mDoubleDCT2DCache.put(pWidth, pHeight, lDoubleDCT_2D);
			}
			catch (final Throwable e)
			{
				e.printStackTrace();
			}
		}

		return lDoubleDCT_2D;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final double[] computeImageQualityMetric(OffHeapPlanarImg<UnsignedShortType, ShortOffHeapAccess> pOffHeapPlanarImg)
	{
		final int lWidth = (int) pOffHeapPlanarImg.dimension(0);
		final int lHeight = (int) pOffHeapPlanarImg.dimension(1);
		final int lDepth = (int) pOffHeapPlanarImg.dimension(2);

		if (mDoubleWorkingStack != null)
		{
			final boolean lWrongDimensions = mDoubleWorkingStack.dimension(0) != lWidth || mDoubleWorkingStack.dimension(1) != lHeight
												|| mDoubleWorkingStack.dimension(2) != lDepth;
			if (lWrongDimensions)
			{
				mDoubleWorkingStack.free();
				mDoubleWorkingStack = null;
			}
		}

		if (mDoubleWorkingStack == null)
		{
			final OffHeapPlanarImgFactory<DoubleType> lOffHeapPlanarImgFactory = new OffHeapPlanarImgFactory<DoubleType>();
			final int[] lDimensions = new int[]
			{ lWidth, lHeight, lDepth };
			mDoubleWorkingStack = (OffHeapPlanarImg<DoubleType, DoubleOffHeapAccess>) lOffHeapPlanarImgFactory.create(	lDimensions,
																														new DoubleType());
		}

		final PlanarCursor<UnsignedShortType> lCursorShort = pOffHeapPlanarImg.cursor();
		final PlanarCursor<DoubleType> lCursorDouble = mDoubleWorkingStack.cursor();

		while (lCursorShort.hasNext())
		{
			final int lValue = lCursorShort.next().get();
			lCursorDouble.next().set(lValue);
		}

		final double[] lDCTSArray = new double[lDepth];
		for (int z = 0; z < lDepth; z++)
		{
			final ContiguousMemoryInterface lPlaneContiguousMemory = mDoubleWorkingStack.getPlaneContiguousMemory(z);
			final long lAddress = lPlaneContiguousMemory.getAddress();
			final long lLengthInElements = lWidth * lHeight;
			final DoubleLargeArray lDoubleLargeArray = new DoubleLargeArray(mDoubleWorkingStack,
																			lAddress,
																			lLengthInElements);

			final double lDCTS = computeDCTSForSinglePlane(	lDoubleLargeArray,
															lWidth,
															lHeight,
															getPSFSupportRadius());

			lDCTSArray[z] = lDCTS;
		}

		return lDCTSArray;
	}

	private final double computeDCTSForSinglePlane(	DoubleLargeArray pDoubleLargeArray,
													long pWidth,
													long pHeight,
													double pPSFSupportRadius)
	{
		final DoubleDCT_2D lDCTForWidthAndHeight = getDCTForWidthAndHeight(	pWidth,
																			pHeight);

		lDCTForWidthAndHeight.forward(pDoubleLargeArray, false);

		normalizeL2(pDoubleLargeArray);

		final long lOTFSupportRadiusX = Math.round(pWidth / pPSFSupportRadius);
		final long lOTFSupportRadiusY = Math.round(pHeight / pPSFSupportRadius);

		final double lEntropy = entropyPerPixelSubTriangle(	pDoubleLargeArray,
															pWidth,
															pHeight,
															0,
															0,
															lOTFSupportRadiusX,
															lOTFSupportRadiusY);

		return lEntropy;
	}

	private void normalizeL2(DoubleLargeArray pDoubleLargeArray)
	{
		final double lL2 = computeL2(pDoubleLargeArray);
		final double lIL2 = 1.0 / lL2;
		final long lLength = pDoubleLargeArray.length();

		for (long i = 0; i < lLength; i++)
		{
			final double lValue = pDoubleLargeArray.getDouble(i);
			pDoubleLargeArray.setDouble(i, lValue * lIL2);
		}
	}

	private double computeL2(DoubleLargeArray pDoubleLargeArray)
	{
		final long lLength = pDoubleLargeArray.length();

		double l2 = 0;
		for (long i = 0; i < lLength; i++)
		{
			final double lValue = pDoubleLargeArray.getDouble(i);
			l2 += lValue * lValue;
		}

		return sqrt(l2);
	}

	private final double entropyPerPixelSubTriangle(DoubleLargeArray pDoubleLargeArray,
													final long pWidth,
													final long pHeight,
													final long xl,
													final long yl,
													final long xh,
													final long yh)
	{
		double entropy = 0;
		for (long y = yl; y < yh; y++)
		{
			final long yi = y * pWidth;

			final long xend = xh - y * xh / yh;
			entropy = entropySub(	pDoubleLargeArray,
									xl,
									entropy,
									yi,
									xend);
		}
		entropy = -entropy / (2 * xh * yh);

		return entropy;
	}

	private double entropySub(	DoubleLargeArray pDoubleLargeArray,
								final long xl,
								final double entropy,
								final long yi,
								final long xend)
	{
		double lEntropy = entropy;
		for (long x = xl; x < xend; x++)
		{
			final long i = yi + x;
			final double value = pDoubleLargeArray.getDouble(i);
			if (value > 0)
			{
				lEntropy += value * Math.log(value);
			}
			else if (value < 0)
			{
				lEntropy += -value * Math.log(-value);
			}
		}
		return lEntropy;
	}

	public double getPSFSupportRadius()
	{
		return mPSFSupportRadius;
	}

	public void setPSFSupportRadius(double pPSFSupportRadius)
	{
		mPSFSupportRadius = pPSFSupportRadius;
	}

}
