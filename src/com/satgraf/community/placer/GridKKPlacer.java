/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.placer;

import com.satlib.community.Community;
import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityGraphAdapter;
import com.satlib.community.CommunityNode;
import com.satlib.community.placer.AbstractPlacer;
import com.satlib.graph.DrawableNode;
import com.satlib.graph.UnionFind;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author zacknewsham
 */
public class GridKKPlacer extends AbstractPlacer{
  private TIntObjectHashMap<KKPlacer> kkPlacers = new TIntObjectHashMap<KKPlacer>();
  private KKPlacer communityKKPlacer;
  private HashMap<CommunityNode, Point> nodePositions = new HashMap<CommunityNode, Point>();
  private GridPlacer gridPlacer;
  private int maxCount = 0;
  private FakeCommunityGraph fake;
  public GridKKPlacer(CommunityGraph graph) {
    super(graph);
    gridPlacer = new GridPlacer(graph);
    int dim = 250 + graph.getNodeCount();
    communityKKPlacer = new KKPlacer(new FakeCommunityGraph(graph), dim, dim);
  }
  
    public String getProgressionName(){
      return "Placing Communities";
    }
    public double getProgress(){
      return 0.0;
    }
  public TIntArrayList getCommunitiesAtXY(int x, int y){
    TIntArrayList list = new TIntArrayList();
    Iterator<Community> comms = graph.getCommunities().iterator();
    while(comms.hasNext()){
      Community com = comms.next();
      int community = com.getId();
      int cx = getCommunityX(community);
      int cy = getCommunityY(community);
      int cw = getCommunityWidth(community);
      int ch = getCommunityHeight(community);
      Rectangle r = new Rectangle(cx, cy, cw, ch);
      if(r.contains(x, y)){
        list.add(community);
      }
    }
    return list;
  }
  
  @Override
  public CommunityNode getNodeAtXY(int x, int y, double scale) {
    TIntArrayList list = getCommunitiesAtXY(x, y);
    for(int i = 0; i < list.size(); i++){
      Community com = graph.getCommunity(list.get(i));
      if(com == null){
        continue;
      }
      Iterator<CommunityNode> nodes = com.getNodeIterator();
      Rectangle r = new Rectangle(0, 0, DrawableNode.NODE_DIAMETER, DrawableNode.NODE_DIAMETER);
      while(nodes.hasNext()){
        CommunityNode node = (CommunityNode)nodes.next();
        r.x = getX(node);
        r.y = getY(node);
        if(r.contains(x, y)){
          return node;
        }
      }
    }
    return null;
  }

  @Override
  public void init() {
    Iterator<Community> comms = graph.getCommunities().iterator();
    while(comms.hasNext()){
      Community com = comms.next();
      int newMaxCount = com.getMaxCount();
      if(newMaxCount > maxCount){
        maxCount = newMaxCount;
      }
      KKPlacer kk = new KKPlacer(com, gridPlacer.getCommunityWidth(com.getId()), gridPlacer.getCommunityHeight(com.getId()));
      kk.init();
      kkPlacers.put(com.getId(), kk);
    }
    communityKKPlacer.init();
  }

  @Override
  public int getX(CommunityNode node) {
    Point p = nodePositions.get(node);
    if(p == null){
      p = new Point();
      int com = node.getCommunity();
      p.x = communityKKPlacer.getX(com) + kkPlacers.get(com).getX(node);
      p.y = communityKKPlacer.getY(com) + kkPlacers.get(com).getY(node);
      nodePositions.put(node, p);
    }
    return p.x;
  }

  @Override
  public int getY(CommunityNode node) {
    Point p = nodePositions.get(node);
    if(p == null){
      p = new Point();
      p.x = communityKKPlacer.getX(node.getCommunity()) + kkPlacers.get(node.getCommunity()).getX(node);
      p.y = communityKKPlacer.getY(node.getCommunity()) + kkPlacers.get(node.getCommunity()).getY(node);
      nodePositions.put(node, p);
    }
    return p.y;
  }

  @Override
  public int getCommunityX(int community) {
    return communityKKPlacer.getX(community);
  }

  @Override
  public int getCommunityY(int community) {
    return communityKKPlacer.getY(community);
  }

  @Override
  public int getCommunityWidth(int community) {
    return gridPlacer.getCommunityWidth(community);
  }

  @Override
  public int getCommunityHeight(int community) {
    return gridPlacer.getCommunityHeight(community);
  }
  
  
  class FakeCommunityNode extends CommunityNode{
    private FakeCommunity c;
    private FakeCommunityGraph g;
    public FakeCommunityNode(FakeCommunity c, FakeCommunityGraph g) {
      super(c.getId(), "");
      this.c = c;
      this.g = g;
    }
    
    @Override
    public int getCommunity(){
      return c.getId();
    }
    
