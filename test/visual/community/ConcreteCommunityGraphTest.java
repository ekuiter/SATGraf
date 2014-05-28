/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.community;

import gnu.trove.map.hash.TObjectCharHashMap;
import java.util.Iterator;
import org.junit.Assert;
import org.junit.Test;
import visual.graph.Clause;

/**
 *
 * @author zacknewsham
 */
public class ConcreteCommunityGraphTest {
    
    @Test
    public void to3CNF5Vars(){
        ConcreteCommunityGraph g = new ConcreteCommunityGraph();
        TObjectCharHashMap<CommunityNode> nodes = new TObjectCharHashMap<>();
        for(int i = 1; i <= 5; i++){
            CommunityNode cn = g.createNode(i, "");
            nodes.put(cn, '1');
        }
        g.createClause(nodes);
        String gString = "";
        Iterator<Clause> clauses = g.getClauses();
        while(clauses.hasNext()){
            Clause c = clauses.next();
            gString = gString.concat(c.toString()).concat("0\n");
        }
        Assert.assertEquals("5 4 3 2 1 0\n", gString);
        
        CommunityGraph cnf3 = g.to3CNF();
        Assert.assertEquals(3, cnf3.getClausesCount());
        clauses = cnf3.getClauses();
        String cnf3String = "";
        while(clauses.hasNext()){
            Clause c = clauses.next();
            cnf3String = cnf3String.concat(c.toString()).concat("0\n");
        }
        Assert.assertEquals("7 -6 3 0\n6 5 4 0\n-7 2 1 0\n", cnf3String);
    }
    
    @Test
    public void to3CNF4Vars(){
        ConcreteCommunityGraph g = new ConcreteCommunityGraph();
        TObjectCharHashMap<CommunityNode> nodes = new TObjectCharHashMap<>();
        for(int i = 1; i <= 4; i++){
            CommunityNode cn = g.createNode(i, "");
            nodes.put(cn, '1');
        }
        g.createClause(nodes);
        String gString = "";
        Iterator<Clause> clauses = g.getClauses();
        while(clauses.hasNext()){
            Clause c = clauses.next();
            gString = gString.concat(c.toString()).concat("0\n");
        }
        Assert.assertEquals("4 3 2 1 0\n", gString);
        
        CommunityGraph cnf3 = g.to3CNF();
        Assert.assertEquals(2, cnf3.getClausesCount());
        clauses = cnf3.getClauses();
        String cnf3String = "";
        while(clauses.hasNext()){
            Clause c = clauses.next();
            cnf3String = cnf3String.concat(c.toString()).concat("0\n");
        }
        Assert.assertEquals("5 4 3 0\n-5 2 1 0\n", cnf3String);
    }
    
    @Test
    public void to3CNFNegVars(){
        ConcreteCommunityGraph g = new ConcreteCommunityGraph();
        TObjectCharHashMap<CommunityNode> nodes = new TObjectCharHashMap<>();
        CommunityNode cn = g.createNode(113, "");
        nodes.put(cn, '1');
        cn = g.createNode(561, "");
        nodes.put(cn, '1');
        cn = g.createNode(305, "");
        nodes.put(cn, '1');
        cn = g.createNode(465, "");
        nodes.put(cn, '1');
        cn = g.createNode(49, "");
        nodes.put(cn, '0');
            
        g.createClause(nodes);
        String gString = "";
        Iterator<Clause> clauses = g.getClauses();
        while(clauses.hasNext()){
            Clause c = clauses.next();
            gString = gString.concat(c.toString()).concat("0\n");
        }
        Assert.assertEquals("113 561 305 465 -49 0\n", gString);
        
        CommunityGraph cnf3 = g.to3CNF();
        Assert.assertEquals(3, cnf3.getClausesCount());
        clauses = cnf3.getClauses();
        String cnf3String = "";
        while(clauses.hasNext()){
            Clause c = clauses.next();
            cnf3String = cnf3String.concat(c.toString()).concat("0\n");
        }
        Assert.assertEquals("5 4 3 0\n-5 2 1 0\n", cnf3String);
    }
    
    
    @Test
    public void to3CNF2Clauses45Vars(){
        ConcreteCommunityGraph g = new ConcreteCommunityGraph();
        TObjectCharHashMap<CommunityNode> nodes = new TObjectCharHashMap<>();
        for(int i = 1; i <= 4; i++){
            CommunityNode cn = g.createNode(i, "");
            nodes.put(cn, '1');
        }
        g.createClause(nodes);
        nodes = new TObjectCharHashMap<>();
        for(int i = 5; i <= 9; i++){
            CommunityNode cn = g.createNode(i, "");
            nodes.put(cn, '1');
        }
        g.createClause(nodes);
        String gString = "";
        Iterator<Clause> clauses = g.getClauses();
        while(clauses.hasNext()){
            Clause c = clauses.next();
            gString = gString.concat(c.toString()).concat("0\n");
        }
        Assert.assertEquals("4 3 2 1 0\n9 8 7 6 5 0\n", gString);
        
        CommunityGraph cnf3 = g.to3CNF();
        Assert.assertEquals(5, cnf3.getClausesCount());
        clauses = cnf3.getClauses();
        String cnf3String = "";
        while(clauses.hasNext()){
            Clause c = clauses.next();
            cnf3String = cnf3String.concat(c.toString()).concat("0\n");
        }
        Assert.assertEquals("5 4 3 0\n-5 2 1 0\n", cnf3String);
    }
}
