package ch.spacebase.openclassic.client.sound;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.sound.AudioManager;
import ch.spacebase.openclassic.api.math.MathHelper;
import ch.spacebase.openclassic.api.math.Vector;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.client.ClassicClient;
import ch.spacebase.openclassic.client.player.ClientPlayer;

import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryJavaSound;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

public class ClientAudioManager implements AudioManager {
	
	private static final Random rand = new Random();
	private static int nextSoundId = 0;
	
	private final Map<String, List<URL>> sounds = new HashMap<String, List<URL>>();
	private final Map<String, List<URL>> music = new HashMap<String, List<URL>>();
	
	private SoundSystem system;;
	public long lastBGM = System.currentTimeMillis();
	
	public ClientAudioManager() {
		Class<? extends Library> lib = Library.class;
		
		if(SoundSystem.libraryCompatible(LibraryLWJGLOpenAL.class)) {
			lib = LibraryLWJGLOpenAL.class;
		} else if(SoundSystem.libraryCompatible(LibraryJavaSound.class)) {
			lib = LibraryJavaSound.class;
		}
		
		try {
			this.system = new SoundSystem(lib);
			SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
			SoundSystemConfig.setCodec("wav", CodecWav.class);
		} catch(SoundSystemException e) {
			e.printStackTrace();
		}
		
		// TODO: More sounds
		this.registerSound("step.cloth", ClassicClient.class.getResource("/sounds/step/cloth1.ogg"), true);
		this.registerSound("step.cloth", ClassicClient.class.getResource("/sounds/step/cloth2.ogg"), true);
		this.registerSound("step.dirt", ClassicClient.class.getResource("/sounds/step/dirt1.ogg"), true);
		this.registerSound("step.dirt", ClassicClient.class.getResource("/sounds/step/dirt2.ogg"), true);
		this.registerSound("step.grass", ClassicClient.class.getResource("/sounds/step/grass1.ogg"), true);
		this.registerSound("step.grass", ClassicClient.class.getResource("/sounds/step/grass2.ogg"), true);
		this.registerSound("step.gravel", ClassicClient.class.getResource("/sounds/step/gravel1.ogg"), true);
		this.registerSound("step.gravel", ClassicClient.class.getResource("/sounds/step/gravel2.ogg"), true);
		this.registerSound("step.metal", ClassicClient.class.getResource("/sounds/step/metal1.ogg"), true);
		this.registerSound("step.metal", ClassicClient.class.getResource("/sounds/step/metal2.ogg"), true);
		this.registerSound("step.sand", ClassicClient.class.getResource("/sounds/step/sand1.ogg"), true);
		this.registerSound("step.sand", ClassicClient.class.getResource("/sounds/step/sand2.ogg"), true);
		this.registerSound("step.stone", ClassicClient.class.getResource("/sounds/step/stone1.ogg"), true);
		this.registerSound("step.stone", ClassicClient.class.getResource("/sounds/step/stone2.ogg"), true);
		this.registerSound("step.wood", ClassicClient.class.getResource("/sounds/step/wood1.ogg"), true);
		this.registerSound("step.wood", ClassicClient.class.getResource("/sounds/step/wood2.ogg"), true);
		
		this.registerSound("generic.explode", ClassicClient.class.getResource("/sounds/generic/explode.ogg"), true);
	}
	
