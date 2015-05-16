package com.kola.kmp.logic.competition.teampvp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.dataaccess.KGameDBException;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.StringUtil;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.DataStatus;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kgame.db.dataaccess.DataAccesserFactory;
import com.kola.kgame.db.dataobject.DBGameExtCA;
import com.kola.kmp.logic.activity.KActivityRoleExtCaCreator;
import com.kola.kmp.logic.activity.KActivityRoleExtData;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.combat.ICombatMirrorDataGroup;
import com.kola.kmp.logic.combat.resulthandler.ICombatResult;
import com.kola.kmp.logic.combat.resulthandler.ICombatRoleResult;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPDanStageInfo.KDanStageType;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPTeam.KTeamPVPTeamMember;
import com.kola.kmp.logic.fashion.KFashionDataStructs.KFashionTemplate;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.OtherFlowTypeEnum;
import com.kola.kmp.logic.mission.guide.KGuideManager;
import com.kola.kmp.logic.mission.guide.MainMenuFunction;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KFunctionTypeEnum;
import com.kola.kmp.logic.other.KGameExtDataDBTypeEnum;
import com.kola.kmp.logic.other.KGangTecTypeEnum;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.other.KTableInfo;
import com.kola.kmp.logic.rank.teampvp.KTeamPVPRankTypeEnum;
import com.kola.kmp.logic.rank.teampvp.TeamPVPRankElement;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KGameUtilTool;
import com.kola.kmp.logic.util.tips.CompetitionTips;

/**
 * 
 * @author PERRY CHAN
 */
public class KTeamPVPManager {

	static final Logger LOGGER = KGameLogger.getLogger(KTeamPVPManager.class);
	
	private static final Comparator<KTeamPVPTeam> _SCORE_SORTER = new Comparator<KTeamPVPTeam>() {

		@Override
		public int compare(KTeamPVPTeam o1, KTeamPVPTeam o2) {
			if(o1.getCurrentScore() > o2.getCurrentScore()) {
				return -1;
			} else if (o1.getCurrentScore() == o2.getCurrentScore()) {
				if(o1.getDanData().danGradeId > o2.getDanData().danGradeId) {
					return -1;
				}
				return 0;
			}
			return 1;
		}
	};
	private static final int[] _defaultHonorReward = new int[] { 0, 0 };
	private static final Map<Integer, KTeamPVPDanData> _danDataMap = new LinkedHashMap<Integer, KTeamPVPDanData>(); // 段位数据，key=段位id，value=段位数据
	private static final Map<Integer, KTeamPVPDanStageInfo> _danRankInfoMap = new LinkedHashMap<Integer, KTeamPVPDanStageInfo>(); // 段位阶级升级数据表，key=段位阶级id，value=段位阶级数据
	private static final Map<Integer, Map<Integer, KTeamPVPChallengeInfo>> _challengeInfoMap = new HashMap<Integer, Map<Integer, KTeamPVPChallengeInfo>>(); // 每次挑战的信息集合，key=挑战次数，value=信息集合
	private static final Map<Integer, Integer> _maxChallengeRewardTimes = new HashMap<Integer, Integer>();
	private static final Map<Integer, int[]> _challengeHonorRewardMap = new HashMap<Integer, int[]>(); // 每次挑战的荣誉奖励配置，key=等级，value={胜利荣誉，失败荣誉}
	private static final Map<Integer, Integer> _dailyExpRewardMap = new HashMap<Integer, Integer>(); // key=等级，value=每日奖励的经验基数
	private static final Map<Integer, Map<KJobTypeEnum, List<KTeamPVPRobotTemplate>>> _robotTemplateMap = new HashMap<Integer, Map<KJobTypeEnum, List<KTeamPVPRobotTemplate>>>(); // key=挑战次数，value={key=职业，value=各个等级模板的集合}
	private static final ConcurrentHashMap<Long, Long> _roleIdToTeamId = new ConcurrentHashMap<Long, Long>(); // key=角色id，value=战队id
	private static final Map<Long, KTeamPVPTeam> _allTeams = new ConcurrentHashMap<Long, KTeamPVPTeam>(); // 所有的竞技队伍
	private static final Map<Long, KTeamPVPRobotTeam> _allRobotTeams = new ConcurrentHashMap<Long, KTeamPVPRobotTeam>();
	private static final Queue<KTeamPVPRobotTeam> _reusableRobotTeams = new ConcurrentLinkedQueue<KTeamPVPRobotTeam>();
	private static final Map<Long, KTeamPVPRobotGroup> _robotGroupMap = new ConcurrentHashMap<Long, KTeamPVPRobotGroup>();
	private static final Map<Long, Long> _robotIdToTeamId = new ConcurrentHashMap<Long, Long>();
	private static final Queue<KTeamPVPTeam> _removeTeams = new ConcurrentLinkedQueue<KTeamPVPTeam>();
	private static final Queue<String> _teamNames = new ConcurrentLinkedQueue<String>();
	private static final Map<Long, Queue<KTeamPVPInvitation>> _invitationMap = new ConcurrentHashMap<Long, Queue<KTeamPVPInvitation>>();
	private static final Map<Long, List<Long>> _invitationIdMap = new ConcurrentHashMap<Long, List<Long>>(); // key=被邀请者id，value=邀请的队伍集合
	private static final Map<Byte, List<String>> _fashionResByJob = new HashMap<Byte, List<String>>();
	
	private static void putTeamToCollection(KTeamPVPTeam team, boolean putToNameQueue) {
		_allTeams.put(team.getId(), team);
		long[] roleIds = team.getAllMemberIds();
		for(int i = 0; i < roleIds.length; i++) {
			_roleIdToTeamId.put(roleIds[i], team.getId());
		}
		if (putToNameQueue) {
			_teamNames.add(team.getTeamName());
		}
	}
	
	private static Queue<KTeamPVPInvitation> getInvitationQueue(KRole role) {
		Queue<KTeamPVPInvitation> queue = _invitationMap.get(role.getId());
		if (queue == null) {
			synchronized (role) {
				queue = _invitationMap.get(role.getId());
				if (queue == null) {
					queue = new ConcurrentLinkedQueue<KTeamPVPInvitation>();
					_invitationMap.put(role.getId(), queue);
				}
			}
		}
		return queue;
	}
	
