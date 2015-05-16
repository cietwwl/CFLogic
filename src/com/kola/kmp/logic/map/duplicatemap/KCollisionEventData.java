package com.kola.kmp.logic.map.duplicatemap;

public class KCollisionEventData implements CollisionEventObjectData {
	private int mapInstanceId;
	private Object attachment;
	private float x, y;

	public KCollisionEventData() {

	}

	public KCollisionEventData(int mapInstanceId, float x, float y) {
		super();
		this.mapInstanceId = mapInstanceId;
		this.x = x;
		this.y = y;
	}

	@Override
	public int getMapInstanceId() {
		return this.mapInstanceId;
	}

	@Override
	public Object getAttachment() {
		return this.attachment;
	}

	@Override
	public void setAttachment(Object obj) {
		this.attachment = obj;
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

	public void setMapInstanceId(int mapInstanceId) {
		this.mapInstanceId = mapInstanceId;
	}

	@Override
	public int compareTo(CollisionEventObjectData o) {
		if (this.x < o.getX()) {
			return 1;
		} else if (this.x > o.getX()) {
			return -1;
		} else {
			if (this.y < o.getY()) {
				return 1;
			} else if (this.y > o.getY()) {
				return -1;
			} else {
				return 0;
			}
		}
	}

}
