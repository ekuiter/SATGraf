package visual.evolution2;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import visual.community.CommunityGraph;
import visual.community.CommunityGraphViewer;
import visual.community.CommunityNode;
import visual.evolution.EvolutionGrapher;

public class EvolutionGrapher2 extends EvolutionGrapher {
	
	  static EvolutionGrapher2 instance = null;
	  static public EvolutionGrapher2 getInstance() {
		  return instance;
		  
	  }
	
	  public EvolutionGrapher2(String dimacsFile, String communityMetric, String placer, int dumpFreq, HashMap<String, String> patterns, String minisat) {
	    super(dimacsFile, communityMetric, placer, 0, patterns, minisat);
	    instance = this;
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
	    ((EvolutionGraphFrame2)getFrame()).buildEvolutionFile();
        graph = cg;
	    getFrame().show();
	  }
	  
	  @Override
	  public void init(){
	    graphViewer = new CommunityGraphViewer((CommunityGraph)graph, node_lists, this.placer);
	    frmMain = new EvolutionGraphFrame2((CommunityGraphViewer)graphViewer, patterns, this);
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
	    EvolutionGrapher2 ag = new EvolutionGrapher2(args[0], args[1], args[2], Integer.parseInt(args[3]), patterns, args[4]);
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
            + "View the evolution of the community VIG of a SAT formula while being solved, dumping after every [dumpfreq] variable assignments";
  }
}
