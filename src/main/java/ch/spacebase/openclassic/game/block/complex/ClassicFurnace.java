package ch.spacebase.openclassic.game.block.complex;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.complex.ComplexBlock;
import ch.spacebase.openclassic.api.block.complex.vanilla.Furnace;
import ch.spacebase.openclassic.api.inventory.FurnaceInventory;
import ch.spacebase.openclassic.api.inventory.ItemStack;
import ch.spacebase.openclassic.api.inventory.recipe.Fuel;
import ch.spacebase.openclassic.api.inventory.recipe.SmeltingRecipe;
import ch.spacebase.openclassic.api.item.Item;
import ch.spacebase.openclassic.api.item.Items;
import ch.spacebase.opennbt.TagBuilder;
import ch.spacebase.opennbt.tag.ByteTag;
import ch.spacebase.opennbt.tag.CompoundTag;
import ch.spacebase.opennbt.tag.IntTag;
import ch.spacebase.opennbt.tag.ShortTag;

public class ClassicFurnace extends ComplexBlock implements Furnace {

	private FurnaceInventory inv = new FurnaceInventory();
	private int totalFuelTime = 0;
	private int fuelTime = 0;
	private int smeltTime = 0;

	public ClassicFurnace(Position pos) {
		super(pos);
	}

	@Override
	public FurnaceInventory getInventory() {
		return this.inv;
	}

	@Override
	public int getFuelTime() {
		return this.fuelTime;
	}

	@Override
	public int getTotalFuelTime() {
		return this.totalFuelTime;
	}

	@Override
	public int getSmeltTime() {
		return this.smeltTime;
	}

	@Override
	public int getRemainingSmeltTime(int scale) {
		return (this.smeltTime * scale) / 200;
	}

	@Override
	public int getRemainingFuel(int scale) {
		if(this.totalFuelTime == 0) this.totalFuelTime = 200;
		return (this.fuelTime * scale) / this.totalFuelTime;
	}

	@Override
	public boolean hasFuel() {
		return this.fuelTime > 0;
	}

	@Override
	public String getName() {
		return "Furnace";
	}

	@Override
	public void read(CompoundTag tag) {
		this.totalFuelTime = ((IntTag) tag.get("TotalFuelTime")).getValue();
		this.fuelTime = ((IntTag) tag.get("FuelTime")).getValue();
		this.smeltTime = ((IntTag) tag.get("SmeltTime")).getValue();
		CompoundTag inventory = (CompoundTag) tag.get("Inventory");
		for(int slot = 0; slot < this.inv.getSize(); slot++) {
			CompoundTag item = (CompoundTag) inventory.get("Slot" + slot);
			short id = ((ShortTag) item.get("ID")).getValue();
			if(id != -1) {
				ItemStack it = new ItemStack(Items.get(id, ((ByteTag) item.get("Data")).getValue()), ((IntTag) item.get("Size")).getValue());
				it.setDamage(((IntTag) item.get("Damage")).getValue());
				this.inv.setItem(slot, it);
			} else {
				this.inv.setItem(slot, null);
			}
		}
	}

	@Override
	public void write(CompoundTag tag) {
		tag.put("TotalFuelTime", new IntTag("TotalFuelTime", this.totalFuelTime));
		tag.put("FuelTime", new IntTag("FuelTime", this.fuelTime));
		tag.put("SmeltTime", new IntTag("SmeltTime", this.smeltTime));
		TagBuilder inventory = new TagBuilder("Inventory");
		for(int slot = 0; slot < this.inv.getSize(); slot++) {
			ItemStack it = this.inv.getItem(slot);
			TagBuilder item = new TagBuilder("Slot" + slot);
			item.append("ID", (short) (it != null ? it.getItem().getId() : -1));
			if(it != null) {
				item.append("Data", it.getItem().getData());
				item.append("Size", it.getSize());
				item.append("Damage", it.getDamage());
			}

			inventory.append(item);
		}

		CompoundTag inv = inventory.toCompoundTag();
		tag.put(inv.getName(), inv);
	}

	@Override
	public void tick() {
		boolean fueled = this.hasFuel();
		if(this.fuelTime > 0) this.fuelTime--;
		if(this.fuelTime == 0 && this.canSmelt()) {
			this.totalFuelTime = this.fuelTime = this.getBurnTime(this.inv.getFuel());
			if(this.fuelTime > 0) {
				if(this.inv.getFuel() != null) {
					this.inv.getFuel().setSize(this.inv.getFuel().getSize() - 1);
					if(this.inv.getFuel().getSize() == 0) {
						this.inv.setFuel(null);
					}
				}
			}
		}

		if(this.hasFuel() && this.canSmelt()) {
			this.smeltTime++;
			if(this.smeltTime == 200) {
				this.smeltTime = 0;
				this.smeltItem();
			}
		} else {
			this.smeltTime = 0;
		}

		if(fueled && !this.hasFuel()) {
			byte data = this.getPosition().getBlockType().getData();
			if(data >= 4) {
				data -= 4;
				this.getPosition().getLevel().setBlockAt(this.getPosition(), Blocks.get(31, data));
			}
		} else if(!fueled && this.hasFuel()) {
			byte data = this.getPosition().getBlockType().getData();
			if(data < 4) {
				data += 4;
				this.getPosition().getLevel().setBlockAt(this.getPosition(), Blocks.get(31, data));
			}
		}
	}

	private boolean canSmelt() {
		if(this.inv.getSmelting() == null) return false;
		ItemStack item = this.getSmeltResult(this.inv.getSmelting().getItem());
		return item != null && (this.inv.getResult() == null || this.inv.getResult().getItem() == item.getItem() && this.inv.getResult().getSize() + item.getSize() <= this.inv.getResult().getItem().getMaxStackSize());
	}

	public void smeltItem() {
		if(!this.canSmelt()) return;
		ItemStack item = this.getSmeltResult(this.inv.getSmelting().getItem());
		if(item == null) return;
		if(this.inv.getResult() == null) {
			this.inv.setResult(item.clone());
		} else if(this.inv.getResult().getItem() == item.getItem()) {
			this.inv.getResult().setSize(this.inv.getResult().getSize() + item.getSize());
		}

		this.inv.getSmelting().setSize(this.inv.getSmelting().getSize() - 1);
		if(this.inv.getSmelting().getSize() <= 0) {
			this.inv.setSmelting(null);
		}
	}

	private ItemStack getSmeltResult(Item item) {
		SmeltingRecipe recipe = OpenClassic.getGame().getRecipeManager().getSmeltingRecipe(item);
		if(recipe != null) return recipe.getResult();
		return null;
	}

	private int getBurnTime(ItemStack item) {
		if(item == null) return 0;
		Fuel fuel = OpenClassic.getGame().getRecipeManager().getFuel(item.getItem());
		if(fuel != null) return fuel.getTime();
		return 0;
	}

}
