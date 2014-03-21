/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.evolution;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.JButton;
import javax.swing.Timer;
import visual.community.CommunityGraphViewer;
import visual.community.CommunityOptionsPanel;

/**
 *
 * @author zacknewsham
 */
public class EvolutionOptionsPanel extends CommunityOptionsPanel implements ActionListener{
  private static final String play_text = "Play";
  private static final String stop_text = "Stop";
  private JButton playButton = new JButton("Play");
  private EvolutionPanel evolutionPanel;
  private final Timer changeSlideTimer = new Timer(300, this);
  public EvolutionOptionsPanel(CommunityGraphViewer graph, Collection<String> groups) {
    super(graph, groups, false);
    setGraph(graph);
    playButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if(playButton.getText().equals(play_text)){
          playButton.setText(stop_text);
          changeSlideTimer.start();
        }
        else{
          playButton.setText(play_text);
          changeSlideTimer.stop();
        }
      }
    });
    this.getOptionsPanel().setCustomComponent(playButton);
  }
  
  void setEvolutionPanel(EvolutionPanel ep){
    this.evolutionPanel = ep;
  }
  
  public void setGraph(CommunityGraphViewer graph){
    super.setGraph(graph);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if(evolutionPanel.pane.getSelectedIndex() < evolutionPanel.pane.getTabCount() - 1){
      evolutionPanel.getGraph(evolutionPanel.pane.getSelectedIndex() + 1).setScale(evolutionPanel.getGraph(evolutionPanel.pane.getSelectedIndex()).getScale());
      evolutionPanel.pane.setSelectedIndex(evolutionPanel.pane.getSelectedIndex() + 1);
    }
    else{
      evolutionPanel.getGraph(0).setScale(evolutionPanel.getGraph(evolutionPanel.pane.getSelectedIndex()).getScale());
      evolutionPanel.pane.setSelectedIndex(0);
    }
  }
}
