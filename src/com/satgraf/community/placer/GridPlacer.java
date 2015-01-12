/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.placer;

import com.satlib.community.Community;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityNode;
import com.satlib.community.placer.AbstractPlacer;
import com.satlib.graph.DrawableNode;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author zacknewsham
 */
public class GridPlacer extends AbstractPlacer {
	
  private HashMap<CommunityNode, Point> nodePositions = new HashMap<CommunityNode, Point>();
  private TIntObjectHashMap<Dimension> communitySizes = new TIntObjectHashMap<>();

  private static class GridPoint{
    public int x;
    public int y;

    GridPoint(int x, int y){
      this.x = x;
      this.y = y;
    }
    @Override
    public boolean equals(Object o){
      GridPoint p = (GridPoint) o;
      return p.x == this.x && p.y == this.y;
    }

    @Override
    public int hashCode() {
      int hash = 7;
      hash = 31 * hash + this.x;
      hash = 31 * hash + this.y;
      return hash;
    }
  }
  private static class GridEntry{
    private boolean free;
    GridPoint leftEntry;
    GridPoint rightEntry;
    GridPoint downEntry;
    GridPoint upEntry;
    GridEntry(int x, int y, boolean free){
      leftEntry = new GridPoint(x-1, y);
      rightEntry = new GridPoint(x+1, y);
      upEntry = new GridPoint(x, y+1);
      downEntry = new GridPoint(x, y+1);
      this.free = free;
    }
  }
  private static final int resolution = DrawableNode.NODE_DIAMETER + DrawableNode.NODE_X_SPACING;
  /**
   * Maps a cell on a grid to the number of free cells down and rights of it. 
   */
  private HashMap<Integer, Rectangle> communityAreas = new HashMap<Integer, Rectangle>();
  
