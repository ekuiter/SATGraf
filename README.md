#SATLib
SATGraf is the graphical UI for visualising the evolution of a SAT solver. It uses [SATLib](https://bitbucket.org/znewsham/satlib) as the backend for strucural information and evolution data.

#Installation:     
    unzip satgraf.zip   
    cd satgraf   
    cd solvers/minisat   
    make   
    cd ../../   

#Notes:   
    The fgpu option is under development and may not work fully. 
    The circle layout algorithm works poorly with disjoint graphs.
    The Louvian method requires a non disjoint graph
    Either -f or -u must be specified.   
    
#Usage: java -jar SatGraf.jar [com|imp|evo] <options>

##com - View the static community representation of the formula.   
	-f --file(default = null) The file (either .cnf or .sb)   
	Must be an existing file For which you have: read permissions   
	-u --url(default = null) A file URL (either .cnf or .sb)   
	-c --community(default = ol) The community detection algorithm   
	Must be one of:    
		l - The Louvian community detection algorithm   
		ol - The Online community detection algorithm   
		cnm - The Clauset-Neuman-Moore community detection algorithm   
	-p --pattern(default = null) A list of regex expressions to group variables (not yet implemented)   
	-l --layout(default = f) The layout algorithm to use   
	Must be one of:    
		f - An implementation of the Fruchtermon and Reingold force directed layout algorithm   
		fgpu - A GPU implementation of the Fruchtermon and Reingold force directed layout algorithm   
		kk - An implementation of the Kamada-Kawai placement algorithm   
		c - A basic layout algorithm, nodes are layed out within a community using the FR layout algorithm. The communities are then layed out in a circle, with those most connected next to each other.   
		grid - A basic layout algorithm, nodes are layed out within a community in a grid pattern. The communities are then layed out on a grid   
		gkk - A basic layout algorithm, nodes are layed out within a community using the KK algorithm. The communities are then layed out on a grid   


##imp - View the graph and manually set the values of nodes to see how they propagate.   
	-f --file(default = null) The file (either .cnf or .sb)   
	Must be an existing file For which you have: read permissions   
	-u --url(default = null) A file URL (either .cnf or .sb)   
	-p --pattern(default = null) A list of regex expressions to group variables (not yet implemented)   
	-l --layout(default = f) The layout algorithm to use   
	Must be one of:    
		f - An implementation of the Fruchtermon and Reingold force directed layout algorithm   
		fgpu - A GPU implementation of the Fruchtermon and Reingold force directed layout algorithm   
		kk - An implementation of the Kamada-Kawai placement algorithm   
	   
##evo - View the evolution of the structure of the graph, with other evolution properties presented.   
	-f --file(default = null) The file (either .cnf or .sb)   
	Must be an existing file For which you have: read permissions   
	-u --url(default = null) A file URL (either .cnf or .sb)   
	-s --solver(default = solvers/minisat/minisat) The location of the modified solver   
	Must be an existing file For which you have: execute permissions   
	-c --community(default = ol) The community detection algorithm   
	Must be one of:    
		l - The Louvian community detection algorithm   
		ol - The Online community detection algorithm   
		cnm - The Clauset-Neuman-Moore community detection algorithm   
	-p --pattern(default = null) A list of regex expressions to group variables (not yet implemented)   
	-o --observers(default = null) A named evolution observer   
	Must be one of:    
		Q - A graphical representation of the evolution of Q over the solution of the solver (based on the selected community metric)   
		VSIDST - A graphical representation of the temporal locailty of the VSIDS decision heuristic   
		VSIDSS - A graphical representation of the spacial locailty of the VSIDS decision heuristic   
	-l --layout(default = f) The layout algorithm to use   
	Must be one of:    
		f - An implementation of the Fruchtermon and Reingold force directed layout algorithm   
		fgpu - A GPU implementation of the Fruchtermon and Reingold force directed layout algorithm   
		kk - An implementation of the Kamada-Kawai placement algorithm   
		c - A basic layout algorithm, nodes are layed out within a community using the FR layout algorithm. The communities are then layed out in a circle, with those most connected next to each other.   
		grid - A basic layout algorithm, nodes are layed out within a community in a grid pattern. The communities are then layed out on a grid   
		gkk - A basic layout algorithm, nodes are layed out within a community using the KK algorithm. The communities are then layed out on a grid


#Extending SATGraf:
SATGraf may be extendid with trivial ease in three areas, and with slightly more involvement in a fourth.
The first three extensions can be done without access to the sourcecode if desired.

##Strucutral detection
At the moment various community structure mechanisms are the only ones supported, however any structure that assigns nodes to groups may also be used.

1. Create a class that implements `com.satlib.community.CommunityMetric`
   This interface has a single method: `getCommunities` which detects the community structure and assigns variables to groups.
2. Choose a name for the implementation and register it with `com.satlib.community.CommunityMetricFactory` using the `register(String name, String description, Class implementation)` method. 
3. your chosen named implementation is now available as one of the options of the `-c` command line option.

##Layout algorithm
At the moment only single level layout algorithms are supported.

1. Create a class that either implements `com.satgraf.graph.placer.Placer` interface, or extends the `com.satgraf.graph.placer.AbstractPlacer` class. 
   The `Placer` interface has only four methods, which are self explanatory. `getX(Node)`, `getY(Node)`, `getNodeAtXY(int x,int y)` and `init()` - this function is accessed only once, the layout algorithm should be located here.
2. Choose a name for the implementation and register it with `com.satgraf.graph.placer.PlacerFactory` using the `register(String name, String description, Class implementation)` method.
2. Your chosen named implementation is now available as one of the options of the `-l` command line option.

##Supplementary evolution views
These views are observers of the evolution, and are notified whenever a variable or clause changes. They can show details about how different metrics change during the evolution.

1. Create a class that either implements either `com.satgraf.evolution.observers.VisualEvolutionObserver` or `com.satlib.evolution.observers.EvolutionObserver`. The only difference between these two interfaces is that one will be added as a view (to display graphical elements) and the other may only log to the console (or file).
2. Choose a name for the observer and register it with `com.satgraf.evolution.observers.EvolutionObserverFactory using the `register(String name, String description, Class implementation)` method.
3. Your chosen named observer is now available as one of the options of the `-o` command line option.

##Different graph representations
At the moment only the variable-incidence graph is supported, however the implementation of SATGraf doesnt care how the graph was built. It is possible to implement either new adapters for CNF that create literal-incidence graphs or clause-incidence graphs, or implement an adapter that allows non-CNF input. This requires modifying the source in some places, and will be "cleaned up" at a later date.

1. Create a class that implements `com.satlib.graph.GraphFactory` (e.g. `DimacsGraphFactory` which takes CNF and outputs a VIG). Implement `makeGraph(File)` and `makeGraph(URL)` in such a way that the provided input is converted to the desired graph representation
2. Modify the `getFactory` methods of `com.satlib.evolution.EvolutionGraphFactoryFactory` to return your GraphFactory implementation

#Implementing the SATGraf protocol in a solver
If implementing your solver in C++ 
