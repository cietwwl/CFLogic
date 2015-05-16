package com.kola.kmp.logic.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.level.KGameLevelDropPool.KDropGroup;
import com.kola.kmp.logic.level.KGameLevelDropPool.KDropGroupItemTemplate;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.support.KSupportFactory;

public class KLevelReward {

	/**
	 * 道具掉落的最大比率系数（万分比）
	 */
	public static final int MAX_ITEM_REWARD_RADIO = 10000;

	// 所有的常规道具掉落奖励模版列表
	private Map<Byte, List<NormalItemRewardTemplate>> allItemReward = new HashMap<Byte, List<NormalItemRewardTemplate>>();
	// 必掉道具列表
	private Map<Byte, List<NormalItemRewardTemplate>> certainItemRewardMap = new HashMap<Byte, List<NormalItemRewardTemplate>>();
	// 需要计算掉落概率的道具列表
	private Map<Byte, List<NormalItemRewardTemplate>> caculateItemRewardMap = new HashMap<Byte, List<NormalItemRewardTemplate>>();

	// S级别所有的常规道具掉落奖励模版列表
	private List<NormalItemRewardTemplate> s_allItemReward = new ArrayList<NormalItemRewardTemplate>();
	// S级别必掉道具列表
	private List<NormalItemRewardTemplate> s_certainItemRewardMap = new ArrayList<NormalItemRewardTemplate>();
	// S级别需要计算掉落概率的道具列表
	private List<NormalItemRewardTemplate> s_caculateItemRewardMap = new ArrayList<NormalItemRewardTemplate>();
	// 普通可能掉落数据
	public Map<Byte, BaseRewardData> probableReward = new HashMap<Byte, BaseRewardData>();
	// s级别可能掉落数据
	public BaseRewardData s_probableReward;

	private Map<Byte, Integer> totalDropWeight = new HashMap<Byte, Integer>();

	private int s_totalDropWeight = 0;
	// 是否有抽奖类型奖励
	private boolean isHasLotteryReward = false;
	// 抽奖数据信息
	public Map<Byte, LotteryGroup> lotteryMap = new HashMap<Byte, LotteryGroup>();

	// 是否有首次通关礼包
	private boolean isHasFirstDropItem = false;

	// 首次通关礼包
	private ItemCountStruct firstDropItem;

	private Map<Integer, Integer> dropPoolMap = new LinkedHashMap<Integer, Integer>();

	// 关卡UI显示的普通掉落数据
	private Map<Byte, BaseRewardData> showRewardDataMap = new HashMap<Byte, BaseRewardData>();

	// 关卡UI显示的普通掉落数据
	private Map<Byte, List<Byte>> showRewardDataDropRateMap = new HashMap<Byte, List<Byte>>();

	private Set<String> checkDropItemCodeSet = new HashSet<String>();

