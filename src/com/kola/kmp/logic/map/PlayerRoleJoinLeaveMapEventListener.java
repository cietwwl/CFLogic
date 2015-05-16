package com.kola.kmp.logic.map;

import com.kola.kmp.logic.role.KRole;


public interface PlayerRoleJoinLeaveMapEventListener {
	
	/**
     * 进入地图
     * @param role 角色
     * @param targetMap 地图
     */
    void notifyPlayerRoleJoinedMap(KRole role, KMap targetMap);

    /**
     * 离开地图
     * @param role 角色
     * @param currentMap 当前地图
     */
    void notifyPlayerRoleLeavedMap(KRole role, KMap currentMap);
    
    /**
     * 
     * 重新进入地图
     * 
     * @param role
     */
    void notifyPlayerRoleRejoinedMap(KRole role);

}
