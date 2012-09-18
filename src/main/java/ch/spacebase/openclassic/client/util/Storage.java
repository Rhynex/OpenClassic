package ch.spacebase.openclassic.client.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.spacebase.openclassic.api.block.custom.CustomBlock;

public class Storage {

	private static List<CustomBlock> clientBlocks = new ArrayList<CustomBlock>();
	private static Map<String, String> favorites = new HashMap<String, String>();
	public static File favoriteStore;
	
	private static final Map<String, Server> servers = new HashMap<String, Server>();
	
	public static List<CustomBlock> getClientBlocks() {
		return clientBlocks;
	}
	
	public static Map<String, String> getFavorites() {
		return favorites;
	}
	
	public static Map<String, Server> getServers() {
		return servers;
	}
	
	public static void loadFavorites(File dir) {
		favoriteStore = new File(dir, "favorites.txt");
		
		if(!favoriteStore.exists()) {
			try {
				favoriteStore.createNewFile();
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(favoriteStore));
			String line = "";
			
			while((line = reader.readLine()) != null) {
				String favorite[] = line.split(", ");
				favorites.put(favorite[0], favorite[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void saveFavorites() {
		if(favoriteStore == null) return;
		
		if(!favoriteStore.exists()) {
			try {
				favoriteStore.createNewFile();
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new FileWriter(favoriteStore));
			for(String favorite : favorites.keySet()) {
				writer.write(favorite + ", " + favorites.get(favorite));
				writer.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
