package clearcontrol.microscope.lightsheet.acquisition;

import static java.lang.Math.floor;
import static java.lang.Math.round;

import java.util.Iterator;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.device.name.NameableAbstract;
import clearcontrol.hardware.lasers.LaserDeviceInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeInterface;
import clearcontrol.microscope.lightsheet.acquisition.tables.InterpolationTables;
import clearcontrol.microscope.lightsheet.component.detection.DetectionArmInterface;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheetInterface;
import clearcontrol.microscope.state.AcquisitionStateInterface;

public class InterpolatedAcquisitionState extends NameableAbstract implements
																																	AcquisitionStateInterface<LightSheetMicroscopeInterface>
{

	private BoundedVariable<Number> mStageX = new BoundedVariable<Number>("StageX",
																																				25.0);

	private BoundedVariable<Number> mStageY = new BoundedVariable<Number>("StageY",
																																				25.0);

	private BoundedVariable<Number> mStageZ = new BoundedVariable<Number>("StageZ",
																																				25.0);

	private final BoundedVariable<Number> mXLow = new BoundedVariable<Number>("LowX",
																																						25.0);
	private final BoundedVariable<Number> mXHigh = new BoundedVariable<Number>(	"HighX",
																																							75.0);

	private final BoundedVariable<Number> mYLow = new BoundedVariable<Number>("LowY",
																																						25.0);
	private final BoundedVariable<Number> mYHigh = new BoundedVariable<Number>(	"HighY",
																																							75.0);

	private final BoundedVariable<Number> mZLow = new BoundedVariable<Number>("LowZ",
																																						25.0);
	private final BoundedVariable<Number> mZHigh = new BoundedVariable<Number>(	"HighZ",
																																							75.0);

	private final BoundedVariable<Number> mZStep = new BoundedVariable<Number>(	"ZStep",
																																							0.5,
																																							0,
																																							1000);

	private volatile InterpolationTables mInterpolationTable;

	public InterpolatedAcquisitionState(String pName,
																			LightSheetMicroscopeInterface pMicroscope)
	{
		super(pName);

		int lNumberOfDetectionArmDevices = 2;
		int lNumberOfLightSheetDevices = 4;
		if (pMicroscope != null)
		{
			lNumberOfDetectionArmDevices = pMicroscope.getDeviceLists()
																								.getNumberOfDevices(DetectionArmInterface.class);
			lNumberOfLightSheetDevices = pMicroscope.getDeviceLists()
																							.getNumberOfDevices(LightSheetInterface.class);
		}

		mInterpolationTable = new InterpolationTables(lNumberOfDetectionArmDevices,
																									lNumberOfLightSheetDevices);

		resetBounds();
	}

	public InterpolatedAcquisitionState(String pName,
																			int pNumberOfDetectionArmDevices,
																			int pNumberOfLightSheetDevices)
	{
		super(pName);

		mInterpolationTable = new InterpolationTables(pNumberOfDetectionArmDevices,
																									pNumberOfLightSheetDevices);

		resetBounds();
	}

	public InterpolatedAcquisitionState(String pName,
																			InterpolatedAcquisitionState pInterpolatedAcquisitionState)
	{
		super(pName);

		mInterpolationTable = new InterpolationTables(pInterpolatedAcquisitionState.mInterpolationTable);

		resetBounds();
	}

	public void resetBounds()
	{

		// TODO: get bounds

	}

	public void setup(double pLowZ,
										double pMiddleZ,
										double pHighZ,
										double pStepZ,
										double pControlPlaneStepZ,
										double pMarginZ)
	{
		getStackZLowVariable().set(pLowZ);
		getStackZHighVariable().set(pHighZ);
		getStackZStepVariable().set(pStepZ);
		mInterpolationTable.setTransitionPlane(pMiddleZ);

		for (double z = pLowZ + pMarginZ; z <= pHighZ - pMarginZ; z += pControlPlaneStepZ)
		{
			mInterpolationTable.addControlPlane(z);
		}

	}

	public double getStackDepthInMicrons()
	{
		return (mZHigh.get().doubleValue() - mZLow.get().doubleValue());
	}

	public void setStackDepth(int pNumberOfPlanes)
	{
		double lStepZ = (getStackDepthInMicrons() / (pNumberOfPlanes));

		getStackZHighVariable().set(getStackZLowVariable().get()
																											.doubleValue() + pNumberOfPlanes
																* lStepZ);
		getStackZStepVariable().set(lStepZ);
	}

	public int getStackDepth()
	{
		return (int) floor(getStackDepthInMicrons() / mZStep.get()
																												.doubleValue());
	}

	@Override
	public void applyState(LightSheetMicroscopeInterface pLightSheetMicroscopeInterface)
	{
		// addStackMargin();
		pLightSheetMicroscopeInterface.clearQueue();
		applyStagePosition(pLightSheetMicroscopeInterface);
		for (int lIndex = 0; lIndex < getStackDepth(); lIndex++)
		{
			applyStateAtStackPlane(pLightSheetMicroscopeInterface, lIndex);
		}
		// addStackMargin();
	}

	public void applyStagePosition(LightSheetMicroscopeInterface pLightSheetMicroscopeInterface)
	{
		double lStageX = getStageXVariable().get().doubleValue();
		double lStageY = getStageYVariable().get().doubleValue();
		double lStageZ = getStageZVariable().get().doubleValue();

		pLightSheetMicroscopeInterface.setStageX(lStageX);
		pLightSheetMicroscopeInterface.setStageY(lStageY);
		pLightSheetMicroscopeInterface.setStageZ(lStageZ);
	}

	public void applyStateAtStackPlane(	LightSheetMicroscopeInterface pLightSheetMicroscopeInterface,
																			int pPlaneIndex)
	{
		final int lNumberOfDetectionPathDevices = pLightSheetMicroscopeInterface.getDeviceLists()
																																						.getNumberOfDevices(DetectionArmInterface.class);

		for (int d = 0; d < lNumberOfDetectionPathDevices; d++)
		{
			pLightSheetMicroscopeInterface.setDZ(d, getDZ(pPlaneIndex, d));
		}

		final int lNumberOfLightsheetDevices = pLightSheetMicroscopeInterface.getDeviceLists()
																																					.getNumberOfDevices(LightSheetInterface.class);

		for (int l = 0; l < lNumberOfLightsheetDevices; l++)
		{
			pLightSheetMicroscopeInterface.setIX(l, getIX(pPlaneIndex, l));
			pLightSheetMicroscopeInterface.setIY(l, getIY(pPlaneIndex, l));
			pLightSheetMicroscopeInterface.setIZ(l, getIZ(pPlaneIndex, l));

			pLightSheetMicroscopeInterface.setIA(l, getIA(pPlaneIndex, l));
			pLightSheetMicroscopeInterface.setIB(l, getIB(pPlaneIndex, l));
			pLightSheetMicroscopeInterface.setIW(l, getIW(pPlaneIndex, l));
			pLightSheetMicroscopeInterface.setIH(l, getIH(pPlaneIndex, l));
			pLightSheetMicroscopeInterface.setIP(l, getIP(pPlaneIndex, l));
		}

		final int lNumberOfLaserDevices = pLightSheetMicroscopeInterface.getDeviceLists()
																																		.getNumberOfDevices(LaserDeviceInterface.class);

		for (int i = 0; i < lNumberOfLaserDevices; i++)
		{
			pLightSheetMicroscopeInterface.setIP(i, getIP(pPlaneIndex, i));
		}

	}

	public void applyStateAtControlPlane(	LightSheetMicroscopeInterface pLightSheetMicroscopeInterface,
																				int pControlPlaneIndex)
	{
		double lControlPlaneZ = getControlPlaneZ(pControlPlaneIndex);
		int lStackPlaneIndex = getPlaneIndexForZRamp(lControlPlaneZ);
		applyStateAtStackPlane(	pLightSheetMicroscopeInterface,
														lStackPlaneIndex);
	}

	public void addStackMargin(	LightSheetMicroscopeInterface pLightSheetMicroscopeInterface,
															int pNumberOfMarginPlanesToAdd)
	{
		addStackMargin(	pLightSheetMicroscopeInterface,
										0,
										pNumberOfMarginPlanesToAdd);
	}

	public void addStackMargin(	LightSheetMicroscopeInterface pLightSheetMicroscopeInterface,
															int pStackPlaneIndex,
															int pNumberOfMarginPlanesToAdd)
	{
		applyStateAtStackPlane(	pLightSheetMicroscopeInterface,
														pStackPlaneIndex);
		pLightSheetMicroscopeInterface.setC(false);
		pLightSheetMicroscopeInterface.setILO(false);
		for (int i = 0; i < pNumberOfMarginPlanesToAdd; i++)
			pLightSheetMicroscopeInterface.addCurrentStateToQueue();
	}

	public double getControlPlaneZ(int pControlPlaneIndex)
	{
		return mInterpolationTable.getZ(pControlPlaneIndex);
	}

	public double getZRamp(int pPlaneIndex)
	{
		final double lZ = mZLow.get().doubleValue() + pPlaneIndex
											* getStackZStepVariable().get().doubleValue();
		return lZ;
	}

	public int getPlaneIndexForZRamp(double pZRampValue)
	{
		double lZStep = getStackZStepVariable().get().doubleValue();
		double lAdjustedZRamp = pZRampValue - mZLow.get().doubleValue();
		final int lIndex = (int) round(lAdjustedZRamp / lZStep);
		return lIndex;
	}

	public double getDZ(int pPlaneIndex, int pDeviceIndex)
	{
		final double lRamp = getZRamp(pPlaneIndex);
		final double lInterpolatedValue = mInterpolationTable.getInterpolatedDZ(pDeviceIndex,
																																						lRamp);
		return lRamp + lInterpolatedValue;
	}

	public double getIX(int pPlaneIndex, int pDeviceIndex)
	{
		final double lRamp = getZRamp(pPlaneIndex);
		final double lInterpolatedValue = mInterpolationTable.getInterpolatedIX(pDeviceIndex,
																																						lRamp);
		return lInterpolatedValue;
	}

	public double getIY(int pPlaneIndex, int pDeviceIndex)
	{
		final double lRamp = getZRamp(pPlaneIndex);
		final double lInterpolatedValue = mInterpolationTable.getInterpolatedIY(pDeviceIndex,
																																						lRamp);
		return lInterpolatedValue;
	}

	public double getIZ(int pPlaneIndex, int pDeviceIndex)
	{
		final double lRamp = getZRamp(pPlaneIndex);
		final double lInterpolatedValue = mInterpolationTable.getInterpolatedIZ(pDeviceIndex,
																																						lRamp);
		return lRamp + lInterpolatedValue;
	}

	public double getIA(int pPlaneIndex, int pDeviceIndex)
	{
		final double lRamp = getZRamp(pPlaneIndex);
		final double lInterpolatedValue = mInterpolationTable.getInterpolatedIA(pDeviceIndex,
																																						lRamp);

		return lInterpolatedValue;
	}

	public double getIB(int pPlaneIndex, int pDeviceIndex)
	{
		final double lRamp = getZRamp(pPlaneIndex);
		final double lInterpolatedValue = mInterpolationTable.getInterpolatedIB(pDeviceIndex,
																																						lRamp);

		return lInterpolatedValue;
	}

	public double getIW(int pPlaneIndex, int pDeviceIndex)
	{
		final double lRamp = getZRamp(pPlaneIndex);
		final double lInterpolatedValue = mInterpolationTable.getInterpolatedIW(pDeviceIndex,
																																						lRamp);

		return lInterpolatedValue;
	}

	public double getIH(int pPlaneIndex, int pDeviceIndex)
	{
		final double lRamp = getZRamp(pPlaneIndex);
		final double lInterpolatedValue = mInterpolationTable.getInterpolatedIH(pDeviceIndex,
																																						lRamp);

		return lInterpolatedValue;
	}

	public double getIP(int pPlaneIndex, int pDeviceIndex)
	{
		final double lRamp = getZRamp(pPlaneIndex);
		final double lInterpolatedValue = mInterpolationTable.getInterpolatedIP(pDeviceIndex,
																																						lRamp);

		return lInterpolatedValue;
	}

	public int getBestDetectionArm(int pControlPlaneIndex)
	{

		double lTransitionPlane = mInterpolationTable.getTransitionPlane();

		if (getZRamp(pControlPlaneIndex) <= lTransitionPlane)
			return 0;
		else if (getZRamp(pControlPlaneIndex) >= lTransitionPlane)
			return 1;

		return -1;

	}

	public BoundedVariable<Number> getStackXLowVariable()
	{
		return mXLow;
	}

	public BoundedVariable<Number> getStackXHighVariable()
	{
		return mXHigh;
	}

	public BoundedVariable<Number> getStackYLowVariable()
	{
		return mYLow;
	}

	public BoundedVariable<Number> getStackYHighVariable()
	{
		return mYHigh;
	}

	public BoundedVariable<Number> getStackZLowVariable()
	{
		return mZLow;
	}

	public BoundedVariable<Number> getStackZHighVariable()
	{
		return mZHigh;
	}

	public BoundedVariable<Number> getStackZStepVariable()
	{
		return mZStep;
	}

	public int getNumberOfControlPlanes()
	{
		return mInterpolationTable.getNumberOfControlPlanes();
	}

	public BoundedVariable<Number> getStageXVariable()
	{
		return mStageX;
	}

	public BoundedVariable<Number> getStageYVariable()
	{
		return mStageY;
	}

	public BoundedVariable<Number> getStageZVariable()
	{
		return mStageZ;
	}

}