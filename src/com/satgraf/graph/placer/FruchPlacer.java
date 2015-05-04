/*$$
 * packages uchicago.src.*
 * Copyright (c) 1999, Trustees of the University of Chicago
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with
 * or without modification, are permitted provided that the following
 * conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *   Neither the name of the University of Chicago nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE TRUSTEES OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Nick Collier
 * nick@src.uchicago.edu
 *
 * packages cern.jet.random.*
 * Copyright (c) 1999 CERN - European Laboratory for Particle
 * Physics. Permission to use, copy, modify, distribute and sell this
 * software and its documentation for any purpose is hereby granted without
 * fee, provided that the above copyright notice appear in all copies
 * and that both that copyright notice and this permission notice appear in
 * supporting documentation. CERN makes no representations about the
 * suitability of this software for any purpose. It is provided "as is"
 * without expressed or implied warranty.
 *
 * Wolfgang Hoschek
 * wolfgang.hoschek@cern.ch
 * @author Hacked by Eytan Adar for Guess classes
 *$$*/

package com.satgraf.graph.placer;

import com.satlib.graph.Clause;
import com.satlib.graph.DrawableNode;
import com.satlib.graph.Edge;
import com.satlib.graph.Graph;
import com.satlib.graph.Node;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.*;

/**
 * Positions nodes in layout according to iterations of an
 * implementation of the Fruchmen-Reingold graph layout
 * algorithm. See the docs to <code>updateLayout</code> for the details.<p>
 *
 * The FruchGraphLayout implements the ActionListener interface to
 * interrupt the layout. This breaks out of the algorithm
 * implementation as soon as possible, but will rescale the display if
 * appropriate. You can have the FruchGraphLayout listener for RePast
 * toolbar button presses by including something like the following
 * code inside your model class.<p>
 *
 * <code><pre>
 * graphLayout = new FruchGraphLayout(...);
 * Controller c = (Controller)getController();
 * c.addStopListener(graphLayout);
 * c.addPauseListener(graphLayout);
 * c.addExitListener(graphLayout);
 * </pre></code>
 *
 * This will cause the FruchGraphLayout graphLayout to interrupt its layout
 * whenever stop, pause, or exit is pressed.<p>
 *
 * <b>Note</b> The FruchGraphLayout is not particularly fast, although
 * it is faster than the KamadaGraphLayout. It is not meant as a
 * "true" visualization tool, but rather is intended only to provide
 * the modeler with "sense" of the network. Real analysis and visualization
 * should be done in a tool like Pajek.
 *
 * @version $Revision: 1.1 $ $Date: 2006/06/01 18:00:34 $
 * @author Skye Bender-deMoll email:skyebend@santafe.edu
 */

public class FruchPlacer extends AbstractPlacer<Node, Graph<Node, Edge, Clause>> {
  static{
    PlacerFactory.getInstance().register("f", FruchPlacer.class);
  }

    private int pad = 20;
    
    //number of loops before cooling starts
    private int initialIter = 30;  
    private double progress = 0;
    private int maxPasses = 500;    
    public double optDist = 100;    
    private int updates = 0;
    private boolean rescaleLayout = true; 
    private boolean firstLayout = true;
    private boolean noBreak = true;
    private int seed = 123712382;
    private boolean isSeedSet = false;

    private Collection<Node> nodeList;
    public int width = 2500;
    public int height = 2500;
    private boolean update = true;
    private Random rand = new Random(this.seed);
    private HashMap locations = new HashMap();

    public String getProgressionName(){
      return "Placing Nodes";
    }
    public double getProgress(){
      return progress;
    }
    
    public FruchPlacer(Graph g){
      this(g, 2500, 2500);
    }
    public FruchPlacer(Graph g, int width, int height) {
      super(g);
      nodeList = g.getNodes();
      this.width = width;
      this.height = height;
      optDist = 0.46 * Math.sqrt(((width * height) / (this.graph.getNodeCount() + 1)));
    }

    /**
     * Sets the number of pixels to shrink radius by. Java draws object
     * from top left hand corner and this allows objects drawn on the
     * far right to be visible.
     *
     * @param p the number of pixels to shrink by
     */
    public void setPad(int p) {
	pad = p;
    }

    /**
     * If the layout has been passed a display to update, and
     * updateEveryN is greater than 0, the layout will update the
     * display after every Nth pass through the algorithm.
     *
     * @param updateEveryN how often to update the display
     */
    public void setUpdateEveryN(int updateEveryN) {
	updates = updateEveryN;
    }

