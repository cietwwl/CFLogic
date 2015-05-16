package com.kola.kmp.logic.activity.worldboss;

import static com.kola.kmp.protocol.activity.KActivityProtocol.SM_SEND_LAST_RANKING;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.koala.game.util.StringUtil;
import com.koala.game.util.XmlUtil;
import com.kola.kmp.logic.activity.KActivityManager;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.combat.ICombatCommonResult;
import com.kola.kmp.logic.combat.ICombatGlobalCommonResult;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KXmlWriter;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.WorldBossTips;

/**
 * 
 * <pre>
 * 世界boss活动的主干逻辑，主要是对世界boss整个活动的监控，包括检查
 * 时间是否到了，是否所有的活动场景的boss都被击杀等等；以及会做一些
 * 事件的传递，把事件传递到活动场景的逻辑
 * </pre>
 * 
 * @author PERRY CHAN
 */
public class KWorldBossActivityMain implements KGameTimerTask {

	private static final Logger _LOGGER = KGameLogger.getLogger(KWorldBossActivityMain.class);
	
	private static final int _DELAY_TIME = 5; // 延迟时间
	
	private static final byte STATUS_NOT_FINISH = 0; // 活动状态：未完成
	private static final byte STATUS_ALL_FIELD_END = 1; // 活动状态：所有场景都已经完成
	private static final byte STATUS_TIME_OUT = 2; // 活动状态：超时
	
//	private static final String _ATTR_NAME_FIELD_TEMPLATE_ID = "fieldId";
	private static final String _ATTR_NAME_RAKING = "ranking";
	private static final String _ATTR_NAME_ROLE_NAME = "roleName";
	private static final String _ATTR_NAME_ROLE_LEVEL = "lv";
	private static final String _ATTR_NAME_DAMAGE = "dm";
	
	private static final String _ELEMENT_NAME_RANKING_LIST = "rankingList";
	
	private static final String _ELEMENT_NAME_BOSS_LV = "bossLv";
//	private static final String _ATTR_NAME_FIELD_ID = "fieldId";
//	private static final String _ATTR_NAME_LV = "lv";
	
	private static String _saveDir;
	private static String _saveInfoPath;
	private static String _fileNamePrefix;
	private static String _bossLvSavePath;
	
//	private List<KWorldBossActivityField> _allActivityField = new ArrayList<KWorldBossActivityField>(); // 所有的活动场景
	KWorldBossActivityField _activityField;
//	private Map<Integer, List<KWorldBossRankingData>> _lastRanking = new HashMap<Integer, List<KWorldBossRankingData>>();
	private List<KWorldBossRankingData> _lastRanking = new ArrayList<KWorldBossRankingData>();
//	private Map<Long, Integer> _memberRecords = new HashMap<Long, Integer>(); // key=角色id，value=活动场景模板id
	private List<Long> _memberRecords = new ArrayList<Long>(); // key=角色id，value=活动场景模板id
	private final KWorldBossBroadcastTask _broadcastTask = new KWorldBossBroadcastTask();
	private final KWorldBossWarnUpTimer _warnUpTask; /*= new KWorldBossWarnUpTimer(Collections.unmodifiableList(_allActivityField));*/
	private int _leftSeconds = 0;
	private boolean _start = false;
	private KGameMessage _lastRankingMsg;
//	private boolean _canJoin = false; // 预热，这个时候，玩家可以进入地图，但是不能参与战斗
	
	static void loadSavePathInfo(Element element) {
		_saveDir = element.getChildTextTrim("saveDir").replace("\\", "/").replace("/", File.separator);
		_saveInfoPath = _saveDir + File.separator + element.getChildTextTrim("saveInfoFile");
		_fileNamePrefix = element.getChildTextTrim("fileNamePrefix");
		_bossLvSavePath = element.getChildTextTrim("bossLvSavePath");
	}
	
	KWorldBossActivityMain() {
		/*List<KWorldBossFieldData> allDatas = KWorldBossManager.getAllWorldBossFieldDatas();
		for (int i = 0; i < allDatas.size(); i++) {
			_allActivityField.add(new KWorldBossActivityField(allDatas.get(i)));
			_lastRanking.put(allDatas.get(i).templateId, new ArrayList<KWorldBossRankingData>());
		}*/
		KWorldBossFieldData fieldData = KWorldBossManager.getWorldBossFieldData();
		_activityField = new KWorldBossActivityField(fieldData);
		_warnUpTask = new KWorldBossWarnUpTimer(_activityField);
	}
	
