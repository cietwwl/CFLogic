package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * 全局扩展数据类型
 * 
 * @author CamusHuang
 * @creation 2014-2-20 下午4:57:51
 * </pre>
 */
public enum KGameExtDataDBTypeEnum{
	军团资源竞价榜(1),//
	物资运送数据(2),
	战队竞技(3),
	物品模块数据(4),
	限时热购数据(5),
	精彩活动数据(6),
	全民竞猜数据(7),
	合服信息数据(99),//
	;

	// 标识数值
	public final int dbType;
	
	private KGameExtDataDBTypeEnum(int dbType) {
		this.dbType = dbType;
	}
	
	public int getDbType() {
		return dbType;
	}

	// //////////////////

	// 所有枚举
	private static final Map<Integer, KGameExtDataDBTypeEnum> typeMap = new HashMap<Integer, KGameExtDataDBTypeEnum>();
	static {
		KGameExtDataDBTypeEnum[] enums = KGameExtDataDBTypeEnum.values();
		KGameExtDataDBTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			typeMap.put(type.dbType, type);
		}
	}

	/**
	 * <pre>
	 * 通过标识数值获取枚举对象
	 * 
	 * @param sign
	 * @return
	 * @author CamusHuang
	 * @creation 2012-11-5 上午10:53:13
	 * </pre>
	 */
	public static KGameExtDataDBTypeEnum getEnum(int dbType) {
		return typeMap.get(dbType);
	}

	public static int size() {
		return typeMap.size();
	}
}