/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.placer;

import com.satlib.community.CommunityGraph;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 *
 * @author zacknewsham
 */
public class CommunityPlacerFactory {
  private static final CommunityPlacerFactory singleton = new CommunityPlacerFactory();
  public static CommunityPlacerFactory getInstance(){
    return singleton;
  }
  
  private static final HashMap<String, Class<? extends CommunityPlacer>> classes = new HashMap<>();
    
  public void register(String name, Class<? extends CommunityPlacer> c){
    classes.put(name, c);
  }
  
  public String[] getNames(){
    String[] names = new String[classes.size()];
    classes.keySet().toArray(names);
    return names;
  }
  
  public CommunityPlacer getByName(String name, CommunityGraph graph){
    if(classes.get(name) == null){
      return null;
    }
    else{
      try {
        Constructor<? extends CommunityPlacer> con = classes.get(name).getConstructor(CommunityGraph.class);
        return con.newInstance(graph);
      } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException | SecurityException ex) {
        return null;
      }
    }
  }
  private CommunityPlacerFactory(){}
}
