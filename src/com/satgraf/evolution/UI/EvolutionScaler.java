package com.satgraf.evolution.UI;

import com.satlib.evolution.observers.EvolutionObserverFactory;
import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityMetric;
import com.satlib.evolution.Evolution;
import com.satlib.evolution.observers.EvolutionObserver;
import com.satlib.evolution.observers.CSVEvolutionObserverFactory;
import com.satlib.community.CommunityNode;
import com.satlib.graph.Edge;
import com.satgraf.graph.UI.GraphViewerObserver;
import com.satlib.graph.Node;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class EvolutionScaler extends JPanel implements ChangeListener, ActionListener, EvolutionObserver{

  private final JSlider progress = new JSlider(0, 0, 0);
  private EvolutionGraphViewer graphviewer;
  private JCheckBox showAssignedVarsBox = new JCheckBox("Show Assigned Variables");

  private Timer changeSlideTimer = new Timer(10, this);
  private final JButton play = new JButton("Play");
  private boolean timerTriggered = false;
  private CommunityMetric metric;

  public EvolutionScaler(final EvolutionGraphViewer graphviewer) {
    this.graphviewer = graphviewer;
    progress.setPreferredSize(new Dimension(100, 20));
    progress.setEnabled(false);
    EvolutionObserverFactory.getInstance().observers().add(this);
    graphviewer.evolution.start();
    play.setPreferredSize(new Dimension(70, 30));
    play.setEnabled(false);

    buildLayout();

    play.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (play.getText().compareTo("Play") == 0) {
          play.setText("Pause");
          changeSlideTimer.start();
        } else {
          stopTimer();
        }
      }
    });

    showAssignedVarsBox.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        updateShowAssignedVars(showAssignedVarsBox.isSelected());
        graphviewer.getEvolution().getUpdatedNodes().addAll(graphviewer.getGraph().getNodes());
        updateGraph();
      }
    });

    progress.addChangeListener(this);
  }

  private void stopTimer() {
    play.setText("Play");
    changeSlideTimer.stop();
    graphviewer.getEvolution().setUpdateInProgress(false);
    graphviewer.getEvolution().setDisplayDecisionVariableCount(0);
    graphviewer.getGraph().clearDecisionVariable();
  }

  private void buildLayout() {
    this.setLayout(new GridLayout(3, 1));

    JLabel title = new JLabel("<html><b>Evolution</b></html>", JLabel.CENTER);
    this.add(title);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(progress);
    panel.add(play);
    this.add(panel);

    this.add(showAssignedVarsBox);
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    if (graphviewer.getEvolution().isUpdateInProgress()) {
      return; // Wait until user is no longer dragging or there isn't a decision variable being drawn
    }
    graphviewer.getEvolution().updatePosition(progress.getValue(), false);

    if (graphviewer.getEvolution().getCurrentPosition() == graphviewer.getEvolution().getTotalLines()) {
      graphviewer.getGraph().clearDecisionVariable();
      updateGraph();
    }
  }

  public int getMaxLine() {
    return progress.getMaximum();
  }

  private void updateProgress() {
    progress.removeChangeListener(this);
    progress.addChangeListener(this);
    progress.revalidate();
    progress.repaint();
  }
  
  @Override
  public void setCommunityMetric(CommunityMetric metric){
    this.metric = metric;
  }
  
  @Override
  public void updateGraph() {
    graphviewer.setUpdatedNodes(new ArrayList<Node>(graphviewer.getEvolution().getUpdatedNodes()));
    graphviewer.setUpdatedEdges(new ArrayList<Edge>(graphviewer.getEvolution().getUpdatedEdges()));
    if (progress.getValue() != graphviewer.getEvolution().getCurrentPosition()) {
      progress.setValue(graphviewer.getEvolution().getCurrentPosition());
    }
    this.timerTriggered = false;
  }

  void setGraphViewer(EvolutionGraphViewer graph) {
    this.graphviewer = graph;
    updateProgress();
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    int update;

    if (this.timerTriggered) // Don't keep advancing evolution if previous step isn't done
    {
      return;
    }

    if (graphviewer.getGraph().getDecisionVariable() != null) {
      graphviewer.getEvolution().setDisplayDecisionVariableCount(graphviewer.getEvolution().getDisplayDecisionVariableCount()+1);

      if (graphviewer.getEvolution().getDisplayDecisionVariableCount() >= graphviewer.getGraph().getDisplayDecisionVariableFor()) {
        graphviewer.getGraph().clearDecisionVariable();
        graphviewer.getEvolution().setDisplayDecisionVariableCount(0);
        graphviewer.getEvolution().setUpdateInProgress(false);
      }

      return;
    }

    if (graphviewer.getEvolution().getCurrentPosition() == -1) {
      update = 1;
    } 
    else {
      update = graphviewer.getEvolution().getCurrentPosition() + graphviewer.getGraph().getEvolutionSpeed();
    }

    if (update >= graphviewer.getEvolution().getTotalLines()) {
      update = graphviewer.getEvolution().getTotalLines();
    }

    timerTriggered = true;
    graphviewer.getEvolution().updatePosition(update, timerTriggered);

    if (graphviewer.getEvolution().getCurrentPosition() == graphviewer.getEvolution().getTotalLines()) {
      stopTimer();
    }
  }

  private void updateShowAssignedVars(boolean show) {
    graphviewer.setShowAssignedVars(show);
  }

  public int getCurrentConflict() {
    return graphviewer.getEvolution().getCurrentConflict();
  }

  
  @Override
  public void addEdge(CommunityEdge e){
    
  }
  
  @Override
  public void removeEdge(CommunityEdge e){
    
  }

  @Override
  public void nodeAssigned(CommunityNode n, Node.NodeAssignmentState state, boolean isDecision) {
    if(isDecision){
      graphviewer.notifyObservers(GraphViewerObserver.Action.decisionVariable);
    }
  }

  @Override
  public void newFileReady() {
    progress.setMaximum(graphviewer.getEvolution().getTotalLines());
    updateProgress();
    progress.setEnabled(true);
    play.setEnabled(true);
  }
}
