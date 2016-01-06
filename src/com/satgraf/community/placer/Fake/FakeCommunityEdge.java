package com.satgraf.community.placer.Fake;

import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityNode;


  
public class FakeCommunityEdge extends CommunityEdge {
  private double weight = 0.1;
  public FakeCommunityEdge(CommunityNode a, CommunityNode b, boolean dummy) {
    super(a, b, dummy);
  }

  @Override
  public double getWeight(){
    return weight;
  }
  
  @Override
  public void setWeight(double weight){
    this.weight = weight;
  }
}