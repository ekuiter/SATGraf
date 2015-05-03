/*$$
 * packages uchicago.src.*
 * Copyright (c) 1999, Trustees of the University of Chicago
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with
 * or without modification, are permitted provided that the following
 * conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *   Neither the name of the University of Chicago nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE TRUSTEES OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Nick Collier
 * nick@src.uchicago.edu
 *
 * packages cern.jet.random.*
 * Copyright (c) 1999 CERN - European Laboratory for Particle
 * Physics. Permission to use, copy, modify, distribute and sell this
 * software and its documentation for any purpose is hereby granted without
 * fee, provided that the above copyright notice appear in all copies
 * and that both that copyright notice and this permission notice appear in
 * supporting documentation. CERN makes no representations about the
 * suitability of this software for any purpose. It is provided "as is"
 * without expressed or implied warranty.
 *
 * Wolfgang Hoschek
 * wolfgang.hoschek@cern.ch
 * @author Hacked by Eytan Adar for Guess classes
 *$$*/
package com.satgraf.graph.placer;

import com.satlib.community.Community;
import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityNode;
import com.satlib.graph.Edge;
import com.satlib.graph.Node;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;


/**
 * Performs various common operations on passed networks and returns them,
 * and/or returns a statistic on a passed network and returns the result.
 * Please note that these may be fairly "naive" algorithm implementations,
 * and no guarantees are made about the accuracy of the statistics.  The
 * intention is that these may be used for "on the fly" qualitative evaluation
 * of a model, but real network statistics should be done with more serious
 * software such as UCINET or Pajek.<p>
 *
 * ALL THE METHODS CAN BE CONSIDERED BETA AND SHOULD ONLY BE USED FOR
 * "ON THE FLY" CALCULATIONS. ACTUAL NETWORK STATISTICS SHOULD BE DONE
 * WITH DEDICATED NETWORK ANALYSIS SOFTWARE, SUCH AS PAJEK OR UCINET.
 *
 * @author Skye Bender-deMoll
 * @version $Revision: 1.1 $ $Date: 2006/06/01 18:00:34 $
 */
public class NetUtilities {
  static CommunityGraph graph;
  public static final Comparator<ArrayList<CommunityNode>> SIZE = new Comparator<ArrayList<CommunityNode>>() {
	
	 @Override
	public int compare(ArrayList<CommunityNode> a, ArrayList<CommunityNode> b) {
		 return b.size() - a.size();
	}
	 
  };

