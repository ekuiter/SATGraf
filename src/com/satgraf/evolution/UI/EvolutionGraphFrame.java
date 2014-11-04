/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution.UI;

import com.satgraf.community.UI.CommunityGraphFrame;
import static com.satgraf.evolution2.UI.Evolution2GraphFrame.help;
import static com.satgraf.evolution2.UI.Evolution2GraphFrame.usage;
import com.satlib.community.CommunityGraphViewer;
import com.satlib.community.CommunityMetric;
import com.satlib.community.JSONCommunityGraph;
import com.satlib.evolution.EvolutionGraphFactory;
import com.satlib.evolution.EvolutionGraphFactoryFactory;
import com.satlib.evolution.EvolutionGraphFactoryObserver;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JMenuItem;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author zacknewsham
 */
public class EvolutionGraphFrame extends CommunityGraphFrame implements EvolutionGraphFactoryObserver {
  EvolutionGraphFactory factory;
  private JMenuItem generate = new JMenuItem("Generate");
  private ArrayList<CommunityGraphViewer> graphs = new ArrayList<CommunityGraphViewer>();
  public EvolutionGraphFrame(EvolutionGraphFactory factory, CommunityGraphViewer graphViewer, HashMap<String, Pattern> patterns, CommunityMetric placer) {
    super(graphViewer, patterns, placer);
    menu.add(generate);
    this.factory = factory;
    factory.addObserver(this);
    
    generate.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        EvolutionGenerator eg = new EvolutionGenerator(EvolutionGraphFrame.this);
        eg.setVisible(true);
      }
    });
  }
  
  public void fromJson(JSONObject json){
    JSONCommunityGraph graph = new JSONCommunityGraph((JSONObject)json.get("graphViewer"));
    graph.init();
    this.graphViewer = new CommunityGraphViewer(graph, graph.getNodeLists(), graph);
    this.patterns = new HashMap<>();
    init();
    show(false);
    
    for(Object o : (JSONArray)json.get("graphs")){
      JSONObject g = (JSONObject)o;
      JSONCommunityGraph _graph = new JSONCommunityGraph(g);
      addGraph(new CommunityGraphViewer(_graph, _graph.getNodeLists(), _graph));
    }
    this.graphViewer.fromJson((JSONObject)json.get("graphViewer"));
    super.fromJson(json);
  }
  
  public String toJson(){
    StringBuilder json = new StringBuilder(super.toJson());
    json.setCharAt(json.length() - 1, ',');
    json.append("\"graphs\":[");
    for(CommunityGraphViewer graph : graphs){
      graph.addObserver(panel);
      json.append(graph.toJson()).append(",");
    }
    json.setCharAt(json.length() - 1, ']');
    json.append("}");
    
    return json.toString();
  }
  
  public void init(){
    super.init();
  }
  public void show(){
    show(true);
  }
  public void show(boolean loadExtra){
    if(graphViewer != null){
      panel = new EvolutionOptionsPanel(this, getGraphViewer(), patterns.keySet());
      canvasPanel = new EvolutionPanel(getGraphViewer(), (EvolutionOptionsPanel)panel);
      ((EvolutionOptionsPanel)panel).setEvolutionPanel((EvolutionPanel)canvasPanel);
      super.show();
      setLeftComponent(canvasPanel);
      setRightComponent(panel);
      if(loadExtra){
        Runnable r = new Runnable() {

          @Override
          public void run() {
            try {
              factory.loadAdditionalGraphs();
            } catch (IOException ex) {
            }
          }
        };
        Thread t = new Thread(r);
        t.start();
      }
    }
    else{
      super.show();
    }
    
  }
  public void addGraph(CommunityGraphViewer graph){
    graphs.add(graph);
    ((EvolutionPanel)canvasPanel).addGraph(graph);
  }
  
  
  
  
  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      args = new String[]{
        "formula/satcomp/dimacs/toybox.dimacs",
        "ol",
        "f",
        "5",
        System.getProperty("user.dir") + "/minisat/minisat"
      };
    } else if (args.length < 5) {
      System.out.println("Too few options. Please use:");
      System.out.print(usage().concat("\n").concat(help()));
    }
    HashMap<String, String> patterns = new HashMap<String, String>();

    for (int i = 5; i < args.length; i += 2) {
      patterns.put(args[i], args[i + 1]);
    }
    EvolutionGraphFactory factory = new DimacsEvolutionGraphFactory(args[4], args[1], patterns);
    factory.makeGraph(new File(args[0]));
    
    CommunityGraphViewer graphViewer = new CommunityGraphViewer(factory.getGraph(), factory.getNodeLists(), CommunityGraphFrame.getPlacer(args[2], factory.getGraph()));
    EvolutionGraphFrame frmMain = new EvolutionGraphFrame(factory, graphViewer, factory.getPatterns(), factory.getMetric());
    frmMain.init();
    
    frmMain.show();
  }
  
  

  @Override
  public void notifyObserver(EvolutionGraphFactory factory, Action action) {
    if(action == Action.newline){
      
    //((EvolutionOptionsPanel)panel).newFileReady(factory.getLineNumber());
    }
    else if(action == Action.process){
      show();
    }
  }
}
