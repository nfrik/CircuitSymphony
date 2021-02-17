package org.circuitsymphony.engine.graph;

import org.circuitsymphony.element.active.OpAmpElm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Provides standard list of {@link NodeDef} describing what outputs are being provided by multi terminal {@link EdgeElm}
 * elements.
 */
public class NodeDefProvider {
    private ArrayList<NodeDef> nodesDef = new ArrayList<>();

    public NodeDefProvider() {
        addNodeDef("t", 16, 16, 32, elm -> elm.extraProps.startsWith("1")) // NTransistor
                .addConnection("base", 0, 0)
                .addConnection("coll", 32, -16)
                .addConnection("emit", 32, 16);
        addNodeDef("t", 16, 16, 32, elm -> elm.extraProps.startsWith("-1")) // PTransistor
                .addConnection("base", 0, 0)
                .addConnection("emit", 32, -16)
                .addConnection("coll", 32, 16);
        addNodeDef("f", 16, 16, 32, elm -> elm.flags == 0) // NMosfet
                .addConnection("gate", 0, 0)
                .addConnection("drain", 32, -16)
                .addConnection("source", 32, 16);
        addNodeDef("f", 16, 16, 32, elm -> elm.flags == 1) // PMosfet
                .addConnection("gate", 0, 0)
                .addConnection("source", 32, -16)
                .addConnection("drain", 32, 16);
        addNodeDef("j", 16, 16, 32, elm -> elm.flags == 0) // NJfet
                .addConnection("gate", 0, 0)
                .addConnection("drain", 32, -16)
                .addConnection("source", 32, 16);
        addNodeDef("j", 16, 16, 32, elm -> elm.flags == 1) // NJfet
                .addConnection("gate", 0, 0)
                .addConnection("source", 32, -16)
                .addConnection("drain", 32, 16);
        addNodeDef("a", 16, 16, 32, elm -> (elm.flags & OpAmpElm.FLAG_SWAP) == 0) // OpAmp (minus on top)
                .addConnection("invIn", 0, -8)
                .addConnection("nonInvIn", 0, 8)
                .addConnection("out", 32, 0);
        addNodeDef("a", 16, 16, 32, elm -> (elm.flags & OpAmpElm.FLAG_SWAP) != 0) // OpAmp (plus on top)
                .addConnection("nonInvIn", 0, -8)
                .addConnection("invIn", 0, 8)
                .addConnection("out", 32, 0);
    }

    private NodeDef addNodeDef(String type, int offsetX, int offsetY, int width, NodeDefRule rule) {
        NodeDef def = new NodeDef(type, offsetX, offsetY, width, rule);
        nodesDef.add(def);
        return def;
    }

    public NodeDef getNodeDef(NodeElm elm) {
        for (NodeDef def : nodesDef) {
            if (def.type.equals(elm.type) && def.rule.matches(elm)) {
                return def;
            }
        }
        return null;
    }
}

/**
 * Provides additional matching rule for single {@link NodeDef}.
 */
interface NodeDefRule {
    boolean matches(NodeElm elm);
}

/**
 * Contains offsets and width of {@link EdgeElm}, provides list of all terminals exposed by this element.
 */
class NodeDef {
    final String type;
    final int offsetX;
    final int offsetY;
    final int width;
    final NodeDefRule rule;

    final HashMap<String, NodeConnectionDef> connections = new HashMap<>();

    public NodeDef(String type, int offsetX, int offsetY, int width, NodeDefRule rule) {
        this.type = type;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.width = width;
        this.rule = rule;
    }

    public NodeDef addConnection(String name, int originOffsetX, int originOffsetY) {
        connections.put(name, new NodeConnectionDef(name, originOffsetX, originOffsetY));
        return this;
    }
}

/**
 * Describes single connection terminal of {@link EdgeElm}.
 */
class NodeConnectionDef {
    final String name;
    final int originOffsetX;
    final int originOffsetY;

    public NodeConnectionDef(String name, int originOffsetX, int originOffsetY) {
        this.name = name;
        this.originOffsetX = originOffsetX;
        this.originOffsetY = originOffsetY;
    }
}
