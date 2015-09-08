/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.graph.UI;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 *
 * @author zacknewsham
 */
public class TiledImage extends BufferedImage{

  public Point origin;
  private boolean finshed = false;
  private Dimension size;
  private Graphics g;
  public int row = -1;
  public int column = -1;
  public TiledImage(int width, int height, int imageType, Point origin, Dimension size) {
    super(width, height, imageType);
    this.origin = origin;
    this.size = size;
  }
  public void setFinished(boolean finished){
    this.finshed = finished;
  }
  public boolean getFinished(){
    return finshed;
  }

  void setGraphics(Graphics2D g1) {
    this.g = g1;
  }
  public Graphics getGraphics(){
    return g;
  }
  public Rectangle getBounds(){
    return new Rectangle(origin, size);
  }
}
