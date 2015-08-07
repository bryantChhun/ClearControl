package rtlib.stages.devices.ecc100.variables;

import ecc100.ECC100Axis;
import rtlib.core.variable.types.booleanv.BooleanVariable;

public class HomingVariable extends BooleanVariable
{

	private final ECC100Axis mECC100Axis;

	public HomingVariable(String pVariableName, ECC100Axis pECC100Axis)
	{
		super(pVariableName, false);
		mECC100Axis = pECC100Axis;
	}

	@Override
	public Double setEventHook(Double pOldValue, Double pNewValue)
	{
		final double lValue = super.setEventHook(pOldValue, pNewValue);
		mECC100Axis.home();
		return lValue;
	}

	@Override
	public Double getEventHook(Double pCurrentValue)
	{
		return super.getEventHook(pCurrentValue);
	}
}
