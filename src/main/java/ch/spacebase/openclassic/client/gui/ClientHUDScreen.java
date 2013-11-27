package ch.spacebase.openclassic.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.Display;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.gui.HUDComponent;
import ch.spacebase.openclassic.api.input.InputHelper;
import ch.spacebase.openclassic.api.input.Keyboard;
import ch.spacebase.openclassic.api.math.MathHelper;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.render.GuiTextures;
import ch.spacebase.openclassic.client.render.RenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;

public class ClientHUDScreen extends HUDComponent {

	public List<ChatLine> chatHistory = new ArrayList<ChatLine>();
	private Random rand = new Random();
	public String hoveredPlayer = null;
	public int ticks = 0;
	public String debugInfo = "";

	public ClientHUDScreen() {
		super("clienthudscreen", 0, 0, Display.getWidth(), Display.getHeight());
	}
	
	@Override
	public void update(int mouseX, int mouseY) {
		this.ticks++;
		for(int index = 0; index < this.chatHistory.size(); index++) {
			this.chatHistory.get(index).incrementTime();
		}
		
		super.update(mouseX, mouseY);
	}

	@Override
	public void render(int mouseX, int mouseY) {
		if(OpenClassic.getClient().getSettings().getBooleanSetting("options.minimap").getValue()) {
			if(this.getComponent("minimap") == null) {
				this.attachComponent(new Minimap("minimap", this.getWidth() - 170, 20, 150, 150));
			}
		} else if(this.getComponent("minimap") != null) {
			this.removeComponent("minimap");
		}
		
		RenderHelper.getHelper().bindTexture("/gui/gui.png", true);
		RenderHelper.getHelper().glColor(1, 1, 1, 1);
		RenderHelper.getHelper().enableBlend();

		if(!OpenClassic.getClient().isHUDHidden()) {
			Player player = OpenClassic.getClient().getPlayer();
			Position pos = player.getPosition();
			RenderHelper.getHelper().drawSubTex(GuiTextures.QUICK_BAR, this.getWidth() / 2 - 182, this.getHeight() - 44, -100, 1);
			RenderHelper.getHelper().drawSubTex(GuiTextures.SELECTION, this.getWidth() / 2 - 184 + player.getSelectedSlot() * 40, this.getHeight() - 46, 1);

			RenderHelper.getHelper().drawSubTex(GuiTextures.CROSSHAIR, this.getWidth() / 2 - 16, this.getHeight() / 2 - 16, 1);

			boolean glow = player.getInvulnerableTime() / 3 % 2 != 0 && player.getInvulnerableTime() >= 10;
			this.rand.setSeed(this.ticks * 312871);
			if(OpenClassic.getClient().isInSurvival()) {
				for(int heart = 0; heart < 10; heart++) {
					int heartX = this.getWidth() / 2 - 182 + (heart << 3) * 2;
					int heartY = this.getHeight() - 66;

					if(player.getHealth() <= 4) {
						heartY += this.rand.nextInt(2) * 2;
					}

					RenderHelper.getHelper().drawSubTex(glow ? GuiTextures.EMPTY_HEART_FLASH : GuiTextures.EMPTY_HEART, heartX, heartY, 1);
					if(glow) {
						if((heart << 1) + 1 < player.getPreviousHealth()) {
							RenderHelper.getHelper().drawSubTex(GuiTextures.FULL_HEART_FLASH, heartX, heartY, 1);
						}

						if((heart << 1) + 1 == player.getPreviousHealth()) {
							RenderHelper.getHelper().drawSubTex(GuiTextures.HALF_HEART_FLASH, heartX, heartY, 1);
						}
					}

					if((heart << 1) + 1 < player.getHealth()) {
						RenderHelper.getHelper().drawSubTex(GuiTextures.FULL_HEART, heartX, heartY, 1);
					}

					if((heart << 1) + 1 == player.getHealth()) {
						RenderHelper.getHelper().drawSubTex(GuiTextures.HALF_HEART, heartX, heartY, 1);
					}
				}

				if(player.isUnderwater()) {
					int full = (int) Math.ceil((player.getAir() - 2) * 10.0D / 300.0D);
					int pop = (int) Math.ceil(player.getAir() * 10.0D / 300.0D) - full;

					for(int count = 0; count < full + pop; count++) {
						if(count < full) {
							RenderHelper.getHelper().drawSubTex(GuiTextures.BUBBLE, this.getWidth() / 2 - 182 + (count << 3) * 2, this.getHeight() - 84, 1);
						} else {
							RenderHelper.getHelper().drawSubTex(GuiTextures.POPPING_BUBBLE, this.getWidth() / 2 - 182 + (count << 3) * 2, this.getHeight() - 84, 1);
						}
					}
				}
			}

			//RenderHelper.getHelper().disableBlend();
			for(int slot = 0; slot < player.getInventoryContents().length; slot++) {
				int x = this.getWidth() / 2 - 180 + slot * 40;
				int y = this.getHeight() - 32;
				int block = player.getInventoryContents()[slot];

				if(block > 0) {
					RenderHelper.getHelper().pushMatrix();
					RenderHelper.getHelper().translate(x + 10.5f, y + 11, -50);

					if(player.getInventoryPopTimes()[slot] > 0) {
						float off = (player.getInventoryPopTimes()[slot] - GeneralUtils.getMinecraft().timer.delta) / 5;
						RenderHelper.getHelper().translate(10, (-MathHelper.sin(off * off * MathHelper.PI) * 8) + 10, 0);
						RenderHelper.getHelper().scale(MathHelper.sin(off * off * MathHelper.PI) + 1, MathHelper.sin(off * MathHelper.PI) + 1, 1);
						RenderHelper.getHelper().translate(-10, -10, 0);
					}

					RenderHelper.getHelper().scale(10, 10, 10);
					RenderHelper.getHelper().translate(1, 0, 0);
					RenderHelper.getHelper().rotate(-30, 1, 0, 0);
					RenderHelper.getHelper().rotate(45, 0, 1, 0);
					RenderHelper.getHelper().scale(2, 2, 2);
					RenderHelper.getHelper().translate(-1.5F, 0.5F, 0.5F);
					RenderHelper.getHelper().scale(-1, -1, -1);
					Blocks.fromId(block).getModel().renderAll(-2, 0, 0, 1);
					RenderHelper.getHelper().popMatrix();

					if(player.getInventoryAmounts()[slot] > 1) {
						RenderHelper.getHelper().renderText(String.valueOf(player.getInventoryAmounts()[slot]), x + 38 - RenderHelper.getHelper().getStringWidth(String.valueOf(player.getInventoryAmounts()[slot])), y + 12, false);
					}
				}
			}

			RenderHelper.getHelper().renderText(Constants.VERSION, 4, 4, false);
			if(OpenClassic.getClient().getSettings().getBooleanSetting("options.show-info").getValue()) {
				RenderHelper.getHelper().renderText(this.debugInfo, 4, 24, false);
				RenderHelper.getHelper().renderText("Position: " + pos.getBlockX() + ", " + pos.getBlockY() + ", " + pos.getBlockZ(), 4, 44, false);
			}

			if(OpenClassic.getClient().isInSurvival()) {
				String score = "Score: &e" + player.getScore();
				RenderHelper.getHelper().renderText(score, this.getWidth() - RenderHelper.getHelper().getStringWidth(score) - 4, 4, false);
				RenderHelper.getHelper().renderText("Arrows: " + player.getArrows(), this.getWidth() / 2 + 16, this.getHeight() - 66, false);
			}
		}

		byte maxMsgs = 10;
		boolean showAllMsgs = false;
		if(OpenClassic.getClient().getActiveComponent() instanceof ChatInputScreen) {
			maxMsgs = 20;
			showAllMsgs = true;
		}

		for(int message = 0; message < this.chatHistory.size() && message < maxMsgs; message++) {
			if(this.chatHistory.get(message).getTime() < 200 || showAllMsgs) {
				RenderHelper.getHelper().renderText(this.chatHistory.get(message).getMessage(), 4, this.getHeight() - 56 - message * 18, false);
			}
		}

		this.hoveredPlayer = null;
		if(InputHelper.getHelper().isKeyDown(Keyboard.KEY_TAB) && OpenClassic.getClient().isInMultiplayer()) {
			List<Player> players = OpenClassic.getClient().getLevel().getPlayers();
			RenderHelper.getHelper().enableBlend();
			RenderHelper.getHelper().drawTranslucentBox(this.getWidth() / 2 - 128, this.getHeight() / 2 - 80, 256, 148);
			RenderHelper.getHelper().renderText("Players in the level:", this.getWidth() / 2 - RenderHelper.getHelper().getStringWidth("Connected players:") / 2, this.getHeight() / 2 - 64 - 12, false);
			for(int count = 0; count < players.size(); count++) {
				int x = this.getWidth() / 2 + count % 2 * 240 - 240;
				int y = this.getHeight() / 2 - 128 + (count / 2 << 3) * 2;
				if(OpenClassic.getClient().getActiveComponent() != null && mouseX >= x && mouseY >= y && mouseX < x + 240 && mouseY < y + 16) {
					this.hoveredPlayer = players.get(count).getName();
					RenderHelper.getHelper().renderTextNoShadow(players.get(count).getName(), x + 4, y, false);
				} else {
					RenderHelper.getHelper().renderTextNoShadow(players.get(count).getName(), x, y, 15658734, false);
				}
			}
		}

		super.render(mouseX, mouseY);
	}

	public void addChat(String message) {
		this.chatHistory.add(0, new ChatLine(message));

		while(this.chatHistory.size() > 50) {
			this.chatHistory.remove(this.chatHistory.size() - 1);
		}
	}

	@Override
	public String getHoveredPlayer() {
		return this.hoveredPlayer;
	}

	@Override
	public List<String> getChat() {
		List<String> result = new ArrayList<String>();
		for(ChatLine line : this.chatHistory) {
			result.add(line.getMessage());
		}

		return result;
	}

	@Override
	public String getChatMessage(int index) {
		if(this.chatHistory.size() <= index) return null;
		return this.chatHistory.get(index).getMessage();
	}

	@Override
	public String getLastChat() {
		if(this.chatHistory.size() <= 0) return null;
		return this.chatHistory.get(0).getMessage();
	}
	
}
