package com.satgraf.evolution.UI;

import com.satgraf.community.UI.CommunityCanvas;

public class EvolutionGraphCanvas extends CommunityCanvas {
	
	private DecisionVariableLayer decisionVariableLayer;
	
	public EvolutionGraphCanvas(EvolutionGraphViewer graph) {
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
