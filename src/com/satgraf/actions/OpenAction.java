/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.satgraf.graph.UI.GraphFrame;

/**
 *
 * @author zacknewsham
 */
public abstract class OpenAction<T extends GraphFrame> implements ActionListener{
  protected T frame;
  public OpenAction(T frame){
    this.frame = frame;
  }
  @Override
  public void actionPerformed(ActionEvent ae) {
    JFileChooser chooser = new JFileChooser();
    FileNameExtensionFilter filter = new FileNameExtensionFilter(
        "SB SatBench JSON files", "sb", "CNF dimacs files","cnf");
    chooser.setFileFilter(filter);
    int returnVal = chooser.showOpenDialog(frame);
    if(returnVal == JFileChooser.APPROVE_OPTION) {
      open(chooser.getSelectedFile());
    }
  }
  
  public abstract void open(File file);
  
}