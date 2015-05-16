package com.kola.kmp.logic.activity.worldboss;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.combat.ICombatEnhanceInfo;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.ResultStructs.MailResult;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.WorldBossTips;

/**
 * 
 * <pre>
 * 
 * 代表世界boss参与的成员的数据结构
 * 
 * </pre>
 * @author PERRY CHAN
 */
public class KWorldBossMember implements ICombatEnhanceInfo, IWorldBossMember {

	private static final Logger _FLOGGER = KGameLogger.getLogger("activityLogger");
//	private static final int DM_ARRAY_SIZE = 2;
//	private static final int INDEX_TEMPLATE_ID = 0;
//	private static final int INDEX_DM = 1;
	
	private static final int[] EMPTY_PIC_RES_ID_ARRAY = new int[0];
	private static final String[] EMPTY_URL_LINK_ARRAY = new String[0];
	
	private long _roleId; // 角色id
	private String _roleName; // 角色昵称
	private String _roleNameEx;
	private int _level; // 角色等级
	private long _totalDm; // 总伤害
	private int _srcMapId; // 进入副本前的地图id
	private Map<Integer, Long> _separateDmRecord; // 分开的伤害记录，二维数组第二围包含两个元素，{0}=模板id，{1}=伤害
	private int _inspireLv; // 鼓舞等级
	private long _lastFinishTime; // 上一次挑战完成时间
	private long _aliveTime; // 等待复活终结的时间
	private boolean _available; // 是否正在活动中
	private KWorldBossCombatAttachment _attachment; // 战斗信息记录
//	private final List<Integer> _killList; // 杀死的怪物的模板id
	private boolean _killBoss;
	private boolean _fighting;
	private boolean _sentFinishTips;
	private boolean _beenSweeped;
	private int _battlePower;
	private final AtomicBoolean _dmChange = new AtomicBoolean();
	
//	private int _atkInc; // 攻击力固定加成
//	private int _defInc; // 防御力固定加成
	private int _atkPctInc; // 攻击力万分比加成
//	private int _defPctInc; // 防御力万分比加成
//	private int _rewardPctInc; // 奖励加成
	
//	private String _defIncDescr;
	private String _atkIncDescr;
	
	public KWorldBossMember(KRole role, int monsterCount) {
		this._roleId = role.getId();
		this._roleName = role.getName();
		this._roleNameEx = role.getExName();
		this._level = role.getLevel();
		this._attachment = new KWorldBossCombatAttachment();
		this._available = true;
		this._separateDmRecord = new HashMap<Integer, Long>();
		this._srcMapId = role.getRoleMapData().getCurrentMapId();
		this._battlePower = role.getBattlePower();
		this._atkIncDescr = WorldBossTips.getTipsInspireDescFormat(KGameAttrType.ATK.getName(), "+0");
//		this._defIncDescr = WorldBossTips.getTipsInspireDescFormat(KGameAttrType.DEF.getName(), "+0");;
	}
	
	private void initInspireInfo() {
		if (this._inspireLv > 0) {
			KInspireTemplateData data = KWorldBossManager.getInspireData(_inspireLv);
//			int[] fixedInc = data.getFixedInc(_inspireLv);
//			this._rewardPctInc = data.rewardIncPct;
			this._atkPctInc = data.atkIncPct;
//			this._defPctInc = data.defIncPct;
//			this._atkInc = fixedInc[0];
//			this._defInc = fixedInc[1];
//			this._atkIncDescr = WorldBossTips.getTipsInspireDescFormat(KGameAttrType.ATK.getName(), StringUtil.format("+{}、{}%", _atkInc, _atkPctInc / UtilTool.HUNDRED_RATIO_UNIT));
//			this._defIncDescr = WorldBossTips.getTipsInspireDescFormat(KGameAttrType.DEF.getName(),StringUtil.format("+{}、{}%", _defInc, _defPctInc / UtilTool.HUNDRED_RATIO_UNIT));
			this._atkIncDescr = WorldBossTips.getTipsInspireDescFormat(KGameAttrType.ATK.getName(), StringUtil.format("+{}%", _atkPctInc / UtilTool.HUNDRED_RATIO_UNIT));
		}
	}
	
	/*private final int calculateSingle(float dmResult, int typePara) {
		float value = dmResult * typePara;
		if (_rewardPctInc > 0) {
			value += UtilTool.calculateTenThousandRatioF(value, _rewardPctInc);
		}
		return UtilTool.round(value);
	}*/
	
