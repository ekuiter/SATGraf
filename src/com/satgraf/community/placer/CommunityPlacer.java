/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.placer;

import com.satgraf.graph.placer.Placer;
import com.satlib.community.CommunityNode;

/**
 *
 * @author zacknewsham
 */
public interface CommunityPlacer extends Placer<CommunityNode>{
	public abstract int getCommunityX(int community);
	public abstract int getCommunityY(int community);
	public abstract int getCommunityWidth(int community);
	public abstract int getCommunityHeight(int community);
}
