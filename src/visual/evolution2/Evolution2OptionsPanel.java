package visual.evolution2;

import java.util.Collection;

import visual.UI.OptionsPanel;
import visual.community.CommunityGraphViewer;
import visual.community.CommunityOptionsPanel;

public class Evolution2OptionsPanel extends CommunityOptionsPanel {
	
	Evolution2Scaler scaler;
	CommunityGraphViewer graph;
	
	public Evolution2OptionsPanel(CommunityGraphViewer graph, Collection<String> groups) {
		super(graph, groups, false);
		setGraph(graph);
		this.graph = graph;
		buildLayout(graph);
	}
	
	private void buildLayout(CommunityGraphViewer graph) {
		OptionsPanel op = getOptionsPanel();
		scaler = new Evolution2Scaler(graph);
		op.setCustomComponent(scaler);
	}
	
	public void setGraph(CommunityGraphViewer graph) {
		super.setGraph(graph);
	}

	public void newFileReady(int numLinesInFile) {
		scaler.newFileReady(numLinesInFile);
	}
}