	public void initKLevelReward(String tableName, KGameExcelRow xlsRow,
			KGameLevelTypeEnum levelType) throws KGameServerException {

		for (KJobTypeEnum job : KJobTypeEnum.values()) {
			allItemReward.put(job.getJobType(),
					new ArrayList<NormalItemRewardTemplate>());
			certainItemRewardMap.put(job.getJobType(),
					new ArrayList<NormalItemRewardTemplate>());
			caculateItemRewardMap.put(job.getJobType(),
					new ArrayList<NormalItemRewardTemplate>());
			totalDropWeight.put(job.getJobType(), 0);
		}

		List<AttValueStruct> attList = new ArrayList<AttValueStruct>();
		int expReward = xlsRow.getInt("copy_exp");
		if (expReward > 0) {
			attList.add(new AttValueStruct(KGameAttrType.EXPERIENCE, expReward,
					0));
		}

		List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
		List<KCurrencyCountStruct> s_moneyList = new ArrayList<KCurrencyCountStruct>();
		long copper = xlsRow.getLong("copy_gold");
		if (copper > 0) {
			KCurrencyCountStruct struct = new KCurrencyCountStruct(
					KCurrencyTypeEnum.GOLD, copper);
			moneyList.add(struct);
		}
		long s_copper = xlsRow.getLong("s_gold");
		if (s_copper > 0) {
			KCurrencyCountStruct struct = new KCurrencyCountStruct(
					KCurrencyTypeEnum.GOLD, s_copper);
			s_moneyList.add(struct);
		}

		int potential = xlsRow.getInt("copy_battlepoint");
		if (potential > 0) {
			KCurrencyCountStruct struct = new KCurrencyCountStruct(
					KCurrencyTypeEnum.POTENTIAL, potential);
			moneyList.add(struct);
		}
		int s_potential = xlsRow.getInt("s_battlepoint");
		if (s_potential > 0) {
			KCurrencyCountStruct struct = new KCurrencyCountStruct(
					KCurrencyTypeEnum.POTENTIAL, s_potential);
			s_moneyList.add(struct);
		}

		int score = xlsRow.getInt("copy_prestige");
		if (score > 0) {
			KCurrencyCountStruct struct = new KCurrencyCountStruct(
					KCurrencyTypeEnum.SCORE, score);
			moneyList.add(struct);
		}

		String soldier_itemRewardData = xlsRow.getData("soldier_item");
		String ninja_itemRewardData = xlsRow.getData("ninja_item");
		String gun_itemRewardData = xlsRow.getData("gun_item");
		List<ItemCountStruct> soldier_itemList = initNormalItemReward(
				tableName, "soldier_item", soldier_itemRewardData, false,
				KJobTypeEnum.WARRIOR.getJobType(), xlsRow.getIndexInFile());
		List<ItemCountStruct> ninja_itemList = initNormalItemReward(tableName,
				"ninja_item", ninja_itemRewardData, false,
				KJobTypeEnum.SHADOW.getJobType(), xlsRow.getIndexInFile());
		List<ItemCountStruct> gun_itemList = initNormalItemReward(tableName,
				"gun_item", gun_itemRewardData, false,
				KJobTypeEnum.GUNMAN.getJobType(), xlsRow.getIndexInFile());
		probableReward.put(
				KJobTypeEnum.WARRIOR.getJobType(),
				new BaseRewardData(attList, moneyList, soldier_itemList,
						Collections.<Integer> emptyList(), Collections
								.<Integer> emptyList()));
		probableReward.put(
				KJobTypeEnum.SHADOW.getJobType(),
				new BaseRewardData(attList, moneyList, ninja_itemList,
						Collections.<Integer> emptyList(), Collections
								.<Integer> emptyList()));
		probableReward.put(
				KJobTypeEnum.GUNMAN.getJobType(),
				new BaseRewardData(attList, moneyList, gun_itemList,
						Collections.<Integer> emptyList(), Collections
								.<Integer> emptyList()));

		String s_itemRewardData = xlsRow.getData("s_item");
		List<ItemCountStruct> s_itemList = initNormalItemReward(tableName,
				"s_item", s_itemRewardData, true, (byte) 0,
				xlsRow.getIndexInFile());

		s_probableReward = new BaseRewardData(null, s_moneyList, s_itemList,
				Collections.<Integer> emptyList(),
				Collections.<Integer> emptyList());

		if (xlsRow.containsCol("first_box")) {
			String firstDropStr = xlsRow.getData("first_box");
			if (firstDropStr != null && firstDropStr.split("\\*").length == 2) {
				this.isHasFirstDropItem = true;
				String[] itemInfo = firstDropStr.split("\\*");
				KItemTempAbs itemTemp = KSupportFactory.getItemModuleSupport()
						.getItemTemplate(itemInfo[0]);
				if (itemTemp == null) {
					throw new KGameServerException("初始化表<" + tableName
							+ ">字段first_box的道具错误：" + firstDropStr
							+ "找不到道具类型，excel行数：" + xlsRow.getIndexInFile());
				}
				Long count = Long.parseLong(itemInfo[1]);
				this.firstDropItem = new ItemCountStruct(itemTemp, count);
			}
		}

		// for (KJobTypeEnum job:KJobTypeEnum.values()) {
		// if(!allItemReward.containsKey(job.getJobType())
		// ||!certainItemRewardMap.containsKey(job.getJobType())
		// ||!caculateItemRewardMap.containsKey(job.getJobType())){
		// throw new KGameServerException("初始化表<" + tableName
		// + ">字段道具错误，找不到职业："+job+"的道具类型，excel行数：" + xlsRow.getIndexInFile());
		// }
		// }

		for (int i = 1; i <= 6; i++) {
			String drop_ID = xlsRow.getData("drop_ID" + i);
			if (drop_ID != null && drop_ID.length() > 0 && !drop_ID.equals("0")) {
				int dropGroupId = xlsRow.getInt("drop_ID" + i);
				int weight = xlsRow.getInt("weight" + i);
				if (!KGameLevelDropPool.dropGroupMap.containsKey(dropGroupId)) {
					throw new KGameServerException("初始化表<" + tableName
							+ ">字段drop_ID" + i + "错误，找不到该掉落组ID，excel行数："
							+ xlsRow.getIndexInFile());
				}
				this.dropPoolMap.put(dropGroupId, weight);
				KDropGroup group = KGameLevelDropPool.dropGroupMap
						.get(dropGroupId);
				for (KDropGroupItemTemplate temp : group.itemList) {
					this.checkDropItemCodeSet.add(temp.itemCode);
				}
			}
		}

		String soldier_itemShowData = xlsRow.getData("soldier_itemdisplay");
		List<ItemCountStruct> soldier_itemShowList = Collections
				.<ItemCountStruct> emptyList();
		if (soldier_itemShowData != null && soldier_itemShowData.length() > 0
				&& !soldier_itemShowData.equals("0")) {
			soldier_itemShowList = decodeShowItemString(tableName,
					"soldier_itemdisplay", soldier_itemShowData,
					xlsRow.getIndexInFile(), levelType,
					KJobTypeEnum.WARRIOR.getJobType());
		}
		showRewardDataMap.put(
				KJobTypeEnum.WARRIOR.getJobType(),
				new BaseRewardData(attList, moneyList, soldier_itemShowList,
						Collections.<Integer> emptyList(), Collections
								.<Integer> emptyList()));

		String ninja_itemShowData = xlsRow.getData("ninja_itemdisplay");
		List<ItemCountStruct> ninja_itemShowList = Collections
				.<ItemCountStruct> emptyList();
		if (ninja_itemShowData != null && ninja_itemShowData.length() > 0
				&& !ninja_itemShowData.equals("0")) {
			ninja_itemShowList = decodeShowItemString(tableName,
					"ninja_itemdisplay", ninja_itemShowData,
					xlsRow.getIndexInFile(), levelType,
					KJobTypeEnum.SHADOW.getJobType());
		}
		showRewardDataMap.put(
				KJobTypeEnum.SHADOW.getJobType(),
				new BaseRewardData(attList, moneyList, ninja_itemShowList,
						Collections.<Integer> emptyList(), Collections
								.<Integer> emptyList()));

		String gun_itemShowData = xlsRow.getData("gun_itemdisplay");
		List<ItemCountStruct> gun_itemShowList = Collections
				.<ItemCountStruct> emptyList();
		if (gun_itemShowData != null && gun_itemShowData.length() > 0
				&& !gun_itemShowData.equals("0")) {
			gun_itemShowList = decodeShowItemString(tableName,
					"gun_itemdisplay", gun_itemShowData,
					xlsRow.getIndexInFile(), levelType,
					KJobTypeEnum.GUNMAN.getJobType());
		}
		showRewardDataMap.put(
				KJobTypeEnum.GUNMAN.getJobType(),
				new BaseRewardData(attList, moneyList, gun_itemShowList,
						Collections.<Integer> emptyList(), Collections
								.<Integer> emptyList()));
	}

