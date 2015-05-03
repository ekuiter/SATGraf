/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.test;
import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityMetric;
import com.satlib.community.CommunityNode;
import com.satlib.community.ConcreteCommunityGraph;
import com.satlib.community.OLCommunityMetric;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectCharHashMap;
import java.util.HashSet;

/**
 *
 * @author zacknewsham
 */
public class InstanceGenerator {

  /**
   * Creates new form EvolutionGenerator
   */
  public InstanceGenerator(){
  }

  private static int getLitBetween(int low, int high){
    int ret = getRandomBetween(low, high);
    
    boolean pos = Math.random() >= 0.5;
    return pos ? ret : -ret;
  }
  private static int getRandomBetween(int low, int high){
    return low + (int)Math.round(Math.random() * (high - low));
  }
  
  private static int getLitOutside(int low, int high, int max){
    boolean first = Math.random() >= 0.5;
    if(first && low > 0){
      return getLitBetween(0, low);
    }
    else{
      return getLitBetween(high, max);
    }
  }
  private static int[] makeClause(int vars_count, int clauseLength){
    int[] ret = new int[clauseLength];
    HashSet<Integer> in = new HashSet<>();
    for(int i = 0; i < clauseLength; i++){
      int var = getRandomBetween(1, vars_count);
      while(in.contains(var) || in.contains(0-var)){
        var = getRandomBetween(1, vars_count);
      }
      int pos = (int)Math.round(Math.random());
      ret[i] = pos == 0 ? 0 - var : var;
      
      in.add(var);
    }
    return ret;
  }
  private static int[] makeClause(int vars_count, int coms_count, double q){
    return makeClause(vars_count, coms_count, q, 3);
  }
  
  private static int[] makeClause(int vars_count, int coms_count, double q, int clauseLength){
    int cmty = getRandomBetween(1, coms_count);
    int cmtySize = (int)Math.round((double)vars_count / (double)coms_count);
    int[] ret = new int[clauseLength];
    
    for(int i = 0; i < clauseLength; i++){
      int pos = (int)Math.round(Math.random());
      if(i == 0){
        int a = 0;
        while(a == 0 || a > vars_count){
          a = getLitBetween(1, cmtySize);
          a = a < 0 ? 0 - (Math.abs(a) * coms_count + cmty) : Math.abs(a) * coms_count + cmty;
        }
        ret[0] = pos == 0 ? 0 - a : a;
      }
      else{
        int b = 0;
        while(b == 0 || b > vars_count){
          double r1 = Math.random();
          b = r1 < q ? getLitBetween(0, cmtySize) * coms_count + cmty : getLitBetween(1, vars_count);
        }
        ret[i] = pos == 0 ? 0 - b : b;
      }
    }
    return ret;
  }
  

  public static CommunityGraph makeCommunity(int vars_count, int clauses_count, int clauseLength) {
    CommunityGraph cg = new ConcreteCommunityGraph();
    TIntIntHashMap varDist = new TIntIntHashMap();
    cg.setVariableDistribution(varDist);
    while(cg.getClausesCount() < clauses_count){
      TObjectCharHashMap<CommunityNode> nodes = new TObjectCharHashMap<CommunityNode>();
      int[] clause = makeClause(vars_count, clauseLength);
      for(int i = 0; i < clauseLength; i++){
        boolean t = clause[i] > 0;
        if(!t){
          clause[i] = 0 - clause[i];
        }
        varDist.put(clause[i], varDist.get(clause[i]) + 1);
        
        CommunityNode a = cg.createNode(clause[i], null);
        nodes.put(a, t ? '1' : '0');
      }
      cg.createClause(nodes);
      for(CommunityNode n1 : nodes.keySet()){
        for(CommunityNode n2 : nodes.keySet()){
          if(n1 != n2){
            CommunityEdge e = cg.createEdge(n1, n2, false);
            if(!n1.getEdgesList().contains(e)){
              n1.addEdge(e);
            }
            if(!n2.getEdgesList().contains(e)){
              n2.addEdge(e);
            }
          }
        }
      }
    }
    CommunityMetric cm = new OLCommunityMetric();
    
    double mod = cm.getCommunities(cg);
    return cg;
  }
  
