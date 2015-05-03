package com.satgraf.graph.placer;

import com.satgraf.community.placer.CommunityPlacerFactory;
import com.satgraf.graph.placer.AbstractPlacer;
import com.satgraf.graph.placer.Coordinates;
import com.satgraf.graph.placer.NetUtilities;
import com.satgraf.graph.placer.SymettricMatrix;
import com.satlib.graph.Clause;
import com.satlib.graph.DrawableNode;
import com.satlib.graph.Edge;
import com.satlib.graph.Graph;
import com.satlib.graph.Node;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

public class KKPlacer extends AbstractPlacer<Node, Graph<Node, Edge, Clause>> {
  static{
    PlacerFactory.getInstance().register("kk", KKPlacer.class);
  }
	
	//kamada-kawai algorithm vars
    private double springConst = 1;       //K in KK paper (avg. i,j distance?)
    private double minEpsilon = 1;  //target deltaM goal
    private int maxPasses = 5000;   //maximum number of inner loops
    private static int pad = 20;
    
    private boolean stop = false;    //flag to break layout

    private int width, height;
    private static int maxWidth = 3000;
    private static int maxHeight = 3000;

    private HashMap<Node, Coordinates> locations = new HashMap<>();
   
    private static final Comparator<Node> NODE_SIZE_COMPARATOR = new Comparator<Node>() {
    	
   	 	@Override
	   	public int compare(Node a, Node b) {
	   		 return (int) (b.getSize() - a.getSize());
	   	}
   	 
    };

    public KKPlacer(Graph g) {
    	this(g, maxWidth, maxHeight);
    }
    
    public KKPlacer(Graph g, int width, int height) {
    	super(g);
		this.width = width;
		this.height = height;
    }

    /**
     * Sets the minimum "spring" energy which the layout attempts to
     * achieve.  Small values mean greater accuracy, and an unknown (but
     * large) amount of additional run time. The algorithm will start
     * with an initially high epsilon value, and keep decreasing it
     * until the layout drops below epsilon, the layout stops improving,
     * or maxPasses is exceeded.  Default is 1, so setting to a higher
     * value will speed up layouts.
     * 
     * @param energy the value for the minimum epsilon
     */
    public void setMinEpsilon(double energy) {
    	minEpsilon = energy;
    }

    /**
     * Sets the "springiness" of the imaginary springs connecting the
     * nodes.  Impact on layout is not well understood, seems to control
     * how far nodes are moved each time.  Default is 1.
     *
     * @param spring the value for the spring constant in the algorithm
     */
    public void setSpringConst(double spring) {
    	springConst = spring;
    }


    /**
     * Sets the maximum number of passes the inner loop of the KK
     * algorithm will execute.  Lower values mean that the layout is
     * more likely to end before arriving at a minima, but it will break
     * more quickly when stuck in a cycle.  The number of loops needed
     * to a achieve a layout is roughly proportional to the number of
     * nodes (but not in all cases!). Default is 5000
     *
     * @param passes the maximum number of time the inner loop will
     * execute.
     */
    public void setMaxPasses(int passes) {
    	maxPasses = passes;
    }
    
  
    public String getProgressionName(){
      return "Placing Communities";
    }
    public double getProgress(){
      return 0.0;
    }
    /**
     * Will place each component as close together as possible on the graph
     * 
     * @param comp
     * @param index of current comp
     * @return x and y coordinates of bottom right corner of current comp
     */
	private void layout(Collection<Node> comp) {
    	int nNodes = comp.size();
    	
    	// calculate the origin of the circle
    	int originX = (int)(width / 2);
		int originY = (int)(height / 2);
    	
    	//calc radius
		int radius;
		if (height > width) {
			radius = (int)(width / 2) - (pad);
		} else {
			radius = (int)(height / 2) - (pad);
		}
	      
		Iterator<Node> nodes = comp.iterator();
        int i = 0;
        while(nodes.hasNext()){
        	Node node = nodes.next();
            double x = radius * Math.cos(2 * Math.PI * i / nNodes) + originX;
            double y = radius * Math.sin(2 * Math.PI * i / nNodes) + originY;
            locations.put(node,new Coordinates(x, y));
            i++;
		}
    }

