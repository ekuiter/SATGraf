/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.test;

import gnu.trove.map.hash.TIntIntHashMap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import com.satlib.community.CommunityGraph;

/**
 *
 * @author zacknewsham
 */
public class To3CNF {
    private QAgainstTime qt = new QAgainstTime();
    public void run(String fa) throws IOException{
        String name = fa.split("/")[fa.split("/").length - 1];
        /*CommunityGrapher g = new CommunityGrapher(fa,"ol", "grid", new HashMap<String, String>());
        g.generateGraph();
        CommunityGraph cnf3 = g.getGraph().to3CNF();
        double origCVR = (double)g.getGraph().getClausesCount() / (double)g.getGraph().getNodeCount();
        double newCVR = (double)cnf3.getClausesCount() / (double)cnf3.getNodeCount();
        System.out.printf("Orig: %f, 3CNF: %f\n", origCVR, newCVR);
        
        File fOrig = new File("/home/zacknewsham/results/industrial/orig/".concat(name));
        File f3CNF = new File("/home/zacknewsham/results/industrial/3cnf/".concat(name));
        g.getGraph().writeDimacs(fOrig);
        cnf3.writeDimacs(f3CNF);
        
        File f1 = new File("/home/zacknewsham/results/industrial/orig/".concat(name).concat(".dist"));
        File f2 = new File("/home/zacknewsham/results/industrial/3cnf/".concat(name).concat(".dist"));
        
        TIntIntHashMap map = g.getGraph().getVariableDistribution();
        BufferedWriter bw = new BufferedWriter(new FileWriter(f1));
        for(int var : map.keys()){
            bw.write(String.format("%d,%d\n", var, map.get(var)));
        }
        bw.close();
        map = cnf3.getVariableDistribution();
        bw = new BufferedWriter(new FileWriter(f2));
        for(int var : map.keys()){
            bw.write(String.format("%d,%d\n", var, map.get(var)));
        }
        bw.close();
        System.out.printf("Orig, ");
        qt.timeout = 15 * 60 * 1000;
        qt.f = fOrig;
        qt.run(g.getGraph(), 0.0);
        System.out.printf("3CNF, ");
        qt.f = f3CNF;
        qt.run(cnf3, 0.0);
        */
    }
    
    public static void main(String[] args){
        if(args.length == 0){
            args = new String[]{"/home/zacknewsham/Sites/satgraf/formula/satcomp/target/"};
        }
        To3CNF cnf = new To3CNF();
        String[] files = new String[]{
            "connm-ue-csp-sat-n800-d-0.02-s1542454144.sat05-533.reshuffled-07.cnf",
            /*"em_11_3_4_exp.cnf",
            "em_12_2_4_exp.cnf",
            "em_7_4_8_all.cnf",
            "em_7_4_9_all.cnf",
            "em_7_4_9_cmp.cnf",
            "em_7_4_9_exp.cnf",
            "em_7_4_9_fbc.cnf",
            "em_8_4_5_cmp.cnf",
            "Hidoku_enu_6.cnf",
            "jkkk-random-132906006148277-10-10-34-SUM-sat.cnf",
            "jkkk-random-132906006427000-10-10-35-OR-sat.cnf",
            "jkkk-random-132906006427000-10-10-36-SUM-sat.cnf",
            "jkkk-random-132906006632161-10-10-33-OR-sat.cnf",
            "LABS_n041_goal003.cnf",
            "LABS_n044_goal003.cnf",
            "LABS_n064_goal005.cnf",
            "LABS_n081_goal007.cnf",
            "LABS_n087_goal008.cnf",
            "LABS_n088_goal008.cnf",
            "LABS_n091_goal008.cnf",
            "LABS_n091_goal009.cnf",*/
            "mod2c-rand3bip-sat-240-2.shuffled-as.sat05-2519.cnf",
            "mod2c-rand3bip-sat-240-3.shuffled-as.sat05-2520.cnf",
            "mod2c-rand3bip-sat-250-3.shuffled-as.sat05-2535.cnf",
            "mod2-rand3bip-sat-250-3.sat05-2220.reshuffled-07.cnf",
            "mod4block_2vars_10gates_u2_autoenc.cnf",
            "mrpp_4x4#10_16.cnf",
            "mrpp_4x4#10_20.cnf",
            "mrpp_6x6#10_24.cnf",
            "mrpp_6x6#12_16.cnf",
            "mrpp_6x6#12_24.cnf",
            "mrpp_6x6#14_10.cnf",
            "mrpp_6x6#14_20.cnf",
            "mrpp_6x6#18_10.cnf",
            "mrpp_6x6#18_20.cnf",
            "ndist.b.26487.cnf",
            "ndist.b.27984.cnf",
            "ndist.b.28483.cnf",
            "ndist.b.28982.cnf",
            "ndist.b.29481.cnf",
            "ndist.b.29980.cnf",
            "Q3inK10.cnf",
            "rbsat-v760c43649g3.cnf",
            "rbsat-v760c43649g6.cnf",
            "rbsat-v945c61409g10.cnf",
            "rbsat-v945c61409g5.cnf",
            "rbsat-v945c61409gyes1.cnf",
            "rbsat-v945c61409gyes2.cnf",
            "rbsat-v945c61409gyes9.cnf",
            "sgen1-sat-140-100.cnf",
            "sgen1-sat-230-100.cnf",
            "sgen4-sat-200-8.cnf",
            "sgp_6-6-10.sat05-2670.reshuffled-07.cnf",
            "toughsat_factoring_1238s.cnf",
            "toughsat_factoring_148s.cnf",
            "toughsat_factoring_155s.cnf",
            "toughsat_factoring_426s.cnf",
            "toughsat_factoring_428s.cnf",
            "toughsat_factoring_895s.cnf",
            "toughsat_factoring_958s.cnf",
            "VanDerWaerden_pd_2-3-20_388.cnf",
            "VanDerWaerden_pd_2-3-23_505.cnf"
        };
        for(String file : files){
            try{
                file = args[0].concat(file);
                cnf.run(file);
            }
            catch(Exception e){
                System.err.printf("problem with: %s\n", file);
            }
        }
        
    }
}
