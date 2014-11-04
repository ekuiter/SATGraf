package com.satgraf.evolution2.UI;

import com.satlib.community.CommunityGraphViewer;
import java.util.Collection;
import com.satgraf.graph.UI.GraphFrame;
import com.satgraf.graph.UI.OptionsPanel;
import com.satgraf.community.UI.CommunityOptionsPanel;

public class Evolution2OptionsPanel extends CommunityOptionsPanel {
	
	Evolution2Scaler scaler;
	CommunityGraphViewer graph;
	
	public Evolution2OptionsPanel(GraphFrame frame, CommunityGraphViewer graph, Collection<String> groups) {
		super(frame, graph, groups, false);
		setGraph(graph);
		this.graph = graph;
		buildLayout(graph);
	}
	
	private void buildLayout(CommunityGraphViewer graph) {
		OptionsPanel op = getOptionsPanel();
		scaler = new Evolution2Scaler(graph);
		op.setCustomComponent(scaler);
	}
	
	public void setGraph(CommunityGraphViewer graph) {
		super.setGraph(graph);
	}

	public void newFileReady(int numLinesInFile) {
		scaler.newFileReady(numLinesInFile);
	}
}