	public void initFirstNoviceGuideBattleLevelReward(String tableName,
			KGameExcelRow xlsRow) throws KGameServerException {
		for (KJobTypeEnum job : KJobTypeEnum.values()) {
			allItemReward.put(job.getJobType(),
					new ArrayList<NormalItemRewardTemplate>());
			certainItemRewardMap.put(job.getJobType(),
					new ArrayList<NormalItemRewardTemplate>());
			caculateItemRewardMap.put(job.getJobType(),
					new ArrayList<NormalItemRewardTemplate>());
			totalDropWeight.put(job.getJobType(), 0);
		}

		List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
		long copper = xlsRow.getLong("copy_gold");
		if (copper > 0) {
			KCurrencyCountStruct struct = new KCurrencyCountStruct(
					KCurrencyTypeEnum.GOLD, copper);
			moneyList.add(struct);
		}
		List<AttValueStruct> attRewardList = new ArrayList<AttValueStruct>();
		int exp = xlsRow.getInt("copy_exp");
		if(exp > 0) {
			attRewardList.add(new AttValueStruct(KGameAttrType.EXPERIENCE, exp));
		}

		String soldier_itemRewardData = xlsRow.getData("soldier_item");
		String ninja_itemRewardData = xlsRow.getData("ninja_item");
		String gun_itemRewardData = xlsRow.getData("gun_item");
		List<ItemCountStruct> soldier_itemList = initNormalItemReward(
				tableName, "soldier_item", soldier_itemRewardData, false,
				KJobTypeEnum.WARRIOR.getJobType(), xlsRow.getIndexInFile());
		List<ItemCountStruct> ninja_itemList = initNormalItemReward(tableName,
				"ninja_item", ninja_itemRewardData, false,
				KJobTypeEnum.SHADOW.getJobType(), xlsRow.getIndexInFile());
		List<ItemCountStruct> gun_itemList = initNormalItemReward(tableName,
				"gun_item", gun_itemRewardData, false,
				KJobTypeEnum.GUNMAN.getJobType(), xlsRow.getIndexInFile());
		probableReward.put(
				KJobTypeEnum.WARRIOR.getJobType(),
				new BaseRewardData(attRewardList,
						moneyList, soldier_itemList, Collections
								.<Integer> emptyList(), Collections
								.<Integer> emptyList()));
		probableReward.put(
				KJobTypeEnum.SHADOW.getJobType(),
				new BaseRewardData(attRewardList,
						moneyList, ninja_itemList, Collections
								.<Integer> emptyList(), Collections
								.<Integer> emptyList()));
		probableReward.put(
				KJobTypeEnum.GUNMAN.getJobType(),
				new BaseRewardData(attRewardList,
						moneyList, gun_itemList, Collections
								.<Integer> emptyList(), Collections
								.<Integer> emptyList()));
	}

