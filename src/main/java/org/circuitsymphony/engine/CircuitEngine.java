package org.circuitsymphony.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.circuitsymphony.Constants;
import org.circuitsymphony.element.CapacitorElm;
import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.element.WireElm;
import org.circuitsymphony.element.io.CurrentElm;
import org.circuitsymphony.element.io.GroundElm;
import org.circuitsymphony.element.io.RailElm;
import org.circuitsymphony.element.io.VoltageElm;
import org.circuitsymphony.element.passive.InductorElm;
import org.circuitsymphony.engine.graph.GridPoint;
import org.circuitsymphony.engine.graph.ReverseNodeDefProvider;
import org.circuitsymphony.util.*;
import org.jblas.DoubleMatrix;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

//import org.la4j.matrix.SparseMatrix;
//import org.la4j.matrix.sparse.CCSMatrix;

public class CircuitEngine {
    public static final int LOG_SIZE = 100;
    public Vector<CircuitElm> elmList;
    public Vector<CircuitNode> nodeList;
    public CircularFifoQueue<String> log = new CircularFifoQueue<>(LOG_SIZE);
    //    public KLU_common Common = new KLU_common();
//    public KLU_symbolic Symbolic = new KLU_symbolic();
    public int subIterations;
    public boolean converged;
    public double t;
    public double timeStep;
    public double voltageRange = 5;
    public Point ps1, ps2;
    private CircuitEngineListener listener;
    //    private double[][] circuitMatrix;
    private DoubleMatrix circuitMatrixBlas;
    // TODO: 03/07/2020 look for ways to get rid of circuitSparseMatrix definition here
    private FlexCompRowMatrix circuitSparseMatrix;
    //    private MathUtil.SparseUtils circuitSparseTriplet = new MathUtil.SparseUtils();
    private CompRowMatrix circuitCSRM;
    private double[] circuitRightSide;
    private double[] origRightSide;
    //    private double[][] origMatrix;
    private DoubleMatrix origMatrixBlas;
    private FlexCompRowMatrix originalSparseMatrix;
    //    private MathUtil.SparseUtils originalSparseTriplet = new MathUtil.SparseUtils();
    private RowInfo[] circuitRowInfo;
    private int[] circuitPermute;
    private boolean circuitNonLinear;
    private int voltageSourceCount;
    private int circuitMatrixSize;
    private int circuitMatrixFullSize;
    private boolean circuitNeedsMap;
    private CircuitElm[] voltageSources;
    private boolean dumpMatrix;
    private long lastIterTime;
    private double iterationCount;
    private String stopMessage;
    private CircuitElm stopElm;
    private int gridSize;
    private int gridMask;
    private int gridRound;
    private int fixedIterations = -1;
    private NicsluSolver nicsluSolver;
    private boolean isNanOrInf;

    private List<List<CircuitElm>> elms;
    private boolean useBLAS;
    private long currentTime = -1;

    //Set this field to manage solver
    // 1 - NICSLU + JBLAS
    // 2 - NICSLU
    // 3 - JBLAS
    private final int mode = 1;
    // Set this field to manage findPath() optimization with element list for every node
    // true - use optimization
    // false - don't use optimization
    private final boolean useOptimization = false;

    public CircuitEngine(CircuitEngineListener listener) {
        this.listener = listener;
        elmList = new Vector<>();
        ps1 = new Point();
        ps2 = new Point();
        nicsluSolver = new NicsluSolver();
    }

    // control voltage source vs with voltage from n1 to n2 (must
    // also call stampVoltageSource())
    public void stampVCVS(int n1, int n2, double coef, int vs) {
        int vn = nodeList.size() + vs;
        stampMatrix(vn, n1, coef);
        stampMatrix(vn, n2, -coef);
    }

    // stamp independent voltage source #vs, from n1 to n2, amount v
    public void stampVoltageSource(int n1, int n2, int vs, double v) {
        int vn = nodeList.size() + vs;
        stampMatrix(vn, n1, -1);
        stampMatrix(vn, n2, 1);
        stampRightSide(vn, v);
        stampMatrix(n1, vn, 1);
        stampMatrix(n2, vn, -1);
    }

    // use this if the amount of voltage is going to be updated in doStep()
    public void stampVoltageSource(int n1, int n2, int vs) {
        int vn = nodeList.size() + vs;
        stampMatrix(vn, n1, -1);
        stampMatrix(vn, n2, 1);
        stampRightSide(vn);
        stampMatrix(n1, vn, 1);
        stampMatrix(n2, vn, -1);
    }

    public void updateVoltageSource(int vs, double v) {
        int vn = nodeList.size() + vs;
        stampRightSide(vn, v);
    }

    public void stampResistor(int n1, int n2, double r) {
        double r0 = 1 / r;
        if (Double.isNaN(r0) || Double.isInfinite(r0)) {
            System.out.print("bad resistance " + r + " " + r0 + "\n");
            int a = 0;
            a /= a;
        }
        stampMatrix(n1, n1, r0);
        stampMatrix(n2, n2, r0);
        stampMatrix(n1, n2, -r0);
        stampMatrix(n2, n1, -r0);
    }

    public void stampConductance(int n1, int n2, double r0) {
        stampMatrix(n1, n1, r0);
        stampMatrix(n2, n2, r0);
        stampMatrix(n1, n2, -r0);
        stampMatrix(n2, n1, -r0);
    }

    // current from cn1 to cn2 is equal to voltage from vn1 to 2, divided by g
    public void stampVCCurrentSource(int cn1, int cn2, int vn1, int vn2, double g) {
        stampMatrix(cn1, vn1, g);
        stampMatrix(cn2, vn2, g);
        stampMatrix(cn1, vn2, -g);
        stampMatrix(cn2, vn1, -g);
    }

    public void stampCurrentSource(int n1, int n2, double i) {
        stampRightSide(n1, -i);
        stampRightSide(n2, i);
    }

