/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.satgraf.graph.UI.GraphFrame;

/**
 *
 * @author zacknewsham
 */
public class SaveAction implements ActionListener{
  private GraphFrame frame;
  public SaveAction(GraphFrame frame){
    this.frame = frame;
  }
  @Override
  public void actionPerformed(ActionEvent ae) {
    JFileChooser chooser = new JFileChooser();
    FileNameExtensionFilter filter = new FileNameExtensionFilter(
        "SB SatBench JSON files and CNF", "sb","cnf");
    chooser.setFileFilter(filter);
    int returnVal = chooser.showOpenDialog(frame);
    if(returnVal == JFileChooser.APPROVE_OPTION) {
      FileWriter f = null;
      try {
        String s = frame.toJson();
        f = new FileWriter(chooser.getSelectedFile());
        f.write(s);
        f.close();
      } 
      catch (IOException ex) {
        Logger.getLogger(SaveAction.class.getName()).log(Level.SEVERE, null, ex);
      } 
      finally {
        try {
          f.close();
        } 
        catch (IOException ex) {
          Logger.getLogger(SaveAction.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
  }
}