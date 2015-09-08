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

/**
 *
 * @author zacknewsham
 */

public class QAgainstTimeRandomBucketingVarLength extends com.satgraf.test.QAgainstTime{
  private String outdir;
  public void run(int vars, int clauses, int minLength, int maxLength, int rep, double avgLength) throws Exception{
    CommunityGraph c = InstanceGenerator.makeCommunity(vars, clauses, minLength, maxLength, avgLength);
    f = new File(String.format(outdir + "/%d.%d.%d.%d.%f.%d.cnf", vars, clauses,minLength, maxLength, (double)Math.round(avgLength * 100) / 100, rep));
    if(f.exists()){
      return;
    }
    c.writeDimacs(f);
    timeout = 60 * 15 * 1000;
    super.run(c, 0.0);
  }
  
  public void getRuns(int varsFrom, int varsTo, int varsInc, int clausesFrom, int clausesTo, int clausesInc, int minClause, int maxClause, double lengthInc){
    ArrayList<Object[]> col =new ArrayList<>();
    for(int i = 0; i < 3; i++){
      for(int v = varsFrom; v < varsTo; v+= varsInc){
        for(int c = clausesFrom; c < clausesTo; c+= clausesInc){
          for(double l = minClause; l < maxClause; l += lengthInc){
            col.add(new Object[]{
              v, c, i, l
            });
          }
        }
      }
    }
    Collections.shuffle(col);
    for(int i = 0; i < col.size(); i++){
      Object[] o = col.get(i);
      try {
        run((int)o[0], (int)o[1], minClause, maxClause, (int)o[2], (double)o[3]);
      } catch (Exception ex) {
        Logger.getLogger(QAgainstTimeRandomBucketingVarLength.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
  public static void main(String[] args){
    QAgainstTimeRandomBucketingVarLength q = new QAgainstTimeRandomBucketingVarLength();
    if(args.length == 0){
      args = new String[]{
        "/media/zacknewsham/SAT/bucketingVarLength",
        "100",//min vars
        "10000",//max vars
        "200",//inc vars
        "1000",//min clauses
        "50000",//max clauses
        "2000",//inc clauses
        "2",//min clause length
        "4",//max clause length
        "0.1"//clause length inc
      };
    }
    q.outdir = args[0];
    int varsFrom = Integer.parseInt(args[1]);
    int varsTo = Integer.parseInt(args[2]);
    int varsInc = Integer.parseInt(args[3]);
    int clausesFrom = Integer.parseInt(args[4]);
    int clausesTo = Integer.parseInt(args[5]);
    int clausesInc = Integer.parseInt(args[6]);
    int minClauseLength = Integer.parseInt(args[7]);
    int maxClauseLength = Integer.parseInt(args[8]);
    double clauseLengthInc = Double.parseDouble(args[9]);
    q.getRuns(varsFrom, varsTo, varsInc, clausesFrom, clausesTo, clausesInc, minClauseLength, maxClauseLength, clauseLengthInc);
  }
}
