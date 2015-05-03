/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.graph.UI;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.json.simple.JSONObject;
import com.satlib.graph.Node;

/**
 *
 * @author zacknewsham
 */
public class NodeCheckboxPanel extends JPanel{
  int count = 0;

  private final HashMap<Node, JCheckBox> checkBoxes = new HashMap<Node, JCheckBox>();
  private final GraphViewer graph;
  private final JCheckBox hideAll = new JCheckBox("All", true);
  private final String set;
  public NodeCheckboxPanel(GraphViewer graph) {
    this.graph = graph;
    set = "";
  }
  
  public void fromJson(JSONObject json){
    
  }
  public String toJson(){
    StringBuilder json = new StringBuilder();
    
    json.append("{\"hideAll\":").append(hideAll.isSelected()).append(",");
    json.append("\"nodes\":[");
    for(Node node : checkBoxes.keySet()){
      json.append("{\"id\":").append(node.getId()).append(",\"checked\":").append(checkBoxes.get(node).isSelected()).append("},");
    }
    json.setCharAt(json.length() - 1, ']');
    json.append("}");
    return json.toString();
  }
  public void update(){
    Iterator<Node> nodes = checkBoxes.keySet().iterator();
    while(nodes.hasNext()){
      Node next = nodes.next();
      JCheckBox jc = checkBoxes.get(next);
      if(next.isVisible()){
        jc.setSelected(true);
      }
      else{
        jc.setSelected(false);
      }
    }
  }
  
  public NodeCheckboxPanel(GraphViewer graph, String set, Iterator<Node> nodes){
    this.graph = graph;
    this.add(hideAll);
    this.set = set;
    hideAll.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        
        if(!hideAll.isSelected()){
          NodeCheckboxPanel.this.graph.hideNodeSet(NodeCheckboxPanel.this.set);
        }
        else{
          NodeCheckboxPanel.this.graph.showNodeSet(NodeCheckboxPanel.this.set);
        }
      }
    });
    count++;
    addAll(nodes);
    this.setLayout(new GridLayout(count, 1));
  }
  final void addAll(Iterator<Node> nodes){
    while(nodes.hasNext()){
      Node node = nodes.next();
      add(node);
    }
    List nodes1 = new ArrayList<Node>();
    nodes1.addAll(checkBoxes.keySet());
    Collections.sort(nodes1, Node.NAME_COMPARATOR);
    Iterator<Node> nodes2 = nodes1.iterator();
    while(nodes2.hasNext()){
      this.add(checkBoxes.get(nodes2.next()));
    }
  }
  
  void add(Node node){
    NodeCheckBox jc = new NodeCheckBox(node);
    jc.setSelected(true);
    jc.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent ie) {
        NodeCheckBox box = (NodeCheckBox)ie.getItem();
        if(box.isSelected()){
          graph.showNode(box.getNode());
        }
        else{
          graph.hideNode(box.getNode());
        }
      }
    });
    checkBoxes.put(node, jc);
    count ++;
  }
}
