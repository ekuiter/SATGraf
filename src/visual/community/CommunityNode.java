/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.community;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import visual.graph.Node;

/**
 *
 * @author zacknewsham
 */
public class CommunityNode extends Node<CommunityEdge>{
  private int community;
  private double size;

  public CommunityNode(int id, String name) {
    super(id, name);
    size = 0;
    community = -1;
  }
  public CommunityNode(int id, String name, boolean is_head, boolean is_tail){
    super(id, name, is_head, is_tail);
    this.community = -1;
  }
  public void setCommunity(int community) {
    this.community = community;
  }

  public int getCommunity() {	  
    return this.community;
  }
  
  public double getSize() {
    return size;
  }
  
  public void setSize(double size) {
    this.size = size;
  }
  
  public static CommunityNode fromJson(JSONObject json, CommunityGraph graph){
    CommunityNode node = new CommunityNode(((Long)json.get("id")).intValue(), (String)json.get("name"));
    node.community = ((Long)json.get("community")).intValue();
    JSONArray groups = (JSONArray)json.get("groups");
    for(Object g : groups){
      JSONObject jGroup = (JSONObject) g;
      node.addGroup((String)jGroup.get("group"));
    }
    
    return node;
  }
  
  public String toJson(){
    String tmp = super.toJson();
    StringBuilder json = new StringBuilder(tmp); 
    json.setCharAt(json.length() - 1, ',');
    json.append("\"community\":");
    json.append(community);
    json.append("}");
    return json.toString();
  }
  
}
