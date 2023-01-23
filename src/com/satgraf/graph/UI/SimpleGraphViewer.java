/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.graph.UI;

import com.satgraf.graph.UI.GraphViewer;
import com.satgraf.graph.placer.Placer;
import com.satlib.graph.DrawableNode;
import com.satlib.graph.Edge;
import com.satlib.graph.Graph;
import com.satlib.graph.Node;
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
  private ArrayList<Node> temp_nodes;
  public SimpleGraphViewer(Graph graph, HashMap node_lists, Placer placer) {
    super(graph, node_lists, placer);
  }

  @Override
  public void init() {
    setUpdatedNodes(graph.getNodes());
    placer.init();
  }
  
  public void fromJson(JSONObject json){
    
  }
  public String toJson(){
    return "";
  }
  public Graph getGraph(){
    return graph;
  }

  @Override
  public Node getNodeAtXY(int x, int y) {
    return placer.getNodeAtXY(x, y, getScale());
  }

  
  @Override
  public int getY(Node node){
    return placer.getY(node);
  }
  
  @Override
  public int getX(Node node){
    return placer.getX(node);
  }
    
  @Override
  public Color getFillColor(Node node){
    return getColor(node);
  }
  
  @Override
  public Color getColor(Node node) {
    return Color.RED;
  }

  @Override
  public Color getColor(Edge e) {
    return Color.BLACK;
  }
  
}