  /**
   * Returns an ArrayList of length equal to the number of components in the
   * graph, each entry of which is an ArrayList of the nodes in that component.
   * @param nodes the network in which components will be counted
   */
  public static ArrayList<ArrayList<CommunityNode>> getComponents(Collection<CommunityNode> nodes) {
    ComponentFinder finder = new ComponentFinder();
    ArrayList<ArrayList<CommunityNode>> comps = finder.findComponents(nodes);
    Collections.sort(comps, SIZE);
    
    return comps;
  }

public static SymettricMatrix getAllShortPathMatrix(Collection<Node> nodes) {
    int nNodes = nodes.size ();
    SymettricMatrix distMatrix = new SymettricMatrix(nNodes);
    distMatrix.assign(Double.POSITIVE_INFINITY);

    // index of nodes to there index in the
    TObjectIntHashMap<Node> nodeIndexer = new TObjectIntHashMap<>();
    ArrayList<Double> priorityList = new ArrayList<>();
    ArrayList<Node> nodeQueue = new ArrayList<>();
    HashSet<Node> checkedNodes = new HashSet<>();

    Iterator<Node> it = nodes.iterator();
    int w = 0;
    
    Node[] nds = new Node[nNodes];

    while(it.hasNext()) {
    	Node work = it.next();
    	nodeIndexer.put(work, new Integer(w));
    	nds[w] = work;
    	w++;
    }

    for (int i = 0; i < nNodes; i++) {
      checkedNodes.clear();
      priorityList.clear();
      nodeQueue.clear();
      
      //find paths to all nodes connected to i
      Node iNode = nds[i];
      distMatrix.setValue(i, i, 0.0);
      checkedNodes.add(iNode);
      priorityList.add(0.0);
      nodeQueue.add(iNode);
      
      while (nodeQueue.size () > 0) {
        //find node with smallest priority value
        double fringeNodePrior = Double.POSITIVE_INFINITY;
        int fringeNodeIndex = Integer.MAX_VALUE;
        for (int n = 0; n < priorityList.size (); n++) {
          if (priorityList.get(n) < fringeNodePrior) {
            fringeNodeIndex = n;
            fringeNodePrior = priorityList.get(fringeNodeIndex);
          }
        }
        
        Node fringeNode = nodeQueue.get(fringeNodeIndex);
        double fringeNodeDist = priorityList.get(fringeNodeIndex);
        nodeQueue.remove(fringeNodeIndex);
        priorityList.remove(fringeNodeIndex);
        checkedNodes.add(fringeNode);

        //put distance in matrix
        int index = nodeIndexer.get(fringeNode);
        distMatrix.setValue(i, index, fringeNodeDist);
        
        //loop over its edges, adding nodes to queue
        Iterator<CommunityEdge> edgeEnum = fringeNode.getConnections();
        while(edgeEnum.hasNext()) {
          Edge edge = edgeEnum.next();
          Node workNode = edge.getOpposite(fringeNode);
          if (!checkedNodes.contains(workNode)) {
        	  double workNodeDist = fringeNodeDist + edge.getWeight();
        	  int prevDistIndex = nodeQueue.indexOf(workNode);
        	  if (prevDistIndex >= 0) {
        		  //check if it has a lower distance
        		  if (priorityList.get(prevDistIndex) > workNodeDist) {
        			  //repace it with new value
        			  priorityList.set(prevDistIndex, workNodeDist);
        		  }
        	  } else {
        		  //add the worknode to the queue with priority
        		  priorityList.add(workNodeDist);
        		  nodeQueue.add(workNode);
        	  }
          }
        }
      }
    }

    return distMatrix;
  }
  
 // class is constructed to make possible the use of recursive
 // tree search methods within a static context of netUtilities
 private static class ComponentFinder {
     
     TIntArrayList checked = new TIntArrayList();
     ArrayList<CommunityNode> currentComp = new ArrayList<CommunityNode>();
     ArrayList<ArrayList<CommunityNode>> currentComps = new ArrayList<ArrayList<CommunityNode>>();
     
     public ArrayList<ArrayList<CommunityNode>> findComponents(Collection<CommunityNode> nodeList) {
		  checked.clear();
		  
		  Iterator<CommunityNode> it = nodeList.iterator();
		  while(it.hasNext()) {
			  CommunityNode iNode = it.next();
		      if (!checked.contains(iNode.getCommunity())) {
				  currentComp = new ArrayList<CommunityNode>();
				  currentComps.add(currentComp);
				  //puts iNode and all connected nodes into currentComponent
				  findConnectedNodes(iNode);
		      }
		  }
	  
	 	return currentComps;
     }
     
     // recursively calls itself to find all nodes connected to iNode
     private void findConnectedNodes(CommunityNode iNode) {
    	  Community com = graph.getCommunity(iNode.getCommunity()); 
          
		  checked.add(com.getId());
		  currentComp.addAll(com.getCommunityNodes());
		  
		  Iterator<CommunityEdge> it = com.getInterCommunityEdges().iterator();
		  while(it.hasNext()) {
			  CommunityEdge edge = it.next();
			  
			  // Find the community that hasn't been checked
			  if (!checked.contains(edge.getStart().getCommunity()))
				  findConnectedNodes(edge.getStart());
			  else if (!checked.contains(edge.getEnd().getCommunity()))
				  findConnectedNodes(edge.getEnd());
		  }
     }
     
 }
}