/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution.UI;

import com.satgraf.community.placer.FruchPlacer;
import com.satlib.NamedFifo;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityGraphViewer;
import com.satlib.community.CommunityNode;
import com.satlib.community.ConcreteCommunityGraph;
import com.satlib.evolution.EvolutionGraphFactoryObserver;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import static com.satlib.evolution.EvolutionGraphFactoryFactory.pipeFileName;

/**
 *
 * @author zacknewsham
 */
public class DimacsEvolutionGraphFactory  extends com.satlib.evolution.DimacsEvolutionGraphFactory{
  private File dimacsFile;
  private List<CommunityGraphViewer> graphs;
  private GraphBuilderExecutor gbe = new GraphBuilderExecutor(this);
  public DimacsEvolutionGraphFactory(String minisat, String metricName, HashMap<String, String> patterns) {
    super(minisat, metricName, patterns);
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
    }
    catch(IOException e){
      
    }
    notifyObservers(EvolutionGraphFactoryObserver.Action.process);
  }

  @Override
  public void buildEvolutionFile() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
  public void addGraph(CommunityGraphViewer cg){
    graphs.add(cg);
    notifyObservers(EvolutionGraphFactoryObserver.Action.addgraph);
  }
  
  public List<CommunityGraphViewer> getGraphs() {
	  return this.graphs;
  }
  
  public void loadAdditionalGraphs() throws FileNotFoundException, IOException{
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
    NamedFifo fifo = new NamedFifo(pipeFileName);
    fifo.create();
    Process minipure = run.exec(String.format(getMinisat() + " -dump-freq=%d -dump-file=%s %s", dumpFreq, pipeFileName, input.getAbsolutePath()));
    BufferedReader reader = new BufferedReader(new FileReader(pipeFileName));
    GraphBuilderRunnable gbr = new GraphBuilderRunnable(
            getGraph(), 
            patterns, 
            getMetric().getClass(), 
            new FruchPlacer(getGraph()).getClass());
    Thread t1 = new Thread(gbr);
    t1.start();
    while((line = reader.readLine()) != null){
      if(line.length() == 0 || line.charAt(0) == 'p' || line.charAt(0) == 'c'){
        continue;
      }
      if(line.equals("$") && gbr.getLineCount() > 0){
        gbr.finished();
        gbe.addThread(gbr);
        gbr = new GraphBuilderRunnable(getGraph(), patterns, getMetric().getClass(), new FruchPlacer(getGraph()).getClass());
        t1 = new Thread(gbr);
        t1.start();
      }
      else{
        gbr.addLine(line);
      }
    }
  }
  
}
