package rtlib.microscope.lsm.component.lightsheet;

import rtlib.core.configuration.MachineConfiguration;
import rtlib.core.device.NamedVirtualDevice;
import rtlib.core.device.SwitchingDeviceInterface;
import rtlib.core.variable.Variable;
import rtlib.core.variable.VariableSetListener;
import rtlib.optomech.OptoMechDeviceInterface;
import rtlib.symphony.movement.Movement;
import rtlib.symphony.staves.ConstantStave;

public class LightSheetSwitch extends NamedVirtualDevice implements
																												SwitchingDeviceInterface,
																												OptoMechDeviceInterface
{

	private final Variable<Boolean>[] mLightSheetOnOff;
	private final ConstantStave[] mBitStave;
	private int[] mStaveIndex;

	public LightSheetSwitch(String pName, int pNumberOfLightSheets)
	{
		super(pName);

		reset();

		final VariableSetListener<Boolean> lBooleanVariableListener = (	u,
																																		v) -> {
			update();
		};

		mBitStave = new ConstantStave[pNumberOfLightSheets];
		mStaveIndex = new int[pNumberOfLightSheets];
		mLightSheetOnOff = new Variable[pNumberOfLightSheets];

		for (int i = 0; i < mBitStave.length; i++)
		{
			mStaveIndex[i] = MachineConfiguration.getCurrentMachineConfiguration()
																						.getIntegerProperty("device.lsm.selector." + getName()
																																		+ i
																																		+ ".index",
																																-1);
			mBitStave[i] = new ConstantStave("lightsheet.s." + i, 0);

			mLightSheetOnOff[i] = new Variable<Boolean>(String.format("LightSheet%dOnOff",
																																			i),
																												false);
			mLightSheetOnOff[i].addSetListener(lBooleanVariableListener);

		}

	}

	@Override
	public int getNumberOfSwitches()
	{
		return mLightSheetOnOff.length;
	}

	public void reset()
	{

	}

	@Override
	public Variable<Boolean> getSwitchingVariable(int pLightSheetIndex)
	{
		return mLightSheetOnOff[pLightSheetIndex];
	}

	public void addStavesToBeforeExposureMovement(Movement pBeforeExposureMovement)
	{
		// Analog outputs before exposure:
		for (int i = 0; i < mBitStave.length; i++)
		{
			pBeforeExposureMovement.setStave(mStaveIndex[i], mBitStave[i]);
		}
	}

	public void addStavesToExposureMovement(Movement pExposureMovement)
	{
		// Analog outputs at exposure:
		for (int i = 0; i < mBitStave.length; i++)
		{
			pExposureMovement.setStave(mStaveIndex[i], mBitStave[i]);
		}
	}

	public void update()
	{
		synchronized (this)
		{
			for (int i = 0; i < mBitStave.length; i++)
			{
				mBitStave[i].setValue(mLightSheetOnOff[i].get() ? 1 : 0);
			}
		}
	}

}
