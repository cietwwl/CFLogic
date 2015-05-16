package com.kola.kmp.logic.role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.role.Role;
import com.kola.kgame.cache.role.RoleBaseInfo;
import com.kola.kgame.cache.role.RoleModuleFactory;
import com.kola.kgame.cache.role.exception.RoleCreationException;
import com.kola.kgame.cache.role.exception.RoleQueryException;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.OtherFlowTypeEnum;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.other.KTableInfo;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KGameUtilTool;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.RoleTips;

/**
 * 
 * @author PERRY CHAN
 */
public class KRoleModuleManager {

	private static final Logger _LOGGER = KGameLogger.getLogger(KRoleModuleManager.class);
	
	private static final Map<Integer, KRoleTemplate> _allTemplates = new HashMap<Integer, KRoleTemplate>();
	private static final Map<Integer, ExpData> _allExpDatas = new HashMap<Integer, ExpData>();
	private static final Map<KJobTypeEnum, Map<Integer, KRoleLevelAttribute>> _roleLvMap = new HashMap<KJobTypeEnum, Map<Integer,KRoleLevelAttribute>>();
	
	private static final List<IRoleEventListener> _roleJoinedGameEventListeners = new ArrayList<IRoleEventListener>();
	private static final List<IRoleEventListener> _roleLeavedGameEventListeners = new ArrayList<IRoleEventListener>();
	private static final List<IRoleEventListener> _roleCreatedEventListeners = new ArrayList<IRoleEventListener>();
	private static final List<IRoleEventListener> _roleDeletedEventListeners = new ArrayList<IRoleEventListener>();
	private static final List<IRoleEventListener> _roleUpgradedEventListeners = new ArrayList<IRoleEventListener>();
	private static final List<IRoleEventListener> _rolePutToCacheEventListeners = new ArrayList<IRoleEventListener>();
	
	private static final Map<Integer, IRoleAttributeProvider> _allAttributeProvider = new HashMap<Integer, IRoleAttributeProvider>();
	private static final Map<Integer, Integer> _pvpHpMultipleMap = new HashMap<Integer, Integer>();
	public static final Map<Integer, Integer> pvpHpMultipleMap = Collections.unmodifiableMap(_pvpHpMultipleMap);
	static List<Integer> allProviderTypes;
	private static KRoleJobCounterTask _jobCounter;
	
	private static void loadLvAttribute(KGameExcelFile file, KTableInfo tableInfo, KJobTypeEnum jobType) {
		KGameExcelTable table = file.getTable(tableInfo.tableName, tableInfo.headerIndex);
		KGameExcelRow[] allRows = table.getAllDataRows();
		Map<Integer, KRoleLevelAttribute> map = new HashMap<Integer, KRoleLevelAttribute>();
		KRoleLevelAttribute roleLvAttr;
		KRoleTemplate template = null;
		KGameExcelRow row;
		for(Iterator<KRoleTemplate> itr = _allTemplates.values().iterator(); itr.hasNext();) {
			template = itr.next();
			if(template.job == jobType.getJobType()) {
				break;
			} else {
				template = null;
			}
		}
		for (int i = 0; i < allRows.length; i++) {
			row = allRows[i];
			roleLvAttr = new KRoleLevelAttribute(row, template);
			map.put(row.getInt("level"), roleLvAttr);
		}
		_roleLvMap.put(jobType, map);
	}
	
//	static void afterRoleJoinedGame(KGamePlayerSession session, KRole role) {
//		role.onLogin();
//		role.checkOffLineIncreasePhyPower(false); // 检查离线战斗力恢复
////		KSupportFactory.getPetSupport().initFightingPets(role.getRoleId()); // 先计算一下战斗副将的属性
////		role.calculateTotalBattlePower(false);
//		KRoleServerMsgPusher.sendInitRoleData(session, role);
////		KPlayerRoleServerMsgPusher.sendEvolveRequiredInfo(role);
//		if (!KRoleModuleConfig.isOpenPlayStory() || role.isHadPlayStory()) {
////			doJoinGame(session, role);
//			notifyRoleJoinedGame(session, role);
//		} else {
//			KRoleServerMsgPusher.sendPlayMovieMsg(session, true); //2013-06-07 临时调整为可以跳过剧情
//		}
//	}
	
	static void afterRoleLeavedGame(/*KGamePlayerSession session,*/ KRole role) {
		notifyRoleLeavedGame(/*session,*/ role);
	}
	
