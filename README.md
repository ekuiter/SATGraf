Installation:

unzip satgraf.zip

cd satgraf

cd solvers/minisat

make

cd ../../


Notes:
the fgpu layout algorithm is currently under development and may not work as expected. 
Either -f or -u must be specified.


Usage: java -jar SatGraf.jar [com|imp|evo|evo2] <options>
--------------------------------
com
	-f --file(default = null) The file (either .cnf or .sb)
	Must be an existing file For which you have: read permissions
	-u --url(default = null) A file URL (either .cnf or .sb)
	-c --community(default = ol) The community detection algorithm
	Must be one of: ol,cnm
	-p --pattern(default = null) A list of regex expressions to group variables (not yet implemented)
	-l --layout(default = f) The layout algorithm to use
	Must be one of: f,fgpu,grid,kk
	
--------------------------------
--------------------------------
imp
	Not currently supported
	
--------------------------------
--------------------------------
evo
	-f --file(default = null) The file (either .cnf or .sb)
	Must be an existing file For which you have: read permissions
	-s --solver(default = solvers/minisat/minisat) The location of the modified solver
	Must be an existing file For which you have: EXECUTE permissions
	-c --community(default = ol) The community detection algorithm
	Must be one of: ol,cnm
	-p --pattern(default = null) A list of regex expressions to group variables (not yet implemented)
	-l --layout(default = f) The layout algorithm to use
	Must be one of: f,fgpu,grid,kk
	
--------------------------------
--------------------------------
evo2
	-f --file(default = null) The file (either .cnf or .sb)
	Must be an existing file For which you have: read permissions
	-u --url(default = null) A file URL (either .cnf or .sb)
	-s --solver(default = solvers/minisat/minisat) The location of the modified solver
	Must be an existing file For which you have: execute permissions
	-c --community(default = ol) The community detection algorithm
	Must be one of: ol,cnm,l
	-p --pattern(default = null) A list of regex expressions to group variables (not yet implemented)
	-l --layout(default = f) The layout algorithm to use
	Must be one of: f,fgpu,grid,kk,c
	-o --observer(default = null) The observer to use
	Must be one of: VSIDST,VSIDSS