    //set up matrix of spring forces between pairs using K/(d[i][j]^2)
    private SymettricMatrix calcKMatrix(SymettricMatrix distMatrix, double spring) {
		int nNodes = distMatrix.getRows();
		SymettricMatrix kMatrix = new SymettricMatrix(nNodes);
		
		for (int i = 0; i < nNodes; i++) {
		    for (int j = 0; j < nNodes; j++) {
				double distMVal = distMatrix.getValue(i, j);
				kMatrix.setValue(i, j, (spring/(distMVal * distMVal)));
		    }
		}
		return kMatrix;
    }

    //set up matrix of desired edge lengths using L*d[i][j]
    private SymettricMatrix calcLMatrix(SymettricMatrix distMatrix, double optDist) {
		int nNodes = distMatrix.getRows();
		SymettricMatrix lMatrix = new SymettricMatrix(nNodes);
		
		for (int i = 0; i < nNodes; i++) {
		    for (int j = 0; j < nNodes; j++) {
		    	lMatrix.setValue(i, j,(optDist * distMatrix.getValue(i,j)));
		    }
		}
		return lMatrix;
    }

    //calculate the diameter of the graph (longest shortest path)
    //requires that path lengths are calc'd first
    private int getDiam(SymettricMatrix distMatrix) {
		int nNodes = distMatrix.getRows();
		double graphDiam = 0;
		
		for(int i = 0;  i < nNodes; i++) {
		    for(int j = 0; j < nNodes; j++) {
		    	graphDiam = Math.max(graphDiam, distMatrix.getValue(i,j));
		    }
		}
		return (int)graphDiam;
    }


    /**
     * Positions the nodes on the layout according to the results of
     * numerous iterations of the Kamada-Kawai spring-embedding
     * algorithm.  Essentially, the network is modeled as a collection
     * of nodes connected by springs with resting lengths proportional
     * to the length of the shortest path distance between each node
     * pair.  Nodes are normally positioned in a circle, and then each
     * node in sequence is repositioned until the "energy" of all of its
     * springs are minimized to a parameter value epsilon.  The location
     * of the local minima for each node is estimated with iterations of
     * a Newtown-Raphson steepest descent method.  Repositioning ceases
     * when all nodes have energy below epsilon.  In this
     * implementation, epsilon is initialized at a high value, and than
     * decreased as in simulated annealing.  the layout SHOULD stop when
     * a low value (epsilon < 1) is reached or when energies of nodes
     * can now longer be decreased.<p>
     *
     * Note: In the current implementation the layout may not always
     * converge!  however, the maxPasses parameter can be set lower to
     * interrupt cycling layouts.  Also has not been tested/ implemented
     * on weighted graphs. The Kamada-Kawai algorithm was not intended
     * to run on disconnected graphs (graphs with multiple components.
     * The kludgy solution implemented here is to run the algorithm
     * independently on each of the components (of size > 1).  This is
     * somewhat unsatisfactory as the components will often overlap.<p>
     *
     * The KK algorithm is relatively slow, especially on the first
     * round.  However, it often discovers layouts of regularly
     * structured graphs which are "better" and more repeatable than the
     * Fruchmen-Reingold technique.  Implementation of the numerics of
     * the Newton-Raphson method follows Shawn Lorae Stutzman, Auburn
     * University, 12/12/96 <A
     * href="http://mathcs.mta.ca/research/rosebrugh/gdct/javasource.htm">
     * http://mathcs.mta.ca/research/rosebrugh/gdct/javasource.htm</A>
     * <p> Kamada, Tomihisa and Satoru Kawai (1989) "An Algorithm for
     * Drawing Undirected Graphs" <CITE> Information Processing
     * Letters</CITE> 31:7-15
     */
    public void advancePositions() {
	    stop = false;
	    layout(graph.getNodes());

	    if (graph.getNodes().size() > 1) {
	    	ArrayList<Node> comp = new ArrayList<>(graph.getNodes());
	    	Collections.sort(comp, NODE_SIZE_COMPARATOR);
            runKamadaOn(comp);
            repositionBasedOnCommunitySize(comp);
            rescaleAndReposition(comp);
        }
        
        Iterator<Edge> dummies = graph.getDummyEdges();
        while(dummies.hasNext()){
          Edge dummy = dummies.next();
          dummy.getStart().removeEdge(dummy);
          dummy.getEnd().removeEdge(dummy);
          graph.removeEdge(dummy);
          dummies = graph.getDummyEdges();
        }
        
	}
    
