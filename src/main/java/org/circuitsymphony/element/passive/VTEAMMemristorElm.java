package org.circuitsymphony.element.passive;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.CirSim;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class VTEAMMemristorElm extends CircuitElm {
    private double r_on;
    private double r_off;
    private double dopeWidth;
    private double totalWidth;
    private double mobility;
    private double resistance;

    private double voff;
    private double von;
    private double koff;
    private double kon;
    private double aoff;
    private double aon;
    private double lambda;

    private  double xon;
    private double xoff;

    private int linear;
    private double p;
    private int hs;

    private double tau;

    protected static final int MEM_LINEAR = 0; //I.e. no memtype
    protected static final int MEM_NONLINEAR = 1;

    public VTEAMMemristorElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        r_on = 100;
        r_off = 160 * r_on;
        dopeWidth = 0;
        totalWidth = 10e-9; // meters
        mobility = 1e-10;   // m^2/sV
        resistance = 10;
//        aoff = 2e-9; //Upper bound of undoped region (Simmons tunnel barrier) [m]
//        aon = 1.2e-9; //Lower bound of undoped region (Simmons tunnel barrier [m]
//        koff = 5e-4; //VTEAM parameter
//        kon = -10; //VTEAM parameter
//        voff = 0.02; //Threshold voltages in VTEAM
//        von = -0.2; //Threshold voltages in VTEAM
        aoff = 3; //Upper bound of undoped region (Simmons tunnel barrier) [m]
        aon = 9; //Lower bound of undoped region (Simmons tunnel barrier [m]
        koff = 5e-4; //VTEAM parameter
        kon = -1.32e-6; //VTEAM parameter
        voff = 0.145; //Threshold voltages in VTEAM
        von = -0.09; //Threshold voltages in VTEAM
        lambda = Math.log(r_off/r_on);
        linear = MEM_LINEAR;

        xon=0;
        xoff=totalWidth;
        p=10;
        tau=1e18;

    }

    public VTEAMMemristorElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                             StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public VTEAMMemristorElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                             StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        r_on = new Double(st.nextToken());
        r_off = new Double(st.nextToken());
        dopeWidth = new Double(st.nextToken());
        totalWidth = new Double(st.nextToken());
//        mobility = new Double(st.nextToken());
        voff = new Double(st.nextToken());
        von = new Double(st.nextToken());
        koff = new Double(st.nextToken());
        kon = new Double(st.nextToken());
        aoff = new Double(st.nextToken());
        aon = new Double(st.nextToken());
        tau = new Double(st.nextToken());
        linear = new Integer(st.nextToken());
