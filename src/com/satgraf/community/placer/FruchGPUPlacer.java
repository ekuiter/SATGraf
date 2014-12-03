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
package com.satgraf.community.placer;

import com.satlib.community.CommunityEdge;
import com.satlib.community.CommunityGraph;
import com.satlib.community.CommunityGraphFactory;
import com.satlib.community.CommunityGraphFactoryFactory;
import com.satlib.community.CommunityNode;
import com.satlib.community.placer.AbstractPlacer;
import com.satlib.community.placer.Coordinates;
import com.satlib.graph.DrawableNode;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.jocl.*;
import static org.jocl.CL.*;
/**
 * Positions nodes in layout according to iterations of an implementation of the
 * Fruchmen-Reingold graph layout algorithm. See the docs to
 * <code>updateLayout</code> for the details.<p>
 *
 * The FruchGraphLayout implements the ActionListener interface to interrupt the
 * layout. This breaks out of the algorithm implementation as soon as possible,
 * but will rescale the display if appropriate. You can have the
 * FruchGraphLayout listener for RePast toolbar button presses by including
 * something like the following code inside your model class.<p>
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
 * <b>Note</b> The FruchGraphLayout is not particularly fast, although it is
 * faster than the KamadaGraphLayout. It is not meant as a "true" visualization
 * tool, but rather is intended only to provide the modeler with "sense" of the
 * network. Real analysis and visualization should be done in a tool like Pajek.
 *
 * @version $Revision: 1.1 $ $Date: 2006/06/01 18:00:34 $
 * @author Skye Bender-deMoll email:skyebend@santafe.edu
 */
public class FruchGPUPlacer extends AbstractPlacer {

  private int pad = 20;

  //number of loops before cooling starts
  private int initialIter = 30;
  private double progress = 0;
  private int maxPasses = 500;
  private double optDist = 100;
  private int updates = 0;
  private boolean rescaleLayout = true;
  private boolean firstLayout = true;
  private boolean noBreak = true;
  private int seed = 123712382;
  private boolean isSeedSet = false;

  private Collection<CommunityNode> nodeList;
  private int width = 2500;
  private int height = 2500;
  private boolean update = true;
  private Random rand = new Random(this.seed);
  private HashMap locations = new HashMap();

  public String getProgressionName() {
    return "Placing Communities";
  }

  public double getProgress() {
    return progress;
  }
  
  private static final String programSource = 
  "__kernel void "+
        "repel(__global const float *optDist,"+
        "      __global const int *nIndexes,"+
        //"      __global const int *uIndexes,"+
        "      __global const float *xPos,"+
        "      __global const float *yPos,"+
        "      __global float *xDisp,"+
        "      __global float *yDisp){"+
        "         int nodes = 1168;" +
        "         int sub = nodes - 1;"+
        "         int z = get_global_id(0);"+
        "         int i = z;"+
        "         int vi,ui;"+
        "         vi = ui = 0;"+
        "         while(i > sub - 1){"+
        "           vi ++;"+
        "           i -= sub;"+
        "           sub--;"+
        "         }"+
        "         ui = i + 1 + vi;"+
        "         int v = vi;"+
        "         int u = ui;"+
        "         float xDelta = xPos[v] - xPos[u];" +
        "         float yDelta = yPos[v] - yPos[u];"+
        "         if ((xDelta == 0) && (yDelta == 0)) {"+
        "           xDisp[z] = 0;"+
        "           yDisp[z] = 0;"+
        "           return;"+
        "         }"+
        "         float deltaLength = sqrt((xDelta * xDelta) + (yDelta * yDelta));"+
        "         float force = optDist[0] / deltaLength;"+
  
        "         xDisp[z] = (xDelta / deltaLength) * force;"+
        "         yDisp[z] = (yDelta / deltaLength) * force;"+
        "}" +
        "__kernel void "+
        "attract(__global const float *optDist,"+
        "        __global const int *edges_start,"+
        "      __global const int *edges_end,"+
        "      __global const float *xPos,"+
        "      __global const float *yPos,"+
        "      __global float *xDisp,"+
        "      __global float *yDisp){"+
        
