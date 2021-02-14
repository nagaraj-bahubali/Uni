package org.processmining.plugins;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class ProcessMiner {
	
	
	  List<List<String>> preprocess(XLog log) throws Exception {
		
		/**
	     * Takes input in the form of XLog and transforms it
	     * in the forms of List of event traces
	     */
		
		List<List<String>> log_data = new ArrayList<>();
		
		for (XTrace trace : log) {

			List<String> event_trace = new ArrayList<>();
			for (XEvent event : trace) {
				XAttributeMap attrMap = event.getAttributes();

				String event_name = attrMap.get("concept:name").toString();
				event_trace.add(event_name);

			}
			log_data.add(event_trace);
		}

		return log_data;
		
	}
	
	  HashMap<String, List<String>> parseInput(XLog log) throws Exception {	
		
		/**
	     * Takes input in the form of XLog and extracts
	     * the running process instance and n-value
	     */
		
		XTrace r_sequence = log.get(0);
		
		List<String> event_trace = new ArrayList<>();
		for (XEvent event : r_sequence) {
			XAttributeMap attrMap = event.getAttributes();

			String event_name = attrMap.get("concept:name").toString();
			event_trace.add(event_name);

		}
		
		String n_value = log.get(1).get(0).getAttributes().get("concept:name").toString();
		
		HashMap<String,List<String>> input_data = new HashMap<>();
		
		input_data.put(n_value, event_trace);
		
		return input_data;
		
	}
	

	 List<List<String>> generate_ngram(List<String> r_sequence, int n_value) {
		
		/**
	     * Takes the running process instance and generates
	     * all possible ngrams
	     */

		List<List<String>> n_gram_list = new ArrayList<>();
		int len = r_sequence.size();

		for (int i = 1; i <= n_value; i++) {
			List<String> i_gram = r_sequence.subList(len - i, len);
			n_gram_list.add(i_gram);
		}

		return n_gram_list;
	}

	 HashMap<String, Integer> formNextEventsMap(List<String> r_sequence, List<List<String>> log_data) {
		
		/**
	     * Takes an ngram and creates a map with next events as keys
	     * and count of those next events as values
	     */

		HashMap<String, Integer> next_events_map = new HashMap<>();

		for (List<String> event_trace : log_data) {

			int r_sequence_index = Collections.indexOfSubList(event_trace, r_sequence);

			if (r_sequence_index >= 0) {

				int next_event_index = r_sequence_index + r_sequence.size();

				if (next_event_index <= event_trace.size() - 1) {

					String next_event = event_trace.get(next_event_index);

					if (next_events_map.containsKey(next_event)) {
						next_events_map.put(next_event, next_events_map.get(next_event) + 1);
					} else {
						next_events_map.put(next_event, 1);
					}

				}
			}

		}
		return next_events_map;
	}

	 HashMap<String, Float> calcEventsProbability(HashMap<String, Integer> next_events_map) {
		
		/**
	     * calculates the probability of next occurring events
	     */

		HashMap<String, Float> probMap = new HashMap<>();

		int total_count = 0;
		for (int count : next_events_map.values()) {
			total_count += count;
		}

		for (Entry<String, Integer> entry : next_events_map.entrySet()) {
			String event_name = entry.getKey();
			float probability = (float) entry.getValue() / total_count;
			probMap.put(event_name, probability);
		}

		return probMap;
	}
	
	
	 static String[][] getTableData(SortedMap<Integer,HashMap<String,Float>> results) {
		
		/**
	     * Forms the contents of table (row and column values)
	     * for displaying as output 
	     */
		
		List<String> unique_events = new ArrayList<>(results.get(1).keySet());
		
		int row_size = unique_events.size();
		int column_size = results.size()+1;
		String[][] tabledata = new String[row_size][column_size];
		
		for(int i=0;i<row_size;i++) {
			
			String unique_event_name = unique_events.get(i);
			
			for(int j=0;j<column_size;j++) {
				
				if(j==0) {
					tabledata[i][0] = unique_event_name;
					continue;
				}
				
				HashMap<String, Float> probMap = results.get(j);
				
				if(probMap.containsKey(unique_event_name)) {
					tabledata[i][j] = Float.toString(probMap.get(unique_event_name));
				}
				else {
					tabledata[i][j] = Float.toString((float) 0);
				}
				
			}
		}
		
		
	 return tabledata;
	}
	
	 JFrame constructTable(SortedMap<Integer,HashMap<String,Float>> results,int n_value) {
		
		String column[] = new String[n_value+1];
		column[0] = "EVENT_NAMES ↓ | N-GRAM → ";
		
		for(int i=1;i<=n_value;i++) {
			column[i] = Integer.toString(i);
		}
		
		
	    JFrame f=new JFrame();    
	    JTable jt=new JTable(getTableData(results),column);    
	    TableColumnModel columnModel = jt.getColumnModel();
	    columnModel.getColumn(0).setPreferredWidth(400);
	    
	    JTableHeader th = jt.getTableHeader();
	    th.setFont(new Font("Serif", Font.BOLD, 15));
	    JScrollPane sp=new JScrollPane(jt);    
	    f.add(sp);          
	    f.setSize(1000,700);    
	    f.setVisible(true); 
	    
	    return f;
		
	}
}
