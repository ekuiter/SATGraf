package com.satgraf.evolution2.UI;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.HashMap;

import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityGraphViewer;
import com.satlib.community.placer.CommunityPlacer;
import com.satlib.graph.Node;

public class Evolution2GraphViewer extends CommunityGraphViewer {
	
	private Node decisionVariable = null;

	public Evolution2GraphViewer(CommunityGraph graph, HashMap<String, TIntObjectHashMap<String>> node_lists, CommunityPlacer pl) {
		super(graph, node_lists, pl);
	}

	public void setDecisionVariable(Node n) {
	    this.decisionVariable = n;
	}

	public Node getDecisionVariable() {
	    return this.decisionVariable;
	}

	public void clearDecisionVariable() {
	    this.decisionVariable = null;
	}
}
