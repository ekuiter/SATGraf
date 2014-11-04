package com.satgraf.test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.satlib.community.CommunityGraph;
import com.satgraf.evolution.UI.EvolutionGenerator;

/**
 *
 * @author zacknewsham
 */

public class QAgainstTimeRandomBucketing extends com.satgraf.test.QAgainstTime{
  
  public void run(int vars, int clauses, int buckets, double prob) throws Exception{
    CommunityGraph c = EvolutionGenerator.makeCommunity(vars, clauses, buckets, prob);
    f = new File(String.format("/home/sfischme/work/vijay/comdelete/tmp/%d.%d.%d.dimacs", vars, clauses, buckets));
    c.writeDimacs(f);
    timeout = 60 * 15 * 1000;
    super.run(c, 0.0);
  }
  
  public void getRuns(int varsFrom, int varsTo, int varsInc, int clausesFrom, int clausesTo, int clausesInc, int buckets){
    ArrayList<Object[]> col =new ArrayList<>();
    for(int i = 0; i < 3; i++){
      for(int q = 0; q < 100; q++){
        for(int v = varsFrom; v < varsTo; v+= varsInc){
          for(int c = clausesFrom; c < clausesTo; c+= clausesInc){
            col.add(new Object[]{
              v, c, buckets, (double)q/(double)100
            });
          }
        }
      }
    }
    Collections.shuffle(col);
    for(int i = 0; i < col.size(); i++){
      Object[] o = col.get(i);
      try {
        run((int)o[0], (int)o[1], (int)o[2], (double)o[3]);
      } catch (Exception ex) {
        Logger.getLogger(QAgainstTimeRandomBucketing.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
  public static void main(String[] args){
    QAgainstTimeRandomBucketing q = new QAgainstTimeRandomBucketing();
    if(args.length == 0){
      args = new String[]{
        "100",
        "1000",
        "100",
        "200",
        "5000",
        "200"
      };
    }
    int varsFrom = Integer.parseInt(args[0]);
    int varsTo = Integer.parseInt(args[1]);
    int varsInc = Integer.parseInt(args[2]);
    int clausesFrom = Integer.parseInt(args[3]);
    int clausesTo = Integer.parseInt(args[4]);
    int clausesInc = Integer.parseInt(args[5]);
    int buckets = Integer.parseInt(args[6]);
    q.getRuns(varsFrom, varsTo, varsInc, clausesFrom, clausesTo, clausesInc, buckets);
  }
}
