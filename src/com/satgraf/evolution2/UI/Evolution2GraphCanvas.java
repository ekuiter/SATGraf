package com.satgraf.evolution2.UI;

import com.satlib.evolution.Evolution2GraphViewer;
import com.satgraf.community.UI.CommunityCanvas;

public class Evolution2GraphCanvas extends CommunityCanvas {
	
	private DecisionVariableLayer decisionVariableLayer;
	
	public Evolution2GraphCanvas(Evolution2GraphViewer graph) {
		super(graph);
	}
	
	@Override
	protected void setupLayers() {
		// Decision Variable Layer
		decisionVariableLayer = new DecisionVariableLayer(this.getSize(), graph);
		this.add(decisionVariableLayer);
		
		super.setupLayers();
	}
}
