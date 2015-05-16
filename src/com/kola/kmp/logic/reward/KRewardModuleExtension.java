package com.kola.kmp.logic.reward;

import java.util.HashMap;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.GameModuleExtension;
import com.kola.kmp.logic.reward.KRewardSonModuleAbs.KRewardSonModuleType;
import com.kola.kmp.logic.reward.activatecode.KActivateCodeSonModule;
import com.kola.kmp.logic.reward.daylucky.KDayluckySonModule;
import com.kola.kmp.logic.reward.exciting.KExcitingSonModule;
import com.kola.kmp.logic.reward.garden.KGardenSonModule;
import com.kola.kmp.logic.reward.login.KLoginSonModule;
import com.kola.kmp.logic.reward.online.KOnlineSonModule;
import com.kola.kmp.logic.reward.redress.KRedressSonModule;
import com.kola.kmp.logic.reward.vitality.KVitalitySonModule;
import com.kola.kmp.logic.support.KSupportFactory;

public class KRewardModuleExtension implements GameModuleExtension<KRewardModule> {

	public static final Logger _LOGGER = KGameLogger.getLogger(KRewardModuleExtension.class);

	private KRewardModule module;

	// 所有子模块
	static Map<KRewardSonModuleType, KRewardSonModuleAbs> rewardSonImplMap = new HashMap<KRewardSonModuleType, KRewardSonModuleAbs>();
	static {
		// 注册子模块
		KRewardModuleExtension.rewardSonImplMap.put(KActivateCodeSonModule.instance.type, KActivateCodeSonModule.instance);
		KRewardModuleExtension.rewardSonImplMap.put(KDayluckySonModule.instance.type, KDayluckySonModule.instance);
		KRewardModuleExtension.rewardSonImplMap.put(KExcitingSonModule.instance.type, KExcitingSonModule.instance);
		KRewardModuleExtension.rewardSonImplMap.put(KGardenSonModule.instance.type, KGardenSonModule.instance);
		KRewardModuleExtension.rewardSonImplMap.put(KLoginSonModule.instance.type, KLoginSonModule.instance);
		KRewardModuleExtension.rewardSonImplMap.put(KOnlineSonModule.instance.type, KOnlineSonModule.instance);
		KRewardModuleExtension.rewardSonImplMap.put(KRedressSonModule.instance.type, KRedressSonModule.instance);
		KRewardModuleExtension.rewardSonImplMap.put(KVitalitySonModule.instance.type, KVitalitySonModule.instance);
	}

	public KRewardModule getModule() {
		return module;
	}

	public void init(KRewardModule module, String cfgPath) throws Exception {
		this.module = module;
		//
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到初始化通知，开始加载静态数据------");
		{
			Document doc = XmlUtil.openXml(cfgPath);
			Element root = doc.getRootElement();
			//
			Element logicE = root.getChild("logicConfig");
			goToLoadConfig(logicE);
			//
			Element excelE = root.getChild("excelConfig");
			KRewardDataManager.goToLoadData(excelE);
		}

		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成加载静态数据------");
	}

	/**
	 * <pre>
	 * 开始加载文件
	 * @param configPath
	 * @author CamusHuang
	 * @creation 2012-11-9 下午7:38:27
	 * </pre>
	 */
	private static void goToLoadConfig(Element configE) throws Exception {

		// 加载数据
		{
			Element tempE = configE.getChild("sonModule");
			{
				// 通知子模块
				for (KRewardSonModuleAbs impl : rewardSonImplMap.values()) {
					impl.loadConfig(tempE);
				}
			}
		}
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到游戏启动完成通知，开始验证静态的数据------");
		// KGameBoundaryDataManager.mBoundaryDataManager.serverStartCompleted();

		// 通知子模块
		for (KRewardSonModuleAbs impl : rewardSonImplMap.values()) {
			impl.onGameWorldInitComplete();
		}
		
		KSupportFactory.getVIPModuleSupport().addVipUpLvListener(new KRewardVIPListener());

		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成静态数据验证------");
	}

	public void notifyCacheLoadComplete() throws KGameServerException {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到缓存初始化完成通知，开始启动时效任务------");

		KRewardDataManager.notifyCacheLoadComplete();

		// 通知子模块
		for (KRewardSonModuleAbs impl : rewardSonImplMap.values()) {
			impl.notifyCacheLoadComplete();
		}
		
		for (KRewardSonModuleAbs impl : rewardSonImplMap.values()) {
			impl.afterNotifyCacheLoadComplete();
		}

		//
		KRewardTaskManager.notifyCacheLoadComplete();

		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成启动时效任务------");
	}

	@Override
	public boolean messageReceived(KGameMessageEvent msgEvent) throws KGameServerException {
		return false;
	}

	@Override
	public void exceptionCaught(KGameServerException ex) {
		_LOGGER.error(ex.getMessage(), ex);
	}

	@Override
	public void serverShutdown() throws KGameServerException {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到关服通知------");
		// CEND 服务器关闭,要做些什么操作??

		// 通知子模块
		for (KRewardSonModuleAbs impl : rewardSonImplMap.values()) {
			impl.serverShutdown();
		}

		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成关服------");
	}
}
