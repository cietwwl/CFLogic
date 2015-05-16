package com.kola.kmp.logic.rank.gang;

import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.db.dataobject.DBRank;
import com.kola.kmp.logic.rank.abs.ElementAbs;

/**
 * <pre>
 * 军团战力
 * 
 * 按照军团等级、战力排序，当等级一样时，按战力顺序排序）
 * 
 * @author CamusHuang
 * @creation 2013-6-10 下午8:59:51
 * </pre>
 */
public class GangRankElementPower extends GangRankElementAbs {
	/* 军团战战力 */
	private final static String Str_battlePow = "pow";

	private long battlePow;

	GangRankElementPower(long gangId, String gangName, int lv, long battlePow) {
		super(gangId, gangName, lv);
		this.battlePow = battlePow;
	}

	GangRankElementPower(DBRank db, JSONObject jsonCA) {
		super(db, jsonCA);
		try {
			this.battlePow = jsonCA.getLong(Str_battlePow);
		} catch (JSONException e1) {
			throw new RuntimeException(e1.getMessage());
		}
	}

	public long getBattlePow() {
		return battlePow;
	}

	void setBattlePow(long battlePow) {
		this.battlePow = battlePow;
	}

	@Override
	public int compareTo(ElementAbs element) {
		GangRankElementPower oo = (GangRankElementPower) element;
		if (elementLv > oo.elementLv) {
			return -1;
		}
		if (elementLv < oo.elementLv) {
			return 1;
		}
		if (battlePow > oo.battlePow) {
			return -1;
		}
		if (battlePow < oo.battlePow) {
			return 1;
		}
		return compareFinal(element);
	}

	protected void saveToXML(Element element) {
		super.saveToXML(element);
		element.setAttribute(Str_battlePow, String.valueOf(battlePow));
	}

	protected void saveToDB(DBRank db, JSONObject jsonCA) throws JSONException {
		super.saveToDB(db, jsonCA);
		jsonCA.put(Str_battlePow, battlePow);
	}

	public void print(Logger _LOGGER) {
		_LOGGER.warn("rank={},id={},name={},lv={},{}={}", getRank(), elementId, elementName, elementLv, Str_battlePow, battlePow);
	}
}
