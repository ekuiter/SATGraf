/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.UI;

import com.satgraf.graph.UI.GraphCanvas;
import com.satgraf.graph.UI.HighlightLayer;
import com.satlib.community.Community;
import com.satlib.community.CommunityNode;
import com.satgraf.graph.UI.GraphViewer;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 *
 * @author zacknewsham
 */
public class CommunityHighlightLayer extends HighlightLayer<CommunityGraphViewer>{

  public CommunityHighlightLayer(Dimension size, CommunityGraphViewer graph) {
    super(size, graph);
  }
	
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHints(GraphCanvas.getRenderingHints());
    Community com = graph.getSelectedCommunity();
    if(com != null){
      for(CommunityNode cn : com.getCommunityNodes()){
        drawNodeHighlight(g, g2, cn, false);
      }
    }
  }
  
}
