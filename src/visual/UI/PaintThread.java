/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.UI;

import java.awt.Graphics;
import java.awt.Rectangle;

/**
 *
 * @author zacknewsham
 */
public class PaintThread extends Thread{
  private final ThreadPaintable panel;
  private final Rectangle bounds;
  private final TiledImage image;
  private final Thread notify;
  private boolean finished = false;
  public PaintThread(ThreadPaintable panel, Rectangle bounds, TiledImage image, Thread notify){
    this.panel = panel;
    this.bounds = bounds;
    this.image = image;
    this.notify = notify;
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
  
}