//        p = new Double(st.nextToken());
        xon=0;
        xoff=totalWidth;
        p=10;
        resistance = 100;
    }

    @Override
    public int getDumpType() {
        return 'Q';
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + r_on + " " + r_off + " " + dopeWidth + " " +
                totalWidth + " " + voff + " " + von + " " + koff + " " + kon+ " " + aoff + " " + aon + " "+tau+ " "+linear;
    }

    @Override
    public void setPoints() {
        super.setPoints();
        calcLeads(32);
    }

    @Override
    public void draw(DrawContext ctx) {
        int segments = 6;
        int i;
        int ox = 0;
        double d = 12; //distance from lead to draw anode bar
        int nn = 3;
        double v1 = volts[0];
        double v2 = volts[1];
        hs = 2 + (int) (8 * (1 - dopeWidth / totalWidth));
        setBbox(point1, point2, hs);
        draw2Leads(ctx);
        setPowerColor(ctx, true);
        double segf = 1. / segments;

        // draw zigzag
        for (i = 0; i <= segments; i++) {
            int nx = (i & 1) == 0 ? 1 : -1;
//            if (i == 0) {
//                int[] xs = new int[] {(int) lead1.getX()-10,(int)lead1.getX()+10,(int)lead1.getX()};
//                int[] ys = new int[] {(int) lead1.getY(),(int)lead1.getY(),(int)lead1.getY()+10};
//                int nn = 3;
//                Polygon p = new Polygon(xs,ys,nn);
//                ctx.fillPolygon(p);
//            }
            if (i == 0) {
                double a=Math.atan2((point1.y-lead1.y),(point1.x-lead1.x));
                int dx=(int) Math.floor(Math.cos(a)*d);
                int dy=(int) Math.floor(Math.sin(a)*d);

                a=a+Math.PI/2;
                double x0=lead1.x+dx;
                double y0=lead1.y+dy;

                double tx1=10*Math.cos(a)+x0;
                double tx2=-10*Math.cos(a)+x0;
                double tx3=-10*Math.sin(a)+x0;
                double ty1=10*Math.sin(a)+y0;
                double ty2=-10*Math.sin(a)+y0;
                double ty3=10*Math.cos(a)+y0;

                int[] xs = new int[] {(int) tx1,(int)tx2,(int)tx3};
                int[] ys = new int[] {(int) ty1,(int)ty2,(int)ty3};

//                int[] xs = new int[] {(int) lead1.getX()-10+dx,(int)lead1.getX()+10+dx,(int)lead1.getX()+dx};
//                int[] ys = new int[] {(int) lead1.getY()+dy,(int)lead1.getY()+dy,(int)lead1.getY()+10+dy};

                Polygon p = new Polygon(xs,ys,nn);
                ctx.fillPolygon(p);

//                ctx.drawDoubleThickLine(ps1.x+dx,ps1.y+dy, ps2.x+dx,ps2.y+dy);
//                interpPoint(lead1, lead2, ps1, i * segf, -hs * nx);
//                ctx.drawDoubleThickLine(ps1.x+dx,ps1.y+dy, ps2.x+dx,ps2.y+dy);
            }
            if (i == segments)
                nx = 0;
            double v = v1 + (v2 - v1) * i / segments;
            setVoltageColor(ctx, v);
            interpPoint(lead1, lead2, ps1, i * segf, hs * ox);
            interpPoint(lead1, lead2, ps2, i * segf, hs * nx);
            ctx.drawThickLine(ps1, ps2);
            if (i == segments)
                break;
            interpPoint(lead1, lead2, ps1, (i + 1) * segf, hs * nx);
            ctx.drawThickLine(ps1, ps2);
            ox = nx;
        }

        doDots(ctx);
        drawPosts(ctx);
    }

    @Override
    public boolean isBasicBoundingBoxSupported() {
        return true;
    }

    @Override
    public void getBasicBoundingBox(Point tempP1, Point tempP2, Rectangle result) {
        setBbox(result, tempP1, tempP2, hs);
    }

    @Override
    public boolean nonLinear() {
        return true;
    }

    @Override
    public void calculateCurrent() {
//        current = (volts[0] - volts[1]) / resistance;
        calculateResistance();
        current=(volts[0]-volts[1])/resistance;
//        if(linear==MEM_LINEAR){
//            current=(volts[0]-volts[1])/(r_on+((r_off-r_on)/(xoff-xon))*(dopeWidth-xon));
//        }else{
//            current=(volts[0]-volts[1])/(r_on*Math.exp(lambda*(dopeWidth-xon)/(xoff-xon)));
//        }
    }

    @Override
    public void reset() {
        dopeWidth = 0;
    }

    private void calculateResistance(){

        if(linear==MEM_LINEAR){
            resistance=(r_on+((r_off-r_on)/(xoff-xon))*(dopeWidth-xon));
        }else{
            resistance=(r_on*Math.exp(lambda*(dopeWidth-xon)/(xoff-xon)));
        }
        if(resistance>r_off){
            resistance=r_off;
        }else if(resistance<r_on){
            resistance=r_on;
        }
    }

    @Override
    public void startIteration() {
//        dopeWidth += engine.timeStep * mobility * r_on * current / totalWidth * window(wd,current);
//        if (dopeWidth <= eps)
//            dopeWidth = eps;
//        if (dopeWidth > totalWidth-eps)
//            dopeWidth = totalWidth-eps;
//        resistance = r_on * wd + r_off * (1 - wd);
//
//        dopeWidth +=


        double v = volts[0]-volts[1];

        if(v>=voff){
            dopeWidth+=(koff * Math.pow(v / voff - 1, aoff)) * engine.timeStep;
        }else if(v<=von) {
            dopeWidth+=(kon * Math.pow(v / von - 1, aon)) * engine.timeStep;
        }

        dopeWidth+= dopeWidth / tau * engine.timeStep;

//        dopeWidth += engine.timeStep * u * (1/(1-wd))*k/r_off*(1-Math.pow(2*wd - 1,2 * p));
        if (dopeWidth <= 0)
            dopeWidth = 0;
        if (dopeWidth > totalWidth)
            dopeWidth = totalWidth;

//        calculateResistance();
        calculateCurrent();


    }

    @Override
    public void stamp() {
        engine.stampNonLinear(nodes[0]);
        engine.stampNonLinear(nodes[1]);
    }

    @Override
    public void doStep() {
        engine.stampResistor(nodes[0], nodes[1], resistance);
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "VTEAM memristor";
        getBasicInfo(ctx, arr);
        arr[3] = "R = " + ctx.getUnitText(resistance, CirSim.ohmString);
        arr[4] = "P = " + ctx.getUnitText(getPower(), "W");
    }

    @Override
    public double getScopeValue(int x) {
        return (x == 2) ? resistance : (x == 1) ? getPower() : getVoltageDiff();
    }

    @Override
    public String getScopeUnits(int x) {
        return (x == 2) ? CirSim.ohmString : (x == 1) ? "W" : "V";
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Ron (ohms)", r_on, 0, 0);
        if (n == 1)
            return new EditInfo("Roff (ohms)", r_off, 0, 0);
        if (n == 2)
            return new EditInfo("Width of Doped Region (nm)", dopeWidth * 1e9, 0, 0);
        if (n == 3)
            return new EditInfo("Total Width (nm)", totalWidth * 1e9, 0, 0);
        if (n == 4)
            return new EditInfo("voff", voff, 0, 0);
        if (n == 5)
            return new EditInfo("von", von, 0, 0);
        if (n == 6)
            return new EditInfo("koff", koff, 0, 0);
        if (n == 7)
            return new EditInfo("kon", kon, 0, 0);
        if (n == 8)
            return new EditInfo("aoff", aoff, 0, 0);
        if (n == 9)
            return new EditInfo("aon", aon, 0, 0);
        if (n == 10)
            return new EditInfo("tau", tau, 0, 0);
        if (n == 11) {
            EditInfo ei = new EditInfo("Linear", linear, -1, -1);
            ei.setChoice(new Choice());
            ei.getChoice().add("Linear");
            ei.getChoice().add("Non-Linear");
            ei.getChoice().select(linear);
            return ei;
        }
        return null;

    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
            r_on = ei.getValue();
        if (n == 1)
            r_off = ei.getValue();
        if (n == 2)
            dopeWidth = ei.getValue() * 1e-9;
        if (n == 3)
            totalWidth = ei.getValue() * 1e-9;
        if (n == 4)
            voff = ei.getValue();
        if (n == 5)
            von = ei.getValue();

        if (n == 6)
            koff = ei.getValue();
        if (n == 7)
            kon = ei.getValue();
        if (n == 8)
            aoff = ei.getValue();
        if (n == 9)
            aon = ei.getValue();
        if (n == 10)
            tau = ei.getValue();
        if (n == 11)
            linear = ei.getChoice().getSelectedIndex();
    }

    public double getROn() {
        return r_on;
    }

    public void setROn(double r_on) {
        this.r_on = r_on;
    }

    public double getROff() {
        return r_off;
    }

    public void setROff(double r_off) {
        this.r_off = r_off;
    }

    public double getDopeWidth() {
        return dopeWidth;
    }

    public void setDopeWidth(double dopeWidth) {
        this.dopeWidth = dopeWidth;
    }

    public double getTotalWidth() {
        return totalWidth;
    }

    public void setTotalWidth(double totalWidth) {
        this.totalWidth = totalWidth;
    }

    public double getMobility() {
        return mobility;
    }

    public void setMobility(double mobility) {
        this.mobility = mobility;
    }

    public double getVoff() {
        return voff;
    }

    public void setVoff(double voff) {
        this.voff = voff;
    }

    public double getVon() {
        return von;
    }

    public void setVon(double von) {
        this.von = von;
    }

    public double getKoff() {
        return koff;
    }

    public void setKoff(double koff) {
        this.koff = koff;
    }


    public double getKon() {
        return kon;
    }

    public void setKon(double kon) {
        this.kon = kon;
    }

    public double getAoff() {
        return aoff;
    }

    public void setAoff(double aoff) {
        this.aoff = aoff;
    }

    public double getAon() {
        return aon;
    }

    public void setAon(double aon) {
        this.aon = aon;
    }

    public int getLinear() {
        return linear;
    }

    public void setLinear(int linear) {
        this.linear = linear;
    }

    public double getTau() {
        return tau;
    }

    public void setTau(double tau) {
        this.tau = tau;
    }


}