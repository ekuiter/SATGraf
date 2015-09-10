/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.graph.placer.jung;

import com.satlib.graph.Clause;
import com.satlib.graph.Edge;
import com.satlib.graph.Graph;
import com.satlib.graph.Node;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 *
 * @author zacknewsham
 */
public class DirectedGraphWrapper extends edu.uci.ics.jung.graph.DirectedSparseGraph{
  public DirectedGraphWrapper(Graph<Node, Edge, Clause> g){
    for(Node n : g.getNodes()){
      super.addVertex(n);
    }
    for(Edge e : g.getEdges()){
      super.addEdge(e, e.getStart(), e.getEnd(), EdgeType.DIRECTED);
    }
  }
}