	public void initLotteryReward(String tableName, KGameExcelRow xlsRow)
			throws KGameServerException {
		// lotteryMap.put(KJobTypeEnum.WARRIOR.getJobType(),
		// new LinkedHashMap<Integer, LotteryGroup>());
		// lotteryMap.put(KJobTypeEnum.GUNMAN.getJobType(),
		// new LinkedHashMap<Integer, LotteryGroup>());
		// lotteryMap.put(KJobTypeEnum.SHADOW.getJobType(),
		// new LinkedHashMap<Integer, LotteryGroup>());
		// for (int i = 0; i < 5; i++) {
		// int usePoint = 0;
		// if (i > 0) {
		// usePoint = xlsRow.getInt("use_rmb" + (i + 1));
		// if (usePoint <= 0 || lotteryMap.containsKey(usePoint)) {
		// throw new KGameServerException("初始化表<" + tableName
		// + ">的字段<use_rmb" + (i + 1)
		// + ">错误，使用钻石数不能0或不能充复：，excel行数："
		// + xlsRow.getIndexInFile() + ",字段名：use_rmb"
		// + (i + 1));
		// }
		// }
		// String soldier_itemInfo = xlsRow
		// .getData("soldierrewards" + (i + 1));
		// String ninja_itemInfo = xlsRow.getData("ninjarewards" + (i + 1));
		// String gun_itemInfo = xlsRow.getData("gunrewards" + (i + 1));
		// if (soldier_itemInfo == null || ninja_itemInfo == null
		// || gun_itemInfo == null) {
		// throw new KGameServerException("初始化表<" + tableName
		// + ">的道具格式错误：soldierrewards" + (i + 1) + "="
		// + soldier_itemInfo + "，ninjarewards" + (i + 1) + "="
		// + ninja_itemInfo + "，gunrewards" + (i + 1) + "="
		// + gun_itemInfo + "，" + "excel行数："
		// + xlsRow.getIndexInFile());
		// }
		// this.isHasLotteryReward = true;
		// LotteryGroup soldier_group = new LotteryGroup();
		// soldier_group.setLotteryGroupUsePoint(usePoint);
		// soldier_group.initNormalItemReward(tableName, soldier_itemInfo,
		// xlsRow.getIndexInFile());
		// LotteryGroup ninja_group = new LotteryGroup();
		// ninja_group.setLotteryGroupUsePoint(usePoint);
		// ninja_group.initNormalItemReward(tableName, ninja_itemInfo,
		// xlsRow.getIndexInFile());
		// LotteryGroup gun_group = new LotteryGroup();
		// gun_group.setLotteryGroupUsePoint(usePoint);
		// gun_group.initNormalItemReward(tableName, gun_itemInfo,
		// xlsRow.getIndexInFile());
		// this.lotteryMap.get(KJobTypeEnum.WARRIOR.getJobType()).put(
		// soldier_group.lotteryGroupUsePoint, soldier_group);
		// this.lotteryMap.get(KJobTypeEnum.SHADOW.getJobType()).put(
		// ninja_group.lotteryGroupUsePoint, ninja_group);
		// this.lotteryMap.get(KJobTypeEnum.GUNMAN.getJobType()).put(
		// gun_group.lotteryGroupUsePoint, gun_group);
		// }

		this.isHasLotteryReward = true;

		String usePointInfo = xlsRow.getData("use_point");
		if (usePointInfo == null || usePointInfo.equals("")) {
			throw new KGameServerException("初始化表<" + tableName
					+ ">的道具格式错误：use_point=" + usePointInfo + ",excel行数："
					+ xlsRow.getIndexInFile());
		}
		String[] usePointStr = usePointInfo.split(",");
		if (usePointStr == null || usePointStr.length != 5) {
			throw new KGameServerException("初始化表<" + tableName
					+ ">的道具格式错误：use_point=" + usePointInfo + ",excel行数："
					+ xlsRow.getIndexInFile());
		}
		List<Integer> usePointList = new ArrayList<Integer>();
		for (int i = 0; i < usePointStr.length; i++) {
			usePointList.add(Integer.parseInt(usePointStr[i]));
		}

		// 每张卡的权重表
		List<Integer> cardWeightList = new ArrayList<Integer>();
		// 每张卡的权重保护次数表
		List<Integer> cardProtectCountList = new ArrayList<Integer>();
		for (int i = 0; i < 5; i++) {
			String[] cardInfo = xlsRow.getData(
					"card" + (i + 1) + "_weightAndProtect").split("\\*");
			if (cardInfo != null && cardInfo.length == 2) {
				cardWeightList.add(Integer.parseInt(cardInfo[0]));
				cardProtectCountList.add(Integer.parseInt(cardInfo[1]));
			} else {
				throw new KGameServerException("初始化表<" + tableName
						+ ">的道具格式错误：card" + (i + 1) + "_weightAndProtect="
						+ cardInfo + ",excel行数：" + xlsRow.getIndexInFile());
			}
		}

		// 战士翻牌
		String[] soldier_itemInfo = new String[5];// xlsRow.getData("soldier_flop");
		for (int i = 0; i < soldier_itemInfo.length; i++) {
			soldier_itemInfo[i] = xlsRow.getData("card" + (i + 1) + "_soldier");
			if (soldier_itemInfo[i] == null) {
				throw new KGameServerException("初始化表<" + tableName
						+ ">的道具格式错误：card" + (i + 1) + "_soldier="
						+ soldier_itemInfo[i] + ",excel行数："
						+ xlsRow.getIndexInFile());
			}
		}
		LotteryGroup soldier_group = new LotteryGroup();
		soldier_group.initNormalItemReward(tableName, soldier_itemInfo,
				xlsRow.getIndexInFile());
		soldier_group.setLotteryGroupUsePointList(usePointList);
		soldier_group.setCardWeightList(cardWeightList);
		soldier_group.setCardProtectCountList(cardProtectCountList);
		soldier_group.checkLotteryGroup(tableName, xlsRow.getIndexInFile());
		lotteryMap.put(KJobTypeEnum.WARRIOR.getJobType(), soldier_group);

		// 忍者翻牌
		String[] ninja_itemInfo = new String[5];// xlsRow.getData("ninja_flop");
		for (int i = 0; i < ninja_itemInfo.length; i++) {
			ninja_itemInfo[i] = xlsRow.getData("card" + (i + 1) + "_ninja");
			if (ninja_itemInfo[i] == null) {
				throw new KGameServerException("初始化表<" + tableName
						+ ">的道具格式错误：card" + (i + 1) + "_ninja="
						+ ninja_itemInfo[i] + ",excel行数："
						+ xlsRow.getIndexInFile());
			}
		}
		LotteryGroup ninja_group = new LotteryGroup();
		ninja_group.initNormalItemReward(tableName, ninja_itemInfo,
				xlsRow.getIndexInFile());
		ninja_group.setLotteryGroupUsePointList(usePointList);
		ninja_group.setCardWeightList(cardWeightList);
		ninja_group.setCardProtectCountList(cardProtectCountList);
		ninja_group.checkLotteryGroup(tableName, xlsRow.getIndexInFile());
		lotteryMap.put(KJobTypeEnum.SHADOW.getJobType(), ninja_group);

		// 枪手翻牌
		String[] gun_itemInfo = new String[5];// xlsRow.getData("gun_flop");
		for (int i = 0; i < gun_itemInfo.length; i++) {
			gun_itemInfo[i] = xlsRow.getData("card" + (i + 1) + "_gun");
			if (gun_itemInfo[i] == null) {
				throw new KGameServerException("初始化表<" + tableName
						+ ">的道具格式错误：card" + (i + 1) + "_gun=" + gun_itemInfo
						+ ",excel行数：" + xlsRow.getIndexInFile());
			}
		}
		LotteryGroup gun_group = new LotteryGroup();
		gun_group.initNormalItemReward(tableName, gun_itemInfo,
				xlsRow.getIndexInFile());
		gun_group.setLotteryGroupUsePointList(usePointList);
		gun_group.setCardWeightList(cardWeightList);
		gun_group.setCardProtectCountList(cardProtectCountList);
		gun_group.checkLotteryGroup(tableName, xlsRow.getIndexInFile());
		lotteryMap.put(KJobTypeEnum.GUNMAN.getJobType(), gun_group);
	}

	public List<NormalItemRewardTemplate> getAllItemReward(byte jobType) {
		return allItemReward.get(jobType);
	}

