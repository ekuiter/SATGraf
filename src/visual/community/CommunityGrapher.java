/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visual.community;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import visual.UI.GraphCanvasPanel;
import visual.community.drawing_algorithms.AbstractPlacer;
import visual.community.drawing_algorithms.GridKKPlacer;
import visual.community.drawing_algorithms.GridPlacer;
import visual.graph.Clause;
import visual.graph.Grapher;

/**
 *
 * @author zacknewsham
 */
public class CommunityGrapher extends Grapher <CommunityNode, CommunityEdge, Clause>{
  public static final boolean REMOVE_NOCON = false;
  public CommunityMetric metric;
  public AbstractPlacer placer;
  public String placerName;
  
  public CommunityGrapher(String dimacsFile, String communityMetric, String placer, HashMap<String, String> patterns){
    super(dimacsFile, patterns);
    if(communityMetric.equals("cnm")){
      this.metric = new CNMCommunityMetric();
    }
    else if(communityMetric.equals("ol")){
      this.metric = new OLCommunityMetric();
    }
    else{
      throw new IllegalArgumentException("Argument must be either ol or cnm");
    }
    
    this.placerName = placer;
    if(!placerName.equals("kk") && !placerName.equals("grid")){
      throw new IllegalArgumentException("third argument must be either kk or grid");
    }
  }
  public CommunityGrapher(String dimacsFile, String mapFile, HashMap<String, String> patterns){
    super(dimacsFile, mapFile, patterns);
  }
  public CommunityGraph getGraph(){
    return (CommunityGraph) graph;
  }
  
  @Override
  protected void show() {
    if(panel == null){
      graphViewer = new CommunityGraphViewer((CommunityGraph)graph, node_lists, this.placer);
      canvasPanel = new GraphCanvasPanel(new CommunityCanvas(graphViewer));

      panel = new CommunityOptionsPanel((CommunityGraphViewer)graphViewer, patterns.keySet());
    }
    super.show();
  }
  
  
  public void generateGraph() throws FileNotFoundException, IOException {
    graph = new ConcreteCommunityGraph();
    super.generateGraph();
    metric.getCommunities((CommunityGraph)graph);
    
    if(placerName.equals("kk")){
      this.placer = new GridKKPlacer((CommunityGraph)graph);
    }
    else if(placerName.equals("grid")){
      this.placer = new GridPlacer((CommunityGraph)graph);
    }
  }
  
  public static void main(String[] args) throws URISyntaxException {
    if(args.length == 0){                     
      args = new String[]{
       "formula/satcomp/dimacs/fiasco.dimacs",
       "cnm",
	   "kk",
	   "5",
	   "Message 1", "m1_[0-9]+_[0-9]+", 
       "Hash 1", "h1_[0-9]+_[0-9]+",
       "Message 2", "m2_[0-9]+_[0-9]+", 
       "Hash 2", "h2_[0-9]+_[0-9]+",
       "Messages", "m(1|2)_[0-9]+_[0-9]+", 
       "Hashes", "h(1|2)_[0-9]+_[0-9]+"
      };
    }
    HashMap<String, String> patterns = new HashMap<String, String>();
  
    for(int i = 4; i < args.length; i+=2){
      patterns.put(args[i], args[i + 1]);
    }
  
    CommunityGrapher ag = new CommunityGrapher(args[0], args[1], args[2], patterns);
    try{
      ag.generateGraph();
      ag.show();
    }
    catch(FileNotFoundException e){
      e.printStackTrace();
    }
    catch(IOException e){
      e.printStackTrace();
    }
  }
}
