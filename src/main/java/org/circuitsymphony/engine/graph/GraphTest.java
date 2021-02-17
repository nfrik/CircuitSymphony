package org.circuitsymphony.engine.graph;

import guru.nidi.graphviz.engine.Engine;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;

import java.io.File;
import java.io.IOException;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;

public class GraphTest {
    public static void main(String[] args){
        Graph g = graph("example5").with(node("abc").link(node("xyz")));
        Graphviz viz = Graphviz.fromGraph(g);
//        viz.width(200).render(Format.SVG).toFile(new File("example/ex5.svg"));
//        viz.width(200).rasterize(Rasterizer.BATIK).toFile(new File("example/ex5b.png"));
//        viz.width(200).rasterize(Rasterizer.SALAMANDER).toFile(new File("example/ex5s.png"));
        String json = viz.engine(Engine.NEATO).render(Format.JSON).toString();
        System.out.println(json);
//        Graph g = graph("example1").directed().with(node("a").link(node("b")));
//        try {
//            Graphviz.fromGraph(g).width(200).render(Format.PNG).toFile(new File("example/ex1.png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
