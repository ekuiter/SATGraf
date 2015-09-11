/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.community.UI;

import com.satgraf.graph.UI.EdgeLayer;
import com.satgraf.graph.UI.GraphCanvas;
import com.satgraf.graph.UI.HighlightLayer;
import java.awt.Graphics;

/**
 *
 * @author zacknewsham
 */
public class CommunityCanvas extends GraphCanvas {
	
  private Graphics g;
  
  public CommunityCanvas(CommunityGraphViewer graph){
    super(graph);
  }
  
  @Override
  protected HighlightLayer createNewHighlightLayer(){
    return new CommunityHighlightLayer(this.getSize(), (CommunityGraphViewer)graph);
  }
  
  @Override
  protected EdgeLayer createNewEdgeLayer() {
	  return new EdgeLayer(this.getSize(), graph);
  }
}