	private byte getStatus() {
		byte finish = STATUS_NOT_FINISH;
		if (this._leftSeconds > 0) {
			/*boolean allFinish = true;
			for (int i = 0; i < _allActivityField.size(); i++) {
				if(!_allActivityField.get(i).isFinish()) {
					allFinish = false;
					break;
				}
			}
			if(allFinish) {
				finish = STATUS_ALL_FIELD_END;
			}*/
			if (_activityField.isFinish()) {
				finish = STATUS_ALL_FIELD_END;
			}
		} else {
			finish = STATUS_TIME_OUT;
		}
		return finish;
	}
	
	private final void timeOut() {
//		KWorldBossActivityField field;
		this._start = false;
//		this._canJoin = false;
		boolean needSweepTask = false;
//		List<KWorldBossRankingData> list;
		/*for (int i = 0; i < _allActivityField.size(); i++) {
			field = _allActivityField.get(i);
			try {
				if (!field.isFinish()) {
					field.notifyActivityTimeOut();
				}
			} catch (Exception e) {
				_LOGGER.error("通知超时异常！", e);
			}
			if (!field.areAllMemberLeft() && !needSweepTask) {
				needSweepTask = true;
			}
			list = _lastRanking.get(field.getFieldId());
			list.clear();
			list.addAll(field.getRankingData());
		}*/
		try {
			if (!_activityField.isFinish()) {
				_activityField.notifyActivityTimeOut();
			}
		} catch (Exception e) {
			_LOGGER.error("通知超时异常！", e);
		}
		if (!_activityField.areAllMemberLeft() && !needSweepTask) {
			_LOGGER.info("》》》》》》提交sweeping任务！《《《《《《");
			KGame.newTimeSignal(new KSweepingTask(_activityField), _DELAY_TIME, TimeUnit.SECONDS);
		}
		_lastRanking.clear();
		_lastRanking.addAll(_activityField.getRankingData());
		/*if(needSweepTask) {
			List<KWorldBossActivityField> needSweepField = new ArrayList<KWorldBossActivityField>();
			KWorldBossActivityField tempField;
			for (int i = 0; i < _allActivityField.size(); i++) {
				tempField = _allActivityField.get(i);
				if (tempField != null && !tempField.areAllMemberLeft()) {
					needSweepField.add(tempField);
				}
			}
			_LOGGER.info("》》》》》》提交sweeping任务！《《《《《《");
			KGame.newTimeSignal(new KSweepingTask(needSweepField), _DELAY_TIME, TimeUnit.SECONDS);
		}*/
		KWorldBossActivityImpl activity = (KWorldBossActivityImpl)KActivityManager.getInstance().getActivity(KWorldBossActivityMonitor.getWorldBossActivityId());
//		activity.setOpened(false);
		activity.notifyActivityOpenStatus(false);
		genLastRankingMsg();
	}
	
	/*private final KWorldBossActivityField getActivityField(int id) {
		KWorldBossActivityField activityField;
		for(int i = 0; i < _allActivityField.size(); i++) {
			activityField = _allActivityField.get(i);
			if(activityField.getFieldId() == id) {
				return activityField;
			}
		}
		return null;
	}*/
	
	private void init(int pTimeIndex) {
		/*for (int i = 0; i < _allActivityField.size(); i++) {
			_allActivityField.get(i).init(pTimeIndex);
		}*/
		_activityField.init(pTimeIndex);
		this._memberRecords.clear();
		this._start = true;
		KWorldBossActivityImpl activity = (KWorldBossActivityImpl)KActivityManager.getInstance().getActivity(KWorldBossActivityMonitor.getWorldBossActivityId());
		activity.notifyActivityOpenStatus(true);
		KWorldBossActivityRecorder.notifyStart(pTimeIndex);
	}
	
