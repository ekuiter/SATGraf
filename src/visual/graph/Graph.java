/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.graph;

import gnu.trove.map.hash.TObjectCharHashMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author zacknewsham
 */
public interface Graph <T extends Node, T1 extends Edge, T2 extends Clause>{

  public T1 getEdge(T a, T b);
  
  public T1 createEdge(T a, T b, boolean dummy);
  
  public T2 createClause(TObjectCharHashMap<T> nodes);
  
  public void connect(T a, T b, boolean dummy);
  
  public void union(T a, T b);
  
  public boolean connected(T a, T b);
  
  public T getNode(int id);
  
  public void removeNode(Node n);
  
  public T createNode(int id, String name);
  
  public T createNode(int id, String name, boolean head, boolean tail);

  public Collection<T> getNodesList();
  
  public Iterator<T> getNodes(String set);
  
  public Iterator<T> getNodeIterator();
  
  public Collection<T> getNodes();
  
  public int getNodeCount();
  
  public Iterator<T2> getClauses();
  
  public int getClausesCount();

  public Iterator<T1> getEdges();
  
  public Collection<T1> getEdgesList();
  
  public int indexOf(T node);

  public void writeDimacs(File dimacsFile) throws IOException;
}
