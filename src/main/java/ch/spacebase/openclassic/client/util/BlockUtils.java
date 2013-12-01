package ch.spacebase.openclassic.client.util;

import java.util.Random;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.model.Model;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.math.BoundingBox;
import ch.spacebase.openclassic.client.level.ClientLevel;

import com.mojang.minecraft.entity.item.Item;
import com.mojang.minecraft.entity.model.Vector;
import com.mojang.minecraft.util.Intersection;

public class BlockUtils {

	private static Random rand = new Random();

	public static final Intersection clipSelection(int id, Level level, int x, int y, int z, Vector point, Vector other) {
		Model model = Blocks.fromId(id).getModel(level, x, y, z);

		point = point.add((-x), (-y), (-z));
		other = other.add((-x), (-y), (-z));
		BoundingBox box = model.getSelectionBox(0, 0, 0);
		if(box == null) return null;
		Vector x1 = point.getXIntersection(other, box.getX1());
		Vector x2 = point.getXIntersection(other, box.getX2());
		Vector y1 = point.getYIntersection(other, box.getY1());
		Vector y2 = point.getYIntersection(other, box.getY2());
		Vector z1 = point.getZIntersection(other, box.getZ1());
		Vector z2 = point.getZIntersection(other, box.getZ2());
		if(x1 != null && !xIntersectsSelection(model.getSelectionBox((int) x1.x, (int) x1.y, (int) x1.z), x1)) {
			x1 = null;
		}

		if(x2 != null && !xIntersectsSelection(model.getSelectionBox((int) x2.x, (int) x2.y, (int) x2.z), x2)) {
			x2 = null;
		}

		if(y1 != null && !yIntersectsSelection(model.getSelectionBox((int) y1.x, (int) y1.y, (int) y1.z), y1)) {
			y1 = null;
		}

		if(y2 != null && !yIntersectsSelection(model.getSelectionBox((int) y2.x, (int) y2.y, (int) y2.z), y2)) {
			y2 = null;
		}

		if(z1 != null && !zIntersectsSelection(model.getSelectionBox((int) z1.x, (int) z1.y, (int) z1.z), z1)) {
			z1 = null;
		}

		if(z2 != null && !zIntersectsSelection(model.getSelectionBox((int) z2.x, (int) z2.y, (int) z2.z), z2)) {
			z2 = null;
		}

		Vector result = null;
		if(x1 != null) {
			result = x1;
		}

		if(x2 != null && (result == null || point.distance(x2) < point.distance(result))) {
			result = x2;
		}

		if(y1 != null && (result == null || point.distance(y1) < point.distance(result))) {
			result = y1;
		}

		if(y2 != null && (result == null || point.distance(y2) < point.distance(result))) {
			result = y2;
		}

		if(z1 != null && (result == null || point.distance(z1) < point.distance(result))) {
			result = z1;
		}

		if(z2 != null && (result == null || point.distance(z2) < point.distance(result))) {
			result = z2;
		}

		if(result == null) {
			return null;
		} else {
			byte side = -1;
			if(result == x1) {
				side = 4;
			}

			if(result == x2) {
				side = 5;
			}

			if(result == y1) {
				side = 0;
			}

			if(result == y2) {
				side = 1;
			}

			if(result == z1) {
				side = 2;
			}

			if(result == z2) {
				side = 3;
			}

			return new Intersection(x, y, z, side, result.add(x, y, z));
		}
	}

