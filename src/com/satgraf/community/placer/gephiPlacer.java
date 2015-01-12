package com.satgraf.community.placer;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;

import org.gephi.layout.plugin.ForceVectorNodeLayoutData;

import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityNode;
import com.satlib.community.placer.AbstractPlacer;
import com.satlib.community.placer.Coordinates;
import com.satlib.graph.DrawableNode;

public class gephiPlacer extends AbstractPlacer {
	
    //Properties
    public double inertia;
    private double repulsionStrength;
    private double attractionStrength;
    private double maxDisplacement;
    private boolean freezeBalance;
    private double freezeStrength;
    private double freezeInertia;
    private double gravity;
    private double speed;
    private double cooling;
    private boolean outboundAttractionDistribution;
    private boolean adjustSizes;
    
    private HashMap<CommunityNode, ForceVectorNodeLayoutData> nodeMap = new HashMap<CommunityNode, ForceVectorNodeLayoutData>();
    private HashMap<CommunityNode, Point> locations = new HashMap<CommunityNode, Point>();

	public gephiPlacer(CommunityGraph graph) {
		super(graph);
		
		inertia = 0.1;
        setRepulsionStrength(200d);
        setAttractionStrength(10d);
        setMaxDisplacement(10d);
        setFreezeBalance(true);
        setFreezeStrength(80d);
        setFreezeInertia(0.2);
        setGravity(30d);
        setOutboundAttractionDistribution(false);
        setAdjustSizes(false);
        setSpeed(1d);
        setCooling(1d);
	}

	@Override
	public CommunityNode getNodeAtXY(int x, int y, double scale) {
		x /= scale;
		y /= scale;
		Iterator<CommunityNode> nodes = graph.getNodes("All");
		Rectangle r = new Rectangle(0, 0, DrawableNode.NODE_DIAMETER, DrawableNode.NODE_DIAMETER);
		while(nodes.hasNext()){
			CommunityNode node = (CommunityNode)nodes.next();
			r.x = getX(node);
			r.y = getY(node);
			if(r.contains(x, y)){
				return node;
			}
		}
		return null;
	}

	@Override
	public void init() {
		int i = 0;
        for (CommunityNode n : graph.getNodes()) {
        	ForceVectorNodeLayoutData layoutData = new ForceVectorNodeLayoutData();
        	layoutData.old_dx = layoutData.dx;
            layoutData.old_dy = layoutData.dy;
            layoutData.dx *= inertia;
            layoutData.dy *= inertia;
        	
        	nodeMap.put(n, layoutData);
        	
        	GridPlacer initialPlacer = new GridPlacer(this.graph);
        	initialPlacer.init();
        	
        	locations = initialPlacer.getNodePositions();
        	i++;
        }
		
        long j = 0;
        Integer iterations = 1;
        
        while (true) {
            goAlgo();
            j++;
            if (iterations != null && iterations.longValue() == j) {
                break;
            }
        }
        endAlgo();
	}

	@Override
	public int getX(CommunityNode node) {
		return (int)locations.get(node).getX();
	}

	@Override
	public int getY(CommunityNode node) {
		return (int)locations.get(node).getY();
	}

	@Override
	public int getCommunityX(int community) {
		return 0;
	}

	@Override
	public int getCommunityY(int community) {
		return 0;
	}

	@Override
	public int getCommunityWidth(int community) {
		return 0;
	}

	@Override
	public int getCommunityHeight(int community) {
		return 0;
	}

	@Override
	public String getProgressionName() {
		return "Placing Communities";
	}

	@Override
	public double getProgress() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private void endAlgo() {
		nodeMap.clear();
    }

