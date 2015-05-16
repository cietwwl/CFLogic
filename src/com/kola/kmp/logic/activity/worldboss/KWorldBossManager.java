package com.kola.kmp.logic.activity.worldboss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

import com.koala.game.KGameMessage;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.activity.KActivityManager;
import com.kola.kmp.logic.activity.KActivityModuleDialogProcesser;
import com.kola.kmp.logic.activity.KActivityRoleExtCaCreator;
import com.kola.kmp.logic.mission.guide.KGuideManager;
import com.kola.kmp.logic.mission.guide.MainMenuFunction;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.npc.message.KShowDialogMsg;
import com.kola.kmp.logic.other.KFunctionTypeEnum;
import com.kola.kmp.logic.other.KTableInfo;
import com.kola.kmp.logic.rank.RankElementLevel;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KGameUtilTool;
import com.kola.kmp.logic.util.tips.WorldBossTips;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;

/**
 * 
 * @author PERRY CHAN
 */
public class KWorldBossManager {

	private static final Logger _LOGGER = KGameLogger.getLogger(KWorldBossManager.class);
//	private static final Map<Integer, KWorldBossGenRule> _genRuleMap = new HashMap<Integer, KWorldBossGenRule>(); // key=规则id，value=规则数据
//	private static final Map<Integer, KWorldBossBaseReward> _killRewards = new HashMap<Integer, KWorldBossBaseReward>(); // 击杀奖励，key=奖励id，value=道具集合(key=道具编号，value=道具数量)
//	private static final Map<Integer, Map<Integer, KWorldBossRankReward>> _rankRewards = new HashMap<Integer,  Map<Integer, KWorldBossRankReward>>(); // 排名奖励，key=奖励id，value=排名奖励数据
	private static final Set<KWorldBossRankReward> _rankRewards = new HashSet<KWorldBossRankReward>();
	private static final Set<KWorldBossRankReward> _rankRewardsRO = Collections.unmodifiableSet(_rankRewards);
	private static final Map<Integer, KInspireTemplateData> _allInspireData = new HashMap<Integer, KInspireTemplateData>(); // 所有的鼓舞数据，key=鼓舞等级，value=鼓舞模板数据
	private static final Map<Integer, KWorldBossRewardBasicPara> _rewardParaMap = new HashMap<Integer, KWorldBossRewardBasicPara>();
	private static final Map<Integer, Integer> _bossStartLv = new HashMap<Integer, Integer>(); // key=游戏世界前100名玩家的平均等级，value=对应的boss等级
//	private static int _defaultBossStartLv;
	private static final Map<Integer, KWorldBossTemplate> _bossTemplateMap = new HashMap<Integer, KWorldBossTemplate>(); // key=世界boss的等级，value=怪物模板id
	private static final Map<Integer, Integer> _bossUpgradeMap = new LinkedHashMap<Integer, Integer>(); // key=boss被击杀所使用的时间（分钟），value=boss提升的等级
	private static int _maxInspireLv;
//	private static List<KWorldBossFieldData> _allWorldBossFieldDatas; // 所有的世界boss场景数据
	private static KWorldBossFieldData _worldBossFieldData;
	private static KWorldBossActivityMain _activity;
	private static final Map<Long, Boolean> _autoJoinRoleIds = new ConcurrentHashMap<Long, Boolean>();
	private static final Map<Long, Boolean> _autoJoinRoleIdsRO = Collections.unmodifiableMap(_autoJoinRoleIds);
	private static KGameMessage _autoJoinConfirmMsg;
	
