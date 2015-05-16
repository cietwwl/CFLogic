package com.kola.kmp.logic.gang.war;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.management.timer.Timer;

import com.koala.game.KGameMessage;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.gang.GangIntegrateData;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangExtCASet;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.gang.KGangModuleExtension;
import com.kola.kmp.logic.gang.KGangMsgPackCenter;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace.RaceGangData;
import com.kola.kmp.logic.gang.war.GangWarStatusManager.WarTime;
import com.kola.kmp.logic.gang.war.GangWarTaskManager.BroadData;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.npc.message.KShowDialogMsg;
import com.kola.kmp.logic.other.KChatChannelTypeEnum;
import com.kola.kmp.logic.other.KColorFunEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KGameUtilTool;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.GangWarTips;

/**
 * <pre>
 * 负责处理各种行为的相关广播
 * 
 * @author CamusHuang
 * @creation 2013-9-19 上午10:45:58
 * </pre>
 */
public class GangWarSystemBrocast {
	/**
	 * <pre>
	 * 报名开始到第一场准备时段，世界播报
	 * 每3小时：每周五21:00开启军团战，快去报名吧，丰厚的奖励等着你哦！
	 * 
	 * @author CamusHuang
	 * @creation 2013-9-26 上午10:40:22
	 * </pre>
	 */
	static void onSignupStart() {
		// KSupportFactory.getChatSupport().sendSystemChat(GangWarTips.军团战报名开始,
		// KWordBroadcastType.军团战);
		GangWarLogic.GangWarLogger.warn(GangWarTips.军团战报名开始);

		int round = 1;
		//
		long nowTime = System.currentTimeMillis();
		long startTime = GangWarStatusManager.getWarTime().signUpStartTime + 10 * Timer.ONE_SECOND;
		long endTime = GangWarStatusManager.getWarTime().signUpEndTime;
		long period = KGangWarConfig.getInstance().SignupBroadcastPeroid;
		//
		List<BroadData> broadDataList = new ArrayList<BroadData>();
		//
		KWordBroadcastType _broacastType = KWordBroadcastType.军团战_军团战将于x时间开启快去报名吧;
		String info = StringUtil.format(_broacastType.content, KGameUtilTool.genTimeStrForClient(GangWarStatusManager.getWarTime().getTime_Start(round)));
		for (long sendTime = startTime + period; sendTime < endTime; sendTime += period) {
			if (nowTime < sendTime) {
				broadDataList.add(new BroadData(sendTime, info, _broacastType));
			}
		}
		GangWarTaskManager.WorldBroadcastTask.submitTask(0, broadDataList);
	}

	/**
	 * <pre>
	 * 报名结束时，世界播报
	 * 
	 * @author CamusHuang
	 * @creation 2013-9-25 下午9:01:53
	 * </pre>
	 */
	static void onSignupEnd() {
		List<GangData> list = GangWarDataCenter.getUnmodifyWarGangs();
		if (list.size() < 1) {
			return;
		}
		KWordBroadcastType _boradcastType = KWordBroadcastType.军团战_军团战入围军团已确认;
		KSupportFactory.getChatSupport().sendSystemChat(_boradcastType.content, _boradcastType);
	}

