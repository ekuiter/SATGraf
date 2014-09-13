/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visual.graph;

import gnu.trove.map.hash.TIntObjectHashMap;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.Timer;
import org.json.simple.JSONObject;
import visual.UI.GraphCanvas;
import visual.UI.GraphOptionsPanel;

/**
 *
 * @author zacknewsham
 * @param <T>
 * @param <T1>
 */
public abstract class GraphViewer<T extends Node, T1 extends Edge> implements ActionListener{
  protected Graph graph;
  private T selectedNode;
  protected final HashSet<T> invisibleNodes = new HashSet<T>();
  protected HashSet<T1> visibleConnections;//new Edge.XComparator(this));
  protected HashMap<String, TIntObjectHashMap<T>> visibleNodes = new HashMap<>();
  protected GraphCanvas canvas;
  protected GraphOptionsPanel panel;
  protected final HashMap<String, TIntObjectHashMap<String>> node_lists;
  protected final ArrayList xOrderedNodes = new ArrayList<T>();
  public final int OVERLAP = 50;
  private double scale = 1.0;
  //only required to quickly get bounds
  private boolean simpleConnections = false;
  protected final ArrayList yOrderedNodes = new ArrayList<T>();
  
  private final Timer actionTimer = new Timer(1000, this);
  
  public GraphViewer(Graph graph, HashMap<String, TIntObjectHashMap<String>> node_lists){
    this.node_lists = node_lists;
    this.graph = graph;
  }
  public abstract String toJson();
  
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

