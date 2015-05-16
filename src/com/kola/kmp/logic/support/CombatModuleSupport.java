package com.kola.kmp.logic.support;

import java.util.List;
import java.util.Map;

import com.kola.kmp.logic.combat.ICombatEnhanceInfo;
import com.kola.kmp.logic.combat.ICombatMirrorDataGroup;
import com.kola.kmp.logic.combat.ICombatRoleSideHpUpdater;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.level.gamestory.Animation;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.role.KRole;

/**
 * 
 * @author PERRY CHAN
 */
public interface CombatModuleSupport {

	/**
	 * 
	 * <pre>
	 * role与defenderRoleId所代表的实例的AI进行战斗。
	 * <b><font color="ff0000">role必须在线</font></b>
	 * </pre>
	 * 
	 * @param role
	 * @param defenderRoleId
	 * @param type
	 * @param env 战斗环境
	 * @param attachment 附属对象，这个对象将会在战斗结算传回相应的方法
	 * @return 执行结果，附带一个错误代码。错误代码常量请参考{@link com.kola.kmp.logic.combat.ICombatConstant}
	 */
	public KActionResult<Integer> fightWithAI(KRole role, long defenderRoleId, KCombatType type, ICombatEnv env, Object attachment);
	
	/**
	 * 
	 * <pre>
	 * role与defenderRoleId所代表的实例的AI进行战斗。有时间限制
	 * 时间限制仍未战胜对手，会判定为失败
	 * <b><font color="ff0000">role必须在线</font></b>
	 * </pre>
	 * 
	 * @param role
	 * @param defenderRoleId
	 * @param type
	 * @param env 战斗环境
	 * @param attachment 附属对象，这个对象将会在战斗结算传回相应的方法
	 * @param timeOutMillis 超时的毫秒
	 * @return 执行结果，附带一个错误代码。错误代码常量请参考{@link com.kola.kmp.logic.combat.ICombatConstant}
	 */
	public KActionResult<Integer> fightWithAIWithTimeLimit(KRole role, long defenderRoleId, KCombatType type, ICombatEnv env, Object attachment, int timeOutMillis);

	/**
	 * 
	 * 进入<font color="ff0000">battlefield</font>进行战斗，并且使用<font color="ff0000">map</font>里面
	 * 的元素去替换<font color="ff0000">battlefield</font>的怪物数据（如果存在的话）。
	 * <p>
	 * <font color="ff0000">enhanceInfo</font>表示角色的增强信息。详情请参考{@link ICombatEnhanceInfo}
	 * 
	 * @param role 角色
	 * @param battlefield 目标关卡
	 * @param enhanceInfo 角色增强信息
	 * @param map 要替换到关卡里面的怪物信息，key=怪物在地图数据里面的instanceId，value=怪物信息
	 * @param type 战斗类型（参考{@link KCombatType}）
	 * @param attachment 附属对象，这个对象将会在战斗结算传回相应的方法
	 * @return
	 */
	public KActionResult<Integer> fightByUpdateInfo(KRole role, KGameBattlefield battlefield, ICombatEnhanceInfo enhanceInfo, Map<Integer, ICombatMonsterUpdateInfo> map, KCombatType type, Object attachment);
	
	
	/**
	 * 
	 * 进入<font color="ff0000">battlefield</font>进行战斗，并且使用<font color="ff0000">map</font>里面
	 * 的元素去替换<font color="ff0000">battlefield</font>的怪物数据（如果存在的话）。
	 * <p>
	 * <font color="ff0000">enhanceInfo</font>表示角色的增强信息。详情请参考{@link ICombatEnhanceInfo}
	 * @param role
	 * @param battlefield
	 * @param enhanceInfo
	 * @param map
	 * @param type
	 * @param attachment
	 * @param timeOutMillis 超时的毫秒
	 * @return
	 */
	public KActionResult<Integer> fightByUpdateInfo(KRole role, KGameBattlefield battlefield, ICombatEnhanceInfo enhanceInfo, Map<Integer, ICombatMonsterUpdateInfo> map, KCombatType type, Object attachment, int timeOutMillis);
	/**
	 * 
	 * @param role
	 * @param battlefield
	 * @param enhanceInfo
	 * @param map
	 * @param pRobotIds
	 * @param type
	 * @param attachment
	 * @return
	 */
	public KActionResult<Integer> fightByUpdateInfoWithRobots(KRole role, KGameBattlefield battlefield, ICombatEnhanceInfo enhanceInfo, Map<Integer, ICombatMonsterUpdateInfo> map, List<Long> pRobotIds, KCombatType type, Object attachment);
	