	static void loadData(String path, Map<Byte, KTableInfo> tableMap) throws Exception {
		KGameExcelFile file = new KGameExcelFile(path);
		KGameExcelRow[] allRows = KGameUtilTool.getAllDataRows(file, tableMap, IRoleTableConfig.TABLE_ROLE_ATTRIBUTE_DATA);
		Map<Integer, KGameExcelRow> attributeMap = new HashMap<Integer, KGameExcelTable.KGameExcelRow>();
		KGameExcelRow row;
		for(int i = 0; i < allRows.length; i++) {
			row = allRows[i];
			int attributeId = row.getInt("attributeId");
			if(attributeMap.containsKey(attributeId)) {
				throw new RuntimeException("重复的属性模板id：" + attributeId);
			} else {
				attributeMap.put(attributeId, row);
			}
		}
		
		allRows = KGameUtilTool.getAllDataRows(file, tableMap, IRoleTableConfig.TABLE_ROLE_TEMPLATE_DATA);
		for(int i = 0; i < allRows.length; i++) {
			row = allRows[i];
			KRoleTemplate template = new KRoleTemplate(row, attributeMap.get(row.getInt("attributeId")));
			_allTemplates.put(template.templateId, template);
		}
		
		allRows = KGameUtilTool.getAllDataRows(file, tableMap, IRoleTableConfig.TABLE_ROLE_EXP_DATA);
		for(int i = 0; i < allRows.length; i++) {
			row = allRows[i];
			ExpData expData = new ExpData(row.getInt("level"), row.getInt("upgradeExp"), row.getInt("expLimited"));
			_allExpDatas.put(expData.level, expData);
		}
		
		_roleLvMap.put(KJobTypeEnum.WARRIOR, new HashMap<Integer, KRoleLevelAttribute>());
		_roleLvMap.put(KJobTypeEnum.SHADOW, new HashMap<Integer, KRoleLevelAttribute>());
		_roleLvMap.put(KJobTypeEnum.GUNMAN, new HashMap<Integer, KRoleLevelAttribute>());
		
		KTableInfo tableInfo = tableMap.get(IRoleTableConfig.TABLE_WARRIOR_UP_LV_ATTR);
		loadLvAttribute(file, tableInfo, KJobTypeEnum.WARRIOR);
		
		tableInfo = tableMap.get(IRoleTableConfig.TABLE_MAGICIAN_UP_LV_ATTR);
		loadLvAttribute(file, tableInfo, KJobTypeEnum.SHADOW);
		
		tableInfo = tableMap.get(IRoleTableConfig.TABLE_BOWMAN_UP_LV_ATTR);
		loadLvAttribute(file, tableInfo, KJobTypeEnum.GUNMAN);
		
		tableInfo = tableMap.get(IRoleTableConfig.TABLE_BATTLE_POWER_PARA);
		KRoleModuleConfig.loadBattlePowerPara(file.getTable(tableInfo.tableName, tableInfo.headerIndex));
		
		tableInfo = tableMap.get(IRoleTableConfig.TABLE_PVP_HP_MULTIPLE);
		allRows = file.getTable(tableInfo.tableName, tableInfo.headerIndex).getAllDataRows();
		for (int i = 0; i < allRows.length; i++) {
			row = allRows[i];
			_pvpHpMultipleMap.put(row.getInt("level"), row.getInt("hpMultiple"));
		}
	}
	
