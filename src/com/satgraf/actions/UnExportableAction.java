/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.actions;

import java.io.File;
import com.satgraf.graph.UI.GraphFrame;

/**
 *
 * @author zacknewsham
 */
public class UnExportableAction extends ExportAction<GraphFrame>{

  public UnExportableAction(GraphFrame frame) {
    super(frame);
  }

  @Override
  public void export(File file) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
}