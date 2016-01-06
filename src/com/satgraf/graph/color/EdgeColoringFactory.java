/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.graph.color;

import com.satlib.GenericFactory;
import com.satlib.graph.Graph;

/**
 *
 * @author zacknewsham
 */
public class EdgeColoringFactory extends GenericFactory<EdgeColoring, Graph>{
  private static EdgeColoringFactory singleton = new EdgeColoringFactory();
  public static EdgeColoringFactory getInstance(){
    return singleton;
  }
  
  private EdgeColoringFactory(){}
}
