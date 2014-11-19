/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.UI;

import java.awt.Graphics;
import java.awt.Rectangle;

import com.satgraf.graph.UI.TiledImage;

/**
 *
 * @author zacknewsham
 */
public class PaintThread extends Thread{
  private final ThreadPaintable panel;
  private final Rectangle bounds;
  private final TiledImage image;
  private boolean finished = false;
  private boolean forceDraw = false;
  
  public PaintThread(ThreadPaintable panel, Rectangle bounds, TiledImage image, boolean forceDraw){
    this.panel = panel;
    this.bounds = bounds;
    this.image = image;
    this.forceDraw = forceDraw;
    setFinished(false);
  }
  
  @Override
  public void run() {
	panel.paintThread(this);
  }
  
  public Graphics getGraphics(){
    return image.getGraphics();
  }
  
  public Rectangle getBounds(){
    return bounds;
  }
  
  public void setFinished(boolean finished){
    this.finished = finished;
    image.setFinished(finished);
  }
  
  public boolean getFinished(){
    return finished;
  }
  
  public boolean getForceDraw() {
	  return forceDraw;
  }
}