	/*private static Map<Integer, List<KGameExcelRow>> separateRowByKey(KGameExcelRow[] rows, String key) {
		Map<Integer, List<KGameExcelRow>> rewardRowsByRewardId = new HashMap<Integer, List<KGameExcelRow>>();
		KGameExcelRow row;
		List<KGameExcelRow> rowList;
		int keyId;
		for (int i = 0; i < rows.length; i++) {
			row = rows[i];
			keyId = row.getInt(key);
			rowList = rewardRowsByRewardId.get(keyId);
			if (rowList == null) {
				rowList = new ArrayList<KGameExcelRow>();
				rewardRowsByRewardId.put(keyId, rowList);
			}
			rowList.add(row);
		}
		return rewardRowsByRewardId;
	}*/
	
	static void loadData(String path, Map<Byte, KTableInfo> tableMap) throws Exception {
		KGameExcelFile file = new KGameExcelFile(path);
		KGameExcelRow[] allRows;
		
		{
			allRows = KGameUtilTool.getAllDataRows(file, tableMap, KWorldBossTableInfo.TABLE_WORLD_BOSS_FIELD_DATA);
//			List<KWorldBossFieldData> tempList = new ArrayList<KWorldBossFieldData>();
//			for (int i = 0; i < allRows.length; i++) {
//				tempList.add(new KWorldBossFieldData(allRows[i]));
//			}
//			_allWorldBossFieldDatas = Collections.unmodifiableList(tempList);
			_worldBossFieldData = new KWorldBossFieldData(allRows[0]);
		}
		
		{
			/*allRows = KGameUtilTool.getAllDataRows(file, tableMap, KWorldBossTableInfo.TABLE_INSPIRE_FIXED_INC_DATA);
			Map<Integer, List<KGameExcelRow>> fixedIncs = new HashMap<Integer, List<KGameExcelRow>>();
			List<KGameExcelRow> fixedIncRows;
			for (int i = 0; i < allRows.length; i++) {
				KGameExcelRow row = allRows[i];
				int inspireLv = row.getInt("inspireLv");
				fixedIncRows = fixedIncs.get(inspireLv);
				if (fixedIncRows == null) {
					fixedIncRows = new ArrayList<KGameExcelRow>();
					fixedIncs.put(inspireLv, fixedIncRows);
				}
				if (_maxInspireLv < inspireLv) {
					_maxInspireLv = inspireLv;
				}
				fixedIncRows.add(row);
			}*/
			
			allRows = KGameUtilTool.getAllDataRows(file, tableMap, KWorldBossTableInfo.TABLE_INSPIRE_BASIC_DATA);
			for (int i = 0; i < allRows.length; i++) {
				KGameExcelRow row = allRows[i];
				int inspireLv = row.getInt("inspireLv");
				KInspireTemplateData templateData = new KInspireTemplateData(row);
				_allInspireData.put(templateData.inspireLv, templateData);
				if (_maxInspireLv < inspireLv) {
					_maxInspireLv = inspireLv;
				}
			}
		}
		
		{
			/*allRows = KGameUtilTool.getAllDataRows(file, tableMap, KWorldBossTableInfo.TABLE_MONSTER_GEN_RULE_TEMPLATE);
			Map<Integer, List<KGameExcelRow>> rowsByKey = separateRowByKey(allRows, "genTemplateId");
			for (Iterator<List<KGameExcelRow>> itr = rowsByKey.values().iterator(); itr.hasNext();) {
				KWorldBossGenRule temp = new KWorldBossGenRule(itr.next());
				_genRuleMap.put(temp.genId, temp);
			}*/

			/*Map<Integer, List<KGameExcelRow>> rowsByKey;
			allRows = KGameUtilTool.getAllDataRows(file, tableMap, KWorldBossTableInfo.TABLE_KILL_REWARD_DATA);
			rowsByKey = separateRowByKey(allRows, "rewardId");
			List<KGameExcelRow> tempRowList;
			for (Iterator<List<KGameExcelRow>> itr = rowsByKey.values().iterator(); itr.hasNext();) {
				tempRowList = itr.next();
				KWorldBossBaseReward reward = new KWorldBossBaseReward(tempRowList);
				_killRewards.put(reward.rewardId, reward);
			}*/
		}
		
		{
			allRows = KGameUtilTool.getAllDataRows(file, tableMap, KWorldBossTableInfo.TABLE_RANK_REWARD_DATA);
			for (int i = 0; i < allRows.length; i++) {
				KWorldBossRankReward rankReward = new KWorldBossRankReward(allRows[i]);
				_rankRewards.add(rankReward);
			}
		}
		
		{
			allRows = KGameUtilTool.getAllDataRows(file, tableMap, KWorldBossTableInfo.TABLE_REWARD_PARA);
			for (int i = 0; i < allRows.length; i++) {
				KWorldBossRewardBasicPara para = new KWorldBossRewardBasicPara(allRows[i]);
				_rewardParaMap.put(para.lv, para);
			}
		}
		
		{
			KGameExcelRow tempRow;
			allRows = KGameUtilTool.getAllDataRows(file, tableMap, KWorldBossTableInfo.TABLE_BOSS_START_LV);
			int defaultBossStartLv = 0;
			for (int i = 0; i < allRows.length; i++) {
				tempRow = allRows[i];
				_bossStartLv.put(tempRow.getInt("lv"), tempRow.getInt("BossLv"));
				if (i == 0) {
					defaultBossStartLv = tempRow.getInt("BossLv");
				}
			}
			KWorldBossConfig.setDefaultBossStartLv(defaultBossStartLv);
		}
		
		{
			KGameExcelRow tempRow;
			allRows = KGameUtilTool.getAllDataRows(file, tableMap, KWorldBossTableInfo.TABLE_BOSS_UPGRADE_CONFIG);
			Map<Integer, Integer> tempMap = new HashMap<Integer, Integer>();
			for (int i = 0; i < allRows.length; i++) {
				tempRow = allRows[i];
				tempMap.put(tempRow.getInt("usedMinutesOfKilled"), tempRow.getInt("Lvup"));
			}
			List<Integer> timeList = new ArrayList<Integer>(tempMap.values());
			Collections.sort(timeList);
			for (int i = 0; i < timeList.size(); i++) {
				_bossUpgradeMap.put(timeList.get(i), tempMap.get(timeList.get(i)));
			}
		}
		
		{
			KGameExcelRow tempRow;
			allRows = KGameUtilTool.getAllDataRows(file, tableMap, KWorldBossTableInfo.TABLE_BOSS_LV_AND_TEMPLATE_ID_MAPPING);
			KWorldBossTemplate template;
			int maxBossLv = 0;
			for (int i = 0; i < allRows.length; i++) {
				tempRow = allRows[i];
				template = new KWorldBossTemplate(tempRow);
				_bossTemplateMap.put(template.bossLv, template);
				if (maxBossLv < template.bossLv) {
					maxBossLv = template.bossLv;
				}
			}
			KWorldBossConfig.setMaxBossLv(maxBossLv);
		}
	}
	
