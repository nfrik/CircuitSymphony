package org.circuitsymphony.engine.graph;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Parsers provided graph circuit in json format
 */
public class GraphJsonParser {
    @SuppressWarnings("unchecked")
    public GraphJsonParserResult parseJson(String json) {
        GraphJsonParserResult result = new GraphJsonParserResult();
        try {
            ObjectMapper mapper = new ObjectMapper();
            HashMap<String, ArrayList<Object>> circuit = mapper.readValue(json, HashMap.class);
            circuit.forEach((key, elm) -> {
                if (key.equals("0") && elm.size() > 0 && elm.get(0).equals("$")) {
                    result.configString = elm.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(" "));
                    return;
                }

                String type = (String) elm.get(0);
                if (elm.get(1) instanceof Integer) {
                    int g1 = (int) elm.get(1);
                    int g2 = (int) elm.get(2);
                    int flags = (int) elm.get(3);
                    int elementId = (int) elm.get(4);
                    String extraProps = getJsonElmExtraProps(elm, 5);
                    result.edges.add(new EdgeElm(type, g1, g2, flags, elementId, extraProps));
                } else {
                    Map<String, Integer> connections = (Map<String, Integer>) elm.get(1);
                    int flags = (int) elm.get(2);
                    int elementId = (int) elm.get(3);
                    String extraProps = getJsonElmExtraProps(elm, 4);
                    result.nodes.add(new NodeElm(type, connections, flags, elementId, extraProps));
                }
            });
            return result;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String getJsonElmExtraProps(ArrayList<Object> elm, int startIdx) {
        String extraProps = "";
        if (elm.size() > startIdx) {
            extraProps = elm.stream()
                    .skip(startIdx)
                    .map(Object::toString)
                    .collect(Collectors.joining(" "));
        }
        return extraProps;
    }
}

/**
 * Contains result of {@link GraphJsonParser}. Contains parsed config string, edge list (2 terminal components) and
 * node list (multi terminal components).
 */
class GraphJsonParserResult {
    public String configString;
    public final ArrayList<EdgeElm> edges = new ArrayList<>();
    public final ArrayList<NodeElm> nodes = new ArrayList<>();
}
