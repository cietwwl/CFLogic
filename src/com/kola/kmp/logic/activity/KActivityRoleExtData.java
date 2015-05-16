package com.kola.kmp.logic.activity;

import java.util.HashMap;
import java.util.Map;

import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.role.RoleExtCABaseImpl;
import com.kola.kmp.logic.activity.goldact.GoldActivityRoleRecordData;
import com.kola.kmp.logic.activity.mineral.KRoleDiggerData;
import com.kola.kmp.logic.activity.newglodact.NewGoldActivityRoleRecordData;
import com.kola.kmp.logic.activity.transport.KTransportData;
import com.kola.kmp.logic.activity.worldboss.KWorldBossRoleData;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPRoleRecordData;

public class KActivityRoleExtData extends RoleExtCABaseImpl {

	private static final String KEY_WORLD_BOSS_DATA = "wb";
	private static final String KEY_TRANSPORT_DATA = "ts";
	private static final String KEY_GOLD_ACT_DATA = "gd";
	private static final String KEY_NEW_GOLD_ACT_DATA = "ngd";
	private static final String KEY_TEAM_PVP_DATA = "tpd";
	private static final String KEY_MINE_DIGGER_DATA = "dig";

	private KTransportData transportData;
	private KWorldBossRoleData _worldBossData;
	private GoldActivityRoleRecordData _glodActivityData;
	private NewGoldActivityRoleRecordData _newGlodActivityData;
	private KTeamPVPRoleRecordData _teamPVPData;
	private KRoleDiggerData _mineDiggerData;
	
	private Map<Integer, Long> activityCdTimeMap = new HashMap<Integer, Long>();

	/**
	 * <pre>
	 * 为缓存生成对象时使用
	 * 
	 * @param dbdata
	 * @author zhaizl
	 * @creation 2014-2-13 下午5:04:42
	 * </pre>
	 */
	protected KActivityRoleExtData() {
	}

	/**
	 * <pre>
	 * 逻辑新建对象时使用
	 * 
	 * @param dbdata
	 * @author zhaizl
	 * @creation 2014-2-13 下午5:04:25
	 * </pre>
	 */
	protected KActivityRoleExtData(long pRoleId, int pType) {
		super(pRoleId, pType);
		this._worldBossData = new KWorldBossRoleData(pRoleId, this.getDataStatusProxy());
		this._glodActivityData = new GoldActivityRoleRecordData(this.getDataStatusProxy());
		this.transportData = new KTransportData(this.getRoleId(), this.getDataStatusProxy());
		this._newGlodActivityData = new NewGoldActivityRoleRecordData(this.getDataStatusProxy());
		this._teamPVPData = new KTeamPVPRoleRecordData(this.getDataStatusProxy());
		this._mineDiggerData = new KRoleDiggerData(this.getRoleId(), this.getDataStatusProxy());
	}

	@Override
	protected void decode(String attribute) {
		try {
			JSONObject obj = new JSONObject(attribute);
			this._worldBossData.parseData(obj.optString(KEY_WORLD_BOSS_DATA));
			this._glodActivityData.decode(obj.optString(KEY_GOLD_ACT_DATA));
			this.transportData.decode(obj.optString(KEY_TRANSPORT_DATA));
			this._newGlodActivityData.decode(obj.optString(KEY_NEW_GOLD_ACT_DATA));
			this._teamPVPData.decode(obj.optString(KEY_TEAM_PVP_DATA, ""));
			String diggerData = obj.optString(KEY_MINE_DIGGER_DATA, "");
			if(diggerData != null && diggerData.length() > 0) {
				this._mineDiggerData.parse(diggerData);
			}
		} catch (Exception e) {
			LOGGER.error("解析角色活动数据出现异常！角色id：{}", getRoleId(), e);
		}
	}

	@Override
	protected String encode() {
		JSONObject obj = new JSONObject();
		try {
			obj.put(KEY_WORLD_BOSS_DATA, this._worldBossData.saveData());
			obj.put(KEY_GOLD_ACT_DATA, this._glodActivityData.encode());
			obj.put(KEY_TRANSPORT_DATA, this.transportData.encode());
			obj.put(KEY_NEW_GOLD_ACT_DATA, this._newGlodActivityData.encode());
			obj.put(KEY_TEAM_PVP_DATA, this._teamPVPData.encode());
			obj.put(KEY_MINE_DIGGER_DATA, _mineDiggerData.save());
		} catch (Exception e) {
			LOGGER.error("保存角色活动数据出现异常！角色id：{}", getRoleId(), e);
		}
		return obj.toString();
	}

	public KTransportData getTransportData() {
		if (transportData == null) {
			transportData = new KTransportData(this.getRoleId(),
					this.getDataStatusProxy());
		}
		return transportData;
	}

	public KWorldBossRoleData getWorldBossRoleData() {
		return this._worldBossData;
	}

	public GoldActivityRoleRecordData getGlodActivityData() {
		return this._glodActivityData;
	}
	
	public NewGoldActivityRoleRecordData getNewGoldActivityData() {
		return this._newGlodActivityData;
	}
	
	public KTeamPVPRoleRecordData getTeamPVPData() {
		return this._teamPVPData;
	}
	
	public KRoleDiggerData getMineDiggerData() {
		return this._mineDiggerData;
	}

	public void notifyDB() {
		this.notifyUpdate();
	}
	
	public boolean isActivityCdTime(int activityId){
		boolean isCdTime = false;
		if(this.activityCdTimeMap.containsKey(activityId)){
			long nowTime = System.currentTimeMillis();
			long endTime = this.activityCdTimeMap.get(activityId) + KActivityManager.getInstance().getActivity(activityId).getCdTimeSeconds() * 1000;
			if (nowTime >= endTime) {
				activityCdTimeMap.remove(activityId);
			} else {
				isCdTime = true;
			}
		}
		return isCdTime;
	}
	
	public int getActivityRestCdTimeSeconds(int activityId){
		if(this.activityCdTimeMap.containsKey(activityId)){
			long nowTime = System.currentTimeMillis();
			long endTime = this.activityCdTimeMap.get(activityId) + KActivityManager.getInstance().getActivity(activityId).getCdTimeSeconds() * 1000;
			if (nowTime >= endTime) {
				return 0;
			} else {
				return (int) ((endTime - nowTime) / 1000);
			}
		}
		return 0;
	}
	
	public void notifyActivityCdTime(int activityId){
		this.activityCdTimeMap.put(activityId, System.currentTimeMillis());
	}

}
