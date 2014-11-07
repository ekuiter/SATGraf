/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visual.UI;

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

import visual.graph.DrawableNode;
import visual.graph.Edge;
import visual.graph.Edge.EdgeState;
import visual.graph.GraphViewer;
import visual.graph.Node;

/**
 *
 * @author zacknewsham
 */
public abstract class GraphCanvas extends JTable implements MouseListener, MouseMotionListener, ThreadPaintable{
  protected GraphViewer graph;
  private GraphCanvasRenderer renderer;
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
    graph.setGraphCanvas(this);
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
  
  /*public void paintComponent(Graphics g){
    if(lastPaint.x < g.getClipBounds().x){
      generateNewFrames(g, ThreadPaintablePanel.SCROLL_DIRECTION.EAST);
    }
    else if(lastPaint.y < g.getClipBounds().y){
      generateNewFrames(g, ThreadPaintablePanel.SCROLL_DIRECTION.SOUTH);
    }
    lastPaint = new Point(g.getClipBounds().x, g.getClipBounds().y);
    if(image == null){
      callPaintThread(g);
    }
    ArrayList<TiledImage> required = getRequiredFrames(g);
    for(int i = 0; i < required.size(); i++){
      if(required.get(i) != null){
        while(!required.get(i).getFinished()){
          try {
            Thread.sleep(1);
          } catch (InterruptedException ex) {
            Logger.getLogger(ThreadPaintablePanel.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
        g.drawImage(required.get(i), required.get(i).origin.x, required.get(i).origin.y, this);
      }
    }
  }*/

  protected void drawConnection(Edge c, Rectangle o,Graphics image) {
	if (!graph.showEdge(c))
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
}
