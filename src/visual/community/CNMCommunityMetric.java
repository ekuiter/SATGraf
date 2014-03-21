package visual.community;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.IOException;
import java.io.PrintStream;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import visual.graph.Edge;
import visual.graph.Graph;
import visual.graph.Node;
import visual.graph.UnionFind;
    
    /// Clauset-Newman-Moore community detection method.
    /// At every step two communities that contribute maximum positive value to global modularity are merged.
    /// See: Finding community structure in very large networks, A. Clauset, M.E.J. Newman, C. Moore, 2004
    public class CNMCommunityMetric implements CommunityMetric{
      private static class DoubleIntInt implements Comparable<DoubleIntInt>{
        public double val1;
        public int val2;
        public int val3;
        DoubleIntInt(double val1, int val2, int val3){
          this.val1 = val1;
          this.val2 = val2;
          this.val3 = val3;
        }

        @Override
        public int compareTo(DoubleIntInt o) {
          //int this_sum = this.val2 + this.val3;
          //int oth_sum = o.val2 + o.val3;
          if(this.equals(o)){
            return 0;
          }
          else if(val1 < o.val1 || (val1 == o.val1 && val2 < o.val2) || (val1 == o.val1 && val2 == o.val2 && val3 < o.val3)){
            return 1;
          }
          else{
            return -1;
          }
          //return this.val1 < o.val1 ? 1 : (this.val1 > o.val1 ? -1 : this_sum - oth_sum);
        }
        
        @Override
        public boolean equals(Object o){
          return this.val2 == ((DoubleIntInt)o).val2 && this.val3 == ((DoubleIntInt)o).val3;
        }

        @Override
        public int hashCode() {
          int hash = 3;
          hash = 79 * hash + this.val2;
          hash = 79 * hash + this.val3;
          return hash;
        }
      }

      private static class CommunityData {
        double DegFrac;
        TIntDoubleHashMap nodeToQ = new TIntDoubleHashMap();
        int maxQId;

        CommunityData(){
          maxQId = -1;
        }

        CommunityData(double nodeDegFrac, int outDeg){
          DegFrac = nodeDegFrac;
          maxQId = -1;
        }

        void addQ(int NId, double Q) { 
          nodeToQ.put(NId, Q);
          if (maxQId == -1 || nodeToQ.get(maxQId) < Q) { 
            maxQId = NId;
          } 
        }

        void updateMaxQ() { 
          maxQId=-1; 
          int[] nodeIDs = nodeToQ.keys();
          double maxQ = nodeToQ.get(maxQId);
          for(int i = 0; i < nodeIDs.length; i++){
            int id = nodeIDs[i];
            if(maxQId == -1 || maxQ < nodeToQ.get(id)){
              maxQId = id;
              maxQ = nodeToQ.get(maxQId);
            }
          } 
        }

        void delLink(int K) { 
          int NId=getMxQNId(); 
          nodeToQ.remove(K); 
          if (NId == K) { 
            updateMaxQ(); 
          }  
        }

        int getMxQNId() { 
          return maxQId;
        }

        double getMxQ() {
          return nodeToQ.get(maxQId); 
        }
      };
      //private ZIntObjectHashMap<CommunityData> communityData;
      private TIntObjectHashMap<CommunityData> communityData = new TIntObjectHashMap<CommunityData>();
      private TreeSet<DoubleIntInt> heap = new TreeSet<DoubleIntInt>();
      private TIntObjectHashMap<DoubleIntInt> set = new TIntObjectHashMap<DoubleIntInt>();
      private double Q = 0.0;
      private UnionFind uf = new UnionFind();
      @Override
      public double getCommunities(CommunityGraph graph) {
        //communityData = new ZIntObjectHashMap<CommunityData>(graph.getNodeCount() + 10000);
        init(graph);
        //CNMMCommunityMetric metric = new CNMMCommunityMetric();
        //metric.getCommunities(graph);
        // maximize modularity
        while (this.mergeBestQ(graph)) {
        }
        // reconstruct communities
        Iterator<CommunityNode> ns = graph.getNodeIterator();
        int community = 0;
        HashMap<Integer, Community> communities = new HashMap<Integer, Community>();
        
        // Nodes
        while(ns.hasNext()){
          CommunityNode n = ns.next();
          int r = uf.find(n);
          if(!communities.containsKey(r)){
            communities.put(r, graph.createNewCommunity(community++));
          }
          
          Community com = communities.get(r);
          com.addCommunityNode(n);
          n.setCommunity(com.getId());
        }
        
        // Edges
        Iterator<CommunityEdge> edges = graph.getEdges();
        while(edges.hasNext()) {
        	CommunityEdge e = edges.next();
        	Community com1 = graph.getCommunity(e.getStart().getCommunity());
        	Community com2 = graph.getCommunity(e.getEnd().getCommunity());
        	
        	if (com1 == com2) {
        		com1.addIntraCommunityEdge(e);
        	} else {
        		com1.addInterCommunityEdge(e);
        		com2.addInterCommunityEdge(e);
        	}
        }
        
        return this.Q;
      }

      private void init(Graph graph) {
        double M = 0.5/graph.getEdgesList().size();
        Iterator<Node> ns = graph.getNodeIterator();
        while(ns.hasNext()){
          Node n = ns.next();
          uf.add(n);
          int edges = n.getEdgesList().size();
          if(edges == 0){
            continue;
          }
          CommunityData dat = new CommunityData(M * edges, edges);
          communityData.put(n.getId(), dat);
          Iterator<Edge> es = n.getEdges();
          while(es.hasNext()){
            Edge e = es.next();
            Node dest = e.getStart() == n ? e.getEnd() : e.getStart();
            double dstMod = 2 * M * (1.0 - edges * dest.getEdgesList().size() * M);//(1 / (2 * M)) - ((n.getEdgesList().size() * dest.getEdgesList().size()) / ((2 * M) * (2 * M)));// * (1.0 - edges * dest.getEdgesList().size() * M);
            dat.addQ(dest.getId(), dstMod);
          }
          Q += -1.0 * (edges*M) * (edges*M);
          if(n.getId() < dat.getMxQNId()){
            addToHeap(createEdge(dat.getMxQ(), n.getId(), dat.getMxQNId()));
          }
        }
      }
      void addToHeap(DoubleIntInt o){
        heap.add(o);
      }
      
      DoubleIntInt createEdge(double val1, int val2, int val3){
        int hash = 3;
        hash = 79 * hash + val2;
        hash = 79 * hash + val3;
        DoubleIntInt n1 = set.get(hash);
        if(n1 != null){
          heap.remove(n1);
          //if(n1.val1 < val1){
            n1.val1 = val1;
          //}
        }
        else{
          n1 = new DoubleIntInt(val1, val2, val3);
          set.put(hash, n1);
        }
        return n1;
      }
      void removeFromHeap(Collection<DoubleIntInt> col, DoubleIntInt o){
        //set.remove(o);
        col.remove(o);
      }
      DoubleIntInt findMxQEdge() {
        while (true) {
          if (heap.isEmpty()) {
            break; 
          }
          
          DoubleIntInt topQ = heap.first();
          removeFromHeap(heap, topQ);
          //heap.remove(topQ);
          if (!communityData.containsKey(topQ.val2) || ! communityData.containsKey(topQ.val3)) {
            continue; 
          }
          if (topQ.val1 != communityData.get(topQ.val2).getMxQ() && topQ.val1 != communityData.get(topQ.val3).getMxQ()) { 
            continue; 
          }
          return topQ;
        }
        return new DoubleIntInt(-1.0, -1, -1);
      }
      boolean mergeBestQ(Graph graph) {
        DoubleIntInt topQ = findMxQEdge();
        if (topQ.val1 <= 0.0) { 
          return false; 
        }
        // joint communities
        int i = topQ.val3;
        int j = topQ.val2;
        uf.union(i, j);
        
        Q += topQ.val1;
        CommunityData datJ = communityData.get(j);
        CommunityData datI = communityData.get(i);
        datI.delLink(j);
        datJ.delLink(i);

        int[] datJData = datJ.nodeToQ.keys();
        for(int _k = 0; _k < datJData.length; _k++){
          int k = datJData[_k];
          CommunityData datK = communityData.get(k);
          double newQ = datJ.nodeToQ.get(k);
          //if(datJ.nodeToQ.containsKey(i)){
          //  newQ = datJ.nodeToQ.get(i);
          //}
          if (datI.nodeToQ.containsKey(k)) { 
            newQ = newQ + datI.nodeToQ.get(k);
            datK.delLink(i);
          }     // K connected to I and J
          else if(datK != null){ 
            newQ = newQ - 2 * datI.DegFrac * datK.DegFrac;
          }  // K connected to J not I
          datJ.addQ(k, newQ);
          if(datK != null){
            datK.addQ(j, newQ);
          }
          addToHeap(createEdge(newQ, Math.min(j, k), Math.max(j, k)));
        }

        int[] datIData = datI.nodeToQ.keys();
        for(int _k = 0; _k < datIData.length; _k++){
          int k = datIData[_k];
          if (!datJ.nodeToQ.containsKey(k)) { // K connected to I not J
            CommunityData datK = communityData.get(k);
            if(datK != null){
              double newQ = datI.nodeToQ.get(k) - 2 * datJ.DegFrac * datK.DegFrac; 
              datJ.addQ(k, newQ);
              datK.delLink(i);
              datK.addQ(j, newQ);
              addToHeap(createEdge(newQ, Math.min(j, k), Math.max(j, k)));
            }
          }
        } 
        datJ.DegFrac += datI.DegFrac; 
        if (datJ.nodeToQ.isEmpty()) { 
          communityData.remove(j); 
        } // isolated community (done)
        communityData.remove(i);
        return true;
      }
      
      public static double getQ(String file, PrintStream output) throws IOException{
        CommunityGrapher grapher = new CommunityGrapher(file, "ol","kk",new HashMap<String, String>());
        grapher.generateGraph();
        CNMCommunityMetric cnm = new CNMCommunityMetric();
        double Q = cnm.getCommunities(grapher.getGraph());
        Iterator<CommunityNode> nodes = grapher.getGraph().getNodeIterator();
        output.printf("0 %f\n", Q);
        while(nodes.hasNext()){
          CommunityNode node = nodes.next();
          output.printf("%d %d\n", node.getId(), node.getCommunity());
        }
        return Q;
      }
      public static void main(String[] args) throws IOException{
        if(args == null || args.length < 1){
          args = new String[]{"formula/satcomp/dimacs/fiasco.dimacs"};
        }
        getQ(args[0], System.out);
      }
    }
