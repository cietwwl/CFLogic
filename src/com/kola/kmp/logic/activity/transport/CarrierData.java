package com.kola.kmp.logic.activity.transport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.util.text.HyperTextTool;

public class CarrierData {
	// 航道类型为水道
	public final static byte LANE_TYPE_WATER = 2;
	// 航道类型为陆路
	public final static byte LANE_TYPE_LAND = 1;

	public static SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
	static {
		formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
	}

	// 载具ID
	private int carrierId;
	// 载具名称
	private String carrierName;
	// 染色后的载具名称
	private String extName;
	// 运输时间
	private long tranTime;
	// 运输时间(单位：秒)
	private int tranTimeSeconds;
	// 基础经验奖励
	private int baseExp;
	// 基础潜能奖励
	private int basePotential;
	// 基础权重
	private int baseWeight;
	// 权重上限
	private int maxWeight;
	// 刷新权重增长
	private int reflashUpWieghtCount;
	// 航道类型
	private byte laneType;
	// 客户端资源Id
	private int resId;

	public CarrierData(int carrierId, String carrierName, int tranTimeSeconds,
			int baseExp, int basePotential, int baseWeight, int maxWeight,
			int reflashUpWieghtCount, byte laneType, int resId) {
		super();
		this.carrierId = carrierId;
		this.carrierName = carrierName;
		this.extName = HyperTextTool.extRoleName(carrierName);
		this.tranTimeSeconds = tranTimeSeconds;
		this.tranTime = tranTimeSeconds * 1000;
		this.baseExp = baseExp;
		this.basePotential = basePotential;
		this.baseWeight = baseWeight;
		this.maxWeight = maxWeight;
		this.reflashUpWieghtCount = reflashUpWieghtCount;
		this.laneType = laneType;
		this.resId = resId;
	}

	public int getCarrierId() {
		return carrierId;
	}

	public String getCarrierName() {
		return carrierName;
	}

	public String getExtName() {
		return extName;
	}

	public long getTranTime() {
		return tranTime;
	}

	public int getTranTimeSeconds() {
		return tranTimeSeconds;
	}

	public String getTranTimeStr() {
		return formatter.format(tranTime);
	}

	public int getBaseExp() {
		return baseExp;
	}

	public int getBasePotential() {
		return basePotential;
	}

	public int getBaseWeight() {
		return baseWeight;
	}

	public int getMaxWeight() {
		return maxWeight;
	}

	public int getReflashUpWieghtCount() {
		return reflashUpWieghtCount;
	}

	public byte getLaneType() {
		return laneType;
	}

	public int getResId() {
		return resId;
	}

}
