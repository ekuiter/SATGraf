/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.evolution;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import visual.community.CommunityGraph;
import visual.community.CommunityGraphViewer;
import visual.community.CommunityGrapher;
import visual.community.CommunityMetric;
import visual.community.CommunityNode;

/**
 *
 * @author zacknewsham
 */
public class EvolutionGrapher extends CommunityGrapher{
  private File dumpFile;
  private int dumpFreq = 5;
  protected final String minisat;
  
  public String getMinisat(){
    return this.minisat;
  }
  public EvolutionGrapher(String dimacsFile, String communityMetric, String placer, int dumpFreq, HashMap<String, String> patterns, String minisat) {
    super(dimacsFile, communityMetric, placer, patterns);
    this.dumpFreq = dumpFreq;
    this.dumpFile = new File(dimacsFile.replaceFirst("[.][^.]+$", "").concat(".dump"));
    if(this.dumpFile.exists()){
      this.dumpFile.delete();
    }
    this.minisat = minisat;
  }
      
  public void process(CommunityGraph cg){
    node_lists = new HashMap<String, TIntObjectHashMap<String>>();
    TIntObjectHashMap<String> list = new TIntObjectHashMap<String>();
    Iterator<CommunityNode> nodes = cg.getNodeIterator();
    while(nodes.hasNext()){
      CommunityNode node = nodes.next();
      list.put(node.getId(), node.getName());
    }
    node_lists.put("All", list);
    try{
      graph = cg;
      double d = Math.random();
      dimacsFile = new File(String.format("/tmp/%f.dimacs", d));
      dimacsFile.createNewFile();
      graph.writeDimacs(dimacsFile);

      dumpFile = new File(String.format("/tmp/%f.dump", d));
      dumpFile.createNewFile();
      ((EvolutionGraphFrame)getFrame()).loadAdditionalGraphs();
    }
    catch(IOException e){
      
    }
    getFrame().show();
  }
  
  
  public void init(){
    graphViewer = new CommunityGraphViewer((CommunityGraph)graph, node_lists, this.placer);
    frmMain = new EvolutionGraphFrame((CommunityGraphViewer)graphViewer, patterns, this);
  }
  public CommunityMetric getMetric(){
    return metric;
  }
  public File getDumpFile(){
    return dumpFile;
  }
  public File getDimacsFile(){
    return dimacsFile;
  }
  public int getDumpFreq(){
    return dumpFreq;
  }
  
          
  
  public static void main(String[] args){
	if (args.length == 0) {
	    args = new String[]{
	      "formula/satcomp/dimacs/fiasco.dimacs",
	      "ol",
	      "f",
	      "5",
          System.getProperty("user.dir") + "/minisat/minisat"
	    };
	}
    else if(args.length < 5){
      System.out.println("Too few options. Please use:");
      System.out.print(usage().concat("\n").concat(help()));
    }
    HashMap<String, String> patterns = new HashMap<String, String>();
      
    for(int i = 5; i < args.length; i+=2){
      patterns.put(args[i], args[i + 1]);
    }
    EvolutionGrapher ag = new EvolutionGrapher(args[0], args[1], args[2], Integer.parseInt(args[3]), patterns, args[4]);
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
    return "[formula/path.cnf | saved/path.sb] [ol | cnm] [f | grid | kk] [dumpfreq] [/path/to/solver]";
  }
  
  public static String help(){
    return "\"formula\" \"community algorithm\" \"layout algorithm\" \"dump frequency\" \"path to modified solver + options\"\n"
            + "View the evolution of the community VIG of a SAT formula while being solved, dumping after every [dumpfreq] conflict clauses";
  }
}
