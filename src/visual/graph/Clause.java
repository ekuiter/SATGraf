/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.graph;

import gnu.trove.map.hash.TObjectCharHashMap;
import java.util.Iterator;
import java.util.Objects;

/**
 *
 * @author zacknewsham
 */
public class Clause<T extends Node> {
  protected TObjectCharHashMap<T> literals = new TObjectCharHashMap<T> ();
  public Clause(TObjectCharHashMap<T> literals){
    this.literals = literals;
  }
  
  /*@Override
  public String toString(){
    Iterator<T> ns = literals.keySet().iterator();
    StringBuilder sb = new StringBuilder("(");
    while(ns.hasNext()){
      T n = ns.next();
      sb.append(String.format("%s%s", literals.get(n) == false ? "-" : "", n.getName()));
      if(ns.hasNext()){
        sb.append(", ");
      }
    }
    sb.append(")");
    return sb.toString();
  }*/
  
  public Iterator<T> getNodes(){
    return this.literals.keySet().iterator();
  }
  
  public int size(){
    return literals.size();
  }
  @Override
  public boolean equals(Object o){
    if(!(o instanceof Clause)){
      return false;
    }
    Clause other = (Clause)o;
    if(other.literals.size() != this.literals.size()){
      return false;
    }
    Iterator<T> ns = this.literals.keySet().iterator();
    while(ns.hasNext()){
      T n = ns.next();
      if(!other.literals.containsKey(n)){
        return false;
      }
      if(this.literals.get(n) != other.literals.get(n)){
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 67 * hash + Objects.hashCode(this.literals);
    return hash;
  }
  public boolean isConflict(){
    return false;
  }
  public boolean isSatisfied(){
    return true;
  }
  public boolean satisfiedBy(T node, boolean ignoreUnset){
    return true;
  }
  
  public boolean satisfiedBy(T node){
    return satisfiedBy(node, false);
  }

  public boolean getValue(T n) {
    return literals.get(n) == '1';
  }
  
  public String toString(){
    StringBuilder sb = new StringBuilder();
    Iterator<T> nodes = getNodes();
    while(nodes.hasNext()){
      T node = nodes.next();
      sb.append(literals.get(node) == '1' ? "" : "-");
      sb.append(node.getId());
      sb.append(" ");
    }
    
    return sb.toString();
  }
}
