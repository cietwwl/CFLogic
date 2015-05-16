package com.kola.kmp.logic.rank;

import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.db.dataobject.DBRank;
import com.kola.kmp.logic.rank.abs.ElementAbs;

/**
 * <pre>
 * 随从
 * 
 * @author CamusHuang
 * @creation 2013-6-10 下午8:59:51
 * </pre>
 */
public class RankElementPet extends RankElementAbs {

	// 经验
	private final static String Str_petName = "petName";
	private final static String Str_petLv = "petLv";
	private final static String Str_petPow = "petPow";

	private String petName;
	private int petLv;
	private int petPow;
	
	RankElementPet(long roleId, String roleName, int roleLv, int job, int battlePower, String gangName) {
		super(roleId, roleName, roleLv, job, battlePower, gangName);
	}

	RankElementPet(DBRank db, JSONObject jsonCA) {
		super(db, jsonCA);
		try {
			this.petName = jsonCA.getString(Str_petName);
			this.petLv = jsonCA.getInt(Str_petLv);
			this.petPow = jsonCA.getInt(Str_petPow);
		} catch (JSONException e1) {
			throw new RuntimeException(e1.getMessage());
		}
	}

	@Override
	public int compareTo(ElementAbs element) {
		RankElementPet oo = (RankElementPet) element;

		if (petPow > oo.petPow) {
			return -1;
		}
		if (petPow < oo.petPow) {
			return 1;
		}
		if (petLv > oo.petLv) {
			return -1;
		}
		if (petLv < oo.petLv) {
			return 1;
		}
		
		return compareFinal(oo);
	}

	String getPetName() {
		return petName;
	}

	RankElementPet setPetName(String petName) {
		this.petName = petName;
		return this;
	}

	int getPetLv() {
		return petLv;
	}

	RankElementPet setPetLv(int petLv) {
		this.petLv = petLv;
		return this;
	}

	int getPetPow() {
		return petPow;
	}

	RankElementPet setPetPow(int petPow) {
		this.petPow = petPow;
		return this;
	}
	
	protected void saveToXML(Element element) {
		super.saveToXML(element);
		element.setAttribute(Str_petName, petName);
		element.setAttribute(Str_petLv, String.valueOf(petLv));
		element.setAttribute(Str_petPow, String.valueOf(petPow));
	}
	
	protected void saveToDB(DBRank db,JSONObject jsonCA) throws JSONException {
		super.saveToDB(db, jsonCA);
		jsonCA.put(Str_petName, petName);
		jsonCA.put(Str_petLv, petLv);
		jsonCA.put(Str_petPow, petPow);
	}

	public void print(Logger _LOGGER) {
		_LOGGER.warn("rank={},roleId={},roleName={},roleLv={},{}={},{}={},{}={}", rank, elementId, elementName, elementLv, Str_petName, petName, Str_petLv, petLv,
				Str_petPow, petPow);
	}
}
