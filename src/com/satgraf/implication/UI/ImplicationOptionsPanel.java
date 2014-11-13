/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.implication.UI;

import com.satlib.graph.GraphObserver;
import com.satlib.graph.GraphViewer;
import java.util.Collection;
import org.json.simple.JSONObject;
import com.satgraf.graph.UI.GraphFrame;
import com.satgraf.graph.UI.GraphOptionsPanel;
import com.satgraf.graph.UI.NodePanel;
import com.satgraf.graph.UI.OptionsPanel;

/**
 *
 * @author zacknewsham
 */
public class ImplicationOptionsPanel extends GraphOptionsPanel{
  public ImplicationOptionsPanel(GraphFrame frame, GraphViewer graph, Collection<String> groups) {
    super(graph, groups);
    NodePanel nodePanel = new ImplicationNodePanel(graph);
    optionsPanel = new OptionsPanel(frame, graph,nodePanel);
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
  public void notify(GraphViewer graph, GraphObserver.Action action) {
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
