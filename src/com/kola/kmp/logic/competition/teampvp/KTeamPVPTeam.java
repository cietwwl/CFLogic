package com.kola.kmp.logic.competition.teampvp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.koala.game.KGameMessage;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.DataStatus;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.util.UUIDGenerator;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kgame.db.DataIdGeneratorFactory;
import com.kola.kgame.db.dataobject.DBGameExtCA;
import com.kola.kgame.db.dataobject.impl.DBGameExtCAImpl;
import com.kola.kmp.logic.combat.ICombatMirrorDataGroup;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.combat.api.ICombatRole;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPDanStageInfo.KDanStageType;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.OtherFlowTypeEnum;
import com.kola.kmp.logic.flow.KRoleAttrModifyType;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KGameExtDataDBTypeEnum;
import com.kola.kmp.logic.rank.teampvp.KTeamPVPRankTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.IRoleEquipShowData;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.CompetitionTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;

/**
 * 
 * @author PERRY CHAN
 */
public class KTeamPVPTeam implements ITeamPVPTeam {
	
	private static final String KEY_TEAM_NAME = "1";
	private static final String KEY_MEMBER_INFO = "2";
	private static final String KEY_WIN_COUNT = "3";
	private static final String KEY_TOTAL_CHALLENGE_COUNT = "4";
	private static final String KEY_CURRENT_SCORE= "5";
	private static final String KEY_IN_PROMOTE_STATUS = "6";
	private static final String KEY_PROMOTE_FIGHT_RESULT = "7";
	private static final String KEY_DEMOTE_FIGHT_COUNT = "8";
	private static final String KEY_INVITE_ROLE_ID = "9";
	private static final String KEY_GOOD_COUNT = "10";
	private static final String KEY_UUID = "11";
	private static final String KEY_SAVE_TIME = "T";
	
	private long _id; // 数据库唯一id
	private String _name; // 队伍的名字
//	private int _danRankId; // 段位阶级id
	private KTeamPVPDanData _danData; // 段位数据
	private KTeamPVPDanStageInfo _danStageData; // 段位阶级数据
	private List<ITeamPVPTeamMember> _members;
	private AtomicInteger _winCount = new AtomicInteger(); // 胜利的次数
	private AtomicInteger _ctnWinCount = new AtomicInteger(); // 连胜的次数
	private int _totalChallengeCount; // 总共挑战次数
	private final AtomicInteger _currentScore = new AtomicInteger(); // 当前的胜点
	private int _teamBattlePower; // 队伍的战斗力
	private final AtomicBoolean _inPromoteFighting = new AtomicBoolean(false);
	private List<Boolean> _promoteFightResult = new ArrayList<Boolean>(); // 晋级赛的结果
	private final AtomicInteger _demoteLoseCount = new AtomicInteger(); // 降级战斗数量
	private int _teamLv;
	private final AtomicInteger _goodCount = new AtomicInteger(); // 被点赞的次数
	private long _lastSaveTime;
	private String _uuid; // 战队的UUID
	
	private List<Long> _inviteRoleIds = new ArrayList<Long>();
	private List<Long> _inviteRoleIdsRO = Collections.unmodifiableList(_inviteRoleIds);
	
	private final ReadWriteLock _historyLock = new ReentrantReadWriteLock();
	private final Lock _demoteLock = new ReentrantLock();
//	private final List<String> _history = new LinkedList<String>(); // 战斗历史记录
	private final List<CommonResult> _history = new LinkedList<CommonResult>(); // 战斗历史记录
	private final DataStatus _dataStatus = new DataStatus(); // 数据状态
	
	
	KTeamPVPTeam() {
		this._members = new ArrayList<ITeamPVPTeamMember>(KTeamPVPConfig.getMaxTeamMemberCount());
	}
	
	public KTeamPVPTeam(String pName, KRole captain) {
		this();
		this._name = pName;
		this._dataStatus.notifyInsert();
		this._id = DataIdGeneratorFactory.getGameExtDataIdGenerator().nextId();
		KTeamPVPTeamMember member = new KTeamPVPTeamMember(captain, this._dataStatus);		
		this._members.add(member);
		this.initComplete(KTeamPVPConfig.getFirstDanRankId(), false);
		this._uuid = UUIDGenerator.generate();
	}
	
	public KTeamPVPTeam(DBGameExtCA extCA) throws Exception {
		this();
		this._id = extCA.getDBId();
		this.parseAttribute(extCA.getAttribute());
		this.initComplete(extCA.getCAType(), true);
	}
	
	private void initComplete(int danStageId, boolean isOldData) {
		_danStageData = KTeamPVPManager.getDanStageInfo(danStageId);
		_danData = KTeamPVPManager.getDanData(_danStageData.danGradeId);
		calculateTeamLevel();
		calculateBattlePower(false);
		if (UtilTool.isBetweenDay(_lastSaveTime, System.currentTimeMillis())) {
			if (isOldData) {
				// 新创建的战队不用reset，因为会重置到挑战次数以及购买次数这些
				this.reset();
			}
			this.calculateDailyReward();
		}
//		checkHighestStageIdOfMember();
	}
	
	private void checkHighestStageIdOfMember(boolean sync) {
		for (int i = 0; i < _members.size(); i++) {
			KTeamPVPTeamMember member = (KTeamPVPTeamMember) _members.get(i);
			if (member._highestStage.danStageId < _danStageData.danStageId) {
				member.setMyHighestStageInfo(_danStageData.danStageId);
				if (sync && member._hasOpen) {
					KTeamPVPMsgCenter.syncMyHighestDanInfo(member);
				}
			}
		}
	}
	
	private void calculateTeamLevel() {
		if (this._members.size() == 1) {
			this._teamLv = this._members.get(0).getLevel();
		} else {
			int highLv = this._members.get(0).getLevel();
			int lowLv = this._members.get(1).getLevel();
			if (highLv < lowLv) {
				int temp = highLv;
				highLv = lowLv;
				lowLv = temp;
			}
			this._teamLv = Math.round(highLv * KTeamPVPConfig.getTeamLvGreaterPara() + lowLv * KTeamPVPConfig.getTeamLvLessPara());
		}
	}
	
	private void calculateBattlePower(boolean sync) {
		int pre = this._teamBattlePower;
		this._teamBattlePower = 0;
		for (int i = 0; i < _members.size(); i++) {
			this._teamBattlePower += ((KTeamPVPTeamMember) _members.get(i))._battlePower;
		}
		if (sync && pre != this._teamBattlePower ) {
			KTeamPVPMsgCenter.syncTeamBattlePowerToClient(this);
		}
		if (pre != _teamBattlePower) {
			KSupportFactory.getTeamPVPRankSupport().resetTeamBattlePower(_id, KTeamPVPManager.getRankType(_danData.danGradeId), _teamBattlePower);
		}
	}
	
