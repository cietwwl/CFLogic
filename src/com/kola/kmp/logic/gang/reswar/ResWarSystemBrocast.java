package com.kola.kmp.logic.gang.reswar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.management.timer.Timer;

import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.gang.GangIntegrateData;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangExtCASet;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.gang.KGangModuleExtension;
import com.kola.kmp.logic.gang.KGangMsgPackCenter;
import com.kola.kmp.logic.gang.reswar.KResWarDataManager.KCityTempManager.CityTemplate;
import com.kola.kmp.logic.gang.reswar.ResWarCity.CityWarData.GangData;
import com.kola.kmp.logic.gang.reswar.ResWarStatusManager.WarTime;
import com.kola.kmp.logic.gang.reswar.ResWarTaskManager.BroadData;
import com.kola.kmp.logic.other.KChatChannelTypeEnum;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KGameUtilTool;
import com.kola.kmp.logic.util.tips.GangResWarTips;

/**
 * <pre>
 * 负责处理各种行为的相关广播
 * 
 * 参赛名单公布后即周日0点开始，会每隔1小时进行世界播报：“【资源争夺】竞价成功军团名单现已公布，请到活动界面进行查看”
 * 每次资源争夺战在开始前5分钟时会进行世界播报：“离本场军团资源争夺战开启剩余5分钟，请参与军团做好准备”
 * 当一个城市的争夺战结束时，系统会进行世界播报：“XXX军团在本次【资源争夺】活动中战胜XXX军团，成功夺得【XX城市】的控制权”
 * 
 * @author CamusHuang
 * @creation 2014-5-12 上午11:08:26
 * </pre>
 */
public class ResWarSystemBrocast {

	/**
	 * <pre>
	 * 竞价开始到准备时段，世界播报
	 * 每3小时：每周五21:00开启军团资源战，快去竞价吧，丰厚的奖励等着你哦！
	 * 
	 * @author CamusHuang
	 * @creation 2013-9-26 上午10:40:22
	 * </pre>
	 */
	static void onBidStart() {

		KWordBroadcastType _brocastType = KWordBroadcastType.资源战_军团资源战将于x时间开启快去竞价吧;
		String info = StringUtil.format(_brocastType.content, KGameUtilTool.genTimeStrForClient(ResWarStatusManager.getWarTime().warStartTime));
		//
		long nowTime = System.currentTimeMillis();
		long startTime = ResWarStatusManager.getWarTime().bidStartTime + 10 * Timer.ONE_SECOND;
		long endTime = ResWarStatusManager.getWarTime().warReadyTime;
		long period = KResWarConfig.BidBroadcastPeroid;
		//
		List<BroadData> broadDataList = new ArrayList<BroadData>();
		for (long sendTime = startTime + period; sendTime < endTime; sendTime += period) {
			if (nowTime < sendTime) {
				broadDataList.add(new BroadData(sendTime, info, _brocastType));
			}
		}
		ResWarTaskManager.WorldBroadcastTask.submitTask(0, broadDataList);
	}

	/**
	 * <pre>
	 * 竞价结束时，世界播报
	 * 
	 * @author CamusHuang
	 * @creation 2013-9-25 下午9:01:53
	 * </pre>
	 */
	static void onBidEnd() {
		KWordBroadcastType _broadcastType = KWordBroadcastType.资源战_军团资源战竞价成功军团名单现已公布;
		KSupportFactory.getChatSupport().sendSystemChat(_broadcastType.content, _broadcastType);
	}

