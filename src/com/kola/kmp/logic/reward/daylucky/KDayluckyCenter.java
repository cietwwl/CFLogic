package com.kola.kmp.logic.reward.daylucky;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.reward.daylucky.KDayluckyDataManager.KDayluckyRateDataManager.DayluckyRateData;
import com.kola.kmp.logic.reward.daylucky.KDayluckyDataManager.KDayluckyRewardDataManager.DayluckyRewardData;
import com.kola.kmp.logic.reward.vitality.KVitalityCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KSimpleDialyManager;
import com.kola.kmp.logic.util.KSimpleDialyManager.Dialy;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_DayluckOpenNum;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_GoodLuck;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_SendMail;
import com.kola.kmp.logic.util.tips.RewardTips;
import com.kola.kmp.logic.util.tips.ShopTips;
import com.kola.kmp.protocol.reward.KRewardProtocol;

public class KDayluckyCenter {

	/**
	 * 1~10的备选号码
	 */
	private static final List<Integer> StaticLuckNums = new ArrayList<Integer>();
	static {
		for (int i = 1; i <= 10; i++) {
			StaticLuckNums.add(i);
		}
	}

	private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();// 读写锁

	/** 今日的日期时间 */
	private static long todayDataTime;
	/** 今日幸运号码 */
	private static int[] todayLuckNums = new int[KDayluckyDataManager.NUM_COUNT];
	// <今日幸运号码>
	private static Set<Integer> todayLuckNumsSet = new HashSet<Integer>();
	// <号码位置,号码>：10个号码，幸运号码固定排前1，2，3位，其它号码排在后面，号码位置对应几率表的几率
	private static Map<Integer, Integer> todayLuckNumsMap = new HashMap<Integer, Integer>();

	/** 大奖日志:”玩家名字在每日幸运中获得了物品名让人羡慕嫉妒恨!” */
	private static KSimpleDialyManager dialyManager;

	static void notifyCacheLoadComplete() {
		{
			// 初始化日志
			dialyManager = new KSimpleDialyManager(KDayluckyDataManager.DayluckyDailySaveDir, KDayluckyDataManager.DayluckyDailySaveFileName);
			dialyManager.loadDialys();
		}
		{
			// CTODO 加载幸运号码数据

			// 如果不存在幸运号码数据或者数据过时，则重置
			long nowTime = System.currentTimeMillis();
			if (todayLuckNums[0] < 1 || UtilTool.isBetweenDay(nowTime, todayDataTime)) {
				resetLuckNums();
			}
		}
		//

	}

	/**
	 * <pre>
	 * 生成幸运号码
	 * 
	 * @author CamusHuang
	 * @creation 2014-4-24 下午2:56:19
	 * </pre>
	 */
	static void resetLuckNums() {
		rwLock.writeLock().lock();
		try {
			todayDataTime = System.currentTimeMillis();
			todayLuckNumsSet.clear();
			todayLuckNumsMap.clear();
			List<Integer> StaticLuckNums = new ArrayList<Integer>(KDayluckyCenter.StaticLuckNums);
			int index = 0;
			// 随机三张幸运号码
			for (; index < todayLuckNums.length; index++) {
				todayLuckNums[index] = StaticLuckNums.remove(UtilTool.random(StaticLuckNums.size()));
				todayLuckNumsSet.add(todayLuckNums[index]);
				// 对应1，2，3号位置
				todayLuckNumsMap.put(index + 1, todayLuckNums[index]);
			}
			// 剩余7个号码放入4，5，6，7，8，9，10位置
			for (Integer num : StaticLuckNums) {
				todayLuckNumsMap.put(index + 1, num);
				index++;
			}
		} finally {
			rwLock.writeLock().unlock();
		}
	}

	static KSimpleDialyManager getDialyManager() {
		return dialyManager;
	}

