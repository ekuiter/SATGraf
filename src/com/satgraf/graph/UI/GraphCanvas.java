/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.graph.UI;

import com.satlib.Progressive;
import com.satlib.graph.Node;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.PriorityQueue;
import javax.swing.JLayeredPane;
import org.json.simple.JSONObject;

/**
 *
 * @author zacknewsham
 */
public class GraphCanvas extends JLayeredPane implements MouseListener, MouseMotionListener, GraphViewerObserver, Progressive{

  protected GraphViewer graph;
  private Node clickedVariable = null;
  private BufferedImage buffer;

  // Layers
  private HighlightLayer highlightLayer;
  private NodeLayer nodeLayer;
  private EdgeLayer edgeLayer;

  public GraphCanvas(GraphViewer graph) {
    this.setLayout(new BorderLayout());
    this.setBackground(Color.BLACK);
    this.setPreferredSize(graph.getBounds().getSize());
    this.setSize(graph.getBounds().getSize());
    this.graph = graph;
    graph.addObserver(this);
    this.addMouseListener(this);
    this.addMouseMotionListener(this);
    this.setOpaque(true);

    setupLayers();
  }

  protected void setupLayers() {
    highlightLayer = createNewHighlightLayer();

    nodeLayer = createNewNodeLayer();
    this.add(nodeLayer);

    edgeLayer = createNewEdgeLayer();
    this.add(edgeLayer);
  }

  protected NodeLayer createNewNodeLayer() {
    return new NodeLayer(this.getSize(), graph);
  }

  protected HighlightLayer createNewHighlightLayer(){
    return new HighlightLayer(this.getSize(), graph);
  }
  
  protected EdgeLayer createNewEdgeLayer() {
    return new EdgeLayer(this.getSize(), graph);
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    if(d == null){
      d = new Dimension();
    }
    Dimension canvasBounds = GraphCanvasPanel.getCanvasDimensions();

    d.height = (int) (graph.getScale() * d.height);
    d.width = (int) (graph.getScale() * d.width);

    if (d.width < canvasBounds.width) {
      d.width = canvasBounds.width;
    }
    if (d.height < canvasBounds.height) {
      d.height = canvasBounds.height;
    }

    return d;
  }

  boolean drawnAll = false;
  private Node lastHighlighted = null;
  private void mouseHighlight(MouseEvent e) {
    boolean clicked = e.getClickCount() == 1;
    Node n = highlightLayer.getNodeAroundXY(e.getX(), e.getY());

    //e.getClickCount()
    if (this.clickedVariable == null || clicked) {
      if (n != null && !(!graph.getShowAssignedVars() && n.isAssigned())) {
        graph.selectNode(n);
        this.repaint();
      } else if (n == null && graph.getSelectedNode() != null) {
        graph.selectNode(null);
        this.repaint();
      }
    }

    if (clicked) {
      this.clickedVariable = n;
    }
    if(lastHighlighted != n){
      highlightLayer.highligted = n;
      repaint();
    }
    lastHighlighted = n;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    mouseHighlight(e);
  }

  @Override
  public void mousePressed(MouseEvent e) {
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  public void mouseDragged(MouseEvent e) {
  }

  public void mouseMoved(MouseEvent e) {
    mouseHighlight(e);
  }

  @Override
  public String JsonName() {
    return "canvas";
  }

  @Override
  public String toJson() {
    return "null";
  }

  @Override
  public void initFromJson(JSONObject o) {

  }

  private Area currentClip;
  private boolean isLocalUpdateRequired(){
    if(currentClip == null){
      return true;
    }
    else{
      return !currentClip.contains(this.getVisibleRect());
    }
  }
  
  @Override
  public void paint(Graphics g) {
    if (graph.isUpdateRequired() || isLocalUpdateRequired()) {
      if(currentClip == null){
        currentClip = new Area();
      }
      currentClip.add(new Area(g.getClipBounds()));
      if(buffer == null){
        buffer = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
      }
      Graphics g2 = buffer.createGraphics();
      g2.setClip(g.getClip());
      super.paint(g2);
    }
    g.drawImage(buffer, 0, 0, Color.BLACK, null);
    highlightLayer.paintComponent(g);
  }

  @Override
  public void notify(GraphViewer graph, GraphViewerObserver.Action action) {
    if (action == Action.setscale) {
      revalidate();
    } else if (action == Action.updatedEdges || action == Action.updatedNodes) {
      repaint();
    } else {
      repaint();
    }
  }

  public static RenderingHints getRenderingHints() {
    RenderingHints hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    return hints;
  }

  @Override
  public String getProgressionName() {
    return "Drawing Nodes";
  }

  @Override
  public double getProgress() {
    double avg = (nodeLayer.getProgress() + edgeLayer.getProgress()) / 2;
    return avg;
  }
}
