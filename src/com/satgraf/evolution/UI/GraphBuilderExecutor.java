/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution.UI;

import com.satlib.evolution.EvolutionGraphFactory;
import java.util.ArrayList;

/**
 *
 * @author zacknewsham
 */
public class GraphBuilderExecutor implements Runnable{
  private final ArrayList<GraphBuilderRunnable> runnables = new ArrayList<>();
  private final EvolutionGraphFactory callback;
  public GraphBuilderExecutor(EvolutionGraphFactory callback){
    this.callback = callback;
  }
  @Override
  public void run() {
    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
    while(true){
      boolean empty = false;
      synchronized(runnables){
        if(runnables.isEmpty()){
          empty = true;
        }
      }
      if(empty){
        try {
          Thread.sleep(1000);
        } 
        catch (InterruptedException ex) {

        }
      }
      GraphBuilderRunnable run = null;
      synchronized(runnables){
        //dont actually want threads at this point in time.
        if(!runnables.isEmpty()){
          if(runnables.get(0).isFinished()){
            run = runnables.remove(0);
          }
        }
      }
      if(run != null && run.isFinished()){
        callback.addGraph(run.getGraphViewer());
      }
      else if(run != null){
        synchronized(runnables){
          runnables.add(run);
        }
      }
    }
  }
  
  public void addThread(GraphBuilderRunnable runnable){
    synchronized(runnables){
      runnables.add(runnable);
    }
  }
  
}