	private static TeamPVPRankElement selectRankElement(int start, int end, KTeamPVPRankTypeEnum rankType) {
		TeamPVPRankElement element;
		for (int i = start; i < end; i++) {
			element = KSupportFactory.getTeamPVPRankSupport().getRankElement(rankType, i);
			if (element != null) {
				return element;
			}
		}
		return null;
	}
	
	private static List<KTeamPVPTeam> getRankTeams(int beginRank, int endRank, KTeamPVPRankTypeEnum rankType) {
		TeamPVPRankElement element;
		List<KTeamPVPTeam> list = new ArrayList<KTeamPVPTeam>();
		KTeamPVPTeam team;
		for (int i = beginRank; i < endRank; i++) {
			element = KSupportFactory.getTeamPVPRankSupport().getRankElement(rankType, i);
			if (element != null) {
				team = getTeamByTeamId(element.elementId);
				if (team != null) {
					list.add(team);
				}
			} else {
				break;
			}
		}
		return list;
	}
	
	private static void resetTeamInfo() {
		KTeamPVPTeam team;
		for (Iterator<KTeamPVPTeam> itr = _allTeams.values().iterator(); itr.hasNext();) {
			team = itr.next();
			if (team != null) {
				try {
					team.reset();
				} catch (Exception e) {
					LOGGER.info("执行team.reset()的时候出现异常，队伍id：{}", team.getId());
				}
				try {
					team.calculateDailyReward();
				} catch (Exception e) {
					LOGGER.info("执行team.calculateDailyReward()的时候出现异常，队伍id：{}", team.getId());
				}
//				try {
//					team.checkMembers();
//				} catch (Exception e) {
//					_LOGGER.info("执行team.checkMembers()的时候出现异常，队伍id：{}", team.getId());
//				}
			}
		}
	}
	
	private static void genKing() {
		// 产生最强王者
		int maxRank = KTeamPVPConfig.getMaxKingCount() + 1;
//		List<KTeamPVPTeam> teamList = new ArrayList<KTeamPVPTeam>();
//		teamList.addAll(getRankTeams(1, maxRank, KTeamPVPRankTypeEnum.最强王者));
//		teamList.addAll(getRankTeams(1, maxRank, KTeamPVPRankTypeEnum.钻石));
		List<KTeamPVPTeam> teamList = getRankTeams(1, maxRank, KTeamPVPRankTypeEnum.最强王者);
		Collections.sort(teamList, _SCORE_SORTER);
		int maxCount = KTeamPVPConfig.getMaxKingCount();
		KTeamPVPTeam team;
//		boolean down = true;
//		if(teamList.size() < maxCount) {
//			maxCount = teamList.size();
//			down = false;
//		}
//		for(int i = 0; i < maxCount; i++) {
//			team = teamList.get(i);
//			team.notifyKingStatus(true);
//			_LOGGER.info("当选最强王者：{},{}", team.getId(), team.getTeamName());
//		}
		if (teamList.size() > maxCount) {
			int end = teamList.size();
			for (int i = maxCount; i < end; i++) {
				team = teamList.get(i);
				team.notifyLoseInKingElection();
				LOGGER.info("落选最强王者：{},{}", team.getId(), team.getTeamName());
			}
		}
	}
	
