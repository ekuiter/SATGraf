/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.graph.UI;

import com.satlib.Progressive;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

/**
 *
 * @author zacknewsham
 */
public class GraphCanvasPanel extends JPanel implements Progressive{
  protected GraphCanvas canvas;
  protected JScrollPane canvasPane;
  public GraphCanvasPanel(){
    
  }
  public GraphCanvasPanel(GraphCanvas canvas){
    this.canvas = canvas;
    canvasPane = new JScrollPane(canvas);
    canvasPane.setBorder(BorderFactory.createEmptyBorder());
    canvasPane.setSize(getCanvasDimensions());
    canvasPane.setPreferredSize(getCanvasDimensions());
    canvasPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
  }
  
  public void init(){
    this.setLayout(new BorderLayout());
    this.setBackground(Color.BLACK);
    this.add(canvasPane, BorderLayout.CENTER);
  }
  
  public int getHorizontalScrollPosition(){
    return canvasPane.getHorizontalScrollBar().getValue();
  }
  public int getVerticalScrollPosition(){
    return canvasPane.getVerticalScrollBar().getValue();
  }
  public void setVerticalScrollPosition(int pos){
    canvasPane.getVerticalScrollBar().setValue(pos);
    canvasPane.updateUI();
    canvasPane.revalidate();
    canvasPane.repaint();
  }
  public void setHorizontalScrollPosition(int pos){
    canvasPane.getHorizontalScrollBar().setValue(pos);
    canvasPane.updateUI();
    canvasPane.revalidate();
    canvasPane.repaint();
  }
  public int getFullWidth(){
    return canvas.getWidth();
  }
  public int getFullHeight(){
    return canvas.getHeight();
  }
  public void paintFull(Graphics g){
    canvas.paint(g);
  }
  public void repaint(){
    super.repaint();
    if(canvas != null){
      canvas.repaint();
    }
    if(canvasPane != null){
      canvasPane.repaint();
    }
  }
  
  static public Dimension getCanvasDimensions() {
	  GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	  int width = ((int) gd.getDisplayMode().getWidth()) - 410;
	  int height = ((int) gd.getDisplayMode().getHeight()) - 68;
	  
	  return new Dimension(width, height);
  }

  @Override
  public String getProgressionName() {
    return canvas.getProgressionName();
  }

  @Override
  public double getProgress() {
    return canvas.getProgress();
  }
}
