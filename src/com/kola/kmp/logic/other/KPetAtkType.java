package com.kola.kmp.logic.other;

/**
 * 
 * @author PERRY CHAN
 */
public enum KPetAtkType {

	
//	GRADE_ONE(1, 1, "一阶"),
//	GRADE_TWO(2, 2, "二阶"),
//	GRADE_THREE(3, 3, "三阶"),
//	GRADE_FOUR(4, 4, "四阶"),
//	GRADE_FIVE(5, 5, "五阶"),
//	GRADE_SIX(6, 6, "六阶"),
	ATK_TYPE_SHORT(1, "近战"),
	ATK_TYPE_LONG(2, "远战"),
	ATK_TYPE_DEF(3, "防御")
	;
	public final int sign; // 标识
//	public final int grade; // 阶级值
	private String _name; // 名字
	
	private KPetAtkType(int pSign, /*int pGrade,*/ String pName) {
		this.sign = pSign;
//		this.grade = pGrade;
		this._name = pName;
	}
	
	public String getName() {
		return this._name;
	}
	
	public static KPetAtkType getPetAtkType(int sign) {
		KPetAtkType petGrade;
		KPetAtkType[] array = KPetAtkType.values();
		for(int i = 0; i < array.length; i++) {
			petGrade = array[i];
			if(petGrade.sign == sign) {
				return petGrade;
			}
		}
		return null;
	}
}
