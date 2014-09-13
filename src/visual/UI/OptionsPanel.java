/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.UI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import visual.graph.GraphViewer;

/**
 *
 * @author zacknewsham
 */
public class OptionsPanel extends JPanel{
  private final NodePanel nodePanel;
  private final JPanel options = new JPanel();
  private final GraphScaler scale;
  private final GraphViewer graph;
  private JCheckBox hideAllNodes = new JCheckBox("All Nodes", true);
  private JCheckBox hideAllEdges = new JCheckBox("All Edges", true);
  public OptionsPanel(GraphViewer graph, NodePanel nodePanel){
    this.nodePanel = nodePanel;
    this.graph = graph;
    hideAllEdges.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        if(!hideAllEdges.isSelected()){
          OptionsPanel.this.graph.hideEdgeSet("All");
        }
        else{
          OptionsPanel.this.graph.showEdgeSet("All");
        }
      }
    });
    hideAllNodes.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        if(!hideAllNodes.isSelected()){
          OptionsPanel.this.graph.hideNodeSet("All");
        }
        else{
          OptionsPanel.this.graph.showNodeSet("All");
        }
      }
    });
    scale = new GraphScaler(graph);
    options.setLayout(new GridLayout(4, 1));
    this.options.add(scale);
    this.options.add(hideAllNodes);
    this.options.add(hideAllEdges);
    this.setLayout(new BorderLayout());
    this.add(nodePanel, BorderLayout.CENTER);
    this.add(options, BorderLayout.NORTH);
  }
  
  public String toJson(){
    StringBuilder json = new StringBuilder();
    json.append("{\"hideAllNodes\":").append(hideAllNodes.isSelected()).append(",");
    json.append("\"hideAllEdges\":").append(hideAllEdges.isSelected()).append("}");
    return json.toString();
  }
  
  public NodePanel getNodePanel(){
    return this.nodePanel;
  }
  public void update(){
    nodePanel.update();
  }

  void setGraph(GraphViewer graph) {
    scale.setGraph(graph);
  }
  public void setCustomComponent(Component c){
    options.add(c);
  }
}
