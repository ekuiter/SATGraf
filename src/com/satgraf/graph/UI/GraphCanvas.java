/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.graph.UI;

import static com.satgraf.graph.UI.GraphCanvas.HIGHLIGHT_COLOR;

import com.satgraf.UI.PaintThread;
import com.satgraf.UI.ThreadPaintable;
import com.satlib.graph.DrawableNode;
import com.satlib.graph.Edge;
import com.satlib.graph.Edge.EdgeState;
import com.satlib.graph.GraphObserver;
import com.satlib.graph.GraphViewer;
import com.satlib.graph.Node;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import org.json.simple.JSONObject;

import sun.java2d.pipe.DrawImage;

/**
 *
 * @author zacknewsham
 */
public abstract class GraphCanvas extends JTable implements MouseListener, MouseMotionListener, ThreadPaintable, GraphObserver{
  protected GraphViewer graph;
  public static Color HIGHLIGHT_COLOR = new Color(0xe4, 0xfd, 0x03);
  public static int FRAME_WIDTH = 500;
  public static int FRAME_HEIGHT = 250;
  private static ArrayList<TiledImage> images = new ArrayList<TiledImage>();
  private boolean forceDraw = true;
  
  public GraphCanvas(GraphViewer graph){
    super(new GraphTableModel(graph));
    this.setRowHeight(FRAME_HEIGHT);
    for(int i = 0; i < this.getModel().getColumnCount(); i++){
      this.getColumnModel().getColumn(i).setMinWidth(FRAME_WIDTH);
    }
    this.setPreferredSize(graph.getBounds().getSize());
    this.setSize(graph.getBounds().getSize());
    this.setAutoResizeMode(AUTO_RESIZE_OFF);
    this.setTableHeader(null);
    this.graph = graph;
    graph.addObserver(this);
    this.addMouseListener(this);
    this.addMouseMotionListener(this);
  }
  
  public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    Dimension canvasBounds = GraphCanvasPanel.getCanvasDimensions();
    
    d.height = (int)(graph.getScale() * d.height);
    d.width = (int)(graph.getScale() * d.width);
    
    if (d.width < canvasBounds.width) {
    	d.width = canvasBounds.width;
    }
    if (d.height < canvasBounds.height) {
    	d.height = canvasBounds.height;
    }
    
