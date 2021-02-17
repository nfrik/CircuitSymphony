package org.circuitsymphony.util;

import com.nicslu.jni.SNicsLU;
import com.nicslu.jni.doubleArray;
import com.nicslu.jni.nicslu;
import com.nicslu.jni.uintArray;
import no.uib.cipr.matrix.sparse.CompColMatrix;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import org.circuitsymphony.nicslu.NICSLUtest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NicsluSolver {

    public static final int N_CORES = 4;
    private int N;

    static {
        try {
//            NativeUtils.loadLibraryFromJar("/nicslu.dll");
            NativeUtils.loadLibraryFromJar("/libnicslu.dylib");
//            NativeUtils.loadLibraryFromJar("/libnicslu.so");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SNicsLU nics;

    uintArray ap=null;
    uintArray ai=null;
    doubleArray ax=null;

    public NicsluSolver(){
//        int n= NICSLUtest.MTXReader.nColumns;
//        int nnz = NICSLUtest.MTXReader.nNonZeros;

//        uintArray ap=new uintArray(n+1);
//        uintArray ai=new uintArray(nnz);
//        doubleArray ax=new doubleArray(nnz);
//        doubleArray b=new doubleArray(n);
//        doubleArray x=new doubleArray(n+n);
//        uintArray N=new uintArray(1);
//        uintArray NNZ=new uintArray(1);
//        doubleArray err=new doubleArray(1);

        nics = new SNicsLU();
        nicslu.NicsLU_Initialize(nics);

    }

    public boolean SetMatrix(CompColMatrix ccm){
        int n = ccm.numRows();
        N = n;
        int [ ] Ap = null;
        int [ ] Ai = null;
        double [ ] Ax = null;
//        SparseMatrix sparseMatrix = CCSMatrix.from2DArray(A.toArray2());
        int nnz=ccm.getRowIndices().length;
//        ccm = new CompColMatrix(sparseMatrix);
        Ap = ccm.getColumnPointers();
        Ai = ccm.getRowIndices();
        Ax = ccm.getData();

        ap=new uintArray(n+1);
        ai=new uintArray(nnz);
        ax=new doubleArray(nnz);

        for(int i=0;i<nnz;i++){
            ai.setitem(i,Ai[i]);
            ax.setitem(i,Ax[i]);
        }

        for(int i=0;i<n+1;i++){
            ap.setitem(i,Ap[i]);
        }

//        nicslu.NicsLU_ResetMatrixValues(nics,ax.cast());
        int ret= nicslu.NicsLU_CreateMatrix(nics, n, nnz, ax.cast(), ai.cast(), ap.cast());
        doubleArray cfgf= doubleArray.frompointer(nics.getCfgf());
        cfgf.setitem(0, 1.0);
        nics.setCfgf(cfgf.cast());

        return ret == 0;
    }

    public boolean SetMatrix(CompRowMatrix ccm){
        int n = ccm.numRows();
        N = n;
        int [ ] Ap = null;
        int [ ] Ai = null;
        double [ ] Ax = null;
//        SparseMatrix sparseMatrix = CCSMatrix.from2DArray(A.toArray2());
        int nnz=ccm.getColumnIndices().length;
//        ccm = new CompColMatrix(sparseMatrix);
        Ap = ccm.getRowPointers();
        Ai = ccm.getColumnIndices();
        Ax = ccm.getData();
//        System.out.println("Number of non-zeros: " + nnz + ", matrix size: "+n);
        ap=new uintArray(n+1);
        ai=new uintArray(nnz);
        ax=new doubleArray(nnz);
        double sum = 0d, scale = 10e6d;

        for(int i=0;i<nnz;i++){
            ai.setitem(i,Ai[i]);
            ax.setitem(i,Ax[i]);
        }

        for(int i=0;i<n+1;i++){
            ap.setitem(i,Ap[i]);
        }

//        nicslu.NicsLU_ResetMatrixValues(nics,ax.cast());
        int ret= nicslu.NicsLU_CreateMatrix(nics, n, nnz, ax.cast(), ai.cast(), ap.cast());
        //doubleArray cfgf= doubleArray.frompointer(nics.getCfgf());
        //cfgf.setitem(0, 1.0);
        //nics.setCfgf(cfgf.cast());

        return ret == 0;
    }

    /**
     * Call it after SetMatrix
     * @return
     */
    public boolean AnalyzeAndFactorize(){
        nicslu.NicsLU_Analyze(nics);
        doubleArray stat= doubleArray.frompointer(nics.getStat());
//        System.out.printf("Analysis time: %.8g\n", stat.getitem(0));

        int ret= nicslu.NicsLU_CreateScheduler(nics);
//        System.out.printf("Time of creating scheduler: %.8g\n", stat.getitem(4));
//        System.out.printf("Suggestion: %s.\n", ret==0?"parallel":"sequential");
//        long startTime=System.currentTimeMillis();
        ret = 1;
        if (ret==0/* && N>=1500*/){
            uintArray cfgi= uintArray.frompointer(nics.getCfgi());

            nicslu.NicsLU_CreateThreads(nics, N_CORES, true);
//            System.out.printf("Total cores: %d, threads created: %d\n", (int)stat.getitem(9), (int)cfgi.getitem(5));

            nicslu.NicsLU_BindThreads(nics, true);

            ret = nicslu.NicsLU_Factorize_MT(nics);
//            System.out.printf("Factorization time: %.8g\n", stat.getitem(1));
        }
        else {
            ret = nicslu.NicsLU_Factorize(nics);
//            System.err.println("Single thread NICSLU");
        }

//        System.out.println("Factorization time: " + (double)(System.currentTimeMillis()-startTime)/1000d+" s");

        nicslu.NicsLU_ConditionNumber(nics, null);
        //System.out.printf("Condition number: %.8g\n", stat.getitem(6));

        return ret == 0;

    }

    /**
     * Call it after Analyze
     * @param B
     * @return
     */
    public boolean nicslu_solve(double[] B){
//        int nnz=ccm.numRows();
        doubleArray rhs=new doubleArray(B.length);

        for(int i=0;i<B.length;i++){
            rhs.setitem(i,B[i]);
        }

        int ret = nicslu.NicsLU_Solve(nics,rhs.cast());

        if (ret==0){
            for(int i=0;i<B.length;i++){
                B[i]=rhs.getitem(i);
            }
            return true;
        }else{
            System.out.println("NICSLU failed");
            return false;
        }

    }

    public String getStatistics(){
        doubleArray stat= doubleArray.frompointer(nics.getStat());
        nicslu.NicsLU_Flops(nics, null);
        nicslu.NicsLU_Throughput(nics, null);
        nicslu.NicsLU_ConditionNumber(nics, null);
        nicslu.NicsLU_MemoryUsage(nics, null);
        String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(new Date());
        String data = String.format(timeStamp+" Flops: %.8g, Throughput (bytes): %.8g, Condition number: %.8g, Memory (Mbytes): %.8g",stat.getitem(5),stat.getitem(12),stat.getitem(6),stat.getitem(21)/1024./1024.);

        return data;
    }

    public void nicslu_destroy(){
        nicslu.NicsLU_Destroy(nics);
    }

}
