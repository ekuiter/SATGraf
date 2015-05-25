package com.satgraf.evolution.UI;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.satgraf.UI.TextRocker;
import com.satgraf.UI.TextRockerListener;
import com.satgraf.community.UI.CommunityOptionsPanel;
import com.satgraf.graph.UI.GraphFrame;
import com.satgraf.graph.UI.OptionsPanel;

public class EvolutionOptionsPanel extends CommunityOptionsPanel implements TextRockerListener {
	
	EvolutionScaler scaler;
	EvolutionGraphViewer graph;
	
	// Options
	private JCheckBox hideDecisionVariable = new JCheckBox("Decision Variable", true);
	private TextRocker evolutionSpeed;
	private TextRocker decisionVisibleLength;
	private TextRocker conflictRocker;
	private JLabel conflictDescription = new JLabel("Conflict Description");
	private boolean evolutionTriggeredConflict = false;
	
	public EvolutionOptionsPanel(GraphFrame frame, EvolutionGraphViewer graph, Collection<String> groups) {
		super(frame, graph, groups, false);
        infoPanel = new EvolutionGraphInfoPanel(graph);
		setGraph(graph);
		this.graph = graph;
		
		buildLayout(graph);
		this.setTopComponent(checkBoxPanel);
	}
	
	private void buildLayout(EvolutionGraphViewer graph) {
		OptionsPanel op = getOptionsPanel();
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setPreferredSize(new Dimension(250, 500));
		
		// Scaler
		scaler = new EvolutionScaler(graph);
		panel.add(scaler);
		
		// Evolution speed
		this.evolutionSpeed = new TextRocker(0, "Speed of Evolution", graph.getGraph().getEvolutionSpeed(), 1, Integer.MAX_VALUE);
		panel.add(evolutionSpeed);
		evolutionSpeed.registerListener(this);
		
		// Conflict scanning
		this.conflictRocker = new TextRocker(2, "Conflict Scan", 0, 0, Integer.MAX_VALUE);
		panel.add(conflictRocker);
		JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayout(1, 1));
		conflictDescription.setHorizontalAlignment(JLabel.CENTER);
		panel1.add(conflictDescription);
		panel.add(panel1);
		conflictRocker.registerListener(this);
		
		// Decision Variables
		hideDecisionVariable.addChangeListener(new ChangeListener() {
	      @Override
	      public void stateChanged(ChangeEvent e) {
	        EvolutionOptionsPanel.this.graph.getGraph().setShowDecisionVariable(hideDecisionVariable.isSelected());
	        if (!hideDecisionVariable.isSelected()) {
	        	int o = 0;
	        }
	      }
	    });
		JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayout(1, 1));
		panel2.add(hideDecisionVariable);
		panel.add(panel2);
		
		// Decision Visible length
		this.decisionVisibleLength = new TextRocker(1, "Decision Variable Display Length", graph.getGraph().getDisplayDecisionVariableFor(), 0, 10000);
		panel.add(decisionVisibleLength);
		decisionVisibleLength.registerListener(this);
		
		op.setCustomComponent(panel);
	}
	
	public void setGraph(EvolutionGraphViewer graph) {
		checkBoxPanel.removeBars();
        checkboxPanels.clear();
        checkBoxPanel.addBar("General Options", optionsPanel);
		super.setGraph(graph, false);
	}

	/*public void newFileReady(int numLinesInFile) {
		scaler.newFileReady(numLinesInFile);
	}*/
	
	@Override
	public void update(){
	    super.update();
	    updateConflictRockerWithScalerInfo();
	}
	
	private void updateConflictRockerWithScalerInfo() {
		int currentConflict = scaler.getCurrentConflict();
		this.evolutionTriggeredConflict = true;
	    conflictRocker.setValue(currentConflict);
	    updateConflictDescription(currentConflict);
	}

	@Override
	public void stateChanged(int id, int value) {
		if (id == evolutionSpeed.getId())
			graph.getGraph().setEvolutionSpeed(value);
		else if (id == decisionVisibleLength.getId())
			graph.getGraph().setDisplayDecisionVariableFor(value);
		else if (id == conflictRocker.getId()) {
			if (this.evolutionTriggeredConflict) {
				this.evolutionTriggeredConflict = false;
				return;
			}
			
			boolean scanWasApplied = graph.getEvolution().scanToConflict(value, false);
			
			if (scanWasApplied) {
				updateConflictDescription(value);
			} 
            else {
				Runnable doRevert = new Runnable() {
					
					@Override
					public void run() {
						updateConflictRockerWithScalerInfo();
					}
				};
		    	
				SwingUtilities.invokeLater(doRevert);
			}
		}
	}
	
	private void updateConflictDescription(int value) {
		String conflict = String.valueOf((int) Math.ceil((double)value / 2));
		if (value == 0) {
			conflictDescription.setText("Conflict Description");
		} else if (value % 2 == 0) {
			conflictDescription.setText("Post Conflict " + conflict + " Analysis");
		} else {
			conflictDescription.setText("Pre Conflict " + conflict + " Analysis");
		}
	}
}