	/**
	 * <pre>
	 * 参考{@link KRewardProtocol#SM_DAYLUCK_PUSHDATA}
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2014-4-23 下午4:05:07
	 * </pre>
	 */
	public static void packDayluckyData(KGameMessage msg, long roleId) {

		KRoleRewardDaylucky data = KDayluckySonModule.instance.getRewardSon(roleId);
		data.rwLock.lock();
		try {
			for (int i = 0; i < todayLuckNums.length; i++) {
				msg.writeByte(todayLuckNums[i]);
			}
			//
			int[] hidedNums = data.getHidedNums();
			boolean isHide = hidedNums != null;
			msg.writeBoolean(isHide);
			if (isHide) {
				for (int i = 0; i < hidedNums.length; i++) {
					msg.writeByte(hidedNums[i]);
				}
			}
			//
			msg.writeByte(data.todayLuckNums.size());
			for (int[] temp : data.todayLuckNums) {
				for (int i = 0; i < temp.length; i++) {
					msg.writeByte(temp[i]);
				}
			}
			//
			int nextTime = data.todayLuckNums.size() + (isHide ? 2 : 1);
			DayluckyRateData nextData = KDayluckyDataManager.mDayluckyRateDataManager.getData(nextTime);
			msg.writeBoolean(nextData != null);
			msg.writeInt(nextData == null ? -1 : nextData.needvitality);
		} finally {
			data.rwLock.unlock();
		}
		//
		msg.writeByte(KDayluckyDataManager.BigRewardLogMaxCount);
	}