    /**
     * Sets whether the completed layout will be resized to exactly fill
     * the display window.  Setting rescale to false may mean that
     * individual nodes or the entire network may drift off the screen,
     * but it will insure maximum visual continuity between layouts, and
     * minimum layout time.  default is true.
     *
     * @param rescale sets if layout will be rescaled
     */
    public void setRescaleLayout(boolean rescale) {
	rescaleLayout = rescale;
    }


	private double calcAttraction(double dist) {
		return dist * dist / optDist;
	}

    private double calcRepulsion(double dist) {
    	return Math.pow(optDist,4) / dist;
    }

    private double coolTemp(double val) {
    	return val / 1.1;
    }

    /**
     * Sets the Random seed used in the intial random placement of the
     * nodes. If the seed is not set, the current timestamp is used as
     * the seed.
     *
     * @param seed the random seed for the initial random placement of the nodes
     */
    public void setRandomSeed(int seed) {
	this.seed = seed;
    this.rand = new Random(this.seed);
	isSeedSet = true;
    }

    
    public double getRandomBelow(int max){
      //return Math.random() * max;
      return rand.nextDouble() * max;
    }
    
    /**
     * Randomly positions nodes on layout.  Called internally before
     * update layout is called for the first time to insure that nodes
     * have starting coordinates. This uses a random generator stream
     * separate from the default RePast random stream.  You can set the seed
     * for this stream using the setRandomSeed method.
     */
    public void randomizeLayout() {
		System.out.println("randomizing layout");
		//MersenneTwister mt = null;
		//if (isSeedSet) mt = new MersenneTwister(seed);
		//else mt = new MersenneTwister(new Date());
		//Uniform uni = new Uniform(mt);
	
		int xLimit = width - pad;
		int yLimit = height - pad;
		Iterator<Node> it = nodeList.iterator();
		while(it.hasNext()) {
		    Node node = it.next();
		    Coordinates c = (Coordinates)locations.get(node);
	        if(c == null){
	          c = new Coordinates(0, 0);
	          locations.put(node, c);
	        }
		    c.setX(getRandomBelow(xLimit));
		    c.setY(getRandomBelow(yLimit));
		}
    }

    double maxWidth = 100;
    double maxHeight = 100;

