package rtlib.stages.hub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rtlib.core.device.NamedVirtualDevice;
import rtlib.core.variable.booleanv.BooleanVariable;
import rtlib.core.variable.doublev.DoubleVariable;
import rtlib.stages.StageDeviceInterface;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class StageDeviceHub extends NamedVirtualDevice implements
																								StageDeviceInterface
{

	private ArrayList<StageDeviceInterface> mStageDeviceInterfaceList = new ArrayList<StageDeviceInterface>();
	private ArrayList<StageDeviceDOF> mDOFList = new ArrayList<StageDeviceDOF>();
	private BiMap<String, StageDeviceDOF> mNameToStageDeviceDOFMap = HashBiMap.create();

	public StageDeviceHub(String pDeviceName)
	{
		super(pDeviceName);
	}

	public String addDOF(	StageDeviceInterface pStageDeviceInterface,
												int pDOFIndex)
	{
		mStageDeviceInterfaceList.add(pStageDeviceInterface);
		String lDOFName = pStageDeviceInterface.getDOFNameByIndex(pDOFIndex);
		StageDeviceDOF lStageDeviceDOF = new StageDeviceDOF(pStageDeviceInterface,
																												pDOFIndex);
		mDOFList.add(lStageDeviceDOF);
		mNameToStageDeviceDOFMap.put(lDOFName, lStageDeviceDOF);
		return lDOFName;
	}

	public List<StageDeviceDOF> getDOFList()
	{
		return Collections.unmodifiableList(mDOFList);
	}

	@Override
	public boolean open()
	{
		boolean lOpen = true;
		for (StageDeviceInterface lStageDeviceInterface : mStageDeviceInterfaceList)
			lOpen &= lStageDeviceInterface.open();
		return lOpen;
	}

	@Override
	public boolean start()
	{
		boolean lStart = true;
		for (StageDeviceInterface lStageDeviceInterface : mStageDeviceInterfaceList)
			lStart &= lStageDeviceInterface.start();
		return lStart;
	}

	@Override
	public boolean stop()
	{
		boolean lStop = true;
		for (StageDeviceInterface lStageDeviceInterface : mStageDeviceInterfaceList)
			lStop &= lStageDeviceInterface.stop();
		return lStop;
	}

	@Override
	public boolean close()
	{
		boolean lClose = true;
		for (StageDeviceInterface lStageDeviceInterface : mStageDeviceInterfaceList)
			lClose &= lStageDeviceInterface.close();
		return lClose;
	}

	@Override
	public int getNumberOfDOFs()
	{
		int lNumberOFDOFs = mDOFList.size();
		return lNumberOFDOFs;
	}

	@Override
	public int getDOFIndexByName(String pName)
	{
		StageDeviceDOF lStageDeviceDOF = mNameToStageDeviceDOFMap.get(pName);
		int lIndex = mDOFList.indexOf(lStageDeviceDOF);
		return lIndex;
	}

	@Override
	public String getDOFNameByIndex(int pDOFIndex)
	{
		StageDeviceDOF lStageDeviceDOF = mDOFList.get(pDOFIndex);
		String lName = lStageDeviceDOF.getName();
		return lName;
	}

	@Override
	public void home(int pDOFIndex)
	{
		mDOFList.get(pDOFIndex).home();
	}

	@Override
	public void enable(int pDOFIndex)
	{
		mDOFList.get(pDOFIndex).enable();
	}

	@Override
	public double getCurrentPosition(int pDOFIndex)
	{
		return mDOFList.get(pDOFIndex).getCurrentPosition();
	}

	@Override
	public void goToPosition(int pDOFIndex, double pValue)
	{
		mDOFList.get(pDOFIndex).goToPosition(pValue);
	}

	@Override
	public Boolean waitToBeReady(	int pDOFIndex,
																int pTimeOut,
																TimeUnit pTimeUnit)
	{
		return mDOFList.get(pDOFIndex).waitToBeReady(pTimeOut,
																									pTimeUnit);
	}

	@Override
	public DoubleVariable getMinPositionVariable(int pDOFIndex)
	{
		return mDOFList.get(pDOFIndex).getMinPositionVariable();
	}

	@Override
	public DoubleVariable getMaxPositionVariable(int pDOFIndex)
	{
		return mDOFList.get(pDOFIndex).getMaxPositionVariable();
	}

	@Override
	public DoubleVariable getEnableVariable(int pDOFIndex)
	{
		return mDOFList.get(pDOFIndex).getEnableVariable();
	}

	@Override
	public DoubleVariable getPositionVariable(int pDOFIndex)
	{
		return mDOFList.get(pDOFIndex).getPositionVariable();
	}

	@Override
	public DoubleVariable getReadyVariable(int pDOFIndex)
	{
		return mDOFList.get(pDOFIndex).getReadyVariable();
	}

	@Override
	public DoubleVariable getHomingVariable(int pDOFIndex)
	{
		return mDOFList.get(pDOFIndex).getHomingVariable();
	}

	@Override
	public BooleanVariable getStopVariable(int pDOFIndex)
	{
		return mDOFList.get(pDOFIndex).getStopVariable();
	}

	@Override
	public String toString()
	{
		return "StageHub [mDOFList=" + mDOFList
						+ ", getNumberOfDOFs()="
						+ getNumberOfDOFs()
						+ "]";
	}

}
