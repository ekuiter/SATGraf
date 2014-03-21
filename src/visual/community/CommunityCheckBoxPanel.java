/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.community;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JPanel;

/**
 *
 * @author zacknewsham
 */
class CommunityCheckBoxPanel extends JPanel{
  int count = 0;

  private final CommunityGraphViewer graph;
  public CommunityCheckBoxPanel(CommunityGraphViewer graph) {
    this.graph = graph;
    init();
    this.setLayout(new GridLayout(count, 1));
  }
  private void init(){
    int community = 0;
    Collection<CommunityNode> communities = graph.getCommunityNodes(community);
    while(communities != null){
      int intercomm = graph.getInterCommunityConnections(community).size();
      CommunityCheckBox jc = new CommunityCheckBox(community, communities.size(), intercomm);
      jc.setSelected(true);
      jc.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent ie) {
          CommunityCheckBox box = (CommunityCheckBox)ie.getItem();
          if(box.isSelected()){
            graph.showCommunity(box.getCommunity());
          }
          else{
            graph.hideCommunity(box.getCommunity());
          }
        }
      });
      community++;
      communities = graph.getCommunityNodes(community);
      this.add(jc);
      count ++;
    }
  }
}
