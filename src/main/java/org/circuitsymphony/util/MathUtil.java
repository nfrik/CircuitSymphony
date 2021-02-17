package org.circuitsymphony.util;

//import jcuda.jcusolver.JCusolverRf;

import edu.ufl.cise.klu.common.KLU_common;
import edu.ufl.cise.klu.common.KLU_numeric;
import edu.ufl.cise.klu.common.KLU_symbolic;
import no.uib.cipr.matrix.sparse.CompColMatrix;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
import no.uib.cipr.matrix.sparse.LinkedSparseMatrix;
import org.jblas.DoubleMatrix;
import org.jblas.FloatMatrix;
import org.jblas.NativeBlas;

import java.util.Random;

import static edu.ufl.cise.klu.tdouble.Dklu_analyze.klu_analyze;
import static edu.ufl.cise.klu.tdouble.Dklu_defaults.klu_defaults;
import static edu.ufl.cise.klu.tdouble.Dklu_diagnostics.*;
import static edu.ufl.cise.klu.tdouble.Dklu_factor.klu_factor;
import static edu.ufl.cise.klu.tdouble.Dklu_solve.klu_solve;

//import org.la4j.matrix.DenseMatrix;
//import org.la4j.matrix.SparseMatrix;
//import org.la4j.matrix.sparse.CCSMatrix;
//import jcuda.*;
////import jcuda.jcublas.*;
///*
//import org.nd4j.linalg.api.ndarray.INDArray;
//import org.nd4j.linalg.factory.Nd4j;
//import org.nd4j.linalg.jcublas.blas.JcublasLapack;
//*/
//
//import jcuda.jcublas.JCublas2;
//import jcuda.jcublas.*;
//import static jcuda.runtime.JCuda.*;
//import static jcuda.jcublas.JCublas2.*;
//import static jcuda.runtime.JCuda.cudaMemcpy;
//import static jcuda.runtime.cudaMemcpyKind.cudaMemcpyDeviceToDevice;


import java.util.Random;

import static edu.ufl.cise.klu.tdouble.Dklu_analyze.klu_analyze;
import static edu.ufl.cise.klu.tdouble.Dklu_defaults.klu_defaults;
import static edu.ufl.cise.klu.tdouble.Dklu_diagnostics.*;
import static edu.ufl.cise.klu.tdouble.Dklu_diagnostics.klu_flops;
import static edu.ufl.cise.klu.tdouble.Dklu_factor.klu_factor;
import static edu.ufl.cise.klu.tdouble.Dklu_solve.klu_solve;


public class MathUtil {

    private static Random random = new Random();

//    public static KLU_common Common = new KLU_common();
//    public static KLU_symbolic Symbolic = null;

    //private static INDArray INFO = null;
    private static double MAX (double a, double b)
    {
        return (a) > (b) ? (a) : (b) ;
    }

    public static int getrand(int x) {
        int q = random.nextInt();
        if (q < 0)
            q = -q;
        return q % x;
    }
/*
    private static JcublasLapack lapack;



    //ND4J CPU lapack
    public static JcublasLapack getLapack() {
        if(lapack==null){
            lapack = new JcublasLapack();
            INFO = Nd4j.createArrayFromShapeBuffer(Nd4j.getDataBufferFactory().createInt(1),
                    Nd4j.getShapeInfoProvider().createShapeInformation(new int[] {1, 1}));
        }
        return lapack;
    }

    public static boolean getrf(INDArray A, int n, INDArray IPIV){
        //// TODO: 3/5/2017  create info once, big overhead

//        INDArray IPIV = Nd4j.createArrayFromShapeBuffer(Nd4j.getDataBufferFactory().createInt(n),
//                Nd4j.getShapeInfoProvider().createShapeInformation(new int[] {1, n}));

        getLapack().sgetrf(n, n, A, IPIV, INFO);

        if(INFO.getInt(new int[]{0}) < 0) {
            throw new Error("Parameter #" + INFO.getInt(new int[]{0}) + " to getrf() was not valid");
        } else {
            if(INFO.getInt(new int[]{0}) > 0) {
//                logger.warn("The matrix is singular - cannot be used for inverse op. Check L matrix at row " + INFO.getInt(new int[]{0}));
                return false;
            }

            return true;
        }
    }
*/

/*
    // Solves the set of n linear equations using a LU factorization
    // previously performed by lu_factor. On input, b[0..n-1] is the right
    // hand side of the equations, and on output, contains the solution.
    public static void lu_solve(INDArray a, int n, INDArray ipvt, double b[]) {
        int i;

        // find first nonzero b element
        for (i = 0; i != n; i++) {
            int row = ipvt.getInt(i)-1;

            double swap = b[row];
            b[row] = b[i];
            b[i] = swap;
            if (swap != 0)
                break;
        }

        int bi = i++;
        for (; i < n; i++) {
            int row = ipvt.getInt(i)-1;
            int j;
            double tot = b[row];

            b[row] = b[i];
            // forward substitution using the lower triangular matrix
            for (j = bi; j < i; j++)
                tot -= a.getDouble(i,j) * b[j];
            b[i] = tot;
        }
        for (i = n - 1; i >= 0; i--) {
            double tot = b[i];

            // back-substitution using the upper triangular matrix
            int j;
            for (j = i + 1; j != n; j++)
                tot -= a.getDouble(i,j) * b[j];
            b[i] = tot / a.getDouble(i,i);
        }
    }
*/

