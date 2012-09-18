package ch.spacebase.openclassic.client.render.animation;

public class WaterAnimation extends Animation {

	public WaterAnimation() {
		super(14);
	}

	@Override
	public void refresh() {
		for(int x = 0; x < 16; x++) {
			for(int y = 0; y < 16; y++) {
				float green = 0;

				for(int xx = x - 1; xx <= x + 1; xx++) {
					green += this.red[(xx & 15) + ((y & 15) << 4)];
				}

				this.green[x + (y << 4)] = green / 3.3f + this.blue[x + (y << 4)] * 0.8f;
				this.blue[x + (y << 4)] += this.alpha[x + (y << 4)] * 0.05f;
				if (this.blue[x + (y << 4)] < 0) {
					this.blue[x + (y << 4)] = 0;
				}

				this.alpha[x + (y << 4)] -= 0.1f;
				if (Math.random() < 0.05) {
					this.alpha[x + (y << 4)] = 0.5f;
				}
			}
		}

		float[] oldgreen = this.green;
		this.green = this.red;
		this.red = oldgreen;

		for(int pix = 0; pix < 256; pix++) {
			float red = this.red[pix];
			if(red > 1) {
				red = 1;
			}

			if (red < 0) {
				red = 0;
			}

			int r = (int) (32 + red * red * 32);
			int g = (int) (50 + red * red * 64);
			int a = (int) (146 + red * red * 50);

			this.pixelData[pix << 2] = (byte) r;
			this.pixelData[(pix << 2) + 1] = (byte) g;
			this.pixelData[(pix << 2) + 2] = (byte) 255;
			this.pixelData[(pix << 2) + 3] = (byte) a;
		}
	}

}
