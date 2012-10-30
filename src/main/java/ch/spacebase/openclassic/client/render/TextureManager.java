package ch.spacebase.openclassic.client.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GLContext;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_MAX_LEVEL;
import static org.lwjgl.opengl.GL14.GL_GENERATE_MIPMAP;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.model.Texture;
import ch.spacebase.openclassic.client.ClassicClient;

public class TextureManager {

	private HashMap<String, Integer> textures = new HashMap<String, Integer>();
	private IntBuffer buffer = BufferUtils.createIntBuffer(1);
	private MipmapMode mipmaps = MipmapMode.NONE;
	
	public void pickMipmaps() {
		if (GLContext.getCapabilities().OpenGL30) {
			this.mipmaps = MipmapMode.GL30;
		} else if (GLContext.getCapabilities().GL_EXT_framebuffer_object) {
			this.mipmaps = MipmapMode.FRAMEBUFFER;
		} else if (GLContext.getCapabilities().OpenGL14) {
			this.mipmaps = MipmapMode.GL14;
			glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_TRUE);
		}
	}

	public int load(String file) {
		return this.load(file, true);
	}
	
	public int load(Texture tex) {
		return this.load(tex.getTexture(), tex.isInJar());
	}

	public int load(String file, boolean jar) {
		if(this.getTextureId(file) != -1) {
			return this.getTextureId(file);
		} else {
			try {
				BufferedImage img = null;
				if(!jar) {
					img = ImageIO.read(new FileInputStream(file));
				} else if(OpenClassic.getClient().getConfig().getString("settings.resourcepack", "none").equals("none")) {
					img = ImageIO.read(ClassicClient.class.getResourceAsStream(file));
				} else {
					ZipFile zip = new ZipFile(new File(OpenClassic.getClient().getDirectory(), "resourcepacks/" + OpenClassic.getClient().getConfig().getString("settings.resourcepack", "none")));
					if(zip.getEntry(file.startsWith("/") ? file.substring(1, file.length()) : file) != null) {
						img = ImageIO.read(zip.getInputStream(zip.getEntry(file.startsWith("/") ? file.substring(1, file.length()) : file)));
					} else {
						img = ImageIO.read(ClassicClient.class.getResourceAsStream(file));
					}
					
					zip.close();
				}
				
				int id = this.load(img);
				this.textures.put(file, id);
				return id;
			} catch(IOException e) {
				e.printStackTrace();
				return 0;
			}
		}
	}

	public final int load(BufferedImage img) {
		this.buffer.clear();
		glGenTextures(this.buffer);
		int id = this.buffer.get(0);
		this.load(img, id);
		return id;
	}

	public void load(BufferedImage img, int id) {
		if(img == null) return;
		glBindTexture(GL_TEXTURE_2D, id);
		if(OpenClassic.getClient().getConfig().getBoolean("options.smoothing") && this.mipmaps != MipmapMode.NONE) {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 2);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
		} else {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		}
		
		int[] pixels = new int[img.getWidth() * img.getHeight()];
		img.getRGB(0, 0, img.getWidth(), img.getHeight(), pixels, 0, img.getWidth());

		ByteBuffer buffer = BufferUtils.createByteBuffer(img.getWidth() * img.getHeight() * 4);
		for(int y = 0; y < img.getHeight(); y++){
			for(int x = 0; x < img.getWidth(); x++){
				int pixel = pixels[y * img.getWidth() + x];
				int red = (pixel >> 16) & 0xFF;
				int blue = pixel & 0xFF;
				int green = (pixel >> 8) & 0xFF;
				int alpha = (pixel >> 24) & 0xFF;

				buffer.put((byte) red);
				buffer.put((byte) green);
				buffer.put((byte) blue);
				buffer.put((byte) alpha);
			}
		}

		buffer.flip();
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, img.getWidth(), img.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		
		if(OpenClassic.getClient().getConfig().getBoolean("options.smoothing") && this.mipmaps != MipmapMode.NONE) {
			switch(this.mipmaps) {
				case GL30:
					glGenerateMipmap(GL_TEXTURE_2D);
					break;
				case FRAMEBUFFER:
					EXTFramebufferObject.glGenerateMipmapEXT(GL_TEXTURE_2D);
					break;
			}
		}
	}

	public void bind(String file) {
		glBindTexture(this.load(file), GL_TEXTURE_2D);
	}
	
	public void bind(String file, boolean jar) {
		glBindTexture(this.load(file, jar), GL_TEXTURE_2D);
	}
	
	public void bind(BufferedImage img) {
		glBindTexture(this.load(img), GL_TEXTURE_2D);
	}
	
	public void clear() {
		this.textures.clear();
	}
	
	public int getTextureId(String file) {
		if(!this.textures.containsKey(file)) return -1;
		return this.textures.get(file);
	}
	
}
