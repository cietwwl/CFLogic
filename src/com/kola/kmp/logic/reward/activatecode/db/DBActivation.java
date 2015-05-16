package com.kola.kmp.logic.reward.activatecode.db;


/**
 * <pre>
 * 模拟DB激活码数据
 * 
 * @author CamusHuang
 * @creation 2013-6-1 下午2:53:40
 * </pre>
 */
public class DBActivation {
	/** 兑换码 */
	public String activationCode;
	/** 奖励类型（EXCEL奖励套餐） */
	public int type;
	/** 失效时刻 */
	public long effectEndTime;
	/**
	 * 使用此兑换码的GSID（0表示未使用）
	 */
	public int useGSId;
	/**
	 * 平台账号ID（0表示未使用）
	 */
	public long playerId;
	/**
	 * 使用此兑换码的角色ID（0表示未使用）
	 */
	public long useRoleId;
	/**
	 * 使用此兑换码的角色名称（""）
	 */
	public String useRoleName;
	/**
	 * 使用时间（0表示未使用）
	 */
	public long useTime;

	public DBActivation(String activationCode, int type, long effectEndTime, int useGSId, long playerId, long useRoleId,
			String useRoleName, long useTime) {
		this.activationCode = activationCode;
		this.type = type;
		this.effectEndTime = effectEndTime;
		this.useGSId = useGSId;
		this.playerId = playerId;
		this.useRoleId = useRoleId;
		this.useRoleName = useRoleName;
		this.useTime = useTime;
	}
}
