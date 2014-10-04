/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.community;

import static visual.graph.DrawableNode.COMMUNITY_COLORS;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.json.simple.JSONObject;

import visual.community.drawing_algorithms.CommunityPlacer;
import visual.graph.Edge;
import visual.graph.Edge.EdgeState;
import visual.graph.GraphViewer;
import visual.graph.Node.NodeAssignmentState;

/**
 *
 * @author zacknewsham
 */
public class CommunityGraphViewer extends GraphViewer<CommunityNode, CommunityEdge>{

  public CommunityPlacer placer;
  
  public CommunityGraphViewer(CommunityGraph graph, HashMap<String, TIntObjectHashMap<String>> node_lists, CommunityPlacer pl) {
    super(graph, node_lists);
    placer = pl;
    init();
  }
  
  public CommunityGraph getGraph(){
    return (CommunityGraph) graph;
  }
  
  public void fromJson(JSONObject json){
    panel.fromJson((JSONObject)json.get("options"));
    if(json.containsKey("selectedNode")){
      this.selectNode(getGraph().getNode(((Long)json.get("selectedNode")).intValue()));
    }
    setScale((Double)json.get("scale"));
  }
  
  public String toJson(){
    StringBuilder json = new StringBuilder();
    ArrayList<String> nodes = new ArrayList<>();
    ArrayList<String> edges = new ArrayList<>();
    json.append("{\"nodes\":[");
    for(CommunityNode node : getGraph().getNodes()){
      StringBuilder nsb = new StringBuilder(node.toJson());
      nsb.setCharAt(nsb.length() - 1, ',');
      nsb.append("\"x\":");
      nsb.append(placer.getX(node));
      nsb.append(",\"y\":");
      nsb.append(placer.getY(node));
      nsb.append("},");
      
      json.append(nsb.toString());
    }
    json.setCharAt(json.length() - 1, ']');
    json.append(",\"edges\":[");
    for(CommunityEdge edge : getGraph().getEdgesList()){
      json.append(edge.toJson());
      json.append(",");
    }
    json.setCharAt(json.length() - 1, ']');
    
    if(getSelectedNode() != null){
      json.append(",\"selectedNode\":");
      json.append(getSelectedNode().getId());
    }
    json.append(",\"scale\":").append(getScale());
    json.append(",\"options\":");
    json.append(panel.toJson());
    
    json.append("}");
    return json.toString();
  }
  
  public void init(){
    placer.init();
  }
  public Collection<CommunityNode> getCommunityNodes(int community){
	Community com = getGraph().getCommunity(community);
    return (com == null ? null : (Collection<CommunityNode>)com.getCommunityNodes());
  }
  
  @Override
  protected  Color getFillColor(CommunityNode node){
    return getColor(node);
  }
  protected Color getColor(CommunityNode n){
    if(n.getAssignmentState() == NodeAssignmentState.ASSIGNED_FALSE) {
      return Color.RED;
    } else if (n.getAssignmentState() == NodeAssignmentState.ASSIGNED_TRUE) {
    	return Color.GREEN;
    } else {
      return Color.BLUE;
    }
  }
  protected Color getColor(CommunityEdge e){
    if(e.getStart().getCommunity() == e.getEnd().getCommunity()){
      return COMMUNITY_COLORS[e.getStart().getCommunity() % COMMUNITY_COLORS.length];
    }
    else {
      return Color.BLACK;
    }
  }
  
  public Collection<CommunityEdge> getInterCommunityConnections(int community) {
    if(getGraph().getCommunity(community) == null){
      return null;
    }
    return getGraph().getCommunity(community).getInterCommunityEdges();
  }

  public Collection<CommunityEdge> getIntraCommunityConnections(int community) {
    if(getGraph().getCommunity(community) == null){
      return null;
    }
    return getGraph().getCommunity(community).getIntraCommunityEdges();
  }

