/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visual.UI;

import visual.graph.GraphViewer;
import java.awt.Dimension;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

/**
 *
 * @author zacknewsham
 */
public abstract class GraphOptionsPanel extends JSplitPane{
  private GraphViewer graph;
  protected OptionsPanel optionsPanel;
  protected final JAccordianPanel checkBoxPanel = new JAccordianPanel();
  private final HashMap<String, NodeCheckboxPanel> checkboxPanels = new HashMap<String, NodeCheckboxPanel>();
  protected final Collection<String> groups;
  protected GraphOptionsPanel(GraphViewer graph, Collection<String> groups, boolean callSet){
    this.groups = groups;
    this.setOrientation(JSplitPane.VERTICAL_SPLIT);
    this.setSize(200, 0);
    this.setPreferredSize(new Dimension(200, 0));
    if(callSet){
      setGraph(graph);
    }
    this.setBottomComponent(checkBoxPanel);
    this.setDividerLocation(0.5);
  }
  public GraphOptionsPanel(GraphViewer graph, Collection<String> groups){
    this(graph, groups, false);
  }
  
  public NodePanel getNodePanel(){
    return optionsPanel.getNodePanel();
  }
  public OptionsPanel getOptionsPanel(){
    return optionsPanel;
  }
  public void update(){
    Iterator<NodeCheckboxPanel> panels = checkboxPanels.values().iterator();
    while(panels.hasNext()){
      panels.next().update();
    }            
  }
  
  protected void setGraph(GraphViewer graph){
    optionsPanel.setGraph(graph);
    synchronized(checkBoxPanel){
      this.graph = graph;
      this.graph.setGraphPanel(this);
      checkBoxPanel.removeBars();
      checkboxPanels.clear();
      Iterator<String> groupsI = groups.iterator();
      while(groupsI.hasNext()){
        String group = groupsI.next();
        NodeCheckboxPanel temp = new NodeCheckboxPanel(graph, group, graph.getNodes(group));
        checkboxPanels.put(group, temp);
        JScrollPane tempScroll = new JScrollPane(temp);
        checkBoxPanel.addBar(group, tempScroll);
      }
      checkBoxPanel.setVisibleBar(0);
    }
  }
}
