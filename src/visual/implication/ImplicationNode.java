/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.implication;

import java.util.HashSet;
import java.util.Iterator;
import visual.graph.Edge;
import visual.graph.Node;

/**
 *
 * @author zacknewsham
 */
public class ImplicationNode extends Node<Edge>{
  public static enum SET{
    UNSET, IMPLICATION, DECISION, CONSTANT
  }
  private boolean value;
  private SET set = SET.UNSET; //1 = implication, 2 = decision, 3 = constant
  private boolean conflict = false;
  private ImplicationNode setBy;
  private HashSet<ImplicationClause> clauses = new HashSet<ImplicationClause>();
  public ImplicationNode(int id, String name) {
    super(id, name);
  }
  
  public HashSet<ImplicationClause> getClauses(){
    return clauses;
  }
  public void addClause(ImplicationClause c){
    clauses.add(c);
  }
  
  public boolean getValue(){
    return value;
  }
  public void setValue(boolean value, SET decision){
    this.setBy = null;
    if(this.value != value && this.set.ordinal() > decision.ordinal()){
      this.conflict = true;
    }
    else{
      this.value = value;
      this.set = decision;
      resolveClauses();
    }
  }
  private void resolveClauses(){
    boolean changed = true;
    Iterator<ImplicationClause> cs = clauses.iterator();
    while(changed){
      changed = false;
      while(cs.hasNext()){
        ImplicationClause c = cs.next();
        if(c.satisfied()){
          continue;
        }
        Iterator<ImplicationNode> ns = c.getNodes();
        ImplicationNode onlyUnset = null;
        while(ns.hasNext()){
          ImplicationNode n = ns.next();
          if(n.isSet() == false){
            if(onlyUnset == null){
              onlyUnset = n;
            }
            else{
              onlyUnset = null;
              break;
            }
          }
        }
        if(onlyUnset != null){
          if(!c.satisfiedBy(onlyUnset, true)){
            onlyUnset.setValue(!onlyUnset.getValue(), this);
          }
          else{
            onlyUnset.setValue(onlyUnset.getValue(), this);
          }
          changed = true;
        }
      }
    }
  }
  public ImplicationNode setBy(){
    return this.setBy;
  }
  public void unset(){
    this.value = false;
    this.set = SET.UNSET;
  }
  public void setValue(boolean value, ImplicationNode from){
    this.setBy = from;
    this.setValue(value, SET.IMPLICATION);
  }
  public boolean isSet(){
    return set != SET.UNSET;
  }

  boolean isConflict() {
    return conflict;
  }
}
