/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.UI;

import java.awt.Component;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import visual.actions.OpenAction;
import visual.actions.SaveAction;
import visual.graph.GraphViewer;

/**
 *
 * @author zacknewsham
 */
public class GraphFrame extends JFrame{
  protected JMenu menu = new JMenu("File");
  protected JMenuBar menuBar = new JMenuBar();
  
  private JMenuItem open = new JMenuItem("Open");
  private JMenuItem save = new JMenuItem("Save");
  private JMenuItem export = new JMenuItem("Export");
  
  protected final JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
  protected GraphCanvasPanel canvasPanel;
  protected GraphOptionsPanel panel;
  protected GraphViewer graphViewer;
  
  public GraphFrame(GraphViewer graphViewer){
    super();
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
      this.graphViewer = graphViewer;
      init();
    } catch (ClassNotFoundException ex) {
      Logger.getLogger(GraphFrame.class.getName()).log(Level.SEVERE, null, ex);
    } catch (InstantiationException ex) {
      Logger.getLogger(GraphFrame.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(GraphFrame.class.getName()).log(Level.SEVERE, null, ex);
    } catch (UnsupportedLookAndFeelException ex) {
      Logger.getLogger(GraphFrame.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  public void open(File file){
    
  }
  
  public void init(){
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1000, 700);
    setContentPane(mainPane);
    if(menu.getMenuComponentCount() == 0){
      //menu.add("File");
      menu.add(open);
      menu.add(save);
      menu.add(export);
      menuBar.add(menu);
      setJMenuBar(menuBar);
    }
    for(int i = 0; i < save.getActionListeners().length; i++){
      save.removeActionListener(save.getActionListeners()[i]);
    }
    for(int i = 0; i < open.getActionListeners().length; i++){
      open.removeActionListener(open.getActionListeners()[i]);
    }
    save.addActionListener(new SaveAction(graphViewer));
    open.addActionListener(new OpenAction(this));
  }
  
  public void show(){
    if(graphViewer != null){
      mainPane.setLeftComponent(canvasPanel);
      mainPane.setRightComponent(panel);
    }
    super.show();
  }
  protected void setLeftComponent(Component c){
    mainPane.setLeftComponent(c);
  }
  protected void setRightComponent(Component c){
    mainPane.setRightComponent(c);
  }
}
