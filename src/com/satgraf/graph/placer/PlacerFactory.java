/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.graph.placer;

import com.satlib.GenericFactory;


/**
 *
 * @author zacknewsham
 */
public class PlacerFactory extends GenericFactory<Placer>{
  private static PlacerFactory singleton = new PlacerFactory();
  public static PlacerFactory getInstance(){
    return singleton;
  }
  
  private PlacerFactory(){}
}
