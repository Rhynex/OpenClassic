package ch.spacebase.openclassic.client.util;

import static org.lwjgl.opengl.GL11.*;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.BlockFace;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.model.BoundingBox;

public class Selection {

	private static float OFFSET = 0.001f;
	
	private Position pos;
	private BlockFace face;
	private BoundingBox bb;
	
	public Position getPosition() {
		return this.pos;
	}
	
	public BlockFace getFace() {
		return this.face;
	}
	
	public void set(Position pos, BlockFace face) {
		this.pos = pos;
		this.face = face;
		if(this.pos == null || this.face == null || OpenClassic.getClient().getLevel().getBlockIdAt(this.pos) == 0 || OpenClassic.getClient().getLevel().getBlockTypeAt(this.pos).isLiquid()) {
			this.pos = null;
			this.face = null;
			this.bb = null;
		} else {
			this.bb = OpenClassic.getClient().getLevel().getBlockTypeAt(this.pos.getBlockX(), this.pos.getBlockY(), this.pos.getBlockZ()).getModel().getSelectionBox(this.pos.getBlockX(), this.pos.getBlockY(), this.pos.getBlockZ());
		}
	}
	
	public boolean isValid() {
		boolean valid = this.pos != null && this.face != null && this.bb != null;
		if(valid && (this.pos.getBlockType() == VanillaBlock.AIR || this.pos.getBlockType().isLiquid())) {
			this.pos = null;
			this.face = null;
			this.bb = null;
			valid = false;
		}
		
		return valid;
	}

	public void render() {
		if(this.bb == null) return;
		glPushMatrix();
		glDisable(GL_TEXTURE_2D);
		glColor4f(0, 0, 0, 0.4f);
		glLineWidth(2);
		
		glBegin(GL_LINE_STRIP);
		glVertex3f(this.bb.getX1() - OFFSET, this.bb.getY1() - OFFSET, this.bb.getZ1() - OFFSET);
		glVertex3f(this.bb.getX2() + OFFSET, this.bb.getY1() - OFFSET, this.bb.getZ1() - OFFSET);
		glVertex3f(this.bb.getX2() + OFFSET, this.bb.getY1() - OFFSET, this.bb.getZ2() + OFFSET);
		glVertex3f(this.bb.getX1() - OFFSET, this.bb.getY1() - OFFSET, this.bb.getZ2() + OFFSET);
		glVertex3f(this.bb.getX1() - OFFSET, this.bb.getY1() - OFFSET, this.bb.getZ1() - OFFSET);
		glEnd();
		
		glBegin(GL_LINE_STRIP);
		glVertex3f(this.bb.getX1() - OFFSET, this.bb.getY2() + OFFSET, this.bb.getZ1() - OFFSET);
		glVertex3f(this.bb.getX2() + OFFSET, this.bb.getY2() + OFFSET, this.bb.getZ1() - OFFSET);
		glVertex3f(this.bb.getX2() + OFFSET, this.bb.getY2() + OFFSET, this.bb.getZ2() + OFFSET);
		glVertex3f(this.bb.getX1() - OFFSET, this.bb.getY2() + OFFSET, this.bb.getZ2() + OFFSET);
		glVertex3f(this.bb.getX1() - OFFSET, this.bb.getY2() + OFFSET, this.bb.getZ1() - OFFSET);
		glEnd();
		
		glBegin(GL_LINES);
		glVertex3f(this.bb.getX1() - OFFSET, this.bb.getY1() - OFFSET, this.bb.getZ1() - OFFSET);
		glVertex3f(this.bb.getX1() - OFFSET, this.bb.getY2() + OFFSET, this.bb.getZ1() - OFFSET);
		glVertex3f(this.bb.getX2() + OFFSET, this.bb.getY1() - OFFSET, this.bb.getZ1() - OFFSET);
		glVertex3f(this.bb.getX2() + OFFSET, this.bb.getY2() + OFFSET, this.bb.getZ1() - OFFSET);
		glVertex3f(this.bb.getX2() + OFFSET, this.bb.getY1() - OFFSET, this.bb.getZ2() + OFFSET);
		glVertex3f(this.bb.getX2() + OFFSET, this.bb.getY2() + OFFSET, this.bb.getZ2() + OFFSET);
		glVertex3f(this.bb.getX1() - OFFSET, this.bb.getY1() - OFFSET, this.bb.getZ2() + OFFSET);
		glVertex3f(this.bb.getX1() - OFFSET, this.bb.getY2() + OFFSET, this.bb.getZ2() + OFFSET);
		glEnd();
		
		glEnable(GL_TEXTURE_2D);
		glPopMatrix();
	}
	
}
