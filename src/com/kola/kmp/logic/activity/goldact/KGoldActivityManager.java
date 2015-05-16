package com.kola.kmp.logic.activity.goldact;

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
import com.kola.kmp.logic.activity.goldact.BarrelDataStruct.BarrelData;
import com.kola.kmp.logic.activity.goldact.KBarrelBattlefield.KBarrelBattleData;
import com.kola.kmp.logic.activity.transport.CarrierData;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.level.FightEvaluateData;
import com.kola.kmp.logic.level.FightResult;
import com.kola.kmp.logic.level.KLevelReward;
import com.kola.kmp.logic.level.KLevelTemplate;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.BornPointData;
import com.kola.kmp.logic.level.KGameLevelManager.CompleteGameLevelTempRecord;
import com.kola.kmp.logic.level.KGameLevelManager.LevelRewardResultData;
import com.kola.kmp.logic.level.FightEventListener;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KGameBattlefieldTypeEnum;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.ItemModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GoldActivityTips;
import com.kola.kmp.logic.util.tips.LevelTips;
import com.kola.kmp.protocol.level.KLevelProtocol;

public class KGoldActivityManager {

	// 油桶表，Key：角色等级
	public static Map<Integer, BarrelDataStruct> barrelDataMap = new LinkedHashMap<Integer, BarrelDataStruct>();
	// 奖励表，Key：角色等级
	public static Map<Integer, GoldActivityReward> rewardMap = new LinkedHashMap<Integer, GoldActivityReward>();
	// 每波油桶参数表
	public static List<WaveParamData> waveParamList = new ArrayList<WaveParamData>();

	// 每天角色可拦截次数
	public static int max_can_challenge_count = 3;
	// 战斗总时间限制
	public static long battleTime;
	// 战场音乐资源Id
	public static int battleMusicResId;
	// 战场数据
	public static KBarrelBattlefield battlefield;

	private static String battle_res_path;
	// 战斗评价表，Key：评价等级
	private static Map<Byte, FightEvaluateData> fightEvaluateDataMap = new LinkedHashMap<Byte, FightEvaluateData>();

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

			battlefield = new KBarrelBattlefield();
			battlefield.setTotalBattleTimeMillis(battleTime);
			battlefield.setBattlefieldResId(0);
			battlefield.setBattlefieldType(KGameBattlefieldTypeEnum.产金活动战场);
			battlefield.setBgMusicResId(battleMusicResId);
			battlefield.setBattlefieldId(KGameLevelModuleExtension.getManager()
					.getBattlefieldIdGenerator().nextBattlefieldId());
			battlefield.initBattlefield(battle_res_path);

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
			// 许愿数据表
			int dataRowIndex = 3;
			KGameExcelTable dataTable = xlsFile.getTable("随机油桶", dataRowIndex);
			KGameExcelRow[] allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					int lv = allDataRows[i].getInt("lvLimit");
					BarrelDataStruct struct = new BarrelDataStruct();
					struct.lvLimit = lv;

					Set<Integer> checkSet = new HashSet<Integer>();

					for (int j = 1; j <= 6; j++) {
						String barrelIdKey = "id" + j;
						String weightKey = "weight" + j;
						String dropIdKey = "dropId" + j;

						int barrelId = allDataRows[i].getInt(barrelIdKey);
						if (barrelId != 0) {
							checkSet.add(barrelId);
							int weight = allDataRows[i].getInt(weightKey);
							String dropIdInfo = allDataRows[i]
									.getData(dropIdKey);

							if (dropIdInfo == null || dropIdInfo.equals("")) {
								throw new KGameServerException("加载产金活动"
										+ xlsPath + "的《随机油桶》表：" + xlsPath + "的"
										+ dropIdKey + "=" + dropIdInfo
										+ "字段出错，该值不能空，excel行数："
										+ allDataRows[i].getIndexInFile());
							}

							BarrelData barrelData = new BarrelData();
							barrelData.templateId = barrelId;
							barrelData.weight = weight;
							struct._totalBarrelWeight += weight;

							// System.out.println(dropIdInfo);
							String[] dropInfo = dropIdInfo.split(",");
							String[] singleDropInfo;
							Map<Integer, Integer> tempDropMap = new LinkedHashMap<Integer, Integer>();
							int totalRate = 0;
							if (dropInfo.length > 0 && dropInfo[0].length() > 0) {
								if (dropInfo[0].contains("*")) {
									for (int k = 0; k < dropInfo.length; k++) {
										singleDropInfo = dropInfo[k]
												.split("\\*");
										totalRate += Integer
												.parseInt(singleDropInfo[1]);
										tempDropMap.put(Integer
												.parseInt(singleDropInfo[0]),
												totalRate);
									}
								} else {
									tempDropMap.put(
											Integer.parseInt(dropInfo[0]),
											UtilTool.TEN_THOUSAND_RATIO_UNIT);
									totalRate = UtilTool.TEN_THOUSAND_RATIO_UNIT;
								}
							}
							barrelData.dropMap = Collections
									.unmodifiableMap(tempDropMap);
							barrelData._totalDropRate = totalRate;

							struct.barrelDataList.add(barrelData);
						}
					}

