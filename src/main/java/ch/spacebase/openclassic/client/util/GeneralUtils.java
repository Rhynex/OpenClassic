package ch.spacebase.openclassic.client.util;

import java.io.File;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.client.ClassicClient;

import com.mojang.minecraft.Minecraft;

public class GeneralUtils {

	public static Minecraft getMinecraft() {
		return ((ClassicClient) OpenClassic.getClient()).getMinecraft();
	}
	
	public static File getMinecraftDirectory() {
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
	    
	    if(!result.exists()) {
	    	try {
	    		result.mkdirs();
	    	} catch(SecurityException e) {
	    		throw new RuntimeException(OpenClassic.getGame().getTranslator().translate("core.fail-working-dir"), e);
	    	}
	    }
	    
	    return result;
	}
	
}
