package org.circuitsymphony.engine;

import org.apache.commons.io.FilenameUtils;
import org.circuitsymphony.element.CapacitorElm;
import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.element.WireElm;
import org.circuitsymphony.element.active.*;
import org.circuitsymphony.element.cdseries.*;
import org.circuitsymphony.element.chips.*;
import org.circuitsymphony.element.devices.*;
import org.circuitsymphony.element.io.*;
import org.circuitsymphony.element.logicgates.*;
import org.circuitsymphony.element.passive.*;
import org.circuitsymphony.engine.graph.GraphManager;
import org.circuitsymphony.util.CirSimUtil;
import org.circuitsymphony.util.Scope;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.*;

/**
 * Used to load circuit file into {@link CircuitEngine}.
 *
 */
public class CircuitLoader {
    private final CircuitEngine engine;
    private final CircuitLoaderListener listener;

    private Class<?>[] dumpTypes;

    private GraphManager graphManager = new GraphManager();

    private List<MappedElement> elements = new ArrayList<>();

    public CircuitLoader(CircuitEngine engine, CircuitLoaderListener listener) {
        this.engine = engine;
        this.listener = listener;

        dumpTypes = new Class[300];
        // these characters are reserved
        dumpTypes['o'] = Scope.class;
        dumpTypes['h'] = Scope.class;
        dumpTypes['$'] = Scope.class;
        dumpTypes['%'] = Scope.class;
        dumpTypes['?'] = Scope.class;
        dumpTypes['B'] = Scope.class;

        createElementMappings();

        for (int i = 0; i < elements.size(); i++) {

            //for (MappedElement element : elements) {
            MappedElement element = elements.get(i);
            CircuitElm elm = CirSimUtil.constructElement(engine, element.clazz, 0, 0);
            try {
                register(element.clazz, elm);
            } catch (NullPointerException e) {
                e.printStackTrace();
                System.out.println("NullPointer " + i + "  " + element.toString());
            }
            if (elm != null) {
                elm.delete();
            }
        }
    }