	private List<ItemCountStruct> initNormalItemReward(String tableName,
			String fieldName, String dropData, boolean is_S_Level,
			byte jobType, int index) throws KGameServerException {
		List<ItemCountStruct> list = new ArrayList<ItemCountStruct>();
		if (dropData == null || dropData.equals("0") || dropData.length() == 0) {
			return list;
		}
		if (dropData != null) {
			String[] itemInfoStr = dropData.split(",");
			if (itemInfoStr != null && itemInfoStr.length > 0) {
				for (int i = 0; i < itemInfoStr.length; i++) {
					String[] itemData = itemInfoStr[i].split("\\*");
					if (itemData != null && itemData.length >= 2) {
						NormalItemRewardTemplate itemTemplate = null;
						String itemCode = itemData[0];
						int count = Integer.parseInt(itemData[1]);
						boolean isNoneDrop = false;
						if (itemData.length == 2) {
							itemTemplate = new NormalItemRewardTemplate(
									itemCode, count);
							if (KSupportFactory.getItemModuleSupport()
									.getItemTemplate(itemCode) == null) {
								throw new KGameServerException("初始化表<"
										+ tableName + ">的道具错误，找不到道具类型："
										+ itemCode + "，字段：" + fieldName + "="
										+ dropData + "，excel行数：" + index);
							}
							if (!is_S_Level) {
								this.allItemReward.get(jobType).add(
										itemTemplate);
								this.certainItemRewardMap.get(jobType).add(
										itemTemplate);
							} else {
								this.s_allItemReward.add(itemTemplate);
								this.s_certainItemRewardMap.add(itemTemplate);
							}
						} else if (itemData.length == 3) {
							int dropWeight = Integer.parseInt(itemData[2]);
							if (itemCode.equals("0")) {
								itemTemplate = new NormalItemRewardTemplate(
										dropWeight);
								isNoneDrop = true;
							} else {
								itemTemplate = new NormalItemRewardTemplate(
										itemCode, count, dropWeight);
							}
							if (!itemTemplate.isNoneDrop
									&& KSupportFactory.getItemModuleSupport()
											.getItemTemplate(itemCode) == null) {
								throw new KGameServerException("初始化表<"
										+ tableName + ">的道具错误，找不到道具类型："
										+ itemCode + "，字段：" + fieldName + "="
										+ dropData + "，excel行数：" + index);
							}
							if (!is_S_Level) {
								this.allItemReward.get(jobType).add(
										itemTemplate);
								this.caculateItemRewardMap.get(jobType).add(
										itemTemplate);
								int weight = 0;
								if (totalDropWeight.containsKey(jobType)) {
									weight = totalDropWeight.get(jobType);
								}
								this.totalDropWeight.put(jobType,
										(weight + dropWeight));
							} else {
								this.s_allItemReward.add(itemTemplate);
								this.s_caculateItemRewardMap.add(itemTemplate);
								this.s_totalDropWeight += dropWeight;
							}
						}
						if (!isNoneDrop) {
							list.add(new ItemCountStruct(KSupportFactory
									.getItemModuleSupport().getItemTemplate(
											itemCode), count));
							this.checkDropItemCodeSet.add(itemCode);
						}
					} else {
						throw new KGameServerException("初始化表<" + tableName
								+ ">的道具格式错误：：" + dropData + "，excel行数：" + index);
					}
				}
			}
		}
		return list;
	}

	public List<ItemCountStruct> caculateItemReward(byte jobType, int multiple) {
		List<ItemCountStruct> list = new ArrayList<ItemCountStruct>();
		KItemTempAbs itemTemplate;
		for (NormalItemRewardTemplate template : certainItemRewardMap
				.get(jobType)) {
			itemTemplate = KSupportFactory.getItemModuleSupport()
					.getItemTemplate(template.getItemCode());
			list.add(new ItemCountStruct(itemTemplate, template.rewardCount));
		}
		int totalWeight = totalDropWeight.get(jobType);
		if (totalWeight > 0) {
			int weight = UtilTool.random(0, totalWeight);
			int tempRate = 0;
			for (NormalItemRewardTemplate template : caculateItemRewardMap
					.get(jobType)) {
				if (tempRate < weight
						&& weight <= (tempRate + template.dropWeight)) {
					if (!template.isNoneDrop) {
						itemTemplate = KSupportFactory.getItemModuleSupport()
								.getItemTemplate(template.getItemCode());
						list.add(new ItemCountStruct(itemTemplate,
								template.rewardCount * multiple));
					}
					break;
				} else {
					tempRate += template.dropWeight;
				}
			}
		}

		return list;
	}

	public List<ItemCountStruct> caculateS_ItemReward(int multiple) {
		List<ItemCountStruct> list = new ArrayList<ItemCountStruct>();
		KItemTempAbs itemTemplate;
		for (NormalItemRewardTemplate template : s_certainItemRewardMap) {
			itemTemplate = KSupportFactory.getItemModuleSupport()
					.getItemTemplate(template.getItemCode());
			list.add(new ItemCountStruct(itemTemplate, template.rewardCount
					* multiple));
		}
		if (s_totalDropWeight > 0) {
			int weight = UtilTool.random(0, s_totalDropWeight);
			int tempRate = 0;
			for (NormalItemRewardTemplate template : s_caculateItemRewardMap) {
				if (tempRate < weight
						&& weight <= (tempRate + template.dropWeight)) {
					if (!template.isNoneDrop) {
						itemTemplate = KSupportFactory.getItemModuleSupport()
								.getItemTemplate(template.getItemCode());
						list.add(new ItemCountStruct(itemTemplate,
								template.rewardCount * multiple));
					}
					break;
				} else {
					tempRate += template.dropWeight;
				}
			}
		}

		return list;
	}

	/**
	 * 该关卡奖励是否有抽奖奖励方式
	 * 
	 * @return
	 */
	public boolean isHasLotteryReward() {
		return isHasLotteryReward;
	}

	/**
	 * 设置该关卡奖励是否有抽奖奖励方式
	 * 
	 * @return
	 */
	public void setHasLotteryReward(boolean isHasLotteryReward) {
		this.isHasLotteryReward = isHasLotteryReward;
	}

	public boolean isHasFirstDropItem() {
		return isHasFirstDropItem;
	}

	public ItemCountStruct getFirstDropItem() {
		return firstDropItem;
	}

	public Set<String> getCheckDropItemCodeSet() {
		return checkDropItemCodeSet;
	}

	private List<ItemCountStruct> decodeShowItemString(String tableName,
			String fieldName, String data, int index,
			KGameLevelTypeEnum levelType, byte jobType)
			throws KGameServerException {
		List<ItemCountStruct> list = new ArrayList<ItemCountStruct>();
		String[] itemCodes = data.split(",");
		for (int i = 0; i < itemCodes.length; i++) {
			if (levelType == KGameLevelTypeEnum.技术副本关卡
					|| levelType == KGameLevelTypeEnum.精英副本关卡
					|| levelType == KGameLevelTypeEnum.随从挑战副本关卡
					|| levelType == KGameLevelTypeEnum.高级随从挑战副本关卡) {
				String[] codes = itemCodes[i].split("\\*");
				if (codes == null || codes.length != 2) {
					throw new KGameServerException("初始化表<" + tableName
							+ ">的道具错误：" + itemCodes[i] + "，字段：" + fieldName
							+ "=" + data + "，excel行数：" + index);
				}
				if (KSupportFactory.getItemModuleSupport().getItemTemplate(
						codes[0]) == null) {
					throw new KGameServerException("初始化表<" + tableName
							+ ">的道具错误，找不到道具类型：" + itemCodes[i] + "，字段："
							+ fieldName + "=" + data + "，excel行数：" + index);
				}
				list.add(new ItemCountStruct(codes[0], 1));
				byte rate = Byte.parseByte(codes[1]);
				if (!showRewardDataDropRateMap.containsKey(jobType)) {
					showRewardDataDropRateMap.put(jobType,
							new ArrayList<Byte>());
				}
				showRewardDataDropRateMap.get(jobType).add(rate);
			} else {
				if (KSupportFactory.getItemModuleSupport().getItemTemplate(
						itemCodes[i]) == null) {
					throw new KGameServerException("初始化表<" + tableName
							+ ">的道具错误，找不到道具类型：" + itemCodes[i] + "，字段："
							+ fieldName + "=" + data + "，excel行数：" + index);
				}
				list.add(new ItemCountStruct(itemCodes[i], 1));
			}
		}
		return list;
	}