    // stamp a current source from n1 to n2 depending on current through vs
    public void stampCCCS(int n1, int n2, int vs, double gain) {
        int vn = nodeList.size() + vs;
        stampMatrix(n1, vn, gain);
        stampMatrix(n2, vn, -gain);
    }

    // stamp value x in row i, column j, meaning that a voltage change
    // of dv in node j will increase the current into node i by x dv.
    // (Unless i or j is a voltage source node.)
    public void stampMatrix(int i, int j, double x) {
        if (i > 0 && j > 0) {
            if (circuitNeedsMap) {
                i = circuitRowInfo[i - 1].getMapRow();
                RowInfo ri = circuitRowInfo[j - 1];
                if (ri.getType() == RowInfo.ROW_CONST) {
                    // System.out.println("Stamping constant " + i + " " + j + "
                    // " + x);
                    circuitRightSide[i] -= x * ri.getValue();
                    return;
                }
                j = ri.getMapCol();
                // System.out.println("stamping " + i + " " + j + " " + x);
            } else {
                i--;
                j--;
            }
//            circuitMatrix[i][j] += x;
            if (useBLAS) circuitMatrixBlas.put(i, j, circuitMatrixBlas.get(i, j) + x);
            else circuitSparseMatrix.add(i,j,x);
        }
    }

    // stamp value x on the right side of row i, representing an
    // independent current source flowing into node i
    public void stampRightSide(int i, double x) {
        if (i > 0) {
            if (circuitNeedsMap) {
                i = circuitRowInfo[i - 1].getMapRow();
                // System.out.println("stamping " + i + " " + x);
            } else
                i--;
            circuitRightSide[i] += x;
        }
    }

    // indicate that the value on the right side of row i changes in doStep()
    public void stampRightSide(int i) {
        // System.out.println("rschanges true " + (i-1));
        if (i > 0)
            circuitRowInfo[i - 1].setRsChanges(true);
    }

    // indicate that the values on the left side of row i change in doStep()
    public void stampNonLinear(int i) {
        if (i > 0)
            circuitRowInfo[i - 1].setLsChanges(true);
    }

    public void analyzeCircuit() throws Exception {
//        System.out.println("Analyzing circuit");
        if (elmList.isEmpty())
            return;
        stopMessage = null;
        stopElm = null;
        int i, j;
        int vscount = 0;
        nodeList = new Vector<>();
        boolean gotGround = false;
        boolean gotRail = false;
        CircuitElm volt = null;

        if (mode==1) useBLAS = elmList.size()<=1000;
        else if (mode==2) useBLAS = false;
        else useBLAS = true;

//        Logger.getLogger(CircuitEngine.class.getSimpleName()).log(Level.INFO, "Analyzing circuit of size [" + elmList.size() + "] using ["+(useBLAS?"JBLAS":"NICSLU") + "], ["+(useOptimization?"with":"without") + "] findPath() optimization ... ");
//        time(); // start time measurement

        // System.out.println("ac1");
        // look for voltage or ground element
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            if (ce instanceof GroundElm) {
                gotGround = true;
                break;
            }
            if (ce instanceof RailElm)
                gotRail = true;
            if (volt == null && ce instanceof VoltageElm)
                volt = ce;
        }

        // if no ground, and no rails, then the voltage elm's first terminal
        // is ground
        if (!gotGround && volt != null && !gotRail) {
            CircuitNode cn = new CircuitNode();
            Point pt = volt.getPost(0);
            cn.setX(pt.x);
            cn.setY(pt.y);
            nodeList.addElement(cn);
        } else {
            // otherwise allocate extra node for ground
            CircuitNode cn = new CircuitNode();
            cn.setX(-1);
            cn.setY(-1);
            nodeList.addElement(cn);
        }
        // System.out.println("ac2");

        // allocate nodes and voltage sources
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            int inodes = ce.getInternalNodeCount();
            int ivs = ce.getVoltageSourceCount();
            int posts = ce.getPostCount();

            // allocate a node for each post and match posts to nodes
            for (j = 0; j != posts; j++) {
                Point pt = ce.getPost(j);
                int k;
                for (k = 0; k != nodeList.size(); k++) {
                    CircuitNode cn = getCircuitNode(k);
                    if (pt.x == cn.getX() && pt.y == cn.getY())
                        break;
                }
                if (k == nodeList.size()) {
                    CircuitNode cn = new CircuitNode();
                    cn.setX(pt.x);
                    cn.setY(pt.y);
                    CircuitNodeLink cnl = new CircuitNodeLink(j, ce);
                    cn.getLinks().addElement(cnl);
                    ce.setNode(j, nodeList.size());
                    nodeList.addElement(cn);
                } else {
                    CircuitNodeLink cnl = new CircuitNodeLink(j, ce);
                    getCircuitNode(k).getLinks().addElement(cnl);
                    ce.setNode(j, k);
                    // if it's the ground node, make sure the node voltage is 0,
                    // cause it may not get set later
                    if (k == 0)
                        ce.setNodeVoltage(j, 0);
                }
            }
            for (j = 0; j != inodes; j++) {
                CircuitNode cn = new CircuitNode();
                cn.setX(-1);
                cn.setY(-1);
                cn.setInternal(true);
                CircuitNodeLink cnl = new CircuitNodeLink(j + posts, ce);
                cn.getLinks().addElement(cnl);
                ce.setNode(cnl.getNum(), nodeList.size());
                nodeList.addElement(cn);
            }
            vscount += ivs;
        }
        voltageSources = new CircuitElm[vscount];
        vscount = 0;
        circuitNonLinear = false;
        // System.out.println("ac3");

        // determine if circuit is nonlinear
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            if (ce.nonLinear())
                circuitNonLinear = true;
            int ivs = ce.getVoltageSourceCount();
            for (j = 0; j != ivs; j++) {
                voltageSources[vscount] = ce;
                ce.setVoltageSource(j, vscount++);
            }
        }
        voltageSourceCount = vscount;

        int matrixSize = nodeList.size() - 1 + vscount;

        if (useBLAS) circuitMatrixBlas = new DoubleMatrix(matrixSize, matrixSize);
        else circuitSparseMatrix = new FlexCompRowMatrix(matrixSize, matrixSize);

