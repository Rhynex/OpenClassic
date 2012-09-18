package ch.spacebase.openclassic.client.math;

import ch.spacebase.openclassic.api.Position;

public class Intersection {
	
    private boolean hit = false;
    private Position diff = new Position(null, 0, 0, 0);
    private Position pos = new Position(null, 0, 0, 0);
    private double length;
    
    public boolean isHit() {
    	return this.hit;
    }
    
    public void setHit(boolean hit) {
    	this.hit = hit;
    }
    
    public Position getDiff() {
    	return this.diff;
    }
    
    public Position getPosition() {
    	return this.pos;
    }
    
    public double getLength() {
    	return this.length;
    }
    
    public void setLength(double length) {
    	this.length = length;
    }

    @Override
    public String toString() {
        return "IntersectionResult{hit=" + hit + ",length=" + length + ",diff=" + diff + ",pos=" + pos + "}";
    }
    
}
