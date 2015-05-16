package com.kola.kmp.logic.activity.worldboss;

import static com.kola.kmp.protocol.activity.KActivityProtocol.SM_SEND_WORLD_BOSS_BASIC_DATA;
import static com.kola.kmp.protocol.activity.KActivityProtocol.SM_SYNC_WORLD_BOSS_HP;
import static com.kola.kmp.protocol.activity.KActivityProtocol.SM_UPDATE_RANKING;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.actionrecord.KActionRecorder;
import com.kola.kmp.logic.actionrecord.KActionType;
import com.kola.kmp.logic.activity.KActivityRoleExtCaCreator;
import com.kola.kmp.logic.activity.worldboss.KWorldBossActivityMain.KWorldBossRankingData;
import com.kola.kmp.logic.activity.worldboss.KWorldBossFieldData.KWorldBossMonsterKey;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.combat.ICombatCommonResult;
import com.kola.kmp.logic.combat.ICombatGlobalCommonResult;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.map.duplicatemap.CollisionEventListener;
import com.kola.kmp.logic.map.duplicatemap.CollisionEventObjectData;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMap;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMapBornPoint;
import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KMonsterType;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.CombatModuleSupport.ICombatMonsterUpdateInfo;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.ResultStructs.MailResult;
import com.kola.kmp.logic.util.tips.WorldBossTips;

/**
 * 
 * <pre>
 * 代表一个世界boss的活动场景
 * </pre>
 * 
 * @author PERRY CHAN
 */
public class KWorldBossActivityField implements CollisionEventListener {

	private static final Logger _FLOGGER = KGameLogger.getLogger("activityLogger");
	private static final Logger _LOGGER = KGameLogger.getLogger(KWorldBossActivityField.class);
	private static final Comparator<KWorldBossMember> _comparator = new Comparator<KWorldBossMember>() {

		@Override
		public int compare(KWorldBossMember o1, KWorldBossMember o2) {
			return o1.getTotalDm() >= o2.getTotalDm() ? -1 : 1;
		}
	};

	private static final int[] EMPTY_PIC_RES_ID_ARRAY = new int[0];
	private static final String[] EMPTY_URL_LINK_ARRAY = new String[0];
	
	private static final Comparator<CollisionEventObjectData> _mapMonsterComparator = new Comparator<CollisionEventObjectData>() {

		@Override
		public int compare(CollisionEventObjectData o1, CollisionEventObjectData o2) {
			return o1.getX() < o2.getX() ? -1 : 1;
		}
	};
	
	private static final Comparator<IWorldBossMember> _memberComparator = new Comparator<IWorldBossMember>() {

		@Override
		public int compare(IWorldBossMember o1, IWorldBossMember o2) {
			return o1.getBattlePower() > o2.getBattlePower() ? -1 : 1;
		}
	};
	
	private static final SimpleDateFormat _SDF = new SimpleDateFormat("yyyy年MM月dd日");
	
	private static int _MAX_ROBOT_SIZE = 5;
	private static final boolean _PRESENT = true;

//	private static final List<KDialogButton> RESULT_BUTTONS = Collections.unmodifiableList(Arrays.asList(new KDialogButton(KActivityModuleDialogProcesser.FUN_WORLD_BOSS_RESULT_CONFIRM, "",
//			KDialogButton.CONFIRM_DISPLAY_TEXT)));
	private final KWorldBossFieldData _worldBossFieldData; // 模板数据
	private final Map<Long, KWorldBossMember> _joinMembers = new ConcurrentHashMap<Long, KWorldBossMember>(); // 当前参加的角色
	private final List<KWorldBossMember> _rankingMembers = new ArrayList<KWorldBossMember>(); // 排名
	private final List<KWorldBossMember> _lastSendRankings = new ArrayList<KWorldBossMember>(KWorldBossConfig.getSendRakingCount());
	private final Map<Integer, Map<Integer, KWorldBossMonsterInfo>> _monsterInfos = new HashMap<Integer, Map<Integer, KWorldBossMonsterInfo>>(); // key=生成规则id，value={key=instanceId，value=KWorldBossMonsterInfo实例}
	private final Map<Integer, Map<Integer, ICombatMonsterUpdateInfo>> _monsterInfosForCombat = new HashMap<Integer, Map<Integer, ICombatMonsterUpdateInfo>>(); // 这个内容和_monsterInfos一样，只是换一种数据格式，map的泛型不支持数据格式的传递
	private final Map<Integer, KGameBattlefield> _monsterBattlefields = new HashMap<Integer, KGameBattlefield>();
//	private final Map<Integer, KGenRuleData> _genRuleDataMap = new ConcurrentHashMap<Integer, KWorldBossGenRule.KGenRuleData>();
//	private final Map<Integer, KGenRuleData> _genRuleDataMapAll = new ConcurrentHashMap<Integer, KWorldBossGenRule.KGenRuleData>();
	private final Map<Integer, List<KWorldBossMonsterKey>> _genMonsterKeys = new HashMap<Integer, List<KWorldBossMonsterKey>>(); // 已经随机好的monsterKey，用于在每次重置小怪数据的时候作为凭证
	private final Map<Integer, Integer> _aliveMap = new HashMap<Integer, Integer>(); // key=碰撞事件的地图instanceId, // value=本次自生成的怪物id
	private final Map<Integer, Integer> _instanceIdToMapInstanceId = new HashMap<Integer, Integer>(); // key=本次自生成的怪物id, value=碰撞事件的地图instanceId，用于再次生成怪物的时候使用
	private final Queue<String> _broadcastQueue = new ConcurrentLinkedQueue<String>();
	private final List<KWorldBossMonsterInfo> _bosses = new ArrayList<KWorldBossMonsterInfo>();
	private final AtomicInteger _instanceIdGenerator = new AtomicInteger(); // id生成器
	private final Map<Long, Boolean> _fightingRoles = new ConcurrentHashMap<Long, Boolean>();
	private volatile boolean _finish;
	private volatile boolean _canFight;
	