  @Override
  public void actionPerformed(ActionEvent e) {
    canvas.reset();
    panel.update();
    canvas.repaint();
    actionTimer.stop();
  }
  public void updateObservers(){
    actionTimer.restart();
  }
  public ArrayList<Node> getNodes(String set, Rectangle bounds){
    synchronized(visibleNodes){
      if(visibleNodes.get(set) == null){
        boolean all = set.equals("All");
        TIntObjectHashMap<T> nodes = new TIntObjectHashMap<T>();
        visibleNodes.put(set, nodes);
        Iterator<T> gnodes = graph.getNodeIterator();
        while(gnodes.hasNext()){
          T next = gnodes.next();
          if(next.inGroup(set) || all){
            nodes.put(next.getId(), next);
          }
        }
      }
      if(xOrderedNodes.isEmpty()){
        xOrderedNodes.clear();
        yOrderedNodes.clear();
        xOrderedNodes.addAll(visibleNodes.get(set).valueCollection());
        Collections.sort(xOrderedNodes, new Node.XComparator(this));
        yOrderedNodes.addAll(visibleNodes.get(set).valueCollection());
        Collections.sort(yOrderedNodes, new Node.YComparator(this));
      }
    }
    ArrayList y = new ArrayList<Node>();
    
    ArrayList<Node> xy = new ArrayList<Node>();
    int firstX = findFirstXIndex(xOrderedNodes, bounds.x - OVERLAP);
    for(int i = firstX; i < xOrderedNodes.size(); i++){
      T xn = (T)xOrderedNodes.get(i);
      if(getX(xn) >= bounds.x && xn.getY(this) >= bounds.y - OVERLAP && xn.getY(this) <= bounds.y + bounds.height + OVERLAP){
        if(getX(xn) <= bounds.x + bounds.width + OVERLAP){
          if(isVisible(xn)){
            xy.add(xn);
          }
        }
        else{
          break;
        }
      }
    }
    return xy;
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
  public Iterator<T> getNodes(String set){
    if(visibleNodes.get(set) == null){
      TIntObjectHashMap<T> nodes = new TIntObjectHashMap<T>();
      visibleNodes.put(set, nodes);
      Iterator<T> gnodes = graph.getNodeIterator();
      while(gnodes.hasNext()){
        T next = gnodes.next();
        if(next.inGroup(set)){
          nodes.put(next.getId(), next);
        }
      }
    }
    return visibleNodes.get(set).valueCollection().iterator();
  }
  public Iterator<Edge> getConnections(){
    return getConnections(new Rectangle());
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
  public Iterator<Edge> getConnections(Rectangle bounds){
    synchronized(sem){
      if(visibleConnections == null){
        visibleConnections = new HashSet<>();
        Iterator<T1> conns = graph.getEdges();
        Edge dummy = new Edge();
        visibleConnections.add((T1)dummy);
        while(conns.hasNext()){
          T1 next = conns.next();
          visibleConnections.add(next);
        }
      }
    }
    ArrayList<Edge> edges = new ArrayList<>();
    if(simpleConnections){
      Iterator<Node> nodes = getNodes("All", bounds).iterator();
      while(nodes.hasNext()){
        Node n = nodes.next();
        Iterator<T1> ei = n.getEdges();
        while(ei.hasNext()){
          T1 e = ei.next();
          if(visibleConnections.contains(e)){
            edges.add(e);
          }
        }
      }
    }
    else{
      Iterator<T1> conns = visibleConnections.iterator();
      while(conns.hasNext()){
        Edge con = conns.next();

        //deal with dummy
        if(con.getStart() == null){
          continue;
        }
        if(simpleConnections == true){
          if(bounds.contains(con.getStart().getX(this), con.getStart().getY(this)) || bounds.contains(con.getEnd().getX(this), con.getEnd().getY(this))){
            edges.add(con);
          }
        }
        else if(lineIntersectsRect(bounds, con.getStart().getX(this), con.getStart().getY(this), con.getEnd().getX(this), con.getEnd().getY(this))){
          edges.add(con);
        }
      }
    }
    /*Iterator<Node> nodes = getNodes("All", bounds).iterator();
    while(nodes.hasNext()){
      Node node = nodes.next();
      Iterator<Edge> cons = node.getConnections();
      while(cons.hasNext()){
        edges.add(cons.next());
      }
    }*/
    
    //required for synhronization
    return edges.iterator();
  }
  public void showConnection(T1 conn){
    if((conn.getType() & Edge.REAL) == Edge.REAL){
      if(!visibleConnections.contains(conn)){
        visibleConnections.add(conn);
        updateObservers();
      }
    }
    updateObservers();
  }
  public void hideEdge(T1 conn){
    if((conn.getType() & Edge.REAL) == Edge.REAL){
      if(visibleConnections.contains(conn)){
        visibleConnections.remove(conn);
        updateObservers();
      }
    }
    updateObservers();
  }
  public void showEdgeSet(String set){
    synchronized(visibleConnections){
      Iterator<T> ns = graph.getNodes(set);
      while(ns.hasNext()){
        T n = ns.next();
        Iterator<T1> edges = n.getEdges();
        while(edges.hasNext()){
          T1 e = edges.next();
          visibleConnections.add(e);
        }
      }
    }
    updateObservers();
  }
  public void hideEdgeSet(String set){
    synchronized(visibleConnections){
      Iterator<T> ns = graph.getNodes(set);
      while(ns.hasNext()){
        T n = ns.next();
        Iterator<T1> edges = n.getEdges();
        while(edges.hasNext()){
          T1 e = edges.next();
          visibleConnections.remove(e);
        }
      }
    }
    updateObservers();
  }
  public void showNodeSet(String set){
    synchronized(visibleNodes){
      Iterator<T> ns = graph.getNodes(set);
      while(ns.hasNext()){
        T n = ns.next();
        if(!visibleNodes.get(set).containsKey(n.getId())){
          visibleNodes.get(set).put(n.getId(), n);
          invisibleNodes.remove(n);
        }
      }
    }
    updateObservers();
  }
  public void hideNodeSet(String set){
    synchronized(visibleNodes){
      Iterator<T> ns = graph.getNodes(set);
      while(ns.hasNext()){
        T n = ns.next();
        if(visibleNodes.get(set).containsKey(n.getId())){
          visibleNodes.get(set).remove(n.getId());
          invisibleNodes.add(n);
        }
      }
    }
    updateObservers();
  }
  public void showNode(T node){
    Iterator<String> groups = node.getGroups();
    while(groups.hasNext()){
      String group = groups.next();
      TIntObjectHashMap<T> nodes = visibleNodes.get(group);
      //if(!nodes.containsValue(node)){
        nodes.put(node.getId(), node);
      //}
    }
    invisibleNodes.remove(node);
    updateObservers();
  }
  public void hideNode(T node){
    Iterator<String> groups = node.getGroups();
    while(groups.hasNext()){
      String group = groups.next();
      TIntObjectHashMap<T> nodes = visibleNodes.get(group);
      //if(nodes.containsValue(node)){
        nodes.remove(node.getId());
      //}
    }
    invisibleNodes.add(node);
    updateObservers();
  }
  public boolean isVisible(T node){
    return !invisibleNodes.contains(node);
  }
  public abstract T getNodeAtXY(int x, int y);
  protected abstract int getX(T node);
  protected abstract int getY(T node);
  protected abstract Color getFillColor(T node);
  protected abstract Color getColor(T node);
  protected abstract Color getColor(T1 e);
  public Rectangle getBounds(){
    //ensure we have an ordered list
    getNodes("All", new Rectangle(0, 0, 1000, 1000));
    return new Rectangle(
            ((T)xOrderedNodes.get(0)).getX(this) - 50, ((T)yOrderedNodes.get(0)).getY(this) - 50,
            ((T)xOrderedNodes.get(xOrderedNodes.size() - 1)).getX(this) + 50,((T)yOrderedNodes.get(yOrderedNodes.size() - 1)).getY(this) + 50);
  }
  
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
    this.updateObservers();
  }
  public double getScale(){
    return scale;
  }
  
}