    private void createElementMappings() {
        addMapping(MappedElementCategory.MainMenu, "Add Wire", WireElm.class); // (Dump Data) w
        addMapping(MappedElementCategory.MainMenu, "Add Resistor", ResistorElm.class); // r
        addMapping(MappedElementCategory.MainMenu, "Add Capacitor", CapacitorElm.class); // c
        addMapping(MappedElementCategory.MainMenu, "Add Ground", GroundElm.class); // g
        addMapping(MappedElementCategory.MainMenu, "Add Memristor", MemristorElm.class); // m

        addMapping(MappedElementCategory.Passive, "Add Inductor", InductorElm.class); // l
        addMapping(MappedElementCategory.Passive, "Add Switch", SwitchElm.class); // s
        addMapping(MappedElementCategory.Passive, "Add Push Switch", PushSwitchElm.class);
        addMapping(MappedElementCategory.Passive, "Add SPDT Switch", Switch2Elm.class); //S
        addMapping(MappedElementCategory.Passive, "Add Potentiometer", PotElm.class); //174
        addMapping(MappedElementCategory.Passive, "Add Transformer", TransformerElm.class); //T
        addMapping(MappedElementCategory.Passive, "Add Tapped  Transformer", TappedTransformerElm.class); //169
        addMapping(MappedElementCategory.Passive, "Add Transmission Line", TransLineElm.class); //171
        addMapping(MappedElementCategory.Passive, "Add Relay", RelayElm.class); //178
        addMapping(MappedElementCategory.Passive, "Add Memristor", MemristorElm.class);
        addMapping(MappedElementCategory.Passive, "Add Window Memristor", WindowMemristorElm.class); //q
        addMapping(MappedElementCategory.Passive, "Add Sillin Memristor", SillinMemristorElm.class); //190
        addMapping(MappedElementCategory.Passive, "Add Biolek Memristor", BiolekMemristorElm.class); //191
        addMapping(MappedElementCategory.Passive, "Add Prodromakis Memristor", ProdromakisMemristorElm.class); //192
        addMapping(MappedElementCategory.Passive, "Add Zha Memristor", ZhaMemristorElm.class); //193
        addMapping(MappedElementCategory.Passive, "Add Singh Memristor", SinghMemristorElm.class); //194
        addMapping(MappedElementCategory.Passive, "Add VTEAM Memristor", VTEAMMemristorElm.class); //Q
        addMapping(MappedElementCategory.Passive, "Add Stochastic Memristor", FStochasticMemristorElm.class); //195
        addMapping(MappedElementCategory.Passive, "Add Spark Gap", SparkGapElm.class);

        addMapping(MappedElementCategory.InputOutput, "Add Ground", GroundElm.class);
        addMapping(MappedElementCategory.InputOutput, "Add Voltage Source (2-terminal)", DCVoltageElm.class);
        addMapping(MappedElementCategory.InputOutput, "Add A/C Source (2-terminal)", ACVoltageElm.class);
        addMapping(MappedElementCategory.InputOutput, "Add Voltage Source (1-terminal)", RailElm.class);
        addMapping(MappedElementCategory.InputOutput, "Add A/C Source (1-terminal)", ACRailElm.class);
        addMapping(MappedElementCategory.InputOutput, "Add Square Wave (1-terminal)", SquareRailElm.class);
        addMapping(MappedElementCategory.InputOutput, "Add Analog Output", OutputElm.class);
        addMapping(MappedElementCategory.InputOutput, "Add Logic Input", LogicInputElm.class);
        addMapping(MappedElementCategory.InputOutput, "Add Logic Output", LogicOutputElm.class);
        addMapping(MappedElementCategory.InputOutput, "Add Clock", ClockElm.class);
        addMapping(MappedElementCategory.InputOutput, "Add A/C Sweep", SweepElm.class); // 170
        addMapping(MappedElementCategory.InputOutput, "Add Var. Voltage", VarRailElm.class); // 172
        addMapping(MappedElementCategory.InputOutput, "Add Antenna", AntennaElm.class);
        addMapping(MappedElementCategory.InputOutput, "Add Current Source", CurrentElm.class);

        addMapping(MappedElementCategory.Active, "Add Diode", DiodeElm.class);
        addMapping(MappedElementCategory.Active, "Add Zener Diode", ZenerElm.class);
        addMapping(MappedElementCategory.Active, "Add Transistor (bipolar, NPN)", NTransistorElm.class);
        addMapping(MappedElementCategory.Active, "Add Transistor (bipolar, PNP)", PTransistorElm.class);
        addMapping(MappedElementCategory.Active, "Add Op Amp (- on top)", OpAmpElm.class);
        addMapping(MappedElementCategory.Active, "Add Op Amp (+ on top)", OpAmpSwapElm.class);
        addMapping(MappedElementCategory.Active, "Add MOSFET (n-channel)", NMosfetElm.class);
        addMapping(MappedElementCategory.Active, "Add MOSFET (p-channel)", PMosfetElm.class);
        addMapping(MappedElementCategory.Active, "Add JFET (n-channel)", NJfetElm.class);
        addMapping(MappedElementCategory.Active, "Add JFET (p-channel)", PJfetElm.class);
        addMapping(MappedElementCategory.Active, "Add Analog Switch (SPST)", AnalogSwitchElm.class);
        addMapping(MappedElementCategory.Active, "Add Analog Switch (SPDT)", AnalogSwitch2Elm.class);
        addMapping(MappedElementCategory.Active, "Add SCR", SCRElm.class); // 177
//        addMapping(Active, "Add Varactor/Varicap", VaractorElm.class);
        addMapping(MappedElementCategory.Active, "Add Tunnel Diode", TunnelDiodeElm.class); // 175
        addMapping(MappedElementCategory.Active, "Add Triode", TriodeElm.class); // 173
//        addMapping(Active, "Add Diac", DiacElm.class);
//        addMapping(Active, "Add Triac", TriacElm.class);
//        addMapping(Active, "Add Photoresistor", PhotoResistorElm.class); // Alpha
//        addMapping(Active, "Add Thermistor", ThermistorElm.class);
        addMapping(MappedElementCategory.Active, "Add CCII+", CC2Elm.class); // 179
        addMapping(MappedElementCategory.Active, "Add CCII-", CC2NegElm.class);

        addMapping(MappedElementCategory.LogicGate, "Add Inverter", InverterElm.class);
        addMapping(MappedElementCategory.LogicGate, "Add NAND Gate", NandGateElm.class);
        addMapping(MappedElementCategory.LogicGate, "Add NOR Gate", NorGateElm.class);
        addMapping(MappedElementCategory.LogicGate, "Add AND Gate", AndGateElm.class);
        addMapping(MappedElementCategory.LogicGate, "Add OR Gate", OrGateElm.class);
        addMapping(MappedElementCategory.LogicGate, "Add XOR Gate", XorGateElm.class);
        addMapping(MappedElementCategory.LogicGate, "Add ST Inverter", InverterSTElm.class); // 186

        addMapping(MappedElementCategory.Chip, "Add D Flip-Flop", DFlipFlopElm.class);
        addMapping(MappedElementCategory.Chip, "Add JK Flip-Flop", JKFlipFlopElm.class);
        addMapping(MappedElementCategory.Chip, "Add 7 Segment LED", SevenSegElm.class);
        addMapping(MappedElementCategory.Chip, "Add VCO", VCOElm.class);
        addMapping(MappedElementCategory.Chip, "Add Phase Comparator", PhaseCompElm.class);
        addMapping(MappedElementCategory.Chip, "Add Counter 4-bit", CounterElm.class);
        addMapping(MappedElementCategory.Chip, "Add Decade Counter", DecadeElm.class);
        addMapping(MappedElementCategory.Chip, "Add 555 Timer", TimerElm.class);
        addMapping(MappedElementCategory.Chip, "Add DAC", DACElm.class);
        addMapping(MappedElementCategory.Chip, "Add ADC", ADCElm.class);
        addMapping(MappedElementCategory.Chip, "Add Latch", LatchElm.class); // 168

        addMapping(MappedElementCategory.Display, "Add Text", TextElm.class);
        addMapping(MappedElementCategory.Display, "Add Scope Probe", ProbeElm.class);
        addMapping(MappedElementCategory.Display, "Add LED", LEDElm.class);
        addMapping(MappedElementCategory.Display, "Add Lamp (beta)", LampElm.class);
        addMapping(MappedElementCategory.Display, "Add LED Array", LEDArrayElm.class); // 176
        addMapping(MappedElementCategory.Display, "Add LED-Matrix 5x7", Matrix5x7Elm.class); // 180

        addMapping(MappedElementCategory.CDSeries, "Add Counter 7-bit (4024)", CD4024.class); // 182
        addMapping(MappedElementCategory.CDSeries, "Add BCD-to-Decimal Decoder (4028)", CD4028.class); // 185
        addMapping(MappedElementCategory.CDSeries, "Add Counter 12-bit (4040)", CD4040.class); // 183
        addMapping(MappedElementCategory.CDSeries, "Add BCD to 7-Segment Decoder (4511)", CD4511.class); // 184
        addMapping(MappedElementCategory.CDSeries, "Add Decade Counter (4017)", CD4017.class); // 189
    }

