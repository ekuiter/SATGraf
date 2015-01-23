package com.satgraf.graph.UI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.Iterator;

import com.satgraf.UI.Layer;
import com.satlib.graph.DrawableNode;
import com.satlib.graph.Edge;
import com.satlib.graph.GraphViewer;
import com.satlib.graph.Node;

public class HighlightLayer extends Layer {
	
	protected GraphViewer graph;
	public static Color HIGHLIGHT_COLOR = new Color(0xe4, 0xfd, 0x03);
	
	public HighlightLayer(Dimension size, GraphViewer graph) {
		super(size);
		this.graph = graph;
	}
	
	public void paintComponent(final Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHints(GraphCanvas.getRenderingHints());
		drawNodeHighlight(g, g2);
	}
	
	public Node getNodeAroundXY(int x, int y) {
		Node n;
		int radius = (int) (DrawableNode.NODE_DIAMETER * graph.getScale() / 2);
		
		for (int i = x - radius; i < x + radius; i++) {
			for (int j = y - radius; j < y + radius; j++) {
				n = graph.getNodeAtXY(i, j);
				
				if (n != null)
					return n;
			}
		}
		
		return null;
	}
	
	private void drawNodeHighlight(Graphics g, Graphics2D g2) {
	    Node n = graph.getSelectedNode();
	    Point pos = getMousePosition();
	    if ((n == null || !n.isVisible()) && pos != null) {
  	      n = getNodeAroundXY(pos.x, pos.y);
	    }
	    int scaled_diameter = (int) Math.ceil(DrawableNode.NODE_DIAMETER * graph.getScale());
	    while (n != null && (n.isVisible())) {
	      Stroke s = g2.getStroke();
	      Iterator<Edge> es = n.getEdges();
	      while (es.hasNext()) {
	        Edge e = es.next();
	        if (!graph.shouldShowEdge(e)) {
	          continue;
	        }
	        g2.setColor(HIGHLIGHT_COLOR);
	        Rectangle bounds = g.getClipBounds();
	        g2.setStroke(new BasicStroke(5));
	        int x1 = (int) ((e.getStart().getX(graph)) * graph.getScale());
	        int y1 = (int) ((e.getStart().getY(graph)) * graph.getScale());
	        int x2 = (int) ((e.getEnd().getX(graph)) * graph.getScale());
	        int y2 = (int) ((e.getEnd().getY(graph)) * graph.getScale());
	        
	        g.drawLine(x1, y1, x2, y2);
	        g2.setColor(Color.BLACK);

	        g2.setStroke(new BasicStroke(3));
	        g.drawLine(x1, y1, x2, y2);
	      }
	      g2.setStroke(s);
	      g.setColor(HIGHLIGHT_COLOR);
	      scaled_diameter += 1;
	      int x = (int) ((n.getX(graph)) * graph.getScale()) - scaled_diameter/2;
	      int y = (int) ((n.getY(graph)) * graph.getScale()) - scaled_diameter/2;
	      g.fillArc(x, y, scaled_diameter, scaled_diameter, 0, 360);
	      g.setColor(Color.BLACK);
	      g.drawArc(x, y, scaled_diameter, scaled_diameter, 0, 360);
	      
	      if (getMousePosition() == null || n == getNodeAroundXY(getMousePosition().x, getMousePosition().y)) {
	        n = null;
	      } else {
	        n = getNodeAroundXY(getMousePosition().x, getMousePosition().y);
	      }
	    }
	}
}
