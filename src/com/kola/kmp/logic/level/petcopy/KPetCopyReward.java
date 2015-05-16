package com.kola.kmp.logic.level.petcopy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.level.KLevelReward.LotteryGroup;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.support.KSupportFactory;

public class KPetCopyReward {

	public int levelId;

	public int roleLv;

	public int baseGold;// 基础金币
	public int basePotential;// 基础潜能
	public int baseExp;// 基础经验
	public int baseScore;// 基础荣誉

	public int sGold;// S级金币
	public int sPotential;// S级潜能
	public List<ItemCountStruct> sItemList = new ArrayList<ItemCountStruct>();// S级道具

	// public BaseRewardData sRewardData;

	public List<KPetCopyItemRewardTemplate> dropItemList = new ArrayList<KPetCopyItemRewardTemplate>();// 普通掉落道具
	// 抽奖数据信息
	public LotteryGroup lotteryGroup;
	// 是否有抽奖类型奖励
	private boolean isHasLotteryReward;

	public void init(String tableName, KGameExcelRow xlsRow)
			throws KGameServerException {
		this.levelId = xlsRow.getInt("CopyID");
		this.roleLv = xlsRow.getInt("lv");
		this.baseGold = xlsRow.getInt("copy_gold");
		this.baseExp = xlsRow.getInt("copy_exp");
		this.basePotential = xlsRow.getInt("copy_battlepoint");
		this.baseScore = xlsRow.getInt("copy_prestige");

		this.sGold = xlsRow.getInt("s_gold");
		this.sPotential = xlsRow.getInt("s_battlepoint");
		this.isHasLotteryReward = true;

		String sItemStr = xlsRow.getData("s_item");
		decodeSItem(tableName, "s_item", sItemStr, xlsRow.getIndexInFile());

		String normalItemKey = "drop_ID";
		for (int i = 1; i <= 3; i++) {
			String normalItemStr = xlsRow.getData(normalItemKey + i);
			decodeNormalItem(tableName, normalItemKey + i, normalItemStr,
					xlsRow.getIndexInFile());
		}

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

//		String lotteryInfo = xlsRow.getData("rewards");
//		if (lotteryInfo == null) {
//			throw new KGameServerException("初始化表<" + tableName
//					+ ">的道具格式错误：rewards=" + lotteryInfo + ",excel行数："
//					+ xlsRow.getIndexInFile());
//		}
		String[] lotteryInfo = new String[5];// xlsRow.getData("soldier_flop");
		for (int i = 0; i < lotteryInfo.length; i++) {
			lotteryInfo[i] = xlsRow.getData("card" + (i + 1));
			if (lotteryInfo[i] == null) {
				throw new KGameServerException("初始化表<" + tableName
						+ ">的道具格式错误：card" + (i + 1) + "="
						+ lotteryInfo[i] + ",excel行数："
						+ xlsRow.getIndexInFile());
			}
		}
		this.lotteryGroup = new LotteryGroup();
		this.lotteryGroup.initNormalItemReward(tableName, lotteryInfo,
				xlsRow.getIndexInFile());
		this.lotteryGroup.setLotteryGroupUsePointList(usePointList);
		this.lotteryGroup.setCardWeightList(cardWeightList);		
		this.lotteryGroup.setCardProtectCountList(cardProtectCountList);
		this.lotteryGroup.checkLotteryGroup(tableName, xlsRow.getIndexInFile());
	}

	private void decodeSItem(String tableName, String fieldName, String data,
			int index) throws KGameServerException {
		if (data != null && data.length() > 0 && !data.equals("0")) {
			String[] itemCodes = data.split(",");
			if (itemCodes != null && itemCodes.length > 0) {
				for (int i = 0; i < itemCodes.length; i++) {
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
					sItemList.add(new ItemCountStruct(codes[0], Integer
							.parseInt(codes[1])));
				}
			}
		}
	}

	public void decodeNormalItem(String tableName, String fieldName,
			String data, int index) throws KGameServerException {
		if (data != null && data.length() > 0 && !data.equals("0")) {
			String[] itemCodes = data.split(",");
			if (itemCodes != null && itemCodes.length > 0) {
				for (int i = 0; i < itemCodes.length; i++) {
					String[] codes = itemCodes[i].split("\\*");
					if (codes == null || codes.length != 3) {
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
					int count = Integer.parseInt(codes[1]);
					int rate = Integer.parseInt(codes[2]);
					dropItemList.add(new KPetCopyItemRewardTemplate(codes[0],
							count, rate));
				}
			}
		}
	}

	public List<ItemCountStruct> caculateNormalItemReward(int multiple) {

		List<ItemCountStruct> itemList = new ArrayList<ItemCountStruct>();
		for (KPetCopyItemRewardTemplate template : dropItemList) {
			int rate = UtilTool.random(UtilTool.TEN_THOUSAND_RATIO_UNIT);
			if (rate >= UtilTool.TEN_THOUSAND_RATIO_UNIT - template.dropRate) {
				itemList.add(new ItemCountStruct(template.itemCode,
						template.itemCount * multiple));
			}
		}

		return itemList;
	}

	public List<ItemCountStruct> caculateSReward(int multiple) {

		List<ItemCountStruct> itemList = new ArrayList<ItemCountStruct>();
		for (ItemCountStruct struct : sItemList) {
			itemList.add(new ItemCountStruct(struct.itemCode, struct.itemCount
					* multiple));
		}
		return itemList;
	}

	public boolean isHasLotteryReward() {
		return isHasLotteryReward;
	}

	public static class KPetCopyItemRewardTemplate {
		public String itemCode;
		public int itemCount;
		public int dropRate;

		private KPetCopyItemRewardTemplate(String itemCode, int itemCount,
				int dropRate) {
			super();
			this.itemCode = itemCode;
			this.itemCount = itemCount;
			this.dropRate = dropRate;
		}

	}
}