	static void loadListener(String path) throws Exception {
		Document doc = XmlUtil.openXml(path);
		Element root = doc.getRootElement();
		Element listenerElement = root.getChild("listeners");
		@SuppressWarnings("unchecked")
		List<Element> children = listenerElement.getChildren("listener");
		Element child;
		int id;
		boolean listenJoined;
		boolean listenLeaved;
		boolean listenCreated;
		boolean listenDeleted;
		boolean listenLevelUp;
		boolean listenPutToCache;
		Map<Integer, IRoleEventListener> joinMap = new HashMap<Integer, IRoleEventListener>();
		Map<Integer, IRoleEventListener> leaveMap = new HashMap<Integer, IRoleEventListener>();
		Map<Integer, IRoleEventListener> createMap = new HashMap<Integer, IRoleEventListener>();
		Map<Integer, IRoleEventListener> deleteMap = new HashMap<Integer, IRoleEventListener>();
		Map<Integer, IRoleEventListener> levelUpMap = new HashMap<Integer, IRoleEventListener>();
		Map<Integer, IRoleEventListener> putToCacheMap = new HashMap<Integer, IRoleEventListener>();
		String[] seq = listenerElement.getAttributeValue("sequence").split(",");
		List<Integer> seqList = new ArrayList<Integer>();
		List<Integer> notSeqList = new ArrayList<Integer>();
		IRoleEventListener listener;
		if (seq.length > 0 && seq[0].length() > 0) {
			for (int i = 0; i < seq.length; i++) {
				seqList.add(Integer.parseInt(seq[i]));
			}
		}
		for(int i = 0; i < children.size(); i++) {
			child = children.get(i);
			listener = (IRoleEventListener)Class.forName(child.getAttributeValue("clazz")).newInstance();
			id = Integer.parseInt(child.getChildTextTrim("id"));
			listenJoined = UtilTool.getStringToBoolean(child.getChildTextTrim("listenJoinedGameEvent"));
			listenLeaved = UtilTool.getStringToBoolean(child.getChildTextTrim("listenLeavedGameEvent"));
			listenCreated = UtilTool.getStringToBoolean(child.getChildTextTrim("listenCreatedEvent"));
			listenDeleted = UtilTool.getStringToBoolean(child.getChildTextTrim("listenDeletedEvent"));
			listenLevelUp = UtilTool.getStringToBoolean(child.getChildTextTrim("listenLevelUpEvent"));
			listenPutToCache = UtilTool.getStringToBoolean(child.getChildTextTrim("listenPutToCacheEvent"));
			if(listenJoined) {
				joinMap.put(id, listener);
			}
			if(listenLeaved) {
				leaveMap.put(id, listener);
			}
			if(listenCreated) {
				createMap.put(id, listener);
			}
			if(listenDeleted) {
				deleteMap.put(id, listener);
			}
			if(listenLevelUp) {
				levelUpMap.put(id, listener);
			}
			if(listenPutToCache) {
				putToCacheMap.put(id, listener);
			}
			if(!seqList.contains(id)) {
				notSeqList.add(id);
			}
		}
		Collections.sort(notSeqList);
		seqList.addAll(notSeqList);
		for(int i = 0; i < seqList.size(); i++) {
			id = seqList.get(i);
			listener = joinMap.remove(id);
			if(listener != null) {
				_roleJoinedGameEventListeners.add(listener);
			}
			listener = leaveMap.remove(id);
			if(listener != null) {
				_roleLeavedGameEventListeners.add(listener);
			}
			listener = createMap.remove(id);
			if(listener != null) {
				_roleCreatedEventListeners.add(listener);
			}
			listener = deleteMap.remove(id);
			if(listener != null) {
				_roleDeletedEventListeners.add(listener);
			}
			listener = levelUpMap.remove(id);
			if(listener != null) {
				_roleUpgradedEventListeners.add(listener);
			}
			listener = putToCacheMap.remove(id);
			if(listener != null) {
				_rolePutToCacheEventListeners.add(listener);
			}
		}
		_roleCreatedEventListeners.addAll(createMap.values());
		_roleDeletedEventListeners.addAll(deleteMap.values());
		_roleJoinedGameEventListeners.addAll(joinMap.values());
		_roleLeavedGameEventListeners.addAll(leaveMap.values());
		_roleUpgradedEventListeners.addAll(levelUpMap.values());
		_rolePutToCacheEventListeners.addAll(putToCacheMap.values());
	}
	
	static void loadAttributeProvider(String path) throws Exception {
		Document doc = XmlUtil.openXml(path);
		@SuppressWarnings("unchecked")
		List<Element> children = doc.getRootElement().getChildren("provider");
		Element child;
		int type = 0;
		List<Integer> tempList = new ArrayList<Integer>();
		for(int i = 0; i < children.size(); i++) {
			child = children.get(i);
			type++;
			IRoleAttributeProvider provider = (IRoleAttributeProvider)Class.forName(child.getAttributeValue("clazz")).newInstance();
			provider.notifyTypeAssigned(type);
			_allAttributeProvider.put(type, provider);
			tempList.add(type);
		}
		allProviderTypes = Collections.unmodifiableList(tempList);
	}
	
	static void onGameWorldInitComplete() throws Exception {
//		List<String> exceptions = new ArrayList<String>();
		int exceptionCount = 0;
		for (int i = KRoleModuleConfig.getRoleMaxLv(); i > 0; i--) {
			if(!_allExpDatas.containsKey(i)) {
				_LOGGER.error("缺少等级[{}]的升级经验配置！", i);
				exceptionCount++;
			}
		}
		Map.Entry<KJobTypeEnum, Map<Integer, KRoleLevelAttribute>> current;
		for (Iterator<Map.Entry<KJobTypeEnum, Map<Integer, KRoleLevelAttribute>>> itr = _roleLvMap.entrySet().iterator(); itr.hasNext();) {
			current = itr.next();
			for (int i = KRoleModuleConfig.getRoleMaxLv(); i > 0; i--) {
				if (!current.getValue().containsKey(i)) {
					_LOGGER.error("职业：[{}]，缺少等级[{}]的升级属性！", current.getKey().getJobName(), i);
					exceptionCount++;
				}
			}
		}
		if(exceptionCount > 0) {
			throw new RuntimeException("角色模块数据检查异常！");
		}
		_jobCounter = new KRoleJobCounterTask(new ArrayList<Integer>(_allTemplates.keySet()));
		_jobCounter.start();
	}
	
