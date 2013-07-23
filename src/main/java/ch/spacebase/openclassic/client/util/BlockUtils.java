package ch.spacebase.openclassic.client.util;

import java.util.Random;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.model.BoundingBox;
import ch.spacebase.openclassic.api.block.model.Model;

import com.mojang.minecraft.entity.item.Item;
import com.mojang.minecraft.entity.model.Vector;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.phys.Intersection;

public class BlockUtils {

	private static Random rand = new Random();

	public static final Intersection clipSelection(int id, int x, int y, int z, Vector point, Vector other) {
		Model model = Blocks.fromId(id).getModel();

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
		if(!xIntersectsSelection(id, x1)) {
			x1 = null;
		}

		if(!xIntersectsSelection(id, x2)) {
			x2 = null;
		}

		if(!yIntersectsSelection(id, y1)) {
			y1 = null;
		}

		if(!yIntersectsSelection(id, y2)) {
			y2 = null;
		}

		if(!zIntersectsSelection(id, z1)) {
			z1 = null;
		}

		if(!zIntersectsSelection(id, z2)) {
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

	public static final Intersection clip(int id, int x, int y, int z, Vector point, Vector other) {
		Model model = Blocks.fromId(id).getModel();

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
		if(!xIntersects(id, x1)) {
			x1 = null;
		}

		if(!xIntersects(id, x2)) {
			x2 = null;
		}

		if(!yIntersects(id, y1)) {
			y1 = null;
		}

		if(!yIntersects(id, y2)) {
			y2 = null;
		}

		if(!zIntersects(id, z1)) {
			z1 = null;
		}

		if(!zIntersects(id, z2)) {
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

	private static boolean xIntersectsSelection(int id, Vector point) {
		Model model = Blocks.fromId(id).getModel();
		return point != null && point.y >= model.getSelectionBox((int) point.x, (int) point.y, (int) point.z).getY1() && point.y <= model.getSelectionBox((int) point.x, (int) point.y, (int) point.z).getY2() && point.z >= model.getSelectionBox((int) point.x, (int) point.y, (int) point.z).getZ1() && point.z <= model.getSelectionBox((int) point.x, (int) point.y, (int) point.z).getZ2();
	}

	private static boolean yIntersectsSelection(int id, Vector point) {
		Model model = Blocks.fromId(id).getModel();
		return point != null && point.x >= model.getSelectionBox((int) point.x, (int) point.y, (int) point.z).getX1() && point.x <= model.getSelectionBox((int) point.x, (int) point.y, (int) point.z).getX2() && point.z >= model.getSelectionBox((int) point.x, (int) point.y, (int) point.z).getZ1() && point.z <= model.getSelectionBox((int) point.x, (int) point.y, (int) point.z).getZ2();
	}

	private static boolean zIntersectsSelection(int id, Vector point) {
		Model model = Blocks.fromId(id).getModel();
		return point != null && point.x >= model.getSelectionBox((int) point.x, (int) point.y, (int) point.z).getX1() && point.x <= model.getSelectionBox((int) point.x, (int) point.y, (int) point.z).getX2() && point.y >= model.getSelectionBox((int) point.x, (int) point.y, (int) point.z).getY1() && point.y <= model.getSelectionBox((int) point.x, (int) point.y, (int) point.z).getY2();
	}

	private static boolean xIntersects(int id, Vector point) {
		Model model = Blocks.fromId(id).getModel();
		return point != null && point.y >= model.getCollisionBox((int) point.x, (int) point.y, (int) point.z).getY1() && point.y <= model.getCollisionBox((int) point.x, (int) point.y, (int) point.z).getY2() && point.z >= model.getCollisionBox((int) point.x, (int) point.y, (int) point.z).getZ1() && point.z <= model.getCollisionBox((int) point.x, (int) point.y, (int) point.z).getZ2();
	}

	private static boolean yIntersects(int id, Vector point) {
		Model model = Blocks.fromId(id).getModel();
		return point != null && point.x >= model.getCollisionBox((int) point.x, (int) point.y, (int) point.z).getX1() && point.x <= model.getCollisionBox((int) point.x, (int) point.y, (int) point.z).getX2() && point.z >= model.getCollisionBox((int) point.x, (int) point.y, (int) point.z).getZ1() && point.z <= model.getCollisionBox((int) point.x, (int) point.y, (int) point.z).getZ2();
	}

	private static boolean zIntersects(int id, Vector point) {
		Model model = Blocks.fromId(id).getModel();
		return point != null && point.x >= model.getCollisionBox((int) point.x, (int) point.y, (int) point.z).getX1() && point.x <= model.getCollisionBox((int) point.x, (int) point.y, (int) point.z).getX2() && point.y >= model.getCollisionBox((int) point.x, (int) point.y, (int) point.z).getY1() && point.y <= model.getCollisionBox((int) point.x, (int) point.y, (int) point.z).getY2();
	}

	public static AABB getSelectionBox(int id, int x, int y, int z) {
		BlockType type = Blocks.fromId(id);
		if(type == null) return null;
		BoundingBox bb = type.getModel().getSelectionBox(x, y, z);
		if(bb == null) return null;
		return new AABB(bb.getX1(), bb.getY1(), bb.getZ1(), bb.getX2(), bb.getY2(), bb.getZ2());
	}

	public static AABB getCollisionBox(int id, int x, int y, int z) {
		BlockType type = Blocks.fromId(id);
		if(type == null) return null;
		BoundingBox bb = type.getModel().getCollisionBox(x, y, z);
		if(bb == null) return null;
		return new AABB(bb.getX1(), bb.getY1(), bb.getZ1(), bb.getX2(), bb.getY2(), bb.getZ2());
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

	public static void dropItems(int block, Level level, int x, int y, int z) {
		dropItems(block, level, x, y, z, 1);
	}

	public static void dropItems(int block, Level level, int x, int y, int z, float chance) {
		if(!level.creativeMode) {
			int dropCount = getDropCount(Blocks.fromId(block));

			for(int count = 0; count < dropCount; count++) {
				if(rand.nextFloat() <= chance) {
					float xOffset = rand.nextFloat() * 0.7F + (1.0F - 0.7F) * 0.5F;
					float yOffset = rand.nextFloat() * 0.7F + (1.0F - 0.7F) * 0.5F;
					float zOffset = rand.nextFloat() * 0.7F + (1.0F - 0.7F) * 0.5F;
					level.addEntity(new Item(level, x + xOffset, y + yOffset, z + zOffset, getDrop(Blocks.fromId(block))));
				}
			}

		}
	}

}
