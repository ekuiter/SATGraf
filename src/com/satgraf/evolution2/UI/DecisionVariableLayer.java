package com.satgraf.evolution2.UI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import com.satgraf.UI.Layer;
import com.satgraf.graph.UI.HighlightLayer;
import com.satlib.graph.DrawableNode;
import com.satlib.graph.GraphViewer;
import com.satlib.graph.Node;

public class DecisionVariableLayer extends Layer {
	
	protected GraphViewer graph;
	
	public DecisionVariableLayer(Dimension size, GraphViewer graph) {
		super(size);
		this.graph = graph;
	}
	
	public void paintComponent(final Graphics g) {
		Evolution2GraphViewer graph = (Evolution2GraphViewer) this.graph;
		
		if (graph.getShowDecisionVariable()) {
		    Node decisionVariable = graph.getDecisionVariable();
		    if (decisionVariable != null && decisionVariable.isVisible()) {
		      drawDecisionVariable(decisionVariable, g);
		    }
	    }
	}
	
	private void drawDecisionVariable(Node n, Graphics g) {
		int diameter = (int) Math.ceil(DrawableNode.NODE_DIAMETER * graph.getScale());
	    int diameter_small = diameter*2 + 1;
	    int disp_small = (diameter_small / 2) - diameter / 2;
	    int diameter_large = diameter*2 + 3;
	    int disp_large = (diameter_large / 2) - diameter / 2;

	    int x = (int) ((n.getX(graph)) * graph.getScale()) - diameter/2;
	    int y = (int) ((n.getY(graph)) * graph.getScale()) - diameter/2;
	    
	    if (n.getActivity() > 0) {
	    	g.setColor(Color.RED);
	    } else {
	    	g.setColor(Color.BLUE);
	    }
	    
        g.fillArc(x - disp_large, y - disp_large, diameter_large, diameter_large, 0, 360);

        g.setColor(HighlightLayer.HIGHLIGHT_COLOR);
	    g.fillArc(x - disp_small, y - disp_small, diameter_small, diameter_small, 0, 360);
	    
	    
	}
}
