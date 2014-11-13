/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.graph.UI;

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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import org.json.simple.JSONObject;

/**
 *
 * @author zacknewsham
 */
public abstract class GraphCanvas extends JTable implements MouseListener, MouseMotionListener, ThreadPaintable, GraphObserver{
  protected GraphViewer graph;
  protected GraphCanvasRenderer renderer;
  public static Color HIGHLIGHT_COLOR = new Color(0xe4, 0xfd, 0x03);
  
  public GraphCanvas(GraphViewer graph){
    super(new GraphTableModel(graph));
    renderer = new GraphCanvasRenderer(this, graph);
    this.setRowHeight(GraphCanvasRenderer.FRAME_HEIGHT);
    for(int i = 0; i < this.getModel().getColumnCount(); i++){
      this.getColumnModel().getColumn(i).setMinWidth(GraphCanvasRenderer.FRAME_WIDTH);
    }
    this.setPreferredSize(graph.getBounds().getSize());
    this.setSize(graph.getBounds().getSize());
    this.setAutoResizeMode(AUTO_RESIZE_OFF);
    this.setTableHeader(null);
    this.graph = graph;
    graph.addObserver(this);
    this.addMouseListener(this);
    this.addMouseMotionListener(this);
    this.setDefaultRenderer(Object.class, renderer);
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
  
  /*@Override
  public void paintComponent(Graphics g) {
    if(drawnAll == false){
      Rectangle bounds = graph.getBounds();
      this.setSize(bounds.getSize());
      this.setPreferredSize(bounds.getSize());
      //this.setScale(graph.getScale());
      drawnAll = true;
    }
    super.paintComponent(g);
    //overlay for selection.
    
  }*/
  
  public void reset(){
    renderer.reset();
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
      
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g2d.drawLine(
          startX - (o.x - i.x) , 
          startY - (o.y - i.y),
          endX - (o.x - i.x) ,
          endY - (o.y - i.y)
      );
    }
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    Node n = graph.getNodeAtXY(e.getX(), e.getY());
    if(n != null){
    	if (graph.getSelectedNode() == n) {
    		graph.selectNode(null);
    	} else {
    		graph.selectNode(n);
    	}
    }
    this.repaint();
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
    repaint(new Rectangle(0,0,this.getWidth(), this.getHeight()));
  }
  
  @Override
  public String JsonName(){
    return "canvas";
  }
  
  @Override 
  public String toJson(){
    return "";
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
}