    public static class SparseUtils {

        public double[] values=null;
        public int[] rows=null;
        public int[] cols=null;
        public CompColMatrix ccm = null;
        public FlexCompColMatrix fccm = null;

        public void get_triplet(CompColMatrix ccm){
            this.ccm=ccm;
            int n = ccm.getRowIndices().length;

            int numRows = ccm.numRows();
            int numCols = ccm.numColumns();
            int nz_length = ccm.getData().length;

            rows = new int[n];
            cols = new int[n];
            values = new double[n];

            int[] col_idx=ccm.getColumnPointers();
            int[] nz_rows=ccm.getRowIndices();
            double[] nz_values=ccm.getData();

//            System.out.println(" , rows = " + numRows + " , cols = " + numCols + " , nz_length = " + nz_length);

            int globidx=0;
            for(int col = 0; col < numCols; ++col) {
                int idx0 = col_idx[col];
                int idx1 = col_idx[col + 1];

                for(int i = idx0; i < idx1; ++i) {
                    int row = nz_rows[i];
                    double value = nz_values[i];
                    values[globidx]=value;
                    rows[globidx]=row;
                    cols[globidx]=col;
                    globidx++;
//                    System.out.printf(format, row, col, value);
                }
            }
        }
        public void get_triplet(FlexCompColMatrix fccm){
            this.fccm=fccm;
            this.ccm=new CompColMatrix(this.fccm);
            get_triplet(this.ccm);
        }
    }



    // Solves the set of n linear equations using a LU factorization
    // previously performed by lu_factor. On input, b[0..n-1] is the right
    // hand side of the equations, and on output, contains the solution.
    public static void lu_solve(double a[][], int n, int ipvt[], double b[]) {
        int i;

        // find first nonzero b element
        for (i = 0; i != n; i++) {
            int row = ipvt[i];

            double swap = b[row];
            b[row] = b[i];
            b[i] = swap;
            if (swap != 0)
                break;
        }

        int bi = i++;
        for (; i < n; i++) {
            int row = ipvt[i];
            int j;
            double tot = b[row];

            b[row] = b[i];
            // forward substitution using the lower triangular matrix
            for (j = bi; j < i; j++)
                tot -= a[i][j] * b[j];
            b[i] = tot;
        }
        for (i = n - 1; i >= 0; i--) {
            double tot = b[i];

            // back-substitution using the upper triangular matrix
            int j;
            for (j = i + 1; j != n; j++)
                tot -= a[i][j] * b[j];
            b[i] = tot / a[i][i];
        }
    }