	public static void init(String path) throws Exception {
		Document doc = XmlUtil.openXml(path);
		Element root = doc.getRootElement();
		@SuppressWarnings("unchecked")
		List<Element> tableConfigs = root.getChild("tableConfig").getChildren();
		Element child;
		KTableInfo tableInfo;
		Map<Byte, KTableInfo> tableMap = new HashMap<Byte, KTableInfo>();
		for(int i = 0; i < tableConfigs.size(); i++) {
			child = tableConfigs.get(i);
			tableInfo = new KTableInfo(Byte.parseByte(child.getAttributeValue("type")), child.getAttributeValue("name"), Integer.parseInt(child.getAttributeValue("headerIndex")));
			tableMap.put(tableInfo.tableType, tableInfo);
		}
		String excelPath = root.getChildTextTrim("excelDataPath");
		KGameExcelFile file = new KGameExcelFile(excelPath);
		KGameExcelRow[] allRows = KGameUtilTool.getAllDataRows(file, tableMap, ITeamPVPTableConfig.TABLE_DAN_BASIC_DATA);
		KGameExcelRow currentRow;
		
		{
			// 加载段位数据 BEGIN
//			Map<Integer, KGameExcelRow> basicDataRowMap = new HashMap<Integer, KGameExcelRow>();
//			for (int i = 0; i < allRows.length; i++) {
//				currentRow = allRows[i];
//				basicDataRowMap.put(currentRow.getInt("danGradeId"), currentRow);
//			}
//			allRows = KGameUtilTool.getAllDataRows(file, tableMap, ITeamPVPTableConfig.TABLE_DAN_INCREASE_ATTR);
			KTeamPVPDanData danData;
			List<Integer> danIds = new ArrayList<Integer>();
//			for (int i = 0; i < allRows.length; i++) {
//				currentRow = allRows[i];
//				danData = new KTeamPVPDanData(basicDataRowMap.get(currentRow.getInt("danGradeId")), currentRow);
//				_danDataMap.put(danData.danGradeId, danData);
//				danIds.add(danData.danGradeId);
//			}
			for (int i = 0; i < allRows.length; i++) {
				currentRow = allRows[i];
				danData = new KTeamPVPDanData(currentRow);
				_danDataMap.put(danData.danGradeId, danData);
				danIds.add(danData.danGradeId);
			}
			KTeamPVPDanType.init(danIds);
			// END
		}
		
		{
			allRows = KGameUtilTool.getAllDataRows(file, tableMap, ITeamPVPTableConfig.TABLE_DAN_INCREASE_ATTR);
			Map<Integer, KGameExcelRow> incAttrRowMap = new HashMap<Integer, KGameExcelRow>();
			for (int i = 0; i < allRows.length; i++) {
				currentRow = allRows[i];
				incAttrRowMap.put(currentRow.getInt("danRankId"), currentRow);
			}
			// 加载段位阶级数据 BEGIN
			allRows = KGameUtilTool.getAllDataRows(file, tableMap, ITeamPVPTableConfig.TABLE_DAN_RANK_UPGRADE_INFO);
			KTeamPVPDanStageInfo rankInfo;
			int last = allRows.length - 1;
			int lastSecond = allRows.length - 2;
			for (int i = 0; i < allRows.length; i++) {
				currentRow = allRows[i];
				rankInfo = new KTeamPVPDanStageInfo(currentRow, incAttrRowMap.get(currentRow.getInt("danRankId")), (i == last ? KDanStageType.KING : (i == lastSecond ? KDanStageType.HIGHEST : KDanStageType.COMMON)));
				_danRankInfoMap.put(rankInfo.danStageId, rankInfo);
				if(i == 0) {
					KTeamPVPConfig.setFirstDanRankId(rankInfo.danStageId);
				}
			}
			// 加载段位阶级数据 END
		}
		
		{
			// 加载挑战信息数据 BEGIN
			allRows = KGameUtilTool.getAllDataRows(file, tableMap, ITeamPVPTableConfig.TABLE_CHALLENGE_MATCH_RULE);
			Map<Integer, Map<Integer, KGameExcelRow>> challengeMatchRuleRoleMap = new HashMap<Integer, Map<Integer, KGameExcelRow>>();
			Map<Integer, KGameExcelRow> tempMatchMap;
			for (int i = 0; i < allRows.length; i++) {
				currentRow = allRows[i];
				int stageId = currentRow.getInt("danRankId");
				tempMatchMap = challengeMatchRuleRoleMap.get(stageId);
				if (tempMatchMap == null) {
					tempMatchMap = new HashMap<Integer, KGameExcelRow>();
					challengeMatchRuleRoleMap.put(stageId, tempMatchMap);
				}
				tempMatchMap.put(currentRow.getInt("challengeTimes"), currentRow);
			}
			
			allRows = KGameUtilTool.getAllDataRows(file, tableMap, ITeamPVPTableConfig.TABLE_CHALLENGE_SCORE_RESULT);
			int challengeTime;
			int danStageId;
			Map<Integer, KTeamPVPChallengeInfo> map;
			for (int i = 0; i < allRows.length; i++) {
				currentRow = allRows[i];
				challengeTime = currentRow.getInt("challengeTimes");
//				challengeScoreResultRowMap.put(currentRow.getInt("challengeTimes"), challengeMatchRuleRoleMap.get(challengeTime));
				danStageId = currentRow.getInt("danRankId");
				map = _challengeInfoMap.get(danStageId);
				if(map == null) {
					map = new HashMap<Integer, KTeamPVPChallengeInfo>();
					_challengeInfoMap.put(danStageId, map);
				}
				map.put(challengeTime, new KTeamPVPChallengeInfo(currentRow, challengeMatchRuleRoleMap.get(danStageId).get(challengeTime)));
				Integer maxChallengeRewardTime = _maxChallengeRewardTimes.get(danStageId);
				if(maxChallengeRewardTime == null || maxChallengeRewardTime < challengeTime) {
					_maxChallengeRewardTimes.put(danStageId, challengeTime);
				}
			}
			// END
		}
		
		{
			// 加载机器人属性 BEGIN
			
			// 按照职业加载技能列表 BEGIN
			allRows = KGameUtilTool.getAllDataRows(file, tableMap, ITeamPVPTableConfig.TABLE_ROBOT_SKILL_CONFIG);
			Map<KJobTypeEnum, int[]> skillTemplateIdMap = new HashMap<KJobTypeEnum, int[]>(); // key=职业类型，value=技能模板id
			for (int i = 0; i < allRows.length; i++) {
				currentRow = allRows[i];
				KJobTypeEnum job = KJobTypeEnum.getJob(currentRow.getByte("jobType"));
				skillTemplateIdMap.put(job, UtilTool.getStringToIntArray(currentRow.getData("script"), ";"));
			}
			// 按照职业加载技能列表 END
			
			// 按照等级加载技能等级 BEGIN
			allRows = KGameUtilTool.getAllDataRows(file, tableMap, ITeamPVPTableConfig.TABLE_ROBOT_SKILL_LEVEL);
			Map<int[], int[]> skillLevelMap = new HashMap<int[], int[]>(); // key={最小等级, 最大等级}，value=技能等级数组
			Map<int[], String> petDataMap = new HashMap<int[], String>();
			Map<int[], String> mountDataMap = new HashMap<int[], String>();
			for (int i = 0; i < allRows.length; i++) {
				currentRow = allRows[i];
				int[] key = new int[] { currentRow.getInt("minLevel"), currentRow.getInt("maxLevel") };
				skillLevelMap.put(key, UtilTool.getStringToIntArray(currentRow.getData("script"), ";"));
				petDataMap.put(key, currentRow.getData("pet"));
				mountDataMap.put(key, currentRow.getData("mecha"));
			}
			// 按照职业加载技能等级 END

			allRows = KGameUtilTool.getAllDataRows(file, tableMap, ITeamPVPTableConfig.TABLE_ROBOT_ATTRIBUTE);
			int challengeTime;
			Map<KJobTypeEnum, List<KTeamPVPRobotTemplate>> tempMap;
			Map.Entry<KJobTypeEnum, List<KTeamPVPRobotTemplate>> entry;
			Map.Entry<int[], int[]> skillLevelEntry;
			int[] skillLevels;
			String petData;
			String mountData;
			for (int i = 0; i < allRows.length; i++) {
				currentRow = allRows[i];
				challengeTime = currentRow.getInt("challengeTimes");
				tempMap = _robotTemplateMap.get(challengeTime);
				petData = null;
				mountData = null;
				if (tempMap == null) {
					tempMap = new HashMap<KJobTypeEnum, List<KTeamPVPRobotTemplate>>();
					for (int k = 0; k < KJobTypeEnum.values().length; k++) {
						tempMap.put(KJobTypeEnum.values()[k], new ArrayList<KTeamPVPRobotTemplate>());
					}
					_robotTemplateMap.put(challengeTime, tempMap);
				}
				for (Iterator<Map.Entry<KJobTypeEnum, List<KTeamPVPRobotTemplate>>> itr = tempMap.entrySet().iterator(); itr.hasNext();) {
					entry = itr.next();
					int minLevel = currentRow.getInt("minLevel");
					int maxLevel = currentRow.getInt("maxLevel");
					int[] skillIds = skillTemplateIdMap.get(entry.getKey());
					skillLevels = null;
					for (Iterator<Map.Entry<int[], int[]>> itr2 = skillLevelMap.entrySet().iterator(); itr2.hasNext();) {
						// 找到对应等级的技能等级
						skillLevelEntry = itr2.next();
						if (skillLevelEntry.getKey()[0] == minLevel && skillLevelEntry.getKey()[1] == maxLevel) {
							skillLevels = skillLevelEntry.getValue();
							petData = petDataMap.get(skillLevelEntry.getKey());
							mountData = mountDataMap.get(skillLevelEntry.getKey());
							break;
						}
					}
					entry.getValue().add(new KTeamPVPRobotTemplate(currentRow, skillIds, skillLevels, petData, mountData, entry.getKey()));
				}
			}
			// END
		}
		
		{
			// 加载荣誉奖励数据 BEGIN
			allRows = KGameUtilTool.getAllDataRows(file, tableMap, ITeamPVPTableConfig.TABLE_CHALLENGE_HONOR_REWARD);
			for (int i = 0; i < allRows.length; i++) {
				currentRow = allRows[i];
				_challengeHonorRewardMap.put(currentRow.getInt("lv"), new int[] { currentRow.getInt("winpower"), currentRow.getInt("losepower") });
			}
			// END
		}
		
		{
			// 加载每日经验奖励数据 BEGIN
			allRows = KGameUtilTool.getAllDataRows(file, tableMap, ITeamPVPTableConfig.TABLE_DAILY_EXP_REWARD);
			for (int i = 0; i < allRows.length; i++) {
				currentRow = allRows[i];
				_dailyExpRewardMap.put(currentRow.getInt("lv"), currentRow.getInt("lvexp"));
			}
			// END
		}
		
		{
			KTeamPVPConfig.init(root.getChild("excelConfigPath"), tableMap.get(ITeamPVPTableConfig.TABLE_PARA_CONFIG));
		}
		
		{
			Element eRandomName = root.getChild("randomName");
			KTeamPVPRandomName.init(eRandomName.getChildTextTrim("malePath"), eRandomName.getChildTextTrim("femalePath"), eRandomName.getChildTextTrim("teamNamePath"));
		}
	}
	
