package ch.spacebase.openclassic.game.level.column;

import ch.spacebase.openclassic.api.util.storage.DoubleIntHashMap;

public class ColumnUnloadThread extends Thread {

	private boolean running = true;
	private DoubleIntHashMap<ClassicColumn> queue = new DoubleIntHashMap<ClassicColumn>();
	
	public boolean isUnloading(int x, int z) {
		return this.queue.containsKey(x, z);
	}
	
	public ClassicColumn pull(int x, int z) {
		return this.queue.remove(x, z);
	}
	
	public void unload(ClassicColumn col) {
		this.queue.put(col.getX(), col.getZ(), col);
	}
	
	public void dispose() {
		this.running = false;
		this.queue.clear();
	}
	
	public void run() {
		while(this.running) {
			if(this.queue.size() > 0) {
				ClassicColumn column = this.queue.remove(this.queue.keySet().toArray(new Long[this.queue.size()])[0]);
				column.save();
			}
		}
	}
	
}
