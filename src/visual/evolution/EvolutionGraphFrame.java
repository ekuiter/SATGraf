/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.evolution;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JMenuItem;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import visual.NamedFifo;
import visual.UI.GraphFrame;
import visual.community.CommunityGraph;
import visual.community.CommunityGraphFrame;
import visual.community.CommunityGraphViewer;
import visual.community.CommunityGrapher;
import visual.community.CommunityMetric;
import visual.community.ConcreteCommunityGraph;
import visual.community.JSONCommunityGraph;
import visual.graph.GraphViewer;

/**
 *
 * @author zacknewsham
 */
public class EvolutionGraphFrame extends CommunityGraphFrame {
  EvolutionGrapher grapher;
  private JMenuItem generate = new JMenuItem("Generate");
  private ArrayList<CommunityGraphViewer> graphs = new ArrayList<CommunityGraphViewer>();
  private GraphBuilderExecutor gbe = new GraphBuilderExecutor(this);
  public EvolutionGraphFrame(CommunityGraphViewer graphViewer, HashMap<String, Pattern> patterns, EvolutionGrapher grapher) {
    super(graphViewer, patterns);
    menu.add(generate);
    this.grapher = grapher;
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
      graph.setGraphPanel(panel);
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
      panel = new EvolutionOptionsPanel((CommunityGraphViewer)graphViewer, patterns.keySet());
      canvasPanel = new EvolutionPanel((CommunityGraphViewer)graphViewer, (EvolutionOptionsPanel)panel);
      ((EvolutionOptionsPanel)panel).setEvolutionPanel((EvolutionPanel)canvasPanel);
      super.show();
      setLeftComponent(canvasPanel);
      setRightComponent(panel);
      if(loadExtra){
        Runnable r = new Runnable() {

          @Override
          public void run() {
            try {
              loadAdditionalGraphs();
            } catch (IOException ex) {
              Logger.getLogger(EvolutionGrapher.class.getName()).log(Level.SEVERE, null, ex);
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
  
  public void process(CommunityGraph cg){
    grapher.process(cg);
  }
  void loadAdditionalGraphs() throws FileNotFoundException, IOException{
    graphs = new ArrayList<>();
    Thread t = new Thread(gbe);
    t.start();
    String line;
    CommunityGraph g = new ConcreteCommunityGraph();
    //DimacsThread thread = new DimacsThread(g, this, n);
    //Thread t = new Thread(thread);
    //t.start();
    boolean clean = false;
    int lineCount = 0;
    
    Runtime run = Runtime.getRuntime();
    NamedFifo fifo = new NamedFifo(grapher.getDumpFile());
    fifo.create();
    Process minipure = run.exec(String.format(System.getProperty("user.dir") + "/Minipure/binary/minipure -dump-freq=%d -dump-file=%s %s", grapher.getDumpFreq(), grapher.getDumpFile().getAbsolutePath(), grapher.getDimacsFile().getAbsolutePath()));
    BufferedReader reader = new BufferedReader(new FileReader(grapher.getDumpFile()));
    GraphBuilderRunnable gbr = new GraphBuilderRunnable(
            grapher.getGraph(), 
            patterns, 
            grapher.getMetric().getClass(), 
            ((CommunityGraphViewer)graphViewer).placer.getClass());
    Thread t1 = new Thread(gbr);
    t1.start();
    while((line = reader.readLine()) != null){
      if(line.length() != 0 && (line.charAt(0) == 'p' || line.charAt(0) == 'c')){
        continue;
      }
      if(line.equals("$")){
        gbr.finished();
        gbe.addThread(gbr);
        gbr = new GraphBuilderRunnable(grapher.getGraph(), patterns, grapher.getMetric().getClass(), ((CommunityGraphViewer)graphViewer).placer.getClass());
        t1 = new Thread(gbr);
        t1.start();
      }
      else{
        gbr.addLine(line);
      }
    }
  }
  
  
  public static void main(String args[]){
    EvolutionGraphFrame frame = new EvolutionGraphFrame(null, null, null);
    
    frame.show();
  }
}
