package com.kola.kmp.logic.rank.gang;

import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.db.dataobject.DBRank;
import com.kola.kmp.logic.rank.abs.ElementAbs;

/**
 * <pre>
 * 军团战积分
 * 
 * 实力排名（按照军团战积分排序，当积分一样时，按军团编号顺序排序）
 * 
 * @author CamusHuang
 * @creation 2013-6-10 下午8:59:51
 * </pre>
 */
public class GangRankElementWar extends GangRankElementAbs {
	/* 军团战积分 */
	private final static String Str_warScore = "warScore";

	private long warScore;

	GangRankElementWar(long gangId, String gangName, int lv, long warScore) {
		super(gangId, gangName, lv);
		this.warScore = warScore;
	}

	GangRankElementWar(DBRank db, JSONObject jsonCA) {
		super(db, jsonCA);
		try {
			this.warScore = jsonCA.getLong(Str_warScore);
		} catch (JSONException e1) {
			throw new RuntimeException(e1.getMessage());
		}
	}

	public long getWarScore() {
		return warScore;
	}

	void setWarScore(long warScore) {
		this.warScore = warScore;
	}

	@Override
	public int compareTo(ElementAbs element) {
		GangRankElementWar oo = (GangRankElementWar) element;
		if (oo.warScore < warScore) {
			return -1;
		}
		if (oo.warScore > warScore) {
			return 1;
		}
//		if (elementId < oo.elementId) {
//			return -1;
//		}
//		if (elementId > oo.elementId) {
//			return 1;
//		}
//		return 0;
		return compareFinal(element);
	}

	protected void saveToXML(Element element) {
		super.saveToXML(element);
		element.setAttribute(Str_warScore, String.valueOf(warScore));
	}

	protected void saveToDB(DBRank db, JSONObject jsonCA) throws JSONException {
		super.saveToDB(db, jsonCA);
		jsonCA.put(Str_warScore, warScore);
	}

	public void print(Logger _LOGGER) {
		_LOGGER.warn("rank={},id={},name={},lv={},{}={}", getRank(), elementId, elementName, elementLv, Str_warScore, warScore);
	}
}
