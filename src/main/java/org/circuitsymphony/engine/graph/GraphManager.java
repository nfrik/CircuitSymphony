package org.circuitsymphony.engine.graph;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
//import org.circuitsymphony.util.mxFastOrganicLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.view.mxGraph;
import io.swagger.models.auth.In;
import org.apache.commons.io.output.TeeOutputStream;
import org.circuitsymphony.util.mxFastOrganicLayoutWrap;
import org.jenetics.internal.util.Hash;
import javax.swing.JFrame;

import com.mxgraph.swing.mxGraphComponent;
import org.jgrapht.ListenableGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.ext.MatrixExporter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultListenableGraph;


import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Allow to dynamically manage graph of circuit elements encoded in JSON string or file. Provides
 * conversion layer to CMF format so standard loading method can be used.
 */
public class GraphManager {
    private static final String LINE_END = "\n"; //change to "\r\n" if needed

    private NodeDefProvider nodesDef = new NodeDefProvider();
    private GraphJsonParser parser = new GraphJsonParser();

    private Grid grid = new Grid();
    private String configString;
    private ArrayList<EdgeElm> edges = new ArrayList<>();
    private ArrayList<NodeElm> nodes = new ArrayList<>();

    public String append(File json) throws IOException {
        return append(new String(Files.readAllBytes(json.toPath()), "UTF-8"));
    }

    public String append(String json) {
        StringBuilder cmfOut = new StringBuilder();
        GraphJsonParserResult result = parser.parseJson(json);
        if (result.configString != null) {
            configString = result.configString;
            cmfOut.append(result.configString);
            cmfOut.append(LINE_END);
        }
        nodes.addAll(result.nodes);
        edges.addAll(result.edges);
        layoutElements(cmfOut, result.nodes, result.edges);
        return cmfOut.toString();
    }

    public void delete(int elementId) {
        if (elementId == 0) return;
        edges.removeIf(edge -> edge.elementId == elementId);
        nodes.removeIf(node -> node.elementId == elementId);
    }

    public String getCmf() {
        StringBuilder cmfOut = new StringBuilder();
        if (configString != null) {
            cmfOut.append(configString);
            cmfOut.append(LINE_END);
        }
        layoutElements(cmfOut, nodes, edges);
        return cmfOut.toString();
    }

    public HashMap<Integer, List<Object>> getJsonGraph() {
        HashMap<Integer, List<Object>> obj = new HashMap<>();
        int count = 0;
        if (configString != null) {
            obj.put(count++, Arrays.asList(configString.split(" ")));
        }
        for (EdgeElm edgeElm : edges) {
            obj.put(count++, edgeElm.toObjectList());
        }
        for (NodeElm nodeElm : nodes) {
            obj.put(count++, nodeElm.toObjectList());
        }
        return obj;
    }

    class AltEdge{
        private mxCell v1;
        private mxCell v2;

        public AltEdge(mxCell o1,mxCell o2){
            this.v1=o1;
            this.v2=o2;
        }

        public mxCell getV1() {
            return v1;
        }

        public void setV1(mxCell v1) {
            this.v1 = v1;
        }

        public mxCell getV2() {
            return v2;
        }

        public void setV2(mxCell v2) {
            this.v2 = v2;
        }

    }