	private <T> String getArrayToString(List<T> list, String separator) {
		if (list.size() > 0) {
			T t;
			StringBuilder strBld = new StringBuilder();
			for (int i = 0; i < list.size(); i++) {
				t = list.get(i);
				strBld.append(t).append(separator);
			}
			return strBld.deleteCharAt(strBld.length() - 1).toString();
		} else {
			return "";
		}
	}
	
	private String saveMemberInfo() throws Exception {
		JSONObject obj = new JSONObject();
		for (int i = 0; i < _members.size(); i++) {
			obj.put(String.valueOf(i), ((KTeamPVPTeamMember)this._members.get(i)).saveAttribute());
		}
		return obj.toString();
	}
	
	private String saveAttribute() throws Exception {
		JSONObject obj = new JSONObject();
		obj.put(KEY_TEAM_NAME, this._name);
		obj.put(KEY_MEMBER_INFO, this.saveMemberInfo());
		obj.put(KEY_WIN_COUNT, this._winCount);
		obj.put(KEY_TOTAL_CHALLENGE_COUNT, this._totalChallengeCount);
		obj.put(KEY_CURRENT_SCORE, this._currentScore);
		obj.put(KEY_IN_PROMOTE_STATUS, this._inPromoteFighting.get());
		if(this._inPromoteFighting.get() && this._promoteFightResult.size() > 0) {
			if (this._promoteFightResult.size() == 1) {
				obj.put(KEY_PROMOTE_FIGHT_RESULT, String.valueOf(this._promoteFightResult.get(0)));
			} else {
				obj.put(KEY_PROMOTE_FIGHT_RESULT, getArrayToString(_promoteFightResult, ";"));
			}
		}
		obj.put(KEY_DEMOTE_FIGHT_COUNT, this._demoteLoseCount);
		if(this._inviteRoleIds.size() > 0) {
			if(this._inviteRoleIds.size() == 1) {
				obj.put(KEY_INVITE_ROLE_ID, String.valueOf(_inviteRoleIds.get(0)));
			} else {
				obj.put(KEY_INVITE_ROLE_ID, getArrayToString(_inviteRoleIds, ";"));
			}
		}
		obj.put(KEY_GOOD_COUNT, _goodCount.get());
		obj.put(KEY_UUID, _uuid);
		obj.put(KEY_SAVE_TIME, System.currentTimeMillis());
		return obj.toString();
	}
	
	private void parseAttribute(String attribute) throws Exception {
		//PNEXT
		JSONObject obj = new JSONObject(attribute);
		this._name = obj.getString(KEY_TEAM_NAME);
		this._winCount.set(obj.getInt(KEY_WIN_COUNT));
		this._totalChallengeCount = obj.getInt(KEY_TOTAL_CHALLENGE_COUNT);
		this._currentScore.set(obj.getInt(KEY_CURRENT_SCORE));
		this._demoteLoseCount.set(obj.getInt(KEY_DEMOTE_FIGHT_COUNT));
		this._goodCount.set(obj.optInt(KEY_GOOD_COUNT, 0));
		this._inPromoteFighting.set(obj.optBoolean(KEY_IN_PROMOTE_STATUS, false));
		this._uuid = obj.optString(KEY_UUID, "");
		String promoteFightResult = obj.optString(KEY_PROMOTE_FIGHT_RESULT, null);
		if (promoteFightResult != null) {
			String[] array = promoteFightResult.split(";");
			for (int i = 0; i < array.length; i++) {
				this._promoteFightResult.add(Boolean.parseBoolean(array[i]));
			}
		}
		String inviteRoleIdStr = obj.optString(KEY_INVITE_ROLE_ID, null);
		if(inviteRoleIdStr != null) {
			String[] array = inviteRoleIdStr.split(";");
			for(int i = 0; i < array.length; i++) {
				this._inviteRoleIds.add(Long.parseLong(array[i]));
			}
		}
		JSONObject memberInfoObj = new JSONObject(obj.getString(KEY_MEMBER_INFO));
		String key;
		KTeamPVPTeamMember member;
		for (@SuppressWarnings("unchecked")
		Iterator<String> itr = memberInfoObj.keys(); itr.hasNext();) {
			key = itr.next();
			member = new KTeamPVPTeamMember(memberInfoObj.getString(key), this._dataStatus);
			this._members.add(member);
			if (key.equals("0") && !member.isTeamLeader()) {
				member.setIsLeader(true);
			}
		}
		if (_members.size() > 0) {
			member = (KTeamPVPTeamMember) _members.get(0);
			if (!member.isTeamLeader()) {
				KTeamPVPTeamMember leader = (KTeamPVPTeamMember) _members.get(1);
				_members.set(0, leader);
				_members.set(1, member);
			}
		}
		_lastSaveTime = obj.getLong(KEY_SAVE_TIME);
	}
	
	private void notifyRanking() {
		KTeamPVPTeamMember leader = (KTeamPVPTeamMember) _members.get(0);
		KTeamPVPTeamMember mate = _members.size() > 1 ? (KTeamPVPTeamMember) _members.get(1) : null;
		String mateName;
		long mateId;
		int mateVipLv;
		if (mate != null) {
			mateName = mate._name;
			mateId = mate._roleId;
			mateVipLv = mate._vipLevel;
		} else {
			mateName = "";
			mateId = 0;
			mateVipLv = 0;
		}
		KTeamPVPRankTypeEnum rankTypeEnum = KTeamPVPManager.getRankType(_danData.danGradeId);
		KSupportFactory.getTeamPVPRankSupport().notifyTempChange(_id, _name, rankTypeEnum, _danStageData.level, _currentScore.get(), _teamBattlePower, leader.getId(), leader.getName(), leader.getVipLevel(),
				mateId, mateName, mateVipLv);
	}
	