    // Solves the set of n linear equations using a LU factorization
    // previously performed by lu_factor. On input, b[0..n-1] is the right
    // hand side of the equations, and on output, contains the solution.
    public static <T extends DoubleMatrix> void lu_solve(T a, int n, int ipvt[], double b[]) {
        int i;

        // find first nonzero b element
        for (i = 0; i != n; i++) {
            int row = ipvt[i];

            double swap = b[row];
            b[row] = b[i];
            b[i] = swap;
            if (swap != 0)
                break;
        }

        int bi = i++;
        for (; i < n; i++) {
            int row = ipvt[i];
            int j;
            double tot = b[row];

            b[row] = b[i];
            // forward substitution using the lower triangular matrix
            for (j = bi; j < i; j++)
                tot -= a.get(i,j) * b[j];
            b[i] = tot;
        }
        for (i = n - 1; i >= 0; i--) {
            double tot = b[i];

            // back-substitution using the upper triangular matrix
            int j;
            for (j = i + 1; j != n; j++)
                tot -= a.get(i,j) * b[j];
            b[i] = tot / a.get(i,i);
        }
    }

    // factors a matrix into upper and lower triangular matrices by
    // gaussian elimination. On entry, a[0..n-1][0..n-1] is the
    // matrix to be factored. ipvt[] returns an integer vector of pivot
    // indices, used in the lu_solve() routine.
    public static boolean lu_factor(double a[][], int n, int ipvt[]) {
        double scaleFactors[];
        int i, j, k;

        scaleFactors = new double[n];

        // divide each row by its largest element, keeping track of the
        // scaling factors
        for (i = 0; i != n; i++) {
            double largest = 0;
            for (j = 0; j != n; j++) {
                double x = Math.abs(a[i][j]);
                if (x > largest)
                    largest = x;
            }
            // if all zeros, it's a singular matrix
            if (largest == 0)
                return false;
            scaleFactors[i] = 1.0 / largest;
        }

        // use Crout's method; loop through the columns
        for (j = 0; j != n; j++) {

            // calculate upper triangular elements for this column
            for (i = 0; i != j; i++) {
                double q = a[i][j];
                for (k = 0; k != i; k++)
                    q -= a[i][k] * a[k][j];
                a[i][j] = q;
            }

            // calculate lower triangular elements for this column
            double largest = 0;
            int largestRow = -1;
            for (i = j; i != n; i++) {
                double q = a[i][j];
                for (k = 0; k != j; k++)
                    q -= a[i][k] * a[k][j];
                a[i][j] = q;
                double x = Math.abs(q);
                if (x >= largest) {
                    largest = x;
                    largestRow = i;
                }
            }

            // pivoting
            if (j != largestRow) {
                double x;
                for (k = 0; k != n; k++) {
                    x = a[largestRow][k];
                    a[largestRow][k] = a[j][k];
                    a[j][k] = x;
                }
                scaleFactors[largestRow] = scaleFactors[j];
            }

            // keep track of row interchanges
            ipvt[j] = largestRow;

            // avoid zeros
            if (a[j][j] == 0.0) {
                // System.out.println("avoided zero");
                a[j][j] = 1e-18;
            }

            if (j != n - 1) {
                double mult = 1.0 / a[j][j];
                for (i = j + 1; i != n; i++)
                    a[i][j] *= mult;
            }
        }
        return true;
    }

    public static boolean lu(DoubleMatrix A, int[] piv) {
        int info = NativeBlas.dgetrf(A.rows, A.columns, A.data, 0, A.rows, piv, 0);
        if(info!=0){
            return false;
        }
        for(int i=0;i<piv.length;i++){
            piv[i]=piv[i]-1;
        }
        return true;
    }

    //Added from here
    public static boolean luJBLAS(DoubleMatrix A, int[] piv) {
        if(A.columns*A.rows < 1)
            return false;
        int info = NativeBlas.dgetrf(A.rows, A.columns, A.data, 0, A.rows, piv, 0);
        if(info!=0){
            return false;
        }
        lowerPIV(piv);
        return true;
    }

    private static void incPivots( int[] p) {
        for( int i = 0; i < p.length; ++i) {
            p[i] += 1;
        }
    }

    private static void lowerPIV( int[] piv){
        for(int i=0;i<piv.length;i++){
            piv[i]=piv[i]-1;
        }
    }

    //up to here


