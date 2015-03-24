import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class MainFrame extends JFrame {

  // config
  private String configFile = "config.properties";
  private Properties config;
  
  // messages and locale
  private int currentLocale;
  private ResourceBundle messages;
  private Locale[] supportedLocales;
  
  // GUI Components
  private static final long serialVersionUID = 1L;
  private JPanel contentPane;
  private JMenuBar jMenuBar1 = new JMenuBar();
  private JMenu jMenuServer = new JMenu();
  private JMenuItem jMenuServerStart;
  private JMenuItem jMenuServerStop;
  private JMenuItem jMenuServerExit;
  private JMenu jMenuConfig = new JMenu();
  private JMenuItem jMenuLanguage;
  private JMenu jMenuHelp = new JMenu();
  private JMenuItem jMenuHelpAbout;
  private JLabel statusBar = new JLabel();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JTextPane logArea = new JTextPane();
  private JScrollPane jScrollPane1 = new JScrollPane();

  // Tray components
  private boolean traySupported;
  private SystemTray tray;
  private TrayIcon trayIcon;
  private JPopupMenu trayMenu;
  private JDialog hiddenDialog;// helps to hide a menu
  private JMenuItem trayShowHide;
  private JMenuItem trayStart;
  private JMenuItem trayStop;
  private JMenuItem trayExit;  
  
  // Server Execution Helper
  private ExecHelper exh;
  
  // Helping variables
  private String pathToServer;
  private boolean opened = true; //true if window is opened
  private boolean serverIsRunning = false; //true if server is running

  // Construct the frame
  public MainFrame() {
    initConfig();
    initLocales();
    pathToServer = config.getProperty("path");
    currentLocale = Integer.parseInt(config.getProperty("locale"));
    messages = ResourceBundle.getBundle("Locale", supportedLocales[currentLocale]);
    
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    
    try {
      jbInit();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null,
          "Error!!",
          "Main Window cannot be initialized!",
          JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
    traySupported = SystemTray.isSupported();
    if (traySupported)
      initTray();
  }
  
  // get configuration Data from the configuration File and store it local
  private void initConfig(){
    FileInputStream confIn = null;
    config = new Properties();
    try {
      confIn = new FileInputStream(configFile);
      config.load(confIn);
      confIn.close();
    } catch (FileNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (IOException e) {
      try {
        confIn.close();
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  // get avaliable locales from configuration File and store it local
  private void initLocales(){
    String[] locales = config.getProperty("locales").split(" ");
    supportedLocales = new Locale[locales.length];
    for(int i=0; i<locales.length; i++) {
      supportedLocales[i] = new Locale(locales[i]);
    };
  }

  // Main Window Components initialization
  private void jbInit() throws Exception {
    /* Window Properties */
    this.setSize(new Dimension(400, 300));
    this.setTitle(messages.getString("serverOffline"));
    
    /* Main Panel */
    contentPane = (JPanel) this.getContentPane();
    contentPane.setLayout(borderLayout1);
    contentPane.setBackground(UIManager.getColor("control"));
    contentPane.setDoubleBuffered(true);
    contentPane.setOpaque(true);
    
    /* Menus */
    // Server Menu
    jMenuServer.setText(messages.getString("serverMenu"));
    jMenuServerStart = new JMenuItem(messages.getString("runServerBt"));
    jMenuServerStart.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        runCommandActionPerformed();
      }
    });
    jMenuServerStop = new JMenuItem(messages.getString("stopServerBt"));  
    jMenuServerStop.setEnabled(false); //initially server is stopped
    jMenuServerStop.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        confirmStopServer();
      }
    });
    jMenuServerExit = new JMenuItem(messages.getString("exitBt"));
    jMenuServerExit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        confirmExit();
      }
    });
    
    jMenuServer.add(jMenuServerStart);
    jMenuServer.add(jMenuServerStop);
    jMenuServer.add(jMenuServerExit);
    jMenuBar1.add(jMenuServer);
    
    // Config Menu
    jMenuConfig.setText(messages.getString("configMenu"));
    jMenuLanguage = new JMenu(messages.getString("languageMenu"));
    JMenuItem jLanguage;
    for(int i=0;i<supportedLocales.length;i++){
      final int tmp=i;
      jLanguage=new JMenuItem(supportedLocales[i].
          getDisplayLanguage(supportedLocales[i]));
      jLanguage.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          renewLabels(tmp);
        }
      });
      jMenuLanguage.add(jLanguage);
    }
     
    jMenuConfig.add(jMenuLanguage);
    jMenuBar1.add(jMenuConfig);
    
    // Help Menu
    jMenuHelp.setText(messages.getString("helpMenu"));
    jMenuHelpAbout = new JMenuItem(messages.getString("aboutBt"));
    jMenuHelpAbout.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        infoWindow();
      }
    });
    
    jMenuHelp.add(jMenuHelpAbout);
    jMenuBar1.add(jMenuHelp);
    
    
    /* Text Area */    
    logArea.setBackground(UIManager.getColor("control"));
    jScrollPane1.getViewport().setBackground(UIManager.getColor("control"));
    jScrollPane1.setAutoscrolls(true);
    jScrollPane1.setBorder(new TitledBorder(BorderFactory.createBevelBorder(
        BevelBorder.LOWERED, Color.white, Color.white, new Color(103, 101, 98),
        new Color(148, 145, 140)), messages.getString("logTitle")));
    jScrollPane1.setOpaque(false);
    logArea.setDoubleBuffered(true);
    logArea.setOpaque(false);
    logArea.setText("");
    //logArea.setWrapStyleWord(true);
    
    /* Status Bar */
    statusBar.setBorder(BorderFactory.createEtchedBorder());
    statusBar.setDebugGraphicsOptions(0);
    statusBar.setDoubleBuffered(true);
    statusBar.setOpaque(false);
    statusBar.setVerifyInputWhenFocusTarget(true);
    statusBar.setText(messages.getString("serverOffline"));
    
    /* Put all together*/
    setJMenuBar(jMenuBar1);
    contentPane.add(statusBar, BorderLayout.SOUTH);
    contentPane.add(jScrollPane1, BorderLayout.CENTER);
    jScrollPane1.getViewport().add(logArea, null);
    logArea.setEditable(false);
  }

  // Tray Components initialization
  private void initTray() {
    /* Menu with it's Components*/
    
    trayMenu = new JPopupMenu();
    hiddenDialog = new JDialog ();
    hiddenDialog.setSize(10, 10);
    hiddenDialog.setUndecorated(true);
    hiddenDialog.addWindowFocusListener(new WindowFocusListener () {
        @Override
        public void windowLostFocus (WindowEvent we ) {
            hiddenDialog.setVisible(false);
        }
        @Override
        public void windowGainedFocus (WindowEvent we) {}
    });
    
    trayExit = new JMenuItem(messages.getString("exitBt"));
    trayExit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hiddenDialog.setVisible(false);
        confirmExit();
      }
    });
    trayStart = new JMenuItem(messages.getString("runServerBt"));
    trayStart.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hiddenDialog.setVisible(false);
        runCommandActionPerformed();
      }
    });
    trayStop = new JMenuItem(messages.getString("stopServerBt"));
    trayStop.setEnabled(false); //initially server is stopped
    trayStop.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hiddenDialog.setVisible(false);
        confirmStopServer();
      }
    });
    trayShowHide = new JMenuItem(messages.getString("showHideBt"));
    trayShowHide.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hiddenDialog.setVisible(false);
        hideUnhide();
      }
    });
    trayMenu.add(trayShowHide);
    trayMenu.add(trayStart);
    trayMenu.add(trayStop);
    trayMenu.add(trayExit);
    trayMenu.setInvoker(hiddenDialog);
    
    /* Tray Icon */
    Image image = Toolkit.getDefaultToolkit().getImage(config.getProperty("icon"));
    trayIcon = new TrayIcon(image, messages.getString("serverOffline"), null);
    trayIcon.setImageAutoSize(true);
    trayIcon.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          hideUnhide();
        }
      }
      @Override
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          trayMenu.setLocation(e.getX(),e.getY() - trayMenu.getHeight());
          hiddenDialog.setLocation(e.getX(),e.getY() - trayMenu.getHeight());
          hiddenDialog.setVisible(true);
          trayMenu.setVisible(true);
        }
      }
    });
    
    tray = SystemTray.getSystemTray();
    
    try {
      tray.add(trayIcon);
    } catch (AWTException e1) {
      System.out.println("Unable to make a tray Icon");
      e1.printStackTrace();
    }
  }
  
  // Renew all Labels with given index from the currentLocale Array
  private void renewLabels(int index){
    config.setProperty("locale", index+"");
    currentLocale = index;
    messages = ResourceBundle.getBundle("Locale", supportedLocales[index]);
    String serverStatus = serverIsRunning?"serverOnline":"serverOffline";
    this.setTitle(messages.getString(serverStatus));
    statusBar.setText(messages.getString(serverStatus));
    trayIcon.setToolTip(messages.getString(serverStatus));
    jMenuServer.setText(messages.getString("serverMenu"));
    jMenuServerStart.setText(messages.getString("runServerBt"));
    jMenuServerStop.setText(messages.getString("stopServerBt"));
    jMenuServerExit.setText(messages.getString("exitBt"));
    jMenuConfig.setText(messages.getString("configMenu"));
    jMenuLanguage.setText(messages.getString("languageMenu"));
    jMenuHelp.setText(messages.getString("helpMenu"));
    jMenuHelpAbout.setText(messages.getString("aboutBt"));
    ((TitledBorder)jScrollPane1.getBorder()).
              setTitle(messages.getString("logTitle"));
    trayExit.setText(messages.getString("exitBt"));
    trayStart.setText(messages.getString("runServerBt"));
    trayStop.setText(messages.getString("stopServerBt"));
    trayShowHide.setText(messages.getString("showHideBt"));
  }

  // Append a text to the textArea and Update a Cursor
  private void updateTextArea(JTextPane textArea, String text, Color c) {
    StyleContext sc = StyleContext.getDefaultStyleContext();
    AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
    aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
    aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
    int len = textArea.getDocument().getLength();
    textArea.setCaretPosition(len);
    textArea.setCharacterAttributes(aset, false);
    textArea.setText(textArea.getText()+text);
    //textArea.replaceSelection(text);
  }

  // Exit program action performed
  public void ExitActionPerformed() {
    if (exh != null)
      exh.stop();
    updateConfigFile();
    System.exit(0);
  }

  // Overridden so we can handle the window closing process
  protected void processWindowEvent(WindowEvent e) {   
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      if (traySupported) {
        opened=false;
      } else if (exitConfirmationWindow()) {
        ExitActionPerformed();
      } else {
        setVisible(true);
        toFront();
      }
    }
  }

  // Put an input String on the textArea
  public void processNewInput(String input) {
    updateTextArea(logArea, input, Color.BLACK);
  }

  //Put an error String on the textArea
  public void processNewError(String error) {
    updateTextArea(logArea, error, Color.RED);
  }

  // clear the executable Thread and set the right labels
  public void serverStopped(int exitValue) {
    exh = null;
    handleServerState(false);
  }
  
  //switch all captions to the current state
  private void handleServerState(boolean running){
    String state=running?messages.getString("serverOnline"):
      messages.getString("serverOffline");
    serverIsRunning=running;
    statusBar.setText(state);
    setTitle(state);
    jMenuServerStart.setEnabled(!running);
    jMenuServerStop.setEnabled(running);
    
    if (traySupported){
      trayStart.setEnabled(!running);
      trayStop.setEnabled(running);
      trayIcon.setToolTip(state);
    }
  }

  //check if node.js and all modules are available
  //!!! modChecker.js must exists to check the modules availability
  //TODO change the commands for the production modus
  boolean checkPrerequisites(){
    if(!System.getenv("PATH").contains("node")&&
          !new File(pathToServer+"node.exe").exists()){
      processNewError(messages.getString("nodeNotExists"));
      return false;
    } else {
      String[] command = {"node","modChecker.js"};//chec
      ProcessBuilder processBuilder = new ProcessBuilder(command);
      processBuilder.directory(new File(pathToServer)); //change exec dir
      Process pro;
      try {
        ExecHelper.exec(this,pro=processBuilder.start(),false);
        pro.waitFor();
        if(pro.exitValue()!=0){      
          processNewError("Not all modules existing. Trying to fix it...\n");
          command=new String[]{"npm.cmd","update"};
          processBuilder = new ProcessBuilder(command);
          processBuilder.directory(new File(pathToServer));
          ExecHelper.exec(this,pro=processBuilder.start(),false);
          pro.waitFor();
          if(pro.exitValue()!=0){ 
            processNewError("Problem by Update\n");
            return false;
          }
        }
      } catch (IOException | InterruptedException e) {
        processNewError("Something goes wrong!!\n");
        processNewError(e.getMessage());
        return false;
      }
    }
    return true;
  }
  
  // start the Server
  void runCommandActionPerformed() {
    if (exh == null && !serverIsRunning) {
      logArea.setText(null);
      if(!checkPrerequisites()) return;
      handleServerState(true);
      startServer();
    }
  }

  void startServer(){
    try {
      exh = ExecHelper.exec(this, new ArrayList<String>() {
        private static final long serialVersionUID = 1L;
        {
          add("node");
          add(pathToServer + config.getProperty("appFile"));
        }
      }, true);
    } catch (IOException ex) {
      processNewError(String.format(messages.getObject("cannotExec").toString(), "node"));
      processNewError(ex.getMessage());
    }
  }

  // terminate the server
  void stopServerActionPerformed() {
    if (exh != null && serverIsRunning) {
      handleServerState(false);
      exh.stop();
      
      if (traySupported){
        trayStop.setEnabled(false);
        trayStart.setEnabled(true);
        trayIcon.setToolTip(messages.getString("serverOffline"));
      }
    }
  }
  
  //Hide or unhide the Window. Return last window state by unhiding.
  void hideUnhide(){
    if(opened){
      setVisible(false);
      opened=false;
    } else {
      setVisible(true);
      opened=true;
    }
  }
  
  // Shows Exit Confirmation Window and returns the answer
  private boolean exitConfirmationWindow(){
    return yesNo(messages.getString("exitConfirmTitle"), 
        ((serverIsRunning) ? 
            messages.getString("exitServerWarning")+"\n" : "") +
        ((!logArea.getText().equals("")) ? 
            messages.getString("exitLogWarning")+"\n" : "") +
        messages.getString("exitConfirm"));
  }
  
  // Calls Exit Confirmation Window and runs ExitActionPerformed() if Yes was clicked
  private void confirmExit(){
    if (exitConfirmationWindow()) {
      ExitActionPerformed();
    }
  }
  
  // Shows Stop Server Confirmation Window and runs stopServerActionPerformed if Yes was clicked
  private void confirmStopServer(){
    if(yesNo(messages.getString("serverStopConfirmTitle"), 
        messages.getString("serverStopConfirm"))){
      stopServerActionPerformed();
    }
  }
  
  // Easily Create the yes-no Option Panes
  private boolean yesNo(String title, String question){
    return JOptionPane.YES_OPTION == 
        JOptionPane.showConfirmDialog 
          (this, question,title,JOptionPane.YES_NO_OPTION);
  }
  
  private void infoWindow(){
    JOptionPane.showMessageDialog(this,
        messages.getString("aboutTitle"),
        messages.getString("aboutInfo"),
        JOptionPane.INFORMATION_MESSAGE,
        new ImageIcon(config.getProperty("icon")));
  }
  
  // Update the Configuration File with current Data
  private void updateConfigFile(){
    FileOutputStream confOut = null;
    try {
      confOut = new FileOutputStream(configFile);
      config.store(confOut, null);
      confOut.close();
    } catch (FileNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (IOException e) {
      try {
        confOut.close();
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}