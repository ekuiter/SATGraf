/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.community;

import org.json.simple.JSONObject;
import visual.graph.Edge;

/**
 *
 * @author zacknewsham
 */
public class CommunityEdge extends Edge<CommunityNode>{
  public static final int INTER_COMMUNITY = 0x1;
  public static final int INTRA_COMMUNITY = 0x0;
  
  //used for dummy edges
  private final int community_id;
  protected boolean dummy;
  public CommunityEdge(CommunityNode a, CommunityNode b, boolean dummy){
    this(a, b);
    this.dummy = dummy;
  }
  public CommunityEdge(boolean inter_community, int community_id){
    super();
    a = new CommunityNode(0, "");
    b = new CommunityNode(0, "");
    id=community_id;
    if(inter_community){
      a.setCommunity(community_id);
      b.setCommunity(community_id+1);
    }
    else{
      a.setCommunity(community_id);
      b.setCommunity(community_id);
    }
    this.community_id = community_id;
  }
  public CommunityEdge(CommunityNode a, CommunityNode b) {
    super(a, b);
    if(a.getCommunity() == b.getCommunity()){
      community_id = a.getCommunity();
    }
    else{
      community_id = 0;
    }
  }
  public CommunityEdge(boolean inter_community){
    a = new CommunityNode(0, "");
    b = new CommunityNode(0, "");
    id=-1;
    if(inter_community){
      a.setCommunity(0);
      b.setCommunity(-1);
    }
    else{
      a.setCommunity(0);
      b.setCommunity(0);
    }
    community_id = -1;
  }
  
  
  public int getType(){
    if(a.getId() == 0 && dummy){
      return DUMMY;
    }
    else if(getCommunity() < 0 && a.getCommunity() == b.getCommunity()){
      return INTRA_COMMUNITY | DUMMY;
    }
    else if(getCommunity() < 0 && a.getCommunity() != b.getCommunity()){
      return INTER_COMMUNITY | DUMMY;
    }
    else if(a.getCommunity() == b.getCommunity()){
      return INTRA_COMMUNITY;
    }
    else{
      return INTER_COMMUNITY;
    }
  }
  
  @Override
  public String toString(){
    if(a.getCommunity() != b.getCommunity()){
      if(getCommunity() == -1){
        return "All Inter Community";
      }
      return String.format("Community %d Inter Community", getCommunity());
    }
    else if(a.getCommunity() == b.getCommunity()){
      if(getCommunity() == -1){
        return "All Intra Community";
      }
      return String.format("Community %d Intra Community", getCommunity());
    }
    else{
      return super.toString();
    }
  }
  
  public int getCommunity(){
    return community_id;
  }
  
  public static CommunityEdge fromJson(JSONObject json, CommunityGraph graph){
    CommunityEdge edge = new CommunityEdge(graph.getNode(((Long)json.get("start")).intValue()),graph.getNode(((Long)json.get("end")).intValue()));
    edge.id = ((Long)json.get("id")).intValue();
    return edge;
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