	private void sendPromoteReward(KTeamPVPDanStageInfo current) {
		String title = CompetitionTips.getTipsPromoteRewardTitle();
		String content = null;
		KTeamPVPTeamMember member;
//		List<ItemCountStruct> items = new ArrayList<ItemCountStruct>();
//		Map<String, Integer> itemMap = new HashMap<String, Integer>();
		KTeamPVPDanStageInfo temp;
//		Map.Entry<String, Integer> entry;
		for (int i = 0; i < _members.size(); i++) {
			member = (KTeamPVPTeamMember)_members.get(i);
			if(current.danStageId > member._highestStage.danStageId) {
				temp = KTeamPVPManager.getDanStageInfo(member._highestStage.danStageId);
				content = CompetitionTips.getTipsPromoteRewardContent(temp.danStageName, current.danStageName);
//				itemMap.clear();
//				items.clear();
//				while (current.danStageId != temp.danStageId) {
//					if(itemMap.isEmpty()) {
//						itemMap.putAll(temp.promoRewardMap);
//					} else {
//						for (Iterator<Map.Entry<String, Integer>> itr = temp.promoRewardMap.entrySet().iterator(); itr.hasNext();) {
//							entry = itr.next();
//							Integer currentCount = itemMap.get(entry.getKey());
//							if (currentCount == null) {
//								itemMap.put(entry.getKey(), entry.getValue());
//							} else {
//								itemMap.put(entry.getKey(), entry.getValue() + currentCount);
//							}
//						}
//					}
//					temp = KTeamPVPManager.getNextDanRank(temp.danStageId);
//				}
//				for (Iterator<Map.Entry<String, Integer>> itr = itemMap.entrySet().iterator(); itr.hasNext();) {
//					entry = itr.next();
//					items.add(new ItemCountStruct(entry.getKey(), entry.getValue()));
//				}
//				KSupportFactory.getMailModuleSupport().sendAttMailBySystem(_members.get(i).getId(), items, title, content);
				KSupportFactory.getMailModuleSupport().sendMoneyMailBySystem(_members.get(i).getId(), Arrays.asList(current.promoDiamondRewardStruct), PresentPointTypeEnum.战队竞技奖励, title, content);
			}
		}
	}
	
	private void notifyDataChangeToAllMembers() {
		KRole role;
		for (int i = 0; i < this._members.size(); i++) {
			role = KSupportFactory.getRoleModuleSupport().getRole(this._members.get(i).getId());
			if (role.isOnline()) {
				KSupportFactory.getTeamPVPSupport().notifyRoleTeamDataChange(role);
			}
		}
	}
	
	private void promote() {
		if (this._inPromoteFighting.compareAndSet(true, false)) {
			// 处于升级战斗才能晋升
			KTeamPVPDanStageInfo nextStageInfo = KTeamPVPManager.getNextDanRank(_danStageData.danStageId);
			boolean danLvUp = false;
			if (nextStageInfo.danGradeId != this._danData.danGradeId) {
				this._danData = KTeamPVPManager.getDanData(nextStageInfo.danGradeId);
				danLvUp = true;
			}
			this._danStageData = nextStageInfo;
			_promoteFightResult.clear();
			_currentScore.getAndSet(0);
			KTeamPVPMsgCenter.notifyRankRefurbish(this, danLvUp); // 通知客户端
			this.notifyRanking(); // 通知排行榜
			this.sendPromoteReward(this._danStageData); // 发送晋升奖励
			this.checkHighestStageIdOfMember(true); // 检查成员最高的等级
//			if (danLvUp) {
//				this.notifyDataChangeToAllMembers();
//			}
			this.notifyDataChangeToAllMembers();
			_dataStatus.notifyUpdate();
			FlowManager.logOther(_members.get(0).getId(), OtherFlowTypeEnum.战队竞技, CompetitionTips.getTipsFlowTeamPromote(_uuid, _danStageData.danStageName));
		}
	}
	
	private void checkForPromote() {
		if (this._promoteFightResult.size() >= _danStageData.promoWinCount) {
			int winCount = 0;
			for (int i = 0; i < _promoteFightResult.size(); i++) {
				if (_promoteFightResult.get(i)) {
					winCount++;
				}
			}
			if (winCount >= _danStageData.promoWinCount) {
				this.promote();
			} else {
				int leftCount = _danStageData.promoteFightCount - _promoteFightResult.size();
				if (leftCount + winCount < _danStageData.promoWinCount) {
					this._currentScore.getAndSet(_danStageData.promoFailScore);
					_inPromoteFighting.set(false);
					String challengeResult = _promoteFightResult.toString().replace(",", "，").replace("[", "（").replace("]", "）");
					_promoteFightResult.clear();
					FlowManager.logOther(_members.get(0).getId(), OtherFlowTypeEnum.战队竞技, CompetitionTips.getTipsFlowTeamPromoteFail(_uuid, _danStageData.danStageName, challengeResult));
				}
			}
		}
	}
	
	private void demote() {
		KTeamPVPDanStageInfo preStageInfo = KTeamPVPManager.getPreDanRank(_danStageData.danStageId);
		boolean danLvUChange = false;
		if (preStageInfo.danGradeId != this._danData.danGradeId) {
			this._danData = KTeamPVPManager.getDanData(this._danStageData.danGradeId);
			danLvUChange = true;
		}
		_currentScore.getAndSet(_danStageData.demoteScore);
		_danStageData = preStageInfo;
		_inPromoteFighting.set(false);
		_demoteLoseCount.getAndSet(0);
		KTeamPVPMsgCenter.notifyRankRefurbish(this, danLvUChange);
		this.notifyRanking();
		if(danLvUChange) {
			this.notifyDataChangeToAllMembers();
		}
		FlowManager.logOther(_members.get(0).getId(), OtherFlowTypeEnum.战队竞技, CompetitionTips.getTipsFlowTeamDemote(_uuid, _danStageData.danStageName));
	}
	
	private void checkForDemote() {
		if (this._danStageData.canDemote && this._danStageData.danStageId != KTeamPVPConfig.getFirstDanRankId()) {
			int result = _demoteLoseCount.get();
			int cmpCount = KTeamPVPConfig.getLoseCountForDemote();
			if (result >= cmpCount) {
				_demoteLock.lock();
				try {
					result = _demoteLoseCount.get();
					if (result >= cmpCount) {
						this.demote();
					}
				} finally {
					_demoteLock.unlock();
				}
			}
		}
	}
	
	private void calculateDailyReward(KTeamPVPTeamMember member) {
		if (UtilTool.isBetweenDay(member._lastGetRewardTime, System.currentTimeMillis())) {
			int exp = KTeamPVPManager.getDailyExpReward(member._level);
			member._expReward = Math.round(_danStageData.dayExpWeight * exp);
		}
	}
	
	private void notifyRankToReset(KTeamPVPTeamMember leader, KTeamPVPTeamMember teamMate) {
		if (leader._equipmentRes == null) {
			// 可能这里还未初始化队员的数据
			leader.initFromRole(KSupportFactory.getRoleModuleSupport().getRole(leader._roleId));
		}
		KTeamPVPRankTypeEnum type = KTeamPVPManager.getRankType(_danData.danGradeId);
		if (teamMate == null) {
			KSupportFactory.getTeamPVPRankSupport().resetTeamMemChange(_id, type, leader.getId(), leader.getName(), leader.getVipLevel(), 0, "", 0);
		} else {
			KSupportFactory.getTeamPVPRankSupport().resetTeamMemChange(_id, type, leader.getId(), leader.getName(), leader.getVipLevel(), teamMate.getId(), teamMate.getName(), teamMate.getVipLevel());
		}
	}
	
