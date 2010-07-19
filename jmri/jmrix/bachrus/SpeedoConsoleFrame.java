// SpeedoConsoleFrame.java

package jmri.jmrix.bachrus;

import java.awt.*;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.JComboBox;

import jmri.util.JmriJFrame;


/**
 * Frame for Speedo Console for Bachrus running stand reader interface
 * 
 * @author			Andrew Crosland   Copyright (C) 2010
 * @version			$Revision: 1.6 $
 */
public class SpeedoConsoleFrame extends JmriJFrame implements SpeedoListener {

    /***
     * TODO:
     *  Complete the help file
     *  Allow selection of arbitrary scale
     */


    // member declarations
    protected javax.swing.JLabel scaleLabel = new javax.swing.JLabel();
    protected javax.swing.JLabel speedLabel = new javax.swing.JLabel();
    protected javax.swing.JTextField speedTextField = new javax.swing.JTextField(12);

    protected ButtonGroup speedGroup = new ButtonGroup();
    protected JRadioButton mphButton = new JRadioButton("Miles per Hour");
    protected JRadioButton kphButton = new JRadioButton("Kilometers per Hour");

    protected javax.swing.JLabel readerLabel = new javax.swing.JLabel();

    protected String[] scaleStrings = new String [] {
        "Z - 1.39mm - 1:220",
        "Euro N - 1.9mm - 1:160",
        "N Fine - 2mm - 1:152",
        "Japanese N - 2.03mm - 1:150",
        "British N - 2.0625mm - 1:148",
        "3mm - 1:120",
        "Triang TT - 3mm - 1:101.6",
        "00/EM/S4 - 4mm - 1:76",
        "HO - 3.5mm - 1:87",
        "S - 3/16inch - 1:64",
        "O - 1/4inch - 1:48",
        "O - 6.77mm - 1:45",
        "O - 7mm - 1:43"/*,
        "Other..."*/
        };

    protected double[] scales = new double[] {
        220,
        160,
        152,
        150,
        148,
        120,
        101.6,
        76,
        87,
        64,
        48,
        45,
        43/*,
        -1*/
        };

    protected static final int defaultScale = 8;

    protected double selectedScale = 0;
    protected int series = 0;
    protected double speed = 0;
    protected double old_speed = 0;
    protected double old_display_speed = 0;
    protected double speed_acc = 0;
    protected double speed_ave = 0;
    protected int range = 1;
    protected double circ = 0;
    protected double count = 1;
    protected double f;
    
    /*
     * At low speed, readings arrive less often and less filtering
     * is applied to minimise the delay in updating the display
     * 
     * Speed measurement is split into 3 ranges with an overlap, tp
     * prevent "hunting" between the ranges.
     */
    protected static final int RANGE1LO = 0;
    protected static final int RANGE1HI = 5;
    protected static final int RANGE2LO = 3;
    protected static final int RANGE2HI = 20;
    protected static final int RANGE3LO = 16;
    protected static final int RANGE3HI = 9999;
    protected static final int[] filter_length = {0, 2, 5, 10};
    
    protected boolean timerRunning = false;

    //Create the combo box, select item at index 4.
    //Indices start at 0, so 4 specifies british N.
    JComboBox scaleList = new JComboBox(scaleStrings);

    // members for handling the Speedo interface
    SpeedoTrafficController tc = null;
    String replyString;

    public SpeedoConsoleFrame() {
        super();
    }
    
    protected String title() { return "Speedo Console"; }
    
    public void dispose() {
        SpeedoTrafficController.instance().removeSpeedoListener(this);
        super.dispose();
    }
    
    public void initComponents() throws Exception {
        setTitle(title());
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        /*
         * Scale panel
         */
        JPanel scalePanel = new JPanel();
        scalePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Select Scale"));
        scalePanel.setLayout(new FlowLayout());
        
        scaleList.setToolTipText("Select the scale");
        scaleList.setSelectedIndex(defaultScale);
        selectedScale = scales[defaultScale];

        // Listen to selection of scale
        scaleList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                selectedScale = scales[cb.getSelectedIndex()];
                calcSpeed();
                // *** check if -1 and enable text entry box
            }
        });

        scaleLabel.setText("Scale:");
        scaleLabel.setVisible(true);
        
        readerLabel.setText("Unknown Reader");
        readerLabel.setVisible(true);

        scalePanel.add(scaleLabel);
        scalePanel.add(scaleList);
        scalePanel.add(readerLabel);
        
        getContentPane().add(scalePanel);
 
        /*
         * Speed Panel
         */
        JPanel speedPanel = new JPanel();
        speedPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Measured Speed"));
        speedPanel.setLayout(new FlowLayout());
        
        speedGroup.add(mphButton);
        speedGroup.add(kphButton);
        mphButton.setSelected(true);
        
        Font f = new Font("", Font.PLAIN, 96);
        speedTextField.setFont(f);
        speedTextField.setHorizontalAlignment(JTextField.RIGHT);
        speedTextField.setColumns(3);
        speedTextField.setText("0.0");
        speedTextField.setVisible(true);
        speedTextField.setToolTipText("Speed will be displayed here");

        mphButton.setToolTipText("Display Speed in Miles per Hour");
        kphButton.setToolTipText("Display Speed in Kilometers per Hour");
       
        speedPanel.add(speedLabel);
        speedPanel.add(speedTextField);
        speedPanel.add(mphButton);
        speedPanel.add(kphButton);
        
        // Listen to change of units
        mphButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
//                calcSpeed();
            }
        });
        kphButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