	private int _timeIndex;
	private int _leftSeconds;
	private int _warnUpLeftSeconds;
//	private int _leftWaveCount; // 小怪波数
//	private int _refurbishWaiting;
	private int _bossLv;
	private List<Integer> _broadcastHpPct = new ArrayList<Integer>();
	private final AtomicBoolean _sortStatus = new AtomicBoolean();
	private final AtomicBoolean _dataStatus = new AtomicBoolean(); // 是否有怪物需要移除的标识位
	private final AtomicBoolean _updateBossHp = new AtomicBoolean(); // 世界boss同步血量的标志位
	private final ReadWriteLock _rankingLock = new ReentrantReadWriteLock();
	private final AtomicInteger _exitCount = new AtomicInteger();
	private String _killBossRoleName;
	@SuppressWarnings("rawtypes")
	private KDuplicateMap _senceMap;

	KWorldBossActivityField(KWorldBossFieldData pFieldData) {
		_worldBossFieldData = pFieldData;
	}

//	private final boolean matchLv(KRole role) {
//		return role.getLevel() <= this._worldBossFieldData.maxLv && role.getLevel() >= this._worldBossFieldData.minLv;
//	}

	private final void addJoinMember(KWorldBossMember member) {
		this._joinMembers.put(member.getRoleId(), member);
		this._rankingMembers.add(member);
		if (this._lastSendRankings.size() < KWorldBossConfig.getSendRakingCount()) {
			this._lastSendRankings.add(member);
		}
		KWorldBossActivityRecorder.recordJoin(member.getRoleId(), _timeIndex);
	}

	private final void sortRanking() {
		if (_sortStatus.compareAndSet(true, false)) {
			if (_rankingMembers.size() > 0) {
				Collections.sort(_rankingMembers, _comparator);
				int size = _rankingMembers.size() > KWorldBossConfig.getSendRakingCount() ? KWorldBossConfig.getSendRakingCount() : _rankingMembers.size();
				boolean sendUpdate = false;
				if (_lastSendRankings.isEmpty()) {
					this.modifySendRanking(0, size);
					sendUpdate = true;
				} else {
					int endIndex = size > _lastSendRankings.size() ? _lastSendRankings.size() : size;
					KWorldBossMember now;
					KWorldBossMember last;
					for (int i = 0; i < endIndex; i++) {
						last = _lastSendRankings.get(i);
						now = _rankingMembers.get(i);
						if (last != now) {
							_lastSendRankings.set(i, now);
							if (!sendUpdate) {
								sendUpdate = true;
							}
						} else if (now.isDmChange().compareAndSet(true, false) && !sendUpdate) {
							sendUpdate = true;
						}
					}
					if (endIndex < size) {
						this.modifySendRanking(endIndex, size);
						sendUpdate = true;
					}
				}
				if (sendUpdate) {
					sendUpdateRankingMsg();
				}
			}
		}
	}
	
	private void sendMsgToAllMember(KGameMessage msg) {
		for (Iterator<KWorldBossMember> itr = _joinMembers.values().iterator(); itr.hasNext();) {
			KSupportFactory.getRoleModuleSupport().sendMsg(itr.next().getRoleId(), msg);
			if (itr.hasNext()) {
				msg = msg.copy();
			}
		}
	}

	private void modifySendRanking(int beginIndex, int endIndex) {
		_rankingLock.writeLock().lock();
		try {
			for (int i = beginIndex; i < endIndex; i++) {
				_lastSendRankings.add(_rankingMembers.get(i));
			}
		} finally {
			_rankingLock.writeLock().unlock();
		}
	}

	private boolean checkFinished(int subSeconds) {
		KWorldBossMonsterInfo current;
		boolean bossDie = true;
		for (Iterator<Map.Entry<Integer, Map<Integer, KWorldBossMonsterInfo>>> itr = _monsterInfos.entrySet().iterator(); itr.hasNext();) {
			for (Iterator<KWorldBossMonsterInfo> itr2 = itr.next().getValue().values().iterator(); itr2.hasNext();) {
				current = itr2.next();
				if (current.isAlive()) {
					if (current.isBoss) {
						bossDie = false;
					} else {
						return false;
					}
				}
			}
		}
		/*if (this._leftWaveCount > 0) {
			// 如果剩余波数>0，生成下一波怪物，然后返回false
			this._refurbishWaiting += subSeconds;
			if (_refurbishWaiting >= _worldBossFieldData.refurbishSeconds) {
				this.genNextWave();
				this._refurbishWaiting = 0;
			}
			return false;
		} else {
			return bossDie;
		}*/
		return bossDie;
	}
	
