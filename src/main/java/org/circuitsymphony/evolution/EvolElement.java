package org.circuitsymphony.evolution;

import org.jenetics.DoubleGene;
import org.jenetics.util.DoubleRange;

/**
 * Created by nfrik on 10/9/17.
 */
public class EvolElement {

    public enum ELEM{INPUT,OUTPUT,INTERNAL};

    public ELEM definition;
    private DoubleRange range;

    private Integer elemId;

    public EvolElement(Integer elemId, ELEM definition, DoubleRange range){
        setElemId(elemId);
        setDefinition(definition);
        setRange(range);
    }

    public Integer getElemId() {
        return elemId;
    }

    public void setElemId(Integer elemId) {
        this.elemId = elemId;
    }

    public ELEM getDefinition() {
        return definition;
    }

    public void setDefinition(ELEM definition) {
        this.definition = definition;
    }

    public void setRange(DoubleRange range){
        this.range = range;
    }

    public DoubleRange getRange(){
        return this.range;
    }

}
