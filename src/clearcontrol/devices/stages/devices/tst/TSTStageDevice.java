package clearcontrol.devices.stages.devices.tst;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import aptj.APTJDevice;
import aptj.APTJDeviceFactory;
import aptj.APTJDeviceType;
import aptj.APTJExeption;
import clearcontrol.core.concurrent.timing.WaitingInterface;
import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.device.startstop.StartStopDeviceInterface;
import clearcontrol.core.log.LoggingInterface;
import clearcontrol.core.variable.Variable;
import clearcontrol.devices.stages.StageDeviceBase;
import clearcontrol.devices.stages.StageDeviceInterface;
import clearcontrol.devices.stages.StageType;
import clearcontrol.devices.stages.devices.tst.variables.EnableVariable;
import clearcontrol.devices.stages.devices.tst.variables.HomingVariable;
import clearcontrol.devices.stages.devices.tst.variables.MaxPositionVariable;
import clearcontrol.devices.stages.devices.tst.variables.MinPositionVariable;
import clearcontrol.devices.stages.devices.tst.variables.PositionCurrentVariable;
import clearcontrol.devices.stages.devices.tst.variables.PositionTargetVariable;
import clearcontrol.devices.stages.devices.tst.variables.ReadyVariable;
import clearcontrol.devices.stages.devices.tst.variables.ResetVariable;
import clearcontrol.devices.stages.devices.tst.variables.StopVariable;

/**
 * TST001 stage device
 *
 * @author royer
 */
public class TSTStageDevice extends StageDeviceBase implements
                            StageDeviceInterface,
                            StartStopDeviceInterface,
                            WaitingInterface,
                            LoggingInterface
{

  final APTJDeviceFactory mAPTJDeviceFactory;

  private final BiMap<Integer, APTJDevice> mIndexToDeviceMap =
                                                             HashBiMap.create();

  /**
   * Instantiates an APTJ exception
   * 
   * @throws APTJExeption
   *           exception
   */
  public TSTStageDevice() throws APTJExeption
  {
    super("TST001");
    mAPTJDeviceFactory = new APTJDeviceFactory(APTJDeviceType.TST001);

  }

  @Override
  public StageType getStageType()
  {
    return StageType.Multi;
  }

  @Override
  public boolean open()
  {
    try
    {

      final MachineConfiguration lCurrentMachineConfiguration =
                                                              MachineConfiguration.getCurrentMachineConfiguration();

      final int lNumberOfDevices =
                                 mAPTJDeviceFactory.getNumberOfDevices();

      if (lNumberOfDevices == 0)
        return false;

      for (int lDOFIndex =
                         0; lDOFIndex < lNumberOfDevices; lDOFIndex++)
      {

        final APTJDevice lDevice =
                                 mAPTJDeviceFactory.createDeviceFromIndex(lDOFIndex);

        mIndexToDeviceMap.put(lDOFIndex, lDevice);

        final String lDeviceConfigString = "device.stage.tst001."
                                           + lDevice.getSerialNumber();

        info("Found device: " + lDeviceConfigString);
        final String lDeviceName =
                                 lCurrentMachineConfiguration.getStringProperty(lDeviceConfigString,
                                                                                "");
        if (!lDeviceName.isEmpty())
        {
          info("DOF index: %d, serial number: %sdevice name: %s",
               lDOFIndex,
               lDevice.getSerialNumber(),
               lDeviceName);

          mIndexToNameMap.put(lDOFIndex, lDeviceName);
        }
      }

      for (int lDOFIndex =
                         0; lDOFIndex < lNumberOfDevices; lDOFIndex++)
      {
        APTJDevice lAPTJDevice = mIndexToDeviceMap.get(lDOFIndex);

        mEnableVariables.add(new EnableVariable("Enable"
                                                + mIndexToNameMap.get(lDOFIndex),
                                                lAPTJDevice));

        mReadyVariables.add(new ReadyVariable("Ready"
                                              + mIndexToNameMap.get(lDOFIndex),
                                              lAPTJDevice));

        mHomingVariables.add(new HomingVariable("Homing"
                                                + mIndexToNameMap.get(lDOFIndex),
                                                lAPTJDevice));

        mStopVariables.add(new StopVariable("Stop"
                                            + mIndexToNameMap.get(lDOFIndex),
                                            lAPTJDevice));

        mResetVariables.add(new ResetVariable("Reset"
                                              + mIndexToNameMap.get(lDOFIndex),
                                              lAPTJDevice));

        mTargetPositionVariables.add(new PositionTargetVariable("TargetPosition"
                                                                + mIndexToNameMap.get(lDOFIndex),
                                                                lAPTJDevice));

        mCurrentPositionVariables.add(new PositionCurrentVariable("CurrentPosition"
                                                                  + mIndexToNameMap.get(lDOFIndex),
                                                                  lAPTJDevice));

        mMinPositionVariables.add(new MinPositionVariable("MinPosition"
                                                          + mIndexToNameMap.get(lDOFIndex),
                                                          lAPTJDevice));

        mMaxPositionVariables.add(new MaxPositionVariable("MaxPosition"
                                                          + mIndexToNameMap.get(lDOFIndex),
                                                          lAPTJDevice));

        mGranularityPositionVariables.add(new Variable<Double>("GranularityPosition"
                                                               + mIndexToNameMap.get(lDOFIndex),
                                                               0d));
      }

      return true;
    }

    catch (final Exception e)
    {
      e.printStackTrace();
      return false;
    }

  }

  @Override
  public boolean start()
  {
    return true;
  }

  @Override
  public boolean stop()
  {
    return true;
  }

  @Override
  public boolean close()
  {
    try
    {
      mAPTJDeviceFactory.close();
      return true;
    }
    catch (final Exception e)
    {
      return false;
    }
  }

  @Override
  public String toString()
  {
    return String.format("TSTStageDevice [mAPTJDeviceFactory=%s, mIndexToDeviceMap=%s]",
                         mAPTJDeviceFactory,
                         mIndexToDeviceMap);
  }

}