        "             int eIndex = get_global_id(0);"+
        "             int vIndex = edges_start[eIndex];"+
        "             int uIndex = edges_end[eIndex];"+
        "             float xDelta = xPos[vIndex] - xPos[uIndex];"+
        "             float yDelta = yPos[vIndex] - yPos[uIndex];"+
        "             float deltaLength = sqrt((xDelta * xDelta) + (yDelta * yDelta));"+
        "             if (deltaLength == 0) {"+
        "               deltaLength = 0.001;"+
        "             }"+
        "             float force = (deltaLength * deltaLength) / optDist[0];"+ 
        "             xDisp[eIndex] = (xDelta / deltaLength) * force;"+
        "             yDisp[eIndex] = (yDelta / deltaLength) * force;"+
        //"             xDisp[uIndex] += (xDelta / deltaLength) * force;"+
        //"             yDisp[uIndex] += (yDelta / deltaLength) * force;"+           
        "}"+
        "__kernel void "+
        "attractAggregate(__global const int *edges,"+
        "                 __global const int *edges_start,"+
        "                 __global const int *edges_end,"+ 
        "                 __global const int *nodes,"+ 
        "                 __global const float *xDispIn,"+
        "                 __global const float *yDispIn,"+
        "                 __global float *xDispOut,"+
        "                 __global float *yDispOut){"+
        ""+
        "               int n = get_global_id(0);"+
        "               int e;"+
        "               for(e = 0; e < edges[0]; e++){"+
        "                 if(edges_start[e] == n){" +
        "                   xDispOut[n] -= xDispIn[e];"+
        "                   yDispOut[n] -= yDispIn[e];"+
        "                 }"+
        "                 else if(edges_end[e] == n){"+
        "                   xDispOut[n] += xDispIn[e];"+
        "                   yDispOut[n] += yDispIn[e];"+
        "                 }"+
        "               }"+  
        "}"+
        "__kernel void "+
        "repelAggregate(__global const int *pairs,"+
        "                 __global const int *nodes,"+ 
        "                 __global const float *xDispIn,"+
        "                 __global const float *yDispIn,"+
        "                 __global float *xDispOut,"+
        "                 __global float *yDispOut){"+
        ""+
        "               int n = get_global_id(0);"+
        "               int e;"+
        "               for(e = 0; e < pairs[0]; e++){"+
        "                 if(nodes[e] == n){" +
        "                   xDispOut[n] += xDispIn[e];"+
        "                   yDispOut[n] += yDispIn[e];"+
        "                 }"+
        "                 else if(nodes[e] == n){"+
        "                   xDispOut[n] -= xDispIn[e];"+
        "                   yDispOut[n] -= yDispIn[e];"+
        "                 }"+
        "               }"+  
        "}";
  Pointer srcNodes;
  //Pointer srcPairsStart;
  //Pointer srcPairsEnd;
  Pointer srcEdgesStart;
  Pointer srcEdgesEnd;
  Pointer srcNNodes;
  Pointer srcNPairs;
  Pointer srcNEdges;
  Pointer srcAttractOd;
  Pointer srcRepelOd;
  cl_mem memNodes;
  //cl_mem memPairsStart;
  //cl_mem memPairsEnd;
  cl_mem memEdgesStart;
  cl_mem memEdgesEnd;
  cl_mem memNNodes;
  //cl_mem memNPairs;
  cl_mem memNEdges;
  cl_mem memAttractOd;
  cl_mem memRepelOd;
  private void init(int[] nodes, int[][] edges){
    int[] nnodes = new int[]{nodes.length};
    //int[] npairs = new int[]{pairs[0].length};
    int[] nedges = new int[]{edges[0].length};
    float[] repelod = new float[]{(float)Math.pow(optDist,4)};
    float[] attractod = new float[]{(float)optDist};
    
    srcNodes = Pointer.to(nodes);
    //srcPairsStart = Pointer.to(pairs[0]);
    //srcPairsEnd = Pointer.to(pairs[1]);
    srcEdgesStart = Pointer.to(edges[0]);
    srcEdgesEnd = Pointer.to(edges[1]);
    srcNNodes = Pointer.to(nnodes);
    //srcNPairs = Pointer.to(npairs);
    srcNEdges = Pointer.to(nedges);
    srcAttractOd = Pointer.to(attractod);
    srcRepelOd = Pointer.to(repelod);
    
    memNodes = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * nodes.length, srcNodes, null);
    //memPairsStart = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * pairs[0].length, srcPairsStart, null);
    //memPairsEnd = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * pairs[1].length, srcPairsEnd, null);
    memEdgesStart = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * edges[0].length, srcEdgesStart, null);
    memEdgesEnd = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * edges[1].length, srcEdgesEnd, null);
    memNNodes = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * 1, srcNNodes, null);
    //memNPairs = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * 1, srcNPairs, null);
    memNEdges = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * 1, srcNEdges, null);
    memAttractOd = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * 1, srcAttractOd, null);
    memRepelOd = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * 1, srcRepelOd, null);
  }
  
  private void oneRound(float[] xPos, float[] yPos, float[] xDisp, float[] yDisp){
    long local_work_size[] = new long[]{maxWorkItemSizes[0]};
    int work = (int)((Math.pow(xPos.length,2)/2) - (xPos.length/2));
    long repel_global_work_size[] = new long[]{work};
    long agg_global_work_size[] = new long[]{xPos.length};
    long attract_global_work_size[] = new long[]{edges[0].length};
    int status;
    
    int[] err = new int[1];
    float[] tmpRepelXDisp = new float[work];
    float[] tmpRepelYDisp = new float[work];
    float[] tmpAttractXDisp = new float[graph.getEdgesList().size()]; 
    float[] tmpAttractYDisp = new float[graph.getEdgesList().size()]; 
    Pointer srcX,srcY,dstX,dstY,tmpRepelDstXDisp,tmpRepelDstYDisp,tmpAttractDstXDisp,tmpAttractDstYDisp;
    cl_mem memObjects[] = new cl_mem[8];
    srcX = Pointer.to(xPos);
    srcY = Pointer.to(yPos);
    dstX = Pointer.to(tmpRepelXDisp);
    dstY = Pointer.to(tmpRepelYDisp);
    tmpRepelDstXDisp = Pointer.to(tmpRepelXDisp);
    tmpRepelDstYDisp = Pointer.to(tmpRepelYDisp);
    tmpAttractDstXDisp = Pointer.to(tmpAttractXDisp);
    tmpAttractDstYDisp = Pointer.to(tmpAttractYDisp);
    
    memObjects[0] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * xPos.length, srcX, err);
    memObjects[1] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * yPos.length, srcY, err);
    memObjects[2] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR , Sizeof.cl_float * tmpRepelXDisp.length, tmpRepelDstXDisp, err);
    memObjects[3] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR , Sizeof.cl_float * tmpRepelYDisp.length, tmpRepelDstYDisp, err);
    memObjects[4] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR , Sizeof.cl_float * xDisp.length, dstX, err);
    memObjects[5] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR , Sizeof.cl_float * yDisp.length, dstY, err);
    memObjects[6] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * tmpAttractXDisp.length, tmpAttractDstXDisp, null);
    memObjects[7] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * tmpAttractYDisp.length, tmpAttractDstYDisp, null);
    
    status = clSetKernelArg(repel, 0, Sizeof.cl_mem, Pointer.to(memNNodes));
    status = clSetKernelArg(repel, 1, Sizeof.cl_mem, Pointer.to(memRepelOd));
    status = clSetKernelArg(repel, 2, Sizeof.cl_mem, Pointer.to(memNodes));
    status = clSetKernelArg(repel, 3, Sizeof.cl_mem, Pointer.to(memObjects[0]));
    status = clSetKernelArg(repel, 4, Sizeof.cl_mem, Pointer.to(memObjects[1]));
    status = clSetKernelArg(repel, 5, Sizeof.cl_mem, Pointer.to(memObjects[2]));
    status = clSetKernelArg(repel, 6, Sizeof.cl_mem, Pointer.to(memObjects[3]));
    long start = System.currentTimeMillis();
    status = clEnqueueNDRangeKernel(queue, repel, 1, null, repel_global_work_size, local_work_size, 0, null, null);
    clFinish(queue);
    
    
    /*status = clSetKernelArg(repelAggregate, 0, Sizeof.cl_mem, Pointer.to(memNPairs));
    status = clSetKernelArg(repelAggregate, 1, Sizeof.cl_mem, Pointer.to(memPairsStart));
    status = clSetKernelArg(repelAggregate, 2, Sizeof.cl_mem, Pointer.to(memPairsEnd));
    status = clSetKernelArg(repelAggregate, 3, Sizeof.cl_mem, Pointer.to(memNNodes));
    status = clSetKernelArg(repelAggregate, 4, Sizeof.cl_mem, Pointer.to(memObjects[2]));
    status = clSetKernelArg(repelAggregate, 5, Sizeof.cl_mem, Pointer.to(memObjects[3]));
    status = clSetKernelArg(repelAggregate, 6, Sizeof.cl_mem, Pointer.to(memObjects[4]));
    status = clSetKernelArg(repelAggregate, 7, Sizeof.cl_mem, Pointer.to(memObjects[5]));
    status = clEnqueueNDRangeKernel(queue, repelAggregate, 1, null, agg_global_work_size, local_work_size, 0, null, null);
    clFinish(queue);*/
        
    status = clSetKernelArg(attract, 0, Sizeof.cl_mem, Pointer.to(memAttractOd));
    status = clSetKernelArg(attract, 1, Sizeof.cl_mem, Pointer.to(memEdgesStart));
    status = clSetKernelArg(attract, 2, Sizeof.cl_mem, Pointer.to(memEdgesEnd));
    status = clSetKernelArg(attract, 3, Sizeof.cl_mem, Pointer.to(memObjects[0]));
    status = clSetKernelArg(attract, 4, Sizeof.cl_mem, Pointer.to(memObjects[1]));
    status = clSetKernelArg(attract, 5, Sizeof.cl_mem, Pointer.to(memObjects[6]));
    status = clSetKernelArg(attract, 6, Sizeof.cl_mem, Pointer.to(memObjects[7]));
    status = clEnqueueNDRangeKernel(queue, attract, 1, null, attract_global_work_size, local_work_size, 0, null, null);
    clFinish(queue);
    
    
    /*status = clSetKernelArg(attractAggregate, 0, Sizeof.cl_mem, Pointer.to(memNEdges));
    status = clSetKernelArg(attractAggregate, 1, Sizeof.cl_mem, Pointer.to(memEdgesStart));
    status = clSetKernelArg(attractAggregate, 2, Sizeof.cl_mem, Pointer.to(memEdgesEnd));
    status = clSetKernelArg(attractAggregate, 3, Sizeof.cl_mem, Pointer.to(memNNodes));
    status = clSetKernelArg(attractAggregate, 4, Sizeof.cl_mem, Pointer.to(memObjects[6]));
    status = clSetKernelArg(attractAggregate, 5, Sizeof.cl_mem, Pointer.to(memObjects[7]));
    status = clSetKernelArg(attractAggregate, 6, Sizeof.cl_mem, Pointer.to(memObjects[4]));
    status = clSetKernelArg(attractAggregate, 7, Sizeof.cl_mem, Pointer.to(memObjects[5]));
    status = clEnqueueNDRangeKernel(queue, attractAggregate, 1, null, agg_global_work_size, local_work_size, 0, null, null);
    clFinish(queue);*/
    
    
    status = clEnqueueReadBuffer(queue, memObjects[6], CL_TRUE, 0, Sizeof.cl_float * xDisp.length, dstX, 0, null, null);
    clFinish(queue);
    status = clEnqueueReadBuffer(queue, memObjects[7], CL_TRUE, 0, Sizeof.cl_float * yDisp.length, dstY, 0, null, null);
    clFinish(queue);
    long end = System.currentTimeMillis();
    System.out.printf("%f seconds\n", ((double)end - (double)start) / (double)1000);
  }
  
  /*private void repelAggregate(int[][] pairs, int[]nodes, float[] repelXDisp, float[] repelYDisp, float[] xDisp, float[] yDisp){
  long global_work_size[] = new long[]{nodes.length};
    long local_work_size[] = new long[]{maxWorkItemSizes[0]};
    cl_mem memObjects[] = new cl_mem[8];
    int[] od = new int[]{pairs[0].length};
    
    Pointer srcOpt = Pointer.to(od);
    Pointer srcVNodes = Pointer.to(pairs[0]);
    Pointer srcUNodes = Pointer.to(pairs[1]);
    Pointer srcNodes = Pointer.to(nodes);
    Pointer srcX = Pointer.to(repelXDisp);
    Pointer srcY = Pointer.to(repelYDisp);
    Pointer dstX = Pointer.to(xDisp);
    Pointer dstY = Pointer.to(yDisp);
    memObjects[0] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * 1, srcOpt, null);
    memObjects[1] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * pairs[0].length, srcVNodes, null);
    memObjects[2] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * pairs[1].length, srcUNodes, null);
    memObjects[3] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * nodes.length, srcNodes, null);
    memObjects[4] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * repelXDisp.length, srcX, null);
    memObjects[5] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * repelYDisp.length, srcY, null);
    memObjects[6] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * xDisp.length, dstX, null);
    memObjects[7] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * yDisp.length, dstY, null);
    
    clSetKernelArg(repelAggregate, 0, Sizeof.cl_mem, Pointer.to(memObjects[0]));
    clSetKernelArg(repelAggregate, 1, Sizeof.cl_mem, Pointer.to(memObjects[1]));
    clSetKernelArg(repelAggregate, 2, Sizeof.cl_mem, Pointer.to(memObjects[2]));
    clSetKernelArg(repelAggregate, 3, Sizeof.cl_mem, Pointer.to(memObjects[3]));
    clSetKernelArg(repelAggregate, 4, Sizeof.cl_mem, Pointer.to(memObjects[4]));
    clSetKernelArg(repelAggregate, 5, Sizeof.cl_mem, Pointer.to(memObjects[5]));
    clSetKernelArg(repelAggregate, 6, Sizeof.cl_mem, Pointer.to(memObjects[6]));
    clSetKernelArg(repelAggregate, 7, Sizeof.cl_mem, Pointer.to(memObjects[7]));
    long start = System.currentTimeMillis();
    clEnqueueNDRangeKernel(queue, repelAggregate, 1, null, global_work_size, local_work_size, 0, null, null);
    int status = clEnqueueReadBuffer(queue, memObjects[6], CL_TRUE, 0, Sizeof.cl_float * xDisp.length, dstX, 0, null, null);
    clFinish(queue);
    status = clEnqueueReadBuffer(queue, memObjects[7], CL_TRUE, 0, Sizeof.cl_float * yDisp.length, dstY, 0, null, null);
    clFinish(queue);
    long end = System.currentTimeMillis();
    System.out.printf("%d Seconds\n", (end - start) / 1000);
    clReleaseMemObject(memObjects[0]);
    clReleaseMemObject(memObjects[1]);
    clReleaseMemObject(memObjects[2]);
    clReleaseMemObject(memObjects[3]);
    clReleaseMemObject(memObjects[4]);
    clReleaseMemObject(memObjects[5]);
    clReleaseMemObject(memObjects[6]);
  }
  private void attractAggregate(int[][] edges, int[]nodes, float[] attractXDisp, float[] attractYDisp, float[] xDisp, float[] yDisp){
  long global_work_size[] = new long[]{nodes.length};
    long local_work_size[] = new long[]{maxWorkItemSizes[0]};
    cl_mem memObjects[] = new cl_mem[8];
    int[] od = new int[]{edges[0].length};
    
    Pointer srcOpt = Pointer.to(od);
    Pointer srcStartEdges = Pointer.to(edges[0]);
    Pointer srcEndEdges = Pointer.to(edges[1]);
    Pointer srcNodes = Pointer.to(nodes);
    Pointer srcX = Pointer.to(attractXDisp);
    Pointer srcY = Pointer.to(attractYDisp);
    Pointer dstX = Pointer.to(xDisp);
    Pointer dstY = Pointer.to(yDisp);
    memObjects[0] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * 1, srcOpt, null);
    memObjects[1] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * edges[0].length, srcStartEdges, null);
    memObjects[2] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * edges[1].length, srcEndEdges, null);
    memObjects[3] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * nodes.length, srcNodes, null);
    memObjects[4] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * attractXDisp.length, srcX, null);
    memObjects[5] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * attractYDisp.length, srcY, null);
    memObjects[6] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * xDisp.length, dstX, null);
    memObjects[7] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * yDisp.length, dstY, null);
    
    clSetKernelArg(attractAggregate, 0, Sizeof.cl_mem, Pointer.to(memObjects[0]));
    clSetKernelArg(attractAggregate, 1, Sizeof.cl_mem, Pointer.to(memObjects[1]));
    clSetKernelArg(attractAggregate, 2, Sizeof.cl_mem, Pointer.to(memObjects[2]));
    clSetKernelArg(attractAggregate, 3, Sizeof.cl_mem, Pointer.to(memObjects[3]));
    clSetKernelArg(attractAggregate, 4, Sizeof.cl_mem, Pointer.to(memObjects[4]));
    clSetKernelArg(attractAggregate, 5, Sizeof.cl_mem, Pointer.to(memObjects[5]));
    clSetKernelArg(attractAggregate, 6, Sizeof.cl_mem, Pointer.to(memObjects[6]));
    clSetKernelArg(attractAggregate, 7, Sizeof.cl_mem, Pointer.to(memObjects[7]));
    long start = System.currentTimeMillis();
    clEnqueueNDRangeKernel(queue, attractAggregate, 1, null, global_work_size, local_work_size, 0, null, null);
    int status = clEnqueueReadBuffer(queue, memObjects[6], CL_TRUE, 0, Sizeof.cl_float * xDisp.length, dstX, 0, null, null);
    clFinish(queue);
    status = clEnqueueReadBuffer(queue, memObjects[7], CL_TRUE, 0, Sizeof.cl_float * yDisp.length, dstY, 0, null, null);
    clFinish(queue);
    long end = System.currentTimeMillis();
    System.out.printf("%d Seconds\n", (end - start) / 1000);
    clReleaseMemObject(memObjects[0]);
    clReleaseMemObject(memObjects[1]);
    clReleaseMemObject(memObjects[2]);
    clReleaseMemObject(memObjects[3]);
    clReleaseMemObject(memObjects[4]);
    clReleaseMemObject(memObjects[5]);
    clReleaseMemObject(memObjects[6]);
  }*/
  
  
  
  private void repel(float[] xPos, float[] yPos, float[] xDisp, float[] yDisp){
    int work = (int)((Math.pow(xPos.length,2)/2) - (xPos.length/2));
    long global_work_size[] = new long[]{work};
    long local_work_size[] = new long[]{maxWorkItemSizes[0]};
    int[] err = new int[1];
    Pointer srcX,srcY,dstX,dstY, srcvNodes, srcuNodes;
    cl_mem memObjects[] = new cl_mem[7];
    srcX = Pointer.to(xPos);
    srcY = Pointer.to(yPos);
    dstX = Pointer.to(xDisp);
    dstY = Pointer.to(yDisp);
    //memObjects[0] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * 1, srcRepelOd, err);
    //memObjects[1] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * pairs[0].length, srcvNodes, err);
    //memObjects[2] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * pairs[1].length, srcuNodes, err);
    memObjects[3] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * xPos.length, srcX, err);
    memObjects[4] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * yPos.length, srcY, err);
    memObjects[5] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR , Sizeof.cl_float * xDisp.length, dstX, err);
    memObjects[6] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR , Sizeof.cl_float * yDisp.length, dstY, err);
    
    int status = clSetKernelArg(repel, 0, Sizeof.cl_mem, Pointer.to(memRepelOd));
    status = clSetKernelArg(repel, 1, Sizeof.cl_mem, Pointer.to(memNodes));
    //status = clSetKernelArg(repel, 2, Sizeof.cl_mem, Pointer.to(memPairsEnd));
    status = clSetKernelArg(repel, 2, Sizeof.cl_mem, Pointer.to(memObjects[3]));
    status = clSetKernelArg(repel, 3, Sizeof.cl_mem, Pointer.to(memObjects[4]));
    status = clSetKernelArg(repel, 4, Sizeof.cl_mem, Pointer.to(memObjects[5]));
    status = clSetKernelArg(repel, 5, Sizeof.cl_mem, Pointer.to(memObjects[6]));
    long start = System.currentTimeMillis();
    status = clEnqueueNDRangeKernel(queue, repel, 1, null, global_work_size, local_work_size, 0, null, null);
    clFinish(queue);
    clEnqueueReadBuffer(queue, memObjects[5], CL_TRUE, 0, Sizeof.cl_float * xDisp.length, dstX, 0, null, null);
    clFinish(queue);
    clEnqueueReadBuffer(queue, memObjects[6], CL_TRUE, 0, Sizeof.cl_float * yDisp.length, dstY, 0, null, null);
    clFinish(queue);
    long end = System.currentTimeMillis();
    System.out.printf("%d Seconds\n", (end - start) / 1000);
    clReleaseMemObject(memObjects[3]);
    clReleaseMemObject(memObjects[4]);
    clReleaseMemObject(memObjects[5]);
    clReleaseMemObject(memObjects[6]);
  }
  
  
  private void attract(float[] xPos, float[] yPos, float[] xDisp, float[] yDisp){
    long global_work_size[] = new long[]{edges[0].length};
    long local_work_size[] = new long[]{maxWorkItemSizes[0]};
    cl_mem memObjects[] = new cl_mem[7];
    float[] od = new float[]{Float.parseFloat(String.format("%f",optDist))};
    
    Pointer srcX = Pointer.to(xPos);
    Pointer srcY = Pointer.to(yPos);
    Pointer dstX = Pointer.to(xDisp);
    Pointer dstY = Pointer.to(yDisp);
    //memObjects[0] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * 1, srcOpt, null);
    //memObjects[1] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * edges[0].length, srcStartEdges, null);
    //memObjects[2] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * edges[1].length, srcEndEdges, null);
    memObjects[3] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * xPos.length, srcX, null);
    memObjects[4] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * yPos.length, srcY, null);
    memObjects[5] = clCreateBuffer(context, CL_MEM_WRITE_ONLY, Sizeof.cl_float * xDisp.length, null, null);
    memObjects[6] = clCreateBuffer(context, CL_MEM_WRITE_ONLY, Sizeof.cl_float * yDisp.length, null, null);
    
    clSetKernelArg(attract, 0, Sizeof.cl_mem, Pointer.to(memAttractOd));
    clSetKernelArg(attract, 1, Sizeof.cl_mem, Pointer.to(memEdgesStart));
    clSetKernelArg(attract, 2, Sizeof.cl_mem, Pointer.to(memEdgesEnd));
    clSetKernelArg(attract, 3, Sizeof.cl_mem, Pointer.to(memObjects[3]));
    clSetKernelArg(attract, 4, Sizeof.cl_mem, Pointer.to(memObjects[4]));
    clSetKernelArg(attract, 5, Sizeof.cl_mem, Pointer.to(memObjects[5]));
    clSetKernelArg(attract, 6, Sizeof.cl_mem, Pointer.to(memObjects[6]));
    long start = System.currentTimeMillis();
    clEnqueueNDRangeKernel(queue, attract, 1, null, global_work_size, local_work_size, 0, null, null);
    int status = clEnqueueReadBuffer(queue, memObjects[5], CL_TRUE, 0, Sizeof.cl_float * xDisp.length, dstX, 0, null, null);
    clFinish(queue);
    status = clEnqueueReadBuffer(queue, memObjects[6], CL_TRUE, 0, Sizeof.cl_float * yDisp.length, dstY, 0, null, null);
    clFinish(queue);
    long end = System.currentTimeMillis();
    System.out.printf("%d Seconds\n", (end - start) / 1000);
    clReleaseMemObject(memObjects[3]);
    clReleaseMemObject(memObjects[4]);
    clReleaseMemObject(memObjects[5]);
    clReleaseMemObject(memObjects[6]);
  }
  
  private cl_command_queue queue;
  private cl_program program;
  private cl_context context;
  private cl_kernel attract;
  private cl_kernel attractAggregate;
  private cl_kernel repelAggregate;
  private cl_kernel repel;
  long[] maxWorkGroupSize = new long[1];
  long[] maxDimensions = new long[1];
  long[] maxWorkItemSizes;
  private void GPUSetup(){
    CL.setExceptionsEnabled(true);
    int numPlatformsArray[] = new int[1];
    clGetPlatformIDs(0, null, numPlatformsArray);
    int numPlatforms = numPlatformsArray[0];
    
    cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
    clGetPlatformIDs(platforms.length, platforms, null);
    cl_platform_id platform = platforms[0];
    
    cl_context_properties contextProperties = new cl_context_properties();
    contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

    // Obtain the number of devices for the platform
    int numDevicesArray[] = new int[1];
    clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, 0, null, numDevicesArray);
    int numDevices = numDevicesArray[0];

    // Obtain a device ID 
    cl_device_id devices[] = new cl_device_id[numDevices];
    clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, numDevices, devices, null);
    cl_device_id device = devices[1];

    // Create a context for the selected device
    context = clCreateContext(
        contextProperties, 1, new cl_device_id[]{device}, 
        null, null, null);

    // Create a command-queue for the selected device
    queue = clCreateCommandQueue(context, device, 0, null);
    
    program = clCreateProgramWithSource(context, 1, new String[]{ programSource }, null, null);
    // Build the program
    clBuildProgram(program, 0, null, null, null, null);
    attract = clCreateKernel(program, "attract", null);
    attractAggregate = clCreateKernel(program, "attractAggregate", null);
    repelAggregate = clCreateKernel(program, "repelAggregate", null);
    repel = clCreateKernel(program, "repel", null);
    
    
    clGetDeviceInfo(
        devices[1], CL_DEVICE_MAX_WORK_GROUP_SIZE, Sizeof.size_t,
        Pointer.to(maxWorkGroupSize), null);

    clGetDeviceInfo(devices[1], CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS, Sizeof.cl_uint,Pointer.to(maxDimensions), null);
    maxWorkItemSizes = new long[(int)maxDimensions[0]];
    clGetDeviceInfo(devices[1], CL_DEVICE_MAX_WORK_ITEM_SIZES, Sizeof.size_t * maxDimensions[0], Pointer.to(maxWorkItemSizes), null);
  }
  
  

  public FruchGPUPlacer(CommunityGraph g) {
    super(g);
    nodeList = g.getNodes();
  }

  /**
   * Sets the number of pixels to shrink radius by. Java draws object from top
   * left hand corner and this allows objects drawn on the far right to be
   * visible.
   *
   * @param p the number of pixels to shrink by
   */
  public void setPad(int p) {
    pad = p;
  }

  /**
   * If the layout has been passed a display to update, and updateEveryN is
   * greater than 0, the layout will update the display after every Nth pass
   * through the algorithm.
   *
   * @param updateEveryN how often to update the display
   */
  public void setUpdateEveryN(int updateEveryN) {
    updates = updateEveryN;
  }

  /**
   * Sets whether the completed layout will be resized to exactly fill the
   * display window. Setting rescale to false may mean that individual nodes or
   * the entire network may drift off the screen, but it will insure maximum
   * visual continuity between layouts, and minimum layout time. default is
   * true.
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
    return Math.pow(optDist, 4) / dist;
  }

  private double coolTemp(double val) {
    return val / 1.1;
  }

  /**
   * Sets the Random seed used in the intial random placement of the nodes. If
   * the seed is not set, the current timestamp is used as the seed.
   *
   * @param seed the random seed for the initial random placement of the nodes
   */
  public void setRandomSeed(int seed) {
    this.seed = seed;
    this.rand = new Random(this.seed);
    isSeedSet = true;
  }

  public double getRandomBelow(int max) {
    return rand.nextDouble() * max;
  }

  /**
   * Randomly positions nodes on layout. Called internally before update layout
   * is called for the first time to insure that nodes have starting
   * coordinates. This uses a random generator stream separate from the default
   * RePast random stream. You can set the seed for this stream using the
   * setRandomSeed method.
   */
  public void randomizeLayout() {
    System.out.println("randomizing layout");
		//MersenneTwister mt = null;
    //if (isSeedSet) mt = new MersenneTwister(seed);
    //else mt = new MersenneTwister(new Date());
    //Uniform uni = new Uniform(mt);

    int xLimit = width - pad;
    int yLimit = height - pad;
    Iterator<CommunityNode> it = nodeList.iterator();
    while (it.hasNext()) {
      CommunityNode node = it.next();
      Coordinates c = (Coordinates) locations.get(node);
      if (c == null) {
        c = new Coordinates(0, 0);
        locations.put(node, c);
      }
      c.setX(getRandomBelow(xLimit));
      c.setY(getRandomBelow(yLimit));
    }
  }

  double maxWidth = 100;
  double maxHeight = 100;
  //private int[][] pairs;
  private int[][] edges;
  public void advancePositions() {
    if (done) {
      return;
    }

    if (update) {
      long start = System.currentTimeMillis();
      noBreak = true;

      Object[] nl = nodeList.toArray();

      // calc constants
      optDist = 0.46 * Math.sqrt(((width * height) / (nl.length + 1)));
      double temp = width / 10;
      int passes = 0;
      int nNodes = nl.length;
      double xDelta = 0;
      double yDelta = 0;
      double deltaLength = 0;
      double force = 0;
      HashMap<CommunityNode, Integer> nodeIndexer = new HashMap<>();

      if (firstLayout) {
        //make sure nodes have random initial coord to begin with
        randomizeLayout();
        firstLayout = false;
        progress = 0.1;
      }

      Iterator<CommunityNode> it = nodeList.iterator();
      while (it.hasNext()) {
        CommunityNode workNode = it.next();
        locations.put(workNode, new Coordinates(getX(workNode), getY(workNode)));
      }

      //make arrays corresponding to the coords of each node
      float[] xPos = new float[nNodes];
      float[] yPos = new float[nNodes];
      boolean[] fixed = new boolean[nNodes];

      for (int i = 0; i < nNodes; i++) {
        CommunityNode workNode = (CommunityNode) nl[i];
        xPos[i] = getX(workNode);
        yPos[i] = getY(workNode);
        maxWidth = Math.max(maxWidth, DrawableNode.NODE_DIAMETER + DrawableNode.NODE_X_SPACING);
        maxHeight = Math.max(maxHeight, DrawableNode.NODE_DIAMETER + DrawableNode.NODE_X_SPACING);
        //fixed[i] = workNode.__getattr_Fixed();
        fixed[i] = false;
        nodeIndexer.put(workNode, i);
      }

      edges = new int[2][graph.getEdgesList().size()];
      int count = 0;
      TIntObjectHashMap<TIntArrayList> edgeUsageIndexer = new TIntObjectHashMap<>();
      for(CommunityEdge e : graph.getEdgesList()){
        int v = nodeIndexer.get(e.getStart());
        int u = nodeIndexer.get(e.getEnd());
        if(edgeUsageIndexer.get(v) == null){
          edgeUsageIndexer.put(v, new TIntArrayList());
        }
        if(edgeUsageIndexer.get(u) == null){
          edgeUsageIndexer.put(u, new TIntArrayList());
        }
        edgeUsageIndexer.get(v).add(count);
        edgeUsageIndexer.get(u).add(count);
        edges[0][count] = v;
        edges[1][count] = u;
        count++;
      }
      HashMap<Integer, ArrayList<Integer>> pairUsageindexes = new HashMap<>();
      TIntArrayList vNode = new TIntArrayList();
      TIntArrayList uNode = new TIntArrayList();
      count = 0;
      for(int v = 0; v < nNodes - 1; v++){
        for(int u = v + 1; u < nNodes; u++){
          if(pairUsageindexes.get(v) == null){
            pairUsageindexes.put(v, new ArrayList<Integer>());
          }
          if(pairUsageindexes.get(u) == null){
            pairUsageindexes.put(u, new ArrayList<Integer>());
          }
          pairUsageindexes.get(v).add(count);
          pairUsageindexes.get(u).add(count);
          vNode.add(v);
          uNode.add(u);
          count++;
        }
      }
      float[] repelXDisp = new float[vNode.size()]; 
      float[] repelYDisp = new float[vNode.size()]; 
      float[] attractXDisp = new float[graph.getEdgesList().size()]; 
      float[] attractYDisp = new float[graph.getEdgesList().size()]; 
		    //make arrays corresponding to the displacement vector for
      //each node
      float[] xDisp = new float[nNodes];
      float[] yDisp = new float[nNodes];
      int[] nodes = edgeUsageIndexer.keys();

		    // keep passing through the layout loop until the temp is
      // low initialIter + time for cooling schedule
      init(nodes, edges);
      long end = System.currentTimeMillis();
      System.out.printf("Setup: %f Seconds\n",(((double)end)-(double)start) / 1000);
      int[][] pairs = new int[2][];
      pairs[0] = vNode.toArray();
      pairs[1] = uNode.toArray();
      while ((temp > 1) && (passes < maxPasses) && noBreak) {
        //calculate repulsive forces between each pair of nodes (set both)
        //oneRound(xPos, yPos, xDisp, yDisp);
        start = System.currentTimeMillis();
        repel(xPos, yPos, repelXDisp, repelYDisp);
        end = System.currentTimeMillis();
        System.out.printf("rebel: %f Seconds\n",(((double)end)-(double)start) / 1000);
        start = System.currentTimeMillis();
        
        int _v = 0,_u = 0 ;
        for(int i = 0; i < repelXDisp.length; i++){
          int index = i;
          int vi = 0;
          int ui = 0;
          int sub = nNodes - 1;
          while(index > sub - 1){
            index -= sub;
            sub--;
            vi++;
          }
          ui = index+1+vi;
          try{
            xDisp[vi] += repelXDisp[i];
            yDisp[vi] += repelYDisp[i];
            xDisp[ui] -= repelXDisp[i];
            yDisp[ui] -= repelYDisp[i];
          }
          catch(IndexOutOfBoundsException e){
            throw e;
          }
          _u ++;
          _u = _u % 1167;
          if(_u == 0){
            _v ++;
            _u = 1 + _v;
            sub--;
          }
        }
        /*for(Integer v : pairUsageindexes.keySet()){
          xDisp[v] = 0;
          yDisp[v] = 0;
          for(Integer i : pairUsageindexes.get(v)){
            if(pairs[0][i] == v){
              xDisp[v] += repelXDisp[i];
              yDisp[v] += repelYDisp[i];
            }
            else if(pairs[1][i] == v){
              xDisp[v] -= repelXDisp[i];
              yDisp[v] -= repelYDisp[i];
            }
          }
        }*/
        end = System.currentTimeMillis();
        System.out.printf("rebel agg: %f Seconds\n",(((double)end)-(double)start) / 1000);
        //repelAggregate(pairs, nodes, repelXDisp, repelYDisp, xDisp, yDisp);
        start = System.currentTimeMillis();
        attract(xPos, yPos, attractXDisp, attractYDisp);
        end = System.currentTimeMillis();
        System.out.printf("attract: %f Seconds\n",(((double)end)-(double)start) / 1000);
        start = System.currentTimeMillis();
        for(Integer v : edgeUsageIndexer.keys()){
          for(Integer i : edgeUsageIndexer.get(v).toArray()){
            if(edges[0][i] == 1167 || edges[1][i] == 1167){
              int test = 1;
            }
            if(edges[0][i] == v){
              xDisp[v] -= attractXDisp[i];
              yDisp[v] -= attractYDisp[i];
            }
            else if(edges[1][i] == v){
              xDisp[v] += attractXDisp[i];
              yDisp[v] += attractYDisp[i];
            }
          }
        }
        end = System.currentTimeMillis();
        System.out.printf("attract agg: %f Seconds\n",(((double)end)-(double)start) / 1000);
        //attractAggregate(edges, nodes, attractXDisp, attractYDisp, xDisp, yDisp);
        
        //caculate displacement, but limit max displacement to temp
        start = System.currentTimeMillis();
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
        end = System.currentTimeMillis();
        System.out.printf("position update: %f Seconds\n",(((double)end)-(double)start) / 1000);

        //cool temp
        if (passes > initialIter) {
          temp = coolTemp(temp);
        }

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
    boolean[][] cells = new boolean[(int) width / (int) Math.rint(maxWidth) + 10][(int) height / (int) Math.rint(maxHeight) + 10];

    for (int i = 0; i < nNodes; i++) {
      CommunityNode node = (CommunityNode) nl[i];
      //System.out.println("updating..." + node.getX() + " " + xPos[i]);
      int ci = (int) xPos[i] / (int) Math.rint(maxWidth);
      int cj = (int) yPos[i] / (int) Math.rint(maxHeight);
      boolean found = false;
      if (cells[ci][cj]) {
        //System.out.println(ci + " " + cj);
        for (int t = 1; t <= 4; t++) {
          for (int s = 0; s <= t; s++) {
            ci = Math.min(Math.max(0, ci + t), cells.length - 1);
            cj = Math.min(Math.max(0, cj + s), cells[ci].length - 1);
            //System.out.println("\t"+ ci + " " + cj);
            if (!cells[ci][cj]) {
              found = true;
              break;
            }
            ci = Math.min(Math.max(0, ci - t), cells.length - 1);
            cj = Math.min(Math.max(0, cj - s), cells[ci].length - 1);
            //System.out.println("\t"+ ci + " " + cj);
            if (!cells[ci][cj]) {
              found = true;
              break;
            }
          }
          if (found) {
            break;
          }
        }
      }

      Coordinates c = (Coordinates) locations.get(node);
      c.setX((double) ci * Math.rint(maxWidth));
      c.setY((double) cj * Math.rint(maxHeight));

      if (cells[ci][cj]) {
        overlapping++;
        c.setX(c.getX() + r.nextInt((int) maxWidth));
        c.setY(c.getY() + r.nextInt((int) maxHeight));
      }
      cells[ci][cj] = true;
    }
    if (overlapping > 0) {
      System.out.println("\tThere are "
              + overlapping
              + " overlapping nodes, (you may want "
              + "to increase the height/width)");
    }
  }

  public double round(double a, double modx) {
    double temp = Math.rint(a);
    double temp2 = temp - temp % modx;
    //System.out.println(temp2);
    return (temp2);
  }

  /**
   * Rescales the x and y coordinates of each node so that the network will
   * maximally fill the display. Will result in some distortion. Called
   * internally if rescale is set to true, will rescale smoothly if
   * animateTransitions is true.
   *
   * @param nodes the nodes to rescale.
   */
  private void rescalePositions(Object[] nList, float[] xPos, float[] yPos) {
    //System.out.println("rescaling...");
    int nNodes = nList.length;
    //find largest coords
    float xMax = xPos[0];
    float yMax = yPos[0];
    float xMin = xPos[0];
    float yMin = yPos[0];
    for (int i = 1; i < nNodes; i++) {
      xMax = Math.max(xMax, xPos[i]);
      yMax = Math.max(yMax, yPos[i]);
      xMin = Math.min(xMin, xPos[i]);
      yMin = Math.min(yMin, yPos[i]);
    }
    //rescale coords of nodes to fit inside frame
    float xDiff = xMax - xMin;
    float yDiff = yMax - yMin;
    float xPadVal = width - pad;
    float yPadVal = height - pad;
    for (int i = 0; i < nNodes; i++) {
      xPos[i] = ((xPos[i] - xMin) / xDiff) * xPadVal;
      yPos[i] = ((yPos[i] - yMin) / yDiff) * yPadVal;
      CommunityNode node = (CommunityNode) nList[i];
      Coordinates c = (Coordinates) locations.get(node);
      c.setX(xPos[i]);
      c.setY(yPos[i]);
    }
  }

  private Set removeLoops(Set edges) {
    HashSet returnList = new HashSet();
    Iterator edgeIter = edges.iterator();
    while (edgeIter.hasNext()) {
      CommunityEdge edge = (CommunityEdge) edgeIter.next();
      if (edge == null) {
        continue;
      }
      if (edge.getStart() != edge.getEnd()) {
        returnList.add(edge);
      }
    }

    return returnList;
  }

  /**
   * Implements the ActionListener interface. Whenever this is called the layout
   * will be interrupted as soon as possible.
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

  public Coordinates getCoordinates(CommunityNode v) {
    return ((Coordinates) locations.get(v));
  }

  public boolean done = false;

  public boolean incrementsAreDone() {
    return (done);
  }

  @Override
  public CommunityNode getNodeAtXY(int x, int y, double scale) {
    x /= scale;
    y /= scale;
    Iterator<CommunityNode> nodes = graph.getNodes("All");
    Rectangle r = new Rectangle(0, 0, DrawableNode.NODE_DIAMETER, DrawableNode.NODE_DIAMETER);
    while (nodes.hasNext()) {
      CommunityNode node = (CommunityNode) nodes.next();
      r.x = getX(node);
      r.y = getY(node);
      if (r.contains(x, y)) {
        return node;
      }
    }
    return null;
  }

  @Override
  public void init() {
    GPUSetup();
    advancePositions();
  }

  @Override
  public int getX(CommunityNode node) {
    return (int) getCoordinates(node).getX();
  }

  @Override
  public int getY(CommunityNode node) {
    return (int) getCoordinates(node).getY();
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
  
  public static void main(String[] args) throws IOException{
    if(args.length == 0){
      args = new String[]{
        "formula/satcomp/dimacs/aes_16_10_keyfind_3.cnf"
      };
    }
    File input = new File(args[0]);
    CommunityGraphFactoryFactory ff = new CommunityGraphFactoryFactory("ol");
    CommunityGraphFactory f = ff.getFactory(input, new HashMap<String, String>());
    CommunityGraph g = f.makeGraph(input);
    FruchGPUPlacer placer = new FruchGPUPlacer(g);
    placer.init();
  }
}
