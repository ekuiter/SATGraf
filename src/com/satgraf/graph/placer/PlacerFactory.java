/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.graph.placer;

import com.satlib.GenericFactory;
import com.satlib.graph.Graph;


/**
 *
 * @author zacknewsham
 */
public class PlacerFactory extends GenericFactory<Placer, Graph>{
  private static PlacerFactory singleton = new PlacerFactory();
  public static PlacerFactory getInstance(){
    return singleton;
  }
  
  private PlacerFactory(){}
}
