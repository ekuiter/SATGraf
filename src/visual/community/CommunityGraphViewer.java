/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.community;

import static visual.graph.DrawableNode.COMMUNITY_COLORS;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import visual.community.drawing_algorithms.AbstractPlacer;
import visual.graph.Edge;
import visual.graph.GraphViewer;

/**
 *
 * @author zacknewsham
 */
public class CommunityGraphViewer extends GraphViewer<CommunityNode, CommunityEdge>{

  private AbstractPlacer placer;
  
  public CommunityGraphViewer(CommunityGraph graph, HashMap<String, TIntObjectHashMap<String>> node_lists, AbstractPlacer pl) {
    super(graph, node_lists);
    placer = pl;
    init();
  }
  public CommunityGraph getGraph(){
    return (CommunityGraph) graph;
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
    if(n.getCommunity() == -1){
      return Color.RED;
    }
    else{
      return Color.RED;
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
      if(!visibleConnections.contains(conn)){
        visibleConnections.add(conn);
        updateObservers();
      }
    }
    else if((conn.getType() & CommunityEdge.INTER_COMMUNITY) == CommunityEdge.INTER_COMMUNITY && (conn.getType() & Edge.DUMMY) == Edge.DUMMY){
      Iterator<CommunityEdge> conns = graph.getEdges();
      while(conns.hasNext()){
        CommunityEdge conn1 = conns.next();
        if(conn1.getStart().getCommunity() != conn1.getEnd().getCommunity()){
          visibleConnections.add(conn1);
        }
      }
      updateObservers();
    }
    else if((conn.getType() & CommunityEdge.INTER_COMMUNITY) == CommunityEdge.INTER_COMMUNITY){
      Iterator<CommunityEdge> conns = getInterCommunityConnections(conn.getCommunity()).iterator();
      while(conns.hasNext()){
        CommunityEdge conn1 = conns.next();
        visibleConnections.add(conn1);
      }
      updateObservers();
    }
    else if((conn.getType() & CommunityEdge.INTRA_COMMUNITY) == CommunityEdge.INTRA_COMMUNITY && (conn.getType() & Edge.DUMMY) == Edge.DUMMY){
      Iterator<CommunityEdge> conns = graph.getEdges();
      while(conns.hasNext()){
        CommunityEdge conn1 = conns.next();
        if(conn1.getStart().getCommunity() == conn1.getEnd().getCommunity()){
          visibleConnections.add(conn1);
        }
      }
      updateObservers();
    }
    else if((conn.getType() & CommunityEdge.INTRA_COMMUNITY) == CommunityEdge.INTRA_COMMUNITY){
      Iterator<CommunityEdge> conns = getIntraCommunityConnections(conn.getCommunity()).iterator();
      while(conns.hasNext()){
        CommunityEdge conn1 = conns.next();
        visibleConnections.add(conn1);
      }
    }
    updateObservers();
  }
  public void hideEdge(CommunityEdge conn){
    if((conn.getType() & Edge.REAL) == Edge.REAL){
      if(visibleConnections.contains(conn)){
        visibleConnections.remove(conn);
        updateObservers();
      }
    }
    else if((conn.getType() & CommunityEdge.INTER_COMMUNITY) == CommunityEdge.INTER_COMMUNITY && (conn.getType() & Edge.DUMMY) == Edge.DUMMY){
      Iterator<CommunityEdge> conns = graph.getEdges();
      while(conns.hasNext()){
        Edge<CommunityNode> conn1 = conns.next();
        if(conn1.getStart().getCommunity() != conn1.getEnd().getCommunity()){
          visibleConnections.remove(conn1);
        }
      }
      updateObservers();
    }
    else if((conn.getType() & CommunityEdge.INTER_COMMUNITY) == CommunityEdge.INTER_COMMUNITY){
      Iterator<CommunityEdge> conns = getInterCommunityConnections(conn.getCommunity()).iterator();
      while(conns.hasNext()){
        Edge<CommunityNode> conn1 = conns.next();
        visibleConnections.remove(conn1);
      }
      updateObservers();
    }
    else if((conn.getType() & CommunityEdge.INTRA_COMMUNITY) == CommunityEdge.INTRA_COMMUNITY && (conn.getType() & Edge.DUMMY) == Edge.DUMMY){
      Iterator<CommunityEdge> conns = graph.getEdges();
      while(conns.hasNext()){
        Edge<CommunityNode> conn1 = conns.next();
        if(conn1.getStart().getCommunity() == conn1.getEnd().getCommunity()){
          visibleConnections.remove(conn1);
        }
      }
      updateObservers();
    }
    else if((conn.getType() & CommunityEdge.INTRA_COMMUNITY) == CommunityEdge.INTRA_COMMUNITY){
      Iterator<CommunityEdge> conns = getIntraCommunityConnections(conn.getCommunity()).iterator();
      while(conns.hasNext()){
        Edge conn1 = conns.next();
        visibleConnections.remove(conn1);
      }
      updateObservers();
    }
    updateObservers();
  }
  
  public void showCommunity(int community){
    Iterator<CommunityNode> nodes = getCommunityNodes(community).iterator();
    while(nodes.hasNext()){
      showNode(nodes.next());
    }
    updateObservers();
  }
  public void hideCommunity(int community){
    Iterator<CommunityNode> nodes = getCommunityNodes(community).iterator();
    while(nodes.hasNext()){
      hideNode(nodes.next());
    }
    updateObservers();

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
