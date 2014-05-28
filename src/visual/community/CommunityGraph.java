/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.community;

import gnu.trove.map.hash.TIntIntHashMap;
import java.util.Collection;
import java.util.Iterator;

import visual.graph.Clause;
import visual.graph.Graph;

/**
 *
 * @author zacknewsham
 */
public interface CommunityGraph extends Graph<CommunityNode, CommunityEdge, Clause>{
  public Community getCommunity(int community);
  
  public int getCommunitySize(int community);
  
  public Collection<Community> getCommunities();
  
  public int getNumberCommunities();
  
  public Community createNewCommunity(int communityId);
 
  public int getLargestCommunity();
  
  public Iterator<CommunityEdge> getDummyEdges();
  
  public void removeEdge(CommunityEdge e);
  
  public void setVariableDistribution(TIntIntHashMap dist);
  
  public TIntIntHashMap getVariableDistribution();
  
  public CommunityGraph to3CNF();
}
