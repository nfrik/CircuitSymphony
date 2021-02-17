package org.circuitsymphony.element.passive;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.CirSim;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.Random;
import java.util.StringTokenizer;

//Using general memtype function by: Jinxiang Jinxiang Zha, He Huang et.al.: Neurocomputing, 2017
// f(x)=1-[a(x-step(-i)^2b+(1-a)]^g
// where g=10,a=0.9,b=1
public class FStochasticMemristorElm extends CircuitElm {
    private double r_on;
    private double r_off;
    private double r_on_orig;
    private double r_off_orig;
    private double r_on_min;
    private double r_on_max;
    private double dopeWidth;
    private double totalWidth;
    private double mobility;
    private double resistance;
    private double a; //Noise factor
    private double b; //exponent
    private double rho; //resistance multiplier
    //    private double b; //b Cao and betta Chang
//    private double g; //g Biolek and Cao and gamma Chang
//    private double d; //delta Chang
//    private double l; //delta Chang
//    private double e; //delta Chang
    private int hs;
    private double eps;
    private double tau;
    private int memtype = MEM_SILLIN;

    private Random random;


    protected static final int MEM_STRUKOV = 0; //I.e. no memtype
    protected static final int MEM_BIOLEK = 1;
    protected static final int MEM_PRODROMAKIS = 2;
    protected static final int MEM_CAO = 3;
    protected static final int MEM_CHANG = 4;
    protected static final int MEM_TUNNEL = 5;
    //    protected static final int MEM_VTEAM = 6;
    protected static final int MEM_SILLIN = 6;

    private Random getRandom(){
        if(this.random==null){
            this.random = new Random();
        }
        return this.random;
    }

    public FStochasticMemristorElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        r_on = 100;
        r_off = 160 * r_on;
        dopeWidth = 0;
        totalWidth = 10e-9; // meters
        mobility = 1e-10;   // m^2/sV
        resistance = 100;
        a=0.0;
//        b=1;
//        g =10;
//        d=0.9;
//        l=4.5;
//        e=0.04;
        eps=1e-12;
        tau=1e18;
        r_on_min=r_on*0.5;
        r_on_max=r_on*4;
        b=1;
        rho=1e-2;
//        memtype = MEM_SILLIN;
    }

    public FStochasticMemristorElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                              StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public FStochasticMemristorElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                              StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
//        memtype = MEM_SILLIN;
        r_on = r_on_orig = new Double(st.nextToken());
        r_off = new Double(st.nextToken());
        dopeWidth = new Double(st.nextToken());
        totalWidth = new Double(st.nextToken());
        mobility = new Double(st.nextToken());
        a = new Double(st.nextToken());
//        b = new Double(st.nextToken());
//        g = new Double(st.nextToken());
//        d = new Double(st.nextToken());
//        l = new Double(st.nextToken());
//        e = new Double(st.nextToken());
        tau = new Double(st.nextToken());
        r_on_min = new Double(st.nextToken());
        r_on_max = new Double(st.nextToken());
        b = new Double(st.nextToken());
        rho = new Double(st.nextToken());
        eps = 1e-12;
        resistance = 100;

        random = new Random();
    }

    @Override
    public int getDumpType() {
        return 195;
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " "  + r_on + " " + r_off + " " + dopeWidth + " " +
                totalWidth + " " + mobility + " " + a + " " + tau+ " " + r_on_min+ " " + r_on_max+ " " + b + " " + rho;
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
//                ctx.fillRect((int) lead1.getX(), (int) lead1.getY(), 10, 10);
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

        current = (volts[0] - volts[1]) / resistance;
        if(Double.isNaN(current)){
            current=0;
        }
    }

    @Override
    public void reset() {
        dopeWidth = 0;
    }

    private void calculateResistance(){
        resistance = r_on * dopeWidth/totalWidth + r_off * (1 - dopeWidth/totalWidth);
        if(Double.isNaN(resistance)){
            resistance=r_off;
        }
    }

    @Override
    public void startIteration() {

        double u = (volts[0] - volts[1]);

        if (memtype == MEM_SILLIN) {
            dopeWidth += windowSillin();

//            double ron_change = a * r_on_orig * getRandom().nextGaussian() * engine.timeStep;
//            double new_ron = r_on + sdelta;
            double sdelta = a * getRandom().nextGaussian() * engine.timeStep;
            double delta = Math.pow(resistance/rho,1/b);

            double delta_ron = rho*b*Math.pow(delta, b-1)*sdelta;

            double new_ron = r_on + delta_ron;

            double ron_max_val=r_on_max*r_on_orig; //since r_on_max is a coefficient, not an absolute value of resistance
            double ron_min_val=r_on_min*r_on_orig;

            if(new_ron>=ron_max_val){
                r_on -= Math.abs(delta_ron);
                if(r_on<=ron_min_val){
                    r_on=ron_min_val;
                }
            }
            else if(new_ron<=ron_min_val){
                r_on += Math.abs(delta_ron);
                if(r_on>=ron_max_val){
                    r_on=ron_max_val;
                }
            }else{
                r_on += delta_ron;
            }
        }

        if (dopeWidth <= eps)
            dopeWidth = eps;
        if (dopeWidth > totalWidth-eps)
            dopeWidth = totalWidth-eps;
        if (Double.isNaN(dopeWidth)){
            dopeWidth=0;
        }
        double wd =dopeWidth/totalWidth;

        calculateResistance();
        calculateCurrent();

    }