					String idInfo = allDataRows[i].getData("idInfo");
					String[] idInfos = idInfo.split(",");
					if (idInfos == null || idInfos.length != 3) {
						throw new KGameServerException("加载产金活动" + xlsPath
								+ "的《随机油桶》表：" + xlsPath + "的idInfo=" + idInfo
								+ "字段出错，该值不能空或ID数量必须为3，excel行数："
								+ allDataRows[i].getIndexInFile());
					}

					for (int j = 0; j < idInfos.length; j++) {
						int barrelId = Integer.parseInt(idInfos[j]);
						if (!checkSet.contains(barrelId)) {
							throw new KGameServerException("加载产金活动" + xlsPath
									+ "的《随机油桶》表：" + xlsPath + "的idInfo="
									+ idInfo + "字段出错，该值找不到对应的油桶ID，excel行数："
									+ allDataRows[i].getIndexInFile());
						}
						struct.barrelIdShowSet.add(barrelId);
					}

					barrelDataMap.put(struct.lvLimit, struct);
				}
			}

			dataTable = xlsFile.getTable("过关奖励", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					GoldActivityReward reward = new GoldActivityReward();
					reward.roleLv = allDataRows[i].getInt("lv");
					reward.goldValue = allDataRows[i].getLong("copy_gold");
					reward.s_goldValue = allDataRows[i].getLong("s_gold");
					reward.initLotteryReward("过关奖励", allDataRows[i]);

					rewardMap.put(reward.roleLv, reward);
				}
			}

			dataTable = xlsFile.getTable("参数配置", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();
			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					WaveParamData param = new WaveParamData();
					param.waveId = allDataRows[i].getInt("wave");
					param.waveTimeSecond = allDataRows[i].getInt("waveTime");
					param.waveBarrelCount = allDataRows[i].getInt("totalCount");
					param.minPerTimeMillis = allDataRows[i]
							.getLong("minPerTime");
					param.maxPerTimeMillis = allDataRows[i]
							.getLong("maxPerTime");
					param.minPerBarrelCount = allDataRows[i]
							.getInt("minPerCount");
					param.maxPerBarrelCount = allDataRows[i]
							.getInt("maxPerCount");
					param.initPerTimeMillisList();
					waveParamList.add(param);
				}
			}

			dataTable = xlsFile.getTable("战场配置参数", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();
			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					battleTime = allDataRows[i].getLong("battle_time") * 1000;
					battleMusicResId = allDataRows[i].getInt("music");

					String[] fightEvaluateInfo = allDataRows[i].getData(
							"copy_hitby").split(",");
					if (fightEvaluateInfo == null
							|| fightEvaluateInfo.length != 4) {
						throw new KGameServerException("加载产金活动" + xlsPath
								+ "的《战场配置参数》表：" + xlsPath + "的copy_hitby="
								+ allDataRows[i].getData("copy_hitby")
								+ "字段出错，excel行数："
								+ allDataRows[i].getIndexInFile());
					}
					int lv = fightEvaluateInfo.length + 1;
					for (byte j = 0; j < fightEvaluateInfo.length; j++, lv--) {
						FightEvaluateData data = new FightEvaluateData();
						data.fightLv = (byte) (lv);
						data.fightTime = 0;
						data.maxHitCount = 1;
						data.hitByCount = Integer
								.parseInt(fightEvaluateInfo[j]);
						fightEvaluateDataMap.put(data.fightLv, data);
					}

					battle_res_path = allDataRows[i].getData("battle_res_path");
				}
			}
		}

	}

	public static void onGameWorldInitCompleted() throws KGameServerException {
		for (BarrelDataStruct struct : barrelDataMap.values()) {
			for (BarrelData data : struct.barrelDataList) {
				data.template = KSupportFactory.getNpcModuleSupport()
						.getMonstTemplate(data.templateId);
				if (data.template == null) {
					throw new KGameServerException(
							"加载产金活动数值表的《随机油桶》表的油桶ID错误，油桶模版Id="
									+ data.templateId + "，角色等级lv="
									+ struct.lvLimit + "。");
				}
			}
		}

		// KBarrelBattlefield battlefield = new KBarrelBattlefield();
		// battlefield.getBarrelBornPointList().add(new BornPointData(1, 10f,
		// 10f));
		// battlefield.getBarrelBornPointList().add(new BornPointData(2, 20f,
		// 20f));
		// battlefield.getBarrelBornPointList().add(new BornPointData(3, 30f,
		// 30f));
		// Map<BornPointData,List<KBarrelBattleData>> map =
		// battlefield.getBarrelBattleDatas(15);
		//
		// for (BornPointData bornPoint:map.keySet()) {
		//
		// List<KBarrelBattleData> list = map.get(bornPoint);
		// System.err.println("@@@@@@@@@@  bornPoint_"+bornPoint._objInstanceId+":x-"+bornPoint._corX+",y-"+bornPoint._corY+",size:"+list.size());
		// for (KBarrelBattleData barrelBattleData:list) {
		// System.err.println("barrelBattleData:templateId:"+barrelBattleData.monstTemplate.id+",time:"+barrelBattleData.bornTimeMillis);
		// }
		// System.err.println("######################################################");
		// }
		//
		// System.err.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
	}

	public static BarrelDataStruct getBarrelDataStruct(int roleLv) {
		int lvKey = 0;
		for (Integer lv : barrelDataMap.keySet()) {
			lvKey = lv;
			if (roleLv <= lv) {
				break;
			}
		}
		return barrelDataMap.get(lvKey);
	}

	public static KActionResult playerRoleJoinActivity(KRole role) {
		String tips = "";
		GoldActivityRoleRecordData record = KActivityRoleExtCaCreator
				.getGoldActivityRoleRecordData(role.getId());
		record.checkAndRestData(true);

		if (record.restChallengeCount <= 0) {

			tips = GoldActivityTips
					.getTipsMaxChallengeCount(max_can_challenge_count);

			KDialogService.sendUprisingDialog(role, tips);
			return new KActionResult(false, tips);
		}

		record.challenge();
		KDialogService.sendNullDialog(role);

		for (FightEventListener listener : KGameLevelModuleExtension
				.getManager().getFightEventListenerList()) {
			listener.notifyGoldActivityBattle(role, battlefield);
		}

		KGoldActivity.getInstance().sendUpdateActivity(role);

		return new KActionResult(true, tips);
	}

	public static void processCompleteBattle(KRole role, FightResult result) {
		KActivityRoleExtData actData = KActivityRoleExtCaCreator.getActivityRoleExtData(role.getId());
		if(actData!=null){
			actData.notifyActivityCdTime(KGoldActivity.getInstance().getActivityId());
		}

		if (result.getEndType() == FightResult.FIGHT_END_TYPE_ESCAPE) {
			KDialogService.sendNullDialog(role);
			KSupportFactory.getMapSupport().processRoleFinishBattleReturnToMap(role);
			KGoldActivity.getInstance().sendUpdateActivity(role);
			return;
		}

		GoldActivityReward reward = rewardMap.get(role.getLevel());

		// 计算关卡战斗等级
		byte fightLv = caculateLevelFightEvaluate(result);

		// 计算关卡所有奖励
		LevelRewardResultData rewardData = caculateLevelReward(role, result,
				fightLv);

		BarrelDataStruct struct = getBarrelDataStruct(role.getLevel());
		short[] count = new short[] { 0, 0, 0 };
		for (int i = 0; i < struct.barrelIdShowSet.size() && i < count.length; i++) {
			int barrelId = struct.barrelIdShowSet.get(i);
			if (result.getKillMonsterCount().containsKey(barrelId)) {
				count[i] = result.getKillMonsterCount().get(barrelId);
			}
		}

		// 发送完成关卡消息
		KGameMessage sendMsg = KGame
				.newLogicMessage(KLevelProtocol.SM_SHOW_COMPLETE_BARREL_BATTLE_RESULT);
		sendMsg.writeInt(1);
		sendMsg.writeByte(KGameLevelTypeEnum.产金活动关卡.levelType);
		sendMsg.writeBoolean(result.isWin());
		sendMsg.writeByte(fightLv);
		sendMsg.writeInt((int) (result.getBattleTime() / 1000));

		sendMsg.writeShort(count[0]);
		sendMsg.writeShort(count[1]);
		sendMsg.writeShort(count[2]);

		sendMsg.writeShort(result.getMaxBeHitCount());

		rewardData.baseReward.packMsg(sendMsg);

		if (fightLv == FightEvaluateData.MAX_FIGHT_LEVEL
				&& rewardData.sLevelReward != null) {
			sendMsg.writeBoolean(true);
			rewardData.sLevelReward.packMsg(sendMsg);
		} else {
			sendMsg.writeBoolean(false);
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
		
		KGoldActivity.getInstance().sendUpdateActivity(role);
	}

	public static byte caculateLevelFightEvaluate(FightResult result) {
		byte value = -1;
		// byte temp = -1;
		int hitCount = result.getMaxDoubleHitCount();
		int beHitCount = result.getMaxBeHitCount();
		for (FightEvaluateData data : fightEvaluateDataMap.values()) {
			if (hitCount < data.maxHitCount) {
				continue;
			} else if (beHitCount > data.hitByCount) {
				continue;
			} else {
				value = data.fightLv;
				break;
			}
		}
		if (value == -1) {
			value = 1;
		}
		return value;
	}

	public static LevelRewardResultData caculateLevelReward(KRole role,
			FightResult fightResult, int fightLv) {
		GoldActivityReward reward = rewardMap.get(role.getLevel());
		LevelRewardResultData resultData = new LevelRewardResultData(
				CompleteGameLevelTempRecord.LOTTERY_TYPE_CURRENCY);

		// 计算关卡数值奖励
		List<AttValueStruct> attList = new ArrayList<AttValueStruct>();
		List<AttValueStruct> attShowList = new ArrayList<AttValueStruct>();

		// 处理关卡货币奖励
		List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
		List<KCurrencyCountStruct> moneyShowList = new ArrayList<KCurrencyCountStruct>();
		moneyList.add(new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD,
				reward.goldValue));
		moneyShowList.add(new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD,
				reward.goldValue));

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
		List<ItemCountStruct> sLv_itemRewardList = new ArrayList<ItemCountStruct>();
		if (fightLv == FightEvaluateData.MAX_FIGHT_LEVEL) {
			List<KCurrencyCountStruct> s_moneyList = new ArrayList<KCurrencyCountStruct>();
			s_moneyList.add(new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD,
					reward.s_goldValue));
			sLevelReward = new BaseRewardData(
					Collections.<AttValueStruct> emptyList(), s_moneyList,
					sLv_itemRewardList, Collections.<Integer> emptyList(),
					Collections.<Integer> emptyList());

			for (KCurrencyCountStruct attReward : s_moneyList) {
				KSupportFactory.getCurrencySupport().increaseMoney(
						role.getId(), attReward, PresentPointTypeEnum.产金活动奖励, false);
			}
		}

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
		temprecord.setLevelType(KGameLevelTypeEnum.产金活动关卡.levelType);
		List<ItemCountStruct> temprecordItemList = new ArrayList<ItemCountStruct>();
		temprecordItemList.addAll(itemRewardList);
		temprecordItemList.addAll(sLv_itemRewardList);
		temprecordItemList = ItemCountStruct
				.mergeItemCountStructs(temprecordItemList);
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
		resultData.totalItemSize = itemRewardList.size()
				+ sLv_itemRewardList.size();
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

	public static void main(String[] a) {

		try {
			KGoldActivityManager
					.init("./res/gsconfig/activityModule/goldActivityConfig.xml");

			KGoldActivityManager.onGameWorldInitCompleted();
		} catch (KGameServerException e) {
			e.printStackTrace();
		}

	}

}
