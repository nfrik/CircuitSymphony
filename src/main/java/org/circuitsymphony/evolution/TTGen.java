package org.circuitsymphony.evolution;

import java.util.*;

/**
 * Created by nfrik on 9/13/17.
 */
public class TTGen {

    private Vector<Vector<Double>> andset = new Vector<Vector<Double>>();
    private Vector<Vector<Double>> orset = new Vector<Vector<Double>>();
    private Vector<Vector<Double>> xorset = new Vector<Vector<Double>>();

    public TTGen(){

        andset.add(0,new Vector<Double>());
        andset.get(0).add(0.);
        andset.get(0).add(0.);
        andset.get(0).add(0.);

        andset.add(1,new Vector<Double>());
        andset.get(1).add(1.);
        andset.get(1).add(0.);
        andset.get(1).add(0.);

        andset.add(2,new Vector<Double>());
        andset.get(2).add(0.);
        andset.get(2).add(1.);
        andset.get(2).add(0.);

        andset.add(3,new Vector<Double>());
        andset.get(3).add(1.);
        andset.get(3).add(1.);
        andset.get(3).add(1.);


        orset.add(0,new Vector<Double>());
        orset.get(0).add(0.);
        orset.get(0).add(0.);
        orset.get(0).add(0.);

        orset.add(1,new Vector<Double>());
        orset.get(1).add(1.);
        orset.get(1).add(0.);
        orset.get(1).add(1.);

        orset.add(2,new Vector<Double>());
        orset.get(2).add(0.);
        orset.get(2).add(1.);
        orset.get(2).add(1.);

        orset.add(3,new Vector<Double>());
        orset.get(3).add(1.);
        orset.get(3).add(1.);
        orset.get(3).add(1.);


        xorset.add(0,new Vector<Double>());
        xorset.get(0).add(0.);
        xorset.get(0).add(0.);
        xorset.get(0).add(0.);

        xorset.add(1,new Vector<Double>());
        xorset.get(1).add(1.);
        xorset.get(1).add(0.);
        xorset.get(1).add(1.);

        xorset.add(2,new Vector<Double>());
        xorset.get(2).add(0.);
        xorset.get(2).add(1.);
        xorset.get(2).add(1.);

        xorset.add(3,new Vector<Double>());
        xorset.get(3).add(1.);
        xorset.get(3).add(1.);
        xorset.get(3).add(0.);

    }

    public enum DATA{AND,OR,XOR};

    private Vector<Vector<Double>> generateSetOfLenFromSet(int n, Vector<Vector<Double>> set){

        Vector<Vector<Double>> result = new Vector<Vector<Double>>();

        Random r = new Random();

        for(int i=0;i<n;i++){
            result.add(i,set.get(r.nextInt(set.size())));
        }

        r.nextInt();
        return result;
    }

    public Vector<Vector<Double>> getTestDataOfLen(int n,DATA type){
        Vector<Vector<Double>> result = null;
        switch (type){
            case AND: result = generateSetOfLenFromSet(n,andset); break;
            case  OR: result = generateSetOfLenFromSet(n,orset); break;
            case XOR: result = generateSetOfLenFromSet(n,xorset); break;
            default: break;
        }

        return result;
    }

    public static void main(String[] args){
        TTGen ga = new TTGen();

        Vector<Vector<Double>> resultAND = ga.getTestDataOfLen(10,DATA.AND);
        Vector<Vector<Double>> resultOR = ga.getTestDataOfLen(10,DATA.OR);
        Vector<Vector<Double>> resultXOR = ga.getTestDataOfLen(10,DATA.XOR);

        System.out.println(resultAND);
        System.out.println(resultOR);
        System.out.println(resultXOR);
    }
}
