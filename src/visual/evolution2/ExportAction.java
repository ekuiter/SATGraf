/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.evolution2;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import sun.awt.image.ImageAccessException;
import visual.GifSequenceWriter;
import visual.UI.GraphCanvasPanel;
import visual.UI.GraphCanvasRenderer;

/**
 *
 * @author zacknewsham
 */
public class ExportAction extends visual.actions.ExportAction<Evolution2GraphFrame>{
  public static int FRAMES = 10;
  public ExportAction(Evolution2GraphFrame frame) {
    super(frame);
  }
  @Override
  public void export(File file) {
    Evolution2OptionsPanel panel = (Evolution2OptionsPanel)frame.getGraphViewer().getOptionsPanel();
    GraphCanvasPanel canvas = frame.getCanvasPanel();
    double oldscale = frame.getGraphViewer().getScale();
    frame.getGraphViewer().setScale(0.5);
    frame.pack();
    File dir = new File("/tmp/satgraf");
    dir.mkdir();
    GifSequenceWriter gif = null;
    try {
      gif = new GifSequenceWriter(new FileImageOutputStream(file), BufferedImage.TYPE_INT_RGB, 1, true);
    } catch (IOException ex) {
      Logger.getLogger(ExportAction.class.getName()).log(Level.SEVERE, null, ex);
    }
    if(gif == null){
      return;
    }
    for(int i = 0; i < panel.scaler.getMaxLine(); i++){
      try {
        File tmp = new File(String.format("/tmp/satgraf/%d.jpg", i));
        panel.scaler.advanceEvolution(i - 1, i);
        if(i % FRAMES == 0){
          BufferedImage jpg = new BufferedImage(canvas.getFullWidth(), canvas.getFullHeight(), BufferedImage.TYPE_INT_RGB);
          canvas.paintFull(jpg.createGraphics());
          Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
          ImageWriter writer = (ImageWriter)iter.next();
          ImageWriteParam iwp = writer.getDefaultWriteParam();
          iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
          iwp.setCompressionQuality((float)0.50);   // an integer between 0 and 
          FileImageOutputStream output = new FileImageOutputStream(tmp);
          writer.setOutput(output);
          IIOImage image = new IIOImage(jpg, null, null);
          writer.write(null, image, iwp);
          writer.dispose();
          gif.writeToSequence(jpg);
        }
      } catch (InterruptedException ex) {
        System.err.println("Error exporting");
      } catch (IOException ex) {
        Logger.getLogger(ExportAction.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    try {
      gif.close();
    } catch (IOException ex) {
      Logger.getLogger(ExportAction.class.getName()).log(Level.SEVERE, null, ex);
    }
    frame.getGraphViewer().setScale(oldscale);
  }
  
}
