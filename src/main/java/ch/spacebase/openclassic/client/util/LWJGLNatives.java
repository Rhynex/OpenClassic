package ch.spacebase.openclassic.client.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import ch.spacebase.openclassic.api.OpenClassic;

import com.mojang.minecraft.Minecraft;

public class LWJGLNatives {

	public static void load(File dir) {
	    String os = System.getProperty("os.name").toLowerCase();
		if(os.contains("linux")) {
			load(dir.getPath(), "libjinput-linux.so", "86");
			load(dir.getPath(), "libjinput-linux64.so", "64");
			load(dir.getPath(), "liblwjgl.so", "86");
			load(dir.getPath(), "liblwjgl64.so", "64");
			load(dir.getPath(), "libopenal.so", "86");
			load(dir.getPath(), "libopenal64.so", "64");
		} else if(os.contains("solaris")) {
			load(dir.getPath(), "liblwjgl.so", "86");
			load(dir.getPath(), "liblwjgl64.so", "64");
			load(dir.getPath(), "libopenal.so", "86");
			load(dir.getPath(), "libopenal64.so", "64");
		} else if(os.contains("win")) {
			load(dir.getPath(), "OpenAL64.dll", "64");
			load(dir.getPath(), "OpenAL32.dll", "86");
			load(dir.getPath(), "lwjgl64.dll", "64");
			load(dir.getPath(), "lwjgl.dll", "86");
			load(dir.getPath(), "jinput-raw_64.dll", "64");
			load(dir.getPath(), "jinput-raw.dll", "86");
			load(dir.getPath(), "jinput-dx8_64.dll", "64");
			load(dir.getPath(), "jinput-dx8.dll", "86");
		} else if(os.contains("mac")) {
			load(dir.getPath(), "openal.dylib", "both");
			load(dir.getPath(), "liblwjgl.jnilib", "both");
			load(dir.getPath(), "libjinput-osx.jnilib", "both");
		} else {
			throw new RuntimeException(OpenClassic.getGame().getTranslator().translate("core.no-lwjgl"));
		}
		
		System.setProperty("java.library.path", dir.getPath());
		System.setProperty("org.lwjgl.librarypath", dir.getPath());
		System.setProperty("net.java.games.input.librarypath", dir.getPath());
	}

	private static void load(String dir, String lib, String arch) {
		File file = new File(dir + "/" + lib);
		
		try {
			if(file.exists()) {
				InputStream in = Minecraft.class.getResourceAsStream("/" + lib);
				InputStream fin = new FileInputStream(file);
				try {
					if(IOUtils.contentEquals(in, fin)) {
						if(System.getProperty("os.arch").contains(arch) || arch.equals("both")) {
							System.load(file.getPath());
						}
						
						return;
					}
				} finally {
					IOUtils.closeQuietly(in);
					IOUtils.closeQuietly(fin);
				}
			}
			
			InputStream in = Minecraft.class.getResourceAsStream("/" + lib);
			System.out.println("Writing " + lib + " to " + file.getPath());
			IOUtils.copy(in, new FileOutputStream(file));
			IOUtils.closeQuietly(in);
			if(System.getProperty("os.arch").contains(arch) || arch.equals("both")) {
				System.load(file.getPath());
			}
		} catch (Exception e) {
			System.err.println(String.format(OpenClassic.getGame().getTranslator().translate("core.fail-unpack"), lib));
			e.printStackTrace();
		}
	}

}
