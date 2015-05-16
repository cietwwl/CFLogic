package com.kola.kmp.logic.other;

/**
 * 
 * @author PERRY CHAN
 */
public enum KObstructionTargetType {

	TARGET_ON_NONE(0),
	TARGET_ON_ROLE(1),
	TARGET_ON_MONSTER(2),
	TARGET_ON_ALL(3),
	;
	public final int sign;
	
	private KObstructionTargetType(int pSign) {
		this.sign = pSign;
	}
	
	public static KObstructionTargetType getEnum(int pSign) {
		KObstructionTargetType[] all = values();
		KObstructionTargetType temp;
		for(int i = 0; i < all.length; i++) {
			temp = all[i];
			if(temp.sign == pSign) {
				return temp;
			}
		}
		return null;
	}
}
