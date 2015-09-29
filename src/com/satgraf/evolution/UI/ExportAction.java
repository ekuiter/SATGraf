/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.evolution.UI;

import com.satgraf.community.placer.CommunityPlacer;
import com.satgraf.graph.UI.EdgeLayer;
import com.satgraf.graph.UI.NodeLayer;
import com.satlib.GifSequenceWriter;
import com.satlib.Progressive;
import com.satlib.evolution.Evolution;
import com.satlib.evolution.EvolutionGraph;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.SwingWorker;
import org.json.simple.JSONObject;

/**
 *
 * @author zacknewsham
 */
public class ExportAction extends com.satgraf.actions.ExportAction<EvolutionGraphFrame> implements Progressive{
  private ExportableGraphViewer exportableGraphViewer;
  public static int FRAMES = 1000;
  private int line = 0;
  private EdgeLayer edgeLayer;
  private NodeLayer nodeLayer;
  public ExportAction(EvolutionGraphFrame frame) {
    super(frame);
  }
  @Override
  public void export(final File file) {
    line = 0;
    frame.setProgressive(this);
    
    SwingWorker worker = new SwingWorker<Void, Void>(){

      @Override
      protected Void doInBackground() throws Exception {
        EvolutionGraphViewer graphViewer = frame.getGraphViewer();
        exportableGraphViewer = new ExportableGraphViewer(graphViewer.getGraph(), null, (CommunityPlacer)graphViewer.getPlacer());
        exportableGraphViewer.setScale(0.5);
        
        exportableGraphViewer.setEvolution(new Evolution(exportableGraphViewer.getGraph(), graphViewer.evolution.getFile(), graphViewer.evolution.getSolver()));
        
        EvolutionOptionsPanel panel = (EvolutionOptionsPanel)frame.getOptionsPanel();
        Rectangle bounds = graphViewer.getBounds();
        edgeLayer = new EdgeLayer(new Dimension((int)bounds.getWidth(), (int)bounds.getHeight()), exportableGraphViewer);
        nodeLayer = new NodeLayer(new Dimension((int)bounds.getWidth(), (int)bounds.getHeight()), exportableGraphViewer);
        
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
        Evolution evolution = exportableGraphViewer.getEvolution();
        evolution.bufferFile(0);
        evolution.setTotalFiles(graphViewer.getEvolution().getTotalFiles());
        evolution.setTotalLines(graphViewer.getEvolution().getTotalLines());
        int oldLine = -1;
        for(line = 0; line < evolution.getTotalLines(); line+=FRAMES){
          try {
            File tmp = new File(String.format("/tmp/satgraf/%d.jpg", line));
            evolution.advanceEvolutionThread(oldLine, line, false);
            oldLine = line;
            if(line % FRAMES == 0){
              BufferedImage jpg = new BufferedImage((int)(bounds.getWidth() * exportableGraphViewer.getScale()), (int)(bounds.getHeight() * exportableGraphViewer.getScale()), BufferedImage.TYPE_BYTE_INDEXED);
              Graphics g = jpg.createGraphics();
              g.setClip(0, 0, (int)bounds.getWidth(), (int)bounds.getHeight());
              nodeLayer.paintComponent(g);
              edgeLayer.paintComponent(g);
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
          evolution.setTotalFiles(graphViewer.getEvolution().getTotalFiles());
          evolution.setTotalLines(graphViewer.getEvolution().getTotalLines());
        }
        try {
          gif.close();
        } catch (IOException ex) {
          Logger.getLogger(ExportAction.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    return (double) line / (double)exportableGraphViewer.getEvolution().getTotalLines();
  }
  
  private static class ExportableGraphViewer extends EvolutionGraphViewer{

    public ExportableGraphViewer(EvolutionGraph graph, HashMap<String, TIntObjectHashMap<String>> node_lists, CommunityPlacer pl) {
      super(graph, node_lists, pl);
    }
    
  }
}