	private void runKamadaOn(Collection<Node> componentNodes) {
		int nNodes = componentNodes.size();
		
		//sets up the matrix of path distances
		SymettricMatrix distMatrix = NetUtilities.getAllShortPathMatrix(componentNodes); // WEIGHT APPLIED TO EACH EDGE
		
		//sets up kmatrix of forces
		SymettricMatrix kMatrix = calcKMatrix(distMatrix, springConst); // 1/ABOVE^2
		
		//calc desired distance between nodes
		double optDist = Math.min(width, height) / Math.max(getDiam(distMatrix), 1); // getDiam = GET BIGGEST 
		
		//sets up lMatrix of distance between nodes pairs
		SymettricMatrix lMatrix = calcLMatrix(distMatrix, optDist); // MULYIPLY BY L
		
		//arrays for quick acess to node coords
		double[] xPos = new double[nNodes];
		double[] yPos = new double[nNodes];
	 
		Node[] nList = new Node[nNodes];
		Iterator<Node> it = componentNodes.iterator();
		int w = 0;
		
		while(it.hasNext()) {
		    Node workNode = it.next();
		    Coordinates c = locations.get(workNode);
            xPos[w] = c.getX();
            yPos[w] = c.getY();
            nList[w] = workNode;
            w++;
		}
	
		//calc value to start minimization from (should be based on previous?)
		//figure out the initial stat to compare to at the end
		double initialEnergy = getEnergy(lMatrix, kMatrix, xPos, yPos);
		double epsilon = initialEnergy / nNodes;
		
		//figure out which node to start moving first
		double deltaM;
		int maxDeltaMIndex = 0;
		double maxDeltaM = getDeltaM(0, lMatrix, kMatrix, xPos, yPos);
		
		for (int i = 1; i < nNodes; i++) {
		    deltaM = getDeltaM(i, lMatrix, kMatrix, xPos, yPos);
		    if (deltaM > maxDeltaM) {
				maxDeltaM = deltaM;
				maxDeltaMIndex = i;
		    }
		}
	    
		int subPasses = 0;
		
		//epsilon minimizing loop
		while ((epsilon > minEpsilon) && !stop) {
		    double previousMaxDeltaM = maxDeltaM + 1;
		    // KAMADA-KAWAI LOOP: while the deltaM of the node with
		    // the largest deltaM  > epsilon..
		    while ((maxDeltaM > epsilon) && ((previousMaxDeltaM - maxDeltaM) > 0.1) && !stop) {
			    //System.out.print(".");
			    double[] deltas;
			    double moveNodeDeltaM = maxDeltaM;
	
			    //KK INNER LOOP while the node with the largest energy > epsilon...
			    while ((moveNodeDeltaM > epsilon) && !stop) {
		    
					//get the deltas which will move node towards the local minima
		    		deltas = getDeltas(maxDeltaMIndex, lMatrix, kMatrix, xPos, yPos);
					
					//set coords of node to old coords + changes
					xPos[maxDeltaMIndex] += deltas[0];
					yPos[maxDeltaMIndex] += deltas[1];
                    /*if(yPos[maxDeltaMIndex] < 0 || yPos[maxDeltaMIndex] > height){
                      yPos[maxDeltaMIndex] -= deltas[0];
                    }
                    if(xPos[maxDeltaMIndex] < 0 || xPos[maxDeltaMIndex] > width){
                      xPos[maxDeltaMIndex] -= deltas[1];
                    }*/
					
					//recalculate the deltaM of the node w/ new vals
					moveNodeDeltaM = getDeltaM(maxDeltaMIndex, lMatrix, kMatrix, xPos, yPos);
					subPasses++;
					
					if (subPasses > maxPasses) 
						stop = true;
			    }
			    
			    //recalculate deltaMs and find node with max
			    maxDeltaMIndex = 0;
			    maxDeltaM = getDeltaM(0, lMatrix, kMatrix, xPos, yPos);
			    
			    for (int i = 1; i < nNodes; i++) {
					deltaM = getDeltaM(i, lMatrix, kMatrix, xPos, yPos);
					if (deltaM > maxDeltaM) {
					    maxDeltaM = deltaM;
					    maxDeltaMIndex = i;
					}
			    }
			}
	      
		    epsilon -= epsilon / 4;
		}
	    
		//System.out.print("\n");
		for (int i = 0; i < nNodes; i++) {
			Node node = nList[i];
		    Coordinates c = locations.get(node);
            c.setX(xPos[i]);
            c.setY(yPos[i]);
		}
    }

