package com.kola.kmp.logic.map.duplicatemap;

import com.kola.kmp.logic.role.KRole;

public interface CollisionEventListener {
	/**
	 * 通知角色与地图的某个碰撞检测实体发生碰撞事件
	 * @param role
	 * @param data
	 */
	public void notifyPlayerRoleCollisionEvent(KRole role,CollisionEventObjectData data);
	
	/**
	 * 通知角色与地图的另外一个角色发生碰撞事件
	 * @param role
	 * @param otherRole
	 */
	public void notifyPlayerRoleCollisionOtherRole(KRole role,KRole otherRole);

}
