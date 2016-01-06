/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.placer;

import com.satgraf.community.placer.Fake.FakeCommunityGraph;
import com.satgraf.graph.placer.KKPlacer;
import com.satgraf.graph.placer.AbstractPlacer;
import com.satlib.community.Community;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityNode;
import com.satlib.graph.DrawableNode;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author zacknewsham
 */
public class GridKKPlacer extends AbstractPlacer<CommunityNode, CommunityGraph> implements CommunityPlacer{
  static{
    CommunityPlacerFactory.getInstance().register("gkk", "A basic layout algorithm, nodes are layed out within a community using the KK algorithm. The communities are then layed out on a grid", GridKKPlacer.class);
  }
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
      Iterator<CommunityNode> nodes = com.getNodes().iterator();
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
  
}