    private void goAlgo() {
        // repulsion
        if (isAdjustSizes()) {
            for (CommunityNode n1 : graph.getNodes()) {
                for (CommunityNode n2 : graph.getNodes()) {
                    if (n1 != n2) {
                        fcBiRepulsor_noCollide(n1, n2, getRepulsionStrength() * (1 + n1.getEdgesList().size()) * (1 + n2.getEdgesList().size()));
                    }
                }
            }
        } else {
            for (CommunityNode n1 : graph.getNodes()) {
                for (CommunityNode n2 : graph.getNodes()) {
                    if (n1 != n2) {
                        fcBiRepulsor(n1, n2, getRepulsionStrength() * (1 + n2.getEdgesList().size()) * (1 + n2.getEdgesList().size()));
                    }
                }
            }
        }
        // attraction
        if (isAdjustSizes()) {
            if (isOutboundAttractionDistribution()) {
                for (CommunityEdge e : graph.getEdgesList()) {
                    CommunityNode nf = e.getStart();
                    CommunityNode nt = e.getEnd();
                    double bonus = 1;
                    bonus *= e.getWeight();
                    fcBiAttractor_noCollide(nf, nt, bonus * getAttractionStrength() / (1 + nf.getEdgesList().size()));
                }
            } else {
                for (CommunityEdge e : graph.getEdgesList()) {
                    CommunityNode nf = e.getStart();
                    CommunityNode nt = e.getEnd();
                    double bonus = 1;
                    bonus *= e.getWeight();
                    fcBiAttractor_noCollide(nf, nt, bonus * getAttractionStrength());
                }
            }
        } else {
            if (isOutboundAttractionDistribution()) {
                for (CommunityEdge e : graph.getEdgesList()) {
                    CommunityNode nf = e.getStart();
                    CommunityNode nt = e.getEnd();
                    double bonus = 1;
                    bonus *= e.getWeight();
                    fcBiAttractor(nf, nt, bonus * getAttractionStrength() / (1 + nf.getEdgesList().size()));
                }
            } else {
                for (CommunityEdge e : graph.getEdgesList()) {
                    CommunityNode nf = e.getStart();
                    CommunityNode nt = e.getEnd();
                    double bonus = 1;
                    bonus *= e.getWeight();
                    fcBiAttractor(nf, nt, bonus * getAttractionStrength());
                }
            }
        }
        // gravity
        for (CommunityNode n : graph.getNodes()) {

            double nx = locations.get(n).getX();
            double ny = locations.get(n).getY();
            double d = 0.0001 + Math.sqrt(nx * nx + ny * ny);
            double gf = 0.0001 * getGravity() * d;
            ForceVectorNodeLayoutData layoutData = nodeMap.get(n);
            layoutData.dx -= gf * nx / d;
            layoutData.dy -= gf * ny / d;
        }
        // speed
        if (isFreezeBalance()) {
            for (CommunityNode n : graph.getNodes()) {
                ForceVectorNodeLayoutData layoutData = nodeMap.get(n);
                layoutData.dx *= getSpeed() * 10f;
                layoutData.dy *= getSpeed() * 10f;
            }
        } else {
            for (CommunityNode n : graph.getNodes()) {
                ForceVectorNodeLayoutData layoutData = nodeMap.get(n);
                layoutData.dx *= getSpeed();
                layoutData.dy *= getSpeed();
            }
        }
        // apply forces
        for (CommunityNode n : graph.getNodes()) {
            ForceVectorNodeLayoutData nLayout = nodeMap.get(n);
            double d = 0.0001 + Math.sqrt(nLayout.dx * nLayout.dx + nLayout.dy * nLayout.dy);
            float ratio;
            if (isFreezeBalance()) {
                nLayout.freeze = (float) (getFreezeInertia() * nLayout.freeze + (1 - getFreezeInertia()) * 0.1 * getFreezeStrength() * (Math.sqrt(Math.sqrt((nLayout.old_dx - nLayout.dx) * (nLayout.old_dx - nLayout.dx) + (nLayout.old_dy - nLayout.dy) * (nLayout.old_dy - nLayout.dy)))));
                ratio = (float) Math.min((d / (d * (1f + nLayout.freeze))), getMaxDisplacement() / d);
            } else {
                ratio = (float) Math.min(1, getMaxDisplacement() / d);
            }
            nLayout.dx *= ratio / getCooling();
            nLayout.dy *= ratio / getCooling();
            double x = locations.get(n).getX() + nLayout.dx;
            double y = locations.get(n).getY() + nLayout.dy;

            locations.get(n).setLocation(x, y);
        }
    }

    public void setInertia(Double inertia) {
        this.inertia = inertia;
    }

    public Double getInertia() {
        return inertia;
    }

    /**
     * @return the repulsionStrength
     */
    public Double getRepulsionStrength() {
        return repulsionStrength;
    }

    /**
     * @param repulsionStrength the repulsionStrength to set
     */
    public void setRepulsionStrength(Double repulsionStrength) {
        this.repulsionStrength = repulsionStrength;
    }

    /**
     * @return the attractionStrength
     */
    public Double getAttractionStrength() {
        return attractionStrength;
    }

    /**
     * @param attractionStrength the attractionStrength to set
     */
    public void setAttractionStrength(Double attractionStrength) {
        this.attractionStrength = attractionStrength;
    }

    /**
     * @return the maxDisplacement
     */
    public Double getMaxDisplacement() {
        return maxDisplacement;
    }

    /**
     * @param maxDisplacement the maxDisplacement to set
     */
    public void setMaxDisplacement(Double maxDisplacement) {
        this.maxDisplacement = maxDisplacement;
    }

    /**
     * @return the freezeBalance
     */
    public Boolean isFreezeBalance() {
        return freezeBalance;
    }

    /**
     * @param freezeBalance the freezeBalance to set
     */
    public void setFreezeBalance(Boolean freezeBalance) {
        this.freezeBalance = freezeBalance;
    }

    /**
     * @return the freezeStrength
     */
    public Double getFreezeStrength() {
        return freezeStrength;
    }

    /**
     * @param freezeStrength the freezeStrength to set
     */
    public void setFreezeStrength(Double freezeStrength) {
        this.freezeStrength = freezeStrength;
    }

    /**
     * @return the freezeInertia
     */
    public Double getFreezeInertia() {
        return freezeInertia;
    }

    /**
     * @param freezeInertia the freezeInertia to set
     */
    public void setFreezeInertia(Double freezeInertia) {
        this.freezeInertia = freezeInertia;
    }

