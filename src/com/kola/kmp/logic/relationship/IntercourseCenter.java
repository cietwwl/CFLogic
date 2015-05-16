package com.kola.kmp.logic.relationship;

import java.util.List;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.combat.ICombatCommonResult;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.relationship.message.KIntercourseMsg;
import com.kola.kmp.logic.relationship.message.KPushIntercourseData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.GangWarTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.RelationShipTips;

/**
 * <pre>
 * 负责切磋相关逻辑
 * 
 * @author CamusHuang
 * @creation 2014-12-3 下午2:53:55
 * </pre>
 */
public class IntercourseCenter {

	public static final Logger IntercourseLogger = KGameLogger.getLogger(IntercourseCenter.class);

	/**
	 * <pre>
	 * 状态
	 * 
	 * @author CamusHuang
	 * @creation 2013-9-24 下午5:30:56
	 * </pre>
	 */
	enum MemStatusEnum {
		IN(1, "主场景中"), WAR(3, "PVP或PVE中"), ;

		// 标识数值
		public final byte sign;
		// 名称
		public final String name;

		private MemStatusEnum(int sign, String name) {
			this.sign = (byte) sign;
			this.name = name;
		}
	}

	/**
	 * <pre>
	 * 请求PVP 
	 * 
	 * @param role
	 * @param oppRole
	 * @return
	 * @author CamusHuang
	 * @creation 2014-12-3 下午2:39:06
	 * </pre>
	 */
	public static CommonResult dealMsg_startPVP(KRole role, KRole oppRole) {

		CommonResult result = new CommonResult();
		
		if (role.isFighting()) {
			result.tips = GangWarTips.状态有误请重新登陆;
			return result;
		}

		long roleId = role.getId();
		long oppRoleId = oppRole.getId();

		// 本人
		if (oppRole.getId() == role.getId()) {
			result.tips = RelationShipTips.不能与自己切磋;
			return result;
		}

//		// 错误身份
//		if (!oppRole.isOnline()) {
//			result.tips = GlobalTips.角色不在线;
//			return result;
//		}

		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(role.getId());
		set.rwLock.lock();
		try {
			// 可入状态
			// 判断角色状态
			if (set.getPVPStatus() != MemStatusEnum.IN) {
				result.tips = GlobalTips.服务器繁忙请稍候再试;
				return result;
			}

			IntercourseLogger.warn("切磋：{} vs {}！", role.getName(), oppRole.getName());

			{// 本人打对方AI
				KActionResult<Integer> pvpPesult = KSupportFactory.getCombatModuleSupport().fightWithAI(role, oppRoleId, KCombatType.INTERCOURSE_PVP, KRelationShipDataManager.PVPBattlefield,
						oppRole.getId());
				if (!pvpPesult.success) {
					result.tips = pvpPesult.tips;
					return result;
				}

				// 记录参战
				set.setPVPStatus(MemStatusEnum.WAR);// 设置成员状态
				result.isSucess = true;
				return result;
			}
		} finally {
			set.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 外部通知：PVP战斗结果，出结果即时调用
	 * 
	 * @param role
	 * @param result
	 * @author CamusHuang
	 * @creation 2014-12-3 下午2:38:59
	 * </pre>
	 */
	public static void notifyPVPBattleFinished(KRole role, ICombatCommonResult result) {
		long oppRoleId = 0;
		int addScore = 0;
		int totalScore = 0;

		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(role.getId());
		set.rwLock.lock();
		try {
			oppRoleId = (Long) result.getAttachment();

			IntercourseLogger.warn("切磋：结果({})通知：角色{} vs 角色{}！", (result.isWin() ? "胜出" : "战败"), role.getId(), oppRoleId);
			//
			// 积分计算
			addScore = ExpressionForPVPScore(result.isWin(), role, oppRoleId);

			set.addPVPScore(addScore);
			totalScore = set.getPVPScore();

			set.setPVPStatus(MemStatusEnum.IN);// 设置成员状态
		} finally {
			set.rwLock.unlock();

			//
			KRelationShipSet oppSet = KRelationShipModuleExtension.getRelationShipSet(oppRoleId);
			if (oppSet != null) {
				// 被挑战的次数，以及胜利次数，失败次数。以及发起切磋人信息。
				oppSet.notifyPVPResult(!result.isWin(), role.getExName());
			}

			// 战斗结算奖励
			String tips = null;
			if (result.isWin()) {
				tips = StringUtil.format(RelationShipTips.切磋战胜加x积分, addScore);
			} else {
				tips = RelationShipTips.切磋战败提示;
			}
			KIntercourseMsg.pushPVPResult(role, result.isWin(), tips, totalScore);
		}
	}

	/**
	 * <pre>
	 * PVP结果奖励积分公式
	 * 
	 * @param isWin
	 * @param role
	 * @param oppRoleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-20 下午6:12:53
	 * </pre>
	 */
	private static int ExpressionForPVPScore(boolean isWin, KRole role, long oppRoleId) {
		if (!isWin) {
			return 0;
		}
		int myLv = role.getLevel();
		int oppRoleLv = KSupportFactory.getRoleModuleSupport().getLevel(oppRoleId);

		int N = 0;
		int 积分 = 0;
		if (oppRoleLv >= myLv) {
			N = Math.min(15, oppRoleLv - myLv);
			积分 = 20 + N;
		} else {
			N = Math.min(11, myLv - oppRoleLv);
			积分 = (int) (0.034 * Math.pow(N, 3) - 0.65 * Math.pow(N, 2) + 1.44 * N + 18.8);
		}
		return 积分;
	}

	public static void dealMsg_confrimPVPResult(KRole role) {
		IntercourseLogger.warn("切磋：角色（{}:{}）确认战斗结算", role.getId(), role.getName());

		KSupportFactory.getMapSupport().processRoleFinishBattleReturnToMap(role);
	}

	static void notifyForHourTask() {
		// 检查在线玩家的切磋数据，在线玩家进行战报推送并重置，不在线的进行保留
		for (long roleId : KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds()) {
			pushPVPData(roleId);
		}
	}
	
	static void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		long nowTime = System.currentTimeMillis();
		long lastTime = role.getLastLeaveGameTime();
		if(nowTime - lastTime >  Timer.ONE_HOUR){
			//离线超过1小时，推送PVP战报
			pushPVPData(role.getId());
		}
		
		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(role.getId());
		set.setPVPStatus(MemStatusEnum.IN);// 设置成员状态
	}

	private static void pushPVPData(long roleId) {
		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(roleId);
		set.rwLock.lock();
		try {
			int pvpCount = set.getPVPCount();
			if (pvpCount > 0) {
				int winCount = Math.min(set.getPVPWinCount(), pvpCount);
				List<String> names = set.getPVPNameList();
				//
				KPushIntercourseData.pushMsg(roleId, pvpCount, winCount, names);
				//
				set.clearPVPData();
			}
		} finally {
			set.rwLock.unlock();
		}
	}
	
	
}
