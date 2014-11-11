

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class HelloWorld extends JApplet {
    //Called when this applet is loaded into the browser.
    public void init() {
      
        //Execute a job on the event-dispatching thread; creating this applet's GUI.
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                  try {
                    URL file = new URL("http://satbench.dev/005-80-12.cnf");
                    URLConnection con = file.openConnection();
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    JLabel lbl = new JLabel(in.readLine());
                    add(lbl);
                  } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
                  } catch (IOException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
                  }
                }
            });
        } catch (Exception e) {
          e.printStackTrace();
            System.err.println("createGUI didn't complete successfully");
        }
    }
}