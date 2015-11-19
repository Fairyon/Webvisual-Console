import java.awt.*;
import javax.swing.*;

public class MainApp {

  public String pathToApp = "";

  // Construct the application
  public MainApp() {

    MainFrame frame = new MainFrame();
    
    frame.validate();
    
    // Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    frame.setLocation((screenSize.width - frameSize.width) / 2,
        (screenSize.height - frameSize.height) / 2);
    frame.setVisible(true);
  }

  // Main method
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }
    new MainApp();
    // java.util.MissingResourceException
  }
}