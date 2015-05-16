package com.kola.kmp.logic.map;


/**
 * 地图坐标
 * @author zhaizl
 *
 */
public class KCoordinate {
	
	private float x;
	private float y;
	
	public KCoordinate(float x, float y) {
		super();
		this.x = x;
		this.y = y;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}
	
	@Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final KCoordinate other = (KCoordinate) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (int)this.x;
        hash = 19 * hash + (int)this.y;
        return hash;
    }
	

}
