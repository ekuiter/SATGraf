/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.community;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import visual.UI.GraphCanvasPanel;
import visual.UI.GraphFrame;

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
    if(graphViewer != null){
      canvasPanel = new GraphCanvasPanel(new CommunityCanvas(getGraphViewer()));

      panel = new CommunityOptionsPanel(getGraphViewer(), patterns.keySet());
    }
    super.show();
  }
  
  public static void main(String args[]){
    CommunityGraphFrame frame = new CommunityGraphFrame(null, null);
    
    frame.show();
  }
  
  public void fromJson(JSONObject json){
    JSONCommunityGraph graph = new JSONCommunityGraph((JSONObject)json.get("graphViewer"));
    graph.init();
    this.graphViewer = new CommunityGraphViewer(graph, graph.getNodeLists(), graph);
    this.patterns = new HashMap<>();
    init();
    show();
    this.graphViewer.fromJson((JSONObject)json.get("graphViewer"));
    super.fromJson(json);
  }
  public String toJson(){
    StringBuilder json = new StringBuilder(super.toJson());
    
    return json.toString();
  }
  
  public void open(File file){
    try {
      String[] parts = file.getAbsolutePath().split("\\.");
      if(parts[parts.length - 1].equals("cnf")){
        CommunityGrapher grapher = new CommunityGrapher(file.getAbsolutePath(), "ol", "f", new HashMap<String, String>());
        grapher.generateGraph();
        this.graphViewer = new CommunityGraphViewer(grapher.getGraph(), grapher.getNode_lists(), grapher.placer);
        this.patterns = new HashMap<>();
        init();
        show();
      }
      else{
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder contents = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null){
          contents.append(line).append("\n");
        }
        JSONObject json = (JSONObject)JSONValue.parse(contents.toString());
        this.fromJson(json);
      }
    } 
    catch (IOException ex) {
      Logger.getLogger(CommunityGraphFrame.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
}
