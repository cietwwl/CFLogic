package com.kola.kmp.logic.other;

/**
 * 随从副本打开笼子的掉落类型枚举
 * @author Administrator
 *
 */
public enum KPetCopyDropTypeEnum {
	/** 掉落怪物类型 */
	MONSTER(1),
	/** 掉落道具类型 */
	ITEM(2),
	/** 掉落货币类型  */
	CURRENCY(3)
	;
	public final int sign;
	private KPetCopyDropTypeEnum(int pType) {
		this.sign = pType;
	}
	
	public static final KPetCopyDropTypeEnum getTypeEnum(int pType) {
		for(int i = 0; i < values().length; i++) {
			if(values()[i].sign == pType) {
				return values()[i];
			}
		}
		return null;
	}
}