	/**
	 * <pre>
	 * 结束竞价时，对入围的军团进行通报
	 * 军团长邮件+军团频道+不在线成员私聊+军团日志
	 * 
	 * @param gangId
	 * @param minResource
	 * @param warResource
	 * @author CamusHuang
	 * @creation 2013-9-26 下午12:12:05
	 * </pre>
	 */
	static void onBidEnd_JoinList(int cityId, long gangId) {

		// 军团长及副团长邮件：恭喜你的军团入围了本次军团资源战，对战将于x时间正式打响。
		// 普通成员私聊：恭喜您的军团入围了本次军团资源战，对战将于x时间正式打响。
		GangIntegrateData gangData = KGangModuleExtension.getGangAndSet(gangId);
		if (gangData == null) {
			return;
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet gangExtSet = (KGangExtCASet) gangData.getGangExtCASet();

		CityTemplate cityTemp = KResWarDataManager.mCityTempManager.getData(cityId);
		//
		WarTime warTime = ResWarStatusManager.getWarTime();
		String startTime = UtilTool.DATE_FORMAT5.format(new Date(warTime.warStartTime));
		{// 军团长邮件
			String content = StringUtil.format(GangResWarTips.恭喜入围军团资源战x城市对战将于今天x时间正式打响邮件正文, cityTemp.cityname, startTime);
			KGangMsgPackCenter.sendMailToSirs(gang, StringUtil.format(GangResWarTips.军团资源战x城市入围通知邮件标题, cityTemp.cityname), content);
		}
		{// 军团频道+不在线成员私聊
			String content = StringUtil.format(GangResWarTips.入围军团资源战x城市对战将于今天x时间正式打响, cityTemp.cityname, startTime);
			KSupportFactory.getChatSupport().sendChatToAnyChannel(KChatChannelTypeEnum.军团, content, gang.getId());
			//
			KGangMsgPackCenter.sendPrivateChatToNotOnlineRoles(gang, content);
		}
		{// 军团日志
			KGangLogic.addDialy(gang, gangExtSet, StringUtil.format(GangResWarTips.入围军团资源战x城市, cityTemp.cityname), true, true, true);
		}
	}

	/**
	 * <pre>
	 * 结束竞价时，对未入围的军团退还竞价费用
	 * 军团长邮件+军团频道+不在线成员私聊+军团日志
	 * 
	 * @param isSuccess 是否如常进行
	 * @param gangId
	 * @param resource
	 * @author CamusHuang
	 * @creation 2013-9-26 上午10:30:56
	 * </pre>
	 */
	static void onBidEnd_OutList_BackBidResource(boolean isSuccess, int cityId, long gangId, long resource) {
		GangIntegrateData gangData = KGangModuleExtension.getGangAndSet(gangId);
		if (gangData == null) {
			return;
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet gangExtSet = (KGangExtCASet) gangData.getGangExtCASet();

		CityTemplate cityTemp = KResWarDataManager.mCityTempManager.getData(cityId);
		//
		{// 军团长邮件
			String content = null;
			if (isSuccess) {
				content = StringUtil.format(GangResWarTips.军团资源战x城市竞价失败退还竞价资金x, cityTemp.cityname, resource);
			} else {
				content = StringUtil.format(GangResWarTips.军团资源战x城市取消退还竞价资金x, cityTemp.cityname, resource);
			}
			KGangMsgPackCenter.sendMailToSirs(gang, StringUtil.format(GangResWarTips.军团资源战x城市竞价失败邮件标题, cityTemp.cityname), content);
		}
		{// 军团频道+不在线成员私聊
			String content = StringUtil.format(GangResWarTips.很遗憾您的军团竞价失败未获得x城市军团资源战参赛资格, cityTemp.cityname);
			KSupportFactory.getChatSupport().sendChatToAnyChannel(KChatChannelTypeEnum.军团, content, gang.getId());
			//
			KGangMsgPackCenter.sendPrivateChatToNotOnlineRoles(gang, content);
		}
		{// 军团日志
			KGangLogic.addDialy(gang, gangExtSet, StringUtil.format(GangResWarTips.军团资源战x城市竞价失败返还资金x, cityTemp.cityname, resource), true, true, true);
		}
	}

	/**
	 * <pre>
	 * 准备期的定时世界播报
	 * 准备到开战：按指定时间定时发送世界广播
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-28 下午8:40:15
	 * </pre>
	 */
	static void onWarReady() {
		
		{
			KWordBroadcastType _boradcastType = KWordBroadcastType.资源战_军团资源战开始入场;
			KSupportFactory.getChatSupport().sendSystemChat(_boradcastType.content, _boradcastType);
			ResWarDataCenter.RESWAR_LOGGER.warn(_boradcastType.content);
		}

		long warReadyTime = ResWarStatusManager.getWarTime().warReadyTime;
		long warStartTime = ResWarStatusManager.getWarTime().warStartTime;
		//
		long nowTime = System.currentTimeMillis();
		if (nowTime < warStartTime) {
			// 世界广播:军团资源战将于x时间开始
			List<BroadData> broadDataList = new ArrayList<BroadData>();
			{
				// 开始准备~开战前60分钟：按每30分钟发送一次
				long period = 30 * Timer.ONE_MINUTE;
				long startTime = warReadyTime;
				long endTime = warStartTime - Timer.ONE_HOUR;
				//
				KWordBroadcastType _broadcastType = KWordBroadcastType.资源战_军团资源战竞价成功军团名单现已公布;
				for (long sendTime = startTime; sendTime < endTime; sendTime += period) {
					if (nowTime < sendTime) {
						broadDataList.add(new BroadData(sendTime, _broadcastType.content, _broadcastType));
					}
				}
			}
			{
				// 开始准备~开战前60~20分钟：按每10分钟发送一次
				long period = 10 * Timer.ONE_MINUTE;
				long startTime = warStartTime - Timer.ONE_HOUR;
				long endTime = warStartTime - 20 * Timer.ONE_MINUTE;
				//
				KWordBroadcastType _broadcastType = KWordBroadcastType.资源战_军团资源战将于x分钟后开启;
				for (long sendTime = startTime; sendTime < endTime; sendTime += period) {
					if (nowTime < sendTime) {
						int minute = (int) ((warStartTime - sendTime) / Timer.ONE_MINUTE);// N分钟后
						broadDataList.add(new BroadData(sendTime, StringUtil.format(_broadcastType.content, minute), _broadcastType));
					}
				}
			}
			{
				// 最后20分钟按定死的更小周期发送
				int[] times = new int[] { 20, 15, 10, 8, 6, 4, 3, 2, 1 };// 距开战前多少分钟发送公告
				//
				KWordBroadcastType _broadcastType = KWordBroadcastType.资源战_军团资源战将于x分钟后开启请各参战军团做好准备;
				for (int i = 0; i < times.length; i++) {
					long sendTime = warStartTime - times[i] * Timer.ONE_MINUTE;// 发送公告的时刻
					if (nowTime < sendTime) {
						broadDataList.add(new BroadData(sendTime, StringUtil.format(_broadcastType.content, times[i]), _broadcastType));
					}
				}
			}

			{
				int[] times = new int[] { 45, 30, 20, 15 };// 距开战前多少秒发送公告
				//
				KWordBroadcastType _broadcastType = KWordBroadcastType.资源战_军团资源战将于x秒后开启;
				for (int i = 0; i < times.length; i++) {
					long sendTime = warStartTime - times[i] * Timer.ONE_SECOND;// 发送公告的时刻
					if (nowTime < sendTime) {
						broadDataList.add(new BroadData(sendTime, StringUtil.format(_broadcastType.content, times[i]), _broadcastType));
					}
				}
			}
			ResWarTaskManager.WorldBroadcastTask.submitTask(0, broadDataList);
		}
	}

	/**
	 * <pre>
	 * 对战期的定时世界播报
	 * 1.按分钟定时发送世界广播
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-28 下午8:40:15
	 * </pre>
	 */
	static void onWarStart() {

		KWordBroadcastType _broadcastType = KWordBroadcastType.资源战_军团资源战正式开始请尽快入场;
		KSupportFactory.getChatSupport().sendSystemChat(_broadcastType.content, _broadcastType);
		ResWarDataCenter.RESWAR_LOGGER.warn(_broadcastType.content);

		long warStartTime = ResWarStatusManager.getWarTime().warStartTime;
		long warEndTime = ResWarStatusManager.getWarTime().warEndTime;
		//
		long nowTime = System.currentTimeMillis();
		if (nowTime < warEndTime) {
			// 战斗场景广播:本场军团资源战将于X分钟后结束
			List<BroadData> broadDataList = new ArrayList<BroadData>();
			{
				// 按每1分钟发送一次
				long period = 1 * Timer.ONE_MINUTE;// 每1分钟发送一次
				//
				KWordBroadcastType _broadcastType2 = KWordBroadcastType.资源战_军团资源战将于x分钟后结束;
				KWordBroadcastType _broadcastType3 = KWordBroadcastType.资源战_军团资源战致胜关键;
				for (long sendTime = warStartTime + period; sendTime < warEndTime; sendTime += period) {

					// 提前30秒，提醒致胜关键
					long sendTime2 = sendTime - 30 * Timer.ONE_SECOND;
					if (nowTime < sendTime2) {
						broadDataList.add(new BroadData(sendTime2, _broadcastType3.content, _broadcastType3));
					}

					// 提醒剩余多少分钟
					if (nowTime < sendTime) {
						int minute = (int) ((warEndTime - sendTime) / Timer.ONE_MINUTE);// N分钟后
						broadDataList.add(new BroadData(sendTime, StringUtil.format(_broadcastType2.content, minute), _broadcastType2));
					}
				}
			}

			ResWarTaskManager.CityBroadcastTask.submitTask(broadDataList);
		}
	}

	static void onPVPWin() {
		// CTODO
	}

	static void onWarEnd_Lose_BackBidResource(int cityId, long gangId, long resource) {
		GangIntegrateData gangData = KGangModuleExtension.getGangAndSet(gangId);
		if (gangData == null) {
			return;
		}
		CityTemplate cityTemp = KResWarDataManager.mCityTempManager.getData(cityId);

		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet gangExtSet = (KGangExtCASet) gangData.getGangExtCASet();
		//
		{// 军团长邮件
			String content = StringUtil.format(GangResWarTips.军团资源战x城市争夺失败返还竞价资金x, cityTemp.cityname, resource);
			KGangMsgPackCenter.sendMailToSirs(gang, StringUtil.format(GangResWarTips.军团资源战x城市争夺失败邮件标题, cityTemp.cityname), content);
		}
		{// 军团日志
			KGangLogic.addDialy(gang, gangExtSet, StringUtil.format(GangResWarTips.军团资源战x城市争夺失败返还资金x, cityTemp.cityname, resource), true, true, true);
		}
	}

	static void onWarEnd_Gang(CityTemplate temp, boolean isWin, KGang gang, KGangExtCASet set, int addExp) {
		// 争夺战获胜利时，军团动态内记录信息：军团成功在本次争夺战中占领了XXX城市，获得XXX军团经验
		// 争夺战失败时，军团动态内记录信息：军团未能在本次争夺战中占领了XXX城市，获得XXX军团经验
		//
		{// 军团日志
			if (isWin) {
				KGangLogic.addDialy(gang, set, StringUtil.format(GangResWarTips.军团成功在本次争夺战中占领了x城市获得x军团经验, temp.cityname, addExp), true, true, true);
			} else {
				KGangLogic.addDialy(gang, set, StringUtil.format(GangResWarTips.军团未能在本次争夺战中占领x城市获得x军团经验, temp.cityname, addExp), true, true, true);
			}
		}
	}

	static void onWarEnd_City(ResWarCity city, GangData winner, GangData loser) {
		if (winner == null && loser == null) {
			return;
		}
		if (loser == null) {
			KWordBroadcastType _brocastType = KWordBroadcastType.资源战_x军团在本次资源争夺活动中成功夺得x城市控制权;
			KSupportFactory.getChatSupport().sendSystemChat(StringUtil.format(_brocastType.content, winner.gangName, city.getTemplate().cityname), _brocastType);
		} else {
			KWordBroadcastType _brocastType = KWordBroadcastType.资源战_x军团在本次资源争夺活动中战胜x军团成功夺得x城市控制权;
			KSupportFactory.getChatSupport().sendSystemChat(StringUtil.format(_brocastType.content, winner.gangName, loser.gangName, city.getTemplate().cityname), _brocastType);
		}
	}

	static void onWarEnd() {
		KWordBroadcastType _broadcastType = KWordBroadcastType.资源战_军团资源战完满结束;
		KSupportFactory.getChatSupport().sendSystemChat(_broadcastType.content, _broadcastType);
		ResWarDataCenter.RESWAR_LOGGER.warn(_broadcastType.content);
	}
}