    return d;
  }

  boolean drawnAll = false;
  
  public void reset(){
    images = null;
    forceDraw = true;
  }
  
  protected void drawNode(Node n, Rectangle o, Graphics image) {	  
	if (!graph.getShowAssignedVars() && n.isAssigned())
		return;
	
	drawNodeWithColor(n, o, image, n.getColor(graph), n.getFillColor(graph));
  }
  
  public void drawNodeWithColor(Node n, Rectangle o, Graphics image, Color color, Color fillColor) {
	  Rectangle i = image.getClipBounds();
	  int x = n.getX(graph) - (o.x - i.x);
	  int y = n.getY(graph) - (o.y - i.y);
	  image.setColor(color);
	  image.drawArc(x, y, DrawableNode.NODE_DIAMETER, DrawableNode.NODE_DIAMETER, 0, 360);
	  image.setColor(fillColor);
	  image.fillArc(x, y, DrawableNode.NODE_DIAMETER, DrawableNode.NODE_DIAMETER, 0, 360);
  }

  protected void drawConnection(Edge c, Rectangle o,Graphics image) {
	if (!graph.shouldShowEdge(c))
		return;
	  
    Rectangle i = image.getClipBounds();
    if(c.getStart().isVisible() && c.getEnd().isVisible()){
      Graphics2D g2d = (Graphics2D) image.create();
      int startX = c.getStart().getX(graph) + DrawableNode.NODE_DIAMETER / 2;
      int startY = c.getStart().getY(graph) + DrawableNode.NODE_DIAMETER / 2;
      int endX = c.getEnd().getX(graph) + DrawableNode.NODE_DIAMETER / 2;
      int endY = c.getEnd().getY(graph) + DrawableNode.NODE_DIAMETER / 2;
      
      g2d.setRenderingHints(getRenderingHints());
      g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g2d.drawLine(
          startX - (o.x - i.x) , 
          startY - (o.y - i.y),
          endX - (o.x - i.x) ,
          endY - (o.y - i.y)
      );
    }
  }
  
  private RenderingHints getRenderingHints() {
      RenderingHints hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      return hints;
  }
  
  private void mouseHighlight(MouseEvent e) {
	Node n = graph.getNodeAtXY(e.getX(), e.getY());
    if(n != null && !(!graph.getShowAssignedVars() && n.isAssigned())){
    	if (graph.getSelectedNode() == n) {
    		graph.selectNode(null);
    	} else {
    		graph.selectNode(n);
    	}
    	this.repaint();
    } else if (graph.getSelectedNode() != null) {
    	graph.selectNode(null);
    	this.repaint();
    }
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
     
  public void mouseDragged(MouseEvent e){
  }
  
  public void mouseMoved(MouseEvent e){
	  mouseHighlight(e);
  }
  
  @Override
  public String JsonName(){
    return "canvas";
  }
  
  @Override 
  public String toJson(){
    return "null";
  } 
  
  @Override
  public void initFromJson(JSONObject o){
    
  }
  
  @Override
  public void notify(GraphViewer graph, GraphObserver.Action action){
    if(action == Action.setscale){
      reset();
      revalidate();
    }
    else if(action == Action.updatedEdges || action == Action.updatedNodes){
      repaint();
    }
    else{
      repaint();
    }
  }
  
  protected TiledImage getImageFromTable(int row, int column) {	
	if (images == null) {
		images = new ArrayList<TiledImage>();
		return null;
	}
	  
    for (int i = 0; i < images.size(); i++) {
      TiledImage aImage = images.get(i);
      if (aImage.row == row && aImage.column == column) {
        return aImage;
      }
    }
    
    return null;
  }
  
  private int getNumRows() {
	  return (int) Math.ceil(this.getSize().height / FRAME_HEIGHT) + 1; 
  }
  
  private int getNumColumns() {
	  return (int) Math.ceil(this.getSize().width / FRAME_WIDTH) + 1; 
  }

  private TiledImage createNewImage(int row, int column, GraphCanvas canvas) {
	TiledImage image = null;
    Point origin = new Point(column * (int) (FRAME_WIDTH / graph.getScale()), row * (int) (FRAME_HEIGHT / graph.getScale()));

    image = new TiledImage((int) (FRAME_WIDTH / graph.getScale()), (int) (FRAME_HEIGHT / graph.getScale()), BufferedImage.TYPE_INT_ARGB, origin, new Dimension((int) (FRAME_WIDTH / graph.getScale()), (int) (FRAME_HEIGHT / graph.getScale())));
    Graphics2D g1 = image.createGraphics();
    g1.setClip(0, 0, (int) (FRAME_WIDTH / graph.getScale()), (int) (FRAME_HEIGHT / graph.getScale()));
    image.setGraphics(g1);
    image.row = row;
    image.column = column;
    images.add(image);
    
    return image;
  }
  
  @Override
  public void paintComponent(Graphics g) {
	  Graphics2D g2 = (Graphics2D) g;
	  g2.setRenderingHints(getRenderingHints());
	  
	  int numRows = getNumRows();
	  int numColumns = getNumColumns();
	  int numThreads = numRows * numColumns;
	  PaintThread[] threads = new PaintThread[numThreads];
	  
	  // Build images asynchronously
	  for (int i = 0; i < numThreads; i++) {
		  int row = i / numColumns;
		  int column = i % numColumns;
		  
		  TiledImage image = getImageFromTable(row, column);

		  if (image == null) {
		    image = createNewImage(row, column, this);
		  }
		  
		  threads[i] = new PaintThread(this, new Rectangle(column * (int) (FRAME_WIDTH / graph.getScale()), row * (int) (FRAME_HEIGHT / graph.getScale()), (int) (FRAME_WIDTH / graph.getScale()), (int) (FRAME_HEIGHT / graph.getScale())), image, forceDraw);
		  threads[i].start();
	  }
	  
	  // Draw the images built
	  for (int i = 0; i < numThreads; i++) {
		  int row = i / numColumns;
		  int column = i % numColumns;
		  
		  // Wait for thread first
		  try {
			threads[i].join();
		  } catch (InterruptedException e) {
			e.printStackTrace();
		  }
		  
		  TiledImage image = getImageFromTable(row, column);
		  drawGraphics(g2, image, row, column);
	  }
	  
	  drawNodeHighlight(g, g2);
	  forceDraw = false;
  }
  
  public void drawGraphics(Graphics2D g2, TiledImage image, int row, int column) {
	  g2.drawImage(image, new AffineTransformOp(AffineTransform.getScaleInstance(FRAME_WIDTH / (double) image.getWidth(), FRAME_HEIGHT / (double) image.getHeight()), AffineTransformOp.TYPE_BICUBIC), column * FRAME_WIDTH, row * FRAME_HEIGHT);
  }

  private void drawNodeHighlight(Graphics g, Graphics2D g2) {
    Node n = graph.getSelectedNode();
    Point pos = getMousePosition();
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
        int x1 = (int) ((e.getStart().getX(graph)) * graph.getScale());
        int y1 = (int) ((e.getStart().getY(graph)) * graph.getScale());
        int x2 = (int) ((e.getEnd().getX(graph)) * graph.getScale());
        int y2 = (int) ((e.getEnd().getY(graph)) * graph.getScale());
        if ((x1 >= 0 && y1 >= 0 && x1 <= bounds.width && y1 <= bounds.height)
                || (x2 >= 0 && y2 >= 0 && x2 <= bounds.width && y2 <= bounds.height)) {
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
      int x = (int) ((n.getX(graph)) * graph.getScale());
      int y = (int) ((n.getY(graph)) * graph.getScale());
      g.fillArc(x,
              y,
              scaled_diameter,
              scaled_diameter, 0, 360);
      g.setColor(Color.BLACK);
      g.drawArc(x,
              y,
              scaled_diameter,
              scaled_diameter, 0, 360);
      if (getMousePosition() == null || n == graph.getNodeAtXY(getMousePosition().x, getMousePosition().y)) {
        n = null;
      } else {
        n = graph.getNodeAtXY(getMousePosition().x, getMousePosition().y);
      }
    }
  }
}
