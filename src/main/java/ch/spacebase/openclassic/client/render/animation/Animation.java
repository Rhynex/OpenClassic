package ch.spacebase.openclassic.client.render.animation;

public abstract class Animation {

	public static final Animation WATER = new WaterAnimation();
	public static final Animation LAVA = new LavaAnimation();
	
	protected byte pixelData[] = new byte[1024];
	protected float[] red = new float[256];
	protected float[] green = new float[256];
	protected float[] blue = new float[256];
	protected float[] alpha = new float[256];
	
	protected int texture;
	
	public Animation(int textureId) {
		this.texture = textureId;
	}
	
	public byte[] getPixelData() {
		return this.pixelData;
	}
	
	public int getTextureId() {
		return this.texture;
	}
	
	public abstract void refresh();
	
}
