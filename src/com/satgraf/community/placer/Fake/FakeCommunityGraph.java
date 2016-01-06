package com.satgraf.community.placer.Fake;
import com.satgraf.community.placer.GridKKPlacer;
import com.satlib.community.Community;
import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityGraphAdapter;
import com.satlib.community.CommunityNode;
import com.satlib.graph.UnionFind;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class FakeCommunityGraph extends CommunityGraphAdapter {
    private TIntObjectHashMap<CommunityNode> nodes = new TIntObjectHashMap<>();
    private HashMap<CommunityEdge,CommunityEdge> edges = new HashMap<>();
    private UnionFind uf = new UnionFind();
    private CommunityGraph graph;
    
    
    public FakeCommunityGraph(CommunityGraph g){
      this.graph = g;
      Iterator<Community> communities = g.getCommunities().iterator();
      while(communities.hasNext()){
        Community c = communities.next();
        FakeCommunityNode fcn = this.createNode(c);
        uf.add(fcn);
        Iterator<CommunityEdge> edges = new ArrayList<CommunityEdge>(c.getInterCommunityEdges()).iterator();
        while(edges.hasNext()){
          CommunityEdge e = edges.next();
          Community otherC = g.getCommunity(c.getId() == e.getEnd().getCommunity() ? e.getStart().getCommunity() : e.getEnd().getCommunity());
          FakeCommunityNode fcn2 = this.createNode(otherC);
          uf.add(fcn2);
          
          FakeCommunityEdge fce = (FakeCommunityEdge)this.createEdge(fcn, fcn2, false);
          
          fcn.addEdge(fce);
        }
      }
    }
    public Iterator<CommunityEdge> getDummyEdges(){
      return new ArrayList<CommunityEdge>().iterator();
    }

    @Override
    public CommunityEdge createEdge(CommunityNode a, CommunityNode b, boolean dummy) {
      FakeCommunityEdge fce = new FakeCommunityEdge(a, b, dummy);
      uf.union(a, b);
      a.addEdge(fce);
      b.addEdge(fce);
      if(a.getCommunity() != b.getCommunity()){
        graph.getCommunity(a.getCommunity()).addInterCommunityEdge(fce);
        graph.getCommunity(b.getCommunity()).addInterCommunityEdge(fce);
      }
      if(edges.containsKey(fce)){
        edges.get(fce).setWeight(edges.get(fce).getWeight() + 30);
        return edges.get(fce);
      }
      else{
        edges.put(fce, fce);
        return fce;
      }
    }
    @Override
    public CommunityEdge connect(CommunityNode a, CommunityNode b, boolean dummy) {
      CommunityEdge e = createEdge(a, b, dummy);
      union(a, b);
      
      return e;
    }

    @Override
    public void union(CommunityNode a, CommunityNode b) {
      uf.union(a, b);
    }

    @Override
    public boolean connected(CommunityNode a, CommunityNode b) {
      return uf.find(a) == uf.find(b);
    }

    @Override
    public CommunityNode getNode(int id) {
      return nodes.get(id);
    }

    private FakeCommunityNode createNode(Community c){
      FakeCommunity fc = new FakeCommunity(c);
      FakeCommunityNode fcn = new FakeCommunityNode(fc, this);
      //fcn.setSize(FakeCommunityNode.communityWidths.get(c.getId()));
      fc.setFakeCommunityNode(fcn);
      if(!nodes.containsValue(fcn)){
        nodes.put(c.getId(), fcn);
      }
      fcn = (FakeCommunityNode)nodes.get(c.getId());
      return fcn;
    }

    @Override
    public Collection<CommunityNode> getNodesList() {
      return nodes.valueCollection();
    }

    @Override
    public Collection<CommunityNode> getNodes(String set) {
      return nodes.valueCollection();
    }


    @Override
    public Collection<CommunityNode> getNodes() {
      return nodes.valueCollection();
    }

    @Override
    public int getNodeCount() {
      return nodes.size();
    }

    @Override
    public Collection<CommunityEdge> getEdges() {
      return edges.values();
    }

  }