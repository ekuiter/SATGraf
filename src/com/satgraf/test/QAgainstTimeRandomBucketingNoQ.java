package com.satgraf.test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.satlib.community.CommunityGraph;
import com.satgraf.evolution.UI.EvolutionGenerator;

/**
 *
 * @author zacknewsham
 */

public class QAgainstTimeRandomBucketingNoQ extends com.satgraf.test.QAgainstTime{
  private String outdir;
  public void run(int vars, int clauses, int rep) throws Exception{
    CommunityGraph c = EvolutionGenerator.makeCommunity(vars, clauses, 3);
    f = new File(String.format(outdir + "/%d.%d.%d.cnf", vars, clauses, rep));
    c.writeDimacs(f);
    timeout = 60 * 15 * 1000;
    super.run(c, 0.0);
  }
  
  public void getRuns(int varsFrom, int varsTo, int varsInc, int clausesFrom, int clausesTo, int clausesInc){
    ArrayList<Object[]> col = new ArrayList<>();
    for(int i = 0; i < 3; i++){
      for(int v = varsFrom; v < varsTo; v+= varsInc){
        for(int c = clausesFrom; c < clausesTo; c+= clausesInc){
          col.add(new Object[]{
            v, c, i
          });
        }
      }
    }
    Collections.shuffle(col);
    for(int i = 0; i < col.size(); i++){
      Object[] o = col.get(i);
      try {
        run((int)o[0], (int)o[1], (int)o[2]);
      } catch (Exception ex) {
        Logger.getLogger(QAgainstTimeRandomBucketingNoQ.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
  public static void main(String[] args){
    QAgainstTimeRandomBucketingNoQ q = new QAgainstTimeRandomBucketingNoQ();
    if(args.length == 0){
      args = new String[]{
        "/media/zacknewsham/SAT/bucketingNoQ",
        "100",//min vars
        "10000",//max vars
        "200",//inc vars
        "1000",//min clauses
        "100000",//max clauses
        "2000"//inc clauses
      };
    }
    q.outdir = args[0];
    int varsFrom = Integer.parseInt(args[1]);
    int varsTo = Integer.parseInt(args[2]);
    int varsInc = Integer.parseInt(args[3]);
    int clausesFrom = Integer.parseInt(args[4]);
    int clausesTo = Integer.parseInt(args[5]);
    int clausesInc = Integer.parseInt(args[6]);
    q.getRuns(varsFrom, varsTo, varsInc, clausesFrom, clausesTo, clausesInc);
  }
}
