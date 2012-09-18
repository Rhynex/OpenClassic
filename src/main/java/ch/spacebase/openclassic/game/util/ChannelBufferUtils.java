package ch.spacebase.openclassic.game.util;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.jboss.netty.buffer.ChannelBuffer;

public class ChannelBufferUtils {
	
	public static void writeString(ChannelBuffer buffer, String string) {
		try {
			byte data[] = string.getBytes("UTF-8");
			
			if (data.length > 64) {
				data = Arrays.copyOfRange(data, 0, 64);
			}
			
			if(data.length < 64) {
				byte[] newData = new byte[64];
				Arrays.fill(newData, (byte) 32);
				System.arraycopy(data, 0, newData, 0, data.length);
				
				data = newData;
			}
	
			for (int i = 0; i < 64; i++) {
				buffer.writeByte(data[i]);
			}
		} catch(UnsupportedEncodingException e) {
		}
	}

	public static String readString(ChannelBuffer buffer) {
		byte[] data = new byte[64];

		for (int i = 0; i < 64; i++) {
			data[i] = buffer.readByte();
		}

		try {
			return new String(data, "UTF-8").trim();
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}

	private ChannelBufferUtils() {
	}
	
}
