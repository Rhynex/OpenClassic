package ch.spacebase.openclassic.game.block.complex;

import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.complex.ComplexBlock;
import ch.spacebase.openclassic.api.block.complex.vanilla.Chest;
import ch.spacebase.openclassic.api.inventory.Inventory;
import ch.spacebase.openclassic.api.inventory.ItemStack;
import ch.spacebase.openclassic.api.item.Items;
import ch.spacebase.opennbt.TagBuilder;
import ch.spacebase.opennbt.tag.ByteTag;
import ch.spacebase.opennbt.tag.CompoundTag;
import ch.spacebase.opennbt.tag.IntTag;
import ch.spacebase.opennbt.tag.ShortTag;

public class ClassicChest extends ComplexBlock implements Chest {

	private Inventory inv = new Inventory(27);

	public ClassicChest(Position pos) {
		super(pos);
	}

	@Override
	public Inventory getInventory() {
		return this.inv;
	}

	@Override
	public String getName() {
		return "Chest";
	}

	@Override
	public void read(CompoundTag tag) {
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
	}

}
