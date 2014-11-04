/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.implication.UI;

import com.satlib.graph.GraphViewer;
import com.satlib.implication.ImplicationNode;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import com.satgraf.graph.UI.NodeLabel;

/**
 *
 * @author zacknewsham
 */
public class ImplicationNodeLabel extends NodeLabel<ImplicationNode> implements MouseListener{

  public ImplicationNodeLabel(GraphViewer graph, ImplicationNode n, boolean tf) {
    super(graph, n, tf);
    this.addMouseListener(this);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if(node.isSet() == false){
      if(e.getButton() == MouseEvent.BUTTON1){
        node.setValue(true, ImplicationNode.SET.DECISION);
      }
      else if(e.getButton() == MouseEvent.BUTTON2){
        node.setValue(false, ImplicationNode.SET.DECISION);
      }
    }
    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void mousePressed(MouseEvent e) {
    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void mouseExited(MouseEvent e) {
    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
}
