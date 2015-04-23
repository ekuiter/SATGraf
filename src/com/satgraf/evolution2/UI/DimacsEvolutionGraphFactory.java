/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution2.UI;

import com.satlib.NamedFifo;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityGraphViewer;
import com.satlib.community.CommunityNode;
import com.satlib.evolution.EvolutionGraph;
import static com.satlib.evolution.EvolutionGraphFactoryFactory.pipeFileName;
import com.satlib.evolution.EvolutionGraphFactoryObserver;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author zacknewsham
 */
public class DimacsEvolutionGraphFactory extends com.satlib.evolution.DimacsEvolutionGraphFactory{
  public DimacsEvolutionGraphFactory(String minisat, String metricName, HashMap<String, String> patterns) {
    super(minisat, metricName, patterns);
  }

  
  public void process(EvolutionGraph cg) {
    node_lists = new HashMap<>();
    TIntObjectHashMap<String> list = new TIntObjectHashMap<>();
    Iterator<CommunityNode> nodes = cg.getNodeIterator();

    while (nodes.hasNext()) {
      CommunityNode node = nodes.next();
      list.put(node.getId(), node.getName());
    }

    node_lists.put("All", list);
    buildEvolutionFile();
    graph = cg;
    notifyObservers(EvolutionGraphFactoryObserver.Action.process);
  }  

  public void buildEvolutionFile() {
    Runnable r = new Runnable() {

      @Override
      public void run() {
        try {

          NamedFifo fifo = new NamedFifo(pipeFileName);
          
          if (fifo.getFile().exists()){
            fifo.getFile().delete();
          }
    	  fifo.create();

          Runtime.getRuntime().exec(String.format(getMinisat().concat(" %s"), input.getAbsolutePath()));
          BufferedReader reader = openPipedFile();
          String line;

          while ((line = reader.readLine()) != null) {
            outputLine(line);
          }

          closeWriter();
          reader.close();
          fifo.getFile().delete();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    Thread t = new Thread(r);
    t.start();
  }
  
  private BufferedReader openPipedFile() {
	try {
		return new BufferedReader(new FileReader(pipeFileName));
	} catch (Exception e) {
		return openPipedFile();
	}
  }

  @Override
  public void loadAdditionalGraphs()  throws FileNotFoundException, IOException{
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void addGraph(CommunityGraphViewer cg) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
