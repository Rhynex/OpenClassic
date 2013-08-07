package com.mojang.minecraft.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.IOUtils;
import org.lwjgl.input.Keyboard;

import ch.spacebase.openclassic.api.OpenClassic;

public final class Bindings {
	
	public KeyBinding forwardKey = new KeyBinding("options.keys.forward", Keyboard.KEY_W);
	public KeyBinding leftKey = new KeyBinding("options.keys.left", Keyboard.KEY_A);
	public KeyBinding backKey = new KeyBinding("options.keys.back", Keyboard.KEY_S);
	public KeyBinding rightKey = new KeyBinding("options.keys.right", Keyboard.KEY_D);
	public KeyBinding jumpKey = new KeyBinding("options.keys.jump", Keyboard.KEY_SPACE);
	public KeyBinding buildKey = new KeyBinding("options.keys.blocks", Keyboard.KEY_B);
	public KeyBinding chatKey = new KeyBinding("options.keys.chat", Keyboard.KEY_T);
	public KeyBinding fogKey = new KeyBinding("options.keys.toggle-fog", Keyboard.KEY_F);
	public KeyBinding saveLocKey = new KeyBinding("options.keys.save-loc", Keyboard.KEY_RETURN);
	public KeyBinding loadLocKey = new KeyBinding("options.keys.load-loc", Keyboard.KEY_R);
	public KeyBinding speedHackKey = new KeyBinding("options.keys.speedhack", Keyboard.KEY_LCONTROL);
	public KeyBinding flyDownKey = new KeyBinding("options.keys.fly-down", Keyboard.KEY_LSHIFT);
	public KeyBinding[] bindings;
	private File file;

	public Bindings(File path) {
		this.bindings = new KeyBinding[] { this.forwardKey, this.leftKey, this.backKey, this.rightKey, this.jumpKey, this.buildKey, this.chatKey, this.fogKey, this.saveLocKey, this.loadLocKey, this.speedHackKey, this.flyDownKey };
		this.file = new File(path, "options.txt");
		this.load();
	}

	public final String getBinding(int key) {
		return this.getBindingName(key) + ": " + this.getBindingValue(key);
	}

	public final String getBindingName(int key) {
		return OpenClassic.getGame().getTranslator().translate(this.bindings[key].name);
	}

	public final String getBindingValue(int key) {
		return Keyboard.getKeyName(this.bindings[key].key);
	}

	public final void setBinding(int key, int keyId) {
		this.bindings[key].key = keyId;
		this.save();
	}

	private void load() {
		BufferedReader reader = null;
		try {
			if(this.file.exists()) {
				reader = new BufferedReader(new FileReader(this.file));
				String line = null;
				while((line = reader.readLine()) != null) {
					String[] setting = line.split(":");
					for(int index = 0; index < this.bindings.length; index++) {
						if(setting[0].equals("key_" + this.bindings[index].key)) {
							this.bindings[index].key = Integer.parseInt(setting[1]);
						}
					}
				}
			}
		} catch(IOException e) {
			OpenClassic.getLogger().severe(OpenClassic.getGame().getTranslator().translate("core.fail-options-load"));
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	public void save() {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(this.file));
			for(int binding = 0; binding < this.bindings.length; binding++) {
				writer.println("key_" + this.bindings[binding].key + ":" + this.bindings[binding].key);
			}
		} catch(Exception e) {
			OpenClassic.getLogger().severe(OpenClassic.getGame().getTranslator().translate("core.fail-options-save"));
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

}
