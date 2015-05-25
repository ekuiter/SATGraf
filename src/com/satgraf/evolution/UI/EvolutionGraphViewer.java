package com.satgraf.evolution.UI;

import com.satgraf.community.UI.CommunityGraphViewer;
import com.satgraf.community.placer.CommunityPlacer;
import com.satlib.evolution.Evolution;
import com.satlib.evolution.EvolutionGraph;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.HashMap;

public class EvolutionGraphViewer extends CommunityGraphViewer {
  Evolution evolution;
  public EvolutionGraphViewer(EvolutionGraph graph, HashMap<String, TIntObjectHashMap<String>> node_lists, CommunityPlacer pl) {
      super(graph, node_lists, pl);
  }

  public EvolutionGraph getGraph(){
    return (EvolutionGraph) graph;
  }

  public Evolution getEvolution(){
    return evolution;
  }

  public void setEvolution(Evolution evolution){
    this.evolution = evolution;
  }
}
