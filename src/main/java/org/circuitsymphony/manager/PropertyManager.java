package org.circuitsymphony.manager;

import org.circuitsymphony.element.CapacitorElm;
import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.element.active.*;
import org.circuitsymphony.element.io.RailElm;
import org.circuitsymphony.element.passive.*;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Describes what properties are exposed from specified {@link CircuitElm}. Allows to dynamically manage those
 * properties for example from REST API using property keys.
 *
 */
public class PropertyManager {
    private HashMap<Class<? extends CircuitElm>, PropertyList<? extends CircuitElm>> simProps = new HashMap<>();

    private AtomicBoolean needAnalyze;

    public PropertyManager(AtomicBoolean needAnalyze) {
        add(ResistorElm.class, new PropertyList<ResistorElm>()
                .add("resistance", Double.class, ResistorElm::getResistance, ResistorElm::setResistance));
        add(CapacitorElm.class, new PropertyList<CapacitorElm>()
                .add("capacitance", Double.class, CapacitorElm::getCapacitance, CapacitorElm::setCapacitance));
        add(InductorElm.class, new PropertyList<InductorElm>()
                .add("inductance", Double.class, InductorElm::getInductance, InductorElm::setInductance));
        add(TransistorElm.class, new PropertyList<TransistorElm>()
                .add("beta", Double.class, TransistorElm::getBeta, TransistorElm::setBeta));
        add(MemristorElm.class, new PropertyList<MemristorElm>()
                .add("ron", Double.class, MemristorElm::getROn, MemristorElm::setROn)
                .add("roff", Double.class, MemristorElm::getROff, MemristorElm::setROff)
                .add("dopeWidth", Double.class, MemristorElm::getDopeWidth, MemristorElm::setDopeWidth)
                .add("totalWidth", Double.class, MemristorElm::getTotalWidth, MemristorElm::setTotalWidth)
                .add("mobility", Double.class, MemristorElm::getMobility, MemristorElm::setMobility));
        add(WindowMemristorElm.class, new PropertyList<WindowMemristorElm>()
                .add("type", Integer.class, WindowMemristorElm::getType, WindowMemristorElm::setType)
                .add("ron", Double.class, WindowMemristorElm::getROn, WindowMemristorElm::setROn)
                .add("roff", Double.class, WindowMemristorElm::getROff, WindowMemristorElm::setROff)
                .add("dopeWidth", Double.class, WindowMemristorElm::getDopeWidth, WindowMemristorElm::setDopeWidth)
                .add("totalWidth", Double.class, WindowMemristorElm::getTotalWidth, WindowMemristorElm::setTotalWidth)
                .add("mobility", Double.class, WindowMemristorElm::getMobility, WindowMemristorElm::setMobility)
                .add("a",Double.class, WindowMemristorElm::getA, WindowMemristorElm::setA)
                .add("b",Double.class, WindowMemristorElm::getB, WindowMemristorElm::setB)
                .add("g",Double.class, WindowMemristorElm::getG, WindowMemristorElm::setG)
                .add("d",Double.class, WindowMemristorElm::getD, WindowMemristorElm::setD)
                .add("l",Double.class, WindowMemristorElm::getL, WindowMemristorElm::setL)
                .add("e",Double.class, WindowMemristorElm::getE, WindowMemristorElm::setE)
                .add("tau",Double.class, WindowMemristorElm::getTau, WindowMemristorElm::setTau));
        add(SillinMemristorElm.class, new PropertyList<SillinMemristorElm>()
                .add("ron", Double.class, SillinMemristorElm::getROn, SillinMemristorElm::setROn)
                .add("roff", Double.class, SillinMemristorElm::getROff, SillinMemristorElm::setROff)
                .add("dopeWidth", Double.class, SillinMemristorElm::getDopeWidth, SillinMemristorElm::setDopeWidth)
                .add("totalWidth", Double.class, SillinMemristorElm::getTotalWidth, SillinMemristorElm::setTotalWidth)
                .add("mobility", Double.class, SillinMemristorElm::getMobility, SillinMemristorElm::setMobility)
                .add("a",Double.class, SillinMemristorElm::getA, SillinMemristorElm::setA)
                .add("tau",Double.class, SillinMemristorElm::getTau, SillinMemristorElm::setTau));
        add(BiolekMemristorElm.class, new PropertyList<BiolekMemristorElm>()
                .add("ron", Double.class, BiolekMemristorElm::getROn, BiolekMemristorElm::setROn)
                .add("roff", Double.class, BiolekMemristorElm::getROff, BiolekMemristorElm::setROff)
                .add("dopeWidth", Double.class, BiolekMemristorElm::getDopeWidth, BiolekMemristorElm::setDopeWidth)
                .add("totalWidth", Double.class, BiolekMemristorElm::getTotalWidth, BiolekMemristorElm::setTotalWidth)
                .add("mobility", Double.class, BiolekMemristorElm::getMobility, BiolekMemristorElm::setMobility)
                .add("g",Double.class, BiolekMemristorElm::getG, BiolekMemristorElm::setG)
                .add("tau",Double.class, BiolekMemristorElm::getTau, BiolekMemristorElm::setTau));
        add(ProdromakisMemristorElm.class, new PropertyList<ProdromakisMemristorElm>()
                .add("ron", Double.class, ProdromakisMemristorElm::getROn, ProdromakisMemristorElm::setROn)
                .add("roff", Double.class, ProdromakisMemristorElm::getROff, ProdromakisMemristorElm::setROff)
                .add("dopeWidth", Double.class, ProdromakisMemristorElm::getDopeWidth, ProdromakisMemristorElm::setDopeWidth)
                .add("totalWidth", Double.class, ProdromakisMemristorElm::getTotalWidth, ProdromakisMemristorElm::setTotalWidth)
                .add("mobility", Double.class, ProdromakisMemristorElm::getMobility, ProdromakisMemristorElm::setMobility)
                .add("g",Double.class, ProdromakisMemristorElm::getG, ProdromakisMemristorElm::setG)
                .add("l",Double.class, ProdromakisMemristorElm::getL, ProdromakisMemristorElm::setL)
                .add("tau",Double.class, ProdromakisMemristorElm::getTau, ProdromakisMemristorElm::setTau));
        add(ZhaMemristorElm.class, new PropertyList<ZhaMemristorElm>()
                .add("ron", Double.class, ZhaMemristorElm::getROn, ZhaMemristorElm::setROn)
                .add("roff", Double.class, ZhaMemristorElm::getROff, ZhaMemristorElm::setROff)
                .add("dopeWidth", Double.class, ZhaMemristorElm::getDopeWidth, ZhaMemristorElm::setDopeWidth)
                .add("totalWidth", Double.class, ZhaMemristorElm::getTotalWidth, ZhaMemristorElm::setTotalWidth)
                .add("mobility", Double.class, ZhaMemristorElm::getMobility, ZhaMemristorElm::setMobility)
                .add("a",Double.class, ZhaMemristorElm::getA, ZhaMemristorElm::setA)
                .add("b",Double.class, ZhaMemristorElm::getB, ZhaMemristorElm::setB)
                .add("p",Double.class, ZhaMemristorElm::getP, ZhaMemristorElm::setP)
                .add("l",Double.class, ZhaMemristorElm::getL, ZhaMemristorElm::setL)
                .add("tau",Double.class, ZhaMemristorElm::getTau, ZhaMemristorElm::setTau));
        add(SinghMemristorElm.class, new PropertyList<SinghMemristorElm>()
                .add("ron", Double.class, SinghMemristorElm::getROn, SinghMemristorElm::setROn)
                .add("roff", Double.class, SinghMemristorElm::getROff, SinghMemristorElm::setROff)
                .add("dopeWidth", Double.class, SinghMemristorElm::getDopeWidth, SinghMemristorElm::setDopeWidth)
                .add("totalWidth", Double.class, SinghMemristorElm::getTotalWidth, SinghMemristorElm::setTotalWidth)
                .add("mobility", Double.class, SinghMemristorElm::getMobility, SinghMemristorElm::setMobility)
                .add("g",Double.class, SinghMemristorElm::getG, SinghMemristorElm::setG)
                .add("l",Double.class, SinghMemristorElm::getL, SinghMemristorElm::setL)
                .add("tau",Double.class, SinghMemristorElm::getTau, SinghMemristorElm::setTau));
        add(VTEAMMemristorElm.class, new PropertyList<VTEAMMemristorElm>()
                .add("ron", Double.class, VTEAMMemristorElm::getROn, VTEAMMemristorElm::setROn)
                .add("roff", Double.class, VTEAMMemristorElm::getROff, VTEAMMemristorElm::setROff)
                .add("dopeWidth", Double.class, VTEAMMemristorElm::getDopeWidth, VTEAMMemristorElm::setDopeWidth)
                .add("totalWidth", Double.class, VTEAMMemristorElm::getTotalWidth, VTEAMMemristorElm::setTotalWidth)
                .add("voff",Double.class, VTEAMMemristorElm::getVoff, VTEAMMemristorElm::setVoff)
                .add("von",Double.class, VTEAMMemristorElm::getVon, VTEAMMemristorElm::setVon)
                .add("koff",Double.class, VTEAMMemristorElm::getKoff, VTEAMMemristorElm::setKoff)
                .add("kon",Double.class, VTEAMMemristorElm::getKon, VTEAMMemristorElm::setKon)
                .add("aoff",Double.class, VTEAMMemristorElm::getAoff, VTEAMMemristorElm::setAoff)
                .add("aon",Double.class, VTEAMMemristorElm::getAon, VTEAMMemristorElm::setAon)
                .add("tau",Double.class, VTEAMMemristorElm::getTau, VTEAMMemristorElm::setTau)
                .add("linear", Integer.class, VTEAMMemristorElm::getLinear, VTEAMMemristorElm::setLinear));
        add(FStochasticMemristorElm.class, new PropertyList<FStochasticMemristorElm>()
                .add("ron", Double.class, FStochasticMemristorElm::getROn, FStochasticMemristorElm::setROn)
                .add("roff", Double.class, FStochasticMemristorElm::getROff, FStochasticMemristorElm::setROff)
                .add("dopeWidth", Double.class, FStochasticMemristorElm::getDopeWidth, FStochasticMemristorElm::setDopeWidth)
                .add("totalWidth", Double.class, FStochasticMemristorElm::getTotalWidth, FStochasticMemristorElm::setTotalWidth)
                .add("mobility", Double.class, FStochasticMemristorElm::getMobility, FStochasticMemristorElm::setMobility)
                .add("a",Double.class, FStochasticMemristorElm::getA, FStochasticMemristorElm::setA)
                .add("tau",Double.class, FStochasticMemristorElm::getTau, FStochasticMemristorElm::setTau)
                .add("r_on_min",Double.class, FStochasticMemristorElm::getROnMin, FStochasticMemristorElm::setROnMin)
                .add("r_off_max",Double.class, FStochasticMemristorElm::getROnMax, FStochasticMemristorElm::setROnMax)
                .add("b",Double.class, FStochasticMemristorElm::getB, FStochasticMemristorElm::setB)
                .add("rho",Double.class, FStochasticMemristorElm::getRho, FStochasticMemristorElm::setRho));
        add(DiodeElm.class, new PropertyList<DiodeElm>()
                .add("fwdVoltage", Double.class, DiodeElm::getFwdVoltage, DiodeElm::setFwdVoltage));
        add(ZenerElm.class, new PropertyList<ZenerElm>()
                .add("fwdVoltage", Double.class, ZenerElm::getFwdVoltage, ZenerElm::setFwdVoltage)
                .add("zenerVoltage", Double.class, ZenerElm::getZennerVoltage, ZenerElm::setZennerVoltage));
        add(OpAmpElm.class, new PropertyList<OpAmpElm>()
                .add("maxOut", Double.class, OpAmpElm::getMaxOut, OpAmpElm::setMaxOut)
                .add("minOut", Double.class, OpAmpElm::getMinOut, OpAmpElm::setMinOut));
        add(MosfetElm.class, new PropertyList<MosfetElm>()
                .add("thresholdVoltage", Double.class, MosfetElm::getThresholdVoltage, MosfetElm::setThresholdVoltage));
        add(JfetElm.class, new PropertyList<JfetElm>()
                .add("thresholdVoltage", Double.class, JfetElm::getThresholdVoltage, JfetElm::setThresholdVoltage));
        add(RailElm.class, new PropertyList<RailElm>()
                .add("maxVoltage", Double.class, RailElm::getMaxVoltage, RailElm::setMaxVoltage));

        this.needAnalyze=needAnalyze;
    }

