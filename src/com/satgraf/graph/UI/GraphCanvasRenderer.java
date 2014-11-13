/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.graph.UI;

import com.satgraf.UI.PaintThread;
import static com.satgraf.graph.UI.GraphCanvas.HIGHLIGHT_COLOR;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.satlib.graph.DrawableNode;
import com.satlib.graph.Edge;
import com.satlib.graph.GraphViewer;
import com.satlib.graph.Node;

/**
 *
 * @author zacknewsham
 */
public class GraphCanvasRenderer extends JPanel implements TableCellRenderer {

  public static int FRAME_WIDTH = 500;
  public static int FRAME_HEIGHT = 250;
  private static HashMap<JTable, ArrayList<TiledImage>> images = new HashMap<JTable, ArrayList<TiledImage>>();
  protected TiledImage image = null;
  protected GraphViewer graph;
  private GraphCanvas canvas;
  public static int running = 0;

  public GraphCanvasRenderer(GraphCanvas canvas, GraphViewer graph) {
    this.graph = graph;
    this.setSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
    this.canvas = canvas;
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    GraphCanvas canvas = (GraphCanvas) table;
    image = null;
    if (images.get(table) == null) {
      images.put(table, new ArrayList<TiledImage>());
    }
    for (int i = 0; i < images.get(table).size(); i++) {
      TiledImage aImage = images.get(table).get(i);
      if (aImage.row == row && aImage.column == column) {
        image = aImage;
        break;
      }
    }

    if (image == null) {
      createNewImage(table, row, column, canvas);
    }

    PaintThread p = new PaintThread(canvas, new Rectangle(column * (int) (FRAME_WIDTH / graph.getScale()), row * (int) (FRAME_HEIGHT / graph.getScale()), (int) (FRAME_WIDTH / graph.getScale()), (int) (FRAME_HEIGHT / graph.getScale())), image, null);
    canvas.paintThread(p);

    return this;
  }

  private void createNewImage(JTable table, int row, int column, GraphCanvas canvas) {
    Point origin = new Point(column * (int) (FRAME_WIDTH / graph.getScale()), row * (int) (FRAME_HEIGHT / graph.getScale()));

    image = new TiledImage((int) (FRAME_WIDTH / graph.getScale()), (int) (FRAME_HEIGHT / graph.getScale()), BufferedImage.TYPE_INT_ARGB, origin, new Dimension((int) (FRAME_WIDTH / graph.getScale()), (int) (FRAME_HEIGHT / graph.getScale())));
    Graphics2D g1 = image.createGraphics();
    g1.setClip(0, 0, (int) (FRAME_WIDTH / graph.getScale()), (int) (FRAME_HEIGHT / graph.getScale()));
    image.setGraphics(g1);
    image.row = row;
    image.column = column;
    images.get(table).add(image);
  }

  @Override
  public void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    g2.drawImage(image, new AffineTransformOp(AffineTransform.getScaleInstance(FRAME_WIDTH / (double) image.getWidth(), FRAME_HEIGHT / (double) image.getHeight()), AffineTransformOp.TYPE_BICUBIC), 0, 0);//, 0, 0, image.getWidth(), image.getHeight(), this);

    drawNodeHighlight(g, g2);
  }

  private void drawNodeHighlight(Graphics g, Graphics2D g2) {
    Node n = graph.getSelectedNode();
    Point pos = canvas.getMousePosition();
    if ((n == null || !n.isVisible()) && pos != null) {
      n = graph.getNodeAtXY(pos.x, pos.y);
    }
    int scaled_diameter = (int) Math.ceil(DrawableNode.NODE_DIAMETER * graph.getScale());
    while (n != null && (n.isVisible() || n == graph.getSelectedNode())) {
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
        int x1 = (int) ((e.getStart().getX(graph) - image.origin.x) * graph.getScale());
        int y1 = (int) ((e.getStart().getY(graph) - image.origin.y) * graph.getScale());
        int x2 = (int) ((e.getEnd().getX(graph) - image.origin.x) * graph.getScale());
        int y2 = (int) ((e.getEnd().getY(graph) - image.origin.y) * graph.getScale());
        if ((x1 >= 0 && y1 >= 0 && x1 <= bounds.width && y1 <= bounds.height)
                || (x2 >= 0 && y2 >= 0 && x2 <= bounds.width && y2 <= bounds.height)) {
          /*x1 -= image.getBounds().x;
           x2 -= image.getBounds().x;
           y1 -= image.getBounds().y;
           y2 -= image.getBounds().y;*/
          g.drawLine(x1 + scaled_diameter / 2,
                  y1 + scaled_diameter / 2,
                  x2 + scaled_diameter / 2,
                  y2 + scaled_diameter / 2);
          g2.setColor(Color.BLACK);

          g2.setStroke(new BasicStroke(3));
          g.drawLine(x1 + scaled_diameter / 2,
                  y1 + scaled_diameter / 2,
                  x2 + scaled_diameter / 2,
                  y2 + scaled_diameter / 2);
        }
      }
      g2.setStroke(s);
      g.setColor(HIGHLIGHT_COLOR);
      int x = (int) ((n.getX(graph) - image.origin.x) * graph.getScale());
      int y = (int) ((n.getY(graph) - image.origin.y) * graph.getScale());
      g.fillArc(x,
              y,
              scaled_diameter,
              scaled_diameter, 0, 360);
      g.setColor(Color.BLACK);
      g.drawArc(x,
              y,
              scaled_diameter,
              scaled_diameter, 0, 360);
      if (canvas.getMousePosition() == null || n == graph.getNodeAtXY(canvas.getMousePosition().x, canvas.getMousePosition().y)) {
        n = null;
      } else {
        n = graph.getNodeAtXY(canvas.getMousePosition().x, canvas.getMousePosition().y);
      }
    }
  }

  public void reset() {
    images.clear();
    image = null;
  }
}