	public BaseRewardData getShowRewardData(byte jobType) {
		return showRewardDataMap.get(jobType);
	}

	public List<Byte> getShowRewardDataDropRate(byte jobType) {
		if (showRewardDataDropRateMap.containsKey(jobType)) {
			return showRewardDataDropRateMap.get(jobType);
		} else {
			return Collections.<Byte> emptyList();
		}
	}

	public List<ItemCountStruct> caculateDropPoolItems(int multiple) {
		List<ItemCountStruct> list = new ArrayList<ItemCountStruct>();

		for (Integer dropGroupId : dropPoolMap.keySet()) {
			int weight = dropPoolMap.get(dropGroupId);
			KDropGroup group = KGameLevelDropPool.dropGroupMap.get(dropGroupId);
			if (group != null) {
				int rate = UtilTool.random(MAX_ITEM_REWARD_RADIO);
				if (weight >= rate) {
					list.add(group.caculateDropItem().getItemCountStruct(
							multiple));
				}
			}
		}

		return list;
	}

	/**
	 * 表示游戏常规道具掉落奖励的数据模版
	 * 
	 * @author zhaizl
	 * 
	 */
	public static class NormalItemRewardTemplate {
		// 掉落道具的模版编号
		public String itemCode;
		// 掉落数量
		public int rewardCount;
		// 掉落系数
		public int dropWeight;
		// 表示什么都不掉落
		public boolean isNoneDrop;
		// 掉落保护次数（卡牌抽奖时生效）
		public int dropProtectCount;

		/**
		 * 是否肯定掉落（不需要计算权重）
		 */
		public boolean isCertainDrop;

		public NormalItemRewardTemplate(String itemCode, int rewardCount,
				int dropWeight) {
			this.itemCode = itemCode;
			this.rewardCount = rewardCount;
			this.dropWeight = dropWeight;
			this.isCertainDrop = false;
			this.isNoneDrop = false;
		}

		public NormalItemRewardTemplate(String itemCode, int rewardCount) {
			this.itemCode = itemCode;
			this.rewardCount = rewardCount;
			this.dropWeight = 0;
			this.isCertainDrop = true;
		}

		public NormalItemRewardTemplate(int dropWeight) {
			this.itemCode = null;
			this.rewardCount = 0;
			this.dropWeight = dropWeight;
			this.isCertainDrop = false;
			this.isNoneDrop = true;
		}

		public NormalItemRewardTemplate(String itemCode, int rewardCount,
				int dropWeight, int dropProtectCount) {
			this.itemCode = itemCode;
			this.rewardCount = rewardCount;
			this.dropWeight = dropWeight;
			this.dropProtectCount = dropProtectCount;
			this.isCertainDrop = false;
			this.isNoneDrop = false;
		}

		/**
		 * 掉落道具的模版编号
		 * 
		 * @return
		 */
		public String getItemCode() {
			return itemCode;
		}

		/**
		 * 掉落数量
		 * 
		 * @return
		 */
		public int getRewardCount() {
			return rewardCount;
		}

		/**
		 * 掉落系数(万分比，如80%用8000表示)
		 * 
		 * @return
		 */
		public int getDropWeight() {
			return dropWeight;
		}

	}

	/**
	 * 表示游戏常规道具掉落奖励的数据模版
	 * 
	 * @author zhaizl
	 * 
	 */
	public static class NormalCurrencyRewardTemplate {
		// 掉落道具的模版编号
		private KCurrencyTypeEnum curType;
		// 掉落数量
		private int rewardCount;
		// 掉落系数
		private int dropWeight;
		// 表示什么都不掉落
		public boolean isNoneDrop;
		// 掉落保护次数（卡牌抽奖时生效）
		public int dropProtectCount;

		/**
		 * 是否肯定掉落（不需要计算权重）
		 */
		public boolean isCertainDrop;

		public NormalCurrencyRewardTemplate(KCurrencyTypeEnum curType,
				int rewardCount, int dropWeight, int dropProtectCount) {
			this.curType = curType;
			this.rewardCount = rewardCount;
			this.dropWeight = dropWeight;
			this.dropProtectCount = dropProtectCount;
			this.isCertainDrop = false;
			this.isNoneDrop = false;
		}

		/**
		 * 掉落道具的模版编号
		 * 
		 * @return
		 */
		public KCurrencyTypeEnum getCurrencyType() {
			return curType;
		}

		/**
		 * 掉落数量
		 * 
		 * @return
		 */
		public int getRewardCount() {
			return rewardCount;
		}

		/**
		 * 掉落系数(万分比，如80%用8000表示)
		 * 
		 * @return
		 */
		public int getDropWeight() {
			return dropWeight;
		}

	}

