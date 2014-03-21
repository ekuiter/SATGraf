/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.graph;

import gnu.trove.map.hash.TObjectCharHashMap;
import java.util.HashMap;

/**
 *
 * @author zacknewsham
 */
public class SimpleGraph extends AbstractGraph<Node, Edge, Clause>{

  @Override
  public Edge createEdge(Node a, Node b, boolean dummy) {
    Edge e;
    if((e = a.getEdge(b)) != null){
      return e;
    }
    e = new Edge(a, b);
    connections.add(e);
    return e;
  }

  @Override
  public Node createNode(int id, String name, boolean head, boolean tail) {
    if(!nodes.contains(id)){
      Node n = new Node(id, name);
      nodes.put(id, n);
    }
    return nodes.get(id);
  }

  @Override
  public Clause createClause(TObjectCharHashMap<Node> nodes) {
    return new Clause(null);
  }
  
}
