package com.kola.kmp.logic.competition.teampvp;

import com.koala.game.util.KGameExcelTable.KGameExcelRow;

/**
 * 
 * @author PERRY CHAN
 */
public class KTeamPVPDanData {

	public final int danGradeId;
	public final String danGradeName;
	public final int iconId;
	public final int danLv;
//	public final Map<KGameAttrType, Integer> attribute;
	
	public KTeamPVPDanData(KGameExcelRow basicRow) throws Exception {
		this.danGradeId = basicRow.getInt("danGradeId");
		this.danGradeName = basicRow.getData("danGradeName");
		this.danLv = basicRow.getInt("danLv");
		this.iconId = basicRow.getIntSafely("iconId");
//		int[] attrKeys = UtilTool.getStringToIntArray(attrRow.getData("attributeKeys"), ",");
//		int[] attrValues = UtilTool.getStringToIntArray(attrRow.getData("attributeValues"), ",");
//		attribute = Collections.unmodifiableMap(KGameUtilTool.genAttribute(attrKeys, attrValues, true));
	}
}