	boolean isInPromoteFighting() {
		return this._inPromoteFighting.get();
	}
	
	DataStatus getDataStatus() {
		return this._dataStatus;
	}
	
	DBGameExtCA save() throws Exception {
		DBGameExtCA dbData = new DBGameExtCAImpl();
		dbData.setDBId(this._id);
		dbData.setDBType(KGameExtDataDBTypeEnum.战队竞技.dbType);
		dbData.setCAType(this._danStageData.danStageId);
		dbData.setAttribute(this.saveAttribute());
		return dbData;
	}
	
	void reset() {
		KTeamPVPTeamMember member;
		for (int i = 0; i < _members.size(); i++) {
			member = ((KTeamPVPTeamMember) _members.get(i));
			member.reset();
			if (member._hasOpen) {
				KTeamPVPMsgCenter.syncChallengeTime(member);
			}
		}
//		this._goodCount.set(0);
	}
	
//	void checkMembers() {
//		KTeamPVPTeamMember member;
//		long sub;
//		for(int i = 0; i < _members.size(); i++) {
//			member = ((KTeamPVPTeamMember) _members.get(i));
//			sub = System.currentTimeMillis() - member.getLastJoinedGameTime();
//			if (sub > KTeamPVPConfig.getMaxOfflineMillis()) {
//				this.processQuitTeam(member.getId(), false);
//				KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(member.getId(), GlobalTips.getTipsDefaultTitle(),
//						CompetitionTips.getTipsAutoRemoveFromTeam(KTeamPVPConfig.getMaxOfflineDays(), this._name));
//			}
//		}
//	}
	
	void calculateDailyReward() {
		KTeamPVPTeamMember member;
		boolean sync = false;
		for (int i = 0; i < _members.size(); i++) {
			member = (KTeamPVPTeamMember) _members.get(i);
			calculateDailyReward(member);
			if (member._hasOpen) {
				sync = true;
			}
		}
		if (sync) {
			KTeamPVPMsgCenter.notifyRewardRefurbish(this);
		}
	}
	
	void removeInviteRoleId(long roleId) {
		this._inviteRoleIds.remove(roleId);
	}
	
	void addInviteRoleId(long roleId) {
		this._inviteRoleIds.add(roleId);
	}
	
	int getTeamLevel() {
		return _teamLv;
	}
	
	int getGoodCount(){
		return _goodCount.get();
	}
	
	void increaseGoodCount(int count) {
		_goodCount.getAndAdd(count);
		_dataStatus.notifyUpdate();
	}
	
	void notifyBattlePowerChange(long roleId, int battlePower) {
		KTeamPVPTeamMember member = this.getMember(roleId);
		member._battlePower = battlePower;
		this._dataStatus.notifyUpdate();
		this.calculateBattlePower(true);
	}
	
	void notifyLoseInKingElection() {
		_demoteLock.lock();
		try {
			if (this._danStageData.danStageType == KDanStageType.KING) {
				this.demote();
			}
		} finally {
			_demoteLock.unlock();
		}
	}
	
	void processCombatFinish(KRole role, int score, boolean isWin) {
		KTeamPVPTeamMember member = this.getMember(role.getId());
		ITeamPVPTeam targetTeam;
		if(member._currentSelectedIsRobot) {
			
			targetTeam = KTeamPVPManager.getRobotTeam(member._currentSelectedTeamId);
		} else {
			targetTeam = KTeamPVPManager.getTeamByTeamId(member._currentSelectedTeamId);
		}
		String history;
		if (this._inPromoteFighting.get()) {
			synchronized (this._promoteFightResult) {
				if (this._inPromoteFighting.get()) {
					if (this._promoteFightResult.size() < _danStageData.promoteFightCount) {
						this._promoteFightResult.add(isWin);
					}
					FlowManager.logOther(role.getId(), OtherFlowTypeEnum.战队竞技, CompetitionTips.getTipsFlowTeamPromoteFightResult(_uuid, _promoteFightResult.size(), _danStageData.promoteFightCount, targetTeam.getTeamName(), isWin));
					this.checkForPromote();
				}
			}
			if(isWin) {
				history = CompetitionTips.getTipsPromoteChallengeSuccess(role.getName(), targetTeam.getTeamName());
			} else {
				history = CompetitionTips.getTipsPromoteChallengeFail(role.getName(), targetTeam.getTeamName());
			}
		} else {
			if (isWin) {
				this._currentScore.addAndGet(score);
//				this._winCount.incrementAndGet();
				if (_danStageData.danStageType.canPromote) {
					if (_currentScore.get() >= _danStageData.promoteScore) {
						_currentScore.getAndSet(_danStageData.promoteScore);
						this._inPromoteFighting.compareAndSet(false, true);
					}
				}
				if (_demoteLoseCount.get() > 0) {
					_demoteLoseCount.getAndSet(0);
				}
				history = CompetitionTips.getTipsTeamChallengeSuccess(role.getName(), targetTeam.getTeamName(), score);
				this.notifyRanking();
			} else {
				if (_currentScore.get() > 0) {
					int current = this._currentScore.addAndGet(-score);
					this._currentScore.getAndSet(Math.max(0, current));
					this.notifyRanking();
				} else if (_danStageData.canDemote) {
					this._demoteLoseCount.incrementAndGet();
					this.checkForDemote();
				}
				history = CompetitionTips.getTipsTeamChallengeFail(role.getName(), targetTeam.getTeamName(), score);
			}
			FlowManager.logOther(role.getId(), OtherFlowTypeEnum.战队竞技, CompetitionTips.getTipsFlowTeamResult(_uuid, targetTeam.getTeamName(), isWin, score));
		}
		this._dataStatus.notifyUpdate();
		this._historyLock.writeLock().lock();
		try {
			CommonResult historyResult = new CommonResult();
			historyResult.isSucess = isWin;
			historyResult.tips = history;
			this._history.add(historyResult);
			if (this._history.size() > KTeamPVPConfig.getMaxHistorySize()) {
				int removeCount = this._history.size() / 2;
				for (Iterator<CommonResult> itr = _history.iterator(); itr.hasNext();) {
					itr.next();
					itr.remove();
					removeCount--;
					if (removeCount > 0) {
						continue;
					} else {
						break;
					}
				}
				KTeamPVPTeamMember tempMember;
				int lastIndex = _history.size() - 1;
				for (int i = 0; i < _members.size(); i++) {
					tempMember = (KTeamPVPTeamMember) _members.get(i);
					if (tempMember._historyIndex > 0) {
						tempMember._historyIndex = lastIndex;
					}
				}
			}
		} finally {
			this._historyLock.writeLock().unlock();
		}
		if (member._currentSelectedIsRobot) {
			KTeamPVPManager.removeRobotTeam(member._currentSelectedTeamId);
		}
		member._currentSelectedTeamId = 0; // 战斗完之后重置
		member._currentSelectedIsRobot = false; // 战斗完之后重置
		if(isWin) {
			_ctnWinCount.incrementAndGet();
			_winCount.incrementAndGet();
		} else {
			_ctnWinCount.getAndSet(0);
		}
	}
	
