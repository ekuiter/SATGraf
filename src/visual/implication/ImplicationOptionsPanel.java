/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.implication;

import java.util.Collection;
import visual.UI.GraphOptionsPanel;
import visual.UI.NodePanel;
import visual.UI.OptionsPanel;
import visual.graph.GraphViewer;

/**
 *
 * @author zacknewsham
 */
public class ImplicationOptionsPanel extends GraphOptionsPanel{
  public ImplicationOptionsPanel(GraphViewer graph, Collection<String> groups) {
    super(graph, groups);
    NodePanel nodePanel = new ImplicationNodePanel(graph);
    optionsPanel = new OptionsPanel(graph,nodePanel);
    this.setTopComponent(optionsPanel);
    setGraph(graph);
  }
  
  
  @Override
  public void update(){
    super.update();
    optionsPanel.update();
  }
  
  protected void setGraph(GraphViewer graph){
    super.setGraph(graph);
  }
}
