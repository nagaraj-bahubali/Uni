package org.processmining.plugins;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JFrame;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;

/**
 * To run this file you need below files
 * events_log.xes, input.xes, and additional jar file OpenXES-20181205.jar
 * 
 * Place ProcessMiner.java file under same package: org.processmining.plugins
 * which is used by this file
 */

public class NgramPlugin {
	 
	@Plugin
	        (name = "ngram-plugin", 
			parameterLabels = {"eventLogFile","inputFile"}, 
			returnLabels = {"probability matrix of events" }, 
			returnTypes = {JFrame.class }, 
			userAccessible = true, 
			help = " This plugin gives the probability distribution of "
					+ "different events across n-grams of varying length for a given event log")
	
	@UITopiaVariant
	         (affiliation = "University of Koblenz and Landau", 
	          author = "Nagaraj Bahubali", 
	          email = "nagarajbahubali@uni-koblenz")
	
	public static JFrame helloWorld(PluginContext context,XLog log,XLog input) throws Exception {
				
				ProcessMiner pm = new ProcessMiner();
				
				// Read the events log file
				List<List<String>> theLog = pm.preprocess(log);
				
				// Parse input file to fetch running process instance and n-value (ngram)
				HashMap<String, List<String>> input_data =  pm.parseInput(input);
				Entry<String, List<String>> entry = input_data.entrySet().iterator().next();
				int n_value = Integer.parseInt(entry.getKey());
				List<String> r_sequence = entry.getValue();
				
				// Generate ngram sequences from running process instance
				List<List<String>> n_gram_list = pm.generate_ngram(r_sequence, n_value);
				
				SortedMap<Integer,HashMap<String,Float>> results = new TreeMap<>();
				
				// Collect probability results for all possible n-grams
				for (int i = 0; i < n_gram_list.size(); i++) {
					
					List<String> n_gram = n_gram_list.get(i);
					int nth_value = n_gram.size();
					
					HashMap<String, Integer> next_events_map = pm.formNextEventsMap(n_gram, theLog);
					HashMap<String, Float> probMap = pm.calcEventsProbability(next_events_map);
					results.put(nth_value, probMap);

				}
				
				JFrame jframe = pm.constructTable(results,n_value);
				
				return jframe;
	}
}