	public static RewardResult_GoodLuck dealMsg_getNum(KRole role) {

		RewardResult_GoodLuck result = new RewardResult_GoodLuck();

		KRoleRewardDaylucky data = KDayluckySonModule.instance.getRewardSon(role.getId());
		data.rwLock.lock();
		try {
			rwLock.readLock().lock();
			try {
				// 有未刮开的卡片
				int[] hidedNums = data.getHidedNums();
				boolean isHide = hidedNums != null;
				if (isHide) {
					result.tips = RewardTips.这张都刮刮卡还没刮完呢;
					return result;
				}
				//
				int nextTime = data.todayLuckNums.size() + 1;
				DayluckyRateData nextData = KDayluckyDataManager.mDayluckyRateDataManager.getData(nextTime);
				if (nextData == null) {
					result.tips = RewardTips.今天的刮刮卡已经领完了哦;
					return result;
				}
				//
				int nowActiveValue = KVitalityCenter.getVitalityValue(role.getId());
				if (nowActiveValue < nextData.needvitality) {
					result.tips = RewardTips.活跃度不够领取刮刮卡;
					return result;
				}

				//
				hidedNums = randomNum(nextData);
				data.setHidedNums(hidedNums);

				//
				result.isSucess = true;
				result.tips = RewardTips.有新的刮刮卡哦快刮开看看吧;
				result.newNum = hidedNums;
				nextData = KDayluckyDataManager.mDayluckyRateDataManager.getData(nextTime + 1);
				result.nextActiveValue = nextData == null ? -1 : nextData.needvitality;
				return result;
			} finally {
				rwLock.readLock().unlock();
			}
		} finally {
			data.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 生成刮刮卡
	 * 1.先从3个幸运号码中按万比较几率单独随机，选出n（0~3）个号码
	 * 2.若n<3，则从7个非幸运号码中按权重随机选出（3-n）个号码
	 * 
	 * @param nextData
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-23 下午5:08:09
	 * </pre>
	 */
	static int[] randomNum(DayluckyRateData nextData) {
		// 按权重随机得到三个位置
		int[] poss = nextData.randomNum();
		int[] result = new int[poss.length];
		for (int i = 0; i < poss.length; i++) {
			result[i] = todayLuckNumsMap.get(poss[i]);
		}
		return result;
	}

	/**
	 * <pre>
	 * 检查指定的刮刮卡有多少个数字与幸运号码相同
	 * 
	 * @param hidedNums
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-23 下午4:40:25
	 * </pre>
	 */
	private static int checkBingoCount(int[] hidedNums) {
		int count = 0;
		for (int index = 0; index < hidedNums.length; index++) {
			if (todayLuckNumsSet.contains(hidedNums[index])) {
				count++;
			}
		}
		return count;
	}

	/**
	 * <pre>
	 * 检查指定的刮刮卡数组，如果有偶数次全中，则返回true
	 * 
	 * @param todayLuckNums2
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-23 下午4:50:10
	 * </pre>
	 */
	private static boolean checkDoubleBingo(List<int[]> todayLuckNums) {
		int bingoTime = 0;
		for (int[] temp : todayLuckNums) {
			int count = checkBingoCount(temp);
			if (count == temp.length) {
				bingoTime++;
			}
		}
		if (bingoTime < 1) {
			return false;
		}
		return bingoTime % 2 == 0;
	}

	public static RewardResult_DayluckOpenNum dealMsg_openNum(KRole role) {

		RewardResult_DayluckOpenNum result = new RewardResult_DayluckOpenNum();

		KRoleRewardDaylucky data = KDayluckySonModule.instance.getRewardSon(role.getId());
		data.rwLock.lock();
		try {
			rwLock.readLock().lock();
			try {
				// 有未刮开的卡片
				int[] hidedNums = data.getHidedNums();
				boolean isHide = hidedNums != null;
				if (!isHide) {
					result.tips = RewardTips.你没有刮刮卡哦;
					return result;
				}
				// 清空，放入已刮
				data.setHidedNums(null);
				data.todayLuckNums.add(hidedNums);
				data.notifyDB();
				// 是否中奖
				int bingoCount = checkBingoCount(hidedNums);
				if (bingoCount < 1) {
					// 没中奖
					result.isSucess = true;
					result.tips = RewardTips.没有中奖哦;
					return result;
				}

				// 发放奖励
				DayluckyRewardData reward = KDayluckyDataManager.mDayluckyRewardDataManager.getData(bingoCount);
				RewardResult_SendMail sendMailResult = reward.mailReward.sendReward(role, PresentPointTypeEnum.每日幸运, true);
				if (!sendMailResult.isSendByMail) {
					result.addDataUprisingTips(sendMailResult.getDataUprisingTips());
				} else {
					// 转发邮件
					result.addUprisingTips(RewardTips.背包已满奖励通过邮件发送);
				}

				//
				if (reward.isShowInDialy) {
					// 大奖日志
					String dialy = StringUtil.format(RewardTips.刮刮卡大奖日志x获得x, role.getExName(), reward.mailReward.baseRewardData.getRewardTips());
					dialyManager.addDialy(dialy);
					result.isSynLogs = true;
				}

				// 是否中双奖
				if (checkDoubleBingo(data.todayLuckNums)) {
					// 中双奖
					reward = KDayluckyDataManager.mDayluckyRewardDataManager.getData(todayLuckNums.length + 1);
					if (reward != null) {
						sendMailResult = reward.mailReward.sendReward(role, PresentPointTypeEnum.每日幸运, true);
						if (!sendMailResult.isSendByMail) {
							result.addDataUprisingTips(sendMailResult.getDataUprisingTips());
						} else {
							// 转发邮件
							result.addUprisingTips(RewardTips.背包已满奖励通过邮件发送);
						}
						if (reward.isShowInDialy) {
							// 大奖日志
							String dialy = StringUtil.format(RewardTips.刮刮卡大奖日志x获得x, role.getExName(), reward.mailReward.baseRewardData.getRewardTips());
							dialyManager.addDialy(dialy);
							result.isSynLogs = true;
						}

					}
				}
				//
				result.isSucess = true;
				result.tips = UtilTool.getNotNullString(null);
				return result;
			} finally {
				rwLock.readLock().unlock();
			}
		} finally {
			data.rwLock.unlock();
		}

	}

	public static KGameMessage packLogs(int msgId, int maxLogsId) {
		int maxId = dialyManager.getMaxDialyId();
		if (maxLogsId >= maxId) {
			return null;
		}

		List<Dialy> dialys = dialyManager.getAllDialysCopy();
		KGameMessage backMsg = KGame.newLogicMessage(msgId);
		int writeIndex = backMsg.writerIndex();
		backMsg.writeByte(0);
		int count = 0;
		for (Dialy dialy : dialys) {
			if (dialy.id > maxLogsId) {
				backMsg.writeInt(dialy.id);
				backMsg.writeUtf8String(dialy.dialy);
				count++;
			}
		}
		backMsg.setByte(writeIndex, count);
		return backMsg;
	}

	public static void packDayluckyRewards(KGameMessage msg) {

		LinkedHashMap<Integer, DayluckyRewardData> map = KDayluckyDataManager.mDayluckyRewardDataManager.getDataCache();
		msg.writeByte(map.size());
		for (DayluckyRewardData data : map.values()) {
			msg.writeByte(data.addItems.size());
			for (ItemCountStruct struct : data.addItems) {
				KItemMsgPackCenter.packItem(msg, struct.getItemTemplate(), struct.itemCount);
			}
		}
	}
}
