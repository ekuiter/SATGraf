/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.UI;

import com.satgraf.UI.SortCheckboxesDropDown;
import com.satgraf.UI.SortableCheckboxes;
import com.satlib.community.Community;
import com.satlib.community.CommunityNode;
import com.satlib.graph.Node;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author zacknewsham
 */
class CommunityCheckBoxPanel extends JPanel implements SortableCheckboxes<CommunityCheckBox>{
  int count = 0;
  private HashMap<Integer, CommunityCheckBox> checkBoxes = new HashMap<>();
  private List<CommunityCheckBox> ordered = new ArrayList<>();
  private final CommunityGraphViewer graph;
  private final SortCheckboxesDropDown dd;
  private final Map<String, Comparator> comparators = new HashMap<>();
  public CommunityCheckBoxPanel(CommunityGraphViewer graph) {
    this.graph = graph;
    comparators.put("+ID", new NumericComparatorInc());
    comparators.put("-ID", new NumericComparatorDec());
    comparators.put("+Nodes", new NodeComparatorInc());
    comparators.put("-Nodes", new NodeComparatorDec());
    comparators.put("+Inter", new InterComparatorInc());
    comparators.put("-Inter", new InterComparatorDec());
    comparators.put("+Intra", new IntraComparatorInc());
    comparators.put("-Intra", new IntraComparatorDec());
    dd = new SortCheckboxesDropDown(this, comparators);
    init();
    this.setLayout(new GridLayout(count+1, 1));
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
  private void init(){
    int community = 0;
    Collection<CommunityNode> communities = graph.getCommunityNodes(community);
    while(communities != null){
      int intercomm = graph.getInterCommunityConnections(community).size();
      int intracomm = graph.getIntraCommunityConnections(community).size();
      CommunityCheckBox jc = new CommunityCheckBox(community, communities.size(), intercomm, intracomm);
      checkBoxes.put(community, jc);
      jc.setSelected(true);
      jc.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent ie) {
          CommunityCheckBox box = (CommunityCheckBox)ie.getItem();
          if(box.isSelected()){
            graph.showCommunity(box.getCommunity());
          }
          else{
            graph.hideCommunity(box.getCommunity());
          }
        }
      });
      community++;
      communities = graph.getCommunityNodes(community);
      ordered.add(jc);
      sort(new NumericComparatorInc());
      count ++;
    }
  }

  @Override
  public void sort(Comparator<CommunityCheckBox> comp) {
    Collections.sort(ordered, comp);
    this.removeAll();
    this.add(dd);
    for(CommunityCheckBox box: ordered){
      this.add(box);
    }
  }
  
  private static class NumericComparatorInc implements Comparator<CommunityCheckBox>{
    @Override
    public int compare(CommunityCheckBox t, CommunityCheckBox t1) {
      return t.getCommunity() - t1.getCommunity();
    }
  }
  
  private static class NumericComparatorDec implements Comparator<CommunityCheckBox>{
    @Override
    public int compare(CommunityCheckBox t, CommunityCheckBox t1) {
      return t1.getCommunity() - t.getCommunity();
    }
  }
  
  private static class NodeComparatorInc implements Comparator<CommunityCheckBox>{
    @Override
    public int compare(CommunityCheckBox t, CommunityCheckBox t1) {
      return t.getCommunitySize()- t1.getCommunitySize();
    }
  }
  
  private static class NodeComparatorDec implements Comparator<CommunityCheckBox>{
    @Override
    public int compare(CommunityCheckBox t, CommunityCheckBox t1) {
      return t.getCommunitySize()- t1.getCommunitySize();
    }
  }
  
  private static class InterComparatorInc implements Comparator<CommunityCheckBox>{
    @Override
    public int compare(CommunityCheckBox t, CommunityCheckBox t1) {
      return t.getInterConnections()- t1.getInterConnections();
    }
  }
  
  private static class InterComparatorDec implements Comparator<CommunityCheckBox>{
    @Override
    public int compare(CommunityCheckBox t, CommunityCheckBox t1) {
      return t.getInterConnections()- t1.getInterConnections();
    }
  }
  
  private static class IntraComparatorInc implements Comparator<CommunityCheckBox>{
    @Override
    public int compare(CommunityCheckBox t, CommunityCheckBox t1) {
      return t.getIntraConnections()- t1.getIntraConnections();
    }
  }
  
  private static class IntraComparatorDec implements Comparator<CommunityCheckBox>{
    @Override
    public int compare(CommunityCheckBox t, CommunityCheckBox t1) {
      return t.getIntraConnections()- t1.getIntraConnections();
    }
  }
}
