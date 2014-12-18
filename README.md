Usage: java -jar SatGraf.jar [com|imp|evo|evo2] <options>

--------------------------------
com
-f --file(default = null) The file (either .cnf or .sb)
Must be an existing file For which you have: read permissions
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
-s --solver(default = /home/zacknewsham/satgraf/minisat/minisat) The location of the modified solver
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
-s --solver(default = /home/zacknewsham/satgraf/minisat/minisat) The location of the modified solver
-c --community(default = ol) The community detection algorithm
Must be one of: ol,cnm
-p --pattern(default = null) A list of regex expressions to group variables (not yet implemented)
-l --layout(default = f) The layout algorithm to use
Must be one of: f,fgpu,grid,kk
--------------------------------
