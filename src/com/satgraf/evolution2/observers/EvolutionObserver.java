/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution2.observers;

import com.satlib.community.CommunityNode;
import com.satlib.graph.Clause;
import javax.swing.JPanel;

/**
 *
 * @author zacknewsham
 */
public interface EvolutionObserver {
    void clauseAdded(Clause c);
    void nodeAssigned(CommunityNode n, boolean isDecision);
    String getName();
}
