/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.UI;

import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
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
    this.graphViewer = graphViewer;
    init();
  }
  public void init(){
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1000, 700);
    setContentPane(mainPane);
    
    menu.add("File");
    menu.add(open);
    menu.add(save);
    save.addActionListener(new SaveAction(graphViewer));
    menu.add(export);
    menuBar.add(menu);
    setJMenuBar(menuBar);
  }
  
  public void show(){
    mainPane.setLeftComponent(canvasPanel);
    mainPane.setRightComponent(panel);
    super.show();
  }
  protected void setLeftComponent(Component c){
    mainPane.setLeftComponent(c);
  }
  protected void setRightComponent(Component c){
    mainPane.setRightComponent(c);
  }
}
