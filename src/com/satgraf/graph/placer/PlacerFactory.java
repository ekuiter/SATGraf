/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.graph.placer;

import com.satlib.graph.Graph;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;


/**
 *
 * @author zacknewsham
 */
public class PlacerFactory {
  private static final PlacerFactory singleton = new PlacerFactory();
  private final HashMap<String, Class<? extends Placer>> classes = new HashMap<>();
  private final HashMap<String, String> descriptions = new HashMap<>();
  public static PlacerFactory getInstance(){
    return singleton;
  }
  
  public void register(String name, String description, Class<? extends Placer> c){
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
  
  public Placer getByName(String name, Graph graph){
    if(classes.get(name) == null){
      return null;
    }
    else{
      try {
        Constructor<? extends Placer> con = classes.get(name).getConstructor(Graph.class);
        return con.newInstance(graph);
      } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException | SecurityException ex) {
        ex.printStackTrace();
        ex.getCause().printStackTrace();
        return null;
      }
    }
  }
  
  private PlacerFactory(){}
}