	/**
	 * 进入<font color="ff0000">battlefield</font>进行战斗
	 * 
	 * @param role 进入战斗的角色
	 * @param battlefield 指定的关卡
	 * @param type 战斗类型
	 * @param attachment 战斗结果附加属性
	 * @return
	 */
	public KActionResult<Integer> fightInBattlefield(KRole role, KGameBattlefield battlefield, List<Animation> animationList, KCombatType type, Object attachment);
	
	/**
	 * 
	 * @param role
	 * @param teammateId
	 * @param enermyIds
	 * @param env
	 * @param attachment
	 * @return
	 */
	public KActionResult<Integer> fightWithAI(KRole role, long teammateId, long[] enemyIds, KCombatType type, ICombatEnvPlus env, Object attachment, long timeoutMillis);
	
	/**
	 * 
	 * @param role
	 * @param teammateId
	 * @param enemies
	 * @param env
	 * @param attachment
	 * @return
	 */
	public KActionResult<Integer> fightWithAI(KRole role, long teammateId, List<ICombatMirrorDataGroup> enemies, KCombatType type, ICombatEnvPlus env, Object attachment, long timeoutMillis);
	
	/**
	 * 
	 * @param role
	 */
	public void forceFinishCombat(KRole role, KCombatType type);
	
	/**
	 * 
	 * @param pUpdater
	 */
	public void registerCombatHpUpdater(ICombatRoleSideHpUpdater pUpdater);
	
	/**
	 * 
	 * <pre>
	 * 代表一个战斗环境的数据规范
	 * 由于进入战斗，都需要赋值例如背景音乐id、地图资源路径等这些数据，
	 * 而这些数据本身不应该有战斗模块管理，故规范此接口，以后每个调用
	 * 进入战斗的方法，都应该同时带有此参数。
	 * </pre>
	 * 
	 * @author PERRY CHAN
	 */
	public static interface ICombatEnv {
		
		/**
		 * 
		 * 获取战斗环境的背景音乐资源id
		 * 
		 * @return
		 */
		public int getBgMusicResId();
		
		/**
		 * 
		 * 获取战斗环境的地图资源路径名称
		 * 
		 * @return
		 */
		public String getBgResPath();
		
		/**
		 * 
		 * 获取主角出生的x坐标
		 * 
		 * @return
		 */
		public float getBornCorX();
		
		/**
		 * 
		 * 获取主角出生的y坐标
		 * 
		 * @return
		 */
		public float getBornCorY();
		
		/**
		 * 
		 * 获取敌人出生的x坐标
		 * 
		 * @return
		 */
		public float getEnermyCorX();
		
		/**
		 * 
		 * 获取敌人出生的y坐标
		 * 
		 * @return
		 */
		public float getEnermyCorY();
	}
	
	/**
	 * 
	 * 战斗怪物更新信息
	 * 
	 * @author PERRY CHAN
	 */
	public interface ICombatMonsterUpdateInfo {
		
		/**
		 * 
		 * 获取instanceId
		 * 
		 * @return
		 */
		public int getInstanceId();
		
		/**
		 * 
		 * @return
		 */
		public int getTemplateId();
		
		/**
		 * 
		 * 获取当前的血量
		 * 
		 * @return
		 */
		public long getCurrentHp();
	}
	
	/**
	 * 
	 * <pre>
	 * 代表一个战斗环境的数据规范
	 * 由于进入战斗，都需要赋值例如背景音乐id、地图资源路径等这些数据，
	 * 而这些数据本身不应该有战斗模块管理，故规范此接口，以后每个调用
	 * 进入战斗的方法，都应该同时带有此参数。
	 * </pre>
	 * 
	 * @author PERRY CHAN
	 */
	public static interface ICombatEnvPlus {
		
		/**
		 * 
		 * 获取战斗环境的背景音乐资源id
		 * 
		 * @return
		 */
		public int getBgMusicResId();
		
		/**
		 * 
		 * 获取战斗环境的地图资源路径名称
		 * 
		 * @return
		 */
		public String getBgResPath();
		
		/**
		 * 
		 * @return
		 */
		public List<float[]> getRoleCorDatas();
		
		/**
		 * 
		 * @return
		 */
		public List<float[]> getEnemyCorDatas();
	}
}
