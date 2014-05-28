/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.community;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectCharHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import test.CommunityClauses;
import visual.graph.AbstractGraph;
import visual.graph.Clause;
import visual.graph.Node;

/**
 *
 * @author zacknewsham
 */
public class ConcreteCommunityGraph extends AbstractGraph<CommunityNode, CommunityEdge, Clause> implements CommunityGraph{
  private final TIntObjectHashMap<Community> communities = new TIntObjectHashMap<Community>();
  private ArrayList<CommunityEdge> dummyEdges = new ArrayList<CommunityEdge>();
  private int edge_count;
  private TIntIntHashMap varDist = new TIntIntHashMap();
  public Community getCommunity(int community){
    return communities.get(community);
  }
  
  public int getCommunitySize(int community){
    return communities.get(community).getSize();
  }
  
  public Collection<Community> getCommunities() {
	  return communities.valueCollection();
  }
  
  public int getNumberCommunities(){
	  return communities.size();
  }
  
  public Community createNewCommunity(int communityId) {
    Community com = communities.get(communityId);
    if(com == null){
	  com = new Community(communityId);
	  communities.put(communityId, com);
    }
    return com;
  }
  
  public void removeEdge(CommunityEdge e){
    dummyEdges.remove(e);
    connections.remove(e.getId());
  }
          
  public Iterator<CommunityEdge> getDummyEdges(){
    return dummyEdges.iterator();
  }
  
  @Override
  public CommunityEdge createEdge(CommunityNode a, CommunityNode b, boolean dummy){
    CommunityEdge e = a.getEdge(b);

    if(e != null){
      return e;
    }
    e = new CommunityEdge(a, b, dummy);
    e.id = ++edge_count;
    connections.add(e);
    if(dummy){
      dummyEdges.add(e);
    }
    return e;
  }
  
  @Override
  public CommunityNode createNode(int id, String name, boolean is_head, boolean is_tail){
    synchronized(nodes){
        varDist.put(id, varDist.get(id) + 1);
      if(nodes.containsKey(id)){
        CommunityNode node = nodes.get(id);
        if(name != null){
          node.setName(name);
        }
        uf.add(node);
        return node;
      }
      else{
        CommunityNode n = new CommunityNode(id, name, is_head, is_tail);
        nodes.put(id, n);
        uf.add(n);
        return n;
      }
    }
  }
  

  public int getLargestCommunity(){
    int largest = 0;
    getCommunity(0);
    Iterator<Community> comms = communities.valueCollection().iterator();
    while(comms.hasNext()){
      int size = comms.next().getSize();
      if(size > largest){
        largest = size;
      }
    }
    return largest;
  }

  @Override
  public Clause createClause(TObjectCharHashMap<CommunityNode> nodes) {
    Clause c = new Clause(nodes);
    Clause c1 = clauses.get(c);
    if(c1 != null){
      return c1;
    }
    else{
      clauses.put(c,c);
      return c;
    }
  }
  
  
  @Override
  public void setVariableDistribution(TIntIntHashMap dist){
      this.varDist = dist;
  }
  
  @Override
  public TIntIntHashMap getVariableDistribution(){
      return this.varDist;
  }
  
  public int getMaxLiteral(){
      int max = 0;
      for(Node n : getNodes()){
          if(n.getId() > max){
              max = n.getId();
          }
      }
      return max;
  }
  
  @Override
  public CommunityGraph to3CNF(){
      ConcreteCommunityGraph graph = new ConcreteCommunityGraph();
      int maxLit = getMaxLiteral();
      Iterator<Clause> clauses = getClauses();
      int found = 0;
      while(clauses.hasNext()){
        Clause c = clauses.next();
        Iterator<Node> nodes = c.getNodes();
        TObjectCharHashMap<CommunityNode> newNodes = new TObjectCharHashMap<>();
          if(c.size() > 3){
            int varCount = 0;
            int limit = 2;
            int processed = 0;
            while(nodes.hasNext()){
                Node n = nodes.next();
                if(limit == 1){
                    newNodes.put(graph.createNode(maxLit, ""), '0');
                }
                newNodes.put(graph.createNode(n.getId(), n.getName()), c.getValue(n) ? '1' : '0');
                varCount++;
                processed++;
                if(varCount == limit){
                    if(processed != c.size() - 1){
                        newNodes.put(graph.createNode(++maxLit, ""), '1');
                    }
                    else{
                        Node n1 = nodes.next();
                        newNodes.put(graph.createNode(n1.getId(), ""), c.getValue(n1) ? '1' : '0');
                    }
                    graph.createEdge((CommunityNode)newNodes.keys()[0], (CommunityNode)newNodes.keys()[1], false);
                    graph.createEdge((CommunityNode)newNodes.keys()[1], (CommunityNode)newNodes.keys()[2], false);
                    graph.createEdge((CommunityNode)newNodes.keys()[0], (CommunityNode)newNodes.keys()[2], false);
                    Clause c1 = graph.createClause(newNodes);
                    newNodes = new TObjectCharHashMap<>();
                    limit = 1;
                    varCount = 0;
                }
            }
          }
          else{
            while(nodes.hasNext()){
                Node n = nodes.next();
                newNodes.put(graph.createNode(n.getId(), n.getName()), c.getValue(n) ? '1' : '0');
            }
            if(newNodes.size() == 2){
                graph.createEdge((CommunityNode)newNodes.keys()[0], (CommunityNode)newNodes.keys()[1], false);
            }
            Clause c1 = graph.createClause(newNodes);
          }
      }
      
      return graph;
  }
}
