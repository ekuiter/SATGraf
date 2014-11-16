/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution2.UI;

import com.satlib.community.CommunityGraphFactory;
import com.satlib.evolution.EvolutionGraphFactory;
import com.satlib.evolution.EvolutionGraphFactoryFactory;
import java.io.File;
import java.net.URL;
import java.util.HashMap;

/**
 *
 * @author zacknewsham
 */
public class Evolution2GraphFactoryFactory extends EvolutionGraphFactoryFactory{

  

  private static EvolutionGraphFactory instance;
  public static EvolutionGraphFactory getInstance(){
    return instance;
  }
  public Evolution2GraphFactoryFactory(String metricName, String minisat) {
    super(metricName, minisat);
  }

  public CommunityGraphFactory getFactory(URL input, HashMap<String, String> patterns) {
    return instance = new RemoteDimacsEvolutionGraphFactory(this.metricName, patterns);
  }

  @Override
  public EvolutionGraphFactory getFactory(File input, HashMap<String, String> patterns) {
    return instance = new DimacsEvolutionGraphFactory(this.minisat, this.metricName, patterns);
  }
  
}