	//the bulk of the KK inner loop, estimates location of local minima
    private double[] getDeltas(int i,SymettricMatrix lMatrix, SymettricMatrix kMatrix, double[] xPos, double[] yPos) {
    	//solve deltaM partial eqns to figure out new position for node of index i
		// where deltaM is close to 0 (or less then epsilon)
		int nNodes = lMatrix.getRows();
		double[] deltas = new double[2];  //holds x and y coords to return		
		double dx, dy, dd;
		double xPartial = 0;
		double yPartial = 0;
		double xxPartial = 0;
		double xyPartial = 0;
		double yxPartial = 0;
		double yyPartial = 0;
		for (int j = 0; j < nNodes; j++) {
		    if (i != j) {
		    	dx = xPos[i] - xPos[j];
	    		dy = yPos[i] - yPos[j];
				dd = Math.sqrt(dx*dx+dy*dy);
		
				double kMatrixVal = kMatrix.getValue(i, j);
				double lMatrixVal = lMatrix.getValue(i, j);
				double ddCubed = dd * dd * dd;
			    
				xPartial += kMatrixVal * (dx - lMatrixVal * dx / dd);
				yPartial += kMatrixVal * (dy - lMatrixVal * dy / dd);
				xxPartial += kMatrixVal * (1 - lMatrixVal * dy * dy / ddCubed);
				xyPartial += kMatrixVal * (lMatrixVal * dx * dy/ ddCubed);
				yxPartial += kMatrixVal * (lMatrixVal * dy * dx/ ddCubed);
				yyPartial += kMatrixVal * (1 - lMatrixVal *dx * dx / ddCubed);
		    }
		}
	    
		//calculate x and y position difference using partials
		deltas[0] = ((-xPartial) * yyPartial - xyPartial * (-yPartial)) / (xxPartial * yyPartial - xyPartial * yxPartial);
		deltas[1] = (xxPartial * (-yPartial) - (-xPartial) * yxPartial) / (xxPartial * yyPartial - xyPartial * yxPartial);
	    
		return deltas;
    }
	
    //returns the energy of i (looping over all other nodes)
    private double getDeltaM(int i, SymettricMatrix lMatrix, SymettricMatrix kMatrix, double[] xPos, double[] yPos) {
		int nNodes = lMatrix.getRows();
		double deltaM;
		double xPartial = 0;
		double yPartial = 0;
		double dx, dy, dd;
		for (int j = 0; j < nNodes; j++) {
		    if (i != j) {
				dx = xPos[i] - xPos[j];
				dy = yPos[i] - yPos[j];
				dd = Math.sqrt(dx*dx+dy*dy);
                /*if(dd < 50){
                  continue;
                }*/
				double kMatrixVal = kMatrix.getValue(i, j);
				double lMatrixVal = lMatrix.getValue(i, j);
				xPartial += kMatrixVal * (dx - lMatrixVal * dx / dd);
				yPartial += kMatrixVal * (dy - lMatrixVal * dy / dd);
		    }
		}
		deltaM = Math.sqrt(xPartial * xPartial + yPartial * yPartial);
		return deltaM;
    }	
	
    private double getEnergy(SymettricMatrix lMatrix, SymettricMatrix kMatrix, double[] xPos, double[] yPos) {
		int nNodes = lMatrix.getRows();
		double energy = 0;
		double dx, dy, lij;
		int limit =  nNodes - 1;
		
		//for all pairs..
		for(int i = 0; i < limit; i++) {
		    for(int j = i+1; j<nNodes; j++) {
				dx = xPos[i] - xPos[j];
				dy = yPos[i] - yPos[j];
				lij = lMatrix.getValue(i,j);
				energy += 0.5 * kMatrix.getValue(i,j) * (dx * dx + dy * dy + lij * lij - 2 * lij * Math.sqrt(dx*dx+dy*dy));
		    }
		}
		return energy;
    }
  