	static final KRoleLevelAttribute getRoleLevelAttribute(KJobTypeEnum job, int level) {
		Map<Integer, KRoleLevelAttribute> map = _roleLvMap.get(job);
		return map.get(level);
	}
	
	static final ExpData getExpData(int level) {
		return _allExpDatas.get(level);
	}
	
	static final int getUpgradeExp(int level) {
		ExpData expData = _allExpDatas.get(level);
		if (expData != null) {
			return expData.upgradeExp;
		} else {
			return Integer.MAX_VALUE;
		}
	}
	
	static final IRoleAttributeProvider getAttributeProvider(int type) {
		return _allAttributeProvider.get(type);
	}
	
	static final void notifyRoleJoinedGame(KGamePlayerSession session, Role role) {
		_LOGGER.info("通知个模块角色登录事件开始！角色id：{}", role.getId());
		IRoleEventListener listener;
		for (int i = 0; i < _roleJoinedGameEventListeners.size(); i++) {
			try {
				listener = _roleJoinedGameEventListeners.get(i);
//				_LOGGER.info("通知[{}]角色登陆事件！", listener.getClass().getSimpleName());
				listener.notifyRoleJoinedGame(session, (KRole) role);
			} catch (Exception e) {
				_LOGGER.error("通知其他模块角色登陆事件出现异常！", e);
			}
		}
		_LOGGER.info("通知个模块角色登录事件结束！角色id：{}", role.getId());
	}
	
	static final void notifyRoleLeavedGame(/*KGamePlayerSession session,*/ Role role) {
		for (int i = 0; i < _roleLeavedGameEventListeners.size(); i++) {
			try {
				_roleLeavedGameEventListeners.get(i).notifyRoleLeavedGame(/*session, */(KRole) role);
			} catch (Exception e) {
				_LOGGER.error("通知其他模块角色登出事件出现异常！", e);
			}
		}
	}
	
	static final void notifyRoleCreated(KGamePlayerSession session, Role role) {
		for(int i = 0; i < _roleCreatedEventListeners.size(); i++) {
			try {
				_roleCreatedEventListeners.get(i).notifyRoleCreated(session, (KRole) role);
			} catch (Exception e) {
				_LOGGER.error("通知其他模块角色创建事件出现异常！", e);
			}
		}
		_jobCounter.notifyRoleCreated(role.getType());
	}
	
	static final void notifyRoleDeleted(long roleId) {
		RoleBaseInfo baseInfo = KRoleBaseInfoCacheManager.getRoleBaseInfo(roleId);
		for (int i = 0; i < _roleDeletedEventListeners.size(); i++) {
			try {
				_roleDeletedEventListeners.get(i).notifyRoleDeleted(roleId);
			} catch (Exception e) {
				_LOGGER.error("通知其他模块角色删除事件出现异常！", e);
			}
		}
		if (baseInfo != null) {
			_jobCounter.notifyRoleDeleted(baseInfo.getType());
		}
	}
	
	static final void notifyRoleLevelUp(KRole role, int preLv) {
		for(int i = 0; i < _roleUpgradedEventListeners.size(); i++) {
			_roleUpgradedEventListeners.get(i).notifyRoleLevelUp(role, preLv);
		}
		KGamePlayerSession session = KGame.getPlayerSession(role.getPlayerId());
		if(session != null && session.getBoundPlayer() != null) {
			session.getBoundPlayer().updateRoleSimpleInfo(KGame.getGSID(), role.getId(), role.getLevel(), role.getJob());
		}
		FlowManager.logOther(role.getId(), OtherFlowTypeEnum.角色升级, RoleTips.getTipsFlowRoleLevelUp(preLv, role.getLevel()));
	}
	
