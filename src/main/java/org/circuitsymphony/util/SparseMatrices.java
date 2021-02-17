package org.circuitsymphony.util;

import java.util.Random;

//import jeigen.DenseMatrix;
//import jeigen.SparseMatrixLil;
//import jeigen.SparseMatrixCCS;
//import static jeigen.Shortcuts.*;
//import jeigen;
//import org.la4j.Matrix;
//import org.la4j.decomposition.LUDecompositor;
//import org.la4j.matrix.SparseMatrix;
//import org.la4j.matrix.sparse.CCSMatrix;
//import org.la4j.matrix.sparse.CRSMatrix;
//import org.ojalgo.OjAlgoUtils;
//import org.ojalgo.array.LongToNumberMap;
//import org.ojalgo.array.Primitive64Array;
//import org.ojalgo.array.SparseArray;
//import org.ojalgo.matrix.decomposition.LU;
//import org.ojalgo.matrix.store.MatrixStore;
//import org.ojalgo.matrix.store.SparseStore;
//import org.ojalgo.matrix.task.iterative.ConjugateGradientSolver;
//import org.ojalgo.netio.BasicLogger;
//import org.ojalgo.series.BasicSeries;
//import org.ojalgo.type.CalendarDateUnit;
//import org.ojalgo.type.Stopwatch;

//import org.ujmp.core.Matrix;
//import org.ujmp.core.SparseMatrix;


public class SparseMatrices {
    private static final String NON_ZEROS = "{} non-zeroes out of {} matrix elements calculated in {}";
    private static final Random RANDOM = new Random();

    /*public static void main(final String[] args) {

        BasicLogger.debug();
        BasicLogger.debug(SparseMatrices.class.getSimpleName());
        BasicLogger.debug(OjAlgoUtils.getTitle());
        BasicLogger.debug(OjAlgoUtils.getDate());
        BasicLogger.debug();

        final int dim = 100;

        final SparseStore<Double> mtrxA = SparseStore.PRIMITIVE.make(dim, dim);
        final SparseStore<Double> mtrxB = SparseStore.PRIMITIVE.make(dim, dim);
        final SparseStore<Double> mtrxC = SparseStore.PRIMITIVE.make(dim, dim);
        final MatrixStore<Double> mtrxZ = MatrixStore.PRIMITIVE.makeZero(dim, dim).get();
        final MatrixStore<Double> mtrxI = MatrixStore.PRIMITIVE.makeIdentity(dim).get();

        // 5 matrices * 100k rows * 100k cols * 8 bytes per element => would be more than 372GB if dense
        // This program runs with default settings of any JVM

        for (int i = 0; i < dim; i++) {
            final int j = RANDOM.nextInt(dim);
            final double val = RANDOM.nextDouble();
            mtrxA.set(i, j, val);
        } // Each row of A contains 1 non-zero element at random column

        for (int j = 0; j < dim; j++) {
            final int i = RANDOM.nextInt(dim);
            final double val = RANDOM.nextDouble();
            mtrxB.set(i, j, val);
        } // Each column of B contains 1 non-zero element at random row

        final Stopwatch stopwatch = new Stopwatch();
        LU<Double> lu = LU.PRIMITIVE.make();
        lu.decompose(mtrxA);

        BasicLogger.debug();
        BasicLogger.debug("Sparse-Sparse multiplication");
        stopwatch.reset();
        mtrxA.multiply(mtrxB, mtrxC);
        BasicLogger.debug(NON_ZEROS, mtrxC.nonzeros().estimateSize(), mtrxC.count(), stopwatch.stop(CalendarDateUnit.MILLIS));

        // It's the left matrix that decides the multiplication algorithm,
        // and it knows nothing about the input/right matrix other than that it implements the required interface.
        // It could be another sparse matrix as in the example above. It could be a full/dense matrix. Or, it could something else...

        // Let's try an identity matrix...

        BasicLogger.debug();
        BasicLogger.debug("Sparse-Identity multiplication");
        stopwatch.reset();
        mtrxA.multiply(mtrxI, mtrxC);
        BasicLogger.debug(NON_ZEROS, mtrxC.nonzeros().estimateSize(), mtrxC.count(), stopwatch.stop(CalendarDateUnit.MILLIS));

        // ...or an all zeros matrix...

        BasicLogger.debug();
        BasicLogger.debug("Sparse-Zero multiplication");
        stopwatch.reset();
        mtrxA.multiply(mtrxZ, mtrxC);
        BasicLogger.debug(NON_ZEROS, mtrxC.nonzeros().estimateSize(), mtrxC.count(), stopwatch.stop(CalendarDateUnit.MILLIS));

        // What if we turn things around so that the identity or zero matrices become "this" (the left matrix)?

        BasicLogger.debug();
        BasicLogger.debug("Identity-Sparse multiplication");
        stopwatch.reset();
        mtrxI.multiply(mtrxB, mtrxC);
        BasicLogger.debug(NON_ZEROS, mtrxC.nonzeros().estimateSize(), mtrxC.count(), stopwatch.stop(CalendarDateUnit.MILLIS));

        BasicLogger.debug();
        BasicLogger.debug("Zero-Sparse multiplication");
        stopwatch.reset();
        mtrxZ.multiply(mtrxB, mtrxC);
        BasicLogger.debug(NON_ZEROS, mtrxC.nonzeros().estimateSize(), mtrxC.count(), stopwatch.stop(CalendarDateUnit.MILLIS));

        // Q: Why create identity and zero matrices?
        // A: The can be used as building blocks for larger logical structures.

        final MatrixStore<Double> mtrxL = mtrxI.logical().right(mtrxA).below(mtrxZ, mtrxB).get();

        // There's much more you can do with that logical builder...

        BasicLogger.debug();
        BasicLogger.debug("Scale logical structure");
        stopwatch.reset();
        final MatrixStore<Double> mtrxScaled = mtrxL.multiply(3.14);
        BasicLogger.debug("{} x {} matrix scaled in {}", mtrxScaled.countRows(), mtrxScaled.countColumns(), stopwatch.stop(CalendarDateUnit.MILLIS));

        // By now we would have passed 1TB, if dense

        SparseArray.factory(Primitive64Array.FACTORY, dim).make();

        LongToNumberMap.factory(Primitive64Array.FACTORY).make();
        BasicSeries.INSTANT.build(Primitive64Array.FACTORY);
        new ConjugateGradientSolver();

    }*/


//    public static void main(String[] args){
//
//        int dim=4000;
//        SparseMatrix sparse = SparseMatrix.Factory.zeros(dim, dim);
////        sparse.setAsDouble(2.0,0,0);
////        sparse.setAsDouble(1.0, 0, 0);
////        sparse.setAsDouble(3.0, 1, 1);
////        sparse.setAsDouble(4.0, 2, 2);
////        sparse.setAsDouble(-2.0, 3, 3);
////        sparse.setAsDouble(-2.0, 1, 3);
//
//        for (int i = 0; i < sparse.getSize()[0]; i++) {
//            final int j = RANDOM.nextInt((int) sparse.getSize()[0]);
//            final double val = RANDOM.nextDouble();
////            mtrxA.set(i, j, val);
//            sparse.setAsDouble(val, j, i);
//        } // Each row of A contains 1 non-zero element at random column
//
//        Matrix[] luDecomposition = sparse.lu();
//        System.out.println(luDecomposition.toString());
//    }

