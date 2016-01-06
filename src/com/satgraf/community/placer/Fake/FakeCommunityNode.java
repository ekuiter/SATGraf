/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.placer.Fake;

import com.satlib.community.CommunityNode;
import java.util.HashMap;
import java.util.Map;

  

public class FakeCommunityNode extends CommunityNode{
  public static Map<Integer, Integer> communityWidths = new HashMap<>();
  public static Map<Integer, Integer> communityHeights = new HashMap<>();
  
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
    return communityWidths.get(c.c.getId());
  }
  public int getHeight(){
    return communityHeights.get(c.c.getId());
  }
}