Installation:   
unzip satgraf.zip   
cd satgraf   
cd solvers/minisat   
make   
cd ../../   
Notes:   
The fgpu option is under development and may not work fully. 
Either -f or -u must be specified.   
    
Usage: java -jar SatGraf.jar [com|imp|evo] <options>
--------------------------------   
    com - View the static community representation of the formula.   
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
	   
--------------------------------   
--------------------------------   
    imp - View the graph and manually set the values of nodes to see how they propagate.   
	-f --file(default = null) The file (either .cnf or .sb)   
	Must be an existing file For which you have: read permissions   
	-u --url(default = null) A file URL (either .cnf or .sb)   
	-p --pattern(default = null) A list of regex expressions to group variables (not yet implemented)   
	-l --layout(default = f) The layout algorithm to use   
	Must be one of:    
		f - An implementation of the Fruchtermon and Reingold force directed layout algorithm   
		fgpu - A GPU implementation of the Fruchtermon and Reingold force directed layout algorithm   
		kk - An implementation of the Kamada-Kawai placement algorithm   
	   
--------------------------------   
--------------------------------   
    evo - View the evolution of the structure of the graph, with other evolution properties presented.   
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
