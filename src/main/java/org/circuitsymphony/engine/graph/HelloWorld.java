package org.circuitsymphony.engine.graph;

import javax.swing.JFrame;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource;
import com.mxgraph.view.mxGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelloWorld
{

    /**
     *
     */
    private static final long serialVersionUID = -2707712944901661771L;

    public static Map<EdgeElm,mxCell> LayoutEdges(List<EdgeElm> edges){
        Grid grid = new Grid();
        Map<EdgeElm,mxCell> result = new HashMap<>();
        mxGraph graph = new mxGraph();
        Object parent = graph.getDefaultParent();
        graph.getModel().beginUpdate();

        for(EdgeElm edgeElm : edges) {
            GridPoint p1 = grid.getNodePoint(edgeElm.g1);
            GridPoint p2 = grid.getNodePoint(edgeElm.g2);

            Object v1 = graph.insertVertex(parent, null, null, p1.x * 10., p1.y * 10., 0, 0);
            Object v2 = graph.insertVertex(parent, null, null, p2.x * 10., p2.y * 10., 0, 0);
            mxCell edge = (mxCell) graph.insertEdge(parent, null, null, v1, v2);

            result.put(edgeElm, edge);
        }
        graph.getModel().endUpdate();
        result.forEach((edgeElm,altEdge) ->{
                System.out.println(altEdge.getSource().getGeometry().toString()+altEdge.getTarget().getGeometry().toString());
        });

        JFrame frame = new JFrame();
        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        frame.getContentPane().add(graphComponent);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setVisible(true);
        mxFastOrganicLayout layout = new mxFastOrganicLayout(graph);
//        layout.setDisableEdgeStyle(false);
//        layout.setMaxIterations(1);
////        layout.setMaxDistanceLimit(50);
//        layout.setForceConstant(0.3); // the higher, the more separated
//        layout.execute(parent);

//        mxIGraphLayout layout = new mxHierarchicalLayout(graph);
// layout graph
//        layout.execute(graph.getDefaultParent());
        graph.getModel().beginUpdate();
        try {
            layout.execute(graph.getDefaultParent());
        }finally{
            mxMorphing morph = new mxMorphing(graphComponent, 20, 1.2, 20);

            morph.addListener(mxEvent.DONE, new mxEventSource.mxIEventListener() {

                @Override
                public void invoke(Object arg0, mxEventObject arg1) {
                    graph.getModel().endUpdate();
                    // fitViewport();
                }

            });

            morph.startAnimation();
        }

        result.forEach((edgeElm,altEdge) ->{
            System.out.println(altEdge.getSource().getGeometry().toString()+altEdge.getTarget().getGeometry().toString());
        });


        return result;
    }

    public HelloWorld()
    {
//        super("Hello, World!");

        mxGraph graph = new mxGraph();
        Object parent = graph.getDefaultParent();

        List<mxCell> edges = new ArrayList<>();

        graph.getModel().beginUpdate();
//            Object v1 = graph.insertVertex(parent, null, "Hello", 0, 0, 0,
//                    0);
//            Object v2 = graph.insertVertex(parent, null, "World!", 200, 0,
//                    0, 0);
//            Object v3 = graph.insertVertex(parent, null, "Hello", 200, 200, 0,
//                    0);
//            Object v4 = graph.insertVertex(parent, null, "World!", 0, 200,
//                    0, 0);
            Object v1 = graph.insertVertex(parent, null, null, 300, 300, 0,
                    0);
            Object v2 = graph.insertVertex(parent, null, null, 300., 0,
                    0, 0);
            Object v3 = graph.insertVertex(parent, null, null, 0, 300, 0,
                    0);
            Object v4 = graph.insertVertex(parent, null, null, 0, 0,
                    0, 0);
            mxCell e1 = (mxCell) graph.insertEdge(parent, null, null, v1, v2);
            mxCell e2 = (mxCell) graph.insertEdge(parent, null, null, v3, v1);
            mxCell e3 = (mxCell) graph.insertEdge(parent, null, null, v4, v2);
            mxCell e4 = (mxCell) graph.insertEdge(parent, null, null, v4, v3);
            edges.add(e1);
            edges.add(e2);
            edges.add(e3);
            edges.add(e4);

            graph.getModel().endUpdate();

        for(mxCell e:edges){
            System.out.println(e.getSource().getGeometry().toString()+e.getTarget().getGeometry().toString());
        }

//        mxFastOrganicLayout layout = new mxFastOrganicLayout(graph);

//        // set some properties
//        layout.setForceConstant( 40); // the higher, the more separated
//        layout.setDisableEdgeStyle( false); // true transforms the edges and makes them direct lines
//        // layout graph
//        layout.execute(graph.getDefaultParent());
//        mxIGraphLayout layout = new mxHierarchicalLayout(graph);
// layout graph
//        layout.execute(graph.getDefaultParent());


        for(mxCell e:edges){
            System.out.println(e.getSource().getGeometry().toString()+e.getTarget().getGeometry().toString());
        }
//        mxGraphComponent graphComponent = new mxGraphComponent(graph);
//        getContentPane().add(graphComponent);
        mxFastOrganicLayout layout = new mxFastOrganicLayout(graph);
        JFrame frame = new JFrame();
        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        frame.getContentPane().add(graphComponent);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setVisible(true);


        graph.getModel().beginUpdate();
        try {
            layout.execute(graph.getDefaultParent());
        }finally{
            mxMorphing morph = new mxMorphing(graphComponent, 20, 1.2, 20);

            morph.addListener(mxEvent.DONE, new mxEventSource.mxIEventListener() {

                @Override
                public void invoke(Object arg0, mxEventObject arg1) {
                    graph.getModel().endUpdate();
                    // fitViewport();
                }

            });

            morph.startAnimation();
        }

    }

    public static void main(String[] args)
    {
        HelloWorld frame = new HelloWorld();
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(400, 320);
//        frame.setVisible(true);
    }

}
