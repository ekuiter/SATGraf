/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.graph.UI;

import com.satgraf.graph.placer.Placer;
import com.satlib.graph.Edge;
import com.satlib.graph.Edge.EdgeState;
import com.satlib.graph.Graph;
import com.satlib.graph.Node;
import com.satlib.graph.Node.NodeState;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.json.simple.JSONObject;

/**
 *
 * @author zacknewsham
 * @param <T>
 * @param <T1>
 */
public abstract class GraphViewer<T extends Node, T1 extends Edge> implements ActionListener {

  protected Placer placer;
  public Graph graph;
  private T selectedNode;
  //protected GraphCanvas canvas;
  //protected GraphOptionsPanel panel;
  protected final HashMap<String, TIntObjectHashMap<String>> node_lists;
  protected final ArrayList xOrderedNodes = new ArrayList<T>();
  protected HashMap<String, TIntObjectHashMap<T>> nodeGroups = new HashMap<>();
  public final int OVERLAP = 50;
  private double scale = 0.4;
  //only required to quickly get bounds
  protected final ArrayList yOrderedNodes = new ArrayList<T>();
  private boolean showAssignedVars = false;
  private Rectangle bounds = null;

  protected Collection<GraphViewerObserver> observers = new HashSet<>();
  private final Collection<Node> updatedNodes = new ArrayList<Node>();
  private HashMap<Edge, List<Point[]>> updatedEdges = new HashMap<Edge, List<Point[]>>();
  
  private synchronized void edgeEvent(){
	  notifyObservers(GraphViewerObserver.Action.updatedEdges);
  }
  
  public GraphViewer(Graph graph, HashMap<String, TIntObjectHashMap<String>> node_lists, Placer placer) {
    this.node_lists = node_lists;
    this.graph = graph;
    placer = placer;
    if(placer != null){
      init();
    }
  }

  public abstract String toJson();

  public abstract void fromJson(JSONObject json);

  public abstract void init();

  public abstract Graph getGraph();

  public void addObserver(GraphViewerObserver observer) {
    observers.add(observer);
  }

