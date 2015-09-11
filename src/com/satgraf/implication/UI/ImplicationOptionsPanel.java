/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.implication.UI;

import com.satgraf.graph.UI.GraphViewerObserver;
import com.satgraf.graph.UI.GraphViewer;
import java.util.Collection;
import org.json.simple.JSONObject;
import com.satgraf.graph.UI.GraphFrame;
import com.satgraf.graph.UI.GraphInfoPanel;
import com.satgraf.graph.UI.GraphOptionsPanel;
import com.satgraf.graph.UI.NodePanel;
import com.satgraf.graph.UI.OptionsPanel;
import com.satlib.graph.Edge;
import com.satlib.graph.Graph;
import com.satlib.graph.Node;
import com.satlib.implication.ImplicationGraph;
import com.satlib.implication.ImplicationNode;

/**
 *
 * @author zacknewsham
 */
public class ImplicationOptionsPanel extends GraphOptionsPanel<ImplicationNode,Edge, ImplicationGraph, ImplicationGraphViewer>{
  public ImplicationOptionsPanel(GraphFrame frame, ImplicationGraphViewer graph, Collection<String> groups) {
    super(graph, groups);
    NodePanel nodePanel = new ImplicationNodePanel(graph);
    optionsPanel = new OptionsPanel(frame, graph,nodePanel);
    infoPanel = new ImplicationGraphInfoPanel(graph);
    this.setTopComponent(optionsPanel);
    setGraph(graph);
  }
  
  
  @Override
  public void update(){
    super.update();
    optionsPanel.update();
  }
  
  protected void setGraph(GraphViewer graph){
    super.setGraph(graph, true);
  }

  @Override
  public void notify(GraphViewer graph, GraphViewerObserver.Action action) {
    super.update();
    optionsPanel.update();
  }

  @Override
  public void initFromJson(JSONObject json) {
    //nothing to do 
  }

  @Override
  public String toJson() {
    return "";
  }
}