	void packUpdateInfoToMessage(KGameMessage msg, long memberId) {
		KTeamPVPTeamMember member = this.getMember(memberId);
//		int canTime = KCompetitionTeamPVPConfig.getChallengeCountPerDay();
		msg.writeShort(_currentScore.get());
		msg.writeShort(_winCount.get());
		msg.writeShort(_totalChallengeCount);
		msg.writeBoolean(_inPromoteFighting.get());
		if(_inPromoteFighting.get()) {
			msg.writeByte(_promoteFightResult.size());
			for(int i = 0; i < _promoteFightResult.size(); i++) {
				msg.writeBoolean(_promoteFightResult.get(i));
			}
			msg.writeByte(_danStageData.promoteFightCount - _promoteFightResult.size());
		}
		msg.writeByte(member.getRewardLeftCount());
		msg.writeByte(member.getMaxChallengeRewardCount());
		int size = this._history.size();
		if (size > member._historyIndex) {
			_historyLock.readLock().lock();
			try {
				CommonResult history;
				msg.writeByte(size - member._historyIndex);
				for (int i = member._historyIndex; i < size; i++) {
					history = _history.get(i);
					msg.writeBoolean(history.isSucess);
					msg.writeUtf8String(history.tips);
				}
				member._historyIndex = size;
			} finally {
				_historyLock.readLock().unlock();
			}
		} else {
			msg.writeByte(0);
		}
	}
	
	void notifyMemberLevelUp(KRole role) {
		KTeamPVPTeamMember member = this.getMember(role.getId());
		if(member != null) {
			member._level = role.getLevel();
			member._battlePower = role.getBattlePower();
			calculateTeamLevel();
			calculateBattlePower(true);
		}
	}
	
	public KTeamPVPTeamMember getMember(long roleId) {
		KTeamPVPTeamMember member;
		for (int i = 0; i < _members.size(); i++) {
			member = (KTeamPVPTeamMember) _members.get(i);
			if (member._roleId == roleId) {
				if (member.getEquipmentRes() == null) {
					KRole role = KSupportFactory.getRoleModuleSupport().getRole(member._roleId);
					member.initFromRole(role);
				}
				return member;
			}
		}
		return null;
	}
	
	public List<Long> getInviteRoleIds() {
		return _inviteRoleIdsRO;
	}
	
	public void packDataToMsg(KGameMessage msg, long roleId) {
		KTeamPVPTeamMember member = this.getMember(roleId);
		msg.writeUtf8String(_name);
		msg.writeBoolean(member.isTeamLeader()); // 是否队长
		msg.writeInt(_teamBattlePower);
		msg.writeInt(_danData.iconId);
		msg.writeInt(_danStageData.iconId);
		msg.writeUtf8String(_danStageData.danStageName);
		msg.writeShort(_currentScore.intValue());
		msg.writeShort(_danStageData.promoteScore);
		msg.writeShort(_winCount.intValue());
		msg.writeShort(_totalChallengeCount);
		msg.writeBoolean(_inPromoteFighting.get());
		if(_inPromoteFighting.get()) {
			msg.writeByte(_promoteFightResult.size());
			for(int i = 0; i < _promoteFightResult.size(); i++) {
				msg.writeBoolean(_promoteFightResult.get(i));
			}
			msg.writeByte(_danStageData.promoteFightCount - _promoteFightResult.size());
		}
//		int canChallengeCount = KCompetitionTeamPVPConfig.getChallengeCountPerDay();
		member._hasOpen = true;
		member._historyIndex = _history.size();
		msg.writeByte(member.getRewardLeftCount());
		msg.writeByte(member.getMaxChallengeRewardCount());
		_historyLock.readLock().lock();
		try {
			msg.writeByte(_history.size());
			if (_history.size() > 0) {
				for (CommonResult result : _history) {
					msg.writeBoolean(result.isSucess);
					msg.writeUtf8String(result.tips);
				}
			}
		} finally {
			_historyLock.readLock().unlock();
		}
		msg.writeByte(_members.size());
		for (int i = 0; i < _members.size(); i++) {
			KTeamPVPTeamMember tempMember = (KTeamPVPTeamMember) _members.get(i);
			if (tempMember._equipmentRes == null) {
				tempMember.initFromRole(KSupportFactory.getRoleModuleSupport().getRole(tempMember._roleId));
			}
			KTeamPVPMsgCenter.packMemberDataToMsg(msg, _members.get(i));
		}
		List<KActionResult<Long>> allFriendIds = KTeamPVPManager.getCandidateFriendIds(roleId, this);
		KActionResult<Long> temp;
		msg.writeByte(allFriendIds.size());
		for (int i = 0; i < allFriendIds.size(); i++) {
			temp = allFriendIds.get(i);
			msg.writeLong(temp.attachment);
			msg.writeBoolean(temp.success);
			if(!temp.success) {
				msg.writeUtf8String(temp.tips);
			}
		}
		boolean matchTeam = member._currentSelectedTeamId > 0;
		msg.writeBoolean(matchTeam);
		if(matchTeam) {
			ITeamPVPTeam selectedTeam;
			if (member._currentSelectedIsRobot) {
				selectedTeam = KTeamPVPManager.getRobotTeam(member._currentSelectedTeamId);
			} else {
				selectedTeam = KTeamPVPManager.getTeamByTeamId(member._currentSelectedTeamId);
			}
			msg.writeUtf8String(selectedTeam.getTeamName());
			msg.writeByte(selectedTeam.getTeamMembers().size());
			for (int i = 0; i < selectedTeam.getTeamMembers().size(); i++) {
				KTeamPVPMsgCenter.packMemberDataToMsg(msg, selectedTeam.getTeamMembers().get(i));
			}
		}
		boolean hasReward = member._expReward > 0;
		msg.writeBoolean(hasReward);
		if (hasReward) {
			msg.writeInt(member._expReward);
		}
	}
	
