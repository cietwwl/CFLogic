package com.kola.kmp.logic.rank.gang;

import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.db.dataobject.DBRank;
import com.kola.kmp.logic.rank.abs.ElementAbs;

/**
 * <pre>
 * 全部军团
 * 
 * @author CamusHuang
 * @creation 2013-6-10 下午8:59:51
 * </pre>
 */
public class GangRankElement extends GangRankElementAbs {
	// 经验
	private final static String Str_exp = "exp";
	private int exp;

	GangRankElement(long gangId, String gangName, int lv, int exp) {
		super(gangId, gangName, lv);
		this.exp = exp;
	}

	GangRankElement(DBRank db, JSONObject jsonCA) {
		super(db, jsonCA);
		try {
			this.exp = jsonCA.getInt(Str_exp);
		} catch (JSONException e1) {
			throw new RuntimeException(e1.getMessage());
		}
	}

	int getExp() {
		return exp;
	}

	void setExp(int exp) {
		this.exp = exp;
	}

	@Override
	public int compareTo(ElementAbs element) {
		// 军团列表按军团的等级、等级段内经验值进行排名。
		GangRankElement oo = (GangRankElement) element;
		if (elementLv > oo.elementLv) {
			return -1;
		}
		if (elementLv < oo.elementLv) {
			return 1;
		}
//		if (exp > oo.exp) {
//			return -1;
//		}
//		if (exp < oo.exp) {
//			return 1;
//		}
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
		element.setAttribute(Str_exp, String.valueOf(exp));
	}

	protected void saveToDB(DBRank db, JSONObject jsonCA) throws JSONException {
		super.saveToDB(db, jsonCA);
		jsonCA.put(Str_exp, exp);
	}

	public void print(Logger _LOGGER) {
		_LOGGER.warn("rank={},id={},name={},lv={},{}={}", getRank(), elementId, elementName, elementLv, Str_exp, exp);
	}
}
