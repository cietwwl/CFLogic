package com.kola.kmp.logic.activity.worldboss;

import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;

/**
 * 
 * <pre>
 * 世界boss的鼓舞加成数据
 * </pre>
 * 
 * @author PERRY CHAN
 */
public class KInspireTemplateData {

	public final int inspireLv;
	public final KCurrencyCountStruct consume;
	public final int atkIncPct;
//	public final int defIncPct;
//	public final int rewardIncPct;
//	private final List<KFixedIncAttr> fixedIncList;
	
	public KInspireTemplateData(KGameExcelRow basicRow/*, List<KGameExcelRow> fixedIncRows*/) {
		this.inspireLv = basicRow.getInt("inspireLv");
		this.consume = new KCurrencyCountStruct(KCurrencyTypeEnum.getEnum(basicRow.getInt("currencyType")), basicRow.getInt("currencyCount"));
		this.atkIncPct = UtilTool.round(basicRow.getFloat("atkIncPct") * UtilTool.TEN_THOUSAND_RATIO_UNIT);
//		this.defIncPct = UtilTool.round(basicRow.getFloat("defIncPct") * UtilTool.TEN_THOUSAND_RATIO_UNIT);
//		this.rewardIncPct = UtilTool.round(basicRow.getFloat("rewardIncPct") *UtilTool.TEN_THOUSAND_RATIO_UNIT);
//		List<KFixedIncAttr> tempList = new ArrayList<KFixedIncAttr>();
//		fixedIncList = Collections.unmodifiableList(tempList);
//		for (int i = 0; i < fixedIncRows.size(); i++) {
//			tempList.add(new KFixedIncAttr(fixedIncRows.get(i)));
//		}
	}
	
//	/**
//	 * 获取固定的加成
//	 * @param lv
//	 * @return{0}表示攻击力的固定加成，{1}表示防御力的固定加成
//	 */
//	final int[] getFixedInc(int lv) {
//		KFixedIncAttr fixedAttr;
//		int[] result = new int[2];
//		for (int i = 0; i < fixedIncList.size(); i++) {
//			fixedAttr = fixedIncList.get(i);
//			if (fixedAttr.isLvMatch(lv)) {
//				result[0] = fixedAttr.atkInc;
//				result[1] = fixedAttr.defInc;
//			}
//		}
//		return result;
//	}
	
	
	
//	/**
//	 * <pre>
//	 * 世界boss鼓舞的固定加成
//	 * </pre>
//	 * 
//	 * @author PERRY CHAN
//	 */
//	static class KFixedIncAttr {
//		public final int minLv;
//		public final int maxLv;
//		public final int atkInc;
////		public final int defInc;
//
//		KFixedIncAttr(KGameExcelRow row) {
//			this.minLv = row.getInt("minLv");
//			this.maxLv = row.getInt("maxLv");
//			this.atkInc = row.getInt("atkInc");
////			this.defInc = row.getInt("defInc");
//		}
//		
//		public final boolean isLvMatch(int lv) {
//			return lv >= minLv && lv <= maxLv;
//		}
//	}
}
