package com.satgraf.evolution2.UI;

import com.satlib.community.CommunityGraphViewer;
import com.satlib.graph.GraphViewer;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JScrollPane;

import com.satgraf.graph.UI.GraphFrame;
import com.satgraf.graph.UI.NodeCheckboxPanel;
import com.satgraf.graph.UI.OptionsPanel;
import com.satgraf.community.UI.CommunityOptionsPanel;

public class Evolution2OptionsPanel extends CommunityOptionsPanel {
	
	Evolution2Scaler scaler;
	Evolution2GraphViewer graph;
	
	public Evolution2OptionsPanel(GraphFrame frame, Evolution2GraphViewer graph, Collection<String> groups) {
		super(frame, graph, groups, false);
		setGraph(graph);
		this.graph = graph;
		buildLayout(graph);
	}
	
	private void buildLayout(Evolution2GraphViewer graph) {
		OptionsPanel op = getOptionsPanel();
		scaler = new Evolution2Scaler(graph);
		op.setCustomComponent(scaler);
	}
	
	public void setGraph(Evolution2GraphViewer graph) {
		super.setGraph(graph);
	}

	public void newFileReady(int numLinesInFile) {
		scaler.newFileReady(numLinesInFile);
	}
	
	@Override
	protected void setGraph(GraphViewer graph){
	    optionsPanel.setGraph(graph);
	    synchronized(checkBoxPanel){
	      this.graph = (Evolution2GraphViewer)graph;
	      this.graph.addObserver(this);
	      checkBoxPanel.removeBars();
	      checkboxPanels.clear();
	      Iterator<String> groupsI = groups.iterator();
	      while(groupsI.hasNext()){
	        String group = groupsI.next();
	        NodeCheckboxPanel temp = new NodeCheckboxPanel(graph, group, graph.getNodes(group));
	        checkboxPanels.put(group, temp);
	        JScrollPane tempScroll = new JScrollPane(temp);
	        checkBoxPanel.addBar(group, tempScroll);
	      }
	      checkBoxPanel.setVisibleBar(0);
	    }
	}
}
