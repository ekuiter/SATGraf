/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.evolution.UI;

import com.satgraf.community.UI.CommunityColoring;
import com.satgraf.graph.color.EdgeColoringFactory;
import com.satgraf.graph.color.NodeColoringFactory;
import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityNode;
import com.satlib.graph.Graph;
import com.satlib.graph.Node;
import java.awt.Color;

/**
 *
 * @author zacknewsham
 */
public class EvolutionColoring extends CommunityColoring{
  static{
    NodeColoringFactory.getInstance().register("auto", "Nodes are blue", EvolutionColoring.class);
    EdgeColoringFactory.getInstance().register("auto","Edges are coloured according to their community (or white for intercommunity and red for conflict)", EvolutionColoring.class);
  }
  private static Color CONFLICT_COLOR = Color.RED;
  public EvolutionColoring(Graph g){
    super(g);
  }
  
  @Override
  public Color getColor(CommunityEdge e){
    if(e.isConflictEdge()){
      return CONFLICT_COLOR;
    }
    else{
      return super.getColor(e);
    }
  }
  
  @Override
  public Color getFillColor(CommunityNode n){
    if(!n.isAssigned()){
      return super.getFillColor(n);
    }
    else{
      return n.getAssignmentState() == Node.NodeAssignmentState.ASSIGNED_FALSE ? Color.RED : Color.GREEN;
    }
  }
}