	static void onGameWorldInitComplete() {
		int exceptionCount = 0;
		_activity = new KWorldBossActivityMain();
		/*for (Iterator<KWorldBossGenRule> itr = _genRuleMap.values().iterator(); itr.hasNext();) {
			try {
				itr.next().onGameWorldInitComplete();
			} catch (Exception e) {
				_LOGGER.error(e.getMessage(), e);
				exceptionCount++;
			}
		}*/
//		for (int i = 0; i < _allWorldBossFieldDatas.size(); i++) {
//			try {
//				_allWorldBossFieldDatas.get(i).onGameWorldInitComplete();
//			} catch (Exception e) {
//				_LOGGER.error(e.getMessage(), e);
//				exceptionCount++;
//			}
//		}
		try {
			_worldBossFieldData.onGameWorldInitComplete();
		} catch (Exception e) {
			_LOGGER.error(e.getMessage(), e);
			exceptionCount++;
		}
		Map.Entry<Integer, Integer> entry;
		for (Iterator<Map.Entry<Integer, Integer>> itr = _bossStartLv.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			if (!_bossTemplateMap.containsKey(entry.getKey())) {
				_LOGGER.error("世界boss：不存在等级：{}对应的boss模板id！", entry.getKey());
				exceptionCount++;
			}
		}
		if (exceptionCount > 0) {
			throw new RuntimeException("世界boss数据检测不通过！");
		}
		KWorldBossActivityImpl activity = (KWorldBossActivityImpl)KActivityManager.getInstance().getActivity(KWorldBossActivityMonitor.getWorldBossActivityId());
		KWorldBossConfig.setActivityName(activity.getActivityName());
		KWorldBossActivityMonitor.start();
		_activity.initComplete();
		KWorldBossActivityRecorder.submit();
		MainMenuFunction function = KGuideManager.getMainMenuFunctionInfoMap().get(KFunctionTypeEnum.丧尸攻城.functionId);
		KWorldBossConfig.setWorldBossMinJoinLv(function.getOpenRoleLevelLimit());
		for (int i = 1; i < 10; i++) {
			VIPLevelData vipLevelData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(i);
			if (vipLevelData.bAutomaticParticipation) {
				KWorldBossConfig.setWolrdBossAutoJoinVipLv(vipLevelData.lvl);
				break;
			}
		}
		List<KDialogButton> buttonList = new ArrayList<KDialogButton>();
		buttonList.add(new KDialogButton(KActivityModuleDialogProcesser.FUN_CANCLE_AUTO_JOIN, "", KDialogButton.CANCEL_DISPLAY_TEXT));
		buttonList.add(new KDialogButton(KActivityModuleDialogProcesser.FUN_CONFIRM_AUTO_JOIN, "", KDialogButton.CONFIRM_DISPLAY_TEXT));
		_autoJoinConfirmMsg = KShowDialogMsg.createFunMsg(WorldBossTips.getTipsAutoJoinIntroduceTitle(), WorldBossTips.getTipsAutoJoinIntroduceContent(), false, (byte)-1, buttonList);
	}
	
