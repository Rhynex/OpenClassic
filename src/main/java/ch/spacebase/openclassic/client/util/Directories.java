package ch.spacebase.openclassic.client.util;

import java.io.File;
import ch.spacebase.openclassic.api.OpenClassic;

public class Directories {
	
	private static File getMinecraftDirectory() {
		File result = null;
	    String os = System.getProperty("os.name").toLowerCase();
	    if (os.contains("win")) {
	        result = new File(System.getenv("APPDATA"), ".minecraft_classic/");
	    } else if (os.contains("mac")) {
	    	result = new File(System.getProperty("user.home"), "/Library/Application Support/minecraft_classic");
	    } else if (os.contains("linux") || os.contains("solaris")) {
	    	result = new File(System.getProperty("user.home"), ".minecraft_classic/");
	    } else {
	    	result = new File(System.getProperty("user.home"), "minecraft_classic/");
	    }
	    
	    return result;
	}
	
	public static File getWorkingDirectory() {
		File result = null;
	    String os = System.getProperty("os.name").toLowerCase();
	    if (os.contains("win")) {
	        result = new File(System.getenv("APPDATA"), ".openclassic/");
	    } else if (os.contains("mac")) {
	    	result = new File(System.getProperty("user.home"), "/Library/Application Support/openclassic");
	    } else if (os.contains("linux") || os.contains("solaris")) {
	    	result = new File(System.getProperty("user.home"), ".openclassic/");
	    } else {
	    	result = new File(System.getProperty("user.home"), "openclassic/");
	    }
	    
	    if(!result.exists()) {
			if(getMinecraftDirectory().exists()) {
				getMinecraftDirectory().renameTo(result);
			} else {
		    	try {
		    		result.mkdirs();
		    	} catch(SecurityException e) {
		    		throw new RuntimeException(OpenClassic.getGame().getTranslator().translate("core.fail-working-dir"), e);
		    	}
			}
	    }
	    
	    return result;
	}
	
}
