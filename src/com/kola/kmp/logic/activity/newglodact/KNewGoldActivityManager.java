package com.kola.kmp.logic.activity.newglodact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jxl.read.biff.BiffException;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.XmlUtil;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.KActivityRoleExtCaCreator;
import com.kola.kmp.logic.activity.KActivityRoleExtData;
import com.kola.kmp.logic.activity.goldact.BarrelDataStruct;
import com.kola.kmp.logic.activity.goldact.GoldActivityReward;
import com.kola.kmp.logic.activity.goldact.GoldActivityRoleRecordData;
import com.kola.kmp.logic.activity.goldact.KBarrelBattlefield;
import com.kola.kmp.logic.activity.goldact.KGoldActivity;
import com.kola.kmp.logic.activity.goldact.WaveParamData;
import com.kola.kmp.logic.activity.goldact.BarrelDataStruct.BarrelData;
import com.kola.kmp.logic.level.FightEvaluateData;
import com.kola.kmp.logic.level.FightEventListener;
import com.kola.kmp.logic.level.FightResult;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.MonsterData;
import com.kola.kmp.logic.level.KGameLevelManager.CompleteGameLevelTempRecord;
import com.kola.kmp.logic.level.KGameLevelManager.LevelRewardResultData;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KGameBattlefieldTypeEnum;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KGangTecTypeEnum;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.ItemModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GoldActivityTips;
import com.kola.kmp.logic.util.tips.TimeLimitProducActivityTips;
import com.kola.kmp.protocol.level.KLevelProtocol;

public class KNewGoldActivityManager {
	// 奖励表，Key：角色等级
	public static Map<Integer, NewGoldActivityReward> rewardMap = new LinkedHashMap<Integer, NewGoldActivityReward>();
	// 默认奖励
	public static NewGoldActivityReward default_reward;
	// 每天角色可拦截次数
	public static int max_can_challenge_count = 3;
	// 战斗总时间限制
	public static long battleTime;
	// 战场音乐资源Id
	public static int battleMusicResId;
	// 战场数据
	public static KGameBattlefield battlefield;
	// 战场资源路径
	private static String battle_res_path;
	// BOSS总血量
	private static long boss_total_hp;
	// 战斗评价表，Key：评价等级
	private static Map<Integer, Map<Byte, GoldFightEvaluateData>> fightEvaluateDataMap = new LinkedHashMap<Integer, Map<Byte, GoldFightEvaluateData>>();

	public static void init(String configPath) throws KGameServerException {
		Document doc = XmlUtil.openXml(configPath);
		if (doc != null) {
			Element root = doc.getRootElement();

			String excelFilePath = root.getChildText("excelFilePath");

			// battleTime = Long.parseLong(root.getChildText("battle_time")) *
			// 1000;
			// battleMusicResId = Integer.parseInt(root
			// .getChildText("battle_music_res_id"));
			//
			// String[] fightEvaluateInfo = root.getChildText("fight_lv").split(
			// ",");
			// if (fightEvaluateInfo == null || fightEvaluateInfo.length != 4) {
			// throw new KGameServerException("初始化产金活动配置：" + configPath
			// + "的字段<fight_lv>错误：,值：" + root.getChildText("fight_lv"));
			// }
			// int lv = fightEvaluateInfo.length + 1;
			// for (byte i = 0; i < fightEvaluateInfo.length; i++, lv--) {
			// FightEvaluateData data = new FightEvaluateData();
			// data.fightLv = (byte) (lv);
			// data.fightTime = (int) battleTime;
			// data.maxHitCount = 1;
			// data.hitByCount = Integer.parseInt(fightEvaluateInfo[i]);
			// fightEvaluateDataMap.put(data.fightLv, data);
			// }
			//
			// String battle_res_path = root.getChildText("battle_res_path");

			loadExcelData(excelFilePath);

			battlefield = new KGameBattlefield();
			battlefield.setBattlefieldId(1);
			battlefield.setBattlefieldType(KGameBattlefieldTypeEnum.新产金活动战场);
			battlefield.setBattlefieldResId(0);
			battlefield.setLevelId(1);
			battlefield.setBattlefieldSerialNumber(1);
			battlefield.setBgMusicResId(battleMusicResId);
			battlefield.setFirstBattlefield(true);
			battlefield.setLastBattlefield(true);
			battlefield.initBattlefield("新产金活动战场", battle_res_path, 0, 1);
			
		} else {
			throw new NullPointerException("产金活动配置文件不存在！！");
		}
	}

