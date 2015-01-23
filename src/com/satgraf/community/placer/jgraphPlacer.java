package com.satgraf.community.placer;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;

import com.mxgraph.view.mxGraph;
import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityNode;
import com.satlib.community.placer.AbstractPlacer;
import com.satlib.graph.DrawableNode;

public class jgraphPlacer extends AbstractPlacer {
	
	private mxGraph mxgraph = new mxGraph();
	private HashMap<CommunityNode, Object> nodeMap = new HashMap<CommunityNode, Object>();

	public jgraphPlacer(CommunityGraph graph) {
		super(graph);
	}

	@Override
	public CommunityNode getNodeAtXY(int x, int y, double scale) {
		x /= scale;
		y /= scale;
		Iterator<CommunityNode> nodes = graph.getNodes("All");
		Rectangle r = new Rectangle(0, 0, DrawableNode.NODE_DIAMETER, DrawableNode.NODE_DIAMETER);
		while(nodes.hasNext()){
			CommunityNode node = (CommunityNode)nodes.next();
			r.x = getX(node);
			r.y = getY(node);
			if(r.contains(x, y)){
				return node;
			}
		}
		return null;
	}

	@Override
	public void init() {
		Object parent = mxgraph.getDefaultParent();
		
		mxgraph.getModel().beginUpdate();
		try {
			// Add all nodes
			for (CommunityNode n1 : graph.getNodes()) {
				Object n2 = mxgraph.insertVertex(parent, null, "", 0, 0, 1, 1);
				nodeMap.put(n1, n2);
			}
			
			// Add all edges
			for (CommunityEdge e : graph.getEdgesList()) {
				mxgraph.insertEdge(parent, null, "", nodeMap.get(e.getStart()), nodeMap.get(e.getEnd()));
			}
		} finally {
			mxgraph.getModel().endUpdate();
		}
		
		mxFastOrganicLayout layout = new mxFastOrganicLayout(mxgraph);
		layout.execute(parent);
	}

	@Override
	public int getX(CommunityNode node) {
		return (int) mxgraph.getModel().getGeometry(nodeMap.get(node)).getCenterX();
	}

	@Override
	public int getY(CommunityNode node) {
		return (int) mxgraph.getModel().getGeometry(nodeMap.get(node)).getCenterY();
	}

	@Override
	public int getCommunityX(int community) {
		return 0;
	}

	@Override
	public int getCommunityY(int community) {
		return 0;
	}

	@Override
	public int getCommunityWidth(int community) {
		return 0;
	}

	@Override
	public int getCommunityHeight(int community) {
		return 0;
	}

	@Override
	public String getProgressionName() {
		return "Placing Communities";
	}

	@Override
	public double getProgress() {
		// TODO Auto-generated method stub
		return 0;
	}

}