    private void addMapping(MappedElementCategory category, String menuString, Class<? extends CircuitElm> clazz) {
        elements.add(new MappedElement(category, menuString, clazz));
    }

    @SuppressWarnings("unchecked")
    private void register(Class<?> c, CircuitElm elm) {
        int t = elm.getDumpType();
        if (t == 0) {
            System.out.println("no dump type: " + c);
            return;
        }
        Class<? extends CircuitElm> dclass = elm.getDumpClass();
        if (dumpTypes[t] == dclass)
            return;
        if (dumpTypes[t] != null) {
            System.out.println("dump type conflict: " + c + " " + dumpTypes[t]);
            return;
        }
        dumpTypes[t] = dclass;
    }

    public CircuitLoadResult addGraphElements(String json) {
        String cmf = graphManager.append(json);
        return loadCircuit(cmf, true, true);
    }

    public void deleteGraphElement(int elementId) {
        if (elementId == 0) return;
        graphManager.delete(elementId);
        for (Iterator<CircuitElm> it = engine.elmList.iterator(); it.hasNext(); ) {
            CircuitElm elm = it.next();
            if (elm.flags2 == elementId) {
                elm.delete();
                it.remove();
                engine.setAnalyzeFlag();
            }
        }
    }

    public String getGraphAsCmf() {
        return graphManager.getCmf();
    }


