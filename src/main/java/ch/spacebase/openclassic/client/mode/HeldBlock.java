package ch.spacebase.openclassic.client.mode;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL;

import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.item.Item;
import ch.spacebase.openclassic.api.item.Items;
import ch.spacebase.openclassic.api.math.MathHelper;

public class HeldBlock {

	private float pos;
	private float prevpos;
	private float off;
	private boolean moving;
	private Item held = Items.get(VanillaBlock.STONE.getId());
	
	public float getPos() {
		return this.pos;
	}
	
	public float getPrevPos() {
		return this.prevpos;
	}
	
	public float getOffset() {
		return this.off;
	}
	
	public boolean isMoving() {
		return this.moving;
	}
	
	public void setPos(float pos) {
		this.pos = pos;
	}
	
	public void setOffset(float off) {
		this.off = off;
	}
	
	public void setMoving(boolean moving) {
		this.moving = moving;
	}
	
	public void onBlock() {
		this.off = -1;
		this.moving = true;
	}
	
	public void update(Item held) {
		this.prevpos = this.pos;
		if(this.moving) {
			this.off++;
			if(this.off == 7) {
				this.off = 0;
				this.moving = false;
			}
		}

		float position = (held == this.held ? 1 : 0) - this.pos;
		if(position < -0.4f) {
			position = -0.4f;
		}

		if(position > 0.4f) {
			position = 0.4f;
		}

		this.pos += position;
		if(this.pos < 0.1f) {
			this.held = held;
		}
	}
	
	public void render(float brightness, float delta) { 
		if(this.held == null) return; // TODO: arm
		float progress = this.prevpos + (this.pos - this.prevpos) * delta;
		glPushMatrix();
		if(this.moving) {
			float off = (this.off + delta) / 7f;
			float mz = MathHelper.sin(off * (float) Math.PI);
			float mx = MathHelper.sin((float) Math.sqrt(off) * (float) Math.PI);
			glTranslatef(-mx * 0.4f, MathHelper.sin((float) Math.sqrt(off) * (float) Math.PI * 2) * 0.2f, -mz * 0.2f);
		}
		
		glTranslatef(0.7f * 0.8f, -0.65f * 0.8f - (1 - progress) * 0.6f, -0.9f * 0.8f);
		glRotatef(45f, 0, 1, 0);
		glEnable(GL_RESCALE_NORMAL);
		if(this.moving) {
			float off = (this.off + delta) / 7f;
			float yr = MathHelper.sin(off * off * (float) Math.PI);
			float xzr = MathHelper.sin((float) Math.sqrt(off) * (float) Math.PI);
			glRotatef(-yr * 20f, 0, 1, 0);
			glRotatef(-xzr * 20f, 0, 0, 1);
			glRotatef(-xzr * 80f, 1, 0, 0);
		}
		
		glScalef(0.4f, 0.4f, 0.4f);
		this.held.renderHeld(brightness);
		glPopMatrix();
	}
	
}
