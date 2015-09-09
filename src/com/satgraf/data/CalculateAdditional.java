package com.satgraf.data;


import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityGraphFactory;
import com.satlib.community.CommunityMetric;
import com.satlib.graph.Clause;
import com.satlib.graph.GraphFactoryFactory;
import gnu.trove.map.hash.TIntIntHashMap;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author zacknewsham
 */
public class CalculateAdditional {
    public static void main(String[] args) throws IOException{
        if(args.length == 0){
            args = new String[]{
                "/home/zacknewsham/run.csv"
            };
        }
        
        CSVModel data = new CSVModel(new File(args[0]));
        
        /*data.addHeader("meanvar");
        data.addHeader("maxclause");
        data.addHeader("totaledges");
        data.addHeader("weight");
        data.addHeader("gentime");
        data.addHeader("qtime");*/
        
        for(int i = 0; i < data.getRowCount(); i++){
            String file = (String)data.get(i, "file");
            File f = new File(file);
            if(!f.exists() || i < 130){
                continue;
            }
            CommunityGraphFactory factory = (CommunityGraphFactory)GraphFactoryFactory.getInstance().getByNameAndExtension("auto", "cnf", "ol", new HashMap<String, String>());
            long startTime = System.currentTimeMillis();
            factory.makeGraph(f);
            CommunityGraph c = factory.getGraph();
            long endTime = System.currentTimeMillis();
            long generateTime = endTime - startTime;

            CommunityMetric metric = factory.getMetric();
            metric.getCommunities(c);
            long Qtime = System.currentTimeMillis() - endTime;
            TIntIntHashMap map = c.getVariableDistribution();
            Integer maxVar = 0;
            Integer maxClause = 0;
            Double meanVar = 0.0;


            Iterator<Clause> clauses = c.getClauses().iterator();
            while(clauses.hasNext()){
                Clause cl = clauses.next();
                if(maxClause < cl.size()){
                    maxClause = cl.size();
                }
            }
            for(int count : map.values()){
                if(count > maxVar){
                    maxVar = count;
                }
                meanVar += count;
            }
            meanVar = meanVar / map.size();
        
            data.set(i, "meanvar", meanVar);
            data.set(i, "maxclause", new Double(maxClause));
            data.set(i, "totaledges", new Double(c.getTotalEdges()));
            data.set(i, "weight", new Double(c.getWeight()));
            data.set(i, "gentime", new Double(generateTime));
            data.set(i, "qtime", new Double(Qtime));
        }
        data.toFile(new File(args[0]));
    }
}