    /**
     * Positions nodes in layout according to a modified implementation
     * of the Fruchterman-Reingold graph layout algorithm. Nodes are
     * positioned according to an iterative algorithm that assumes that
     * nodes repel each other when close, but are attracted to connected
     * nodes. Convergence is obtained by using a "simulated
     * annealing"-style technique with an arbitrary cooling
     * function. Acts on existing node positions, so randomizeLayout()
     * will be called before first update. The default random seed for this
     * initial layout is the current timestamp. The seed can, however, be set with
     * the <code>setRandomSeed</code> method. The random stream for this initial
     * layout is separate from RePast's default random stream.<p>
     *
     * See, Fruchterman, T.M.J and Reingold, E.M. (1991) <CITE>
     * "Graph Drawing by Force-directed Placement" in Software-Practice and
     * Experience, </CITE>Vol 21(11), 1129-1164
     * Modified for code optimization. (Skye Bender-deMoll)
     *
     * <b> Note at this point, this implementation does not take into account
     * edge strengths</b><p>
     *
     * <code><pre>
     * pseudo code of implementation of Furchterman-Reingold Algorithm
     * -----------------------------------------
     *
     * As implemented in Pajek, the algorithm makes initialIter (10) through
     * the algorithm before starting the cooling function.  If this is the
     * first layout, each nodes is given a random initial position.
     *
     * while temp > 0.5 and passes < maxIterations (500)
     *  //calculate repulsive forces between each node
     *  for v = 0 to numberOfNodes
     *    for u = v+1 to numberOfNodes
     *      calculate the distance vector between the positions of v and u
     *      calculate a displacement displaceVec = (distVec/|distVec|)
     *                                               * repulsion(|distVec|)
     *      add displaceVec vector to v's displacement vector
     *      subtract displaceVec from u's displacement vector
     *    end
     *  end
     *
     *  //calculate attractive forces
     *  for e = 0 to numberOfEdges
     *    get the nodes attached to the edge (v and u)
     *    calculate the distance vector between the positions of v and u
     *    calculate a displacement displaceVec = (distVec/|distVec|) * attraction(|distVec|)
     *    subtract displaceVec vector from v's displacement vector
     *    add displaceVec to u's displacement vector
     *  end
     *
     *  calculate each nodes's displacement, but limit max displacement to temp
     *
     *  //decrease temperature parameter
     *  coolTemp()
     *
     *  if this is an Nth pass, update the layout on screen
     *
     *
     *  at the end, go over all the nodes to find the max and min of the coords,
     *  rescale all coords so that network will fill the display
     * end while
     *
     *  //repulsion function
     *  repulsion(distance) = (distance^2)/optimalDistance

     * //attraction function attraction(distance) =
     * (optimalDistance^2)/distance^2 (formula used in pajek)
     *
     *  //cooling function
     *  coolTemp(temp) = unchanged for initialIter iterations, temp/1.1
     *
     * //optimal distance optimalDistance = 0.46*Math.sqrt(((width *
     * height) / (nodeList.size()+1)))
     * </pre></code>
     *
     * <p>
     * Additional comments: Because the original algorithm repositions the
     * nodes in a deterministic order, highly structured / regular networks
     * may exhibit rotations drift during the layout.
     *
     */
	public void advancePositions() {
		if (done) {
		    return;
		}
	
		if (update) {
	
		    noBreak = true;
	
		    Object[] nl = nodeList.toArray();
	
		    // calc constants
		    double temp = width / 10;
		    int passes = 0;
		    int nNodes = nl.length;
		    double xDelta = 0;
		    double yDelta = 0;
		    double deltaLength = 0;
		    double force = 0;
		    HashSet edges = new HashSet();
		    HashMap nodeIndexer = new HashMap();
	
		    if (firstLayout) {
	          //make sure nodes have random initial coord to begin with
	          randomizeLayout();
	          firstLayout = false;
               progress = 0.1;
		    }
	
		    Iterator<Node> it = nodeList.iterator();
		    while(it.hasNext()) {
	          Node workNode = it.next();
	          locations.put(workNode,new Coordinates(getX(workNode), getY(workNode)));
		    }
	
		    //make arrays corresponding to the coords of each node
		    double[] xPos = new double[nNodes];
		    double[] yPos = new double[nNodes];
		    boolean[] fixed = new boolean[nNodes];
            edges.addAll(graph.getEdges());
		    for (int i = 0; i < nNodes; i++) {
				Node workNode = (Node)nl[i];
				xPos[i] = getX(workNode);
				yPos[i] = getY(workNode);
				maxWidth = Math.max(maxWidth,DrawableNode.NODE_DIAMETER + DrawableNode.NODE_X_SPACING);
				maxHeight = Math.max(maxHeight,DrawableNode.NODE_DIAMETER + DrawableNode.NODE_X_SPACING);
				//fixed[i] = workNode.__getattr_Fixed();
				fixed[i] = false;
				nodeIndexer.put(workNode, new Integer(i));
		    }
	
		    //remove check for self loops and remove them
		    //if (NetUtilities.hasSelfLoops(nodeList)) 
			edges = (HashSet)removeLoops(edges);
	
		    //make arrays corresponding to the displacement vector for
		    //each node
		    double[] xDisp = new double[nNodes];
		    double[] yDisp = new double[nNodes];
		    
		    // keep passing through the layout loop until the temp is
		    // low initialIter + time for cooling schedule
		    while ((temp > 1) && (passes < maxPasses) && noBreak) {
				//calculate repulsive forces between each pair of nodes (set both)
				int limit = nNodes - 1;
				for (int v = 0; v < limit; v++) {	
					xDisp[v] = 0;
			    	yDisp[v] = 0;
					
				    // can skip many loops by assuming that uv = -vu
				    // and looping in factorial
				    for (int u = v + 1; u < nNodes; u++) {
						//get difference of position vectors
						xDelta = xPos[v] - xPos[u];
						yDelta = yPos[v] - yPos[u];
			
						//trap condition where nodes have same position
						if ((xDelta == 0) && (yDelta == 0)) {
						    //don't do anything in hopes that someone
						    //else will kick them apart
			
						    // Do we continue or break here? Waiting
						    // for Skye's answer.
						    continue;
						    //break;
						}
						
						//set vu disp vector
						deltaLength = Math.sqrt((xDelta * xDelta) + (yDelta * yDelta));
						force = calcRepulsion(deltaLength);
						
						xDisp[v] += (xDelta / deltaLength) * force;
						yDisp[v] += (yDelta / deltaLength) * force;
						
						//set uv disp vector (-vu)
						xDisp[u] -= (xDelta / deltaLength) * force;
						yDisp[u] -= (yDelta / deltaLength) * force;
				    }
				}
		
				//calculate attractive forces between nodes connected by an edge
				Iterator iter = edges.iterator();
				int z = 0;
				while (iter.hasNext() && noBreak) {
					z++;
				    Edge edge = (Edge) iter.next();
				    int vIndex = ((Integer) nodeIndexer.get(edge.getStart())).intValue();
				    int uIndex = ((Integer) nodeIndexer.get(edge.getEnd())).intValue();
		
				    //get difference of position vectors
				    xDelta = xPos[vIndex] - xPos[uIndex];
				    yDelta = yPos[vIndex] - yPos[uIndex];
				    
				    //set vu disp vector
				    deltaLength = Math.sqrt((xDelta * xDelta) + (yDelta * yDelta));
				    
				    // get div by 0 "errors" if deltaLength is 0.
				    // BUT WHAT SHOULD deltaLength BE IN THESE CASES?
				    if (deltaLength == 0) 
				    	deltaLength = 0.001;
				    
				    force = calcAttraction(deltaLength) * edge.getWeight();
				    if(vIndex == 1167){
                      int test = 1;
                    }
				    xDisp[vIndex] -= (xDelta / deltaLength) * force;
				    yDisp[vIndex] -= (yDelta / deltaLength) * force;
				    
				    //set uv disp vector to (-vu) because nodes may not be mutually
				    xDisp[uIndex] += (xDelta / deltaLength) * force;
				    yDisp[uIndex] += (yDelta / deltaLength) * force;
				}
		
				//caculate displacement, but limit max displacement to temp
				for (int v = 0; v < nNodes; v++) {
				    double xDispVal = xDisp[v];
				    double yDispVal = yDisp[v];
				    deltaLength = Math.sqrt((xDispVal * xDispVal) + (yDispVal * yDispVal));
				    
				    if (!fixed[v]) {
						if (deltaLength > temp) {
						    xPos[v] += xDisp[v] / (deltaLength / temp);
						    yPos[v] += yDisp[v] / (deltaLength / temp);
						    
						} else {
						    xPos[v] += xDisp[v];
						    yPos[v] += yDisp[v];
						}
						int l = 0;
				    }
				}
				
				//cool temp
				if (passes > initialIter) 
					temp = coolTemp(temp);
		
				passes++;
                progress += 0.001;
				//System.out.println("passes: " + passes);
		    }
	
		    if (rescaleLayout) {
		    	rescalePositions(nl, xPos, yPos);
		    }
		    
		    //removeOverlaps(nl, nNodes, xPos, yPos); // TODO: The problem is here
		}
        progress = 1.0;
		done = true;
	}

