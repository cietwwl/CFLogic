package com.kola.kmp.logic.other;

/**
 * 
 * 宠物的类型
 * 
 * @author PERRY CHAN
 */
public enum KPetType {

	COMMON(1, "普通"),
	ELITIST(2, "精英"),
	BOSS(3, "BOSS"),
	;
	
	public final int sign;
	private String _typeName;
	
	private KPetType(int pSign, String pTypeName) {
		this.sign = pSign;
		this._typeName = pTypeName;
	}
	
	public String getTypeName() {
		return _typeName;
	}
	
	public static KPetType getPetType(int pType) {
		KPetType[] array = values();
		KPetType temp;
		for(int i = 0; i < array.length; i++) {
			temp = array[i];
			if(temp.sign == pType) {
				return temp;
			}
		}
		return null;
	}
}