	// /**
	// * 表示卡牌抽奖功能的一个抽奖组数据
	// *
	// * @author zhaizl
	// *
	// */
	// public static class LotteryGroup {
	// // 抽奖组编号
	// public int lotteryGroupUsePoint;
	//
	// public int totalDropWeight = 0;
	// // 需要计算掉落概率的道具列表
	// private List<NormalItemRewardTemplate> caculateItemRewardMap = new
	// ArrayList<NormalItemRewardTemplate>();
	//
	// public int getLotteryGroupUsePoint() {
	// return lotteryGroupUsePoint;
	// }
	//
	// public void setLotteryGroupUsePoint(int lotteryGroupUsePoint) {
	// this.lotteryGroupUsePoint = lotteryGroupUsePoint;
	// }
	//
	// public List<ItemCountStruct> initNormalItemReward(String tableName,
	// String dropData, int index) throws KGameServerException {
	// List<ItemCountStruct> list = new ArrayList<ItemCountStruct>();
	// if (dropData != null) {
	// String[] itemInfoStr = dropData.split(",");
	// if (itemInfoStr != null && itemInfoStr.length > 0) {
	// for (int i = 0; i < itemInfoStr.length; i++) {
	// String[] itemData = itemInfoStr[i].split("\\*");
	// if (itemData != null && itemData.length == 3) {
	// NormalItemRewardTemplate itemTemplate = null;
	// String itemCode = itemData[0];
	// int count = Integer.parseInt(itemData[1]);
	// int dropWeight = Integer.parseInt(itemData[2]);
	// itemTemplate = new NormalItemRewardTemplate(
	// itemCode, count, dropWeight);
	// if (KSupportFactory.getItemModuleSupport()
	// .getItemTemplate(itemCode) == null) {
	// throw new KGameServerException("初始化表<"
	// + tableName + ">的道具错误，找不到道具类型："
	// + itemCode + "，excel行数：" + index);
	// }
	// this.caculateItemRewardMap.add(itemTemplate);
	// this.totalDropWeight += dropWeight;
	//
	// list.add(new ItemCountStruct(KSupportFactory
	// .getItemModuleSupport().getItemTemplate(
	// itemCode), count));
	// } else {
	// throw new KGameServerException("初始化表<" + tableName
	// + ">的道具格式错误：：" + dropData + "，excel行数："
	// + index);
	// }
	// }
	// }
	// }
	// return list;
	// }
	//
	// public ItemCountStruct caculateItemReward() {
	// KItemTempAbs itemTemplate = null;
	// ItemCountStruct struct = null;
	// int weight = UtilTool.random(0, totalDropWeight);
	// int tempRate = 0;
	// for (NormalItemRewardTemplate template : caculateItemRewardMap) {
	// if (tempRate < weight
	// && weight <= (tempRate + template.dropWeight)) {
	// itemTemplate = KSupportFactory.getItemModuleSupport()
	// .getItemTemplate(template.getItemCode());
	// struct = new ItemCountStruct(itemTemplate,
	// template.rewardCount);
	// break;
	// } else {
	// tempRate += template.dropWeight;
	// }
	// }
	//
	// if (struct == null) {
	// NormalItemRewardTemplate template = caculateItemRewardMap
	// .get(0);
	// itemTemplate = KSupportFactory.getItemModuleSupport()
	// .getItemTemplate(template.getItemCode());
	// struct = new ItemCountStruct(itemTemplate, template.rewardCount);
	// }
	//
	// return struct;
	// }
	// }

	/**
	 * 表示卡牌抽奖功能的一个抽奖组数据
	 * 
	 * @author zhaizl
	 * 
	 */
	public static class LotteryGroup {

		public List<Integer> lotteryGroupUsePointList = new ArrayList<Integer>();

		// public int totalDropWeight = 0;
		// 需要计算掉落概率的道具列表
		private List<List<NormalItemRewardTemplate>> caculateItemRewardMap = new ArrayList<List<NormalItemRewardTemplate>>();
		private List<Integer> totalDropWeightList = new ArrayList<Integer>();
		// private List<ItemCountStruct> lotteryRewardShowItemList = new
		// ArrayList<ItemCountStruct>();
		// 每张卡的权重表
		private List<Integer> cardWeightList = new ArrayList<Integer>();
		// 每张卡的权重保护次数表
		private List<Integer> cardProtectCountList = new ArrayList<Integer>();

		public List<Integer> getLotteryGroupUsePointList() {
			return lotteryGroupUsePointList;
		}

		public void setLotteryGroupUsePointList(List<Integer> list) {
			lotteryGroupUsePointList = list;
		}

		public void initNormalItemReward(String tableName, String[] dropData,
				int index) throws KGameServerException {
			if (dropData != null) {
				for (int k = 0; k < dropData.length; k++) {
					this.caculateItemRewardMap
							.add(new ArrayList<NormalItemRewardTemplate>());
					String[] itemInfoStr = dropData[k].split(",");
					int totalWeight = 0;
					if (itemInfoStr != null) {
						for (int i = 0; i < itemInfoStr.length; i++) {
							String[] itemData = itemInfoStr[i].split("\\*");
							if (itemData != null && itemData.length == 3) {
								NormalItemRewardTemplate itemTemplate = null;
								String itemCode = itemData[0];
								int count = Integer.parseInt(itemData[1]);
								int dropWeight = Integer.parseInt(itemData[2]);
								itemTemplate = new NormalItemRewardTemplate(
										itemCode, count, dropWeight);
								if (KSupportFactory.getItemModuleSupport()
										.getItemTemplate(itemCode) == null) {
									throw new KGameServerException("初始化表<"
											+ tableName + ">的道具错误，找不到道具类型："
											+ itemCode + "，excel行数：" + index);
								}
								this.caculateItemRewardMap.get(k).add(
										itemTemplate);
								totalWeight += dropWeight;

								// lotteryRewardShowItemList
								// .add(new ItemCountStruct(
								// KSupportFactory
								// .getItemModuleSupport()
								// .getItemTemplate(
								// itemCode),
								// count));
							} else {
								throw new KGameServerException("初始化表<"
										+ tableName + ">的道具格式错误：：" + dropData
										+ "，excel行数：" + index);
							}
						}

					} else {
						throw new KGameServerException("初始化表<" + tableName
								+ ">的道具格式错误：：" + dropData + "，excel行数：" + index);
					}
					totalDropWeightList.add(totalWeight);
				}
			}
		}

