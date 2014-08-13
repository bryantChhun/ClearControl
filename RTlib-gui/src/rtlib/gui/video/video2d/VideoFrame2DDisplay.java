package rtlib.gui.video.video2d;

import java.io.IOException;

import rtlib.core.concurrent.asyncprocs.AsynchronousProcessorBase;
import rtlib.core.device.NamedVirtualDevice;
import rtlib.core.variable.booleanv.BooleanVariable;
import rtlib.core.variable.doublev.DoubleVariable;
import rtlib.core.variable.objectv.ObjectVariable;
import rtlib.gui.video.video2d.jogl.VideoWindow;
import rtlib.stack.Stack;

public class VideoFrame2DDisplay<T> extends NamedVirtualDevice
{
	private final VideoWindow mVideoWindow;

	private final ObjectVariable<Stack<T>> mInputStackVariable;

	private final BooleanVariable mDisplayOn;
	private final BooleanVariable mManualMinMaxIntensity;
	private final DoubleVariable mMinimumIntensity;
	private final DoubleVariable mMaximumIntensity;

	private AsynchronousProcessorBase<Stack<T>, Object> mAsynchronousDisplayUpdater;

	public VideoFrame2DDisplay()
	{
		this("2D Video Display", 512, 512, 1);
	}

	public VideoFrame2DDisplay(	final int pVideoWidth,
															final int pVideoHeight)
	{
		this("2D Video Display", pVideoWidth, pVideoHeight, 10);
	}

	public VideoFrame2DDisplay(	final String pWindowName,
															final int pVideoWidth,
															final int pVideoHeight)
	{
		this(pWindowName, pVideoWidth, pVideoHeight, 10);
	}

	public VideoFrame2DDisplay(	final String pWindowName,
															final int pVideoWidth,
															final int pVideoHeight,
															final int pUpdaterQueueLength)
	{
		super(pWindowName);

		mVideoWindow = new VideoWindow(	pWindowName,
																		pVideoWidth,
																		pVideoHeight);

		mAsynchronousDisplayUpdater = new AsynchronousProcessorBase<Stack<T>, Object>("AsynchronousDisplayUpdater",
																																									pUpdaterQueueLength)
		{
			@Override
			public Object process(final Stack<T> pStack)
			{
				// TODO: need to add method that handles RAMdirect sources!
				mVideoWindow.setSourceBuffer(pStack.getNDArray());
				mVideoWindow.setWidth((int) pStack.getWidth());
				mVideoWindow.setHeight((int) pStack.getHeight());
				mVideoWindow.notifyNewFrame();

				mVideoWindow.display();
				pStack.releaseStack();
				return null;
			}
		};

		mAsynchronousDisplayUpdater.start();

		mInputStackVariable = new ObjectVariable<Stack<T>>(pWindowName)
		{

			@Override
			public Stack<T> setEventHook(	final Stack<T> pOldStack,
																		final Stack<T> pNewStack)
			{
				if (!mAsynchronousDisplayUpdater.passOrFail(pNewStack))
				{
					pNewStack.releaseStack();
				}
				return super.setEventHook(pOldStack, pNewStack);
			}

		};

		mDisplayOn = new BooleanVariable("DisplayOn", true)
		{
			@Override
			public double setEventHook(	final double pOldValue,
																	final double pNewValue)
			{
				final boolean lDisplayOn = BooleanVariable.double2boolean(pNewValue);
				mVideoWindow.setDisplayOn(lDisplayOn);
				return super.setEventHook(pOldValue, pNewValue);
			}
		};

		mManualMinMaxIntensity = new BooleanVariable(	"ManualMinMaxIntensity",
																									false)
		{
			@Override
			public double setEventHook(	final double pOldValue,
																	final double pNewValue)
			{
				final boolean lManualMinMax = BooleanVariable.double2boolean(pNewValue);
				mVideoWindow.setManualMinMax(lManualMinMax);
				return super.setEventHook(pOldValue, pNewValue);
			}
		};

		mMinimumIntensity = new DoubleVariable("MinimumIntensity", 0)
		{
			@Override
			public double setEventHook(	final double pOldValue,
																	final double pNewMinIntensity)
			{
				final double lMinIntensity = Math.pow(pNewMinIntensity, 6);
				mVideoWindow.setMinIntensity(lMinIntensity);
				return super.setEventHook(pOldValue, pNewMinIntensity);
			}
		};

		mMaximumIntensity = new DoubleVariable("MaximumIntensity", 1)
		{
			@Override
			public double setEventHook(	final double pOldValue,
																	final double pNewMaxIntensity)
			{
				final double lMaxIntensity = Math.pow(pNewMaxIntensity, 6);
				mVideoWindow.setMaxIntensity(lMaxIntensity);
				return super.setEventHook(pOldValue, pNewMaxIntensity);
			}
		};
	}

	public BooleanVariable getDisplayOnVariable()
	{
		return mDisplayOn;
	}

	public BooleanVariable getManualMinMaxIntensityOnVariable()
	{
		return mManualMinMaxIntensity;
	}

	public DoubleVariable getMinimumIntensityVariable()
	{
		return mMinimumIntensity;
	}

	public DoubleVariable getMaximumIntensityVariable()
	{
		return mMaximumIntensity;
	}

	public ObjectVariable<Stack<T>> getFrameReferenceVariable()
	{
		return mInputStackVariable;
	}

	public void setVisible(final boolean pIsVisible)
	{
		mVideoWindow.setVisible(pIsVisible);
	}

	public void setLinearInterpolation(final boolean pLinearInterpolation)
	{
		mVideoWindow.setLinearInterpolation(pLinearInterpolation);
	}

	public void setSyncToRefresh(final boolean pSyncToRefresh)
	{
		mVideoWindow.setSyncToRefresh(pSyncToRefresh);
	}

	@Override
	public boolean open()
	{
		setVisible(true);
		return true;
	}

	@Override
	public boolean close()
	{
		setVisible(false);
		try
		{
			mVideoWindow.close();
			return true;
		}
		catch (final IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean start()
	{

		mDisplayOn.setValue(true);
		return true;
	}

	@Override
	public boolean stop()
	{

		mDisplayOn.setValue(false);
		return true;
	}

	public void disableClose()
	{
		mVideoWindow.disableClose();
	}

}