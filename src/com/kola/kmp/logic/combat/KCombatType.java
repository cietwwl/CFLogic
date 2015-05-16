package com.kola.kmp.logic.combat;

/**
 * 
 * @author PERRY CHAN
 */
public enum KCombatType {
	
	/** 战斗类型：关卡 */
	GAME_LEVEL(1, false, false),
	/** 战斗类型：竞技场 */
	COMPETITION(2, true, false),
	/** 战斗类型：塔防 */
	TOWER_COMBAT(3, false, true),
	/** 战斗类型：世界boss */
	WORLD_BOSS(4, false, false),
	/** 战斗类型：物资运送对战 */
	TRANSPORT_COMBAT(5, true, false),
	/** 战斗类型：军团资源战 */
	GANG_RESWAR(6, true, false),
	/** 战斗类型：军团战PVP */
	GANG_WAR_PVP(7, true, false),
	/** 战斗类型：军团战PVE */
	GANG_WAR_PVE(8, false, true),
	/**战斗类型：产金活动 */
	BARREL(9, false, true),
	/**战斗类型：离线战斗 */
	OFFLINE_COMBAT(10, false, false),
	/** 战斗类型：队伍竞技 */
	TEAM_PVP(11, true, true),
	/** 战斗类型：随从挑战副本 */
	PET_CHALLENGE_COPY(12, false, false),
	/** 战斗类型：切磋PVP */
	INTERCOURSE_PVP(13, true, true),
	/** 战斗类型：挖矿PVP */
	MINERAL_PVP(14, true, false),
	/** 战斗类型：爬塔 */
	CLIMB_TOWER_COMBAT(15, false ,false),
	/** 战斗类型：随从副本 */
	PET_COPY(16, false, true),
	/** 战斗类型：随从挑战副本 */
	PET_CHALLENGE_SENIOR_COPY(17, false, false),
	/** 战斗类型：测试*/
	TEST(999, false, false),
	;
	public final int sign;
	/**
	 * 是否PVP战斗
	 */
	public final boolean isPVP;
	/**
	 * <pre>
	 * 如果客户端撤退，是否也发结束消息到客户端
	 * 客户端撤退流程：
	 * 客户端发送撤退消息--->服务器处理撤退--->发送战斗结束消息--->客户端显示结算界面
	 * A.有些战斗，撤退的时候，是不需要显示结算界面的，所以就不用发送战斗结束消息到客户端，
	 * 而是由每个战斗后处理器单独处理诸如返回主城的操作。
	 * B.有些战斗，撤退的时候，同样需要显示结算界面，这个时候，就需要服务器发送战斗结束
	 * 消息到客户端，因为客户端只有等到战斗结束消息，才会进行结算界面的显示调用
	 * </pre>
	 */
	public final boolean alsoSendFinishMsgIfEscape;
	private boolean _canUsePVPSkill; // 能否使用PVP被动技能（即天赋提供的一些技能，例如加速之类的）
	private boolean _canAutoFight; // 能否自动战斗
	private boolean _useServerMonsterResId; // 是否使用服务器发送的怪物资源id

	private KCombatType(int pSign, boolean pIsPVP, boolean pAlsoSendFinishMsgIfEscape) {
		this.sign = pSign;
		this.isPVP = pIsPVP;
		this.alsoSendFinishMsgIfEscape = pAlsoSendFinishMsgIfEscape;
	}
	
	public void setCanUsePVPSkill(boolean value) {
		this._canUsePVPSkill = value;
	}
	
	public boolean isCanUsePVPSkill() {
		return this._canUsePVPSkill;
	}
	
	public void setCanAutoFight(boolean pCanAutoFight) {
		this._canAutoFight = pCanAutoFight;
	}
	
	public boolean isCanAutoFight() {
		return _canAutoFight;
	}
	
	public void setUseServerResIdOfMonster(boolean flag) {
		this._useServerMonsterResId = flag;
	}
	
	public boolean isUseServerResIdOfMonster() {
		return _useServerMonsterResId;
	}
	
	public static KCombatType getCombatType(int pSign) {
		KCombatType[] types = values();
		KCombatType tempType;
		for(int i = 0; i < types.length; i++) {
			tempType = types[i];
			if(tempType.sign == pSign) {
				return tempType;
			}
		}
		return null;
	}
}
