package visual.community.drawing_algorithms;

import java.awt.Rectangle;
import java.util.Collection;

import visual.community.Community;
import visual.community.CommunityGraph;
import visual.community.CommunityNode;

public abstract class AbstractPlacer implements CommunityPlacer{
	
	CommunityGraph graph;
	
	public AbstractPlacer(CommunityGraph graph){
	    this.graph = graph;
    }
    
	
	public int getCommunityAtXY(int x, int y){
	    int community = 0;
	    Collection<CommunityNode> nodes = getCommunityNodes(community);
	    while(nodes != null){
	      int cx = getCommunityX(community);
	      int cy = getCommunityY(community);
	      int cw = getCommunityWidth(community);
	      int ch = getCommunityHeight(community);
	      Rectangle r = new Rectangle(cx, cy, cw, ch);
	      if(r.contains(x, y)){
	        return community;
	      }
	      nodes = getCommunityNodes(++community);
	    }
	    return -1;
	  }
	
	public Collection<CommunityNode> getCommunityNodes(int community){
		Community com = graph.getCommunity(community);
		return (com != null ? com.getCommunityNodes() : null);
	}
}
