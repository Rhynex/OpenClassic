package ch.spacebase.openclassic.client.level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.model.BoundingBox;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.level.LevelInfo;
import ch.spacebase.openclassic.api.network.msg.Message;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.client.ClassicClient;
import ch.spacebase.openclassic.client.math.Tracer;
import ch.spacebase.openclassic.client.mode.Multiplayer;
import ch.spacebase.openclassic.client.particle.ParticleManager;
import ch.spacebase.openclassic.client.player.OtherPlayer;
import ch.spacebase.openclassic.client.render.LevelRenderer;
import ch.spacebase.openclassic.game.level.ClassicLevel;

public class ClientLevel extends ClassicLevel implements Level {
	
	private LevelRenderer renderer;
	private Tracer tracer;
	private ParticleManager particles = new ParticleManager();
	
	public ClientLevel(String name) {
		this(name, false);
	}
	
	public ClientLevel(String name, boolean remote) {
		super(name, remote);
		this.renderer = new LevelRenderer(this);
		this.executor.shutdown();
		this.tracer = new Tracer(this);
	}

	public ClientLevel(LevelInfo info) {
		super(info);
		this.executor.shutdown();
		this.tracer = new Tracer(this);
		this.renderer = new LevelRenderer(this);
	}

	@Override
	public boolean isLit(int x, int y, int z) {
		return true; // TODO: rework lighting
	}
	
	public int getHighestOpaque(int x, int z) {
		return this.getColumnFromBlock(x, z).getHighestOpaque(x, z);
	}
	
	@Override
	public void update() {
		super.update();
		this.particles.update();
		for(Player player : this.getPlayers()) {
			if(player instanceof OtherPlayer) ((OtherPlayer) player).update();
		}
		
		if(!(((ClassicClient) OpenClassic.getClient()).getMode() instanceof Multiplayer)) {
			this.physics();
		}
	}
	
	@Override
	public void render(float delta) {
		this.renderer.render();
		this.particles.render(delta);
		for(Player player : this.getPlayers()) {
			if(player instanceof OtherPlayer) ((OtherPlayer) player).render(delta);
		}
	}
	
	@Override
	public boolean setBlockIdAt(int x, int y, int z, byte type, boolean physics) {
		if(super.setBlockIdAt(x, y, z, type, physics)) {
			this.renderer.queue(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
			return true;
		}
		
		return false;
	}

	@Override
	public void addPlayer(Player player) {
		if(player instanceof OtherPlayer) {
			super.addPlayer(player);
		}
	}

	public boolean contains(BoundingBox bb, BlockType... types) {
		List<BlockType> list = Arrays.asList(types);
		int x1 = bb.getX1() < 0 ? 0 : (int) bb.getX1();
		int y1 = bb.getY1() < 0 ? 0 : (int) bb.getY1();
		int z1 = bb.getZ1() < 0 ? 0 : (int) bb.getZ1();

		for(int x = x1; x < bb.getX2(); x++) {
			for(int y = y1; y < bb.getY2(); y++) {
				for(int z = z1; z < bb.getZ2(); z++) {
					BlockType type = this.getBlockTypeAt(x, y, z);
					if(list.contains(type)) {
						return true;
					}
				}
			}
		}

		return false;
	}
	
	public List<BoundingBox> getBoxes(BoundingBox bb) {
		List<BoundingBox> result = new ArrayList<BoundingBox>();
		int x1 = (int) bb.getX1() - (bb.getX1() < 0 ? 1 : 0);
		int y1 = (int) bb.getY1() - (bb.getY1() < 0 ? 1 : 0);
		int z1 = (int) bb.getZ1() - (bb.getZ1() < 0 ? 1 : 0);

		for(int x = x1; x < (int) bb.getX2() + 1; x++) {
			for(int y = y1; y < (int) bb.getY2() + 1; y++) {
				for(int z = z1; z < (int) bb.getZ2() + 1; z++) {
					BlockType type = this.getBlockTypeAt(x, y, z);
					if(type != null && type != VanillaBlock.AIR) {
						BoundingBox blockbb = type.getModel().getCollisionBox(x, y, z);
						if (blockbb != null && bb.intersectsInner(blockbb)) {
							result.add(blockbb);
						}
					}
				}
			}
		}

		return result;
	}

	@Override
	public void sendToAll(Message message) {
	}

	@Override
	public void sendToAllExcept(Player skip, Message message) {
	}
	
	public Tracer getTracer() {
		return this.tracer;
	}
	
	public ParticleManager getParticleManager() {
		return this.particles;
	}
	
	public LevelRenderer getRenderer() {
		return this.renderer;
	}
	
}
