package com.kola.kmp.logic.rank;

import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.db.dataobject.DBRank;
import com.kola.kmp.logic.rank.abs.ElementAbs;

/**
 * <pre>
 * 战神
 * 
 * @author CamusHuang
 * @creation 2013-6-10 下午8:59:51
 * </pre>
 */
public class RankElementPower extends RankElementAbs {

	RankElementPower(long roleId, String roleName, int roleLv, int job, int battlePower, String gangName) {
		super(roleId, roleName, roleLv, job, battlePower, gangName);
	}

	RankElementPower(DBRank db, JSONObject jsonCA) {
		super(db, jsonCA);
	}

	@Override
	public int compareTo(ElementAbs element) {
		RankElementPower oo = (RankElementPower) element;

		if (getBattlePower() > oo.getBattlePower()) {
			return -1;
		}
		if (getBattlePower() < oo.getBattlePower()) {
			return 1;
		}
		if (elementLv > oo.elementLv) {
			return -1;
		}
		if (elementLv < oo.elementLv) {
			return 1;
		}
		
		return compareFinal(oo);
	}
}
