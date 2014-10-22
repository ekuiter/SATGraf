/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visual.graph;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.Timer;

import org.json.simple.JSONObject;

import visual.UI.GraphCanvas;
import visual.UI.GraphCanvasPanel;
import visual.UI.GraphOptionsPanel;
import visual.graph.Edge.EdgeState;
import visual.graph.Node.NodeState;

/**
 *
 * @author zacknewsham
 * @param <T>
 * @param <T1>
 */
public abstract class GraphViewer<T extends Node, T1 extends Edge> implements ActionListener {
  protected Graph graph;
  private T selectedNode;
  protected GraphCanvas canvas;
  protected GraphOptionsPanel panel;
  protected final HashMap<String, TIntObjectHashMap<String>> node_lists;
  protected final ArrayList xOrderedNodes = new ArrayList<T>();
  protected HashMap<String, TIntObjectHashMap<T>> nodeGroups = new HashMap<>();
  public final int OVERLAP = 50;
  private double scale = 0.4;
  //only required to quickly get bounds
  private boolean simpleConnections = false;
  protected final ArrayList yOrderedNodes = new ArrayList<T>();
  private final Timer actionTimer = new Timer(100, this);
  private boolean showAssignedVars = false;
  
  private Collection<Node> updatedNodes = null;
  
  public GraphViewer(Graph graph, HashMap<String, TIntObjectHashMap<String>> node_lists){
    this.node_lists = node_lists;
    this.graph = graph;
    setUpdatedNodes(graph.getNodesList());
  }
  public abstract String toJson();
  public abstract void fromJson(JSONObject json);
  
  public abstract void init();
  protected abstract Graph getGraph();
  public void setGraphCanvas(GraphCanvas canvas){
    this.canvas = canvas;
  }
  public void setGraphPanel(GraphOptionsPanel panel){
    this.panel = panel;
  }
  
  public GraphCanvas getGraphCanvas(){
    return canvas;
  }
  
  public GraphOptionsPanel getOptionsPanel(){
    return panel;
  }
  
  @Override
  public void actionPerformed(ActionEvent e) {
	if (canvas == null || panel == null)
		return;
	  
	canvas.repaint();
    panel.update();
    actionTimer.stop();
  }
  
  public void updateCanvas() {
	  actionTimer.start();
  }
  
  private int findLastYIndex(ArrayList<HasGraphPosition> nodes, int y){
    int low = 0;
    int high = nodes.size();
    int middle = (high - low) / 2;
    if(y <= nodes.get(0).getY(this)){
      return 0;
    }
    while(high != low){
      HasGraphPosition n = nodes.get(middle);
      if(n.getY(this) > y){
        high = middle - 1; 
      }
      else if(n.getY(this) < y){
        low = middle + 1;
      }
      else{
        if(nodes.get(middle + 1).getY(this) != n.getY(this)){
          return middle;
        }
        else{
          low = middle;
        }
      }
      if(low == middle || high == middle){
        return middle;
      }
      middle = low + (high - low) / 2;
    }
    return 0;
  }
  
  private int findFirstYIndex(ArrayList<HasGraphPosition> nodes, int y){
    int low = 0;
    int high = nodes.size();
    int middle = (high - low) / 2;
    if(y <= nodes.get(0).getY(this)){
      return 0;
    }
    while(high != low){
      HasGraphPosition n = nodes.get(middle);
      if(n.getY(this) > y){
        high = middle - 1; 
      }
      else if(n.getY(this) < y){
        low = middle + 1;
      }
      else{
        if(nodes.get(middle - 1).getY(this) != n.getY(this)){
          return middle;
        }
        else{
          high = middle;
        }
      }
      if(low == middle || high == middle){
        return middle;
      }
      middle = low + (high - low) / 2;
    }
    return 0;
  }
  
