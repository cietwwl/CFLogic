package com.kola.kmp.logic.rank;

import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.db.dataobject.DBRank;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.rank.abs.ElementAbs;

/**
 * <pre>
 * 等级
 * 
 * @author CamusHuang
 * @creation 2013-6-10 下午8:59:51
 * </pre>
 */
public class RankElementLevel extends RankElementAbs {

	// 经验
	private final static String Str_exp = "exp";

	private int exp;

	RankElementLevel(long roleId, String roleName, int roleLv, int exp, int job, int battlePower, String gangName) {
		super(roleId, roleName, roleLv, job, battlePower, gangName);
		this.exp = exp;
	}

	RankElementLevel(DBRank db,JSONObject jsonCA) {
		super(db, jsonCA);
		try {
			this.exp = jsonCA.getInt(Str_exp);
		} catch (JSONException e1) {
			throw new RuntimeException(e1.getMessage());
		}
	}

	@Override
	public int compareTo(ElementAbs element) {
		RankElementLevel oo = (RankElementLevel) element;

		if (elementLv > oo.elementLv) {
			return -1;
		}
		if (elementLv < oo.elementLv) {
			return 1;
		}
		
		return compareFinal(oo);
	}

	int getExp() {
		return exp;
	}

	void setExp(int exp) {
		this.exp = exp;
	}

	protected void saveToXML(Element element) {
		super.saveToXML(element);
		element.setAttribute(Str_exp, String.valueOf(exp));
	}
	
	protected void saveToDB(DBRank db,JSONObject jsonCA) throws JSONException {
		super.saveToDB(db, jsonCA);
		jsonCA.put(Str_exp, exp);
	}

	public void print(Logger _LOGGER) {
		_LOGGER.warn("rank={},roleId={},roleName={},roleLv={},{}={},{}={},{}={},{}={}", rank, elementId, elementName, elementLv, Str_exp, exp, Str_job, KJobTypeEnum.getJobName((byte) job),
				Str_battlePower, getBattlePower(), Str_gangName, getGangName());
	}
}
