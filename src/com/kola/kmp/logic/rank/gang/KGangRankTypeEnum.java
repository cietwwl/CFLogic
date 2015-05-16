package com.kola.kmp.logic.rank.gang;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.kola.kmp.logic.rank.abs.RankTypeInterface;

/**
 * <pre>
 * 
 * @author camus
 * @creation 2012-12-31 下午5:08:51
 * </pre>
 */

public enum KGangRankTypeEnum implements RankTypeInterface {
	全部军团(10, "./res/output/gangRank/", "allGang", ".xml"), //
	军团战积分(11, "./res/output/gangRank/", "gangWar", ".xml"), //
	军团战报名(12, "./res/output/gangRank/", "gangWarSignUp", ".xml"), //
	军团战力(13, "./res/output/gangRank/", "gangPow", ".xml"), //军团战力
	;

	// 标识数值
	public final int sign;
	// 最大容量
	private int maxLen = 1000;
	// 保存路径
	public final String saveDirPath;
	// 保存文件名称
	public final String saveFileName;
	// 保存文件后缀
	public final String saveFileNameSuffix;

	private KGangRankTypeEnum(int sign, String saveDirPath, String saveFileName, String saveFileNameSuffix) {
		this.sign = sign;
		this.saveDirPath = saveDirPath;
		this.saveFileName = saveFileName;
		this.saveFileNameSuffix = saveFileNameSuffix;
	}

	public void reset(int maxLen) {
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
		return 0;
	}

	// //////////////////

	private static int MinTypeSign = Integer.MAX_VALUE;
	private static int MaxTypeSign = Integer.MIN_VALUE;
	// 所有枚举
	private static final Map<Integer, KGangRankTypeEnum> typeMap = new HashMap<Integer, KGangRankTypeEnum>();
	static {
		KGangRankTypeEnum[] enums = KGangRankTypeEnum.values();
		KGangRankTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			typeMap.put(type.sign, type);
			if (type.sign < MinTypeSign) {
				MinTypeSign = type.sign;
			}
			if (type.sign > MaxTypeSign) {
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
	public static KGangRankTypeEnum getEnum(int sign) {
		return typeMap.get(sign);
	}

	public static int size() {
		return typeMap.size();
	}
}