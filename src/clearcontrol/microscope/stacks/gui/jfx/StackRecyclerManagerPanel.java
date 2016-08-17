package clearcontrol.microscope.stacks.gui.jfx;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import clearcontrol.gui.jfx.recycler.RecyclerPanel;
import clearcontrol.microscope.stacks.StackRecyclerManager;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import coremem.recycling.RecyclerInterface;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Separator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * StackRecyclerManagerPanel is a GUI element that displays information about
 * all recyclers managed by a StackRecyclerManager.
 * 
 * @author royer
 */
public class StackRecyclerManagerPanel extends VBox
{

	/**
	 * Constructs a {@link StackRecyclerManagerPanel} given a
	 * {@link StackRecyclerManager}.
	 * 
	 * @param pStackRecyclerManager
	 *          {@link StackRecyclerManager} to use.
	 */
	public StackRecyclerManagerPanel(StackRecyclerManager pStackRecyclerManager)
	{
		super();

		pStackRecyclerManager.addChangeListener((m) -> {
			updateRecyclerPanels(m.getRecyclerMap());
		});

	}

	/**
	 * This private method is responsible to upate the Recyclers display. It
	 * should be called whenever the list of recyclers in the manager is changed.
	 * 
	 * @param pMap
	 */
	private void updateRecyclerPanels(ConcurrentHashMap<String, RecyclerInterface<StackInterface, StackRequest>> pMap)
	{
		StackRecyclerManagerPanel lMainVBox = this;

		Platform.runLater(() -> {

			lMainVBox.getChildren().clear();

			ScrollPane lScrollPane = new ScrollPane();
			lScrollPane.setPrefSize(RecyclerPanel.cPrefWidth,
															RecyclerPanel.cPrefHeight * 1.5);
			lScrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
			lScrollPane.setVmax(RecyclerPanel.cPrefHeight * 1.5);
			lMainVBox.getChildren().add(lScrollPane);
			VBox.setVgrow(lScrollPane, Priority.ALWAYS);

			VBox lVBox = new VBox();
			lScrollPane.setContent(lVBox);

			Set<Entry<String, RecyclerInterface<StackInterface, StackRequest>>> lEntrySet = pMap.entrySet();
			for (Entry<String, RecyclerInterface<StackInterface, StackRequest>> lEntry : lEntrySet)
			{
				String lRecyclerName = lEntry.getKey();
				RecyclerInterface<StackInterface, StackRequest> lRecycler = lEntry.getValue();

				Label lLabel = new Label(lRecyclerName);
				RecyclerPanel lRecyclerPane = new RecyclerPanel(lRecycler);
				lRecyclerPane.setPadding(10);
				Separator lSeparator = new Separator();
				lVBox.getChildren().addAll(lLabel, lRecyclerPane, lSeparator);

			}

		});

	}

}