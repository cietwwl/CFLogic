package com.kola.kmp.logic.other;

/**
 * 
 * @author PERRY CHAN
 */
public enum KGameGender {

	MALE(1),
	FEMALE(2),
	;
	public final int sign;
	private KGameGender(int pSign) {
		sign = pSign;
	}
	
	public static KGameGender getGender(int pSign) {
		return pSign == MALE.sign ? MALE : FEMALE;
	}
}
