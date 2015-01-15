package com.satgraf.community.UI;

import java.awt.Color;
import java.awt.Dimension;

import com.satgraf.graph.UI.EdgeLayer;
import com.satlib.community.CommunityEdge;
import com.satlib.graph.Edge;
import com.satlib.graph.GraphViewer;


public class CommunityEdgeLayer extends EdgeLayer {

	public CommunityEdgeLayer(Dimension size, GraphViewer graph) {
		super(size, graph);
	}
	
	@Override
	protected Color getColor(Edge e) {
		CommunityEdge ce = (CommunityEdge) e;
		
		if (ce.isConflictEdge()) {
		  return Color.RED;
	    } else if(ce.getStart().getCommunity() == ce.getEnd().getCommunity() && ce.getStart().getCommunity() != -1) {
          return e.getColor(graph);
	    } else {
	      return Color.WHITE;
	    }
	}
}
