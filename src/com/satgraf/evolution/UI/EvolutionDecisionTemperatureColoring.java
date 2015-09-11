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
public class EvolutionDecisionTemperatureColoring implements NodeColoring<CommunityNode>, EvolutionObserver{
  static{
    NodeColoringFactory.getInstance().register("decisionTemp", "Color the nodes between green and red based on number of decisions", EvolutionDecisionTemperatureColoring.class);
  }
  private Graph graph;
  private int maxDecisions = 1;
  private Map<Node, Integer> decisions = new HashMap<>();
  
  public EvolutionDecisionTemperatureColoring(Graph g){
    graph = g;
    EvolutionObserverFactory.getInstance().addObserver(this);
  }
  
  @Override
  public Color getOutlineColor(CommunityNode node) {
    int n = 0;
    if(decisions.containsKey(node)){
      n = decisions.get(node);
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
    if(isDecision){
      if(!decisions.containsKey(n)){
        decisions.put(n, 1);
      }
      else{
        decisions.put(n, decisions.get(n) + 1);
      }
      maxDecisions=(int)Math.max(maxDecisions, decisions.get(n));
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
