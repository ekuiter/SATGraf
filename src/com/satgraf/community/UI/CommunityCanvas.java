/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.community.UI;

import java.awt.Graphics;

import com.satgraf.graph.UI.EdgeLayer;
import com.satgraf.graph.UI.GraphCanvas;
import com.satlib.graph.GraphViewer;

/**
 *
 * @author zacknewsham
 */
public class CommunityCanvas extends GraphCanvas {
	
  private Graphics g;
  
  public CommunityCanvas(GraphViewer graph){
    super(graph);
  }
  
  @Override
  protected EdgeLayer createNewEdgeLayer() {
	  return new CommunityEdgeLayer(this.getSize(), graph);
  }
}
