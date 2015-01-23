package com.satgraf.community.placer;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;

import y.base.Node;
import y.layout.organic.SmartOrganicLayouter;
import y.view.Graph2D;

import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityNode;
import com.satlib.community.placer.AbstractPlacer;
import com.satlib.community.placer.CommunityPlacerFactory;
import com.satlib.graph.DrawableNode;

public class yPlacer extends AbstractPlacer {
  static{
    CommunityPlacerFactory.getInstance().register("y", yPlacer.class);
  }
	
	private Graph2D graph2D = new Graph2D();
	private HashMap<CommunityNode, Node> nodeMap = new HashMap<CommunityNode, Node>();

	public yPlacer(CommunityGraph graph) {
		super(graph);
	}
	
	private void buildGraph2D() {
		for (CommunityNode n1 : this.graph.getNodes()) {
			Node n2 = graph2D.createNode();
			nodeMap.put(n1, n2);
		}
		
		for (CommunityEdge e : this.graph.getEdgesList()) {
			graph2D.createEdge(nodeMap.get(e.getStart()), nodeMap.get(e.getEnd()));
		}
		
		SmartOrganicLayouter layouter = new SmartOrganicLayouter();
		layouter.doLayout(graph2D);
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
		buildGraph2D();
	}

	@Override
	public int getX(CommunityNode node) {
		return (int) graph2D.getCenterX(nodeMap.get(node));
	}

	@Override
	public int getY(CommunityNode node) {
		return (int) graph2D.getCenterY(nodeMap.get(node));
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
