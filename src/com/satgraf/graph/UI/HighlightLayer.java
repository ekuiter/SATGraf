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
import com.satlib.graph.Node;

public class HighlightLayer<T extends GraphViewer> extends Layer {

  protected T graph;
  public static Color HIGHLIGHT_COLOR = new Color(0xe4, 0xfd, 0x03);
  Node highligted;

  public HighlightLayer(Dimension size, T graph) {
    super(size);
    this.graph = graph;
  }

  public void paintComponent(final Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHints(GraphCanvas.getRenderingHints());
    Node n = graph.getSelectedNode();

    Node n1 = null;
    if(highligted != null){
      n1 = highligted;
  }
    if ((n == null || !drawNode(n))) {
      n = n1;
    }
    drawNodeHighlight(g, g2, n, true);
    if (n != n1 && n1 != null) {
      drawNodeHighlight(g, g2, n1, true);
    }
  }

  public Node getNodeAroundXY(int x, int y) {
    Node n;
    int radius = (int) (DrawableNode.NODE_DIAMETER * graph.getScale() / 2);

    for (int i = x - radius; i < x + radius; i++) {
      for (int j = y - radius; j < y + radius; j++) {
        n = graph.getNodeAtXY(i, j);

        if (n != null) {
          return n;
        }
      }
    }

    return null;
  }

  private boolean drawNode(Node n) {
    return n.isVisible() && !(!graph.getShowAssignedVars() && n.isAssigned());
  }

  protected void drawNodeHighlight(Graphics g, Graphics2D g2, Node n, boolean edges) {
    if (n == null || !drawNode(n)) {
      return;
    }
    int scaled_diameter = (int) Math.ceil(DrawableNode.NODE_DIAMETER * graph.getScale());
    
    g2.setStroke(new BasicStroke(1));
    Stroke s = g2.getStroke();
    Iterator<Edge> es = n.getEdges().iterator();
    if(edges){
      while (es.hasNext()) {
        Edge e = es.next();
        if (!graph.shouldShowEdge(e)) {
          continue;
        }
        g2.setColor(HIGHLIGHT_COLOR);
        Rectangle bounds = g.getClipBounds();
        g2.setStroke(new BasicStroke(5));
        int x1 = (int) (graph.getX(e.getStart()) * graph.getScale());
        int y1 = (int) (graph.getY(e.getStart()) * graph.getScale());
        int x2 = (int) (graph.getX(e.getEnd()) * graph.getScale());
        int y2 = (int) (graph.getY(e.getEnd()) * graph.getScale());

        g.drawLine(x1, y1, x2, y2);
        g2.setColor(Color.BLACK);

        g2.setStroke(new BasicStroke(3));
        g.drawLine(x1, y1, x2, y2);
      }
    }
    g2.setStroke(s);
    g.setColor(HIGHLIGHT_COLOR);
    scaled_diameter += 3;
    int x = (int) (graph.getX(n) * graph.getScale()) - scaled_diameter / 2;
    int y = (int) (graph.getY(n) * graph.getScale()) - scaled_diameter / 2;
    g.fillArc(x, y, scaled_diameter, scaled_diameter, 0, 360);
    g.setColor(Color.BLACK);
    g.drawArc(x, y, scaled_diameter, scaled_diameter, 0, 360);
  }
}
