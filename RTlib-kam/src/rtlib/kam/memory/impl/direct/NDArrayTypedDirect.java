package rtlib.kam.memory.impl.direct;

import java.io.IOException;
import java.nio.channels.FileChannel;

import rtlib.core.memory.SizeOf;
import rtlib.core.memory.SizedInBytes;
import rtlib.core.rgc.Freeable;
import rtlib.kam.memory.ReadWriteBytesFileChannel;
import rtlib.kam.memory.cursor.NDBoundedCursor;
import rtlib.kam.memory.ndarray.NDArrayTyped;
import rtlib.kam.memory.ram.ReadAtAligned;
import rtlib.kam.memory.ram.WriteAtAligned;

public class NDArrayTypedDirect<T> extends NDArrayTyped<T> implements
																													SizedInBytes,
																													ReadWriteBytesFileChannel,
																													WriteAtAligned,
																													ReadAtAligned,
																													Freeable
{

	private NDArrayTypedDirect(	RAMDirect pRAMDirect,
															Class<T> pType,
															NDBoundedCursor pCursor)
	{
		super(pRAMDirect, pType, pCursor);
	}

	public NDArrayTypedDirect(long pElementSizeInBytes,
														Class<T> pType,
														NDBoundedCursor pCursor)
	{
		super(RAMDirect.allocate(pCursor.getLengthInElements() * pElementSizeInBytes),
					pType,
					pCursor);
	}

	public static <T> NDArrayTypedDirect<T> allocateNDArray(RAMDirect pRAM,
																													Class<T> pType,
																													final NDBoundedCursor pCursor)
	{
		return new NDArrayTypedDirect<T>(pRAM, pType, pCursor);
	}

	public static <T> NDArrayTypedDirect<T> allocateTVND(	Class<T> pType,
																												long... pDimensions)
	{
		NDBoundedCursor lNDBoundedCursor = NDBoundedCursor.createNDVectorCursor(pDimensions);
		return new NDArrayTypedDirect<T>(	SizeOf.sizeOf(pType),
																			pType,
																			lNDBoundedCursor);
	}


	public static <T> NDArrayTypedDirect<T> allocateTXYZ(	Class<T> pType,
																												long pWidth,
																												long pHeight,
																												long pDepth)
	{
		NDBoundedCursor lNDBoundedCursor = NDBoundedCursor.createNDVectorCursor(1,
																																						pWidth,
																																						pHeight,
																																						pDepth);
		return new NDArrayTypedDirect<T>(	SizeOf.sizeOf(pType),
																			pType,
																			lNDBoundedCursor);
	}

	public static <T> NDArrayTypedDirect<T> allocateTXY(Class<T> pType,
																											long pWidth,
																											long pHeight)
	{
		return allocateTXYZ(pType, pWidth, pHeight, 1);
	}

	public static <T> NDArrayTypedDirect<T> allocateTX(	Class<T> pType,
																											long pWidth)
	{
		return allocateTXYZ(pType, pWidth, 1, 1);
	}

	public static <T> NDArrayTypedDirect<T> wrapPointerTXYZ(Object pParent,
																													long pNativeAddress,
																													long pLengthInBytes,
																													Class<T> pType,
																													int pWidth,
																													int pHeight,
																													int pDepth)
	{

		NDBoundedCursor lNDBoundedCursor = NDBoundedCursor.createNDVectorCursor(1,
																																						pWidth,
																																						pHeight,
																																						pDepth);
		return new NDArrayTypedDirect<T>(	RAMDirect.wrapPointer(pParent,
																														pNativeAddress,
																														pLengthInBytes),
																			pType,
																			lNDBoundedCursor);
	}

	private RAMDirect getRAMDirect()
	{
		return (RAMDirect) getRAM();
	}

	@Override
	public long getSizeInBytes()
	{
		return getRAMDirect().getSizeInBytes();
	}

	@Override
	public long writeBytesToFileChannel(FileChannel pFileChannel,
																			long pFilePositionInBytes) throws IOException
	{
		return getRAMDirect().writeBytesToFileChannel(pFileChannel,
																									pFilePositionInBytes);
	}

	@Override
	public long writeBytesToFileChannel(long pBufferPositionInBytes,
																			FileChannel pFileChannel,
																			long pFilePositionInBytes,
																			long pLengthInBytes) throws IOException
	{
		return getRAMDirect().writeBytesToFileChannel(pBufferPositionInBytes,
																									pFileChannel,
																									pFilePositionInBytes,
																									pLengthInBytes);
	}

	@Override
	public void readBytesFromFileChannel(	FileChannel pFileChannel,
																				long pFilePositionInBytes,
																				long pLengthInBytes) throws IOException
	{
		getRAMDirect().readBytesFromFileChannel(pFileChannel,
																						pFilePositionInBytes,
																						pLengthInBytes);
	}

	@Override
	public void readBytesFromFileChannel(	long pBufferPositionInBytes,
																				FileChannel pFileChannel,
																				long pFilePositionInBytes,
																				long pLengthInBytes) throws IOException
	{
		getRAMDirect().readBytesFromFileChannel(pBufferPositionInBytes,
																						pFileChannel,
																						pFilePositionInBytes,
																						pLengthInBytes);
	}



	@Override
	public void free()
	{
		mRAM.free();
	}

	@Override
	public boolean isFree()
	{
		return mRAM.isFree();
	}

	@Override
	public String toString()
	{
		return "NDArrayDirect [mDefaultBoundedCursor=" + mDefaultBoundedCursor
						+ ", mRAM="
						+ mRAM
						+ "]";
	}

	public NDArrayTypedDirect<T> sliceMajorAxis(long pSliceIndex)
	{
		// TODO: this is very inelegant way to slice, allocates memory forcefully...
		Class<T> lType = this.getType();
		int lDimension = mDefaultBoundedCursor.getDimension();
		long[] lDimensions = mDefaultBoundedCursor.getDimensions();
		long[] lDimensionsSlice = new long[lDimensions.length - 1];
		for (int i = 0; i < lDimensions.length - 1; i++)
			lDimensionsSlice[i] = lDimensions[i];

		long[] lSliceVector = new long[(int) lDimension];
		lSliceVector[lDimension - 1] = pSliceIndex;

		long lSliceLinearIndex = NDBoundedCursor.getIndex(lDimensions,
																											lSliceVector);

		NDBoundedCursor lNDBoundedCursor = NDBoundedCursor.createNDVectorCursor(lDimensionsSlice);
		RAMDirect lRAM = mRAM.subRegion(lSliceLinearIndex * SizeOf.sizeOf(lType),
																		lNDBoundedCursor.getVolume() * SizeOf.sizeOf(lType));

		NDArrayTypedDirect<T> lNDArraySlice = NDArrayTypedDirect.allocateNDArray(	lRAM,
																																							lType,
																																							lNDBoundedCursor);
		return lNDArraySlice;
	}

}
