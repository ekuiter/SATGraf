/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.actions;

import java.io.File;
import visual.UI.GraphFrame;

/**
 *
 * @author zacknewsham
 */
public class UnopenableAction extends OpenAction{

  public UnopenableAction(GraphFrame frame) {
    super(frame);
  }

  @Override
  public void open(File file) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
}