package com.satgraf.evolution2.UI;

import static com.satgraf.graph.UI.GraphCanvas.HIGHLIGHT_COLOR;

import java.awt.Graphics;

import com.satgraf.graph.UI.GraphCanvas;
import com.satgraf.graph.UI.GraphCanvasRenderer;
import com.satlib.graph.DrawableNode;
import com.satlib.graph.GraphViewer;
import com.satlib.graph.Node;

public class Evolution2GraphCanvasRenderer extends GraphCanvasRenderer {

	public Evolution2GraphCanvasRenderer(GraphCanvas canvas, GraphViewer graph) {
		super(canvas, graph);
	}
	
	@Override
	public void paintComponent(Graphics g) {
	    super.paintComponent(g);

	    Node decisionVariable = ((Evolution2GraphViewer)graph).getDecisionVariable();
	    if (decisionVariable != null) {
	      drawDecisionVariable(decisionVariable, g);
	    }
	}
	
	private void drawDecisionVariable(Node n, Graphics g) {
	    int scaled_diameter = (int) Math.ceil(DrawableNode.NODE_DIAMETER * 2 * graph.getScale());
	    int radius = scaled_diameter / 2;

	    g.setColor(HIGHLIGHT_COLOR);

	    int x = (int) ((n.getX(graph) - image.origin.x) * graph.getScale()) - radius;
	    int y = (int) ((n.getY(graph) - image.origin.y) * graph.getScale()) - radius;

	    g.fillArc(x,
	            y,
	            scaled_diameter,
	            scaled_diameter, 0, 360);
	}
}
