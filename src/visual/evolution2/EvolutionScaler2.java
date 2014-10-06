package visual.evolution2;

import java.awt.BorderLayout;
import java.awt.Dimension;
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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import visual.community.CommunityEdge;
import visual.community.CommunityGraph;
import visual.community.CommunityNode;
import visual.graph.Edge;
import visual.graph.Edge.EdgeState;
import visual.graph.GraphViewer;
import visual.graph.Node;
import visual.graph.Node.NodeAssignmentState;

public class EvolutionScaler2 extends JPanel implements ChangeListener, ActionListener {
	  
	  private final JSlider progress = new JSlider(0, 0, 0);
	  private GraphViewer graphviewer;
	  static String outputDirectory = EvolutionGraphFrame2.outputDirectory;
	  static int linesPerFile = EvolutionGraphFrame2.maxLinesPerFile;
	  int totalFiles = 0;
	  int currentFile = -1;
	  int nextFile = -1;
	  int currentPosition = -1;
	  int totalLines = 0;
	  List<Node> updatedNodes = null;
	  List<Edge> updatedEdges = null;
	  private JCheckBox showAssignedVarsBox = new JCheckBox("Hide Assigned Variables");
	  
	  List<String> currentFileLines = null;
	  List<String> nextFileLines = null;
	  Thread bufferThread = null;
	  
	  private Timer changeSlideTimer = new Timer(10, this);
	  private final JButton play = new JButton("Play");
	  
	  public EvolutionScaler2(GraphViewer graphviewer){
	    this.graphviewer = graphviewer;
	    
	    progress.setSize(new Dimension(100, 20));
	    progress.setPreferredSize(new Dimension(100, 20));
	    
	    play.setSize(new Dimension(70, 30));
	    play.setPreferredSize(new Dimension(70, 30));
	    
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
	    		updateShowAssignedVars(!showAssignedVarsBox.isSelected());
	    		updateGraph();
	    	}
	    });
	    
	    progress.addChangeListener(this);
	  }
	  
	  private void stopTimer() {
		  play.setText("Play");
		  changeSlideTimer.stop();
	  }

	  private void buildLayout() {
		  this.setLayout(new BorderLayout());
		  
		  JLabel title = new JLabel("Evolution", JLabel.CENTER);
		  this.add(title, BorderLayout.NORTH);
		  
		  this.add(progress, BorderLayout.CENTER);
		  this.add(play, BorderLayout.EAST);
		  
		  this.add(showAssignedVarsBox, BorderLayout.SOUTH);
	  }
	  
	  @Override
	  public void stateChanged(ChangeEvent e) {
		  updatePosition();
	  }
	  
	  private void updatePosition() {
		  int position = progress.getValue();
		  
		  if (currentPosition != position && !(currentPosition == -1 && position == 0)) {
			  try {
				advanceEvolution(currentPosition, position);
			} catch (InterruptedException e1) {
				System.out.println("Error advancing the evolution.");
			}
		  }
	  }
	  
	  private void updateProgress() {
		  progress.removeChangeListener(this);
	      progress.addChangeListener(this);
	      progress.revalidate();
	      progress.repaint();
	  }
	  
	  private void updateGraph() {
		  graphviewer.setUpdatedNodes(updatedNodes);
		  graphviewer.setUpdatedEdges(updatedEdges);
		  graphviewer.getGraphCanvas().repaint();
	  }
	  
	  void setGraphViewer(GraphViewer graph){
	    this.graphviewer = graph;
	    updateProgress();
	  }
	  
	  private void parseLine(String line, boolean forwards) {
			if (line.charAt(0) == 'v') {
		    	parseNodeLine(line, forwards);
		    } else if (line.charAt(0) == 'c') {
		    	parseEdgeLine(line, forwards);
		    }
		}
	  
	  private void parseNodeLine(String line, boolean forwards) {
		  CommunityGraph graph = EvolutionGrapher2.getInstance().getGraph();
		  NodeAssignmentState state = NodeAssignmentState.UNASSIGNED;
		  CommunityNode n = null;
		  boolean stateFound = false;
		  
		  if (line.compareTo("v 1 23") == 0) {
			  int o = 0;
		  }
		  
		  for (String c : line.split(" ")) {
			  if (c.compareTo("v") == 0) { // Start of line
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
			  } else {
				  n = graph.getNode(Integer.parseInt(c));
			  }
		  }
		  
		  if (n != null) {
			  NodeAssignmentState prevState = n.getAssignmentState();
			  
			  if (forwards)
				  n.setAssignmentState(state);
			  else
				  n.revertToPreviousAssignmentState();
			  
			  if (n.getAssignmentState() != prevState) // Will redraw the node if it has changed at all
				  updatedNodes.add(n);
		  }
	  }
	  
	  private void parseEdgeLine(String line, boolean forwards) {
		  ArrayList<CommunityNode> nodes = new ArrayList<CommunityNode>();
		  CommunityGraph graph = EvolutionGrapher2.getInstance().getGraph();
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
					  graph.connect(n1, n2, false);
					  e = graph.getEdge(n1, n2);
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
	  
	  private void advanceEvolution(int startingLine, int endingLine) throws InterruptedException {
		  updatedNodes = new ArrayList<Node>();
		  updatedEdges = new ArrayList<Edge>();
		  
		  if (startingLine < endingLine)
			  forwardsEvolution(startingLine, endingLine);
		  else
			  backwardsEvolution(startingLine, endingLine);
		  
		  currentPosition = endingLine;
		  updateGraph();
	  }
	  
	  private void forwardsEvolution(int startingLine, int endingLine) throws InterruptedException {
		  for (int i = startingLine+1; i <= endingLine; i++) {
			  parseLine(getLine(i, true), true);
		  }
	  }
	  
	  private void backwardsEvolution(int startingLine, int endingLine) throws InterruptedException {
		  for (int i = startingLine; i > endingLine; i--) {
			  parseLine(getLine(i, false), false);
		  }
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
		  
		  if (currentPosition == totalLines)
			  stopTimer();
		  
		  if (currentPosition == -1) {
			  update = 1;
		  } else {
			  update = currentPosition+10;
		  }
		  
		  progress.setValue(update);
	  }
	  
	  private void updateShowAssignedVars(boolean show) {
		  graphviewer.setShowAssignedVars(show);
	  }
}