	static void saveAlLTeam() {
		List<DBGameExtCA> updateList = new ArrayList<DBGameExtCA>();
		List<DBGameExtCA> insertList = new ArrayList<DBGameExtCA>();
		List<Long> deleteList = new ArrayList<Long>();
		int original = 0;
		KTeamPVPTeam tempTeam;
		DBGameExtCA dbExtCA;
		for(Iterator<KTeamPVPTeam> itr = _allTeams.values().iterator(); itr.hasNext();) {
			tempTeam = itr.next();
			if(tempTeam.getDataStatus().isDirty()) {
				original = tempTeam.getDataStatus().getStatus();
				try {
					dbExtCA = tempTeam.save();
					switch(original) {
					case DataStatus.STATUS_INSERT:
						insertList.add(dbExtCA);
						break;
					case DataStatus.STATUS_UPDATE:
						updateList.add(dbExtCA);
						break;
					}
					tempTeam.getDataStatus().changeToNone(original);
				} catch (Exception e) {
					LOGGER.error("保存竞技队伍数据出错！队伍id：{}", tempTeam.getId());
				}
			}
		}
		if(_removeTeams.size() > 0) {
			for(Iterator<KTeamPVPTeam> itr = _removeTeams.iterator(); itr.hasNext();) {
				deleteList.add(itr.next().getId());
				itr.remove();
			}
		}
		if(insertList.size() > 0) {
			try {
				DataAccesserFactory.getGameExtCADataAccesser().addDBGameExtCAs(insertList);
			} catch (KGameDBException e) {
				LOGGER.error("保存新增战队到数据库的时候出现异常！", e);
			}
		}
		if (updateList.size() > 0) {
			try {
				DataAccesserFactory.getGameExtCADataAccesser().updateDBGameExtCAs(updateList);
			} catch (KGameDBException e) {
				LOGGER.error("更新战队数据到数据库的时候出现异常！", e);
			}
		}
		if (deleteList.size() > 0) {
			try {
				DataAccesserFactory.getGameExtCADataAccesser().deleteDBGameExtCAs(deleteList);
			} catch (KGameDBException e) {
				LOGGER.error("通知数据库删除战队数据的时候出现异常！", e);
			}
		}
	}
	
