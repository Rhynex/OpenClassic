package ch.spacebase.openclassic.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.gui.MainScreen;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.api.util.GuiTextures;
import ch.spacebase.openclassic.client.ClassicClient;
import ch.spacebase.openclassic.client.mode.Mode;
import ch.spacebase.openclassic.client.mode.Multiplayer;

public class ClassicMainScreen extends MainScreen {

	private List<Chat> history = new CopyOnWriteArrayList<Chat>();
	private int width;
	private int height;
	private boolean setup = false;
	private Mode mode;
	private String hoveredPlayer;
	
	public ClassicMainScreen(Mode mode) {
		this.mode = mode;
	}
	
	public void setup(int width, int height) {
		this.width = width;
		this.height = height;
		this.setup = true;
	}
	
	public boolean isSetup() {
		return this.setup;
	}
	
	@Override
	public String getHoveredPlayer() {
		return this.hoveredPlayer;
	}

	@Override
	public void addChat(String message) {
		this.history.add(0, new Chat(message));
		while (this.history.size() > 50) {
			this.history.remove(this.history.size() - 1);
		}
	}

	@Override
	public List<String> getChat() {
		List<String> result = new ArrayList<String>();
		for(Chat chat : this.history) {
			result.add(chat.getMessage());
		}
		
		return result;
	}

	@Override
	public String getChatMessage(int index) {
		if(index > this.history.size()) return "";
		return this.history.get(index).getMessage();
	}

	@Override
	public String getLastChat() {
		if(this.history.size() <= 0) return "";
		return this.history.get(0).getMessage();
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}
	
	public void update() {
		for(Chat chat : this.history) {
			chat.incrementTime();
		}
		
		super.update();
	}
	
	public void render() {
		RenderHelper.getHelper().drawSubTex(GuiTextures.CROSSHAIR, this.width / 2 - 8, this.height / 2 - 8, 1);
		RenderHelper.getHelper().drawSubTex(GuiTextures.QUICK_BAR, this.width / 2 - 91, this.height - 22, 0, 1);
		RenderHelper.getHelper().drawSubTex(GuiTextures.SELECTION, this.width / 2 - 92 + this.mode.getPlayer().getQuickBar().getSelected() * 20, this.height - 23, 1);
		for(int slot = 0; slot < 9; slot++) {
			int x = this.width / 2 - 90 + slot * 20;
			int y = this.height - 11;
			if(this.mode.getPlayer().getQuickBar().getBlock(slot) > 0) {
				RenderHelper.getHelper().drawRotatedBlock(x, y, Blocks.fromId(this.mode.getPlayer().getQuickBar().getBlock(slot)));
			}
		}
		
		RenderHelper.getHelper().renderText(Constants.CLIENT_VERSION, 2, 2, false);
		if(OpenClassic.getClient().getConfig().getBoolean("options.show-info", false)) {
			RenderHelper.getHelper().renderText("FPS: " + ((ClassicClient) OpenClassic.getClient()).getFps(), 2, 12, false);
			RenderHelper.getHelper().renderText("Position: " + OpenClassic.getClient().getPlayer().getPosition().getBlockX() + ", " + OpenClassic.getClient().getPlayer().getPosition().getBlockY() + ", " + OpenClassic.getClient().getPlayer().getPosition().getBlockZ(), 2, 22, false);
			RenderHelper.getHelper().renderText("Columns: " + ((ClassicClient) OpenClassic.getClient()).getColumns(), 2, 32, false);
			RenderHelper.getHelper().renderText("Memory: " + ((ClassicClient) OpenClassic.getClient()).getMemoryDisplay(), 2, 42, false);
		}
		
		super.render();
		
		int max = 10;
		boolean all = false;
		if(OpenClassic.getClient().getCurrentScreen() instanceof ChatInputScreen) {
			max = 20;
			all = true;
		}

		List<Chat> visible = this.getVisible(max, all);
		if(visible.size() > 0) {
			RenderHelper.getHelper().drawBox(2, this.height - 32, 377, this.height - 34 - Math.min(visible.size(), max) * 9, Integer.MIN_VALUE);
			for(int count = 0; count < visible.size(); count++) {
				RenderHelper.getHelper().renderText(visible.get(count).getMessage(), 4, this.height - 41 - count * 9, false);
			}
		}
		
		if(Keyboard.isKeyDown(OpenClassic.getClient().getConfig().getInteger("keys.playerlist", Keyboard.KEY_TAB)) && this.mode instanceof Multiplayer && this.mode.isInGame()) {
			List<Player> players = this.mode.getLevel().getPlayers();
			RenderHelper.getHelper().drawBox(this.width / 2 - 128, this.height / 2 - 80, this.width / 2 + 128, this.height / 2 + 68, Integer.MIN_VALUE);
			RenderHelper.getHelper().renderText("Connected players:", this.width / 2 - RenderHelper.getHelper().getStringWidth("Connected players:") / 2, this.height / 2 - 64 - 12, false);
			for(int count = 0; count < players.size(); count++) {
				int x = this.width / 2 + count % 2 * 120 - 120;
				int y = this.height / 2 - 64 + (count / 2 << 3);
				if(!Mouse.isGrabbed() && Mouse.getX() >= x && Mouse.getY() >= y && Mouse.getX() < x + 120 && Mouse.getY() < y + 8) {
					this.hoveredPlayer = players.get(count).getName();
					RenderHelper.getHelper().renderText(players.get(count).getName(), x + 2, y, false);
				} else {
					this.hoveredPlayer = null;
					RenderHelper.getHelper().renderText(players.get(count).getName(), x, y, 15658734, false);
				}
			}
		}
	}

	private List<Chat> getVisible(int max, boolean all) {
		List<Chat> result = new ArrayList<Chat>();
		for(Chat chat : this.history) {
			if(max <= 0) break;
			if(chat.getTime() < 200 || all) {
				result.add(chat);
				max--;
			} else {
				break;
			}
		}

		return result;
	}

}
