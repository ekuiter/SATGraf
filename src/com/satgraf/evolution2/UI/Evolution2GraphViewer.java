package com.satgraf.evolution2.UI;

import com.satgraf.community.UI.CommunityGraphViewer;
import com.satlib.community.placer.CommunityPlacer;
import com.satlib.evolution.EvolutionGraph;
import com.satlib.evolution.EvolutionGraph;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.HashMap;

public class Evolution2GraphViewer extends CommunityGraphViewer {

	public Evolution2GraphViewer(EvolutionGraph graph, HashMap<String, TIntObjectHashMap<String>> node_lists, CommunityPlacer pl) {
		super(graph, node_lists, pl);
	}
    
    public EvolutionGraph getGraph(){
      return (EvolutionGraph) graph;
    }
}