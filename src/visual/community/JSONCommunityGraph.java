/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.community;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectCharHashMap;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import visual.community.drawing_algorithms.CommunityPlacer;
import visual.graph.AbstractGraph;
import visual.graph.Clause;
import visual.graph.DrawableNode;
import visual.graph.Edge;
import visual.graph.Node;

/**
 *
 * @author zacknewsham
 */
public class JSONCommunityGraph extends AbstractGraph<CommunityNode, CommunityEdge, Clause> implements CommunityGraph, CommunityPlacer{
  private HashMap<Integer, Point> nodePositions = new HashMap<>();
  private HashMap<Point, CommunityNode> nodesByPosition = new HashMap<>();
  private HashMap<String, TIntObjectHashMap<String>> node_lists = new HashMap<>();
  private HashMap<Integer, Community> communities = new HashMap<>();
  private JSONObject tmp;
  private TIntIntHashMap varDist = new TIntIntHashMap();
  public JSONCommunityGraph(JSONObject json){
    tmp = json;
  }
  public void init(){
    if(tmp == null){
      return;
    }
    JSONArray jNodes = (JSONArray)tmp.get("nodes");
    for(Object n : jNodes){
      JSONObject jNode = (JSONObject)n;
      CommunityNode node = CommunityNode.fromJson(jNode, this);
      Point p = new Point(((Long)jNode.get("x")).intValue(),((Long)jNode.get("y")).intValue());
      nodesByPosition.put(p, node);
      nodePositions.put(node.getId(), p);
      if(communities.get(node.getCommunity()) == null){
        Community com = new Community(node.getCommunity());
        communities.put(node.getCommunity(), com);
      }
      communities.get(node.getCommunity()).addCommunityNode(node);
      Iterator<String> groups = node.getGroups();
      while(groups.hasNext()){
        String group = groups.next();
        if(node_lists.get(group) == null){
          node_lists.put(group, new TIntObjectHashMap<String>());
          nodes_set.put(group, new ArrayList<CommunityNode>());
        }
        node_lists.get(group).put(node.getId(), node.getName());
        nodes_set.get(group).add(node);
      }
      nodes.put(node.getId(), node);
    }
    JSONArray jEdges = (JSONArray)tmp.get("edges");
    for(Object e : jEdges){
      JSONObject jEdge = (JSONObject)e;
      CommunityEdge edge = CommunityEdge.fromJson(jEdge, this);
      if(edge.getStart().getCommunity() == edge.getEnd().getCommunity()){
        communities.get(edge.getStart().getCommunity()).addIntraCommunityEdge(edge);
      }
      else{
        communities.get(edge.getStart().getCommunity()).addInterCommunityEdge(edge);
        communities.get(edge.getEnd().getCommunity()).addInterCommunityEdge(edge);
      }
      edge.getStart().addEdge(edge);
      edge.getEnd().addEdge(edge);
      connections.add(edge);
    }
    tmp = null;
  }
  
  public HashMap<String, TIntObjectHashMap<String>> getNodeLists(){
    return node_lists;
  }
  
  @Override
  public Community getCommunity(int community) {
    return communities.get(community);
  }

  @Override
  public int getCommunitySize(int community) {
    return communities.get(community).getNodeCount();
  }

  @Override
  public Collection<Community> getCommunities() {
    return communities.values();
  }

  @Override
  public int getNumberCommunities() {
    return communities.size();
  }

  @Override
  public Community createNewCommunity(int communityId) {
    Community c = new Community(communityId);
    communities.put(communityId, c);
    return c;
  }

  @Override
  public int getLargestCommunity() {
    int size = 0;
    int largest = -1;
    for(Community c : communities.values()){
      if(size < c.getSize()){
        size = c.getSize();
        largest = c.getId();
      }
    }
    return largest;
  }

  @Override
  public Iterator<CommunityEdge> getDummyEdges() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void removeEdge(CommunityEdge e) {
    connections.remove(e);
    e.getStart().removeEdge(e);
    e.getEnd().removeEdge(e);
  }

  @Override
  public void setVariableDistribution(TIntIntHashMap dist) {
    this.varDist = dist;
  }

  @Override
  public TIntIntHashMap getVariableDistribution() {
    return varDist;
  }

  @Override
  public CommunityGraph to3CNF() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public CommunityNode getNode(int id) {
    return nodes.get(id);
  }

  @Override
  public void removeNode(Node n) {
    Point p = nodePositions.get(n.getId());
    nodePositions.remove(n.getId());
    nodesByPosition.remove(p);
    nodes.remove(n.getId());
    for(CommunityEdge e : ((CommunityNode)n).getEdgesList()){
      removeEdge(e);
    }
  }

  @Override
  public CommunityNode createNode(int id, String name) {
    CommunityNode n = new CommunityNode(id, name);
    nodes.put(n.getId(), n);
    return n;
  }

  @Override
  public CommunityNode createNode(int id, String name, boolean head, boolean tail) {
    CommunityNode n = new CommunityNode(id, name);
    nodes.put(n.getId(), n);
    return n;
  }

  @Override
  public Collection<CommunityNode> getNodesList() {
    return nodes.valueCollection();
  }

  @Override
  public Iterator<CommunityNode> getNodes(String set) {
    return nodes_set.get(set).iterator();
  }

  @Override
  public Iterator<CommunityNode> getNodeIterator() {
    return nodes.valueCollection().iterator();
  }

  @Override
  public Collection<CommunityNode> getNodes() {
    return nodes.valueCollection();
  }

  @Override
  public int getNodeCount() {
    return nodes.size();
  }

  @Override
  public Iterator<Clause> getClauses() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public int getClausesCount() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Iterator<CommunityEdge> getEdges() {
    return connections.iterator();
  }

  @Override
  public Collection<CommunityEdge> getEdgesList() {
    return connections;
  }

  @Override
  public void writeDimacs(File dimacsFile) throws IOException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Clause createClause(TObjectCharHashMap nodes) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public CommunityEdge createEdge(CommunityNode a, CommunityNode b, boolean dummy) {
    CommunityEdge e = new CommunityEdge(a, b, dummy);
    connections.add(e);
    a.addEdge(e);
    b.addEdge(e);
    return e;
  }

  @Override
  public CommunityNode getNodeAtXY(int x, int y, double scale) {
    x /= scale;
    y /= scale;
    for(int i = x - DrawableNode.NODE_DIAMETER; i < x + DrawableNode.NODE_DIAMETER; i++){
      for(int a = y - DrawableNode.NODE_DIAMETER; a < y + DrawableNode.NODE_DIAMETER; a++){
        Point p = new Point(i, a);
        CommunityNode n = nodesByPosition.get(p);
        if(n != null){
          return n;
        }
      }
    }
    return null;
  }

  @Override
  public int getX(CommunityNode node) {
    return nodePositions.get(node.getId()).x;
  }

  @Override
  public int getY(CommunityNode node) {
    return nodePositions.get(node.getId()).y;
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
  
}
