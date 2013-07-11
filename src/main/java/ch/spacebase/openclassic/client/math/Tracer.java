package ch.spacebase.openclassic.client.math;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.math.Vector;
import ch.spacebase.openclassic.api.util.Constants;

// Base tracer math borrowed from ArdorcraftAPI (https://github.com/rherlitz/ArdorCraft)
public class Tracer {

	private static final int MAX_ITERATIONS = 4;
	private static final double EPSILON = 0.0001;

	private double mult = 1;
	private final Vector tmax = new Vector(0, 0, 0);
	private final Vector tdelta = new Vector(0, 0, 0);
	private final Vector newPos = new Vector(0, 0, 0);
	private final Level level;

	public Tracer(Level level) {
		this.level = level;
	}
	
	public Intersection trace(Vector curpos, Vector raydir, int iterations) {
		return this.trace(curpos, raydir, iterations, new Conditions() {
			@Override
			public boolean satisfied(BlockType block, int x, int y, int z) {
				return block != null && block != VanillaBlock.AIR && !block.isLiquid() && block.getModel().getSelectionBox(x, y, z) != null;
			}
		});
	}
	
	public Intersection traceLiquid(Vector curpos, Vector raydir, int iterations) {
		return this.trace(curpos, raydir, iterations, new Conditions() {
			@Override
			public boolean satisfied(BlockType block, int x, int y, int z) {
				return block != null && block.isLiquid() && block.getData() == 0;
			}
		});
	}

	public Intersection trace(Vector curpos, Vector raydir, int iterations, Conditions cond) {
		this.tmax.set(0, 0, 0);
		this.tdelta.set(0, 0, 0);

		this.mult = 1;
		boolean back = false;

		Intersection inter = new Intersection();
		if (curpos.getY() < 0) return inter;
		if (curpos.getY() >= Constants.COLUMN_HEIGHT && raydir.getY() >= 0) {
			return inter;
		}

		if (curpos.getY() >= Constants.COLUMN_HEIGHT) {
			double diff = Constants.COLUMN_HEIGHT - curpos.getY();
			double t = diff / raydir.getY();
			this.newPos.set(raydir).multiply(t, t, t).add(curpos);
			curpos = this.newPos;
		}

		int x = (int) Math.floor(curpos.getX());
		int y = (int) Math.floor(curpos.getY());
		int z = (int) Math.floor(curpos.getZ());

		BlockType block1 = this.level.getBlockTypeAt(x, y, z);
		if(cond.satisfied(block1, x, y, z)) {
			raydir = raydir.clone().multiply(-1, -1, -1);
			this.mult = -1;
			back = true;
		}

		int stepX, stepY, stepZ;
		double cbx, cby, cbz;

		if (raydir.getX() > 0) {
			stepX = 1;
			cbx = x + 1;
		} else {
			stepX = -1;
			cbx = x;
		}
		
		if (raydir.getY() > 0) {
			stepY = 1;
			cby = y + 1;
		} else {
			stepY = -1;
			cby = y;
		}
		
		if (raydir.getZ() > 0) {
			stepZ = 1;
			cbz = z + 1;
		} else {
			stepZ = -1;
			cbz = z;
		}

		if (raydir.getX() != 0) {
			float rxr = 1 / raydir.getX();
			this.tmax.setX((float) (cbx - curpos.getX()) * rxr);
			this.tdelta.setX(stepX * rxr);
		} else {
			this.tmax.setX(1000000);
		}

		if (raydir.getY() != 0) {
			float ryr = 1 / raydir.getY();
			this.tmax.setY((float) (cby - curpos.getY()) * ryr);
			this.tdelta.setY(stepY * ryr);
		} else {
			tmax.setY(1000000);
		}

		if (raydir.getZ() != 0) {
			float rzr = 1 / raydir.getZ();
			this.tmax.setZ((float) (cbz - curpos.getZ()) * rzr);
			this.tdelta.setZ(stepZ * rzr);
		} else {
			this.tmax.setZ(1000000);
		}

		int oldX = x, oldY = y, oldZ = z;
		int modIterations = 0;
		for (int iteration = 0; iteration < iterations; iteration++) {
			if(modIterations > MAX_ITERATIONS) {
				inter.setHit(false);
				return inter;
			}
			
			if (this.tmax.getX() < this.tmax.getY()) {
				if (this.tmax.getX() < this.tmax.getZ()) {
					x = x + stepX;
					modIterations++;
					BlockType block = this.level.getBlockTypeAt(x, y, z);
					boolean hit = cond.satisfied(block, x, y, z);
					if (back && !hit || !back && hit) {
						this.gatherMin(inter, this.tmax, x, y, z, oldX, oldY, oldZ);
						inter.setHit(true);
						return inter;
					}

					this.tmax.setX(this.tmax.getX() + this.tdelta.getX());
				} else {
					z = z + stepZ;
					modIterations++;
					BlockType block = this.level.getBlockTypeAt(x, y, z);
					boolean hit = cond.satisfied(block, x, y, z);
					if (back && !hit || !back && hit) {
						this.gatherMin(inter, this.tmax, x, y, z, oldX, oldY, oldZ);
						inter.setHit(true);
						return inter;
					}

					this.tmax.setZ(this.tmax.getZ() + this.tdelta.getZ());
				}
			} else {
				if (this.tmax.getY() < this.tmax.getZ()) {
					y = y + stepY;
					modIterations++;
					if (y >= Constants.COLUMN_HEIGHT) {
						return inter;
					}
					
					BlockType block = this.level.getBlockTypeAt(x, y, z);
					boolean hit = cond.satisfied(block, x, y, z);
					if (back && !hit || !back && hit) {
						this.gatherMin(inter, this.tmax, x, y, z, oldX, oldY, oldZ);
						inter.setHit(true);
						return inter;
					}

					this.tmax.setY(this.tmax.getY() + this.tdelta.getY());
				} else {
					z = z + stepZ;
					modIterations++;
					BlockType block = this.level.getBlockTypeAt(x, y, z);
					boolean hit = cond.satisfied(block, x, y, z);
					if (back && !hit || !back && hit) {
						this.gatherMin(inter, this.tmax, x, y, z, oldX, oldY, oldZ);
						inter.setHit(true);
						return inter;
					}

					this.tmax.setZ(this.tmax.getZ() + this.tdelta.getZ());
				}
			}

			oldX = x;
			oldY = y;
			oldZ = z;
		}
		
		return inter;
	}

	private void gatherMin(Intersection inter, Vector tmax, int x, int y, int z, int oldX, int oldY, int oldZ) {
		inter.getDiff().setLevel(this.level);
		inter.getDiff().set(-(x - oldX), -(y - oldY), -(z - oldZ));
		inter.getPosition().setLevel(this.level);
		inter.getPosition().set(x, y, z);

		double min = tmax.getX();
		if (tmax.getY() < min) min = tmax.getY();
		if (tmax.getZ() < min) min = tmax.getZ();
		double length = min * this.mult;
		if (length > 0) {
			length = Math.max(length - EPSILON, 0);
		} else if (length < 0) {
			length = Math.min(length - EPSILON, 0);
		}

		inter.setLength(length);
	}
	
	private static interface Conditions {
		public boolean satisfied(BlockType block, int x, int y, int z);
	}

}
