/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.community.drawing_algorithms;

import visual.community.CommunityNode;

/**
 *
 * @author zacknewsham
 */
public interface CommunityPlacer {
    public abstract CommunityNode getNodeAtXY(int x, int y, double scale);
	
	public abstract void init();
	public abstract int getX(CommunityNode node);
	public abstract int getY(CommunityNode node);
	public abstract int getCommunityX(int community);
	public abstract int getCommunityY(int community);
	public abstract int getCommunityWidth(int community);
	public abstract int getCommunityHeight(int community);
}