	/*private void genNextWave() {
		Map.Entry<Integer, KGenRuleData> tempData;
		List<KWorldBossMonsterKey> list;
		int instanceId;
		int mapInstanceId;
		KGenRuleData genRuleData;
		Map<Integer, Integer> idMap = new HashMap<Integer, Integer>();
		Map<Integer, KGenRuleData> nGenRuleMap = new HashMap<Integer, KGenRuleData>();
		for (Iterator<Map.Entry<Integer, KGenRuleData>> itr = _genRuleDataMap.entrySet().iterator(); itr.hasNext();) {
			tempData = itr.next();
			instanceId = _instanceIdGenerator.incrementAndGet();
			genRuleData = tempData.getValue();
			if (genRuleData.isBoss) {
				continue;
			}
			list = _genMonsterKeys.remove(tempData.getKey());
			this.generateMonsters(instanceId, genRuleData, list, nGenRuleMap, _genMonsterKeys);
			mapInstanceId = _instanceIdToMapInstanceId.remove(tempData.getKey());
			idMap.put(instanceId, mapInstanceId);
			_instanceIdToMapInstanceId.put(instanceId, mapInstanceId);
			itr.remove();
		}
		_genRuleDataMap.putAll(nGenRuleMap);
		Map.Entry<Integer, Integer> entry;
		for(Iterator<Map.Entry<Integer, Integer>> itr = idMap.entrySet().iterator(); itr.hasNext();) {
			// key=新产生的instanceId，value=地图的instanceId
			entry = itr.next();
			instanceId = entry.getKey();
			mapInstanceId = entry.getValue();
			this._senceMap.putCollisionEventObjectData(mapInstanceId, instanceId);
			this._aliveMap.put(mapInstanceId, instanceId);
		}
		_leftWaveCount--;
	}*/
	
	/*private void generateMonsters(int genInstanceId, KGenRuleData genRuleData, List<KWorldBossMonsterKey> list, Map<Integer, KGenRuleData> genRuleRecordMap, Map<Integer, List<KWorldBossMonsterKey>> keyMap) {
		KWorldBossMonsterKey tempKey;
		KWorldBossMonsterInfo monsterInfo;
		Map<Integer, KWorldBossMonsterInfo> map = new HashMap<Integer, KWorldBossMonsterInfo>();
		Map<Integer, ICombatMonsterUpdateInfo> mapCombat = new HashMap<Integer, ICombatMonsterUpdateInfo>();
		_monsterInfos.put(genInstanceId, map);
		_monsterInfosForCombat.put(genInstanceId, mapCombat);
		_monsterBattlefields.put(genInstanceId, genRuleData.getBattlefield());
		genRuleRecordMap.put(genInstanceId, genRuleData);
		_genRuleDataMapAll.put(genInstanceId, genRuleData);
		keyMap.put(genInstanceId, list);
		for (int k = 0; k < list.size(); k++) {
			tempKey = list.get(k);
			monsterInfo = new KWorldBossMonsterInfo(tempKey.templateId, tempKey.instanceId);
			map.put(tempKey.instanceId, monsterInfo);
			mapCombat.put(tempKey.instanceId, monsterInfo);
		}
	}*/
	