  private static final double e = 0.001;
  public static CommunityGraph makeCommunity(int vars_count, int clauses_count, int minClause, int maxClause, double avgLength){
    CommunityGraph cg = new ConcreteCommunityGraph();
    TIntIntHashMap varDist = new TIntIntHashMap();
    cg.setVariableDistribution(varDist);
    int totalLength = 0;
    while(cg.getClausesCount() < clauses_count){
      TObjectCharHashMap<CommunityNode> nodes = new TObjectCharHashMap<CommunityNode>();
      int clauseLength = 0;
      
      //we are right on the wanted average clause length, so generate random clause within full range
      if(totalLength == 0 || Math.abs((double)totalLength / (double) cg.getClausesCount() - avgLength) < 0.001){
        clauseLength = getRandomBetween(minClause, maxClause);
      }
      else if((double)totalLength / (double) cg.getClausesCount() - avgLength > 0){
        clauseLength = getRandomBetween(minClause, (int)Math.floor(avgLength));
      }
      else{
        clauseLength = getRandomBetween((int)Math.ceil(avgLength), maxClause);
      }
      int[] clause = makeClause(vars_count, clauseLength);
      totalLength += clause.length;
      for(int i = 0; i < clauseLength; i++){
        boolean t = clause[i] > 0;
        if(!t){
          clause[i] = 0 - clause[i];
        }
        varDist.put(clause[i], varDist.get(clause[i]) + 1);
        
        CommunityNode a = cg.createNode(clause[i], null);
        nodes.put(a, t ? '1' : '0');
      }
      cg.createClause(nodes);
      for(CommunityNode n1 : nodes.keySet()){
        for(CommunityNode n2 : nodes.keySet()){
          if(n1 != n2){
            CommunityEdge e = cg.createEdge(n1, n2, false);
            if(!n1.getEdgesList().contains(e)){
              n1.addEdge(e);
            }
            if(!n2.getEdgesList().contains(e)){
              n2.addEdge(e);
            }
          }
        }
      }
    }
    CommunityMetric cm = new OLCommunityMetric();
    
    double mod = cm.getCommunities(cg);
    return cg;
  }
  
  public static CommunityGraph makeCommunity(int vars_count, int clauses_count, int coms_count, double q){
    CommunityGraph cg = new ConcreteCommunityGraph();
    TIntIntHashMap varDist = new TIntIntHashMap();
    cg.setVariableDistribution(varDist);
    while(cg.getClausesCount() < clauses_count){
      int[] clause = makeClause(vars_count, coms_count, q);
      boolean _a = clause[0] > 0;
      boolean _b = clause[1] > 0;
      boolean _c = clause[2] > 0;
      if(!_a){
        clause[0] = 0 - clause[0];
      }
      if(!_b){
        clause[1] = 0 - clause[1];
      }
      if(!_c){
        clause[2] = 0 - clause[2];
      }
      varDist.put(clause[0], varDist.get(clause[0]) + 1);
      varDist.put(clause[1], varDist.get(clause[1]) + 1);
      varDist.put(clause[2], varDist.get(clause[2]) + 1);
      TObjectCharHashMap<CommunityNode> nodes = new TObjectCharHashMap<CommunityNode>();
      CommunityNode a = cg.createNode(clause[0], null);
      CommunityNode b = cg.createNode(clause[1], null);
      CommunityNode c = cg.createNode(clause[2], null);
      
      
      nodes.put(a, _a ? '1' : '0');
      nodes.put(b, _b ? '1' : '0');
      nodes.put(c, _c ? '1' : '0');
      cg.createClause(nodes);
      
      CommunityEdge ab = cg.createEdge(a, b, false);
      CommunityEdge ac = cg.createEdge(a, c, false);
      CommunityEdge bc = cg.createEdge(b, c, false);
      
      a.addEdge(ab);
      a.addEdge(ac);
      b.addEdge(ab);
      b.addEdge(bc);
      c.addEdge(ac);
      c.addEdge(bc);
    }
    CommunityMetric cm = new OLCommunityMetric();
    
    double mod = cm.getCommunities(cg);
    return cg;
  }
}