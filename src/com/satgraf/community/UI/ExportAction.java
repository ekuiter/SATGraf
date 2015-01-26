/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.community.UI;

import com.satgraf.graph.UI.GraphCanvasPanel;
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

/**
 *
 * @author zacknewsham
 */
public class ExportAction extends com.satgraf.actions.ExportAction<CommunityGraphFrame>{

  public ExportAction(CommunityGraphFrame frame) {
    super(frame);
  }
  @Override
  public void export(File file) {
    try {
      CommunityOptionsPanel panel = (CommunityOptionsPanel)frame.getOptionsPanel();
      GraphCanvasPanel canvas = frame.getCanvasPanel();
      BufferedImage jpg = new BufferedImage(canvas.getFullWidth(), canvas.getFullHeight(), BufferedImage.TYPE_BYTE_INDEXED);
      canvas.paintFull(jpg.createGraphics());
      Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
      FileImageOutputStream output = new FileImageOutputStream(file);
      ImageWriter writer = (ImageWriter)iter.next();
      ImageWriteParam iwp = writer.getDefaultWriteParam();
      iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
      iwp.setCompressionQuality((float)0.75);   // an integer between 0 and
      writer.setOutput(output);
      IIOImage image = new IIOImage(jpg, null, null);
      writer.write(null, image, iwp);
      writer.dispose();
      output.close();
    } 
    catch (IOException ex) {
      Logger.getLogger(ExportAction.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
}
