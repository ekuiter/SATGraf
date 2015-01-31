/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution.UI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JMenuItem;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.satgraf.community.UI.CommunityGraphFrame;
import com.satlib.community.CommunityGraphViewer;
import com.satlib.community.CommunityMetric;
import com.satlib.community.CommunityMetricFactory;
import com.satlib.community.JSONCommunityGraph;
import com.satlib.community.placer.CommunityPlacerFactory;
import com.satlib.evolution.EvolutionGraphFactory;
import com.satlib.evolution.EvolutionGraphFactoryObserver;
import com.validatedcl.validation.Help;
import com.validatedcl.validation.RequiredOption;
import com.validatedcl.validation.ValidatedCommandLine;
import com.validatedcl.validation.ValidatedOption;
import com.validatedcl.validation.rules.FileValidationRule;
import com.validatedcl.validation.rules.ListValidationRule;

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
  
  
  public static Options options(){
    Options options = new Options();
    ValidatedOption o;
    
    o = new RequiredOption("f","file",true, "The file (either .cnf or .sb)");
    o.addRule(new FileValidationRule(FileValidationRule.FileExists.yes, new String[]{"read"}));
    options.addOption(o);
    
    o = new ValidatedOption("c", "community", true,"The community detection algorithm");
    o.setDefault("ol");
    o.addRule(new ListValidationRule(CommunityMetricFactory.getInstance().getNames()));
    options.addOption(o);
    
    o = new ValidatedOption("s","solver",true,"The location of the modified solver");
    o.addRule(new FileValidationRule(FileValidationRule.FileExists.yes,new String[]{"EXECUTE"}));
    o.setDefault(System.getProperty("user.dir") + "/solvers/Minipure/binary/minipure");
    options.addOption(o);
    
    o = new ValidatedOption("l","layout",true,"The layout algorithm to use");
    o.setDefault("f");
    o.addRule(new ListValidationRule(CommunityPlacerFactory.getInstance().getNames()));
    options.addOption(o);
    
    
    o = new ValidatedOption("p", "pattern",true,"A list of regex expressions to group variables (not yet implemented)");
    options.addOption(o);
    
    return options;
  }
  
  
  public static void main(String[] args) throws IOException, ParseException {
    if (args.length == 0) {
      args = new String[]{
        "-f","formula/satcomp/dimacs/toybox.dimacs",
        "-c","ol",
        "-l","f"
      };
      System.out.print(Help.getHelp(options()));
      //return;
    }
    CommandLineParser clp = new GnuParser();
    Options o = options();
    CommandLine cl = clp.parse(o, args);
    
    if(!ValidatedCommandLine.validateCommandLine(o, cl)){
      System.err.println(ValidatedCommandLine.getError());
      return;
    }
    HashMap<String, String> patterns = new HashMap<String, String>();

    /*for (int i = 5; i < args.length; i += 2) {
      patterns.put(args[i], args[i + 1]);
    }*/
    
    EvolutionGraphFactory factory = new DimacsEvolutionGraphFactory(cl.getOptionValue("s", o.getOption("s").getValue()), cl.getOptionValue("c", o.getOption("c").getValue()), patterns);
    factory.makeGraph(new File(cl.getOptionValue("f")));
    CommunityGraphViewer graphViewer = new CommunityGraphViewer(factory.getGraph(), factory.getNodeLists(), CommunityPlacerFactory.getInstance().getByName(cl.getOptionValue("l", o.getOption("l").getValue()), factory.getGraph()));
    EvolutionGraphFrame frmMain = new EvolutionGraphFrame(factory, graphViewer, factory.getPatterns(), factory.getMetric());
    frmMain.setProgressive(factory);
    frmMain.init();
    frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frmMain.show();
  }
  
  

  @Override
  public void notifyObserver(EvolutionGraphFactory factory, Action action) {
    if(action == Action.newline){
    }
    else if(action == Action.process){
      show();
    } else if (action == Action.addgraph) {
    	List<CommunityGraphViewer> graphs = ((DimacsEvolutionGraphFactory)this.factory).getGraphs();
    	CommunityGraphViewer gv = graphs.get(graphs.size()-1);
    	((EvolutionPanel)canvasPanel).addGraph(gv);
    }
  }
}
