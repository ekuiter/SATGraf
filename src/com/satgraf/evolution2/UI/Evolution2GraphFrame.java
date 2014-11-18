package com.satgraf.evolution2.UI;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import com.satgraf.community.UI.CommunityGraphFrame;
import com.satgraf.graph.UI.GraphCanvasPanel;
import com.satlib.community.CommunityMetric;
import com.satlib.community.placer.CommunityPlacer;
import com.satlib.evolution.EvolutionGraphFactory;
import com.satlib.evolution.EvolutionGraphFactoryObserver;

public class Evolution2GraphFrame extends CommunityGraphFrame implements EvolutionGraphFactoryObserver{

  private EvolutionGraphFactory factory;
  public Evolution2GraphFrame(EvolutionGraphFactory factory, Evolution2GraphViewer viewer, HashMap<String, Pattern> patterns, CommunityMetric metric) {
    super(viewer, patterns, metric);
    this.factory = factory;
    factory.addObserver(this);
  }

  public String toJson() {
    StringBuilder json = new StringBuilder(super.toJson());

    return json.toString();
  }
  
  public Evolution2GraphViewer getGraphViewer(){
	 return (Evolution2GraphViewer)graphViewer;
  }

  public void show() {
    if (graphViewer != null && graphViewer.graph != null && panel == null) {
      canvasPanel = new GraphCanvasPanel(new Evolution2GraphCanvas(graphViewer));
      panel = new Evolution2OptionsPanel(this, getGraphViewer(), patterns.keySet());
      factory.buildEvolutionFile();
      super.show();
    } else {
      super.show();
    }
  }

  @Override
  public com.satgraf.actions.OpenAction getOpenAction() {
    return new OpenAction(this);
  }

  @Override
  public com.satgraf.actions.ExportAction getExportAction() {
    return new ExportAction(this);
  }
  

  @Override
  public void init() {
    super.init();
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
    
    Evolution2GraphFactoryFactory factoryfactory = new Evolution2GraphFactoryFactory(args[1], args[4]);
    EvolutionGraphFactory factory = factoryfactory.getFactory(new File(args[4]), patterns);//new DimacsEvolutionGraphFactory(args[4], args[1], patterns);
    
    Evolution2GraphViewer graphViewer = new Evolution2GraphViewer(null, factory.getNodeLists(), null);
    Evolution2GraphFrame frmMain = new Evolution2GraphFrame(factory, graphViewer, factory.getPatterns(), factory.getMetric());
    frmMain.setProgressive(factory);
    frmMain.preinit();
    
    frmMain.setVisible(true);
    factory.makeGraph(new File(args[0]));
    CommunityPlacer p = CommunityGraphFrame.getPlacer(args[2], factory.getGraph());
    frmMain.setProgressive(p);
    graphViewer.graph = factory.getGraph();
    graphViewer.setPlacer(p);
    frmMain.init();
    frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frmMain.show();
  }

  public static String usage() {
    return "[formula/path.cnf | saved/path.sb] [ol | cnm] [f | grid | kk] [dumpfreq] [/path/to/solver]";
  }

  public static String help() {
    return "\"formula\" \"community algorithm\" \"layout algorithm\" \"dump frequency\" \"path to modified solver + options\"\n"
            + "View the evolution of the community VIG of a SAT formula while being solved, dumping after every [dumpfreq] variable assignments";
  }

  @Override
  public void notifyObserver(EvolutionGraphFactory factory, Action action) {
    if(action == Action.newline){
      
    ((Evolution2OptionsPanel)panel).newFileReady(factory.getLineNumber());
    }
    else if(action == Action.process){
      show();
    }
  }
}
