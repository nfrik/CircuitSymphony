package org.circuitsymphony.ui;

/******************************************************************************
 *  Compilation:  javac DLA.java
 *  Execution:    java DLA N
 *  Dependencies: StdDraw.java
 *
 *  Diffusion limited aggregation.
 *
 *  % java DLA 500
 *
 ******************************************************************************/

import java.awt.Color;

public class DLA {

    // test client
    public static void main(String[] args) {
//        int N = Integer.parseInt(args[0]);     // N-by-N grid
        int N = 500;
        int launch = N - 10;                   // row to launch particles from
        boolean[][] dla = new boolean[N][N];   // is cell (x, y) occupied

        Picture pic = new Picture(N, N);

        int particles = 0;                     // only used to pick colors

        // create rainbow of colors
        Color[] colors = new Color[256];
        for (int i = 0; i < 256; i++)
            colors[i] = Color.getHSBColor(1.0f * i / 255, .8f, .8f);


        // set seed to be bottom row
        for (int x = 0; x < N; x++) dla[x][0] = true;


        // repeat until aggregate hits top
        boolean done = false;
        while (!done) {

            // random launching point
            int x = (int) (N * Math.random());
            int y = launch;

            // particle takes a 2d random walk
            while (x < N - 2 && x > 1 && y < N - 2 && y > 1) {
                double r = Math.random();
                if      (r < 0.25) x--;
                else if (r < 0.50) x++;
                else if (r < 0.65) y++;
                else               y--;

                // check if neighboring site is occupied
                if (dla[x-1][y]   || dla[x+1][y]   || dla[x][y-1]   || dla[x][y+1]   ||
                        dla[x-1][y-1] || dla[x+1][y+1] || dla[x-1][y+1] || dla[x+1][y-1] ) {
                    dla[x][y] = true;
                    particles++;
                    pic.set(x, N-y-1, colors[(particles / 256) % 256]);
                    pic.show();

                    // aggregate hits top, so set flag to stop outer while loop
                    if (y > launch) done = true;

                    // particle stuck, so break out of inner while loop
                    break;
                }
            }
        }
    }
}
