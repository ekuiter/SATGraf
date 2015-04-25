/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution2.UI;

import com.satgraf.community.UI.CommunityGraphInfoPanel;
import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityNode;
import com.satlib.graph.GraphObserver;
import com.satlib.graph.GraphViewer;
import java.awt.GridBagConstraints;
import java.util.Collection;
import javax.swing.JLabel;
import org.json.simple.JSONObject;

/**
 *
 * @author zacknewsham
 */
public class Evolution2GraphInfoPanel extends CommunityGraphInfoPanel implements GraphObserver{
  private JLabel lblRatio1 = new JLabel("n/a");
  private JLabel lblRatio2 = new JLabel("n/a");
  private JLabel lblRatio3 = new JLabel("n/a");
  public Evolution2GraphInfoPanel(Evolution2GraphViewer graphViewwer) {
    super(graphViewwer);
    graphViewwer.addObserver(this);
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
    this.add(new JLabel("% Inter Decision Vars"), c);
    c.gridx = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_END;
    try{      
      this.add(lblRatio1, c);
    }
    catch(UnsupportedOperationException e){
      
    }
    
    c.gridx = 0;
    c.gridy = rows++;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    this.add(new JLabel("% Majority Inter Decision Vars"), c);
    c.gridx = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_END;
    try{      
      this.add(lblRatio2, c);
    }
    catch(UnsupportedOperationException e){
      
    }
    
    c.gridx = 0;
    c.gridy = rows++;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    this.add(new JLabel("% Decisions in same Community"), c);
    c.gridx = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_END;
    try{      
      this.add(lblRatio3, c);
    }
    catch(UnsupportedOperationException e){
      
    }
  }

  @Override
  public void notify(GraphViewer graph, Action action) {
    Collection<CommunityNode> decisions = ((Evolution2GraphViewer)graph).getGraph().getDecisionVariables();
    if(action == Action.decisionVariable && decisions.size() % 10 == 0){
      int lastCommunity = -1;
      int interCom = 0;
      int mostInterCom = 0;
      int sameCom = 0;
      for(CommunityNode n : decisions){
        int interComEdges = 0;
        for(CommunityEdge e : n.getEdgesList()){
          if(e.getOpposite(n).getCommunity() != n.getCommunity()){
            interComEdges++;
          }
        }
        if(interComEdges != 0){
          interCom++;
        }
        if(interComEdges > (double)n.getEdgesList().size() / 2.0){
          mostInterCom++;
        }
        if(lastCommunity == n.getCommunity()){
          sameCom++;
        }
        lastCommunity = n.getCommunity();
      }
      Double d1 = (double)interCom/(double)decisions.size();
      Double d2 = (double)mostInterCom/(double)decisions.size();
      Double d3 = (double)sameCom/(double)(decisions.size() - 1);
      lblRatio1.setText(d1.toString().substring(0, Math.min(d1.toString().length(), 5)));
      lblRatio1.revalidate();
      lblRatio2.setText(d2.toString().substring(0, Math.min(d2.toString().length(), 5)));
      lblRatio2.revalidate();
      lblRatio3.setText(d3.toString().substring(0, Math.min(d3.toString().length(), 5)));
      lblRatio3.revalidate();
    }
  }

  @Override
  public void initFromJson(JSONObject json) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String toJson() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String JsonName() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
