package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

import com.kola.kmp.logic.rank.abs.RankTypeInterface;

/**
 * <pre>
 * 角色排行榜类型
 * 
 * @author CamusHuang
 * @creation 2014-2-20 下午4:57:51
 * </pre>
 */
public enum KRankTypeEnum implements RankTypeInterface {
	竞技榜(1, "竞技榜", "", "", ""), //
	战力榜(2, "战力榜", "./res/output/rank/", "ares", ".xml"), //
	等级榜(3, "等级榜", "./res/output/rank/", "level", ".xml"), //
	随从榜(4, "随从榜", "./res/output/rank/", "pet", ".xml");

	// 标识数值
	public final int sign;
	// 名称
	public final String name;
	// 入榜等级
	private int joinMinLv = 10;
	// 最大容量
	private int maxLen = 100;
	// 保存路径
	public final String saveDirPath;
	// 保存文件名称
	public final String saveFileName;
	// 保存文件后缀
	public final String saveFileNameSuffix;

	private KRankTypeEnum(int sign, String name, String saveDirPath, String saveFileName, String saveFileNameSuffix) {
		this.sign = sign;
		this.name = name;
		this.saveDirPath = saveDirPath;
		this.saveFileName = saveFileName;
		this.saveFileNameSuffix = saveFileNameSuffix;
	}

	public void reset(int joinMinLv, int maxLen) {
		this.joinMinLv = joinMinLv;
		this.maxLen = maxLen;
	}

	@Override
	public int getSign() {
		return sign;
	}

	@Override
	public String getSaveDirPath() {
		return saveDirPath;
	}

	@Override
	public String getSaveFileName() {
		return saveFileName;
	}

	@Override
	public String getSaveFileNameSuffix() {
		return saveFileNameSuffix;
	}

	public int getMaxLen() {
		return maxLen;
	}

	public int getJoinMinLv() {
		return joinMinLv;
	}

	// //////////////////

	private static int MinTypeSign = Integer.MAX_VALUE;
	private static int MaxTypeSign = Integer.MIN_VALUE;
	// 所有枚举
	private static final Map<Integer, KRankTypeEnum> typeMap = new HashMap<Integer, KRankTypeEnum>();
	static {
		KRankTypeEnum[] enums = KRankTypeEnum.values();
		KRankTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			typeMap.put(type.sign, type);
			if (type.sign < MinTypeSign) {
				MinTypeSign = type.sign;
			}
			if (type.sign < MaxTypeSign) {
				MaxTypeSign = type.sign;
			}
		}
	}
	
	public static int getMinTypeSign() {
		return MinTypeSign;
	}

	public static int getMaxTypeSign() {
		return MaxTypeSign;
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
	public static KRankTypeEnum getEnum(int sign) {
		return typeMap.get(sign);
	}

	public static int size() {
		return typeMap.size();
	}
}