package org.circuitsymphony.nicslu;

//import wrapper.doubleArray;
//import wrapper.nicslu;
//import wrapper.uintArray;
import com.nicslu.jni.*;

import no.uib.cipr.matrix.sparse.CompColMatrix;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
import org.circuitsymphony.util.NativeUtils;
import org.circuitsymphony.util.NicsluSolver;

import java.nio.file.*;
import java.lang.Integer;
import java.io.*;

public class NICSLUtest {
    static {
//        System.out.println(System.getProperty("java.class.path"));
//        System.loadLibrary("nicslu");

//        System.load(ad);
//        File resourcesDirectory = new File("src/main/resources");
        try {
//            NativeUtils.loadLibraryFromJar("/libnicslu.so");
            NativeUtils.loadLibraryFromJar("/libnicslu.dylib");
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.load(resourcesDirectory.getAbsolutePath()+"/libdemop.so");
        System.out.println(System.getProperty("java.library.path"));
        System.out.println("LD_LIBRARY_PATH = " + System.getenv("LD_LIBRARY_PATH"));
//        System.loadLibrary("nicslu");
    }

    //private static double [ ] Ax = {2., 3., 3., -1., 4., 4., -3., 1., 2., 2., 6., 1.} ;

    public static class MTXReader {

        public static int nRows =0;
        public static int nColumns = 0;
        public static int nNonZeros = 0;



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

            br.close();
        }
    }

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

    /*public static void main(String[] args) {
        //For ncsu.test file inputs change n, nnz (look in file) and file name
        //int n=99340, nnz=954163;
        //String f="ASIC_100k.mtx";
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

        int n=MTXReader.nColumns;
        int nnz = MTXReader.nNonZeros;

        uintArray ap=new uintArray(n+1);
        uintArray ai=new uintArray(nnz);
        doubleArray ax=new doubleArray(nnz);
        doubleArray b=new doubleArray(n);
        doubleArray x=new doubleArray(n);
        uintArray N=new uintArray(1);
        uintArray NNZ=new uintArray(1);
        doubleArray err=new doubleArray(1);

        //For checking from java, arrays are set like this
        //for (int i=0;i<12;i++) ax.setitem(i, Ax[i]);

        N.setitem(0, n);
        NNZ.setitem(0, nnz);

//        int ret= nicslu.readFromFile(f, N.cast(), NNZ.cast(), ai.cast(), ap.cast(), ax.cast(), x.cast(), b.cast());
//        n=N.getitem(0);
//        nnz=NNZ.getitem(0);
        n=MTXReader2.nColumns;
        nnz=MTXReader2.nNonZeros;
        ai=MTXReader2.ai;
        ap=MTXReader2.ap;
        ax=MTXReader2.ax;
        b = MTXReader2.b;
        x = MTXReader2.x;


        SNicsLU nics =  new SNicsLU();
        int ret= nicslu.NicsLU_Initialize(nics);

        ret= nicslu.NicsLU_CreateMatrix(nics, n, nnz, ax.cast(), ai.cast(), ap.cast());
        doubleArray cfgf= doubleArray.frompointer(nics.getCfgf());
        cfgf.setitem(0, 1.0);
        nics.setCfgf(cfgf.cast());

        ret = nicslu.NicsLU_Analyze(nics);
        doubleArray stat= doubleArray.frompointer(nics.getStat());
        System.out.printf("Analysis time: %.8g\n", stat.getitem(0));

        ret= nicslu.NicsLU_CreateScheduler(nics);
        System.out.printf("Time of creating scheduler: %.8g\n", stat.getitem(4));
        System.out.printf("Suggestion: %s.\n", ret==0?"parallel":"sequential");

        uintArray cfgi= uintArray.frompointer(nics.getCfgi());

        nicslu.NicsLU_CreateThreads(nics, Integer.parseInt(args[1]), true);
        System.out.printf("Total cores: %d, threads created: %d\n", (int)stat.getitem(9), (int)cfgi.getitem(5));

        nicslu.NicsLU_BindThreads(nics, false);

        nicslu.NicsLU_Factorize_MT(nics);
        System.out.printf("Factorization time: %.8g\n", stat.getitem(1));

        nicslu.NicsLU_ReFactorize_MT(nics, ax.cast());
        System.out.printf("Re-factorization time: %.8g\n", stat.getitem(2));

        nicslu.NicsLU_Solve(nics, x.cast());
        System.out.printf("Substitution time: %.8g\n", stat.getitem(3));

        ret= nicslu.NicsLU_Residual(n, ax.cast(), ai.cast(), ap.cast(), x.cast(), b.cast(), err.cast(), 1, 0);
        System.out.printf("Ax-b (1-norm): %.8g\n", err.getitem(0));

        nicslu.NicsLU_Residual(n, ax.cast(), ai.cast(), ap.cast(), x.cast(), b.cast(), err.cast(), 2, 0);
        System.out.printf("Ax-b (2-norm): %.8g\n", err.getitem(0));

        nicslu.NicsLU_Residual(n, ax.cast(), ai.cast(), ap.cast(), x.cast(), b.cast(), err.cast(), 0, 0);
        System.out.printf("Ax-b (infinite-norm): %.8g\n", err.getitem(0));

        System.out.printf("NNZ(L+U-I): %d\n", (long)nics.getLu_nnz());
        nicslu.NicsLU_Flops(nics, null);
        nicslu.NicsLU_Throughput(nics, null);
        nicslu.NicsLU_ConditionNumber(nics, null);
        System.out.printf("Flops: %.8g\n", stat.getitem(5));
        System.out.printf("Throughput (bytes): %.8g\n", stat.getitem(12));
        System.out.printf("Condition number: %.8g\n", stat.getitem(6));
        nicslu.NicsLU_MemoryUsage(nics, null);
        System.out.printf("memory (Mbytes): %.8g\n", stat.getitem(21)/1024./1024.);
        nicslu.NicsLU_DestroyThreads(nics);

        System.out.println(ret);
    }*/


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


    NicsluSolver nicsluSolver = new NicsluSolver();

    for(int j=0;j<6;j++) {

        CompColMatrix ccm = MTXReader2.matrix.copy();

        nicsluSolver.SetMatrix(ccm);

        nicsluSolver.AnalyzeAndFactorize();

        for(int i=0;i<n;i++){
            rhs[i]=b.getitem(i);
        }
        nicsluSolver.nicslu_solve(rhs);

        for (int i = 0; i < n; i++) {
            x.setitem(i, rhs[i]);
        }

        int ret = nicslu.NicsLU_Residual(n, ax.cast(), ai.cast(), ap.cast(), x.cast(), b.cast(), err.cast(), 1, 0);
        System.out.printf("Ax-b (1-norm): %.8g\n", err.getitem(0));
    }

}

}