  public void showConnection(CommunityEdge conn){
    if((conn.getType() & Edge.REAL) == Edge.REAL){
    	addUpdatedEdge(conn, EdgeState.SHOW, true);
    }
    else if((conn.getType() & CommunityEdge.INTER_COMMUNITY) == CommunityEdge.INTER_COMMUNITY && (conn.getType() & Edge.DUMMY) == Edge.DUMMY){
      Iterator<CommunityEdge> conns = graph.getEdges();
      while(conns.hasNext()){
        CommunityEdge conn1 = conns.next();
        if(conn1.getStart().getCommunity() != conn1.getEnd().getCommunity()){
        	addUpdatedEdge(conn, EdgeState.SHOW, true);
        }
      }
    }
    else if((conn.getType() & CommunityEdge.INTER_COMMUNITY) == CommunityEdge.INTER_COMMUNITY){
      Iterator<CommunityEdge> conns = getInterCommunityConnections(conn.getCommunity()).iterator();
      while(conns.hasNext()){
        CommunityEdge conn1 = conns.next();
        addUpdatedEdge(conn1, EdgeState.SHOW, true);
      }
    }
    else if((conn.getType() & CommunityEdge.INTRA_COMMUNITY) == CommunityEdge.INTRA_COMMUNITY && (conn.getType() & Edge.DUMMY) == Edge.DUMMY){
      Iterator<CommunityEdge> conns = graph.getEdges();
      while(conns.hasNext()){
        CommunityEdge conn1 = conns.next();
        if(conn1.getStart().getCommunity() == conn1.getEnd().getCommunity()){
        	addUpdatedEdge(conn1, EdgeState.SHOW, true);
        }
      }
    }
    else if((conn.getType() & CommunityEdge.INTRA_COMMUNITY) == CommunityEdge.INTRA_COMMUNITY){
      Iterator<CommunityEdge> conns = getIntraCommunityConnections(conn.getCommunity()).iterator();
      while(conns.hasNext()){
        CommunityEdge conn1 = conns.next();
        addUpdatedEdge(conn1, EdgeState.SHOW, true);
      }
    }
  }
  
  public void hideEdge(CommunityEdge conn){
    if((conn.getType() & Edge.REAL) == Edge.REAL){
    	addUpdatedEdge(conn, EdgeState.HIDE, true);
    }
    else if((conn.getType() & CommunityEdge.INTER_COMMUNITY) == CommunityEdge.INTER_COMMUNITY && (conn.getType() & Edge.DUMMY) == Edge.DUMMY){
      Iterator<CommunityEdge> conns = graph.getEdges();
      while(conns.hasNext()){
        Edge<CommunityNode> conn1 = conns.next();
        if(conn1.getStart().getCommunity() != conn1.getEnd().getCommunity()){
        	addUpdatedEdge(conn, EdgeState.HIDE, true);
        }
      }
    }
    else if((conn.getType() & CommunityEdge.INTER_COMMUNITY) == CommunityEdge.INTER_COMMUNITY){
      Iterator<CommunityEdge> conns = getInterCommunityConnections(conn.getCommunity()).iterator();
      while(conns.hasNext()){
        Edge<CommunityNode> conn1 = conns.next();
        addUpdatedEdge(conn1, EdgeState.HIDE, true);
      }
    }
    else if((conn.getType() & CommunityEdge.INTRA_COMMUNITY) == CommunityEdge.INTRA_COMMUNITY && (conn.getType() & Edge.DUMMY) == Edge.DUMMY){
      Iterator<CommunityEdge> conns = graph.getEdges();
      while(conns.hasNext()){
        Edge<CommunityNode> conn1 = conns.next();
        if(conn1.getStart().getCommunity() == conn1.getEnd().getCommunity()){
        	addUpdatedEdge(conn1, EdgeState.HIDE, true);
        }
      }
    }
    else if((conn.getType() & CommunityEdge.INTRA_COMMUNITY) == CommunityEdge.INTRA_COMMUNITY){
      Iterator<CommunityEdge> conns = getIntraCommunityConnections(conn.getCommunity()).iterator();
      while(conns.hasNext()){
        Edge conn1 = conns.next();
        addUpdatedEdge(conn1, EdgeState.HIDE, true);
      }
    }
  }
  
  public void showCommunity(int community){
    Iterator<CommunityNode> nodes = getCommunityNodes(community).iterator();
    while(nodes.hasNext()){
      showNode(nodes.next());
    }
  }
  public void hideCommunity(int community){
    Iterator<CommunityNode> nodes = getCommunityNodes(community).iterator();
    while(nodes.hasNext()){
      hideNode(nodes.next());
    }
  }
  
	@Override
	public CommunityNode getNodeAtXY(int x, int y) {
		return placer.getNodeAtXY(x, y, getScale());
	}

	@Override
	protected int getX(CommunityNode node) {
		return placer.getX(node);
	}
	
	@Override
	protected int getY(CommunityNode node) {
		return placer.getY(node);
	}
}
