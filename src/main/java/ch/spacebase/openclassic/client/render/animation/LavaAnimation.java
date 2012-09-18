package ch.spacebase.openclassic.client.render.animation;

import ch.spacebase.openclassic.api.math.MathHelper;

public class LavaAnimation extends Animation {

	public LavaAnimation() {
		super(30);
	}

	@Override
	public void refresh() {
		for(int x = 0; x < 16; x++) {
			for(int y = 0; y < 16; y++) {
				float red = 0;
				int sinX = (int) (MathHelper.sin(x * (float) Math.PI * 2 / 16f) * 1.2f);
				int sinY = (int) (MathHelper.sin(y * (float) Math.PI * 2 / 16f) * 1.2f);

				for(int xx = x - 1; xx <= x + 1; xx++) {
					for(int yy = y - 1; yy <= y + 1; yy++) {
						red += this.red[(xx + sinY & 15) + ((yy + sinX & 15) << 4)];
					}
				}

				this.green[x + (y << 4)] = red / 10.0F + (this.blue[(x & 15) + ((y & 15) << 4)] + this.blue[(x + 1 & 15) + ((y & 15) << 4)] + this.blue[(x + 1 & 15) + ((y + 1 & 15) << 4)] + this.blue[(x & 15) + ((y + 1 & 15) << 4)]) / 4 * 0.8f;
				this.blue[x + (y << 4)] += this.alpha[x + (y << 4)] * 0.01f;
				if(this.blue[x + (y << 4)] < 0) {
					this.blue[x + (y << 4)] = 0;
				}

				this.alpha[x + (y << 4)] -= 0.06f;
				if(Math.random() < 0.005) {
					this.alpha[x + (y << 4)] = 1.5f;
				}
			}
		}

		float[] oldgreen = this.green;
		this.green = this.red;
		this.red = oldgreen;

		for(int pix = 0; pix < 256; pix++) {
			float red = this.red[pix] * 2;
			if(red > 1) {
				red = 1;
			}

			if(red < 0) {
				red = 0;
			}

			int r = (int) (red * 100 + 155);
			int g = (int) (red * red * 255);
			int b = (int) (red * red * red * red * 128);

			this.pixelData[pix << 2] = (byte) r;
			this.pixelData[(pix << 2) + 1] = (byte) g;
			this.pixelData[(pix << 2) + 2] = (byte) b;
			this.pixelData[(pix << 2) + 3] = -1;
		}
	}

}
