/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import visual.UI.GraphFrame;

/**
 *
 * @author zacknewsham
 */
public class OpenAction implements ActionListener{
  GraphFrame frame;
  public OpenAction(GraphFrame frame){
    this.frame = frame;
  }
  @Override
  public void actionPerformed(ActionEvent ae) {
    JFileChooser chooser = new JFileChooser();
    FileNameExtensionFilter filter = new FileNameExtensionFilter(
        "SB SatBench JSON files", "sb");
    chooser.setFileFilter(filter);
    int returnVal = chooser.showOpenDialog(frame);
    if(returnVal == JFileChooser.APPROVE_OPTION) {
      frame.open(chooser.getSelectedFile());
    }
  }
  
}
