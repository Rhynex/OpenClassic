package ch.spacebase.openclassic.client.particle;

import java.util.List;


import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.model.BoundingBox;
import ch.spacebase.openclassic.api.math.Vector;
import ch.spacebase.openclassic.client.level.ClientLevel;

public abstract class Particle {

	private static final float BB_SIZE = 0.2f;
	
	private final float lifetime = (int) (4f / (Math.random() * 0.9f + 0.1f));
	private final float gravity;
	private float age = 0;
	private float size = (float) Math.random();
	
	private Position pos;
	private Vector velocity = new Vector(0, 0, 0);
	private BoundingBox basebb = new BoundingBox(BB_SIZE / 2, BB_SIZE / 2, BB_SIZE / 2, BB_SIZE / 2 + BB_SIZE, BB_SIZE / 2 + BB_SIZE, BB_SIZE / 2 + BB_SIZE);
	private BoundingBox bb = this.basebb.clone();
	private boolean grounded = true;
	private boolean disposed = false;
	
	public Particle(Position pos, float velX, float velY, float velZ, float gravity) {
		this.pos = pos;
		this.bb.move(pos.getX(), pos.getY(), pos.getZ());
		this.gravity = gravity;
		this.velocity.set(velX + (float) (Math.random() * 2 - 1) * 0.4f, velY + (float) (Math.random() * 2 - 1) * 0.4f, velZ + (float) (Math.random() * 2 - 1) * 0.4f);
	}
	
	public void update() {
		if (this.age++ >= this.lifetime) {
			this.dispose();
			return;
		}
		
		this.velocity.subtract(0, 0.04f * this.gravity, 0);
		this.move(this.velocity.getX(), this.velocity.getY(), this.velocity.getZ());
		this.velocity.multiply(0.98f, 0.98f, 0.98f);
		if (this.isOnGround()) {
			this.velocity.multiply(0.7f, 0, 0.7f);
		}
	}
	
	public BoundingBox getBoundingBox() {
		return this.bb;
	}
	
	protected void setBoundingBox(BoundingBox bb) {
		this.basebb = bb;
		this.bb = this.basebb.clone();
		this.bb.move(this.pos.getX(), this.pos.getY(), this.pos.getZ());
	}
	
	public void move(float x, float y, float z) {
		float oldMovX = x;
		float oldMovY = y;
		float oldMovZ = z;
		List<BoundingBox> boxes = ((ClientLevel) OpenClassic.getClient().getLevel()).getBoxes(this.bb.expand(x, y, z));
		for (BoundingBox box : boxes) {
			y = box.clipYCollide(this.bb, y);
		}

		this.bb.move(0, y, 0);

		for (BoundingBox box : boxes) {
			x = box.clipXCollide(this.bb, x);
		}

		this.bb.move(x, 0, 0);

		for (BoundingBox box : boxes) {
			z = box.clipZCollide(this.bb, z);
		}

		this.bb.move(0, 0, z);

		this.pos.setX((this.bb.getX1() + this.bb.getX2()) / 2f);
		this.pos.setY(this.bb.getY1());
		this.pos.setZ((this.bb.getZ1() + this.bb.getZ2()) / 2f);
		this.grounded = oldMovY != y && oldMovY < 0;

		if(oldMovX != x) {
			this.velocity.setX(0);
		}

		if(oldMovY != y) {
			this.velocity.setY(0);
		}

		if(oldMovZ != z) {
			this.velocity.setZ(0);
		}
	}
	
	public boolean isOnGround() {
		return this.grounded;
	}
	
	public abstract void render(float delta);
	
	public void dispose() {
		this.disposed = true;
	}
	
	public boolean isDisposed() {
		return this.disposed;
	}
	
	public Position getPosition() {
		return this.pos;
	}
	
	public void setPosition(Position pos) {
		this.pos = pos;
	}
	
	public Vector getVelocity() {
		return this.velocity;
	}
	
	public void setVelocity(Vector velocity) {
		this.velocity = velocity;
	}
	
	public float getSize() {
		return this.size;
	}
	
	public Particle setSize(float size) {
		this.size = size;
		return this;
	}
	
	public Particle setPower(float power) {
		this.velocity.multiply(power, 0, power);
		this.velocity.setY((this.velocity.getY() - 0.1f) * power + 0.1f);
		return this;
	}
	
}
