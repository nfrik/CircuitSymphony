# CircuitSymphony
Circuit simulation as a service

CircuitSymphony is a software tool for transient analysis of large circuits primarily made of passive nonlinear bipolar devices such as memristors, capacitors, and diodes with algorithms ported from Pillage et al. (Pillage 1994). The goal was to provide circuit simulation capabilities as a service with an option for horizontal scalability and support for multiprocessing in a publisher-subscriber pattern. 

It has the following features: 
1. Support for JSON netlist format
2. Sparse solvers on CPU and dense solvers on both CPU and GPU
3. Headless server mode with REST-API communication layer 
4. Improved GUI for visualization of large circuits as a graph 
5. Added support for containerization scalability
6. Added support for multiprocessing
7. Fully supported on Windows, Linux, and FreeBSD (OSX)

CircuitSymphony can solve circuits comprised of 100s of thousands of elements through adopting a single-threaded KLU and multithreaded NICSLU (Davis 2010, Chen 2013) direct sparse solvers which were ported for Linux, FreeBSD, and Windows and packaged with the software.

CircuitSymphony naturally handles modification, addition, and removal of circuit elements during runtime, as long as the change doesn't lead to a singular circuit matrix (such as open current sources or short circuit). Since the original goal of developing the headless server operation in CircuitSymphony was to analyze circuits made of bipolar passive devices, a token-based service layer allows multiple clients to perform simulations isolated from other users. Each user can create, modify and destroy circuits where the service layer resolves resource allocation and administers tasks. This property is useful in simulating dynamic circuits with randomly disappearing connections that, for example, can be observed in nanoscale composites.

<strong>Community</strong>

<strong>Acknowledgments</strong>
Paul Falstad for the original Java code (http://www.falstad.com/mathphysics.html) 

Developers of the circuitmod project for inspiring current version https://sourceforge.net/projects/circuitmod/

This project was supported by NSF grants no. 1748459, 1608847.
We want to thank Freescale LLC (NC, USA) for their support in developing components for this software.

<strong>Bibliography</strong>
<strong>L. Pillage, R. Rohrer, C. Visweswariah. Electronic Circuit and System Simulation Methods. Mcgraw-Hill, 1994 </strong>

<strong>Timothy A. Davis, Ekanathan Palamadai Natarajan. Algorithm 907. ACM Transactions on Mathematical Software 37, 1–17 Association for Computing Machinery (ACM), 2010.</strong>

<strong>Xiaoming Chen, Yu Wang, Huazhong Yang. NICSLU: An Adaptive Sparse Matrix Solver for Parallel Circuit Simulation. IEEE Transactions on Computer-Aided Design of Integrated Circuits and Systems 32, 261–274 Institute of Electrical and Electronics Engineers (IEEE), 2013. </strong>
