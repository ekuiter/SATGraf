package com.satgraf.evolution2.UI;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityNode;
import com.satlib.evolution.EvolutionGraphFactoryFactory;
import com.satlib.graph.Edge;
import com.satlib.graph.Edge.EdgeState;
import com.satlib.graph.Node;
import com.satlib.graph.Node.NodeAssignmentState;

public class Evolution2Scaler extends JPanel implements ChangeListener, ActionListener {
	  
	  private final JSlider progress = new JSlider(0, 0, 0);
	  private Evolution2GraphViewer graphviewer;
	  static String outputDirectory = EvolutionGraphFactoryFactory.outputDirectory;
	  static int linesPerFile = EvolutionGraphFactoryFactory.maxLinesPerFile;
	  private int totalFiles = 0;
	  private int currentFile = -1;
	  private int nextFile = -1;
	  private int currentPosition = -1;
	  private int totalLines = 0;
	  private int currentConflict = 0;
	  private int desiredConflict = 0;
	  private boolean isScanningForConflict = false;
	  List<Node> updatedNodes = new ArrayList<Node>();
	  List<Edge> updatedEdges = new ArrayList<Edge>();
	  private JCheckBox showAssignedVarsBox = new JCheckBox("Show Assigned Variables");

	  List<String> currentFileLines = null;
	  List<String> nextFileLines = null;
	  Thread bufferThread = null;
	  int lastDecisionVariable = -1;
	  private int displayDecisionVariableCount = 0;
	  
	  private int nextLineAdvancingTo = 0;
	  private Thread evolutionThread = null;
	  
	  private Timer changeSlideTimer = new Timer(10, this);
	  private final JButton play = new JButton("Play");
	  private boolean timerTriggered = false;
	  private boolean updateInProgress = false;
	  