//    private double windowBiolek(){
//        if(-current >= 0)
//            return 1-Math.pow(dopeWidth/totalWidth -1, 2* g);
//        return 1-Math.pow(dopeWidth/totalWidth, 2* g);
//    }
//
//
//    private double windowCao(){
//        if(-current >= 0)
//            return l*(1 - Math.pow(a * Math.pow((dopeWidth/totalWidth - 1),2*b) +(1-a), g));
//        return l*(1 - Math.pow(a * Math.pow((dopeWidth/totalWidth),2*b) +(1-a), g));
//    }

    private double windowSillin(){
//        double val = mobility * r_on / totalWidth * current * (dopeWidth/totalWidth-Math.pow(dopeWidth/totalWidth,2)) * engine.timeStep;
        double val = mobility * r_on / totalWidth * current * (dopeWidth/totalWidth/totalWidth*(totalWidth+eps-dopeWidth)) * engine.timeStep;
        val += (dopeWidth-totalWidth) / tau * engine.timeStep;
//        val += val * a * getRandom().nextGaussian() * engine.timeStep;
//        val += (dopeWidth-totalWidth) / tau * engine.timeStep;
        return val;
    }

//    private double stepKnowm(double u, double w){
//        double dw=(l*Math.sinh(e*u)-w/tau)*engine.timeStep;
//        return dw;
//
//    }
//
//    private double windowProdromakis(){
//        double val = l*(1-Math.pow(Math.pow(dopeWidth/totalWidth-0.5,2)+0.75,g));
//        return val;
//    }

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
        arr[0] = "Stochastic memristor";
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
//        if (n == 0) {
//            EditInfo ei = new EditInfo("Window", memtype, -1, -1);
//            ei.setChoice(new Choice());
////            ei.getChoice().add("Strukov");
////            ei.getChoice().add("Biolek");
////            ei.getChoice().add("Prodromakis");
////            ei.getChoice().add("Cao");
////            ei.getChoice().add("Chang");
////            ei.getChoice().add("Tunnel");
//            ei.getChoice().add("Sillin");
//            ei.getChoice().select(memtype);
//            return ei;
//        }
        if (n == 0)
            return new EditInfo("Ron (Ohm)", r_on, 0, 1e16);
        if (n == 1)
            return new EditInfo("Roff (Ohm)", r_off, 0, 1e16);
        if (n == 2)
            return new EditInfo("Width of Doped Region (nm)", dopeWidth * 1e9, 0, 0);
        if (n == 3)
            return new EditInfo("Total Width (nm)", totalWidth * 1e9, 0, 0);
        if (n == 4)
            return new EditInfo("Mobility (um^2/(s*V))", mobility * 1e12, 0, 0);
        if (n == 5)
            return new EditInfo("a Noise factor", a , 0, 1e9);
