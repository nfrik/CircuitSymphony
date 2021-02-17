package test;

//import jcuda.Pointer;
//import jcuda.Sizeof;
//import jcuda.jcublas.cublasHandle;
//import jcuda.jcusolver.JCusolver;
//import jcuda.jcusolver.cusolverDnHandle;
//import jcuda.jcusparse.JCusparse;
//import jcuda.runtime.JCuda;
//import jcuda.runtime.cudaStream_t;
//import static jcuda.jcublas.cublasFillMode.*;
//import static jcuda.jcublas.cublasDiagType.*;
//import static jcuda.jcublas.cublasOperation.*;
//import static jcuda.jcublas.cublasSideMode.*;
//import static jcuda.jcublas.JCublas2.*;
//import static jcuda.jcusolver.JCusolverDn.*;
//import static jcuda.runtime.JCuda.*;
//import static jcuda.runtime.cudaMemcpyKind.*;

public class JCudaLinSolver {

//    private static int linearSolverLU(
//            cusolverDnHandle handle,
//            int n,
//            Pointer Acopy,
//            int lda,
//            Pointer b,
//            Pointer x)
//    {
//        int bufferSize[] = { 0 };
//        Pointer info = new Pointer();
//        Pointer buffer = new Pointer();
//        Pointer A = new Pointer();
//        Pointer ipiv = new Pointer(); // pivoting sequence
//        int h_info[] = { 0 };
//        long start, stop;
//        double time_solve;
//
//        cusolverDnDgetrf_bufferSize(handle, n, n, Acopy, lda, bufferSize);
//
//        cudaMalloc(info, Sizeof.INT);
//        cudaMalloc(buffer, Sizeof.DOUBLE*bufferSize[0]);
//        cudaMalloc(A, Sizeof.DOUBLE*lda*n);
//        cudaMalloc(ipiv, Sizeof.INT*n);
//
//
//        // prepare a copy of A because getrf will overwrite A with L
//        cudaMemcpy(A, Acopy, Sizeof.DOUBLE*lda*n, cudaMemcpyDeviceToDevice);
//        cudaMemset(info, 0, Sizeof.INT);
//
//        start = System.nanoTime();
//
//        cusolverDnDgetrf(handle, n, n, A, lda, buffer, ipiv, info);
//        cudaMemcpy(Pointer.to(h_info), info, Sizeof.INT, cudaMemcpyDeviceToHost);
//
//        if ( 0 != h_info[0] ){
//            System.err.printf("Error: LU factorization failed\n");
//        }
//
//        cudaMemcpy(x, b, Sizeof.DOUBLE*n, cudaMemcpyDeviceToDevice);
//        cusolverDnDgetrs(handle, CUBLAS_OP_N, n, 1, A, lda, ipiv, x, n, info);
//        cudaDeviceSynchronize();
//        stop = System.nanoTime();
//
//        time_solve = (stop - start) / 1e9;
//        System.out.printf("timing: LU = %10.6f sec\n", time_solve);
//
//        cudaFree(info  );
//        cudaFree(buffer);
//        cudaFree(A);
//        cudaFree(ipiv);
//
//        return 0;
//    }
//
//    public static void main(String[] args){
////        Pointer pointer = new Pointer();
////        JCuda.cudaMalloc(pointer,16);
////        System.out.println("Pointer: "+pointer);
////        JCuda.cudaFree(pointer);
//
//
//
//
//        cusolverDnHandle handle = new cusolverDnHandle();
//        int n=4;
//        int lda   = 0; // leading dimension in dense matrix
//
//        int bufferSize[] = { 0 };
//        Pointer info = new Pointer();
//        Pointer buffer = new Pointer();
//        Pointer A = new Pointer();
//
//        float mat[][] = {
//                {1,2,3},
//                {1,2,3},
//                {1,2,3}
//        };
//
//
//
//        Pointer ipiv = new Pointer(); // pivoting sequence
//        int h_info[] = { 0 };
//        long start, stop;
//        double time_solve;
//
//        cusolverDnDgetrf_bufferSize(handle, n, n, A, lda, bufferSize);
//    }
//
////    void copyHost2Device(float[][] mat,Pointer pointer,int ni,int nj){
////        cudaMemcpy(pointer,Pointer.to(mat),ni*nj,)
////    }
////
////    void copyDevice2Host(float[][] mat,Pointer pointer, int ni,int nj){
////
////    }
}
