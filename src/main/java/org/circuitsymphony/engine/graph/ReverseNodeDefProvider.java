package org.circuitsymphony.engine.graph;

import org.circuitsymphony.element.CircuitElm;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides standard list of {@link ReverseNodeDef}, allows to create connection list for multi terminal components
 * required by JSON graph
 */
public class ReverseNodeDefProvider {
    private HashMap<String, ReverseNodeDef> nodesDef = new HashMap<>();

    public ReverseNodeDefProvider() {
        addReverseNodeDef("t") //Transistor
                .addConnection("base", 0)
                .addConnection("coll", 1)
                .addConnection("emit", 2);
        addReverseNodeDef("f") //Mosfet
                .addConnection("gate", 0)
                .addConnection("source", 1)
                .addConnection("drain", 2);
        addReverseNodeDef("j") //Jfet
                .addConnection("gate", 0)
                .addConnection("source", 1)
                .addConnection("drain", 2);
        addReverseNodeDef("a") //OpAmp
                .addConnection("invIn", 0)
                .addConnection("nonInvIn", 1)
                .addConnection("out", 2);
    }

    private ReverseNodeDef addReverseNodeDef(String type) {
        ReverseNodeDef def = new ReverseNodeDef(type);
        nodesDef.put(type, def);
        return def;
    }

    public Map<String, Integer> getGraphConnections(HashMap<GridPoint, Integer> gridPoints, String dumpType, CircuitElm elm) {
        Map<String, Integer> connections = new HashMap<>();
        ReverseNodeDef def = nodesDef.get(dumpType);
        if (def == null) throw new IllegalStateException("Missing ReverseNodeDef for type: " + dumpType);
        for (int i = 0; i < elm.getPostCount(); i++) {
            Point p = elm.getPost(i);
            connections.put(def.connections.get(i), gridPoints.get(new GridPoint(p.x, p.y)));
        }
        return connections;
    }
}

/**
 * Describes all terminals used by {@link CircuitElm}
 */
class ReverseNodeDef {
    final String type;
    final HashMap<Integer, String> connections = new HashMap<>();

    public ReverseNodeDef(String type) {
        this.type = type;
    }

    public ReverseNodeDef addConnection(String name, int elmPotIndex) {
        connections.put(elmPotIndex, name);
        return this;
    }
}
