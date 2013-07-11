package ch.spacebase.openclassic.client.gui.inventory;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

import java.util.ArrayList;
import java.util.List;

import ch.spacebase.openclassic.api.asset.texture.SubTexture;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.inventory.CraftingInventory;
import ch.spacebase.openclassic.api.inventory.CraftingListener;
import ch.spacebase.openclassic.api.inventory.ItemStack;
import ch.spacebase.openclassic.api.render.RenderHelper;

public class InventoryScreen extends GuiScreen implements CraftingListener {

	private List<Slot> slots = new ArrayList<Slot>();
	private ItemStack cursor = null;
	private SubTexture background;
	
	public InventoryScreen(SubTexture background) {
		this.background = background;
	}
	
	protected void addSlot(Slot slot) {
		this.slots.add(slot);
	}
	
	public void onClose() {
		if(this.cursor != null) {
			// TODO: drop item
			this.cursor = null;
		}
		
		this.slots.clear();
	}

	private Slot getSlotOnScreen(int x, int y) {
		for(Slot slot : this.slots) {
			if(x >= slot.getX() && x <= slot.getX() + 31 && y >= slot.getY() - 16 && y <= slot.getY() + 16) {
				return slot;
			}
		}
		
		return null;
	}

	public void render() {
		int mouseX = RenderHelper.getHelper().getRenderMouseX();
		int mouseY = RenderHelper.getHelper().getRenderMouseY();
		Slot slot = this.getSlotOnScreen(mouseX, mouseY);
		float x = this.getWidth() / 2 - this.background.getWidth() / 2;
		float y = this.getHeight() / 2 - this.background.getHeight() / 2;
		RenderHelper.getHelper().drawSubTex(this.background, x, y, 1);
		if(slot != null) {
			RenderHelper.getHelper().color(slot.getX() - 5, slot.getY() - 16, slot.getX() + 27, slot.getY() + 16, -1862270977, -1056964609);
		}

		this.renderExtra(x, y);
		for(Slot sl : this.slots) {
			this.renderStack(sl.getItem(), sl.getX(), sl.getY());
		}
		
		if(this.cursor != null) {
			this.renderStack(this.cursor, mouseX - 10, mouseY + 1);
		}
	}
	
	protected void renderExtra(float x, float y) {
	}
	
	private void renderStack(ItemStack it, int x, int y) {
		if(it == null) return;
		it.getItem().renderInventory(x, y);
		if(it.getSize() > 1) RenderHelper.getHelper().renderText(String.valueOf(it.getSize()), x + 26 - RenderHelper.getHelper().getStringWidth(String.valueOf(it.getSize())), y - 2, false);
		if(it.getDamage() > 0) {
			int wid = 26 - (it.getDamage() * 26) / it.getItem().getMaxDamage();
			int col = 255 - (it.getDamage() * 255) / it.getItem().getMaxDamage();
			glDisable(GL_DEPTH_TEST);
			int col2 = 255 - col << 16 | col << 8;
			int col1 = (255 - col) / 4 << 16 | 0x3f00;
			RenderHelper.getHelper().colorSolid(x - 3, y + 10, x + 25, y + 14, 0);
			RenderHelper.getHelper().colorSolid(x - 3, y + 10, x + 23, y + 13, col1);
			RenderHelper.getHelper().colorSolid(x - 3, y + 10, x - 3 + wid, y + 13, col2);
			glEnable(GL_DEPTH_TEST);
		}
	}

	public void onMouseClick(int x, int y, int button) {
		if(button == 0) {
			if(this.cursor != null && (x < this.getWidth() / 2 - this.background.getWidth() / 2 || x > this.getWidth() / 2 + this.background.getWidth() / 2 || y < this.getHeight() / 2 - this.background.getHeight() / 2 || y > this.getHeight() / 2 + this.background.getHeight() / 2)) {
				// TODO: drop item
				this.cursor = null;
				return;
			}
			
			Slot slot = this.getSlotOnScreen(x, y);
			if(slot == null) return;
			ItemStack it = slot.getItem();
			if(it != null) {
				if(this.cursor != null) {
					if(this.cursor.getItem() == it.getItem()) {
						int amt = Math.min(it.getItem().getMaxStackSize() - it.getSize(), this.cursor.getSize());
						if(amt <= 0) {
							if(slot.canPutItem() && slot.canTakeItem()) {
								slot.setItem(this.cursor);
								slot.onSwapItem();
								this.cursor = it;
							}
						} else {
							if(slot.canPutItem()) {
								this.cursor.setSize(this.cursor.getSize() - amt);
								it.setSize(it.getSize() + amt);
								slot.onPutItem();
								if(this.cursor.getSize() <= 0) this.cursor = null;
							} else if(slot.canTakeItem()) {
								int am = Math.min(this.cursor.getItem().getMaxStackSize() - this.cursor.getSize(), it.getSize());
								this.cursor.setSize(this.cursor.getSize() + am);
								it.setSize(it.getSize() - amt);
								if(it.getSize() <= 0) {
									slot.setItem(null);
									slot.onTakeItem();
								} else {
									slot.onTakePartialItem();
								}
							}
						}
					} else {
						if(slot.canPutItem() && slot.canTakeItem()) {
							slot.setItem(this.cursor);
							slot.onSwapItem();
							this.cursor = it;
						}
					}
				} else {
					if(slot.canTakeItem()) {
						this.cursor = it;
						slot.setItem(null);
						slot.onTakeItem();
					}
				}
			} else {
				if(slot.canPutItem()) {
					slot.setItem(this.cursor);
					slot.onPutItem();
					this.cursor = null;
				}
			}
		} else if(button == 1) {
			if(this.cursor != null && (x < this.getWidth() / 2 - 176 || x > this.getWidth() / 2 + 176 || y < 60 || y > 394)) {
				// TODO: drop item
				this.cursor.setSize(this.cursor.getSize() - 1);
				if(this.cursor.getSize() <= 0) this.cursor = null;
				return;
			}
			
			Slot slot = this.getSlotOnScreen(x, y);
			if(slot == null) return;
			ItemStack it = slot.getItem();
			if(it != null) {
				if(this.cursor != null) {
					if(this.cursor.getItem() == it.getItem() && it.getSize() < it.getItem().getMaxStackSize()) {
						if(slot.canPutItem()) {
							it.setSize(it.getSize() + 1);
							slot.onPutItem();
							this.cursor.setSize(this.cursor.getSize() - 1);
							if(this.cursor.getSize() <= 0) this.cursor = null;
						}
					}
				} else if(it.getSize() > 1) {
					if(slot.canTakeItem()) {
						int amt = it.getSize() / 2;
						this.cursor = new ItemStack(it.getItem(), amt);
						it.setSize(it.getSize() - amt);
						slot.onTakePartialItem();
					}
				} else {
					if(slot.canTakeItem()) {
						this.cursor = it;
						slot.setItem(null);
						slot.onTakeItem();
					}
				}
			} else if(this.cursor != null) {
				if(slot.canPutItem()) {
					slot.setItem(new ItemStack(this.cursor.getItem(), 1));
					slot.onPutItem();
					this.cursor.setSize(this.cursor.getSize() - 1);
					if(this.cursor.getSize() <= 0) this.cursor = null;
				}
			}
		}
	}

	@Override
	public void updateCrafting(CraftingInventory inv) {
	}
	
}
