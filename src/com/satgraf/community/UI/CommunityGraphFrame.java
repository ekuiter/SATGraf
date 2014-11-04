/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.UI;

import com.satlib.community.CommunityGraphViewer;
import com.satlib.community.JSONCommunityGraph;
import java.util.HashMap;
import java.util.regex.Pattern;
import org.json.simple.JSONObject;
import com.satgraf.graph.UI.GraphCanvasPanel;
import com.satgraf.graph.UI.GraphFrame;
import com.satgraf.evolution.UI.EvolutionGraphFrame;

/**
 *
 * @author zacknewsham
 */
public class CommunityGraphFrame extends GraphFrame{

  protected HashMap<String, Pattern> patterns;
  public CommunityGraphFrame(CommunityGraphViewer graphViewer, HashMap<String, Pattern> patterns) {
    super(graphViewer);
    this.patterns = patterns;
  }
  public CommunityGraphViewer getGraphViewer(){
    return (CommunityGraphViewer)graphViewer;
  }
  
  public void setPatterns(HashMap<String, Pattern> patterns){
    this.patterns = patterns;
  }
  
  public void show() {
    if(graphViewer != null && panel == null){
      canvasPanel = new GraphCanvasPanel(new CommunityCanvas(getGraphViewer()));

      panel = new CommunityOptionsPanel(this, getGraphViewer(), patterns.keySet());
    }
    super.show();
  }
  
  public static void main(String args[]){
    CommunityGraphFrame frame = new CommunityGraphFrame(null, null);
    
    frame.show();
  }
  public void fromJson(JSONObject json){
    if(!(this instanceof EvolutionGraphFrame)){
      JSONCommunityGraph graph = new JSONCommunityGraph((JSONObject)json.get("graphViewer"));
      graph.init();
      this.graphViewer = new CommunityGraphViewer(graph, graph.getNodeLists(), graph);
      this.patterns = new HashMap<>();
      init();
      show();
      this.graphViewer.fromJson((JSONObject)json.get("graphViewer"));
    }
    super.fromJson(json);
  }
  public String toJson(){
    StringBuilder json = new StringBuilder(super.toJson());
    
    return json.toString();
  }
  
  @Override
  public com.satgraf.actions.OpenAction getOpenAction(){
    return new OpenAction(this);
  }  
  
  @Override
  public com.satgraf.actions.ExportAction getExportAction(){
    return new ExportAction(this);
  }  
}
