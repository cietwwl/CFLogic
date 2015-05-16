package com.kola.kmp.logic.rank.teampvp;

import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.db.dataobject.DBRank;
import com.kola.kmp.logic.rank.abs.ElementAbs;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-9-3 下午12:36:07
 * </pre>
 */
public class TeamPVPRankElement extends ElementAbs {

	private final static String Str_exp = "exp";
	private final static String Str_battlePow = "pow";
	private final static String Str_leaderRoleId = "leaderId";
	private final static String Str_leaderRoleName = "leaderName";
	private final static String Str_leaderRoleVip = "leaderVip";
	private final static String Str_memRoleId = "memId";
	private final static String Str_memRoleName = "memName";
	private final static String Str_memRoleVip = "memVip";

	private int exp;// 胜点
	private int battlePow;// 队伍总战力
	private long leaderRoleId;// 队长角色ID
	private String leaderRoleName;// 队长角色名称
	private byte leaderRoleVip;// 队长角色VIP
	private long memRoleId;// 队员角色ID
	private String memRoleName;// 队员角色名称
	private byte memRoleVip;// 队员角色VIP

	TeamPVPRankElement(long tempId, String tempName, int lv, int exp, int battlePow) {
		super(tempId, tempName, lv);
		this.exp = exp;
		this.battlePow = battlePow;
	}

	TeamPVPRankElement(DBRank db, JSONObject jsonCA) {
		super(db, jsonCA);
		try {
			this.exp = jsonCA.getInt(Str_exp);
			this.battlePow = jsonCA.getInt(Str_battlePow);
			this.leaderRoleId = jsonCA.getLong(Str_leaderRoleId);
			this.leaderRoleName = jsonCA.getString(Str_leaderRoleName);
			this.leaderRoleVip = jsonCA.getByte(Str_leaderRoleVip);
			this.memRoleName = jsonCA.optString(Str_memRoleName, null);
			if (this.memRoleName != null) {
				this.memRoleId = jsonCA.getLong(Str_memRoleId);
				this.memRoleVip = jsonCA.getByte(Str_memRoleVip);
			}
		} catch (JSONException e1) {
			throw new RuntimeException(e1.getMessage());
		}
	}

	public int getExp() {
		return exp;
	}

	public int getBattlePow() {
		return battlePow;
	}

	public long getLeaderRoleId() {
		return leaderRoleId;
	}

	public String getLeaderRoleName() {
		return leaderRoleName;
	}

	public int getLeaderRoleVip() {
		return leaderRoleVip;
	}

	public long getMemRoleId() {
		return memRoleId;
	}

	public String getMemRoleName() {
		return memRoleName;
	}

	public int getMemRoleVip() {
		return memRoleVip;
	}

	void resetInfo(int lv, int exp, int battlePow) {
		super.setElementLv(lv);
		this.exp = exp;
		this.battlePow = battlePow;
	}

	void setBattlePower(int battlePower) {
		this.battlePow = battlePower;
	}

	void resetRoles(long leaderRoleId, String leaderRoleName, int leaderRoleVip, long memRoleId, String memRoleName, int memRoleVip) {
		this.leaderRoleId = leaderRoleId;
		this.leaderRoleName = leaderRoleName;
		this.leaderRoleVip = (byte) leaderRoleVip;
		this.memRoleId = memRoleId;
		this.memRoleName = memRoleName;
		this.memRoleVip = (byte) memRoleVip;
	}

	@Override
	public int compareTo(ElementAbs element) {
		// 按等级、等级段内经验值进行排名。
		TeamPVPRankElement oo = (TeamPVPRankElement) element;
		if (elementLv < oo.elementLv) {
			return -1;
		}
		if (elementLv > oo.elementLv) {
			return 1;
		}
		if (exp > oo.exp) {
			return -1;
		}
		if (exp < oo.exp) {
			return 1;
		}
		return compareFinal(element);
	}

	protected void saveToXML(Element element) {
		super.saveToXML(element);
		element.setAttribute(Str_exp, String.valueOf(exp));
	}

	protected void saveToDB(DBRank db, JSONObject jsonCA) throws JSONException {
		super.saveToDB(db, jsonCA);
		jsonCA.put(Str_exp, exp);
		jsonCA.put(Str_battlePow, battlePow);
		jsonCA.put(Str_leaderRoleId, leaderRoleId);
		jsonCA.put(Str_leaderRoleName, leaderRoleName);
		jsonCA.put(Str_leaderRoleVip, leaderRoleVip);
		if(memRoleName!=null){
			jsonCA.put(Str_memRoleId, memRoleId);
			jsonCA.put(Str_memRoleName, memRoleName);
			jsonCA.put(Str_memRoleVip, memRoleVip);
		}
	}

	public void print(Logger _LOGGER) {
		_LOGGER.warn("rank={},id={},name={},lv={},{}={}", getRank(), elementId, elementName, elementLv, Str_exp, exp, Str_battlePow, battlePow, Str_leaderRoleId, leaderRoleId, Str_leaderRoleName,
				leaderRoleName, Str_leaderRoleVip, leaderRoleVip, Str_memRoleId, memRoleId, Str_memRoleName, memRoleName, Str_memRoleVip, memRoleVip);
	}
}
