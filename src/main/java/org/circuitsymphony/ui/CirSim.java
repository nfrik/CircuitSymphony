package org.circuitsymphony.ui;

import org.circuitsymphony.Editable;
import org.circuitsymphony.element.CapacitorElm;
import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.element.active.ResistorElm;
import org.circuitsymphony.element.devices.TextElm;
import org.circuitsymphony.element.passive.InductorElm;
import org.circuitsymphony.element.passive.SwitchElm;
import org.circuitsymphony.engine.*;
import org.circuitsymphony.util.*;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

// For information about the theory behind this,
// see Electronic Circuit & System Simulation Methods by Pillage

public class CirSim extends Frame implements ComponentListener, ActionListener, AdjustmentListener, MouseMotionListener,
        MouseListener, ItemListener, CircuitEngineListener {

    public static final double PI = 3.14159265358979323846;
    public static final String muString = "μ";
    public static final String ohmString = "Ω";
    public static final String PROGRAM_NAME = "CircuitSymhony";
    private static final int MODE_ADD_ELM = 0;
    private static final int MODE_DRAG_ALL = 1;
    private static final int MODE_DRAG_ROW = 2;
    private static final int MODE_DRAG_COLUMN = 3;
    private static final int MODE_DRAG_SELECTED = 4;
    private static final int MODE_DRAG_POST = 5;
    private static final int MODE_SELECT = 6;
    private static final int infoWidth = 120;
    private static final int HINT_LC = 1;
    private static final int HINT_RC = 2;
    private static final int HINT_3DB_C = 3;
    private static final int HINT_TWINT = 4;
    private static final int HINT_3DB_L = 5;
    public static EditDialog editDialog;
    public static ImportDialog impDialog;
    public Dimension winSize;
    public Checkbox stoppedCheck;
    public CheckboxMenuItem conductanceCheckItem;
    public CheckboxMenuItem printableCheckItem;
    public MenuItem scopeSelectYMenuItem;
    public PopupMenu scopeMenu;
    public PopupMenu transScopeMenu;
    public CheckboxMenuItem scopeVMenuItem;
    public CheckboxMenuItem scopeIMenuItem;
    public CheckboxMenuItem scopeMaxMenuItem;
    public CheckboxMenuItem scopeMinMenuItem;
    public CheckboxMenuItem scopeFreqMenuItem;
    public CheckboxMenuItem scopePowerMenuItem;
    public CheckboxMenuItem scopeIbMenuItem;
    public CheckboxMenuItem scopeIcMenuItem;
    public CheckboxMenuItem scopeIeMenuItem;
    public CheckboxMenuItem scopeVbeMenuItem;
    public CheckboxMenuItem scopeVbcMenuItem;
    public CheckboxMenuItem scopeVceMenuItem;
    public CheckboxMenuItem scopeVIMenuItem;
    public CheckboxMenuItem scopeXYMenuItem;
    public CheckboxMenuItem scopeResistMenuItem;
    public CheckboxMenuItem scopeVceIcMenuItem;
    public int scopeSelected = -1;
    public CircuitElm dragElm;
    public CircuitElm mouseElm;
    public CircuitElm plotXElm, plotYElm;
    private Button resetButton;
    private Button dumpMatrixButton;
    private CheckboxMenuItem dotsCheckItem;
    private CheckboxMenuItem voltsCheckItem;
    private CheckboxMenuItem powerCheckItem;
    private CheckboxMenuItem showGridCheckItem;
    private CheckboxMenuItem smallGridCheckItem;
    private CheckboxMenuItem showValuesCheckItem;
    private CheckboxMenuItem euroResistorCheckItem;
    private CheckboxMenuItem conventionCheckItem;
    private Image dbimage;
    private Label titleLabel;
    private Label modeInfoLabel;
    private Label powerLabel;
    private MenuItem newItem;
    private MenuItem openItem;
    private MenuItem saveItem;
    private MenuItem saveAsItem;
    private MenuItem exitItem;
    private MenuItem undoItem;
    private MenuItem redoItem;
    private MenuItem cutItem;
    private MenuItem copyItem;
    private MenuItem pasteItem;
    private MenuItem mainMenuPasteItem;
    private MenuItem selectAllItem;
    private MenuItem optionsItem;
    private MenuItem nimbusSimulationItem;
    private MenuItem circuitElmListItem;
    private MenuItem aboutItem;
    private MenuItem shortcutItem;
    private Menu optionsMenu;
    private Scrollbar speedBar;
    private Scrollbar currentBar;
    private Scrollbar powerBar;
    private MenuItem elmEditMenuItem;
    private MenuItem elmCutMenuItem;
    private MenuItem elmCopyMenuItem;
    private MenuItem elmDeleteMenuItem;
    private MenuItem elmRotateMenuItem;
    private MenuItem elmFlipHorizontalMenuItem;
    private MenuItem elmFlipVerticalMenuItem;
    private MenuItem elmScopeMenuItem;
    private PopupMenu elmMenu;
    private PopupMenu mainMenu;
    private boolean analyzeFlag;
    private CircuitCanvas cv;
    private DrawContext drawContext;
    private Class<?> addingClass;
    private int mouseMode = MODE_SELECT;
    private int tempMouseMode = MODE_SELECT;
    private String mouseModeStr = "Select";
    private int dragX;
    private int dragY;
    private int initDragX;
    private int initDragY;
    private Rectangle selectedArea;
    private boolean dragging;
    private int pause = 10;
    private int menuScope = -1;
    private int hintType = -1;
    private int hintItem1;
    private int hintItem2;
    private Vector<String> setupList;
    private CircuitElm menuElm;
    private int mousePost = -1;
    private int draggingPost;
    private SwitchElm heldSwitchElm;

    private int scopeCount;
    private Scope[] scopes;
    private int[] scopeColCount;
    private ScopeExportDialog scopeExportDialog;
    private String clipboard;
    private Rectangle circuitArea;
    private int selectedItemIndex = 0;
    private Vector<String> undoStack;
    private Vector<String> redoStack;
    private SaveOpenDialog saveOpenDialog;
    private boolean circuitIsModified;
    private String startCircuit = null;
    private String startLabel = null;
    private String startCircuitText = null;
    private String baseURL = "https://github.com/nfrik/CircuitSymphony";
    private boolean shown = false;
    private long lastTime = 0;
    private long lastFrameTime;
    private long secTime = 0;

    private CircuitEngine engine;
    private CircuitLoader circuitLoader;

    public CirSim() {
        super();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                dispose();
            }
        });

        // Set program name at startup
        setTitleNameStart();

        // Set icons to frame
        ImageIcon img16 = new ImageIcon(getClass().getResource("/images/icon16.png"));
        ImageIcon img32 = new ImageIcon(getClass().getResource("/images/icon32.png"));
        List<Image> iconList = new ArrayList<>();
        iconList.add(img16.getImage());
        iconList.add(img32.getImage());
        setIconImages(iconList);

        try { // Set native look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {
            try { // Set Metal look and feel as alternative
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Metal".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void init(String initCircuit) throws Exception {
        engine = new CircuitEngine(this);

        circuitLoader = new CircuitLoader(engine, new CircuitLoaderListener() {
            @Override
            public void configureOptions(CircuitOptions options) {
                engine.timeStep = options.timeStep;
                dotsCheckItem.setState(options.dotsCheck);
                smallGridCheckItem.setState(options.smallGridCheck);
                powerCheckItem.setState(options.powerCheck);
                voltsCheckItem.setState(options.voltsCheck);
                showValuesCheckItem.setState(options.showValuesCheck);
                speedBar.setValue(options.speedBarValue);
                currentBar.setValue(options.currentBarValue);
                powerBar.setValue(options.powerBarValue);
                engine.voltageRange = options.volateRange;

                engine.updateIterationCount(options.speedBarValue);
                engine.updateGrid(smallGridCheckItem.getState());
                if (options.defaults) {
                    hintType = -1;
                    scopeCount = 0;
                }
            }

            @Override
            public void handleScope(StringTokenizer tokenizer) {
                Scope sc = new Scope(CirSim.this);
                sc.setPosition(scopeCount);
                sc.undump(tokenizer, true);
                scopes[scopeCount++] = sc;
            }

            @Override
            public void configureHints(int hintType, int hintItem1, int hintItem2) {
                CirSim.this.hintType = hintType;
                CirSim.this.hintItem1 = hintItem1;
                CirSim.this.hintItem2 = hintItem2;
            }

            @Override
            public void afterLoading(EnumSet<CircuitLoader.RetentionPolicy> retain) {
                enableItems();
                if (retain.isEmpty())
                    handleResize(); // for scopes
                needAnalyze();
            }
        });

        String ctrlMetaKey = (OsUtils.isMac()) ? "⌘" : "Ctrl";

        setLayout(new CircuitLayout());
        cv = new CircuitCanvas(this);
        cv.addComponentListener(this);
        cv.addMouseMotionListener(this);
        cv.addMouseListener(this);
        add(cv);
        drawContext = new DrawContext(this);

        keyboardInit();

        mainMenu = new PopupMenu();
        MenuBar mb = new MenuBar();
        Menu m = new Menu("File");
        mb.add(m);
        m.add(newItem = getMenuItem("New"));
        m.add(openItem = getMenuItem("Open..."));
        m.addSeparator();
        m.add(saveItem = getMenuItem("Save..."));
        m.add(saveAsItem = getMenuItem("Save As..."));
        m.addSeparator();
        m.add(exitItem = getMenuItem("Exit"));

        m = new Menu("Edit");
        m.add(undoItem = getMenuItem("Undo"));
        undoItem.setShortcut(new MenuShortcut(KeyEvent.VK_Z));
        m.add(redoItem = getMenuItem("Redo"));
        redoItem.setShortcut(new MenuShortcut(KeyEvent.VK_Z, true));
        m.addSeparator();
        m.add(cutItem = getMenuItem("Cut"));
        cutItem.setShortcut(new MenuShortcut(KeyEvent.VK_X));
        m.add(copyItem = getMenuItem("Copy"));
        copyItem.setShortcut(new MenuShortcut(KeyEvent.VK_C));
        m.add(pasteItem = getMenuItem("Paste"));
        pasteItem.setShortcut(new MenuShortcut(KeyEvent.VK_V));
        pasteItem.setEnabled(false);
        m.add(selectAllItem = getMenuItem("Select All"));
        selectAllItem.setShortcut(new MenuShortcut(KeyEvent.VK_A));
        mb.add(m);

        m = new Menu("Scope");
        mb.add(m);
        m.add(getMenuItem("Stack All", "stackAll"));
        m.add(getMenuItem("Unstack All", "unstackAll"));

        optionsMenu = m = new Menu("Options");
        mb.add(m);

        m.add(dotsCheckItem = getCheckItem("Show Current"));
        dotsCheckItem.setState(true);

        m.add(voltsCheckItem = getCheckItem("Show Voltage"));
        voltsCheckItem.setState(true);

        m.add(powerCheckItem = getCheckItem("Show Power"));

        m.add(showValuesCheckItem = getCheckItem("Show Values"));
        showValuesCheckItem.setState(true);

        m.add(showGridCheckItem = getCheckItem("Show Grid"));
        showGridCheckItem.setState(true);

        m.addSeparator();

        // m.add(conductanceCheckItem = getCheckItem("Show Conductance"));
        m.add(smallGridCheckItem = getCheckItem("Small Grid"));

        m.add(euroResistorCheckItem = getCheckItem("European Resistors"));
        euroResistorCheckItem.setState(false);

        m.add(printableCheckItem = getCheckItem("White Background"));
        printableCheckItem.setState(false);

        m.add(conventionCheckItem = getCheckItem("Conventional Current Motion"));
        conventionCheckItem.setState(true);

        m.add(optionsItem = getMenuItem("Other Options..."));

        Menu circuitsMenu = new Menu("Circuits");
        mb.add(circuitsMenu);

        Menu toolsMenu = new Menu("Tools");
        mb.add(toolsMenu);

        toolsMenu.add(nimbusSimulationItem = getMenuItem("Nimbus Simulation"));
        toolsMenu.add(circuitElmListItem = getMenuItem("Circuit elements"));

        Menu helpMenu = new Menu("Help");
        mb.add(helpMenu);

        helpMenu.add(aboutItem = getMenuItem("About CircuitSymphony"));
        helpMenu.add(shortcutItem = getMenuItem("Keyboard Shortcuts"));

        Menu passMenu = new Menu("Passive Components");
        Menu inputMenu = new Menu("Inputs/Outputs");
        Menu activeMenu = new Menu("Active Components");
        Menu gateMenu = new Menu("Logic Gates");
        Menu chipMenu = new Menu("Chips");
        Menu displayMenu = new Menu("Display Devices");
        Menu sdChipMenu = new Menu("CD Series");

        for (MappedElement element : circuitLoader.getMappedElements()) {
            Menu targetMenu = null;
            switch (element.category) {
                case MainMenu:
                    targetMenu = mainMenu;
                    break;
                case Passive:
                    targetMenu = passMenu;
                    break;
                case Active:
                    targetMenu = activeMenu;
                    break;
                case InputOutput:
                    targetMenu = inputMenu;
                    break;
                case LogicGate:
                    targetMenu = gateMenu;
                    break;
                case Chip:
                    targetMenu = chipMenu;
                    break;
                case CDSeries:
                    targetMenu = sdChipMenu;
                    break;
                case Display:
                    targetMenu = displayMenu;
                    break;
            }
            targetMenu.add(getClassCheckItem(element));
        }

        mainMenu.add(passMenu);
        mainMenu.add(inputMenu);
        mainMenu.add(activeMenu);
        mainMenu.add(gateMenu);
        mainMenu.add(chipMenu);
        mainMenu.add(displayMenu);
        mainMenu.add(sdChipMenu);

        Menu otherMenu = new Menu("Other");
        mainMenu.add(otherMenu);
        otherMenu.add(getCheckItem("Drag All (Alt-drag)", "DragAll"));
        otherMenu.add(getCheckItem(OsUtils.isMac() ? "Drag Row (Alt-S-drag, S-right)" : "Drag Row (S-right)", "DragRow"));
        otherMenu.add(getCheckItem(OsUtils.isMac() ? "Drag Column (Alt-⌘-drag, ⌘-right)" : "Drag Column (C-right)", "DragColumn"));
        otherMenu.add(getCheckItem("Drag Selected", "DragSelected"));
        otherMenu.add(getCheckItem("Drag Post (" + ctrlMetaKey + "-drag)", "DragPost"));
        mainMenu.addSeparator();
        mainMenu.add(mainMenuPasteItem = getMenuItem("Paste"));
        mainMenuPasteItem.setEnabled(false);
        mainMenu.add(getCheckItem("Select/Drag Selected (space or Shift-drag)", "Select"));
        add(mainMenu);

        add(resetButton = new Button("Reset"));
        resetButton.addActionListener(this);
        dumpMatrixButton = new Button("Dump Matrix");
        // main.add(dumpMatrixButton);
        dumpMatrixButton.addActionListener(this);
        stoppedCheck = new Checkbox("Stopped");
        stoppedCheck.addItemListener(this);
        add(stoppedCheck);

        add(new Label("Simulation Speed", Label.CENTER));

        // was max of 140 (260)
        add(speedBar = new Scrollbar(Scrollbar.HORIZONTAL, 3, 1, 0, 260));
        speedBar.addAdjustmentListener(this);

        add(new Label("Current Speed", Label.CENTER));
        currentBar = new Scrollbar(Scrollbar.HORIZONTAL, 50, 1, 1, 100);
        currentBar.addAdjustmentListener(this);
        add(currentBar);

        add(powerLabel = new Label("Power Brightness", Label.CENTER));
        add(powerBar = new Scrollbar(Scrollbar.HORIZONTAL, 50, 1, 1, 100));
        powerBar.addAdjustmentListener(this);

        powerBar.setEnabled(false);
        powerLabel.setEnabled(false);

        // main.add(new Label("CircuitSymphony"));

        Font f = new Font("SansSerif", Font.BOLD, 12);

        titleLabel = new Label("Untitled");
        titleLabel.setFont(f);

        modeInfoLabel = new Label("Select");
        modeInfoLabel.setFont(f);

        add(new Label(""));
        add(new Label("Current Circuit"));
        add(titleLabel);
        add(new Label("Current Mode"));
        add(modeInfoLabel);

        engine.updateGrid(smallGridCheckItem.getState());
        setupList = new Vector<>();
        undoStack = new Vector<>();
        redoStack = new Vector<>();

        scopes = new Scope[20];
        scopeColCount = new int[20];
        scopeCount = 0;

        cv.setBackground(Color.black);
        cv.setForeground(Color.lightGray);

        elmMenu = new PopupMenu();
        elmMenu.add(elmEditMenuItem = getMenuItem("Edit"));
        elmMenu.addSeparator();
        elmMenu.add(elmCutMenuItem = getMenuItem("Cut"));
        elmMenu.add(elmCopyMenuItem = getMenuItem("Copy"));
        elmMenu.add(elmDeleteMenuItem = getMenuItem("Delete"));
        elmMenu.addSeparator();
        // TODO: implement these functions
        // elmMenu.add(elmRotateMenuItem = getMenuItem("Rotate"));
        // elmMenu.add(elmFlipVerticalMenuItem = getMenuItem("Flip Vertical"));
        // elmMenu.add(elmFlipHorizontalMenuItem = getMenuItem("Flip
        // Horizontal"));
        // elmRotateMenuItem.setEnabled(false);
        // elmFlipVerticalMenuItem.setEnabled(false);
        // elmFlipHorizontalMenuItem.setEnabled(false);
        // elmMenu.addSeparator();
        elmMenu.add(elmScopeMenuItem = getMenuItem("View in Scope"));
        add(elmMenu);

        scopeMenu = buildScopeMenu(false);
        transScopeMenu = buildScopeMenu(true);

        getSetupList(circuitsMenu);

        // Init SaveOpenDialog
        saveOpenDialog = new SaveOpenDialog(this);
        circuitIsModified = false;

        scopeExportDialog = new ScopeExportDialog(this);
        setMenuBar(mb);

        if (initCircuit != null) {
            loadStartup(initCircuit);
        } else if (startCircuitText != null)
            circuitLoader.loadCircuit(startCircuitText);
        else if (engine.getStopMessage() == null && startCircuit != null)
            readSetupFile(startCircuit, startLabel);
    }

    public void setFrameAndShow() {
        handleResize();
        setSize(860, 640);
        setLocationByPlatform(true);
        setVisible(true);

        // Start maximized
        // setExtendedState( getExtendedState()|MAXIMIZED_BOTH );

        requestFocus();
    }

    private PopupMenu buildScopeMenu(boolean t) {
        PopupMenu m = new PopupMenu();
        m.add(getMenuItem("Remove", "remove"));
        m.add(getMenuItem("Speed 2x", "speed2"));
        m.add(getMenuItem("Speed 1/2x", "speed1/2"));
        m.add(getMenuItem("Scale 2x", "scale"));
        m.add(getMenuItem("Max Scale", "maxscale"));
        m.add(getMenuItem("Stack", "stack"));
        m.add(getMenuItem("Unstack", "unstack"));
        m.add(getMenuItem("Reset", "reset"));
        if (t) {
            m.add(scopeIbMenuItem = getCheckItem("Show Ib"));
            m.add(scopeIcMenuItem = getCheckItem("Show Ic"));
            m.add(scopeIeMenuItem = getCheckItem("Show Ie"));
            m.add(scopeVbeMenuItem = getCheckItem("Show Vbe"));
            m.add(scopeVbcMenuItem = getCheckItem("Show Vbc"));
            m.add(scopeVceMenuItem = getCheckItem("Show Vce"));
            m.add(scopeVceIcMenuItem = getCheckItem("Show Vce vs Ic"));
        } else {
            m.add(scopeVMenuItem = getCheckItem("Show Voltage"));
            m.add(scopeIMenuItem = getCheckItem("Show Current"));
            m.add(scopePowerMenuItem = getCheckItem("Show Power Consumed"));
            m.add(scopeMaxMenuItem = getCheckItem("Show Peak Value"));
            m.add(scopeMinMenuItem = getCheckItem("Show Negative Peak Value"));
            m.add(scopeFreqMenuItem = getCheckItem("Show Frequency"));
            m.add(scopeVIMenuItem = getCheckItem("Show V vs I"));
            m.add(scopeXYMenuItem = getCheckItem("Plot X/Y"));
            m.add(scopeSelectYMenuItem = getMenuItem("Select Y", "selecty"));
            m.add(scopeResistMenuItem = getCheckItem("Show Resistance"));
        }
        m.add(getMenuItem("Export Data", "exportData"));
        add(m);
        return m;
    }

    private MenuItem getMenuItem(String label) {
        MenuItem mi = new MenuItem(label);
        mi.addActionListener(this);
        return mi;
    }

    private MenuItem getMenuItem(String label, String actionCommand) {
        MenuItem mi = new MenuItem(label);
        mi.setActionCommand(actionCommand);
        mi.addActionListener(this);
        return mi;
    }

    private CheckboxMenuItem getCheckItem(String label) {
        CheckboxMenuItem mi = new CheckboxMenuItem(label);
        mi.addItemListener(this);
        mi.setActionCommand("");
        return mi;
    }

    private CheckboxMenuItem getClassCheckItem(MappedElement element) {
        String label = element.menuString;
        try {
            CircuitElm elm = CirSimUtil.constructElement(engine, element.clazz, 0, 0);
            if (elm.needsShortcut() && elm.getDumpClass() == element.clazz) {
                int dt = elm.getDumpType();
                label += " (" + (char) dt + ")";
            }
            elm.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getCheckItem(label, element.clazz.getSimpleName());
    }

    private CheckboxMenuItem getCheckItem(String label, String actionCommand) {
        CheckboxMenuItem mi = new CheckboxMenuItem(label);
        mi.addItemListener(this);
        mi.setActionCommand(actionCommand);
        return mi;
    }

    private void handleResize() {
        winSize = cv.getSize();
        try {
            dbimage = createImage(winSize.width, winSize.height);
        } catch (Exception e) {
            cv.setSize(getMinimumSize());
            winSize = cv.getSize();
            dbimage = createImage(winSize.width, winSize.height);
        }
        int h = winSize.height / 5;
        /*
         * if (h < 128 && winSize.height > 300) h = 128;
         */
        circuitArea = new Rectangle(0, 0, winSize.width, winSize.height - h);
        cv.circuitAreaChanged();
        // after moving elements, need this to avoid singular matrix probs
        needAnalyze();
    }

    @Override
    protected void processEvent(AWTEvent e) {
        if (e.getID() == Event.WINDOW_DESTROY) {
            doExit();
        } else {
            super.processEvent(e);
        }
    }

    @Override
    public void paint(Graphics g) {
        cv.repaint();
    }

    public CircuitEngine getCircuitEngine() {
        return engine;
    }

    public CircuitLoader getCircuitLoader() {
        return circuitLoader;
    }

    private Vector<CircuitElm> getElmList() {
        return engine.elmList;
    }

    private Vector<CircuitNode> getNodeList() {
        return engine.nodeList;
    }

    public boolean isSimulationStopped() {
        return stoppedCheck.getState();
    }

    public void updateCircuit(Graphics realg) throws Exception {
        CircuitElm realMouseElm;
        if (winSize == null || winSize.width == 0)
            return;
        if (analyzeFlag) {
            engine.analyzeCircuit();
            analyzeFlag = false;
        }
        if (editDialog != null && editDialog.getElm() instanceof CircuitElm)
            mouseElm = (CircuitElm) (editDialog.getElm());
        realMouseElm = mouseElm;
        if (mouseElm == null)
            mouseElm = engine.getStopElement();
        setupScopes();
        Graphics g;
        g = dbimage.getGraphics();
        drawContext.begin(g);
        Color gridColor = new Color(10, 36, 36);
        drawContext.selectColor = Color.cyan;
        if (printableCheckItem.getState()) {
            drawContext.whiteColor = Color.black;
            drawContext.lightGrayColor = Color.black;
            gridColor = Color.yellow;
            g.setColor(Color.white);
        } else {
            drawContext.whiteColor = Color.white;
            drawContext.lightGrayColor = Color.lightGray;
            g.setColor(Color.black);
        }
        g.fillRect(0, 0, winSize.width, winSize.height);
        if (!stoppedCheck.getState()) {
            try {
                engine.runCircuit();
            } catch (Exception e) {
                e.printStackTrace();
                analyzeFlag = true;
                cv.repaint();
                return;
            }
        }
        if (!stoppedCheck.getState()) {
            long sysTime = System.currentTimeMillis();
            if (lastTime != 0) {
                int inc = (int) (sysTime - lastTime);
                double c = currentBar.getValue();
                c = Math.exp(c / 3.5 - 14.2);
                drawContext.currentMult = 1.7 * inc * c;
                if (!conventionCheckItem.getState())
                    drawContext.currentMult = -drawContext.currentMult;
            }
            if (sysTime - secTime >= 1000) {
                secTime = sysTime;
            }
            lastTime = sysTime;
        } else
            lastTime = 0;
        drawContext.powerMult = Math.exp(powerBar.getValue() / 4.762 - 7);

        if (showGridCheckItem.getState()) {
            paintGrid(g, gridColor);
        }

        int i;
        Font oldfont = g.getFont();
        for (i = 0; i != getElmList().size(); i++) {
            if (powerCheckItem.getState())
                g.setColor(Color.gray);
            /*
             * else if (conductanceCheckItem.getState())
             * g.setColor(Color.white);
             */
            getElm(i).draw(drawContext);
        }
        if (tempMouseMode == MODE_DRAG_ROW || tempMouseMode == MODE_DRAG_COLUMN
                || tempMouseMode == MODE_DRAG_POST
                || tempMouseMode == MODE_DRAG_SELECTED)
            for (i = 0; i != getElmList().size(); i++) {
                CircuitElm ce = getElm(i);
                ce.drawPost(drawContext, ce.x, ce.y);
                ce.drawPost(drawContext, ce.x2, ce.y2);
            }
        int badnodes = 0;
        // find bad connections, nodes not connected to other elements which
        // intersect other elements' bounding boxes
        for (i = 0; i != getNodeList().size(); i++) {
            CircuitNode cn = getCircuitNode(i);
            if (!cn.isInternal() && cn.getLinks().size() == 1) {
                int bb = 0, j;
                CircuitNodeLink cnl = cn.getLinks().elementAt(0);
                for (j = 0; j != getElmList().size(); j++)
                    if (cnl.getElm() != getElm(j) && getElm(j).boundingBox.contains(cn.getX(), cn.getY()))
                        bb++;
                if (bb > 0) {
                    g.setColor(Color.red);
                    Point screenPos = cv.project(cn.getX() - 3, cn.getY() - 3);
                    g.fillOval(screenPos.x, screenPos.y, (int) (7 / cv.getCameraZoom()), (int) (7 / cv.getCameraZoom()));
                    badnodes++;
                }
            }
        }
        /*
         * if (mouseElm != null) { g.setFont(oldfont); g.drawString("+",
         * mouseElm.x+10, mouseElm.y); }
         */
        if (dragElm != null && (dragElm.x != dragElm.x2 || dragElm.y != dragElm.y2))
            dragElm.draw(drawContext);
        g.setFont(oldfont);
        int ct = scopeCount;
        if (engine.getStopMessage() != null)
            ct = 0;
        for (i = 0; i != ct; i++)
            scopes[i].draw(drawContext, g);
        g.setColor(drawContext.whiteColor);
        if (engine.getStopMessage() != null) {
            g.drawString(engine.getStopMessage(), 10, circuitArea.height);
        } else {
            String info[] = new String[10];
            if (mouseElm != null) {
                if (mousePost == -1)
                    mouseElm.getInfo(drawContext, info);
                else
                    info[0] = "V = " + drawContext.getUnitText(mouseElm.getPostVoltage(mousePost), "V");
                /*
                 * //shownodes for (i = 0; i != mouseElm.getPostCount(); i++)
                 * info[0] += " " + mouseElm.nodes[i]; if
                 * (mouseElm.getVoltageSourceCount() > 0) info[0] += ";" +
                 * (mouseElm.getVoltageSource()+nodeList.size());
                 */

            } else {
                drawContext.getShowFormat().setMinimumFractionDigits(2);
                info[0] = "t = " + drawContext.getUnitText(engine.t, "s");
                drawContext.getShowFormat().setMinimumFractionDigits(0);
            }
            if (hintType != -1) {
                for (i = 0; info[i] != null; i++)
                    ;
                String s = getHint();
                if (s == null)
                    hintType = -1;
                else
                    info[i] = s;
            }
            int x = 0;
            if (ct != 0)
                x = scopes[ct - 1].rightEdge() + 20;
            x = Math.max(x, winSize.width * 2 / 3);

            // count lines of data
            for (i = 0; info[i] != null; i++)
                ;
            if (badnodes > 0)
                info[i++] = badnodes + ((badnodes == 1) ? " bad connection"
                        : " bad connections");

            // find where to show data
            int ybase = winSize.height - 15 * i - 5;
            ybase = Math.min(ybase, circuitArea.height);
            for (i = 0; info[i] != null; i++)
                g.drawString(info[i], x, ybase + 15 * (i + 1));
        }
        if (selectedArea != null) {
            g.setColor(drawContext.selectColor);
            Point screenPos = cv.project(selectedArea.x, selectedArea.y);
            g.drawRect(screenPos.x, screenPos.y, (int) (selectedArea.width / cv.getCameraZoom()), (int) (selectedArea.height / cv.getCameraZoom()));
        }
        mouseElm = realMouseElm;
        /*
         * g.setColor(Color.white); g.drawString("Framerate: " + framerate, 10,
         * 10); g.drawString("Steprate: " + steprate, 10, 30); g.drawString(
         * "Steprate/iter: " + (steprate/getIterCount()), 10, 50); g.drawString(
         * "iterc: " + (getIterCount()), 10, 70);
         */
        drawContext.end();

        Graphics2D g2dd = (Graphics2D) realg.create();
        g2dd.drawImage(dbimage, g2dd.getTransform(), this);
        g2dd.dispose();

//        realg.drawImage(dbimage, 0, 0, this);
        if (!stoppedCheck.getState() && engine.isCircuitMatrixNull() == false) {
            // Limit to 50 fps (thanks to Jürgen Klötzer for this)
            long delay = 1000 / 50
                    - (System.currentTimeMillis() - lastFrameTime);
            // realg.drawString("delay: " + delay, 10, 90);
            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                }
            }

            cv.repaint(0);
        }
        lastFrameTime = lastTime;
    }

    private void paintGrid(Graphics g, Color gridColor) {
        if (smallGridCheckItem.getState()) { // old drawing method for small grid, remove later when small grid is not needed
            float dashes[] = {1.0f};
            BasicStroke dashedStroke = new BasicStroke(0.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, dashes, 0.0f);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(gridColor);
            BasicStroke oldStroke = (BasicStroke) g2d.getStroke();

            for (int gx = 0; gx <= circuitArea.width; gx += 16) {
                if (gx % 16 == 0)
                    g2d.setStroke(oldStroke);
                else
                    g2d.setStroke(dashedStroke);
                Point screenPos = cv.project(gx, 0);
                g2d.drawLine(screenPos.x, 0, screenPos.x, circuitArea.height - 5);
            }

            for (int gy = 0; gy <= circuitArea.height; gy += 16) {
                if (gy % 16 == 0)
                    g2d.setStroke(oldStroke);
                else
                    g2d.setStroke(dashedStroke);
                Point screenPos = cv.project(0, gy);
                g2d.drawLine(0, screenPos.y, circuitArea.width, screenPos.y);
            }
            g2d.dispose();
            return;
        }
        cv.paintGird(g, gridColor);
    }

    private void setupScopes() {
        int i;

        // check scopes to make sure the elements still exist, and remove
        // unused scopes/columns
        int pos = -1;
        for (i = 0; i < scopeCount; i++) {
            if (locateElm(scopes[i].getElm()) < 0)
                scopes[i].setElm(null);
            if (scopes[i].getElm() == null) {
                int j;
                for (j = i; j != scopeCount; j++)
                    scopes[j] = scopes[j + 1];
                scopeCount--;
                i--;
                continue;
            }
            if (scopes[i].getPosition() > pos + 1)
                scopes[i].setPosition(pos + 1);
            pos = scopes[i].getPosition();
        }
        while (scopeCount > 0 && scopes[scopeCount - 1].getElm() == null)
            scopeCount--;
        int h = winSize.height - circuitArea.height;
        pos = 0;
        for (i = 0; i != scopeCount; i++)
            scopeColCount[i] = 0;
        for (i = 0; i != scopeCount; i++) {
            pos = Math.max(scopes[i].getPosition(), pos);
            scopeColCount[scopes[i].getPosition()]++;
        }
        int colct = pos + 1;
        int iw = infoWidth;
        if (colct <= 2)
            iw = iw * 3 / 2;
        int w = (winSize.width - iw) / colct;
        int marg = 10;
        if (w < marg * 2)
            w = marg * 2;
        pos = -1;
        int colh = 0;
        int row = 0;
        int speed = 0;
        for (i = 0; i != scopeCount; i++) {
            Scope s = scopes[i];
            if (s.getPosition() > pos) {
                pos = s.getPosition();
                colh = h / scopeColCount[pos];
                row = 0;
                speed = s.getSpeed();
            }
            if (s.getSpeed() != speed) {
                s.setSpeed(speed);
                s.resetGraph();
            }
            Rectangle r = new Rectangle(pos * w,
                    winSize.height - h + colh * row, w - marg, colh);
            row++;
            if (!r.equals(s.getRect()))
                s.setRect(r);
        }
    }

    private String getHint() {
        CircuitElm c1 = getElm(hintItem1);
        CircuitElm c2 = getElm(hintItem2);
        if (c1 == null || c2 == null)
            return null;
        if (hintType == HINT_LC) {
            if (!(c1 instanceof InductorElm))
                return null;
            if (!(c2 instanceof CapacitorElm))
                return null;
            InductorElm ie = (InductorElm) c1;
            CapacitorElm ce = (CapacitorElm) c2;
            return "res.f = "
                    + drawContext.getUnitText(
                    1 / (2 * PI
                            * Math.sqrt(
                            ie.getInductance() * ce.getCapacitance())),
                    "Hz");
        }
        if (hintType == HINT_RC) {
            if (!(c1 instanceof ResistorElm))
                return null;
            if (!(c2 instanceof CapacitorElm))
                return null;
            ResistorElm re = (ResistorElm) c1;
            CapacitorElm ce = (CapacitorElm) c2;
            return "RC = " + drawContext.getUnitText(re.getResistance() * ce.getCapacitance(), "s");
        }
        if (hintType == HINT_3DB_C) {
            if (!(c1 instanceof ResistorElm))
                return null;
            if (!(c2 instanceof CapacitorElm))
                return null;
            ResistorElm re = (ResistorElm) c1;
            CapacitorElm ce = (CapacitorElm) c2;
            return "f.3db = " + drawContext.getUnitText(1 / (2 * PI * re.getResistance() * ce.getCapacitance()), "Hz");
        }
        if (hintType == HINT_3DB_L) {
            if (!(c1 instanceof ResistorElm))
                return null;
            if (!(c2 instanceof InductorElm))
                return null;
            ResistorElm re = (ResistorElm) c1;
            InductorElm ie = (InductorElm) c2;
            return "f.3db = " + drawContext.getUnitText(re.getResistance() / (2 * PI * ie.getInductance()), "Hz");
        }
        if (hintType == HINT_TWINT) {
            if (!(c1 instanceof ResistorElm))
                return null;
            if (!(c2 instanceof CapacitorElm))
                return null;
            ResistorElm re = (ResistorElm) c1;
            CapacitorElm ce = (CapacitorElm) c2;
            return "fc = " + drawContext.getUnitText(1 / (2 * PI * re.getResistance() * ce.getCapacitance()), "Hz");
        }
        return null;
    }

    public void toggleSwitch(int n) {
        int i;
        for (i = 0; i != getElmList().size(); i++) {
            CircuitElm ce = getElm(i);
            if (ce instanceof SwitchElm) {
                n--;
                if (n == 0) {
                    ((SwitchElm) ce).toggle();
                    analyzeFlag = true;
                    cv.repaint();
                    return;
                }
            }
        }
    }

    public void needAnalyze() {
        analyzeFlag = true;
        cv.repaint();
    }

    public CircuitNode getCircuitNode(int n) {
        return engine.getCircuitNode(n);
    }

    public CircuitElm getElm(int n) {
        return engine.getElm(n);
    }

    @Override
    public void stop(String cause, CircuitElm ce) {
        stoppedCheck.setState(true);
        analyzeFlag = false;
        cv.repaint();
    }

    @Override
    public long getLastFrameTime() {
        return lastFrameTime;
    }

    @Override
    public void updateScopes() {
        for (int i = 0; i != scopeCount; i++)
            scopes[i].timeStep();
    }

    @Override
    public CircuitElm getDraggedElement() {
        return dragElm;
    }

    @Override
    public boolean isUISupported() {
        return true;
    }

    @Override
    public void createUI(Component comp) {
        add(comp);
        validate();
    }

    @Override
    public void removeUI(Component comp) {
        remove(comp);
    }

    @Override
    public void setAnalyzeFlag() {
        analyzeFlag = true;
    }

    private boolean isAnyElementIdNonZero() {
        for (CircuitElm e : engine.elmList) {
            if (e.flags2 != 0) return true;
        }
        return false;
    }

    public void editFuncPoint() {
        cv.repaint(pause);
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
        cv.repaint();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        handleResize();
        cv.repaint(100);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String ac = e.getActionCommand();
        if (e.getSource() == resetButton) {
            int i;

            // on IE, drawImage() stops working inexplicably every once in
            // a while. Recreating it fixes the problem, so we do that here.
            dbimage = createImage(winSize.width, winSize.height);

            for (i = 0; i != getElmList().size(); i++)
                getElm(i).reset();
            for (i = 0; i != scopeCount; i++)
                scopes[i].resetGraph();
            analyzeFlag = true;
            engine.t = 0;
            stoppedCheck.setState(false);
            cv.repaint();
        }
        if (e.getSource() == dumpMatrixButton)
            engine.setDumpMatrix(true);
        if (e.getSource() == newItem)
            doNew();
        if (e.getSource() == openItem)
            doOpen();
        if (e.getSource() == saveItem)
            doSave();
        if (e.getSource() == saveAsItem)
            doSaveAs();
        if (e.getSource() == optionsItem)
            doEdit(new EditOptions(this));
        if (e.getSource() == undoItem)
            doUndo();
        if (e.getSource() == redoItem)
            doRedo();
        if (e.getSource() == nimbusSimulationItem)
            new NimbusSimulationDialog(this);
        if (e.getSource() == circuitElmListItem)
            new CircuitElmListDialog(this);
        if (e.getSource() == aboutItem)
            doAbout();
        if (e.getSource() == shortcutItem)
            doShowShortcuts();
        if (ac.compareTo("Cut") == 0) {
            if (e.getSource() != elmCutMenuItem)
                menuElm = null;
            doCut();
        }
        if (ac.compareTo("Copy") == 0) {
            if (e.getSource() != elmCopyMenuItem)
                menuElm = null;
            doCopy();
        }
        if (ac.compareTo("Paste") == 0)
            doPaste();
        if (e.getSource() == selectAllItem)
            doSelectAll();
        if (e.getSource() == exitItem) {
            doExit();
        }
        if (ac.compareTo("stackAll") == 0)
            stackAll();
        if (ac.compareTo("unstackAll") == 0)
            unstackAll();
        if (e.getSource() == elmEditMenuItem)
            doEdit(menuElm);
        if (ac.compareTo("Delete") == 0) {
            if (e.getSource() != elmDeleteMenuItem)
                menuElm = null;
            doDelete();
        }
        if (e.getSource() == elmScopeMenuItem && menuElm != null) {
            addToScope(menuElm);
        }
        if (e.getSource() == elmRotateMenuItem && menuElm != null) {
            doRotate(menuElm);
        }
        if (e.getSource() == elmFlipVerticalMenuItem && menuElm != null) {
            doFlipVertical(menuElm);
        }
        if (e.getSource() == elmFlipHorizontalMenuItem && menuElm != null) {
            doFlipHorizontal(menuElm);
        }
        if (menuScope != -1) {
            if (ac.compareTo("remove") == 0)
                scopes[menuScope].setElm(null);
            if (ac.compareTo("speed2") == 0)
                scopes[menuScope].speedUp();
            if (ac.compareTo("speed1/2") == 0)
                scopes[menuScope].slowDown();
            if (ac.compareTo("scale") == 0)
                scopes[menuScope].adjustScale(.5);
            if (ac.compareTo("maxscale") == 0)
                scopes[menuScope].adjustScale(1e-50);
            if (ac.compareTo("stack") == 0)
                stackScope(menuScope);
            if (ac.compareTo("unstack") == 0)
                unstackScope(menuScope);
            if (ac.compareTo("selecty") == 0)
                scopes[menuScope].selectY();
            if (ac.compareTo("reset") == 0)
                scopes[menuScope].resetGraph();
            if (ac.compareTo("exportData") == 0) {
                Scope mouseOverScope = scopes[menuScope];
                ArrayList<Scope> stackedScopes = new ArrayList<>();
                for (Scope s : scopes) {
                    if (s == null) continue;
                    if (s.getPosition() == mouseOverScope.getPosition()) {
                        stackedScopes.add(s);
                    }
                }
                scopeExportDialog.begin(this, stackedScopes, scopes[menuScope]);
            }
            cv.repaint();
        }
        if (ac.indexOf("setup ") == 0) {
            int res = saveIfModified();
            if (res != 2) {
                try {
                    readSetupFile(ac.substring(6), ((MenuItem) e.getSource()).getLabel());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void addToScope(CircuitElm elmToAdd) {
        if (elmToAdd == null) return;
        int i;
        for (i = 0; i != scopeCount; i++)
            if (scopes[i].getElm() == null)
                break;
        if (i == scopeCount) {
            if (scopeCount == scopes.length)
                return;
            scopeCount++;
            scopes[i] = new Scope(this);
            scopes[i].setPosition(i);
            handleResize();
        }
        scopes[i].setElm(elmToAdd);
    }

    private void stackScope(int s) {
        if (s == 0) {
            if (scopeCount < 2)
                return;
            s = 1;
        }
        if (scopes[s].getPosition() == scopes[s - 1].getPosition())
            return;
        scopes[s].setPosition(scopes[s - 1].getPosition());
        for (s++; s < scopeCount; s++)
            scopes[s].setPosition(scopes[s].getPosition() - 1);
    }

    private void unstackScope(int s) {
        if (s == 0) {
            if (scopeCount < 2)
                return;
            s = 1;
        }
        if (scopes[s].getPosition() != scopes[s - 1].getPosition())
            return;
        for (; s < scopeCount; s++)
            scopes[s].setPosition(scopes[s].getPosition() + 1);
    }

    private void stackAll() {
        int i;
        for (i = 0; i != scopeCount; i++) {
            scopes[i].setPosition(0);
            scopes[i].setShowMax(false);
            scopes[i].setShowMin(false);
        }
    }

    private void unstackAll() {
        int i;
        for (i = 0; i != scopeCount; i++) {
            scopes[i].setPosition(i);
            scopes[i].setShowMax(true);
        }
    }

    public void editCircuitElm(CircuitElm elm) {
        if (elm.getEditInfo(0) == null) return;
        doEdit(elm);
    }

    private void doEdit(Editable eable) {
        clearSelection();
        // pushUndo();
        if (editDialog != null) {
            requestFocus();
            editDialog.setVisible(false);
            editDialog = null;
        }
        editDialog = new EditDialog(eable, this);
        // editDialog.setModal(true);
        if (eable instanceof EditOptions)
            editDialog.setTitle("Edit Other Options");
        editDialog.setVisible(true);
    }

    private void doRotate(CircuitElm elm) {
        System.out.println(elm.dump(true));
    }

    private void doFlipVertical(CircuitElm elm) {
        System.out.println(elm.dump(true));
    }

    private void doFlipHorizontal(CircuitElm elm) {
        System.out.println(elm.dump(true));
    }

    // Disable frame and show About.
    private void doAbout() {
        // setEnabled(false);
        new AboutFrame(this);
    }

    ///////////////////
    // About methods //
    ///////////////////

    private void doShowShortcuts() {
        // setEnabled(false);
        new ShortcutsFrame(this);
    }

    // Set time to 0.
    private void resetTime() {
        engine.t = 0;
    }

    //////////////////
    // Time methods //
    //////////////////

    // Set title at start.
    private void setTitleNameStart() {
        setTitle("Untitled - " + PROGRAM_NAME);
    }

    ///////////////////
    // Title methods //
    ///////////////////

    private void setTitleName(String s) {
        setTitle(s + " - " + PROGRAM_NAME);
    }

    ///////////////////////////
    // Warning/Error Dialogs //
    ///////////////////////////
    private void showSetupReadDumpWarningDialog() {
        Object[] options = {"   Ok   "};
        JOptionPane.showOptionDialog(this,
                "Warning. Some elements could not be read.\n"
                        + "Make sure CircuitSymphony is up to date.",
                "Warning", JOptionPane.PLAIN_MESSAGE,
                JOptionPane.WARNING_MESSAGE, null, options, options[0]);
    }

    private void showSetupReadErrorDialog() {
        Object[] options = {"   Ok   "};
        JOptionPane.showOptionDialog(this, "ERROR. The file cannot be opened.",
                "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE,
                null, options, options[0]);
    }

    private void loadStartup(String s1) {
        // Get circuit from path s1.
        String s = saveOpenDialog.load(s1);

        // Load circuit
        load(s);
    }

    ////////////////////////////
    // SaveOpenDialog methods //
    ////////////////////////////

    // Load a circuit (when user clicks on .cmf file).
    private void load(String s) {
        if (s != null) {
            // Store the current circuit
            String aux = dumpCircuit(true);

            // Try to read loaded circuit.
            CircuitLoadResult status = circuitLoader.loadCircuit(s, saveOpenDialog.getCurrentFileNameExtension());

            // Check if the read file is ok to load.
            if (status == CircuitLoadResult.OK || status == CircuitLoadResult.DUMP_WARNING) {
                afterCircuitLoaded(saveOpenDialog.getFileName());
            }

            // If the circuit had a dump warning, tell the user.
            if (status == CircuitLoadResult.DUMP_WARNING) {
                showSetupReadDumpWarningDialog();
            }

            // If the circuit can't be load, tell the user and
            // reload current circuit.
            else if (status == CircuitLoadResult.ERROR) {
                circuitLoader.loadCircuit(aux, false, true);

                showSetupReadErrorDialog();
            }
        }
    }

    private void afterCircuitLoaded(String fileName) {
        clearUndoRedo();
        resetTime();
        circuitIsModified = false;
        if (fileName.lastIndexOf('.') != -1) {
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        }
        setTitleName(fileName);
        titleLabel.setText(fileName);
    }

    // Erase current circuit; we load "blankCircuit" and
    // ask if circuit needs saving.
    private void doNew() {
        // Check if circuit needs saving.
        int n = saveIfModified();

        // Only create new if operation wasn't cancelled.
        if (n != 2) {
            circuitIsModified = false;
            deleteAll();
            clearUndoRedo();
            resetTime();
            titleLabel.setText("Untitled");
            setTitleNameStart();
        }
    }

    // Open a circuit.
    private void doOpen() {
        // Check if circuit needs saving.
        int n = saveIfModified();

        // Only open if operation wasn't cancelled.
        if (n != 2) {
            // Get path from file.
            String s = saveOpenDialog.open();

            // Load circuit into program if not null.
            if (s != null) {
                load(s);
            }
        }
    }

    // Save circuit.
    private boolean doSave() {
        // We readSetup the current circuit so it's positioned in the center.
        circuitLoader.loadCircuit(dumpCircuit(true), false, true);
        if (saveOpenDialog.save(dumpCircuit(false), dumpCircuit(true),
                () -> engine.dumpAsGraphString(dumpFlags(), currentBar.getValue(), powerBar.getValue()),
                isAnyElementIdNonZero())) {
            circuitIsModified = false; // Circuit has been saved.
            String txt = saveOpenDialog.getFileName();
            txt = txt.substring(0, txt.lastIndexOf('.'));
            setTitleName(txt);
            return true;
        }

        return false;
    }

    // Save circuit as.
    private void doSaveAs() {
        // We readSetup the current circuit so it's positioned correctly.
        circuitLoader.loadCircuit(dumpCircuit(true), false, true);

        if (saveOpenDialog.saveAs(dumpCircuit(false), dumpCircuit(true),
                () -> engine.dumpAsGraphString(dumpFlags(), currentBar.getValue(), powerBar.getValue()),
                isAnyElementIdNonZero())) {
            circuitIsModified = false; // Circuit has been saved.
            String txt = saveOpenDialog.getFileName();
            txt = txt.substring(0, txt.lastIndexOf('.'));
            setTitleName(txt);
        }
    }

    // Check if circuit has been modified
    private boolean circuitIsModified() {
        return circuitIsModified;
    }

    // Checks if circuit needs saving. Returns 0 if saved; 1 if not saved; 2 if
    // cancelled.
    private int saveIfModified() {
        // Check if the circuit has been modified and needs saving
        if (circuitIsModified()) {
            // Custom button text
            Object[] options = {"Yes", "No", "Cancel"};

            int n = JOptionPane.showOptionDialog(null, "Save changes?",
                    "CircuitSymphony: " + titleLabel.getText().trim(),
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (n == 0) {
                if (doSave()) {
                    return 0;
                }
            }

            if (n == 1) {
                return 1;
            }

            return 2;
        }
        return 0;
    }

    // Exit the program.
    private void doExit() {
        // Check if circuit needs saving
        int n = saveIfModified();

        // Only exit if opertion wasn't cancelled.
        if (n != 2) {
            dispose();
            System.exit(0);
        }
    }

    private void readSetupFile(String str, String title) throws Exception {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(JarUtils.getJarPath(CirSim.class) + "circuits/" + str));
            circuitLoader.loadCircuit(bytes, bytes.length, false);
            afterCircuitLoaded(title);
        } catch (Exception e) {
            e.printStackTrace();
            engine.stop("Unable to read " + str + "!", null);
        }
    }

    private void doImport(boolean imp, boolean url) {
        if (impDialog != null) {
            requestFocus();
            impDialog.setVisible(false);
            impDialog = null;
        }
        String dump = (imp) ? "" : dumpCircuit(true);
        if (url)
            try {
                dump = baseURL + "#" + URLEncoder.encode(dump, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        impDialog = new ImportDialog(this, dump, url);
        impDialog.setVisible(true);
        pushUndo();
    }


    private int dumpFlags() {
        int f = (dotsCheckItem.getState()) ? 1 : 0;
        f |= (smallGridCheckItem.getState()) ? 2 : 0;
        f |= (voltsCheckItem.getState()) ? 0 : 4;
        f |= (powerCheckItem.getState()) ? 8 : 0;
        f |= (showValuesCheckItem.getState()) ? 0 : 16;
        // 32 = linear scale in afilter
        return f;
    }

    public String dumpCircuit(boolean newFormat) {
        int f = dumpFlags();
        StringBuilder dump = new StringBuilder("$ " + f + " " + engine.timeStep + " " + engine.getIterationCount() + " "
                + currentBar.getValue() + " " + engine.voltageRange + " " + powerBar.getValue() + "\n");
        engine.dumpElements(dump, newFormat);
        for (int i = 0; i != scopeCount; i++) {
            String d = scopes[i].dump();
            if (d != null) {
                dump.append(d).append("\n");
            }
        }
        if (hintType != -1) {
            dump.append("h ").append(hintType).append(" ").append(hintItem1).append(" ").append(hintItem2).append("\n");
        }
        return dump.toString();
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (e.getSource() == speedBar) {
            engine.updateIterationCount(speedBar.getValue());
        }
//        System.out.print(((Scrollbar) e.getSource()).getValue() + "\n");
    }

    private void getSetupList(Menu menu) throws Exception {
        Menu stack[] = new Menu[6];
        int stackptr = 0;
        stack[stackptr++] = menu;
        try {
            byte[] b = Files.readAllBytes(Paths.get(JarUtils.getJarPath(CirSim.class) + "setuplist.txt"));
            int len = b.length;
            int p;
            if (len == 0 || b[0] != '#') {
                // got a redirect, try again
                getSetupList(menu);
                return;
            }
            for (p = 0; p < len; ) {
                int l;
                for (l = 0; l != len - p; l++)
                    if (b[l + p] == '\n') {
                        l++;
                        break;
                    }
                String line = new String(b, p, l - 1);
                if (line.charAt(0) == '#')
                    ;
                else if (line.charAt(0) == '+') {
                    Menu n = new Menu(line.substring(1));
                    menu.add(n);
                    menu = stack[stackptr++] = n;
                } else if (line.charAt(0) == '-') {
                    menu = stack[--stackptr - 1];
                } else {
                    int i = line.indexOf(' ');
                    if (i > 0) {
                        String title = line.substring(i + 1);
                        boolean first = false;
                        if (line.charAt(0) == '>')
                            first = true;
                        String file = line.substring(first ? 1 : 0, i);
                        menu.add(getMenuItem(title, "setup " + file));
                        if (first && startCircuit == null) {
                            startCircuit = file;
                            startLabel = title;
                        }
                    }
                }
                p += l;
            }
        } catch (Exception e) {
            e.printStackTrace();
            engine.stop("Can't read setuplist.txt!", null);
        }
    }

    private boolean doSwitch() {
        if (mouseElm == null || !(mouseElm instanceof SwitchElm))
            return false;
        SwitchElm se = (SwitchElm) mouseElm;
        se.toggle();
        if (se.isMomentary())
            heldSwitchElm = se;
        needAnalyze();
        return true;
    }

    public int locateElm(CircuitElm elm) {
        int i;
        for (i = 0; i != getElmList().size(); i++)
            if (elm == getElmList().elementAt(i))
                return i;
        return -1;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point worldPos = cv.unproject(e.getX(), e.getY());
        int x = worldPos.x;
        int y = worldPos.y;

        if (SwingUtilities.isLeftMouseButton(e) == false) return;
        // ignore right mouse button with no modifiers (needed on PC)
        if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
            int ex = e.getModifiersEx();
            if ((ex & (InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK
                    | InputEvent.CTRL_DOWN_MASK
                    | InputEvent.ALT_DOWN_MASK)) == 0)
                return;
        }
        if (scopeSelected != -1) return;
        if (dragElm != null) {
            dragElm.drag(this, x, y);
        }
        boolean success = true;
        switch (tempMouseMode) {
            case MODE_DRAG_ALL:
                dragAll(snapGrid(x), snapGrid(y));
                break;
            case MODE_DRAG_ROW:
                dragRow(snapGrid(y));
                break;
            case MODE_DRAG_COLUMN:
                dragColumn(snapGrid(x));
                break;
            case MODE_DRAG_POST:
                if (mouseElm != null)
                    dragPost(snapGrid(x), snapGrid(y));
                break;
            case MODE_SELECT:
                if (mouseElm == null)
                    selectArea(x, y);
                else {
                    tempMouseMode = MODE_DRAG_SELECTED;
                    success = dragSelected(x, y);
                }
                break;
            case MODE_DRAG_SELECTED:
                success = dragSelected(x, y);
                break;
        }
        dragging = true;
        if (success) {
            if (tempMouseMode == MODE_DRAG_SELECTED
                    && mouseElm instanceof TextElm) {
                dragX = x;
                dragY = y;
            } else {
                dragX = snapGrid(x);
                dragY = snapGrid(y);
            }
        }
        cv.repaint(pause);
    }

    public int snapGrid(int pos) {
        return engine.snapGrid(pos);
    }

    public int getGridSize() {
        return engine.getGridSize();
    }

    private void dragAll(int x, int y) {
        int dx = x - dragX;
        int dy = y - dragY;
        if (dx == 0 && dy == 0)
            return;
        int i;
        for (i = 0; i != getElmList().size(); i++) {
            CircuitElm ce = getElm(i);
            ce.move(dx, dy);
        }
        removeZeroLengthElements();
    }

    private void dragRow(int y) {
        int dy = y - dragY;
        if (dy == 0)
            return;
        int i;
        for (i = 0; i != getElmList().size(); i++) {
            CircuitElm ce = getElm(i);
            if (ce.y == dragY)
                ce.movePoint(0, 0, dy);
            if (ce.y2 == dragY)
                ce.movePoint(1, 0, dy);
        }
        removeZeroLengthElements();
    }

    private void dragColumn(int x) {
        int dx = x - dragX;
        if (dx == 0)
            return;
        int i;
        for (i = 0; i != getElmList().size(); i++) {
            CircuitElm ce = getElm(i);
            if (ce.x == dragX)
                ce.movePoint(0, dx, 0);
            if (ce.x2 == dragX)
                ce.movePoint(1, dx, 0);
        }
        removeZeroLengthElements();
    }

    private boolean dragSelected(int x, int y) {
        boolean me = false;
        if (mouseElm != null && !mouseElm.isSelected())
            mouseElm.setSelected(me = true);

        // snap grid, unless we're only dragging text elements
        int i;
        for (i = 0; i != getElmList().size(); i++) {
            CircuitElm ce = getElm(i);
            if (ce.isSelected() && !(ce instanceof TextElm))
                break;
        }
        if (i != getElmList().size()) {
            x = snapGrid(x);
            y = snapGrid(y);
        }

        int dx = x - dragX;
        int dy = y - dragY;
        if (dx == 0 && dy == 0) {
            // don't leave mouseElm selected if we selected it above
            if (me)
                mouseElm.setSelected(false);
            return false;
        }
        boolean allowed = true;

        // check if moves are allowed
        for (i = 0; allowed && i != getElmList().size(); i++) {
            CircuitElm ce = getElm(i);
            if (ce.isSelected() && !ce.allowMove(dx, dy))
                allowed = false;
        }

        if (allowed) {
            for (i = 0; i != getElmList().size(); i++) {
                CircuitElm ce = getElm(i);
                if (ce.isSelected())
                    ce.move(dx, dy);
            }
            needAnalyze();
        }

        // don't leave mouseElm selected if we selected it above
        if (me)
            mouseElm.setSelected(false);

        return allowed;
    }

    private void dragPost(int x, int y) {
        if (draggingPost == -1) {
            draggingPost = (MathUtil.distanceSq(mouseElm.x, mouseElm.y, x,
                    y) > MathUtil.distanceSq(mouseElm.x2, mouseElm.y2, x, y)) ? 1 : 0;
        }
        int dx = x - dragX;
        int dy = y - dragY;
        if (dx == 0 && dy == 0)
            return;
        mouseElm.movePoint(draggingPost, dx, dy);
        needAnalyze();
    }

    private void selectArea(int x, int y) {
        int x1 = Math.min(x, initDragX);
        int x2 = Math.max(x, initDragX);
        int y1 = Math.min(y, initDragY);
        int y2 = Math.max(y, initDragY);
        selectedArea = new Rectangle(x1, y1, x2 - x1, y2 - y1);
        for (int i = 0; i != getElmList().size(); i++) {
            CircuitElm ce = getElm(i);
            ce.selectRect(selectedArea);
        }
    }

    public void setSelectedElm(CircuitElm cs) {
        int i;
        for (i = 0; i != getElmList().size(); i++) {
            CircuitElm ce = getElm(i);
            ce.setSelected(ce == cs);
        }
        mouseElm = cs;
    }

    private void removeZeroLengthElements() {
        int i;
        for (i = getElmList().size() - 1; i >= 0; i--) {
            CircuitElm ce = getElm(i);
            if (ce.x == ce.x2 && ce.y == ce.y2) {
                getElmList().removeElementAt(i);
                ce.delete();
            }
        }
        needAnalyze();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0)
            return;
        Point2D.Double worldPos = new Point2D.Double();
        cv.unprojectPrecise(e.getX(), e.getY(), worldPos);
        int x = (int) Math.floor(worldPos.x + 0.5);
        int y = (int) Math.floor(worldPos.y + 0.5);
        dragX = snapGrid(x);
        dragY = snapGrid(y);
        draggingPost = -1;
        int i;
        CircuitElm origMouse = mouseElm;
        mouseElm = null;
        mousePost = -1;
        plotXElm = plotYElm = null;
        MouseElmSelectResult elmSelectRes = new MouseElmSelectResult();
        Point2D.Double tmpPoint2D = new Point2D.Double();
        Point tmpPoint = new Point();
        Rectangle tmpRect = new Rectangle();
        for (i = 0; i != getElmList().size(); i++) {
            CircuitElm ce = getElm(i);
            if (ce.boundingBox.contains(x, y)) {
                if (ce.getPostCount() == 0) {
                    mouseElm = ce;
                } else if ((ce.getPostCount() == 1 || ce.getPostCount() == 2) && ce.isBasicBoundingBoxSupported()) {
                    Point p1 = ce.getPost(0); //p1 is the rotation origin point
                    Point p2 = ce.getPost(1);
                    double angle = Math.atan2(p2.y - p1.y, p2.x - p1.x);
                    rotatePoint(p2.x, p2.y, -angle, p1.x, p1.y, tmpPoint2D);
                    tmpPoint.setLocation(tmpPoint2D.x, tmpPoint2D.y);
                    ce.getBasicBoundingBox(p1, tmpPoint, tmpRect);
                    rotatePoint(worldPos.x, worldPos.y, -angle, p1.x, p1.y, tmpPoint2D);
                    if (tmpRect.contains(tmpPoint2D)) {
                        int basicArea = tmpRect.width * tmpRect.height;
                        checkNewMouseElmSelect(ce, x, y, basicArea, elmSelectRes);
                    }
                } else {
                    int area = ce.boundingBox.width * ce.boundingBox.height;
                    checkNewMouseElmSelect(ce, x, y, area, elmSelectRes);
                }
            }
        }
        scopeSelected = -1;
        if (mouseElm == null) {
            for (i = 0; i != scopeCount; i++) {
                Scope s = scopes[i];
                if (s.getRect().contains(e.getX(), e.getY())) { //scopes are absolute on screen - use mouse pos on screen
                    s.select();
                    scopeSelected = i;
                }
            }
            // the mouse pointer was not in any of the bounding boxes, but we
            // might still be close to a post
            for (i = 0; i != getElmList().size(); i++) {
                CircuitElm ce = getElm(i);
                int j;
                int jn = ce.getPostCount();
                for (j = 0; j != jn; j++) {
                    Point pt = ce.getPost(j);
                    //int dist = distanceSq(x, y, pt.x, pt.y);
                    if (MathUtil.distanceSq(pt.x, pt.y, x, y) < 26) {
                        mouseElm = ce;
                        mousePost = j;
                        break;
                    }
                }
            }
        } else {
            mousePost = -1;
            // look for post close to the mouse pointer
            for (i = 0; i != mouseElm.getPostCount(); i++) {
                Point pt = mouseElm.getPost(i);
                if (MathUtil.distanceSq(pt.x, pt.y, x, y) < 26)
                    mousePost = i;
            }
        }
        if (mouseElm != origMouse)
            cv.repaint();
    }

    private void checkNewMouseElmSelect(CircuitElm ce, int x, int y, int area, MouseElmSelectResult res) {
        int maxDistChecks = Math.min(ce.getPostCount(), 2);
        for (int j = 0; j != maxDistChecks; j++) {
            Point pt = ce.getPost(j);
            int dist = MathUtil.distanceSq(x, y, pt.x, pt.y);

            // if multiple elements have overlapping bounding boxes,
            // we prefer selecting elements that have posts close
            // to the mouse pointer and that have a small bounding
            // box area.
            if (dist <= res.bestDist && area <= res.bestArea) {
                res.bestDist = dist;
                res.bestArea = area;
                mouseElm = ce;
            }
        }
    }

    private class MouseElmSelectResult {
        int bestDist = 100000;
        int bestArea = 100000;
    }

    private void rotatePoint(double px, double py, double angle, double ox, double oy, Point2D.Double result) {
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        px -= ox;
        py -= oy;
        double x = px * cos - py * sin;
        double y = px * sin + py * cos;
        result.setLocation(x + ox, y + oy);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
            if (mouseMode == MODE_SELECT || mouseMode == MODE_DRAG_SELECTED)
                clearSelection();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        scopeSelected = -1;
        mouseElm = plotXElm = plotYElm = null;
        cv.repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point worldPos = cv.unproject(e.getX(), e.getY());
        int x = worldPos.x;
        int y = worldPos.y;

        // System.out.println("mod="+e.getModifiers());
        int ex = e.getModifiersEx();
        if ((ex & (InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)) == 0
                && e.isPopupTrigger()) {
            doPopupMenu(e);
            return;
        }
        if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
            // left mouse
            tempMouseMode = mouseMode;
            if ((ex & InputEvent.ALT_DOWN_MASK) != 0
                    && (ex & InputEvent.META_DOWN_MASK) != 0)
                tempMouseMode = MODE_DRAG_COLUMN;
            else if ((ex & InputEvent.ALT_DOWN_MASK) != 0
                    && (ex & InputEvent.SHIFT_DOWN_MASK) != 0)
                tempMouseMode = MODE_DRAG_ROW;
            else if ((ex & InputEvent.SHIFT_DOWN_MASK) != 0)
                tempMouseMode = MODE_SELECT;
            else if ((ex & InputEvent.ALT_DOWN_MASK) != 0)
                tempMouseMode = MODE_DRAG_ALL;
            else if ((ex & (InputEvent.CTRL_DOWN_MASK
                    | InputEvent.META_DOWN_MASK)) != 0)
                tempMouseMode = MODE_DRAG_POST;
        } else if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
            // right mouse
            if ((ex & InputEvent.SHIFT_DOWN_MASK) != 0)
                tempMouseMode = MODE_DRAG_ROW;
            else if ((ex & (InputEvent.CTRL_DOWN_MASK
                    | InputEvent.META_DOWN_MASK)) != 0)
                tempMouseMode = MODE_DRAG_COLUMN;
            else
                return;
        }

        if (tempMouseMode != MODE_SELECT && tempMouseMode != MODE_DRAG_SELECTED)
            clearSelection();
        if (doSwitch())
            return;
        if (SwingUtilities.isLeftMouseButton(e)) {
            initDragX = x;
            initDragY = y;
            dragging = true;
            if (tempMouseMode != MODE_ADD_ELM || addingClass == null)
                return;

            int x0 = snapGrid(x);
            int y0 = snapGrid(y);

            pushUndo();
            dragElm = CirSimUtil.constructElement(engine, addingClass, x0, y0);
        }
    }

    private void doPopupMenu(MouseEvent e) {
        menuElm = mouseElm;
        menuScope = -1;
        if (cv.wasMouseDragged()) return;
        if (scopeSelected != -1) {
            PopupMenu m = scopes[scopeSelected].getMenu();
            menuScope = scopeSelected;
            if (m != null)
                m.show(e.getComponent(), e.getX(), e.getY());
        } else if (mouseElm != null) {
            elmEditMenuItem.setEnabled(mouseElm.getEditInfo(0) != null);
            elmScopeMenuItem.setEnabled(mouseElm.canViewInScope());
            elmMenu.show(e.getComponent(), e.getX(), e.getY());
        } else {
            doMainMenuChecks(mainMenu);
            mainMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private void doMainMenuChecks(Menu m) {
        int i;
        if (m == optionsMenu)
            return;
        for (i = 0; i != m.getItemCount(); i++) {
            MenuItem mc = m.getItem(i);
            if (mc instanceof Menu)
                doMainMenuChecks((Menu) mc);
            if (mc instanceof CheckboxMenuItem) {
                CheckboxMenuItem cmi = (CheckboxMenuItem) mc;
                cmi.setState(
                        mouseModeStr.compareTo(cmi.getActionCommand()) == 0);
            }
        }
    }

    private void setMainMenuLabel(Menu m, CircuitElm elm) {
        for (int i = 0; i != m.getItemCount(); i++) {
            MenuItem mc = m.getItem(i);
            if (mc instanceof Menu)
                setMainMenuLabel((Menu) mc, elm);
            if (mc instanceof CheckboxMenuItem) {
                if (mc.getActionCommand().equals(elm.getClass().getName())) {
                    modeInfoLabel.setText(mc.getLabel());
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int ex = e.getModifiersEx();
        if ((ex & (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK
                | InputEvent.META_DOWN_MASK)) == 0 && e.isPopupTrigger()) {
            doPopupMenu(e);
            return;
        }
        tempMouseMode = mouseMode;
        selectedArea = null;
        dragging = false;
        boolean circuitChanged = false;
        if (heldSwitchElm != null) {
            heldSwitchElm.mouseUp();
            heldSwitchElm = null;
            circuitChanged = true;
        }
        if (dragElm != null) {
            // if the element is zero size then don't create it
            if (dragElm.x == dragElm.x2 && dragElm.y == dragElm.y2)
                dragElm.delete();
            else {
                getElmList().addElement(dragElm);
                circuitChanged = true;
            }
            dragElm = null;
        }
        if (circuitChanged)
            needAnalyze();
        if (dragElm != null)
            dragElm.delete();
        dragElm = null;
        cv.repaint();
    }

    private void enableItems() {
        if (powerCheckItem.getState()) {
            powerBar.setEnabled(true);
            powerLabel.setEnabled(true);
        } else {
            powerBar.setEnabled(false);
            powerLabel.setEnabled(false);
        }
        enableUndoRedo();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        cv.repaint(pause);
        Object mi = e.getItemSelectable();
        if (mi == stoppedCheck)
            return;
        if (mi == smallGridCheckItem)
            engine.updateGrid(smallGridCheckItem.getState());
        if (mi == powerCheckItem) {
            if (powerCheckItem.getState())
                voltsCheckItem.setState(false);
            else
                voltsCheckItem.setState(true);
        }
        if (mi == voltsCheckItem && voltsCheckItem.getState())
            powerCheckItem.setState(false);
        enableItems();
        if (menuScope != -1) {
            Scope sc = scopes[menuScope];
            sc.handleMenu(mi);
        }
        if (mi instanceof CheckboxMenuItem) {
            MenuItem mmi = (MenuItem) mi;
            String s = mmi.getActionCommand();
            mouseModeStr = s;
            if (s.compareTo("DragAll") == 0) {
                mouseMode = MODE_DRAG_ALL;
                modeInfoLabel.setText("Drag All");
            } else if (s.compareTo("DragRow") == 0) {
                mouseMode = MODE_DRAG_ROW;
                modeInfoLabel.setText("Drag Row");
            } else if (s.compareTo("DragColumn") == 0) {
                mouseMode = MODE_DRAG_COLUMN;
                modeInfoLabel.setText("Drag Column");
            } else if (s.compareTo("DragSelected") == 0) {
                mouseMode = MODE_DRAG_SELECTED;
                modeInfoLabel.setText("Drag Selected");
            } else if (s.compareTo("DragPost") == 0) {
                mouseMode = MODE_DRAG_POST;
                modeInfoLabel.setText("Drag Post");
            } else if (s.compareTo("Select") == 0) {
                mouseMode = MODE_SELECT;
                modeInfoLabel.setText("Select");
            } else if (s.length() > 0) {
                try {
                    addingClass = ClassFinder.forName(s);
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
                mouseModeStr = s;
                mouseMode = MODE_ADD_ELM;
                modeInfoLabel.setText(mmi.getLabel());
            } else {
                // System.out.println("unhandled itemStateChanged: " + s);
            }
            tempMouseMode = mouseMode;
        }
    }

    public void pushUndo() {
        redoStack.removeAllElements();
        String s = dumpCircuit(true);
        if (undoStack.size() > 0 && s.compareTo((undoStack.lastElement())) == 0)
            return;
        undoStack.add(s);
        enableUndoRedo();
        circuitIsModified = true;
    }

    private void doUndo() {
        if (undoStack.size() == 0)
            return;
        redoStack.add(dumpCircuit(true));
        String s = (undoStack.remove(undoStack.size() - 1));
        circuitLoader.loadCircuit(s, false, true);
        enableUndoRedo();
    }

    private void doRedo() {
        if (redoStack.size() == 0)
            return;
        undoStack.add(dumpCircuit(true));
        String s = (redoStack.remove(redoStack.size() - 1));
        circuitLoader.loadCircuit(s, false, true);
        enableUndoRedo();
    }

    private void enableUndoRedo() {
        redoItem.setEnabled(redoStack.size() > 0);
        undoItem.setEnabled(undoStack.size() > 0);
    }

    private void clearUndoRedo() {
        redoStack.clear();
        undoStack.clear();
    }

    private void setMenuSelection() {
        if (menuElm != null) {
            if (menuElm.selected)
                return;
            clearSelection();
            menuElm.setSelected(true);
        }
    }

    private void doCut() {
        int i;
        pushUndo();
        if (mouseElm != null)
            menuElm = mouseElm;
        setMenuSelection();
        clipboard = "";
        for (i = getElmList().size() - 1; i >= 0; i--) {
            CircuitElm ce = getElm(i);
            if (ce.isSelected()) {
                clipboard += ce.dump(true) + "\n";
                ce.delete();
                getElmList().removeElementAt(i);
            }
        }
        enablePaste();
        needAnalyze();
    }

    public void deleteElm(CircuitElm elm) {
        mouseElm = elm;
        doDelete();
    }

    private void doDelete() {
        int i;
        pushUndo();
        if (mouseElm != null)
            menuElm = mouseElm;
        setMenuSelection();
        for (i = getElmList().size() - 1; i >= 0; i--) {
            CircuitElm ce = getElm(i);
            if (ce.isSelected()) {
                ce.delete();
                getElmList().removeElementAt(i);
            }
        }
        needAnalyze();
    }

    // Delete entire circuit
    private void deleteAll() {
        for (int i = getElmList().size() - 1; i >= 0; i--) {
            CircuitElm ce = getElm(i);
            ce.delete();
            getElmList().removeElementAt(i);
        }
        needAnalyze();
    }

    private void doCopy() {
        int i;
        clipboard = "";
        if (mouseElm != null)
            menuElm = mouseElm;
        setMenuSelection();
        for (i = getElmList().size() - 1; i >= 0; i--) {
            CircuitElm ce = getElm(i);
            if (ce.isSelected())
                clipboard += ce.dump(true) + "\n";
        }
        enablePaste();
    }

    private void enablePaste() {
        if (clipboard.length() > 0) {
            pasteItem.setEnabled(true);
            mainMenuPasteItem.setEnabled(true);
        }
    }

    private void doPaste() {
        pushUndo();
        clearSelection();
        int i;
        Rectangle oldbb = null;
        for (i = 0; i != getElmList().size(); i++) {
            CircuitElm ce = getElm(i);
            Rectangle bb = ce.getBoundingBox();
            if (oldbb != null)
                oldbb = oldbb.union(bb);
            else
                oldbb = bb;
        }
        int oldsz = getElmList().size();
        circuitLoader.loadCircuit(clipboard, true, true);

        // select new items
        Rectangle newbb = null;
        for (i = oldsz; i != getElmList().size(); i++) {
            CircuitElm ce = getElm(i);
            ce.setSelected(true);
            Rectangle bb = ce.getBoundingBox();
            if (newbb != null)
                newbb = newbb.union(bb);
            else
                newbb = bb;
        }
        if (oldbb != null && newbb != null && oldbb.intersects(newbb)) {
            // find a place for new items
            int dx = 0, dy = 0;
            int spacew = circuitArea.width - oldbb.width - newbb.width;
            int spaceh = circuitArea.height - oldbb.height - newbb.height;
            if (spacew > spaceh)
                dx = snapGrid(oldbb.x + oldbb.width - newbb.x + getGridSize());
            else
                dy = snapGrid(oldbb.y + oldbb.height - newbb.y + getGridSize());
            for (i = oldsz; i != getElmList().size(); i++) {
                CircuitElm ce = getElm(i);
                ce.move(dx, dy);
            }
            // center circuit
            handleResize();
        }
        needAnalyze();
    }

    private void clearSelection() {
        int i;
        for (i = 0; i != getElmList().size(); i++) {
            CircuitElm ce = getElm(i);
            ce.setSelected(false);
        }
    }

    private void doSelectAll() {
        int i;
        for (i = 0; i != getElmList().size(); i++) {
            CircuitElm ce = getElm(i);
            ce.setSelected(true);
        }
    }

    private void doSelectNext(int direction) {
        int size = getElmList().size();
        for (int i = 0; i != getElmList().size(); i++) {
            getElm(i).setSelected(false);
            if (getElm(i) == mouseElm) {
                selectedItemIndex = i + 1;
                if (selectedItemIndex == size)
                    selectedItemIndex = 0;
                if (selectedItemIndex < 0)
                    selectedItemIndex = size;
            }
        }
        mouseElm = getElm(selectedItemIndex);
        mouseElm.setSelected(true);
        selectedItemIndex += direction;
        if (selectedItemIndex == size)
            selectedItemIndex = 0;
        if (selectedItemIndex < 0)
            selectedItemIndex = size;
    }

    // Init keyboard detection
    private void keyboardInit() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {
                    if (SwingUtils.isModalDialogPresent() || !isFocused()) return false;
                    if (e.getID() == KeyEvent.KEY_PRESSED) {
                        char keyChar = e.getKeyChar();
                        int keyCode = e.getKeyCode();
                        if (keyCode == KeyEvent.VK_E && e.isControlDown()
                                && editDialog == null) {
                            doEdit(mouseElm);
                            return true;
                        }
                        if (keyChar > ' ' && keyChar < 127) {
                            CircuitElm elm;
                            String element = null;
                            if (keyChar == 'f' || keyChar == 'j'
                                    || keyChar == 't' || keyChar == 'v') {
                                if (keyChar == 'f')
                                    element = "NMosfetElm";
                                if (keyChar == 'f' && e.isAltDown())
                                    element = "PMosfetElm";
                                if (keyChar == 'j')
                                    element = "NJfetElm";
                                if (keyChar == 'j' && e.isAltDown())
                                    element = "PJfetElm";
                                if (keyChar == 't')
                                    element = "NTransistorElm";
                                if (keyChar == 't' && e.isAltDown())
                                    element = "PTransistorElm";
                                if (keyChar == 'v')
                                    element = "DCVoltageElm";

                                addingClass = ClassFinder.forName(element);
                                elm = CirSimUtil.constructElement(engine, addingClass, 0, 0);
                                mouseMode = MODE_ADD_ELM;
                                mouseModeStr = element;
                                setMainMenuLabel(mainMenu, elm);
                                return true;
                            }
                            Class<?> c = circuitLoader.getDumpTypes()[keyChar];
                            if (c == null || c == Scope.class)
                                return false;
                            elm = CirSimUtil.constructElement(engine, c, 0, 0);
                            if (elm == null || !(elm.needsShortcut()
                                    && elm.getDumpClass() == c))
                                return false;
                            mouseMode = MODE_ADD_ELM;
                            mouseModeStr = c.getName();
                            addingClass = c;
                            setMainMenuLabel(mainMenu, elm);
                        }
                        if (keyChar == ' ') {
                            mouseMode = MODE_SELECT;
                            mouseModeStr = "Select";
                            modeInfoLabel.setText(mouseModeStr);
                        }
                        switch (keyCode) {
                            case KeyEvent.VK_UP:
                                if (mouseElm != null) {
                                    pushUndo();
                                    mouseElm.move(0, -getGridSize());
                                    needAnalyze();
                                }
                                break;
                            case KeyEvent.VK_DOWN:
                                if (mouseElm != null) {
                                    pushUndo();
                                    mouseElm.move(0, getGridSize());
                                    needAnalyze();
                                }
                                break;
                            case KeyEvent.VK_LEFT:
                                if (mouseElm != null) {
                                    pushUndo();
                                    mouseElm.move(-getGridSize(), 0);
                                    needAnalyze();
                                }
                                break;
                            case KeyEvent.VK_RIGHT:
                                if (mouseElm != null) {
                                    pushUndo();
                                    mouseElm.move(getGridSize(), 0);
                                    needAnalyze();
                                }
                                break;
                        }
                        if (keyCode == KeyEvent.VK_DELETE
                                && editDialog == null) {
                            doDelete();
                        }
                        if (e.isControlDown() && keyCode == KeyEvent.VK_Z) {
                            doUndo();
                        }
                        if (e.isControlDown() && e.isShiftDown()
                                && keyCode == KeyEvent.VK_Z) {
                            doRedo();
                        }
                        if (e.isControlDown()
                                && e.getKeyCode() == KeyEvent.VK_X) {
                            doCut();
                        }
                        if (e.isControlDown() && keyCode == KeyEvent.VK_C) {
                            doCopy();
                        }
                        if (e.isControlDown() && keyCode == KeyEvent.VK_V) {
                            if (clipboard != null && clipboard.length() > 0)
                                doPaste();
                        }
                        if (e.isControlDown() && keyCode == KeyEvent.VK_A) {
                            doSelectAll();
                        }
                        if (e.isAltDown() && keyCode == KeyEvent.VK_TAB) {
                            doSelectNext(1);
                        }
                        if (e.isAltDown() && e.isShiftDown()
                                && keyCode == KeyEvent.VK_TAB) {
                            doSelectNext(-1);
                        }
                        tempMouseMode = mouseMode;
                    } else if (e.getID() == KeyEvent.KEY_RELEASED) {

                    } else if (e.getID() == KeyEvent.KEY_TYPED) {

                    }
                    return false;
                });
    }

    public int getCircuitAreaWidth() {
        return circuitArea.width;
    }

    public int getCircuitAreaHeight() {
        return circuitArea.height;
    }

    public CircuitCanvas getCircuitCanvas() {
        return cv;
    }


    public boolean isMouseModeDragRowOrColumn() {
        return (mouseMode == CirSim.MODE_DRAG_ROW ||
                mouseMode == CirSim.MODE_DRAG_COLUMN);
    }

    public CheckboxMenuItem getDotsCheckItem() {
        return dotsCheckItem;
    }

    public CheckboxMenuItem getVoltsCheckItem() {
        return voltsCheckItem;
    }

    public CheckboxMenuItem getPowerCheckItem() {
        return powerCheckItem;
    }

    public CheckboxMenuItem getShowValuesCheckItem() {
        return showValuesCheckItem;
    }

    public CheckboxMenuItem getEuroResistorCheckItem() {
        return euroResistorCheckItem;
    }
}
