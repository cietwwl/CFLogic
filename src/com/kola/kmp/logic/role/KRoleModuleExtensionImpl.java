package com.kola.kmp.logic.role;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.role.Role;
import com.kola.kgame.cache.role.RoleBaseInfo;
import com.kola.kgame.cache.role.RoleExtCA;
import com.kola.kgame.cache.role.RoleExtCASet;
import com.kola.kgame.cache.role.RoleModule;
import com.kola.kgame.cache.role.RoleModuleExtension;
import com.kola.kmp.logic.combat.impl.KCombatModule;
import com.kola.kmp.logic.other.KRoleExtTypeEnum;
import com.kola.kmp.logic.other.KTableInfo;
import com.kola.kmp.logic.role.message.KRoleServerMsgPusher;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KRoleModuleExtensionImpl implements RoleModuleExtension {

	private static final Logger _LOGGER = KGameLogger.getLogger(KRoleModuleExtensionImpl.class);
	private RoleModule _module;
	private final Map<Integer, IRoleExtCACreator> _extCACreators = new HashMap<Integer, IRoleExtCACreator>();
	private boolean _enableWaitWhenReceiveLeaveGameEvent;
	private int _waitMillisWhenReceiveLeaveGameEvent;
	
	private void loadExtCreator(String path) throws Exception {
		Document doc = XmlUtil.openXml(path);
		@SuppressWarnings("unchecked")
		List<Element> list = doc.getRootElement().getChildren();
		Element current;
		int type;
		for(int i = 0; i < list.size(); i++) {
			current = list.get(i);
			type = Integer.parseInt(current.getAttributeValue("type"));
			KRoleExtTypeEnum typeEnum = KRoleExtTypeEnum.getTypeEnum(type);
			if (typeEnum != null) {
				_extCACreators.put(type, (IRoleExtCACreator) Class.forName(current.getAttributeValue("creatorClassPath")).newInstance());
			} else {
				throw new RuntimeException("找不到扩展属性类型：{}" + type);
			}
		}
	}
	
	@Override
	public void init(RoleModule module, String cfgPath) throws Exception {
		_module = module;
		_LOGGER.warn(" ----- {} extension 初始化开始------", module.getModuleName());
		Document doc = XmlUtil.openXml(cfgPath);
		Element root = doc.getRootElement();
		this._enableWaitWhenReceiveLeaveGameEvent = Boolean.parseBoolean(root.getChildTextTrim("enableWaitWhenReceiveLeaveGameEvent"));
		this._waitMillisWhenReceiveLeaveGameEvent = Integer.parseInt(root.getChildTextTrim("waitMillisWhenReceiveLeaveGameEvent"));
		if (_waitMillisWhenReceiveLeaveGameEvent > 1000) {
			throw new RuntimeException("_waitMillisWhenReceiveLeaveGameEvent时间过大：" + _waitMillisWhenReceiveLeaveGameEvent);
		}
		@SuppressWarnings("unchecked")
		List<Element> tableConfigs = root.getChild("tableConfig").getChildren();
		Map<Byte, KTableInfo> tableMap = new HashMap<Byte, KTableInfo>();
		for (int i = 0; i < tableConfigs.size(); i++) {
			Element temp = tableConfigs.get(i);
			byte type = Byte.parseByte(temp.getAttributeValue("type"));
			tableMap.put(type, new KTableInfo(type, temp.getAttributeValue("name"), Integer.parseInt(temp.getAttributeValue("headerIndex"))));
		}
		KRoleModuleManager.loadData(root.getChildTextTrim("roleDataPath"), tableMap);
		KRoleModuleManager.loadListener(root.getChildTextTrim("eventListenerPath"));
		KRoleModuleManager.loadAttributeProvider(root.getChildTextTrim("roleAttrProviderPath"));
		KRandomNameManager.loadNames(root.getChild("randomNameConfig"));
		KRoleModuleConfig.init(root.getChildTextTrim("commonConfigPath"), tableMap.get(IRoleTableConfig.TABLE_ROLE_MODULE_LOGIC_CONFIG));
		KRoleBaseInfoCacheManager.init();
		this.loadExtCreator(root.getChildTextTrim("extCAConfigPath"));
		_LOGGER.warn(" ----- {} extension 初始化完成------", module.getModuleName());
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {
		_LOGGER.warn(" ----- {} extension 收到游戏世界初始化完成通知 ------", _module.getModuleName());
		try {
			KRoleModuleManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException(e);
		}
		KRecoverPhyPowerTask.submit();
		KSupportFactory.getVIPModuleSupport().addVipUpLvListener(new KRoleVipLvListener());
		
		KRandomNameManager.onGameWorldInitComplete();
		
		_LOGGER.warn(" ----- {} extension 处理完初始化完成的通知 ------", _module.getModuleName());
	}

	@Override
	public boolean messageReceived(KGameMessageEvent msgEvent) throws KGameServerException {
		if(KCombatModule.processMsg(msgEvent)) {
			return true;
		}
		return false;
	}

	@Override
	public void exceptionCaught(KGameServerException ex) {
		
	}

	@Override
	public void serverShutdown() throws KGameServerException {
		KRandomNameManager.shutdown();
	}

	@Override
	public Role newRoleInstance(int templateId) {
		KRoleTemplate template = KRoleModuleManager.getRoleTemplate(templateId);
		return new KRole(template);
	}
	
	@Override
	public Role newRoleInstanceForDB() {
		return new KRole();
	}

	@Override
	public Role newSystemRoleInstance() {
		return new KSystemRole();
	}
	
	@Override
	public RoleBaseInfo newRoleSimpleInfoInstance() {
		return new KRoleBaseInfo();
	}
	
	@Override
	public RoleExtCA newRoleExtCAInstanceFromDB(long roleId, int type) {
		IRoleExtCACreator creator = _extCACreators.get(type);
		if(creator != null) {
			return creator.createRoleExtCAForDB(roleId, type);
		}
		return null;
	}
	
	@Override
	public RoleExtCA newRoleExtCAInstance(long roleId, int type) {
		IRoleExtCACreator creator = _extCACreators.get(type);
		if(creator != null) {
			return creator.newRoleExtCAInstance(roleId, type);
		}
		return null;
	}

	@Override
	public RoleExtCASet newRoleExtCASetInstance() {
		return new KRoleExtCASet();
	}
	
//	@Override
//	public void notifyRoleCreated(Role role) {
//		
//	}
	
	@Override
	public void notifyRoleDeleted(long roleId) {
		KRoleModuleManager.notifyRoleDeleted(roleId);
	}

	@Override
	public void notifyRoleJoinedGame(Role role) {
		_LOGGER.info("角色登录回调通知！角色id：{}", role.getId());
		KGamePlayerSession session = KGame.getPlayerSession(role.getPlayerId());
//		KRoleModuleManager.afterRoleJoinedGame(session, (KRole) role);
		KRole kRole = (KRole) role;
		kRole.onLogin();
		kRole.checkOffLineIncreasePhyPower(false); // 检查离线战斗力恢复
		KRoleServerMsgPusher.sendInitRoleData(session, kRole);
		KRoleModuleManager.notifyRoleJoinedGame(session, kRole);
	}

	@Override
	public void notifyRoleLeavedGame(Role role, boolean isPlayerLogout) {
//		KGamePlayerSession session = KGame.getPlayerSession(role.getPlayerId());
//	    KRoleModuleManager.afterRoleLeavedGame(session, (KRole)role);
		KRoleModuleManager.afterRoleLeavedGame((KRole) role);
		if (!isPlayerLogout && _enableWaitWhenReceiveLeaveGameEvent) {
			try {
				TimeUnit.MILLISECONDS.sleep(_waitMillisWhenReceiveLeaveGameEvent);
			} catch (Exception e) {
				_LOGGER.error("notifyRoleLeavedGame等待出现异常！roleId:{}", role.getId());
			}
		}
	}
	
	@Override
	public void notifyCacheLoadComplete() {
		_LOGGER.warn(" ----- {} extension 收到缓存初始化完成通知 ------", _module.getModuleName());
		_LOGGER.warn(" ----- {} extension 处理完缓存初始化完成通知 ------", _module.getModuleName());
	}

}
