package com.kola.kmp.logic.rank.teampvp;

import java.util.HashMap;
import java.util.Map;

import com.kola.kmp.logic.rank.abs.RankTypeInterface;
import com.kola.kmp.logic.rank.gang.KGangRankTypeEnum;

/**
 * <pre>
 * 21青铜，22白银，23黄金，24白金，25钻石，26最强王者
 * 
 * @author camus
 * @creation 2012-12-31 下午5:08:51
 * </pre>
 */

public enum KTeamPVPRankTypeEnum implements RankTypeInterface {
	青铜(21, "青铜", "./res/output/tempPVPRank/", "青铜", ".xml"), 
	白银(22, "白银", "./res/output/tempPVPRank/", "白银", ".xml"), 
	黄金(23, "黄金", "./res/output/tempPVPRank/", "黄金", ".xml"),
	白金(24, "白金", "./res/output/tempPVPRank/", "白金", ".xml"), 
	钻石(25, "钻石", "./res/output/tempPVPRank/", "钻石", ".xml"), 
	最强王者(26, "最强王者", "./res/output/tempPVPRank/", "最强王者", ".xml"), 
	;

	// 标识数值
	public final int sign;
	// 名称
	public final String name;
	// 最大容量
	private int maxLen = 1000;
	// 保存路径
	public final String saveDirPath;
	// 保存文件名称
	public final String saveFileName;
	// 保存文件后缀
	public final String saveFileNameSuffix;

	private KTeamPVPRankTypeEnum(int sign, String name, String saveDirPath, String saveFileName, String saveFileNameSuffix) {
		this.sign = sign;
		this.name = name;
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
	private static final Map<Integer, KTeamPVPRankTypeEnum> typeMap = new HashMap<Integer, KTeamPVPRankTypeEnum>();
	static {
		KTeamPVPRankTypeEnum[] enums = KTeamPVPRankTypeEnum.values();
		KTeamPVPRankTypeEnum type;
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
	public static KTeamPVPRankTypeEnum getEnum(int sign) {
		return typeMap.get(sign);
	}

	public static int size() {
		return typeMap.size();
	}
}