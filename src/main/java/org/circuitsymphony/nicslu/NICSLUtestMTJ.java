package org.circuitsymphony.nicslu;

import com.nicslu.jni.doubleArray;
import com.nicslu.jni.nicslu;
import com.nicslu.jni.uintArray;
import no.uib.cipr.matrix.sparse.CompColMatrix;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
import org.circuitsymphony.util.NicsluSolver;

import java.io.*;

public class NICSLUtestMTJ {

    public static class MTXReader2 {

        public static int nRows =0;
        public static int nColumns = 0;
        public static int nNonZeros = 0;

        public static CompColMatrix matrix;

        public static uintArray ap=null;//new uintArray(n+1);
        public static uintArray ai=null;//new uintArray(nnz);
        public static doubleArray ax=null;//new doubleArray(nnz);
        public static doubleArray b=null;//new doubleArray(n);
        public static doubleArray x=null;//new doubleArray(n);

        public static void read(String filename) throws java.io.IOException {
            InputStream s = new FileInputStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(s));

            // read type code initial line
            String line = br.readLine();

            // read comment lines if any
            boolean comment = true;
            while (comment) {
                line = br.readLine();
                comment = line.startsWith("%");
            }

            // line now contains the size information which needs to be parsed
            String[] str = line.split("( )+");
            nRows = (Integer.valueOf(str[0].trim())).intValue();
            nColumns = (Integer.valueOf(str[1].trim())).intValue();
            nNonZeros = (Integer.valueOf(str[2].trim())).intValue();

            // now we're into the data section
            FlexCompColMatrix fccm = new FlexCompColMatrix(nRows,nColumns);
//            matrix = new SparseMatrix(nRows, nColumns);
            while (true) {
                line = br.readLine();
                if (line == null)  break;
                str = line.split("( )+");
                int i = (Integer.valueOf(str[0].trim())).intValue();
                int j = (Integer.valueOf(str[1].trim())).intValue();
                double x = (Double.valueOf(str[2].trim())).doubleValue();
                fccm.set(i-1, j-1, x);
            }

            ap=new uintArray(nRows+1);
            ai=new uintArray(nNonZeros);
            ax=new doubleArray(nNonZeros);
            b=new doubleArray(nRows);
            x=new doubleArray(nRows);

            matrix = new CompColMatrix(fccm);

            int [] Ai = matrix.getRowIndices();
            int [] Ap = matrix.getColumnPointers();
            double [] Ax = matrix.getData();

            for(int i=0;i<Ai.length;i++){
                ai.setitem(i,Ai[i]);
            }

            for(int i=0;i<Ap.length;i++){
                ap.setitem(i,Ap[i]);
            }

            for(int i=0;i<Ax.length;i++){
                ax.setitem(i,Ax[i]);
            }

            for(int i=0;i<nRows;i++){
                b.setitem(i,1.);
                x.setitem(i,1.);
            }

            br.close();
        }
    }

    public static void main(String [] args){

        if(args.length==0) {
            String[] newargs = {"src/main/resources/ASIC_100k.mtx", "12"};
            args = newargs;
        }
        String f = args[0];

        try{
//            MTXReader.read(f);
            MTXReader2.read(f);
        }catch(IOException e){
            e.printStackTrace();
        }

        int n= MTXReader2.nColumns;
        int nnz = MTXReader2.nNonZeros;

        uintArray ap=new uintArray(n+1);
        uintArray ai=new uintArray(nnz);
        doubleArray ax=new doubleArray(nnz);
        doubleArray b=new doubleArray(n);
        doubleArray x=new doubleArray(n);
        uintArray N=new uintArray(1);
        uintArray NNZ=new uintArray(1);
        doubleArray err=new doubleArray(1);

        n= MTXReader2.nColumns;
        nnz= MTXReader2.nNonZeros;
        ai= MTXReader2.ai;
        ap= MTXReader2.ap;
        ax= MTXReader2.ax;
        b = MTXReader2.b;
        x = MTXReader2.x;

        double []rhs=new double[n];

        for(int i=0;i<n;i++){
            rhs[i]=b.getitem(i);
        }

        CompColMatrix ccm = MTXReader2.matrix;

        NicsluSolver nicsluSolver = new NicsluSolver();

        nicsluSolver.SetMatrix(ccm);

        nicsluSolver.AnalyzeAndFactorize();

        nicsluSolver.nicslu_solve(rhs);

        int ret= nicslu.NicsLU_Residual(n, ax.cast(), ai.cast(), ap.cast(), x.cast(), b.cast(), err.cast(), 1, 0);
        System.out.printf("Ax-b (1-norm): %.8g\n", err.getitem(0));

    }
}
