package com.satgraf.test;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityMetric;
import com.satlib.community.OLCommunityMetric;

/**
 *
 * @author zacknewsham
 */

public class QAgainstTime {
  
  protected File f;
  protected int timeout; 
  public void run(CommunityGraph c, double satCompTime) throws IOException{
    
    CommunityMetric metric = new OLCommunityMetric();
    double Q = metric.getCommunities(c);
    long startTime = System.currentTimeMillis();
    System.out.printf("\"%s\",%d,%d,%d,%d,", f.getAbsolutePath(), c.getNodes().size(), c.getClausesCount(), c.getCommunities().size(),c.getEdgesList().size());
    
    if(satCompTime == 0.0){
      Runtime run = Runtime.getRuntime();
      Process minipure = run.exec(String.format("/usr/bin/minisat %s", f.getAbsolutePath()));
      try {
        synchronized(minipure){
          minipure.wait(timeout);
        }
        try{
          minipure.destroy();
        }
        catch(Exception e){
          
        }
        long endTime = System.currentTimeMillis();
        System.out.printf("%d,%d,%f\n", endTime-startTime, minipure.exitValue(), Q);
      } 
      catch (Exception ex) {
        long endTime = System.currentTimeMillis();
        System.out.printf("%d,%d,%f\n", endTime-startTime, 30, Q);
      }
      
    }
    else{
        System.out.printf("%d,%d,%f\n", (int)(satCompTime * 1000), 40, Q);
    }
  }
  public void run(double satCompTime) throws IOException{
    System.out.printf("\n%s,",f.getAbsolutePath());
    /*CommunityGrapher cg = new CommunityGrapher(f.getAbsolutePath(), "ol","kk",new HashMap<String, String>());
    cg.generateGraph();
    CommunityGraph c = cg.getGraph();
    run(c, satCompTime);*/
    
    
  }
  public static void main(String[] args) throws IOException{
    if(args.length == 0){
      args = new String[]{
        "100",
        "15",
        "application",
        "/Users/zacknewsham/Downloads/Core_solvers_Sequential_Application_SAT_results.csv"
      };
    }
    int timeLimit = Integer.parseInt(args[0]);
    int choose = Integer.parseInt(args[1]);
    HashMap<String, Double> chosen = SATChooser.choose( timeLimit, choose, args[2], args[3]);
    Iterator<String> chosenI = chosen.keySet().iterator();
    while(chosenI.hasNext()){
      String s = chosenI.next();
      QAgainstTime q = new QAgainstTime();
      q.f = new File(s);
      q.timeout = timeLimit * 2000;
      try{
        q.run(chosen.get(s));
      }
      catch(Exception e){
        e.printStackTrace();
      }
    }
  }
}
