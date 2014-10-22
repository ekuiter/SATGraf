/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
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
import org.json.simple.JSONObject;
import visual.actions.ExportAction;
import visual.actions.OpenAction;
import visual.actions.SaveAction;
import visual.graph.GraphViewer;

/**
 *
 * @author zacknewsham
 */
public abstract class GraphFrame extends JFrame{
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
  
  public abstract OpenAction getOpenAction();
  public abstract ExportAction getExportAction();
  
  public GraphCanvasPanel getCanvasPanel(){
    return canvasPanel;
  }
  public void fromJson(JSONObject json){
    this.setLocation(new Point(
      ((Long)json.get("x")).intValue(),
      ((Long)json.get("y")).intValue()
    ));
    this.setSize(new Dimension(
      ((Long)json.get("width")).intValue(),
      ((Long)json.get("height")).intValue()
    ));
    mainPane.setDividerLocation(((Long)json.get("dividerLocation")).intValue());
    if(canvasPanel.canvasPane != null){
      canvasPanel.canvasPane.getHorizontalScrollBar().setValue(((Long)json.get("scrollX")).intValue());
      canvasPanel.canvasPane.getVerticalScrollBar().setValue(((Long)json.get("scrollY")).intValue());
    }
    panel.setGraph(graphViewer);
  }
  public String toJson(){
    StringBuilder json = new StringBuilder();
    json.append("{\"x\":").append(this.getLocation().x);
    json.append(",\"y\":").append(this.getLocation().y);
    json.append(",\"width\":").append(this.getWidth());
    json.append(",\"height\":").append(this.getHeight());
    json.append(",\"scrollX\":").append(canvasPanel.getHorizontalScrollPosition());
    json.append(",\"scrollY\":").append(canvasPanel.getVerticalScrollPosition());
    json.append(",\"dividerLocation\":").append(mainPane.getDividerLocation());
    json.append(",\"graphViewer\":").append(graphViewer.toJson()).append("}");
    return json.toString();
  }
  
  public GraphViewer getGraphViewer(){
    return graphViewer;
  }
  
  public void init(){
	Toolkit tk = Toolkit.getDefaultToolkit();
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize((int) tk.getScreenSize().getWidth(), (int) tk.getScreenSize().getHeight());
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
    save.addActionListener(new SaveAction(this));
    open.addActionListener(this.getOpenAction());
    export.addActionListener(this.getExportAction());
    
  }
  public void setPanel(GraphOptionsPanel panel){
    this.panel = panel;
  }
  public void setGraphViewer(GraphViewer viewer){
    this.graphViewer = viewer;
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
