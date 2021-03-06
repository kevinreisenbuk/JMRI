package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class MemoryInputIconTest extends PositionableJPanelTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists",p);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
        if (!GraphicsEnvironment.isHeadless()) {
            editor = new EditorScaffold();
            MemoryInputIcon t = new MemoryInputIcon(5,editor);
            t.setMemory("IM1");
            p = t;
        }
    }

    @After
    public void tearDown() {
        editor = null;
        p = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MemoryInputIconTest.class);

}
