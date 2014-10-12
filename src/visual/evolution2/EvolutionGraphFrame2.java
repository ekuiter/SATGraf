package visual.evolution2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.regex.Pattern;

import visual.NamedFifo;
import visual.UI.GraphCanvasPanel;
import visual.community.CommunityCanvas;
import visual.community.CommunityGraphFrame;
import visual.community.CommunityGraphViewer;

public class EvolutionGraphFrame2 extends CommunityGraphFrame {
	
	static String dumpFileDirectory = System.getProperty("user.dir") + "/minisat/piping/";
	static String pipeFileName = dumpFileDirectory + "myPipe.txt";
	static String outputDirectory = dumpFileDirectory + "output/";
	
	EvolutionGrapher2 grapher;
	int fileNumber = 0;
	int lineNumber = 0;
	static int maxLinesPerFile = 1000;
	PrintWriter writer = null;
	
    public EvolutionGraphFrame2(CommunityGraphViewer graphViewer, HashMap<String, Pattern> patterns, EvolutionGrapher2 grapher) {
      super(graphViewer, patterns);
      this.grapher = grapher;
      createOutputFolder();
    }

    public void init(){
      super.init();
    }

    public void show() {
        if(graphViewer != null) {
            canvasPanel = new GraphCanvasPanel(new CommunityCanvas(graphViewer));
            panel = new EvolutionOptionsPanel2(getGraphViewer(), patterns.keySet());
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
		((EvolutionOptionsPanel2)panel).newFileReady(lineNumber);
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
}


