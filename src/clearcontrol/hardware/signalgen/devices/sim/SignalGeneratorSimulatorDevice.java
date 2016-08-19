package clearcontrol.hardware.signalgen.devices.sim;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import clearcontrol.core.concurrent.thread.ThreadUtils;
import clearcontrol.core.variable.Variable;
import clearcontrol.hardware.signalgen.SignalGeneratorBase;
import clearcontrol.hardware.signalgen.SignalGeneratorInterface;
import clearcontrol.hardware.signalgen.score.ScoreInterface;

public class SignalGeneratorSimulatorDevice	extends
																						SignalGeneratorBase	implements
																																SignalGeneratorInterface
{

	public SignalGeneratorSimulatorDevice()
	{
		super(SignalGeneratorSimulatorDevice.class.getSimpleName());

	}

	@Override
	public boolean open()
	{
		return true;
	}

	@Override
	public boolean close()
	{
		return true;
	}

	@Override
	public boolean playScore(ScoreInterface pScore)
	{
		final long lDurationInMilliseconds = pScore.getDuration(TimeUnit.MILLISECONDS);

		long ltriggerPeriodInMilliseconds = lDurationInMilliseconds/mEnqueuedStateCounter;
		
		for(int i=0; i<mEnqueuedStateCounter; i++)
		{
			mTriggerVariable.setEdge(false, true);
			ThreadUtils.sleep(ltriggerPeriodInMilliseconds, TimeUnit.MILLISECONDS);
		}
		
		return true;
	}

	@Override
	public Future<Boolean> playQueue()
	{
		return super.playQueue();
	}

	@Override
	public double getTemporalGranularityInMicroseconds()
	{
		return 0;
	}

	@Override
	public Variable<Boolean> getTriggerVariable()
	{
		return mTriggerVariable;
	}

}
