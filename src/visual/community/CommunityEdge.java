/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.community;

import visual.graph.Edge;

/**
 *
 * @author zacknewsham
 */
public class CommunityEdge extends Edge<CommunityNode>{
  public static final int INTER_COMMUNITY = 0x1;
  public static final int INTRA_COMMUNITY = 0x0;
  //private final int community_id;
  protected boolean dummy;
  public CommunityEdge(CommunityNode a, CommunityNode b, boolean dummy){
    this(a, b);
    this.dummy = dummy;
  }
  public CommunityEdge(boolean inter_community, int community_id){
    super();
    a = new CommunityNode(0, "");
    a.setCommunity(community_id);
    b = new CommunityNode(0, "");
    a.setCommunity(community_id+1);
    //this.community_id = community_id;
  }
  public CommunityEdge(CommunityNode a, CommunityNode b) {
    super(a, b);
  }
  public CommunityEdge(boolean inter_community){
    a = new CommunityNode(0, "");
    b = new CommunityNode(0, "");
    //community_id = -1;
  }
  
  public int getCommunity(){
    return 0;//community_id;
  }
  
  public int getType(){
    if(a.getId() == 0 && dummy){
      return DUMMY;
    }
    else if(a.getId() == 0 && a.getCommunity() == b.getCommunity()){
      return INTRA_COMMUNITY | DUMMY;
    }
    else if(a.getId() == 0 && a.getCommunity() != b.getCommunity()){
      return INTER_COMMUNITY | DUMMY;
    }
    else if(a.getId() != 0 && a.getCommunity() == b.getCommunity()){
      return INTRA_COMMUNITY;
    }
    else{
      return INTER_COMMUNITY;
    }
  }
  
  @Override
  public String toString(){
    if(a.getId() == 0 && dummy){
      return "All Inter Community";
    }
    else if(a.getId() != b.getId()){
      return String.format("Community %d Inter Community", 0);
    }
    else if(a.getId() == b.getId()){
      return String.format("Community %d Intra Community", 0);
    }
    else{
      return super.toString();
    }
  }
  
  public void setWeight(double weight) {
  }
  
  public double getWeight() {
    if(dummy){
      return 5;
    }
    else if(a.getCommunity() == b.getCommunity()){
      return 1;
    }
    else{
      return 0.4;
    }
  }
  
}
