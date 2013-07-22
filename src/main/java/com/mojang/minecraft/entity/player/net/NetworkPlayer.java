package com.mojang.minecraft.entity.player.net;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.Color;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.entity.player.Player;
import com.mojang.minecraft.render.FontRenderer;
import com.mojang.minecraft.render.TextureManager;

public class NetworkPlayer extends Player {

	private transient List<PositionUpdate> moveQueue = new LinkedList<PositionUpdate>();
	private transient Minecraft minecraft;
	public int playerId;
	private int xp;
	private int yp;
	private int zp;
	public String name;
	public String displayName;

	public NetworkPlayer(Minecraft mc, int playerId, String name, float x, float y, float z, float yaw, float pitch) {
		super(mc.level, x, y, z);
		this.minecraft = mc;
		this.playerId = playerId;
		this.displayName = name;
		this.name = Color.stripColor(name);
		this.xp = (int) (x * 32);
		this.yp = (int) (y * 32);
		this.zp = (int) (z * 32);
		this.heightOffset = 0.0F;
		this.pushthrough = 0.8F;
		this.pitch = pitch;
		this.yaw = yaw;
		this.renderOffset = 0.6875F;
		this.allowAlpha = false;
	}

	public void aiStep() {
		int steps = 5;

		while (steps-- > 0 && this.moveQueue.size() > 10) {
			if (this.moveQueue.size() > 0) {
				this.setPos(this.moveQueue.remove(0));
			}
		}

		this.onGround = true;
	}

	public void renderHover(TextureManager textures, float dt) {
		FontRenderer fontRenderer = this.minecraft.fontRenderer;
		GL11.glPushMatrix();
		GL11.glTranslatef(this.xo + (this.x - this.xo) * dt, this.yo + (this.y - this.yo) * dt + 0.8F + this.renderOffset, this.zo + (this.z - this.zo) * dt);
		GL11.glRotatef(-this.minecraft.player.yaw, 0.0F, 1.0F, 0.0F);
		GL11.glScalef(0.05F, -0.05F, 0.05F);
		GL11.glTranslatef((-fontRenderer.getWidth(this.displayName)) / 2.0F, 0.0F, 0.0F);
		GL11.glNormal3f(1.0F, -1.0F, 1.0F);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_COLOR_BUFFER_BIT);
		if (this.name.equalsIgnoreCase("Notch")) {
			fontRenderer.renderNoShadow(this.displayName, 0, 0, 16776960);
		} else {
			fontRenderer.renderNoShadow(this.displayName, 0, 0, 16777215);
		}

		GL11.glDepthFunc(GL11.GL_GREATER);
		GL11.glDepthMask(false);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.8F);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		fontRenderer.renderNoShadow(this.displayName, 0, 0, 16777215);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDepthMask(true);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glTranslatef(1.0F, 1.0F, -0.05F);
		fontRenderer.renderNoShadow(this.name, 0, 0, 5263440);
		GL11.glEnable(GL11.GL_COLOR_BUFFER_BIT);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}

	public void queue(byte xChange, byte yChange, byte zChange, float yawChange, float pitchChange) {
		float yaw = yawChange - this.yaw;
		float pitch = pitchChange - this.pitch;
		
		while (yaw >= 180.0F) {
			yaw -= 360.0F;
		}

		while (yaw < -180.0F) {
			yaw += 360.0F;
		}

		while (pitch >= 180.0F) {
			pitch -= 360.0F;
		}

		while (pitch < -180.0F) {
			pitch += 360.0F;
		}

		yaw = this.yaw + yaw * 0.5F;
		pitch = this.pitch + pitch * 0.5F;
		this.moveQueue.add(new PositionUpdate((this.xp + xChange / 2.0F) / 32.0F, (this.yp + yChange / 2.0F) / 32.0F, (this.zp + zChange / 2.0F) / 32.0F, yaw, pitch));
		this.xp += xChange;
		this.yp += yChange;
		this.zp += zChange;
		this.moveQueue.add(new PositionUpdate(this.xp / 32.0F, this.yp / 32.0F, this.zp / 32.0F, yawChange, pitchChange));
	}

	public void teleport(short x, short y, short z, float yaw, float pitch) {
		float newYaw = yaw - this.yaw;
		float newPitch = pitch - this.pitch;
		while (newYaw >= 180.0F) {
			newYaw -= 360.0F;
		}

		while (newYaw < -180.0F) {
			newYaw += 360.0F;
		}

		while (newPitch >= 180.0F) {
			newPitch -= 360.0F;
		}

		while (newPitch < -180.0F) {
			newPitch += 360.0F;
		}

		newYaw = this.yaw + newYaw * 0.5F;
		newPitch = this.pitch + newPitch * 0.5F;
		this.moveQueue.add(new PositionUpdate((this.xp + x) / 64.0F, (this.yp + y) / 64.0F, (this.zp + z) / 64.0F, newYaw, newPitch));
		this.xp = x;
		this.yp = y;
		this.zp = z;
		this.moveQueue.add(new PositionUpdate(this.xp / 32.0F, this.yp / 32.0F, this.zp / 32.0F, yaw, pitch));
	}

	public void queue(byte x, byte y, byte z) {
		this.moveQueue.add(new PositionUpdate((this.xp + x / 2.0F) / 32.0F, (this.yp + y / 2.0F) / 32.0F, (this.zp + z / 2.0F) / 32.0F));
		this.xp += x;
		this.yp += y;
		this.zp += z;
		this.moveQueue.add(new PositionUpdate(this.xp / 32.0F, this.yp / 32.0F, this.zp / 32.0F));
	}

	public void queue(float yaw, float pitch) {
		float newYaw = yaw - this.yaw;

		float newPitch = pitch - this.pitch;
		while (newYaw >= 180.0F) {
			newYaw -= 360.0F;
		}

		while (newYaw < -180.0F) {
			newYaw += 360.0F;
		}

		while (newPitch >= 180.0F) {
			newPitch -= 360.0F;
		}

		while (newPitch < -180.0F) {
			newPitch += 360.0F;
		}

		newYaw = this.yaw + newYaw * 0.5F;
		newPitch = this.pitch + newPitch * 0.5F;
		this.moveQueue.add(new PositionUpdate(newYaw, newPitch));
		this.moveQueue.add(new PositionUpdate(yaw, pitch));
	}
	
	public void clear() {
		if (this.newTextureId >= 0 && this.textures != null) {
			this.textures.textureImgs.remove(this.newTextureId);
			this.textures.textureBuffer.clear();
			this.textures.textureBuffer.put(this.newTextureId);
			this.textures.textureBuffer.flip();
			GL11.glDeleteTextures(this.textures.textureBuffer);
		}
	}

	@Override
	public String getName() {
		return this.name;
	}

}
