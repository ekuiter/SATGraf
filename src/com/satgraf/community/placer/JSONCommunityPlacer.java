/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.placer;

import com.satlib.community.CommunityNode;
import com.satlib.community.JSONCommunityGraph;

/**
 *
 * @author zacknewsham
 */
public class JSONCommunityPlacer implements CommunityPlacer{
  private final JSONCommunityGraph graph;
  private double progress = 1.0;
  public JSONCommunityPlacer(JSONCommunityGraph graph){
    this.graph = graph;
  }
  
  @Override
  public void init(){
    graph.init();
  }

  @Override
  public String getProgressionName() {
    return "Building graph from JSON";
  }

  @Override
  public double getProgress() {
    return progress;
  }
  

  @Override
  public CommunityNode getNodeAtXY(int x, int y, double scale) {
    return graph.getNodeAtXY(x, y, scale);
  }

  @Override
  public int getX(CommunityNode node) {
    return graph.getX(node);
  }

  @Override
  public int getY(CommunityNode node) {
    return graph.getY(node);
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
