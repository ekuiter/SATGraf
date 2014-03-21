/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.community;

import javax.swing.JCheckBox;

/**
 *
 * @author zacknewsham
 */
public class CommunityCheckBox extends JCheckBox{
  private final int community;
  private final int size;
  private final int intercommunity;
  public CommunityCheckBox(int community, int size, int intercommunity){
    super(String.format("%d (%d, %d)", community, size, intercommunity));
    this.community = community;
    this.size = size;
    this.intercommunity = intercommunity;
  }
  public int getCommunity(){
    return this.community;
  }
}