    public HashMap<Integer, List<Object>> getGraph() {
        HashMap<Integer, List<Object>> graph = graphManager.getJsonGraph();
        if (graph.isEmpty()) { //if circuit wasn't loaded as graph fallback to dump method
            graph = engine.dumpAsGraph(1, -1, -1);
        }
        return graph;
    }

    public CircuitLoadResult loadGraphCircuit(File jsonFile) {
        try {
            graphManager.clear();
            String cmf = graphManager.append(jsonFile);
            return loadCircuit(cmf, false, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return CircuitLoadResult.ERROR;
    }

    public CircuitLoadResult loadCircuit(File file) {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String ext = FilenameUtils.getExtension(file.getAbsolutePath().toString());
            boolean newFormat = false;
            if (ext.equals("cmf"))
                newFormat = true;
            return loadCircuit(bytes, bytes.length, false, newFormat);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return CircuitLoadResult.ERROR;
    }

    public CircuitLoadResult loadCircuit(String text, String extension) {
        boolean newFormat = false;
        if (extension.equals("cmf")) {
            newFormat = true;
        } else if (extension.equals("json")) {
            newFormat = true;
            GraphManager graphManager = new GraphManager();
            text = graphManager.append(text);
        }
        return loadCircuit(text.getBytes(), text.length(), false, newFormat);
    }

    public CircuitLoadResult loadCircuit(String text) {
        return loadCircuit(text, false);
    }

    public CircuitLoadResult loadCircuit(String text, boolean retain) {
        return loadCircuit(text.getBytes(), text.length(), retain);
    }

    public CircuitLoadResult loadCircuit(String text, boolean retain, boolean isNewFormat) {
        return loadCircuit(text.getBytes(), text.length(), retain, isNewFormat);
    }

    public CircuitLoadResult loadCircuit(byte b[], int len, boolean retain) {
        return loadCircuit(b, len, retain, false);
    }

    public CircuitLoadResult loadCircuit(byte b[], int len, boolean retain, boolean isNewFormat) {
        return loadCircuit(b, len, retain ? EnumSet.allOf(RetentionPolicy.class) : EnumSet.noneOf(RetentionPolicy.class), isNewFormat);
    }

    public CircuitLoadResult loadCircuit(byte b[], int len, EnumSet<RetentionPolicy> retain, boolean isNewFormat) {
        CircuitLoadResult status = CircuitLoadResult.OK;

        if (!retain.contains(RetentionPolicy.ELEMENTS)) {
            for (int i = 0; i != engine.elmList.size(); i++) {
                CircuitElm ce = engine.elmList.get(i);
                ce.delete();
            }
            engine.elmList.removeAllElements();
        }
        if (!retain.contains(RetentionPolicy.CONFIG)) {
            listener.configureOptions(new CircuitOptions());
        }
        for (int p = 0; p < len; ) {
            int l;
            int linelen = 0;
            for (l = 0; l != len - p; l++)
                if (b[l + p] == '\n' || b[l + p] == '\r') {
                    linelen = l++;
                    if (l + p < b.length && b[l + p] == '\n')
                        l++;
                    break;
                }
            String line = new String(b, p, linelen);
            StringTokenizer st = new StringTokenizer(line);
            while (st.hasMoreTokens()) {
                String type = st.nextToken();
                int tint = type.charAt(0);
                try {
                    if (tint == 'o') {
                        listener.handleScope(st);
                        break;
                    }
                    if (tint == 'h') {
                        readHint(st);
                        break;
                    }
                    if (tint == '$') {
                        listener.configureOptions(new CircuitOptions(st));
                        break;
                    }
                    if (tint == '%' || tint == '?' || tint == 'B') {
                        // ignore afilter-specific stuff
                        break;
                    }
                    if (tint >= '0' && tint <= '9')
                        tint = new Integer(type);
                    int x1 = new Integer(st.nextToken());
                    int y1 = new Integer(st.nextToken());
                    int x2 = new Integer(st.nextToken());
                    int y2 = new Integer(st.nextToken());
                    int f = new Integer(st.nextToken());
                    int f2 = 0;

                    //If cmf file then read second flag for simulations purpose only
                    if (isNewFormat)
                        f2 = new Integer(st.nextToken());

                    CircuitElm ce;
                    Class<?> cls = dumpTypes[tint];
                    if (cls == null) {
                        System.out.println("unrecognized dump type: " + type);
                        status = CircuitLoadResult.DUMP_WARNING;
                        break;
                    }

                    // find element class
                    Class<?> carr[];
                    Constructor<?> cstr = null;
                    if (!isNewFormat) {
                        carr = new Class[7];
                        carr[0] = CircuitEngine.class;
                        carr[1] = carr[2] = carr[3] = carr[4] = carr[5] = int.class;
                        carr[6] = StringTokenizer.class;
                        cstr = cls.getConstructor(carr);
                    } else {
                        carr = new Class[8];
                        carr[0] = CircuitEngine.class;
                        carr[1] = carr[2] = carr[3] = carr[4] = carr[5] = carr[6] = int.class;
                        carr[7] = StringTokenizer.class;
                        cstr = cls.getConstructor(carr);
                    }

                    // invoke constructor with starting coordinates
                    Object oarr[];
                    if (!isNewFormat) {
                        oarr = new Object[7];
                        oarr[0] = engine;
                        oarr[1] = x1;
                        oarr[2] = y1;
                        oarr[3] = x2;
                        oarr[4] = y2;
                        oarr[5] = f;
                        oarr[6] = st;
                    } else {
                        oarr = new Object[8];
                        oarr[0] = engine;
                        oarr[1] = x1;
                        oarr[2] = y1;
                        oarr[3] = x2;
                        oarr[4] = y2;
                        oarr[5] = f;
                        oarr[6] = f2;
                        oarr[7] = st;
                    }

                    ce = (CircuitElm) cstr.newInstance(oarr);
                    ce.setPoints();
                    engine.elmList.addElement(ce);
                } catch (InvocationTargetException ee) {
                    ee.getTargetException().printStackTrace();
                    return CircuitLoadResult.ERROR;
                } catch (Exception ee) {
                    ee.printStackTrace();
                    return CircuitLoadResult.ERROR;
                }
                break;
            }
            p += l;
        }
        listener.afterLoading(retain);

        return status;
    }

    private void readHint(StringTokenizer st) {
        int hintType = new Integer(st.nextToken());
        int hintItem1 = new Integer(st.nextToken());
        int hintItem2 = new Integer(st.nextToken());
        listener.configureHints(hintType, hintItem1, hintItem2);
    }

    public Class<?>[] getDumpTypes() {
        return dumpTypes;
    }

    public List<MappedElement> getMappedElements() {
        return elements;
    }

    public enum RetentionPolicy {
        CONFIG, ELEMENTS,
    }
}
