# ngram-plugin
This is a plugin built for ProM tool as part of the course : Smart Process Analytics. The plugin predicts the next events based on the input logfile (xes format) and n value (ngram). 

**Instructions to run the plugin**
1) Setup the ProM project in eclipse in your local environment with the help of below links.
   > https://svn.win.tue.nl/trac/prom/wiki/setup/HowToBecomeAProMDeveloper  <br />
   > https://svn.win.tue.nl/trac/prom/wiki/ManuallyCheckingOutProM  <br />
   > https://svn.win.tue.nl/trac/prom/wiki/setup/RunningProM  <br />
   > https://svn.win.tue.nl/trac/prom/wiki/Plugins  <br />
   > https://www.promtools.org/doku.php?id=troubleshooting:start  <br />

2) Place `NgramPlugin.java` and `ProcessMiner.java`  under same package: org.processmining.plugins
3) Run the file `NgramPlugin.java` to launch ProM application.
4) You can use sample log file (`events_log.xes`) and input file (`input.xes`) as input to the plugin.
5) The running process instance and n-value has to be updated in `input.xes`.
6) Once everything is set you should import these files into ProM.

 