  private HashMap<GridPoint, GridEntry> grid = new HashMap<GridPoint, GridEntry>();
  
  
    public String getProgressionName(){
      return "Placing Communities";
    }
    public double getProgress(){
      return 0.0;
    }
  public GridPlacer(CommunityGraph graph){
    super(graph);
  }
  public int getCommunityAtXY(int x, int y){
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
        return community;
      }
    }
    return -1;
  }
  public CommunityNode getNodeAtXY(int x, int y, double scale) {
    x /= scale;
    y /= scale;
    int community = getCommunityAtXY(x, y);
    if(community == -1){
      return null;
    }
    Iterator<CommunityNode> nodes = getCommunityNodes(community).iterator();
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
  public void init(){
    int largest = (int)Math.round(Math.sqrt(graph.getLargestCommunity()));
    //largest *= (int) Math.round(Math.sqrt(graph.getNumberCommunities()));
    
    //worst case scenario, we can fit all communities regardless of size into: 
    /*for(int x = 0; x < largest; x++){
      for(int y = 0; y < largest; y++){
        grid.put(new GridPoint(x, y), new GridEntry(x, y, true));
      }
    }*/
  }
  private GridPoint availableGridEntry(Dimension d, GridPoint check){
    /*GridEntry atPoint = grid.get(check);
    if(atPoint.free){
      boolean free = true;
      for(int x = 0; x < d.width; x++){
        for(int y = 0; y < d.height; y++){
          Point p = new Point(check.x + x, check.y + y);
          if(grid.get(check).free == false){
            free = false;
            break;
          }
        }
        if(free == false){
          break;
        }
      }
      if(!free){
        if(check.x > check.y){
          check.y++;
        }
        else{
          check.x++;
        }
        check = availableGridEntry(d, check);
      }
    }
    else if(check.x < Math.sqrt(grid.size()) - d.width && check.y < Math.sqrt(grid.size()) - d.height){
      if(check.x > check.y){
        check.y++;
      }
      else{
        check.x++;
      }
      check = availableGridEntry(d, check);
    }
    else{
      check = null;
    }*/
    return check;
  }
  
  private void occupyGridEntry(Rectangle r){
    /*GridPoint p = new GridPoint();
    p.x = r.x;
    p.y = r.y;
    Dimension s = new Dimension();
    s.width = r.width;
    s.height = r.height;
    
    
    for(int x = s.width; x > 0; x--){
        GridEntry e = grid.get(p);
        e.free = false;
        grid.get(e.rightEntry).leftEntry = e.leftEntry;
        
        e.rightEntry.x += x-1;
        grid.get(e.leftEntry).rightEntry = e.rightEntry;
        
        grid.get(e.downEntry).upEntry = e.upEntry;
        e.downEntry.y += s.height-1;
        grid.get(e.upEntry).downEntry = e.downEntry;
        
        
        p.y += s.height - 1;
        GridEntry e1 = grid.get(p);
        e1.free = false;
        grid.get(e1.rightEntry).leftEntry = e1.leftEntry;
        
        e.rightEntry.x += x-1;
        grid.get(e1.leftEntry).rightEntry = e1.rightEntry;
        
        grid.get(e1.upEntry).downEntry = e1.downEntry;
        e1.upEntry.y -= s.height;
        grid.get(e1.downEntry).upEntry = e1.upEntry;
        
        p.y -= (s.height - 1);
        p.x++;
    }
    p.x = r.x;
    for(int y = s.height - 1; y > 1;  y--){
      p.y ++;
      GridEntry e = grid.get(p);
      e.free = false;
      grid.get(e.rightEntry).
    }*/
  }
  
  /*public int getCommunityX(int community){
    if(communityAreas.get(community) == null){
      Rectangle r = new Rectangle();
      r.width = getCommunityWidth(community) / resolution;
      r.height = getCommunityHeight(community) / resolution;
      GridPoint free = availableGridEntry(r.getSize(), new GridPoint(0, 0));
      occupyGridEntry(r);
      r.width *= resolution;
      r.height *= resolution;
      r.x = free.x * resolution;
      r.y = free.y * resolution;
      communityAreas.put(community, r);
    }
    return communityAreas.get(community).x;
  }
  
  public int getCommunityY(int community){
    if(communityAreas.get(community) == null){
      Rectangle r = new Rectangle();
      r.width = getCommunityWidth(community);
      r.height = getCommunityHeight(community);
      r.width = getCommunityWidth(community) / resolution;
      r.height = getCommunityHeight(community) / resolution;
      GridPoint free = availableGridEntry(r.getSize(), new GridPoint(0, 0));
      occupyGridEntry(r);
      r.width *= resolution;
      r.height *= resolution;
      r.x = free.x * resolution;
      r.y = free.y * resolution;
      communityAreas.put(community, r);
    }
    return communityAreas.get(community).y;
  }*/

  private TIntIntHashMap rowSizes = new TIntIntHashMap();
  private int getLargestCommunityInRow(int community){
    if(rowSizes.containsKey(community)){
      return rowSizes.get(community);
    }
      int comms_across = (int)Math.ceil(Math.sqrt(graph.getNumberCommunities()));
      //int i = community + (community % comms_across);
      //i = Math.max(i, 6);
      int largest = 0;
      int largest_i = 0;
      int i = 0;
      int start = community - (community % comms_across);
      while(start + i < start + comms_across){
        if(graph.getCommunitySize(start +i) > largest){
          largest = graph.getCommunitySize(start + i);
          largest_i = start + i;
        }
        i++;
      }
      rowSizes.put(community, largest_i);
      return largest_i;
  }
  public int getCommunityWidth(int community){
    return getDimensions(community).width;
  }
  
  public Dimension getDimensions(int community) {
	Dimension d = communitySizes.get(community);
    if(d == null){
      d = new Dimension();
      int nNodes = graph.getCommunitySize(community);
      if (nNodes == 1) {
          d.width = 50;
          d.height = 50;
      } 
      else {
          d.width = 50 + nNodes * 3;
          d.height = 50 + nNodes * 3;
      }
      communitySizes.put(community, d);
    }
    
    return d;
  }
  
  public int getCommunityHeight(int community){
	  return getDimensions(community).height;
  }
  
  public int getCommunityX(int community){
    Rectangle r = communityAreas.get(community);
    if(r == null){
      r = new Rectangle();
      int comms_across = (int)Math.ceil(Math.sqrt(graph.getNumberCommunities()));
      int communityXIndex = community % comms_across;
      int x = 50;
      for (int i = communityXIndex - 1; i >= 0; i-- ){
        x += getCommunityWidth(community - (i + 1));
      }
      r.x = x;
      r.y = getCommunityY(community);
      r.width = getCommunityWidth(community);
      r.height = getCommunityHeight(community);
      communityAreas.put(community, r);
    }
    return r.x;
  }
  
  public int getCommunityY(int community){
    Rectangle r = communityAreas.get(community);
    if(r == null){
      int comms_down = (int)Math.ceil(Math.sqrt(graph.getNumberCommunities()));
      int communityYIndex = community / comms_down;
      int y = 50;
      for (int i = communityYIndex - 1; i >= 0; i-- ){
        y += getCommunityHeight(getLargestCommunityInRow(community - ((i + 1) * comms_down)));//getCommunityHeight(community - ((i + 1) * comms_down));
      }
      return y;
    }
    return r.y;
  }
  
  public int getX(CommunityNode node){
    return getX(node, false);
  }
  private int getX(CommunityNode node, boolean fromY) {
    if(node.getCommunity() < -1){
      return 0;
    }
    if(nodePositions.get(node) == null || fromY){
      int index = node.getCommunity() == -1 ? node.getId() : graph.getCommunity(node.getCommunity()).indexOf(node);
      int offset = 50 + (int)(index % Math.round(Math.sqrt(graph.getCommunity(node.getCommunity()).getSize()))) * (DrawableNode.NODE_DIAMETER + DrawableNode.NODE_X_SPACING);
      
      Point p;
      if(!fromY){
        p = new Point();
        nodePositions.put(node, p);
        p.y = getY(node, true);
      }
      else{
        p = nodePositions.get(node);
      }
      p.x = getCommunityX(node.getCommunity()) + offset;
      return p.x;
    }
    return nodePositions.get(node).x;
  }
  
  public int getY(CommunityNode node){
    return getY(node, false);
  }
  private int getY(CommunityNode node, boolean fromX) {
    if(node.getCommunity() < -1){
      return 0;
    }
    if(nodePositions.get(node) == null || fromX){
      int index = node.getCommunity() == -1 ? node.getId() : graph.getCommunity(node.getCommunity()).indexOf(node);
      int offset =  50 + (int)(index / Math.round(Math.sqrt(graph.getCommunity(node.getCommunity()).getSize()))) * (DrawableNode.NODE_DIAMETER + DrawableNode.NODE_X_SPACING);
      
      Point p;
      
      if(!fromX){
        p = new Point();
        nodePositions.put(node, p);
        p.x = getX(node, true);
      }
      else{
        p = nodePositions.get(node);
      }
      p.y = getCommunityY(node.getCommunity()) + offset;
      return p.y;
    }
    return nodePositions.get(node).y;
  }

  public HashMap<CommunityNode, Point> getNodePositions() {
	  return this.nodePositions;
  }
}
