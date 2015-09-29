/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.UI;

import javax.swing.JCheckBox;

/**
 *
 * @author zacknewsham
 */
public class CommunityCheckBox extends JCheckBox{
  private final int community;
  private final int size;
  private final int intercommunity;
  private final int intracommunity;
  public CommunityCheckBox(int community, int size, int intercommunity, int intracommunity){
    super(String.format("%d (Nodes: %d, Inter: %d, Intra: %d)", community, size, intercommunity, intracommunity));
    this.community = community;
    this.size = size;
    this.intracommunity = intracommunity;
    this.intercommunity = intercommunity;
  }
  public int getCommunity(){
    return this.community;
  }
  public int getCommunitySize(){
    return this.size;
  }
  public int getInterConnections(){
    return this.intercommunity;
  }
  public int getIntraConnections(){
    return this.intracommunity;
  }
}
