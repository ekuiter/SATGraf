/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.evolution;

import gnu.trove.map.hash.TIntObjectHashMap;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import visual.community.CommunityGraph;
import visual.community.CommunityGraphViewer;
import visual.community.CommunityMetric;
import visual.community.CommunityNode;
import visual.community.ConcreteCommunityGraph;
import visual.community.drawing_algorithms.AbstractPlacer;
import visual.community.drawing_algorithms.GridKKPlacer;
/**
 *
 * @author zacknewsham
 */
class GraphBuilderRunnable implements Runnable{
  private CommunityGraph g = new ConcreteCommunityGraph();
  CommunityGraph original;
  private CommunityGraphViewer cgv;
  private HashMap<String, Pattern> patterns;
  private final ArrayList<String> lines = new ArrayList<String>();
  private boolean finished = false;
  private boolean finishedAll = false;
  private final Class<? extends CommunityMetric> metricClass;
  private final Class<? extends AbstractPlacer> placerClass;
  public GraphBuilderRunnable(CommunityGraph original, HashMap<String, Pattern> patterns, Class<? extends CommunityMetric> metricClass, Class<? extends AbstractPlacer> placerClass){
    this.original = original;
    this.patterns = patterns;
    this.metricClass = metricClass;
    this.placerClass = placerClass;
  }
  
  public void addLine(String line){
    synchronized(lines){
      lines.add(line);
    }
  }
  
  public String getLine(){
    synchronized(lines){
      if(lines.isEmpty()){
        return null;
      }
      return lines.remove(0);
    }
  }
  public void finished(){
    finished = true;
  }
  @Override
  public void run() {
    HashMap<String, TIntObjectHashMap<String>> nodes = new HashMap<>();
    TIntObjectHashMap<String>ns = new TIntObjectHashMap<String>();
    nodes.put("All", ns);
    nodes.put("Conflict", new TIntObjectHashMap<String>());
    String line = null;
    while((line = getLine()) != null || !finished){
      if(line == null){
        try {
          Thread.sleep(1000);
          continue;
        } 
        catch (InterruptedException ex) {

        }
      }
      String[] terms = line.split(" ");
      for(int i = 0; i < terms.length - 2; i++){
        int lit1 = Integer.parseInt(terms[i]);
        if(lit1 < 0){
          lit1 = 0 - lit1;
        }
        CommunityNode a1 = g.createNode(lit1, null);
        CommunityNode a2 = original.getNode(lit1);
        if(a2 != null && a1.inGroup("All") == false){
          a1.setName(a2.getName());
          Iterator<String> sets = a2.getGroups();
          while(sets.hasNext()){
            String s = sets.next();
            a1.addGroup(s);
            if(nodes.get(s) == null){
              nodes.put(s, new TIntObjectHashMap<String>());
            }
            nodes.get(s).put(lit1, a1.getName());
          }
        }
        else if(a2 == null){
          nodes.get("Conflict").put(lit1, "");
          ns.put(lit1, "");
          a1.addGroup("Conflict");
        }
        for(int a = i + 1; a < terms.length - 1; a++){
          int lit2 = Integer.parseInt(terms[a]);
          if(lit2 < 0){
            lit2 = 0 - lit2;
          }
          /*if(aExists && g.getNode(lit2) != null){
            continue;
          }*/
          CommunityNode b1 = g.createNode(lit2, null);
          CommunityNode b2 = original.getNode(lit2);
          if(b2 != null && b1.inGroup("All") == false){
            b1.setName(b2.getName());
            Iterator<String> sets = b2.getGroups();
            while(sets.hasNext()){
              String s = sets.next();
              b1.addGroup(s);
              if(nodes.get(s) == null){
                nodes.put(s, new TIntObjectHashMap<String>());
              }
              nodes.get(s).put(lit1, b1.getName());
            }
          }
          else if(b2 == null){
            nodes.get("Conflict").put(lit2, "");
            ns.put(lit2, "");
            b1.addGroup("Conflict");
          }
          g.connect(a1, b1, false);
        }
      }
    }
    CommunityMetric metric = null;
    try {
      metric = metricClass.newInstance();
      metric.getCommunities(g);
      cgv = new CommunityGraphViewer(g, nodes, placerClass.getConstructor(CommunityGraph.class).newInstance(g));
      finishedAll = true;
    } 
    catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
      Logger.getLogger(GraphBuilderRunnable.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  public CommunityGraphViewer getGraphViewer(){
    return cgv;
  }

  boolean isFinished() {
    return finishedAll;
  }
  
}
