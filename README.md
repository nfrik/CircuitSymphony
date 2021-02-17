# CircuitSymphony
Circuit simulator


The CircuitSymphony simulator is based on Paul Falstad's original Java code (http://www.falstad.com/mathphysics.html) with algorithms implemented from Pillage et al. (Pillage 1994). Besides a substantial code refactoring, we added a handful of modifications to the original code: enhanced support for various netlist formats, solving circuits with sparse solvers on CPU or dense solvers on both CPU and GPU, added headless API communication layer, improved GUI for visualizing large circuits, added support for containerization suitable for scalable, parallel simulations. CircuitSymphony can solve circuits comprised of 100s of thousands of elements through adopting efficient single-threaded KLU and multithreaded NICSLU (Chen 2013) direct sparse solvers.

CircuitSymphony naturally handles modification, addition, and removal of circuit elements during runtime, as long as the change doesn't lead to a singular circuit matrix (such as open current sources or short circuit). 

CircuitSymphony naturally handles modification, addition, and removal of circuit elements during runtime, as long as the change doesn't lead to a singular circuit matrix (such as open current sources or short circuit). Since the original goal of developing the headless server operation in CircuitSymphony was to analyze circuits made of bipolar passive devices, a token-based service layer allows multiple clients to perform simulations isolated from other users. Each user can create, modify and destroy circuits where the service layer resolves resource allocation and administers tasks. This property is advantageous in simulating dynamic circuits with randomly disappearing connections, that can be observed in nanoscale composites.

<strong>L. Pillage, R. Rohrer, C. Visweswariah. Electronic Circuit and System Simulation Methods. Mcgraw-Hill, 1994 </strong>

<strong>Xiaoming Chen, Yu Wang, Huazhong Yang. NICSLU: An Adaptive Sparse Matrix Solver for Parallel Circuit Simulation. IEEE Transactions on Computer-Aided Design of Integrated Circuits and Systems 32, 261–274 Institute of Electrical and Electronics Engineers (IEEE), 2013. </strong>