//        circuitMatrix = new double[matrixSize][matrixSize];
        circuitRightSide = new double[matrixSize];
//        origMatrix = new double[matrixSize][matrixSize];
//        originalSparseMatrix = new FlexCompRowMatrix(matrixSize,matrixSize);
        origRightSide = new double[matrixSize];
        circuitMatrixSize = circuitMatrixFullSize = matrixSize;
        circuitRowInfo = new RowInfo[matrixSize];
        if (useBLAS) circuitPermute = new int[matrixSize];
        // int vs = 0;
        for (i = 0; i != matrixSize; i++)
            circuitRowInfo[i] = new RowInfo();
        circuitNeedsMap = false;

        // stamp linear circuit elements
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            ce.stamp();
        }
        // System.out.println("ac4");

        // determine nodes that are unconnected
        boolean closure[] = new boolean[nodeList.size()];
        // boolean tempclosure[] = new boolean[nodeList.size()];
        boolean changed = true;
        closure[0] = true;

        if (useOptimization) {
            elms = new ArrayList<>();
            for (i=0;i<nodeList.size();i++) {
                elms.add(new ArrayList<>());
            }
        }

        while (changed) {
            changed = false;
            for (i = 0; i != elmList.size(); i++) {
                CircuitElm ce = getElm(i);

                // loop through all ce's nodes to see if they are connected
                // to other nodes not in closure
                for (j = 0; j < ce.getPostCount(); j++) {

                    if (useOptimization) elms.get(ce.getNode(j)).add(ce);

                    if (!closure[ce.getNode(j)]) {
                        if (ce.hasGroundConnection(j))
                            closure[ce.getNode(j)] = changed = true;
                        continue;
                    }
                    int k;
                    for (k = 0; k != ce.getPostCount(); k++) {
                        if (j == k)
                            continue;
                        int kn = ce.getNode(k);
                        if (ce.getConnection(j, k) && !closure[kn]) {
                            closure[kn] = true;
                            changed = true;
                        }
                    }
                }
            }
            if (changed)
                continue;

            // connect unconnected nodes
            for (i = 0; i != nodeList.size(); i++)
                if (!closure[i] && !getCircuitNode(i).isInternal()) {
                    // System.out.println("node " + i + " unconnected");
                    stampResistor(0, i, 1e8);
                    closure[i] = true;
                    changed = true;
                    break;
                }
        }
        // System.out.println("ac5");
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            // look for inductors with no current path
            if (ce instanceof InductorElm) {
                FindPathInfo fpi = new FindPathInfo(FindPathInfo.INDUCT, ce);
                // first try findPath with maximum depth of 5, to avoid
                // slowdowns
                if (!fpi.findPath(ce.getNode(0), 5)
                        && !fpi.findPath(ce.getNode(0))) {
                    System.out.println(ce + " no path");
                    ce.reset();
                }
            }
            // look for current sources with no current path
            if (ce instanceof CurrentElm) {
                FindPathInfo fpi = new FindPathInfo(FindPathInfo.INDUCT, ce);
                if (!fpi.findPath(ce.getNode(0))) {
                    stop("No path for current source!", ce);
                    return;
                }
            }
            // look for voltage source loops
            if ((ce instanceof VoltageElm && ce.getPostCount() == 2)
                    || ce instanceof WireElm) {
                FindPathInfo fpi = new FindPathInfo(FindPathInfo.VOLTAGE, ce);
                if (fpi.findPath(ce.getNode(0))) {
                    stop("Voltage source/wire loop with no resistance!", ce);
                    return;
                }
            }
            // look for shorted caps, or caps w/ voltage but no R
            if (ce instanceof CapacitorElm) {
                FindPathInfo fpi = new FindPathInfo(FindPathInfo.SHORT, ce);
                if (fpi.findPath(ce.getNode(0))) {
                    System.out.println(ce + " shorted");
                    ce.reset();
                } else {
                    fpi = new FindPathInfo(FindPathInfo.CAP_V, ce);
                    if (fpi.findPath(ce.getNode(0))) {
                        stop("Capacitor loop with no resistance!", ce);
                        return;
                    }
                }
            }
        }
        // System.out.println("ac6");