  private int findFirstXIndex(ArrayList<HasGraphPosition> nodes, int x){
    int low = 0;
    int high = nodes.size();
    int middle = (high - low) / 2;
    if(x <= nodes.get(0).getX(this)){
      return 0;
    }
    while(high != low && middle >= 0 && middle < nodes.size()){
      HasGraphPosition n = nodes.get(middle);
      if(n.getX(this) > x){
        high = middle - 1; 
      }
      else if(n.getX(this) < x){
        low = middle + 1;
      }
      else{
        if(nodes.get(middle - 1).getX(this) != n.getX(this)){
          return middle;
        }
        else{
          high = middle - 1;
        }
      }
      if(low == middle || high == middle){
        return middle;
      }
      middle = low + (int)Math.ceil((double)(high - low) / 2);
    }
    return 0;
  }
  
  
  static boolean linesIntersect(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3){
    int s1_x, s1_y, s2_x, s2_y;
    s1_x = x1 - x0;     
    s1_y = y1 - y0;
    s2_x = x3 - x2;     
    s2_y = y3 - y2;
    float det = (-s2_x * s1_y + s1_x * s2_y);
    if(det < Math.E && det > 0 || (0 - det < Math.E && det < 0)){
      return false;
    }
    float s, t;
    s = (-s1_y * (x0 - x2) + s1_x * (y0 - y2)) / det;
    t = ( s2_x * (y0 - y2) - s2_y * (x0 - x2)) / det;

    if (s >= 0 && s <= 1 && t >= 0 && t <= 1){
        return true;
    }
    return false; // No collision
  }
  static boolean lineIntersectsRect(Rectangle r, int x0, int y0, int x1, int y1){
    boolean intersect;
    if(r.x <= x0 && r.y <= y0 && r.x + r.width >= x0 && r.y + r.height >= y0){
      return true;
    }
    if(r.x <= x1 && r.y <= y1 && r.x + r.width >= x1 && r.y + r.height >= y1){
      return true;
    }
    intersect = linesIntersect(x0, y0, x1, y1, r.x, r.y, r.x + r.width, r.y);
    if(intersect){
      return true;
    }
    intersect = linesIntersect(x0, y0, x1, y1, r.x, r.y, r.x, r.y + r.height);
    if(intersect){
      return true;
    }
    intersect = linesIntersect(x0, y0, x1, y1, r.x + r.width, r.y, r.x + r.width, r.y + r.height);
    if(intersect){
      return true;
    }
    intersect = linesIntersect(x0, y0, x1, y1, r.x, r.y + r.height, r.x + r.width, r.y + r.height);
    if(intersect){
      return true;
    }
    return false;
  }
  private final Object sem = new Object();

  public void showConnection(T1 conn){
    if((conn.getType() & Edge.REAL) == Edge.REAL){
    	addUpdatedEdge(conn, EdgeState.SHOW, true);
    }
  }
  public void hideEdge(T1 conn){
    if((conn.getType() & Edge.REAL) == Edge.REAL){
    	addUpdatedEdge(conn, EdgeState.HIDE, true);
    }
  }
  public void showEdgeSet(String set){
	  Iterator<T> ns = graph.getNodes(set);
	  while(ns.hasNext()){
	    T n = ns.next();
	    Iterator<T1> edges = n.getEdges();
	    while(edges.hasNext()){
	      T1 e = edges.next();
	      addUpdatedEdge(e, EdgeState.SHOW, true);
	    }
	  }
  }
  public void hideEdgeSet(String set){
	  Iterator<T> ns = graph.getNodes(set);
	  while(ns.hasNext()){
	    T n = ns.next();
	    Iterator<T1> edges = n.getEdges();
	    while(edges.hasNext()){
	      T1 e = edges.next();
	      addUpdatedEdge(e, EdgeState.HIDE, true);
	    }
	  }
  }
  public void showNodeSet(String set){
      Iterator<T> ns = graph.getNodes(set);
      while(ns.hasNext()){
        T n = ns.next();
        addUpdatedNode(n, NodeState.SHOW, true);
      }
  }
  public void hideNodeSet(String set){
      Iterator<T> ns = graph.getNodes(set);
      while(ns.hasNext()){
        T n = ns.next();
        addUpdatedNode(n, NodeState.HIDE, true);
      }
  }
  public void showNode(T n){
	  addUpdatedNode(n, NodeState.SHOW, true);
  }
  public void hideNode(T n){
	  addUpdatedNode(n, NodeState.HIDE, true);
  }
  
  public abstract T getNodeAtXY(int x, int y);
  protected abstract int getX(T node);
  protected abstract int getY(T node);
  protected abstract Color getFillColor(T node);
  protected abstract Color getColor(T node);
  protected abstract Color getColor(T1 e);
  
  public void selectNode(T node){
    this.selectedNode = node;
    this.panel.getNodePanel().setNode(node);
    this.panel.getNodePanel().update();
  }
  public T getSelectedNode(){
    return this.selectedNode;
  }

  public Edge getEdge(T a, T b) {
    return graph.createEdge(a, b, false);
  }
  
  public void setScale(double scale){
    this.scale = scale;
    setUpdatedNodes(this.graph.getNodesList());
    canvas.reset();
  }
  public double getScale(){
    return scale;
  }
  