    private void layoutElements(StringBuilder cmfOut, ArrayList<NodeElm> nodes, ArrayList<EdgeElm> edges) {

        edges.forEach(edgeElm -> {
            GridPoint p1 = grid.getNodePoint(edgeElm.g1);
            GridPoint p2 = grid.getNodePoint(edgeElm.g2);
            cmfOut.append(Stream.of(edgeElm.type, p1.x, p1.y, p2.x, p2.y, edgeElm.flags, edgeElm.elementId, edgeElm.extraProps)
                    .map(Object::toString)
                    .filter(s -> s.isEmpty() == false)
                    .collect(Collectors.joining(" ")));
            cmfOut.append(LINE_END);
        });
//        Map<EdgeElm,mxCell> edgeMap = new HashMap<>();
//        ListenableGraph<String, DefaultEdge> g = new DefaultListenableGraph<>(new DefaultDirectedGraph<>(DefaultEdge.class));
//        JGraphXAdapter<String, DefaultEdge> jgxAdapter = new JGraphXAdapter<>(g);
//
//        jgxAdapter.getModel().beginUpdate();
//        try {
//            edges.forEach(edgeElm -> {
//
//                GridPoint p1 = grid.getNodePoint(edgeElm.g1);
//                GridPoint p2 = grid.getNodePoint(edgeElm.g2);
//
//                String v1 = String.valueOf(p1.hashCode());
//                String v2 = String.valueOf(p2.hashCode());
//                g.addVertex(v1);
//                g.addVertex(v2);
//                DefaultEdge edge = g.addEdge(v1,v2);
//                Object [] mxedges = jgxAdapter.getChildEdges(jgxAdapter.getDefaultParent());
//                edgeMap.put(edgeElm, (mxCell) mxedges[mxedges.length-1]);
//
//            });
//        }
//        finally {
//            jgxAdapter.getModel().endUpdate();
//        }
//       // System.out.println(jgxAdapter);
////
//  //      edgeMap.forEach((edgeElm,altEdge) ->{
//    //            System.out.println(altEdge.getSource().getGeometry().toString()+altEdge.getTarget().getGeometry().toString());
//      //  });
//
//        PrintStream out = new PrintStream(System.out);
//
//        mxFastOrganicLayout layout = new mxFastOrganicLayoutWrap(jgxAdapter);
////        mxCircleLayout layout = new mxCircleLayout(jgxAdapter);
////        layout.setDisableEdgeStyle(false);
//        layout.setMaxIterations(100);
////        layout.setMinDistanceLimit(1500);
//        layout.setForceConstant(1000); // the higher, the more separated
//        layout.execute(jgxAdapter.getDefaultParent());
//
//
//        //edgeMap.forEach((edgeElm,altEdge) ->{
//        //    System.out.println(altEdge.getSource().getGeometry().toString()+altEdge.getTarget().getGeometry().toString());
//        //});
//
//        edgeMap.forEach((edgeElm,altEdge) -> {
//
//
//            GridPoint p1 = new GridPoint(Double.valueOf(altEdge.getSource().getGeometry().getX()).intValue(),Double.valueOf(altEdge.getSource().getGeometry().getY()).intValue());
//            GridPoint p2 = new GridPoint(Double.valueOf(altEdge.getTarget().getGeometry().getX()).intValue(),Double.valueOf(altEdge.getTarget().getGeometry().getY()).intValue());
//
//            cmfOut.append(Stream.of(edgeElm.type, p1.x, p1.y, p2.x, p2.y, edgeElm.flags, edgeElm.elementId, edgeElm.extraProps)
//                    .map(Object::toString)
//                    .filter(s -> s.isEmpty() == false)
//                    .collect(Collectors.joining(" ")));
//            cmfOut.append(LINE_END);
//        });



        nodes.forEach(nodeElm -> {
            NodeDef def = nodesDef.getNodeDef(nodeElm);
            if (def == null) {
                throw new IllegalStateException("Unsupported multi terminal component type: " + nodeElm.type);
            }
            GridPoint p = grid.getElementPoint();
            int originX = p.x + def.offsetX;
            int originY = p.y + def.offsetY;
            cmfOut.append(Stream.of(nodeElm.type, originX, originY, originX + def.width, originY, nodeElm.flags, nodeElm.elementId, nodeElm.extraProps)
                    .map(Object::toString)
                    .filter(s -> s.isEmpty() == false)
                    .collect(Collectors.joining(" ")));
            cmfOut.append(LINE_END);
            nodeElm.connections.forEach((src, target) -> {
            GridPoint tp = grid.getNodePoint(target);

            NodeConnectionDef connDef = def.connections.get(src);
            if (connDef == null) {
                throw new IllegalStateException("Unsupported connection src: " + src + ", for element type: " + nodeElm.type);
            }
            int sx = originX + connDef.originOffsetX;
            int sy = originY + connDef.originOffsetY;
            cmfOut.append(Stream.of("w", sx, sy, tp.x, tp.y, "0", "0")
                    .map(Object::toString)
                    .collect(Collectors.joining(" ")));
            cmfOut.append(LINE_END);
            });
        });
    }

    public void clear() {
        grid.clear();
        configString = null;
        edges.clear();
        nodes.clear();
    }
}

/**
 * Controls grid of graph elements.
 */
class Grid {
    private static final int GRID_SIZE = 30;
    private static final int GRID_COLUMNS = 100;

    private HashMap<Integer, GridPoint> nodeGridPoints = new HashMap<>();
    private ArrayList<GridPoint> elementGridPoints = new ArrayList<>();

    public GridPoint getNodePoint(int g) {
        GridPoint p = nodeGridPoints.get(g);
        if (p == null) {
            p = allocateNextPoint();
            nodeGridPoints.put(g, p);
        }
        return p;
    }

    public GridPoint getElementPoint() {
        GridPoint p = allocateNextPoint();
        elementGridPoints.add(p);
        return p;
    }

    private GridPoint allocateNextPoint() {
        int line = count() / GRID_COLUMNS;
        int column = count() - line * GRID_COLUMNS;
        return new GridPoint(column * GRID_SIZE, line * GRID_SIZE);
    }

    public void clear() {
        nodeGridPoints.clear();
        elementGridPoints.clear();
    }

    private int count() {
        return nodeGridPoints.size() + elementGridPoints.size();
    }
}

/**
 * Represents multi terminal element (transistor etc.).
 */
class EdgeElm {
    final String type;
    final int g1;
    final int g2;
    final int flags;
    final int elementId;
    final String extraProps;

    public EdgeElm(String type, int g1, int g2, int flags, int elementId, String extraProps) {
        this.type = type;
        this.g1 = g1;
        this.g2 = g2;
        this.flags = flags;
        this.elementId = elementId;
        this.extraProps = extraProps;
    }

    public ArrayList<Object> toObjectList() {
        ArrayList<Object> obj = new ArrayList<>();
        Collections.addAll(obj, type, g1, g2, flags, elementId);
        if (extraProps.isEmpty() == false) {
            obj.addAll(Arrays.asList(extraProps.split(" ")));
        }
        return obj;
    }
}

/**
 * Represents two terminal element (resistor etc.).
 */
class NodeElm {
    final String type;
    final Map<String, Integer> connections;
    final int flags;
    final int elementId;
    final String extraProps;

    NodeElm(String type, Map<String, Integer> connections, int flags, int elementId, String extraProps) {
        this.type = type;
        this.connections = connections;
        this.flags = flags;
        this.elementId = elementId;
        this.extraProps = extraProps;
    }

    public ArrayList<Object> toObjectList() {
        ArrayList<Object> obj = new ArrayList<>();
        Collections.addAll(obj, type, connections, flags, elementId);
        if (extraProps.isEmpty() == false) {
            obj.addAll(Arrays.asList(extraProps.split(" ")));
        }
        return obj;
    }
}
