package com.kola.kmp.logic.combat.impl;

import static com.kola.kmp.protocol.fight.KFightProtocol.CM_BAEELE_COMMAND_EXIT;
import static com.kola.kmp.protocol.fight.KFightProtocol.CM_BATTLE_COMMAND;
import static com.kola.kmp.protocol.fight.KFightProtocol.CM_BATTLE_STATUS;
import static com.kola.kmp.protocol.fight.KFightProtocol.CM_VERIFY_BATTLE_TIMESTAMP;
import static com.kola.kmp.protocol.fight.KFightProtocol.SM_BAEELE_COMMAND_EXIT;
import static com.kola.kmp.protocol.fight.KFightProtocol.SM_BATTLE_COMMAND;
import static com.kola.kmp.protocol.fight.KFightProtocol.SM_BATTLE_OBJECT_DIE;
import static com.kola.kmp.protocol.fight.KFightProtocol.SM_SYNC_ENTER_BATTLE;
import static com.kola.kmp.protocol.fight.KFightProtocol.SM_SYNC_LEFT_TIME_TO_CLIENT;
import static com.kola.kmp.protocol.fight.KFightProtocol.SM_VERIFY_BATTLE_TIMESTAMP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatConstant;
import com.kola.kmp.logic.combat.ICombatDropInfo;
import com.kola.kmp.logic.combat.ICombatEventListener;
import com.kola.kmp.logic.combat.ICombatForce;
import com.kola.kmp.logic.combat.ICombatGround;
import com.kola.kmp.logic.combat.ICombatGround.ICombatGroundBuilder;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.ICombatMinion;
import com.kola.kmp.logic.combat.ICombatPlugin;
import com.kola.kmp.logic.combat.IOperationResult;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.combat.cmd.ICombatCommand;
import com.kola.kmp.logic.combat.cmd.ICombatCommandCreator;
import com.kola.kmp.logic.combat.operation.IOperation;
import com.kola.kmp.logic.combat.operation.IOperationMsgHandler;
import com.kola.kmp.logic.combat.resulthandler.ICombatGameLevelInfo;
import com.kola.kmp.logic.combat.vitorycondition.IVitoryCondition;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatImpl implements ICombat {
	
//	private static final int _ZIP_PAYLOAD_LENGTH = 2048;
	private static final int ALLOW_IDLE_MILLIS = ICombatConstant.ONE_MINUTE_MILLIS * 4;
	private static final AtomicInteger _idGenerator = new AtomicInteger();
	private static final Comparator<IOperation> _operationSorter = new Comparator<IOperation>() {
		
		@Override
		public int compare(IOperation o1, IOperation o2) {
			if(o1.getOperationTime() > o2.getOperationTime()) {
				return 1;
			} else if (o1.getOperationTime() == o2.getOperationTime()) {
				return o1.getPriority() > o2.getPriority() ? 1 : 0;
			} else {
				return -1;
			}
//			return o1.getOperationTime() > o2.getOperationTime() ? 1 : (o1.getOperationTime() == o2.getOperationTime() ? 0 : -1);
		}
	};
	private final Map<Integer, ICombatGround> _combatGroundMap = new LinkedHashMap<Integer, ICombatGround>(); // 战场里面所有的战斗场景，key=战斗场景的模板id，value=战斗场景对象
	private List<ICombatForce> _allMonsterForces;
	private List<ICombatForce> _allMonsterForcesRO; // 所有的怪物势力
	private final List<KCombatAnimation> _animationList = new ArrayList<KCombatAnimation>(); // 战场的环境列表
	private final List<KCombatMessageChannel> _allChannels = new ArrayList<KCombatMessageChannel>(); // 与客户端收发消息的通道
	private final List<IOperationResult> _roundOpResults = new ArrayList<IOperationResult>(); // 战斗指令的结果列表
	private final Queue<IOperation> _extractOp = new ConcurrentLinkedQueue<IOperation>(); // 额外的指令
	private final List<Short> _roundDeadMembers = new ArrayList<Short>(); // 每执行一轮指令后，死亡的对象列表
	private final ReadWriteLock _opResultLock = new ReentrantReadWriteLock(); // 操作结果集合的读写锁
	private final ReadWriteLock _deadMemberLock = new ReentrantReadWriteLock(); // 死亡对象集合的读写锁
	private KCombatResultImpl _combatResult = new KCombatResultImpl(); // 战斗结果
	private final Map<Short, Short> _masterIdMapMountId = new HashMap<Short, Short>(); // key=主人的id，value=坐骑的id
	private final Map<Short, Short> _petIdMapMasterId = new HashMap<Short, Short>(); // key=随从的shadowId，value=主人的shadowId
	private final Map<Short, Short> _minionIdMapMasterId = new HashMap<Short, Short>(); // key=召唤物id，value=主人的shadowId
	private final Map<Long, Short> _masterSrcIdOfPets = new HashMap<Long, Short>();
	private final List<ICombatMember> _robotMembers = new ArrayList<ICombatMember>();
	private final List<Integer> _alreadyPickUpDropIds = new ArrayList<Integer>();
	private final List<Short> _syncHpList = new ArrayList<Short>(10);
	private final Set<Integer> _timeAlreadySync = new HashSet<Integer>();
//	private final List<ICombatMember> _terminateByTimeMembers = new ArrayList<ICombatMember>();
	private Map<Short, ICombatMember> _allMembers; // 所有的战场成员集合
	private final Map<Byte, List<ICombatMember>> _membersByType = new HashMap<Byte, List<ICombatMember>>();
	private Map<Long, Short> _roleIdToShadowId;
	private ICombatForce _roleForce; // 角色势力
	private ICombatForce _extractMonsterForce; // 额外的怪物势力
	private ICombatForce _extractNeturaForce; // 额外的中立实例
	private IOperationMsgHandler _opMsgHandler; // 战场指令的消息处理器
	private ICombatCommandCreator _cmdCreator;
	
	private int _combatId; // 战场唯一的自生成id
	private short _maxShadowId; // 战场当前最大的实例对象id
	private int _validationCode; // 战场验证码
	private int _randomSeed; // 战场随机种子，用于进行各种随机，这个随机种子将发送到客户端，以达到随机同步
	private boolean _canAutoFight; // 能否自动战斗
	private Random _randomInstance; // 战场的随机器
	private int _currentCombatGroundKey; // 当前战场的场景id
	private KCombatType _combatType; // 战斗类型
	private boolean _alsoSendFinishMsgIfEscape; // 如果角色逃跑，是否发送战斗结束消息
	private int _timeOutMillis; // 限时时长（单位：毫秒）
	private int _totalWaveCount; // 总共的怪物波数（塔防战场特有）
	private List<ICombatEventListener> _preEventList = new ArrayList<ICombatEventListener>();
	
	private KCombatTimer _timer; // 战场时效任务
	
	private IVitoryCondition _vitoryCondition; // 胜利条件
	
	private long _combatStartTime; // 开始时间
	private long _combatPauseTime;
	private long _combatEndTime; // 结束时间
	private int _currentUseTime; // 当前使用的时间（单位：毫秒）
	private long _totalPauseTime; // 总共暂停的时间
	
	private boolean _serverStart; // 战斗是否已经开始
	private boolean _clientStart; // 客户端是否已经开始
	private boolean _terminal; // 战斗是否已经结束
	
	private void verifyTime(long roleId, KGameMessage msg) {
		long preTime = msg.readLong();
		KCombatMessageChannel channel = this.getChannelByRoleId(roleId);
		if (channel != null) {
			channel.setDelay((System.currentTimeMillis() - preTime) / 2);
		}
		if (!_serverStart) {
			// 第一次收到verifyTime，表示与客户端校对时间完毕，正式开始战斗
			this.submitCommand(_cmdCreator.createStartCombatCommand(this));
		} else {
			// 后面收到verifyTime，表示是服务器测试与客户端是否中断连接
			LOGGER.info("roleId={}，战场id={}，仍然在战场中！", roleId, _combatId);
		}
	}
	
	private KCombatMessageChannel getChannelByRoleId(long roleId) {
		KCombatMessageChannel channel;
		for (int i = 0; i < _allChannels.size(); i++) {
			channel = _allChannels.get(i);
			if(channel.getRoleId() == roleId) {
				return channel;
			}
		}
		return null;
	}
	
	private void sendVerifyTimeMsg(KCombatMessageChannel channel) {
		KGameMessage msg = KGame.newLogicMessage(SM_VERIFY_BATTLE_TIMESTAMP);
		msg.writeLong(System.currentTimeMillis());
		if (channel == null) {
			this.sendMsgToAll(msg);
			for (int i = 0; i < _allChannels.size(); i++) {
				_allChannels.get(i).setLastCommunicateTime(System.currentTimeMillis());
			}
		} else {
			channel.sendMsg(msg);
		}
	}
	
	private void putMembersToMap(List<ICombatMember> memberList) {
		ICombatMember member;
		List<ICombatMember> list;
		for(int i = 0; i < memberList.size(); i++) {
			member = memberList.get(i);
			this._allMembers.put(member.getShadowId(), member);
			list = this._membersByType.get(member.getMemberType());
			if(list == null) {
				list = new ArrayList<ICombatMember>();
				this._membersByType.put(member.getMemberType(), list);
			}
			list.add(member);
		}
	}
	
	private KGameMessage createCombatInfoMsg() {
		List<ICombatMember> roleMembers = _roleForce.getAllMembers();
		ICombatGround ground;
		KGameMessage msg = KGame.newLogicMessage(SM_SYNC_ENTER_BATTLE);
//		msg.setEncryption(KGameMessage.ENCRYPTION_ZIP);
		msg.writeByte(_combatType.sign);
		switch (_combatType) {
		case BARREL:
		case COMPETITION:
		case TRANSPORT_COMBAT:
			msg.writeShort((int) TimeUnit.SECONDS.convert(_timeOutMillis, TimeUnit.MILLISECONDS));
			break;
		case TOWER_COMBAT:
			msg.writeByte(_totalWaveCount);
			break;
		default:
			break;
		}
		msg.writeInt(_validationCode);
		msg.writeInt(_randomSeed);
		msg.writeBoolean(_canAutoFight); // 能否自动战斗
		msg.writeBoolean(_combatType.isUseServerResIdOfMonster());
		msg.writeShort(_maxShadowId);
//		LOGGER.info("maxShadowId={}", _maxShadowId);
		msg.writeByte(roleMembers.size() + _robotMembers.size());
		for(int i = 0; i < roleMembers.size(); i++) {
			roleMembers.get(i).packDataToMsg(msg);
			msg.writeBoolean(false); // 是否有掉落
		}
		for(int i = 0; i < _robotMembers.size(); i++) {
			// 机器人
			_robotMembers.get(i).packDataToMsg(msg);
			msg.writeBoolean(false); // 是否有掉落
		}
		msg.writeByte(_combatGroundMap.size());
		for(Iterator<ICombatGround> itr = _combatGroundMap.values().iterator(); itr.hasNext();) {
			ground = itr.next();
			msg.writeInt(ground.getBattleFieldTemplateId());
			msg.writeUtf8String(ground.getMapResPath());
			msg.writeInt(ground.getBgAudioResId());
			ground.packMemberDataToMsg(msg);
			msg.writeInt(ground.getNextBattleFieldTemplateId());
		}
		msg.writeInt(_currentCombatGroundKey);
		int index = msg.writerIndex();
		msg.writeByte(0);
		Map<Integer, ICombatMinion> tempMap;
		int size = 0;
		ICombatMinion tempMinion;
		for (Iterator<ICombatMember> itr = _allMembers.values().iterator(); itr.hasNext();) {
			tempMap = itr.next().getCombatMinions();
			if (tempMap != null) {
				for (Iterator<ICombatMinion> minionItr = tempMap.values().iterator(); minionItr.hasNext();) {
					tempMinion = minionItr.next();
					tempMinion.packDataToMsg(msg);
				}
				size += tempMap.size();
			}
		}
		msg.setByte(index, size);
		msg.writeByte(_animationList.size());
		for(int i = 0; i < _animationList.size(); i++) {
			_animationList.get(i).packDataToMsg(msg);
		}
//		if(msg.getPayloadLength() > _ZIP_PAYLOAD_LENGTH) {
//			msg.setEncryption(KGameMessage.ENCRYPTION_ZIP);
//		}
		return msg;
	}
	
	/**
	 * 发送战场消息到客户端
	 * 消息体详见：{@link com.kola.kmp.protocol.fight.KFightProtocol#SM_SYNC_ENTER_BATTLE}
	 */
	private void packCombatMsgAndSend() {
		KGameMessage msg = this.createCombatInfoMsg();
		sendMsgToAll(msg);
		LOGGER.info("战场id：{}，角色：{}，已发送战场内容消息！", _combatId, _allChannels.get(0).getRoleId());
	}
	
	/**
	 * <pre>
	 * 处理消息{@link com.kola.kmp.protocol.fight.KFightProtocol#CM_BATTLE_COMMAND}
	 * <pre>
	 * @param msg
	 */
	private void handleCommandMsg(long roleId, KGameMessage msg) {
//		if (!this.isTerminal()) {
		int validationCode = msg.readInt();
		if (validationCode == _validationCode) {
			List<IOperation> opList = _opMsgHandler.handleOperationMsg(roleId, this, msg);
			if (opList.size() > 0) {
//				long begin = opList.get(0).getOperationTime();
//				long end = opList.get(opList.size() - 1).getOperationTime();
//				if (begin == end) {
//					begin = 0;
//				}
//				List<IOperation> tempList;
//				for (Iterator<ICombatMember> itr = this._allMembers.values().iterator(); itr.hasNext();) {
//					tempList = itr.next().getPeriodOperation(begin, end);
//					if (tempList != null && tempList.size() > 0) {
//						opList.addAll(tempList);
//					}
//				}
				Collections.sort(opList, _operationSorter);
				this.submitCommand(_cmdCreator.createCombatOperationCommand(this, opList));
			} else {
				byte[] data = new byte[msg.getPayloadLength()];
				msg.getBytes(KGameMessage.LENGTH_OF_HEADER, data);
				LOGGER.error("opList的长度为0！！validationCode={}, 消息体的数据：{}, 角色id：{}", _validationCode, data, roleId);
			}
		} else {
			byte[] data = new byte[msg.getPayloadLength()];
			msg.getBytes(KGameMessage.LENGTH_OF_HEADER, data);
			LOGGER.error("验证码不通过！！服务器验证码：{}，客户端验证码：{}，消息体数据：{}，角色id：{}", _validationCode, validationCode, data, roleId);
		}
//		}
	}
	
	private void handleBattleStatusMsg(long roleId, KGameMessage msg) {
		byte status = msg.readByte();
		switch(status) {
		case STATUS_EXIT_FINISH:
			// 客户端退出战斗完毕
			LOGGER.info("角色：{}，退出战斗完成通知，战场id：{}", roleId, _combatId);
			this.handleClientExitFinished(roleId);
			break;
		case STATUS_START:
			// 客户端开始战斗，设置开始时间
			if (_combatStartTime > 0) {
				LOGGER.error("角色：{}，战场id：{}，重复发送开始战斗状态！当前时间：{}，战斗开始时间：{}", roleId, _combatId, System.currentTimeMillis(), _combatStartTime);
				break;
			} else {
				KCombatMessageChannel channel = this.getChannelByRoleId(roleId);
				if (channel != null) {
					long currentTime = System.currentTimeMillis();
					_combatStartTime = currentTime - channel.getDelay();
					LOGGER.info("角色：{}，战场id：{}，开始战斗，当前时间：{}，预计延迟：{}，战斗开始时间：{}", roleId, _combatId, currentTime, channel.getDelay(), _combatStartTime);
				} else {
					_combatStartTime = System.currentTimeMillis();
				}
				for(Iterator<ICombatGround> itr =_combatGroundMap.values().iterator(); itr.hasNext();) {
					itr.next().notifyStart(this);
				}
				_clientStart = true;
			}
			break;
		case STATUS_FINISH:
			short maxCtnHitCount = msg.readShort(); // 最大连击数
			LOGGER.info("角色：{}，战场id：{}，通知战斗结束，最大连击数：{}", roleId, _combatId, maxCtnHitCount);
			break;
		case STATUS_CLIENT_END:
			boolean isClientWin = msg.readBoolean();
			if (this._combatType == KCombatType.OFFLINE_COMBAT) {
				LOGGER.info("角色：{}，战场id：{}，客户端通知战斗结束，是否胜利？{}", roleId, _combatId, isClientWin);
				this.submitCommand(_cmdCreator.createKillAllMemberCommand(this, isClientWin));
			} else {
				LOGGER.warn("角色：{}，战场id：{}，客户端通知战斗结束，是否胜利？{}，但是战斗类型不是离线战斗！不做任何处理！", roleId, _combatId, isClientWin);
			}
			break;
		case STATUS_PAUSE:
			_combatPauseTime = System.currentTimeMillis();
			break;
		case STATUS_CONTINUE:
			int pauseTime = (int)(System.currentTimeMillis() - _combatPauseTime);
			if(pauseTime > 0) {
				_totalPauseTime += pauseTime;
			}
			break;
		}
	}
	
	private void destroyWaitingTimeOut() {
		KCombatMessageChannel channel = null;
		for (int i = 0; i < _allChannels.size(); i++) {
			channel = _allChannels.get(i);
			if (!channel.isExit()) {
				setClientExit(channel);
			}
		}
	}
	
	private void setClientExit(KCombatMessageChannel channel) {
		channel.setExit();
		KCombatManager.setRoleNotFighting(channel.getRoleId());
		KCombatManager.processRoleExitCombatFinish(channel.getRoleId(), this, this._combatResult);
	}
	
	private void handleClientEscape(long roleId) {
		KCombatMessageChannel channel = this.getChannelByRoleId(roleId);
		if (channel != null) {
			if (this.isTerminal()) {
				setClientExit(channel);
				KCombatManager.processRoleEscapeAfterCombatFinish(roleId);
			} else {
				ICombatMember member = this.getCombatMember(channel.getShadowId());
				member.setEscape(true);
			}
		}
	}
	
	private void checkSyncHp() {
		if (this._syncHpList.size() > 0) {
			ICombatMember member;
			for (int i = 0; i < _syncHpList.size(); i++) {
				member = this.getCombatMember(_syncHpList.get(i));
				if (member != null && member.isAlive()) {
					this._roundOpResults.add(new KSyncHpOperationResult(member.getShadowId(), member.getCurrentHp()));
				}
			}
			LOGGER.info("战场id：{}，同步HP，待同步的单位：{}", _combatId, _syncHpList);
			this._syncHpList.clear();
		}
	}
	
	private void checkAndSendOperationResult() {
		if (this._roundOpResults.size() > 0) {
			_opResultLock.writeLock().lock();
			List<IOperationResult> copy = null;
			try {
				copy = new ArrayList<IOperationResult>(_roundOpResults);
				_roundOpResults.clear();
			} finally {
				_opResultLock.writeLock().unlock();
			}
			if (copy != null) {
				IOperationResult result;
				int size = copy.size();
				KGameMessage msg = KGame.newLogicMessage(SM_BATTLE_COMMAND);
//				msg.setEncryption(KGameMessage.ENCRYPTION_ZIP);
				msg.writeInt(_validationCode);
				msg.writeByte(size);
				for (int i = 0; i < size; i++) {
					result = copy.get(i);
					msg.writeByte(result.getOperationType());
					result.fillMsg(msg);
				}
				this.sendMsgToAll(msg);
			}
		}
	}
	
	private void checkAndSendDeadMembers() {
		if (this._roundDeadMembers.size() > 0) {
			List<Short> copy = null;
			_deadMemberLock.writeLock().lock();
			try {
				copy = new ArrayList<Short>(_roundDeadMembers);
				_roundDeadMembers.clear();
			} finally {
				_deadMemberLock.writeLock().unlock();
			}
			if(copy != null) {
				short tempId;
				LOGGER.info("战场id：{}，死亡的成员：{}", this._combatId, copy.toString());
				KGameMessage msg = KGame.newLogicMessage(SM_BATTLE_OBJECT_DIE);
				msg.writeByte(copy.size());
				for(int i = 0; i < copy.size(); i++) {
					tempId = copy.get(i);
//					LOGGER.info("！！！！[{}]已经死亡！！！！", tempId);
					msg.writeShort(tempId);
				}
				this.sendMsgToAll(msg);
			}
		}
	}
	
	private void syncTimeToClient(int leftSecond) {
//		LOGGER.info("同步剩余时间，战场id：{}，剩余秒数：{}", this._combatId, leftSecond);
		_timeAlreadySync.add(leftSecond);
		KGameMessage msg = KGame.newLogicMessage(SM_SYNC_LEFT_TIME_TO_CLIENT);
		msg.writeShort(leftSecond);
		this.sendMsgToAll(msg);
	}
	
	
	private List<ICombatMember> getMinionSummoned(short masterShadowId) {
		if (this._minionIdMapMasterId.size() > 0) {
			List<ICombatMember> list = new ArrayList<ICombatMember>();
			Map.Entry<Short, Short> entry;
			for (Iterator<Map.Entry<Short, Short>> itr = _minionIdMapMasterId.entrySet().iterator(); itr.hasNext();) {
				entry = itr.next();
				if (entry.getValue() == masterShadowId) {
					list.add(this.getCombatMember(entry.getKey()));
				}
			}
			return list;
		} else {
			return Collections.emptyList();
		}
	}
	
	private ICombatMember getPetOfMaster(long masterShadowId) {
		if (this._petIdMapMasterId.size() > 0) {
			Map.Entry<Short, Short> current;
			for (Iterator<Map.Entry<Short, Short>> itr = _petIdMapMasterId.entrySet().iterator(); itr.hasNext();) {
				current = itr.next();
				if (current.getValue() == masterShadowId) {
					return this.getCombatMember(current.getKey());
				}
			}
		}
		return null;
	}
	
	private void checkClientIsAlive() {
		long now = System.currentTimeMillis();
		KCombatMessageChannel channel;
		for (int i = 0; i < _allChannels.size(); i++) {
			channel = _allChannels.get(i);
			if (channel.getCheckTime() > 0) {
				long time = now - channel.getCheckTime();
				if (time > ALLOW_IDLE_MILLIS) {
					LOGGER.error("战场id：{}，[{}]超过4分钟没有通信，按照异常中断处理！", this._combatId, channel.getRoleId());
					channel.setOnline(false);
					ICombatMember member = this.getCombatMember(channel.getShadowId());
					member.setEscape(true);
				}
			} else if (now - channel.getLastCommunicateTime() > ICombatConstant.ONE_MINUTE_MILLIS) {
				LOGGER.error("战场id：{}，[{}]超过一分钟没有通信，检查是否异常中断！", this._combatId, channel.getRoleId());
				this.sendVerifyTimeMsg(channel);
				channel.setCheckTime(now);
			}
		}
	}
	
	private void sendCombatFinishMsg(boolean pIsRoleWin) {
		KGameMessage msg = KGame.newLogicMessage(SM_BAEELE_COMMAND_EXIT);
		msg.writeBoolean(pIsRoleWin);
		if (_alsoSendFinishMsgIfEscape) {
			this.sendMsgToAll(msg);
		} else {
			KCombatMessageChannel channel;
			ICombatMember member;
			int lastIndex = _allChannels.size() - 1;
			for (int i = 0; i < _allChannels.size(); i++) {
				channel = _allChannels.get(i);
				member = _allMembers.get(channel.getShadowId());
				if (!member.isEscape()) {
					channel.sendMsg(msg);
				}
				if (lastIndex > i) {
					msg = msg.copy();
				}
			}
		}
	}
	
	private boolean checkCombatFinish() {
		// 检查战斗是否已经完毕
		boolean allEscape = true;
		KCombatMessageChannel channel;
		for(int i = 0; i < _allChannels.size(); i++) {
			channel = _allChannels.get(i);
			ICombatMember member = this.getCombatMember(channel.getShadowId());
			if(!member.isEscape()) {
				allEscape = false;
				break;
			}
		}
		if(allEscape) {
			return true;
		}
		boolean result = _vitoryCondition.validateFinish(this);
		if (result && _timeOutMillis > 0) {
			int left = _timeOutMillis - _currentUseTime;
			if (left < ICombatConstant.ONE_SECOND_MILLIS && !_timeAlreadySync.contains(0)) {
				this.syncTimeToClient(0);
			}
		}
		return result;
	}
	
	private void combatFinish() {
		// 战斗完毕的处理方法
		boolean isRoleWin = _vitoryCondition.isRoleWin(this);
		this._combatEndTime = System.currentTimeMillis();
		LOGGER.info("战场：{}，战斗结束，是否角色胜利：{}，结束时间：{}", _combatId, isRoleWin, this._combatEndTime);
		_combatResult.setTotalCombatTime(_combatEndTime - _combatStartTime - _totalPauseTime);
		_combatResult.setIsRoleWin(isRoleWin);
		for(Iterator<ICombatGround> itr = _combatGroundMap.values().iterator(); itr.hasNext();) {
			itr.next().processCombatFinish(this, _combatResult);
		}
		KCombatMessageChannel channel;
		List<ICombatMember> minions;
		ICombatMember pet;
		ICombatMember currentMinion;
		long totalDm;
		Map<Integer, Short> killMap;
		for(int i = 0; i < _allChannels.size(); i++) {
			channel = _allChannels.get(i);
			ICombatMember member = this.getCombatMember(channel.getShadowId());
			minions = this.getMinionSummoned(member.getShadowId());
			totalDm = member.getCombatRecorder().getTotalDm();
			killMap = member.getCombatRecorder().getKillMemberMap();
			if(minions.size() > 0) {
				for(int k = 0; k < minions.size(); k++) {
					currentMinion = minions.get(k);
					totalDm += currentMinion.getCombatRecorder().getTotalDm();
					killMap.putAll(currentMinion.getCombatRecorder().getKillMemberMap());
				}
			}
			pet = this.getPetOfMaster(member.getShadowId());
			if (pet != null) {
				totalDm += pet.getCombatRecorder().getTotalDm();
				killMap.putAll(pet.getCombatRecorder().getKillMemberMap());
			}
			KCombatRoleResultImpl result = _combatResult.getRoleResult(member.getSrcObjId());
			result.addKillMonsterAll(killMap);
			result.setTotalDamage(totalDm);
			result.setAlive(member.isAlive());
			result.setEscape(member.isEscape());
			result.setIsWin(isRoleWin);
//			result.setAttachment(_combatResult.getAttachment());
			result.setCombatTime(_combatResult.getTotalCombatTime());
			result.setMaxBeHitCount(member.getCombatRecorder().getBeHitCount());
			result.setMaxComboCount(member.getCombatRecorder().getMaxComboAttackCount());
			if (member.getMaxHp() == member.getSrcMaxHp()) {
				result.setCurrentHp(member.getCurrentHp());
			} else {
				float mutiple = (float) ((double) member.getMaxHp() / member.getSrcMaxHp());
				result.setCurrentHp((long) (member.getCurrentHp() / mutiple));
			}
			Short petId = _masterSrcIdOfPets.get(member.getSrcObjId());
			if (petId != null) {
				ICombatMember petMember = _allMembers.get(petId);
				if (petMember.getSrcMaxHp() == petMember.getMaxHp()) {
					result.setPetCurrentHp(petMember.getCurrentHp());
				} else {
					float mutiple = (float) ((double) petMember.getMaxHp() / petMember.getSrcMaxHp());
					result.setPetCurrentHp((long) (petMember.getCurrentHp() / mutiple));
				}
			}
			for(Iterator<ICombatGround> itr = _combatGroundMap.values().iterator(); itr.hasNext();) {
				itr.next().processRoleResult(this, _combatResult, result);
			}
		}
		KCombatManager.processCombatFinish(this, _combatResult);
		for(int i = 0; i < _allChannels.size(); i++) {
			channel = _allChannels.get(i);
			KCombatManager.processCombatResultToRole(channel.getRoleId(), this, _combatResult, !channel.isOnline());
		}
		sendCombatFinishMsg(isRoleWin); // 发送战斗结束消息给客户端
	}
	
	private boolean checkCanDestroy() {
		int exitCount = 0;
		for(int i = 0; i < _allChannels.size(); i++) {
			if(_allChannels.get(i).isExit()) {
				exitCount++;
			}
		}
		if( exitCount == _allChannels.size()) {
			return true;
		} else if (System.currentTimeMillis() - _combatEndTime > ICombatConstant.ONE_MINUTE_MILLIS) {
			LOGGER.error("战场：[{}]，销毁等待超时！", _combatId);
			this.destroyWaitingTimeOut();
			return true;
		} else {
			return false;
		}
	}
	
	private void destroyCombat() {
		// 销毁战场
		Map.Entry<Integer, ICombatGround> entry;
		for(Iterator<Map.Entry<Integer, ICombatGround>> itr = this._combatGroundMap.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			entry.getValue().dispose();
			KCombatGroundPool.returnCombatGround(entry.getValue());
		}
		ICombatMember member;
		for (Iterator<ICombatMember> itr = _allMembers.values().iterator(); itr.hasNext();) {
			member = itr.next();
			member.release();
			KCombatMemberFactory.returnCombatMember(member);
		}
		this._timer.terminateTimer();
		this._preEventList.clear();
		this._combatGroundMap.clear();
		this._allChannels.clear();
		this._roleIdToShadowId.clear();
		this._animationList.clear();
		this._allMembers.clear();
		this._combatGroundMap.clear();
		this._extractOp.clear();
		this._allMonsterForces.clear();
		//_allMonsterForcesRO
		this._roundDeadMembers.clear();
		this._roundOpResults.clear();
		this._combatResult.reset();
		this._masterIdMapMountId.clear();
		this._petIdMapMasterId.clear();
		this._minionIdMapMasterId.clear();
		this._masterSrcIdOfPets.clear();
		this._robotMembers.clear();
		this._roleForce.dispose();
		this._extractMonsterForce.dispose();
		this._extractNeturaForce.dispose();
		this._membersByType.clear();
		this._currentCombatGroundKey = 0;
		this._combatStartTime = 0;
		this._combatEndTime = 0;
		this._combatPauseTime = 0;
		this._totalPauseTime = 0;
		this._serverStart = false;
		this._clientStart = false;
		this._terminal = false;
		this._alsoSendFinishMsgIfEscape = false;
		this._timeOutMillis = 0;
		this._syncHpList.clear();
		this._alreadyPickUpDropIds.clear();
		this._timeAlreadySync.clear();
		this._currentUseTime = 0;
		this._totalWaveCount = 0;
		//_combatId
		//_combatType
		//_maxShadowId
		//_randomSeed
		//_validationCode
		//_vitoryCondition
		//_deadMemberLock
		//_opMsgHandler
		//_opResultLock
		//_randomInstance
		KCombatManager.removeCombat(_combatId);
	}
	
	private void addCombatMember(ICombatMember member) {
		switch(member.getForceType()) {
		case ICombatForce.FORCE_TYPE_MONSTER_SIDE:
			this._extractMonsterForce.addMemberToForce(member);
			break;
		case ICombatForce.FORCE_TYPE_NEUTRAL:
			this._extractNeturaForce.addMemberToForce(member);
			break;
		case ICombatForce.FORCE_TYPE_ROLE_SIDE:
			this._roleForce.addMemberToForce(member);
			break;
		}
		this._allMembers.put(member.getShadowId(), member);
	}
	
	private void removeMountId(short mountId) {
		Map.Entry<Short, Short> entry;
		for (Iterator<Map.Entry<Short, Short>> itr = this._masterIdMapMountId.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			if (entry.getValue() == mountId) {
				itr.remove();
				break;
			}
		}
	}
	
	private void addToDeadMemberSyncList(ICombatMember member) {
		boolean sync = true;
		if (member.getMemberType() == ICombatMember.MEMBER_TYPE_VEHICLE) {
			if (member.getCurrentHp() > 0) {
				sync = false;
			}
		}
		if (sync) {
			this._deadMemberLock.readLock().lock();
			try {
				if (!_roundDeadMembers.contains(member.getShadowId())) {
					_roundDeadMembers.add(member.getShadowId());
				}
			} finally {
				this._deadMemberLock.readLock().unlock();
			}
		}
	}
	
	KCombatImpl() {
		this._combatId = _idGenerator.incrementAndGet();
		this._allMonsterForces = new ArrayList<ICombatForce>();
		this._extractMonsterForce = new KCombatForceImpl(new ArrayList<ICombatMember>());
		this._extractNeturaForce = new KCombatForceImpl(new ArrayList<ICombatMember>());
		this._allMonsterForcesRO = Collections.unmodifiableList(_allMonsterForces);
		this._roleIdToShadowId = new HashMap<Long, Short>(4);
		this._roleForce = new KCombatForceImpl(new ArrayList<ICombatMember>());
		this._allMembers = new ConcurrentHashMap<Short, ICombatMember>(20, 0.5f); // 2014-11-22 从HashMap改为ConcurrentHashMap，因为这个有可能是多线程在访问（_combatTimer会添加，消息处理的时候会访问）
		this._timer = new KCombatTimer(this);
	}
	
	void init(List<ICombatGroundBuilder> combatGroundList, List<KCombatEntrance> pEntranceList, List<KCombatEntrance> pRobotList, List<ICombatPlugin> pluginList) {
//		List<ICombatMember> roleSideMembers = new ArrayList<ICombatMember>();
//		_allMonsterForces = new ArrayList<ICombatForce>();
//		_extractMonsterForce = new KCombatForceImpl(new ArrayList<ICombatMember>());
//		_allMonsterForces.add(_extractMonsterForce);
//		_allMonsterForcesRO = Collections.unmodifiableList(_allMonsterForces);
//		_extractNeturaForce = new KCombatForceImpl(new ArrayList<ICombatMember>());
//		_roleIdToShadowId = new HashMap<Long, Short>(2);
		_allMonsterForces.add(_extractMonsterForce); // 每次都添加一次
		List<ICombatMember> roleSideMembers = new ArrayList<ICombatMember>();
		List<ICombatMember> roleTypeMembers = new ArrayList<ICombatMember>();
		AtomicInteger shadowIdGenerator = new AtomicInteger();
		ICombatMember member;
		KCombatEntrance entrance;
		ICombatGround combatGround;
		for (int i = 0; i < combatGroundList.size(); i++) {
			combatGround = combatGroundList.get(i).build(this, shadowIdGenerator);
			_combatGroundMap.put(combatGround.getBattleFieldTemplateId(), combatGround);
			_allMonsterForces.add(combatGround.getMonsterForce());
			if(combatGround.isFirst()) {
				this._currentCombatGroundKey = combatGround.getBattleFieldTemplateId();
			}
		}
		for (int i = 0; i < pEntranceList.size(); i++) {
			entrance = pEntranceList.get(i);
			member = KCombatMemberFactory.getCombatMemberInstance(entrance.getMemberType());
			member.init(ICombatForce.FORCE_TYPE_ROLE_SIDE, (short)shadowIdGenerator.incrementAndGet(), System.currentTimeMillis(), entrance, this);
			roleSideMembers.add(member);
			if (member.getMemberType() == ICombatMember.MEMBER_TYPE_ROLE) {
				_allChannels.add(new KCombatMessageChannel(member.getSrcObjId(), member.getShadowId(), true));
				_combatResult.addRoleId(member.getSrcObjId());
				_combatResult.addCombatRoleResult(member.getSrcObjId(), new KCombatRoleResultImpl(this._combatType));
				_combatResult.addCombatReward(member.getSrcObjId(), new KCombatRewardImpl(member.getSrcObjId()));
			}
			switch (member.getMemberType()) {
			case ICombatMember.MEMBER_TYPE_ROLE:
			case ICombatMember.MEMBER_TYPE_TEAM_MATE_ROLE:
				_roleIdToShadowId.put(member.getSrcObjId(), member.getShadowId());
				roleTypeMembers.add(member);
				break;
			case ICombatMember.MEMBER_TYPE_PET:
			case ICombatMember.MEMBER_TYPE_TEAM_MATE_PET:
				this.recordMasterIdOfPet(entrance.getOwnerId(), member.getShadowId());
				break;
			}
			KCombatEntrancePool.returnEntrance(entrance); // 归还
		}
//		_roleForce = new KCombatForceImpl(roleSideMembers);
		_roleForce.addAllMembersToForce(roleSideMembers);
		if (pRobotList != null) {
			for (int i = 0; i < pRobotList.size(); i++) {
				entrance = pRobotList.get(i);
				member = KCombatMemberFactory.getCombatMemberInstance(entrance.getMemberType());
				member.init(ICombatForce.FORCE_TYPE_ROLE_SIDE, (short) shadowIdGenerator.incrementAndGet(), System.currentTimeMillis(), entrance, this);
				_robotMembers.add(member);
				switch (member.getMemberType()) {
				case ICombatMember.MEMBER_TYPE_PET:
				case ICombatMember.MEMBER_TYPE_TEAM_MATE_PET:
				case ICombatMember.MEMBER_TYPE_WORLD_BOSS_OTHER_PET:
					this.recordMasterIdOfPet(entrance.getOwnerId(), member.getShadowId());
					break;
				case ICombatMember.MEMBER_TYPE_ROLE:
				case ICombatMember.MEMBER_TYPE_TEAM_MATE_ROLE:
				case ICombatMember.MEMBER_TYPE_WORLD_BOSS_OTHER_ROLE:
					roleTypeMembers.add(member);
					break;
				}
				KCombatEntrancePool.returnEntrance(entrance); // 归还
				this._allMembers.put(member.getShadowId(), member);
			}
		}
		_maxShadowId = (short) (shadowIdGenerator.incrementAndGet());
		
//		// 计算所有member的数量
//		int memberSize = _roleForce.getAllMembers().size();
//		for(Iterator<ICombatGround> itr = _combatGroundMap.values().iterator(); itr.hasNext();) {
//			combatGround = itr.next();
//			memberSize += combatGround.getMonsterForce().getAllMembers().size();
//			memberSize += combatGround.getNeutralForce().getAllMembers().size();
//		}
//		 //将所有的member放到一个map里面，方便管理
//		this._allMembers = new HashMap<Short, ICombatMember>(memberSize);
		this.putMembersToMap(roleSideMembers);
		for(Iterator<ICombatGround> itr = _combatGroundMap.values().iterator(); itr.hasNext();) {
			combatGround = itr.next();
			this.putMembersToMap(combatGround.getMonsterForce().getAllMembers());
			this.putMembersToMap(combatGround.getNeutralForce().getAllMembers());
		}
		List<ICombatMember> monsterRoleTypeMembers = this.getCombatMemberByType(ICombatMember.MEMBER_TYPE_ROLE_MONSTER);
		if (monsterRoleTypeMembers != null) {
			roleTypeMembers.addAll(monsterRoleTypeMembers);
		}
		for (int i = 0; i < roleTypeMembers.size(); i++) {
			member = roleTypeMembers.get(i);
			Short tempShadowId = _masterSrcIdOfPets.remove(member.getSrcObjId());
			if (tempShadowId != null) {
				_petIdMapMasterId.put(tempShadowId, member.getShadowId());
			}
		}
		this._validationCode = UtilTool.randomPositiveInt(); // 生成战场验证码
		this._randomSeed = UtilTool.randomPositiveInt(); // 生成随机种子
//		this._combatId = _idGenerator.incrementAndGet(); // 生成战斗id
		this._randomInstance = new Random(_randomSeed); // 战斗随机器
		if(pluginList != null && pluginList.size() > 0) {
			for(int i =0 ; i < pluginList.size(); i++) {
				pluginList.get(i).beforeStart(this);
			}
		}
//		_timer = KCombatTimer.createAndSubmitNewTask(this); // 提交战场时效任务
		_timer.start();
		sendVerifyTimeMsg(null); // 发送时间校对消息
	}
	
	@Override
	public void sendMsgToAll(KGameMessage msg) {
		int endIndex = _allChannels.size() - 1;
		for (int i = 0; i < _allChannels.size(); i++) {
			_allChannels.get(i).sendMsg(msg);
			if (i < endIndex) {
				msg = msg.copy();
			}
		}
	}
	
	@Override
	public void submitCommand(ICombatCommand cmd) {
		_timer.addCmd(cmd);
	}
	
	@Override
	public void startCombat() {
		packCombatMsgAndSend();
//		this._combatStartTime = System.currentTimeMillis();
		this._serverStart = true;
	}
	
	@Override
	public KCombatType getCombatType() {
		return _combatType;
	}
	
	@Override
	public ICombatForce getForce(byte pForceType) {
		switch(pForceType) {
		case ICombatForce.FORCE_TYPE_ROLE_SIDE:
			return _roleForce;
		case ICombatForce.FORCE_TYPE_MONSTER_SIDE:
		case ICombatForce.FORCE_TYPE_NEUTRAL:
			ICombatGround ground = this._combatGroundMap.get(_currentCombatGroundKey);
			return pForceType == ICombatForce.FORCE_TYPE_MONSTER_SIDE ? ground.getMonsterForce() : ground.getNeutralForce();
		}
		return null;
	}
	
	@Override
	public List<ICombatForce> getAllEnermyForces() {
		return _allMonsterForcesRO;
	}

	@Override
	public ICombatMember getCombatMember(short shadowId) {
		ICombatMember member = _allMembers.get(shadowId);
		if (member != null) {
			switch (_combatType) {
			case WORLD_BOSS:
				switch (member.getMemberType()) {
				case ICombatMember.MEMBER_TYPE_WORLD_BOSS_OTHER_PET:
				case ICombatMember.MEMBER_TYPE_WORLD_BOSS_OTHER_ROLE:
					LOGGER.warn("世界boss战斗，收到获取其他同屏机械人的指令！战场id：{}", _combatId);
					return null;
				}
				break;
			default:
				break;
			}
		} else {
			LOGGER.error("战场id：{}，不存在shadowId为[{}]的战斗单位！", this._combatId, shadowId);
		}
		return member;
	}
	
	@Override
	public ICombatMember getRoleTypeMemberBySrcId(long objId) {
		Short shadowId = this._roleIdToShadowId.get(objId);
		if (shadowId != null) {
			return getCombatMember(shadowId);
		}
		return null;
	}
	
	@Override
	public List<ICombatMember> getCombatMemberByType(byte type) {
		return _membersByType.get(type);
	}
	
	@Override
	public Random getCombatRandomInstance() {
		return _randomInstance;
	}
	
	@Override
	public int getSerialId() {
		return _combatId;
	}
	
	@Override
	public long getCombatStartTime() {
		return _combatStartTime;
	}
	
	@Override
	public int getCurrentUseTime() {
		return _currentUseTime;
	}
	
	@Override
	public int getTimeOutMillis() {
		return _timeOutMillis;
	}
	
	@Override
	public boolean isTerminal() {
		return _terminal;
	}
	
	@Override
	public void addExtractOperation(IOperation op) {
		this._extractOp.add(op);
	}
	
	@Override
	public void addOperationResult(IOperationResult opResult) {
		this._opResultLock.readLock().lock();
		try {
			this._roundOpResults.add(opResult);
		} finally {
			this._opResultLock.readLock().unlock();
		}
	}
	
	@Override
	public void notifyMemberDead(ICombatMember member) {
//		if (member.getMemberType() != ICombatMember.MEMBER_TYPE_VEHICLE) {
//			// 机甲死亡不用发送消息到客户端
//		boolean sync = true;
//		if(member.getMemberType() == ICombatMember.MEMBER_TYPE_VEHICLE) {
//			if(member.getCurrentHp() > 0) {
//				sync = false;
//			}
//		}
//		if (sync) {
//			this._deadMemberLock.readLock().lock();
//			try {
//				if (!_roundDeadMembers.contains(member.getShadowId())) {
//					_roundDeadMembers.add(member.getShadowId());
//				}
//			} finally {
//				this._deadMemberLock.readLock().unlock();
//			} 
//		}
		this.addToDeadMemberSyncList(member);
			ICombatGround ground = this._combatGroundMap.get(_currentCombatGroundKey);
			if (ground != null) {
				ground.notifyMemberDead(this, member);
			}
//		} else {
		if (member.getMemberType() == ICombatMember.MEMBER_TYPE_VEHICLE) {
			// 机甲需要移除
			this.removeMountId(member.getShadowId());
		}
	}
	
	@Override
	public void addRoundDeadMemberAgain(ICombatMember member) {
//		if (member.getMemberType() != ICombatMember.MEMBER_TYPE_VEHICLE) {
//			this._deadMemberLock.readLock().lock();
//			try {
//				short shadowId = member.getShadowId();
//				if (!_roundDeadMembers.contains(shadowId)) {
//					_roundDeadMembers.add(shadowId);
//				}
//			} finally {
//				this._deadMemberLock.readLock().unlock();
//			}
//		}
		this.addToDeadMemberSyncList(member);
	}

	@Override
	public void onTimeSignal() {
		this.checkSyncHp();
		this.checkAndSendOperationResult();
		this.checkAndSendDeadMembers();
		this.nowTimeNotify(System.currentTimeMillis());
		boolean isTerminal = this.isTerminal();
		if (!isTerminal && this.checkCombatFinish()) {
			this._terminal = true;
			this.combatFinish();
		} else if (isTerminal) {
			if (this.checkCanDestroy()) {
				this.destroyCombat();
			}
		} else {
			this.checkClientIsAlive();
		}
	}
	
	@Override
	public void msgReceived(long roleId, KGameMessage msg) {
		switch (msg.getMsgID()) {
		case CM_VERIFY_BATTLE_TIMESTAMP:
			// 时间戳校验完毕
			this.verifyTime(roleId, msg);
			break;
		case CM_BATTLE_COMMAND:
			// 处理战斗指令
			this.handleCommandMsg(roleId, msg);
			break;
		case CM_BATTLE_STATUS:
			// 客户端发送状态消息
			this.handleBattleStatusMsg(roleId, msg);
			break;
		case CM_BAEELE_COMMAND_EXIT:
			// 客户端中途退出战斗
			this.handleClientEscape(roleId);
			break;
		default:
			ICombatGround ground = this._combatGroundMap.get(this._currentCombatGroundKey);
			if(ground != null) {
				ground.messageReceived(this, msg);
			}
			return;
		}
		KCombatMessageChannel channel = this.getChannelByRoleId(roleId);
		if (channel != null) {
			channel.setLastCommunicateTime(System.currentTimeMillis());
			if(channel.getCheckTime() > 0) {
				channel.setCheckTime(0);
			}
		}
	}
	
	@Override
	public void handleRoleLeaveGame(long roleId) {
		KCombatMessageChannel channel = this.getChannelByRoleId(roleId);
		if (channel != null) {
			if (this.isTerminal()) {
				this.setClientExit(channel);
			} else {
				channel.setOnline(false);
				ICombatMember member = this.getCombatMember(channel.getShadowId());
				member.setEscape(true);
			}
		}
	}
	
	@Override
	public void recordMasterIdOfPet(long masterSrcId, short petShadowId) {
		this._masterSrcIdOfPets.put(masterSrcId, petShadowId);
	}
	
	@Override
	public void recordKillInstanceId(int pInstanceId) {
		this._combatResult.recordKillInstanceId(pInstanceId);
	}
	
	@Override
	public short getMasterShadowIdOfPet(short shadowIdOfPet) {
		Short shadowId = _petIdMapMasterId.get(shadowIdOfPet);
		if (shadowId != null) {
			return shadowId;
		}
		return 0;
	}
	
	@Override
	public ICombatMember addMinion(ICombatMember master, ICombatMinion minion, long createTime) {
//		KCombatEntrance entrance = new KCombatEntrance(minion, null, master.getX(), master.getY(), 0);
		KCombatEntrance entrance = KCombatEntrancePool.borrowEntrance(minion, null, master.getX(), master.getY(), 0);
		if (this._preEventList.size() > 0) {
			entrance.addAllEvent(_preEventList);
		}
		ICombatMember member = KCombatMemberFactory.getCombatMemberInstance(ICombatMember.MEMBER_TYPE_MINION);
		member.init(master.getForceType(), ++_maxShadowId, createTime, entrance, this);
//		LOGGER.info("召唤物的shadowId={}，memberType={}，createTime={}", member.getShadowId(), member.getMemberType(), createTime);
		this.addCombatMember(member);
		this._minionIdMapMasterId.put(member.getShadowId(), master.getShadowId());
		KCombatEntrancePool.returnEntrance(entrance);
		return member;
	}
	
	@Override
	public ICombatMember addMount(ICombatMember master, KCombatEntrance mount, long createTime) {
		ICombatMember mountMember = KCombatMemberFactory.getCombatMemberInstance(ICombatMember.MEMBER_TYPE_VEHICLE);
		mountMember.init(master.getForceType(), ++_maxShadowId, createTime, mount, this);
//		LOGGER.info("机甲的shadowId={}，memberType={}，createTime={}", member.getShadowId(), member.getMemberType(), createTime);
		this.addCombatMember(mountMember);
		this._masterIdMapMountId.put(master.getShadowId(), mountMember.getShadowId());
		if(this._extractOp.size() > 0) {
			for(Iterator<IOperation> itr = _extractOp.iterator(); itr.hasNext();) {
				itr.next().notifyMountAdded(master, mountMember);
			}
		}
		return mountMember;
	}
	
	@Override
	public void releaseMount(ICombatMember master, long happenTime) {
		Short mountId = this._masterIdMapMountId.remove(master.getShadowId());
		if (mountId != null) {
//			LOGGER.info("解除机甲！masterId={}，时间={}", master.getShadowId(), happenTime);
			ICombatMember mount = this.getCombatMember(mountId);
			mount.sentenceToDead(happenTime);
			if(this._extractOp.size() > 0) {
				for(Iterator<IOperation> itr = _extractOp.iterator(); itr.hasNext();) {
					itr.next().notifyMountReleased(master, mount);
				}
			}
		}
	}
	
	@Override
	public ICombatMember getInUseMount(ICombatMember master) {
		Short mountId = this._masterIdMapMountId.get(master.getShadowId());
		if(mountId != null) {
			return this.getCombatMember(mountId);
		}
		return null;
	}
	
	@Override
	public ICombatMember getMasterOfMount(ICombatMember mount) {
		if (mount != null) {
			Map.Entry<Short, Short> temp;
			for (Iterator<Map.Entry<Short, Short>> itr = _masterIdMapMountId.entrySet().iterator(); itr.hasNext();) {
				temp = itr.next();
				if (temp.getValue() == mount.getShadowId()) {
					return this.getCombatMember(temp.getKey());
				}
			}
		}
		return null;
	}
	
	@Override
	public ICombatMember getMasterOfMinion(ICombatMember minion) {
		if(minion != null) {
			Short masterId = _minionIdMapMasterId.get(minion.getShadowId());
			if(masterId != null) {
				return this._allMembers.get(masterId);
			}
		}
		return null;
	}
	
	@Override
	public void notifyMemberAddToCombatGround(List<ICombatMember> memberList) {
		if(memberList != null && memberList.size() > 0) {
			this.putMembersToMap(memberList);
		}
	}
	
	@Override
	public void pickUp(long roleId, int dropId, long happenTime) {
		if (_alreadyPickUpDropIds.contains(dropId)) {
			LOGGER.error("重复拾取掉落！roleId={}, combatId={}, dropId={}", roleId, _combatId, dropId);
			return;
		}
		_alreadyPickUpDropIds.add(dropId);
		short shadowId = 0;
		ICombatGround ground;
		for(Iterator<ICombatGround> itr = this._combatGroundMap.values().iterator(); itr.hasNext();) {
			ground = itr.next();
			shadowId = ground.getDropOwner(dropId);
			if(shadowId > 0) {
				break;
			}
		}
		if(shadowId > 0) {
			ICombatMember member = this._allMembers.get(shadowId);
			if(member != null && !member.isAlive()) {
				ICombatDropInfo dropInfo = null;
				for(Iterator<ICombatGround> itr = this._combatGroundMap.values().iterator(); itr.hasNext();) {
					dropInfo = itr.next().getDropInfo(dropId);
					if(dropInfo != null) {
						break;
					}
				}
				if (dropInfo != null) {
					KCombatRewardImpl result = _combatResult.getCombatReward(roleId);
					member = this.getRoleTypeMemberBySrcId(roleId);
					dropInfo.executeReward(this, member, result, happenTime);
				}
			}
		}
	}
	
	@Override
	public void afterOneOperationExecuted(long happenTime) {
//		List<IOperation> tempList;
//		for (Iterator<ICombatMember> itr = this._allMembers.values().iterator(); itr.hasNext();) {
//			tempList = itr.next().getPeriodOperation(0, happenTime);
//			if (tempList != null && tempList.size() > 0) {
//				for (int i = 0; i < tempList.size(); i++) {
//					tempList.get(i).executeOperation(this);
//				}
//			}
//		}
		if(this._extractOp.size() > 0) {
			IOperation temp;
			for(Iterator<IOperation> itr = _extractOp.iterator(); itr.hasNext();) {
				temp = itr.next();
				if(temp.getOperationTime() <= happenTime) {
					temp.executeOperation(this);
					itr.remove();
				}
			}
		}
//		if (this._terminateByTimeMembers.size() > 0) {
//			ICombatMember member;
//			for (Iterator<ICombatMember> itr = _terminateByTimeMembers.iterator(); itr.hasNext();) {
//				member = itr.next();
//				if (member.getTerminateTime() < happenTime) {
//					member.sentenceToDead(happenTime);
//				}
//			}
//		}
	}
	
	@Override
	public void switchSence(int current, int target) {
		if (current == _currentCombatGroundKey) {
			ICombatGround ground = this._combatGroundMap.get(_currentCombatGroundKey);
			if (ground.getNextBattleFieldTemplateId() == target) {
				_currentCombatGroundKey = target;
			}
		}
	}

	@Override
	public void addSyncHpShadowId(short shadowId) {
		if (!this._syncHpList.contains(shadowId)) {
			this._syncHpList.add(shadowId);
		}
	}
	
	@Override
	public void nowTimeNotify(long pNowTime) {
		if (this._timeOutMillis > 0 && _clientStart && !_terminal) {
			// _timeOutMillis > 0的时候才执行
			this._currentUseTime = (int) (pNowTime - this._combatStartTime);
			for (Iterator<ICombatGround> itr = _combatGroundMap.values().iterator(); itr.hasNext();) {
				itr.next().notifyTime(this, pNowTime);
			}
			int leftTime = this._timeOutMillis - this._currentUseTime;
			if (leftTime > ICombatConstant.ONE_SECOND_MILLIS) {
				int leftSecond = (int)TimeUnit.SECONDS.convert(leftTime, TimeUnit.MILLISECONDS);
				if(leftSecond % ICombatConstant.SYNC_TIME_ITERVAL == 0 && !_timeAlreadySync.contains(leftSecond)) {
					this.syncTimeToClient(leftSecond);
				}
			}
//			LOGGER.info("now time : {}", _currentUseTime);
		}
	}
	
	@Override
	public void handleClientExitFinished(long roleId) {
		KCombatMessageChannel channel = this.getChannelByRoleId(roleId);
		if (channel != null) {
			setClientExit(channel);
		}
	}
	
	public static class KCombatBuilder {

//		private short _beginShadowId;
		private List<ICombatGroundBuilder> _combatGroundList;
		private List<KCombatEntrance> _roleSide;
		private List<KCombatEntrance> _robotList; // 机器人
//		private int _lastBattleFieldId;
//		private KGameBattlefieldTypeEnum _lastBattleFieldType;
		private KCombatType _combatType;
		private IOperationMsgHandler _opMsgHandler;
		private ICombatCommandCreator _cmdCreator;
		private IVitoryCondition _vitoryCondition;
		private Object _resultAttachment;
		private ICombatGameLevelInfo _levelInfo;
		private List<KCombatAnimation> _combatAnimations;
		private List<ICombatPlugin> _pluginList;
		private boolean _alsoSendFinishMsgIfEscape;
		private int _timeOutMillis; // 限时时长（单位：毫秒）
		private int _totalWaveCount; // 总共的波数
		private boolean _canAutoFight; // 能否自动战斗
		private List<ICombatEventListener> _preEventList;
		
//		public KCombatBuilder beginShadowId(short pBeginShadowId) {
//			this._beginShadowId = pBeginShadowId;
//			return this;
//		}
		
		public KCombatBuilder combatGroundBuilderList(List<ICombatGroundBuilder> pCombatGroundList) {
			this._combatGroundList = new ArrayList<ICombatGroundBuilder>(pCombatGroundList);
			return this;
		}
		
		public KCombatBuilder roleSide(List<KCombatEntrance> pRoleSide) {
			this._roleSide = new ArrayList<KCombatEntrance>(pRoleSide);
			return this;
		}
		
//		public KCombatBuilder lastBattleFieldId(int pLastBattleFieldId) {
//			this._lastBattleFieldId = pLastBattleFieldId;
//			return this;
//		}
//		
//		public KCombatBuilder lastBattleFieldType(KGameBattlefieldTypeEnum pType) {
//			this._lastBattleFieldType = pType;
//			return this;
//		}
		
		public KCombatBuilder combatType(KCombatType pCombatType) {
			this._combatType = pCombatType;
			return this;
		}
		
		public KCombatBuilder opMsgHandler(IOperationMsgHandler pOpMsgHandler) {
			this._opMsgHandler = pOpMsgHandler;
			return this;
		}
		
		public KCombatBuilder cmdCreator(ICombatCommandCreator pCreator) {
			this._cmdCreator = pCreator;
			return this;
		}
		
		public KCombatBuilder vitoryCondition(IVitoryCondition pVitoryCondition) {
			this._vitoryCondition = pVitoryCondition;
			return this;
		}
		
		public KCombatBuilder resultAttachment(Object pAttachment) {
			this._resultAttachment = pAttachment;
			return this;
		}
		
		public KCombatBuilder gameLevelInfo(ICombatGameLevelInfo pGameLevelInfo) {
			this._levelInfo = pGameLevelInfo;
			return this;
		}
		
		public KCombatBuilder animations(List<KCombatAnimation> pAnimationList) {
			this._combatAnimations = new ArrayList<KCombatAnimation>(pAnimationList);
			return this;
		}
		
		public KCombatBuilder pluginList(List<ICombatPlugin> list) {
			this._pluginList = list;
			return this;
		}
		
		public KCombatBuilder alsoSendFinishMsgIfEscape(boolean flag) {
			this._alsoSendFinishMsgIfEscape = flag;
			return this;
		}
		
		public KCombatBuilder timeOutMillis(int pTimeOutMillis) {
			this._timeOutMillis = pTimeOutMillis;
			return this;
		}
		
		
		public KCombatBuilder totalWaveCount(int pTotalWaveCount) {
			this._totalWaveCount = pTotalWaveCount;
			return this;
		}
		
		public KCombatBuilder preEventList(List<ICombatEventListener> list) {
			if (list != null) {
				this._preEventList = new ArrayList<ICombatEventListener>(list);
			}
			return this;
		}
		
		public KCombatBuilder canAutoFight(boolean pCanAutoFight) {
			this._canAutoFight = pCanAutoFight;
			return this;
		}
		
		public KCombatBuilder robotList(List<KCombatEntrance> pRobotList) {
			this._robotList = pRobotList;
			return this;
		}
		
		public ICombat build() {
//			KCombatImpl instance = new KCombatImpl();
			KCombatImpl instance = KCombatPool.borrowCombat();
			instance._combatType = this._combatType;
//			instance._combatResult.setLastBattleFieldId(_lastBattleFieldId);
//			instance._combatResult.setLastBattleFieldType(_lastBattleFieldType);
			instance._opMsgHandler = this._opMsgHandler;
			instance._cmdCreator = this._cmdCreator;
			instance._vitoryCondition = this._vitoryCondition;
			instance._combatResult.setAttachment(this._resultAttachment);
			instance._combatResult.setGameLevelInfo(_levelInfo);
			instance._animationList.addAll(this._combatAnimations);
			instance._alsoSendFinishMsgIfEscape = this._alsoSendFinishMsgIfEscape;
			instance._timeOutMillis = this._timeOutMillis;
			instance._totalWaveCount = this._totalWaveCount;
			instance._canAutoFight = this._canAutoFight;
			if(this._preEventList != null) {
				instance._preEventList.addAll(_preEventList);
			}
			instance.init( _combatGroundList, _roleSide, _robotList, _pluginList);
			return instance;
		}
	}
	
	private static class KSyncHpOperationResult implements IOperationResult {

		private short _targetShadowId;
		private long _currentHp;
		
		
		public KSyncHpOperationResult(short pTargetId, long pCurrentHp) {
			this._targetShadowId = pTargetId;
			this._currentHp = pCurrentHp;
		}
		@Override
		public byte getOperationType() {
			return OPERATION_TYPE_SYNC_HP;
		}

		@Override
		public void fillMsg(KGameMessage msg) {
			msg.writeShort(this._targetShadowId);
			msg.writeLong(this._currentHp);
		}
		
	}

}