		public void checkLotteryGroup(String tableName, int index)
				throws KGameServerException {
			if (lotteryGroupUsePointList.size() != 5) {
				throw new KGameServerException("初始化表<" + tableName
						+ ">的use_point错误，数量必须为5，excel行数：" + index);
			}
			if (caculateItemRewardMap.size() != 5) {
				throw new KGameServerException("初始化表<" + tableName
						+ ">的卡牌数据错误，数量必须为5，excel行数：" + index);
			}
			List<NormalItemRewardTemplate> checkList;
			for(int i = 0;i < caculateItemRewardMap.size(); i++) {
				checkList = caculateItemRewardMap.get(i);
				if(checkList.isEmpty()){
					throw new KGameServerException("初始化表<" + tableName
							+ ">的卡牌数据错误，第"+i+"个卡牌道具数量为0，excel行数：" + index);
				}
			}
			if (this.cardWeightList.size() != 5) {
				throw new KGameServerException("初始化表<" + tableName
						+ ">的cardx_weightAndProtect错误，数量必须为5，excel行数：" + index);
			}
			if (this.cardProtectCountList.size() != 5) {
				throw new KGameServerException("初始化表<" + tableName
						+ ">的cardx_weightAndProtect错误，数量必须为5，excel行数：" + index);
			}
			if (this.totalDropWeightList.size() != 5) {
				throw new KGameServerException("初始化表<" + tableName
						+ ">的卡牌数据错误，数量必须为5，excel行数：" + index);
			}
		}

		public void setCardWeightList(List<Integer> cardWeightList) {
			this.cardWeightList = cardWeightList;
		}

		public void setCardProtectCountList(List<Integer> cardProtectCountList) {
			this.cardProtectCountList = cardProtectCountList;
		}

		public List<ItemCountStruct> getLotteryRewardShowItemList(
				List<NormalItemRewardTemplate> templateList) {

			List<ItemCountStruct> resultList = new ArrayList<ItemCountStruct>();
			for (int i = 0; i < templateList.size(); i++) {
				NormalItemRewardTemplate template = templateList.get(i);
				resultList.add(new ItemCountStruct(template.itemCode,
						template.rewardCount));
			}

			return resultList;
		}

		public List<NormalItemRewardTemplate> getCaculateItemRewardList() {
			List<NormalItemRewardTemplate> resultList = new ArrayList<KLevelReward.NormalItemRewardTemplate>();

			L1: for (int i = 0; i < caculateItemRewardMap.size(); i++) {
				List<NormalItemRewardTemplate> cardList = caculateItemRewardMap
						.get(i);
				int totalWeight = totalDropWeightList.get(i);
				int cardDropWeight = cardWeightList.get(i);
				int protectCount = cardProtectCountList.get(i);
				if (totalWeight > 0) {
					int weight = UtilTool.random(0, totalWeight);
					int tempRate = 0;
					NormalItemRewardTemplate resultRemplate = null;
					L2: for (NormalItemRewardTemplate template : cardList) {
						if (tempRate < weight
								&& weight <= (tempRate + template.dropWeight)) {
							if (!template.isNoneDrop) {
								resultList.add(new NormalItemRewardTemplate(
										template.itemCode,
										template.rewardCount, cardDropWeight,
										protectCount));
								resultRemplate = template;
							}
							break L2;
						} else {
							tempRate += template.dropWeight;
						}
					}
					if(resultRemplate == null){
						resultRemplate = cardList.get(0);
						resultList.add(new NormalItemRewardTemplate(
								resultRemplate.itemCode,
								resultRemplate.rewardCount, cardDropWeight,
								protectCount));
					}
				}
			}

			return resultList;
		}

	}

	/**
	 * 表示卡牌抽奖功能的一个抽奖组数据
	 * 
	 * @author zhaizl
	 * 
	 */
	public static class LotteryCurrencyGroup {

		public List<Integer> lotteryGroupUsePointList = new ArrayList<Integer>();

		public int totalDropWeight = 0;
		// 需要计算掉落概率的道具列表
		private List<NormalCurrencyRewardTemplate> caculateCurrencyRewardMap = new ArrayList<NormalCurrencyRewardTemplate>();

		private List<KCurrencyCountStruct> lotteryCurrencyRewardShowList = new ArrayList<KCurrencyCountStruct>();

		public List<Integer> getLotteryGroupUsePointList() {
			return lotteryGroupUsePointList;
		}

		public void setLotteryGroupUsePointList(List<Integer> list) {
			lotteryGroupUsePointList = list;
		}

		public void initNormalItemReward(String tableName, String dropData,
				int index) throws KGameServerException {
			if (dropData != null) {
				String[] itemInfoStr = dropData.split(",");
				if (itemInfoStr != null && itemInfoStr.length == 5) {
					for (int i = 0; i < itemInfoStr.length; i++) {
						String[] itemData = itemInfoStr[i].split("\\*");
						if (itemData != null && itemData.length == 4) {
							NormalCurrencyRewardTemplate curTemplate = null;
							byte type = Byte.parseByte(itemData[0]);
							KCurrencyTypeEnum currencyType = KCurrencyTypeEnum
									.getEnum(type);
							int count = Integer.parseInt(itemData[1]);
							int dropWeight = Integer.parseInt(itemData[2]);
							int dropProtectCount = Integer
									.parseInt(itemData[3]);
							if (currencyType == null) {
								throw new KGameServerException("初始化表<"
										+ tableName + ">的货币类型错误，找不到货币类型："
										+ type + "，excel行数：" + index);
							}
							curTemplate = new NormalCurrencyRewardTemplate(
									currencyType, count, dropWeight,
									dropProtectCount);
							this.caculateCurrencyRewardMap.add(curTemplate);
							this.totalDropWeight += dropWeight;

							lotteryCurrencyRewardShowList
									.add(new KCurrencyCountStruct(currencyType,
											count));
						} else {
							throw new KGameServerException("初始化表<" + tableName
									+ ">的道具格式错误：：" + dropData + "，excel行数："
									+ index);
						}
					}
				} else {
					throw new KGameServerException("初始化表<" + tableName
							+ ">的道具格式错误：：" + dropData + "，excel行数：" + index);
				}
			}
		}

		public List<KCurrencyCountStruct> getLotteryRewardShowCurrencyList() {
			return lotteryCurrencyRewardShowList;
		}

		public List<NormalCurrencyRewardTemplate> getCaculateCurrencyRewardList() {
			return caculateCurrencyRewardMap;
		}

	}
}
