/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.evolution.UI;

import com.satgraf.graph.color.NodeColoring;
import com.satgraf.graph.color.NodeColoringFactory;
import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityMetric;
import com.satlib.community.CommunityNode;
import com.satlib.evolution.observers.EvolutionObserver;
import com.satlib.evolution.observers.EvolutionObserverFactory;
import com.satlib.graph.Graph;
import com.satlib.graph.Node;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author zacknewsham
 */
public class EvolutionAssignmentTemperatureColoring implements NodeColoring<CommunityNode>, EvolutionObserver{
  static{
    NodeColoringFactory.getInstance().register("assignmentTemp", "Color the nodes between green and red based on number of times they have been assigned", EvolutionDecisionTemperatureColoring.class);
  }
  private Graph graph;
  private Map<Node, Integer> assignments = new HashMap<>();
  
  public EvolutionAssignmentTemperatureColoring(Graph g){
    graph = g;
    EvolutionObserverFactory.getInstance().addObserver(this);
  }
  
  @Override
  public Color getOutlineColor(CommunityNode node) {
    int n = 0;
    if(assignments.containsKey(node)){
      n = assignments.get(node);
      n = Math.min(n, 255);
    }
            
    int R = n;
    int G = (255 -n);
    int B = 0;
    
    return new Color(R, G, B);
  }

  @Override
  public Color getFillColor(CommunityNode node) {
    return getOutlineColor(node);
  }

  @Override
  public void addEdge(CommunityEdge e) {
  }

  @Override
  public void removeEdge(CommunityEdge e) {
  }

  @Override
  public void nodeAssigned(CommunityNode n, Node.NodeAssignmentState state, boolean isDecision) {
    if(state != Node.NodeAssignmentState.UNASSIGNED){
      if(!assignments.containsKey(n)){
        assignments.put(n, 1);
      }
      else{
        assignments.put(n, assignments.get(n) + 1);
      }
    }
  }

  @Override
  public void newFileReady() {
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public void setCommunityMetric(CommunityMetric metric) {
  }

  @Override
  public void updateGraph() {
  }
  
}
