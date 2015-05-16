package com.kola.kmp.logic.relationship;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.relationship.RelationShipModule;
import com.kola.kgame.cache.relationship.impl.KARelationShipModuleExtension;
import com.kola.kgame.cache.relationship.impl.KRelationShipModule;
import com.kola.kmp.logic.role.KRoleVipLvListener;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.vip.KVIPDataLoader;

public class KRelationShipModuleExtension extends KARelationShipModuleExtension {

	public void init(RelationShipModule module, String cfgPath) throws Exception {
		super.init(module, cfgPath);
		//
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到初始化通知，开始加载静态数据------");
		{
			Document doc = XmlUtil.openXml(cfgPath);
			Element root = doc.getRootElement();
			//
			Element logicE = root.getChild("logicConfig");
			KRelationShipConfig.init(logicE);
			//
			Element excelE = root.getChild("excelConfig");
			KRelationShipDataLoader.goToLoadData(excelE);
		}
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成加载静态数据------");
	}

	@Override
	public void onGameWorldInitComplete() {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到游戏启动完成通知，开始验证静态的数据------");
		// CTODO
		// KGameBoundaryDataManager.mBoundaryDataManager.serverStartCompleted();
		
		KSupportFactory.getVIPModuleSupport().addVipUpLvListener(new KRelationVIPListener());
		
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成静态数据验证------");
	}

	public void notifyCacheLoadComplete() throws KGameServerException{
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到缓存初始化完成通知，开始启动时效任务------");
		
		KRelationShipDataManager.notifyCacheLoadComplete();
		
		KRelationShipTaskManager.notifyCacheLoadComplete();
		
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成启动时效任务------");
	}

	@Override
	public boolean messageReceived(KGameMessageEvent msgEvent) throws KGameServerException {
		// CTODO Auto-generated method stub
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
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成关服------");
	}

	public KRelationShipSet newRelationShipSet(long roleId, boolean isFirstNew) {
		return new KRelationShipSet(roleId, isFirstNew);
	}

	public static KRelationShipSet getRelationShipSet(long roleId) {
		return (KRelationShipSet) KRelationShipModule.instance.getRelationShipSet(roleId);
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 演示代码：新建关系
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-13 下午3:15:31
	 * </pre>
	 */
	public boolean addRelationShip(int type, long hostRoleId, long guestRoleId) {
		KRelationShipSet owner = getRelationShipSet(hostRoleId);
		KRelationShip ship = new KRelationShip(owner, type, guestRoleId);

		return owner.addRelationShip(ship);
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 演示代码：删除道具
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-13 下午3:15:31
	 * </pre>
	 */
	public boolean deleteRelationShip(long roleId, int type, long guestRoleId) {
		KRelationShipSet owner = getRelationShipSet(roleId);
		KRelationShip ship = owner.notifyElementDelete(type, guestRoleId);
		if (ship != null) {
			return true;
		}
		return false;
	}
}
