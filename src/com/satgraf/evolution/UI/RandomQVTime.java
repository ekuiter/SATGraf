/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution.UI;

import java.io.File;
import java.io.IOException;
import com.satlib.community.CommunityGraph;

/**
 *
 * @author zacknewsham
 */
public class RandomQVTime {
  public static void runAll(
          int vFrom, int vTo, int vInc,
          int clFrom, int clTo, int clInc,
          double qFrom, double qTo, double qInc,
          int coFrom, int coTo, int coInc,
          int reps
          ) throws IOException{
    int count = 0; 
    int total = 
            (((vTo - vFrom) / vInc) *
            ((clTo - clFrom) / clInc) * 
            ((coTo - coFrom) / coInc) * 
            (int)((qTo - qFrom) / qInc)) * reps;
    int skip = 0;
    for(int v = vFrom; v <= vTo; v += vInc){
      for(int cl = clFrom; cl <= clTo; cl += clInc){
        for(int co = coFrom; co <= coTo; co += coInc){
          for(double q = qFrom; q <= qTo; q += qInc){
            for(int r = 0; r < reps; r++){
              count++;
              if(count < skip){
                continue;
              }
              CommunityGraph cg = EvolutionGenerator.makeCommunity(v, cl, co, q);
              cg.writeDimacs(new File(String.format("/media/SAT/bucketing/%d.%d.%d.%d.%d.cnf",v,cl,co,Long.parseLong(String.valueOf(q).replace("0.","")), r)));
              if(count % 100 == 0){
                System.out.printf("%d/%d\n", count, total);
              }
            }
          }
        }
      }
    }
  }
  
  public static void main(String[] args) throws IOException{
    if(args.length == 0){
      args = new String[]{
        "500",
        "2000",
        "100",
        "2000",
        "10000",
        "1000",
        "0",
        "1",
        "0.01",
        "20",
        "400",
        "20",
        "3"
      };
    }
    runAll(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), 
            Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), 
            Double.parseDouble(args[6]), Double.parseDouble(args[7]), Double.parseDouble(args[8]), 
            Integer.parseInt(args[9]), Integer.parseInt(args[10]), Integer.parseInt(args[11]), 
            Integer.parseInt(args[12]));
  }
  
}
