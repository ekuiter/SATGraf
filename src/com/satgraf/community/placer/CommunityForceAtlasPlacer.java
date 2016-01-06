/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.placer;

import com.satgraf.community.placer.Fake.FakeCommunityGraph;
import com.satgraf.graph.placer.AbstractPlacer;
import com.satgraf.graph.placer.Coordinates;
import com.satgraf.graph.placer.ForceAtlas2;
import com.satgraf.graph.placer.FruchPlacer;
import com.satgraf.graph.placer.Placer;
import com.satlib.community.Community;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityNode;
import com.satlib.graph.DrawableNode;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author zacknewsham
 */
public class CommunityForceAtlasPlacer extends AbstractPlacer<CommunityNode, CommunityGraph> implements CommunityPlacer{
  static{
    CommunityPlacerFactory.getInstance().register("cForceAtlas2", "A basic layout algorithm, nodes are layed out within a community using the FR layout algorithm. The communities are then layed out using the force atlas2.", CommunityForceAtlasPlacer.class);
  }
  private double progress = 0.0;
  private static final int NODE_WIDTH = 10;
  private static final int NODE_PADDING = 10;
  private static final int COMMUNITY_PADDING = 100;
  private final HashMap<Community, Placer> nodePositions = new HashMap<>();
  private final HashMap<Community, Coordinates> communityPositions = new HashMap<>();
  private final HashMap<Community, Dimension> communitySizes = new HashMap<>();
  Random r = new Random(789543781278l);
  
  public CommunityForceAtlasPlacer(CommunityGraph graph) {
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
    Iterator<CommunityNode> nodes = c.getNodes().iterator();
    Rectangle r = new Rectangle(0, 0, DrawableNode.NODE_DIAMETER, DrawableNode.NODE_DIAMETER);
    while(nodes.hasNext()){
        CommunityNode node = (CommunityNode)nodes.next();
        r.x = getX(node) - DrawableNode.NODE_DIAMETER / 2;
        r.y = getY(node) - DrawableNode.NODE_DIAMETER / 2;
        if(r.contains(x, y)){
            return node;
        }
    }
    return null;
  }

  private Community c;
  private int offsetX = 0;
  private int offsetY = 0;
  ForceAtlas2 mainLayout;
  @Override
  public void init() {
    int complete = 0;
    double largest = 0.0;
    double circumference = 0;
    for(Community c : graph.getCommunities()){
      double radius = ((NODE_WIDTH + NODE_PADDING) * graph.getCommunitySize(c.getId())) / 2 / Math.PI;
      Dimension d = new Dimension();
      d.setSize((radius * 2), (radius * 2));
      communitySizes.put(c, d);
      Placer fr = new ForceAtlas2(c);//new FruchPlacer(c, (int)d.getWidth(), (int)d.getHeight());
      int minX = Integer.MAX_VALUE;
      int minY = Integer.MAX_VALUE;
      int maxX = 0;
      int maxY = 0;
      fr.init();
      for(CommunityNode n : c.getCommunityNodes()){
        int x = fr.getX(n); 
        int y = fr.getY(n); 
        if(x > maxX){
          maxX = x;
        }
        if(y > maxY){
          maxY = y;
        }
        if(x < minX){
          minX = x;
        }
        if(y < minY){
          minY = y;
        }
      }
      communitySizes.put(c, new Dimension(maxX-minX, maxY-minY));
      complete+=c.getNodeCount();
      progress = ((double)complete / (double)graph.getNodeCount()) / 3 * 2;
      nodePositions.put(c, fr);
    }
    
    FakeCommunityGraph fakeGraph = new FakeCommunityGraph(graph);
    for(CommunityNode n : fakeGraph.getNodes()){
      n.setSize(Math.max(communitySizes.get(graph.getCommunity(n.getId())).getWidth(), communitySizes.get(graph.getCommunity(n.getId())).getHeight()));
    }
    mainLayout = new ForceAtlas2(fakeGraph);
    mainLayout.setAdjustSizes(true);
    mainLayout.init();
    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    for(CommunityNode n : fakeGraph.getNodes()){
      int x = mainLayout.getX(n);
      int y = mainLayout.getY(n);
      minX = Math.min(x, minX);
      minY = Math.min(y, minY);
      communityPositions.put(graph.getCommunity(n.getId()), new Coordinates(x, y));
    }
    offsetX = 0 - minX;
    offsetY  = 0 - minY;
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
    return offsetX + (int)communityPositions.get(graph.getCommunity(community)).getX();
  }

  @Override
  public int getCommunityY(int community) {
    return offsetY + (int)communityPositions.get(graph.getCommunity(community)).getY();
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
    if(mainLayout == null){
      return progress;
    }
    else{
      return progress + (0.3 * mainLayout.getProgress());
    }
  }
  
}
