/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.evolution;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenuItem;

import visual.NamedFifo;
import visual.community.CommunityGraph;
import visual.community.CommunityGraphViewer;
import visual.community.CommunityGrapher;
import visual.community.CommunityNode;
import visual.community.ConcreteCommunityGraph;

/**
 *
 * @author zacknewsham
 */
public class EvolutionGrapher extends CommunityGrapher{
  private EvolutionPanel canvas;
  private File dumpFile;
  private int dumpFreq = 5;
  private ArrayList<CommunityGraphViewer> graphs = new ArrayList<CommunityGraphViewer>();
  private EvolutionPanel ep;
  private GraphBuilderExecutor gbe = new GraphBuilderExecutor(this);
  private JMenuItem generate = new JMenuItem("Generate");
  /*public EvolutionGrapher(String dimacsFile, String mapFile, String dumpFile, HashMap<String, String> patterns) {
    super(dimacsFile, mapFile, patterns);
    this.dumpFile = new File(dimacsFile.concat(".dump"));
    if(this.dumpFile.exists()){
      this.dumpFile.delete();
    }
  }*/
  
  public EvolutionGrapher(String dimacsFile, String communityMetric, String placer, int dumpFreq, HashMap<String, String> patterns) {
    super(dimacsFile, communityMetric, placer, patterns);
    this.dumpFreq = dumpFreq;
    this.dumpFile = new File(dimacsFile.replaceFirst("[.][^.]+$", "").concat(".dump"));
    if(this.dumpFile.exists()){
      this.dumpFile.delete();
    }
  }
      
  public void process(CommunityGraph cg){
    node_lists = new HashMap<String, TIntObjectHashMap<String>>();
    TIntObjectHashMap<String> list = new TIntObjectHashMap<String>();
    Iterator<CommunityNode> nodes = cg.getNodeIterator();
    while(nodes.hasNext()){
      CommunityNode node = nodes.next();
      list.put(node.getId(), node.getName());
    }
    node_lists.put("All", list);
    try{
      graph = cg;
      double d = Math.random();
      dimacsFile = new File(String.format("/tmp/%f.dimacs", d));
      dimacsFile.createNewFile();
      graph.writeDimacs(dimacsFile);

      dumpFile = new File(String.format("/tmp/%f.dump", d));
      dumpFile.createNewFile();
      loadAdditionalGraphs();
    }
    catch(IOException e){
      
    }
    show();
  }
  
  @Override
  protected void initFrame(){
    super.initFrame();
    menu.add(generate);
    generate.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        EvolutionGenerator eg = new EvolutionGenerator(EvolutionGrapher.this);
        eg.setVisible(true);
      }
    });
  }
  @Override
  protected void show(){
    graphViewer = new CommunityGraphViewer((CommunityGraph)graph, node_lists, this.placer);
    
    panel = new EvolutionOptionsPanel((CommunityGraphViewer)graphViewer, patterns.keySet());
    ep = new EvolutionPanel((CommunityGraphViewer)graphViewer, (EvolutionOptionsPanel)panel);
    ((EvolutionOptionsPanel)panel).setEvolutionPanel(ep);
    super.show();
    setLeftComponent(ep);
    setRightComponent(panel);
    frmMain.setVisible(true);
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
  
  private void loadAdditionalGraphs() throws FileNotFoundException, IOException{
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
    NamedFifo fifo = new NamedFifo(dumpFile);
    fifo.create();
    Process minipure = run.exec(String.format(System.getProperty("user.dir") + "/Minipure/binary/minipure -dump-freq=%d -dump-file=%s %s", dumpFreq, dumpFile.getAbsolutePath(), dimacsFile.getAbsolutePath()));
    BufferedReader reader = new BufferedReader(new FileReader(dumpFile));
    GraphBuilderRunnable gbr = new GraphBuilderRunnable((CommunityGraph)this.graph, patterns, metric.getClass(), placer.getClass());
    Thread t1 = new Thread(gbr);
    t1.start();
    while((line = reader.readLine()) != null){
      if(line.length() != 0 && (line.charAt(0) == 'p' || line.charAt(0) == 'c')){
        continue;
      }
      if(line.equals("$")){
        gbr.finished();
        gbe.addThread(gbr);
        gbr = new GraphBuilderRunnable((CommunityGraph)this.graph, patterns, metric.getClass(), placer.getClass());
        t1 = new Thread(gbr);
        t1.start();
      }
      else{
        gbr.addLine(line);
      }
    }
  }
  
  public void addGraph(CommunityGraphViewer graph){
    graphs.add(graph);
    ep.addGraph(graph);
  }
  
  public static void main(String[] args){
	if (args.length == 0) {
	    args = new String[]{
	      "formula/satcomp/dimacs/fiasco.dimacs",
	      "ol",
	      "kk",
	      "5"
	    };
	}
    HashMap<String, String> patterns = new HashMap<String, String>();
      
    for(int i = 4; i < args.length; i+=2){
      patterns.put(args[i], args[i + 1]);
    }
    EvolutionGrapher ag = new EvolutionGrapher(args[0], args[1], args[2], Integer.parseInt(args[3]), patterns);
    try{
      ag.generateGraph();
      ag.show();
    }
    catch(FileNotFoundException e){
      e.printStackTrace();
    }
    catch(IOException e){
      e.printStackTrace();
    }
  }
}
