package com.kola.kmp.logic.activity.newglodact;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.level.KLevelReward.LotteryCurrencyGroup;

public class NewGoldActivityReward {
	//角色等级
		public int roleLv;
		//基础金币数量
		public long goldValue;
		// 是否有抽奖类型奖励
		public boolean isHasLotteryReward = false;
		public LotteryCurrencyGroup lotteryGroup;

		public void initLotteryReward(String tableName, KGameExcelRow xlsRow)
				throws KGameServerException {
			String usePointInfo = xlsRow.getData("use_rmb");
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

			String itemInfo = xlsRow.getData("rewards");
			if (itemInfo == null) {
				throw new KGameServerException("初始化表<" + tableName
						+ ">的道具格式错误：rewards=" + itemInfo + "，" + "excel行数："
						+ xlsRow.getIndexInFile());
			}
			this.isHasLotteryReward = true;
			lotteryGroup = new LotteryCurrencyGroup();
			lotteryGroup.setLotteryGroupUsePointList(usePointList);
			lotteryGroup.initNormalItemReward(tableName, itemInfo,
					xlsRow.getIndexInFile());

		}
}
