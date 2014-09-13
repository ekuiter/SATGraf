/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import visual.graph.GraphViewer;

/**
 *
 * @author zacknewsham
 */
public class SaveAction implements ActionListener{
  private GraphViewer graphViewer;
  public SaveAction(GraphViewer graphViewer){
    this.graphViewer = graphViewer;
  }
  @Override
  public void actionPerformed(ActionEvent ae) {
    JFileChooser chooser = new JFileChooser();
    FileNameExtensionFilter filter = new FileNameExtensionFilter(
        "SB SatBench JSON files", "sb");
    chooser.setFileFilter(filter);
    int returnVal = chooser.showOpenDialog(graphViewer.getGraphCanvas());
    if(returnVal == JFileChooser.APPROVE_OPTION) {
      FileWriter f = null;
      try {
        String s = graphViewer.save();
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