	static final void notifyRolePutToCache(KRole role) {
		for (int i = 0; i < _rolePutToCacheEventListeners.size(); i++) {
			_rolePutToCacheEventListeners.get(i).notifyRoleDataPutToCache(role);
		}
	}
	
//	static void doJoinGame(KGamePlayerSession session, KRole role) {
//		if (role != null) {
////			role.checkAndResetPhyPowerBuyTimes(false);
////			role.getRoleTowerAttribute().checkResetChallengeTimes(false);
//			notifyRoleJoinedGame(session, role);
////			KunlunLoggerCenter.onRoleLogin(session, session.getBoundPlayer(), role);
//		}
//	}
	
	static KRoleTemplate getRoleTemplateByJob(byte job) {
		KRoleTemplate template;
		for(Iterator<KRoleTemplate> itr = _allTemplates.values().iterator(); itr.hasNext();) {
			template = itr.next();
			if(template.job == job) {
				return template;
			}
		}
		return null;
	}
	
	public final static KRoleTemplate getRoleTemplate(int templateId) {
		return _allTemplates.get(templateId);
	}
	
	public static KActionResult<Long> createRole(KGamePlayerSession session, String name, int templateId, boolean joinGameIfSuccess, String inviteCode) {
		KActionResult<Long>  result = new KActionResult<Long>();
		int length = UtilTool.getStringLength(name);
		long playerId = session.getBoundPlayer().getID();
		if (length < KRoleModuleConfig.getRoleNameLengthMin() || length > KRoleModuleConfig.getRoleNameLengthMax()) {
			result.tips = RoleTips.getTipsRoleNameLengthNotPass();
		} else if (!RoleModuleFactory.getRoleModule().checkNameUseable(name)) {
			KRandomNameManager.nameUsed(playerId, name);
			result.tips = RoleTips.getTipsRoleNameDuplicate();
		} else if (KSupportFactory.getDirtyWordSupport().containDirtyWord(name) != null) {
			result.tips = RoleTips.getTipsRoleNameContainsDirtyWord();
		} else {
			boolean roleCountPass = false;
			try {
				roleCountPass = KRoleModuleConfig.getMaxRoleCountOfPlayer() > RoleModuleFactory.getRoleModule().getRoleCountOfPlayer(session.getBoundPlayer().getID());
			} catch (KGameServerException e) {
				_LOGGER.error("检查角色数量时出现异常！", e);
			}
			if (roleCountPass) {
				KRoleTemplate template = getRoleTemplate(templateId);
				if (template == null) {
					result.tips = GlobalTips.getTipsServerBusy();
				} else {
					Role role = null;
					String tips;
					try {
						role = RoleModuleFactory.getRoleModule().createRole(session.getBoundPlayer().getID(), name, template.templateId, templateId);
					} catch (RoleCreationException e) {
						tips = RoleTips.getTipsCreateRoleFail(e.getErrorCode());
					}
					if (role != null) {
						result.success = true;
						result.attachment = role.getId();
						try {
							notifyRoleCreated(session, role);
						} catch (Exception e) {
							_LOGGER.error("notifyRoleCreated出现异常！角色id：{}", role.getId(), e);
						}
						try {
							KRandomNameManager.roleCreated(playerId, name);
						} catch (Exception e) {
							_LOGGER.error("KRandomNameManager.roleCreated出现异常！角色id：{}", role.getId(), e);
						}
						if (joinGameIfSuccess) {
							try {
								RoleModuleFactory.getRoleModule().roleJoinGame(role.getId());
							} catch (KGameServerException e) {
								e.printStackTrace();
							}
						}
						tips = null;
						// 职业统计
						RoleModuleFactory.getRoleModule().recrodCreateRoleJob(template.job);
					} else {
						tips = GlobalTips.getTipsServerBusy();
					}
					result.tips = tips;
				}
			} else {
				result.tips = RoleTips.getTipsRoleCountLimited();
			}
		}
		return result;
	}
	
	public static String deletePlayerRole(KGamePlayerSession session, long roleId) {
		String result = null;
		try {
			RoleModuleFactory.getRoleModule().deleteRole(session.getBoundPlayer().getID(), roleId);
		} catch (RoleQueryException ex) {
			_LOGGER.error("删除角色时出现异常，roleId是：" + roleId, ex);
			result = RoleTips.getTipsDeleteRoleFail(ex.getErrorCode());
		}
		return result;
	}
	
	public static int getDefaultTemplateId() {
		return _jobCounter.getDefaultTemplateId();
	}
	
	static class ExpData {
		
		public final int level;
		public final int upgradeExp;
		public final int maxExpOnce;
		
		public ExpData(int pLevel, int pUpgradeExp, int pMaxExpOnce) {
			this.level = pLevel;
			this.upgradeExp = pUpgradeExp;
			this.maxExpOnce = pMaxExpOnce;
		}
	}
}