//        long secondTime=System.currentTimeMillis();
//        System.out.println("Time prior to simplification: "+(double)(secondTime-startTime)/1000d+"s");
        // simplify the matrix; this speeds things up quite a bit

        //START
        for (i = 0; i != matrixSize; i++) {
            int qm = -1, qp = -1;
            double qv = 0;
            RowInfo re = circuitRowInfo[i];
            /*
             * System.out.println("row " + i + " " + re.lsChanges + " " +
             * re.rsChanges + " " + re.dropRow);
             */
            if (re.isLsChanges() || re.isDropRow() || re.isRsChanges())
                continue;
            double rsadd = 0;

            // look for rows that can be removed
            for (j = 0; j != matrixSize; j++) {
//                double q = circuitMatrix[i][j];
//                double q = circuitMatrixBlas.get(i, j);
                double q = useBLAS?circuitMatrixBlas.get(i, j):circuitSparseMatrix.get(i,j);
                if (circuitRowInfo[j].getType() == RowInfo.ROW_CONST) {
                    // keep a running total of const values that have been
                    // removed already
                    rsadd -= circuitRowInfo[j].getValue() * q;
                    continue;
                }
                if (q == 0)
                    continue;
                if (qp == -1) {
                    qp = j;
                    qv = q;
                    continue;
                }
                if (qm == -1 && q == -qv) {
                    qm = j;
                    continue;
                }
                break;
            }
            /*
             * //System.out.println("line " + i + " " + qp + " " + qm + " " +
             * j); if (qp != -1 && circuitRowInfo[qp].lsChanges) {
             * System.out.println("lschanges"); continue; } if (qm != -1 &&
             * circuitRowInfo[qm].lsChanges) { System.out.println("lschanges");
             * continue; }
             */
                if (j == matrixSize) {
                    if (qp == -1) {
                        stop("Matrix error", null);
                        return;
                    }
                    RowInfo elt = circuitRowInfo[qp];
                    if (qm == -1) {
                        // we found a row with only one nonzero entry; that value
                        // is a constant
                        int k;
                        for (k = 0; elt.getType() == RowInfo.ROW_EQUAL && k < 100; k++) {
                            // follow the chain
                            /*
                             * System.out.println("following equal chain from " + i
                             * + " " + qp + " to " + elt.nodeEq);
                             */
                            qp = elt.getNodeEq();
                            elt = circuitRowInfo[qp];
                        }
                        if (elt.getType() == RowInfo.ROW_EQUAL) {
                            // break equal chains
                            // System.out.println("Break equal chain");
                            elt.setType(RowInfo.ROW_NORMAL);
                            continue;
                        }
                        if (elt.getType() != RowInfo.ROW_NORMAL) {
                            System.out.println("type already " + elt.getType() + " for "
                                    + qp + "!");
                            continue;
                        }
                        elt.setType(RowInfo.ROW_CONST);
                        elt.setValue((circuitRightSide[i] + rsadd) / qv);
                        circuitRowInfo[i].setDropRow(true);
                        // System.out.println(qp + " * " + qv + " = const " +
                        // elt.value);
                        i = -1; // start over from scratch
                    } else if (circuitRightSide[i] + rsadd == 0) {
                        // we found a row with only two nonzero entries, and one
                        // is the negative of the other; the values are equal
                        if (elt.getType() != RowInfo.ROW_NORMAL) {
                            // System.out.println("swapping");
                            int qq = qm;
                            qm = qp;
                            qp = qq;
                            elt = circuitRowInfo[qp];
                            if (elt.getType() != RowInfo.ROW_NORMAL) {
                                // we should follow the chain here, but this
                                // hardly ever happens so it's not worth worrying
                                // about
    //                            System.out.println("swap failed");
                                continue;
                            }
                        }
                        elt.setType(RowInfo.ROW_EQUAL);
                        elt.setNodeEq(qm);
                        circuitRowInfo[i].setDropRow(true);
                        // System.out.println(qp + " = " + qm);
                    }
                }
        }

        // System.out.println("ac7");
//        long simpleTime=System.currentTimeMillis();
//        System.out.println("Simplification time: "+(double)(simpleTime-secondTime)/1000d+"s");
        // find size of new matrix
        int nn = 0;
        for (i = 0; i != matrixSize; i++) {
            RowInfo elt = circuitRowInfo[i];
            if (elt.getType() == RowInfo.ROW_NORMAL) {
                elt.setMapCol(nn++);
                // System.out.println("col " + i + " maps to " + elt.mapCol);
                continue;
            }
            if (elt.getType() == RowInfo.ROW_EQUAL) {
                RowInfo e2;
                // resolve chains of equality; 100 max steps to avoid loops
                for (j = 0; j != 100; j++) {
                    e2 = circuitRowInfo[elt.getNodeEq()];
                    if (e2.getType() != RowInfo.ROW_EQUAL)
                        break;
                    if (i == e2.getNodeEq())
                        break;
                    elt.setNodeEq(e2.getNodeEq());
                }
            }
            if (elt.getType() == RowInfo.ROW_CONST)
                elt.setMapCol(-1);
        }
        for (i = 0; i != matrixSize; i++) {
            RowInfo elt = circuitRowInfo[i];
            if (elt.getType() == RowInfo.ROW_EQUAL) {
                RowInfo e2 = circuitRowInfo[elt.getNodeEq()];
                if (e2.getType() == RowInfo.ROW_CONST) {
                    // if something is equal to a const, it's a const
                    elt.setType(e2.getType());
                    elt.setValue(e2.getValue());
                    elt.setMapCol(-1);
                    // System.out.println(i + " = [late]const " + elt.value);
                } else {
                    elt.setMapCol(e2.getMapCol());
                    // System.out.println(i + " maps to: " + e2.mapCol);
                }
            }
        }
        // System.out.println("ac8");

        /*
         * System.out.println("matrixSize = " + matrixSize);
         *
         * for (j = 0; j != circuitMatrixSize; j++) { System.out.println(j +
         * ": "); for (i = 0; i != circuitMatrixSize; i++)
         * System.out.print(circuitMatrix[j][i] + " "); System.out.print("  " +
         * circuitRightSide[j] + "\n"); } System.out.print("\n");
         */

        // make the new, simplified matrix
        int newsize = nn;
        double newmatx[][] = new double[1][1];
