/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.implication;

import gnu.trove.map.hash.TObjectCharHashMap;
import java.util.HashMap;
import java.util.Iterator;
import visual.graph.Clause;
import visual.graph.Node;

/**
 *
 * @author zacknewsham
 */
public class ImplicationClause extends Clause<ImplicationNode>{

  public ImplicationClause(TObjectCharHashMap<ImplicationNode> literals) {
    super(literals);
  }
  
  public boolean isConflict(){
    Iterator<ImplicationNode> nodes = literals.keySet().iterator();
    while(nodes.hasNext()){
      ImplicationNode n = nodes.next();
      if(n.getValue() != (literals.get(n) == '1')){
        return true;
      }
    }
    return false;
  }
  public boolean isSatisfied(){
    if(isConflict()){
      return false;
    }
    else{
      Iterator<ImplicationNode> nodes = literals.keySet().iterator();
      while(nodes.hasNext()){
        ImplicationNode n = nodes.next();
        if(!n.isSet()){
          return false;
        }
      }
      return true;
    }
  }

  public boolean satisfiedBy(ImplicationNode node, boolean ignoreUnset){
    return (node.isSet() || ignoreUnset) && node.getValue() == (this.literals.get(node) == '1');
  }
  public boolean satisfiedBy(ImplicationNode node) {
    return satisfiedBy(node, false);
  }

  boolean satisfied() {
    Iterator<ImplicationNode> ns = this.getNodes();
    while(ns.hasNext()){
      ImplicationNode n = ns.next();
      if(satisfiedBy(n)){
        return true;
      }
    }
    return false;
  }
  
}