	public static final Intersection clip(int id, Level level, int x, int y, int z, Vector point, Vector other) {
		Model model = Blocks.fromId(id).getModel(level, x, y, z);

		point = point.add((-x), (-y), (-z));
		other = other.add((-x), (-y), (-z));

		BoundingBox box = model.getCollisionBox(0, 0, 0);
		if(box == null) return null;
		Vector x1 = point.getXIntersection(other, box.getX1());
		Vector x2 = point.getXIntersection(other, box.getX2());
		Vector y1 = point.getYIntersection(other, box.getY1());
		Vector y2 = point.getYIntersection(other, box.getY2());
		Vector z1 = point.getZIntersection(other, box.getZ1());
		Vector z2 = point.getZIntersection(other, box.getZ2());
		if(x1 != null && !xIntersects(model.getCollisionBox((int) x1.x, (int) x1.y, (int) x1.z), x1)) {
			x1 = null;
		}

		if(x2 != null && !xIntersects(model.getCollisionBox((int) x2.x, (int) x2.y, (int) x2.z), x2)) {
			x2 = null;
		}

		if(y1 != null && !yIntersects(model.getCollisionBox((int) y1.x, (int) y1.y, (int) y1.z), y1)) {
			y1 = null;
		}

		if(y2 != null && !yIntersects(model.getCollisionBox((int) y2.x, (int) y2.y, (int) y2.z), y2)) {
			y2 = null;
		}

		if(z1 != null && !zIntersects(model.getCollisionBox((int) z1.x, (int) z1.y, (int) z1.z), z1)) {
			z1 = null;
		}

		if(z2 != null && !zIntersects(model.getCollisionBox((int) z2.x, (int) z2.y, (int) z2.z), z2)) {
			z2 = null;
		}

		Vector result = null;
		if(x1 != null) {
			result = x1;
		}

		if(x2 != null && (result == null || point.distance(x2) < point.distance(result))) {
			result = x2;
		}

		if(y1 != null && (result == null || point.distance(y1) < point.distance(result))) {
			result = y1;
		}

		if(y2 != null && (result == null || point.distance(y2) < point.distance(result))) {
			result = y2;
		}

		if(z1 != null && (result == null || point.distance(z1) < point.distance(result))) {
			result = z1;
		}

		if(z2 != null && (result == null || point.distance(z2) < point.distance(result))) {
			result = z2;
		}

		if(result == null) {
			return null;
		} else {
			byte side = -1;
			if(result == x1) {
				side = 4;
			}

			if(result == x2) {
				side = 5;
			}

			if(result == y1) {
				side = 0;
			}

			if(result == y2) {
				side = 1;
			}

			if(result == z1) {
				side = 2;
			}

			if(result == z2) {
				side = 3;
			}

			return new Intersection(x, y, z, side, result.add(x, y, z));
		}
	}
	
	public static Intersection clip(BoundingBox box, Vector point, Vector other) {
		Vector x0 = point.getXIntersection(other, box.getX1());
		Vector x1 = point.getXIntersection(other, box.getX2());
		Vector y0 = point.getYIntersection(other, box.getY1());
		Vector y1 = point.getYIntersection(other, box.getY2());
		Vector z0 = point.getZIntersection(other, box.getZ1());
		Vector z1 = point.getZIntersection(other, box.getZ2());
		if(!xIntersects(box, x0)) {
			x0 = null;
		}

		if(!xIntersects(box, x1)) {
			x1 = null;
		}

		if(!yIntersects(box, y0)) {
			y0 = null;
		}

		if(!yIntersects(box, y1)) {
			y1 = null;
		}

		if(!zIntersects(box, z0)) {
			z0 = null;
		}

		if(!zIntersects(box, z1)) {
			z1 = null;
		}

		Vector result = null;

		if(x0 != null) {
			result = x0;
		}

		if(x1 != null && (result == null || point.distanceSquared(x1) < point.distanceSquared(result))) {
			result = x1;
		}

		if(y0 != null && (result == null || point.distanceSquared(y0) < point.distanceSquared(result))) {
			result = y0;
		}

		if(y1 != null && (result == null || point.distanceSquared(y1) < point.distanceSquared(result))) {
			result = y1;
		}

		if(z0 != null && (result == null || point.distanceSquared(z0) < point.distanceSquared(result))) {
			result = z0;
		}

		if(z1 != null && (result == null || point.distanceSquared(z1) < point.distanceSquared(result))) {
			result = z1;
		}

		if(result == null) {
			return null;
		} else {
			byte side = -1;
			if(result == x0) {
				side = 4;
			}

			if(result == x1) {
				side = 5;
			}

			if(result == y0) {
				side = 0;
			}

			if(result == y1) {
				side = 1;
			}

			if(result == z0) {
				side = 2;
			}

			if(result == z1) {
				side = 3;
			}

			return new Intersection(0, 0, 0, side, result);
		}
	}