	private void removeOverlaps(Object[] nl, int nNodes, double[] xPos, double[] yPos) {
		Random r = new Random();
	    int overlapping = 0;
	    boolean[][] cells = new boolean[(int)width/(int)Math.rint(maxWidth)+10][(int)height/(int)Math.rint(maxHeight)+10];
	    
	    for (int i = 0; i < nNodes; i++) {
			Node node = (Node) nl[i];
			//System.out.println("updating..." + node.getX() + " " + xPos[i]);
			int ci = (int)xPos[i]/(int)Math.rint(maxWidth);
			int cj = (int)yPos[i]/(int)Math.rint(maxHeight);
			boolean found = false;
			if (cells[ci][cj]) {
			    //System.out.println(ci + " " + cj);
			    for (int t = 1 ; t <= 4 ; t++) {
					for (int s = 0 ; s <= t ; s++) {
					    ci = Math.min(Math.max(0,ci+t),cells.length-1);
					    cj = Math.min(Math.max(0,cj+s),cells[ci].length-1);
					    //System.out.println("\t"+ ci + " " + cj);
					    if (!cells[ci][cj]) {
							found = true;
							break;
					    }
					    ci = Math.min(Math.max(0,ci-t),cells.length-1);
					    cj = Math.min(Math.max(0,cj-s),cells[ci].length-1);
					    //System.out.println("\t"+ ci + " " + cj);
					    if (!cells[ci][cj]) {
							found = true;
							break;
					    }
					}
					if (found)
					    break;
			    }
			}
			
			Coordinates c = (Coordinates)locations.get(node);
			c.setX((double)ci*Math.rint(maxWidth));
			c.setY((double)cj*Math.rint(maxHeight));
	
			if (cells[ci][cj]) {
			    overlapping++;
			    c.setX(c.getX()+r.nextInt((int)maxWidth));
			    c.setY(c.getY()+r.nextInt((int)maxHeight));
			}
			cells[ci][cj] = true;
	    }
	    if (overlapping > 0) {
			System.out.println("\tThere are " + 
					   overlapping +
					   " overlapping nodes, (you may want "+
					   "to increase the height/width)");
	    }
	}

