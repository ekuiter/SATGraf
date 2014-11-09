/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visual.graph;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.Timer;

import org.json.simple.JSONObject;

import visual.UI.GraphCanvas;
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
  private final List<Edge> xOrderedEdges = new ArrayList<Edge>();
  protected HashMap<String, TIntObjectHashMap<T>> nodeGroups = new HashMap<>();
  public final int OVERLAP = 50;
  private double scale = 0.4;
  //only required to quickly get bounds
  private boolean simpleConnections = false;
  protected final ArrayList yOrderedNodes = new ArrayList<T>();
  private final Timer actionTimer = new Timer(100, this);
  private boolean showAssignedVars = false;
  private Rectangle bounds = null;
  private boolean rectangleHasUpdate = false;
  
  private Node decisionVariable = null;
  
  private Collection<Node> updatedNodes = null;
  private HashMap<Edge, List<Point[]>> updatedEdges = new HashMap<Edge, List<Point[]>>();
  
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
  
  static boolean lineInsideRect(Rectangle r, int x0, int y0, int x1, int y1) {
	  return pointInsideRect(r, x0, y0) && pointInsideRect(r, x1, y1);
  }
  
  static boolean pointInsideRect(Rectangle r, int x, int y) {
	  return (r.x <= x && r.y <= y && r.x + r.width >= x && r.y + r.height >= y);
  }
  
  static boolean lineIntersectsRect(Rectangle r, int x0, int y0, int x1, int y1){
    boolean intersect;
    if(pointInsideRect(r, x0, y0)){
      return true;
    }
    if(pointInsideRect(r, x0, y0)){
      return true;
    }
    if(linesIntersect(x0, y0, x1, y1, r.x, r.y, r.x + r.width, r.y)){
      return true;
    }
    if(linesIntersect(x0, y0, x1, y1, r.x, r.y, r.x, r.y + r.height)){
      return true;
    }
    if(linesIntersect(x0, y0, x1, y1, r.x + r.width, r.y, r.x + r.width, r.y + r.height)){
      return true;
    }
    if(linesIntersect(x0, y0, x1, y1, r.x, r.y + r.height, r.x + r.width, r.y + r.height)){
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
    setUpdatedEdges(this.graph.getEdgesList());
    canvas.reset();
    canvas.revalidate();
  }
  public double getScale(){
    return scale;
  }
  
  public void setUpdatedNodes(Collection<Node> updatedNodes) {
	  if (this.updatedNodes == null) {
		  this.updatedNodes = updatedNodes;
	  } else {
		  synchronized (this.updatedNodes) {
			  for (Node n : updatedNodes) {
				  this.updatedNodes.add(n);
			  }
		  }
	  }
	  updateCanvas();
  }
  
  public void addUpdatedNode(Node n, NodeState s, boolean updateCanvas) {
	  n.setState(s);
	  
	  if (this.updatedNodes == null)
		  this.updatedNodes = new ArrayList<Node>();
	  
	  synchronized (this.updatedNodes) {
		  this.updatedNodes.add(n);
	  }
	  
	  if (updateCanvas)
		  updateCanvas();
  }
  
  public void setUpdatedEdges(Collection<Edge> updatedEdges) {
	  List<Edge> updatedEdgesCopy = new ArrayList<Edge>(updatedEdges);
	  
	  for (Edge e : updatedEdgesCopy) {
		  if (e == null) {
			  int o = 0;
		  }
		  
		  addUpdatedEdge(e, e.getState(), false);
	  }
	  
	  updateCanvas();
  }
  
  public void addUpdatedEdge(Edge e, EdgeState s, boolean updateCanvas) {
	  e.setState(s);
	  
	  synchronized (this.updatedEdges) {
		  Point ps[] = new Point[2];
		  ps[0] = new Point(e.getStart().getX(this), e.getStart().getY(this));
		  ps[1] = new Point(e.getEnd().getX(this), e.getEnd().getY(this));
		  List<Point[]> psList = new ArrayList<Point[]>();
		  psList.add(ps);
		  this.updatedEdges.put(e, psList);
	  }
	  
	  if (updateCanvas)
		  updateCanvas();
  }
  
  public Iterator<Edge> getEdgeIterator() {
	  return graph.getEdges();
  }
  
  public Iterator<Node> getNodeIterator() {
	  return graph.getNodeIterator();
  }
  
  public Rectangle getBounds(){
	  if (bounds == null) {
		//ensure we have an ordered list
		getOrderedUpdatedNodes(new Rectangle(0, 0, 2000, 2000), false);
		
		int minX = ((T)xOrderedNodes.get(0)).getX(this) - 50;
		int minY = ((T)yOrderedNodes.get(0)).getY(this) - 50;
		int maxX = ((T)xOrderedNodes.get(xOrderedNodes.size() - 1)).getX(this) + 50;
		int maxY = ((T)yOrderedNodes.get(yOrderedNodes.size() - 1)).getY(this) + 50;
		
		bounds = new Rectangle(minX, minY, maxX, maxY);
	  }
	  
	  return (Rectangle) bounds.clone();
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
	
	private void initOrderedNodes() {
		  if(xOrderedNodes.isEmpty()){
	        xOrderedNodes.clear();
	        yOrderedNodes.clear();
	    	xOrderedNodes.addAll(updatedNodes);
	    	yOrderedNodes.addAll(updatedNodes);
	        Collections.sort(xOrderedNodes, new Node.XComparator(this));
	        Collections.sort(yOrderedNodes, new Node.YComparator(this));
	      }
	}
	
	private void initOrderedEdges() {
	  if(xOrderedEdges.isEmpty()){
        xOrderedEdges.clear();
        xOrderedEdges.addAll(graph.getEdgesList());
        Collections.sort(xOrderedEdges, new Edge.XComparator(this));
      }
	}
	
	public ArrayList<Node> getOrderedUpdatedNodes(Rectangle bounds, boolean removeUpdatedNodes) {
		ArrayList<Node> xy = new ArrayList<Node>();
		xy.addAll(getNodesInBounds(bounds, removeUpdatedNodes));
		xy.addAll(determineIfEdgesCauseUpdate(bounds));
		
		if (this.rectangleHasUpdate && removeUpdatedNodes) {
			xy.addAll(getNodesViaEdgesInBounds(bounds));
		}
		    
		return xy;
	}
	
	private ArrayList<Node> determineIfEdgesCauseUpdate(Rectangle bounds) {
		ArrayList<Node> xy = new ArrayList<Node>();
		
		if (this.updatedEdges  != null) {
			// Check edges
			synchronized (this.updatedEdges) {
				boolean rectangleHasUpdate = false;
				HashMap<Edge, List<Point[]>> updatedEdgesCopy = new HashMap<Edge, List<Point[]>>(updatedEdges);
				
				for (Edge e : updatedEdgesCopy.keySet()) {
					List<Point[]> psListCopy = new ArrayList<Point[]>(updatedEdgesCopy.get(e));
					
					for (Point[] ps : psListCopy) {
						Point startPoint = ps[0];
						Point endPoint   = ps[1];
						
						if (lineIntersectsRect(bounds, startPoint.x, startPoint.y, endPoint.x, endPoint.y)) {
							rectangleHasUpdate = true;
							xy.add(e.getStart());
							
							List<Point[]> psList = updatedEdges.get(e);
							updateEdgeStartAndEndPoints(ps, bounds, psList);
							
							if (psList.size() == 0) {
								updatedEdges.remove(e);
							}
						}
					}
				}
				
				if (rectangleHasUpdate)
					this.rectangleHasUpdate = rectangleHasUpdate;
			}
		}
		
		return xy;
	}
	
	private ArrayList<Node> getNodesInBounds(Rectangle bounds, boolean removeUpdatedNodes) {
		ArrayList<Node> xy = new ArrayList<Node>();
		
		synchronized (this.updatedNodes) {
			boolean rectangleHasUpdate = false;
			initOrderedNodes();
		    ArrayList y = new ArrayList<Node>();
		    
		    int firstX = findFirstXIndex(xOrderedNodes, bounds.x - OVERLAP);
		    T prevNode = null;
		    
		    
	    	for(int i = firstX; i < xOrderedNodes.size(); i++){
	    		T xn = (T)xOrderedNodes.get(i);
	    		int xnx = xn.getX(this);
	    		int xny = xn.getY(this);
	    		
	    		if(xnx >= bounds.x && xny >= bounds.y && xny <= bounds.height + bounds.y){
	    			if(xnx <= bounds.width + bounds.x){
	    				if(xn.isVisible() && xn != prevNode){
	    					xy.add(xn);
	    					prevNode = xn;
	    					
	    					if (removeUpdatedNodes && updatedNodes.contains(xn)) {
	    						updatedNodes.remove(xn);
	    						rectangleHasUpdate = true;
	    					}
	    				}
	    			}
	    			else{
	    				break;
	    			}
	    		}
		    }
	    	
	    	this.rectangleHasUpdate = rectangleHasUpdate;
		}
		
		return xy;
	}
	
	private ArrayList<Node> getNodesViaEdgesInBounds(Rectangle bounds) {
		ArrayList<Node> xy = new ArrayList<Node>();
		initOrderedEdges();
		
	    for (Edge e : xOrderedEdges) {
	    	Node n = e.getLeft(this);
	    	
	    	if (n.getX(this) > bounds.x + bounds.width) {
	    		break;
	    	} else  {
	    		Node n1 = e.getStart();
	    		Node n2 = e.getEnd();
	    		
	    		if (lineIntersectsRect(bounds, n1.getX(this), n1.getY(this), n2.getX(this), n2.getY(this))) {
	    			xy.add(n1);
	    		}
	    	}
	    }
		
		return xy;
	}
	
	private void updateEdgeStartAndEndPoints(Point[] ps, Rectangle bounds, List<Point[]> psList) {
		Point startPoint = ps[0];
		Point endPoint   = ps[1];
		
		psList.remove(ps);
		
		if (!lineInsideRect(bounds, startPoint.x, startPoint.y, endPoint.x, endPoint.y)) {
			// Update end points to no longer be inside of the rectangle
			splitEdgeViaPoint(ps, bounds, psList);
		}
	}
	
	/**
	 * Will look at the given point which should be outside of the rectangle and return the same edge with new
	 * starting and end points outside of the rectangle.
	 * 
	 * @param e
	 * @param r
	 * @param p
	 * @return
	 */
	private void splitEdgeViaPoint(Point[] ps, Rectangle bounds, List<Point[]> psList) {
		// Top bounds edge
		Point newPoint = checkBoundsEdge(ps, bounds.x, bounds.y, bounds.x + bounds.width, bounds.y);
		
		if (newPoint != null) {
			Point[] ret = new Point[2];
			ret[0] = ps[0].y < ps[1].y ? ps[0] : ps[1];
			ret[1] = newPoint;
			psList.add(ret);
		}
		
		// Right bounds edge
		newPoint = checkBoundsEdge(ps, bounds.x + bounds.width, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
		
		if (newPoint != null) {
			Point[] ret = new Point[2];
			ret[0] = ps[0].x > ps[1].x ? ps[0] : ps[1];
			ret[1] = newPoint;
			psList.add(ret);
		}
		
		// Bottom bounds edge
		newPoint = checkBoundsEdge(ps, bounds.x + bounds.width, bounds.y + bounds.height, bounds.x, bounds.y + bounds.height);
		
		if (newPoint != null) {
			Point[] ret = new Point[2];
			ret[0] = ps[0].y > ps[1].y ? ps[0] : ps[1];
			ret[1] = newPoint;
			psList.add(ret);
		}
				
		// Left bounds edge
		newPoint = checkBoundsEdge(ps, bounds.x, bounds.y + bounds.height, bounds.x, bounds.y);
		
		if (newPoint != null) {
			Point[] ret = new Point[2];
			ret[0] = ps[0].x < ps[1].x ? ps[0] : ps[1];
			ret[1] = newPoint;
			psList.add(ret);
		}
	}
	
	private Point checkBoundsEdge(Point[] ps, int x1, int y1, int x2, int y2) {
		Point boundsStart = new Point(x1, y1);
		Point boundsEnd = new Point(x2, y2);
		Point newPoint = findIntersectionPointBetweenTwoLines(ps[0], ps[1], boundsStart, boundsEnd);
		
		if (!isOnLineSegment(newPoint, x1, y1, x2, y2))
			return null;
		
		if (!isOnLineSegment(newPoint, ps[0].x, ps[0].y, ps[1].x, ps[1].y))
			return null;
		
		return newPoint;
	}
	
	private boolean isOnLineSegment(Point newPoint, int x1, int y1, int x2, int y2) {
		if (newPoint == null)
			return false;
		
		// Check that it is not outside of the bounds of the lines
		int lesserX = x1 > x2 ? x2 : x1;
		int greaterX = x1 > x2 ? x1 : x2;
		int lesserY = y1 > y2 ? y2 : y1;
		int greaterY = y1 > y2 ? y1 : y2;
		
		if (newPoint.x < lesserX || newPoint.x > greaterX)
			return false;
		
		if (newPoint.y < lesserY || newPoint.y > greaterY)
			return false;
		
		return true;
		
	}
	
	private Point findIntersectionPointBetweenTwoLines(Point a1, Point a2, Point c1, Point c2) {
		// Vertical check
		boolean vert1 = a1.x == a2.x;
		boolean vert2 = c1.x == c2.x;
		
		if (vert1 && vert2) {
			// What if the lines are overlapping...
			return null;
		}
		
		// Building y = mx + b
		double m1 = 0, m2 = 0, b1 = 0, b2 = 0;
		if (!vert1) {
			m1 = ((double)a2.y - (double)a1.y)/((double)a2.x - (double)a1.x);
			b1 = (double)a1.y - m1 * (double)a1.x;
		}
		
		if (!vert2) {
			m2 = ((double)c2.y - (double)c1.y)/((double)c2.x - (double)c1.x);
			b2 = (double)c1.y - m2 * (double)c1.x;
		}
		
		if (m1 == m2) {
			// They are perfectly parallel...
			return null;
		}
		
		double x, y;
		if (!vert1 && !vert2) {
			// m1*x + b1 = m2*x + b2 and solve for x
			x = (b2 - b1) / (m1 - m2);
			// Use original equation to find y
			y = m1*x + b1;
		} else {
			if (vert1) {
				x = a1.x;
				y = m2*x + b2;
			} else {
				x = c1.x;
				y = m1*x + b1;
			}
		}
		
		return new Point((int)x, (int)y);
	}
	
	public void setShowAssignedVars(boolean show) {
		this.showAssignedVars = show;
	}
	
	public boolean getShowAssignedVars() {
		return this.showAssignedVars;
	}
	
	public void setDecisionVariable(Node n) {
		this.decisionVariable = n;
	}
	
	public Node getDecisionVariable() {
		return this.decisionVariable;
	}
	
	public void clearDecisionVariable() {
		this.decisionVariable = null;
	}
	
	public boolean showEdge(Edge e) {
		if (e.getAssignmentState() == EdgeState.HIDE || e.getState() == EdgeState.HIDE) {
			return false;
		} else if (!getShowAssignedVars() && (e.getStart().isAssigned() || e.getEnd().isAssigned())) {
			return false;
		}
		
		return true;
	}
	
	public boolean doesRectangleHaveUpdate() {
		return this.rectangleHasUpdate;
	}
}
