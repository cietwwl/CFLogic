package com.kola.kmp.logic.gang.war;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.pet.Pet;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.KGameCheatCenter.CheatResult;
import com.kola.kmp.logic.combat.ICombatCommonResult;
import com.kola.kmp.logic.combat.ICombatGlobalCommonResult;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangMember;
import com.kola.kmp.logic.gang.war.GangWarRaceMapAndPKCenter.GangRacePVECenter.RacePVEBoss;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace.NotifyPKResult;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace.RaceGangData;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace.RaceGangData.RaceMemberData;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace.RaceGangData.RaceMemberData.MemStatusEnum;
import com.kola.kmp.logic.gang.war.GangWarStatusManager.WarTime;
import com.kola.kmp.logic.gang.war.message.KGWPushMsg;
import com.kola.kmp.logic.gang.war.message.KGWRaceSynMsg;
import com.kola.kmp.logic.map.duplicatemap.CollisionEventObjectData;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMapBornPoint;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.rank.gang.GangRank;
import com.kola.kmp.logic.rank.gang.GangRankElementWarSignUp;
import com.kola.kmp.logic.rank.gang.KGangRankLogic;
import com.kola.kmp.logic.rank.gang.KGangRankTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.KGameUtilTool;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.GangTips;
import com.kola.kmp.logic.util.tips.GangWarTips;
import com.kola.kmp.protocol.gang.war.KGangWarProtocol;

/**
 * <pre>
 *  军团战管理器
 *  异常处理基本规则：
 *  1.报名不足8个军团则本周军团战取消，全部退还报名费用（有优化需求和空间）
 *  ----可以"轮空"，继续进行军团战流程
 *  ----若参战军团数量不足时，则优先填充每组存在一个军团，然后再分配多余的军团进行军团战匹配，例如当前有5个军团，则优先将前4名分别放置到4个组内，第5个与第1个进行战斗，其他军团轮空
 *  2.军团解散或增减成员，不影响报名和开战（按规则，入围的已解散的军团会战败，不入围的会退还报名费（实际上军团不存在退不了））
 *  ----对战中的入围军团若已解散，则会战败
 *  3.单场对战期间维护，则由后台手工清理军团战相关数据和填写赔偿文件
 *  ----已经破坏军团战流程，直接赔偿、清理现场、重新开始军团战
 *  4.不同单场中间休息时段维护，开服后可以继续进行军团战
 *  
 *  
 *  单场进入、开始、结束时均应该同步客户端数据
 *  NPC生成积分
 *  PVE,PVP 结果通知
 *  奖励发放检查
 *  退出主场景(要记录)、进入主场景(要记录)
 *  进入挑战关卡(要记录)、要记录是否战斗中（禁止PVP）、
 *  完善 PVP 结果处理
 *  复活流程
 *  主界面入口ICON
 *  WarStatusManager 各状态切换时的TODO行为
 *  各环节通知完善
 *  军团福利（获取、失去、领奖）
 *  特殊情况下分组策略：军团不足8个时的处理---轮空实现
 *  地图初始化：服务器启动，对阵地图（含NPC，出生点），战斗关卡
 *  测试指令+逻辑实现
 *  地图切换：主城<->对阵地图<-->关卡
 *  --单场结束时，清理主场景，清理PVE、PVP
 *  PVE,PVP关闭客户端，会有战斗结果通知
 *  角色上线，会纠正错误的成员状态
 *  所有分组结束，则本场提前结束
 *  
 *  CTODO 数据保存，开服恢复，赔偿--------------不支持开战期间停服维护，若要停服维护，则用GM指令赔偿军团资金或福利
 *  
 *  @author CamusHuang
 *  @creation 2013-7-8 下午10:52:10
 * </pre>
 */
public class GangWarLogic implements KGangWarProtocol {

	public static final Logger GangWarLogger = KGameLogger.getLogger("gangWar");

	// ////读写锁
	static final ReentrantLock lock = new ReentrantLock();

	/**
	 * <pre>
	 * 停止当前军团战、重新加载配置，且重新开启军团战
	 * 不允许在军团战在进入、开战状态下进行重启操作
	 * 
	 * @deprecated 一般在测试环境中使用GM指令调用
	 * @author CamusHuang
	 * @creation 2013-10-11 下午10:57:08
	 * </pre>
	 */
	public static CheatResult stopAndRestartGangWarByGM(boolean isReloadXmlConfig, boolean isForce) {
		CheatResult result = null; 
				
		// 保存数据
		GangWarDataCenter.saveData("-" + UtilTool.DATE_FORMAT7.format(new Date()) + "-动态重启军团战前备份");

		GangWarLogic.GangWarLogger.warn("军团战：开始重启...");
		lock.lock();
		try {
			// 重新加载配置
			if (isReloadXmlConfig) {
				GangWarLogic.GangWarLogger.warn("军团战：>>>>>> 重新加载配置");
				try {
					KGangWarConfig.init(null);
				} catch (KGameServerException e) {
					result = new CheatResult();
					result.tips = e.getMessage();
					return result;
				}
			}

			// 停止当前军团战
			GangWarLogic.GangWarLogger.warn("军团战：>>>>>> 停止当前军团战");
			result = GangWarStatusManager.stopGangWar(isForce);
			if (!result.isSuccess) {
				return result;
			}

			// 初始化军团战的状态
			GangWarLogic.GangWarLogger.warn("军团战：>>>>>> 重启军团战");
			GangWarStatusManager.notifyCacheLoadComplete();

			result = new CheatResult();
			result.isSuccess = true;
			return result;
		} finally {
			lock.unlock();
			
			if(result.isSuccess){
				KGWPushMsg.syncConstance();
			}
		}
	}
	
