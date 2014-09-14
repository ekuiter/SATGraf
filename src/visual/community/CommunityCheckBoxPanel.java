/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.community;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.swing.JPanel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import visual.graph.Node;

/**
 *
 * @author zacknewsham
 */
class CommunityCheckBoxPanel extends JPanel{
  int count = 0;
  private HashMap<Integer, CommunityCheckBox> checkBoxes = new HashMap<>();
  private final CommunityGraphViewer graph;
  public CommunityCheckBoxPanel(CommunityGraphViewer graph) {
    this.graph = graph;
    init();
    this.setLayout(new GridLayout(count, 1));
  }
  public void fromJson(JSONObject json){
    for(Object o : (JSONArray)json.get("boxes")){
      int id = ((Long)((JSONObject)o).get("id")).intValue();
      boolean selected = (Boolean)((JSONObject)o).get("checked");
      if(checkBoxes.get(id).isSelected() != selected){
        checkBoxes.get(id).setSelected(selected);
      }
    }
  }
  public String toJson(){
    StringBuilder json = new StringBuilder();
    
    json.append("{\"boxes\":[");
    for(Integer com : checkBoxes.keySet()){
      json.append("{\"id\":").append(com).append(",\"checked\":").append(checkBoxes.get(com).isSelected()).append("},");
    }
    json.setCharAt(json.length() - 1, ']');
    json.append("}");
    return json.toString();
  }
  private void init(){
    int community = 0;
    Collection<CommunityNode> communities = graph.getCommunityNodes(community);
    while(communities != null){
      int intercomm = graph.getInterCommunityConnections(community).size();
      CommunityCheckBox jc = new CommunityCheckBox(community, communities.size(), intercomm);
      checkBoxes.put(community, jc);
      jc.setSelected(true);
      jc.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent ie) {
          CommunityCheckBox box = (CommunityCheckBox)ie.getItem();
          if(box.isSelected()){
            graph.showCommunity(box.getCommunity());
          }
          else{
            graph.hideCommunity(box.getCommunity());
          }
        }
      });
      community++;
      communities = graph.getCommunityNodes(community);
      this.add(jc);
      count ++;
    }
  }
}