	private static boolean xIntersectsSelection(BoundingBox box, Vector point) {
		return point != null && point.y >= box.getY1() && point.y <= box.getY2() && point.z >= box.getZ1() && point.z <= box.getZ2();
	}

	private static boolean yIntersectsSelection(BoundingBox box, Vector point) {
		return point != null && point.x >= box.getX1() && point.x <= box.getX2() && point.z >= box.getZ1() && point.z <= box.getZ2();
	}

	private static boolean zIntersectsSelection(BoundingBox box, Vector point) {
		return point != null && point.x >= box.getX1() && point.x <= box.getX2() && point.y >= box.getY1() && point.y <= box.getY2();
	}

	private static boolean xIntersects(BoundingBox box, Vector point) {
		return point != null && point.y >= box.getY1() && point.y <= box.getY2() && point.z >= box.getZ1() && point.z <= box.getZ2();
	}

	private static boolean yIntersects(BoundingBox box, Vector point) {
		return point != null && point.x >= box.getX1() && point.x <= box.getX2() && point.z >= box.getZ1() && point.z <= box.getZ2();
	}

	private static boolean zIntersects(BoundingBox box, Vector point) {
		return point != null && point.x >= box.getX1() && point.x <= box.getX2() && point.y >= box.getY1() && point.y <= box.getY2();
	}

	public static boolean canExplode(BlockType type) {
		return type != VanillaBlock.STONE && type != VanillaBlock.COBBLESTONE && type != VanillaBlock.BEDROCK && type != VanillaBlock.COAL_ORE && type != VanillaBlock.IRON_ORE && type != VanillaBlock.GOLD_ORE && type != VanillaBlock.GOLD_BLOCK && type != VanillaBlock.IRON_BLOCK && type != VanillaBlock.SLAB && type != VanillaBlock.DOUBLE_SLAB && type != VanillaBlock.BRICK_BLOCK && type != VanillaBlock.MOSSY_COBBLESTONE && type != VanillaBlock.OBSIDIAN;
	}

	public static int getHardness(BlockType type) {
		if(type == VanillaBlock.SAPLING || type == VanillaBlock.DANDELION || type == VanillaBlock.ROSE || type == VanillaBlock.BROWN_MUSHROOM || type == VanillaBlock.RED_MUSHROOM || type == VanillaBlock.TNT) {
			return 0;
		} else if(type == VanillaBlock.RED_CLOTH || type == VanillaBlock.ORANGE_CLOTH || type == VanillaBlock.YELLOW_CLOTH || type == VanillaBlock.LIME_CLOTH || type == VanillaBlock.GREEN_CLOTH || type == VanillaBlock.AQUA_GREEN_CLOTH || type == VanillaBlock.CYAN_CLOTH || type == VanillaBlock.BLUE_CLOTH || type == VanillaBlock.PURPLE_CLOTH || type == VanillaBlock.INDIGO_CLOTH || type == VanillaBlock.VIOLET_CLOTH || type == VanillaBlock.MAGENTA_CLOTH || type == VanillaBlock.PINK_CLOTH || type == VanillaBlock.BLACK_CLOTH || type == VanillaBlock.GRAY_CLOTH || type == VanillaBlock.WHITE_CLOTH) {
			return 16;
		} else if(type == VanillaBlock.GRASS || type == VanillaBlock.DIRT || type == VanillaBlock.GRAVEL || type == VanillaBlock.SPONGE || type == VanillaBlock.SAND) {
			return 12;
		} else if(type == VanillaBlock.STONE || type == VanillaBlock.MOSSY_COBBLESTONE || type == VanillaBlock.SLAB || type == VanillaBlock.DOUBLE_SLAB) {
			return 20;
		} else if(type == VanillaBlock.COBBLESTONE || type == VanillaBlock.WOOD || type == VanillaBlock.BOOKSHELF) {
			return 30;
		} else if(type == VanillaBlock.BEDROCK) {
			return 19980;
		} else if(type == VanillaBlock.WATER || type == VanillaBlock.STATIONARY_WATER || type == VanillaBlock.LAVA || type == VanillaBlock.STATIONARY_LAVA) {
			return 2000;
		} else if(type == VanillaBlock.GOLD_ORE || type == VanillaBlock.IRON_ORE || type == VanillaBlock.COAL_ORE || type == VanillaBlock.GOLD_BLOCK) {
			return 60;
		} else if(type == VanillaBlock.LOG) {
			return 50;
		} else if(type == VanillaBlock.LEAVES) {
			return 4;
		} else if(type == VanillaBlock.GLASS) {
			return 6;
		} else if(type == VanillaBlock.IRON_BLOCK) {
			return 100;
		} else if(type == VanillaBlock.OBSIDIAN) {
			return 200;
		}

		return 0;
	}

