package com.kola.kmp.logic.item.listener;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.ItemTaskManager;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.mission.KMissionItemEventListener;

public class KItemListenerManager {

	public static final Logger _LOGGER = KGameLogger.getLogger(KItemListenerManager.class);
	/**
	 * <pre>
	 * 所有道具事件监听者
	 * unmodifiableList
	 * </pre>
	 */
	private static List<KItemEventListener> itemListenerList = new ArrayList<KItemEventListener>();

	public static void serverStartCompleted() throws KGameServerException {
		// CTODO 在下面硬代码添加道具事件监听器
		// itemListenerList.add(new XXXX());
		
		//任务系统道具监听器
		itemListenerList.add(new KMissionItemEventListener());
		
	}

	/**
	 * <pre>
	 * 通知角色背包中某个道具类型数量发生变化
	 * 
	 * @param roleId 角色
	 * @param itemTemplate 道具模版
	 * @param nowCount 目前剩余数量
	 * </pre>
	 */
	public static void notifyBagItemChangeCount(long roleId, KItemTempAbs itemTemplate, long nowCount, boolean isAdd) {
		// 通知监听者
		for (KItemEventListener listener : itemListenerList) {
			try {
				listener.notifyItemCountChangeInBag(roleId, itemTemplate, nowCount);
			} catch (Exception ex) {
				_LOGGER.error("通知其他模块道具数量变更时出现异常！", ex);
			}
		}
		// 通知任务
		if (isAdd) {
			if (KItemLogic.isAutoOpenBox(itemTemplate)) {
				ItemTaskManager.AutoOpenBoxTask.instance.addData(roleId);
			}
		}
	}
}