	void notifyLvUp(int nowLv) {
		this._level = nowLv;
		this.initInspireInfo();
	}
	
	/*void executeDmReward(KWorldBossActivityField fieldData) {
		int expReward = 0;
		int goldReward = 0;
		int potentialReward = 0;
		Map.Entry<Integer, Long> singleDmInfo;
		int genRuleTemplateId;
		float dmBasicPara = fieldData.getDmBasicPara();
		float dmResult;
		KWorldBossRewardPara rewardPara = KWorldBossManager.getRewardPara(this._level);
		StringBuilder builder = new StringBuilder("[");
		for (Iterator<Map.Entry<Integer, Long>> itr = _separateDmRecord.entrySet().iterator(); itr.hasNext();) {
			singleDmInfo = itr.next();
			genRuleTemplateId = singleDmInfo.getKey();
			if (genRuleTemplateId == 0) {
				builder.append("]");
				break;
			} else {
				builder.append(singleDmInfo.toString()).append(",");
			}
		}
		dmResult = _totalDm / dmBasicPara;
		float expPara = (KWorldBossConfig.getExpRewardParaAdd() + Math.min(KWorldBossConfig.getExpRewardParaMultiple(), dmResult));
		float moneyPara = (KWorldBossConfig.getMoneyRewardParaAdd() + Math.min(KWorldBossConfig.getMoneyRewardParaMultiple(), dmResult));
		expReward = calculateSingle(expPara, rewardPara.expBasicPara);
		goldReward = calculateSingle(moneyPara, rewardPara.goldBasicPara);
		potentialReward = calculateSingle(moneyPara, rewardPara.potentialBasicPara);
		if (expReward > 0 || goldReward > 0 || potentialReward > 0) {
			AttValueStruct expRewardStruct = new AttValueStruct(KGameAttrType.EXPERIENCE, expReward);
			KCurrencyCountStruct honorRewardStruct = new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, goldReward);
			KCurrencyCountStruct potentialRewardStruct = new KCurrencyCountStruct(KCurrencyTypeEnum.POTENTIAL, potentialReward);
			BaseRewardData reward = new BaseRewardData(Arrays.asList(expRewardStruct), Arrays.asList(honorRewardStruct, potentialRewardStruct), null, null, null);
			BaseMailContent mailContent = new BaseMailContent(WorldBossTips.getTipsDmRewardMailTitle(), WorldBossTips.getTipsDmRewardMailContent(_totalDm, _inspireLv, _rewardPctInc),
					EMPTY_PIC_RES_ID_ARRAY, EMPTY_URL_LINK_ARRAY);
			BaseMailRewardData mailData = new BaseMailRewardData(0, mailContent, reward);
			MailResult result = KSupportFactory.getMailModuleSupport().sendAttMailBySystem(_roleId, mailData, PresentPointTypeEnum.世界BOSS);
			_FLOGGER.info("世界boss，发送伤害奖励，角色id：{}，是否成功：{}，经验：{}，荣誉：{}，潜能：{}，总伤害：{}，各个怪物的伤害信息：{}", _roleId, result.isSucess, expReward, goldReward, potentialReward, _totalDm, builder.toString());
		} else {
			_FLOGGER.info("世界boss，没有任何伤害奖励，角色id：{}", _roleId);
		}
	}*/