	private void executeRankReward(IWorldBossMember member, KWorldBossRankReward reward, int rank, String mailTitle, String mailContent) {
		KWorldBossRewardBasicPara rewardPara = KWorldBossManager.getRewardPara(member.getLevel());
		int gold = UtilTool.calculateTenThousandRatio(reward.rewardData.goldProportion, rewardPara.goldBasicPara);
		int expReward = UtilTool.calculateTenThousandRatio(reward.rewardData.expProportion, rewardPara.expBasicPara);
		int potential = UtilTool.calculateTenThousandRatio(reward.rewardData.potentialProportion, rewardPara.potentialBasicPara);
		AttValueStruct expRewardStruct = new AttValueStruct(KGameAttrType.EXPERIENCE, expReward);
		List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
		moneyList.add(new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, gold));
		moneyList.add(new KCurrencyCountStruct(KCurrencyTypeEnum.POTENTIAL, potential));
		BaseRewardData mailReward = new BaseRewardData(Arrays.asList(expRewardStruct), moneyList, reward.rewardData.itemRewards.size() > 0 ? reward.rewardData.itemRewards : null, null, null);
		BaseMailContent baseMailContent = new BaseMailContent(mailTitle, mailContent, EMPTY_PIC_RES_ID_ARRAY, EMPTY_URL_LINK_ARRAY);
		BaseMailRewardData mailData = new BaseMailRewardData(0, baseMailContent, mailReward);
		MailResult result = KSupportFactory.getMailModuleSupport().sendAttMailBySystem(member.getRoleId(), mailData, PresentPointTypeEnum.世界BOSS);
		_FLOGGER.info("世界boss，发送排名奖励，角色名字：{}，排名：{}，等级：{}，是否成功：{}", member.getRoleName(), rank, member.getLevel(), result.isSucess);
	}

	private void settle() {
		this._sortStatus.compareAndSet(false, true);
		this.sortRanking();
//		Map<Integer, KWorldBossRankReward> rewardMap = KWorldBossManager.getRankReward(_worldBossFieldData.rankRewardId);
		Set<KWorldBossRankReward> rewardSet = KWorldBossManager.getRankReward();
		KWorldBossRankReward reward = null;
		KWorldBossMember member;
		int rank;
		boolean bossKilled = true;
		for (int i = 0; i < _rankingMembers.size(); i++) {
			member = _rankingMembers.get(i);
			rank = i + 1;
			try {
				for(Iterator<KWorldBossRankReward> itr = rewardSet.iterator(); itr.hasNext();) {
					reward = itr.next();
					if (reward.beginRank >= rank && reward.endRank <= rank) {
						break;
					} else {
						reward = null;
					}
				}
//				member.executeDmReward(this);
				member.executeKillReward(this._bossLv);
				if (reward != null) {
//					member.executeRankReward(rank, reward);
					this.executeRankReward(member, reward, rank, WorldBossTips.getTipsRankRewardMailTitle(), WorldBossTips.getTipsRankRewardMailContent(member.getTotalDm(), rank));
				}
			} catch (Exception e) {
				_LOGGER.error("执行结算出现异常！角色id:{}，鼓舞等级:{}, 排名:{}", member.getRoleId(), member.getInspireLv(), rank, e);
			}
		}
		for(int i = 0; i < _bosses.size(); i++) {
			if(_bosses.get(i).isAlive()) {
				bossKilled = false;
				break;
			}
		}
		KWorldBossActivityRecorder.recordBossKillStatus(this._worldBossFieldData.templateId, bossKilled, _timeIndex);
		if (_rankingMembers.size() > 0) {
			member = _rankingMembers.get(0);
			KSupportFactory.getChatSupport().sendSystemChat(StringUtil.format(KWordBroadcastType.丧尸攻城_恭喜x角色在本次活动中对丧尸攻城造成了x点伤害的恐怖输出排名第一.content, member.getRoleNameEx(), member.getTotalDm()),
					KWordBroadcastType.丧尸攻城_恭喜x角色在本次活动中对丧尸攻城造成了x点伤害的恐怖输出排名第一);
		}
		if (bossKilled && this._bossLv < KWorldBossConfig.getMaxBossLv()) {
			int second = KWorldBossConfig.getDurationSecond() - _leftSeconds;
			int minute = (int) TimeUnit.MINUTES.convert(second, TimeUnit.SECONDS);
			if (second % 60 > 30) {
				minute++;
			}
			int upLv = KWorldBossManager.getWorldBossUpgradeLv(minute);
			if (upLv > 0) {
				this._bossLv += upLv;
			}
			if (this._bossLv > KWorldBossConfig.getMaxBossLv()) {
				this._bossLv = KWorldBossConfig.getMaxBossLv();
			}
		}
		processOfflineMemberReward(rewardSet);
	}
	
	private void processOfflineMemberReward(Set<KWorldBossRankReward> rewardSet) {
		Set<Long> autoJoinIds = KWorldBossManager.getAutoJoinRoleIds();
		KRole role;
		Long roleId;
		List<IWorldBossMember> autoJoinMembers = new ArrayList<IWorldBossMember>();
		List<Long> failIds = new ArrayList<Long>();
		KWorldBossRoleData roleData;
		KWorldBossRankReward reward = null;
		List<Long> removeIds = new ArrayList<Long>();
		for (Iterator<Long> itr = autoJoinIds.iterator(); itr.hasNext();) {
			roleId = itr.next();
			if (_joinMembers.containsKey(roleId)) {
				continue;
			}
			role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role != null) {
				if (KSupportFactory.getCurrencySupport().decreaseMoney(roleId, KCurrencyTypeEnum.DIAMOND, KWorldBossConfig.getAutoJoinPrice(), UsePointFunctionTypeEnum.世界boss自动参与,
						role.isOnline()) > 0) {
					autoJoinMembers.add(new KWorldBossMemberSimpleImpl(role));
					roleData = KActivityRoleExtCaCreator.getWorldBossRoleData(roleId);
					roleData.notifyAutoJoin();
					if(roleData.getCurrentAutoJoinTime() >= KWorldBossConfig.getAutoJoinMaxCountPerTime()) {
						roleData.setAutoJoinStatus(false);
						removeIds.add(roleId);
					}
				} else {
					failIds.add(role.getId());
				}
			}
		}
		Date current = Calendar.getInstance().getTime();
		String dateStr = _SDF.format(current);
		if (autoJoinMembers.size() > 0) {
			List<IWorldBossMember> allMembers = new ArrayList<IWorldBossMember>(_joinMembers.size() + autoJoinMembers.size());
			allMembers.addAll(_joinMembers.values());
			allMembers.addAll(autoJoinMembers);
			Collections.sort(allMembers, _memberComparator);
			IWorldBossMember member;
			int rank;
			for (int i = 0; i < allMembers.size(); i++) {
				member = allMembers.get(i);
				rank = i + 1;
				if (member instanceof KWorldBossMemberSimpleImpl) {
					for (Iterator<KWorldBossRankReward> itr = rewardSet.iterator(); itr.hasNext();) {
						reward = itr.next();
						if (reward.beginRank >= rank && reward.endRank <= rank) {
							break;
						} else {
							reward = null;
						}
					}
					if(reward != null) {
						this.executeRankReward(member, reward, rank, WorldBossTips.getTipsAutoJoinMailTitle(), WorldBossTips.getTipsAutoJoinMailContent(dateStr, KWorldBossConfig.getAutoJoinPrice()));
					}
				}
			}
		}
		if(failIds.size() > 0) {
			for(int i = 0; i < failIds.size(); i++) {
				roleId = failIds.get(i);
				KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(roleId, WorldBossTips.getTipsAutoJoinFailMailTitle(), WorldBossTips.getTipsAutoJoinFailMailContent(dateStr));
			}
		}
		if (removeIds.size() > 0) {
			// 移除次数已满的人
			for (int i = 0; i < removeIds.size(); i++) {
				KWorldBossManager.removeAutoJoinRoleId(removeIds.get(i));
			}
		}
	}

	private void checkToRemove() {
		if (_aliveMap.size() > 0 && _dataStatus.compareAndSet(true, false)) {
			Map<Integer, KWorldBossMonsterInfo> map;
			List<Integer> removeList = new ArrayList<Integer>();
			Map.Entry<Integer, Integer> entry;
			for (Iterator<Map.Entry<Integer, Integer>> itr = _aliveMap.entrySet().iterator(); itr.hasNext();) {
				boolean remove = true;
				entry = itr.next();
				map = this._monsterInfos.get(entry.getValue());
				for (Iterator<KWorldBossMonsterInfo> itr2 = map.values().iterator(); itr2.hasNext();) {
					if (itr2.next().isAlive()) {
						remove = false;
						break;
					}
				}
				if (remove) {
					removeList.add(entry.getKey()); // key是instanceId
					itr.remove(); // 从aliveMap移除
				}
			}
			if (removeList.size() > 0) {
				// 发送消失的消息
				for (int i = 0; i < removeList.size(); i++) {
					_senceMap.removeCollisionEventObjectData(removeList.get(i));
				}
			}
		}
	}
	
	private void broadcast() {
		if (this._broadcastQueue.size() > 0) {
			String tips;
			while ((tips = _broadcastQueue.poll()) != null) {
				KSupportFactory.getChatSupport().sendSystemChat(tips, true, true);
			}
		}
	}
	
	private void syncBossHp() {
		if (this._updateBossHp.compareAndSet(true, false)) {
			long total = 0;
			long current = 0;
			KWorldBossMonsterInfo monsterInfo;
			KGameMessage msg = KGame.newLogicMessage(SM_SYNC_WORLD_BOSS_HP);
			msg.writeByte(_bosses.size());
			for (int i = 0; i < _bosses.size(); i++) {
				monsterInfo = _bosses.get(i);
				msg.writeInt(monsterInfo.getInstanceId());
				msg.writeLong(monsterInfo.getCurrentHp());
				total += monsterInfo.maxHp;
				current += monsterInfo.getCurrentHp();
			}
			this.sendMsgToAllMember(msg);
			if(_broadcastHpPct.size() > 0) {
				int pct = UtilTool.calculatePercentageL(current, total, false);
				int index = -1;
				for(int i = 0; i < _broadcastHpPct.size(); i++) {
					if(_broadcastHpPct.get(i) >= pct) {
						index = i;
						_broadcastHpPct.remove(i);
						break;
					}
				}
				if(index >= 0) {
					@SuppressWarnings("deprecation")
					KGameMessage broadcastMsg = KSupportFactory.getChatSupport().genSystemChatMsg(StringUtil.format(KWordBroadcastType.丧尸攻城_BOSS还剩余百分之x生命值.content, pct), KWordBroadcastType.丧尸攻城_BOSS还剩余百分之x生命值);
					this.sendMsgToAllMember(broadcastMsg);
				}
			}
		}
	}
	
	private void sendUpdateRankingMsg() {
		KGameMessage msg = KGame.newLogicMessage(SM_UPDATE_RANKING);
		msg.writeByte(_lastSendRankings.size());
		KWorldBossMember member;
		for(int i = 0; i < _lastSendRankings.size(); i++) {
			member = _lastSendRankings.get(i);
			msg.writeShort(i + 1);
			msg.writeUtf8String(member.getRoleName());
			msg.writeShort(member.getLevel());
//			msg.writeInt(member.getTotalDm());
			msg.writeLong(member.getTotalDm()); // 2014-09-15 从int改为long
		}
		this.sendMsgToAllMember(msg);
	}
	
	int getBossLv() {
		return _bossLv;
	}
	
	void setBossLv(int pLv) {
		this._bossLv = pLv;
	}

	/**
	 * 清理所有的成员，主要是让他们离开地图
	 */
	void sweepMembers() {
		KWorldBossMember member;
		for (Iterator<KWorldBossMember> itr = _joinMembers.values().iterator(); itr.hasNext();) {
			member = itr.next();
			if (member.isAvailable()) {
				if (!member.isFighting()) {
					if (member.hasSentFinishTips()) {
						member.setAvailable(false);
						member.setBeenSweeped(true);
						KSupportFactory.getDuplicateMapSupport().playerRoleLeaveDuplicateMap(KSupportFactory.getRoleModuleSupport().getRole(member.getRoleId()), _senceMap.getDuplicateId());
						_exitCount.incrementAndGet();
					} else {
						String tips = null;
						if (_killBossRoleName != null) {
							tips = WorldBossTips.getTipsActivityFinishedWithBossKilled(_killBossRoleName, _rankingMembers.indexOf(member) + 1);
						} else {
							tips = WorldBossTips.getTipsActivityFinished(_rankingMembers.indexOf(member) + 1);
						}
						KDialogService.sendSimpleDialog(member.getRoleId(), "", tips);
						member.setSentFinishTips(true);
					}
				} else {
					_LOGGER.info("》》》》{}正在战斗中，不能被移除《《《《", member.getRoleName());
				}
			} else {
				if (!member.hasBeenSweeped()) {
					_exitCount.incrementAndGet();
					member.setBeenSweeped(true);
				}
			}
		}
		if (_exitCount.get() >= _joinMembers.size()) {
			_LOGGER.info("》》》》世界boss活动，移除副本地图:{}《《《《", /*_worldBossFieldData.lvDescr,*/ _senceMap.getDuplicateId());
			KSupportFactory.getDuplicateMapSupport().removeDuplicateMap(_senceMap.getDuplicateId());
		}
	}

	void init(int pTimeIndex) {
		_timeIndex = pTimeIndex;
		_joinMembers.clear();
		_rankingMembers.clear();
		_lastSendRankings.clear();
		_monsterInfos.clear();
		_aliveMap.clear();
//		_genRuleDataMap.clear();
//		_genRuleDataMapAll.clear();
		_broadcastHpPct.clear();
		_bosses.clear();
		_exitCount.set(0);
		_finish = false;
		_canFight = false;
		_sortStatus.set(false);
		_dataStatus.set(false);
		_genMonsterKeys.clear();
//		_genRuleDataMap.clear();
		_fightingRoles.clear();
//		_refurbishWaiting = 0;
		_killBossRoleName = null;
//		_leftWaveCount = _worldBossFieldData.waveCount;
		_leftSeconds = KWorldBossConfig.getDurationSecond(); // 初始化的时候，初始化剩余秒数，这是为了防止，在还没开始的时候，没有onTimeSignal，客户端进入游戏的时候，剩余时间显示0
		for(int i = 0; i < KWorldBossConfig.getBroadcastHpPct().length; i++) {
			_broadcastHpPct.add(KWorldBossConfig.getBroadcastHpPct()[i]);
		}
		if(this._bossLv == 0) {
			this._bossLv = KWorldBossManager.getWorldBossStartLv();
		}
		int bossTemplateId = KWorldBossManager.getBossTemplateIdByLv(_bossLv);
		Collections.sort(_broadcastHpPct);
		Collections.reverse(_broadcastHpPct);
		_senceMap = KSupportFactory.getDuplicateMapSupport().createDuplicateMap(this._worldBossFieldData.mapId);
		_senceMap.setCollisionEventListener(this);
		@SuppressWarnings("unchecked")
		List<CollisionEventObjectData> list = _senceMap.getAllCollisionEventObject();
		Collections.sort(list, _mapMonsterComparator);
		int instanceId = 0;
		int eSize = list.size();
		List<Integer> genIdList = new ArrayList<Integer>();
		List<KWorldBossMonsterKey> monsterIds = _worldBossFieldData.monsterIds;
		KWorldBossMonsterKey tempKey;
		KWorldBossMonsterInfo monsterInfo;
		Map<Integer, KWorldBossMonsterInfo> map;
		Map<Integer, ICombatMonsterUpdateInfo> combatMap;
		for (int i = 0; i < eSize; i++) {
			instanceId = _instanceIdGenerator.incrementAndGet();
			genIdList.add(instanceId);
			map = new HashMap<Integer, KWorldBossMonsterInfo>();
			combatMap = new HashMap<Integer, ICombatMonsterUpdateInfo>();
			_monsterInfos.put(instanceId, map);
			_monsterInfosForCombat.put(instanceId, combatMap);
			_monsterBattlefields.put(instanceId, _worldBossFieldData.getBattlefield());
			for (int k = 0; k < monsterIds.size(); k++) {
				tempKey = monsterIds.get(k);
				monsterInfo = new KWorldBossMonsterInfo(bossTemplateId, tempKey.instanceId, _bossLv);
				map.put(tempKey.instanceId, monsterInfo);
				combatMap.put(tempKey.instanceId, monsterInfo);
				_bosses.add(monsterInfo);
			}
		}
		CollisionEventObjectData temp;
		for (int i = 0; i < list.size(); i++) {
			instanceId = genIdList.get(i);
			temp = list.get(i);
			temp.setAttachment(instanceId);
			_aliveMap.put(temp.getMapInstanceId(), instanceId);
			_instanceIdToMapInstanceId.put(instanceId, temp.getMapInstanceId());
		}
	}
	
	void start() {
		this._canFight = true;
	}
	
	void warnUpLeftTimePass(int currentLeft) {
		_warnUpLeftSeconds = currentLeft;
	}

	void onTimeSignal(int leftSeconds) throws Exception {
		if (!_finish) {
			int preLeft = this._leftSeconds;
			this._leftSeconds = leftSeconds;
			try {
				this.sortRanking();
			} catch (Exception e) {
				throw e;
			}
			try {
				this.checkToRemove();
			} catch (Exception e) {
				throw e;
			}
			try {
				this.broadcast();
			} catch (Exception e) {
				throw e;
			}
			try {
				this.syncBossHp();
			} catch (Exception e) {
				throw e;
			}
			_finish = checkFinished(preLeft - leftSeconds);
			if (_finish) {
				this.settle();
			}
		}
	}
	
	void timeBroadcast(int leftMin) {
		if (!_finish) {
			@SuppressWarnings("deprecation")
			KGameMessage msg = KSupportFactory.getChatSupport().genSystemChatMsg(StringUtil.format(KWordBroadcastType.丧尸攻城_丧尸攻城活动结束时间剩余x分钟.content, leftMin), KWordBroadcastType.丧尸攻城_丧尸攻城活动结束时间剩余x分钟);
			sendMsgToAllMember(msg);
		}
	}

	void notifyActivityTimeOut() {
		if (!_finish) {
			this._finish = true;
			this.settle();
		}
		this.sweepMembers();
	}
	
	List<KWorldBossRankingData> getRankingData() {
		List<KWorldBossRankingData> list = new ArrayList<KWorldBossActivityMain.KWorldBossRankingData>(_rankingMembers.size());
		KWorldBossMember member;
		for (int i = 0; i < _rankingMembers.size(); i++) {
			member = _rankingMembers.get(i);
			list.add(new KWorldBossRankingData(i + 1, member.getRoleName(), member.getLevel(), member.getTotalDm()));
		}
		return list;
	}

	final KWorldBossMember getWorldBossMember(long roleId) {
		return _joinMembers.get(roleId);
	}

	boolean joinActivity(KRole role) {
		KWorldBossMember member = this.getWorldBossMember(role.getId());
		boolean joinSuccess = false;
		if (member != null) {
			joinSuccess = true;
		} else /*if (this.matchLv(role))*/ {
			member = new KWorldBossMember(role, _monsterInfos.size());
			this.addJoinMember(member);
			joinSuccess = true;
			KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.参加丧尸攻城活动);
			KActionRecorder.recordAction(role.getId(), KActionType.ACTION_TYPE_WORLDBOSS, 1);
		}
		if (joinSuccess) {
			member.setAvailable(true);
			// **** 发送活动数据BEGIN
			KWorldBossRoleData worldBossRoleData = KActivityRoleExtCaCreator.getWorldBossRoleData(role.getId());
			KWorldBossMonsterInfo monsterInfo;
			KGameMessage msg = KGame.newLogicMessage(SM_SEND_WORLD_BOSS_BASIC_DATA);
			msg.writeLong(member.getTotalDm());
			msg.writeBoolean(worldBossRoleData.isShowIntroduce());
			msg.writeInt(_warnUpLeftSeconds);
			msg.writeInt(_leftSeconds);
			msg.writeUtf8String(KWorldBossConfig.getActivityName());
			msg.writeByte(_bosses.size());
			for (int i = 0; i < _bosses.size(); i++) {
				monsterInfo = _bosses.get(i);
				msg.writeInt(monsterInfo.getInstanceId());
				msg.writeInt(monsterInfo.headResId);
				msg.writeLong(monsterInfo.maxHp);
				msg.writeLong(monsterInfo.getCurrentHp());
			}
			boolean gotLock = false;
			try {
				gotLock = _rankingLock.readLock().tryLock(2l, TimeUnit.SECONDS);
			} catch (Exception e) {
				_LOGGER.error("获取排行榜读锁出错！", e);
			}
			if (gotLock) {
				try {
					KWorldBossMember tempMember;
					msg.writeByte(_lastSendRankings.size());
					for (int i = 0; i < _lastSendRankings.size(); i++) {
						tempMember = _lastSendRankings.get(i);
						msg.writeShort(i + 1);
						msg.writeUtf8String(tempMember.getRoleName());
						msg.writeShort(tempMember.getLevel());
//						msg.writeInt(tempMember.getTotalDm());
						msg.writeLong(tempMember.getTotalDm()); // 2014-09-15 从int改为long
					}
				} finally {
					_rankingLock.readLock().unlock();
				}
			} else {
				msg.writeByte(0);
			}
			KWorldBossMessageHandler.packInspireData(msg, member);
			boolean coolingDown = !member.coolDownFinish();
			msg.writeBoolean(coolingDown);
			if (coolingDown) {
				msg.writeInt(member.getWaitingAliveSecond());
				msg.writeShort(KWorldBossConfig.getRelivePrice());
			}
			role.sendMsg(msg);
			// *** 发送活动数据END
			KSupportFactory.getDuplicateMapSupport().playerRoleJoinDuplicateMap(role, _senceMap.getDuplicateId());
		}
		return joinSuccess;
	}
	
	void leaveActivity(KRole role) {
		KWorldBossMember member = this._joinMembers.get(role.getId());
		if (member != null && member.isAvailable()) {
			member.setAvailable(false);
			KSupportFactory.getDuplicateMapSupport().playerRoleLeaveDuplicateMap(role, _senceMap.getDuplicateId());
		}
	}

	CommonResult processCombatStart(KRole role, int genId) {
		CommonResult result = new CommonResult();
		if (!this._finish) {
			KWorldBossMember member = this.getWorldBossMember(role.getId());
			if (!member.isAvailable()) {
				result.tips = WorldBossTips.getTipsYouAreNotInActivity();
			} else if (!member.coolDownFinish()) {
				result.tips = WorldBossTips.getTipsYouAreWaitingToRelive();
			} else {
				Map<Integer, KWorldBossMonsterInfo> map = this._monsterInfos.get(genId);
				if (map != null) {
					List<Long> robotIds = new ArrayList<Long>(_MAX_ROBOT_SIZE);
					int count = 0;
					Map.Entry<Long, Boolean> entry;
					for (Iterator<Map.Entry<Long, Boolean>> itr = _fightingRoles.entrySet().iterator(); itr.hasNext();) {
						entry = itr.next();
						if (entry.getKey() != role.getId()) {
							robotIds.add(entry.getKey());
							count++;
							if (count == _MAX_ROBOT_SIZE) {
								break;
							}
						}
					}
					Map<Integer, ICombatMonsterUpdateInfo> combatMonsterMap = this._monsterInfosForCombat.get(genId);
					KGameBattlefield battlefield = this._monsterBattlefields.get(genId);
					boolean someAlive = false;
					synchronized (map) {
						KWorldBossMonsterInfo monsterInfo;
						for (Iterator<KWorldBossMonsterInfo> itr = map.values().iterator(); itr.hasNext();) {
							monsterInfo = itr.next();
							if (monsterInfo.isAlive()) {
								someAlive = true;
								break;
							}
						}
						if (someAlive) {
							member.getCombatAttachment().fillData(genId, map);
							KActionResult<Integer> combatResult = KSupportFactory.getCombatModuleSupport().fightByUpdateInfoWithRobots(role, battlefield, member, combatMonsterMap, robotIds, KCombatType.WORLD_BOSS,
									this._worldBossFieldData.templateId);
							result.tips = combatResult.tips;
							result.isSucess = combatResult.success;
							if (result.isSucess) {
								member.setFighting(true);
								_fightingRoles.put(member.getRoleId(), _PRESENT);
							}
						} else {
							result.tips = WorldBossTips.getTipsMonsterIsDead();
						}
					}
				} else {
					result.tips = WorldBossTips.getTipsMonsterNotExists();
				}
			}
		} else {
			result.tips = WorldBossTips.getTipsWorldBossFieldFinish();
		}
		return result;
	}

	void processCombatFinished(long roleId, ICombatCommonResult roleResult, ICombatGlobalCommonResult globalResult) {
		KWorldBossMember member = this.getWorldBossMember(roleId);
		if (member != null) {
			_fightingRoles.remove(member.getRoleId());
			if (!this._finish) {
				Map<Integer, KWorldBossMonsterInfo> map = this._monsterInfos.get(member.getCombatAttachment().getGenRuleInstanceId());
				Map<Integer, Long> hpMap = member.getCombatAttachment().getCurrentHpInfo();
				Map<Integer, Long> resultMap = globalResult.getMonsterHpInfo();
				KWorldBossMonsterInfo monsterInfo;
				long srcHp;
				Long resultHp;
				boolean aliveStatus;
				long dm;
				long totalDm = 0;
				StringBuilder strKillName = null;
				synchronized (map) {
					for (Iterator<KWorldBossMonsterInfo> itr = map.values().iterator(); itr.hasNext();) {
						monsterInfo = itr.next();
						srcHp = hpMap.get(monsterInfo.getInstanceId());
						resultHp = resultMap.get(monsterInfo.getInstanceId());
						if (resultHp != null) {
							if (resultHp < 0) {
								resultHp = 0l;
							} else if (srcHp == resultHp) {
								continue;
							}
							aliveStatus = monsterInfo.isAlive();
							dm = (srcHp - resultHp);
							totalDm += dm;
							_sortStatus.compareAndSet(false, true);
							monsterInfo.decreaseHp(dm);
							if (aliveStatus && !monsterInfo.isAlive()) {
								_dataStatus.compareAndSet(false, true);
								member.setKillBoss(true);
//								member.addKillMonster(_genRuleDataMap.get(member.getCombatAttachment().getGenRuleInstanceId()).ruleTemplateId);
								if (monsterInfo.isBoss) {
									if (strKillName == null) {
										strKillName = new StringBuilder();
									} else {
										strKillName.append("、");
									}
									strKillName.append(monsterInfo.name);
								}
							}
							if (monsterInfo.isBoss) {
								_updateBossHp.compareAndSet(false, true);
							}
						}
					}
				}
				if (totalDm > 0) {
					member.recordDm(member.getCombatAttachment().getGenRuleInstanceId(), totalDm);
				}
				if (!roleResult.isWin()) {
					member.setLastFinishTime(System.currentTimeMillis());
				}
				// KDialogService.sendFunDialog(roleId, "",
				// WorldBossTips.getTipsCombatResult(totalDm),
				// RESULT_BUTTONS, true, (byte) -1);
				if (strKillName != null) {
					_broadcastQueue.add(StringUtil.format(KWordBroadcastType.丧尸攻城_恭喜x角色成功击杀x怪物.content, member.getRoleNameEx(), strKillName));
					_killBossRoleName = member.getRoleName();
				}
				KWorldBossMessageHandler.sendCombatResult(member, totalDm);
			} else {
				// KDialogService.sendFunDialog(roleId, "",
				// WorldBossTips.getTipsCombatResultActivityEnd(),
				// RESULT_BUTTONS, true, (byte) -1);
				KWorldBossMessageHandler.sendCombatResult(member, roleResult.getTotalDamage());
			}
		}
		// member.setFighting(false);
	}

	boolean isFinish() {
		return _finish;
	}

