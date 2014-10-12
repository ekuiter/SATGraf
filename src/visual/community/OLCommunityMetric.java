/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.community;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author zacknewsham
 */

//////////////////////////////////////////////////////////////////
// OnlineCommunityDetection                                     //
//////////////////////////////////////////////////////////////////
// Author       : Wangsheng Zhang  (zws10@zju.edu.cn)			//
// Location     : Zhejiang University						    //
// Time         : January-August 2013							//
//////////////////////////////////////////////////////////////////
public class OLCommunityMetric implements CommunityMetric{
  private TIntObjectHashMap<TIntHashSet> aadjht = new TIntObjectHashMap<TIntHashSet>();
  private TIntIntHashMap anodecommht = new TIntIntHashMap();
  private int commidx;
  private int m;
  private TIntIntHashMap acommdeght = new TIntIntHashMap();
  private TIntObjectHashMap<TIntIntHashMap> anodecommdeght = new TIntObjectHashMap<TIntIntHashMap>();
  private TIntObjectHashMap<TIntIntHashMap> acommnodeht = new TIntObjectHashMap<TIntIntHashMap>();  
  
  private int GetNodeToCommDeg(int i, int k)
  {
      int res = 0;
      TIntIntHashMap acommht = anodecommdeght.get(i);
      if (acommht.containsKey(k))
      {
          res = acommht.get(k);
      }
      return res;
  }
  private long DeltaQMoveNode(int i, int a, int b)
  {
      return 2 * (long)m * (long)(GetNodeToCommDeg(i, b) - GetNodeToCommDeg(i, a)) + (long)(GetCommDeg(a) - GetCommDeg(b) - GetNodeDeg(i)) * (long)GetNodeDeg(i);
  }

  private void ReduceNodeToCommDeg(int i, int k)
  {
      TIntIntHashMap acommht = anodecommdeght.get(i);
      acommht.put(k, acommht.get(k) - 1);
  }
  private void SetNewNodeToCommDeg(int i, int k)
  {
      TIntIntHashMap acommht = new TIntIntHashMap();
      acommht.put(k, 1);
      anodecommdeght.put(i, acommht);
  }

  private void SetExistNodeToCommDeg(int i, int k)
  {
      TIntIntHashMap acommht = anodecommdeght.get(i);
      acommht.put(k, 1);
  }
  private void AddNodeToCommDeg(int i, int k)
  {
      TIntIntHashMap acommht = anodecommdeght.get(i);
      if (acommht.containsKey(k))
      {
          acommht.put(k, acommht.get(k) + 1);
      }
      else
      {
          acommht.put(k, 1);
      }
  }

  private int GetCIdxOfNode(int label){
      return anodecommht.get(label);
  }

  private void SetCIdxOfExistNode(int label, int cidx)
  {
      anodecommht.put(label,cidx);
  }

  private void SetCIdxOfNewNode(int label, int cidx)
  {
      anodecommht.put(label, cidx);
  }

  private int GetNodeDeg(int label)
  {
      return aadjht.get(label).size();
  }

  private int GetCommDeg(int cidx)
  {
      return acommdeght.get(cidx);
  }

  private void SetNewCommDeg(int cidx, int deg)
  {
      acommdeght.put(cidx, deg);
  }

  private void AddCommDeg(int cidx, int deg)
  {
      acommdeght.put(cidx, acommdeght.get(cidx) + deg);
  }

  private void ReduceCommDeg(int cidx, int deg)
  {
      acommdeght.put(cidx, acommdeght.get(cidx) - deg);
  }

  private void SetCommNodeHt()
  {
      acommnodeht = new TIntObjectHashMap<TIntIntHashMap>();
      for(int label: anodecommht.keys()){
        int cidx = anodecommht.get(label);
        if (!acommnodeht.containsKey(cidx)){
          acommnodeht.put(cidx, new TIntIntHashMap());
        }
        TIntIntHashMap anodeht = acommnodeht.get(cidx);
        anodeht.put(label, 0);
      }
  }
  