    /**
     * Performs analysis of the matrix
     * @param ccm
     * @param Common
     * @return
     */
    public static KLU_symbolic klu_decomp(CompColMatrix ccm,KLU_common Common) {
        int n = 0;
        int [ ] Ap = null;
        int [ ] Ai = null;
        double [ ] Ax = null;
//        SparseMatrix sparseMatrix = CCSMatrix.from2DArray(A.toArray2());
        n=ccm.numRows();
//        ccm = new CompColMatrix(sparseMatrix);
        Ap= ccm.getColumnPointers();
        Ai= ccm.getRowIndices();
//        Ax= ccm.getData();

//        KLU_symbolic Symbolic;
//        KLU_numeric Numeric;
        klu_defaults (Common);
        KLU_symbolic Symbolic = klu_analyze (n, Ap, Ai, Common);


        if(Common.status != KLU_OK){
            return null;
        }else{
            return Symbolic;
        }
//        int info = NativeBlas.dgetrf(A.rows, A.columns, A.data, 0, A.rows, piv, 0);
//        if(info!=0){
//            return false;
//        }
//        for(int i=0;i<piv.length;i++){
//            piv[i]=piv[i]-1;
//        }
//        return true;
    }

//    public static boolean lu(FloatMatrix A, int[] piv, cublasHandle handle) {
//        if(null == handle)
//            return lu(A, piv);
//        // here comes cuBLAS assisted working
//        //long starttime = System.nanoTime();
//
//        boolean rv= false;
//
//
//            Pointer dA = new Pointer();
//            Pointer dP = new Pointer();
//            Pointer INFO = new Pointer();
//            int[] info = new int[1];
//        //long stopAlloctime = System.nanoTime();
//
//
//        cudaMalloc(dA, Sizeof.FLOAT* A.rows* A.columns);
//            cudaMalloc(dP, Sizeof.INT* A.rows);
//            cudaMalloc(INFO, Sizeof.INT);
//            cublasSetMatrix(A.rows, A.columns, Sizeof.FLOAT, Pointer.to(A.data), A.rows, dA, A.rows);
//        //long starttime2 = System.nanoTime();
//                cublasSgetrfBatched(handle, A.rows, dA, A.rows, dP, INFO,1  );
//        //long starttime3 = System.nanoTime();
//        //System.out.println("allocatioin took (ns): "+(stopAlloctime-starttime));
//        //System.out.println("batched took (ns)    : "+(starttime3-starttime2));
//        //System.out.println("set matrix took (ns) : "+(starttime2-stopAlloctime));
//
//            cublasGetMatrix(A.rows, A.columns, Sizeof.FLOAT, dA, A.rows, Pointer.to(A.data), A.rows);
//            cublasGetVector(A.rows, Sizeof.INT, dP, 1, Pointer.to(piv), 1);
//            cublasGetVector(1, Sizeof.INT, INFO, 1, Pointer.to(info), 1);
//
//            cudaFree(dA);
//            cudaFree(dP);
//            cudaFree(INFO);
//            rv = (info[0] == 0);
//            info = null;
//
//
//        return rv;
//    }

    public static boolean klu_backslash(CompColMatrix ccm,double[] B,KLU_common Common,KLU_symbolic Symbolic){
        int n = ccm.numRows();
//        CompColMatrix ccm = new CompColMatrix(sparseMatrix);
        int[] Ap = ccm.getColumnPointers();
        int[] Ai = ccm.getRowIndices();
        double[] Ax = ccm.getData();
        boolean isreal = true;
        double [] X = new double[n];
        double [] R = new double[n];
        int[] lunz = new int[1];
        double[] rnorm = new double[1];

//        KLU_common Common = new KLU_common();
        klu_defaults (Common);

        if(klu_backslash(n,Ap,Ai,Ax,isreal,B,X,R,lunz,rnorm,Common,Symbolic)==0){
            System.out.println("KLU failed");
            return false;
        }else{
//            B=X;
            for(int i=0;i<n;i++)
                B[i]=X[i];
            return true;
        }

    }

