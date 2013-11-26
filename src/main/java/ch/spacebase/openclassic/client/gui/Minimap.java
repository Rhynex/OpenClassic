package ch.spacebase.openclassic.client.gui;

import java.awt.Color;
import java.awt.image.BufferedImage;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.model.SubTexture;
import ch.spacebase.openclassic.api.gui.Screen;
import ch.spacebase.openclassic.api.gui.widget.Widget;
import ch.spacebase.openclassic.client.render.RenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;

public class Minimap extends Widget {

	private BufferedImage texture;

	public Minimap(int id, int x, int y, int width, int height, Screen parent) {
		super(id, x, y, width, height, parent);
		this.texture = new BufferedImage(width - 2, height - 2, BufferedImage.TYPE_INT_ARGB);
	}

	@Override
	public void render() {
		if(OpenClassic.getClient().getLevel() == null) return;
		this.updateTexture();
		RenderHelper.getHelper().drawBox(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), Color.black.getRGB());
		RenderHelper.getHelper().drawImage(this.texture, this.getX() + 1, this.getY() + 1);
	}

	private void updateTexture() {
		if(OpenClassic.getClient().getLevel() == null) return;
		int imgX = 0;
		int imgY = 0;
		int scale = 2;
		int xrad = this.texture.getWidth() / scale / 2;
		int zrad = this.texture.getHeight() / scale / 2;
		for(int x = OpenClassic.getClient().getPlayer().getPosition().getBlockX() - xrad; x <= OpenClassic.getClient().getPlayer().getPosition().getBlockX() + xrad; x++) {
			for(int z = OpenClassic.getClient().getPlayer().getPosition().getBlockZ() - zrad; z <= OpenClassic.getClient().getPlayer().getPosition().getBlockZ() + zrad; z++) {
				int y = OpenClassic.getClient().getLevel().getHighestBlockY(x, z);
				BlockType type = OpenClassic.getClient().getLevel().getBlockTypeAt(x, y, z);
				while(type == VanillaBlock.GLASS) {
					int old = y;
					y = OpenClassic.getClient().getLevel().getHighestBlockY(x, z, y - 1);
					if(y == old || y < 0) {
						type = VanillaBlock.AIR;
						break;
					}

					type = OpenClassic.getClient().getLevel().getBlockTypeAt(x, y, z);
				}

				int rgb = this.getRGB(type);
				for(int w = 0; w < scale; w++) {
					for(int h = 0; h < scale; h++) {
						if(imgX + w < this.texture.getWidth() && imgY + h < this.texture.getHeight()) {
							if(type != null) {
								this.texture.setRGB(imgX + w, imgY + h, rgb);
							} else {
								this.texture.setRGB(imgX + w, imgY + h, 0);
							}
						}
					}
				}

				imgY += scale;
			}

			imgY = 0;
			imgX += scale;
		}

		for(int w = 0; w < scale; w++) {
			for(int h = 0; h < scale; h++) {
				this.texture.setRGB((this.texture.getWidth() / 2) + w, (this.texture.getHeight() / 2) + h, Color.orange.getRGB());
			}
		}
	}

	// TODO: Possible to base on block texture?
	private int getRGB(BlockType type) {
		if(type == VanillaBlock.AIR || type == null || type.getModel() == null || type.getModel().getQuads().size() == 0) return 0;
		int quad = 1;
		if(type.getModel().getQuads().size() < 2) {
			quad = 0;
		}
		
		SubTexture tex = type.getModel().getQuad(quad).getTexture();
		if(tex == null) return 0;
		
		if(!GeneralUtils.getMinecraft().textureManager.textures.containsKey(tex.getParent().getTexture())) {
			RenderHelper.getHelper().bindTexture(tex.getParent().getTexture(), tex.getParent().isInJar());
		}

		BufferedImage img = GeneralUtils.getMinecraft().textureManager.textureImgs.get(GeneralUtils.getMinecraft().textureManager.textures.get(tex.getParent().getTexture()));
		return img.getRGB((int) tex.getX1(), (int) tex.getY1());
	}

}