	public String getUUID() {
		return _uuid;
	}
	
	public int getCurrentScore() {
		return this._currentScore.get();
	}
	
	public int getTeamBattlePower() {
		return _teamBattlePower;
	}
	
	public KTeamPVPDanData getDanData() {
		return _danData;
	}
	
	public KTeamPVPDanStageInfo getDanStageInfo() {
		return _danStageData;
	}
	
	public boolean isTeamFull() {
		return this._members.size() == KTeamPVPConfig.getMaxTeamMemberCount();
	}
	
	public KActionResult<Long> processKickTeamMember(long roleId) {
		KActionResult<Long> result = new KActionResult<Long>();
		if (_members.size() > 1) {
			KTeamPVPTeamMember member = this.getMember(roleId);
			if (member.isTeamLeader()) {
				int index = _members.indexOf(member);
				KTeamPVPTeamMember mate = (KTeamPVPTeamMember) _members.get(index == 0 ? 1 : 0);
				this.processQuitTeam(mate.getId(), false);
				result.success = true;
				result.attachment = mate.getId();
			} else {
				result.tips = CompetitionTips.getTipsYouAreNotCaptain();
			}
		} else {
			result.tips = CompetitionTips.getTipsNoMembersToKick();
		}
		return result;
	}
	
	public void processQuitTeam(long roleId, boolean isDelete) {
		KTeamPVPTeamMember member = this.getMember(roleId);
		if (member != null) {
			this._members.remove(member);
			this._teamBattlePower -= member._battlePower;
			KTeamPVPManager.onRoleQuitTeam(roleId, this);
			if(_members.isEmpty()) {
				KTeamPVPManager.removeTeam(this);
			} else {
				this.calculateTeamLevel();
				this.calculateBattlePower(true);
				KTeamPVPTeamMember leader = (KTeamPVPTeamMember)_members.get(0);
				leader.setIsLeader(true);
				this.notifyRankToReset(leader, null);
				if (KSupportFactory.getRelationShipModuleSupport().isInFriendList(leader.getId(), roleId)) {
					KTeamPVPMsgCenter.syncFriendId(leader.getId(), roleId, true);
				}
				KTeamPVPMsgCenter.notifyMemberQuited(leader.getId(), roleId);
				KTeamPVPMsgCenter.syncTeamBattlePowerToClient(this, leader.getId());
			}
			if(member._currentSelectedIsRobot) {
				KTeamPVPManager.removeRobotTeam(member._currentSelectedTeamId);
			}
			this._dataStatus.notifyUpdate();
			if (!isDelete) {
				KTeamPVPRoleRecordData extData = KTeamPVPManager.getTeamPVPRecordData(roleId);
				extData.setCurrentBuyTime(member._currentBuyTime);
				extData.setCurrentChallengeTime(member._currentChallengeTime);
				extData.setLastGetDailyRewardTime(member._lastGetRewardTime);
				extData.setMyHighestStage(member._highestStage.danStageId);
			}
		}
	}
	
	public ITeamPVPTeam processSelectTeam(long roleId) {
		KTeamPVPTeamMember member = this.getMember(roleId);
		ITeamPVPTeam team = KTeamPVPManager.selectChallenger(member._currentChallengeTime + 1, this);
		if (team != null) {
//			long preTeamId = member._currentSelectedTeamId;
//			boolean preIsRobot = member._currentSelectedIsRobot;
			member._currentSelectedTeamId = team.getId();
			member._currentSelectedIsRobot = team.isRobotTeam();
//			if (preTeamId > 0 && preIsRobot) {
//				KTeamPVPManager.removeRobotTeam(preTeamId);
//			}
			this._dataStatus.notifyUpdate();
		}
		return team;
	}
	
	public void notifyRejectInvitation(long targetRoleId) {
		this._inviteRoleIds.remove(targetRoleId);
		this._dataStatus.notifyUpdate();
		KTeamPVPMsgCenter.syncFriendId(_members.get(0).getId(), targetRoleId, true);
	}
	
