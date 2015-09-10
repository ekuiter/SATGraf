/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution.observers;

import com.satgraf.evolution.UI.EvolutionGraphViewer;
import com.satlib.evolution.observers.EvolutionObserverFactory;
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
public class VisualEvolutionObserverFactory{
  private static final VisualEvolutionObserverFactory singleton = new VisualEvolutionObserverFactory();
  
  protected final HashMap<String, Class<? extends VisualEvolutionObserver>> classes = new HashMap<>();
  private final HashMap<String, String> descriptions = new HashMap<>();
  private VisualEvolutionObserverFactory(){}
  
  public static VisualEvolutionObserverFactory getInstance(){
    return singleton;
  }
  
  public VisualEvolutionObserver getByName(String name, EvolutionGraphViewer graphViewer){
    if(classes.get(name) == null){
      return null;
    }
    else{
      try {
        Constructor<? extends VisualEvolutionObserver> con = classes.get(name).getConstructor(graphViewer.getClass());
        VisualEvolutionObserver i = con.newInstance(graphViewer);
        EvolutionObserverFactory.getInstance().addObserver(i);
        return i;
      } 
      catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException | SecurityException ex) {
        Logger.getLogger(VisualEvolutionObserverFactory.class.getName()).log(Level.SEVERE, null, ex);
        return null;
      }
    }
    
  }
  
  
  public void register(String name, String description, Class<? extends VisualEvolutionObserver> c){
    classes.put(name, c);
    descriptions.put(name, description);
  }
  
  public String[] getDescriptions(){
    String[] names = new String[classes.size()];
    descriptions.values().toArray(names);
    return names;
  }
  
  public String[] getNames(){
    String[] names = new String[classes.size()];
    classes.keySet().toArray(names);
    return names;
  }
}