	private static void loadExcelData(String xlsPath)
			throws KGameServerException {
		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(xlsPath);
		} catch (BiffException e) {
			throw new KGameServerException("读取许愿系统excel表头发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取许愿系统excel表头发生错误！", e);
		}

		if (xlsFile != null) {
			// 过关奖励表
			int dataRowIndex = 3;
			KGameExcelTable dataTable = xlsFile.getTable("过关奖励", dataRowIndex);
			KGameExcelRow[] allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					NewGoldActivityReward reward = new NewGoldActivityReward();
					reward.roleLv = allDataRows[i].getInt("lv");
					reward.goldValue = allDataRows[i].getLong("goldBase");
					reward.initLotteryReward("过关奖励", allDataRows[i]);

					rewardMap.put(reward.roleLv, reward);
					if (i == 0) {
						default_reward = reward;
					}
				}
			}

			dataTable = xlsFile.getTable("过关评价", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					// byte fightEvaluate = allDataRows[i].getBy("");
					int maxRoleLv = allDataRows[i].getInt("lv");
					int sHurt = allDataRows[i].getInt("s_hurt");
					int aHurt = allDataRows[i].getInt("a_hurt");
					int bHurt = allDataRows[i].getInt("b_hurt");

					GoldFightEvaluateData bLevel = new GoldFightEvaluateData(
							(byte) 3, bHurt, maxRoleLv);
					GoldFightEvaluateData aLevel = new GoldFightEvaluateData(
							(byte) 4, aHurt, maxRoleLv);
					GoldFightEvaluateData sLevel = new GoldFightEvaluateData(
							(byte) 5, sHurt, maxRoleLv);

					if (!fightEvaluateDataMap.containsKey(maxRoleLv)) {
						fightEvaluateDataMap
								.put(maxRoleLv,
										new LinkedHashMap<Byte, GoldFightEvaluateData>());
					}
					fightEvaluateDataMap.get(maxRoleLv).put(
							bLevel.fightEvaluate, bLevel);
					fightEvaluateDataMap.get(maxRoleLv).put(
							aLevel.fightEvaluate, aLevel);
					fightEvaluateDataMap.get(maxRoleLv).put(
							sLevel.fightEvaluate, sLevel);
				}
			}

			dataTable = xlsFile.getTable("战场配置参数", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					battleMusicResId = allDataRows[i].getInt("music");
					battleTime = allDataRows[i].getLong("battle_time") * 1000;
					battle_res_path = allDataRows[i].getData("battle_res_path");
//					boss_total_hp = allDataRows[i].getInt("hp");
				}
			}
		}

	}

	public static void onGameWorldInitComplete() throws KGameServerException {
		if (battlefield.getBornPoint() == null) {
			throw new KGameServerException(
					"#########  加载新产金活动newGoldActivityConfig.xls表<战场配置参数>的战场xml数据错误，该战场没有设置出生点，xml文件名="
							+ battlefield.getBattlePathName());
		}
		if (!battlefield.isInitOK) {
			throw new KGameServerException(
					"#########  加载新产金活动newGoldActivityConfig.xls表<战场配置参数>的战场xml数据错误，该战场初始化失败，xml文件名="
							+ battlefield.getBattlePathName());
		}	
		if(battlefield.getMonsterMap().size()>0){
			for (MonsterData data : battlefield.getMonsterMap().values()) {
				if(data._monsterTemplate != null){
				    boss_total_hp += data._monsterTemplate.allEffects.get(KGameAttrType.MAX_HP);
				}
			}
		}
	}

	public static NewGoldActivityReward getNewGoldActivityReward(int roleLv) {
		NewGoldActivityReward reward = null;
		for (Integer lv : rewardMap.keySet()) {
			if (roleLv > lv) {
				continue;
			} else {
				reward = rewardMap.get(lv);
				break;
			}
		}
		if (reward == null) {
			reward = default_reward;
		}
		return reward;
	}

	public static KActionResult playerRoleJoinActivity(KRole role) {

		// 检测是否有限时产出活动
		boolean isCopyActivityPrice = false;
		float expRate = 1, goldRate = 1, potentialRate = 1;
		int itemMultiple = 1;
		TimeLimieProduceActivity activity = KSupportFactory
				.getExcitingRewardSupport().getTimeLimieProduceActivity(
						KLimitTimeProduceActivityTypeEnum.赚金币活动);

		if (activity != null && activity.isActivityTakeEffectNow()) {
			isCopyActivityPrice = true;
			expRate = activity.expRate;
			goldRate = activity.goldRate;
			potentialRate = activity.potentialRate;
			itemMultiple = activity.itemMultiple;
		}

		String tips = "";

		NewGoldActivityReward reward = getNewGoldActivityReward(role.getLevel());

		NewGoldActivityRoleRecordData record = KActivityRoleExtCaCreator
				.getNewGoldActivityRoleRecordData(role.getId());

		record.checkAndRestData(true);

		int addChallengeCount = 0;
		TimeLimieProduceActivity activity1 = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.赚金币增加挑战次数);
		if(activity1!=null && activity1.isActivityTakeEffectNow()){
			addChallengeCount = activity1.discountTimes;
		}
		
		if (record.restChallengeCount + addChallengeCount <= 0) {

			tips = GoldActivityTips
					.getTipsMaxChallengeCount((max_can_challenge_count + addChallengeCount));

			KDialogService.sendUprisingDialog(role, tips);
			return new KActionResult(false, tips);
		}

		record.challenge();
		KDialogService.sendNullDialog(role);

		for (FightEventListener listener : KGameLevelModuleExtension
				.getManager().getFightEventListenerList()) {
			listener.notifyNewGoldActivityBattle(role, battlefield,
					(int) (reward.goldValue * goldRate), battleTime);
		}

		// FightResult result = new FightResult();
		// result.setBattlefieldId(battlefield.getBattlefieldId());
		// result.setBattlefieldType(KGameBattlefieldTypeEnum.新产金活动战场);
		// result.setBattleTime(battleTime);
		// result.setTotalDamage(9000000);
		// result.setEndType(FightResult.FIGHT_END_TYPE_NORMAL);
		// result.setWin(true);
		//
		// processCompleteBattle(role, result);

		KNewGoldActivity.getInstance().sendUpdateActivity(role);
		
		//活跃度
		KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.赚金币);

		return new KActionResult(true, tips);
	}

	/**
	 * 处理战斗结算
	 * 
	 * @param role
	 * @param result
	 */
	public static void processCompleteBattle(KRole role, FightResult result) {
		KActivityRoleExtData actData = KActivityRoleExtCaCreator
				.getActivityRoleExtData(role.getId());
		if (actData != null) {
			actData.notifyActivityCdTime(KNewGoldActivity.getInstance()
					.getActivityId());
		}

		if (result.getEndType() == FightResult.FIGHT_END_TYPE_ESCAPE) {
			KDialogService.sendNullDialog(role);
			KSupportFactory.getMapSupport().processRoleFinishBattleReturnToMap(
					role);
			KNewGoldActivity.getInstance().sendUpdateActivity(role);
			return;
		}

		NewGoldActivityReward reward = rewardMap.get(role.getLevel());

		// 计算关卡战斗等级
		byte fightLv = caculateLevelFightEvaluate(role.getLevel(), result);

		// 计算关卡所有奖励
		LevelRewardResultData rewardData = caculateLevelReward(role, result,
				fightLv);

		// 发送完成关卡消息
		KGameMessage sendMsg = KGame
				.newLogicMessage(KLevelProtocol.SM_SHOW_COMPLETE_BARREL_BATTLE_RESULT);
		sendMsg.writeInt(1);
		sendMsg.writeByte(KGameLevelTypeEnum.产金活动关卡.levelType);
		sendMsg.writeBoolean(result.isWin());
		sendMsg.writeByte(fightLv);
		sendMsg.writeInt((int) (result.getBattleTime() / 1000));
		sendMsg.writeInt(result.getTotalDamage());

		rewardData.baseReward.packMsg(sendMsg);
		sendMsg.writeBoolean(false);
		
		// 无S级特别奖励
		sendMsg.writeBoolean(false);

		//显示的产出金币
		sendMsg.writeBoolean(rewardData.isAttrDouble[LevelRewardResultData.GOLD_LINE]);
		for (int i = 0; i < rewardData.attrAndCurrencyShowData[LevelRewardResultData.GOLD_LINE].length; i++) {
			sendMsg.writeInt(rewardData.attrAndCurrencyShowData[LevelRewardResultData.GOLD_LINE][i]);
		}

		// 处理发送抽奖信息
		if (reward.isHasLotteryReward
				&& rewardData.lotteryCurrencyRewardList != null
				&& rewardData.lotteryRewardUsePointList != null
				&& rewardData.lotteryCurrencyRewardList.size() > 0) {
			sendMsg.writeBoolean(true);
			sendMsg.writeByte(rewardData.lotteryCurrencyRewardList.size());
			sendMsg.writeInt(rewardData.lotteryCurrencyRewardList.size());
			for (int i = 0; i < rewardData.lotteryCurrencyRewardList.size(); i++) {
				KCurrencyCountStruct lotteryReward = rewardData.lotteryCurrencyRewardList
						.get(i);
				sendMsg.writeByte(i);
				sendMsg.writeByte(1);
				sendMsg.writeByte(lotteryReward.getCurrencyType());
				sendMsg.writeLong(lotteryReward.currencyCount);
			}
			for (int i = 0; i < rewardData.lotteryRewardUsePointList.size(); i++) {
				sendMsg.writeInt(rewardData.lotteryRewardUsePointList.get(i));
			}
		} else {
			sendMsg.writeBoolean(false);
		}
		role.sendMsg(sendMsg);

		KNewGoldActivity.getInstance().sendUpdateActivity(role);
	}

	public static byte caculateLevelFightEvaluate(int roleLv, FightResult result) {

		int targetRoleLv = 0;
		for (Integer lv : fightEvaluateDataMap.keySet()) {
			targetRoleLv = lv;
			if (roleLv > lv) {
				continue;
			} else {
				break;
			}
		}

		byte value = -1;
		// byte temp = -1;
		int totalDamage = result.getTotalDamage();
		for (GoldFightEvaluateData data : fightEvaluateDataMap
				.get(targetRoleLv).values()) {
			if (totalDamage < data.maxDamage) {
				continue;
			} else {
				value = data.fightEvaluate;
			}
		}
		if (value == -1) {
			value = 1;
		}
		return value;
	}

	public static LevelRewardResultData caculateLevelReward(KRole role,
			FightResult fightResult, int fightLv) {
		LevelRewardResultData resultData = new LevelRewardResultData(
				CompleteGameLevelTempRecord.LOTTERY_TYPE_CURRENCY);
		// 检测是否有限时产出活动
		boolean isCopyActivityPrice = false;
		float expRate = 1, goldRate = 1, potentialRate = 1;
		int itemMultiple = 1;
		TimeLimieProduceActivity activity = KSupportFactory
				.getExcitingRewardSupport().getTimeLimieProduceActivity(
						KLimitTimeProduceActivityTypeEnum.赚金币活动);

		if (activity != null && activity.isActivityTakeEffectNow()) {
			isCopyActivityPrice = true;
			expRate = activity.expRate;
			goldRate = activity.goldRate;
			potentialRate = activity.potentialRate;
			itemMultiple = activity.itemMultiple;
			KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(
					role.getId(), activity.mailTitle, activity.mailContent);
			resultData.isAttrDouble[0] = activity.isExpDouble;
			resultData.isAttrDouble[1] = activity.isGoldDouble;
			resultData.isAttrDouble[2] = activity.isPotentialDouble;
		}

		NewGoldActivityReward reward = rewardMap.get(role.getLevel());

		// 计算关卡数值奖励
		List<AttValueStruct> attList = new ArrayList<AttValueStruct>();
		List<AttValueStruct> attShowList = new ArrayList<AttValueStruct>();

		// 处理关卡货币奖励
		List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
		List<KCurrencyCountStruct> moneyShowList = new ArrayList<KCurrencyCountStruct>();
		float damage = (float) fightResult.getTotalDamage();
		long baseValue = (long) ((damage / boss_total_hp) * reward.goldValue);
		long goldValue = (long) (baseValue * goldRate);
		resultData.attrAndCurrencyShowData[LevelRewardResultData.GOLD_LINE][LevelRewardResultData.NORMAL_ROW] = (int) goldValue;
		// 军团科技加成
		int addRate = KSupportFactory.getGangSupport().getGangEffect(
				KGangTecTypeEnum.赚金币产出加成, role.getId());
		if (addRate > 0) {
			int addValue = (int) ((baseValue * addRate) / UtilTool.TEN_THOUSAND_RATIO_UNIT);
			resultData.attrAndCurrencyShowData[LevelRewardResultData.GOLD_LINE][LevelRewardResultData.GANG_ROW] = addValue;
			goldValue += (long) addValue;
		}

		moneyList.add(new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD,
				goldValue));
		moneyShowList.add(new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD,
				goldValue));

		ItemModuleSupport itemSupport = KSupportFactory.getItemModuleSupport();

		// 计算获得道具列表
		List<ItemCountStruct> itemRewardList = new ArrayList<ItemCountStruct>();
		List<ItemCountStruct> itemRewardShowList = new ArrayList<ItemCountStruct>();

		// 计算关卡战斗的掉落
		if (fightResult.getBattleReward() != null) {
			if (!fightResult.getBattleReward().getAdditionalCurrencyReward()
					.isEmpty()) {
				for (KCurrencyTypeEnum currType : fightResult.getBattleReward()
						.getAdditionalCurrencyReward().keySet()) {
					int value = fightResult.getBattleReward()
							.getAdditionalCurrencyReward().get(currType);
					moneyShowList
							.add(new KCurrencyCountStruct(currType, value));
				}
				moneyShowList = KCurrencyCountStruct
						.mergeCurrencyCountStructs(moneyShowList);
			}
			// 道具
			if (fightResult.getBattleReward().getAdditionalItemReward().size() > 0) {
				for (String itemCode : fightResult.getBattleReward()
						.getAdditionalItemReward().keySet()) {
					int count = fightResult.getBattleReward()
							.getAdditionalItemReward().get(itemCode);
					ItemCountStruct itemData = new ItemCountStruct(itemCode,
							count);
					if (itemData != null) {
						itemRewardShowList.add(itemData);
					}
				}
				itemRewardShowList = ItemCountStruct
						.mergeItemCountStructs(itemRewardShowList);
			}
		}

		// 处理S级别奖励
		BaseRewardData sLevelReward = null;

		// 处理数值和货币奖励
		BaseRewardData baseReward = new BaseRewardData(attShowList,
				moneyShowList, itemRewardShowList,
				Collections.<Integer> emptyList(),
				Collections.<Integer> emptyList());

		for (KCurrencyCountStruct attReward : moneyList) {
			KSupportFactory.getCurrencySupport().increaseMoney(role.getId(),
					attReward, PresentPointTypeEnum.产金活动奖励, true);
		}
		// 得出所有的道具奖励，生成等待确认奖励的临时记录
		CompleteGameLevelTempRecord temprecord = new CompleteGameLevelTempRecord(
				CompleteGameLevelTempRecord.LOTTERY_TYPE_CURRENCY);
		temprecord.setRoleId(role.getId());
		temprecord.setLevelId(1);
		temprecord.setLevelType(KGameLevelTypeEnum.普通关卡.levelType);
		List<ItemCountStruct> temprecordItemList = new ArrayList<ItemCountStruct>();
		temprecordItemList.addAll(itemRewardList);
		temprecord.setItemRewardResultDataList(temprecordItemList);
		List<KCurrencyCountStruct> lotteryCurrencyRewardList = null;
		List<Integer> lotteryRewardUsePointList = null;
		if (reward.isHasLotteryReward) {
			temprecord.setHasLotteryReward(true);
			// lotteryRewardList =
			// reward.getLotteryReward().getLotteryGroup()
			// .caculateLotteryRewards(role.getLevel());
			lotteryCurrencyRewardList = reward.lotteryGroup
					.getLotteryRewardShowCurrencyList();
			lotteryRewardUsePointList = reward.lotteryGroup.lotteryGroupUsePointList;
			temprecord.setLotteryCurrencyInfo(
					reward.lotteryGroup.getCaculateCurrencyRewardList(),
					lotteryRewardUsePointList);
		}

		KGameLevelModuleExtension.getManager().allCompleteGameLevelTempRecord
				.put(temprecord.getRoleId(), temprecord);

		resultData.baseReward = baseReward;
		resultData.sLevelReward = sLevelReward;
		resultData.lotteryCurrencyRewardList = lotteryCurrencyRewardList;
		resultData.lotteryRewardUsePointList = lotteryRewardUsePointList;
		resultData.totalItemSize = itemRewardList.size();
		return resultData;
	}

	public static void sendBattleFaildedResult(KRole role) {
		// 发送完成关卡消息
		KGameMessage sendMsg = KGame
				.newLogicMessage(KLevelProtocol.SM_SHOW_COMPLETE_BARREL_BATTLE_RESULT);
		sendMsg.writeInt(1);
		sendMsg.writeByte(KGameLevelTypeEnum.产金活动关卡.levelType);
		sendMsg.writeBoolean(false);
		role.sendMsg(sendMsg);
	}

	public static class GoldFightEvaluateData {
		public static byte MIN_FIGHT_EVALUATE = 1;
		public static byte MAX_FIGHT_EVALUATE = 3;

		public byte fightEvaluate;
		public int maxDamage;
		public int maxRoleLv;

		public GoldFightEvaluateData(byte fightEvaluate, int maxDamage,
				int maxRoleLv) {
			this.fightEvaluate = fightEvaluate;
			this.maxDamage = maxDamage;
			this.maxRoleLv = maxRoleLv;
		}

	}
}
