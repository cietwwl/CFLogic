package com.kola.kmp.logic.item.listener;

import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;

public interface KItemEventListener {

	/**
	 * <pre>
	 * 通知角色背包中某个道具类型数量发生变化
	 * 
	 * @param roleId 角色
	 * @param itemTemplate 道具模版
	 * @param nowCount 目前剩余数量
	 * </pre>
	 */
	public void notifyItemCountChangeInBag(long roleId, KItemTempAbs itemTemplate, long nowCount);

}
