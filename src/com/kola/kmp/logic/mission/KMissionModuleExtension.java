package com.kola.kmp.logic.mission;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.kola.kgame.cache.mission.MissionCompleteRecordSet;
import com.kola.kgame.cache.mission.MissionModule;
import com.kola.kgame.cache.mission.MissionSet;
import com.kola.kgame.cache.mission.impl.KAMissionModuleExtension;
import com.kola.kgame.cache.mission.impl.KMissionModule;

public class KMissionModuleExtension extends KAMissionModuleExtension {

	private static KMissionManager manager;

	@Override
	public void init(MissionModule module, String cfgPath) throws Exception {
		super.init(module, cfgPath);
		//
		_LOGGER.warn("----- " + getModule().getModuleName()
				+ " extension 收到初始化通知，开始加载静态数据------");

		manager = new KMissionManager();
		manager.init(cfgPath);
		_LOGGER.warn("----- " + getModule().getModuleName()
				+ " extension 完成加载静态数据------");
	}

	@Override
	public void onGameWorldInitComplete() {
		_LOGGER.warn("----- " + getModule().getModuleName()
				+ " extension 收到游戏启动完成通知，开始验证静态的数据------");
		// CTODO

		_LOGGER.warn("----- " + getModule().getModuleName()
				+ " extension 完成静态数据验证------");
	}

	public void notifyCacheLoadComplete() throws KGameServerException {
		_LOGGER.warn("----- " + getModule().getModuleName()
				+ " extension 收到缓存初始化完成通知，开始启动时效任务------");
		try {
			manager.onGameWorldInitComplete();
		} catch (Exception e) {
//			_LOGGER.error("----- " + getModule().getModuleName()
//					+ " extension onGameWorldInitComplete()错误！", e);
			throw new KGameServerException("----- " + getModule().getModuleName()
					+ " extension onGameWorldInitComplete()错误！", e);
		}

		_LOGGER.warn("----- " + getModule().getModuleName()
				+ " extension 完成启动时效任务------");
	}

	@Override
	public boolean messageReceived(KGameMessageEvent msgEvent)
			throws KGameServerException {
		// CTODO Auto-generated method stub
		return false;
	}

	@Override
	public void exceptionCaught(KGameServerException ex) {
		_LOGGER.error(ex.getMessage(), ex);
	}

	@Override
	public void serverShutdown() throws KGameServerException {
		_LOGGER.warn("----- " + getModule().getModuleName()
				+ " extension 收到关服通知------");
		// CEND 服务器关闭,要做些什么操作??
		_LOGGER.warn("----- " + getModule().getModuleName()
				+ " extension 完成关服------");
	}

	@Override
	public MissionSet newMissionSet(long roleId, boolean isFirstNew) {
		return new KMissionSet(roleId, isFirstNew);
	}

	public static KMissionSet getMissionSet(long roleId) {
		return (KMissionSet) KMissionModule.getInstance().getMissionSet(roleId);
	}

	@Override
	public MissionCompleteRecordSet newMissionCompleteRecordSet(long roleId,
			boolean isFirstNew) {
		return new KMissionCompleteRecordSet(roleId, isFirstNew);
	}

	public static KMissionCompleteRecordSet getMissionCompleteRecordSet(
			long roleId) {
		return (KMissionCompleteRecordSet) KMissionModule.getInstance()
				.getMissionCompleteRecordSet(roleId);
	}

	public static KMissionManager getManager() {
		return manager;
	}

}