	/**
	 * <pre>
	 * 结束报名时，对入围的军团进行通报
	 * 团长邮件+军团频道+不在线成员私聊+军团日志
	 * 
	 * @param gangId
	 * @param minResource
	 * @param warResource
	 * @author CamusHuang
	 * @creation 2013-9-26 下午12:12:05
	 * </pre>
	 */
	static void onSignupEnd_JoinList(long gangId, long minResource, long warResource) {

		// 军团团长及副团长邮件+普通成员私聊：恭喜你的军团入围了本次军团战，本周第一轮军团将于本日21:00正式打响。
		GangIntegrateData gangData = KGangModuleExtension.getGangAndSet(gangId);
		if (gangData == null) {
			return;
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet gangExtSet = (KGangExtCASet) gangData.getGangExtCASet();
		//
		WarTime warTime = GangWarStatusManager.getWarTime();
		String startTime = UtilTool.DATE_FORMAT5.format(new Date(warTime.getTime_Start(1)));
		// 团长邮件
		String tips = StringUtil.format(GangWarTips.入围军团战第1场对战将于今天x时间正式打响, startTime);
		KGangMsgPackCenter.sendMailToSirs(gang, GangWarTips.军团战入围通知邮件标题, tips);
		// 军团频道
		KSupportFactory.getChatSupport().sendChatToAnyChannel(KChatChannelTypeEnum.军团, tips, gang.getId());
		// 不在线成员私聊
		KGangMsgPackCenter.sendPrivateChatToNotOnlineRoles(gang, tips);
		// 军团日志
		KGangLogic.addDialy(gang, gangExtSet, GangWarTips.入围军团战, true, true, false);
	}

	/**
	 * <pre>
	 * 结束报名时，对未入围的军团退还报名费用
	 * 团长邮件+军团频道+不在线成员私聊+军团日志
	 * 
	 * @param isSuccess 是否如常进行
	 * @param gangId
	 * @param minResource
	 * @param warResource
	 * @author CamusHuang
	 * @creation 2013-9-26 上午10:30:56
	 * </pre>
	 */
	static void onSignupEnd_OutList(boolean isSuccess, long gangId, long minResource, long warResource) {
		GangIntegrateData gangData = KGangModuleExtension.getGangAndSet(gangId);
		if (gangData == null) {
			return;
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet gangExtSet = (KGangExtCASet) gangData.getGangExtCASet();
		// 团长邮件
		String content = null;
		if (isSuccess) {
			content = GangWarTips.军团战入围失败邮件内容;
		} else {
			content = GangWarTips.军团战取消;
		}
		KGangMsgPackCenter.sendMailToSirs(gang, GangWarTips.军团战入围失败邮件标题, content);
		// 军团频道
		content = GangWarTips.很遗憾您的军团入围失败未获得参加军团战参赛资格;
		KSupportFactory.getChatSupport().sendChatToAnyChannel(KChatChannelTypeEnum.军团, content, gang.getId());
		// 不在线成员私聊
		KGangMsgPackCenter.sendPrivateChatToNotOnlineRoles(gang, content);
		// 军团日志
		KGangLogic.addDialy(gang, gangExtSet, StringUtil.format(GangWarTips.军团战入围失败邮件标题, warResource), true, true, false);
	}

	/**
	 * <pre>
	 * 等待期的相关播报
	 * 1.开战前定时世界广播
	 * 2.开战前对战地图内广播
	 * 3.开始前对战地图内10秒倒计时
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-28 下午8:40:15
	 * </pre>
	 */
	static void onRoundWait(int round) {

		GangWarLogic.GangWarLogger.warn(StringUtil.format(GangWarTips.军团战第x场将于x时间开始, round, UtilTool.DATE_FORMAT2.format(new Date(GangWarStatusManager.getWarTime().getTime_Start(round)))));

		long warStartTime = GangWarStatusManager.getWarTime().getTime_Start(round);
		//
		long nowTime = System.currentTimeMillis();
		if (nowTime < warStartTime) {
			// 世界广播:第X轮军团战将于X分钟后开始
			{
				List<BroadData> broadDataList = new ArrayList<BroadData>();
				{
					// 开战前60分钟~开战前20分钟：按每10分钟发送一次
					long period = 10 * Timer.ONE_MINUTE;
					long startTime = warStartTime - Timer.ONE_HOUR;
					long endTime = warStartTime - 20 * Timer.ONE_MINUTE;
					//
					KWordBroadcastType _boradcastType = KWordBroadcastType.军团战_第x场军团战将于x分钟后开启;
					for (long sendTime = startTime; sendTime < endTime; sendTime += period) {
						if (nowTime < sendTime) {
							int minute = (int) ((warStartTime - sendTime) / Timer.ONE_MINUTE);// N分钟后
							broadDataList.add(new BroadData(sendTime, StringUtil.format(_boradcastType.content, round, minute), _boradcastType));
						}
					}
				}
				{
					// 最后20分钟按定死的更小周期发送
					int[] times = new int[] { 20, 15, 10, 8, 6, 4, 3, 2, 1 };
					//
					KWordBroadcastType _boradcastType = KWordBroadcastType.军团战_第x场军团战将于x分钟后开启请各参战军团做好准备;
					for (int i = 0; i < times.length; i++) {
						long sendTime = warStartTime - times[i] * Timer.ONE_MINUTE;
						if (nowTime < sendTime) {
							broadDataList.add(new BroadData(sendTime, StringUtil.format(_boradcastType.content, round, times[i]), _boradcastType));
						}
					}
				}

				GangWarTaskManager.WorldBroadcastTask.submitTask(0, broadDataList);
			}
			// 战斗场景广播
			{
				List<BroadData> broadDataList = new ArrayList<BroadData>();
				int[] times = new int[] { 45, 30, 20, 15 };// 距开战前多少秒发送公告
				//
				KWordBroadcastType _boradcastType = KWordBroadcastType.军团战_第x场军团战将于x秒后开始;
				for (int i = 0; i < times.length; i++) {
					long sendTime = warStartTime - times[i] * Timer.ONE_SECOND;// 发送公告的时刻
					if (nowTime < sendTime) {
						broadDataList.add(new BroadData(sendTime, StringUtil.format(_boradcastType.content, round, times[i]), _boradcastType));
					}
				}
				GangWarTaskManager.RaceBroadcastTask.submitTask(broadDataList);
			}
			// 倒计时时效
			{
				long delayTime = KGangWarConfig.getInstance().StartRoundCountDown;// 10秒
				long runtime = warStartTime - delayTime * Timer.ONE_SECOND;// 触发的时刻
				if (nowTime < runtime) {
					GangWarTaskManager.StartCountdownTask.submitTask(runtime - nowTime);
				}
			}
		}
	}

	static void onRoundReady(int round) {
		KWordBroadcastType _boradcastType = KWordBroadcastType.军团战_第x场军团战开始入场;
		KSupportFactory.getChatSupport().sendSystemChat(StringUtil.format(_boradcastType.content, round), _boradcastType);
		GangWarLogic.GangWarLogger.warn(StringUtil.format(_boradcastType.content, round));
	}

	/**
	 * <pre>
	 * 1.发送公告
	 * 2.开场到本场结束时段，定时世界播报：呼叫未入场的成员
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-28 下午8:40:15
	 * </pre>
	 */
	static void onRoundStart(int round) {

		// 发送公告
		{
			KWordBroadcastType _boradcastType = KWordBroadcastType.军团战_第x场军团战现在开始;
			KSupportFactory.getChatSupport().sendSystemChat(StringUtil.format(_boradcastType.content, round), _boradcastType);
			GangWarLogic.GangWarLogger.warn(StringUtil.format(_boradcastType.content, round));
		}

		// 定时世界播报
		{
			long warStartTime = GangWarStatusManager.getWarTime().getTime_Start(round);
			//
			long nowTime = System.currentTimeMillis();
			// 世界广播:本场军团战正式开始，还未到场的参战人员请尽快入场
			{
				List<BroadData> broadDataList = new ArrayList<BroadData>();
				{
					// 开战后0分钟~开战后5分钟：按每1分钟发送一次
					long period = Timer.ONE_MINUTE;
					long startTime = warStartTime;
					long endTime = warStartTime + 5 * Timer.ONE_MINUTE;
					//
					KWordBroadcastType _boradcastType = KWordBroadcastType.军团战_第x场军团战正式开始请尽快入场;
					String tips = StringUtil.format(_boradcastType.content, round);
					for (long sendTime = startTime; sendTime < endTime; sendTime += period) {
						if (nowTime < sendTime) {
							broadDataList.add(new BroadData(sendTime, tips, _boradcastType));
						}
					}
				}

				GangWarTaskManager.WorldBroadcastTask.submitTask(0, broadDataList);
			}
		}
	}

	/**
	 * <pre>
	 * PK胜出时，战场景内世界播报
	 * 按连杀人数不同，对战场景内世界播报不同内容
	 * 
	 * @param gangId
	 * @param roleId
	 * @param winCount
	 * @author CamusHuang
	 * @creation 2013-9-19 上午11:09:16
	 * </pre>
	 */
	static void onRoundRun_PKWin(GangRace race, long gangId, KRole role, int winCount) {
		if (winCount < 1) {
			return;
		}
		KGang gang = KSupportFactory.getGangSupport().getGang(gangId);
		if (gang == null) {
			return;
		}

		//  连续击杀敌方成员数量超过5人时会在本场景内进行播报：“XXX已经连杀5人，勇猛无双”
		//  连续击杀敌方成员数量超过10人时会在本场景内进行播报：“XXX已经连杀10人，妖孽般的杀戮”
		//  连续击杀敌方成员数量超过20人时会在本场景内进行播报：“XXX已经连杀20人，如神一般”

		KWordBroadcastType _broadcastType = null;
		switch (winCount) {
		case 5:
			_broadcastType = KWordBroadcastType.军团战_x角色已经连杀5人;
			break;
		case 10:
			_broadcastType = KWordBroadcastType.军团战_x角色已经连杀10人;
			break;
		case 20:
			_broadcastType = KWordBroadcastType.军团战_x角色已经连杀20人;
			break;
		}

		if (_broadcastType != null) {
			String content = StringUtil.format(_broadcastType.content, role.getExName());
			KGameMessage msg = KSupportFactory.getChatSupport().genSystemChatMsg(content, _broadcastType);
			KGangWarMsgPackCenter.sendMsgToRoleInWarOfRace(msg, race);
			// 通知GM
			KSupportFactory.getGMSupport().onChat(KSupportFactory.getChatSupport().genSystemChatDataForGM(content, _broadcastType));
		}
	}

	/**
	 * <pre>
	 * 被杀出场时，军团内世界播报
	 * 
	 * @param gangId
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2013-9-19 上午11:09:47
	 * </pre>
	 */
	static void onRoundRun_KeepKillBeBreak(GangRace race, long gangId, KRole role, String oppRoleExtName, int orgWinCount) {
		if (orgWinCount < KGangWarConfig.getInstance().WinCountClearBroad) {
			return;
		}

		// 连续杀敌数量超过5人后被人击杀时：“XXX的连杀被YYY终结了”，表示XXX被YYY给击杀了
		KWordBroadcastType _broadcastType = KWordBroadcastType.军团战_x角色的连杀被x角色终结了;
		String content = StringUtil.format(_broadcastType.content, role.getExName(), oppRoleExtName);
		KGameMessage msg = KSupportFactory.getChatSupport().genSystemChatMsg(content, _broadcastType);
		KGangWarMsgPackCenter.sendMsgToRoleInWarOfRace(msg, race);
		// 通知GM
		KSupportFactory.getGMSupport().onChat(KSupportFactory.getChatSupport().genSystemChatDataForGM(content, _broadcastType));
	}

	/**
	 * <pre>
	 * 胜负通报
	 * 世界播报
	 * 团长邮件+军团频道+不在线成员私聊+军团日志
	 * 
	 * @param race
	 * @author CamusHuang
	 * @creation 2013-9-26 上午10:53:47
	 * </pre>
	 */
	static void onRaceEnd(int round, GangRace race) {
		
		if (race.gangDataA.gangId < 0 && race.gangDataB.gangId < 0) {
			return;
		}
		
		RaceGangData winFD = race.getWinner();
		RaceGangData loseFD = race.getOppRaceGang(winFD.gangId);
		// 世界播报：当某个军团在单场获胜时：经过一场鏖战，XXX军团艰难的战胜XXX军团。让我们恭喜XXX军团！;
		{
			if (winFD.gangId > 0 || loseFD.gangId > 0) {
				KWordBroadcastType _boradcastType = KWordBroadcastType.军团战_第x场军团战军团x战胜军团x恭喜军团x;
				String tips = StringUtil.format(_boradcastType.content, round, winFD.gangData.extGangName,
						loseFD.gangData.extGangName, winFD.gangData.extGangName);
				KSupportFactory.getChatSupport().sendSystemChat(tips, _boradcastType);
			}
		}

		// 军团频道：军团胜利方：在大家同心协力下，XXX军团被我们战胜了，大家一起鼓舞庆祝吧。（XXX为敌军军团名）;
		// 军团频道：军团失败方：很遗憾，XXX军团已微弱的优势战胜了我们，大家不要泄气，下次再努力吧（XXX为敌军军团名）;
		// ////////////
		// 胜出军团
		{
			GangIntegrateData gangData = KGangModuleExtension.getGangAndSet(winFD.gangId);
			if (gangData != null) {
				KGang gang = (KGang) gangData.getGang();
				KGangExtCASet gangExtSet = (KGangExtCASet) gangData.getGangExtCASet();
				// 团长邮件
				String title = StringUtil.format(GangWarTips.军团战第x场胜出邮件标题, round);
				String content = StringUtil.format(GangWarTips.军团战第x场战胜军团x邮件正文, round, loseFD.gangData.extGangName);
				KGangMsgPackCenter.sendMailToSirs(gang, title, content);
				// 军团频道
				KSupportFactory.getChatSupport().sendChatToAnyChannel(KChatChannelTypeEnum.军团, content, gang.getId());
				// 不在线成员私聊
				KGangMsgPackCenter.sendPrivateChatToNotOnlineRoles(gang, content);
				// 军团日志
				KGangLogic.addDialy(gang, gangExtSet, title, true, false, false);
			}
		}
		// ////////////
		// 战败军团
		{
			GangIntegrateData gangData = KGangModuleExtension.getGangAndSet(loseFD.gangId);
			if (gangData != null) {
				KGang gang = (KGang) gangData.getGang();
				KGangExtCASet gangExtSet = (KGangExtCASet) gangData.getGangExtCASet();
				// 团长邮件
				String title = StringUtil.format(GangWarTips.军团战第x场战败邮件标题, round);
				String content = StringUtil.format(GangWarTips.军团战第x场被军团x战胜邮件正文, round, winFD.gangData.extGangName);
				KGangMsgPackCenter.sendMailToSirs(gang, title, content);
				// 军团频道
				KSupportFactory.getChatSupport().sendChatToAnyChannel(KChatChannelTypeEnum.军团, content, gang.getId());
				// 不在线成员私聊
				KGangMsgPackCenter.sendPrivateChatToNotOnlineRoles(gang, content);
				// 军团日志
				KGangLogic.addDialy(gang, gangExtSet, title, true, false, false);
			}
		}
		
		// 在线成员弹窗口
		showRaceEndDialog(race, winFD, loseFD);
	}
	
	/**
	 * <pre>
	 * 单场对战结束时，显示对话框
	 * 
	 * @param winFD
	 * @param loseFD
	 * @author CamusHuang
	 * @creation 2014-11-26 下午6:06:15
	 * </pre>
	 */
	private static void showRaceEndDialog(GangRace race, RaceGangData winFD, RaceGangData loseFD){
		String content = null; 
		if(race.MapAndPKCenter.getRacePVEBoss(loseFD.gangId).isAlive()){
			// BOSS 未死
			content = GangWarTips.时长结束x军团x积分x军团x积分x军团胜出;
			content = StringUtil.format(content,loseFD.gangData.extGangName, loseFD.getScore(), winFD.gangData.extGangName,
					winFD.getScore(), winFD.gangData.extGangName);
		} else {
			// BOSS 死亡
			content = GangWarTips.中途结束x军团Boss被杀x军团胜出;
			content = StringUtil.format(content,loseFD.gangData.extGangName, winFD.gangData.extGangName);
		}
		
		KGameMessage msg = KShowDialogMsg.createSimpleDialogMsg(GangWarTips.战斗结束, content, true, (byte)-1);
		KGangMsgPackCenter.sendMsgToMemebers(msg, winFD.gangId);
		KGangMsgPackCenter.sendMsgToMemebers(msg, loseFD.gangId);
	}

	/**
	 * <pre>
	 * 单场结束后，周期世界播报本场结果
	 * 
	 * @author CamusHuang
	 * @creation 2013-9-25 下午9:53:19
	 * </pre>
	 */
	static void onRoundEnd(int round) {
		// 第x场军团战已结束;
		KWordBroadcastType _boradcastType = KWordBroadcastType.军团战_第x场军团战结束;
		KSupportFactory.getChatSupport().sendSystemChat(StringUtil.format(_boradcastType.content, round), _boradcastType);
		GangWarLogic.GangWarLogger.warn(StringUtil.format(_boradcastType.content, round));
	}

	/**
	 * <pre>
	 * 全部结束后，周期世界播报本周结果
	 * 
	 * @author CamusHuang
	 * @creation 2013-9-25 下午9:53:33
	 * </pre>
	 */
	static void onWarEnd_Win(GangData NO1) {

		if (NO1 == null) {
			return;
		}

		// 恭喜XX军团在本届军团战中以绝对实力夺得冠军桂冠！
		KWordBroadcastType _boradcastType = KWordBroadcastType.军团战_恭喜x军团在本周军团战中夺得冠军;
		KSupportFactory.getChatSupport().sendSystemChat(StringUtil.format(_boradcastType.content, NO1.extGangName), _boradcastType);
	}

	/**
	 * <pre>
	 * 全部结束后
	 * 
	 * @author CamusHuang
	 * @creation 2013-9-25 下午9:53:33
	 * </pre>
	 */
	static void onWarEnd() {
		KWordBroadcastType _boradcastType = KWordBroadcastType.军团战_本周军团战完满结束;
		KSupportFactory.getChatSupport().sendSystemChat(_boradcastType.content, _boradcastType);
		GangWarLogic.GangWarLogger.warn(_boradcastType.content);
	}
}
