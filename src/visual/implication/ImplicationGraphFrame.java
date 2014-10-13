/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.implication;

import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;
import visual.UI.GraphCanvasPanel;
import visual.UI.GraphFrame;
import visual.UI.SimpleCanvas;
import visual.actions.OpenAction;
import visual.actions.UnopenableAction;
import visual.graph.GraphViewer;

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
    panel = new ImplicationOptionsPanel((ImplicationGraphViewer)graphViewer, patterns.keySet());
  }
  
  @Override
  public OpenAction getOpenAction(){
    return new UnopenableAction(this);
  }
}
