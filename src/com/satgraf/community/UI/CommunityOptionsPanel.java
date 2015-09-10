/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.UI;



import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JScrollPane;

import org.json.simple.JSONObject;

import com.satgraf.graph.UI.EdgeCheckBoxPanel;
import com.satgraf.graph.UI.GraphFrame;
import com.satgraf.graph.UI.GraphOptionsPanel;
import com.satgraf.graph.UI.NodePanel;
import com.satgraf.graph.UI.OptionsPanel;
import com.satlib.community.CommunityEdge;
import com.satlib.graph.Edge;

/**
 *
 * @author zacknewsham
 */
public class CommunityOptionsPanel extends GraphOptionsPanel{

  private CommunityCheckBoxPanel communityPanel;
  private JScrollPane communityScroll;
  private EdgeCheckBoxPanel interConnectionPanel;
  private JScrollPane interConncetionScroll;
  
  private EdgeCheckBoxPanel intraConnectionPanel;
  private JScrollPane intraConncetionScroll;
  
  
  protected CommunityOptionsPanel(GraphFrame frame, CommunityGraphViewer graph, Collection<String> groups, boolean callSet) {
    super(graph, groups, false);
    NodePanel nodePanel = new CommunityNodePanel(graph);
    optionsPanel = new OptionsPanel(frame,graph,nodePanel);
    if(callSet){
      setGraph(graph, true);
    }
    this.setTopComponent(optionsPanel);
  }
  
  public CommunityOptionsPanel(GraphFrame frame, CommunityGraphViewer graph, Collection<String> groups) {
    this(frame, graph, groups, true);
  }
  
  @Override
  public void update(){
    super.update();
    optionsPanel.update();
  }
  
  public void initFromJson(JSONObject json){
    super.fromJson((JSONObject)json.get(JsonName()));
    //communityPanel.fromJson((JSONObject)json.get("communities"));
    //interConnectionPanel.fromJson((JSONObject)json.get("interEdges"));
    //intraConnectionPanel.fromJson((JSONObject)json.get("intraEdges"));
  }
  public String toJson(){
    StringBuilder json = new StringBuilder(super.toJson());
    
    json.setCharAt(json.length() - 1, ',');
    json.append("\"communities\":").append(communityPanel.toJson()).append(",");
    json.append("\"interEdges\":").append(interConnectionPanel.toJson()).append(",");
    json.append("\"intraEdges\":").append(intraConnectionPanel.toJson()).append("}");
    
    return json.toString();
  }
  
  protected void setGraph(CommunityGraphViewer graph, boolean clearPanel){
    if(infoPanel == null){
      infoPanel = new CommunityGraphInfoPanel(graph);
    }
    super.setGraph(graph, clearPanel);
    synchronized(checkBoxPanel){
      communityPanel = new CommunityCheckBoxPanel(graph);
      communityScroll = new JScrollPane(communityPanel);
      checkBoxPanel.addBar("Communities", communityScroll);
      
      HashSet<CommunityEdge> interConnections = new HashSet<>();
      HashSet<CommunityEdge> intraConnections = new HashSet<>();
      
      /*Iterator<CommunityEdge> eit = graph.getEdges().iterator();
      while (eit.hasNext()) {
    	  CommunityEdge e = (CommunityEdge) eit.next();
    	  
    	  if (e.isInterCommunityEdge()) {
    		  interConnections.add(e);
    	  } else {
    		  intraConnections.add(e);
    	  }
      }*/
      
      interConnectionPanel = new EdgeCheckBoxPanel(graph, interConnections);
      interConncetionScroll = new JScrollPane(interConnectionPanel);
      checkBoxPanel.addBar("Inter Connections", interConncetionScroll);

      intraConnectionPanel = new EdgeCheckBoxPanel(graph, intraConnections);
      intraConncetionScroll = new JScrollPane(intraConnectionPanel);
      checkBoxPanel.addBar("Intra Connections", intraConncetionScroll);
    }
  }
}
