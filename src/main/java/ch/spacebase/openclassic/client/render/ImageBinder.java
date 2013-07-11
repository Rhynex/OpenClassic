package ch.spacebase.openclassic.client.render;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;

public class ImageBinder {

	private Map<Integer, BufferedImage> imgs = new HashMap<Integer, BufferedImage>();
	private IntBuffer buffer = BufferUtils.createIntBuffer(1);

	public final int load(BufferedImage img) {
		for(int key : this.imgs.keySet()) {
			if(this.imgs.get(key) != null && this.imgs.get(key).equals(img)) {
				return key;
			}
		}
		
		this.buffer.clear();
		glGenTextures(this.buffer);
		int id = this.buffer.get(0);
		this.load(img, id);
		return id;
	}

	public void load(BufferedImage img, int id) {
		if(!this.imgs.containsKey(id)) this.imgs.put(id, img);
		if(img == null) return;
		glBindTexture(GL_TEXTURE_2D, id);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
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
	}
	
}
