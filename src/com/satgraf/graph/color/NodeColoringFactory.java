/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.graph.color;

import com.satlib.GenericFactory;

/**
 *
 * @author zacknewsham
 */
public class NodeColoringFactory extends GenericFactory<NodeColoring>{
  private static NodeColoringFactory singleton = new NodeColoringFactory();
  public static NodeColoringFactory getInstance(){
    return singleton;
  }
  
  private NodeColoringFactory(){}
  
}