    private <T extends CircuitElm> void add(Class<T> klass, PropertyList<T> props) {
        simProps.put(klass, props);
    }

    public <T extends CircuitElm> List<String> getPropertyList(T element) {
        return simProps.getOrDefault(element.getClass(), new PropertyList<T>()).elmProps.values().stream()
                .map(property -> property.key)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public <T extends CircuitElm, V> V getProperty(T element, String propertyKey) {
        Property<T, V> prop = (Property<T, V>) simProps.get(element.getClass()).elmProps.get(propertyKey);
        if (prop == null) throw new IllegalStateException("No such property with name: " + propertyKey);
        return prop.getter.get(element);
    }

    @SuppressWarnings("unchecked")
    public <T extends CircuitElm, V> void setProperty(T element, String propertyKey, V newValue) {
        Property<T, V> prop = (Property<T, V>) simProps.get(element.getClass()).elmProps.get(propertyKey);
        if (prop == null) throw new IllegalStateException("No such property with name: " + propertyKey);

        // handle special case because boxed Integer can't be casted directly to double
        if (newValue instanceof Integer && prop.type == Double.class) {
            double doubleValue = ((Integer) newValue).doubleValue();
            Property<T, Double> doubleProp = (Property<T, Double>) prop;
            doubleProp.setter.set(element, doubleValue);
        } else {
            prop.setter.set(element, newValue);
        }

        this.needAnalyze.set(true);

    }

    private class PropertyList<T extends CircuitElm> {
        private HashMap<String, Property<T, ?>> elmProps = new HashMap<>();

        private <V> PropertyList<T> add(String key, Class<V> type, Getter<T, V> getter, Setter<T, V> setter) {
            elmProps.put(key, new Property<>(key, type, getter, setter));
            return this;
        }
    }

    private class Property<T extends CircuitElm, V> {
        private String key;
        private Class<V> type;
        private Getter<T, V> getter;
        private Setter<T, V> setter;

        public Property(String key, Class<V> type, Getter<T, V> getter, Setter<T, V> setter) {
            this.key = key;
            this.type = type;
            this.getter = getter;
            this.setter = setter;
        }
    }

    private interface Getter<T extends CircuitElm, V> {
        V get(T obj);
    }

    private interface Setter<T extends CircuitElm, V> {
        void set(T obj, V value);
    }
}

