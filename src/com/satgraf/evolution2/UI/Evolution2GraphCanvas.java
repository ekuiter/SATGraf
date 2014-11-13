package com.satgraf.evolution2.UI;

import com.satgraf.community.UI.CommunityCanvas;
import com.satlib.graph.GraphViewer;

public class Evolution2GraphCanvas extends CommunityCanvas {

	public Evolution2GraphCanvas(GraphViewer graph) {
		super(graph);
		renderer = new Evolution2GraphCanvasRenderer(this, graph);
		this.setDefaultRenderer(Object.class, renderer);
	}
	
}
