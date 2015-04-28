package com.satgraf.evolution2.UI;

import com.satlib.evolution.Evolution2GraphViewer;
import com.satlib.evolution.Evolution;
import com.satlib.evolution.observers.EvolutionObserver;
import com.satlib.evolution.observers.EvolutionObserverFactory;
import com.satlib.community.CommunityNode;
import com.satlib.graph.Clause;
import com.satlib.graph.Edge;
import com.satlib.graph.GraphObserver;
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

public class Evolution2Scaler extends JPanel implements ChangeListener, ActionListener, EvolutionObserver{

  private final JSlider progress = new JSlider(0, 0, 0);
  private Evolution2GraphViewer graphviewer;
  public final Evolution evolution;
  private JCheckBox showAssignedVarsBox = new JCheckBox("Show Assigned Variables");

  private Timer changeSlideTimer = new Timer(10, this);
  private final JButton play = new JButton("Play");
  private boolean timerTriggered = false;

  public Evolution2Scaler(final Evolution2GraphViewer graphviewer) {
    this.graphviewer = graphviewer;
    this.evolution = new Evolution(this.graphviewer.getGraph());
    progress.setPreferredSize(new Dimension(100, 20));
    progress.setEnabled(false);
    EvolutionObserverFactory.getInstance().observers().add(this);
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
        evolution.getUpdatedNodes().addAll(graphviewer.getGraph().getNodes());
        updateGraph();
      }
    });

    progress.addChangeListener(this);
  }

  private void stopTimer() {
    play.setText("Play");
    changeSlideTimer.stop();
    evolution.setUpdateInProgress(false);
    evolution.setDisplayDecisionVariableCount(0);
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
    if (evolution.isUpdateInProgress()) {
      return; // Wait until user is no longer dragging or there isn't a decision variable being drawn
    }
    evolution.updatePosition(progress.getValue(), false);

    if (evolution.getCurrentPosition() == evolution.getTotalLines()) {
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
  public void updateGraph() {
    graphviewer.setUpdatedNodes(new ArrayList<Node>(evolution.getUpdatedNodes()));
    graphviewer.setUpdatedEdges(new ArrayList<Edge>(evolution.getUpdatedEdges()));
    if (progress.getValue() != evolution.getCurrentPosition()) {
      progress.setValue(evolution.getCurrentPosition());
    }
    this.timerTriggered = false;
  }

  void setGraphViewer(Evolution2GraphViewer graph) {
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
      evolution.setDisplayDecisionVariableCount(evolution.getDisplayDecisionVariableCount()+1);

      if (evolution.getDisplayDecisionVariableCount() >= graphviewer.getGraph().getDisplayDecisionVariableFor()) {
        graphviewer.getGraph().clearDecisionVariable();
        evolution.setDisplayDecisionVariableCount(0);
        evolution.setUpdateInProgress(false);
      }

      return;
    }

    if (evolution.getCurrentPosition() == -1) {
      update = 1;
    } 
    else {
      update = evolution.getCurrentPosition() + graphviewer.getGraph().getEvolutionSpeed();
    }

    if (update >= evolution.getTotalLines()) {
      update = evolution.getTotalLines();
    }

    timerTriggered = true;
    evolution.updatePosition(update, timerTriggered);

    if (evolution.getCurrentPosition() == evolution.getTotalLines()) {
      stopTimer();
    }
  }

  private void updateShowAssignedVars(boolean show) {
    graphviewer.setShowAssignedVars(show);
  }

  public int getCurrentConflict() {
    return evolution.getCurrentConflict();
  }

  @Override
  public void clauseAdded(Clause c) {
  }

  @Override
  public void nodeAssigned(CommunityNode n, boolean isDecision) {
    if(isDecision){
      graphviewer.notifyObservers(GraphObserver.Action.decisionVariable);
    }
  }

  @Override
  public void newFileReady() {
    progress.setMaximum(evolution.getTotalLines());
    updateProgress();
    progress.setEnabled(true);
    play.setEnabled(true);
  }
}
