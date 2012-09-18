package ch.spacebase.openclassic.client.mode;

import static org.lwjgl.opengl.GL11.*;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.math.MathHelper;

public class HeldBlock {

	private float pos;
	private float prevpos;
	private float off;
	private boolean moving;
	private BlockType block = VanillaBlock.STONE;
	
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
	
	public void update(BlockType held) {
		this.prevpos = this.pos;
		if(this.moving) {
			this.off++;
			if(this.off == 7) {
				this.off = 0;
				this.moving = false;
			}
		}

		float position = (held == this.block ? 1 : 0) - this.pos;
		if(position < -0.4f) {
			position = -0.4f;
		}

		if(position > 0.4f) {
			position = 0.4f;
		}

		this.pos += position;
		if(this.pos < 0.1f) {
			this.block = held;
		}
	}
	
	public void render(float brightness, float delta) {
		glPushMatrix();
		float interpolated = this.prevpos + (this.pos - this.prevpos) * delta;
		if(this.moving) {
			float off = (this.off + delta) / 7f;
			glTranslatef(-MathHelper.sin((float) Math.sqrt(off) * (float) Math.PI) * 0.4f, MathHelper.sin((float) Math.sqrt(off) * (float) Math.PI * 2) * 0.2f, -MathHelper.sin(off * (float) Math.PI) * 0.2f);
		}

		glTranslatef(0.56f, -0.52f - (1 - interpolated) * 0.6f, -0.72f);
		glRotatef(45, 0, 1, 0);
		glDisable(GL_DEPTH_TEST);
		if(this.moving) {
			float off =  (this.off + delta) / 7f;
			glRotatef(MathHelper.sin((float) Math.sqrt(off) * (float) Math.PI) * 80, 0, 1, 0);
			glRotatef(-MathHelper.sin(off * off * (float) Math.PI) * 20, 1, 0, 0);
		}

		if(this.block != null) {
			glScalef(0.4f, 0.4f, 0.4f);
			glTranslatef(-0.5f, -0.5f, -0.5f);
			this.block.getModel().renderAll(0, 0, 0, brightness);
		}
		
		glEnable(GL_DEPTH_TEST);
		glPopMatrix();
	}
	
}
