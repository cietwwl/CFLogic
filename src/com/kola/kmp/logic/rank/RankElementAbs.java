package com.kola.kmp.logic.rank;

import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.db.dataobject.DBRank;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.rank.abs.ElementAbs;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 排行榜元素
 * 
 * @author CamusHuang
 * @creation 2013-6-10 下午6:37:36
 * </pre>
 */
public abstract class RankElementAbs extends ElementAbs {

	// 职业、战力、军团
	final static String Str_job = "job";
	final static String Str_battlePower = "bPower";
	final static String Str_gangName = "gangId";
	final static String Str_vip = "vip";

	public final int job;
	private int battlePower;
	private String gangName;
	private byte vipLv;

	RankElementAbs(long roleId, String roleName, int roleLv, int job, int battlePower, String gangName) {
		super(roleId, roleName, roleLv);
		this.job = job;
		this.battlePower = battlePower;
		this.gangName = gangName;
		this.vipLv = (byte)KSupportFactory.getVIPModuleSupport().getVipLv(roleId);
	}

	RankElementAbs(DBRank db,JSONObject jsonCA) {
		super(db, jsonCA);
		try {
			this.job = jsonCA.getInt(Str_job);
			this.battlePower = jsonCA.getInt(Str_battlePower);
			this.gangName = jsonCA.getString(Str_gangName);
			this.vipLv = jsonCA.optByte(Str_vip, (byte)0);
		} catch (JSONException e1) {
			throw new RuntimeException(e1.getMessage());
		}
	}

	int getBattlePower() {
		return battlePower;
	}

	void setBattlePower(int battlePower) {
		this.battlePower = battlePower;
	}

	String getGangName() {
		return gangName;
	}

	void setGangName(String gangName) {
		this.gangName = gangName;
	}
	
	void setVipLv(byte vipLv){
		this.vipLv = vipLv;
	}
	
	byte getVipLv(){
		return vipLv;
	}

	protected void saveToXML(Element element) {
		super.saveToXML(element);
		element.setAttribute(Str_job, String.valueOf(job));
		element.setAttribute(Str_battlePower, String.valueOf(battlePower));
		element.setAttribute(Str_gangName, gangName);
		element.setAttribute(Str_vip, String.valueOf(vipLv));
	}
	
	protected void saveToDB(DBRank db,JSONObject jsonCA) throws JSONException {
		super.saveToDB(db, jsonCA);
		jsonCA.put(Str_job, job);
		jsonCA.put(Str_battlePower, battlePower);
		jsonCA.put(Str_gangName, gangName);
		jsonCA.put(Str_vip, vipLv);
	}

	public void print(Logger _LOGGER) {
		_LOGGER.warn("rank={},roleId={},roleName={},roleLv={},{}={},{}={},{}={}", rank, elementId, elementName, elementLv, Str_job, KJobTypeEnum.getJobName((byte) job),Str_battlePower, battlePower, Str_gangName, gangName);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
