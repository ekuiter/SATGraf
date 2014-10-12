/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.implication;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import visual.UI.GraphCanvasPanel;
import visual.UI.SimpleCanvas;
import visual.graph.Grapher;

/**
 *
 * @author zacknewsham
 */
public class ImplicationGrapher extends Grapher{
  public ImplicationGrapher(String dimacsFile, HashMap<String, String> patterns) {
   super(dimacsFile, patterns);
   graph = new ConcreteImplicationGraph();
  }
  
  public void init(){
    if(graphViewer == null){
      graphViewer = new ImplicationGraphViewer(getGraph(), node_lists);
      graphViewer.init();
      frmMain = new ImplicationGraphFrame((ImplicationGraphViewer)graphViewer, patterns);
      frmMain.init();
    }
  }
  public ImplicationGraph getGraph(){
    return (ImplicationGraph)graph;
  }
  
  @Override
  public void generateGraph() throws IOException{
    super.generateGraph();
    Iterator<ImplicationClause> cs = graph.getClauses();
    while(cs.hasNext()){
      ImplicationClause c = cs.next();
      if(c.size() == 1){
        ImplicationNode n = c.getNodes().next();
        if(c.satisfiedBy(n, true)){
          n.setValue(n.getValue(), ImplicationNode.SET.CONSTANT);
        }
        else{
          n.setValue(!n.getValue(), ImplicationNode.SET.CONSTANT);
        }
      }
    }
  }
  
  
  public static void main(String[] args) throws URISyntaxException{
    if(args.length == 0){                     
      args = new String[]{
       "formula/satcomp/dimacs/fiasco.dimacs",
       "ol",
  	   "kk"
      };
    }
    else if(args.length < 3){
      System.out.println("Too few options. Please use:");
      System.out.print(usage().concat("\n").concat(help()));
    }
    HashMap<String, String> patterns = new HashMap<String, String>();
  
    for(int i = 3; i < args.length; i+=2){
      patterns.put(args[i], args[i + 1]);
    }
    ImplicationGrapher ag = new ImplicationGrapher(args[0], patterns);
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
            + "Display the VIG of a SAT formula and view how assigning specific variables specific values forces the implied value of other variables";
  }
}