	  public Evolution2Scaler(final Evolution2GraphViewer graphviewer){
	    this.graphviewer = graphviewer;
	    
	    progress.setPreferredSize(new Dimension(100, 20));
	    progress.setEnabled(false);
	    
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
	    		updatedNodes.addAll(graphviewer.getGraph().getNodes());
	    		updateGraph();
	    	}
	    });
	    
	    progress.addChangeListener(this);
	  }
	  
	  private void stopTimer() {
		  play.setText("Play");
		  changeSlideTimer.stop();
		  updateInProgress = false;
		  displayDecisionVariableCount = 0;
		  graphviewer.clearDecisionVariable();
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
		  if (this.updateInProgress)
			  return; // Wait until user is no longer dragging or there isn't a decision variable being drawn
		  
		  updatePosition(progress.getValue());
		  
		  if (currentPosition == totalLines) {
			  graphviewer.clearDecisionVariable();
			  updateGraph();
		  }
	  }
	  
	  private void updatePosition(int position) {
		  if (currentPosition != position && !(currentPosition == -1 && position == 0)) {
			  advanceEvolution(currentPosition, position);
		  }
	  }
      
      public int getMaxLine(){
        return progress.getMaximum();
      }
	  
	  private void updateProgress() {
		  progress.removeChangeListener(this);
	      progress.addChangeListener(this);
	      progress.revalidate();
	      progress.repaint();
	  }
	  
	  private void updateGraph() {
		  graphviewer.setUpdatedNodes(new ArrayList<Node>(updatedNodes));
		  graphviewer.setUpdatedEdges(new ArrayList<Edge>(updatedEdges));
		  
		  if (progress.getValue() != currentPosition)
			  progress.setValue(currentPosition);
		  
		  updatedEdges.clear();
		  updatedNodes.clear();
	  }
	  
	  void setGraphViewer(Evolution2GraphViewer graph){
	    this.graphviewer = graph;
	    updateProgress();
	  }
	  
	  public boolean scanToConflict(int conflictNumber) {
		  if (timerTriggered)
			  stopTimer();
		  
		  if ((conflictNumber > currentConflict && currentPosition >= totalLines-1) || (conflictNumber < currentConflict && currentPosition <= 0)) {
			  return false;
		  }
		  
		  this.isScanningForConflict = true;
		  this.desiredConflict = conflictNumber;
		  
		  if (conflictNumber > currentConflict) {
			  updatePosition(totalLines);
		  } else if (conflictNumber < currentConflict) {
			  updatePosition(0);
		  }
		  
		  return true;
	  }
	  
	  private void parseLine(String line, boolean forwards, int lineNumber) {
			if (line.charAt(0) == 'v') {
		    	parseNodeLine(line, forwards, lineNumber);
		    } else if (line.charAt(0) == 'c') {
		    	parseEdgeLine(line, forwards);
		    } else if (line.charAt(0) == '!') {
		    	foundConflict(line);
		    }
	  }
	  
	  private void foundConflict(String line) {
			this.currentConflict = Integer.parseInt(line.split(" ")[1]);
	  }
	  
	  private void parseNodeLine(String line, boolean forwards, int lineNumber) {
		  CommunityGraph graph = Evolution2GraphFactoryFactory.getInstance().getGraph();
		  NodeAssignmentState state = NodeAssignmentState.UNASSIGNED;
		  CommunityNode n = null;
		  int activity = 0;
		  boolean stateFound = false;
		  boolean isDecisionVariable = false;
		  boolean activityFound = false;
		  
		  for (String c : line.split(" ")) {
			  if (c.compareTo("v") == 0) { // Start of line
				  continue;
			  } else if (c.compareTo("d") == 0) {
				  if (forwards)
				  	  isDecisionVariable = true;
			  	  continue;
			  } else if (c.compareTo("p") == 0) {
				  // This is just a propagation variable. Do nothing with it at the moment.
				  continue;
			  } else if (!stateFound) { // Var state
				  stateFound = true;
				  
				  if (forwards) { // Not necessary to do if in reverse
					  switch (Integer.parseInt(c)) {
					  	case 0:
					  		state = NodeAssignmentState.ASSIGNED_FALSE;
					  		break;
					  	case 1:
					  		state = NodeAssignmentState.ASSIGNED_TRUE;
					  		break;
				  		default:
					  		state = NodeAssignmentState.UNASSIGNED;
					  }
				  }
			  } else if (!activityFound) {
				  activity = Integer.parseInt(c);
				  activityFound = true;
			  } else {
				  n = graph.getNode(Integer.parseInt(c));
			  }
		  }
		  
		  if (n != null) {
			  NodeAssignmentState prevState = n.getAssignmentState();
			  n.setActivity(activity);
			  if(isDecisionVariable){
                graphviewer.recordDecisionVariable(n);
              }
			  if (isDecisionVariable && graphviewer.getShowDecisionVariable() && lastDecisionVariable != lineNumber) {
				  graphviewer.setDecisionVariable(n);
				  lastDecisionVariable = lineNumber;
				  				  				  
				  if (timerTriggered)
				  	  return;
			  }
			  
			  if (forwards && state != prevState)
				  n.setAssignmentState(state);
			  else if (!forwards)
				  n.revertToPreviousAssignmentState();
			  
			  if (n.getAssignmentState() != prevState || isDecisionVariable) // Will redraw the node if it has changed at all
				  updatedNodes.add(n);
			  if (n.getAssignmentState() != prevState)
				  updatedEdges.addAll(n.getEdgesList());
		  }
	  }
	  
	  private void parseEdgeLine(String line, boolean forwards) {
		  ArrayList<CommunityNode> nodes = new ArrayList<CommunityNode>();
		  CommunityGraph graph = Evolution2GraphFactoryFactory.getInstance().getGraph();
		  boolean addEdge = true;
		  
		  for (String c : line.split(" ")) {
			  if (c.compareTo("c") == 0) { // Start of line
				  continue;
			  } else if (c.compareTo("0") == 0) { // End of line
				  break;
			  } else if (c.compareTo("+") == 0) {
				  addEdge = true;
			  } else if (c.compareTo("-") == 0) {
				  addEdge = false;
			  } else {
				  nodes.add(graph.getNode(Integer.parseInt(c)));
			  }
		  }
		  
		  CommunityNode n1;
		  CommunityNode n2;
		  CommunityEdge e;
		  EdgeState prevState;
		  
		  for (int i = 0; i < nodes.size() - 1; i++) {
			  n1 = nodes.get(i);
			  for (int j = i + 1; j < nodes.size(); j++) {
				  n2 = nodes.get(j);
				  e = graph.getEdge(n1, n2);
				  
				  if (e == null) {
					  e = graph.connect(n1, n2, false);
					  
					  if (addEdge)
						  e.setEdgeAsConflict();
				  }
				  
				  prevState = e.getAssignmentState();
				  
				  if (forwards) {
					  if (addEdge) {
						  e.setAssignmentState(EdgeState.SHOW);
					  } else {
						  e.setAssignmentState(EdgeState.HIDE);
					  }
				  } else {
					  e.revertToPreviousAssignmentState();
				  }
				  
				  if (e.getAssignmentState() != prevState)
					  updatedEdges.add(e);
			  }
		  }
	  }
	  
	  public void newFileReady(int numLinesInFile) {
		  int max = progress.getMaximum();
		  if (totalFiles == 0)
			  max -= 1; // Since it starts with 1 element in it which we don't want to count
		  totalFiles++;
		  totalLines = max + numLinesInFile;
		  progress.setMaximum(totalLines);
		  updateProgress();
		  
		  if (nextFileLines == null && bufferThread == null) {
			  bufferNextFile(totalFiles-1);
		  }
	  }
	  
	  private void bufferNextFile(final int fileNumber) {
		nextFile = fileNumber;
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				try {
					nextFileLines = Files.readAllLines(Paths.get(outputDirectory + Integer.toString(fileNumber) + ".txt"), Charset.defaultCharset());
					progress.setEnabled(true);
					play.setEnabled(true);
				} catch (IOException e) {
					System.out.println("Error opening output file.");
				}
			}
		};
		bufferThread = new Thread(r);
		bufferThread.start();
	  }
	  
	  private void updateBufferedLines(int lineInCurrentFile, boolean forwards) {
		  if (currentFile >= totalFiles-1 && forwards)
			  forwards = false;
		  else if (currentFile <= 0 && !forwards)
			  forwards = true;
		  
		  boolean closeToEndOfCurrentFile = forwards && (lineInCurrentFile >= (int)(0.9 * linesPerFile));
		  boolean closeToStartOfCurrentFile = !forwards && (lineInCurrentFile <= (int)(0.1 * linesPerFile));
		  
		  boolean updateBuffer = nextFileLines == null || closeToEndOfCurrentFile || closeToStartOfCurrentFile;
		  
		  if (updateBuffer) {
			  int nf = -1;
			  
			  if (forwards && nextFile != currentFile+1) {
				  nf = currentFile+1;
			  } else if (!forwards && nextFile != currentFile-1) {
				  nf = currentFile-1;
			  }
			  
			  if (nf != -1) 
				  bufferNextFile(nf);
		  }
	  }
	  
	  private String getLine(int lineNumber, boolean forwards) throws InterruptedException {
		  int fileOfLine = getFileNumberFromLine(lineNumber);
		  
		  boolean isInCurrentFile = fileOfLine == currentFile;
		  boolean isInNextFile = fileOfLine == nextFile;
		  
		  if (!isInCurrentFile && !isInNextFile) {
			  if (bufferThread.isAlive()) {
				  bufferThread.join();
			  }
			  
			  nextFileLines = null;
			  nextFile = -1;
			  bufferNextFile(fileOfLine);
			  bufferThread.join();
			  isInNextFile = true;
		  }
		  
		  if (isInNextFile) {
			  if (bufferThread.isAlive()) {
				  bufferThread.join();
			  }
			  
			  currentFileLines = nextFileLines;
			  currentFile = nextFile;
			  nextFileLines = null;
			  nextFile = -1;
		  }
		  
		  int lineInCurrentFile = adjustOverallLineToCurrentFileLine(lineNumber);
		  updateBufferedLines(lineInCurrentFile, forwards);
		  
		  // Must have the proper lines in the buffer
		  return currentFileLines.get(lineInCurrentFile);
	  }
	  
	  public void advanceEvolution(int startlingLine, int endingLine) {
		  this.nextLineAdvancingTo = endingLine;
		  
		  if (this.evolutionThread == null || !this.updateInProgress) {
			  this.updateInProgress = true;
			  graphviewer.clearDecisionVariable();
			  
			  Runnable r = new Runnable() {
				
				@Override
				public void run() {
					boolean repeat = true;
					
					while (repeat) {
						int el = nextLineAdvancingTo;
						repeat = false;
						
						try {
							advanceEvolutionThread(currentPosition, el);
						} catch (InterruptedException e) {
							System.out.println("Error advancing the evolution.");
						} finally {
							repeat = el != nextLineAdvancingTo;
						}
					}
					
					if (graphviewer.getDecisionVariable() == null || !timerTriggered)
						updateInProgress = false;
					timerTriggered = false;
				}
			};
			
			this.evolutionThread = new Thread(r);
			this.evolutionThread.start();
		  }
	  }
	  
	  public void advanceEvolutionThread(int startingLine, int endingLine) throws InterruptedException {
		  int lastLine = endingLine;
		  
		  if (startingLine < endingLine)
			  lastLine = forwardsEvolution(startingLine, endingLine);
		  else
			  lastLine = backwardsEvolution(startingLine, endingLine);
		  
		  currentPosition = lastLine;
		  updateGraph();
	  }
	  
	  private int forwardsEvolution(int startingLine, int endingLine) throws InterruptedException {
		  for (int i = startingLine+1; i <= endingLine; i++) {
			  parseLine(getLine(i, true), true, i);
			  			  
			  if (this.isScanningForConflict) {
				  if (this.currentConflict >= this.desiredConflict) {
					  this.isScanningForConflict = false;
					  graphviewer.clearDecisionVariable();
					  return i;
				  }
			  } else if (graphviewer.getDecisionVariable() != null && timerTriggered)
			      return i-1;
		  }
		  
		  return endingLine;
	  }
	  
	  private int backwardsEvolution(int startingLine, int endingLine) throws InterruptedException {
		  for (int i = startingLine; i > endingLine; i--) {
			  parseLine(getLine(i, false), false, i);
			  
			  if (this.isScanningForConflict) {
				  if (this.currentConflict <= this.desiredConflict) {
					  this.isScanningForConflict = false;
					  graphviewer.clearDecisionVariable();
					  return i;
				  }
			  }
		  }
		  
		  if (endingLine == 0)
			  this.currentConflict = 0;
		  
		  return endingLine;
	  }
	  
	  private int adjustOverallLineToCurrentFileLine(int lineNumber) {
		  return lineNumber % linesPerFile;
	  }
	  
	  private int getFileNumberFromLine(int lineNumber) {
		  int fileNumber = 0;
		  while (lineNumber > linesPerFile-1) {
			  fileNumber++;
			  lineNumber -= linesPerFile;
		  }
		  
		  return fileNumber;
	  }
	  
	  @Override
	  public void actionPerformed(ActionEvent arg0) {
		  int update;
		  
		  if (this.timerTriggered) // Don't keep advancing evolution if previous step isn't done
			  return;
		  
		  if (graphviewer.getDecisionVariable() != null) {
			  displayDecisionVariableCount++;
			  			  
			  if (displayDecisionVariableCount >= graphviewer.getDisplayDecisionVariableFor()) {
			      graphviewer.clearDecisionVariable();
			  	  displayDecisionVariableCount = 0;
			  	  updateInProgress = false;
			  }
			  			  
			  return;
		  }
		  
		  if (currentPosition == -1) {
			  update = 1;
		  } else {
			  update = currentPosition + graphviewer.getEvolutionSpeed();
		  }
			  		  
		  if (update >= totalLines) {
			  stopTimer();
			  update = totalLines;
		  }
		  
		  timerTriggered = true;
		  updatePosition(update);
	  }
	  
	  private void updateShowAssignedVars(boolean show) {
		  graphviewer.setShowAssignedVars(show);
	  }
	  
	  public int getCurrentConflict() {
	  	  return this.currentConflict;
	  }
}
