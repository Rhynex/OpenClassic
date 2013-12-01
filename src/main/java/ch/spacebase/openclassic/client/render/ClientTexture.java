package ch.spacebase.openclassic.client.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.game.GameTexture;

public class ClientTexture extends GameTexture {

	protected static int bound = -1;
	
	private int textureId = -1;
	private boolean disposed = false;
	
	private int frameTimer = 0;
	private int currentFrame = 0;
	private BufferedImage frames[];
	
	public ClientTexture(URL url) {
		super(url);
	}
	
	public ClientTexture(URL url, int frameWidth, int frameHeight, int frameSpeed) {
		super(url, frameWidth, frameHeight, frameSpeed);
	}
	
	public ClientTexture(BufferedImage image) {
		super(null);
		this.image = image;
	}
	
	@Override
	public int[] getRGBA() {
		BufferedImage image = this.getCurrentImage();
		int rgba[] = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), rgba, 0, image.getWidth());
		return rgba;
	}
	
	@Override
	public int getRGB(int x, int y) {
		return this.getCurrentImage().getRGB(x, y);
	}
	
	@Override
	public int getWidth() {
		return this.getCurrentImage().getWidth();
	}
	
	@Override
	public int getHeight() {
		return this.getCurrentImage().getHeight();
	}
	
	@Override
	public boolean isBound() {
		return this.textureId != -1 && bound == this.textureId;
	}

	@Override
	public void bind() {
		if(this.isDisposed()) {
			return;
		}
		
		if(this.textureId == -1) {
			this.textureId = this.generateTextureId();
		}
		
		if(this.isBound()) {
			return;
		}

		bound = this.textureId;
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureId);
	}
	
	public int generateTextureId() {
		int textureId = GL11.glGenTextures();
		this.bind(this.getCurrentImage(), textureId);
		return textureId;
	}
	
	private void bind(BufferedImage image, int textureId) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		if(OpenClassic.getClient().getSettings().getBooleanSetting("options.smoothing").getValue() && RenderHelper.getHelper().getMipmapMode() != MipmapMode.NONE) {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 2);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);
		} else {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		}

		int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

		ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);

		for(int y = 0; y < image.getHeight(); y++) {
			for(int x = 0; x < image.getWidth(); x++) {
				int pixel = pixels[y * image.getWidth() + x];
				int red = (pixel >> 16) & 0xFF;
				int blue = pixel & 0xFF;
				int green = (pixel >> 8) & 0xFF;
				int alpha = (pixel >> 24) & 0xFF;
				if(OpenClassic.getClient().getSettings().getBooleanSetting("options.3d-anaglyph").getValue()) {
					green = (red * 30 + green * 70) / 100;
					blue = (red * 30 + blue * 70) / 100;
					red = (red * 30 + green * 59 + blue * 11) / 100;
				}

				buffer.put((byte) red);
				buffer.put((byte) green);
				buffer.put((byte) blue);
				buffer.put((byte) alpha);
			}
		}

		buffer.flip();
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		if(OpenClassic.getClient().getSettings().getBooleanSetting("options.smoothing").getValue()) {
			switch(RenderHelper.getHelper().getMipmapMode()) {
				case GL30:
					GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
					break;
				case FRAMEBUFFER_EXT:
					EXTFramebufferObject.glGenerateMipmapEXT(GL11.GL_TEXTURE_2D);
					break;
			}
		}
	}
	
	public void resetTextureId() {
		this.textureId = -1;
	}
	
	private BufferedImage getCurrentImage() {
		return this.frames != null ? this.frames[this.currentFrame] : this.image;
	}
	
	public void update() {
		if(this.frames != null) {
			this.frameTimer++;
			if(this.frameTimer >= this.frameSpeed) {
				this.frameTimer = 0;
				this.currentFrame++;
				if(this.currentFrame >= this.frames.length) {
					this.currentFrame = 0;
				}
				
				BufferedImage image = this.frames[this.currentFrame];
				int rgba[] = new int[image.getWidth() * image.getHeight()];
				image.getRGB(0, 0, image.getWidth(), image.getHeight(), rgba, 0, image.getWidth());
				this.image.setRGB(0, 0, this.getWidth(), this.getHeight(), rgba, 0, this.getWidth());
				this.bind(this.image, this.textureId);
			}
		}
	}
	
	public boolean isDisposed() {
		return this.disposed;
	}
	
	public void dispose() {
		GL11.glDeleteTextures(this.textureId);
		this.disposed = true;
	}
	
	public void reload() {
		this.loadImage(this.url);
		this.resetTextureId();
	}
	
	@Override
	public void loadImage(URL url) {
		String pack = OpenClassic.getGame().getConfig().getString("options.texture-pack");
		boolean found = false;
		if(!pack.equals("none")) {
			String path = url.getPath();
			ZipFile zip = null;
			try {
				zip = new ZipFile(new File(OpenClassic.getClient().getDirectory(), "texturepacks/" + pack));
				String p = path.startsWith("/") ? path.substring(1, path.length()) : path;
				if(zip.getEntry(p) != null) {
					this.image = ImageIO.read(zip.getInputStream(zip.getEntry(path.startsWith("/") ? path.substring(1, path.length()) : path)));
					found = true;
				}
			} catch(IOException e) {
				OpenClassic.getLogger().severe("Failed to read texture \"" + url.toString() + "\" from texture pack.");
				e.printStackTrace();
				return;
			} finally {
				IOUtils.closeQuietly(zip);
			}
		}

		if(!found) {
			super.loadImage(url);
		}
		
		if(this.frameSpeed > -1) {
			this.frames = new BufferedImage[(this.image.getWidth() / frameWidth) * (this.image.getHeight() / frameHeight)];
			int count = 0;
			for(int x = 0; x < this.image.getWidth(); x += frameWidth) {
				for(int y = 0; y < this.image.getHeight(); y += frameHeight) {
					this.frames[count] = this.image.getSubimage(x, y, frameWidth, frameHeight);
					count++;
				}
			}
		}
	}
	
}
