/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.implication.UI;

import com.satgraf.actions.ExportAction;
import com.satgraf.actions.OpenAction;
import com.satgraf.actions.UnExportableAction;
import com.satgraf.actions.UnOpenableAction;
import com.satgraf.community.UI.CommunityGraphFrame;
import com.satgraf.graph.UI.GraphCanvasPanel;
import com.satgraf.graph.UI.GraphFrame;
import com.satgraf.graph.UI.SimpleCanvas;
import com.satlib.community.CommunityGraphFactory;
import com.satlib.community.CommunityGraphFactoryFactory;
import com.satlib.community.CommunityGraphViewer;
import com.satlib.implication.ImplicationGraphFactory;
import com.satlib.implication.ImplicationGraphFactoryFactory;
import com.satlib.implication.ImplicationGraphViewer;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;


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
  
  public static void main(String args[]) throws IOException{
    if(args.length < 3){
      args = new String[]{
        "formula/satcomp/dimacs/toybox.dimacs"
      };
    }
    
    HashMap<String, String> patterns = new HashMap<String, String>();

    for (int i = 1; i < args.length; i += 2) {
      patterns.put(args[i], args[i + 1]);
    }
    ImplicationGraphFactory factory = (new ImplicationGraphFactoryFactory()).getFactory(new File(args[0]), patterns);
    factory.makeGraph(new File(args[0]));
    
    ImplicationGraphViewer graphViewer = new ImplicationGraphViewer(factory.getGraph(), factory.getNodeLists());
    ImplicationGraphFrame frmMain = new ImplicationGraphFrame(graphViewer, factory.getPatterns());
    
    frmMain.show();
  }
}