//                calcSpeed();
            }
        });

        getContentPane().add(speedPanel);
        
        // connect to TrafficController
        tc = SpeedoTrafficController.instance();
        tc.addSpeedoListener(this);

        // add help menu to window
    	addHelpMenu("package.jmri.jmrix.bachrus.SpeedoConsoleFrame", true);

        // pack for display
        pack();
    }
    
    /**
     * Define help menu for this window.
     * <p>
     * By default, provides a generic help page
     * that covers general features.  Specific
     * implementations can override this to 
     * show their own help page if desired.
     */
    protected void addHelpMenu() {
    	addHelpMenu("package.jmri.jmrix.bachrus.SpeedoConsoleFrame", true);
    }

    /**
     * Handle "replies" from the hardware. In fact, all the hardware does
     * is send a constant stream of unsolicited speed updates.
     * @param l
     */
    public synchronized void reply(SpeedoReply l) {  // receive a reply message and log it
        //log.debug("Speedo reply " + l.toString());
        count = l.getCount();
        series = l.getSeries();
        if (count > 0) {
            switch (series) {
                case 4:
                    circ = 12.5664;
                    readerLabel.setText("40 Series Reader");
                    break;
                case 5:
                    circ = 18.8496;
                    readerLabel.setText("50 Series Reader");
                    break;
                case 6:
                    circ = 50.2655;
                    readerLabel.setText("60 Series Reader");
                    break;
                default:
                    speedTextField.setText("Error!");
                    log.error("Invalid reader type");
                    break;
            }

            // Update speed
            calcSpeed();
        }
        if (timerRunning == false) {
            // first reply starts the timer
            startReplyTimer();
            startDisplayTimer();
            timerRunning = true;
        } else {
            // subsequnet replies restart it
            replyTimer.restart();
        }
    }

    /*
     * Calculate the scale speed
     */
    protected void calcSpeed() {
        if (series > 0) {
            // Scale the data and calculate kph
            try {
                f = 1500000/count;
                speed = (f/24)*circ*selectedScale*3600/1000000;
                // Convert to mph?
                if (mphButton.isSelected()) {
                    speed = speed/1.609344;
                }
            } catch (ArithmeticException ae) {
                log.error("Exception calculating speed " + ae);
            }
            // Averaging function used for speed is
            // S(t) = S(t-1) - [S(t-1)/N] + speed
            // A(t) = S(t)/N
            //
            // where S is an accumulator, N is the length of the filter (i.e.,
            // the number of samples included in the rolling average), and A is
            // the result of the averaging function.
            //
            // Re-arranged
            // S(t) = S(t-1) - A(t-1) + speed
            // A(t) = S(t)/N
            // If speed reading is wildly out then use current average to filter
            // out mechanical jitter in locos and readers
            if ((speed > old_speed*.85) && (speed < old_speed*1.15)) {
                speed_acc = speed_acc - speed_ave + speed;
            } else {
                speed_acc = speed_acc - speed_ave + speed_ave;
            }
            speed_ave = speed_acc/filter_length[range];
            log.info("Speed: "+speed+" Ave: "+speed_ave);
            // When we switch range we must compensate the current accumulator
            // value for the longer filter.
            switch (range) {
                case 1:
                    if (speed > RANGE1HI) {
                        range++;
                        speed_acc = speed_acc*filter_length[2]/filter_length[1];
                    }
                    break;
                case 2:
                    if (speed < RANGE2LO){
                        range--;
                        speed_acc = speed_acc*filter_length[1]/filter_length[2];
                    }
                    else if (speed > RANGE2HI) {
                        range++;
                        speed_acc = speed_acc*filter_length[3]/filter_length[2];
                    }
                    break;
                case 3:
                    if (speed < RANGE3LO) {
                        range--;
                        speed_acc = speed_acc*filter_length[2]/filter_length[3];
                    }
                    break;
            }
//            showSpeed(speed_ave, 0);
            old_speed = speed;
        }
    }

    /*
     * Display the speed
     */
    protected void showSpeed(double speed, int force) {
        if (series > 0) {
            if ((speed < 0) || (speed > 999)) {
                log.error("Calculated speed out of range: " + speed);
                speedTextField.setText("999");
            } else {
                // Final smoothing as applied by Bachrus Console. Don't update display
                // unless speed has changed more than 4%
                if ((speed > old_display_speed*1.02) || (speed < old_display_speed*0.98) || force > 0) {
                    speedTextField.setText(MessageFormat.format("{0,number,##0.0}", speed));
                    speedTextField.setHorizontalAlignment(JTextField.RIGHT);
                    old_display_speed = speed;
                }
            }
        }
    }

    javax.swing.Timer replyTimer = null;

	// Once we receive a speedoReply we expect them regularly, at
    // least once every 4 seconds
    protected void startReplyTimer() {
        replyTimer = new javax.swing.Timer(4000, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                replyTimeout();
            }
        });
        replyTimer.setRepeats(true);     // refresh until stopped by dispose
        replyTimer.start();
    }

    /**
     * Internal routine to resend the speed on a timeout
     */
    synchronized protected void replyTimeout() {
        //log.debug("Timed out - display speed zero");
        speed = 0;
        speed_acc = 0;
        speed_ave = 0;
        old_display_speed = 0;
        showSpeed(speed, 1);
    }

    javax.swing.Timer displayTimer = null;

	// A timer is used to update the display every .25s
    protected void startDisplayTimer() {
        displayTimer = new javax.swing.Timer(250, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                displayTimeout();
            }
        });
        displayTimer.setRepeats(true);     // refresh until stopped by dispose
        displayTimer.start();
    }

    /**
     * Internal routine to update the
     */
    synchronized protected void displayTimeout() {
        //log.info("Display timeout");
        showSpeed(speed_ave, 0);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SpeedoConsoleFrame.class.getName());
    
}
