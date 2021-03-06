package jmri.jmrit.picker;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;
import org.netbeans.jemmy.operators.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Dave Sand Copyright (C) 2018
 */
public class PickFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PickFrame t = new PickFrame("Pick Frame Test");
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testAddNames() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PickFrame f = new PickFrame("Pick Frame Test Adds");
        Assert.assertNotNull("exists",f);
        JFrameOperator jfo = new JFrameOperator("Pick Frame Test Adds");
        Assert.assertNotNull(jfo);

        JTabbedPaneOperator jtab = new JTabbedPaneOperator(jfo);
        jtab.selectPage("Sensor Table");

        // Add an invalid name
        JTextFieldOperator jto = new JTextFieldOperator(jfo, 0);
        jto.enterText("AAA");
        Thread add1 = createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"), "add1");  // NOI18N
        new JButtonOperator(jfo, "Add to Table").doClick();  // NOI18N
        JUnitUtil.waitFor(()->{return !(add1.isAlive());}, "add1 finished");  // NOI18N

        // Add a valid name
        jto.enterText("IS123");
        new JButtonOperator(jfo, "Add to Table").doClick();  // NOI18N

        // Switch to the signal mast table
        jtab.selectPage("Signal Mast Table");

        //  Verify that the add fields are gone
        JLabelOperator jlo = new JLabelOperator(jfo, 1);
        Assert.assertTrue(jlo.getText().startsWith("Cannot add new items"));

        // Display other pages
        jtab.selectPage("Turnout Table");
        jtab.selectPage("Signal Head Table");
        jtab.selectPage("Signal Mast Table");
        jtab.selectPage("Memory Table");
        jtab.selectPage("Reporter Table");
        jtab.selectPage("Light Table");
        jtab.selectPage("Warrant Table");
        jtab.selectPage("Block Table");
        jtab.selectPage("Entry Exit Table");
        jtab.selectPage("Logix Table");

        JUnitUtil.dispose(f);
    }

    Thread createModalDialogOperatorThread(String dialogTitle, String buttonText, String threadName) {
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
            jbo.pushNoBlock();
        });
        t.setName(dialogTitle + " Close Dialog Thread: " + threadName);  // NOI18N
        t.start();
        return t;
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
