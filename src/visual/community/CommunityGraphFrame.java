/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.community;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import visual.UI.GraphCanvasPanel;
import visual.UI.GraphFrame;
import visual.graph.GraphViewer;

/**
 *
 * @author zacknewsham
 */
public class CommunityGraphFrame extends GraphFrame{

  protected HashMap<String, Pattern> patterns;
  public CommunityGraphFrame(CommunityGraphViewer graphViewer, HashMap<String, Pattern> patterns) {
    super(graphViewer);
    this.patterns = patterns;
  }
  public CommunityGraphViewer getGraphViewer(){
    return (CommunityGraphViewer)graphViewer;
  }
  public void show() {
    if(panel == null && graphViewer != null){
      canvasPanel = new GraphCanvasPanel(new CommunityCanvas(getGraphViewer()));

      panel = new CommunityOptionsPanel(getGraphViewer(), patterns.keySet());
    }
    super.show();
  }
  
  public static void main(String args[]){
    CommunityGraphFrame frame = new CommunityGraphFrame(null, null);
    
    frame.show();
  }
  
  public void open(File file){
    CommunityGrapher grapher = new CommunityGrapher(file.getAbsolutePath(), "ol", "f", new HashMap<String, String>());
    
    try {
      grapher.generateGraph();
      this.graphViewer = new CommunityGraphViewer(grapher.getGraph(), grapher.getNode_lists(), grapher.placer);
      this.patterns = new HashMap<>();
      init();
      show();
    } 
    catch (IOException ex) {
      Logger.getLogger(CommunityGraphFrame.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
}