//        if (n == 7)
//            return new EditInfo("b (beta Cao) (a.u.)", b, 0, 0);
//        if (n == 8)
//            return new EditInfo("g (gamma Cao) (a.u.)", g, 0, 0);
//        if (n == 9)
//            return new EditInfo("d (delta Cao) (a.u.)", d , 0, 0);
//        if (n == 10)
//            return new EditInfo("l (lambda Cao) (a.u.)", l, 0, 0);
//        if (n == 11)
//            return new EditInfo("e (eta Cao) (a.u.)", e, 0, 0);
        if (n == 6)
            return new EditInfo("tau (s)", tau, 1e-12, 1e12);
        if (n == 7)
            return new EditInfo("Ron_min (Coeff)", r_on_min, 1e-15, 1e15);
        if (n == 8)
            return new EditInfo("Ron_max (Coeff)", r_on_max, 1e-15, 1e15);
        if (n == 9)
            return new EditInfo("b exponent", b, -10, 10);
        if (n == 10)
            return new EditInfo("rho (ohm)", rho, 1e-15, 1e15);

        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
//        if (n == 0)
//            memtype = ei.getChoice().getSelectedIndex();
        if (n == 0)
            setROn(ei.getValue());
        if (n == 1)
            setROff(ei.getValue());
        if (n == 2)
            dopeWidth = ei.getValue() * 1e-9;
        if (n == 3)
            totalWidth = ei.getValue() * 1e-9;
        if (n == 4)
            mobility = ei.getValue() * 1e-12;
        if (n == 5)
            a = ei.getValue();
//        if (n == 7)
//            b = ei.getValue();
//        if (n == 8)
//            g = ei.getValue();
//        if (n == 9)
//            d = ei.getValue();
//        if (n == 10)
//            l = ei.getValue();
//        if (n == 11)
//            e = ei.getValue();
        if (n == 6)
            setTau(ei.getValue());
        if (n == 7)
            setROnMin(ei.getValue());
        if (n == 8)
            setROnMax(ei.getValue());
        if (n == 9)
            setB(ei.getValue());
        if (n == 10)
            setRho(ei.getValue());
    }

    public double getROn() {
        return r_on;
    }

    public void setROn(double r_on) {

        this.r_on = r_on;
        this.r_on_orig = r_on;
    }

    public double getROff() {
        return r_off;
    }

    public void setROff(double r_off) {
        this.r_off = r_off;
        this.r_off_orig = r_off;
    }

    public void setROnMin(double r_on_min){
        this.r_on_min=r_on_min;
    }

    public void setROnMax(double r_on_max){
        this.r_on_max=r_on_max;
    }

    public double getROnMin(){
        return this.r_on_min;
    }

    public double getROnMax(){
        return this.r_on_max;
    }

    public void setB(double b){
        this.b=b;
    }

    public double getB(){
        return this.b;
    }

    public void setRho(double rho){
        this.rho=rho;
    }

    public double getRho(){
        return this.rho;
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

    public double getA() {
        return this.a;
    }

    public void setA(double a) {
        this.a = a;
    }

//    public double getB() {
//        return this.b;
//    }
//
//    public void setB(double b) {
//        this.b = b;
//    }
//
//    public void setG(double g) {
//        this.g = g;
//    }
//
//    public double getG() {
//        return this.g;
//    }
//
//    public void setD(double d) {
//        this.d = d;
//    }
//
//    public double getD() {
//        return this.d;
//    }
//
//    public void setL(double l) {
//        this.l = l;
//    }
//
//    public double getL() {
//        return this.l;
//    }
//
//    public void setE(double eta) {
//        this.e = eta;
//    }
//
//    public double getE() {
//        return this.e;
//    }

    public void setTau(double tau) {
        this.tau = tau;
    }

    public double getTau() {
        return this.tau;
    }

//    public void setType(int type) {
//        this.memtype = type;
//    }
//
//    public int getType() {
//        return this.memtype;
//    }

}