    public static void main(String[] args){
////        Matrix a = CRSMatrix.random(100, 100, 0.25 /* density */, new Random());
//
//        int dim = 10000;
//
//        Matrix a = CRSMatrix.zero(dim,dim);
//
//        for (int i = 0; i < a.rows(); i++) {
//            final int j = RANDOM.nextInt(a.rows());
//            final double val = RANDOM.nextDouble();
////            mtrxA.set(i, j, val);
////            sparse.setAsDouble(val, j, i);
//            a.set(i,j,val);
//        } // Each row of A contains 1 non-zero element at random column
//        System.out.println("Started decomposition");
//        LUDecompositor decomp = new LUDecompositor(a);
//
//        Matrix [] result=decomp.decompose();
//        System.out.println("Finished decomposition");
//        System.out.print(result);


//        DenseMatrix A = new DenseMatrix("1 2; 3 5; 7 9"); // matrix with 3 rows and 2 columns with values
//        // {{1,2},{3,5},{7,9}}
//        DenseMatrix B = new DenseMatrix(new double[][]{{4,3},{3,7}}); // matrix with 2 rows and 2 columns
//        DenseMatrix C = A.mmul(B); // mmul is matrix multiplication
//        System.out.println(C); // displays C formatted appropriately
//
//
////        SparseMatrixCCS sm1 = new SparseMatrixCCS(100,100);
////
////        sm1 = sm1.spzeros(100,100); // creates an empty 5*3 sparse matrix
//
//        SparseMatrixLil sm1;
//        sm1 = spzeros(500,500); // creates an empty 5*3 sparse matrix
////        sm1 = spdiag(rand(5,1)); // creates a sparse 5*5 diagonal matrix of random
//
//        // numbers
//        sm1 = speye(5); // creates a 5*5 identity matrix, sparse
//        sm1.svd


    }
}