	public void update(ClientPlayer player) {
		if(player != null && OpenClassic.getClient().isInGame()) {
			Vector forward = MathHelper.toForwardVec(player.getPosition().getYaw(), player.getPosition().getPitch());
			this.system.setListenerPosition(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
			this.system.setListenerOrientation(forward.getX(), forward.getY(), forward.getZ(), (float) Math.sin(Math.toRadians(player.getPosition().getPitch())), (float) Math.sin(Math.toRadians(player.getPosition().getYaw())), 1);
		} else {
			this.system.setListenerPosition(0, 0, 0);
			this.system.setListenerOrientation(0, 0, -1, 0, 1, 0);
		}
	}
	
	public void cleanup() {
		this.system.cleanup();
	}
	
	public void registerSound(String sound, URL file, boolean included) {
		if(!included) {
			this.download(file);
		}
		
		if(!this.sounds.containsKey(sound)) this.sounds.put(sound, new ArrayList<URL>());
		this.sounds.get(sound).add(file);
	}
	
	public void registerMusic(String music, URL file, boolean included) {
		if(!included) {
			this.download(file);
		}
		
		if(!this.music.containsKey(music)) this.music.put(music, new ArrayList<URL>());
		this.music.get(music).add(file);
		this.system.backgroundMusic(music, file, file.getFile(), false);
		this.system.backgroundMusic(music + "_loop", file, file.getFile(), true);
	}
	
	private void download(URL url) {
		File file = new File(OpenClassic.getClient().getDirectory(), "cache/music/" + url.getFile());
		if(!file.exists()) {
			if(!file.getParentFile().exists()) {
				try {
					file.getParentFile().mkdirs();
				} catch(SecurityException e) {
					e.printStackTrace();
				}
			}

			try {
				file.createNewFile();
			} catch(SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println(String.format(OpenClassic.getGame().getTranslator().translate("http.downloading"), file.getName()));

			byte[] data = new byte[4096];
			DataInputStream in = null;
			DataOutputStream out = null;

			try {
				in = new DataInputStream(url.openStream());
				out = new DataOutputStream(new FileOutputStream(file));

				int length = 0;
				while(OpenClassic.getClient().isRunning()) {
					length = in.read(data);
					if (length < 0) break;
					out.write(data, 0, length);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (in != null)
						in.close();
					if (out != null)
						out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			System.out.println(String.format(OpenClassic.getGame().getTranslator().translate("http.downloaded"), file.getName()));
		}
	}
	
	public boolean playSound(String sound, float x, float y, float z, float volume, float pitch) {
		if(!OpenClassic.getClient().getConfig().getBoolean("options.sound", true)) return true;
		
		List<URL> files = this.sounds.get(sound);
		if(files != null) {
			URL file = files.get(rand.nextInt(files.size()));
			
			nextSoundId = (nextSoundId + 1) % 256;
			String source = "sound_" + nextSoundId;

			float attenuation = 16;
			if(volume > 1) attenuation = volume * 16;
			
			this.system.newSource(volume > 1, source, file, file.getFile(), false, x, y, z, SoundSystemConfig.ATTENUATION_LINEAR, attenuation);
			
			if(volume > 1) volume = 1;
			this.system.setVolume(source, volume);
			this.system.setPitch(source, pitch);
			
			this.system.play(source);
			return true;
		}
		
		return false;
	}
	
	public boolean playSound(String sound, float volume, float pitch) {
		return this.playSound(sound, this.system.getListenerData().position.x, this.system.getListenerData().position.y, this.system.getListenerData().position.z, volume, pitch);
	}
	
	public boolean playMusic(String music) {
		return this.playMusic(music, false);
	}
	
	public boolean playMusic(String music, boolean loop) {
		if(!OpenClassic.getClient().getConfig().getBoolean("options.music", true)) return true;
		
		List<URL> files = this.music.get(music);
		if(files != null) {
			if(this.isPlaying(music)) return true;
			if(this.isPlayingMusic()) {
				this.stopMusic();
			}
			
			this.system.play(music + (loop ? "_loop" : ""));
			return true;
		}
		
		return false;
	}
	
	public boolean isPlayingMusic() {
		for(String music : this.music.keySet()) {
			if(this.isPlaying(music)) return true;
		}
		
		return false;
	}
	
	public void stopMusic() {
		for(String music : this.music.keySet()) {
			if(this.isPlaying(music)) this.stop(music);
		}
	}
	
	public boolean isPlaying(String music) {
		return this.system.playing(music) || this.system.playing(music + "_loop");
	}
	
	public void stop(String music) {
		this.system.stop(music);
		this.system.stop(music + "_loop");
	}

	@Override
	public boolean playSound(Player player, String sound, float volume, float pitch) {
		return this.playSound(sound, volume, pitch);
	}

	@Override
	public boolean playSound(Player player, String sound, float x, float y, float z, float volume, float pitch) {
		return this.playSound(sound, x, y, z, volume, pitch);
	}

	@Override
	public boolean playMusic(Player player, String music) {
		return this.playMusic(music);
	}

	@Override
	public boolean playMusic(Player player, String music, boolean loop) {
		return this.playMusic(music, loop);
	}

	@Override
	public void stopMusic(Player player) {
		this.stopMusic();
	}

	@Override
	public void stop(Player player, String music) {
		this.stop(music);
	}
	
}