//	KGenRuleData getGenRuleData(int instanceId) {
//		return _genRuleDataMapAll.get(instanceId);
//	}

//	float getDmBasicPara() {
//		return _worldBossFieldData.dmBasicPara;
//	}
	
	boolean areAllMemberLeft() {
		return _exitCount.get() >= _joinMembers.size();
	}
	
	void processReturnToMap(KRole role) {
		KSupportFactory.getDuplicateMapSupport().resetPlayerRoleToBornPoint(role, (KDuplicateMapBornPoint) _senceMap.getAllBornPointEntity().get(0));
		KSupportFactory.getMapSupport().processRoleFinishBattleReturnToMap(role);
	}

	public int getFieldId() {
		return _worldBossFieldData.templateId;
	}

	@Override
	public void notifyPlayerRoleCollisionEvent(KRole role, CollisionEventObjectData data) {
		if (_canFight && !role.isFighting()) {
			int genId = (Integer) data.getAttachment();
			this.processCombatStart(role, genId);
		}
	}
	
	@Override
	public void notifyPlayerRoleCollisionOtherRole(KRole role, KRole otherRole) {
		
	}

	public static class KWorldBossMonsterInfo implements ICombatMonsterUpdateInfo {

		private final int _templateId;
		private final int _instanceId;
		public final boolean isBoss;
		public final long maxHp;
		public final int headResId;
		public final String name;
		public final int lv;
		private AtomicLong _currentHp;

		KWorldBossMonsterInfo(int pTemplateId, int pInstanceId, int pLv) {
			this._templateId = pTemplateId;
			this._instanceId = pInstanceId;
			KMonstTemplate template = KSupportFactory.getNpcModuleSupport().getMonstTemplate(pTemplateId);
			this.maxHp = template.allEffects.get(KGameAttrType.MAX_HP);
			this._currentHp = new AtomicLong(this.maxHp);
			this.isBoss = template.getMonsterTypeEnum() == KMonsterType.BOSS;
			this.headResId = template.monstUIData.monster_head;
			this.name = template.name;
			this.lv = pLv;
		}

		final boolean isAlive() {
			// 是否生存
			return _currentHp.get() > 0;
		}

		final void decreaseHp(long value) {
			if (this.isAlive()) {
				_currentHp.getAndAdd(-value);
			}
			if (_currentHp.get() < 0) {
				_currentHp.getAndSet(0);
			}
		}

		@Override
		public int getTemplateId() {
			return _templateId;
		}

		@Override
		public int getInstanceId() {
			return _instanceId;
		}

		@Override
		public long getCurrentHp() {
			return this._currentHp.get();
		}
	}
	
	public static class KWorldBossMemberSimpleImpl implements IWorldBossMember {

		private long _roleId;
		private String _roleName;
		private int _roleLv;
		private int _battlePower;
		
		KWorldBossMemberSimpleImpl (KRole role) {
			this._roleId = role.getId();
			this._roleName = role.getName();
			this._roleLv = role.getLevel();
			this._battlePower = role.getBattlePower();
		}
		
		@Override
		public long getRoleId() {
			return _roleId;
		}

		@Override
		public String getRoleName() {
			return _roleName;
		}

		@Override
		public int getLevel() {
			return _roleLv;
		}

		@Override
		public int getBattlePower() {
			return _battlePower;
		}
		
	}
}
