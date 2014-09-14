/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.graph;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.json.simple.JSONObject;

/**
 *
 * @author zacknewsham
 */
public class SimpleGraphViewer extends GraphViewer<Node, Edge>{
  protected HashMap<Node, Point> nodePositions = new HashMap<Node, Point>();
  private ArrayList<Node> temp_nodes;
  public SimpleGraphViewer(Graph graph, HashMap node_lists) {
    super(graph, node_lists);
  }

  @Override
  public void init() {
    
  }
  
  public void fromJson(JSONObject json){
    
  }
  public String toJson(){
    return "";
  }
  protected Graph getGraph(){
    return graph;
  }

  @Override
  public Node getNodeAtXY(int x, int y) {
    x /= getScale();
    y /= getScale();
    Iterator<Node> nodes = graph.getNodeIterator();
    Rectangle r = new Rectangle(0, 0, DrawableNode.NODE_DIAMETER, DrawableNode.NODE_DIAMETER);
    while(nodes.hasNext()){
      Node node = nodes.next();
      r.x = getX(node);
      r.y = getY(node);
      if(r.contains(x, y)){
        return node;
      }
    }
    return null;
  }

  
  protected int getY(Node node){
    return getY(node, false);
  }
  private int getY(Node node, boolean fromX) {
    if(sqrt_size == 0){
      sqrt_size = (int)Math.round(Math.sqrt(getGraph().getNodesList().size()));
    }
    if(nodePositions.get(node) == null || fromX){
      int index = node.getId();
      int offset =  50 + (int)(index / sqrt_size) * (DrawableNode.NODE_DIAMETER + DrawableNode.NODE_X_SPACING);
      
      Point p;
      
      if(!fromX){
        p = new Point();
        nodePositions.put(node, p);
        p.x = getX(node, true);
      }
      else{
        p = nodePositions.get(node);
      }
      p.y = offset;
      return p.y;
    }
    return nodePositions.get(node).y;
  }
  @Override
  protected int getX(Node node){
    return getX(node, false);
  }
  private int sqrt_size = 0;
  protected int getX(Node node, boolean fromY) {
    if(sqrt_size == 0){
      sqrt_size = (int)Math.round(Math.sqrt(getGraph().getNodesList().size()));
    }
    if(nodePositions.get(node) == null || fromY){
      int index = node.getId();
      int offset = 50 + (int)(index % sqrt_size) * (DrawableNode.NODE_DIAMETER + DrawableNode.NODE_X_SPACING);
      
      Point p;
      if(!fromY){
        p = new Point();
        nodePositions.put(node, p);
        p.y = getY(node, true);
      }
      else{
        p = nodePositions.get(node);
      }
      p.x = offset;
      return p.x;
    }
    return nodePositions.get(node).x;
  }
  
  @Override
  protected Color getFillColor(Node node){
    return getColor(node);
  }
  @Override
  protected Color getColor(Node node) {
    return Color.RED;
  }

  @Override
  protected Color getColor(Edge e) {
    return Color.BLACK;
  }
  
}