    /**
     * Implements the ActionListener interface. Whenever this is called the
     * layout will be interrupted as soon as possible.
     */
    public void actionPerformed(ActionEvent evt) {
    	stop = true;
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
    
    /*public double getX(Vertex n) {
        Coordinates d2d = (Coordinates)locations.get(n);
        return(d2d.getX());
    }*/

    public Coordinates getCoordinates(Node v) {
    	return((Coordinates)locations.get(v));
    }

    public boolean done = false;

    public boolean incrementsAreDone() {
    	return(done);
    }

    public void initialize_local() {
    }

    public boolean isIncremental() {
    	return(false);
    }

	@Override
	public void init() {		
      //ensure fully connected graph
	  ArrayList<Node> coms = new ArrayList<>(graph.getNodes());
	  Collections.sort(coms, NODE_SIZE_COMPARATOR);
	  
      Node rootNode = coms.iterator().next();
      Iterator<Node> nodes = graph.getNodeIterator();
      while(nodes.hasNext()){
        Node compNode = nodes.next();
        if(!graph.connected(rootNode, compNode)){
          graph.connect(rootNode, compNode, true);
        }
      }
      advancePositions();
	}

	@Override
	public int getX(Node node) {
            return (int)locations.get(node).getX();
	}
	
    public int getX(int id){
      Iterator<Node> ls = locations.keySet().iterator();
      while(ls.hasNext()){
        Node next = ls.next();
        if(next.getId() == id){
          return getX(next);
        }
      }
      return 0;
    }
	
    public int getY(int id){
      Iterator<Node> ls = locations.keySet().iterator();
      while(ls.hasNext()){
        Node next = ls.next();
        if(next.getId() == id){
          return getY(next);
        }
      }
      return 0;
    }
    
	@Override
	public Node getNodeAtXY(int x, int y, double scale) {
		x /= scale;
		y /= scale;
		Iterator<Node> nodes = graph.getNodes("All");
		Rectangle r = new Rectangle(0, 0, DrawableNode.NODE_DIAMETER, DrawableNode.NODE_DIAMETER);
		while(nodes.hasNext()){
			Node node = (Node)nodes.next();
			r.x = getX(node);
			r.y = getY(node);
			if(r.contains(x, y)){
				return node;
			}
		}
		return null;
	}

	@Override
	public int getY(Node node) {
            return (int) locations.get(node).getY();
	}
	
	public void rescaleAndReposition(Collection<Node> comp) {
		int nNodes = comp.size();
		if (nNodes <= 1) {
		    return;
		}
	
		double[] xPos = new double[nNodes];
		double[] yPos = new double[nNodes];
		Node[] nlist = new Node[nNodes];
	
		double xMax = Double.MIN_VALUE;
		double yMax = Double.MIN_VALUE;
		double xMin = Double.MAX_VALUE;
		double yMin = Double.MAX_VALUE;
		
		Iterator<Node> it = comp.iterator();
		int i = 0;
		while(it.hasNext()) {
		    nlist[i] = it.next();
		    Coordinates c = locations.get(nlist[i]);
            xPos[i] = c.getX();
            yPos[i] = c.getY();
            xMax = Math.max(xMax, xPos[i]);
            yMax = Math.max(yMax, yPos[i]);
            xMin = Math.min(xMin, xPos[i]);
            yMin = Math.min(yMin, yPos[i]);
            i++;
		}
	
		//rescale coords of nodes to fit inside frame, move to 
		//position
		for (i = 0; i < nNodes; i++) {
		    xPos[i] = ((xPos[i] - xMin) / (xMax - xMin)) * (width - pad);
		    yPos[i] = ((yPos[i] - yMin) / (yMax - yMin)) * (height - pad);
		    locations.put(nlist[i], new Coordinates(xPos[i], yPos[i]));
		}
    }
	
	private void repositionBasedOnCommunitySize(ArrayList<Node> comp) {
		int nNodes = comp.size();
		if (nNodes <= 1) {
		    return;
		}
		
		Iterator<Node> it1 = comp.iterator();
		Iterator<Node> it2;
		
		while(it1.hasNext()) {
		    Node node1 = it1.next();
		    Coordinates c1 = locations.get(node1);
		    double radius = node1.getSize()/2;
		    
		    if (radius > 0) {
		    	it2 = comp.iterator();
			    while (it2.hasNext()) {
			    	Node node2 = it2.next();
			    	if (node1 != node2) {
                                    Coordinates c2 = locations.get(node2);

                                    // Calculate exact position difference
                                    double xDiff = Math.abs(c1.getX() - c2.getX());
                                    double yDiff = Math.abs(c1.getY() - c2.getY());
                                    double theta = Math.atan(yDiff/xDiff);
                                    double xDisp = radius * Math.cos(theta);
                                    double yDisp = radius * Math.sin(theta);

                                    if (c1.getX() < c2.getX()) { // to the right
                                            c2.setX(c2.getX() + xDisp);
                                    } else {
                                            c2.setX(c2.getX() - xDisp);
                                    }
                                    if (c1.getY() < c2.getY()) { // to the bottom
                                            c2.setY(c2.getY() + yDisp);
                                    } else {
                                            c2.setY(c2.getY() - yDisp);
                                    }
			    	}
			    }
		    }
		}
	}
}
