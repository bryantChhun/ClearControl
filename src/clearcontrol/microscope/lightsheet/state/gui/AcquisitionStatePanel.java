package clearcontrol.microscope.lightsheet.state.gui;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.onoffarray.OnOffArrayPane;
import clearcontrol.gui.jfx.var.rangeslider.VariableRangeSlider;
import clearcontrol.gui.jfx.var.slider.VariableSlider;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Acquisition state panel
 *
 * @author royer
 */
public class AcquisitionStatePanel extends CustomGridPane
{

  /**
   * Acquisition state
   * 
   * @param pAcquisitionState
   *          acquisition state
   */
  public AcquisitionStatePanel(InterpolatedAcquisitionState pAcquisitionState)
  {
    super();

    BoundedVariable<Number> lStageXVariable =
                                            pAcquisitionState.getStageXVariable();
    BoundedVariable<Number> lStageYVariable =
                                            pAcquisitionState.getStageYVariable();
    BoundedVariable<Number> lStageZVariable =
                                            pAcquisitionState.getStageZVariable();

    VariableSlider<Number> lStageXSlider =
                                         new VariableSlider<Number>("stageX",
                                                                    lStageXVariable,
                                                                    5);

    VariableSlider<Number> lStageYSlider =
                                         new VariableSlider<Number>("stageY",
                                                                    lStageYVariable,
                                                                    5);

    VariableSlider<Number> lStageZSlider =
                                         new VariableSlider<Number>("stageZ",
                                                                    lStageZVariable,
                                                                    5);

    // Collecting variables:

    BoundedVariable<Number> lZLow =
                                  pAcquisitionState.getStackZLowVariable();
    BoundedVariable<Number> lZHigh =
                                   pAcquisitionState.getStackZHighVariable();

    Variable<Number> lZStep =
                            pAcquisitionState.getStackZStepVariable();

    Variable<Number> lNumberOfPlanes =
                                     pAcquisitionState.getStackDepthInPlanesVariable();

    // Creating elements:

    VariableRangeSlider<Number> lZRangeSlider =
                                              new VariableRangeSlider<>("Z-range",
                                                                        lZLow,
                                                                        lZHigh,
                                                                        lZLow.getMinVariable(),
                                                                        lZHigh.getMaxVariable(),
                                                                        0.01d,
                                                                        null);

    NumberVariableTextField<Number> lZStepTextField =
                                                    new NumberVariableTextField<Number>("Z-step:",
                                                                                        lZStep,
                                                                                        0d,
                                                                                        Double.POSITIVE_INFINITY,
                                                                                        0d);
    lZStepTextField.getTextField().setPrefWidth(100);

    NumberVariableTextField<Number> lNumberOfPlanesTextField =
                                                             new NumberVariableTextField<Number>("Number of planes:",
                                                                                                 lNumberOfPlanes,
                                                                                                 0,
                                                                                                 Double.POSITIVE_INFINITY,
                                                                                                 0);

    lNumberOfPlanesTextField.getTextField().setPrefWidth(100);

    OnOffArrayPane lCameraOnOffArray = new OnOffArrayPane();

    for (int i =
               0; i < pAcquisitionState.getNumberOfDetectionArms(); i++)
    {
      lCameraOnOffArray.addSwitch("C" + i,
                                  pAcquisitionState.getCameraOnOffVariable(i));
    }

    OnOffArrayPane lLightSheetOnOffArray = new OnOffArrayPane();

    for (int i =
               0; i < pAcquisitionState.getNumberOfLightSheets(); i++)
    {
      lLightSheetOnOffArray.addSwitch("L" + i,
                                      pAcquisitionState.getLightSheetOnOffVariable(i));
    }

    OnOffArrayPane lLaserOnOffArray = new OnOffArrayPane();

    for (int i =
               0; i < pAcquisitionState.getNumberOfLaserLines(); i++)
    {
      lLaserOnOffArray.addSwitch("La" + i,
                                 pAcquisitionState.getLaserOnOffVariable(i));
    }

    AcquistionStateMultiChart lMultiChart =
                                          new AcquistionStateMultiChart(pAcquisitionState);

    AcquistionStateTableView lTableView =
                                        new AcquistionStateTableView(pAcquisitionState);

    // Laying out components:

    int lRow = 0;

    {
      add(lStageXSlider.getLabel(), 0, lRow);
      add(lStageXSlider.getTextField(), 1, lRow);
      add(lStageXSlider.getSlider(), 2, lRow);
      lRow++;
    }

    {
      add(lStageYSlider.getLabel(), 0, lRow);
      add(lStageYSlider.getTextField(), 1, lRow);
      add(lStageYSlider.getSlider(), 2, lRow);
      lRow++;
    }

    {
      add(lStageZSlider.getLabel(), 0, lRow);
      add(lStageZSlider.getTextField(), 1, lRow);
      add(lStageZSlider.getSlider(), 2, lRow);
      lRow++;
    }

    {
      add(lZRangeSlider.getLabel(), 0, lRow);
      add(lZRangeSlider.getLowTextField(), 1, lRow);
      add(lZRangeSlider.getRangeSlider(), 2, lRow);
      add(lZRangeSlider.getHighTextField(), 3, lRow);
      lRow++;
    }

    {
      HBox lHBox = new HBox(new Label("Z-step: "),
                            lZStepTextField.getTextField(),
                            new Label("      Nb of planes: "),
                            lNumberOfPlanesTextField.getTextField(),
                            new Label("      Cameras: "),
                            lCameraOnOffArray,
                            new Label("      Lightsheets: "),
                            lLightSheetOnOffArray,
                            new Label("      Lasers: "),
                            lLaserOnOffArray);
      lHBox.setAlignment(Pos.CENTER_LEFT);
      GridPane.setColumnSpan(lHBox, 8);
      add(lHBox, 0, lRow);
      lRow++;
    }
    
   /* {
      Button lCopySettingsButton = new Button("Copy current microscope settings to this state");
      lCopySettingsButton.setOnAction((e)-> pAcquisitionState.copyCurrentMicroscopeSettings());
      GridPane.setColumnSpan(lCopySettingsButton, 8);
      add(lCopySettingsButton, 0, lRow);
      lRow++;
    }/**/

    {
      TabPane lTabPane = new TabPane();
      Tab lChartTab = new Tab("Chart");
      Tab lTableTab = new Tab("Table");
      lTabPane.getTabs().addAll(lChartTab, lTableTab);

      lChartTab.setContent(lMultiChart);
      lTableTab.setContent(lTableView);

      GridPane.setVgrow(lTabPane, Priority.ALWAYS);
      GridPane.setHgrow(lTabPane, Priority.ALWAYS);
      GridPane.setColumnSpan(lTabPane, 8);
      add(lTabPane, 0, lRow);
      lRow++;
    }

    // Update events:

    pAcquisitionState.addChangeListener((e) -> {
      if (isVisible())
      {
        Platform.runLater(() -> {
          lMultiChart.updateChart(pAcquisitionState);
        });
        Platform.runLater(() -> {
          lTableView.updateTable(pAcquisitionState);
        });
      }

    });
  }

}