//        FlexCompRowMatrix newmatx = new FlexCompRowMatrix(newsize,newsize);
        if (useBLAS) newmatx = new double[newsize][newsize];
        else originalSparseMatrix = new FlexCompRowMatrix(newsize, newsize);

        double newrs[] = new double[newsize];
        int ii = 0;
        for (i = 0; i != matrixSize; i++) {
            RowInfo rri = circuitRowInfo[i];
            if (rri.isDropRow()) {
                rri.setMapRow(-1);
                continue;
            }
            newrs[ii] = circuitRightSide[i];
            rri.setMapRow(ii);
            // System.out.println("Row " + i + " maps to " + ii);
            for (j = 0; j != matrixSize; j++) {
                RowInfo ri = circuitRowInfo[j];
                double value=useBLAS?circuitMatrixBlas.get(i,j):circuitSparseMatrix.get(i, j);
                if (ri.getType() == RowInfo.ROW_CONST)
//                    newrs[ii] -= ri.getValue() * circuitMatrixBlas.get(i, j);
                    newrs[ii] -= ri.getValue() * value;
                else
//                    newmatx[ii][ri.getMapCol()] += circuitMatrixBlas.get(i, j);
//                      newmatx[ii][ri.getMapCol()] += circuitSparseMatrix.get(i, j);
//                    newmatx.set(ii,ri.getMapCol(),newmatx.get(ii,ri.getMapCol())+circuitSparseMatrix.get(i, j));
                    if (value!=0) {
                        if (useBLAS) newmatx[ii][ri.getMapCol()] += value;
                        else originalSparseMatrix.add(ii, ri.getMapCol(), value);
                    }
            }
            ii++;
        }
        if (newsize>0) {
            if (useBLAS) circuitMatrixBlas = new DoubleMatrix(newmatx);
            else circuitSparseMatrix = new FlexCompRowMatrix(originalSparseMatrix);
        }
        else {
            if (useBLAS) circuitMatrixBlas = new DoubleMatrix(0);
            else circuitSparseMatrix = new FlexCompRowMatrix(0, 0);
        }

        if (useBLAS) origMatrixBlas = circuitMatrixBlas.dup();

//        System.out.println("Time after building new matrix: "+(double)(System.currentTimeMillis()-simpleTime)/1000d+"s");
//        circuitMatrix = newmatx;
//        if(0!= newsize)
////            circuitMatrixBlas = new DoubleMatrix(newmatx);
////            circuitSparseMatrix = CCSMatrix.from2DArray(newmatx);
//            circuitSparseMatrix = new FlexCompRowMatrix(newmatx);
//        else
//            //            circuitMatrixBlas = new DoubleMatrix(0);
////            circuitSparseMatrix = CCSMatrix.zero(0,0);
//            circuitSparseMatrix = new FlexCompRowMatrix(0,0);

        circuitRightSide = newrs;
        matrixSize = circuitMatrixSize = newsize;
        for (i = 0; i != matrixSize; i++)
            origRightSide[i] = circuitRightSide[i];



//        circuitSparseTriplet.get_triplet(circuitSparseMatrix);
        /*
        for(int gidx=0; gidx<circuitSparseTriplet.values.length; gidx++){
            int col=circuitSparseTriplet.cols[gidx];
            int row=circuitSparseTriplet.rows[gidx];
            double val=circuitSparseTriplet.values[gidx];
            originalSparseMatrix.set(row,col,val);
        }*/
//        originalSparseTriplet.get_triplet(originalSparseMatrix);
//        for (i = 0; i != matrixSize; i++)
//            for (j = 0; j != matrixSize; j++)
////                origMatrixBlas.put(i, j, circuitMatrixBlas.get(i, j));
//                originalSparseMatrix.set(i,j,circuitSparseMatrix.get(i,j));


//        for(int col=0;col!=matrixSize;col++){
//            SparseVector spv = circuitSparseMatrix.getColumn(col);
//            for(int ind=0; ind<spv.getIndex().length; ind++){
//                originalSparseMatrix.set(spv.getIndex()[ind],col,spv.getData()[ind]);
//            }
//        }

        circuitNeedsMap = true;

        /*
         * System.out.println("matrixSize = " + matrixSize + " " +
         * circuitNonLinear); for (j = 0; j != circuitMatrixSize; j++) { for (i
         * = 0; i != circuitMatrixSize; i++)
         * System.out.print(circuitMatrix[j][i] + " "); System.out.print("  " +
         * circuitRightSide[j] + "\n"); } System.out.print("\n");
         */

        // if a matrix is linear, we can do the lu_factor here instead of
        // needing to do it every frame
        if (!circuitNonLinear) {
            if (useBLAS) {
                if (!MathUtil.luJBLAS(circuitMatrixBlas, circuitPermute)) {
                    stop("Singular matrix!", null);
                }
                MathUtil.lu_solve(circuitMatrixBlas, circuitMatrixSize, circuitPermute, circuitRightSide);
            }
            else {
                circuitCSRM =new CompRowMatrix(circuitSparseMatrix);
//                Symbolic = MathUtil.klu_decomp(circuitCCSM,Common);
                nicsluSolver.SetMatrix(circuitCSRM);
                boolean nonsingular = nicsluSolver.AnalyzeAndFactorize();
                log.offer(nicsluSolver.getStatistics());

                if (!nonsingular) {
                    stop(Constants.SINGULAR_MSG, null);
                }
            }

//            if (Symbolic==null) {
//                stop(Constants.SINGULAR_MSG, null);
//            }
//            if (!MathUtil.lu(circuitMatrixBlas, circuitPermute)) {
//                stop("Singular matrix!", null);
//            }
        }

        //added NaN or Infinite matrix check here
        boolean stop = false;
        isNanOrInf = false;

        if (useBLAS) {
            for (j = 0; j != circuitMatrixSize; j++) {
                if (stop) break;
                for (i = 0; i != circuitMatrixSize; i++) {
                    double x = circuitMatrixBlas.get(i,j);
                    if (Double.isNaN(x) || Double.isInfinite(x)) {
                        isNanOrInf = stop = true;
                        break;
                    }
                }
            }

        }

        else {
            for (i=0;i<matrixSize;i++) {
                if (stop) break;
                SparseVector sparseVector = originalSparseMatrix.getRow(i);
                double[] values = sparseVector.getData();
                for (double x : values) {
                    if (Double.isNaN(x) || Double.isInfinite(x)) {
                        isNanOrInf = stop = true;
                        break;
                    }
                }
            }
        }