	static void loadAllTeam() {
		List<DBGameExtCA> list = null;
		try {
			list = DataAccesserFactory.getGameExtCADataAccesser().getDBGameExtCA(KGameExtDataDBTypeEnum.战队竞技.dbType);
		} catch (KGameDBException e) {
			LOGGER.error("读取战队数据的时候出现异常！", e);
		}
		KTeamPVPTeam team;
		List<Long> invitationIds;
		List<Long> invitationTeamIds;
		long tempRoleId;
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				try {
					team = new KTeamPVPTeam(list.get(i));
				} catch (Exception e) {
					LOGGER.error("解析队伍数据的时候出现异常！", e);
					continue;
				}
				putTeamToCollection(team, true);
				invitationIds = team.getInviteRoleIds();
				if (invitationIds.size() > 0) {
					for (int k = 0; k < invitationIds.size(); k++) {
						tempRoleId = invitationIds.get(k);
						invitationTeamIds = _invitationIdMap.get(tempRoleId);
						if (invitationTeamIds == null) {
							invitationTeamIds = new ArrayList<Long>();
							_invitationIdMap.put(tempRoleId, invitationTeamIds);
						}
						invitationTeamIds.add(team.getId());
					}
				}
			}
		}
	}
	
	static void notifyDayChange() {
		try {
			resetTeamInfo();
		} catch (Exception e) {
			LOGGER.error("执行resetTeamInfo()出现异常！", e);
		}
		genKing();
	}
	
	static void removeTeam(KTeamPVPTeam team) {
		_allTeams.remove(team.getId());
		long[] roleIds = team.getAllMemberIds();
		for (int i = 0; i < roleIds.length; i++) {
			_roleIdToTeamId.remove(roleIds[i]);
		}
		if (team.getDataStatus().getStatus() != DataStatus.STATUS_INSERT) {
			_removeTeams.add(team);
		}
		_teamNames.remove(team.getTeamName());
		KSupportFactory.getTeamPVPRankSupport().notifyTeampDelete(team.getId());
	}
	
	static ITeamPVPTeam selectChallenger(int challengeTimes, KTeamPVPTeam team) {
		int maxChallengeTime = _maxChallengeRewardTimes.get(team.getDanStageInfo().danStageId);
		if(challengeTimes > maxChallengeTime) {
			challengeTimes = maxChallengeTime;
		}
		KTeamPVPChallengeInfo info = _challengeInfoMap.get(team.getDanStageInfo().danStageId).get(challengeTimes);
		if (info != null) {
			TeamPVPRankElement element = null;
			int rank = KSupportFactory.getTeamPVPRankSupport().checkRank(team.getId());
			KTeamPVPRankTypeEnum rankType = getRankType(team.getDanData().danGradeId);
			int end = rank - info.matchMaxRanking;
			int start = rank - info.matchMinRanking;
			if (start > end) {
				int temp = start;
				start = end;
				end = temp;
			}
			if (start < 0 && end > 0) {
				start = 0;
			}
			if (start > 0) {
				if (element == null) {
					switch (rankType) {
					case 最强王者:
						if (start > KTeamPVPConfig.getMaxKingCount()) {
							element = selectRankElement(start, end, KTeamPVPRankTypeEnum.钻石);
						} else {
							element = selectRankElement(start, end, KTeamPVPRankTypeEnum.最强王者);
							if (element == null) {
								element = selectRankElement(start, end, KTeamPVPRankTypeEnum.钻石);
							}
						}
						break;
					default:
						element = selectRankElement(start, end, rankType);
						break;
					}
				}
			}
			if (element != null) {
				KTeamPVPTeam selectedTeam = _allTeams.get(element.elementId);
				return selectedTeam;
			} else {
				Map<KJobTypeEnum, List<KTeamPVPRobotTemplate>> robotMap = _robotTemplateMap.get(challengeTimes);
				KJobTypeEnum[] allJobs = KJobTypeEnum.values();
				KJobTypeEnum job;
				List<KTeamPVPRobotTemplate> list;
				List<String> fashionResList;
				KTeamPVPRobotTemplate temp;
				KTeamPVPRobotTeam selectedTeam = null;
				List<ITeamPVPTeamMember> selected = null;
				String fashionRes;
				if (_reusableRobotTeams.size() > 0) {
					selectedTeam = _reusableRobotTeams.poll();
				}
				boolean reuse = false;
				if (selectedTeam != null) {
					selected = selectedTeam.getTeamMembers();
					reuse = true;
				} else {
					selected = new ArrayList<ITeamPVPTeamMember>();
				}
				for (int i = KTeamPVPConfig.getMaxTeamMemberCount(); i-- > 0;) {
					job = allJobs[UtilTool.random(allJobs.length)];
					list = robotMap.get(job);
					fashionResList = _fashionResByJob.get(job.getJobType());
					fashionRes = fashionResList.get(UtilTool.random(fashionResList.size()));
					for (int k = 0; k < list.size(); k++) {
						temp = list.get(k);
						if (temp.minLevel <= team.getTeamLevel() && temp.maxLevel >= team.getTeamLevel()) {
							KTeamPVPRobot robot;
							if (reuse) {
								robot = (KTeamPVPRobot) selected.get(i);
								robot.init(temp, fashionRes);
							} else {
								robot = new KTeamPVPRobot(temp, fashionRes);
								selected.add(robot);
							}
							KTeamPVPRobotGroup group = _robotGroupMap.get(robot.getId());
							if (group == null) {
								group = new KTeamPVPRobotGroup();
								group.setRoleData(robot);
								_robotGroupMap.put(robot.getId(), group);
							}
							List<Integer> petInfo = temp.getPetInfo();
							if (petInfo != null) {
								if(group.getPetData() != null) {
									group.getPetData().init(petInfo.get(0), petInfo.get(1));
								} else {
									KTeamPVPRobotPet robotPet = new KTeamPVPRobotPet(petInfo.get(0), petInfo.get(1));
									group.setPetData(robotPet);
								}
								group.getPetData().setOwnerId(robot.getId());
								group.setPetStatus(true);
							} else {
								group.setPetStatus(false);
							}
							
							List<Integer> mountInfo = temp.getMountInfo();
							if (mountInfo != null) {
								if (group.getMountData() != null) {
									group.getMountData().init(mountInfo.get(0), mountInfo.get(1));
								} else {
									KTeamPVPRobotMount robotMount = new KTeamPVPRobotMount(mountInfo.get(0), mountInfo.get(1));
									group.setMountData(robotMount);
								}
								group.setMountStatus(true);
							} else {
								group.setMountStatus(false);
							}
							break;
						}
					}
				}
				if(selected.isEmpty()) {
					LOGGER.error("匹配不到任何队伍！队伍id：{}，队伍等级：{}，挑战次数：{}", team.getId(), team.getTeamLevel(), challengeTimes);
					return null;
				}
				if (selectedTeam == null) {
					selectedTeam = new KTeamPVPRobotTeam((KTeamPVPRobot) selected.get(0), (KTeamPVPRobot) selected.get(1));
				} else {
					selectedTeam.resetName();
				}
				_allRobotTeams.put(selectedTeam.getId(), selectedTeam);
				long[] ids = selectedTeam.getAllMemberIds();
				for(int i = ids.length; i-- > 0;) {
					_robotIdToTeamId.put(ids[i], selectedTeam.getId());
				}
				return selectedTeam;
			}
		} else {
			return null;
		}
	}
	
	static ICombatMirrorDataGroup getRobotGroup(long robotId) {
		return _robotGroupMap.get(robotId);
	}
	
	static void removeRobotTeam(long teamId) {
		KTeamPVPRobotTeam robotTeam = _allRobotTeams.remove(teamId);
		if (robotTeam != null) {
			_reusableRobotTeams.offer(robotTeam);
			long[] ids = robotTeam.getAllMemberIds();
			for(int i = 0; i < ids.length; i++) {
				_robotIdToTeamId.remove(ids[i]);
			}
		}
	}
	
	static void onRoleJoinTeam(long roleId, KTeamPVPTeam team) {
		_roleIdToTeamId.put(roleId, team.getId());
	}
	
	static void onRoleQuitTeam(long roleId, KTeamPVPTeam team) {
		_roleIdToTeamId.remove(roleId, team.getId());
	}
	
	static KTeamPVPDanStageInfo getNextDanRank(int danRankId) {
		Map.Entry<Integer, KTeamPVPDanStageInfo> entry;
		for (Iterator<Map.Entry<Integer, KTeamPVPDanStageInfo>> itr = _danRankInfoMap.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			if (entry.getKey() == danRankId) {
				if (itr.hasNext()) {
					return itr.next().getValue();
				} else {
					return null;
				}
			}
		}
		throw new RuntimeException("_danRankInfoMap.zie()==0");
	}
	
	static KTeamPVPDanStageInfo getPreDanRank(int danRankId) {
		Map.Entry<Integer, KTeamPVPDanStageInfo> entry;
		KTeamPVPDanStageInfo pre = null;
		for (Iterator<Map.Entry<Integer, KTeamPVPDanStageInfo>> itr = _danRankInfoMap.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			if (entry.getKey() == danRankId) {
				return pre;
			} else {
				pre = entry.getValue();
			}
		}
		throw new RuntimeException("_danRankInfoMap.zie()==0");
	}
	
	static KTeamPVPDanData getNextDanData(int danGradeId) {
		Map.Entry<Integer, KTeamPVPDanData> entry;
		for (Iterator<Map.Entry<Integer, KTeamPVPDanData>> itr = _danDataMap.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			if (entry.getKey() == danGradeId) {
				if (itr.hasNext()) {
					return itr.next().getValue();
				} else {
					return null;
				}
			}
		}
		throw new RuntimeException("_danRankInfoMap.zie()==0");
	}
	
	static KTeamPVPRankTypeEnum getRankType(int danId) {
		KTeamPVPDanType danType = KTeamPVPDanType.getDanType(danId);
		switch (danType) {
		default:
		case 青铜:
			return KTeamPVPRankTypeEnum.青铜;
		case 白金:
			return KTeamPVPRankTypeEnum.白金;
		case 白银:
			return KTeamPVPRankTypeEnum.白银;
		case 钻石:
			return KTeamPVPRankTypeEnum.钻石;
		case 黄金:
			return KTeamPVPRankTypeEnum.黄金;
		case 最强王者:
			return KTeamPVPRankTypeEnum.最强王者;
		}
	}
	
	static void processInvitation(KRole role) {
		List<Long> teamIds = _invitationIdMap.remove(role.getId());
		if (teamIds != null) {
			Queue<KTeamPVPInvitation> queue = getInvitationQueue(role);
			KTeamPVPTeam team;
			KRole captain;
			for (int i = 0; i < teamIds.size(); i++) {
				team = _allTeams.get(teamIds.get(i));
				if (team != null) {
					if (!team.isTeamFull()) {
						captain = KSupportFactory.getRoleModuleSupport().getRole(team.getAllMemberIds()[0]);
						queue.add(new KTeamPVPInvitation(captain, team));
					} else {
						team.removeInviteRoleId(role.getId());
					}
				}
			}
		}
	}
	
	static void separateFashionResByJob() {
		List<KFashionTemplate> list = KSupportFactory.getFashionModuleSupport().getAllFashionTemplate();
		List<String> resList;
		KFashionTemplate template;
		for (int i = 0; i < list.size(); i++) {
			template = list.get(i);
			resList = _fashionResByJob.get((byte)template.job);
			if (resList == null) {
				resList = new ArrayList<String>();
				_fashionResByJob.put((byte) template.job, resList);
			}
			resList.add(template.res_id);
		}
	}
	
	public static void notifyBattlePowerChange(KRole role, int battlePower) {
		KTeamPVPTeam team = getTeamByRoleId(role.getId());
		if(team != null) {
			team.notifyBattlePowerChange(role.getId(), battlePower);
		}
	}
	
	public static void notifyRoleJoinedGame(KRole role) {
		processInvitation(role);
		Queue<KTeamPVPInvitation> queue = _invitationMap.get(role.getId());
		if (queue != null) {
			KTeamPVPMsgCenter.pushInvitation(queue, role);
		}
		KTeamPVPTeam team = getTeamByRoleId(role.getId());
		if(team != null) {
			KTeamPVPTeamMember member = team.getMember(role.getId());
			member.notifyJoinGame();
		}
	}
	
	public static void notifyRoleLeavedGame(KRole role) {
		KTeamPVPTeam team = getTeamByRoleId(role.getId());
		if(team != null) {
			KTeamPVPTeamMember member = team.getMember(role.getId());
			member.notifyOffline();
		}
	}
	
	public static void notifyRoleDeleted(long roleId) {
		KTeamPVPTeam team = getTeamByRoleId(roleId);
		if(team != null) {
			team.processQuitTeam(roleId, true);
		}
	}
	
	public static void notifyRoleUpgraded(KRole role, int preLv) {
		KTeamPVPTeam team = getTeamByRoleId(role.getId());
		if(team != null) {
			team.notifyMemberLevelUp(role);
		}
	}
	
	public static List<KActionResult<Long>> getCandidateFriendIds(long roleId, KTeamPVPTeam team) {
		List<Long> allFriendIds = KSupportFactory.getRelationShipModuleSupport().getAllFriends(roleId);
		List<KActionResult<Long>> result = new ArrayList<KActionResult<Long>>(allFriendIds.size());
		List<KActionResult<Long>> canAddList = new ArrayList<KActionResult<Long>>(allFriendIds.size());
		if (allFriendIds.size() > 0) {
			List<Long> list = team.getInviteRoleIds();
			Long friendId;
			KRole role;
			KActionResult<Long> temp;
			for (Iterator<Long> itr = allFriendIds.iterator(); itr.hasNext();) {
				friendId = itr.next();
				if (list.contains(friendId)) {
//					itr.remove();
					continue;
				} else {
					temp = new KActionResult<Long>();
					temp.attachment = friendId;
					if (_roleIdToTeamId.containsKey(friendId)) {
//						itr.remove();
						temp.success = false;
						temp.tips = CompetitionTips.getTipsAlreadyJoinOtherTeamLabel();
					} else {
						role = KSupportFactory.getRoleModuleSupport().getRole(friendId);
						if (role == null || role.getLevel() < KTeamPVPConfig.getJoinMinLevel()) {
//							itr.remove();
							temp.success = false;
							temp.tips = CompetitionTips.getTipsLevelNotReachLabel();
						} else {
							temp.success = true;
						}
					}
					if(temp.success) {
						canAddList.add(temp);
					} else {
						result.add(temp);
					}
				}
			}
		}
		result.addAll(canAddList);
		Collections.reverse(result);
		return result;
	}
	
	public static boolean isInTeam(long roleId) {
		return _roleIdToTeamId.containsKey(roleId);
	}
	
	public static boolean isNameExists(String name) {
		return _teamNames.contains(name);
	}
	
	public static KTeamPVPTeam getTeamByTeamId(long teamId) {
		return _allTeams.get(teamId);
	}
	
	public static KTeamPVPTeam getTeamByRoleId(long roleId) {
		Long teamId = _roleIdToTeamId.get(roleId);
		if(teamId != null) {
			return _allTeams.get(teamId);
		}
		return null;
	}
	
	public static long getTeamIdByRoleId(long roleId) {
		Long teamId = _roleIdToTeamId.get(roleId);
		if(teamId != null) {
			return teamId;
		}
		return 0;
	}
	
	public static void onGameWorldInitComplete() throws KGameServerException {
		int exCount = 0;
		for (Iterator<Map<KJobTypeEnum, List<KTeamPVPRobotTemplate>>> itr = _robotTemplateMap.values().iterator(); itr.hasNext();) {
			Map<KJobTypeEnum, List<KTeamPVPRobotTemplate>> entry = itr.next();
			for (Iterator<List<KTeamPVPRobotTemplate>> itr2 = entry.values().iterator(); itr2.hasNext();) {
				List<KTeamPVPRobotTemplate> list = itr2.next();
				for (int i = 0; i < list.size(); i++) {
					try {
						list.get(i).onGameWorldInitComplete();
					} catch (Exception e) {
						LOGGER.error("", e);
						exCount++;
					}
				}
			}
		}
		if (exCount > 0) {
			throw new KGameServerException("天梯赛系统，onGameWorldInitComplete()异常！");
		}
		KTeamPVPTask task = new KTeamPVPTask();
		task.start();
		KSupportFactory.getVIPModuleSupport().addVipUpLvListener(new KTeamPVPVipEventListener());
		separateFashionResByJob();
		MainMenuFunction function = KGuideManager.getMainMenuFunctionInfoMap().get(KFunctionTypeEnum.队伍竞技.functionId);
		KTeamPVPConfig.setMinJoinLevel(function.getOpenRoleLevelLimit());
	}
	
	public static void notifyCacheLoadComplete() {
		loadAllTeam();
	}
	
	public static void shutdown() {
		saveAlLTeam();
	}
	
	public static int getRewardHonor(int lv, boolean getWin) {
		int[] values = _challengeHonorRewardMap.get(lv);
		if (values != null) {
			return getWin ? values[0] : values[1];
		}
		return 0;
	}
	
	public static int getDailyExpReward(int lv) {
		Integer value = _dailyExpRewardMap.get(lv);
		if(value != null) {
			return value;
		}
		return 0;
	}
	
	public static KTeamPVPDanStageInfo getDanStageInfo(int danRankId) {
		return _danRankInfoMap.get(danRankId);
	}
	
	public static KTeamPVPDanData getDanData(int danGradeId) {
		return _danDataMap.get(danGradeId);
	}
	
	public static KTeamPVPRobotTeam getRobotTeam(long teamId) {
		return _allRobotTeams.get(teamId);
	}
	
	public static KTeamPVPRobotTeam getRobotTeamByRobotId(long robotId) {
		Long teamId = _robotIdToTeamId.get(robotId);
		if (teamId != null) {
			return _allRobotTeams.get(teamId);
		} else {
			return null;
		}
	}
	
	public static KActionResult<KTeamPVPTeam> createCompetitionTeam(String teamName, KRole captain) {
		KActionResult<KTeamPVPTeam> result = new KActionResult<KTeamPVPTeam>();
		boolean addSuccess = false;
		synchronized (teamName) {
			if (!_teamNames.contains(teamName)) {
				_teamNames.add(teamName);
				addSuccess = true;
			}
		}
		if(addSuccess) {
			KTeamPVPTeam team = new KTeamPVPTeam(teamName, captain);
			putTeamToCollection(team, false);
			removeInvitation(0, captain, true);
			result.success = true;
			result.attachment = team;
			KTeamPVPRankTypeEnum type = getRankType(team.getDanData().danGradeId);
			KSupportFactory.getTeamPVPRankSupport().notifyTempChange(team.getId(), team.getTeamName(), type, team.getDanStageInfo().level, team.getCurrentScore(), team.getTeamBattlePower(), captain.getId(), captain.getName(), captain.getVipLevel(), 0, "", 0);
			FlowManager.logOther(captain.getId(), OtherFlowTypeEnum.战队竞技, CompetitionTips.getTipsFlowCreateTeam(captain.getName(), team.getTeamName(), team.getUUID()));
		} else {
			result.tips = CompetitionTips.getTipsDuplicateTeamName();
		}
		return result;
	}
	
	public static KTeamPVPInvitation createInvitation(KRole invitor, KTeamPVPTeam team, KRole friend) {
		Queue<KTeamPVPInvitation> queue = getInvitationQueue(friend);
		KTeamPVPInvitation invitation = new KTeamPVPInvitation(invitor, team);
		queue.add(invitation);
		team.addInviteRoleId(friend.getId());
		return invitation;
	}
	
	public static void removeInvitation(long teamId, KRole role, boolean removeAll) {
		Queue<KTeamPVPInvitation> queue = getInvitationQueue(role);
		KTeamPVPTeam team;
		if (removeAll) {
			for (Iterator<KTeamPVPInvitation> itr = queue.iterator(); itr.hasNext();) {
				team = getTeamByTeamId(itr.next().teamId);
				if (team != null) {
					team.removeInviteRoleId(role.getId());
				}
				itr.remove();
			}
		} else {
			for (Iterator<KTeamPVPInvitation> itr = queue.iterator(); itr.hasNext();) {
				if (teamId == itr.next().teamId) {
					itr.remove();
					break;
				}
			}
		}
	}
	
	
	public static void processCombatFinish(long roleId, ICombatResult result) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		KTeamPVPTeam team = getTeamByRoleId(roleId);
		ICombatRoleResult roleResult = result.getRoleResult(roleId);
		int challengeTime = (Integer) result.getAttachment();
		int[] honorResult;
		if (challengeTime > KTeamPVPConfig.getMaxGetHonorRewardTimes()) {
			honorResult = _defaultHonorReward;
		} else {
			honorResult = _challengeHonorRewardMap.get(role.getLevel());
		}
		if (team != null) {
			Map<Integer, KTeamPVPChallengeInfo> map = _challengeInfoMap.get(team.getDanStageInfo().danStageId);
			KTeamPVPChallengeInfo info = map.get(challengeTime);
			if (info == null) {
				info = map.get(KTeamPVPConfig.getMaxGetHonorRewardTimes());
			}
			int honor;
			int score;
			if (result.isRoleWin()) {
				honor = honorResult[0];
				score = info.winIncScore;
			} else {
				honor = honorResult[1];
				score = info.loseDecScore;
			}
			if (honor > 0) {
				honor = Math.round(honor * info.honorWeight);
				int gangHonorPct = KSupportFactory.getGangSupport().getGangEffect(KGangTecTypeEnum.竞技场天梯赛荣誉产出, role.getId());
				if (gangHonorPct > 0) {
					// 军团科技加成
					honor += UtilTool.calculateTenThousandRatio(honor, gangHonorPct);
				}
			}
			boolean pre = team.isInPromoteFighting();
			int preStageId = team.getDanStageInfo().danStageId;
			team.processCombatFinish(role, score, result.isRoleWin());
			boolean now = team.isInPromoteFighting();
			int nowStageId = team.getDanStageInfo().danStageId;
			KSupportFactory.getCurrencySupport().increaseMoney(roleId, KCurrencyTypeEnum.SCORE, honor, PresentPointTypeEnum.战队竞技奖励, true);
			KTeamPVPMsgCenter.sendCombatResultMsg(role, result.isRoleWin(), result.getTotalCombatTime(), honor, score);
			KTeamPVPMsgCenter.sendUpdateTeamInfo(team);
			if(roleResult.isEscape()) {
				KDialogService.sendNullDialog(role);
			} //else {
				String notice = null;
				boolean promote = false;
				if (!pre && now) {
					notice = CompetitionTips.getTipsPrepareForPromoteNotice(team.getDanStageInfo().promoteFightCount);
				} else if (preStageId < nowStageId) {
					String preName = getDanStageInfo(preStageId).danStageName;
					notice = CompetitionTips.getTipsPromoteSuccessNotice(preName, team.getDanStageInfo().danStageName);
					promote = true;
				} else if (pre && !now) {
					notice = CompetitionTips.getTipsPromoteFailNotice();
				} else if (preStageId > nowStageId) {
					String preName = getDanStageInfo(preStageId).danStageName;
					notice = CompetitionTips.getTipsDemoteNotice(preName, team.getDanStageInfo().danStageName);
				}
				if(notice != null) {
					KTeamPVPMsgCenter.sendUpdateNotice(roleId, notice);
					long[] memberIds = team.getAllMemberIds();
					if(memberIds.length > 1) {
						long mateId = memberIds[0];
						if(mateId == roleId) {
							mateId = memberIds[1];
							KTeamPVPTeamMember member = team.getMember(mateId);
							member.setNotice(notice);
						}
					}
				}
				if(promote) {
					KSupportFactory.getChatSupport().sendSystemChat(StringUtil.format(KWordBroadcastType.天梯赛_XXX战队在天梯赛中段位进阶到XX.content, team.getTeamName(), team.getDanStageInfo().danStageName), KWordBroadcastType.天梯赛_XXX战队在天梯赛中段位进阶到XX);
				}
				if(result.isRoleWin()) {
					int ctnWinCount = team.getCtnWinCount();
					if(ctnWinCount % KTeamPVPConfig.getBroadcastCtnWinTimes() == 0) {
						KSupportFactory.getChatSupport().sendSystemChat(StringUtil.format(KWordBroadcastType.天梯赛_XXX战队在天梯赛中连胜X场.content, team.getTeamName(), ctnWinCount), KWordBroadcastType.天梯赛_XXX战队在天梯赛中连胜X场);
					}
				}
		} else {
			// 可能在战斗中被人踢出退伍
			int honor;
			if(roleResult.isWin()) {
				honor = honorResult[0];
			} else {
				honor = honorResult[1];
			}
			KSupportFactory.getCurrencySupport().increaseMoney(roleId, KCurrencyTypeEnum.SCORE, honor, PresentPointTypeEnum.战队竞技奖励, true);
			KTeamPVPMsgCenter.sendCombatResultMsg(role, result.isRoleWin(), result.getTotalCombatTime(), honor, 0);
		}
	}
	
	public static KTeamPVPRoleRecordData getTeamPVPRecordData(long roleId) {
		KActivityRoleExtData extData = KActivityRoleExtCaCreator.getActivityRoleExtData(roleId);
		return extData.getTeamPVPData();
	}
	
}
