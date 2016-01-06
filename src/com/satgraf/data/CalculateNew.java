/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.data;

import com.satlib.CSVModel;
import static com.satlib.ForceInit.forceInit;
import com.satlib.community.AverageShortestPathMetric;
import com.satlib.community.Community;
import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityGraphFactory;
import com.satlib.community.CommunityNode;
import com.satlib.community.ConcreteCommunityGraph;
import com.satlib.community.DisjointGraphs;
import com.satlib.community.OLCommunityMetric;
import com.satlib.graph.GraphFactoryFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author zacknewsham
 */
public class CalculateNew {
    static{
        forceInit(com.satlib.community.DimacsCommunityGraphFactory.class);
        forceInit(com.satlib.community.OLCommunityMetric.class);
    }
    public static double run(File f) throws IOException{
        CommunityGraphFactory factory = (CommunityGraphFactory)GraphFactoryFactory.getInstance().getByNameAndExtension("auto", "cnf", "ol", new HashMap<String, String>());
        factory.makeGraph(f);
        CommunityGraph c = factory.getGraph();
        return run(c);
    }
    public static double run(File f, BufferedWriter bw) throws IOException{
        CommunityGraphFactory factory = (CommunityGraphFactory)GraphFactoryFactory.getInstance().getByNameAndExtension("auto", "cnf", "ol", new HashMap<String, String>());
        factory.makeGraph(f);
        CommunityGraph c = factory.getGraph();
        run(c, bw);
        return 0.0;
    }
    
    static double realAvg = 0;
    static double approxAvg = 0;
    static double comAvg = 0;
    static double interAvg = 0;
    static double realMin = Double.MAX_VALUE;
    static double approxMin = Double.MAX_VALUE;
    static double comMin = Double.MAX_VALUE;
    static double interMin = Double.MAX_VALUE;
    static double realMax = 0;
    static double approxMax = 0;
    static double comMax = 0;
    static double interMax = 0;
    static int realCount = 0;
    static int approxCount = 0;
    static int comCount = 0;
    static int interCount = 0;
    static int approxCountPairsAvg = 0;
    public static void runSingle(CommunityGraph graph){
        if(graph.getNodeCount() <= 1){
            return;
        }
        AverageShortestPathMetric avg = new AverageShortestPathMetric();
        if(graph.getNodes().size() > AverageShortestPathMetric.THRESHOLD_ALL){
            double approx = avg.getCommunities(graph);
            approxMax = Math.max(approx, approxMax);
            approxMin = Math.min(approx, approxMin);
            approxAvg += approx;
            approxCount ++;
        }
        else{
            double real = avg.getCommunities(graph);
            realCount++;
            realAvg += real;
            realMin = Math.min(real, realMin);
            realMax = Math.max(real, realMax);

            int oldThreshold = AverageShortestPathMetric.THRESHOLD_FLOYD;
            int oldThresholdAll = AverageShortestPathMetric.THRESHOLD_ALL;
            AverageShortestPathMetric.THRESHOLD_FLOYD = AverageShortestPathMetric.THRESHOLD_ALL = Math.min(graph.getNodeCount() / 2,AverageShortestPathMetric.THRESHOLD_ALL);
            double approx = avg.getCommunities(graph);

            approxMax = Math.max(approx, approxMax);
            approxMin = Math.min(approx, approxMin);
            approxAvg += approx;
            approxCount ++;

            AverageShortestPathMetric.THRESHOLD_FLOYD = oldThreshold;
            AverageShortestPathMetric.THRESHOLD_ALL = oldThresholdAll;
        }
        approxCountPairsAvg += avg.count;
        if(graph.getCommunities().isEmpty()){
            OLCommunityMetric ol = new OLCommunityMetric();
            ol.getCommunities(graph);
        }
        CommunityGraph hyper = new ConcreteCommunityGraph();
        for(Community c : graph.getCommunities()){
            hyper.createNode(c.getId() + 1,"");
        }
        for(Community c : graph.getCommunities()){
            for(CommunityEdge e : c.getInterCommunityEdges()){
                CommunityNode a = hyper.getNode(c.getId() + 1);
                CommunityNode b = hyper.getNode(e.getStart().getCommunity() + 1 == a.getId() ? e.getEnd().getCommunity() + 1 : e.getStart().getCommunity() + 1);
                if(hyper.getEdge(a, b) == null){
                    CommunityEdge he = hyper.createEdge(a, b, false);
                    a.addEdge(he);
                    b.addEdge(he);
                }
            }
        }
        double real = -1;
        try{
            double com = avg.getCommunities(hyper);
            comMax = Math.max(com, comMax);
            comMin = Math.min(com, comMin);
            comAvg += com;
            comCount ++;
        }
        catch(Exception e){

        }
        double inter = avg.getInterAvgShortest(graph);
        interMax = Math.max(inter, interMax);
        interMin = Math.min(inter, interMin);
        interAvg += inter;
        interCount ++;
    }
    
