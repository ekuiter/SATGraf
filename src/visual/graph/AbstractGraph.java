/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.graph;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author zacknewsham
 */
public abstract class AbstractGraph <T extends Node, T1 extends Edge, T2 extends Clause> implements Graph<T, T1, T2>{
  protected int clause_count;
  protected final HashMap<T2, T2> clauses = new HashMap<T2, T2>();
  protected final TIntObjectHashMap<T> nodes = new TIntObjectHashMap<T>();
  protected final HashMap<String, ArrayList<T>> nodes_set = new HashMap<>();
  protected final ArrayList<T1> connections = new ArrayList<T1>();
  protected UnionFind uf = new UnionFind();

  
  public int indexOf(T node){
    Iterator<T> nI = nodes.valueCollection().iterator();
    int i = 0;
    while(nI.hasNext()){
      T n1 = nI.next();
      if(n1 == node){
        return i;
      }
      i++;
    }
    return -1;
  }
  public T1 getEdge(T a, T b){
    //int id = ((a.getId() & 0xffff) << 16) | (b.getId() & 0xfff);
    return (T1)a.getEdge(b);
    //return (T1)connections.get(new Edge(a, b));
  }
  public void connect(T a, T b, boolean dummy){
    T1 e = getEdge(a, b);
    if(e == null){
      e = createEdge(a, b, dummy);
      a.addEdge(e);
      b.addEdge(e);
      union(a, b);
    }
  }
  public void union(T a, T b){
    uf.union(a, b);
  }
  
  public boolean connected(T a, T b){
    return uf.connected(a, b);
  }
  public T getNode(int id){
    return nodes.get(id);
  }
  public void removeNode(Node n){
    nodes.remove(n.getId());
  }
  public T createNode(int id, String name){
    return createNode(id, name, false, false);
  }

  public Collection<T> getNodesList() {
    ArrayList<T> ret = new ArrayList<T>(nodes.valueCollection());
    return ret;
  }
  public Iterator<T> getNodes(String set){
    if(nodes_set.get(set) == null){
      ArrayList<T> nodes = new ArrayList<>();
      Iterator<T> ns = getNodeIterator();
      while(ns.hasNext()){
        T n = ns.next();
        if(n.inGroup(set)){
          nodes.add(n);
        }
      }
      nodes_set.put(set, nodes);
    }
    return nodes_set.get(set).iterator();
  }
  public Iterator<T> getNodeIterator() {
    return nodes.valueCollection().iterator();
  }
  
  public Collection<T> getNodes() {
	  return nodes.valueCollection();
  }
  
  public int getNodeCount() {
	return nodes.size();
  }
  
  public int getClausesCount(){
    return clauses.size();
  }
  
  public Iterator<T2> getClauses(){
    //return null;
    return clauses.values().iterator();
  }

  public Iterator<T1> getEdges(){
    return connections.iterator();
  }
  public Collection<T1> getEdgesList() {
    return connections;
  }

  public void writeDimacs(File dimacsFile) throws IOException{
    FileWriter writer = new FileWriter(dimacsFile);
    Iterator<T2> clauses = getClauses();
    while(clauses.hasNext()){
      T2 c = clauses.next();
      writer.write(c.toString());
      writer.write("0\n");
    }
    writer.close();
  }
  
}
