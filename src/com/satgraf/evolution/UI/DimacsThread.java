/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution.UI;

import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.satlib.graph.Graph;
import com.satlib.graph.Node;

/**
 *
 * @author zacknewsham
 */
public class DimacsThread implements Runnable{
  Graph g;
  EvolutionGrapher callback;
  private boolean finished = false;
  private final ArrayList<String> ts = new ArrayList<String>();
  private TIntObjectHashMap<String> n;
  public DimacsThread(Graph g, EvolutionGrapher callback, TIntObjectHashMap<String> n){
    this.g = g;
    this.callback = callback;
    this.n = n;
  }
  public void produce(String t){
    synchronized(ts){
      ts.add(t);
    }
  }
  public void setFinished(){
    this.finished = true;
  }
  private String consume(){
    while(!finished || !ts.isEmpty()){
      synchronized(ts){
        if(!ts.isEmpty()){
          return ts.remove(0);
        }
      }
      try {
        Thread.sleep(1);
      } catch (InterruptedException ex) {
        Logger.getLogger(DimacsThread.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return null;
  }
  @Override
  public void run() {
    while(true){
      String t1 = consume();
      if(t1 == null){
        return;
      }
      String[] t = t1.split(" ");
      for(int i = 0; i < t.length - 2; i++){
        int lit1 = Integer.parseInt(t[i]);
        if(lit1 < 0){
          lit1 = 0 - lit1;
        }
        /*boolean aExists = false;
        if(g.getNode(lit1) != null){
          aExists = true;
        }*/
        Node a1 = g.createNode(lit1, null);
        n.put(lit1, "");
        for(int a = i + 1; a < t.length - 1; a++){
          int lit2 = Integer.parseInt(t[a]);
          if(lit2 < 0){
            lit2 = 0 - lit2;
          }
          /*if(aExists && g.getNode(lit2) != null){
            continue;
          }*/
          Node b1 = g.createNode(lit2, null);
          n.put(lit2, "");
          //g.union(a1, b1);
          g.connect(a1, b1, false);
        }
      }
    }
  }
}
