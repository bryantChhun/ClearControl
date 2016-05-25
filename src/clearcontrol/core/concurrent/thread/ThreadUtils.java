package clearcontrol.core.concurrent.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class ThreadUtils
{
	public static final void sleep(long pTime, TimeUnit pTimeUnit)
	{
		final long lStart = System.nanoTime();
		long lDeadlineInNanos = lStart + pTimeUnit.toNanos(pTime);

		boolean lSleepTimeBelowMillisecond = pTimeUnit.toMillis(pTime) == 0;

		long lNanoTime;
		while ((lNanoTime = System.nanoTime()) < lDeadlineInNanos)
		{

			try
			{
				if (lSleepTimeBelowMillisecond)
				{
					long lTimeToWaitInNanos = 3 * (lDeadlineInNanos - lNanoTime) / 4;
					if (lTimeToWaitInNanos > 0)
						Thread.sleep(0, (int) lTimeToWaitInNanos);
				}
				else
				{
					long lTimeToWaitInNanos = 3 * (lDeadlineInNanos - lNanoTime) / 4;
					if (lTimeToWaitInNanos > 0)
					{
						long lTimeToWaitInMillis = TimeUnit.NANOSECONDS.toMillis(lTimeToWaitInNanos);

						Thread.sleep(	lTimeToWaitInMillis,
													(int) (lTimeToWaitInNanos % 1000000L));
					}
				}
			}
			catch (InterruptedException e)
			{
			}
		}
	}

	public static final void sleepWhile(long pTime,
																			TimeUnit pTimeUnit,
																			Callable<Boolean> pCondition)
	{
		final long lStart = System.nanoTime();
		long lDeadlineInNanos = lStart + pTimeUnit.toNanos(pTime);

		boolean lSleepTimeBelowMillisecond = pTimeUnit.toMillis(pTime) == 0;

		long lNanoTime;
		while ((lNanoTime = System.nanoTime()) < lDeadlineInNanos)
		{

			try
			{
				if (!pCondition.call())
					break;

				if (lSleepTimeBelowMillisecond)
				{
					long lTimeToWaitInNanos = (lDeadlineInNanos - lNanoTime) / 4;
					if (lTimeToWaitInNanos > 0)
						Thread.sleep(0, (int) lTimeToWaitInNanos);
				}
				else
				{
					long lTimeToWaitInNanos = (lDeadlineInNanos - lNanoTime) % 1000000;
					if (lTimeToWaitInNanos > 0)
					{
						long lTimeToWaitInMillis = TimeUnit.NANOSECONDS.toMillis(lTimeToWaitInNanos);

						Thread.sleep(	lTimeToWaitInMillis,
													(int) (lTimeToWaitInNanos % 1000000L));
					}
				}
			}
			catch (InterruptedException e)
			{
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
