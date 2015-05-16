package com.kola.kmp.logic.map;

import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.koala.game.KGameMessage;
import com.koala.game.exception.KGameServerException;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kmp.logic.map.KGameMapEntity.KGameMapEntityState;
import com.kola.kmp.logic.other.KMapEntityTypeEnum;
import com.kola.kmp.logic.role.KRole;

/**
 * <pre>
 * 广播地图角色实体状态时效任务。该时效任务到达执行时间，会遍历游戏中所有地图中
 * 类型为角色（参考{@link KGameMapEntity#ENTITY_TYPE_PLAYERROLE}}）的地图实体， 向他们广播
 * 他们所在地图中周围其他地图角色的实时状态（如进入地图、更新坐标、离开地图等）,请参考：
 * {@link KGameMapEntityState#ENTITY_STATE_JOIN_MAP}||{@link KGameMapEntityState#ENTITY_STATE_UPDATE_XY}||
 * {@link KGameMapEntityState#ENTITY_STATE_LEAVE_MAP}。
 * 
 * 注：地图角色实体会持有自己的周围角色私有列表{@link PlayerRolePrivateEntityList}，以及
 *     一个周围角色状态改变的消息封装器{@link OtherEntityStateChangedMsgPacker}。当时效任务到达时，会检测
 *     各个角色实体的私有表以及消息封装器，看看有无广播消息要发送
 * @author zhaizl
 * </pre>
 */
public class BroadcastMapEntityStateTimerTask implements KGameTimerTask {

	@Override
	public String getName() {
		return BroadcastMapEntityStateTimerTask.class.getName();
	}

	@Override
	public Object onTimeSignal(KGameTimeSignal timeSignal)
			throws KGameServerException {
		long currentTimeMillis = System.currentTimeMillis();

		// 从KGameMapManager中取得所有地图
		Map<Integer, KGameNormalMap> allMaps = KMapModule.getGameMapManager()
				.getGameMaps();

		if (allMaps != null) {
			// 遍历所有地图
			for (KGameNormalMap map : allMaps.values()) {
				// 遍历该地图中所有的角色
				for (KGameMapEntity roleEntity : map
						.getEntities(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE)) {
					if (roleEntity != null) {
						KRole role = (KRole) roleEntity
								.getSourceObject();
						if (role != null) {
							if (!role.isFighting()
									&& role.isOnline()) {
								// 获取打包数据消息,并发送
								KGameMessage msg = roleEntity
										.getOtherEntityStateChangeDataMessage(currentTimeMillis,OtherEntityStateChangedMsgPacker.PACK_MSG_TYPE_GAME_MAP);
								if (msg != null) {
									role.sendMsg(msg);
								}
							}

						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public void done(KGameTimeSignal timeSignal) {
		timeSignal.getTimer().newTimeSignal(this,
				KGameMapManager.broadcastEntityStateChangeMsgPackTimeSeconds,
				TimeUnit.SECONDS);
	}

	@Override
	public void rejected(RejectedExecutionException e) {
		return;
	}

}
