package com.kola.kmp.logic.other;

/**
 * 
 * @author PERRY CHAN
 */
public enum KMonsterType {

	/** 怪物类型：<b><font color="ff0000">普通 </font></b> */
	COMMON(1),
	/** 怪物类型：<b><font color="ff0000">精英</font></b> */
	ELITIST(2),
	/** 怪物类型：<b><font color="ff0000">boss</font></b> */
	BOSS(3)
	;
	public final int sign;
	private KMonsterType(int pType) {
		this.sign = pType;
	}
	
	public static final KMonsterType getTypeEnum(int pType) {
		for(int i = 0; i < values().length; i++) {
			if(values()[i].sign == pType) {
				return values()[i];
			}
		}
		return null;
	}
}
