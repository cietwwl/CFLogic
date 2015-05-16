package com.kola.kmp.logic.gang.war;

import com.kola.kmp.logic.other.KColorFunEnum;
import com.kola.kmp.logic.util.text.HyperTextTool;


/**
 * <pre>
 * 参与军团战的军团
 * 
 * @author CamusHuang
 * @creation 2013-7-15 下午12:02:25
 * </pre>
 */
public class GangData implements Comparable<GangData> {

	// -------顶层数据
	final long gangId;
	final int gangLv;
	final String gangName;
	final String extGangName;
	private int battlePower;// 临时缓存值，应该在排序前进行赋值

	GangData(long gangId, int gangLv, String gangName) {
		this.gangId = gangId;
		this.gangLv = gangLv;
		this.gangName = gangName;
		this.extGangName = HyperTextTool.extColor(gangName, KColorFunEnum.军团名称);
	}

	void setBattlePower(int battlePower) {
		this.battlePower = battlePower;
	}

	int getBattlePower() {
		return battlePower;
	}

	@Override
	public int compareTo(GangData o) {
		if (battlePower > o.battlePower) {
			return -1;
		}
		if (battlePower < o.battlePower) {
			return 1;
		}
		return 0;
	}

}