  public void notifyObservers(GraphViewerObserver.Action action) {
    for (GraphViewerObserver observer : observers) {
      observer.notify(this, action);
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (observers.size() < 2) {
      return;
    }
    notifyObservers(GraphViewerObserver.Action.actionPerformed);
  }

  static boolean linesIntersect(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3) {
    int s1_x, s1_y, s2_x, s2_y;
    s1_x = x1 - x0;
    s1_y = y1 - y0;
    s2_x = x3 - x2;
    s2_y = y3 - y2;
    float det = (-s2_x * s1_y + s1_x * s2_y);
    if (det < Math.E && det > 0 || (0 - det < Math.E && det < 0)) {
      return false;
    }
    float s, t;
    s = (-s1_y * (x0 - x2) + s1_x * (y0 - y2)) / det;
    t = (s2_x * (y0 - y2) - s2_y * (x0 - x2)) / det;

    if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
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

  static boolean lineIntersectsRect(Rectangle r, int x0, int y0, int x1, int y1) {
    boolean intersect;
    if (pointInsideRect(r, x0, y0)) {
      return true;
    }
    if (pointInsideRect(r, x0, y0)) {
      return true;
    }
    if (linesIntersect(x0, y0, x1, y1, r.x, r.y, r.x + r.width, r.y)) {
      return true;
    }
    if (linesIntersect(x0, y0, x1, y1, r.x, r.y, r.x, r.y + r.height)) {
      return true;
    }
    if (linesIntersect(x0, y0, x1, y1, r.x + r.width, r.y, r.x + r.width, r.y + r.height)) {
      return true;
    }
    if (linesIntersect(x0, y0, x1, y1, r.x, r.y + r.height, r.x + r.width, r.y + r.height)) {
      return true;
    }
    return false;
  }
  private final Object sem = new Object();

  public void showEdge(T1 conn) {
    if ((conn.getType() & Edge.REAL) == Edge.REAL) {
      addUpdatedEdge(conn, EdgeState.SHOW, true);
    }
  }

  public void hideEdge(T1 conn) {
    if ((conn.getType() & Edge.REAL) == Edge.REAL) {
      addUpdatedEdge(conn, EdgeState.HIDE, true);
    }
  }

  public void showEdgeSet(String set) {
    Iterator<T> ns = graph.getNodes(set).iterator();
    while (ns.hasNext()) {
      T n = ns.next();
      Iterator<T1> edges = n.getEdges().iterator();
      while (edges.hasNext()) {
        T1 e = edges.next();
        addUpdatedEdge(e, EdgeState.SHOW, false);
      }
    }
    edgeEvent();
  }

  public void hideEdgeSet(String set) {
    Iterator<T> ns = graph.getNodes(set).iterator();
    while (ns.hasNext()) {
      T n = ns.next();
      Iterator<T1> edges = n.getEdges().iterator();
      while (edges.hasNext()) {
        T1 e = edges.next();
        addUpdatedEdge(e, EdgeState.HIDE, false);
      }
    }
    edgeEvent();
  }
  
  public Collection<T1> getEdges(){
    return graph.getEdges();
  }

  public void showNodeSet(String set) {
    Iterator<T> ns = graph.getNodes(set).iterator();
    while (ns.hasNext()) {
      T n = ns.next();
      addUpdatedNode(n, NodeState.SHOW, false);
    }
    notifyObservers(GraphViewerObserver.Action.updatedNodes);
  }

  public void hideNodeSet(String set) {
    Iterator<T> ns = graph.getNodes(set).iterator();
    while (ns.hasNext()) {
      T n = ns.next();
      addUpdatedNode(n, NodeState.HIDE, false);
    }
    notifyObservers(GraphViewerObserver.Action.updatedNodes);
  }

  public void showNode(T n) {
    addUpdatedNode(n, NodeState.SHOW, true);
  }

  public void hideNode(T n) {
    addUpdatedNode(n, NodeState.HIDE, true);
  }

  public abstract T getNodeAtXY(int x, int y);

  public abstract int getX(T node);

  public abstract int getY(T node);

  public abstract Color getFillColor(T node);

  public abstract Color getColor(T node);

  public abstract Color getColor(T1 e);

  public void selectNode(T node) {
    this.selectedNode = node;

    notifyObservers(GraphViewerObserver.Action.selectnode);
  }

  public T getSelectedNode() {
    return this.selectedNode;
  }

  public Edge getEdge(T a, T b) {
    return graph.createEdge(a, b, false);
  }

  public void setScale(double scale) {
    this.scale = scale;
    setUpdatedNodes(this.graph.getNodes());
    setUpdatedEdges(this.graph.getEdges());
    notifyObservers(GraphViewerObserver.Action.setscale);
  }

  public double getScale() {
    return scale;
  }

  public void setUpdatedNodes(Collection<Node> updatedNodes) {
    synchronized (this.updatedNodes) {
      for (Node n : updatedNodes) {
        this.updatedNodes.add(n);
      }
    }
    notifyObservers(GraphViewerObserver.Action.updatedNodes);
  }

  public void addUpdatedNode(Node n, NodeState s, boolean updateCanvas) {
    n.setState(s);

    synchronized (this.updatedNodes) {
      this.updatedNodes.add(n);
    }

    if (updateCanvas) {
    	notifyObservers(GraphViewerObserver.Action.updatedNodes);
    }
  }

  public void setUpdatedEdges(Collection<Edge> updatedEdges) {
    List<Edge> updatedEdgesCopy = new ArrayList<Edge>(updatedEdges);

    for (Edge e : updatedEdgesCopy) {
      if (e != null) {
    	  addUpdatedEdge(e, e.getState(), false);
      }
    }
  }

  public void addUpdatedEdge(Edge e, EdgeState s, boolean updateCanvas) {
    e.setState(s);

    synchronized (this.updatedEdges) {
      Point ps[] = new Point[2];
      ps[0] = new Point(getX((T)e.getStart()), getY((T)e.getStart()));
      ps[1] = new Point(getX((T)e.getEnd()), getY((T)e.getEnd()));
      List<Point[]> psList = new ArrayList<Point[]>();
      psList.add(ps);
      this.updatedEdges.put(e, psList);
    }

    if (updateCanvas) {
      edgeEvent();
    }
  }


  public Rectangle getBounds() {
    if (bounds == null) {
      //ensure we have an ordered list
      initOrderedNodes();
      
      int minX, minY, maxX, maxY;
      synchronized (xOrderedNodes) {
    	  minX = getX((T) xOrderedNodes.get(0)) - 50;
    	  maxX = getX((T) xOrderedNodes.get(xOrderedNodes.size() - 1)) + 50;
	  }
      synchronized (yOrderedNodes) {
    	  minY = getY((T) yOrderedNodes.get(0)) - 50;
    	  maxY = getY((T) yOrderedNodes.get(yOrderedNodes.size() - 1)) + 50;
      }

      bounds = new Rectangle(minX, minY, maxX, maxY);
    }

    return (Rectangle) bounds.clone();
  }

  public Collection<T> getNodes(String group) {
    if (nodeGroups.get(group) == null) {
      TIntObjectHashMap<T> nodes = new TIntObjectHashMap<T>();
      nodeGroups.put(group, nodes);
      Iterator<T> gnodes = graph.getNodes().iterator();
      while (gnodes.hasNext()) {
        T next = gnodes.next();
        if (next.inGroup(group)) {
          nodes.put(next.getId(), next);
        }
      }
    }
    return nodeGroups.get(group).valueCollection();
  }

  private void initOrderedNodes() {
    if (xOrderedNodes.isEmpty()) {
      synchronized (xOrderedNodes) {
    	  xOrderedNodes.clear();
    	  xOrderedNodes.addAll(updatedNodes);
    	  Collections.sort(xOrderedNodes, new NodeXComparator(this));
  	  }
      synchronized (yOrderedNodes) {
    	  yOrderedNodes.clear();
          yOrderedNodes.addAll(updatedNodes);
          Collections.sort(yOrderedNodes, new NodeYComparator(this));
      }
    }
  }

  public void setShowAssignedVars(boolean show) {
    this.showAssignedVars = show;
  }

  public boolean getShowAssignedVars() {
    return this.showAssignedVars;
  }

  public boolean shouldShowEdge(T1 e) {
    if (e.getAssignmentState() == EdgeState.HIDE || e.getState() == EdgeState.HIDE) {
      return false;
    } else if (!getShowAssignedVars() && (e.getStart().isAssigned() || e.getEnd().isAssigned())) {
      return false;
    }

    return true;
  }
  
  public boolean isUpdateRequired() {
	  return this.updatedNodes.size() > 0 || this.updatedEdges.size() > 0;
  }
  
  public void clearUpdatedNodes() {
	  this.updatedNodes.clear();
  }
  
  public void clearUpdatedEdges() {
	  this.updatedEdges.clear();
  }
  
  
  public void setPlacer(Placer placer){
    this.placer = placer;
    init();
  }
  
  public static class NodeXComparator implements Comparator <Node>{
    private GraphViewer graph;
    public NodeXComparator(GraphViewer graph){
      this.graph = graph;
    }
    @Override
    public int compare(Node t, Node t1) {
      return graph.getX(t) - graph.getX(t1);
    }
  }
  
  public static class NodeYComparator implements Comparator <Node>{
    private GraphViewer graph;
    public NodeYComparator(GraphViewer graph){
      this.graph = graph;
    }
    @Override
    public int compare(Node t, Node t1) {
      return graph.getY(t) - graph.getY(t1);
    }
  }
}
