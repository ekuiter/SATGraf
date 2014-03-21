/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.community;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectCharHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
}
