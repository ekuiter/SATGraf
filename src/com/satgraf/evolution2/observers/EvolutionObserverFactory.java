/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution2.observers;

import com.satgraf.evolution2.UI.Evolution2GraphViewer;
import com.satlib.evolution.observers.EvolutionObserver;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author zacknewsham
 */
public class EvolutionObserverFactory extends com.satlib.evolution.observers.EvolutionObserverFactory{
  static{
    com.satlib.evolution.observers.EvolutionObserverFactory.setSingleton(new EvolutionObserverFactory());
  }
  private EvolutionObserverFactory(){}
  
  public static EvolutionObserverFactory getInstance(){
    if(singleton == null){
      singleton = new EvolutionObserverFactory();
    }
    return (EvolutionObserverFactory)singleton;
  }
  
  public EvolutionObserver getByName(String name, Evolution2GraphViewer graphViewer){
    if(classes.get(name) == null){
      return null;
    }
    else{
      try {
        Constructor<? extends EvolutionObserver> con = classes.get(name).getConstructor(graphViewer.getClass());
        EvolutionObserver i = con.newInstance(graphViewer);
        observers.add(i);
        return i;
      } 
      catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException | SecurityException ex) {
        Logger.getLogger(com.satlib.evolution.observers.EvolutionObserverFactory.class.getName()).log(Level.SEVERE, null, ex);
        return null;
      }
    }
    
  }
}
