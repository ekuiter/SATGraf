# Headless SATGraf

This fork of SATGraf visualizes community structure of propositional formulas in DIMACS format.
Operation can be automated on the shell.

```
git clone https://github.com/ekuiter/SATGraf
cd satgraf
./gradlew shadowJar
java -jar build/libs/dev-satgraf-1.0-SNAPSHOT-all.jar exp -f some-formula.cnf -o some-formula.jpg
```

# Notes

* The fgpu option is under development and may not work fully. 
* The circle layout algorithm works poorly with disjoint graphs.
* The Louvian method requires a non-disjoint graph
* Either -f or -u must be specified.   
    
# Usage

```
java -jar SatGraf.jar [exp] <options>

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
		jung - use Jung (ISOM)
		fr - An implementation of the Fruchtermon and Reingold force directed layout algorithm, that selects a random set of nodes (rather than all pairs)
	-m --format(default = auto) The format of the file, and desired graph representation
	Must be one of:
		auto - accepts: 
			.cnf - Load a DIMACS format file into a VIG and compute communities
			.sb - Load a graph representation from SB (JSON) file
		literal - accepts: 
			.cnf - Load a DIMACS format file into a LIG and compute communities
	-n --node-color(default = auto) node edge colouring implementation to use
	Must be one of: 
		auto - Nodes are blue
	-e --edge-color(default = auto) The edge colouring implementation to use
	Must be one of: 
		auto - Edges are coloured according to their community (or white for intercommunity)
```