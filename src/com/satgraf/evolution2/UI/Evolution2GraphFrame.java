package com.satgraf.evolution2.UI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.regex.Pattern;

import com.satlib.NamedFifo;
import com.satgraf.graph.UI.GraphCanvasPanel;
import com.satgraf.community.UI.CommunityCanvas;
import com.satgraf.community.UI.CommunityGraphFrame;
import com.satlib.community.CommunityGraphViewer;

public class Evolution2GraphFrame extends CommunityGraphFrame {
	
	static String dumpFileDirectory = System.getProperty("user.dir") + "/minisat/piping/";
	static String pipeFileName = dumpFileDirectory + "myPipe.txt";
	static String outputDirectory = dumpFileDirectory + "output/";
	
	Evolution2Grapher grapher;
	int fileNumber = 0;
	int lineNumber = 0;
	static int maxLinesPerFile = 10000;
	PrintWriter writer = null;
	
    public Evolution2GraphFrame(CommunityGraphViewer graphViewer, HashMap<String, Pattern> patterns, Evolution2Grapher grapher) {
      super(graphViewer, patterns);
      this.grapher = grapher;
      createOutputFolder();
    }

    public void init(){
      super.init();
    }
    
    public String toJson(){
      StringBuilder json = new StringBuilder(super.toJson());
      
      return json.toString();
    }
    
    public void show() {
        if(graphViewer != null) {
            canvasPanel = new GraphCanvasPanel(new CommunityCanvas(graphViewer));
            panel = new Evolution2OptionsPanel(this, getGraphViewer(), patterns.keySet());
            super.show();
            buildEvolutionFile();
        } else {
            super.show();
        }
    }

    public void buildEvolutionFile() {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {

                    NamedFifo fifo = new NamedFifo(pipeFileName);
                    fifo.create();

                    Runtime.getRuntime().exec(String.format(grapher.getMinisat().concat(" %s"), grapher.getDimacsFile().getAbsolutePath()));
                    BufferedReader reader = new BufferedReader(new FileReader(pipeFileName));
                    String line;

                    while((line = reader.readLine()) != null) {
                        outputLine(line);
                    }

                    closeWriter();
                    reader.close();
                    fifo.getFile().delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
	}
	  
	private void closeWriter() {
		((Evolution2OptionsPanel)panel).newFileReady(lineNumber);
		writer.close();
	}
	
	private void outputLine(String line) {
		if (writer == null || lineNumber == maxLinesPerFile) {
			if (lineNumber == maxLinesPerFile)
				closeWriter();
			
			try {
				writer = new PrintWriter(outputDirectory + fileNumber + ".txt", "UTF-8");
			} catch (Exception e) {
				System.out.println("Unable to create output file. Please make sure that you have the proper permissions.");
			}
			
			lineNumber = 0;
			fileNumber++;
		}
		
		writer.println(line);
		lineNumber++;
	}
	
	private void createOutputFolder() {
		File dir = new File(outputDirectory);
		String s = dir.getAbsolutePath();
		try {
			if (dir.exists()) {
				for (File f : dir.listFiles()) {
					f.delete();
				}
				
				dir.delete();
			}
			dir.mkdir();
		} catch (Exception e) {
			System.out.println("Unabled to create directory in minisat/. Do you have the correct permissions?");
		}
	}
  
    @Override
    public com.satgraf.actions.OpenAction getOpenAction(){
      return new OpenAction(this);
    }  

    @Override
    public com.satgraf.actions.ExportAction getExportAction(){
      return new ExportAction(this);
    }
}


