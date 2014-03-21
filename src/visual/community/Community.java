/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.community;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class Community extends CommunityGraphAdapter{
  	// Constants
	private ArrayList<CommunityEdge> intraCommunityEdges = new ArrayList<CommunityEdge>();
	private ArrayList<CommunityEdge> interCommunityEdges = new ArrayList<CommunityEdge>();
	//private HashMap<Integer, Integer> edgesCount = new HashMap<>();
    protected HashSet<CommunityNode> communityNodes = new HashSet<CommunityNode>();
	private int id;
	
	public Community(int id) {
		this.id = id;
	}
	
    public int getMaxCount(){
      /*Iterator<Integer> ec = edgesCount.values().iterator();
      int max = 0;
      while(ec.hasNext()){
        int i = ec.next();
        if(i > max){
          max = i;
        }
      }
      return max;*/
      return 0;
    }
    public int getInterEdgesTo(Community c){
      return 0;
      //return edgesCount.get(c.getId());
    }
    public int indexOf(CommunityNode node){
      Iterator<CommunityNode> nI = communityNodes.iterator();
      int i = 0;
      while(nI.hasNext()){
        CommunityNode n1 = nI.next();
        if(n1 == node){
          return i;
        }
        i++;
      }
      return -1;
    }
	public void addIntraCommunityEdge(CommunityEdge edge) {
		intraCommunityEdges.add(edge);
	}
	
	public void addInterCommunityEdge(CommunityEdge edge) {
		interCommunityEdges.add(edge);
        int c = edge.getStart().getCommunity() == this.getId() ? edge.getEnd().getCommunity() : edge.getStart().getCommunity();
        //int a = edgesCount.get(c);
        //edgesCount.put(c, a += 1);
	}
	
	public void addCommunityNode(CommunityNode node) {
        communityNodes.add(node);
	}
	
	public Collection<CommunityEdge> getIntraCommunityEdges() {
		return intraCommunityEdges;
	}
	
	public Collection<CommunityEdge> getInterCommunityEdges() {
		return interCommunityEdges;
	}
	
	public Collection<CommunityNode> getCommunityNodes() {
		return communityNodes;
	}
	
	public int getId() {
		return id;
	}

	public int getSize() {
		return communityNodes.size();
	}

    @Override
    public Community getCommunity(int community) {
      return this;
    }

    @Override
    public int getCommunitySize(int community) {
      return this.getSize();
    }

    @Override
    public Collection<Community> getCommunities() {
      ArrayList<Community> ret = new ArrayList<Community>();;
      ret.add(this);
      return ret;
    }

    @Override
    public int getNumberCommunities() {
      return 1;
    }

    @Override
    public int getLargestCommunity() {
      return 0;
    }

    public Iterator<CommunityEdge> getDummyEdges(){
      return new ArrayList<CommunityEdge>().iterator();
    }
    
    @Override
    public CommunityNode getNode(int id) {
      
      /*List l = new ArrayList(communityNodes);
      Collections.sort(l, new Comparator<CommunityNode>(){

        @Override
        public int compare(CommunityNode o1, CommunityNode o2) {
          
        }
        
      });*/
      Iterator<CommunityNode> nodes = getNodeIterator();
      while(nodes.hasNext()){
        CommunityNode next = nodes.next();
        if(next.getId() == id){
          return next;
        }
      }
      return null;
    }

    @Override
    public Collection<CommunityNode> getNodesList() {
      return communityNodes;
    }

    @Override
    public Iterator<CommunityNode> getNodes(String set) {
      return communityNodes.iterator();
    }

    @Override
    public Iterator<CommunityNode> getNodeIterator() {
      return communityNodes.iterator();
    }

    @Override
    public Collection<CommunityNode> getNodes() {
      return communityNodes;
    }

    @Override
    public int getNodeCount() {
      return communityNodes.size();
    }

    @Override
    public Iterator<CommunityEdge> getEdges() {
      return getEdgesList().iterator();
    }

    @Override
    public Collection<CommunityEdge> getEdgesList() {
      ArrayList<CommunityEdge> e = new ArrayList(interCommunityEdges);
      e.addAll(intraCommunityEdges);
      return e;
    }

    @Override
    public boolean connected(CommunityNode a, CommunityNode b) {
      return true;
    }
	
}
