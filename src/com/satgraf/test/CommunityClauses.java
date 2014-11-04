package com.satgraf.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import com.satlib.community.CommunityGraph;
import com.satlib.community.OLCommunityMetric;
import com.satgraf.evolution.UI.EvolutionGenerator;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author zacknewsham
 */
public class CommunityClauses extends SATChooser{
  protected File f;
  protected File coms;
  public CommunityClauses(){
  }
  public void run() throws FileNotFoundException, IOException{
    //CommunityGrapher cg = new CommunityGrapher(file, new HashMap<String, String>());
    //cg.generateGraph();
    double Q = OLCommunityMetric.getQ(f.getAbsolutePath(), new PrintStream(new FileOutputStream(coms)));
    Runtime run = Runtime.getRuntime();
    
    long startTime1 = System.currentTimeMillis();
    Process minipure = run.exec(String.format("/Users/zacknewsham/Sites/SMTHashTools/Minipure/code/core/minisat -com-file=%s %s", coms.getAbsolutePath(), f.getAbsolutePath()));
    long endTime1 = 0;
    try {
      synchronized(minipure){
        minipure.wait(5 * 60 * 1000);
      }
      endTime1 = System.currentTimeMillis();
      minipure.destroy();
    } 
    catch (InterruptedException ex) {
    }
    long startTime2 = System.currentTimeMillis();
    Process minipure1 = run.exec(String.format("/Users/zacknewsham/Sites/SMTHashTools/Minipure/code/core/minisat %s", f.getAbsolutePath()));
    long endTime2 = 0;
    
    try {
      synchronized(minipure1){
        minipure1.wait(5 * 60 * 1000);
      }
      endTime2 = System.currentTimeMillis();
      minipure1.destroy();
    } 
    catch (InterruptedException ex) {
    }
    
    System.out.printf("%f,%d,%d\n", Q, endTime1-startTime1, endTime2-startTime2);
  }
  
  public static void main(String[] args) throws FileNotFoundException, IOException{
    int timeLimit = Integer.parseInt(args[0]);
    int choose = Integer.parseInt(args[1]);
    HashMap<String, Double> chosen = SATChooser.choose(timeLimit, choose, args[2], args[3]);
    Iterator<String> chosenI = chosen.keySet().iterator();
    while(chosenI.hasNext()){
      String s = chosenI.next();
      CommunityClauses c = new CommunityClauses();
      c.f = new File(s);
      c.coms = new File(s.concat(".com"));
      c.run();
    }
  }
}
