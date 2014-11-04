/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.implication.UI;

import com.satlib.implication.ImplicationGraphViewer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;
import com.satgraf.graph.UI.GraphCanvasPanel;
import com.satgraf.graph.UI.GraphFrame;
import com.satgraf.graph.UI.SimpleCanvas;
import com.satgraf.actions.ExportAction;
import com.satgraf.actions.OpenAction;
import com.satgraf.actions.UnExportableAction;
import com.satgraf.actions.UnOpenableAction;


/**
 *
 * @author zacknewsham
 */
public class ImplicationGraphFrame extends GraphFrame{

  HashMap<String,Pattern> patterns;
  public ImplicationGraphFrame(ImplicationGraphViewer graphViewer, HashMap<String,Pattern> patterns) {
    super(graphViewer);
    this.patterns = patterns;
    canvasPanel = new GraphCanvasPanel(new SimpleCanvas(graphViewer));
    panel = new ImplicationOptionsPanel(this, getGraphViewer(), patterns.keySet());
  }
  
  @Override
  public OpenAction getOpenAction(){
    return new UnOpenableAction(this);
  }
  
  @Override
  public ExportAction getExportAction(){
    return new UnExportableAction(this);
  }
}
