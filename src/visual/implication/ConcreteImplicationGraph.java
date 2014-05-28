/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.implication;

import gnu.trove.map.hash.TObjectCharHashMap;
import java.util.HashMap;
import java.util.Iterator;
import visual.graph.AbstractGraph;
import visual.graph.Edge;
import visual.graph.Node;

/**
 *
 * @author zacknewsham
 */
public class ConcreteImplicationGraph extends AbstractGraph<ImplicationNode, Edge, ImplicationClause> implements ImplicationGraph{
  
  @Override
  public Edge createEdge(ImplicationNode a, ImplicationNode b, boolean dummy) {
    synchronized(connections){
      Edge e;
      if((e = a.getEdge(b)) != null){
        return e;
      }
      e = new Edge(a, b);
      connections.add(e);
      return e;
    }
  }

  @Override
  public ImplicationNode createNode(int id, String name, boolean head, boolean tail) {
    synchronized(nodes){
      if(!nodes.contains(id)){
        ImplicationNode n = new ImplicationNode(id, name);
        nodes.put(id, n);
        uf.add(n);
      }
    }
    return nodes.get(id);
  }

  @Override
  public ImplicationClause createClause(TObjectCharHashMap<ImplicationNode> nodes) {
    ImplicationClause c = new ImplicationClause(nodes);
    if(clauses.containsKey(c)){
      return clauses.get(c);
    }
    
    Iterator<ImplicationNode> ns = nodes.keySet().iterator();
    while(ns.hasNext()){
      ImplicationNode n = ns.next();
      n.addClause(c);
    }
    clauses.put(c, c);
    return c;
  }
  
  
  @Override
  public ImplicationGraph to3CNF(){
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
