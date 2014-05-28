/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import gnu.trove.impl.hash.TIntIntHash;
import gnu.trove.map.hash.TIntIntHashMap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import visual.community.CommunityGraph;
import visual.evolution.EvolutionGenerator;

/**
 *
 * @author zacknewsham
 */
public class QAgainstTimeRandomBucketingFixedCVR extends test.QAgainstTime{
    public void run(int vars, int clauses, int buckets, double prob) throws Exception{
    CommunityGraph c = EvolutionGenerator.makeCommunity(vars, clauses, buckets, prob);
    f = new File(String.format("/home/zacknewsham/results/%d.%d.%d.%f.dimacs", vars, clauses, buckets, prob));
    File f1 = new File(String.format("/home/zacknewsham/results/%d.%d.%d.%f.dist", vars, clauses, buckets, prob));
    c.writeDimacs(f);
    timeout = 60 * 15 * 1000;
    super.run(c, 0.0);
    
    TIntIntHashMap map = c.getVariableDistribution();
    BufferedWriter bw = new BufferedWriter(new FileWriter(f1));
    for(int var : map.keys()){
        bw.write(String.format("%d,%d\n", var, map.get(var)));
    }
    bw.close();
  }
  
  public void getRuns(int clausesFrom, int clausesTo, int clausesInc, int buckets){
      
    ArrayList<Object[]> col =new ArrayList<>();
    for(int i = 0; i < 1; i++){
      for(int q = 0; q < 100; q++){
        for(int c = clausesFrom; c < clausesTo; c+= clausesInc){
            int v = (int)(c / 4.25);
            col.add(new Object[]{
                v, c, buckets, (double)q/(double)100
            });
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
    QAgainstTimeRandomBucketingFixedCVR q = new QAgainstTimeRandomBucketingFixedCVR();
    if(args.length == 0){
      args = new String[]{
        "0",
        "0",
        "0",
        "100",
        "1000000",
        "500",
        "50"
      };
    }
    int varsFrom = Integer.parseInt(args[0]);
    int varsTo = Integer.parseInt(args[1]);
    int varsInc = Integer.parseInt(args[2]);
    int clausesFrom = Integer.parseInt(args[3]);
    int clausesTo = Integer.parseInt(args[4]);
    int clausesInc = Integer.parseInt(args[5]);
    int buckets = Integer.parseInt(args[6]);
    q.getRuns(clausesFrom, clausesTo, clausesInc, buckets);
  }
}