//        System.out.print(" done in ");
//        time();
//        System.out.println(nicsluSolver.getStatistics());
//        Logger.getLogger(getClass().getSimpleName()).log(Level.INFO,"Analyzing circuit mempeak: "+Common.mempeak+" flops: "+Common.flops+" condest: "+Common.condest+" rcond: "+Common.rcond);
    }


    public void runCircuit() throws Exception {
//        if (circuitMatrixBlas == null || elmList.size() == 0) {
//            circuitMatrixBlas = null;
//            return;
//        }
        if (((useBLAS && circuitMatrixBlas==null) || (!useBLAS && circuitSparseMatrix == null)) || elmList.size() == 0) return;
        int iter;
        // int maxIter = getIterCount();
        boolean debugprint = dumpMatrix;
        dumpMatrix = false;
        long steprate = (long) (160 * getIterationCount());
        long tm = System.currentTimeMillis();
        long lit = lastIterTime;
        if (fixedIterations == -1) {
            if (1000 >= steprate * (tm - lastIterTime)) {
                return;
            }
        }

        for (iter = 1; ; iter++) {
            int i, j, k, subiter;
            for (i = 0; i != elmList.size(); i++) {
                CircuitElm ce = getElm(i);
                ce.startIteration();
            }
            final int subiterCount = 5000;
            for (subiter = 0; subiter != subiterCount; subiter++) {
                converged = true;
                subIterations = subiter;
                for (i = 0; i != circuitMatrixSize; i++) {
                    circuitRightSide[i] = origRightSide[i];
                }
                if (circuitNonLinear) {
//                    circuitSparseMatrix=originalSparseMatrix.copy();
//                    circuitSparseMatrix = new LinkedSparseMatrix(originalSparseMatrix);

//                    for(Iterator elem = originalSparseMatrix.iterator(); elem.hasNext();){
//                        MatrixEntry nxt = (MatrixEntry) elem.next();
//                        double x = nxt.get();
//                        int row = nxt.row();
//                        int col = nxt.column();
//                        if(row<circuitMatrixSize && col<circuitMatrixSize) {
//                            circuitSparseMatrix.set(row, col, x);
//                        }
//                    }



//                    for(int col=0;col!=circuitMatrixSize;col++){
//                        SparseVector spv = originalSparseMatrix.getColumn(col);
//                        for(int rowind=0; rowind<spv.getIndex().length; rowind++){
//                            if(spv.getIndex()[rowind]<circuitMatrixSize)
//                            circuitSparseMatrix.set(spv.getIndex()[rowind],col,spv.getData()[rowind]);
//                        }
//                    }

//                    for (i = 0; i != circuitMatrixSize; i++) {
//                        for (j = 0; j != circuitMatrixSize; j++) {
////                            circuitMatrixBlas.put(i, j, origMatrixBlas.get(i, j));
//                            circuitSparseMatrix.set(i,j,originalSparseMatrix.get(i,j));
//                        }
//                    }
                    if (useBLAS) circuitMatrixBlas.copy(origMatrixBlas);
                    else {
                        circuitSparseMatrix=circuitSparseMatrix.zero();
                        circuitSparseMatrix.set(originalSparseMatrix);
                    }
                    /*
                    for(int gidx=0; gidx<originalSparseTriplet.values.length; gidx++){
                        int col=originalSparseTriplet.cols[gidx];
                        int row=originalSparseTriplet.rows[gidx];
                        double val=originalSparseTriplet.values[gidx];
                        if(row < circuitMatrixSize && col < circuitMatrixSize)
                            circuitSparseMatrix.set(row,col,val);
                    }*/
                }
                for (i = 0; i != elmList.size(); i++) {
                    CircuitElm ce = getElm(i);
                    ce.doStep();
                }
                if (stopMessage != null)
                    return;
                boolean printit = debugprint;
                debugprint = false;

//                for (j = 0; j != circuitMatrixSize; j++) {
//                    for (i = 0; i != circuitMatrixSize; i++) {
////                        double x = circuitMatrixBlas.get(i, j);
//                        double x = circuitSparseMatrix.get(i, j);
//                        if (Double.isNaN(x) || Double.isInfinite(x)) {
//                            stop("nan/infinite matrix!", null);
//                            return;
//                        }
//                    }
//                }
//                for(double x: circuitSparseTriplet.values){
//                    if (Double.isNaN(x) || Double.isInfinite(x)) {
//                        stop("nan/infinite matrix!", null);
//                        return;
//                    }
//                }
                if (isNanOrInf) {
                    stop("nan/infinite matrix!", null);
                    return;
                }

                if (printit) {
                    for (j = 0; j != circuitMatrixSize; j++) {
                        for (i = 0; i != circuitMatrixSize; i++) {
                            System.out.print(circuitMatrixBlas.get(j, i) + ",");
                        }
                        System.out.print("  " + circuitRightSide[j] + "\n");
                    }
                    System.out.print("\n");
                }
                if (circuitNonLinear) {
                    if (converged && subiter > 0)
                        break;

                    if (useBLAS) {
                        if (!MathUtil.luJBLAS(circuitMatrixBlas, circuitPermute)) {
                            stop("Singular matrix!", null);
                            return;
                        }
                    }
                    else {
                        circuitCSRM =new CompRowMatrix(circuitSparseMatrix);
                        nicsluSolver.SetMatrix(circuitCSRM);
                        boolean nonsingular = nicsluSolver.AnalyzeAndFactorize();

                        log.offer(nicsluSolver.getStatistics());
                        if (!nonsingular) {
                            stop(Constants.SINGULAR_MSG, null);
                        }
                    }
//                    Symbolic = MathUtil.klu_decomp(circuitCCSM,Common);
//                    if (Symbolic==null) {
//                        stop(Constants.SINGULAR_MSG, null);
//                    }

//                    if (!MathUtil.lu(circuitMatrixBlas, circuitPermute)) {
//                        stop("Singular matrix!", null);
//                        return;
//                    }
//                    circuitCCSM=new CompColMatrix(circuitSparseMatrix);
//                    if (!MathUtil.klu_decomp(circuitCCSM)) {
//                        stop("Singular matrix!", null);
//                        return;
//                    }
                }
//                MathUtil.lu_solve(circuitMatrixBlas, circuitMatrixSize, circuitPermute, circuitRightSide);
//                circuitCCSM=new CompColMatrix(circuitSparseMatrix);

                //nicsluSolver.SetMatrix(circuitCSRM);
                //boolean stat = nicsluSolver.AnalyzeAndFactorize();
//                double[] circuitRightSide2 = circuitRightSide.clone();
                if (useBLAS) MathUtil.lu_solve(circuitMatrixBlas, circuitMatrixSize, circuitPermute, circuitRightSide);
                else if (!nicsluSolver.nicslu_solve(circuitRightSide)){
                    stop(Constants.SINGULAR_MSG, null);
                    return;
                }

//                if(!MathUtil.klu_backslash(circuitCCSM,circuitRightSide,Common,Symbolic)){
//                    stop(Constants.SINGULAR_MSG, null);
//                    return;
//                }

                for (j = 0; j != circuitMatrixFullSize; j++) {
                    RowInfo ri = circuitRowInfo[j];
                    double res;
                    if (ri.getType() == RowInfo.ROW_CONST)
                        res = ri.getValue();
                    else
                        res = circuitRightSide[ri.getMapCol()];
                    // System.out.println(j + " " + res + " " + ri.type + " " + ri.mapCol);
                    if (Double.isNaN(res)) {
                        converged = false;
                        // debugprint = true;
                        break;
                    }
                    if (j < nodeList.size() - 1) {
                        CircuitNode cn = getCircuitNode(j + 1);
                        for (k = 0; k != cn.getLinks().size(); k++) {
                            CircuitNodeLink cnl = cn.getLinks().elementAt(k);
                            cnl.getElm().setNodeVoltage(cnl.getNum(), res);
                        }

                    } else {
                        int ji = j - (nodeList.size() - 1);
                        // System.out.println("setting vsrc " + ji + " to " + res);
                        voltageSources[ji].setCurrent(ji, res);
                    }
                }
                if (!circuitNonLinear)
                    break;
            }
            if (subiter > 5) {
                // System.out.println("converged after " + subiter + " iterations");
                if (subiter == subiterCount) {
                    stop("Convergence failed!", null);
                    break;
                }
            }
            t += timeStep;
            listener.updateScopes();
            tm = System.currentTimeMillis();
            lit = tm;

            if (fixedIterations != -1) {
                if (iter > fixedIterations)
                    break;
            } else {
                if (iter * 1000 >= steprate * (tm - lastIterTime) || (tm - listener.getLastFrameTime() > 500))
                    break;
            }
        }
        lastIterTime = lit;
        // System.out.println((System.currentTimeMillis()-lastFrameTime)/(double) iter);
    }

    //    public boolean isCircuitMatrixNull() {
