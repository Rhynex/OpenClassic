package ch.spacebase.openclassic.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import static org.lwjgl.opengl.GL11.*;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.MainScreen;
import ch.spacebase.openclassic.api.inventory.ItemStack;
import ch.spacebase.openclassic.api.level.generator.biome.Biome;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.api.util.GuiTextures;
import ch.spacebase.openclassic.client.ClassicClient;
import ch.spacebase.openclassic.client.mode.Mode;
import ch.spacebase.openclassic.client.mode.Multiplayer;
import ch.spacebase.openclassic.client.player.ClientPlayer;

public class ClassicMainScreen extends MainScreen {

	private List<Chat> history = new CopyOnWriteArrayList<Chat>();
	private int width;
	private int height;
	private boolean setup = false;
	private Mode mode;
	private String hoveredPlayer;
	private Random rand = new Random();

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
		while(this.history.size() > 50) {
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
		RenderHelper.getHelper().drawSubTex(GuiTextures.CROSSHAIR, this.width / 2 - 16, this.height / 2 - 16, 1);
		RenderHelper.getHelper().drawSubTex(GuiTextures.QUICK_BAR, this.width / 2 - 182, this.height - 44, 0, 1);
		RenderHelper.getHelper().drawSubTex(GuiTextures.SELECTION, this.width / 2 - 184 + this.mode.getPlayer().getInventory().getSelectedSlot() * 40, this.height - 46, 1);
		for(int slot = 0; slot < 9; slot++) {
			int x = this.width / 2 - 170 + slot * 40;
			int y = this.height - 22;
			if(this.mode.getPlayer().getInventory().getItem(slot) != null) {
				ItemStack stack = this.mode.getPlayer().getInventory().getItem(slot);
				stack.getItem().renderInventory(x, y);
				if(stack.getSize() > 1) RenderHelper.getHelper().renderText(String.valueOf(stack.getSize()), x + 26 - RenderHelper.getHelper().getStringWidth(String.valueOf(stack.getSize())), y - 2, false);
				if(stack.getDamage() > 0) {
					int wid = 26 - (stack.getDamage() * 26) / stack.getItem().getMaxDamage();
					int col = 255 - (stack.getDamage() * 255) / stack.getItem().getMaxDamage();
					glDisable(GL_DEPTH_TEST);
					int col2 = 255 - col << 16 | col << 8;
					int col1 = (255 - col) / 4 << 16 | 0x3f00;
					RenderHelper.getHelper().colorSolid(x - 3, y + 10, x + 25, y + 14, 0);
					RenderHelper.getHelper().colorSolid(x - 3, y + 10, x + 23, y + 13, col1);
					RenderHelper.getHelper().colorSolid(x - 3, y + 10, x - 3 + wid, y + 13, col2);
					glEnable(GL_DEPTH_TEST);
				}
			}
		}
		
		boolean flash = (((ClientPlayer) OpenClassic.getClient().getPlayer()).getInvincibleTicks() / 3) % 2 == 1;
		if(((ClientPlayer) OpenClassic.getClient().getPlayer()).getInvincibleTicks() < 10) flash = false;
		int health = OpenClassic.getClient().getPlayer().getHealth();
		int prev = ((ClientPlayer) OpenClassic.getClient().getPlayer()).getPrevHealth();
		int air = OpenClassic.getClient().getPlayer().getAir();
		boolean water = ((ClientPlayer) OpenClassic.getClient().getPlayer()).isHeadInWater();
		for(int count = 0; count < 10; count++) {
			int x = (this.width / 2 - 182) + count * 16;
			int y = this.height - 64;
			if(health <= 4) y += this.rand.nextInt(4);
			RenderHelper.getHelper().drawSubTex(flash ? GuiTextures.EMPTY_HEART_FLASH : GuiTextures.EMPTY_HEART, x, y, 1);
			if(flash) {
				if(count * 2 + 1 < prev) {
					RenderHelper.getHelper().drawSubTex(GuiTextures.FULL_HEART_FLASH, x, y, 1);
				} else if(count * 2 + 1 == prev) {
					RenderHelper.getHelper().drawSubTex(GuiTextures.HALF_HEART_FLASH, x, y, 1);
				}
			}
			
			if(count * 2 + 1 < health) {
				RenderHelper.getHelper().drawSubTex(GuiTextures.FULL_HEART, x, y, 1);
			} else if(count * 2 + 1 == health) {
				RenderHelper.getHelper().drawSubTex(GuiTextures.HALF_HEART, x, y, 1);
			}
		}
		
		if(water) {
			int bubbles = (int) Math.ceil(((air - 2) * 10D) / 300D);
			int total = (int) Math.ceil((air * 10D) / 300D) - bubbles;
			for(int count = 0; count < bubbles + total; count++) {
				if(count < bubbles) {
					RenderHelper.getHelper().drawSubTex(GuiTextures.BUBBLE, (this.width / 2 - 182) + count * 16, this.height - 82, 1);
				} else {
					RenderHelper.getHelper().drawSubTex(GuiTextures.POPPING_BUBBLE, (this.width / 2 - 182) + count * 16, this.height - 82, 1);
				}
			}
		}

		RenderHelper.getHelper().renderText(Constants.CLIENT_VERSION, 2, 2, false);
		if(OpenClassic.getClient().getConfig().getBoolean("options.show-info", false)) {
			RenderHelper.getHelper().renderText("FPS: " + ((ClassicClient) OpenClassic.getClient()).getFps(), 4, 24, false);
			RenderHelper.getHelper().renderText("Position: " + OpenClassic.getClient().getPlayer().getPosition().getBlockX() + ", " + OpenClassic.getClient().getPlayer().getPosition().getBlockY() + ", " + OpenClassic.getClient().getPlayer().getPosition().getBlockZ(), 4, 44, false);
			RenderHelper.getHelper().renderText("Columns: " + ((ClassicClient) OpenClassic.getClient()).getColumns(), 4, 64, false);
			Biome b = OpenClassic.getClient().getLevel().getBiome(OpenClassic.getClient().getPlayer().getPosition().getBlockX(), OpenClassic.getClient().getPlayer().getPosition().getBlockY(), OpenClassic.getClient().getPlayer().getPosition().getBlockZ());
			String biome = b != null ? b.getName() : "None";
			RenderHelper.getHelper().renderText("Biome: " + biome, 4, 84, false);
			RenderHelper.getHelper().renderText("Memory: " + ((ClassicClient) OpenClassic.getClient()).getMemoryDisplay(), 4, 104, false);
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
			RenderHelper.getHelper().drawBox(4, this.height - 64, 754, this.height - 68 - Math.min(visible.size(), max) * 18, Integer.MIN_VALUE);
			for(int count = 0; count < visible.size(); count++) {
				RenderHelper.getHelper().renderText(visible.get(count).getMessage(), 8, this.height - 86 - count * 18, false);
			}
		}

		if(Keyboard.isKeyDown(OpenClassic.getClient().getConfig().getInteger("keys.playerlist", Keyboard.KEY_TAB)) && this.mode instanceof Multiplayer && this.mode.isInGame()) {
			List<Player> players = this.mode.getLevel().getPlayers();
			RenderHelper.getHelper().drawBox(this.width / 2 - 256, this.height / 2 - 160, this.width / 2 + 256, this.height / 2 + 136, Integer.MIN_VALUE);
			RenderHelper.getHelper().renderText("Connected players:", this.width / 2 - RenderHelper.getHelper().getStringWidth("Connected players:") / 2, this.height / 2 - 152, false);
			for(int count = 0; count < players.size(); count++) {
				int x = this.width / 2 + count % 2 * 240 - 240;
				int y = this.height / 2 - 128 + (count / 2 << 3);
				if(!Mouse.isGrabbed() && Mouse.getX() >= x && Mouse.getY() >= y && Mouse.getX() < x + 120 && Mouse.getY() < y + 8) {
					this.hoveredPlayer = players.get(count).getName();
					RenderHelper.getHelper().renderText(players.get(count).getName(), x + 4, y, false);
				} else {
					this.hoveredPlayer = null;
					RenderHelper.getHelper().renderText(players.get(count).getName(), x, y, false);
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
