/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.placer;

import com.satgraf.graph.placer.FruchPlacer;
import com.satgraf.graph.placer.AbstractPlacer;
import com.satgraf.graph.placer.Coordinates;
import com.satlib.community.Community;
import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityNode;
import com.satlib.community.ConcreteCommunityGraph;
import com.satlib.graph.DrawableNode;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 *
 * @author zacknewsham
 */
public class CircularCommunityPlacer extends AbstractPlacer<CommunityNode, CommunityGraph> implements CommunityPlacer{
  static{
    CommunityPlacerFactory.getInstance().register("c", CircularCommunityPlacer.class);
  }
  private double progress = 0.0;
  private static final int NODE_WIDTH = 10;
  private static final int NODE_PADDING = 10;
  private static final int COMMUNITY_PADDING = 100;
  private final HashMap<Community, AbstractPlacer> nodePositions = new HashMap<>();
  private final HashMap<Community, Coordinates> communityPositions = new HashMap<>();
  private final HashMap<Community, Dimension> communitySizes = new HashMap<>();
  Random r = new Random(789543781278l);
  
  public CircularCommunityPlacer(CommunityGraph graph) {
    super(graph);
  }

  @Override
  public CommunityNode getNodeAtXY(int x, int y, double scale) {
    x /= scale;
    y /= scale;
    Community c = null;
    for(Community _c : communityPositions.keySet()){
      Rectangle r = new Rectangle(getCommunityX(_c.getId()), getCommunityY(_c.getId()), getCommunityWidth(_c.getId()), getCommunityHeight(_c.getId()));
        if(r.contains(x, y)){
          c = _c;
          break;
        }
    }
    if(c == null){
      return null;
    }
    Iterator<CommunityNode> nodes = c.getNodeIterator();
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

  private Community c;
  @Override
  public void init() {
    int complete = 0;
    double largest = 0.0;
    double circumference = 0;
    for(Community c : graph.getCommunities()){
      double radius = ((NODE_WIDTH + NODE_PADDING) * graph.getCommunitySize(c.getId())) / 2 / Math.PI;
      double slice = (2 * Math.PI) / c.getCommunityNodes().size();
      double inc = 0;
      Dimension d = new Dimension();
      d.setSize((radius * 2), (radius * 2));
      communitySizes.put(c, d);
      FruchPlacer fr = new FruchPlacer(c, (int)d.getWidth(), (int)d.getHeight());
      fr.init();
      /*for(CommunityNode n : c.getCommunityNodes()){
        double x = radius*Math.cos(inc) + 0;
        double y = radius*Math.sin(inc) + 0;
        nodePositions.put(n, new Coordinates(x, y));
        inc += slice;
      }*/
      complete+=c.getNodeCount();
      progress = (double)complete / (double)graph.getNodeCount();
      nodePositions.put(c, fr);
      double size = d.getWidth();
      circumference += size + COMMUNITY_PADDING;
      if( size > largest){
        largest = size;
      }
    }
    /*FruchPlacer fr = new FruchPlacer(g, 1500, 1500);
    //fr.optDist = largest;
    fr.init();
    for(CommunityNode n : g.getNodes()){
      communityPositions.put(graph.getCommunity(n.getId()), new Coordinates(fr.getX(n), fr.getY(n)));
    }
    if(true)
    return;*/
    double radius = circumference / 2 / Math.PI;
    double inc = 0;
    double degrees = (2 * Math.PI);
    List<Community> communities = new ArrayList<>();
    communities.addAll(graph.getCommunities());
    
    Collections.sort(communities, new Comparator<Community>(){
      @Override
      public int compare(Community t, Community t1) {
        return t1.getInterCommunityEdges().size() - t.getInterCommunityEdges().size();
      }
    });
    
    Community left, right, next;
    left = right = communities.remove(0);
    communityPositions.put(left, new Coordinates(radius*Math.cos(0) + radius + (largest / 2),radius*Math.sin(0) + radius + (largest / 2)));
    double leftInc = 0;
    double rightInc = Math.PI * 2;
    double padding,x,y;
    boolean first = true;
    do{
      c = left;
      //do left first
      Collections.sort(communities, new Comparator<Community>(){
        @Override
        public int compare(Community t, Community t1) {
          return t1.getInterEdgesTo(c) - t.getInterEdgesTo(c);
        }
      });
      
      next = communities.remove(0);
      
      padding = 100;//Math.max(Math.min(COMMUNITY_PADDING, next.getInterEdgesTo(left)), 20);
      leftInc += (degrees / circumference) * ((communitySizes.get(left).getWidth() / 2) + (communitySizes.get(next).getWidth() / 2) + padding);
      x = radius*Math.cos(leftInc) + radius + (largest / 2);
      y = radius*Math.sin(leftInc) + radius + (largest / 2);
      communityPositions.put(next, new Coordinates(x, y));
      left = next;
      //do right
      if(communities.isEmpty()){
        break;
      }
      
      if(first){
        first = false;
      }
      else{
        c = right;
        Collections.sort(communities, new Comparator<Community>(){
          @Override
          public int compare(Community t, Community t1) {
            return t.getInterEdgesTo(c) - t1.getInterEdgesTo(c);
          }
        });
      }
      next = communities.remove(0);
      padding = Math.max(Math.min(COMMUNITY_PADDING, next.getInterEdgesTo(right)), 20);
      rightInc -= (degrees / circumference) * ((communitySizes.get(right).getWidth() / 2) + (communitySizes.get(next).getWidth() / 2) + padding);
      x = radius*Math.cos(rightInc) + radius + (largest / 2);
      y = radius*Math.sin(rightInc) + radius + (largest / 2);
      communityPositions.put(next, new Coordinates(x, y));
      right = next;
    }
    while(!communities.isEmpty());
    
  }
  
  @Override
  public int getX(CommunityNode node) {
    Community c = graph.getCommunity(node.getCommunity());
    return (int)(getCommunityX(node.getCommunity()) + nodePositions.get(c).getX(node));
  }

  @Override
  public int getY(CommunityNode node) {
    Community c = graph.getCommunity(node.getCommunity());
    return (int)(getCommunityY(node.getCommunity()) + nodePositions.get(c).getY(node));
  }

  @Override
  public int getCommunityX(int community) {
    return (int)communityPositions.get(graph.getCommunity(community)).getX();
  }

  @Override
  public int getCommunityY(int community) {
    return (int)communityPositions.get(graph.getCommunity(community)).getY();
  }

  @Override
  public int getCommunityWidth(int community) {
    return (int)communitySizes.get(graph.getCommunity(community)).getWidth();
  }

  @Override
  public int getCommunityHeight(int community) {
    return (int)communitySizes.get(graph.getCommunity(community)).getHeight();
  }

  @Override
  public String getProgressionName() {
    return "Placement";
  }

  @Override
  public double getProgress() {
    return progress;
  }
  
  
  private static class DummyGraph extends ConcreteCommunityGraph{
    private HashMap<CommunityNode, CommunityNode> nodes = new HashMap<>();
    private HashMap<CommunityEdge,CommunityEdge> edges = new HashMap<>();
    CommunityEdge createEdge(DummyNode d1, DummyNode d2){
      DummyEdge d = new DummyEdge(d1, d2);
      if(!edges.containsKey(d)){
      edges.put(d, d);
      }
      edges.get(d).setWeight(edges.get(d).getWeight() + 1);
      return edges.get(d);
    }
    CommunityNode createNode(Community c){
      CommunityNode d = new DummyNode(c);
      if(!nodes.containsKey(d)){
        nodes.put(d, d);
      }
      return nodes.get(d);
    }
    void addEdge(Community c1, Community c2){
      CommunityNode d1 = createNode(c1);
      CommunityNode d2 = createNode(c2);
      d1.addEdge(createEdge((DummyNode)d1, (DummyNode)d2));
      d2.addEdge(createEdge((DummyNode)d1, (DummyNode)d2));
    }
    
    public Collection<CommunityNode> getNodes(){
      return nodes.values();
    }
    public Collection<CommunityEdge> getEdgesList(){
      return edges.values();
    }
    public int getNodeCount(){
      return nodes.values().size();
    }
  }
  
  private static class DummyNode extends CommunityNode{
    private Collection<CommunityEdge> edges = new HashSet<CommunityEdge>();
    
    public DummyNode(Community c) {
      super(c.getId(), String.valueOf(c.getId()));
    }
    
    public Collection<CommunityEdge> getEdgesList(){
      return edges;
    }
    
    public void addEdge(DummyEdge e){
      edges.add(e);
    }
  }
  private static class DummyEdge extends CommunityEdge{
    public DummyEdge(DummyNode a, DummyNode b) {
      super(a, b, false);
    }
  }
  
}
