package com.kola.kmp.logic.other;

/**
 * 
 * @author PERRY CHAN
 */
public enum KRoleExtTypeEnum {

	TEST(9999), //
	FASHION(1), //时装
	REWARD(2), //奖励
	RANK(3), //排行榜
	VIP(4),//VIP
	GANG(5),//军团
	TALENT(6),
	SHOP(7),//商店
	GAMBLE(8),//赌博系统自定义属性类型
	GARDEN(9),//僵尸庄园
	ACTIVITY(10),//活动数据
	EXCITING(11),//精彩活动
	;

	public final int sign;

	private KRoleExtTypeEnum(int pSign) {
		this.sign = pSign;
	}

	public static KRoleExtTypeEnum getTypeEnum(int pSign) {
		KRoleExtTypeEnum type;
		KRoleExtTypeEnum[] array = values();
		for (int i = 0; i < array.length; i++) {
			type = array[i];
			if (type.sign == pSign) {
				return type;
			}
		}
		return null;
	}
}
