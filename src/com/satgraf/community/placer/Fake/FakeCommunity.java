/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.placer.Fake;

import com.satlib.community.Community;
import com.satlib.community.CommunityEdge;
import java.util.HashMap;

public class FakeCommunity extends Community{
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
}