	/**
	 * <pre>
	 * 停止当前军团战、重新加载配置，且重新开启军团战
	 * 不允许在军团战在进入、开战状态下进行重启操作
	 * 
	 * @deprecated 一般在测试环境中使用GM指令调用
	 * @author CamusHuang
	 * @creation 2013-10-11 下午10:57:08
	 * </pre>
	 */
	public static CheatResult startGangWarByGM(long signupEndTime) {
		
		if(signupEndTime<System.currentTimeMillis()+Timer.ONE_MINUTE){
			CheatResult result = new CheatResult();
			result.tips = "指定时间预留不足，请重新输入。";
			return result;
		}
		
		CheatResult result = null;

		// 保存数据
		GangWarDataCenter.saveData("-" + UtilTool.DATE_FORMAT7.format(new Date()) + "-动态重启军团战前备份");

		GangWarLogic.GangWarLogger.warn("军团战：开始重启...");
		lock.lock();
		try {
			// 重新加载配置
			{
				GangWarLogic.GangWarLogger.warn("军团战：>>>>>> 重新配置时间点");
				try {
					KGangWarConfig.init(signupEndTime);
				} catch (Exception e) {
					result = new CheatResult();
					result.tips = e.getMessage();
					return result;
				}
			}
						
			// 停止当前军团战
			GangWarLogic.GangWarLogger.warn("军团战：>>>>>> 停止当前军团战");
			result = GangWarStatusManager.stopGangWar(true);
			if (!result.isSuccess) {
				return result;
			}

			// 初始化军团战的状态
			GangWarLogic.GangWarLogger.warn("军团战：>>>>>> 重启军团战");
			GangWarStatusManager.notifyCacheLoadComplete();

			result = new CheatResult();
			result.isSuccess = true;
			return result;
		} finally {
			lock.unlock();
			
			if(result.isSuccess){
				KGWPushMsg.syncConstance();
			}
		}
	}	

	/**
	 * <pre>
	 * 是否能报名
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-18 下午12:15:34
	 * </pre>
	 */
	public static boolean isCanSignUp() {
		return GangWarStatusManager.getNowStatus() == GangWarStatusEnum.SIGNUP_START_NOW;
	}

