/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution2.UI;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import com.satlib.GifSequenceWriter;
import com.satgraf.graph.UI.GraphCanvasPanel;
import com.satlib.Progressive;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;

/**
 *
 * @author zacknewsham
 */
public class ExportAction extends com.satgraf.actions.ExportAction<Evolution2GraphFrame> implements Progressive{
  public static int FRAMES = 10;
  private int line = 0;
  public ExportAction(Evolution2GraphFrame frame) {
    super(frame);
  }
  @Override
  public void export(final File file) {
    line = 0;
    frame.setProgressive(this);
    SwingWorker worker = new SwingWorker<Void, Void>(){

      @Override
      protected Void doInBackground() throws Exception {
        Evolution2OptionsPanel panel = (Evolution2OptionsPanel)frame.getOptionsPanel();
        GraphCanvasPanel canvas = frame.getCanvasPanel();
        double oldscale = frame.getGraphViewer().getScale();
        //frame.getGraphViewer().setScale(0.5);
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
          return null;
        }
        for(line = 0; line < panel.scaler.getMaxLine(); line++){
          try {
            File tmp = new File(String.format("/tmp/satgraf/%d.jpg", line));
            panel.scaler.advanceEvolution(line - 1, line);

            if(line % FRAMES == 0){
              BufferedImage jpg = new BufferedImage(canvas.getFullWidth(), canvas.getFullHeight(), BufferedImage.TYPE_BYTE_INDEXED);
              canvas.paintFull(jpg.createGraphics());
              Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
              ImageWriter writer = (ImageWriter)iter.next();
              ImageWriteParam iwp = writer.getDefaultWriteParam();
              iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
              iwp.setCompressionQuality((float)0.75);   // an integer between 0 and 
              FileImageOutputStream output = new FileImageOutputStream(tmp);
              writer.setOutput(output);
              IIOImage image = new IIOImage(jpg, null, null);
              writer.write(null, image, iwp);
              writer.dispose();
              gif.writeToSequence(jpg);
              output.close();
            }
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
        panel.scaler.advanceEvolution(panel.scaler.getMaxLine() - 1, 0);
        return null;
      }
    };
    worker.execute();
  }

  @Override
  public String getProgressionName() {
    return "Exporting GIF";
  }

  @Override
  public double getProgress() {
    return (double) line / (double)((Evolution2OptionsPanel)frame.getOptionsPanel()).scaler.getMaxLine();
  }
  
}
