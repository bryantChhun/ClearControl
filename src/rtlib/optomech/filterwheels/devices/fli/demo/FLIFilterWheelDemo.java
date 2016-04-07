package rtlib.optomech.filterwheels.devices.fli.demo;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import rtlib.core.variable.Variable;
import rtlib.optomech.filterwheels.devices.fli.FLIFilterWheelDevice;

public class FLIFilterWheelDemo
{

	@Test
	public void test() throws InterruptedException
	{
		final FLIFilterWheelDevice lFLIFilterWheelDevice = new FLIFilterWheelDevice("COM25");

		assertTrue(lFLIFilterWheelDevice.open());

		final Variable<Integer> lPositionVariable = lFLIFilterWheelDevice.getPositionVariable();
		final Variable<Integer> lSpeedVariable = lFLIFilterWheelDevice.getSpeedVariable();

		for (int i = 0; i < 10; i++)
		{
			int lTargetPosition = i % 10;
			lPositionVariable.set(lTargetPosition);
			lSpeedVariable.set((i / 30));
			Thread.sleep(30);
			int lCurrentPosition = lPositionVariable.get();
			System.out.format("i=%d, tp=%d, cp=%d\n",
												i,
												lTargetPosition,
												lCurrentPosition);
		}

		assertTrue(lFLIFilterWheelDevice.close());

	}

}
