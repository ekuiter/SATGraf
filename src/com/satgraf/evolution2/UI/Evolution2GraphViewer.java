package com.satgraf.evolution2.UI;

import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityGraphViewer;
import com.satlib.community.CommunityNode;
import com.satlib.community.placer.CommunityPlacer;
import com.satlib.graph.GraphObserver;
import com.satlib.graph.Node;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Evolution2GraphViewer extends CommunityGraphViewer {
	
	private Node decisionVariable = null;
    private List<CommunityNode> decisions = new ArrayList<>();
	private boolean showDecisionVariable = true;
	private int displayDecisionVariableFor = 100;
	private int evolutionSpeed = 10;

	public Evolution2GraphViewer(CommunityGraph graph, HashMap<String, TIntObjectHashMap<String>> node_lists, CommunityPlacer pl) {
		super(graph, node_lists, pl);
	}

	public void setDecisionVariable(Node n) {
	    this.decisionVariable = n;
	}

	public Node getDecisionVariable() {
	    return this.decisionVariable;
	}

	public void clearDecisionVariable() {
	    this.decisionVariable = null;
	}
	
	public void setShowDecisionVariable(boolean showDecisionVariable) {
		this.showDecisionVariable = showDecisionVariable;
	}
	
	public boolean getShowDecisionVariable() {
		return this.showDecisionVariable;
	}
	
	public int getDisplayDecisionVariableFor() {
		return this.displayDecisionVariableFor;
	}
	
	public void setDisplayDecisionVariableFor(int length) {
		this.displayDecisionVariableFor = length;
	}
	
	public void setEvolutionSpeed(int speed) {
		this.evolutionSpeed = speed;
	}
	
	public int getEvolutionSpeed() {
		return this.evolutionSpeed;
	}
    
    public void recordDecisionVariable(CommunityNode n){
      decisions.add(n);
      notifyObservers(GraphObserver.Action.decisionVariable);
    }
    public Collection getDecisionVariables(){
      return decisions;
    }
}