	public double round(double a, double modx) {
		double temp = Math.rint(a);
		double temp2 = temp - temp%modx;
		//System.out.println(temp2);
		return(temp2);
	} 

    /**
     * Rescales the x and y coordinates of each node so that the network
     * will maximally fill the display. Will result in some distortion.
     * Called internally if rescale is set to true, will rescale
     * smoothly if animateTransitions is true.
     *
     * @param nodes the nodes to rescale.
     */
	private void rescalePositions(Object[] nList, double[] xPos, double[] yPos) {
		//System.out.println("rescaling...");
		int nNodes = nList.length;
		//find largest coords
		double xMax = xPos[0];
		double yMax = yPos[0];
		double xMin = xPos[0];
		double yMin = yPos[0];
		for (int i = 1; i < nNodes; i++) {
		    xMax = Math.max(xMax, xPos[i]);
		    yMax = Math.max(yMax, yPos[i]);
		    xMin = Math.min(xMin, xPos[i]);
		    yMin = Math.min(yMin, yPos[i]);
		}
		//rescale coords of nodes to fit inside frame
		double xDiff = xMax - xMin;
		double yDiff = yMax - yMin;
		double xPadVal = width - pad;
		double yPadVal = height - pad;
		for (int i = 0; i < nNodes; i++) {
		    xPos[i] = ((xPos[i] - xMin) / xDiff) * xPadVal;
		    yPos[i] = ((yPos[i] - yMin) / yDiff) * yPadVal;
		    Node node = (Node)nList[i];
		    Coordinates c = (Coordinates)locations.get(node);
		    c.setX(xPos[i]);
		    c.setY(yPos[i]);
		}
	}
    
    
	private Set removeLoops(Set edges) {
		HashSet returnList = new HashSet();
		Iterator edgeIter = edges.iterator();
		while (edgeIter.hasNext()) {
		    Edge edge = (Edge)edgeIter.next();
		    if (edge == null) {
			continue;
		    }
		    if (edge.getStart()!= edge.getEnd()) {
			returnList.add(edge);
		    }
		}
		
		return returnList;
	}
    
    /**
     * Implements the ActionListener interface. Whenever this is called the
     * layout will be interrupted as soon as possible.
     */
	public void actionPerformed(ActionEvent evt) {
		noBreak = false;
	}

    /**
     * Gets the height of the area on which to layout the graph.
     */
	public int getHeight() {
		return height;
	}
    
    /**
     * Gets the width of the area on which to layout the graph.
     */
	public int getWidth() {
		return width;
	}
    
    public void setUpdate(boolean doUpdate) {
    	update = doUpdate;
    }

    public Coordinates getCoordinates(Node v) {
    	return((Coordinates)locations.get(v));
    }

    public boolean done = false;

    public boolean incrementsAreDone() {
    	return(done);
    }

  @Override
  public Node getNodeAtXY(int x, int y, double scale) {
	    x /= scale;
		y /= scale;
		Iterator<Node> nodes = graph.getNodes("All").iterator();
		Rectangle r = new Rectangle(0, 0, DrawableNode.NODE_DIAMETER, DrawableNode.NODE_DIAMETER);
		while(nodes.hasNext()){
			Node node = (Node)nodes.next();
			r.x = getX(node) - DrawableNode.NODE_DIAMETER / 2;
			r.y = getY(node) - DrawableNode.NODE_DIAMETER / 2;
			if(r.contains(x, y)){
				return node;
			}
		}
		return null;
  }

  @Override
  public void init() {
    if (this.graph.getNodeCount() <= 0) {
      return;
    }
    advancePositions();
  }

  @Override
  public int getX(Node node) {
    return (int)getCoordinates(node).getX();
  }

  @Override
  public int getY(Node node) {
    return (int)getCoordinates(node).getY();
  }
}