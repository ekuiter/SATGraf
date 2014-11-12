/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.UI;

import com.satgraf.community.placer.FruchPlacer;
import com.satgraf.community.placer.GridKKPlacer;
import com.satgraf.community.placer.GridPlacer;
import com.satgraf.evolution.UI.EvolutionGraphFrame;
import com.satgraf.graph.UI.GraphCanvasPanel;
import com.satgraf.graph.UI.GraphFrame;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityGraphFactory;
import com.satlib.community.CommunityGraphFactoryFactory;
import com.satlib.community.CommunityGraphViewer;
import com.satlib.community.CommunityMetric;
import com.satlib.community.JSONCommunityGraph;
import com.satlib.community.placer.CommunityPlacer;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;
import org.json.simple.JSONObject;

/**
 *
 * @author zacknewsham
 */
public class CommunityGraphFrame extends GraphFrame{

  protected HashMap<String, Pattern> patterns;
  private CommunityMetric metric;
  public CommunityGraphFrame(CommunityGraphViewer graphViewer, HashMap<String, Pattern> patterns, CommunityMetric metric) {
    super(graphViewer);
    this.patterns = patterns;
    this.metric = metric;
    
  }
  public CommunityGraphViewer getGraphViewer(){
    return (CommunityGraphViewer)graphViewer;
  }
  
  public void setPatterns(HashMap<String, Pattern> patterns){
    this.patterns = patterns;
  }
  
  
  public void show() {
    if(graphViewer != null && graphViewer.graph != null && panel == null){
      canvasPanel = new GraphCanvasPanel(new CommunityCanvas(getGraphViewer()));

      panel = new CommunityOptionsPanel(this, getGraphViewer(), patterns.keySet());
    }
    super.show();
  }
  
  public void init(){
    super.init();
    metric.getCommunities((CommunityGraph)graphViewer.getGraph());
  }
  public static CommunityPlacer getPlacer(String placerName, CommunityGraph graph){
    
    if(placerName.equals("kk")){
      return new GridKKPlacer(graph);
    }
    else if(placerName.equals("grid")){
      return new GridPlacer(graph);
    }
    else if(placerName.equals("f")){
      return new FruchPlacer(graph);
    }
    return null;
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
  
  public static void main(String args[]) throws IOException{
    if(args.length < 3){
      args = new String[]{
        //"formula/satcomp/dimacs/toybox.dimacs",
        "formula/satcomp/dimacs/aes_16_10_keyfind_3.cnf",
        //"/media/zacknewsham/SAT/sat2014/sc14-app/005-80-12.cnf",
        "ol",
        "f"
      };
    }
    
    HashMap<String, String> patterns = new HashMap<String, String>();

    for (int i = 5; i < args.length; i += 2) {
      patterns.put(args[i], args[i + 1]);
    }
    CommunityGraphFactory factory = (new CommunityGraphFactoryFactory(args[1])).getFactory(new File(args[0]), patterns);
    
    CommunityGraphViewer graphViewer = new CommunityGraphViewer(null, factory.getNodeLists(), null);
    CommunityGraphFrame frmMain = new CommunityGraphFrame(graphViewer, factory.getPatterns(), factory.getMetric());
    frmMain.setProgressive(factory);
    frmMain.preinit();
    
    frmMain.setVisible(true);
    factory.makeGraph(new File(args[0]));
    CommunityPlacer p = CommunityGraphFrame.getPlacer(args[2], factory.getGraph());
    frmMain.setProgressive(p);
    graphViewer.graph = factory.getGraph();
    graphViewer.setPlacer(p);
    frmMain.init();
    
    frmMain.show();
  }
}
