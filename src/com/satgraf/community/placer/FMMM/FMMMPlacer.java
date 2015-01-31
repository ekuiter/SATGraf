package com.satgraf.community.placer.FMMM;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.geometry.Point2D;

import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityNode;
import com.satlib.community.placer.AbstractPlacer;
import com.satlib.graph.DrawableNode;
import com.satlib.graph.Node;

public class FMMMPlacer extends AbstractPlacer {
	
	HashMap<Node, NodeAttributes> attributes = new HashMap<Node, NodeAttributes>();

	public FMMMPlacer(CommunityGraph graph) {
		super(graph);
		
		ArrayList<Node> nodes = new ArrayList(this.graph.getNodes());
		for (Node n : nodes) {
			NodeAttributes na = new NodeAttributes();
			na.set_NodeAttributes(DrawableNode.NODE_DIAMETER, DrawableNode.NODE_DIAMETER, new Point2D(0, 0), null, null);
			attributes.put(n, na);
		}
	}

	@Override
	public CommunityNode getNodeAtXY(int x, int y, double scale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init() {
		if (this.graph.getNodeCount() > 1)
		{
			double max_integer_position = Math.pow(2.0, 40);
			call_DIVIDE_ET_IMPERA_step();
		}
	}
	
	private void call_DIVIDE_ET_IMPERA_step() {
		// TODO: Add components later
		//NodeArray<int> component(G); //holds for each node the index of its component
		//number_of_components = connectedComponents(G,component);//calculate components of G
		//Graph* G_sub = new Graph[number_of_components];
		//NodeArray<NodeAttributes>* A_sub = new NodeArray<NodeAttributes>[number_of_components];
		//create_maximum_connected_subGraphs(G,A,E,G_sub,A_sub,E_sub,component);

//		if(number_of_components == 1)
			call_MULTILEVEL_step_for_subGraph();
//		else
//			for(int i = 0; i < number_of_components;i++)
//				call_MULTILEVEL_step_for_subGraph(G_sub[i],A_sub[i],E_sub[i],i);

//		pack_subGraph_drawings (A,G_sub,A_sub);
//		delete_all_subGraphs(G_sub,A_sub,E_sub);
	}
	
	// TODO: Change for components
	private void call_MULTILEVEL_step_for_subGraph() {
		//Multilevel Mult;
	}
	
	@Override
	public int getX(CommunityNode node) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getY(CommunityNode node) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCommunityX(int community) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCommunityY(int community) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCommunityWidth(int community) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCommunityHeight(int community) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getProgressionName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProgress() {
		// TODO Auto-generated method stub
		return 0;
	}

}
