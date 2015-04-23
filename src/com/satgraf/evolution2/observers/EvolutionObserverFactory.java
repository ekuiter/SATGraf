/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution2.observers;

import com.satlib.evolution.EvolutionGraph;
import com.satlib.evolution.EvolutionGraphFactoryObserver;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author zacknewsham
 */
public class EvolutionObserverFactory {
  private static final EvolutionObserverFactory singleton = new EvolutionObserverFactory();
  private static final HashMap<String, Class<? extends EvolutionObserver>> classes = new HashMap<>();
  private static final List<EvolutionObserver> observers = new ArrayList<>();
  public static EvolutionObserverFactory getInstance(){
    return singleton;
  }
  
    
  public void register(String name, Class<? extends EvolutionObserver> c){
    classes.put(name, c);
  }
  
  public String[] getNames(){
    String[] names = new String[classes.size()];
    classes.keySet().toArray(names);
    return names;
  }
  
  public EvolutionObserver getByName(String name, EvolutionGraph graph){
    if(classes.get(name) == null){
      return null;
    }
    else{
      try {
        Constructor<? extends EvolutionObserver> con = classes.get(name).getConstructor(EvolutionGraph.class);
        EvolutionObserver i = con.newInstance(graph);
        observers.add(i);
        return i;
      } 
      catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException | SecurityException ex) {
        Logger.getLogger(EvolutionObserverFactory.class.getName()).log(Level.SEVERE, null, ex);
        return null;
      }
    }
  }
  public List<EvolutionObserver> observers(){
    return observers;
  }
  private EvolutionObserverFactory(){}
}
