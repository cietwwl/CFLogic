package com.kola.kmp.logic.gang.war.message;


import javax.management.timer.Timer;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.gang.war.GangWarLogic;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace;
import com.kola.kmp.logic.gang.war.KGangWarConfig;
import com.kola.kmp.logic.gang.war.KGangWarMsgPackCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.util.tips.GangWarTips;
import com.kola.kmp.protocol.gang.war.KGangWarProtocol;

public class KGWRaceSynMsg implements KGangWarProtocol {

	/**
	 * <pre>
	 * 通知所有军团战场景内的玩家
	 * ----活动开始前最后9秒时，会在屏幕中间出现美术数字进行最后的倒数
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2013-8-29 上午9:45:54
	 * </pre>
	 */
	public static void sendCountDownMsg() {
		KGameMessage msg = KGame.newLogicMessage(SM_GW_PUSH_COUNT_DOWN);
		msg.writeInt(KGangWarConfig.getInstance().StartRoundCountDown);
		KGangWarMsgPackCenter.sendMsgToRoleInWarOfRound(msg);
	}

	/**
	 * <pre>
	 * 将军团对战地图内的信息同步给地图内所有玩家
	 * 
	 * @param race
	 * @author CamusHuang
	 * @creation 2014-5-23 下午4:59:11
	 * </pre>
	 */
	public static void syncRaceInit(KRole role, GangRace race, long gangId) {
		if (race == null || role ==null) {
			return;
		}
		KGameMessage msg = KGame.newLogicMessage(SM_GW_SYN_RACEINIT);
		KGangWarMsgPackCenter.packRaceInit(msg, role, race, gangId);

		role.sendMsg(msg);
	}

	/**
	 * <pre>
	 * 将军团对战地图内的信息同步给地图内所有玩家
	 * 人数、积分、BOSS血量
	 * 
	 * @param race
	 * @author CamusHuang
	 * @creation 2014-5-23 下午4:59:11
	 * </pre>
	 */
	public static void syncRaceInfo(GangRace race) {
		if (race == null) {
			return;
		}
		KGameMessage msg = KGame.newLogicMessage(SM_GW_SYN_RACEINFO);
		KGangWarMsgPackCenter.packRaceInfo(msg, race);
		KGangWarMsgPackCenter.sendMsgToRoleInWarOfRace(msg, race);
	}

	/**
	 * <pre>
	 * 军团战地图的连杀数据同步给地图内所有玩家
	 * 
	 * @param race
	 * @author CamusHuang
	 * @creation 2014-5-23 下午4:59:11
	 * </pre>
	 */
	public static void syncRaceKeepWin(GangRace race) {
		if (race == null) {
			return;
		}
		
		KGameMessage msg = KGame.newLogicMessage(SM_GW_SYN_RACE_KEEPWIN);
		KGangWarMsgPackCenter.packRaceKeepWin(msg, race);
		KGangWarMsgPackCenter.sendMsgToRoleInWarOfRace(msg, race);
	}

	/**
	 * <pre>
	 * 通知PVP战斗结果
	 * 
	 * @param role 角色
	 * @param oppRoleId 对方AI
	 * @param isWin 角色是否胜出
	 * @param maxWin 最大连杀
	 * @param keepWin 连杀
	 * @param addScore 加分
	 * @param totalScore 总分
	 * @author CamusHuang
	 * @creation 2014-6-4 下午4:21:40
	 * </pre>
	 */
	public static void pushPVPResult(KRole role, long oppRoleId, boolean isWin, int maxWin, int keepWin, int addScore, int totalScore) {
		pushPKResult(role, true, oppRoleId, isWin, maxWin, keepWin, addScore, totalScore, 0);
	}

	/**
	 * <pre>
	 * 通知PVE战斗结果
	 * 
	 * @param role 角色
	 * @param isWin 角色是否胜出
	 * @param maxWin 最大连杀
	 * @param keepWin 连杀
	 * @param addScore 加分
	 * @param totalScore 总分
	 * @author CamusHuang
	 * @creation 2014-6-4 下午4:21:40
	 * </pre>
	 */
	public static void pushPVEResult(KRole role, boolean isWin, int maxWin, int keepWin, int addScore, int totalScore, long pveKillHP) {
		pushPKResult(role, false, 0, isWin, maxWin, keepWin, addScore, totalScore, pveKillHP);
	}

	/**
	 * <pre>
	 * 通知PK战斗结果
	 * 
	 * @param role
	 * @param isWin
	 * @author CamusHuang
	 * @creation 2014-5-14 上午9:59:49
	 * </pre>
	 */
	private static void pushPKResult(KRole role, boolean isPVP, long oppRoleId, boolean isWin, int maxWin, int keepWin, int addScore, int totalScore, long pveKillHP) {
		/**
		 * <pre>
		 * 服务器通知客户端PK战斗结算界面消息
		 * 
		 * boolean 是否胜利b
		 * String tips（PVP胜利加X积分，PVP战败，PVE伤害X加X积分）
		 * 
		 * if(!胜利b){
		 * 	int 复活倒计时（S）
		 * }
		 * boolean true表示PVP，false表示PVE
		 * if(PVP){
		 * 	long 屏蔽对方角色ID（不在地图中显示对方、也不会发生碰撞）
		 * 	int 屏蔽倒计时（S）
		 * }
		 * int 获得积分
		 * int 当前积分
		 * short 我的最高连杀
		 * short 我的当前连杀
		 * </pre>
		 */
		KGameMessage msg = KGame.newLogicMessage(SM_GW_PUSH_PK_RESULT);
		msg.writeBoolean(isWin);
		
		//TIPS
		{
			String tips = null;//（PVP胜利加X积分，PVP战败，PVE伤害X加X积分）
			if(isPVP){
				if(isWin){
					tips = StringUtil.format(GangWarTips.PVP胜利加x积分结算提示, addScore);
				} else {
					tips = StringUtil.format(GangWarTips.PVP战败结算提示, addScore);
				}
			} else {
				tips = StringUtil.format(GangWarTips.PVE伤害x加x积分结算提示, pveKillHP, addScore);
			}
			
			msg.writeUtf8String(tips);
		}
		
		
		if (!isWin) {
			msg.writeInt((int) (KGangWarConfig.getInstance().DeadCDTime / Timer.ONE_SECOND));
		}
		msg.writeBoolean(isPVP);
		if (isPVP) {
			msg.writeLong(oppRoleId);
			msg.writeInt((int) (KGangWarConfig.getInstance().LastPVPCDTime / Timer.ONE_SECOND));
		}
		msg.writeInt(totalScore);
		msg.writeShort(maxWin);
		msg.writeShort(keepWin);
		role.sendMsg(msg);
		
		GangWarLogic.GangWarLogger.warn(StringUtil.format("角色{},加积分{},总积分{},当前连杀{},最大连杀{},伤害值{}", role.getName(), addScore, totalScore, keepWin, maxWin, pveKillHP));
	}
}