    /**
     *
     * @param n A is n-by-n
     * @param Ap size n+1, column pointers
     * @param Ai size nz = Ap [n], row indices
     * @param Ax size nz, numerical values
     * @param isreal nonzero if A is real, 0 otherwise
     * @param B size n, right-hand-side
     * @param X size n, solution to Ax=b
     * @param R size n, residual r = b-A*x
     * @param lunz size 1, nnz(L+U+F)
     * @param rnorm size 1, norm(b-A*x,1) / norm(A,1)
     * @param Common default parameters and statistics
     * @return 1 if successful, 0 otherwise
     */
    public static int klu_backslash(int n, int[] Ap, int[] Ai, double[] Ax,
                                    boolean isreal, double[] B, double[] X, double[] R, int[] lunz,
                                    double[] rnorm, KLU_common Common, KLU_symbolic Symbolic)
    {
        double anorm = 0, asum;
//        KLU_symbolic Symbolic;
        KLU_numeric Numeric;
        int i, j, p;

        if (Ap == null || Ai == null || Ax == null || B == null || X == null || B == null)
            return(0);

        /* ---------------------------------------------------------------------- */
        /* symbolic ordering and analysis */
        /* ---------------------------------------------------------------------- */

//        Symbolic = klu_analyze (n, Ap, Ai, Common);
        if (Symbolic == null) return(0);

        if (isreal)
        {

            /* ------------------------------------------------------------------ */
            /* factorization */
            /* ------------------------------------------------------------------ */

            Numeric = klu_factor (Ap, Ai, Ax, Symbolic, Common);
            if (Numeric == null)
            {
                //klu_free_symbolic(Symbolic, Common);
                return(0);
            }

            /* ------------------------------------------------------------------ */
            /* statistics(not required to solve Ax=b) */
            /* ------------------------------------------------------------------ */

            klu_rgrowth (Ap, Ai, Ax, Symbolic, Numeric, Common);
            klu_condest (Ap, Ax, Symbolic, Numeric, Common);
            klu_rcond (Symbolic, Numeric, Common);
            klu_flops (Symbolic, Numeric, Common);
            lunz[0] = Numeric.lnz + Numeric.unz - n +
                    (Numeric.Offp != null ? Numeric.Offp [n] : 0);

            /* ------------------------------------------------------------------ */
            /* solve Ax=b */
            /* ------------------------------------------------------------------ */

            for(i = 0; i < n; i++)
            {
                X [i] = B [i];
            }
            klu_solve (Symbolic, Numeric, n, 1, X, 0, Common);

            /* ------------------------------------------------------------------ */
            /* compute residual, rnorm = norm(b-Ax,1) / norm(A,1) */
            /* ------------------------------------------------------------------ */

            for(i = 0; i < n; i++)
            {
                R [i] = B [i];
            }
            for(j = 0; j < n; j++)
            {
                asum = 0;
                for(p = Ap [j]; p < Ap [j+1]; p++)
                {
                    /* R(i) -= A(i,j) * X(j) */
                    R [Ai [p]] -= Ax [p] * X [j];
                    asum += Math.abs(Ax [p]);
                }
                anorm = MAX (anorm, asum);
            }
            rnorm[0] = 0;
            for(i = 0; i < n; i++)
            {
                rnorm[0] = MAX (rnorm[0], Math.abs(R [i]));
            }

            /* ------------------------------------------------------------------ */
            /* free numeric factorization */
            /* ------------------------------------------------------------------ */

            //klu_free_numeric(Numeric, Common);
            Numeric = null;

        }
        else
        {
            throw new UnsupportedOperationException();

            /* ------------------------------------------------------------------ */
            /* statistics(not required to solve Ax=b) */
            /* ------------------------------------------------------------------ */

//			Numeric = klu_z_factor (Ap, Ai, Ax, Symbolic, Common);
//			if (Numeric == null)
//			{
//				klu_free_symbolic (Symbolic, Common);
//				return(0);
//			}
//
//			/* ------------------------------------------------------------------ */
//			/* statistics */
//			/* ------------------------------------------------------------------ */
//
//			klu_z_rgrowth (Ap, Ai, Ax, Symbolic, Numeric, Common);
//			klu_z_condest (Ap, Ax, Symbolic, Numeric, Common);
//			klu_z_rcond (Symbolic, Numeric, Common);
//			klu_z_flops (Symbolic, Numeric, Common);
//			lunz = Numeric.lnz + Numeric.unz - n +
//				(Numeric.Offp != null ? Numeric.Offp [n] : 0);
//
//			/* ------------------------------------------------------------------ */
//			/* solve Ax=b */
//			/* ------------------------------------------------------------------ */
//
//			for(i = 0; i < 2*n; i++)
//			{
//				X [i] = B [i];
//			}
//			klu_z_solve (Symbolic, Numeric, n, 1, X, Common);
//
//			/* ------------------------------------------------------------------ */
//			/* compute residual, rnorm = norm(b-Ax,1) / norm(A,1) */
//			/* ------------------------------------------------------------------ */
//
//			for(i = 0; i < 2*n; i++)
//			{
//				R [i] = B [i];
//			}
//			for(j = 0; j < n; j++)
//			{
//				asum = 0;
//				for(p = Ap [j]; p < Ap [j+1]; p++)
//				{
//					/* R(i) -= A(i,j) * X(j) */
//					i = Ai [p];
//					REAL(R,i) -= REAL(Ax,p) * REAL(X,j) - IMAG(Ax,p) * IMAG(X,j);
//					IMAG(R,i) -= IMAG(Ax,p) * REAL(X,j) + REAL(Ax,p) * IMAG(X,j);
//					asum += CABS(Ax, p);
//				}
//				anorm = MAX(anorm, asum);
//			}
//			rnorm = 0;
//			for(i = 0; i < n; i++)
//			{
//				rnorm = MAX (rnorm, CABS(R, i));
//			}
//
//			/* ------------------------------------------------------------------ */
//			/* free numeric factorization */
//			/* ------------------------------------------------------------------ */
//
//			klu_z_free_numeric (&Numeric, Common);
        }

        /* ---------------------------------------------------------------------- */
        /* free symbolic analysis, and residual */
        /* ---------------------------------------------------------------------- */

        //klu_free_symbolic (Symbolic, Common);
        Symbolic = null;

        return (1);
    }

