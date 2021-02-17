package org.circuitsymphony.util;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

//package com.mxgraph.layout;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

public class mxFastOrganicLayoutWrap extends mxFastOrganicLayout {
//    protected boolean useInputOrigin = true;
//    protected boolean resetEdges = true;
//    protected boolean disableEdgeStyle = true;
//    protected double forceConstant = 50.0D;
//    protected double forceConstantSquared = 0.0D;
//    protected double minDistanceLimit = 2.0D;
//    protected double minDistanceLimitSquared = 0.0D;
//    protected double maxDistanceLimit = 500.0D;
//    protected double initialTemp = 200.0D;
//    protected double temperature = 0.0D;
//    protected double maxIterations = 0.0D;
//    protected double iteration = 0.0D;
//    protected Object[] vertexArray;
//    protected double[] dispX;
//    protected double[] dispY;
//    protected double[][] cellLocation;
//    protected double[] radius;
//    protected double[] radiusSquared;
//    protected boolean[] isMoveable;
//    protected int[][] neighbours;
//    protected boolean allowedToRun = true;
    Random generator = null;
//    protected Hashtable<Object, Integer> indices = new Hashtable();

    public mxFastOrganicLayoutWrap(mxGraph graph) {
        super(graph);
        this.generator = new Random();
    }

    public mxFastOrganicLayoutWrap(mxGraph graph,int seed) {
        super(graph);
        this.generator = new Random(seed);
    }

    protected void calcRepulsion() {
        int vertexCount = this.vertexArray.length;

        for(int i = 0; i < vertexCount; ++i) {
            for(int j = i; j < vertexCount; ++j) {
                if (!this.allowedToRun) {
                    return;
                }

                if (j != i) {
                    double xDelta = this.cellLocation[i][0] - this.cellLocation[j][0];
                    double yDelta = this.cellLocation[i][1] - this.cellLocation[j][1];
                    if (xDelta == 0.0D) {
                        xDelta = 0.01D + generator.nextDouble();//+ Math.random();
                    }

                    if (yDelta == 0.0D) {
                        yDelta = 0.01D + generator.nextDouble();//+ Math.random();
                    }

                    double deltaLength = Math.sqrt(xDelta * xDelta + yDelta * yDelta);
                    double deltaLengthWithRadius = deltaLength - this.radius[i] - this.radius[j];
                    if (deltaLengthWithRadius <= this.maxDistanceLimit) {
                        if (deltaLengthWithRadius < this.minDistanceLimit) {
                            deltaLengthWithRadius = this.minDistanceLimit;
                        }

                        double force = this.forceConstantSquared / deltaLengthWithRadius;
                        double displacementX = xDelta / deltaLength * force;
                        double displacementY = yDelta / deltaLength * force;
                        double[] var10000;
                        if (this.isMoveable[i]) {
                            var10000 = this.dispX;
                            var10000[i] += displacementX;
                            var10000 = this.dispY;
                            var10000[i] += displacementY;
                        }

                        if (this.isMoveable[j]) {
                            var10000 = this.dispX;
                            var10000[j] -= displacementX;
                            var10000 = this.dispY;
                            var10000[j] -= displacementY;
                        }
                    }
                }
            }
        }

    }
}