//        return circuitMatrix == null;
//    }
    public void time() {
        if (currentTime==-1) currentTime = System.currentTimeMillis();
        else {
            System.out.println("[" + (double)(System.currentTimeMillis()-currentTime)/1000d+" s]");
            currentTime = -1;
        }
    }

    public boolean isCircuitMatrixNull() {
        return (useBLAS && circuitMatrixBlas==null)/* || circuitSparseMatrix == null*/;
    }

    public CircuitElm getDraggedElement() {
        return listener.getDraggedElement();
    }

    public boolean isUISupported() {
        return listener.isUISupported();
    }

    public void createUI(Component comp) {
        listener.createUI(comp);
    }

    public void removeUI(Component comp) {
        listener.removeUI(comp);
    }

    public void setAnalyzeFlag() {
        listener.setAnalyzeFlag();
    }

    public void stop(String cause, CircuitElm ce) throws Exception {
//        circuitMatrix = null;
//        circuitSparseMatrix = null;
        circuitMatrixBlas = null;
        stopMessage = cause;
        stopElm = ce;
        listener.stop(cause, ce);
    }

    public CircuitNode getCircuitNode(int n) {
        if (n >= nodeList.size())
            return null;
        return nodeList.elementAt(n);
    }

    public Vector<CircuitElm> getElmList() {
        return elmList;
    }

    public CircuitElm getElm(int n) {
        if (n >= elmList.size())
            return null;
        return elmList.elementAt(n);
    }

    public CircuitElm getElmById(int elmId) {
        for (CircuitElm elm : elmList) {
            if (elm.flags2 == elmId) return elm;
        }
        return null;
    }

    public String dumpAsGraphString(int flags, int currentBarValue, int powerBarValue) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            return mapper.writeValueAsString(dumpAsGraph(flags, currentBarValue, powerBarValue));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public HashMap<Integer, List<Object>> dumpAsGraph(int cirSimFlags, int currentBarValue, int powerBarValue) {
        HashMap<Integer, List<Object>> obj = new HashMap<>();
        HashMap<GridPoint, Integer> gridPoints = new HashMap<>();
        ReverseNodeDefProvider reverseNodeDefProvider = new ReverseNodeDefProvider();
        List<Object> configList = new ArrayList<>();
        configList.add("$");
        configList.add(cirSimFlags);
        configList.add(timeStep);
        configList.add(iterationCount);
        configList.add(currentBarValue);
        configList.add(voltageRange);
        configList.add(powerBarValue);
        obj.put(obj.size(), configList);

        for (int i = 0; i != getElmList().size(); i++) {
            CircuitElm elm = getElm(i);
            String[] parts = getElm(i).dump(true).split(" ", 8);

            String type = parts[0];
            int x1 = Integer.valueOf(parts[1]);
            int y1 = Integer.valueOf(parts[2]);
            int x2 = Integer.valueOf(parts[3]);
            int y2 = Integer.valueOf(parts[4]);
            int flags = Integer.valueOf(parts[5]);
            int elementId = Integer.valueOf(parts[6]);
            String extraProps = parts.length > 7 ? parts[7] : "";

            if (elm.getPostCount() <= 2) {
                gridPoints.putIfAbsent(new GridPoint(x1, y1), gridPoints.size());
                gridPoints.putIfAbsent(new GridPoint(x2, y2), gridPoints.size());
            } else {
                for (int ii = 0; ii < elm.getPostCount(); ii++) {
                    Point p = elm.getPost(ii);
                    gridPoints.putIfAbsent(new GridPoint(p.x, p.y), gridPoints.size());
                }
            }

            List<Object> graphList = new ArrayList<>();
            graphList.add(type);
            if (elm.getPostCount() <= 2) {
                graphList.add(gridPoints.get(new GridPoint(x1, y1)));
                graphList.add(gridPoints.get(new GridPoint(x2, y2)));
            } else {
                graphList.add(reverseNodeDefProvider.getGraphConnections(gridPoints, String.valueOf(type), elm));
            }
            graphList.add(flags);
            graphList.add(elementId);
            if (extraProps.isEmpty() == false) {
                graphList.addAll(Arrays.asList(extraProps.split(" ")));
            }
            obj.put(obj.size(), graphList);
        }

        return obj;
    }

    public StringBuilder dumpElements(StringBuilder target, boolean newFormat) {
        for (int i = 0; i != getElmList().size(); i++) {
            target.append(getElm(i).dump(newFormat)).append("\n");
        }
        return target;
    }

    public void setDumpMatrix(boolean dumpMatrix) {
        this.dumpMatrix = dumpMatrix;
    }

    public void updateIterationCount(int speedBarValue) {
        if (speedBarValue == 0)
            iterationCount = 0;
        iterationCount = .1 * Math.exp((speedBarValue - 61) / 24.);
    }

    public double getIterationCount() {
        return iterationCount;
    }

    public String getStopMessage() {
        return stopMessage;
    }

    public CircuitElm getStopElement() {
        return stopElm;
    }

    public void updateGrid(boolean smallGrid) {
        gridSize = (smallGrid) ? 1 : 8;
        gridMask = ~(gridSize - 1);
        gridRound = gridSize / 2 - 1;
    }

    public int getGridSize() {
        return gridSize;
    }

    public int snapGrid(int pos) {
        return (pos + gridRound) & gridMask;
    }

    public void setFixedIterations(int iterations) {
        this.fixedIterations = iterations;
    }

    private class FindPathInfo {
        static final int INDUCT = 1;
        static final int VOLTAGE = 2;
        static final int SHORT = 3;
        static final int CAP_V = 4;
        final boolean[] used;
        final int dest;
        final CircuitElm firstElm;
        final int type;

        FindPathInfo(int t, CircuitElm e) {
            dest = e.getNode(1);
            type = t;
            firstElm = e;
            used = new boolean[nodeList.size()];
        }

        private boolean checkElementNode(CircuitElm ce, int node, int n1, int depth) {
            if (findPath(node, depth)) {
//            used[n1] = false;
                return true;
            }

            return false;
        }

        boolean findPath(int n1) {
            return findPath(n1, -1);
        }

        boolean findPath(int n1, int depth) {
            if (n1 == dest) {
                return true;
            }
            if (depth-- == 0 || used[n1]) {
                return false;
            }

            used[n1] = true;
//            for (int i = 0; i != elmList.size(); i++) {

            for (CircuitElm ce : useOptimization?elms.get(n1):elmList) {
                if (ce == firstElm) continue;
                if (type == INDUCT && ce instanceof CurrentElm) continue;
                if (type == VOLTAGE && !(ce.isWire() || ce instanceof VoltageElm)) continue;
                if (type == SHORT && !ce.isWire()) continue;
                if (type == CAP_V && !(ce.isWire() || ce instanceof CapacitorElm || ce instanceof VoltageElm)) continue;
                if (n1 == 0) {
                    // look for posts which have a ground connection;
                    // our path can go through ground
                    for (int j = 0; j != ce.getPostCount(); j++) {
                        if (ce.hasGroundConnection(j) && checkElementNode(ce, ce.getNode(j), n1, depth)) return true;
                    }
                }

                int j;
                for (j = 0; j != ce.getPostCount(); j++) {
                    // System.out.println(ce + " " + ce.getNode(j));
                    if (ce.getNode(j) == n1) break;
                }

                if (j == ce.getPostCount()) continue;
                if (ce.hasGroundConnection(j) && checkElementNode(ce, 0, n1, depth)) return true;

                if (type == INDUCT && ce instanceof InductorElm) {
                    double c = ce.getCurrent();
                    if (j == 0) c = -c;
                    // System.out.println("matching " + c + " to " +
                    // firstElm.getCurrent());
                    // System.out.println(ce + " " + firstElm);
                    if (Math.abs(c - firstElm.getCurrent()) > 1e-10) continue;
                }

                for (int k = 0; k != ce.getPostCount(); k++) {
                    if (j == k) continue;
                    // System.out.println(ce + " " + ce.getNode(j) + "-" +
                    // ce.getNode(k));
                    int node = ce.getNode(k);
                    if (ce.getConnection(j, k) && checkElementNode(ce, ce.getNode(k), n1, depth)) return true;
                    // System.out.println("back on findpath " + n1);
                }
            }
//            used[n1] = false;
            // System.out.println(n1 + " failed");
            return false;
        }
    }

    public void kill_nicslu(){
        if (!useBLAS) {
            nicsluSolver.nicslu_destroy();
            System.gc();
        }
    }

}
