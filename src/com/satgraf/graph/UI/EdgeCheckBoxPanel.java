/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.graph.UI;

import com.satlib.graph.Edge;
import com.satlib.graph.Node;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JPanel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author zacknewsham
 */
public class EdgeCheckBoxPanel<T extends Node, T1 extends Edge> extends JPanel{
  private final GraphViewer graph;
  private int count = 2;
  private HashMap<Integer, EdgeCheckBox> checkBoxes = new HashMap<>();
  public EdgeCheckBoxPanel(GraphViewer graph, Node n, Iterator<T> nodes){
    this.graph = graph;
    count = 0;
    while(nodes.hasNext()){
      T next = nodes.next();
      Edge c = graph.getEdge(n, next);
      add(c);
      count++;
    }
    this.setLayout(new GridLayout(count, 1));
  }
  
  public void fromJson(JSONObject json){
    for(Object o : (JSONArray)json.get("boxes")){
      int id = ((Long)((JSONObject)o).get("id")).intValue();
      boolean selected = (Boolean)((JSONObject)o).get("checked");
      if(checkBoxes.get(id).isSelected() != selected){
        checkBoxes.get(id).setSelected(selected);
      }
    }
  }
  
  public String toJson(){
    StringBuilder json = new StringBuilder();
    
    json.append("{\"boxes\":[");
    for(Integer com : checkBoxes.keySet()){
      json.append("{\"id\":").append(com).append(",\"checked\":").append(checkBoxes.get(com).isSelected()).append("},");
    }
    json.setCharAt(json.length() - 1, ']');
    json.append("}");
    return json.toString();
  }
  
  public EdgeCheckBoxPanel(GraphViewer graph, HashSet<T1> connections) {
    this.graph = graph;
    
    Iterator<T1> conns = connections.iterator();
    while(conns.hasNext()){
      add(conns.next());
    }
    this.setLayout(new GridLayout(count, 1));
  }
  public EdgeCheckBoxPanel() {
    this.graph = null;
  }
  public EdgeCheckBoxPanel(GraphViewer graph) {
    this.graph = graph;
    addAll(graph.getEdges());
    this.setLayout(new GridLayout(count, 1));
  }
  final void add(Edge conn){
    EdgeCheckBox jc = new EdgeCheckBox(conn);
    checkBoxes.put(conn.getId(), jc);
      jc.setSelected(conn.getState() == Edge.EdgeState.SHOW);
      jc.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent ie) {
          final EdgeCheckBox box = (EdgeCheckBox)ie.getItem();
          if(box.isSelected()){
            Thread t = new Thread(){
              public void run(){
                graph.showEdge(box.getConnection());
              }
            };
            t.start();
          }
          else{
            Thread t = new Thread(){
              public void run(){
                graph.hideEdge(box.getConnection());
              }
            };
            t.start();
          }
        }
      });
      this.add(jc);
      count ++;
  }
  final void addAll(Collection<Edge> conns){
    for(Edge conn : conns){
      add(conn);
    }
  }
}