    public static boolean lu(FloatMatrix A, int[] piv) {

        if(A.rows*A.columns<1)
            return true;

        int info = NativeBlas.sgetrf(A.rows, A.columns, A.data, 0, A.rows, piv, 0);
        if(info!=0){
            return false;
        }
        for(int i=0;i<piv.length;i++){
            piv[i]=piv[i]-1;
        }
        return true;
    }

    public static int distanceSq(int x1, int y1, int x2, int y2) {
        x2 -= x1;
        y2 -= y1;
        return x2 * x2 + y2 * y2;
    }

    public static void main(String[] args){

        int n = 10 ;
        int [ ] Ap = null;
        int [ ] Ai = null;
        double [ ] Ax = null;
        double [ ] b = null;

        long startTime = System.nanoTime();

        FlexCompColMatrix sparseMatrix = new FlexCompColMatrix(n,n);
//        CompColMatrix tst = new CompColMatrix()

        System.out.println("Matrix instantiated time msec: "+ (System.nanoTime()-startTime)/1000000);

        startTime = System.nanoTime();

        b= new double[n];

        Random rand = new Random();
        rand.setSeed(1212);

        for(int i=0;i<sparseMatrix.numColumns();i++){
            for(int m=0;m<rand.nextInt(50);m++){
                int k = rand.nextInt(sparseMatrix.numRows());
                double val = rand.nextDouble();
                sparseMatrix.set(i,k,val);
                sparseMatrix.set(k,i,val);
                sparseMatrix.set(i,i,rand.nextDouble());
            }

            b[i]=rand.nextDouble();
        }
        System.out.println("Matrix initialized time msec: "+ (System.nanoTime()-startTime)/1000000);
        startTime = System.nanoTime();

        CompColMatrix ccsm = new CompColMatrix(sparseMatrix);

//        new CompColMatrix()

        Ap= ccsm.getColumnPointers();
        Ai= ccsm.getRowIndices();
        Ax= ccsm.getData();

//        sparseMatrix.set(0,0,23f);



        KLU_symbolic Symbolic;
        KLU_numeric Numeric;
        KLU_common Common = new KLU_common();

        //Dklu_version.NPRINT = false ;
        //Dklu_internal.NDEBUG = false ;

        klu_defaults (Common);

//        long startTime = System.nanoTime();
        Symbolic = klu_analyze (n, Ap, Ai, Common);
        Numeric = klu_factor (Ap, Ai, Ax, Symbolic, Common);
        klu_solve (Symbolic, Numeric, n, 1, b, 0, Common);

        System.out.println("Execution time msec: "+ (System.nanoTime()-startTime)/1000000);
        System.out.println("Peak memory:"+ Common.mempeak+" status "+Common.status);

//        float mat[][] = {
//                {1.f,2.f,3.f},
//                {1.f,4.f,9.f},
//                {1.f,8.f,27.f}
//        };
//
//        int n=3;
////        FloatMatrix jmat = FloatMatrix.randn(n,n);
//        FloatMatrix jmat = new FloatMatrix(mat);
////        float mat[][] = jmat.toArray2();
//
//        //FloatMatrix jxvec = FloatMatrix.randn(n);
//        //float xvec[] = jxvec.toArray();
//
//        int pivj[] = new int[n];
//        int pivcu[] = new int[n];
//
//
//
//
//        FloatMatrix matcu = new FloatMatrix(mat);
//
//
//        long starttime = System.nanoTime();
//        boolean jblasres= lu(jmat,pivj);
//        System.out.println("Jblas completed: "+((System.nanoTime()-starttime)*1.e-6) + "  with rv="+String.valueOf(jblasres));
//        // library init should be done only once
//        cublasHandle handle = new cublasHandle();
//        cublasCreate(handle);
//        starttime = System.nanoTime();
//        boolean cudares = lu(matcu, pivcu, handle);
//        System.out.println("cuBlas completed: "+(1.e-6*(System.nanoTime()-starttime)) );
//        //release library handler
//        cublasDestroy(handle);
//// TODO compare these two results
//        System.out.println("Res: "+"");
//
///*
//
////        int n=mat.length;
////        INDArray arr = Nd4j.create(new float[]{
////                        1.f, 4.f, 7.f,
////                        2.f, 5.f, -2.f,
////                        3.f, 0.f, 3.f},
////                new int[]{n, n}, 'f');
//
//        INDArray arr = Nd4j.create(mat,'c');
//        INDArray arr2 = Nd4j.create(mat,'c');
//
////        int [] ipiv = new int[n];
//
//        INDArray circuitPermute = Nd4j.createArrayFromShapeBuffer(Nd4j.getDataBufferFactory().createInt((long)n), Nd4j.getShapeInfoProvider().createShapeInformation(new int[]{1, n}));
////          INDArray circuitPermute = Nd4j.create(n,'c');
////        INDArray circuitPermute = Nd4j.create(ipiv,[1,0]);
////        INDArray matx = Nd4j.create(new int[]{n,n},'f');
////        INDArray matx = Nd4j.create(mat);
//
//
//        getLapack();
//        long starttime = System.nanoTime();
//        boolean cudares = getrf(arr,n,circuitPermute);
//        System.out.println("Cuda completed: "+(System.nanoTime()-starttime));
//        arr.putScalar(0,0,23.4);
//        cudares = getrf(arr,n,circuitPermute);
//        System.out.println("Cuda completed2: "+(System.nanoTime()-starttime));
//        starttime = System.nanoTime();
//        boolean jblasres= lu(jmat,piv);
//        System.out.println("Jblas completed: "+(System.nanoTime()-starttime));
//
//        System.out.println("Res: "+"");
//
//    }
//
//    void deleteme(){
//
//        //Create random matrix in JBlas format
//        int n=1000;
//        FloatMatrix jmat = FloatMatrix.randn(n,n);
//        float mat[][] = jmat.toArray2();
//
//
//        //Pivot vector
//        int piv[] = new int[n];
//
//
//        //Initialize Nd4j matrix with previously created FloatMatrix
//        INDArray arr = Nd4j.create(mat,'c');
//        INDArray circuitPermute = Nd4j.createArrayFromShapeBuffer(Nd4j.getDataBufferFactory().createInt((long)n), Nd4j.getShapeInfoProvider().createShapeInformation(new int[]{1, n}));
//
//        //Initialize lapack handler
//        lapack = new JcublasLapack();
//
//        //Compute processing time for ND4J Cuda
//        long starttime = System.nanoTime();
//        lapack.sgetrf(n, n, arr, circuitPermute, INFO);
//        System.out.println("Cuda completed: "+(System.nanoTime()-starttime));
//
//        //Compute processing time for JBlas
//        starttime = System.nanoTime();
//        boolean jblasres= lu(jmat,piv);
//        System.out.println("Jblas completed: "+(System.nanoTime()-starttime));
//*/
    }
}
