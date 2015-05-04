/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution.UI;

import com.satlib.NamedFifo;
import com.satlib.community.CommunityNode;
import com.satlib.evolution.EvolutionGraph;
import static com.satlib.evolution.EvolutionGraphFactoryFactory.pipeFileName;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    Iterator<CommunityNode> nodes = cg.getNodes().iterator();

    while (nodes.hasNext()) {
      CommunityNode node = nodes.next();
      list.put(node.getId(), node.getName());
    }

    node_lists.put("All", list);
    buildEvolutionFile();
    graph = cg;
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
    Timer timer = new Timer();
    TimerTask watchdog = new TimerTask(){
      @Override
      public void run() {
        System.err.println("Unable to open pipe file: " + pipeFileName);
      }
    };
    timer.schedule(watchdog, 30000);
    
	try {
      return new BufferedReader(new FileReader(pipeFileName));
	} 
    catch (Exception e) {
      Logger.getLogger(DimacsEvolutionGraphFactory.class.getName()).log(Level.SEVERE, null, e);
      return openPipedFile();
	}
    finally{
      watchdog.cancel();
    }
  }
}
