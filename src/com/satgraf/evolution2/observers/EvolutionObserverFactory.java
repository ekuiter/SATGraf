/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution2.observers;

/**
 *
 * @author zacknewsham
 */
public class EvolutionObserverFactory extends com.satlib.evolution.observers.EvolutionObserverFactory{
  static{
    com.satlib.evolution.observers.EvolutionObserverFactory.setSingleton(new EvolutionObserverFactory());
  }
  private EvolutionObserverFactory(){}
  
}
