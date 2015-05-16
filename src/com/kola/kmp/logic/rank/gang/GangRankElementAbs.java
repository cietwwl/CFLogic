package com.kola.kmp.logic.rank.gang;

import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.db.dataobject.DBRank;
import com.kola.kmp.logic.rank.abs.ElementAbs;

/**
 * <pre>
 * 排行榜元素
 * 
 * @author CamusHuang
 * @creation 2013-6-10 下午6:37:36
 * </pre>
 */
public abstract class GangRankElementAbs extends ElementAbs {

	GangRankElementAbs(long id, String name, int lv) {
		super(id, name, lv);
	}

	GangRankElementAbs(DBRank db,JSONObject jsonCA) {
		super(db, jsonCA);
	}

	protected void saveToXML(Element element) {
		super.saveToXML(element);
	}
	
	protected void saveToDB(DBRank db,JSONObject jsonCA) throws JSONException {
		super.saveToDB(db, jsonCA);
	}

	public void print(Logger _LOGGER) {
		super.print(_LOGGER);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