    public int getWidth(){
      return gridPlacer.getCommunityWidth(c.c.getId());
    }
    public int getHeight(){
      return gridPlacer.getCommunityHeight(c.c.getId());
    }
  }
  private class FakeCommunity extends Community{
    Community c;
    private HashMap<Community, CommunityEdge> edges = new HashMap<Community, CommunityEdge>();
    private FakeCommunityNode n;
    public FakeCommunity(Community c) {
      super(c.getId());
      this.c = c;
    }
    public void setFakeCommunityNode(FakeCommunityNode n){
      this.n = n;
      this.communityNodes.add(n);
    }
    
    /*void addFakeEdge(FakeCommunityEdge e){
      if(e.getStart().getCommunity() == this){
        if(!edges.containsKey(e.getEnd().getCommunity())){
          edges.put(e.getEnd().getCommunity(), e);
        }
      }
      else{
        if(!edges.containsKey(e.getStart().getCommunity())){
          edges.put(e.getStart().getCommunity(), e);
        }
      }
    }
    
    @Override
    public Collection<CommunityEdge> getInterCommunityEdges(){
      return edges.values();
    }*/
  }
  
  private class FakeCommunityEdge extends CommunityEdge {
    private double weight = 0.0;
    public FakeCommunityEdge(CommunityNode a, CommunityNode b, boolean dummy) {
      super(a, b, dummy);
    }
    
    @Override
    public double getWeight(){
      if(weight == 0.0 && !dummy){
        if (a.getCommunity() == b.getCommunity())
        	weight = 0.4;
        else
        	weight = 0.1;
      }
      if(dummy){
        weight = 1;
      }
      return weight;
    }
  }
  private class FakeCommunityGraph extends CommunityGraphAdapter {
    private TIntObjectHashMap<CommunityNode> nodes = new TIntObjectHashMap<>();
    private HashMap<CommunityEdge,CommunityEdge> edges = new HashMap<>();
    private UnionFind uf = new UnionFind();
    private FakeCommunityGraph(CommunityGraph g){
      Iterator<Community> communities = g.getCommunities().iterator();
      while(communities.hasNext()){
        Community c = communities.next();
        FakeCommunityNode fcn = this.createNode(c);
        uf.add(fcn);
        Iterator<CommunityEdge> edges = new ArrayList<CommunityEdge>(c.getInterCommunityEdges()).iterator();
        while(edges.hasNext()){
          CommunityEdge e = edges.next();
          Community otherC = graph.getCommunity(c.getId() == e.getEnd().getCommunity() ? e.getStart().getCommunity() : e.getEnd().getCommunity());
          FakeCommunityNode fcn2 = this.createNode(otherC);
          uf.add(fcn2);
          FakeCommunityEdge fce = (FakeCommunityEdge)this.createEdge(fcn, fcn2, false);
          
          fcn.addEdge(fce);
        }
      }
    }
    public Iterator<CommunityEdge> getDummyEdges(){
      return new ArrayList<CommunityEdge>().iterator();
    }

    @Override
    public CommunityEdge createEdge(CommunityNode a, CommunityNode b, boolean dummy) {
      FakeCommunityEdge fce = new FakeCommunityEdge(a, b, dummy);
      uf.union(a, b);
      a.addEdge(fce);
      b.addEdge(fce);
      if(a.getCommunity() != b.getCommunity()){
        graph.getCommunity(a.getCommunity()).addInterCommunityEdge(fce);
        graph.getCommunity(b.getCommunity()).addInterCommunityEdge(fce);
      }
      if(edges.containsKey(fce)){
        return edges.get(fce);
      }
      else{
        edges.put(fce, fce);
        return fce;
      }
    }
    @Override
    public CommunityEdge connect(CommunityNode a, CommunityNode b, boolean dummy) {
      CommunityEdge e = createEdge(a, b, dummy);
      union(a, b);
      
      return e;
    }

    @Override
    public void union(CommunityNode a, CommunityNode b) {
      uf.union(a, b);
    }

    @Override
    public boolean connected(CommunityNode a, CommunityNode b) {
      return uf.find(a) == uf.find(b);
    }

    @Override
    public CommunityNode getNode(int id) {
      return nodes.get(id);
    }

    private FakeCommunityNode createNode(Community c){
      FakeCommunity fc = new FakeCommunity(c);
      FakeCommunityNode fcn = new FakeCommunityNode(fc, this);
      fcn.setSize(gridPlacer.getCommunityWidth(c.getId()));
      fc.setFakeCommunityNode(fcn);
      if(!nodes.containsValue(fcn)){
        nodes.put(c.getId(), fcn);
      }
      fcn = (FakeCommunityNode)nodes.get(c.getId());
      return fcn;
    }

    @Override
    public Collection<CommunityNode> getNodesList() {
      return nodes.valueCollection();
    }

    @Override
    public Iterator<CommunityNode> getNodes(String set) {
      return nodes.valueCollection().iterator();
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
    public Iterator<CommunityEdge> getEdges() {
      return edges.values().iterator();
    }

    @Override
    public Collection<CommunityEdge> getEdgesList() {
      return edges.values();
    }
  }
}
