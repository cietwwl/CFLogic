package com.kola.kmp.logic.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.role.KRole;

/**
 * 
 * @author PERRY CHAN
 */
public class KBattlePowerCalculator {

	private static final Map<KGameAttrType, KBattlePowerPara> _roleBattlePowerCalParaMap = new HashMap<KGameAttrType, KBattlePowerCalculator.KBattlePowerPara>();
	private static final Map<KGameAttrType, KBattlePowerPara> _petBattlePowerCalParaMap = new HashMap<KGameAttrType, KBattlePowerCalculator.KBattlePowerPara>();
	
	private static void initCalculatePara(KGameExcelRow[] allRows, Map<KGameAttrType, KBattlePowerPara> map) {
		KBattlePowerPara para;
		KGameExcelRow row;
		for (int i = 0; i < allRows.length; i++) {
			para = new KBattlePowerPara();
			row = allRows[i];
			para.attrType = KGameAttrType.getAttrTypeEnum(row.getInt("attrType"));
			para.hasTowParas = row.getBoolean("isTwoParameterses");
			para.isLvRelative = row.getBoolean("isLvRelative");
			para.paraValue = row.getFloat("paraValue");
			para.secondaryParaValue = row.getFloat("SecondaryParaValue");
			map.put(para.attrType, para);
		}
	}
	
	public static final void initRoleCalculatePara(KGameExcelRow[] allRows) {
		initCalculatePara(allRows, _roleBattlePowerCalParaMap);
	}
	
	public static final void initPetCalculatePara(KGameExcelRow[] allRows) {
		initCalculatePara(allRows, _petBattlePowerCalParaMap);
	}
	
	public static final int calculateBattlePowerOfRole(Map<KGameAttrType, Integer> attrMap, KRole role) {
		KGameAttrType attrType;
		Map.Entry<KGameAttrType, Integer> entry;
		if (role != null) {
			Map<KGameAttrType, Integer> copy = new HashMap<KGameAttrType, Integer>(attrMap);
			Map<KGameAttrType, Integer> nMap = new HashMap<KGameAttrType, Integer>();
			Integer current;
			int value;
			for (Iterator<Map.Entry<KGameAttrType, Integer>> itr = copy.entrySet().iterator(); itr.hasNext();) {
				entry = itr.next();
				attrType = entry.getKey();
				if (attrType.isPercentageType) {
					itr.remove();
					value = UtilTool.calculateTenThousandRatio(role.getBasicAttrByType(attrType), entry.getValue());
					current = nMap.get(entry.getKey());
					if (current != null) {
						value += current;
					}
					nMap.put(attrType, value);
				} else if (attrType == KGameAttrType.BASIC_ATTR_INC) {
					for (int i = 0; i < KGameAttrType.BASIC_ATTRIBUTE_ARRAY.length; i++) {
						KGameAttrType tempType = KGameAttrType.BASIC_ATTRIBUTE_ARRAY[i];
						value = UtilTool.calculateTenThousandRatio(role.getBasicAttrByType(tempType), UtilTool.TEN_THOUSAND_RATIO_UNIT + entry.getValue());
						current = nMap.get(entry.getKey());
						if (current != null) {
							value += current;
						}
						nMap.put(tempType, value);
					}
					itr.remove();
				}
			}
			if (nMap.size() > 0) {
				KGameUtilTool.combinMap(copy, nMap);
			}
			return calculateBattlePower(copy, role.getLevel(), false);
		}
		return 0;
	}
	
	public static final int calculateBattlePower(Map<KGameAttrType, Integer> attrMap, int lv, boolean isPet) {
		Map<KGameAttrType, KBattlePowerPara> paraMap = isPet ? _petBattlePowerCalParaMap : _roleBattlePowerCalParaMap;
		float result = 0;
		Map.Entry<KGameAttrType, Integer> temp;
		KBattlePowerPara para;
		for (Iterator<Map.Entry<KGameAttrType, Integer>> itr = attrMap.entrySet().iterator(); itr.hasNext();) {
			temp = itr.next();
			para = paraMap.get(temp.getKey());
			if (para != null) {
				if (para.hasTowParas) {
					result += temp.getValue() * para.paraValue * Math.pow((lv / 10 + 1), para.secondaryParaValue);
				} else {
					if (para.isLvRelative) {
						result += temp.getValue() * Math.pow(lv, para.paraValue);
					} else {
						result += temp.getValue() * para.paraValue;
					}
				}
			}
		}
		return UtilTool.round(result);
	}
	
	static class KBattlePowerPara {
		KGameAttrType attrType;
		boolean hasTowParas;
		boolean isLvRelative;
		float paraValue;
		float secondaryParaValue;
	}
}