  private void AddNode(int id){
    if (!aadjht.containsKey(id))
    {
        aadjht.put(id, new TIntHashSet());
    }
  }
  private boolean AddEdge(int olabel, int dlabel)
  {
      boolean isnewedge = false;
      if (olabel != dlabel)
      {
          if (!aadjht.containsKey(olabel))
          {
              aadjht.put(olabel, new TIntHashSet());
              isnewedge = true;
          }
          if (!aadjht.containsKey(dlabel))
          {
              aadjht.put(dlabel, new TIntHashSet());
              isnewedge = true;
          }
          TIntHashSet oadjht = aadjht.get(olabel);
          if (!oadjht.contains(dlabel))
          {
              oadjht.add(dlabel);
              TIntHashSet dadjht = aadjht.get(dlabel);
              dadjht.add(olabel);
              isnewedge = true;
          }
      }
      return isnewedge;
  }

  private boolean IsExistNode(int label)
  {
      return anodecommht.containsKey(label);
  }

  private void NewEvent(int olabel, int dlabel)
  {
      commidx++;
      SetCIdxOfNewNode(olabel, commidx);
      SetCIdxOfNewNode(dlabel, commidx);
      SetNewCommDeg(commidx, 2);
      SetNewNodeToCommDeg(olabel, commidx);
      SetNewNodeToCommDeg(dlabel, commidx);
  }

  private void JoinEvent(int olabel, int dlabel)
  {
      int dcidx = GetCIdxOfNode(dlabel);
      SetCIdxOfNewNode(olabel, dcidx);
      AddCommDeg(dcidx, 2);
      SetNewNodeToCommDeg(olabel, dcidx);
      AddNodeToCommDeg(dlabel, dcidx);
  }

  private void SplitEvent(int olabel, int dlabel)
  {
      int dcidx = GetCIdxOfNode(dlabel);
      commidx++;
      SetCIdxOfNewNode(olabel, commidx);
      AddCommDeg(dcidx, 1);
      SetNewCommDeg(commidx, 1);
      SetNewNodeToCommDeg(olabel, dcidx);
      SetExistNodeToCommDeg(dlabel, commidx);
  }

  private void DenseEvent(int olabel, int dlabel, int ocidx, int dcidx)
  {
      AddCommDeg(ocidx, 2);
      AddNodeToCommDeg(olabel, dcidx);
      AddNodeToCommDeg(dlabel, ocidx);
  }

  private void LinkEvent(int olabel, int dlabel, int ocidx, int dcidx)
  {
      AddCommDeg(dcidx, 1);
      AddCommDeg(ocidx, 1);
      AddNodeToCommDeg(olabel, dcidx);
      AddNodeToCommDeg(dlabel, ocidx);
  }

  private void MoveEvent(int olabel, int ocidx, int dcidx)
  {
      SetCIdxOfExistNode(olabel, dcidx);
      ReduceCommDeg(ocidx, GetNodeDeg(olabel));
      AddCommDeg(dcidx, GetNodeDeg(olabel));
      TIntHashSet oadjht = aadjht.get(olabel);
      for(int oadjlabel: oadjht.toArray())
      {
          AddNodeToCommDeg(oadjlabel, dcidx);
          ReduceNodeToCommDeg(oadjlabel, ocidx);
      }
  }

  private boolean IsJoin(int deg)
  {
      int p =(int)(Math.random() * deg);
      if (p >= 1)
      {
          return true;
      }
      else
      {
          return false;
      }
  }
  
