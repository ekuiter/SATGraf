/*
 Copyright 2008-2011 Gephi
 Authors : Mathieu Jacomy <mathieu.jacomy@gmail.com>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package com.satgraf.graph.placer;

import com.satgraf.graph.UI.GraphViewer;
import com.satgraf.graph.placer.ForceFactory.AttractionForce;
import com.satgraf.graph.placer.ForceFactory.RepulsionForce;
import com.satlib.graph.DrawableNode;
import com.satlib.graph.Edge;
import com.satlib.graph.Graph;
import com.satlib.graph.Node;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ForceAtlas 2 Layout, manages each step of the computations.
 *
 * @author Mathieu Jacomy
 */
public class ForceAtlas2 extends AbstractPlacer {
    static{
        PlacerFactory.getInstance().register("forceAtlas2", "An implementation of ForceAtlas2 from Gephi",ForceAtlas2.class);
    }
    //private GraphViewer graphModel;
    private double edgeWeightInfluence;
    private double jitterTolerance;
    private double scalingRatio;
    private double gravity;
    private double speed;
    private boolean outboundAttractionDistribution;
    private boolean adjustSizes;
    private boolean barnesHutOptimize;
    private double barnesHutTheta;
    private boolean linLogMode;
    private boolean strongGravityMode;
    private int threadCount;
    private int currentThreadCount;
    private Region rootRegion;
    private List<NodesThread> nodeThreads = new ArrayList<>();
    double outboundAttCompensation = 1;
    private int maxIterations = 5000;
    private int iterations = 0;
    
    private ExecutorCompletionService pool;