	/**
	 * <pre>
	 * 双方成员等级混合后取前10求平均
	 * 
	 * @param gangIdA
	 * @param gangIdB
	 * @return
	 * @author CamusHuang
	 * @creation 2014-6-5 下午8:08:47
	 * </pre>
	 */
	static int avgGangMemRoleLv(long gangIdA, long gangIdB) {
		List<Integer> lvs = new ArrayList<Integer>();

		RoleModuleSupport support = KSupportFactory.getRoleModuleSupport();
		{
			KGang gang = KSupportFactory.getGangSupport().getGang(gangIdA);
			if (gang != null) {
				for (long roleId : gang.getAllElementRoleIds()) {
					lvs.add(support.getLevel(roleId));
				}
			}
		}
		{
			KGang gang = KSupportFactory.getGangSupport().getGang(gangIdB);
			if (gang != null) {
				for (long roleId : gang.getAllElementRoleIds()) {
					lvs.add(support.getLevel(roleId));
				}
			}
		}

		Collections.sort(lvs, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				if (o1 > o2) {
					return -1;
				}
				if (o1 < o2) {
					return 1;
				}
				return 0;
			}
		});
		if (lvs.isEmpty()) {
			return 1;
		}
		int toIndex = Math.min(KGangWarConfig.getInstance().BoosAvgRoleLvNum, lvs.size());
		lvs = lvs.subList(0, toIndex);
		int allLv = 0;
		for (int lv : lvs) {
			allLv += lv;
		}
		return allLv / lvs.size();
	}

	/**
	 * <pre>
	 * 报名
	 * 
	 * @param roleId
	 * @param addResource
	 * @return
	 * @author CamusHuang
	 * @creation 2013-9-20 下午12:40:32
	 * </pre>
	 */
	public static CommonResult dealMsg_signUp(KRole role) {
		long roleId = role.getId();
		CommonResult result = new CommonResult();

		KGang gang = KSupportFactory.getGangSupport().getGangByRoleId(roleId);
		if (gang == null) {
			result.tips = GangTips.对不起您不属于任何军团;
			return result;
		}

		lock.lock();
		try {
			KGangMember mem = gang.getMember(roleId);
			if (mem == null || !mem.isSirs()) {
				result.tips = GangTips.只有团长和副团长才有此权限;
				return result;
			}

			//
			if (!isCanSignUp()) {
				WarTime warTime = GangWarStatusManager.getWarTime();
				result.tips = StringUtil.format(GangWarTips.军团战报名时间x至时间x, KGameUtilTool.genTimeStrForClient(warTime.signUpStartTime), KGameUtilTool.genTimeStrForClient(warTime.signUpEndTime));
				return result;
			}

			GangRank<GangRankElementWarSignUp> rank = KGangRankLogic.getRank(KGangRankTypeEnum.军团战报名);
			//
			GangRankElementWarSignUp element = rank.getTempCacheData().getElement(gang.getId());
			if (element != null) {
				result.tips = GangWarTips.你的军团已经报名了;
				return result;
			}
			//
			KGangRankLogic.notifyGangWarSignUp(gang, gang.getFlourish());

			result.tips = StringUtil.format(GangWarTips.报名成功报名结果将于x时间公布, KGameUtilTool.genTimeStrForClient(GangWarStatusManager.getWarTime().signUpEndTime));
			result.isSucess = true;
			return result;
		} finally {
			lock.unlock();

			if (result.isSucess) {
				// 即时刷新排行榜
				KGangRankLogic.getRank(KGangRankTypeEnum.军团战报名).onTimeSignalForPublish(true, false, false);
			}
		}
	}

	public static void dealMsg_confrimPKResult(KRole role) {
		long roleId = role.getId();

		GangWarLogic.GangWarLogger.warn("军团战：角色（{}:{}）确认战斗结算", role.getId(), role.getName());

		boolean isSuccess = false;

		GangRace race = null;
		RaceGangData gangData = null;
		//
		try {
			long gangId = KSupportFactory.getGangSupport().getGangIdByRoleId(roleId);
			if (gangId < 1) {
				return;// 无军团
			}

			lock.lock();
			try {
				GangData warGang = GangWarDataCenter.getWarGang(gangId);
				if (warGang == null) {
					return;// 未入围
				}

				GangWarRound roundData = GangWarDataCenter.getNearRoundData();
				if (roundData == null) {
					return;// 非开战中
				}

				race = roundData.getRaceByGangId(gangId);
				if (race == null) {
					return;// 无分组
				}

				gangData = race.getRaceGang(gangId);
				if (gangData == null) {
					return;// 无分组
				}

				RaceMemberData mem = gangData.getMem(roleId);
				if (mem == null) {
					return;// 未曾进入
				}

				KSupportFactory.getMapSupport().processRoleFinishBattleReturnToMap(role);
				gangData.setMemStatus(roleId, MemStatusEnum.IN);// 设置成员状态

				isSuccess = true;
			} finally {
				lock.unlock();
			}
		} finally {
			// 同步战场数据
			KGWRaceSynMsg.syncRaceInfo(race);

			if (!isSuccess) {
				// 无条件跳转到游戏地图
				rebackToGameWorld(roleId, race, null);
			}
		}
	}

	/**
	 * <pre>
	 * 请求进入军团战主场景
	 * GangWarLogic.GangWarLogger.warn("军团战：军团{}:角色{}进入主场景，处理结果：{}", gangId, roleId, result.tips);
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2013-9-20 下午12:38:15
	 * </pre>
	 */
	public static CommonResult dealMsg_joinRace(KRole role) {
		long roleId = role.getId();
		CommonResult result = new CommonResult();

		long gangId = KSupportFactory.getGangSupport().getGangIdByRoleId(roleId);
		if (gangId < 1) {
			result.tips = GangTips.对不起您不属于任何军团;
			return result;
		}

		GangRace race = null;

		// 军团是否已报名？是否已开战？是否进入对战？
		lock.lock();
		try {

			// 可入状态
			if (!GangWarStatusManager.isCanJoinMap()) {
				long nextJoinTime = GangWarStatusManager.下一场开启进入时间();
				if (nextJoinTime > 0) {
					result.tips = StringUtil.format(GangWarTips.下一场军团战将于x时间开启进入, KGameUtilTool.genTimeStrForClient(nextJoinTime));
					return result;
				}
			}

			// 是否入围
			GangData gangData = GangWarDataCenter.getWarGang(gangId);
			if (gangData == null) {
				result.tips = GangWarTips.你的军团未入围本周军团战;
				return result;
			}

			GangWarRound roundData = GangWarDataCenter.getNowRoundData();
			race = roundData.getRaceByGangId(gangId);
			// 是否胜负已分？
			if (race.getWinner() != null) {
				result.tips = GangWarTips.你所属的分组胜负已分;
				return result;
			}

			//
			RaceGangData raceGangData = race.getRaceGang(gangId);
			// 判断角色状态：任何情况下均允许进场，只需要保证模块内状态正确即可

			// 进入主场景
			String errorTips = joinRaceMap(true, roleId, race, raceGangData);
			if (errorTips != null) {
				result.tips = errorTips;
				return result;
			}
			raceGangData.getMemOrNew(roleId);
			raceGangData.setMemStatus(roleId, MemStatusEnum.IN);// 设置成员状态
			//
			// result.tips = GangWarTips.欢迎进入军团战场景;
			result.isSucess = true;
			return result;
		} finally {
			lock.unlock();

			if (result.isSucess) {

				// 同步战场数据
				KGWRaceSynMsg.syncRaceInit(role, race, gangId);
				KGWRaceSynMsg.syncRaceInfo(race);
			}
		}
	}

	/**
	 * <pre>
	 * 无条件传入主场景
	 * 
	 * 从游戏地图传入，从PVP/PVE返回
	 * 
	 * @param isJoin 是否从主城传入
	 * @param roleId
	 * @param warGroup 不可为NULL
	 * @param gangData 不可为NULL
	 * @param tips 例如：免费复活的提示；可为NULL
	 * @return null表示正常传入军团战主场景
	 * @author CamusHuang
	 * @creation 2013-9-20 上午11:21:51
	 * </pre>
	 */
	private static String joinRaceMap(boolean isJoin, long roleId, GangRace race, RaceGangData gangData) {

		if (race == null || gangData == null) {
			return GangWarTips.状态有误请重新登陆;
		}

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		// 飞入战场，在不同的出生点出生
		KDuplicateMapBornPoint point = race.MapAndPKCenter.getMapBornPoint(gangData.gangId);
		KActionResult result = KSupportFactory.getDuplicateMapSupport().playerRoleJoinDuplicateMap(role, race.MapAndPKCenter.getWarMap().getDuplicateId(), point._corX, point._corY);
		if (!result.success) {
			return result.tips;
		}
		return null;
	}

	/**
	 * <pre>
	 * 玩家主动请求离开主场景（菜单、离线），返回游戏地图
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2013-9-20 下午12:38:15
	 * </pre>
	 */
	public static void dealMsg_leaveRace(KRole role, boolean isBackToGameMap) {
		long roleId = role.getId();

		GangRace race = null;
		RaceGangData gangData = null;
		//
		try {
			long gangId = KSupportFactory.getGangSupport().getGangIdByRoleId(roleId);
			if (gangId < 1) {
				return;// 无军团
			}

			lock.lock();
			try {
				GangData warGang = GangWarDataCenter.getWarGang(gangId);
				if (warGang == null) {
					return;// 未入围
				}

				GangWarRound roundData = GangWarDataCenter.getNearRoundData();
				if (roundData == null) {
					return;// 非开战中
				}

				race = roundData.getRaceByGangId(gangId);
				if (race == null) {
					return;// 无分组
				}

				gangData = race.getRaceGang(gangId);
				if (gangData == null) {
					return;// 无分组
				}

				RaceMemberData mem = gangData.getMem(roleId);
				if (mem == null) {
					return;// 未曾进入
				}

				gangData.setMemStatus(roleId, MemStatusEnum.OUT);// 设置成员状态
			} finally {
				lock.unlock();
			}
		} finally {
			// 无条件跳转到游戏地图
			if (isBackToGameMap) {
				GangWarLogic.GangWarLogger.info("军团战：角色（{}:{}）请求离开主场景", role.getId(), role.getName());
				rebackToGameWorld(roleId, race, null);
				KDialogService.sendNullDialog(role);
			}

			// 同步战场数据
			KGWRaceSynMsg.syncRaceInfo(race);
		}
	}

	/**
	 * <pre>
	 * 无条件离开主场景，返回游戏地图
	 * 此行为应该保证100%成功返回游戏地图
	 * 
	 * 可以是主动离场或离线
	 * 不负责结束PVP\PVE
	 * 
	 * @param roleId
	 * @param gangData 可为NULL
	 * @param tips 例如：免费复活的提示；可为NULL
	 * @return
	 * @author CamusHuang
	 * @creation 2013-9-20 上午11:27:03
	 * </pre>
	 */
	private static String rebackToGameWorld(long roleId, GangRace race, String tips) {

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);

		// 离开场景，返回游戏地图
		int dupMapId = race == null ? -1 : race.MapAndPKCenter.getWarMap().getDuplicateId();
		KActionResult result = KSupportFactory.getDuplicateMapSupport().playerRoleLeaveDuplicateMap(role, dupMapId);
		if (!result.success) {
			return result.tips;
		}
		KDialogService.sendUprisingDialog(roleId, tips);
		return null;
	}

	/**
	 * <pre>
	 * 请求PVP
	 * 
	 * @param role
	 * @param npcNum
	 * @author CamusHuang
	 * @creation 2013-9-18 下午7:51:59
	 * </pre>
	 */
	public static void tryToStartPVP(KRole role, KRole oppRole) {
		GangWarLogic.GangWarLogger.warn("PVP :"+role.getName()+" VS "+ oppRole.getName());
		
		if (role.isFighting()) {
			return;
		}
		
		long roleId = role.getId();
		long oppRoleId = oppRole.getId();
		
		// 本人
		if (roleId == oppRoleId) {
			return;
		}
		long gangId = KSupportFactory.getGangSupport().getGangIdByRoleId(roleId);
		if (gangId < 1) {
			return;
		}
		// 军团是否已报名？是否已开战？是否进入对战？
		lock.lock();
		try {
			// 可入状态
			// 可入状态
			if (GangWarStatusManager.getNowStatus() == GangWarStatusEnum.WAR_WAIT_NOW) {
				// result.tips = GangWarTips.本场军团战未开始;
				return;
			}
			if (GangWarStatusManager.getNowStatus() == GangWarStatusEnum.WAR_ROUND_READY_NOW) {
				// result.tips = GangWarTips.本场军团战准备中;
				return;
			}
			if (GangWarStatusManager.getNowStatus() != GangWarStatusEnum.WAR_ROUND_START_NOW) {
				// result.tips = GangWarTips.本场军团战已结束;
				return;
			}

			// 是否入围
			GangData gangData = GangWarDataCenter.getWarGang(gangId);
			if (gangData == null) {
				return;
			}

			GangWarRound warRound = GangWarDataCenter.getNowRoundData();
			if (warRound == null) {
				return;
			}
			GangRace gangRace = warRound.getRaceByGangId(gangId);
			// 是否胜负已分？
			if (gangRace.getWinner() != null) {
				return;
			}

			//
			RaceGangData raceGangData = gangRace.getRaceGang(gangId);
			// 判断角色状态
			RaceMemberData mem = raceGangData.getMem(roleId);
			if (mem == null || mem.getStatus() != MemStatusEnum.IN) {
				return;
			}
			
			long nowTime = System.currentTimeMillis();
			// PVP CD排除
			{
				if (nowTime < mem.getPVPCDEndTime()) {
					return;
				}
			}
			// 指定PVP CD排除
			{
				long excuteOppRoleId = nowTime > mem.getLastPVPCDEndTime() ? -1 : mem.getLastPVPRoleId();
				if (oppRoleId == excuteOppRoleId) {
					return;
				}
			}
			if (gotoPVPMap(role, gangRace, raceGangData, mem, oppRoleId)) {
				return;
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * <pre>
	 * 无条件进入PVP地图
	 * 
	 * @param role 发起切磋者
	 * @param oppRole 被切磋者
	 * @return
	 * @author CamusHuang
	 * @creation 2013-9-20 上午11:58:40
	 * </pre>
	 */
	private static boolean gotoPVPMap(KRole role, GangRace race, RaceGangData gangData, RaceMemberData mem, long oppRoleId) {

		long roleId = role.getId();

		// 错误身份
		long oppGangId = KSupportFactory.getGangSupport().getGangIdByRoleId(oppRoleId);
		if (oppGangId < 1) {
			return false;
		}
		// 自己人
		if (gangData.gangId == oppGangId) {
			return false;
		}
		// 错误身份
		RaceGangData oppGangData = race.getRaceGang(oppGangId);
		if (oppGangData == null) {
			return false;
		}

		RaceMemberData oppMem = oppGangData.getMem(oppRoleId);
		// 错误状态
		if (oppMem == null || oppMem.getStatus() != MemStatusEnum.IN) {
			return false;
		}

		KRole oppRole = KSupportFactory.getRoleModuleSupport().getRole(oppRoleId);
		// 错误身份
		if (oppRole == null || !oppRole.isOnline()) {
			return true;
		}
		
		GangWarLogic.GangWarLogger.warn("军团战：PVP开战：角色{} vs 角色{}！", role.getName(), oppRoleId);

		{// 本人打对方AI
			GangWarPVPAtt gwAtt = new GangWarPVPAtt(race.roundId, gangData.gangId, oppRoleId);
			KActionResult<Integer> result = KSupportFactory.getCombatModuleSupport().fightWithAIWithTimeLimit(role, oppRoleId, KCombatType.GANG_WAR_PVP, GangWarDataCenter.PVPBattlefield, gwAtt, (int)KGangWarConfig.getInstance().PKMaxTime);
			if (!result.success) {
				return false;
			} else {
				// 记录参战
				gangData.setMemStatus(roleId, MemStatusEnum.WAR);// 设置成员状态
			}
		}
		{// 对方打本人AI
			GangWarPVPAtt gwAtt = new GangWarPVPAtt(race.roundId, oppGangData.gangId, roleId);
			KActionResult<Integer> result = KSupportFactory.getCombatModuleSupport().fightWithAIWithTimeLimit(oppRole, roleId, KCombatType.GANG_WAR_PVP, GangWarDataCenter.PVPBattlefield, gwAtt, (int)KGangWarConfig.getInstance().PKMaxTime);
			if (result.success) {
				// 记录参战
				oppGangData.setMemStatus(oppRoleId, MemStatusEnum.WAR);// 设置成员状态
			}
		}
		return true;
	}

	/**
	 * <pre>
	 * 外部通知：PVP战斗结果，出结果即时调用
	 * 
	 * @param winRoleId
	 * @param loseRoleId
	 * @author CamusHuang
	 * @creation 2013-9-18 下午9:50:51
	 * </pre>
	 */
	public static void notifyPVPBattleFinished(KRole role, ICombatCommonResult result) {
		GangWarPVPAtt gwAtt = (GangWarPVPAtt) result.getAttachment();

		boolean isWin = result.isWin();
		long releaseHP= result.getRoleCurrentHp();
		Pet pet = KSupportFactory.getPetModuleSupport().getFightingPet(role.getId());
		long petReleaseHP = result.getPetCurrentHp();
		long oppRoleId = 0;
		int maxWin = 0;
		int keepWin = 0;
		int addScore = 0;
		int totalScore = 0;

		GangRace gangRace = null;
		RaceMemberData mem = null;
		NotifyPKResult notifyResult = null;

		lock.lock();
		try {
			if (gwAtt == null) {
				return;
			}

			long gangId = gwAtt.gangId;
			oppRoleId = gwAtt.oppRoleId;

			GangWarLogic.GangWarLogger.warn("军团战：PVP结果({})通知：角色{} vs 角色{}！", (isWin ? "胜出" : "战败"), role.getId(), oppRoleId);
			//

			// 是否入围
			GangData warGang = GangWarDataCenter.getWarGang(gangId);
			if (warGang == null) {
				return;
			}

			// 是否对战期
			if (GangWarStatusManager.getNowStatus() != GangWarStatusEnum.WAR_ROUND_START_NOW) {
				return;
			}

			// 是否在本场对战中
			GangWarRound roundData = GangWarDataCenter.getNowRoundData();
			gangRace = roundData.getRaceByGangId(gangId);
			if (gangRace == null) {
				return;
			}

			// 积分计算
			addScore = ExpressionForPVPScore(isWin, role, oppRoleId);

			RaceGangData gangData = gangRace.getRaceGang(gangId);
			mem = gangData.getMem(role.getId());
			if (mem == null) {
				return;
			}
			mem.setReleaseHP(releaseHP, pet==null?-1:pet.getId(), petReleaseHP);
			int orgWinCount = mem.getWinCount();
			//
			notifyResult = gangRace.notifyPVPResult(gangId, role.getId(), oppRoleId, isWin, addScore);
			if (notifyResult.isSuccess) {
				if (isWin) {
					// 连杀播报
					int winCount = mem.getWinCount();
					GangWarSystemBrocast.onRoundRun_PKWin(gangRace, gangId, role, winCount);
				} else {
					// 连杀清0播报
					KRole oppRole = KSupportFactory.getRoleModuleSupport().getRole(oppRoleId);
					GangWarSystemBrocast.onRoundRun_KeepKillBeBreak(gangRace, gangId, role, oppRole.getExName(), orgWinCount);
				}
			} else {
				addScore = 0;
			}

			maxWin = mem.getMaxWinCount();
			keepWin = mem.getWinCount();
			totalScore = mem.getScore();
			
			if(result.isEscape()){
				// 由于PVP撤退时，没有进行客户端确认步骤，因此必须在此时就修正角色状态（如果不修正，则状态仍为战斗中，会出现状态错误）
				// PVP正确战败或胜出，都会有客户端确认步骤，因此留待客户端确认后修正状态（如果过早修正，则等待客户端确认过程中会被攻击等）
				gangData.setMemStatus(role.getId(), MemStatusEnum.IN);// 设置成员状态
			}

		} finally {
			lock.unlock();

			// 战斗结算奖励
			KGWRaceSynMsg.pushPVPResult(role, oppRoleId, isWin, maxWin, keepWin, addScore, totalScore);

			// 同步战场数据
			if (addScore > 0) {
				KGWRaceSynMsg.syncRaceInfo(gangRace);
			}

			if (notifyResult != null && notifyResult.isKillRankChange) {
				KGWRaceSynMsg.syncRaceKeepWin(gangRace);
			}
			
			// 战败则重置回出生点
			if(!isWin && gangRace!=null && mem!=null){
				KDuplicateMapBornPoint point = gangRace.MapAndPKCenter.getMapBornPoint(gwAtt.gangId);
				KSupportFactory.getDuplicateMapSupport().resetPlayerRoleToBornPoint(role, point);
			}
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
		int oppRoleLv = KSupportFactory.getRoleModuleSupport().getLevel(oppRoleId);
		int baseScore = KGangWarDataManager.mPVPScoreDataManager.getData(oppRoleLv).integral;
		// 1. 若 我方等级-敌方等级<=5 ，只获得基础积分
		// 2. 若 10>=我方等级-敌方等级>5 ，获得基础积分*0.5
		// 3. 若 20>=我方等级-敌方等级>10，获得基础积分*0.2
		// 4. 若 我方等级-敌方等级>20，获得1点积分
		int divLv = role.getLevel() - oppRoleLv;
		if (divLv <= 5) {
			return baseScore;
		}
		if (divLv <= 10) {
			return (int) (baseScore * 0.5);
		}
		if (divLv <= 20) {
			return (int) (baseScore * 0.2);
		}
		return 1;
	}

	static CommonResult startPVE(KRole role, CollisionEventObjectData bossMapObj) {
		GangWarLogic.GangWarLogger.info("PVE 碰撞:"+role.getName());
		
		CommonResult result = new CommonResult();
		
		if (role.isFighting()) {
			result.tips = GangWarTips.状态有误请重新登陆;
			return result;
		}

		long roleId = role.getId();
		long gangId = KSupportFactory.getGangSupport().getGangIdByRoleId(roleId);
		if (gangId < 1) {
			result.tips = GangWarTips.状态有误请重新登陆;
			return result;
		}

		{
			RacePVEBoss boss = ((RacePVEBoss) bossMapObj.getAttachment());

			if (boss.gangId == gangId) {
				// 我方BOSS
				return result;
			}
		}

		// 军团是否已报名？是否已开战？是否进入对战？
		lock.lock();
		try {
			// 可入状态
			if (GangWarStatusManager.getNowStatus() == GangWarStatusEnum.WAR_WAIT_NOW) {
				result.tips = GangWarTips.本场军团战未开始;
				return result;
			}
			if (GangWarStatusManager.getNowStatus() == GangWarStatusEnum.WAR_ROUND_READY_NOW) {
				result.tips = GangWarTips.本场军团战准备中;
				return result;
			}
			if (GangWarStatusManager.getNowStatus() != GangWarStatusEnum.WAR_ROUND_START_NOW) {
				result.tips = GangWarTips.本场军团战已结束;
				return result;
			}

			// 是否入围
			GangData gangData = GangWarDataCenter.getWarGang(gangId);
			if (gangData == null) {
				result.tips = GangWarTips.你的军团未入围本周军团战;
				return result;
			}

			GangWarRound roundData = GangWarDataCenter.getNowRoundData();
			GangRace race = roundData.getRaceByGangId(gangId);
			// 是否胜负已分？
			if (race.getWinner() != null) {
				result.tips = GangWarTips.你所属的分组胜负已分;
				return result;
			}

			RaceGangData oppGangData = race.getOppRaceGang(gangId);
			RacePVEBoss oppBoss = race.MapAndPKCenter.getRacePVEBoss(oppGangData.gangId);
			if (oppBoss == null) {
				result.tips = GangWarTips.你所属的分组胜负已分;
				return result;
			}

			//
			RaceGangData raceGangData = race.getRaceGang(gangId);
			// 判断角色状态
			RaceMemberData mem = raceGangData.getMem(roleId);
			if (mem == null) {
				result.tips = GangWarTips.状态有误请重新登陆;
				return result;
			}
			if (mem.getStatus() == MemStatusEnum.REVIVE) {
				result.tips = null;
				return result;
			}
			
			if (mem.getStatus() != MemStatusEnum.IN) {
				result.tips = GangWarTips.状态有误请重新登陆;
				return result;
			}

			{// 本人打对方BOSS
				GangWarPVEAtt gwAtt = new GangWarPVEAtt(race.roundId, gangData.gangId, oppGangData.gangId);
				KActionResult<Integer> result2 = race.MapAndPKCenter.PVECenter.fightWithMonster(role, oppBoss, gwAtt);
				if (!result2.success) {
					result.tips = result2.tips;
					return result;
				} else {
					// 记录参战
					raceGangData.setMemStatus(roleId, MemStatusEnum.WAR);// 设置成员状态
					
					GangWarLogic.GangWarLogger.warn("军团战：PVE开战：角色{}！", role.getName());
				}
			}

			return result;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * <pre>
	 * 外部通知：PVE战斗结果，出结果即时调用
	 * 
	 * @param roleId
	 * @param roleResult
	 * @param globalResult
	 * @author CamusHuang
	 * @creation 2014-6-5 下午6:39:53
	 * </pre>
	 */
	public static void notifyPVEBattleFinished(long roleId, ICombatCommonResult roleResult, ICombatGlobalCommonResult globalResult) {

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		if (role == null) {
			return;
		}
		
		if(roleResult.isEscape()){
			KDialogService.sendNullDialog(role);
		}

		boolean isWin = roleResult.isWin();
		long releaseHP= roleResult.getRoleCurrentHp();
		Pet pet = KSupportFactory.getPetModuleSupport().getFightingPet(role.getId());
		long petReleaseHP = roleResult.getPetCurrentHp();
		long killHP = 0;
		int addScore = 0;
		//
		int maxWin = 0;
		int keepWin = 0;
		int totalScore = 0;
		//
		NotifyPKResult notifyResult = null;

		GangWarPVEAtt gwAtt = (GangWarPVEAtt) roleResult.getAttachment();
		GangRace gangRace = null;
		RaceMemberData mem = null;
		try {
			if (gwAtt == null) {
				return;
			}

			long gangId = gwAtt.myGangId;

			GangWarLogic.GangWarLogger.warn("军团战：PVE结果({})通知：角色{}！", (isWin ? "胜出" : "战败"), roleId);
			//

			// 是否入围
			GangData warGang = GangWarDataCenter.getWarGang(gangId);
			if (warGang == null) {
				return;
			}

			// 是否对战期
			if (GangWarStatusManager.getNowStatus() != GangWarStatusEnum.WAR_ROUND_START_NOW) {
				return;
			}

			// 是否在本场对战中
			GangWarRound roundData = GangWarDataCenter.getNowRoundData();
			gangRace = roundData.getRaceByGangId(gangId);
			if (gangRace == null) {
				return;
			}

			RaceGangData gangData = gangRace.getRaceGang(gangId);
			mem = gangData.getMem(roleId);
			if (mem == null) {
				return;
			}
			mem.setReleaseHP(releaseHP, pet==null?-1:pet.getId(), petReleaseHP);
			
			// 此次PVE对怪物产生的伤害值
			killHP = gangRace.MapAndPKCenter.PVECenter.processCombatFinished(roleId, roleResult, globalResult, gwAtt);
			// 积分计算
			int monstValue = gangRace.MapAndPKCenter.getBossData().HarmIntegral;
			addScore = ExpressionForPVEScore(isWin, roleResult.isEscape(), killHP, monstValue);

			int orgWinCount = mem.getWinCount();
			notifyResult = gangRace.notifyPVEResult(gangId, roleId, isWin, addScore);
			if (notifyResult.isSuccess) {
				if (isWin) {
					// 系统通知
					int winCount = mem.getWinCount();
					GangWarSystemBrocast.onRoundRun_PKWin(gangRace, gangId, role, winCount);
				} else {
					// 连杀清0播报
					GangWarSystemBrocast.onRoundRun_KeepKillBeBreak(gangRace, gangId, role, HyperTextTool.extRoleName(GangWarTips.对方军团BOSS), orgWinCount);
				}
			} else {
				addScore = 0;
			}

			maxWin = mem.getMaxWinCount();
			keepWin = mem.getWinCount();
			totalScore = mem.getScore();
			
			// 由于PVE胜、败、撤退时，均会进行客户端确认步骤，因此留待客户端确认后修正状态（如果过早修正，则等待客户端确认过程中会被攻击等）

		} finally {
			// 战斗结算奖励
			KGWRaceSynMsg.pushPVEResult(role, isWin, maxWin, keepWin, addScore, totalScore, killHP);

			// 同步战场数据
			KGWRaceSynMsg.syncRaceInfo(gangRace);

			if (notifyResult != null && notifyResult.isKillRankChange) {
				KGWRaceSynMsg.syncRaceKeepWin(gangRace);
			}

			// 战败则重置回出生点
			if(!isWin && gangRace!=null && mem!=null){
				KDuplicateMapBornPoint point = gangRace.MapAndPKCenter.getMapBornPoint(gwAtt.myGangId);
				KSupportFactory.getDuplicateMapSupport().resetPlayerRoleToBornPoint(role, point);
			}
		}
	}

	/**
	 * <pre>
	 * PVE结果奖励积分公式
	 * 挑战Boss积分=80+Min（100,Int(单次伤害值/基础积分伤害值))
	 * 
	 * @param isWin
	 * @param oppRoleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-20 下午6:12:53
	 * </pre>
	 */
	private static int ExpressionForPVEScore(boolean isWin, boolean isEscape, long killHP, int monstValue) {
		if(!isWin && isEscape){
			// 逃跑
			return (int) Math.min(100, killHP / monstValue);
		}
		// 挑战Boss积分=80+Min（100,Int(单次伤害值/基础积分伤害值))
		return (int) (80 + Math.min(100, killHP / monstValue));
	}

	/**
	 * <pre>
	 * 用于对战中进行扫描，完成以下任务内容:
	 * 1.执行裁决
	 * 
	 * @return 是否所有对战均已决出胜负
	 * @author CamusHuang
	 * @creation 2013-9-24 下午9:16:23
	 * </pre>
	 */
	static boolean onTimeSignalForJudgeInRound() {
		lock.lock();
		try {
			if (GangWarStatusManager.getNowStatus() != GangWarStatusEnum.WAR_ROUND_START_NOW) {
				return true;
			}

			GangWarRound roundData = GangWarDataCenter.getNowRoundData();
			if (roundData == null) {
				return false;
			}

			// 是否所有分组均已决出胜负
			return roundData.judgeInRound();
		} finally {
			lock.unlock();
		}
	}

	static void onTimeSignalForWarErrorScan() {
		lock.lock();
		try {
			if (GangWarStatusManager.getNowStatus() != GangWarStatusEnum.WAR_ROUND_START_NOW) {
				return;
			}

			GangWarRound roundData = GangWarDataCenter.getNowRoundData();
			if (roundData == null) {
				return;
			}

			// CTODO 各种错误状态处理
		} finally {
			lock.unlock();
		}
	}

	/**
	 * <pre>
	 * 将指定军团的所有成员离场
	 * 
	 * @param gangData
	 * @author CamusHuang
	 * @creation 2013-9-27 下午5:14:25
	 * </pre>
	 */
	static void clearMemsAfterJudge(int round, GangRace warGroup, RaceGangData gangData) {
		for (RaceMemberData mem : gangData.getAllMems()) {
			switch (mem.getStatus()) {
			case OUT:
				break;
			case WAR:// 战斗结束后自然回到主城
				gangData.setMemStatus(mem.roleId, MemStatusEnum.OUT);
				break;
			case IN:
			case REVIVE:
				gangData.setMemStatus(mem.roleId, MemStatusEnum.OUT);
				rebackToGameWorld(mem.roleId, warGroup, StringUtil.format(GangWarTips.第x场军团战已结束, round));
				KDialogService.sendNullDialog(KSupportFactory.getRoleModuleSupport().getRole(mem.roleId));
				break;
			}
		}
	}

	static String getTitle(int rank) {
		switch (rank) {
		case 1:
			return GangWarTips.冠军;
		case 2:
			return GangWarTips.亚军;
		case 3:
			return GangWarTips.季军;
		default:
			return "";
		}
	}

	/**
	 * <pre>
	 * 第几场对应多少强
	 * 	晋级说明：
	 * 	第一场胜利为【16强】，失败为【32强】
	 * 	第二场胜利为【8强】，失败为【16强】
	 * 	第三场胜利为【4强】，失败为【8强】
	 * 	第四场胜利为【决赛】，失败为【4强】
	 * 	第五场胜利为【冠军】，失败为【决赛】
	 * 
	 * @param round
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-20 下午8:13:51
	 * </pre>
	 */
	static String getQiangFromRound(int round, boolean isWin) {
		int maxRound = KGangWarConfig.getInstance().MaxRound;
		int maxGangCount = KGangWarConfig.getInstance().MaxGangCount;
		if (round == maxRound) {
			return isWin ? GangWarTips.冠军 : GangWarTips.决赛;
		}
		if (round == maxRound - 1) {
			return isWin ? GangWarTips.决赛 : ((int) Math.pow(2, maxRound - round + 1)) + GangWarTips.强;
		}

		if (isWin) {
			return ((int) Math.pow(2, maxRound - round)) + GangWarTips.强;
		} else {
			return ((int) Math.pow(2, maxRound - round + 1)) + GangWarTips.强;
		}
	}

	/**
	 * <pre>
	 * PVP战斗时的附件对象
	 * 
	 * @author CamusHuang
	 * @creation 2014-5-14 下午12:23:29
	 * </pre>
	 */
	static class GangWarPVPAtt {
		final int round;
		final long gangId;
		final long oppRoleId;

		private GangWarPVPAtt(int round, long gangId, long oppRoleId) {
			this.round = round;
			this.gangId = gangId;
			this.oppRoleId = oppRoleId;
		}
	}

	/**
	 * <pre>
	 * PVE战斗时的附件对象
	 * 
	 * @author CamusHuang
	 * @creation 2014-5-14 下午12:23:29
	 * </pre>
	 */
	static class GangWarPVEAtt {
		final int round;
		final long myGangId;//我方军团ID
		final long oppGangId;//对方方军团ID
		long oppCurrentHp;//对方BOSS开战时的HP

		private GangWarPVEAtt(int round, long myGangId, long oppGangId) {
			this.round = round;
			this.myGangId = myGangId;
			this.oppGangId = oppGangId;
		}
	}

//	public static String dealGMOrder(KRole role, String[] args) {
//		try {
//			String cmd = args[1];
//			if (cmd.equalsIgnoreCase("signUp")) {
//				CommonResult result = GangWarLogic.dealMsg_signUp(role);
//				return result.tips;
//			}
//			if (cmd.equalsIgnoreCase("leaveRace")) {
//				GangWarLogic.dealMsg_leaveRace(role, true);
//				return "已执行";
//			}
//			if (cmd.equalsIgnoreCase("joinRace")) {
//				CommonResult result = GangWarLogic.dealMsg_joinRace(role);
//				return result.tips;
//			}
//			if (cmd.equalsIgnoreCase("pvp")) {
//				List<Long> oppRoleIds = KSupportFactory.getMapSupport().getAroundRoleIds(role);
//				if (oppRoleIds.isEmpty()) {
//					return "附近没有其它玩家";
//				}
//				KRole oppRole = KSupportFactory.getRoleModuleSupport().getRole(oppRoleIds.get(UtilTool.random(oppRoleIds.size())));
//				if (oppRole == null) {
//					return "附近没有其它玩家";
//				}
//				GangWarLogic.tryToStartPVP(role, oppRole);
//				return "已执行";
//			}
//			if (cmd.equalsIgnoreCase("signList")) {
//
//				KGameMessage backmsg = KGame.newLogicMessage(SM_GW_GET_SIGNUP_LIST_RESULT);
//				KGangWarMsgPackCenter.packSignUpList(backmsg, 10, 1, 2);
////				backmsg.trunOutMsgToInMsg();
//				StringBuffer sbf = new StringBuffer();
//				sbf.append("军团战报名表：");
//				int num = backmsg.readShort();
//				for (int i = 0; i < num; i++) {
//					sbf.append(backmsg.readShort()).append('-').append(backmsg.readUtf8String()).append('、');
//					backmsg.readByte();
//					backmsg.readUtf8String();
//					backmsg.readInt();
//					backmsg.readInt();
//				}
//				return sbf.toString();
//			}
//			if (cmd.equalsIgnoreCase("scoreRank")) {
//				KGameMessage backmsg = KGame.newLogicMessage(SM_GW_GET_SCORE_RANK_RESULT);
//				KGangWarMsgPackCenter.packScoreRank(backmsg, role, (byte) 20, (short) 1, (byte) 3);
////				backmsg.trunOutMsgToInMsg();
//				StringBuffer sbf = new StringBuffer();
//				sbf.append("军团积分表：");
//				int num = backmsg.readShort();
//				for (int i = 0; i < num; i++) {
//					sbf.append(backmsg.readShort()).append('-').append(backmsg.readUtf8String()).append('、');
//					backmsg.readByte();
//					backmsg.readUtf8String();
//					backmsg.readInt();
//					backmsg.readInt();
//				}
//				return sbf.toString();
//			}
//			return "指令不存在";
//			// return "执行成功";
//		} catch (Exception e) {
//			e.printStackTrace();
//			return e.getMessage();
//		}
//	}
}
