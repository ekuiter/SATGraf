package com.satgraf.evolution2.UI;

import com.satgraf.community.UI.CommunityCanvas;
import com.satgraf.community.UI.CommunityGraphFrame;
import com.satgraf.graph.UI.GraphCanvasPanel;
import com.satlib.community.CommunityGraphViewer;
import com.satlib.community.CommunityMetric;
import com.satlib.evolution.EvolutionGraphFactory;
import com.satlib.evolution.EvolutionGraphFactoryObserver;
import com.satlib.evolution.EvolutionGraphFactoryObserver.Action;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Evolution2GraphFrame extends CommunityGraphFrame implements EvolutionGraphFactoryObserver{

  private EvolutionGraphFactory factory;
  public Evolution2GraphFrame(EvolutionGraphFactory factory, CommunityGraphViewer viewer, HashMap<String, Pattern> patterns, CommunityMetric metric) {
    super(viewer, patterns, metric);
    this.factory = factory;
    factory.addObserver(this);
  }

  public String toJson() {
    StringBuilder json = new StringBuilder(super.toJson());

    return json.toString();
  }

  public void show() {
    if (graphViewer != null) {
      canvasPanel = new GraphCanvasPanel(new CommunityCanvas(graphViewer));
      panel = new Evolution2OptionsPanel(this, getGraphViewer(), patterns.keySet());
      super.show();
      factory.buildEvolutionFile();
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
    EvolutionGraphFactory factory = new DimacsEvolutionGraphFactory(args[4], args[1], patterns);
    factory.makeGraph(new File(args[0]));
    
    CommunityGraphViewer graphViewer = new CommunityGraphViewer(factory.getGraph(), factory.getNodeLists(), CommunityGraphFrame.getPlacer(args[2], factory.getGraph()));
    Evolution2GraphFrame frmMain = new Evolution2GraphFrame(factory, graphViewer, factory.getPatterns(), factory.getMetric());
    frmMain.init();
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
