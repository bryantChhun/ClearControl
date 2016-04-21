package rtlib.hardware.stages.devices.ecc100.variables;

import ecc100.ECC100Axis;
import rtlib.core.variable.Variable;

public class StopVariable extends Variable<Boolean>
{

	private final ECC100Axis mECC100Axis;

	public StopVariable(String pVariableName, ECC100Axis pECC100Axis)
	{
		super(pVariableName, false);
		mECC100Axis = pECC100Axis;
	}

	@Override
	public Boolean setEventHook(Boolean pOldValue, Boolean pNewValue)
	{
		final Boolean lValue = super.setEventHook(pOldValue, pNewValue);
		mECC100Axis.stop();
		return lValue;
	}

	@Override
	public Boolean getEventHook(Boolean pCurrentValue)
	{
		return super.getEventHook(pCurrentValue);
	}
}