    /**
     * @return the gravity
     */
    public Double getGravity() {
        return gravity;
    }

    /**
     * @param gravity the gravity to set
     */
    public void setGravity(Double gravity) {
        this.gravity = gravity;
    }

    /**
     * @return the speed
     */
    public Double getSpeed() {
        return speed;
    }

    /**
     * @param speed the speed to set
     */
    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    /**
     * @return the cooling
     */
    public Double getCooling() {
        return cooling;
    }

    /**
     * @param cooling the cooling to set
     */
    public void setCooling(Double cooling) {
        this.cooling = cooling;
    }

    /**
     * @return the outboundAttractionDistribution
     */
    public Boolean isOutboundAttractionDistribution() {
        return outboundAttractionDistribution;
    }

    /**
     * @param outboundAttractionDistribution the outboundAttractionDistribution
     * to set
     */
    public void setOutboundAttractionDistribution(Boolean outboundAttractionDistribution) {
        this.outboundAttractionDistribution = outboundAttractionDistribution;
    }

    /**
     * @return the adjustSizes
     */
    public Boolean isAdjustSizes() {
        return adjustSizes;
    }

    /**
     * @param adjustSizes the adjustSizes to set
     */
    public void setAdjustSizes(Boolean adjustSizes) {
        this.adjustSizes = adjustSizes;
    }
    
    private double repulsion(double c, double dist) {
        return 0.001 * c / dist;
    }
    
    private double attraction(double c, double dist) {
        return 0.01 * -c * dist;
    }
    
    private void fcBiRepulsor_noCollide(CommunityNode N1, CommunityNode N2, double c) {
        double xDist = locations.get(N1).getX() - locations.get(N2).getX();	// distance en x entre les deux noeuds
        double yDist = locations.get(N1).getY() - locations.get(N2).getY();
        double dist = (float) Math.sqrt(xDist * xDist + yDist * yDist) - DrawableNode.NODE_DIAMETER*2;	// distance (from the border of each node)

        if (dist > 0) {
            double f = repulsion(c, dist);

            ForceVectorNodeLayoutData N1L = nodeMap.get(N1);
            ForceVectorNodeLayoutData N2L = nodeMap.get(N2);

            N1L.dx += xDist / dist * f;
            N1L.dy += yDist / dist * f;

            N2L.dx -= xDist / dist * f;
            N2L.dy -= yDist / dist * f;
        } else if (dist != 0) {
            double f = -c;	//flat repulsion

            ForceVectorNodeLayoutData N1L = nodeMap.get(N1);
            ForceVectorNodeLayoutData N2L = nodeMap.get(N2);

            N1L.dx += xDist / dist * f;
            N1L.dy += yDist / dist * f;

            N2L.dx -= xDist / dist * f;
            N2L.dy -= yDist / dist * f;
        }
    }
    
    private void fcBiRepulsor(CommunityNode N1, CommunityNode N2, double c) {
        double xDist = locations.get(N1).getX() - locations.get(N2).getX();	// distance en x entre les deux noeuds
        double yDist = locations.get(N1).getY() - locations.get(N2).getY();
        double dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);	// distance tout court

        if (dist > 0) {
            double f = repulsion(c, dist);

            ForceVectorNodeLayoutData N1L = nodeMap.get(N1);
            ForceVectorNodeLayoutData N2L = nodeMap.get(N2);

            N1L.dx += xDist / dist * f;
            N1L.dy += yDist / dist * f;

            N2L.dx -= xDist / dist * f;
            N2L.dy -= yDist / dist * f;
        }
    }
    
    private void fcBiAttractor_noCollide(CommunityNode N1, CommunityNode N2, double c) {
        double xDist = locations.get(N1).getX() - locations.get(N2).getX();	// distance en x entre les deux noeuds
        double yDist = locations.get(N1).getY() - locations.get(N2).getY();
        double dist = (float) Math.sqrt(xDist * xDist + yDist * yDist) - DrawableNode.NODE_DIAMETER*2;	// distance (from the border of each node)

        if (dist > 0) {
            double f = attraction(c, dist);

            ForceVectorNodeLayoutData N1L = nodeMap.get(N1);
            ForceVectorNodeLayoutData N2L = nodeMap.get(N2);

            N1L.dx += xDist / dist * f;
            N1L.dy += yDist / dist * f;

            N2L.dx -= xDist / dist * f;
            N2L.dy -= yDist / dist * f;
        }
    }
    
    private void fcBiAttractor(CommunityNode N1, CommunityNode N2, double c) {
        double xDist = locations.get(N1).getX() - locations.get(N2).getX();	// distance en x entre les deux noeuds
        double yDist = locations.get(N1).getY() - locations.get(N2).getY();
        double dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);	// distance tout court

        if (dist > 0) {
            double f = attraction(c, dist);

            ForceVectorNodeLayoutData N1L = nodeMap.get(N1);
            ForceVectorNodeLayoutData N2L = nodeMap.get(N2);

            N1L.dx += xDist / dist * f;
            N1L.dy += yDist / dist * f;

            N2L.dx -= xDist / dist * f;
            N2L.dy -= yDist / dist * f;
        }
    }
}
