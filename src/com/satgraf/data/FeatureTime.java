/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.satgraf.data;

import static com.satlib.ForceInit.forceInit;
import com.satlib.community.CNMCommunityMetric;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityGraphFactory;
import com.satlib.community.DimacsCommunityGraphFactory;
import com.satlib.community.DimacsLiteralCommunityGraphFactory;
import com.satlib.community.JSONCommunityGraphFactory;
import com.satlib.community.LouvianCommunityMetric;
import com.satlib.community.OLCommunityMetric;
import com.satlib.graph.GraphFactoryFactory;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author zacknewsham
 */
public class FeatureTime {
    static{
      forceInit(DimacsCommunityGraphFactory.class);
      forceInit(LouvianCommunityMetric.class);
    forceInit(DimacsLiteralCommunityGraphFactory.class);
      forceInit(OLCommunityMetric.class);
      forceInit(CNMCommunityMetric.class);
      forceInit(JSONCommunityGraphFactory.class);
    }
    public static void main(String[] args) throws FileNotFoundException, IOException{
        args = new String[]{
            "/media/zacknewsham/SAT/2013.files"
        };
        System.out.println("graph_time,ol_time,edges,clauses,vars");
        File in = new File(args[0]);
        BufferedReader reader = new BufferedReader(new FileReader(in));
        String cnf = null;
        while((cnf = reader.readLine()) != null){
            File cnfFile = new File(cnf);
            CommunityGraphFactory factory = (CommunityGraphFactory)GraphFactoryFactory.getInstance().getByNameAndExtension("auto", "cnf", "ol", new HashMap<String, String>());
            long startTime = System.currentTimeMillis();
            factory.makeGraph(cnfFile);
            long graphTime = System.currentTimeMillis() - startTime;
            
            OLCommunityMetric metric = new OLCommunityMetric();
            startTime = System.currentTimeMillis();
            metric.getCommunities(factory.getGraph());
            long qTime = System.currentTimeMillis() - startTime;
            CommunityGraph graph = factory.getGraph();
            System.out.printf("%d,%d,%d,%d,%d\n", graphTime, qTime, graph.getEdges().size(), graph.getClausesCount(), graph.getNodes().size());
        }
    }
}