	public static int getDrop(BlockType type) {
		if(type == VanillaBlock.STONE) {
			return VanillaBlock.COBBLESTONE.getId();
		} else if(type == VanillaBlock.GRASS) {
			return VanillaBlock.DIRT.getId();
		} else if(type == VanillaBlock.GOLD_ORE) {
			return VanillaBlock.GOLD_BLOCK.getId();
		} else if(type == VanillaBlock.IRON_ORE) {
			return VanillaBlock.IRON_BLOCK.getId();
		} else if(type == VanillaBlock.COAL_ORE || type == VanillaBlock.DOUBLE_SLAB) {
			return VanillaBlock.SLAB.getId();
		} else if(type == VanillaBlock.LOG) {
			return VanillaBlock.WOOD.getId();
		} else if(type == VanillaBlock.LEAVES) {
			return VanillaBlock.SAPLING.getId();
		}

		return type.getId();
	}

	public static int getDropCount(BlockType type) {
		if(type == VanillaBlock.WATER || type == VanillaBlock.STATIONARY_WATER || type == VanillaBlock.LAVA || type == VanillaBlock.STATIONARY_LAVA || type == VanillaBlock.TNT || type == VanillaBlock.BOOKSHELF) {
			return 0;
		} else if(type == VanillaBlock.DOUBLE_SLAB) {
			return 2;
		} else if(type == VanillaBlock.GOLD_ORE || type == VanillaBlock.COAL_ORE || type == VanillaBlock.IRON_ORE) {
			return rand.nextInt(3) + 1;
		} else if(type == VanillaBlock.LOG) {
			return rand.nextInt(3) + 3;
		} else if(type == VanillaBlock.LEAVES) {
			return rand.nextInt(10) == 0 ? 1 : 0;
		}

		return 1;
	}

	public static void dropItems(BlockType block, ClientLevel level, int x, int y, int z) {
		dropItems(block, level, x, y, z, 1);
	}

	public static void dropItems(BlockType block, ClientLevel level, int x, int y, int z, float chance) {
		if(OpenClassic.getClient().isInSurvival()) {
			int dropCount = getDropCount(block);
			for(int count = 0; count < dropCount; count++) {
				if(rand.nextFloat() <= chance) {
					float xOffset = rand.nextFloat() * 0.7F + (1.0F - 0.7F) * 0.5F;
					float yOffset = rand.nextFloat() * 0.7F + (1.0F - 0.7F) * 0.5F;
					float zOffset = rand.nextFloat() * 0.7F + (1.0F - 0.7F) * 0.5F;
					level.addEntity(new Item(level, x + xOffset, y + yOffset, z + zOffset, getDrop(block)));
				}
			}
		}
	}
	
	public static boolean preventsRendering(ClientLevel level, float x, float y, float z, float distance) {
		return preventsRendering(level, x - distance, y - distance, z - distance) || preventsRendering(level, x - distance, y - distance, z + distance) || preventsRendering(level, x - distance, y + distance, z - distance) || preventsRendering(level, x - distance, y + distance, z + distance) || preventsRendering(level, x + distance, y - distance, z - distance) || preventsRendering(level, x + distance, y - distance, z + distance) || preventsRendering(level, x + distance, y + distance, z - distance) || preventsRendering(level, x + distance, y + distance, z + distance);
	}

	public static boolean preventsRendering(ClientLevel level, float x, float y, float z) {
		BlockType type = level.getBlockTypeAt((int) x, (int) y, (int) z);
		return type != null && type.getPreventsRendering();
	}

}