    public static void run(Set<CommunityGraph> graphs, BufferedWriter bw) throws IOException{
        for(CommunityGraph graph : graphs){
            runSingle(graph);
        }
        
        bw.write(String.format("%f,",realAvg / realCount));
        bw.write(String.format("%f,",realCount == 0 ? 0 : realMin));
        bw.write(String.format("%f,",realMax));
        bw.write(String.format("%d,",realCount));
        
        bw.write(String.format("%f,",approxAvg / approxCount));
        bw.write(String.format("%f,",approxMin));
        bw.write(String.format("%f,",approxMax));
        bw.write(String.format("%d,",approxCount));
        bw.write(String.format("%d,",approxCountPairsAvg / approxCount));
        
        bw.write(String.format("%f,",comAvg / comCount));
        bw.write(String.format("%f,",comMin));
        bw.write(String.format("%f,",comMax));
        bw.write(String.format("%d,",comCount));
        
        bw.write(String.format("%f,",interAvg / interCount));
        bw.write(String.format("%f,",interMin));
        bw.write(String.format("%f,",interMax));
        bw.write(String.format("%d,",interCount));
    }
    public static void run(CommunityGraph _graph, BufferedWriter bw) throws IOException{
        DisjointGraphs dj = new DisjointGraphs();
        Set<CommunityGraph> graphs = dj.getDisjointGraphs(_graph);
        if(graphs.size() == 1){
            graphs.clear();
            graphs.add(_graph);
        }
        run(graphs, bw);
    }
    public static double run(CommunityGraph graph){
        AverageShortestPathMetric avg = new AverageShortestPathMetric();
        double d = avg.getCommunities(graph);
        
        return d;
        
    }
    public static void main(String[] args) throws IOException{
        run(new File("/media/zacknewsham/SAT/sat2014/sc14-app/atco_enc2_opt2_05_9.cnf"), new BufferedWriter(new FileWriter("/dev/null")));
        if(true){return;}
        if(args.length == 0){
            args = new String[]{
                "/media/zacknewsham/SAT/2014.csv"
            };
        }
        
        CSVModel data = new CSVModel(new File(args[0]));
        //data.addHeader("mean_shortest");
        //data.addHeader("mean_approx_shortest");
        /*data.addHeader("meanvar");
        data.addHeader("maxclause");
        data.addHeader("totaledges");
        data.addHeader("weight");
        data.addHeader("gentime");
        data.addHeader("qtime");*/
        
        for(int i = 0; i < data.getRowCount(); i++){
            String file = (String)data.get(i, "file");
            File f = new File(file.replace("media","media/zacknewsham").replace("\"",""));
            try{
                if(!f.exists() || (data.get(i, "mean_shortest") != null && !data.get(i, "mean_shortest").equals("null"))){
                    continue;
                }
            }
            catch(ArrayIndexOutOfBoundsException e){
                e.printStackTrace();
            }
            double d = CalculateNew.run(f);
            data.set(i, "mean_shortest", d);
        data.set(i, "file", f.getAbsolutePath());
        data.toFile(new File(args[0]));
        }
    }
}