	void executeRankReward(int rank, KWorldBossRankReward reward) {
		KWorldBossRewardBasicPara rewardPara = KWorldBossManager.getRewardPara(this._level);
		int gold = UtilTool.calculateTenThousandRatio(reward.rewardData.goldProportion, rewardPara.goldBasicPara);
		int expReward = UtilTool.calculateTenThousandRatio(reward.rewardData.expProportion, rewardPara.expBasicPara);
		int potential = UtilTool.calculateTenThousandRatio(reward.rewardData.potentialProportion, rewardPara.potentialBasicPara);
		AttValueStruct expRewardStruct = new AttValueStruct(KGameAttrType.EXPERIENCE, expReward);
		List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
		moneyList.add(new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, gold));
		moneyList.add(new KCurrencyCountStruct(KCurrencyTypeEnum.POTENTIAL, potential));
		BaseRewardData mailReward = new BaseRewardData(Arrays.asList(expRewardStruct), moneyList, reward.rewardData.itemRewards.size() > 0 ? reward.rewardData.itemRewards : null, null, null);
		BaseMailContent mailContent = new BaseMailContent(WorldBossTips.getTipsRankRewardMailTitle(), WorldBossTips.getTipsRankRewardMailContent(_totalDm, rank), EMPTY_PIC_RES_ID_ARRAY, EMPTY_URL_LINK_ARRAY);
		BaseMailRewardData mailData = new BaseMailRewardData(0, mailContent, mailReward);
		MailResult result = KSupportFactory.getMailModuleSupport().sendAttMailBySystem(_roleId, mailData, PresentPointTypeEnum.世界BOSS);
		_FLOGGER.info("世界boss，发送排名奖励，角色名字：{}，排名：{}，等级：{}，是否成功：{}", _roleName, rank, _level, result.isSucess);
	}
	
	void executeKillReward(int bossLv) {
		if (_killBoss) {
			KWorldBossRewardBasicPara para = KWorldBossManager.getRewardPara(this._level);
			KWorldBossTemplate template = KWorldBossManager.getWorldBossTemplate(bossLv);
			List<ItemCountStruct> list = template.rewardData.itemRewards;
			int exp = UtilTool.calculateTenThousandRatio(para.expBasicPara, template.rewardData.expProportion);
			int money = UtilTool.calculateTenThousandRatio(para.goldBasicPara, template.rewardData.goldProportion);
			BaseRewardData mailReward = new BaseRewardData(Arrays.asList(new AttValueStruct(KGameAttrType.EXPERIENCE, exp)), Arrays.asList(new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, money)),
					list, null, null);
			BaseMailContent content = new BaseMailContent(WorldBossTips.getTipsKillRewardMailTitle(), WorldBossTips.getTipsKillRewardMailContent(), EMPTY_PIC_RES_ID_ARRAY, EMPTY_URL_LINK_ARRAY);
			BaseMailRewardData mailData = new BaseMailRewardData(0, content, mailReward);
			MailResult result = KSupportFactory.getMailModuleSupport().sendAttMailBySystem(_roleId, mailData, PresentPointTypeEnum.世界BOSS);
			_FLOGGER.info("发送击杀奖励，角色：{}，是否成功：{}", _roleName, result.isSucess);
		}
	}
	
	final void notifyLeaveGame() {
		this._available = false;
	}
	
	final void recordDm(int templateId, long value) {
		_totalDm += value;
		_dmChange.compareAndSet(false, true);
		boolean notFound = true;
//		long[] singleDm;
		Map.Entry<Integer, Long> singleDm;
//		int index;
		for (Iterator<Map.Entry<Integer, Long>> itr = _separateDmRecord.entrySet().iterator(); itr.hasNext();) {
			singleDm = itr.next();
			if (singleDm.getKey() == templateId) {
				singleDm.setValue(value + singleDm.getValue());
				notFound = false;
				break;
			} /*else if (singleDm[INDEX_TEMPLATE_ID] == 0) {
				break;
			}*/
		}
		if (notFound) {
//			if (index == _separateDmRecord.length) {
//				int[][] copy = _separateDmRecord;
//				_separateDmRecord = new int[copy.length + copy.length / 2][DM_ARRAY_SIZE];
//				System.arraycopy(copy, 0, _separateDmRecord, 0, copy.length);
//			}
//			singleDm = _separateDmRecord[index];
//			singleDm[INDEX_TEMPLATE_ID] = templateId;
//			singleDm[INDEX_DM] = value;
			_separateDmRecord.put(templateId, value);
		}
	}
	
//	final void addKillMonster(int id) {
//		this._killList.add(id);
//	}
	
	final void setKillBoss(boolean pKillBoss) {
		this._killBoss = pKillBoss;
	}
	
	final void setLastFinishTime(long time) {
		this._lastFinishTime = time;
		this._aliveTime = this._lastFinishTime + KWorldBossConfig.getWaitingForAliveTime();
	}
	
	final boolean coolDownFinish() {
		return this._aliveTime < System.currentTimeMillis();
	}
	
	final long getLastFinishTime() {
		return _lastFinishTime;
	}
	
	/**
	 * 
	 * 获取等待复活结束时间
	 * 
	 * @return
	 */
	final int getWaitingAliveSecond() {
		long currentTimeMillis = System.currentTimeMillis();
		if(this._aliveTime > System.currentTimeMillis()) {
//			return this._aliveTime - currentTimeMillis;
			return (int)TimeUnit.SECONDS.convert(this._aliveTime - currentTimeMillis, TimeUnit.MILLISECONDS);
		} else {
			return 0;
		}
	}
	
	int getInspireLv() {
		return _inspireLv;
	}
	
	boolean isInspireMax() {
		return _inspireLv == KWorldBossManager.getMaxInspireLv();
	}
	
	String getAtkIncDescr() {
		return _atkIncDescr;
	}
	
