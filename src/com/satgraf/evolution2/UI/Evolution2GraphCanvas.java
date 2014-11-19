package com.satgraf.evolution2.UI;

import java.awt.Graphics;

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

	    Node decisionVariable = graph.getDecisionVariable();
	    if (decisionVariable != null && decisionVariable.isVisible()) {
	      drawDecisionVariable(decisionVariable, g);
	    }
	}
	
	private void drawDecisionVariable(Node n, Graphics g) {
	    int scaled_diameter = (int) Math.ceil(DrawableNode.NODE_DIAMETER * 2 * graph.getScale());
	    int radius = scaled_diameter / 2;

	    g.setColor(HIGHLIGHT_COLOR);

	    int x = (int) ((n.getX(graph)) * graph.getScale()) - radius;
	    int y = (int) ((n.getY(graph)) * graph.getScale()) - radius;

	    g.fillArc(x,
	            y,
	            scaled_diameter,
	            scaled_diameter, 0, 360);
	}
}
