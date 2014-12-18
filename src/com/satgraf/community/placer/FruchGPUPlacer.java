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
import com.satlib.community.placer.CommunityPlacerFactory;
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
  static{
    CommunityPlacerFactory.getInstance().register("fgpu", FruchGPUPlacer.class);
  }
  private int pad = 20;
  private static final int float_workers = 1;
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
        "      __global const float *xPos,"+
        "      __global const float *yPos,"+
        "      __global float *xDisp,"+
        "      __global float *yDisp,"+
        "      __global int *nodes," +
        "      __global int *startIndexes,"+
        "      __global int* totalWork){"+
        "         int sub = nodes[0] - 1;"+
        "         int work_dim = get_work_dim();"+
        "         int gid = 0;"+
        "         for(int i = 0; i < work_dim - 1; i++){"+
        "           gid += get_global_id(i) * get_global_size(i);"+
        "         }"+
        "         gid += get_global_id(work_dim - 1);"+
        "         gid *= "+String.valueOf(float_workers)+";"+
        "         int gid_start = gid;"+
        "         if(gid >= totalWork[0]){return;}"+
        "         int v,u,i;"+
        "         v = u = i = 0;"+
        "         float4 xDelta = (float4)(0.0f,0.0f,0.0f,0.0f);"+ 
        "         float4 yDelta = (float4)(0.0f,0.0f,0.0f,0.0f);"+
        "         int found = 0;"+
        "         for(i = 0; i < nodes[0]; i++){"+
        "           if(found == "+String.valueOf(float_workers)+"){"+
        "             break;"+
        "           }"+
        "           if(gid < startIndexes[i]){"+
        "             v = i - 1;"+
        "             u = (gid - startIndexes[i - 1]) + 1;"+
        "             gid ++;"+
        "             i--;"+
        "             if(found == 0){"+
        "               xDelta.s0 = xPos[v] - xPos[u];"+
        "               yDelta.s0 = yPos[v] - yPos[u];"+
        "             }"+
        "             if(found == 1){"+
        "               xDelta.s1 = xPos[v] - xPos[u];"+
        "               yDelta.s1 = yPos[v] - yPos[u];"+
        "             }"+
        "             if(found == 2){"+
        "               xDelta.s2 = xPos[v] - xPos[u];"+
        "               yDelta.s2 = yPos[v] - yPos[u];"+
        "             }"+
        "             if(found == 3){"+
        "               xDelta.s3 = xPos[v] - xPos[u];"+
        "               yDelta.s3 = yPos[v] - yPos[u];"+
        "             }"+
        "             found++;"+
        "           }"+
        "         }"+
        "         float4 deltaLength = sqrt((xDelta * xDelta) + (yDelta * yDelta));"+
        "         float4 _optDist = (float4)(optDist[0], optDist[0], optDist[0], optDist[0]);"+
        "         float4 force = _optDist / deltaLength;"+
        "         float4 xResult = (xDelta / deltaLength) * force;"+
        "         float4 yResult = (yDelta / deltaLength) * force;"+
        "         if ((xDelta.s0 == 0) && (yDelta.s0 == 0)) {"+
        "           xDisp[gid_start + 0] = 0;"+
        "           yDisp[gid_start + 0] = 0;"+
        "         }"+
        "         else{"+
        "           xDisp[gid_start + 0] = xResult.s0;"+
        "           yDisp[gid_start + 0] = yResult.s0;"+
        "         }"+
        "         if(" + String.valueOf(float_workers) + " > 1){"+
        "           if ((xDelta.s1 == 0) && (yDelta.s1 == 0)) {"+
        "             xDisp[gid_start + 1] = 0;"+
        "             yDisp[gid_start + 1] = 0;"+
        "           }"+
        "           else{"+
        "             xDisp[gid_start + 1] = xResult.s1;"+
        "             yDisp[gid_start + 1] = yResult.s1;"+
        "           }"+
        "         }"+
        "         if(" + String.valueOf(float_workers) + " > 2){"+
        "           if ((xDelta.s2 == 0) && (yDelta.s2 == 0)) {"+
        "             xDisp[gid_start + 2] = 0;"+
        "             yDisp[gid_start + 2] = 0;"+
        "           }"+
        "           else{"+
        "             xDisp[gid_start + 2] = xResult.s2;"+
        "             yDisp[gid_start + 2] = yResult.s2;"+
        "             }"+
        "         }"+
        "         if(" + String.valueOf(float_workers) + " > 3){"+
        "           if ((xDelta.s3 == 0) && (yDelta.s3 == 0)) {"+
        "             xDisp[gid_start + 3] = 0;"+
        "             yDisp[gid_start + 3] = 0;"+
        "           }"+
        "           else{"+
        "             xDisp[gid_start + 3] = xResult.s3;"+
        "             yDisp[gid_start + 3] = yResult.s3;"+
        "           }"+
        "         }"+
        "}\n" +
        "__kernel void "+
        "attract(__global const float *optDist,"+
        "        __global const int *edges_start,"+
        "      __global const int *edges_end,"+
        "      __global const float *xPos,"+
        "      __global const float *yPos,"+
        "      __global float *xDisp,"+
        "      __global float *yDisp,"+
        "      __global int* totalWork){"+
        "         int work_dim = get_work_dim();"+
        "         int gid = 0;"+
        "         for(int i = 0; i < work_dim - 1; i++){"+
        "           gid += get_global_id(i) * get_global_size(i);"+
        "         }"+
        "         gid += get_global_id(work_dim - 1);"+
        "         if(gid >= totalWork[0]){return;}"+
        "         int eIndex = gid;"+
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
        "}\n"+
          "__kernel void attractAggregate1(__global int* edges_start,"
          + "__global int* edges_end,"
          + "__global float* xDispIn,"
          + "__global float* yDispIn,"
          + "__global float* xDispOut,"
          + "__global float* yDispOut,"
          + "__global int* chunks,"
          + "__global int* edges,"+
        "    __global int* totalWork){"+
        "         int work_dim = get_work_dim();"+
        "         int gid = 0;"+
        "         for(int i = 0; i < work_dim - 1; i++){"+
        "           gid += get_global_id(i) * get_global_size(i);"+
        "         }"+
        "         gid += get_global_id(work_dim - 1);"+
        "         if(gid >= totalWork[0]){return;}"+
          "int chunk = gid % chunks[0];"
            + "int start = gid * chunks[0];"
            + "int end = min((gid + 1) * chunks[0],edges[0]);"
            + "for(int i = start; i < end; i++){"
            + "   int vStart = edges_start[i];"
            + "   int vEnd = edges_end[i];"
            + "   xDispOut[((vStart)* chunks[0]) + chunk] -= xDispIn[i];"
            + "   yDispOut[((vStart)* chunks[0]) + chunk] -= yDispIn[i];"
            + "   xDispOut[((vEnd)* chunks[0]) + chunk] += xDispIn[i];"
            + "   yDispOut[((vEnd)* chunks[0]) + chunk] += yDispIn[i];"
            + "}"
          + "}"
          +"\n__kernel void attractAggregate2(__global float* xDispIn,"
          + "__global float* yDispIn,"
          + "__global float* xDispOut,"
          + "__global float* yDispOut,"
          + "__global int* chunks,"+
        "    __global int* totalWork){"+
        "         int work_dim = get_work_dim();"+
        "         int gid = 0;"+
        "         for(int i = 0; i < work_dim - 1; i++){"+
        "           gid += get_global_id(i) * get_global_size(i);"+
        "         }"+
        "         gid += get_global_id(work_dim - 1);"+
        "         if(gid >= totalWork[0]){return;}"
            + "int start = gid * chunks[0];"
            + "int end = start + chunks[0];"
            + "for(int i = start; i < end; i++){"
            + "   xDispOut[gid] += xDispIn[i];"
            + "   yDispOut[gid] += yDispIn[i];"
            + "}"
          + "}"
          +"\n__kernel void repelAggregate1("
          + "__global float* xDispIn,"
          + "__global float* yDispIn,"
          + "__global float* xDispOut,"
          + "__global float* yDispOut,"
          + "__global int* nodes,"
          + "__global int* startIndexes,"+
        "    __global int* totalWork){"+
        "         int work_dim = get_work_dim();"+
        "         int gid = 0;"+
        "         for(int i = 0; i < work_dim - 1; i++){"+
        "           gid += get_global_id(i) * get_global_size(i);"+
        "         }"+
        "         gid += get_global_id(work_dim - 1);"+
        "         if(gid >= totalWork[0]){return;}" +
          " xDispOut[gid] = 0;"
          + " yDispOut[gid] = 0;"
          + " int i = startIndexes[gid];"
          + " for(int ui = gid + 1; ui < nodes[0]; ui++){"
          + "   xDispOut[gid] += xDispIn[i];"
          + "   yDispOut[gid] += yDispIn[i];"
          + "   i++;"
          + " }"
          + "}"
          +"\n__kernel void adjust("
            + "__global float* xDisp,"
            + "__global float* yDisp,"
            + "__global float* xPos,"
            + "__global float* yPos,"
            + "__global int* passes,"
            + "__global float* temp,"
            + "__global int* totalWork,"
            + "__global int* stop){"
            + " int work_dim = get_work_dim();"
            + " int gid = 0;"
            + " for(int i = 0; i < work_dim - 1; i++){"
            + "    gid += get_global_id(i) * get_global_size(i);"
            + " }"
            + " gid += get_global_id(work_dim - 1);"
            + " if(gid >= totalWork[0]){return;}"
            + " float xDispVal = xDisp[gid];"
            + " float yDispVal = yDisp[gid];"
            + " float deltaLength = sqrt((xDispVal * xDispVal) + (yDispVal * yDispVal));"
            + " if(deltaLength > temp[0]){"
            + "  xPos[gid] += xDispVal / (deltaLength / temp[0]);"
            + "  yPos[gid] += yDispVal / (deltaLength / temp[0]);"
            + " }"
            + " else{"
            + "  xPos[gid] += xDispVal;"
            + "  yPos[gid] += yDispVal;"
            + " }"
            + " xDisp[gid] = 0;"
            + " yDisp[gid] = 0;"
            + " if(passes[0] > 30){"
            + "   temp[0] = temp[0] / 1.1;"
            + " }"
            + " if(gid == 0){"
            + "  passes[0] ++;"
            + "  if(temp[0] < 1){"
            + "    stop[0] = 500;"
            + "  }"
            + "  else{"
            + "    stop[0] = passes[0];"
            + "  }"
            + " }"
            + "}";
  Pointer srcNodes;
  Pointer srcEdgesStart;
  Pointer srcEdgesEnd;
  Pointer srcNNodes;
  Pointer srcNPairs;
  Pointer srcNEdges;
  Pointer srcAttractOd;
  Pointer srcRepelOd;
  cl_mem memNodes;
  cl_mem memEdgesStart;
  cl_mem memEdgesEnd;
  cl_mem memNNodes;
  cl_mem memNEdges;
  cl_mem memAttractOd;
  cl_mem memRepelOd;
  cl_mem memRepelWorkSize;
  cl_mem memRepelAggregateWorkSize;
  cl_mem memRepelAttractWorkSize;
  cl_mem memRepelAttractAggregate1WorkSize;
  cl_mem memRepelAttractAggregate2WorkSize;
  cl_mem memAttractWorkSize;
  cl_mem memAttractAggregate1WorkSize;
  cl_mem memAttractAggregate2WorkSize;
  cl_mem memAdjustWorkSize;
  cl_mem memTemp;
  cl_mem memPasses;
  cl_mem memStop;
  int deviceIndex = 0;
  cl_mem cl_xPos, cl_yPos,cl_xDisp,cl_yDisp, cl_xInterAttract, cl_yInterAttract, cl_xInter2Attract, cl_yInter2Attract, cl_xInterRepel, cl_yInterRepel,cl_startIndexes,cl_chunks;
  Pointer dstX, dstY,srcX,srcY;
  int work;
  int[] chunks;
  float[] tmpRepelXDisp;
  float[] tmpRepelYDisp;
  float[] tmpAttractXDisp; 
  float[] tmpAttractYDisp; 
  float[] x;
  float[] y;
  private void init(int[] nodes, int[][] edges, float[] xPos, float[] yPos, float[] xDisp, float[] yDisp, int[] startIndexes){
    int[] nnodes = new int[]{nodes.length};
    //int[] npairs = new int[]{pairs[0].length};
    int[] nedges = new int[]{edges[0].length};
    float[] repelod = new float[]{(float)Math.pow(optDist,4)};
    float[] attractod = new float[]{(float)optDist};
    chunks = new int[]{100};
    
    srcNodes = Pointer.to(nodes);
    srcEdgesStart = Pointer.to(edges[0]);
    srcEdgesEnd = Pointer.to(edges[1]);
    srcNNodes = Pointer.to(nnodes);
    srcNEdges = Pointer.to(nedges);
    srcAttractOd = Pointer.to(attractod);
    srcRepelOd = Pointer.to(repelod);
    srcX = Pointer.to(xPos);
    srcY = Pointer.to(yPos);
    dstX = Pointer.to(xDisp);
    dstY = Pointer.to(yDisp);
    
    work = (int)((Math.pow(xPos.length,2)/2) - (xPos.length/2));
    int[] err = new int[1];
    
    
    memNodes = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * nodes.length, srcNodes, null);
    memEdgesStart = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * edges[0].length, srcEdgesStart, null);
    memEdgesEnd = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * edges[1].length, srcEdgesEnd, null);
    memNNodes = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * 1, srcNNodes, null);
    memNEdges = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_uint * 1, srcNEdges, null);
    memAttractOd = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * 1, srcAttractOd, null);
    memRepelOd = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * 1, srcRepelOd, null);
    memRepelWorkSize = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * 1, Pointer.to(new int[]{(int)Math.ceil((double)work / float_workers)}),null);
    memRepelAggregateWorkSize = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * 1, Pointer.to(new int[]{xDisp.length}),null);
    memAttractWorkSize = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * 1, Pointer.to(new int[]{edges[0].length}),null);
    memAttractAggregate1WorkSize = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * 1, Pointer.to(new int[]{chunks[0] * nodeList.size()}),null);
    memAttractAggregate2WorkSize = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * 1, Pointer.to(new int[]{xDisp.length}),null);
    memAdjustWorkSize = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * 1, Pointer.to(new int[]{xDisp.length}),null);
    memStop = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * 1, Pointer.to(new int[]{0}),null);
    memPasses = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * 1, Pointer.to(new int[]{0}),null);
    memTemp = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * 1, Pointer.to(new float[]{width / 10}),null);
    cl_chunks = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * 1, Pointer.to(chunks), null);
    cl_startIndexes = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * xPos.length, Pointer.to(startIndexes), null);
    
    
    
    tmpRepelXDisp = new float[work];
    tmpRepelYDisp = new float[work];
    tmpAttractXDisp = new float[graph.getEdgesList().size()]; 
    tmpAttractYDisp = new float[graph.getEdgesList().size()]; 
    x = new float[chunks[0] * nodeList.size()];
    y = new float[chunks[0] * nodeList.size()];
    
  }
  
  private long getWorkSize(long workSize){
    return (long)Math.ceil((double)workSize/maxWorkItemSizes[0])*maxWorkItemSizes[0];
  }
  
  boolean first = true;
  private boolean oneRound2(float[] xPos, float[] yPos, int[] startIndexes, float[] xDisp, float[] yDisp){
    int[] err = new int[1];
    if(first){
      cl_xPos = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * xPos.length + (int)(xPos.length * 0.1), Pointer.to(xPos), err);
      cl_yPos = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * xPos.length + (int)(yPos.length * 0.1), Pointer.to(yPos), err);
    }
    cl_xInterRepel = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR , Sizeof.cl_float * tmpRepelXDisp.length, Pointer.to(tmpRepelXDisp), err);
    cl_yInterRepel = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR , Sizeof.cl_float * tmpRepelYDisp.length, Pointer.to(tmpRepelYDisp), err);
    cl_xDisp = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR , Sizeof.cl_float * xDisp.length, dstX, err);
    cl_yDisp = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR , Sizeof.cl_float * yDisp.length, dstY, err);
    cl_xInterAttract = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * tmpAttractXDisp.length, Pointer.to(tmpAttractXDisp), err);
    cl_yInterAttract = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * tmpAttractYDisp.length, Pointer.to(tmpAttractYDisp), err);
    cl_xInter2Attract = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * x.length, Pointer.to(x), err);
    cl_yInter2Attract = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * y.length, Pointer.to(y), err);
    
    first = false;
    Pointer dstXDisp = Pointer.to(tmpRepelXDisp);
    long start = System.currentTimeMillis();
    repel(cl_xPos, cl_yPos, cl_xInterRepel, cl_yInterRepel, (int)Math.ceil((double)work / float_workers));
    clFinish(queue);
    clEnqueueReadBuffer(queue, cl_xInterRepel, CL_TRUE, 0, Sizeof.cl_float * xDisp.length, dstXDisp, 0, null, null);
    long end = System.currentTimeMillis();
    System.out.printf("repel: %f seconds\n", (double)(end - start)/ 1000);
    start = System.currentTimeMillis();
    repelAggregate(cl_xDisp, cl_yDisp, cl_xInterRepel, cl_yInterRepel, cl_startIndexes, memNNodes);
    clFinish(queue);
    end = System.currentTimeMillis();
    System.out.printf("repelAggregate: %f seconds\n", (double)(end - start)/ 1000);
    start = System.currentTimeMillis();
    attract(cl_xPos, cl_yPos, cl_xInterAttract, cl_yInterAttract);
    clFinish(queue);
    end = System.currentTimeMillis();
    System.out.printf("attract: %f seconds\n", (double)(end - start)/ 1000);
    start = System.currentTimeMillis();
    attractAggregate(cl_xDisp, cl_yDisp, cl_xInter2Attract, cl_yInter2Attract, cl_xInterAttract, cl_yInterAttract, cl_chunks, memNEdges, (long)Math.ceil((double)edges[0].length / (double)chunks[0]));
    clFinish(queue);
    end = System.currentTimeMillis();
    System.out.printf("attractAggregate: %f seconds\n", (double)(end - start)/ 1000);
    
    start = System.currentTimeMillis();
    adjustPositions(cl_xDisp, cl_yDisp, cl_xPos, cl_yPos, memPasses, memTemp);
    clFinish(queue);
    end = System.currentTimeMillis();
    System.out.printf("adjust: %f seconds\n", (double)(end - start)/ 1000);
    int[] stop = new int[]{0};
    Pointer dstStop = Pointer.to(stop);
    clEnqueueReadBuffer(queue, memStop, CL_TRUE, 0, Sizeof.cl_int * 1, dstStop, 0, null, null);
    if(stop[0] == maxPasses){
      return true;
    }
    clReleaseMemObject(cl_xInterRepel);
    clReleaseMemObject(cl_yInterRepel);
    clReleaseMemObject(cl_xDisp);
    clReleaseMemObject(cl_yDisp);
    clReleaseMemObject(cl_xInterAttract);
    clReleaseMemObject(cl_yInterAttract);
    clReleaseMemObject(cl_xInter2Attract);
    clReleaseMemObject(cl_yInter2Attract);
    return false;
    
  }
  
  private void adjustPositions(cl_mem xDisp, cl_mem yDisp, cl_mem xPos, cl_mem yPos, cl_mem passes, cl_mem temp){
    int status = clSetKernelArg(adjust, 0, Sizeof.cl_mem, Pointer.to(xDisp));
    status = status | clSetKernelArg(adjust, 1, Sizeof.cl_mem, Pointer.to(yDisp));
    status = status | clSetKernelArg(adjust, 2, Sizeof.cl_mem, Pointer.to(xPos));
    status = status | clSetKernelArg(adjust, 3, Sizeof.cl_mem, Pointer.to(yPos));
    status = status | clSetKernelArg(adjust, 4, Sizeof.cl_mem, Pointer.to(passes));
    status = status | clSetKernelArg(adjust, 5, Sizeof.cl_mem, Pointer.to(temp));
    status = status | clSetKernelArg(adjust, 6, Sizeof.cl_mem, Pointer.to(memAdjustWorkSize));
    status = status | clSetKernelArg(adjust, 7, Sizeof.cl_mem, Pointer.to(memStop));
    status = clEnqueueNDRangeKernel(queue, adjust, 1, null, new long[]{getWorkSize(nodeList.size())}, new long[]{maxWorkItemSizes[0]}, 0, null, null);
  }
  
  private void repel(cl_mem xPos, cl_mem yPos, cl_mem xDisp, cl_mem yDisp, long work){
    int status = clSetKernelArg(repel, 0, Sizeof.cl_mem, Pointer.to(memRepelOd));
    status = status | clSetKernelArg(repel, 1, Sizeof.cl_mem, Pointer.to(memNodes));
    status = status | clSetKernelArg(repel, 2, Sizeof.cl_mem, Pointer.to(xPos));
    status = status | clSetKernelArg(repel, 3, Sizeof.cl_mem, Pointer.to(yPos));
    status = status | clSetKernelArg(repel, 4, Sizeof.cl_mem, Pointer.to(xDisp));
    status = status | clSetKernelArg(repel, 5, Sizeof.cl_mem, Pointer.to(yDisp));
    status = status | clSetKernelArg(repel, 6, Sizeof.cl_mem, Pointer.to(memNNodes));
    status = status | clSetKernelArg(repel, 7, Sizeof.cl_mem, Pointer.to(cl_startIndexes));
    status = status | clSetKernelArg(repel, 8, Sizeof.cl_mem, Pointer.to(memRepelWorkSize));
    if(status != 0){
      throw new IllegalArgumentException();
    }
    status = clEnqueueNDRangeKernel(queue, repel, 1, null, new long[]{getWorkSize(work)}, new long[]{maxWorkItemSizes[0]}, 0, null, null);
  }
  
  private void repel(float[] xPos, float[] yPos, float[] xDisp, float[] yDisp){
    int[] err = new int[1];
    cl_mem memObjects[] = new cl_mem[7];
    memObjects[3] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * xPos.length, srcX, err);
    memObjects[4] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * yPos.length, srcY, err);
    memObjects[5] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR , Sizeof.cl_float * xDisp.length, dstX, err);
    memObjects[6] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR , Sizeof.cl_float * yDisp.length, dstY, err);
    
    repel(memObjects[3],memObjects[4],memObjects[5],memObjects[6],work);
    clEnqueueReadBuffer(queue, memObjects[5], CL_TRUE, 0, Sizeof.cl_float * xDisp.length, dstX, 0, null, null);
    clEnqueueReadBuffer(queue, memObjects[6], CL_TRUE, 0, Sizeof.cl_float * yDisp.length, dstY, 0, null, null);
    
    clReleaseMemObject(memObjects[3]);
    clReleaseMemObject(memObjects[4]);
    clReleaseMemObject(memObjects[5]);
    clReleaseMemObject(memObjects[6]);
  }
  
  private void attract(cl_mem xPos, cl_mem yPos, cl_mem xDisp, cl_mem yDisp){
    clSetKernelArg(attract, 0, Sizeof.cl_mem, Pointer.to(memAttractOd));
    clSetKernelArg(attract, 1, Sizeof.cl_mem, Pointer.to(memEdgesStart));
    clSetKernelArg(attract, 2, Sizeof.cl_mem, Pointer.to(memEdgesEnd));
    clSetKernelArg(attract, 3, Sizeof.cl_mem, Pointer.to(xPos));
    clSetKernelArg(attract, 4, Sizeof.cl_mem, Pointer.to(yPos));
    clSetKernelArg(attract, 5, Sizeof.cl_mem, Pointer.to(xDisp));
    clSetKernelArg(attract, 6, Sizeof.cl_mem, Pointer.to(yDisp));
    clSetKernelArg(attract, 7, Sizeof.cl_mem, Pointer.to(memAttractWorkSize));
    clEnqueueNDRangeKernel(queue, attract, 1, null, new long[]{(long)(getWorkSize(edges[0].length))}, new long[]{maxWorkItemSizes[0]}, 0, null, null);
  }
  
  private void attract(float[] xPos, float[] yPos, float[] xDisp, float[] yDisp){
    cl_mem memObjects[] = new cl_mem[7];
    memObjects[3] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * xPos.length, srcX, null);
    memObjects[4] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * yPos.length, srcY, null);
    memObjects[5] = clCreateBuffer(context, CL_MEM_WRITE_ONLY, Sizeof.cl_float * xDisp.length, null, null);
    memObjects[6] = clCreateBuffer(context, CL_MEM_WRITE_ONLY, Sizeof.cl_float * yDisp.length, null, null);
    
    attract(memObjects[3],memObjects[4],memObjects[5],memObjects[6]);
    int status = clEnqueueReadBuffer(queue, memObjects[5], CL_TRUE, 0, Sizeof.cl_float * xDisp.length, dstX, 0, null, null);
    status = clEnqueueReadBuffer(queue, memObjects[6], CL_TRUE, 0, Sizeof.cl_float * yDisp.length, dstY, 0, null, null);
    clReleaseMemObject(memObjects[3]);
    clReleaseMemObject(memObjects[4]);
    clReleaseMemObject(memObjects[5]);
    clReleaseMemObject(memObjects[6]);
  }
  
  private void repelAggregate(cl_mem xDispOut, cl_mem yDispOut, cl_mem xDispIn, cl_mem yDispIn, cl_mem startIndexes, cl_mem nodes){
    clSetKernelArg(repelAggregate1, 0, Sizeof.cl_mem, Pointer.to(xDispIn));
    clSetKernelArg(repelAggregate1, 1, Sizeof.cl_mem, Pointer.to(yDispIn));
    clSetKernelArg(repelAggregate1, 2, Sizeof.cl_mem, Pointer.to(xDispOut));
    clSetKernelArg(repelAggregate1, 3, Sizeof.cl_mem, Pointer.to(yDispOut));
    clSetKernelArg(repelAggregate1, 4, Sizeof.cl_mem, Pointer.to(nodes));
    clSetKernelArg(repelAggregate1, 5, Sizeof.cl_mem, Pointer.to(startIndexes));
    clSetKernelArg(repelAggregate1, 6, Sizeof.cl_mem, Pointer.to(memRepelAggregateWorkSize));
    int status;
    status = clEnqueueNDRangeKernel(queue, repelAggregate1, 1, null, new long[]{getWorkSize(nodeList.size())}, new long[]{maxWorkItemSizes[0]}, 0, null, null);
  }
  
  private void repelAggregate(float[] xDispOut, float[] yDispOut, float[] xDispIn, float[] yDispIn, int[] startIndexes){    
    Pointer srcX = Pointer.to(xDispIn);
    Pointer srcY = Pointer.to(yDispIn);
    Pointer dstX = Pointer.to(xDispOut);
    Pointer dstY = Pointer.to(yDispOut);
    
    int[] err = new int[1];
    cl_mem memObjects[] = new cl_mem[6];
    
    memObjects[0] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * xDispIn.length, srcX, err);
    memObjects[1] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * yDispIn.length, srcY, err);
    memObjects[2] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR , Sizeof.cl_float * xDispOut.length, dstX, err);
    memObjects[3] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR , Sizeof.cl_float * yDispOut.length, dstY, err);
    memObjects[4] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR , Sizeof.cl_int * 1, Pointer.to(new int[]{xDispOut.length}), err);
    memObjects[5] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR , Sizeof.cl_int * startIndexes.length, Pointer.to(startIndexes), err);
    repelAggregate(memObjects[2], memObjects[3], memObjects[0], memObjects[1], memObjects[5], memObjects[4]);
    clEnqueueReadBuffer(queue, memObjects[2], CL_TRUE, 0, Sizeof.cl_float * nodeList.size(), dstX, 0, null, null);
    clEnqueueReadBuffer(queue, memObjects[3], CL_TRUE, 0, Sizeof.cl_float * nodeList.size(), dstY, 0, null, null);
    
  }
  
  private void attractAggregate(cl_mem xDispOut, cl_mem yDispOut, cl_mem xInter, cl_mem yInter, cl_mem xDispIn, cl_mem yDispIn, cl_mem chunks, cl_mem edges, long work_size){
    clSetKernelArg(attractAggregate1, 0, Sizeof.cl_mem, Pointer.to(memEdgesStart));
    clSetKernelArg(attractAggregate1, 1, Sizeof.cl_mem, Pointer.to(memEdgesEnd));
    clSetKernelArg(attractAggregate1, 2, Sizeof.cl_mem, Pointer.to(xDispIn));
    clSetKernelArg(attractAggregate1, 3, Sizeof.cl_mem, Pointer.to(yDispIn));
    clSetKernelArg(attractAggregate1, 4, Sizeof.cl_mem, Pointer.to(xInter));
    clSetKernelArg(attractAggregate1, 5, Sizeof.cl_mem, Pointer.to(yInter));
    clSetKernelArg(attractAggregate1, 6, Sizeof.cl_mem, Pointer.to(chunks));
    clSetKernelArg(attractAggregate1, 7, Sizeof.cl_mem, Pointer.to(edges));
    clSetKernelArg(attractAggregate1, 8, Sizeof.cl_mem, Pointer.to(memAttractAggregate1WorkSize));
    int status;
    status = clEnqueueNDRangeKernel(queue, attractAggregate1, 1, null, new long[]{getWorkSize(work_size)}, new long[]{maxWorkItemSizes[0]}, 0, null, null);
  
    clSetKernelArg(attractAggregate2, 0, Sizeof.cl_mem, Pointer.to(xInter));
    clSetKernelArg(attractAggregate2, 1, Sizeof.cl_mem, Pointer.to(yInter));
    clSetKernelArg(attractAggregate2, 2, Sizeof.cl_mem, Pointer.to(xDispOut));
    clSetKernelArg(attractAggregate2, 3, Sizeof.cl_mem, Pointer.to(yDispOut));
    clSetKernelArg(attractAggregate2, 4, Sizeof.cl_mem, Pointer.to(chunks));
    clSetKernelArg(attractAggregate2, 5, Sizeof.cl_mem, Pointer.to(memAttractAggregate2WorkSize));
    
    status = clEnqueueNDRangeKernel(queue, attractAggregate2, 1, null, new long[]{getWorkSize(nodeList.size())}, new long[]{maxWorkItemSizes[0]}, 0, null, null);
  }
  
  private void attractAggregate(float[] xDispOut, float[] yDispOut, float[] xDispIn, float[] yDispIn){
    
    int[] chunks = new int[]{100};
    long global_work_size[] = new long[]{(long)Math.ceil((double)edges[0].length / (double)chunks[0])};
    long local_work_size[] = new long[]{maxWorkItemSizes[0]};
    
    
    float[] x = new float[chunks[0] * nodeList.size()];
    float[] y = new float[chunks[0] * nodeList.size()];
    
    Pointer srcX = Pointer.to(xDispIn);
    Pointer srcY = Pointer.to(yDispIn);
    Pointer interX = Pointer.to(x);
    Pointer interY = Pointer.to(y);
    Pointer dstX = Pointer.to(xDispOut);
    Pointer dstY = Pointer.to(yDispOut);
    
    int[] err = new int[1];
    cl_mem memObjects[] = new cl_mem[10];
    
    memObjects[0] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * xDispIn.length, srcX, err);
    memObjects[1] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * yDispIn.length, srcY, err);
    memObjects[2] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR , Sizeof.cl_float * x.length, interX, err);
    memObjects[3] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR , Sizeof.cl_float * y.length, interY, err);
    memObjects[4] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR , Sizeof.cl_float * xDispOut.length, dstX, err);
    memObjects[5] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR , Sizeof.cl_float * yDispOut.length, dstY, err);
    memObjects[6] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR , Sizeof.cl_float * 1, Pointer.to(chunks), err);
    memObjects[7] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR , Sizeof.cl_float * 1, Pointer.to(new int[]{edges[0].length}), err);
    
    int status;
    attractAggregate(memObjects[4], memObjects[5], memObjects[2], memObjects[3], memObjects[0], memObjects[1], memObjects[6], memObjects[7], (long)Math.ceil((double)edges[0].length / (double)chunks[0]));
    
    status = clEnqueueReadBuffer(queue, memObjects[4], CL_TRUE, 0, Sizeof.cl_float * xDispOut.length, dstX, 0, null, null);
    status = clEnqueueReadBuffer(queue, memObjects[5], CL_TRUE, 0, Sizeof.cl_float * yDispOut.length, dstY, 0, null, null);
  }
  
  private cl_command_queue queue;
  private cl_program program;
  private cl_context context;
  private cl_kernel attract;
  private cl_kernel attractAggregate1;
  private cl_kernel attractAggregate2;
  private cl_kernel repelAggregate1;
  private cl_kernel adjust;
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
    cl_device_id device = devices[deviceIndex];

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
    repel = clCreateKernel(program, "repel", null);
    attractAggregate1 = clCreateKernel(program, "attractAggregate1", null);
    attractAggregate2 = clCreateKernel(program, "attractAggregate2", null);
    repelAggregate1 = clCreateKernel(program, "repelAggregate1", null);
    adjust = clCreateKernel(program, "adjust", null);
    
    
    clGetDeviceInfo(
        devices[deviceIndex], CL_DEVICE_MAX_WORK_GROUP_SIZE, Sizeof.size_t,
        Pointer.to(maxWorkGroupSize), null);

    clGetDeviceInfo(devices[deviceIndex], CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS, Sizeof.cl_uint,Pointer.to(maxDimensions), null);
    maxWorkItemSizes = new long[(int)maxDimensions[0]];
    //clGetDeviceInfo(devices[deviceIndex], CL_DEVICE_MAX_WORK_GROUP_SIZE, Sizeof.size_t * 1, Pointer.to(maxWorkItemSizes), null);
    clGetDeviceInfo(devices[deviceIndex], CL_DEVICE_MAX_WORK_ITEM_SIZES, Sizeof.size_t * maxDimensions[0], Pointer.to(maxWorkItemSizes), null);
    //maxWorkItemSizes[0] = 2;
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
      float[] xDisp = new float[nNodes];
      float[] yDisp = new float[nNodes];
      int[] nodes = edgeUsageIndexer.keys();

		    // keep passing through the layout loop until the temp is
      // low initialIter + time for cooling schedule
      int[] startIndexes = new int[nNodes];
      for(int i = 0; i < nNodes - 1; i++){
        int prev = 0;
        if(i > 0){
          prev = startIndexes[i];
        }
        startIndexes[i+1] = prev + (nNodes - (i + 1));
      }
      init(nodes, edges, xPos, yPos, xDisp, yDisp, startIndexes);
      long end = System.currentTimeMillis();
      System.out.printf("Setup: %f Seconds\n",(((double)end)-(double)start) / 1000);
      while (true) {
        start = System.currentTimeMillis();
        boolean stop = oneRound2(xPos, yPos, startIndexes, xDisp, yDisp);
        end = System.currentTimeMillis();
        System.out.printf("round time: %f Seconds\n",(((double)end)-(double)start) / 1000);
        progress += 0.001;
        if(stop){
          Pointer dstx = Pointer.to(xPos);
          Pointer dsty = Pointer.to(yPos);
          clEnqueueReadBuffer(queue, cl_xPos, CL_TRUE, 0, Sizeof.cl_float * xPos.length, dstx, 0, null, null);
          clEnqueueReadBuffer(queue, cl_yPos, CL_TRUE, 0, Sizeof.cl_float * yPos.length, dsty, 0, null, null);
          break;
        }
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