	@SuppressWarnings("unchecked")
	private void readRanking() {
		File file = new File(_saveInfoPath);
		if (file.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String fileName = br.readLine();
				br.close();
				Document doc = XmlUtil.openXml(fileName);
				Element root = doc.getRootElement();
				List<Element> children = root.getChildren(_ELEMENT_NAME_RANKING_LIST);
				if (children != null && children.size() > 0) {
					Element child;
					Element rankChild;
//					List<KWorldBossRankingData> rankingList;
					List<Element> rankingElements;
					int ranking;
					String name;
					int lv;
					long dm;
					for (int i = 0; i < children.size(); i++) {
						child = children.get(i);
						rankingElements = child.getChildren();
//						rankingList = _lastRanking.get(Integer.parseInt(child.getAttributeValue(_ATTR_NAME_FIELD_TEMPLATE_ID)));
						for (int k = 0; k < rankingElements.size(); k++) {
							rankChild = rankingElements.get(k);
							ranking = Integer.parseInt(rankChild.getAttributeValue(_ATTR_NAME_RAKING));
							name = rankChild.getAttributeValue(_ATTR_NAME_ROLE_NAME);
							lv = Integer.parseInt(rankChild.getAttributeValue(_ATTR_NAME_ROLE_LEVEL));
							dm = Long.parseLong(rankChild.getAttributeValue(_ATTR_NAME_DAMAGE));
							_lastRanking.add(new KWorldBossRankingData(ranking, name, lv, dm));
						}
					}
				}
			} catch (Exception e) {
				_LOGGER.error("世界boss读取上届排行榜出现异常！", e);
			}
		}
	}
	
	private void genLastRankingMsg() {
		/*KWorldBossFieldData fieldData;
		List<KWorldBossRankingData> tempList;
		KWorldBossRankingData rankingData;
		KGameMessage msg = KGame.newLogicMessage(SM_SEND_LAST_RANKING);
		msg.writeByte(list.size());
		int sendSize;
		KWorldBossActivityImpl activity = (KWorldBossActivityImpl)KActivityManager.getInstance().getActivity(KWorldBossActivityMonitor.getWorldBossActivityId());
		for(int i = 0; i < list.size(); i++) {
			fieldData = list.get(i);
			tempList = this._lastRanking.get(fieldData.templateId);
			sendSize = tempList.size() > KWorldBossConfig.getLastRankingSendCount() ? KWorldBossConfig.getLastRankingSendCount() : tempList.size();
//			msg.writeUtf8String(fieldData.lvDescr);
			msg.writeUtf8String(activity.getActivityName());
			msg.writeByte(sendSize);
			for(int k = 0; k < sendSize; k++) {
				rankingData = tempList.get(k);
				msg.writeShort(rankingData.ranking);
				msg.writeUtf8String(rankingData.name);
				msg.writeShort(rankingData.level);
//				msg.writeInt(rankingData.totalDm);
				msg.writeLong(rankingData.totalDm); // 2014-09-15 从int改为long
			}
		}
		_lastRankingMsg = msg;*/
		KGameMessage msg = KGame.newLogicMessage(SM_SEND_LAST_RANKING);
		msg.writeByte(1);
		int sendSize = _lastRanking.size() > KWorldBossConfig.getLastRankingSendCount() ? KWorldBossConfig.getLastRankingSendCount() : _lastRanking.size();
		msg.writeUtf8String(KWorldBossConfig.getActivityName());
		msg.writeByte(sendSize);
		KWorldBossRankingData rankingData;
		for(int k = 0; k < sendSize; k++) {
			rankingData = _lastRanking.get(k);
			msg.writeShort(rankingData.ranking);
			msg.writeUtf8String(rankingData.name);
			msg.writeShort(rankingData.level);
			msg.writeLong(rankingData.totalDm); // 2014-09-15 从int改为long
		}
		_lastRankingMsg = msg;
	}
	
	private void ReadBossLv() {
		File file = new File(_bossLvSavePath);
		if (file.exists()) {
			Document doc = XmlUtil.openXml(file);
			Element root = doc.getRootElement();
			Element child = root.getChild("bossLv");
			this._activityField.setBossLv(Integer.parseInt(child.getTextTrim()));
		}
	}
	
	private void saveRanking() {
		File file = new File(_saveDir);
		if(!file.exists()) {
			file.mkdir();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MMdd_HHmm");
		String timeStr = sdf.format(new Date(System.currentTimeMillis()));
		String fileName = _saveDir + File.separator + _fileNamePrefix + "_" + timeStr + ".xml";
		try {
			KXmlWriter writer = new KXmlWriter(fileName, true);
			/*Element current;
			Map.Entry<Integer, List<KWorldBossRankingData>> entry;
			List<KWorldBossRankingData> currentList;
			Element grandchild;
			KWorldBossRankingData currentData;
			for (Iterator<Map.Entry<Integer, List<KWorldBossRankingData>>> itr = _lastRanking.entrySet().iterator(); itr.hasNext();) {
				current = new Element(_ELEMENT_NAME_RANKING_LIST);
				entry = itr.next();
				currentList = entry.getValue();
				current.setAttribute(_ATTR_NAME_FIELD_TEMPLATE_ID, String.valueOf(entry.getKey()));
				for (int i = 0; i < currentList.size(); i++) {
					currentData = currentList.get(i);
					grandchild = new Element("rank");
					grandchild.setAttribute(_ATTR_NAME_RAKING, String.valueOf(currentData.ranking));
					grandchild.setAttribute(_ATTR_NAME_ROLE_NAME, String.valueOf(currentData.name));
					grandchild.setAttribute(_ATTR_NAME_ROLE_LEVEL, String.valueOf(currentData.level));
					grandchild.setAttribute(_ATTR_NAME_DAMAGE, String.valueOf(currentData.totalDm));
					current.addContent(grandchild);
				}
				writer.addElement(current);
			}*/
			Element grandchild;
			KWorldBossRankingData currentData;
			for (int i = 0; i < _lastRanking.size(); i++) {
				currentData = _lastRanking.get(i);
				grandchild = new Element("rank");
				grandchild.setAttribute(_ATTR_NAME_RAKING, String.valueOf(currentData.ranking));
				grandchild.setAttribute(_ATTR_NAME_ROLE_NAME, String.valueOf(currentData.name));
				grandchild.setAttribute(_ATTR_NAME_ROLE_LEVEL, String.valueOf(currentData.level));
				grandchild.setAttribute(_ATTR_NAME_DAMAGE, String.valueOf(currentData.totalDm));
				writer.addElement(grandchild);
			}
			writer.output();
			BufferedWriter bw = new BufferedWriter(new FileWriter(_saveInfoPath));
			bw.write(fileName);
			bw.flush();
			bw.close();
		} catch (Exception e) {
			_LOGGER.error("保存伤害排名出现异常！");
		}
	}
	
	private void saveWorldBossLv() {
		File file = new File(_bossLvSavePath);
		if (file.exists()) {
			file.delete();
		}
		try {
			KXmlWriter writer = new KXmlWriter(_bossLvSavePath, true);
			Element element = new Element(_ELEMENT_NAME_BOSS_LV);
			element.setText(String.valueOf(_activityField.getBossLv()));
			writer.addElement(element);
			writer.output();
		} catch (Exception e) {
			_LOGGER.error("保存世界boss等级出现异常！");
		}
	}
	
	boolean isInActivity(long roleId) {
		return this._memberRecords.contains(roleId);
	}
	
	void initComplete(){
		readRanking();
		genLastRankingMsg();
		ReadBossLv();
	}
	
	void shutdown() throws Exception {
		saveRanking();
		saveWorldBossLv();
		KWorldBossActivityRecorder.shutdown();
	}
	
	void warnUp(int startDelaySeconds, int pTimeIndex) {
		this.init(pTimeIndex);
		this._broadcastTask.start(startDelaySeconds);
		this._warnUpTask.start(startDelaySeconds);
	}
	
	void start(int pTimeIndex) {
		if(!_start) {
			// 在start之前，会有一个warnUp的调用，里面会调用init方法，把_start设为true
			// 如果没有经过warnUp的调用，证明还没有初始化，所以这里要初始化一下
			this.init(pTimeIndex);
		}
//		for (int i = 0; i < _allActivityField.size(); i++) {
//			_allActivityField.get(i).start();
//		}
		_activityField.start();
		_leftSeconds = KWorldBossConfig.getDurationSecond();
		KGame.getTimer().newTimeSignal(this, _DELAY_TIME, TimeUnit.SECONDS);
		KSupportFactory.getChatSupport().sendSystemChat(WorldBossTips.getTipsWorldBossStart(), true, true);
		KWorldBossMessageHandler.sendStartDialogToAllOnlineRoles();
	}
	
	public KGameMessage getLastRankingMsg() {
		return _lastRankingMsg.copy();
	}
	
	
	
	@Override
	public String getName() {
		return "KWorldBossActivity";
	}

	@Override
	public Object onTimeSignal(KGameTimeSignal timeSignal) throws KGameServerException {
		_leftSeconds -= _DELAY_TIME;
		boolean timeBroadcast = false;
		int min = 0;
		if (_leftSeconds % 60 == 0) {
			min = (int) TimeUnit.MINUTES.convert(_leftSeconds, TimeUnit.SECONDS);
			if (KWorldBossConfig.containBroadcastMinutes(min)) {
				timeBroadcast = true;
			}
		}
		/*KWorldBossActivityField activityField;
		for (int i = 0; i < _allActivityField.size(); i++) {
			activityField = _allActivityField.get(i);
			try {
				activityField.onTimeSignal(_leftSeconds);
				if (timeBroadcast) {
					activityField.timeBroadcast(min);
				}
			} catch (Exception e) {
				_LOGGER.error("世界boss【{}】分场，onTimeSignal出错！", _allActivityField.get(i).getFieldId(), e);
			}
		}*/
		try {
			_activityField.onTimeSignal(_leftSeconds);
			if (timeBroadcast) {
				_activityField.timeBroadcast(min);
			}
		} catch (Exception e) {
			_LOGGER.error("世界boss【{}】分场，onTimeSignal出错！", _activityField, e);
		}
		byte status = this.getStatus();
		switch (status) {
		case STATUS_NOT_FINISH:
			timeSignal.getTimer().newTimeSignal(this, _DELAY_TIME, TimeUnit.SECONDS);
			break;
		case STATUS_TIME_OUT:
			this.timeOut();
			break;
		case STATUS_ALL_FIELD_END:
			// 停止5秒一次的执行，直接提交一个时效任务，等到时间到才再次执行
			timeSignal.getTimer().newTimeSignal(this, _leftSeconds, TimeUnit.SECONDS);
			_leftSeconds = _DELAY_TIME;
			break;
		}
		return Boolean.TRUE;
	}

	@Override
	public void done(KGameTimeSignal timeSignal) {
		
	}

	@Override
	public void rejected(RejectedExecutionException e) {
		
	}
	
	public CommonResult joinActivity(KRole role) {
		CommonResult result = new CommonResult();
		if (this._start) {
			/*Integer fieldId = _memberRecords.get(role.getId());
			if (fieldId == null) {
				KWorldBossActivityField activity;
				boolean joinSuccess = false;
				for (int i = 0; i < _allActivityField.size(); i++) {
					activity = _allActivityField.get(i);
					if (activity.joinActivity(role)) {
						_memberRecords.put(role.getId(), activity.getFieldId());
						joinSuccess = true;
						break;
					}
				}
				if (joinSuccess) {
					result.isSucess = true;
				} else {
					result.tips = WorldBossTips.getTipsLevelNotReach();
				}
			} else {
				KWorldBossActivityField activityField = this.getActivityField(fieldId);
				result.isSucess = activityField.joinActivity(role);
			}*/
			result.isSucess = _activityField.joinActivity(role);
			if(!_memberRecords.contains(role.getId()) && result.isSucess) {
				_memberRecords.add(role.getId());
			}
		} else {
			result.tips = WorldBossTips.getTipsWorldBossNotStart();
		}
		return result;
	}
	
	public void leaveActivity(KRole role) {
//		Integer fieldId = (Integer) _memberRecords.get(role.getId());
//		if (fieldId != null) {
//			KWorldBossActivityField activityField = this.getActivityField(fieldId);
//			activityField.leaveActivity(role);
//		}
		if(_memberRecords.contains(role.getId())) {
			_activityField.leaveActivity(role);
		}
	}
	
//	public CommonResult processStartCombat(KRole role, int templateId) {
//		CommonResult result = new CommonResult();
//		if(this._start) {
//			Integer fieldId = this._memberRecords.get(role.getId());
//			if(fieldId != null) {
//				KWorldBossActivityField activityField = this.getActivityField(fieldId);
//				result = activityField.processCombatStart(role, templateId);
//			} else {
//				result.tips = WorldBossTips.getTipsYouAreNotInActivity();
//			}
//		} else {
//			result.tips = WorldBossTips.getTipsWorldBossActivityFinish();
//		}
//		return result;
//	}
	
	public void processCombatFinished(long roleId, ICombatCommonResult roleResult, ICombatGlobalCommonResult globalResult) {
		/*Integer fieldId = _memberRecords.get(roleId);
		if(fieldId != null) {
			KWorldBossActivityField activityField = this.getActivityField(fieldId);
			activityField.processCombatFinished(roleId, roleResult, globalResult);
		}*/
		if (_memberRecords.contains(roleId)) {
			_activityField.processCombatFinished(roleId, roleResult, globalResult);
		}
	}
	
	public void confirmCombatResult(KRole role) {
//		_LOGGER.info("返回地图！！角色id：{}", role.getId());
		/*Integer fieldId = _memberRecords.get(role.getId());
		if (fieldId != null) {
			KWorldBossActivityField activityField = this.getActivityField(fieldId);
			activityField.processReturnToMap(role);
		}*/
		if (_memberRecords.contains(role.getId())) {
			_activityField.processReturnToMap(role);
		}
	}

	public void processRoleExitCombatFinish(long roleId) {
//		_LOGGER.info("退出战斗完成！角色id：{}", roleId);
		/*Integer fieldId = (Integer) _memberRecords.get(roleId);
		if (fieldId != null) {
			KWorldBossActivityField activityField = this.getActivityField(fieldId);
			KWorldBossMember member = activityField.getWorldBossMember(roleId);
			member.setFighting(false);
			if (!activityField.isFinish()) {
				KWorldBossMessageHandler.sendWaitingAliveMsg(member);
			}
		}*/
		if (_memberRecords.contains(roleId)) {
			KWorldBossMember member = _activityField.getWorldBossMember(roleId);
			member.setFighting(false);
			if (!_activityField.isFinish()) {
				KWorldBossMessageHandler.sendWaitingAliveMsg(member);
			}
		}
	}
	
	public void notifyRoleLeavedGame(KRole role) {
		/*if (_start) {
			Integer fieldId = (Integer) _memberRecords.get(role.getId());
			if (fieldId != null) {
				KWorldBossActivityField activityField = this.getActivityField(fieldId);
				KWorldBossMember member = activityField.getWorldBossMember(role.getId());
				member.setAvailable(false);
				if(member.isFighting()) {
					member.setFighting(false);
				}
			}
		}*/
		if (_start) {
			if (_memberRecords.contains(role.getId())) {
				KWorldBossMember member = _activityField.getWorldBossMember(role.getId());
				member.setAvailable(false);
				if (member.isFighting()) {
					member.setFighting(false);
				}
			}
		}
	}
	
	public CommonResult processInspire(KRole role) {
		CommonResult result = new CommonResult();
		if (_start) {
			/*Integer fieldId = (Integer) _memberRecords.get(role.getId());
			if (fieldId != null) {
				KWorldBossActivityField activityField = this.getActivityField(fieldId);
				KWorldBossMember member = activityField.getWorldBossMember(role.getId());
				return member.inspire();
			} else {
				result.tips = WorldBossTips.getTipsYouAreNotInActivity();
			}*/
			if (_memberRecords.contains(role.getId())) {
				KWorldBossMember member = _activityField.getWorldBossMember(role.getId());
				return member.inspire();
			} else {
				result.tips = WorldBossTips.getTipsYouAreNotInActivity();
			}
		} else {
			result.tips = WorldBossTips.getTipsWorldBossNotStart();
		}
		return result;
	}
	
	public CommonResult requestRelive(KRole role) {
		CommonResult result = new CommonResult();
		if (_start) {
			/*Integer fieldId = (Integer) _memberRecords.get(role.getId());
			if (fieldId != null) {
				KWorldBossActivityField activityField = this.getActivityField(fieldId);
				KWorldBossMember member = activityField.getWorldBossMember(role.getId());
				return member.relive();
			} else {
				result.tips = WorldBossTips.getTipsYouAreNotInActivity();
			}*/
			if (_memberRecords.contains(role.getId())) {
				KWorldBossMember member = _activityField.getWorldBossMember(role.getId());
				return member.relive();
			} else {
				result.tips = WorldBossTips.getTipsYouAreNotInActivity();
			}
		} else {
			result.tips = WorldBossTips.getTipsWorldBossNotStart();
		}
		return result;
	}
	
	public boolean isWorldBossStart() {
		return this._start;
	}
	
	private static class KWorldBossBroadcastTask implements KGameTimerTask {

		private static final int _1_MINUTE_OF_SECOND = (int)TimeUnit.MINUTES.toSeconds(1);
		private int _totalSeconds;
		
		private void broadcast(int minute) {
			KSupportFactory.getChatSupport().sendSystemChat(StringUtil.format(KWordBroadcastType.丧尸攻城_当前距离丧尸攻城活动开启还有x分钟.content, minute), KWordBroadcastType.丧尸攻城_当前距离丧尸攻城活动开启还有x分钟);
		}
		
		void start(int totalSeconds) {
			if(totalSeconds < _1_MINUTE_OF_SECOND) {
				broadcast(1);
			} else {
				this._totalSeconds = totalSeconds;
				KGame.newTimeSignal(this, 0, TimeUnit.SECONDS);
			}
		}
		
		@Override
		public String getName() {
			return "KWorldBossBroadcastTask";
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal timeSignal) throws KGameServerException {
			int minute = (int)TimeUnit.MINUTES.convert(_totalSeconds, TimeUnit.SECONDS);
			if (minute == 0) {
				minute = 1;
			} else if (_totalSeconds - TimeUnit.MINUTES.toSeconds(minute) > 0) {
				minute++;
			}
			broadcast(minute);
			if (minute > 1) {
				_totalSeconds -= _1_MINUTE_OF_SECOND;
				timeSignal.getTimer().newTimeSignal(this, 1, TimeUnit.MINUTES);
			}
			return Boolean.TRUE;
		}

		@Override
		public void done(KGameTimeSignal timeSignal) {
			
		}

		@Override
		public void rejected(RejectedExecutionException e) {
			
		}
		
	}
	
	private static class KSweepingTask implements KGameTimerTask {

		/*private List<KWorldBossActivityField> _list;
		
		KSweepingTask(List<KWorldBossActivityField> list) {
			this._list = new LinkedList<KWorldBossActivityField>(list);
		}*/
		
		private KWorldBossActivityField _field;

		KSweepingTask(KWorldBossActivityField field) {
			_field = field;
		}
		
		@Override
		public String getName() {
			return "KSweepingTask";
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal timeSignal) throws KGameServerException {
//			KWorldBossActivityField temp;
//			for (Iterator<KWorldBossActivityField> itr = _list.iterator(); itr.hasNext();) {
//				temp = itr.next();
//				temp.sweepMembers();
//				if (temp.areAllMemberLeft()) {
//					itr.remove();
//				}
//			}
			if (_field.areAllMemberLeft()) {
				_LOGGER.info("》》》》世界boss sweeping 任务终结 《《《《《");
			} else {
				_field.sweepMembers();
				timeSignal.getTimer().newTimeSignal(this, _DELAY_TIME, TimeUnit.SECONDS);
			}
			return Boolean.TRUE;
		}

		@Override
		public void done(KGameTimeSignal timeSignal) {
			
		}

		@Override
		public void rejected(RejectedExecutionException e) {
			
		}
		
	}
	
	private static class KWorldBossWarnUpTimer implements KGameTimerTask {

		private int _leftSeconds;
//		private List<KWorldBossActivityField> _fieldList;
		private KWorldBossActivityField _field;
		
		
		/*public KWorldBossWarnUpTimer(List<KWorldBossActivityField> pList) {
			_fieldList = pList;
		}*/
		public KWorldBossWarnUpTimer(KWorldBossActivityField field) {
			_field = field;
		}
		
		void notifyToAllField() {
//			for(int i = 0; i < _fieldList.size(); i++) {
//				_fieldList.get(i).warnUpLeftTimePass(_leftSeconds);
//			}
			_field.warnUpLeftTimePass(_leftSeconds);
		}
		
		void start(int pLeftSeconds) {
			this._leftSeconds = pLeftSeconds;
			notifyToAllField();
			KGame.newTimeSignal(this, 1, TimeUnit.SECONDS);
		}
		
		@Override
		public String getName() {
			return "KWorldBossWarnUpTimer";
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal timeSignal) throws KGameServerException {
			this._leftSeconds--;
			notifyToAllField();
			if (_leftSeconds > 0) {
				KGame.newTimeSignal(this, 1, TimeUnit.SECONDS);
			}
			return null;
		}

		@Override
		public void done(KGameTimeSignal timeSignal) {
			
		}

		@Override
		public void rejected(RejectedExecutionException e) {
			
		}
		
	}
	
	static class KWorldBossRankingData {
		
		final int ranking;
		final String name;
		final int level;
		final long totalDm;
		
		/**
		 * 
		 */
		public KWorldBossRankingData(int pRanking, String pName, int pLevel, long pTotalDm) {
			this.ranking = pRanking;
			this.name = pName;
			this.level = pLevel;
			this.totalDm = pTotalDm;
		}
	}
}