	/*static final KWorldBossGenRule getWorldBossGenRule(int genId) {
		return _genRuleMap.get(genId);
	}*/
	
	/*static final KWorldBossBaseReward getKillReward(int rewardId) {
		return _killRewards.get(rewardId);
	}*/
	
	/*static final Map<Integer, KWorldBossRankReward> getRankReward(int rewardId) {
		return _rankRewards.get(rewardId);
	}*/
	static final Set<KWorldBossRankReward> getRankReward() {
		return _rankRewardsRO;
	}
	
	/*static final List<KWorldBossFieldData> getAllWorldBossFieldDatas() {
		return _allWorldBossFieldDatas;
	}*/
	
	static final KWorldBossFieldData getWorldBossFieldData() {
		return _worldBossFieldData;
	}
	
	static final KInspireTemplateData getInspireData(int inspireLv) {
		return _allInspireData.get(inspireLv);
	}
	
	static final KWorldBossRewardBasicPara getRewardPara(int lv) {
		return _rewardParaMap.get(lv);
	}
	
	static final int getMaxInspireLv() {
		return _maxInspireLv;
	}
	
	static Set<Long> getAutoJoinRoleIds() {
		return _autoJoinRoleIdsRO.keySet();
	}
	
	static void removeAutoJoinRoleId(long roleId) {
		_autoJoinRoleIds.remove(roleId);
	}
	
	public static void notifyRoleDataPutToCache(KRole role) {
		if (role.getLevel() < KWorldBossConfig.getWorldBossMinJoinLv()) {
			return;
		}
		KWorldBossRoleData roleData = KActivityRoleExtCaCreator.getWorldBossRoleData(role.getId());
		if (roleData != null) {
			if (roleData.isAutoJoin()) {
				_autoJoinRoleIds.put(role.getId(), Boolean.TRUE);
			}
		}
	}
	
