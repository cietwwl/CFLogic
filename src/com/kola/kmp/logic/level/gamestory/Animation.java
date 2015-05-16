package com.kola.kmp.logic.level.gamestory;


/**
 * 游戏剧情动画数据
 * @author zhaizl
 *
 */
public class Animation {

	/**
	 * 表示剧情动画起始方式类型为接受任务后触发
	 */
	public static final byte ANIMATION_START_TYPE_ACCEPT_MISSION = 1;
	/**
	 * 表示剧情动画起始方式类型为提交任务后触发
	 */
	public static final byte ANIMATION_START_TYPE_SUBMIT_MISSION = 2;

	/**
	 * 表示任务剧情动画起始方式类型为进入关卡第一场战斗的开始时刻
	 */
	public static final byte ANIMATION_START_TYPE_LEVEL_START = 3;
	/**
	 * 表示任务剧情动画起始方式类型为关卡最后一场战斗结束时触发
	 */
	public static final byte ANIMATION_START_TYPE_LEVEL_END = 4;
	
	/**
	 * 表示任务剧情动画起始方式类型为某场战战斗中遇到第X波怪前播放
	 */
	public static final byte ANIMATION_START_TYPE_BATTLE_WAVE = 5;
	
	/**
	 * 表示任务剧情动画起始方式类型为某个新功能开启后播放
	 */
	public static final byte ANIMATION_START_TYPE_FUNCTION_OPEN = 6;
	
	/**
	 * 表示任务剧情动画起始方式类型为新手引导战场开始时播放
	 */
	public static final byte ANIMATION_START_TYPE_NOVICE_GUIDE_BATTLE_START = 7;
	
	/**
	 * 表示任务剧情动画起始方式类型为新手引导战场结束时播放
	 */
	public static final byte ANIMATION_START_TYPE_NOVICE_GUIDE_BATTLE_END = 8;
	
	/**
	 * 表示任务剧情动画起始方式类型为新手引导战场中遇到第X波怪前播放
	 */
	public static final byte ANIMATION_START_TYPE_NOVICE_GUIDE_BATTLE_WAVE = 9;

	/**
	 * <pre>
	 * 剧情动画起始方式类型，该值参考：
	 * {@link Animation#ANIMATION_START_TYPE_ACCEPT_MISSION}||
	 * {@link Animation#ANIMATION_START_TYPE_SUBMIT_MISSION}||
	 * {@link Animation#ANIMATION_START_TYPE_LEVEL_START}||
	 * {@link Animation#ANIMATION_START_TYPE_LEVEL_END}||
	 * {@link Animation#ANIMATION_START_TYPE_BATTLE_WAVE}
	 * </pre>
	 */
	public byte animationStartType;
	
	/**
	 * 剧情动画编号
	 */
	public int animationId;
	
	/**
	 * 剧情动画资源编号
	 */
	public int animationResId;

	/**
	 * <pre>
	 * 剧情动画目标ID.
	 * 如果animationStartType为ANIMATION_START_TYPE_ACCEPT_MISSION或者ANIMATION_START_TYPE_SUBMIT_MISSION
	 * 则该值表示为任务模版ID
	 * 如果animationStartType为ANIMATION_START_TYPE_LEVEL_START或者ANIMATION_START_TYPE_LEVEL_END
	 * 则该值表示为关卡ID
	 * </pre>
	 */
	public int animationTargetId;
	
	/**
	 * <pre>
	 * 剧情动画所在关卡的战场序号，当animationStartType = ANIMATION_START_TYPE_BATTLE_WAVE时有效。
	 * </pre>
	 */
	public int battlefieldSerialNumber;
	
	/**
	 * <pre>
	 * 剧情动画所在战场的第X波怪物序号，当animationStartType = ANIMATION_START_TYPE_BATTLE_WAVE时有效。
	 * </pre>
	 */
	public int waveId;


	public Animation(int animationId,int animationResId,int animationTargetId,
			byte animationStartType,int battlefieldSerialNumber,int waveId) {
		this.animationId = animationId;
		this.animationResId = animationResId;
		this.animationStartType = animationStartType;
		this.animationTargetId = animationTargetId;
		this.battlefieldSerialNumber = battlefieldSerialNumber;
		this.waveId = waveId;
	}

}