  public void setUpdatedNodes(Collection<Node> updatedNodes) {
	  if (this.updatedNodes == null) {
		  this.updatedNodes = updatedNodes;
	  } else {
		  for (Node n : updatedNodes) {
			  this.updatedNodes.add(n);
		  }
	  }
	  updateCanvas();
  }
  
  public void addUpdatedNode(Node n, NodeState s, boolean updateCanvas) {
	  n.setState(s);
	  
	  if (this.updatedNodes == null)
		  this.updatedNodes = new ArrayList<Node>();
	  
	  this.updatedNodes.add(n);
	  
	  if (updateCanvas)
		  updateCanvas();
  }
  
  public void setUpdatedEdges(Collection<Edge> updatedEdges) {
	  for (Edge e : updatedEdges) {
		  addUpdatedEdge(e, e.getState(), false);
	  }
	  
	  updateCanvas();
  }
  
  public void addUpdatedEdge(Edge e, EdgeState s, boolean updateCanvas) {
	  Node n1 = e.getStart();
	  Node n2 = e.getEnd();
	  
	  addUpdatedNode(n1, n1.getState(), updateCanvas);
	  addUpdatedNode(n2, n2.getState(), updateCanvas);
	  
	  e.setState(s);
  }
  
  public Collection<Node> getUpdatedNodesAndSetToNull() {
	  Collection<Node> updatedNodes = this.updatedNodes;
	  this.updatedNodes = null;
	  
	  return updatedNodes;
  }
  
  public Iterator<Edge> getEdgeIterator() {
	  return graph.getEdges();
  }
  
  public Iterator<Node> getNodeIterator() {
	  return graph.getNodeIterator();
  }
  
  public Rectangle getBounds(){
	//ensure we have an ordered list
	getOrderedUpdatedNodes(new Rectangle(0, 0, 1000, 1000));
	
	int minX = ((T)xOrderedNodes.get(0)).getX(this) - 50;
	int minY = ((T)yOrderedNodes.get(0)).getY(this) - 50;
	int maxX = ((T)xOrderedNodes.get(xOrderedNodes.size() - 1)).getX(this) + 50;
	int maxY = ((T)yOrderedNodes.get(yOrderedNodes.size() - 1)).getY(this) + 50;
	
	return new Rectangle(minX, minY, maxX, maxY);
  }	
  
	public Iterator<T> getNodes(String group){
	    if(nodeGroups.get(group) == null){
	      TIntObjectHashMap<T> nodes = new TIntObjectHashMap<T>();
	      nodeGroups.put(group, nodes);
	      Iterator<T> gnodes = graph.getNodeIterator();
	      while(gnodes.hasNext()){
	        T next = gnodes.next();
	        if(next.inGroup(group)){
	          nodes.put(next.getId(), next);
	        }
	      }
	    }
	    return nodeGroups.get(group).valueCollection().iterator();
	}
	
	public ArrayList<Node> getOrderedUpdatedNodes(Rectangle bounds) {
		 if(xOrderedNodes.isEmpty()){
		        xOrderedNodes.clear();
		        yOrderedNodes.clear();
		        xOrderedNodes.addAll(updatedNodes);
		        Collections.sort(xOrderedNodes, new Node.XComparator(this));
		        yOrderedNodes.addAll(updatedNodes);
		        Collections.sort(yOrderedNodes, new Node.YComparator(this));
		      }
		      
		    ArrayList y = new ArrayList<Node>();
		    
		    ArrayList<Node> xy = new ArrayList<Node>();
		    int firstX = findFirstXIndex(xOrderedNodes, bounds.x - OVERLAP);
		    T prevNode = null;
		    
		    for(int i = firstX; i < xOrderedNodes.size(); i++){
		      T xn = (T)xOrderedNodes.get(i);
		      if(getX(xn) >= bounds.x && xn.getY(this) >= bounds.y - OVERLAP && xn.getY(this) <= bounds.y + bounds.height + OVERLAP){
		        if(getX(xn) <= bounds.x + bounds.width + OVERLAP){
		          if(xn.isVisible() && xn != prevNode){
		            xy.add(xn);
		            prevNode = xn;
		          }
		        }
		        else{
		          break;
		        }
		      }
		    }
		    
		    return xy;
	}
	
	public ArrayList<Node> getOrderedUpdatedNodesAndSetToNull(Rectangle bounds){
		ArrayList<Node> xy = getOrderedUpdatedNodes(bounds);
	    
	    updatedNodes = null;
	    return xy;
	  }
  
	public void setShowAssignedVars(boolean show) {
		this.showAssignedVars = show;
	}
	
	public boolean getShowAssignedVars() {
		return this.showAssignedVars;
	}
}
