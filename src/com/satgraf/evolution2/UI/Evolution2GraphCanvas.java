package com.satgraf.evolution2.UI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import com.satgraf.community.UI.CommunityCanvas;
import com.satlib.graph.DrawableNode;
import com.satlib.graph.GraphViewer;
import com.satlib.graph.Node;

public class Evolution2GraphCanvas extends CommunityCanvas {
	
	private Evolution2GraphViewer graph;

	public Evolution2GraphCanvas(Evolution2GraphViewer graph) {
		super(graph);
		this.graph = graph;
	}
	
	@Override
	public void paintComponent(Graphics g) {
	    super.paintComponent(g);

	    if (graph.getShowDecisionVariable()) {
		    Node decisionVariable = graph.getDecisionVariable();
		    if (decisionVariable != null && decisionVariable.isVisible()) {
		      drawDecisionVariable(decisionVariable, g);
		    }
	    }
	}
	
	private void drawDecisionVariable(Node n, Graphics g) {
		int nodeSize = (int) Math.ceil(DrawableNode.NODE_DIAMETER * 2 * graph.getScale());
	    int diameter_small = nodeSize + 1;
	    int disp_small = (diameter_small / 2) - nodeSize / 2;
	    int diameter_large = nodeSize + 3;
	    int disp_large = (diameter_large / 2) - nodeSize / 2;

	    int x = (int) ((n.getX(graph)) * graph.getScale());
	    int y = (int) ((n.getY(graph)) * graph.getScale());
	    
	    if (n.getActivity() > 0) {
	    	g.setColor(Color.RED);
	    } else {
	    	g.setColor(Color.BLUE);
	    }
	    
        g.fillArc(x - disp_large,
                  y - disp_large,
                  diameter_large,
                  diameter_large, 0, 360);

        g.setColor(HIGHLIGHT_COLOR);
	    g.fillArc(x - disp_small,
	              y - disp_small,
	              diameter_small,
	              diameter_small, 0, 360);
	    
	    
	}
}