  @Override
  public double getCommunities(CommunityGraph graph) {
    Iterator<CommunityNode> nodes = graph.getNodes().iterator();
    while(nodes.hasNext()){
      CommunityNode node = nodes.next();
      AddNode(node.getId());
    }
    Iterator<CommunityEdge> edges = graph.getEdges();
    while(edges.hasNext()){
      CommunityEdge e = edges.next();
      m++;
      int olabel = e.getStart().getId();
      int dlabel = e.getEnd().getId();
      boolean isnewedge = AddEdge(olabel, dlabel);
      if(isnewedge){
        if (!IsExistNode(olabel) && !IsExistNode(dlabel)){
            NewEvent(olabel, dlabel);
        }
        else
        {
          if (!IsExistNode(olabel))
          {
              int ddeg = GetNodeDeg(dlabel);
              if (IsJoin(ddeg + 1))
              {
                  JoinEvent(olabel, dlabel);
              }
              else
              {
                  SplitEvent(olabel, dlabel);
              }
          }
          else
          {
            if (!IsExistNode(dlabel))
            {
              int odeg = GetNodeDeg(olabel);
              if (IsJoin(odeg + 1))
              {
                JoinEvent(dlabel, olabel);
              }
              else
              {
                SplitEvent(dlabel, olabel);
              }
            }
            else
            {
              int ocidx = GetCIdxOfNode(olabel);
              int dcidx = GetCIdxOfNode(dlabel);
              if (ocidx != dcidx)
              {
                LinkEvent(olabel, dlabel, ocidx, dcidx);

                long qO2D = DeltaQMoveNode(olabel, ocidx, dcidx);
                long qD2O = DeltaQMoveNode(dlabel, dcidx, ocidx);

                if (qD2O > 0)
                {
                  if (qO2D > qD2O)
                  {
                    MoveEvent(olabel, ocidx, dcidx);
                  }
                  else
                  {
                    MoveEvent(dlabel, dcidx, ocidx);
                  }
                }
                else
                {
                  if (qO2D > 0)
                  {
                    MoveEvent(olabel, ocidx, dcidx);
                  }
                }
              }
              else
              {
                DenseEvent(olabel, dlabel, ocidx, dcidx);
              }
            }
          }
        }
      }
    }
    SetCommNodeHt();
                
    double modularity = 0.0;
      int comm_count = 0;
    for (int cidx: acommnodeht.keys())
    {
      double cmnq = 0.0;
      int cmnq1 = 0;
      int cmnq2 = 0;
      TIntIntHashMap anodeht = acommnodeht.get(cidx);
      TIntIntHashMap comms = new TIntIntHashMap();
      int com = 0;
      for (int label : anodeht.keys())
      {
        if(comms.containsKey(anodecommht.get(label) - 1)){
          com = comms.get(anodecommht.get(label) - 1);
        }
        else{
          com = comm_count++;
          comms.put(anodecommht.get(label) - 1, com);
        }
        Community c = graph.getCommunity(com);
        if(c == null){
          c = graph.createNewCommunity(com);
        }
        graph.getNode(label).setCommunity(com);
        c.addCommunityNode(graph.getNode(label));
          TIntHashSet ladjht = aadjht.get(label);
          for (int adjlabel : ladjht.toArray())
          {
              if (anodeht.containsKey(adjlabel))
              {
                  cmnq1++;
              }
          }
      }
      cmnq1 = cmnq1 / 2;
      for (int label : anodeht.keys())
      {
          cmnq2 = cmnq2 + GetNodeDeg(label);
      }
      cmnq = (double)cmnq1 / (double)m - Math.pow(((double)cmnq2 / (2.0 * (double)m)), 2.0);
      modularity = modularity + cmnq;
    }
    for(int n : aadjht.keys()){
      CommunityNode node = graph.getNode(n);
      if(node.getCommunity() == -1){
        Community c = graph.createNewCommunity(comm_count++);
        c.addCommunityNode(node);
        node.setCommunity(comm_count - 1);
      }
    }
    edges = graph.getEdges();
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
    return modularity;
  }
  
      
  public static double getQ(String file, PrintStream output) throws IOException{
    CommunityGrapher grapher = new CommunityGrapher(file, "ol", "kk", new HashMap<String, String>());
    grapher.generateGraph();
    CommunityMetric cnm = new OLCommunityMetric();
    double Q = cnm.getCommunities(grapher.getGraph());
    Iterator<CommunityNode> nodes = grapher.getGraph().getNodeIterator();
    output.printf("0 %d,%d,%d,%f\n" , grapher.getGraph().getNodeCount(), grapher.getGraph().getClausesCount(), grapher.getGraph().getCommunities().size(), Q);
    while(nodes.hasNext()){
      CommunityNode node = nodes.next();
      output.printf("%d %d\n", node.getId(), node.getCommunity());
    }
    return Q;
  }
  
  public static void main(String[] args) throws IOException{
    if(args.length < 1){
      args = new String[]{"/media/SAT/sat2013/app/SATBench/satchal12-selected/Application_SAT+UNSAT/SATChallenge2012_Application/SAT_Competition_2009_unselected/application/SAT09/APPLICATIONS/crypto/desgen/gss-18-s100.cnf"};
    }
    getQ(args[0], System.out);
  }
}
