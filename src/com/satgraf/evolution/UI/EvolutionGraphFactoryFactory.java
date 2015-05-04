/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution.UI;

import com.satlib.evolution.EvolutionGraphFactory;
import java.io.File;
import java.net.URL;
import java.util.HashMap;

/**
 *
 * @author zacknewsham
 */
public class EvolutionGraphFactoryFactory extends com.satlib.evolution.EvolutionGraphFactoryFactory{

  

  private static EvolutionGraphFactory instance;
  public static EvolutionGraphFactory getInstance(){
    return instance;
  }
  public EvolutionGraphFactoryFactory(String metricName, String minisat) {
    super(metricName, minisat);
  }

  public EvolutionGraphFactory getFactory(URL input, HashMap<String, String> patterns) {
    /*if(DimacsEvolutionGraphFactory.current != null){
      DimacsEvolutionGraphFactory.current.close();
    }*/
    return instance = new RemoteDimacsEvolutionGraphFactory(this.metricName, patterns);
  }

  @Override
  public EvolutionGraphFactory getFactory(File input, HashMap<String, String> patterns) {
    /*if(DimacsEvolutionGraphFactory.current != null){
      DimacsEvolutionGraphFactory.current.close();
    }*/
    return instance = new DimacsEvolutionGraphFactory(this.minisat, this.metricName, patterns);
  }
  
}
