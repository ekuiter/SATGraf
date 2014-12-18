/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.placer;

import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityNode;
import com.satlib.community.placer.AbstractPlacer;
import com.satlib.community.placer.CommunityPlacer;

/**
 *
 * @author zacknewsham
 */
public class FruchGPUFalloverPlacer extends AbstractPlacer{
  private CommunityPlacer fruchImpl;
  
  public FruchGPUFalloverPlacer(CommunityGraph graph){
    super(graph);
    fruchImpl = new FruchGPUPlacer(graph);
  }
  
  @Override
  public CommunityNode getNodeAtXY(int x, int y, double scale) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void init() {
    try{
      fruchImpl.init();
    }
    catch(Exception e){
      e.printStackTrace();
      System.err.println("GPU Placer not supported, falling back to CPU placer");
      fruchImpl = new FruchPlacer(this.graph);
      fruchImpl.init();
    }
  }

  @Override
  public int getX(CommunityNode node) {
    return fruchImpl.getX(node);
  }

  @Override
  public int getY(CommunityNode node) {
    return fruchImpl.getY(node);
  }

  @Override
  public int getCommunityX(int community) {
    return fruchImpl.getCommunityX(community);
  }

  @Override
  public int getCommunityY(int community) {
    return fruchImpl.getCommunityY(community);
  }

  @Override
  public int getCommunityWidth(int community) {
    return fruchImpl.getCommunityWidth(community);
  }

  @Override
  public int getCommunityHeight(int community) {
    return fruchImpl.getCommunityHeight(community);
  }

  @Override
  public String getProgressionName() {
    return fruchImpl.getProgressionName();
  }

  @Override
  public double getProgress() {
    return fruchImpl.getProgress();
  }
  
}
