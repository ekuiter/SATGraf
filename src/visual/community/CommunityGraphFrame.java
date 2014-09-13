/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.community;

import java.util.HashMap;
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
    if(panel == null){
      canvasPanel = new GraphCanvasPanel(new CommunityCanvas(getGraphViewer()));

      panel = new CommunityOptionsPanel(getGraphViewer(), patterns.keySet());
    }
    super.show();
  }
  
}
