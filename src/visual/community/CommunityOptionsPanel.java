/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.community;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JScrollPane;
import org.json.simple.JSONObject;

import visual.UI.EdgeCheckBoxPanel;
import visual.UI.GraphOptionsPanel;
import visual.UI.NodePanel;
import visual.UI.OptionsPanel;

/**
 *
 * @author zacknewsham
 */
public class CommunityOptionsPanel extends GraphOptionsPanel{

  private CommunityCheckBoxPanel communityPanel;
  private JScrollPane communityScroll;
  
  private EdgeCheckBoxPanel interConnectionPanel;
  private JScrollPane interConncetionScroll;
  
  private EdgeCheckBoxPanel intraConnectionPanel;
  private JScrollPane intraConncetionScroll;
  
  
  protected CommunityOptionsPanel(CommunityGraphViewer graph, Collection<String> groups, boolean callSet) {
    super(graph, groups, false);
    NodePanel nodePanel = new CommunityNodePanel(graph);
    optionsPanel = new OptionsPanel(graph,nodePanel);
    if(callSet){
      setGraph(graph);
    }
    this.setTopComponent(optionsPanel);
  }
  public CommunityOptionsPanel(CommunityGraphViewer graph, Collection<String> groups) {
    this(graph, groups, true);
  }
  
  @Override
  public void update(){
    super.update();
    optionsPanel.update();
  }
  
  public void fromJson(JSONObject json){
    communityPanel.fromJson((JSONObject)json.get("communities"));
    interConnectionPanel.fromJson((JSONObject)json.get("interEdges"));
    intraConnectionPanel.fromJson((JSONObject)json.get("intraEdges"));
  }
  public String toJson(){
    StringBuilder json = new StringBuilder(super.toJson());
    
    json.setCharAt(json.length() - 1, ',');
    json.append("\"communities\":").append(communityPanel.toJson()).append(",");
    json.append("\"interEdges\":").append(interConnectionPanel.toJson()).append(",");
    json.append("\"intraEdges\":").append(intraConnectionPanel.toJson()).append("}");
    
    return json.toString();
  }
  
  protected void setGraph(CommunityGraphViewer graph){
    super.setGraph(graph);
    synchronized(checkBoxPanel){
      communityPanel = new CommunityCheckBoxPanel(graph);
      communityScroll = new JScrollPane(communityPanel);
      checkBoxPanel.addBar("Communities", communityScroll);
      int community = 0;
      Collection<CommunityEdge> interConnections = graph.getInterCommunityConnections(community);

      HashSet<CommunityEdge> interConnectionForCheckboxes = new HashSet<CommunityEdge>();
      interConnectionForCheckboxes.add(new CommunityEdge(true));
      HashSet<CommunityEdge> intraConnectionForCheckboxes = new HashSet<CommunityEdge>();
      intraConnectionForCheckboxes.add(new CommunityEdge(false));
      while(interConnections != null && !interConnections.isEmpty()){
        interConnectionForCheckboxes.add(new CommunityEdge(true, community));
        intraConnectionForCheckboxes.add(new CommunityEdge(false, community));
        community++;
        interConnections = graph.getInterCommunityConnections(community);
      }
      interConnectionPanel = new EdgeCheckBoxPanel(graph, interConnectionForCheckboxes);
      interConncetionScroll = new JScrollPane(interConnectionPanel);
      checkBoxPanel.addBar("Inter Connections", interConncetionScroll);

      intraConnectionPanel = new EdgeCheckBoxPanel(graph, intraConnectionForCheckboxes);
      intraConncetionScroll = new JScrollPane(intraConnectionPanel);
      checkBoxPanel.addBar("Intra Connections", intraConncetionScroll);
    }
  }
}