	public boolean processAcceptInvitation(KRole acceptRole) {
		if (this.isTeamFull()) {
			return false;
		} else {
			if (_inviteRoleIds.contains(acceptRole.getId())) {
				synchronized (this) {
					if (this.isTeamFull()) {
						return false;
					} else {
						KTeamPVPTeamMember member = new KTeamPVPTeamMember(acceptRole, this._dataStatus);
						this._members.add(member);
						this.calculateTeamLevel();
						this.calculateBattlePower(false);
						KTeamPVPManager.onRoleJoinTeam(acceptRole.getId(), this);
						KTeamPVPTeamMember leader = (KTeamPVPTeamMember) _members.get(0);
						this.notifyRankToReset(leader, member);
						_inviteRoleIds.remove(acceptRole.getId());
						KTeamPVPMsgCenter.syncTeamBattlePowerToClient(this, leader.getId());
						this._dataStatus.notifyUpdate();
						this.calculateDailyReward(member);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public KActionResult<Integer> processChallenge(KRole role) {
		KTeamPVPTeamMember member = this.getMember(role.getId());
		String tips = null;
		if(member != null) {
			if(member._currentSelectedTeamId == 0) {
				tips = CompetitionTips.getTipsPlsSelectATarget();
			} /*else if (member._currentChallengeTime >= member.getMaxChallengeCount()){
				tips = CompetitionTips.getTipsCannotChallengeAnyMoreToday();
			} */else {
				long teammateId = 0;
				if(this._members.size() > 1) {
					for(ITeamPVPTeamMember tempMember : _members) {
						if(tempMember.getId() != member._roleId) {
							teammateId = tempMember.getId();
							break;
						}
					}
				}
				ITeamPVPTeam team;
				KActionResult<Integer> result;
				if(member._currentSelectedIsRobot) {
					team = KTeamPVPManager.getRobotTeam(member._currentSelectedTeamId);
					List<ITeamPVPTeamMember> members = team.getTeamMembers();
					List<ICombatMirrorDataGroup> list = new ArrayList<ICombatMirrorDataGroup>(members.size());
					for (int i = 0; i < members.size(); i++) {
						list.add(KTeamPVPManager.getRobotGroup(members.get(i).getId()));
					}
					result = KSupportFactory.getCombatModuleSupport().fightWithAI(role, teammateId, list, KCombatType.TEAM_PVP, KTeamPVPConfig.getCombatEnv(), member._currentChallengeTime + 1, KTeamPVPConfig.getTimeoutMillis());
				} else {
					team = KTeamPVPManager.getTeamByTeamId(member._currentSelectedTeamId);
					result = KSupportFactory.getCombatModuleSupport().fightWithAI(role, teammateId, team.getAllMemberIds(), KCombatType.TEAM_PVP, KTeamPVPConfig.getCombatEnv(), member._currentChallengeTime + 1, KTeamPVPConfig.getTimeoutMillis());
				}
				if(result.success) {
					member._currentChallengeTime++;
					this._totalChallengeCount++;
					this._dataStatus.notifyUpdate();
				}
				return result;
			}
		} else {
			tips = CompetitionTips.getTipsYouAreNotInTeam();
		}
		KActionResult<Integer> result = new KActionResult<Integer>();
		result.tips = tips;
		return result;
	}
	
	public CommonResult processGetDailyReward(KRole role) {
		CommonResult result = new CommonResult();
		KTeamPVPTeamMember member = this.getMember(role.getId());
		if (member != null && member._expReward > 0) {
			role.addExp(member._expReward, KRoleAttrModifyType.队伍竞技每日奖励, this._danStageData.danGradeId);
			result.tips = CompetitionTips.getTipsGetDailyRewardSuccess(member._expReward);
			result.isSucess = true;
			member._expReward = 0;
			member._lastGetRewardTime = System.currentTimeMillis();
			this._dataStatus.notifyUpdate();
		} else {
			result.tips = CompetitionTips.getTipsNoDailyRewardToGet();
		}
		return result;
	}
	
	public int getCtnWinCount() {
		return _ctnWinCount.get();
	}
	
	public long getId() {
		return _id;
	}
	
	@Override
	public String getTeamName() {
		return _name;
	}
	
	@Override
	public boolean isRobotTeam() {
		return false;
	}
	
	@Override
	public List<ITeamPVPTeamMember> getTeamMembers() {
		return _members;
	}
	
	@Override
	public long[] getAllMemberIds() {
		long[] roleIds = new long[_members.size()];
		for (int i = 0; i < _members.size(); i++) {
			roleIds[i] = ((KTeamPVPTeamMember)_members.get(i))._roleId;
		}
		return roleIds;
	}
	
	public static class KTeamPVPTeamMember implements ITeamPVPTeamMember {
		
		/**
		 * 
		 */
		public static final int RES_TYPE_EQUIPMENT = 1;
		/**
		 * 
		 */
		public static final int RES_TYPE_EQUIPMENT_SET = 2;
		/**
		 * 
		 */
		public static final int RES_TYPE_FASHION = 3;
		
		private static final String KEY_ROLE_ID = "1";
		private static final String KEY_BATTLE_POWER = "2";
		private static final String KEY_CURRENT_CHALLENGE_TIME = "3";
		private static final String KEY_CURRENT_SELECT_TEAM_ID = "4";
		private static final String KEY_CURRENT_SELECT_TEAM_IS_ROBOT = "5";
		private static final String KEY_LEVEL = "6";
		private static final String KEY_CURRENT_BUY_TIME = "7";
		private static final String KEY_LAST_GET_REWARD_TIME = "8";
		private static final String KEY_HIGHEST_STAGE_ID = "9";
		private static final String KEY_LAST_JOIN_GAME_TIME = "10";
		private static final String KEY_IS_LEADER = "11";
		
		private long _roleId;
		private int _battlePower;
		private int _maxBuyTime; // 最大的购买次数
		private int _maxChallengeRewardTime; // 最大的挑战次数
		private int _currentBuyTime; // 当前可以购买的次数
		private int _currentChallengeTime; // 当前的挑战次数
		private long _currentSelectedTeamId; // 当前的匹配队伍id
		private boolean _currentSelectedIsRobot; // 当前的匹配队伍是不是机器人
		private long _lastGetRewardTime;
//		private int _highestStageId; // 最高段位
		private KTeamPVPDanStageInfo _highestStage; // 最高段位
		private KTeamPVPDanData _highestDan;
		private boolean _hasOpen; // 是否打开
		private int _historyIndex;
		private int _expReward; // 当前的奖励（0表示没有奖励）
		private long _lastJoinedGameTime;
		private boolean _isLeader;
		
		private String _notice;
		
		private String _name;
		private byte _job;
		private int _level;
		private int _vipLevel;
		private int _headResId;
		private int _inMapResId;
		private List<IRoleEquipShowData> _equipmentRes;
		private String _fashionRes;
		private int[] _equipSetRes;
		
		private DataStatus _dataStatus;
		
		public KTeamPVPTeamMember(KRole role, DataStatus pDataStatus) {
			this._dataStatus = pDataStatus;
			this._roleId = role.getId();
			this._battlePower = role.getBattlePower();
			KTeamPVPRoleRecordData data = KTeamPVPManager.getTeamPVPRecordData(role.getId());
			this._currentBuyTime = data.getCurrentBuyTime();
			this._currentChallengeTime = data.getCurrentChallengeTime();
			this._lastGetRewardTime = data.getLastGetDailyRewardTime();
			int highestStageId = data.getMyHighestStage();
			if(highestStageId < KTeamPVPConfig.getFirstDanRankId()) {
				highestStageId = KTeamPVPConfig.getFirstDanRankId();
			}
			this._lastJoinedGameTime = System.currentTimeMillis();
			this.setMyHighestStageInfo(highestStageId);
			this.initFromRole(role);
		}
		
		public KTeamPVPTeamMember(String attribute, DataStatus pDataStatus) throws Exception {
			this._dataStatus = pDataStatus;
			JSONObject obj = new JSONObject(attribute);
			this._roleId = obj.getLong(KEY_ROLE_ID);
			this._battlePower = obj.getInt(KEY_BATTLE_POWER);
			this._currentChallengeTime = obj.getInt(KEY_CURRENT_CHALLENGE_TIME);
			this._currentSelectedIsRobot = obj.getBoolean(KEY_CURRENT_SELECT_TEAM_IS_ROBOT);
			this._level = obj.getInt(KEY_LEVEL);
			this._currentBuyTime = obj.optInt(KEY_CURRENT_BUY_TIME, 0);
			this._lastGetRewardTime = obj.optInt(KEY_LAST_GET_REWARD_TIME, 0);
			this._isLeader = obj.optBoolean(KEY_IS_LEADER, false);
			long lastJoinedGameTime = obj.optLong(KEY_LAST_JOIN_GAME_TIME, 0);
			int highestStageId = obj.optInt(KEY_HIGHEST_STAGE_ID, KTeamPVPConfig.getFirstDanRankId());
			this.setMyHighestStageInfo(highestStageId);
			if(this._currentSelectedIsRobot) {
				this._currentSelectedIsRobot = false;
			} else {
				this._currentSelectedTeamId = obj.getInt(KEY_CURRENT_SELECT_TEAM_ID);
			}
			if(lastJoinedGameTime > 0) {
				this._lastJoinedGameTime = lastJoinedGameTime;
			} else {
				this._lastJoinedGameTime = System.currentTimeMillis();
				this._dataStatus.notifyUpdate();
			}
		}
		
		synchronized void initFromRole(KRole role) {
			if (this._equipmentRes == null) {
				this._name = role.getName();
				this._job = role.getJob();
				this._level = role.getLevel();
				this._vipLevel = role.getVipLevel();
				this._headResId = role.getHeadResId();
				this._inMapResId = role.getInMapResId();
				this._equipmentRes = new ArrayList<IRoleEquipShowData>(role.getEquipmentRes());
				this._fashionRes = role.getFashionRes();
				this._equipSetRes = Arrays.copyOf(role.getEquipSetRes(), role.getEquipSetRes().length);
				VIPLevelData data = KSupportFactory.getVIPModuleSupport().getVIPLevelData(role.getVipLevel());
				this._maxBuyTime = data.ladderbuyrmb.length + KTeamPVPConfig.getChallengeCountPerDay();
				this._maxChallengeRewardTime = _currentBuyTime + KTeamPVPConfig.getChallengeCountPerDay();
			}
		}
		
		String saveAttribute() throws Exception {
			JSONObject obj = new JSONObject();
			obj.put(KEY_ROLE_ID, _roleId);
//			obj.put(KEY_TODAY_CHALLENGE_COUNT, _todayChallengeCount);
			obj.put(KEY_BATTLE_POWER, _battlePower);
			obj.put(KEY_CURRENT_CHALLENGE_TIME, _currentChallengeTime);
			obj.put(KEY_CURRENT_SELECT_TEAM_ID, _currentSelectedTeamId);
			obj.put(KEY_CURRENT_SELECT_TEAM_IS_ROBOT, _currentSelectedIsRobot);
			obj.put(KEY_LEVEL, _level);
			obj.put(KEY_CURRENT_BUY_TIME, _currentBuyTime);
			obj.put(KEY_LAST_GET_REWARD_TIME, _lastGetRewardTime);
			obj.put(KEY_HIGHEST_STAGE_ID, _highestStage.danStageId);
			obj.put(KEY_LAST_JOIN_GAME_TIME, _lastJoinedGameTime);
			obj.put(KEY_IS_LEADER, _isLeader);
			return obj.toString();
		}
		
		void setIsLeader(boolean pFlag) {
			this._isLeader = pFlag;
			this._dataStatus.notifyUpdate();
		}
		
		void setMyHighestStageInfo(int danStageId) {
			_highestStage = KTeamPVPManager.getDanStageInfo(danStageId);
			if(_highestDan == null || _highestDan.danGradeId != _highestStage.danGradeId) {
				_highestDan = KTeamPVPManager.getDanData(_highestStage.danGradeId);
			}
		}
		
		void notifyOffline() {
			this._hasOpen = false;
		}
		
		int getExpReward() {
			return _expReward;
		}
		
		void reset() {
			this._currentChallengeTime = 0;
			this._maxChallengeRewardTime = KTeamPVPConfig.getChallengeCountPerDay();
			this._currentBuyTime = 0;
		}
		
		void notifyVipChange(int vipLv, int nowCanBuyTime) {
			this._vipLevel = vipLv;
			this._maxBuyTime = nowCanBuyTime;
			this._dataStatus.notifyUpdate();
		}
		
		void notifyJoinGame() {
			this._lastJoinedGameTime = System.currentTimeMillis();
			this._dataStatus.notifyUpdate();
		}
		
		long getLastJoinedGameTime() {
			return this._lastJoinedGameTime;
		}
		
		public boolean isTeamLeader() {
			return _isLeader;
		}
		
		public boolean hasOpen() {
			return _hasOpen;
		}
		
		public void setNotice(String pNotice) {
			_notice = pNotice;
		}
		
		public String getNotice() {
			return _notice;
		} 
		
		public int getRewardLeftCount() {
			int result = _maxChallengeRewardTime - _currentChallengeTime;
			if(result > 0) {
				return result;
			} else {
				return 0;
			}
		}
		
		public int getMaxChallengeRewardCount() {
			return _maxChallengeRewardTime;
		}
		
		void updateHistoryIndex(int index) {
			this._historyIndex = index;
		}
		
		public void notifyBuyTime() {
			this._currentBuyTime++;
			this._maxChallengeRewardTime++;
			_dataStatus.notifyUpdate();
		}
		
		public int getHistoryIndex() {
			return _historyIndex;
		}
		
		public int getCurrentBuyTime() {
			return _currentBuyTime;
		}
		
		public int getMaxBuyTime() {
			return _maxBuyTime;
		}
		
		@Override
		public long getId() {
			return _roleId;
		}

		@Override
		public String getName() {
			return _name;
		}

		@Override
		public byte getJob() {
			return _job;
		}

		@Override
		public int getLevel() {
			return _level;
		}

		@Override
		public int getVipLevel() {
			return _vipLevel;
		}

		@Override
		public int getHeadResId() {
			return _headResId;
		}

		@Override
		public int getInMapResId() {
			return _inMapResId;
		}
		
		boolean updateEquipmentRes(List<IRoleEquipShowData> list) {
			_equipmentRes.clear();
			_equipmentRes.addAll(list);
			return true;
		}

		@Override
		public List<IRoleEquipShowData> getEquipmentRes() {
			return _equipmentRes;
		}
		
		boolean updateFashionRes(String fashionRes) {
			String pre = this._fashionRes;
			this._fashionRes = fashionRes;
			if ((_fashionRes != null && pre == null) || (_fashionRes == null && pre != null)) {
				return true;
			}
			return !this._fashionRes.equals(pre);
		}

		@Override
		public String getFashionResId() {
			return _fashionRes;
		}

		boolean updateEquipSetRes(int[] res) {
			int length = _equipSetRes.length > res.length ? res.length : _equipSetRes.length;
			int index = 0;
			boolean update = false;
			for (; index < length; index++) {
				if (_equipSetRes[index] != res[index]) {
					_equipSetRes[index] = res[index];
					update = true;
				}
			}
			if (index < _equipSetRes.length) {
				for (; index < _equipSetRes.length; index++) {
					_equipSetRes[index] = 0;
					update = true;
				}
			}
			return update;
		}
		
		@Override
		public int[] getEquipSetRes() {
			return _equipSetRes;
		}

		public KTeamPVPDanStageInfo getHighestStage() {
			return _highestStage;
		}

		public KTeamPVPDanData getHighestDan() {
			return _highestDan;
		}
	}
}
