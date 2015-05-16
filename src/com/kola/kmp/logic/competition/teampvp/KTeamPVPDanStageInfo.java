package com.kola.kmp.logic.competition.teampvp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.util.KGameUtilTool;

/**
 * 
 * 段位阶级信息，包含升级所需的胜点、升级奖励、每日经验奖励系数等数据
 * 
 * @author PERRY CHAN
 */
public class KTeamPVPDanStageInfo {

	/**
	 * 段位等级id
	 */
	public final int danStageId;
	/**
	 * 段位等级名字
	 */
	public final String danStageName;
	/**
	 * 段位id
	 */
	public final int danGradeId;
	/**
	 * 升级所需胜点
	 */
	public final int promoteScore;
	/**
	 * 升级所需战斗数量
	 */
	public final int promoteFightCount;
	/**
	 * 升级所需胜利的战斗数量
	 */
	public final int promoWinCount;
	/**
	 * 升级失败后的胜点
	 */
	public final int promoFailScore;
	/**
	 * 降级的胜点
	 */
	public final int demoteScore;
//	/**
//	 * 升级所能获得的奖励
//	 */
//	public final Map<String, Integer> promoRewardMap;
	/**
	 * 
	 */
	public final int promoDiamondReward;
	/**
	 * 
	 */
	public final KCurrencyCountStruct promoDiamondRewardStruct;
	/**
	 * 每日奖励经验的段位系数
	 */
	public final float dayExpWeight;
	/**
	 * 是否最高级别
	 */
	public final KDanStageType danStageType;
	/**
	 * 
	 */
	public final List<ItemCountStruct> promoteReward;
	/**
	 * 能否降级
	 */
	public final boolean canDemote;
	/**
	 * 
	 */
	public final int iconId;
	/**
	 * 
	 */
	public final int level;
	/**
	 * 
	 */
	public final Map<KGameAttrType, Integer> attribute;
	
	public KTeamPVPDanStageInfo(KGameExcelRow row, KGameExcelRow attrRow, KDanStageType pDanStageType)throws Exception {
		this.danStageId = row.getInt("danRankId");
		this.danStageName = row.getData("danRankName");
		this.danGradeId = row.getInt("danGradeId");
		this.promoteScore = row.getInt("promoScore");
		this.promoteFightCount = row.getIntSafely("promoFightCount");
		this.promoWinCount = row.getIntSafely("promoWinCount");
		this.promoFailScore = row.getIntSafely("promoFailScore");
		this.demoteScore = row.getIntSafely("demoteScore");
		this.iconId = row.getIntSafely("iconId");
		this.level = row.getInt("level");
		this.danStageType = pDanStageType;
//		Map<String, Integer> tempRewardMap = new HashMap<String, Integer>();
		List<ItemCountStruct> tempList = new ArrayList<ItemCountStruct>();
//		String[] rewards = row.getData("promoget").split(",");
//		String[] singleReward;
//		if (rewards.length > 0 && rewards[0].length() > 0) {
//			for (int i = 0; i < rewards.length; i++) {
//				singleReward = rewards[i].split("\\*");
//				tempRewardMap.put(singleReward[0], Integer.parseInt(singleReward[1]));
//				tempList.add(new ItemCountStruct(singleReward[0], Integer.parseInt(singleReward[1])));
//			}
//		}
//		this.promoRewardMap = Collections.unmodifiableMap(tempRewardMap);
		this.promoDiamondReward = row.getInt("promoDiamondReward");
		this.promoDiamondRewardStruct = new KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND, this.promoDiamondReward);
		this.promoteReward = Collections.unmodifiableList(tempList);
		this.dayExpWeight = row.getFloat("dayexpweight");
		this.canDemote = this.demoteScore > 0;
		int[] attrKeys = UtilTool.getStringToIntArray(attrRow.getData("attributeKeys"), ",");
		int[] attrValues = UtilTool.getStringToIntArray(attrRow.getData("attributeValues"), ",");
		attribute = Collections.unmodifiableMap(KGameUtilTool.genAttribute(attrKeys, attrValues, true));
	}
	
	public static enum KDanStageType {
		
		/**
		 * 段位阶级类型：普通
		 */
		COMMON(true),
		/**
		 * 段位阶级类型：普通最高级
		 */
		HIGHEST(true),
		/**
		 * 段位阶级类型：最强王者
		 */
		KING(false)
		;
		public final boolean canPromote;
		
		private KDanStageType(boolean pCanPromote) {
			this.canPromote = pCanPromote;
		}
	}
}
