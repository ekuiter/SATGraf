package com.satgraf.evolution2.UI;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.satgraf.UI.TextRocker;
import com.satgraf.UI.TextRockerListener;
import com.satgraf.community.UI.CommunityOptionsPanel;
import com.satgraf.graph.UI.GraphFrame;
import com.satgraf.graph.UI.OptionsPanel;

public class Evolution2OptionsPanel extends CommunityOptionsPanel implements TextRockerListener {
	
	Evolution2Scaler scaler;
	Evolution2GraphViewer graph;
	
	// Options
	private JCheckBox hideDecisionVariable = new JCheckBox("Decision Variable", true);
	private TextRocker decisionVisibleLength;
	
	public Evolution2OptionsPanel(GraphFrame frame, Evolution2GraphViewer graph, Collection<String> groups) {
		super(frame, graph, groups, false);
		setGraph(graph);
		this.graph = graph;
		this.decisionVisibleLength = new TextRocker(0, "Decision Variable Display Length", graph.getDisplayDecisionVariableFor());
		
		buildLayout(graph);
		this.setTopComponent(checkBoxPanel);
	}
	
	private void buildLayout(Evolution2GraphViewer graph) {
		OptionsPanel op = getOptionsPanel();
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(4, 1));
		panel.setPreferredSize(new Dimension(250, 500));
		
		// Scaler
		scaler = new Evolution2Scaler(graph);
		panel.add(scaler);
		
		// Decision Variables
		hideDecisionVariable.addChangeListener(new ChangeListener() {
	      @Override
	      public void stateChanged(ChangeEvent e) {
	        Evolution2OptionsPanel.this.graph.setShowDecisionVariable(hideDecisionVariable.isSelected());
	        if (!hideDecisionVariable.isSelected()) {
	        	int o = 0;
	        }
	      }
	    });
		panel.add(hideDecisionVariable);
		
		// Decision Visible length
		panel.add(decisionVisibleLength);
		decisionVisibleLength.registerListener(this);
		
		op.setCustomComponent(panel);
	}
	
	public void setGraph(Evolution2GraphViewer graph) {
		checkBoxPanel.removeBars();
        checkboxPanels.clear();
        checkBoxPanel.addBar("General Options", optionsPanel);
		super.setGraph(graph, false);
	}

	public void newFileReady(int numLinesInFile) {
		scaler.newFileReady(numLinesInFile);
	}

	@Override
	public void stateChanged(int id, int value) {
		if (id == decisionVisibleLength.getId())
			graph.setDisplayDecisionVariableFor(value);
	}
}
