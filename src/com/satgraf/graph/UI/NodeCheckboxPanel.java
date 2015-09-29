/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.graph.UI;

import com.satgraf.UI.SortCheckboxesDropDown;
import com.satgraf.UI.SortableCheckboxes;
import com.satlib.graph.Node;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.json.simple.JSONObject;

/**
 *
 * @author zacknewsham
 */
public class NodeCheckboxPanel extends JPanel implements SortableCheckboxes<NodeCheckBox>{
  int count = 0;

  private final HashMap<Node, NodeCheckBox> checkBoxes = new HashMap<>();
  private final GraphViewer graph;
  private final JCheckBox hideAll = new JCheckBox("All", true);
  private final String set;
  private final SortCheckboxesDropDown dd;
  private final Map<String, Comparator> comparators = new HashMap<>();
  public NodeCheckboxPanel(GraphViewer graph) {
    this.graph = graph;
    set = "";
    comparators.put("+ID", new NumericComparatorInc());
    comparators.put("-ID", new NumericComparatorDec());
    comparators.put("+Name", new NameComparatorInc());
    comparators.put("-Name", new NameComparatorDec());
    comparators.put("+Degrees", new DegreeComparatorInc());
    comparators.put("-Degrees", new DegreeComparatorDec());
    dd = new SortCheckboxesDropDown(this, comparators);
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
  boolean updating = false;
  public void update(){
    if(!updating){
      updating = true;
      Iterator<Node> nodes = checkBoxes.keySet().iterator();
      while(nodes.hasNext()){
        Node next = nodes.next();
        JCheckBox jc = checkBoxes.get(next);
        if(next.isVisible() && jc.isSelected() == false){
          jc.setSelected(true);
        }
        else if(!next.isVisible() && jc.isSelected() == true){
          jc.setSelected(false);
        }
      }
      updating = false;
    }
  }
  
  public NodeCheckboxPanel(GraphViewer graph, String set, Collection<Node> nodes){
    this.graph = graph;
    comparators.put("+ID", new NumericComparatorInc());
    comparators.put("-ID", new NumericComparatorDec());
    comparators.put("+Name", new NameComparatorInc());
    comparators.put("-Name", new NameComparatorDec());
    comparators.put("+Degrees", new DegreeComparatorInc());
    comparators.put("-Degrees", new DegreeComparatorDec());
    dd = new SortCheckboxesDropDown(this, comparators);
    this.add(dd);
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
    this.setLayout(new GridLayout(count+1, 1));
    sort(new NumericComparatorInc());
  }
  
  final void addAll(Collection<Node> nodes){
    for(Node node : nodes){
      add(node);
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

  @Override
  public void sort(Comparator<NodeCheckBox> comp) {
    List<NodeCheckBox> sorted = new ArrayList<>();
    sorted.addAll(checkBoxes.values());
    Collections.sort(sorted, comp);
    this.removeAll();
    this.add(dd);
    this.add(hideAll);
    for(NodeCheckBox box : sorted){
      this.add(box);
    }
  }

  private static class NumericComparatorInc implements Comparator<NodeCheckBox> {
    @Override
    public int compare(NodeCheckBox t, NodeCheckBox t1) {
      return t.getNode().getId() - t1.getNode().getId();
    }
  }

  private static class NumericComparatorDec implements Comparator<NodeCheckBox> {
    @Override
    public int compare(NodeCheckBox t, NodeCheckBox t1) {
      return t1.getNode().getId() - t.getNode().getId();
    }
  }

  private static class NameComparatorInc implements Comparator<NodeCheckBox> {
    @Override
    public int compare(NodeCheckBox t, NodeCheckBox t1) {
      return t.getNode().getName().compareTo(t1.getNode().getName());
    }
  }

  private static class NameComparatorDec implements Comparator<NodeCheckBox> {
    @Override
    public int compare(NodeCheckBox t, NodeCheckBox t1) {
      return t1.getNode().getName().compareTo(t.getNode().getName());
    }
  }

  private static class DegreeComparatorInc implements Comparator<NodeCheckBox> {
    @Override
    public int compare(NodeCheckBox t, NodeCheckBox t1) {
      return t.getNode().getEdges().size()- t1.getNode().getEdges().size();
    }
  }

  private static class DegreeComparatorDec implements Comparator<NodeCheckBox> {
    @Override
    public int compare(NodeCheckBox t, NodeCheckBox t1) {
      return t1.getNode().getEdges().size()- t.getNode().getEdges().size();
    }
  }
}