    public ForceAtlas2(Graph g) {
        super(g);
        this.threadCount = Math.min(4, Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
    }
    
    List<Node> ordered = new ArrayList<>();
    Map<Node, ForceAtlas2LayoutData> layoutData = new HashMap<>();
    private ForceAtlas2LayoutData getLayoutData(Node n){
        return layoutData.get(n);
    }

    @Override
    public void init() {
        speed = 1.;

        // Initialise layout data
        for (Node n : (Collection<Node>)graph.getNodes()) {
            if (!layoutData.containsKey(n)) {
                ForceAtlas2LayoutData nLayout = new ForceAtlas2LayoutData();
                layoutData.put(n, nLayout);
            }
            ForceAtlas2LayoutData nLayout = getLayoutData(n);
            int deg = 0;
            for(Edge e: (Collection<Edge>)n.getEdges()){
                deg += e.getWeight();
            }
            nLayout.mass = 1 + deg;
            nLayout.old_dx = 0;
            nLayout.old_dy = 0;
            nLayout.dx = 0;
            nLayout.dy = 0;
        }

        randomizeLayout();
        resetPropertiesValues();
        pool = new ExecutorCompletionService(Executors.newFixedThreadPool(threadCount));
        currentThreadCount = threadCount;
        for(iterations = 0; iterations < maxIterations; iterations++){
            nodeThreads.clear();
            goAlgo();
        }
    }
    public void randomizeLayout() {
        System.out.println("randomizing layout");
        //MersenneTwister mt = null;
        //if (isSeedSet) mt = new MersenneTwister(seed);
        //else mt = new MersenneTwister(new Date());
        //Uniform uni = new Uniform(mt);

        Random r = new Random(123038616l);
        int xLimit = 2500 - 50;
        int yLimit = 2500 - 50;
        Iterator<Node> it = graph.getNodes().iterator();
        while(it.hasNext()) {
            Node node = it.next();
            ForceAtlas2LayoutData c = getLayoutData(node);
            c.x = r.nextInt(xLimit);
            c.y = r.nextInt(yLimit);
        }
    }
    public void goAlgo() {

        List<Node> nodes = new ArrayList<>();
        nodes.addAll(graph.getNodes());
        Collection<Edge> edges = graph.getEdges();

        for (Node n : nodes) {
        // Initialise layout data
           if (!layoutData.containsKey(n)) {
                ForceAtlas2LayoutData nLayout = new ForceAtlas2LayoutData();
                layoutData.put(n, nLayout);
            }
            ForceAtlas2LayoutData nLayout = getLayoutData(n);
            int deg = 0;
            for(Edge e: (Collection<Edge>)n.getEdges()){
                deg += e.getWeight();
            }
            nLayout.mass = 1 + deg;
            nLayout.old_dx = 0;
            nLayout.old_dy = 0;
            nLayout.dx = 0;
            nLayout.dy = 0;
        }

        // If Barnes Hut active, initialize root region
        if (isBarnesHutOptimize()) {
            rootRegion = new Region(this.layoutData);
            rootRegion.buildSubRegions();
        }

        // If outboundAttractionDistribution active, compensate.
        if (isOutboundAttractionDistribution()) {
            outboundAttCompensation = 0;
            for (Node n : nodes) {
                ForceAtlas2LayoutData nLayout = getLayoutData(n);
                outboundAttCompensation += nLayout.mass;
            }
            outboundAttCompensation /= nodes.size();
        }

        // Repulsion (and gravity)
        // NB: Muti-threaded
        RepulsionForce Repulsion = ForceFactory.builder.buildRepulsion(isAdjustSizes(), getScalingRatio());

        int taskCount = 8 * currentThreadCount;  // The threadPool Executor Service will manage the fetching of tasks and threads.
        // We make more tasks than threads because some tasks may need more time to compute.
        ArrayList<Future> threads = new ArrayList();
        for (int t = taskCount; t > 0; t--) {
            int from = (int) Math.floor(nodes.size() * (t - 1) / taskCount);
            int to = (int) Math.floor(nodes.size() * t / taskCount);
            NodesThread nt = new NodesThread(nodes, layoutData, from, to, isBarnesHutOptimize(), getBarnesHutTheta(), getGravity(), (isStrongGravityMode()) ? (ForceFactory.builder.getStrongGravity(getScalingRatio())) : (Repulsion), getScalingRatio(), rootRegion, Repulsion);
            synchronized(nodeThreads){
                nodeThreads.add(nt);
            }
            Future future = pool.submit(nt);
            threads.add(future);
        }
        for (Future future : threads) {
            try {
                future.get();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (ExecutionException ex) {
                ex.printStackTrace();
            }
        }

        // Attraction
        AttractionForce Attraction = ForceFactory.builder.buildAttraction(isLinLogMode(), isOutboundAttractionDistribution(), isAdjustSizes(), 1 * ((isOutboundAttractionDistribution()) ? (outboundAttCompensation) : (1)));
        if (getEdgeWeightInfluence() == 0) {
            for (Edge e : edges) {
                Attraction.apply(e.getStart(), getLayoutData(e.getStart()), e.getEnd(), getLayoutData(e.getEnd()),1);
            }
        } else if (getEdgeWeightInfluence() == 1) {
            for (Edge e : edges) {
                Attraction.apply(e.getStart(), getLayoutData(e.getStart()), e.getEnd(), getLayoutData(e.getEnd()), e.getWeight());
            }
        } else {
            for (Edge e : edges) {
                Attraction.apply(e.getStart(),getLayoutData(e.getStart()), e.getEnd(), getLayoutData(e.getEnd()), Math.pow(e.getWeight(), getEdgeWeightInfluence()));
            }
        }

        // Auto adjust speed
        double totalSwinging = 0d;  // How much irregular movement
        double totalEffectiveTraction = 0d;  // Hom much useful movement
        for (Node n : nodes) {
            ForceAtlas2LayoutData nLayout = getLayoutData(n);
            if (true){//!n.isFixed()) {
                double swinging = Math.sqrt(Math.pow(nLayout.old_dx - nLayout.dx, 2) + Math.pow(nLayout.old_dy - nLayout.dy, 2));
                totalSwinging += nLayout.mass * swinging;   // If the node has a burst change of direction, then it's not converging.
                totalEffectiveTraction += nLayout.mass * 0.5 * Math.sqrt(Math.pow(nLayout.old_dx + nLayout.dx, 2) + Math.pow(nLayout.old_dy + nLayout.dy, 2));
            }
        }
        // We want that swingingMovement < tolerance * convergenceMovement
        double targetSpeed = getJitterTolerance() * getJitterTolerance() * totalEffectiveTraction / totalSwinging;

        // But the speed shoudn't rise too much too quickly, since it would make the convergence drop dramatically.
        double maxRise = 0.5;   // Max rise: 50%
        speed = speed + Math.min(targetSpeed - speed, maxRise * speed);

        // Apply forces
        if (isAdjustSizes()) {
            // If nodes overlap prevention is active, it's not possible to trust the swinging mesure.
            for (Node n : nodes) {
                ForceAtlas2LayoutData nLayout = getLayoutData(n);
                if (true){//!n.isFixed()) {

                    // Adaptive auto-speed: the speed of each node is lowered
                    // when the node swings.
                    double swinging = Math.sqrt((nLayout.old_dx - nLayout.dx) * (nLayout.old_dx - nLayout.dx) + (nLayout.old_dy - nLayout.dy) * (nLayout.old_dy - nLayout.dy));
                    double factor = 0.1 * speed / (1f + speed * Math.sqrt(swinging));

                    double df = Math.sqrt(Math.pow(nLayout.dx, 2) + Math.pow(nLayout.dy, 2));
                    factor = Math.min(factor * df, 10.) / df;

                    double x = nLayout.x + nLayout.dx * factor;
                    double y = nLayout.y + nLayout.dy * factor;

                    nLayout.x = x;
                    nLayout.y = y;
                }
            }
        } else {
            for (Node n : nodes) {
                ForceAtlas2LayoutData nLayout = getLayoutData(n);
                if (true){//!n.isFixed()) {

                    // Adaptive auto-speed: the speed of each node is lowered
                    // when the node swings.
                    double swinging = Math.sqrt((nLayout.old_dx - nLayout.dx) * (nLayout.old_dx - nLayout.dx) + (nLayout.old_dy - nLayout.dy) * (nLayout.old_dy - nLayout.dy));
                    //double factor = speed / (1f + Math.sqrt(speed * swinging));
                    double factor = speed / (1f + speed * Math.sqrt(swinging));

                    double x = nLayout.x + nLayout.dx * factor;
                    double y = nLayout.y + nLayout.dy * factor;

                    nLayout.x = x;
                    nLayout.y = y;
                }
            }
        }
        for(NodesThread nt : nodeThreads){
            try {
                pool.take();
            } catch (InterruptedException ex) {
                Logger.getLogger(ForceAtlas2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }


    /*@Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String FORCEATLAS2_TUNING = NbBundle.getMessage(getClass(), "ForceAtlas2.tuning");
        final String FORCEATLAS2_BEHAVIOR = NbBundle.getMessage(getClass(), "ForceAtlas2.behavior");
        final String FORCEATLAS2_PERFORMANCE = NbBundle.getMessage(getClass(), "ForceAtlas2.performance");
        final String FORCEATLAS2_THREADS = NbBundle.getMessage(getClass(), "ForceAtlas2.threads");

        try {
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.scalingRatio.name"),
                    FORCEATLAS2_TUNING,
                    "ForceAtlas2.scalingRatio.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.scalingRatio.desc"),
                    "getScalingRatio", "setScalingRatio"));

            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.strongGravityMode.name"),
                    FORCEATLAS2_TUNING,
                    "ForceAtlas2.strongGravityMode.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.strongGravityMode.desc"),
                    "isStrongGravityMode", "setStrongGravityMode"));

            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.gravity.name"),
                    FORCEATLAS2_TUNING,
                    "ForceAtlas2.gravity.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.gravity.desc"),
                    "getGravity", "setGravity"));

            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.distributedAttraction.name"),
                    FORCEATLAS2_BEHAVIOR,
                    "ForceAtlas2.distributedAttraction.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.distributedAttraction.desc"),
                    "isOutboundAttractionDistribution", "setOutboundAttractionDistribution"));

            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.linLogMode.name"),
                    FORCEATLAS2_BEHAVIOR,
                    "ForceAtlas2.linLogMode.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.linLogMode.desc"),
                    "isLinLogMode", "setLinLogMode"));

            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.adjustSizes.name"),
                    FORCEATLAS2_BEHAVIOR,
                    "ForceAtlas2.adjustSizes.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.adjustSizes.desc"),
                    "isAdjustSizes", "setAdjustSizes"));

            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.edgeWeightInfluence.name"),
                    FORCEATLAS2_BEHAVIOR,
                    "ForceAtlas2.edgeWeightInfluence.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.edgeWeightInfluence.desc"),
                    "getEdgeWeightInfluence", "setEdgeWeightInfluence"));

            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.jitterTolerance.name"),
                    FORCEATLAS2_PERFORMANCE,
                    "ForceAtlas2.jitterTolerance.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.jitterTolerance.desc"),
                    "getJitterTolerance", "setJitterTolerance"));

            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.barnesHutOptimization.name"),
                    FORCEATLAS2_PERFORMANCE,
                    "ForceAtlas2.barnesHutOptimization.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.barnesHutOptimization.desc"),
                    "isBarnesHutOptimize", "setBarnesHutOptimize"));

            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.barnesHutTheta.name"),
                    FORCEATLAS2_PERFORMANCE,
                    "ForceAtlas2.barnesHutTheta.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.barnesHutTheta.desc"),
                    "getBarnesHutTheta", "setBarnesHutTheta"));

            properties.add(LayoutProperty.createProperty(
                    this, Integer.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.threads.name"),
                    FORCEATLAS2_THREADS,
                    "ForceAtlas2.threads.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.threads.desc"),
                    "getThreadsCount", "setThreadsCount"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties.toArray(new LayoutProperty[0]);
    }*/

    public void resetPropertiesValues() {
        int nodesCount = graph.getNodeCount();

        // Tuning
        if (nodesCount >= 100) {
            setScalingRatio(2.0);
        } else {
            setScalingRatio(10.0);
        }
        setStrongGravityMode(false);
        setGravity(1.);

        // Behavior
        setOutboundAttractionDistribution(false);
        setLinLogMode(false);
        setAdjustSizes(false);
        setEdgeWeightInfluence(1.);

        // Performance
        if (nodesCount >= 50000) {
            setJitterTolerance(10d);
        } else if (nodesCount >= 5000) {
            setJitterTolerance(1d);
        } else {
            setJitterTolerance(0.1d);
        }
        if (nodesCount >= 1000) {
            setBarnesHutOptimize(true);
        } else {
            setBarnesHutOptimize(false);
        }
        setBarnesHutTheta(1.2);
        setThreadsCount(2);
    }

    public Double getBarnesHutTheta() {
        return barnesHutTheta;
    }

    public void setBarnesHutTheta(Double barnesHutTheta) {
        this.barnesHutTheta = barnesHutTheta;
    }

    public Double getEdgeWeightInfluence() {
        return edgeWeightInfluence;
    }

    public void setEdgeWeightInfluence(Double edgeWeightInfluence) {
        this.edgeWeightInfluence = edgeWeightInfluence;
    }

    public Double getJitterTolerance() {
        return jitterTolerance;
    }

    public void setJitterTolerance(Double jitterTolerance) {
        this.jitterTolerance = jitterTolerance;
    }

    public Boolean isLinLogMode() {
        return linLogMode;
    }

    public void setLinLogMode(Boolean linLogMode) {
        this.linLogMode = linLogMode;
    }

    public Double getScalingRatio() {
        return scalingRatio;
    }

    public void setScalingRatio(Double scalingRatio) {
        this.scalingRatio = scalingRatio;
    }

    public Boolean isStrongGravityMode() {
        return strongGravityMode;
    }

    public void setStrongGravityMode(Boolean strongGravityMode) {
        this.strongGravityMode = strongGravityMode;
    }

    public Double getGravity() {
        return gravity;
    }

    public void setGravity(Double gravity) {
        this.gravity = gravity;
    }

    public Integer getThreadsCount() {
        return threadCount;
    }

    public void setThreadsCount(Integer threadCount) {
        if (threadCount < 1) {
            setThreadsCount(1);
        } else {
            this.threadCount = threadCount;
        }
    }

    public Boolean isOutboundAttractionDistribution() {
        return outboundAttractionDistribution;
    }

    public void setOutboundAttractionDistribution(Boolean outboundAttractionDistribution) {
        this.outboundAttractionDistribution = outboundAttractionDistribution;
    }

    public Boolean isAdjustSizes() {
        return true;//adjustSizes;
    }

    public void setAdjustSizes(Boolean adjustSizes) {
        this.adjustSizes = adjustSizes;
    }

    public Boolean isBarnesHutOptimize() {
        return barnesHutOptimize;
    }

    public void setBarnesHutOptimize(Boolean barnesHutOptimize) {
        this.barnesHutOptimize = barnesHutOptimize;
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
    public int getX(Node node) {
        return (int)getLayoutData(node).x;
    }

    @Override
    public int getY(Node node) {
        return (int)getLayoutData(node).y;
    }

    @Override
    public String getProgressionName() {
        return "Placing Nodes";
    }

    @Override
    public double getProgress() {
        double progress = 0;
        int size = 1;
        synchronized(nodeThreads){
            size = nodeThreads.size();
            for(NodesThread nt : nodeThreads){
                progress += nt.getProgress();
            }
        }
        return (double)iterations / (double)maxIterations;//progress / size;
    }
}