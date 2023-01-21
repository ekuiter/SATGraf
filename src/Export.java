import com.satgraf.community.UI.CommunityCanvas;
import com.satgraf.community.UI.CommunityGraphFrame;
import com.satgraf.community.UI.CommunityGraphViewer;
import com.satgraf.community.UI.ExportAction;
import com.satgraf.community.placer.CommunityPlacerFactory;
import com.satgraf.graph.color.EdgeColoringFactory;
import com.satgraf.graph.color.NodeColoringFactory;
import com.satgraf.graph.placer.Placer;
import com.satgraf.graph.placer.PlacerFactory;
import com.satlib.Progressive;
import com.satlib.community.CommunityGraphFactory;
import com.satlib.graph.GraphFactoryFactory;
import com.validatedcl.validation.CommandLine;
import com.validatedcl.validation.ValidatedOption;
import org.apache.commons.cli.*;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author zacknewsham
 */
public class Export {
    public static CommandLine commandLine;
    public static CommunityGraphViewer graphViewer;

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        Options options = CommunityGraphFrame.options();
        ValidatedOption o = new ValidatedOption("o", "output", true, "output JPEG file");
        o.setDefault("out.jpg");
        options.addOption(o);
        commandLine = new CommandLine(new GnuParser().parse(options, args), options);

        HashMap<String, String> patterns = new HashMap<>();
        if (commandLine.getCommandLine().getOptionValues("r") != null) {
            for (String pattern : commandLine.getCommandLine().getOptionValues("r")) {
                String[] parts = pattern.split(":", 2);
                patterns.put(parts[0], parts[1]);
            }
        }
        String fileName = commandLine.getOptionValue("f");

        File input = new File(fileName);
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        CommunityGraphFactory factory = (CommunityGraphFactory) GraphFactoryFactory.getInstance()
                .getByNameAndExtension(commandLine.getOptionValue("m"), extension, commandLine.getOptionValue("c"), patterns);
        reportProgress(factory, null);
        factory.makeGraph(input);

        Placer<?> p;
        if (factory.getGraph() instanceof Placer) {
            p = (Placer<?>) factory.getGraph();
        } else {
            p = CommunityPlacerFactory.getInstance().getByName(commandLine.getOptionValue("l"), factory.getGraph());
            if (p == null) {
                p = PlacerFactory.getInstance().getByName(commandLine.getOptionValue("l"), factory.getGraph());
            }
        }
        reportProgress(p, Export::export);
        graphViewer = new CommunityGraphViewer(null, factory.getNodeLists(), null);
        graphViewer.graph = factory.getGraph();
        graphViewer.setPlacer(p);
        graphViewer.setNodeColoring(NodeColoringFactory.getInstance().getByName(commandLine.getOptionValue("n"), graphViewer.graph));
        graphViewer.setEdgeColoring(EdgeColoringFactory.getInstance().getByName(commandLine.getOptionValue("e"), graphViewer.graph));
        factory.getMetric().getCommunities(graphViewer.getGraph());
    }

    public static void reportProgress(Progressive item, Runnable done) {
        java.util.Timer timer = new Timer();
        final int[] i = {0};
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                double value = item.getProgress();
                if (value >= 1) {
                    timer.cancel();
                    if (done != null)
                        done.run();
                } else if (!Double.isNaN(value) && i[0] % 4 == 0)
                    System.out.format("%.1f%%\n", value * 100);
                i[0]++;
            }
        }, 0, 500);
    }

    public static void export() {
//        int width = (int) graphViewer.getBounds().getWidth();
//        int height = (int) graphViewer.getBounds().getHeight();
        int width = 1024;
        int height = 1024;
        try {
            BufferedImage jpg = new BufferedImage(width,height, BufferedImage.TYPE_BYTE_INDEXED);
            Graphics g = jpg.createGraphics();
            g.setClip(0, 0, width, height);
            new CommunityCanvas(graphViewer).paint(g);
            Iterator<?> iter = ImageIO.getImageWritersByFormatName("jpeg");
            String o = commandLine.getOptionValue("o");
            FileImageOutputStream output = new FileImageOutputStream(new File(o));
            ImageWriter writer = (ImageWriter) iter.next();
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality((float) 0.75);   // an integer between 0 and
            writer.setOutput(output);
            IIOImage image = new IIOImage(jpg, null, null);
            writer.write(null, image, iwp);
            writer.dispose();
            output.close();
            System.exit(0);
        } catch (IOException ex) {
            Logger.getLogger(ExportAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
