package ch.spacebase.openclassic.client.util;

import java.util.HashMap;
import java.util.Map;

public class Profiler {

	private static Map<String, Long> tasks = new HashMap<String, Long>();
	
	public static void start(String task) {
		tasks.put(task, System.nanoTime());
	}
	
	public static double end(String task) {
		return (System.nanoTime() - tasks.get(task)) / 1000000.0;
	}
	
}
