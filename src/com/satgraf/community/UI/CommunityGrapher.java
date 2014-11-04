/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.community.UI;

import com.satlib.community.CNMCommunityMetric;
import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityGraphViewer;
import com.satlib.community.CommunityMetric;
import com.satlib.community.CommunityNode;
import com.satlib.community.ConcreteCommunityGraph;
import com.satlib.community.OLCommunityMetric;
import com.satlib.community.placer.AbstractPlacer;
import com.satgraf.community.placer.FruchPlacer;
import com.satgraf.community.placer.GridKKPlacer;
import com.satgraf.community.placer.GridPlacer;
import com.satlib.graph.Clause;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import com.satgraf.graph.UI.GraphCanvasPanel;
import com.satgraf.graph.UI.Grapher;

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
    if(!placerName.equals("kk") && !placerName.equals("f") && !placerName.equals("grid")){
      throw new IllegalArgumentException("third argument must be either kk or grid or f");
    }
  }
  public CommunityGrapher(String dimacsFile, String mapFile, HashMap<String, String> patterns){
    super(dimacsFile, mapFile, patterns);
  }
  public CommunityGraph getGraph(){
    return (CommunityGraph) graph;
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
    else if(placerName.equals("f")){
      this.placer = new FruchPlacer((CommunityGraph)graph);
    }
  }
  public void init(){
      graphViewer = new CommunityGraphViewer((CommunityGraph)graph, node_lists, this.placer);
      frmMain = new CommunityGraphFrame((CommunityGraphViewer)graphViewer, patterns);
  }
  
  public HashMap<String, TIntObjectHashMap<String>> getNode_lists(){
    return node_lists;
  }
  
  public static void main(String[] args) throws URISyntaxException {
    if(args.length == 0){                     
      args = new String[]{
       "formula/satcomp/dimacs/aes_16_10_keyfind_3.cnf",
       "ol",
	   "f"
      };
    }
    else if(args.length < 3){
      System.out.println("Too few options. Please use:");
      System.out.print(usage().concat("\n").concat(help()));
    }
    HashMap<String, String> patterns = new HashMap<String, String>();
  
    for(int i = 4; i < args.length; i+=2){
      patterns.put(args[i], args[i + 1]);
    }
  
    CommunityGrapher ag = new CommunityGrapher(args[0], args[1], args[2], patterns);
    try{
      ag.generateGraph();
      ag.init();
      ag.getFrame().show();
    }
    catch(FileNotFoundException e){
      e.printStackTrace();
    }
    catch(IOException e){
      e.printStackTrace();
    }
  }

  public static String usage(){
    return "[formula/path.cnf | saved/path.sb] [ol | cnm] [f | grid | kk]";
  }
  
  public static String help(){
    return "\"formula\" \"community algorithm\" \"layout algorithm\"\n"
            + "View the VIG of a SAT formula";
  }
}
