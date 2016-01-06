/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.graph.UI;

import com.satgraf.UI.JAccordianPanel;
import com.satlib.graph.Edge;
import com.satlib.graph.Graph;
import com.satlib.graph.Node;
import java.awt.Dimension;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author zacknewsham
 */
public abstract class GraphOptionsPanel<N extends Node, E extends Edge, G extends Graph, V extends GraphViewer> extends JSplitPane implements GraphViewerObserver<N, E, G, V>{
  private GraphViewer graph;
  protected OptionsPanel optionsPanel;
  protected GraphInfoPanel infoPanel;
  private JScrollPane infoScroll;
  protected final JAccordianPanel checkBoxPanel = new JAccordianPanel();
  protected final HashMap<String, NodeCheckboxPanel> checkboxPanels = new HashMap<String, NodeCheckboxPanel>();
  protected final Collection<String> groups;
  protected GraphOptionsPanel(GraphViewer graph, Collection<String> groups, boolean callSet){
    this.groups = groups;
    this.setOrientation(JSplitPane.VERTICAL_SPLIT);
    this.setSize(100, 700);
    this.setPreferredSize(new Dimension(100, 700));
    if(callSet){
      setGraph(graph, true);
    }
    this.setBottomComponent(checkBoxPanel);
    this.setDividerLocation(0.5);
  }
  public GraphOptionsPanel(GraphViewer graph, Collection<String> groups){
    this(graph, groups, false);
  }
  
  public void fromJson(JSONObject json){
    optionsPanel.fromJson((JSONObject)json.get("optionsPanel"));
    this.setDividerLocation(((Long)json.get("dividerLocation")).intValue());
    //TODO: this doesnt work.
    checkBoxPanel.setVisibleBar(((Long)json.get("selectedCheckBoxPanel")).intValue());
    for(Object o : (JSONArray)json.get("checkBoxPanels")){
      JSONObject cb = (JSONObject)o;
      //checkboxPanels.get((String)cb.get("key")).fromJson((JSONObject)cb.get("data"));
    }
  }
  public String toJson(){
    StringBuilder json = new StringBuilder();
    json.append("{\"optionsPanel\":").append(optionsPanel.toJson());
    json.append(",\"dividerLocation\":").append(this.getDividerLocation());
    json.append(",\"selectedCheckBoxPanel\":").append(checkBoxPanel.getVisibleBar());
    json.append(",\"checkBoxPanels\":[");
    boolean added = false;
    for(String key : checkboxPanels.keySet()){
      json.append("{\"key\":\"");
      json.append(key);
      json.append("\",\"data\":");
      json.append(checkboxPanels.get(key).toJson());
      json.append("},");
      added = true;
    }
    if(added){
      json.setCharAt(json.length() - 1, ']');
    }
    else{
      json.append("]");
    }
    json.append("}");
    return json.toString();
  }
  
  public NodePanel getNodePanel(){
    return optionsPanel.getNodePanel();
  }
  public OptionsPanel getOptionsPanel(){
    return optionsPanel;
  }
  
  public void addPanel(JPanel panel, String name){
    checkBoxPanel.addBar(name, panel);
  }
  
  public void update(){
    Iterator<NodeCheckboxPanel> panels = checkboxPanels.values().iterator();
    while(panels.hasNext()){
      panels.next().update();
    }            
  }
  
  protected void setGraph(GraphViewer graph, boolean clearPanel){
    optionsPanel.setGraph(graph);
    synchronized(checkBoxPanel){
      this.graph = graph;
      this.graph.addObserver(this);
      
      if (clearPanel) {
    	  checkBoxPanel.removeBars();
          checkboxPanels.clear();
      }
      infoPanel.init();
      infoScroll = new JScrollPane(infoPanel);
      checkBoxPanel.addBar("Graph Info", infoScroll);
      
      Iterator<String> groupsI = groups.iterator();
      while(groupsI.hasNext()){
        String group = groupsI.next();
        NodeCheckboxPanel temp = new NodeCheckboxPanel(graph, group, graph.getNodes(group));
        checkboxPanels.put(group, temp);
        JScrollPane tempScroll = new JScrollPane(temp);
        checkBoxPanel.addBar("Variables - " + group, tempScroll);
      }
      checkBoxPanel.setVisibleBar(0);
    }
  }

  @Override
  public String JsonName() {
    return "options";
  }
  
  @Override
  public void notify(GraphViewer graph, GraphViewerObserver.Action action){
    this.update();
    if(action == Action.selectnode || action == Action.updatedEdges || action == Action.updatedNodes){
        this.update();
    }
    if(action == Action.selectnode){
      this.getNodePanel().setNode(graph.getSelectedNode());
      this.getNodePanel().update();
    }
  }
}