//	String getDefIncDescr() {
//		return _defIncDescr;
//	}
	
	AtomicBoolean isDmChange() {
		return this._dmChange;
	}
	
	CommonResult inspire() {
		CommonResult result = new CommonResult();
		if (this._inspireLv < KWorldBossManager.getMaxInspireLv()) {
			int toLv = this._inspireLv + 1;
			KInspireTemplateData templateData = KWorldBossManager.getInspireData(toLv);
			if (KSupportFactory.getCurrencySupport().getMoney(_roleId, templateData.consume.currencyType) < templateData.consume.currencyCount) {
				result.tips = GlobalTips.getTipsMaterialNotEnough(templateData.consume.currencyType.extName, (int) templateData.consume.currencyCount);
			} else {
				if (KSupportFactory.getCurrencySupport().decreaseMoney(_roleId, templateData.consume, UsePointFunctionTypeEnum.世界boss鼓舞, true) < 0) {
					result.tips = GlobalTips.getTipsMaterialNotEnough(templateData.consume.currencyType.extName, (int) templateData.consume.currencyCount);
				} else {
					this._inspireLv = toLv;
					this.initInspireInfo();
					result.isSucess = true;
					result.tips = WorldBossTips.getTipsInspireSuccess();
					KWorldBossMessageHandler.sendInspireData(this);
				}
			}
		} else {
			result.tips = WorldBossTips.getTipsYourInpireLvIsMax();
		}
		return result;
	}
	
	CommonResult relive() {
		CommonResult result = new CommonResult();
		if(KSupportFactory.getCurrencySupport().decreaseMoney(_roleId, KCurrencyTypeEnum.DIAMOND, KWorldBossConfig.getRelivePrice(), UsePointFunctionTypeEnum.世界boss复活, true) > 0) {
			result.isSucess = true;
			result.tips = WorldBossTips.getTipsReliveSuccess();
			_aliveTime = System.currentTimeMillis();
		} else {
			result.tips = GlobalTips.getTipsMaterialNotEnough(KCurrencyTypeEnum.DIAMOND.extName, KWorldBossConfig.getRelivePrice());
		}
		return result;
	}
	
	void setFighting(boolean pFighting) {
		this._fighting = pFighting;
	}
	
	boolean isFighting() {
		return _fighting;
	}
	
	void setBeenSweeped(boolean status) {
		this._beenSweeped = status;
	}
	
	boolean hasBeenSweeped() {
		return this._beenSweeped;
	}
	
	/**
	 * 
	 * 获取角色id
	 * 
	 * @return
	 */
	public long getRoleId() {
		return _roleId;
	}
	
	/**
	 * 
	 * 获取角色名字
	 * 
	 * @return
	 */
	public String getRoleName() {
		return _roleName;
	}
	
	/**
	 * 获取角色名字（带颜色）
	 * 
	 * @return
	 */
	public String getRoleNameEx() {
		return _roleNameEx;
	}
	
	/**
	 * 
	 * 获取角色等级
	 * 
	 * @return
	 */
	public int getLevel() {
		return _level;
	}
	
	@Override
	public int getBattlePower() {
		return _battlePower;
	}
	
	/**
	 * 
	 * 获取角色总伤害值
	 * 
	 * @return
	 */
	public long getTotalDm() {
		return this._totalDm;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getSrcMapId() {
		return this._srcMapId;
	}
	
	/**
	 * 
	 * 获取战斗前的信息记录
	 * 
	 * @return
	 */
	public KWorldBossCombatAttachment getCombatAttachment() {
		return this._attachment;
	}
	
	/**
	 * 
	 * 是否在活动场景中
	 * 
	 * @return
	 */
	public boolean isAvailable() {
		return this._available;
	}
	
	void setAvailable(boolean flag) {
		this._available = flag;
	}
	
	public boolean hasSentFinishTips() {
		return _sentFinishTips;
	}

	void setSentFinishTips(boolean pSendFinishTips) {
		this._sentFinishTips = pSendFinishTips;
	}

	@Override
	public int getAtkInc() {
//		return _atkInc;
		return 0;
	}

	@Override
	public int getDefInc() {
//		return _defInc;
		return 0;
	}

	@Override
	public int getAtkPctInc() {
		return _atkPctInc;
	}

	@Override
	public int getDefPctInc() {
//		return _defPctInc;
		return 0;
	}
}
