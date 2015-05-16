package com.kola.kmp.logic.rank.gang;

import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.db.dataobject.DBRank;
import com.kola.kmp.logic.rank.abs.ElementAbs;

/**
 * <pre>
 * 军团战报名
 * 
 * @author CamusHuang
 * @creation 2013-6-10 下午8:59:51
 * </pre>
 */
public class GangRankElementWarSignUp extends GangRankElementAbs {
	/* 军团繁荣度 */
	private final static String Str_flourish = "flourish";

	private int flourish;

	GangRankElementWarSignUp(long gangId, String gangName, int lv, int flourish) {
		super(gangId, gangName, lv);
		this.flourish = flourish;
	}

	GangRankElementWarSignUp(DBRank db, JSONObject jsonCA) {
		super(db, jsonCA);
		try {
			this.flourish = jsonCA.getInt(Str_flourish);
		} catch (JSONException e1) {
			throw new RuntimeException(e1.getMessage());
		}
	}

	public int getFlourish() {
		return flourish;
	}

	void setFlourish(int flourish) {
		this.flourish = flourish;
	}

	@Override
	public int compareTo(ElementAbs element) {
		GangRankElementWarSignUp oo = (GangRankElementWarSignUp) element;
		if (oo.flourish < flourish) {
			return -1;
		}
		if (oo.flourish > flourish) {
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
		element.setAttribute(Str_flourish, String.valueOf(flourish));
	}

	protected void saveToDB(DBRank db, JSONObject jsonCA) throws JSONException {
		super.saveToDB(db, jsonCA);
		jsonCA.put(Str_flourish, flourish);
	}

	public void print(Logger _LOGGER) {
		_LOGGER.warn("rank={},id={},name={},lv={},{}={}", getRank(), elementId, elementName, elementLv, Str_flourish, flourish);
	}
}
