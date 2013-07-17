package ch.spacebase.openclassic.client;

import com.mojang.minecraft.Minecraft;

/**
 * @author Steveice10 <Steveice10@gmail.com>
 */
public class MinecraftStandalone {
	
	public static void start(String[] args) {
		new Thread(new Minecraft(null, 854, 480), "Client-Main").start();
	}

}
