/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.UI;

import com.satgraf.graph.UI.GraphInfoPanel;
import com.satlib.community.CommunityGraph;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;

/**
 *
 * @author zacknewsham
 */
public class CommunityGraphInfoPanel extends GraphInfoPanel<CommunityGraphViewer>{

  public CommunityGraphInfoPanel(CommunityGraphViewer graphViewwer) {
    super(graphViewwer);
  }
  public CommunityGraph getGraph(){
    return (CommunityGraph)super.graph;
  }

  @Override
  public void init() {
    super.init();
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = getInsets();
    c.insets.left = 10;
    c.insets.right = 10;
    c.weighty = 1.0;
    c.gridx = 0;
    c.gridy = rows++;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    this.add(new JLabel("Q"), c);
    c.gridx = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_END;
    try{
      this.add(new JLabel(String.valueOf(getGraph().getQ())), c);
    }
    catch(UnsupportedOperationException e){
      
    }
    
    c.gridx = 0;
    c.gridy = rows++;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;
    this.add(new JLabel("#Communities"), c);
    c.gridx = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_END;
    try{
    this.add(new JLabel(String.valueOf(getGraph().getCommunities().size())), c);
    }
    catch(UnsupportedOperationException | NullPointerException e){
      
    }
    
    c.gridx = 0;
    c.gridy = rows++;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;
    this.add(new JLabel("Min Com Size"), c);
    c.gridx = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_END;
    try{
    this.add(new JLabel(String.valueOf(getGraph().getMinCommunitySize())), c);
    }
    catch(UnsupportedOperationException e){
      
    }
    
    c.gridx = 0;
    c.gridy = rows++;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;
    this.add(new JLabel("Max Com Size"), c);
    c.gridx = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_END;
    try{
    this.add(new JLabel(String.valueOf(getGraph().getMaxCommunitySize())), c);
    }
    catch(UnsupportedOperationException e){
      
    }
    
  }  
}