	public static void notifyRoleDeleted(long roleId) {
		_autoJoinRoleIds.remove(roleId);
	}
	
	public static KWorldBossActivityMain getWorldBossActivity() {
		return _activity;
	}
	
	public static int getWorldBossStartLv() {
		List<RankElementLevel> list = KSupportFactory.getRankModuleSupport().getRankElements_Level(1, KWorldBossConfig.getFirstStartLvCalRanking());
		if (list.size() > 0) {
			int totalLevel = 0;
			for (int i = 0; i < list.size(); i++) {
				totalLevel += list.get(i).getElementLv();
			}
			int avgLv = totalLevel / list.size();
			Integer bossLv = _bossStartLv.get(avgLv);
			if (bossLv == null) {
				bossLv = KWorldBossConfig.getDefaultBossStartLv();
			}
			return bossLv;
		} else {
			return KWorldBossConfig.getDefaultBossStartLv();
		}
	}
	
	public static int getWorldBossUpgradeLv(int usedMinutesOfKilled) {
		Map.Entry<Integer, Integer> entry;
		for (Iterator<Map.Entry<Integer, Integer>> itr = _bossUpgradeMap.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			if (usedMinutesOfKilled < entry.getKey()) {
				return entry.getKey();
			}
		}
		return 0;
	}
	
	public static int getBossTemplateIdByLv(int lv) {
		int maxLv = KWorldBossConfig.getMaxBossLv();
		KWorldBossTemplate template = _bossTemplateMap.get(lv > maxLv ? maxLv : lv);
		return template.monsterTemplateId;
	}

	public static KWorldBossTemplate getWorldBossTemplate(int lv) {
		int maxLv = KWorldBossConfig.getMaxBossLv();
		KWorldBossTemplate template = _bossTemplateMap.get(lv > maxLv ? maxLv : lv);
		return template;
	}
	
	public static void processRequestAutoJoin(KRole role, boolean flag, boolean confirm) {
		if (flag && !confirm) {
			role.sendMsg(_autoJoinConfirmMsg.duplicate());
		} else {
			String tips = null;
			if (role != null) {
				VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(role.getId());
				if (vipData.bAutomaticParticipation) {
					KWorldBossRoleData roleData = KActivityRoleExtCaCreator.getWorldBossRoleData(role.getId());
					roleData.setAutoJoinStatus(flag);
					if (flag) {
						tips = WorldBossTips.getTipsAutoJoinTurnOnSuccess();
						_autoJoinRoleIds.put(role.getId(), Boolean.TRUE);
					} else {
						tips = WorldBossTips.getTipsAutoJoinTurnOffSuccess();
						_autoJoinRoleIds.remove(role.getId());
					}
				} else {
					tips = WorldBossTips.getTipsAutoJoinNotOpen(KWorldBossConfig.getWorldBossAutoJoinVipLv());
				}
			}
			KDialogService.sendUprisingDialog(role, tips);
			KWorldBossMessageHandler.syncAutoJoinFlagToClient(role);
		}
	}
	
	
	static interface KWorldBossTableInfo {
		byte TABLE_WORLD_BOSS_FIELD_DATA = 1;
		byte TABLE_INSPIRE_BASIC_DATA = 2;
		byte TABLE_INSPIRE_FIXED_INC_DATA = 3;
		byte TABLE_MONSTER_GEN_RULE_TEMPLATE = 4;
		byte TABLE_KILL_REWARD_DATA = 5;
		byte TABLE_RANK_REWARD_DATA = 6;
		byte TABLE_REWARD_PARA = 7;
		byte TABLE_BOSS_START_LV = 8;
		byte TABLE_BOSS_UPGRADE_CONFIG = 9;
		byte TABLE_BOSS_LV_AND_TEMPLATE_ID_MAPPING = 10;
		byte TABLE_CONFIG = 11;
	}
}
