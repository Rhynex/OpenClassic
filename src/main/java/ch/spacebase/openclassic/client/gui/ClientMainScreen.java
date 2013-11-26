package ch.spacebase.openclassic.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.gui.MainScreen;
import ch.spacebase.openclassic.api.gui.widget.Widget;
import ch.spacebase.openclassic.api.input.InputHelper;
import ch.spacebase.openclassic.api.input.Keyboard;
import ch.spacebase.openclassic.api.math.MathHelper;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.render.RenderHelper;

public class ClientMainScreen extends MainScreen {

	public List<ChatLine> chatHistory = new ArrayList<ChatLine>();
	private Random rand = new Random();
	public int width;
	public int height;
	public String hoveredPlayer = null;
	public int ticks = 0;
	public String debugInfo = "";

	public ClientMainScreen() {
		this.width = RenderHelper.getHelper().getGuiWidth();
		this.height = RenderHelper.getHelper().getGuiHeight();
	}
	
	@Override
	public void update(int mouseX, int mouseY) {
		this.ticks++;
		for(int index = 0; index < this.chatHistory.size(); index++) {
			this.chatHistory.get(index).incrementTime();
		}
		
		super.update(mouseX, mouseY);
	}

	public void render(float dt) {
		if(OpenClassic.getClient().getSettings().getBooleanSetting("options.minimap").getValue()) {
			if(this.getWidget(0) == null) {
				this.attachWidget(new Minimap(0, this.width - 85, 10, 75, 75, this));
			}
		} else if(this.getWidget(0) != null) {
			this.removeWidget(0);
		}
		
		RenderHelper.getHelper().bindTexture("/gui/gui.png", true);
		RenderHelper.getHelper().glColor(1, 1, 1, 1);
		RenderHelper.getHelper().enableBlend();

		if(!OpenClassic.getClient().isHUDHidden()) {
			Player player = OpenClassic.getClient().getPlayer();
			Position pos = player.getPosition();
			RenderHelper.getHelper().drawSubImage(this.width / 2 - 91, this.height - 22, -90, 0, 0, 182, 22);
			RenderHelper.getHelper().drawSubImage(this.width / 2 - 91 - 1 + player.getSelectedSlot() * 20, this.height - 22 - 1, -90, 0, 22, 24, 22);

			RenderHelper.getHelper().bindTexture("/gui/icons.png", true);
			RenderHelper.getHelper().drawSubImage(this.width / 2 - 7, this.height / 2 - 7, -90, 0, 0, 16, 16);

			boolean glow = player.getInvulnerableTime() / 3 % 2 != 0 && player.getInvulnerableTime() >= 10;
			this.rand.setSeed((this.ticks * 312871L));
			if(OpenClassic.getClient().isInSurvival()) {
				for(int heart = 0; heart < 10; heart++) {
					int heartX = this.width / 2 - 91 + (heart << 3);
					int heartY = this.height - 32;

					if(player.getHealth() <= 4) {
						heartY += this.rand.nextInt(2);
					}

					RenderHelper.getHelper().drawSubImage(heartX, heartY, -90, 16 + (glow ? 9 : 0), 0, 9, 9);
					if(glow) {
						if((heart << 1) + 1 < player.getPreviousHealth()) {
							RenderHelper.getHelper().drawSubImage(heartX, heartY, -90, 70, 0, 9, 9);
						}

						if((heart << 1) + 1 == player.getPreviousHealth()) {
							RenderHelper.getHelper().drawSubImage(heartX, heartY, -90, 79, 0, 9, 9);
						}
					}

					if((heart << 1) + 1 < player.getHealth()) {
						RenderHelper.getHelper().drawSubImage(heartX, heartY, -90, 52, 0, 9, 9);
					}

					if((heart << 1) + 1 == player.getHealth()) {
						RenderHelper.getHelper().drawSubImage(heartX, heartY, -90, 61, 0, 9, 9);
					}
				}

				if(player.isUnderwater()) {
					int full = (int) Math.ceil((player.getAir() - 2) * 10.0D / 300.0D);
					int pop = (int) Math.ceil(player.getAir() * 10.0D / 300.0D) - full;

					for(int count = 0; count < full + pop; count++) {
						if(count < full) {
							RenderHelper.getHelper().drawSubImage(this.width / 2 - 91 + (count << 3), this.height - 32 - 9, -90, 16, 18, 9, 9);
						} else {
							RenderHelper.getHelper().drawSubImage(this.width / 2 - 91 + (count << 3), this.height - 32 - 9, -90, 25, 18, 9, 9);
						}
					}
				}
			}

			RenderHelper.getHelper().disableBlend();

			for(int slot = 0; slot < player.getInventoryContents().length; slot++) {
				int x = this.width / 2 - 90 + slot * 20;
				int y = this.height - 16;
				int block = player.getInventoryContents()[slot];

				if(block > 0) {
					RenderHelper.getHelper().pushMatrix();
					RenderHelper.getHelper().translate(x, y, -50);

					if(player.getInventoryPopTimes()[slot] > 0) {
						float off = (player.getInventoryPopTimes()[slot] - dt) / 5;
						RenderHelper.getHelper().translate(10, (-MathHelper.sin(off * off * MathHelper.PI) * 8) + 10, 0);
						RenderHelper.getHelper().scale(MathHelper.sin(off * off * MathHelper.PI) + 1, MathHelper.sin(off * MathHelper.PI) + 1, 1);
						RenderHelper.getHelper().translate(-10, -10, 0);
					}

					RenderHelper.getHelper().scale(10, 10, 10);
					RenderHelper.getHelper().translate(1, 0.5F, 0);
					RenderHelper.getHelper().rotate(-30, 1, 0, 0);
					RenderHelper.getHelper().rotate(45, 0, 1, 0);
					RenderHelper.getHelper().translate(-1.5F, 0.5F, 0.5F);
					RenderHelper.getHelper().scale(-1, -1, -1);

					Blocks.fromId(block).getModel().renderAll(-2, 0, 0, 1);
					RenderHelper.getHelper().popMatrix();

					if(player.getInventoryAmounts()[slot] > 1) {
						RenderHelper.getHelper().renderText(String.valueOf(player.getInventoryAmounts()[slot]), x + 19 - RenderHelper.getHelper().getStringWidth(String.valueOf(player.getInventoryAmounts()[slot])), y + 6, false);
					}
				}
			}

			RenderHelper.getHelper().renderText(Constants.VERSION, 2, 2, false);
			if(OpenClassic.getClient().getSettings().getBooleanSetting("options.show-info").getValue()) {
				RenderHelper.getHelper().renderText(this.debugInfo, 2, 12, false);
				RenderHelper.getHelper().renderText("Position: " + pos.getBlockX() + ", " + pos.getBlockY() + ", " + pos.getBlockZ(), 2, 22, false);
			}

			if(OpenClassic.getClient().isInSurvival()) {
				String score = "Score: &e" + player.getScore();
				RenderHelper.getHelper().renderText(score, this.width - RenderHelper.getHelper().getStringWidth(score) - 2, 2, false);
				RenderHelper.getHelper().renderText("Arrows: " + player.getArrows(), this.width / 2 + 8, this.height - 33, false);
			}
		}

		byte maxMsgs = 10;
		boolean showAllMsgs = false;
		if(OpenClassic.getClient().getCurrentScreen() instanceof ChatInputScreen) {
			maxMsgs = 20;
			showAllMsgs = true;
		}

		for(int message = 0; message < this.chatHistory.size() && message < maxMsgs; message++) {
			if(this.chatHistory.get(message).getTime() < 200 || showAllMsgs) {
				RenderHelper.getHelper().renderText(this.chatHistory.get(message).getMessage(), 2, this.height - 8 - message * 9 - 20, false);
			}
		}

		this.hoveredPlayer = null;
		if(InputHelper.getHelper().isKeyDown(Keyboard.KEY_TAB) && OpenClassic.getClient().isInMultiplayer()) {
			List<Player> players = OpenClassic.getClient().getLevel().getPlayers();
			RenderHelper.getHelper().enableBlend();
			RenderHelper.getHelper().drawTranslucentBox(this.width / 2 - 128, this.height / 2 - 80, 256, 148);
			RenderHelper.getHelper().renderText("Players in the level:", this.width / 2 - RenderHelper.getHelper().getStringWidth("Connected players:") / 2, this.height / 2 - 64 - 12, false);
			int mouseX = RenderHelper.getHelper().getScaledMouseX();
			int mouseY = RenderHelper.getHelper().getScaledMouseY();
			for(int count = 0; count < players.size(); count++) {
				int x = this.width / 2 + count % 2 * 120 - 120;
				int y = this.height / 2 - 64 + (count / 2 << 3);
				if(OpenClassic.getClient().getCurrentScreen() != null && mouseX >= x && mouseY >= y && mouseX < x + 120 && mouseY < y + 8) {
					this.hoveredPlayer = players.get(count).getName();
					RenderHelper.getHelper().renderTextNoShadow(players.get(count).getName(), x + 2, y, false);
				} else {
					RenderHelper.getHelper().renderTextNoShadow(players.get(count).getName(), x, y, 15658734, false);
				}
			}
		}

		for(Widget widget : this.getWidgets()) {
			if (widget.isVisible()) {
				widget.render();
			}
		}
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
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
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
