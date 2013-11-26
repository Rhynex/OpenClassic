package ch.spacebase.openclassic.server.sound;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.sound.AudioManager;
import ch.spacebase.openclassic.game.network.msg.custom.audio.AudioPlayMessage;
import ch.spacebase.openclassic.game.network.msg.custom.audio.AudioRegisterMessage;
import ch.spacebase.openclassic.game.network.msg.custom.audio.MusicStopMessage;
import ch.spacebase.openclassic.server.player.ServerPlayer;

public class ServerAudioManager implements AudioManager {

	private final Map<String, List<URL>> sounds = new HashMap<String, List<URL>>();
	private final Map<String, List<URL>> music = new HashMap<String, List<URL>>();

	@Override
	public void registerSound(String sound, URL file, boolean included) {
		Validate.notNull(sound, "Sound cannot be null.");
		Validate.notNull(file, "URL cannot be null.");
		if(!this.sounds.containsKey(sound)) this.sounds.put(sound, new ArrayList<URL>());
		this.sounds.get(sound).add(file);

		for(Player player : OpenClassic.getServer().getPlayers()) {
			((ServerPlayer) player).getSession().send(new AudioRegisterMessage(sound, file.getPath(), included, false));
		}
	}

	@Override
	public void registerMusic(String music, URL file, boolean included) {
		Validate.notNull(music, "Music cannot be null.");
		Validate.notNull(file, "URL cannot be null.");
		if(!this.music.containsKey(music)) this.music.put(music, new ArrayList<URL>());
		this.music.get(music).add(file);

		for(Player player : OpenClassic.getServer().getPlayers()) {
			((ServerPlayer) player).getSession().send(new AudioRegisterMessage(music, file.getPath(), included, true));
		}
	}

	@Override
	public boolean playSound(String sound, float volume, float pitch) {
		for(Player player : OpenClassic.getServer().getPlayers()) {
			((ServerPlayer) player).getSession().send(new AudioPlayMessage(sound, 0, 0, 0, volume, pitch, false, false));
		}

		return true;
	}

	@Override
	public boolean playSound(Player player, String sound, float volume, float pitch) {
		((ServerPlayer) player).getSession().send(new AudioPlayMessage(sound, 0, 0, 0, volume, pitch, false, false));
		return true;
	}

	@Override
	public boolean playSound(String sound, float x, float y, float z, float volume, float pitch) {
		for(Player player : OpenClassic.getServer().getPlayers()) {
			((ServerPlayer) player).getSession().send(new AudioPlayMessage(sound, x, y, z, volume, pitch, false, false));
		}

		return true;
	}

	@Override
	public boolean playSound(Player player, String sound, float x, float y, float z, float volume, float pitch) {
		((ServerPlayer) player).getSession().send(new AudioPlayMessage(sound, x, y, z, volume, pitch, false, false));
		return true;
	}

	@Override
	public boolean playMusic(String music) {
		for(Player player : OpenClassic.getServer().getPlayers()) {
			((ServerPlayer) player).getSession().send(new AudioPlayMessage(music, 0, 0, 0, 1, 1, true, false));
		}

		return true;
	}

	@Override
	public boolean playMusic(Player player, String music) {
		((ServerPlayer) player).getSession().send(new AudioPlayMessage(music, 0, 0, 0, 1, 1, true, false));
		return true;
	}

	@Override
	public boolean playMusic(String music, boolean loop) {
		for(Player player : OpenClassic.getServer().getPlayers()) {
			((ServerPlayer) player).getSession().send(new AudioPlayMessage(music, 0, 0, 0, 1, 1, true, true));
		}

		return true;
	}

	@Override
	public boolean playMusic(Player player, String music, boolean loop) {
		((ServerPlayer) player).getSession().send(new AudioPlayMessage(music, 0, 0, 0, 1, 1, true, true));
		return true;
	}

	@Override
	public boolean isPlayingMusic() {
		return true;
	}

	@Override
	public void stopMusic() {
		for(Player player : OpenClassic.getServer().getPlayers()) {
			((ServerPlayer) player).getSession().send(new MusicStopMessage("all_music"));
		}
	}

	@Override
	public void stopMusic(Player player) {
		((ServerPlayer) player).getSession().send(new MusicStopMessage("all_music"));
	}

	@Override
	public boolean isPlaying(String music) {
		return false;
	}

	@Override
	public void stop(String music) {
		for(Player player : OpenClassic.getServer().getPlayers()) {
			((ServerPlayer) player).getSession().send(new MusicStopMessage(music));
		}
	}

	@Override
	public void stop(Player player, String music) {
		((ServerPlayer) player).getSession().send(new MusicStopMessage(music));
	}

}
