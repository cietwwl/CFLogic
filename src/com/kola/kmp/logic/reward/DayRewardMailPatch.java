package com.kola.kmp.logic.reward;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.RunTimeTask;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KGameUtilTool;

/**
 * <pre>
 * 每日奖励邮件
 * 
 * 每日第一次登陆，发送奖励邮件
 * 名单记录到文件中，支持当天重启不重复发送
 * （跨天相关逻辑需要认真测试）
 * 
 * @author CamusHuang
 * @creation 2013-10-25 下午2:26:36
 * </pre>
 */
public class DayRewardMailPatch implements RunTimeTask {
	private static Logger _LOGGER = KGameLogger.getLogger(DayRewardMailPatch.class);

	static Set<Long> roleIdSet = new HashSet<Long>();
	static long todayTime = System.currentTimeMillis();
	static String todayStr = UtilTool.DATE_FORMAT6.format(new Date(todayTime));

	static String dir = "./res/output/DayRewardMailPatch/";
	static String fileName = "roleids.xml";

	static String mailTitle = "登录送体力";
	static String mailContent = "体力用完了，一大波体力送到，让你更深入的体验游戏，精英邀请封测有你更精彩！";
	static List<ItemCountStruct> itemList = new ArrayList<ItemCountStruct>();
	static {
		// 323004 *3
		itemList.add(new ItemCountStruct("323004", 3));
	}

	public String run(String value) {

		if (value.equalsIgnoreCase("start")) {
			todayTime = System.currentTimeMillis();
			todayStr = UtilTool.DATE_FORMAT6.format(new Date(todayTime));

			roleIdSet.clear();
			List<String> result = KGameUtilTool.loadSimpleDialy(dir, fileName, todayStr);
			for (String id : result) {
				roleIdSet.add(Long.parseLong(id));
			}

			SendTask.getInstance().start();
			return "任务已开始，请等待5秒";
		} else {
			SendTask.getInstance().stop();
			return "任务已停止，请等待5秒";
		}
	}

	static void doSomething() {

		boolean saveFile = false;
		long nowTime = System.currentTimeMillis();
		if (UtilTool.isBetweenDay(nowTime, todayTime)) {
			// 跨天重置
			todayTime = nowTime;
			todayStr = UtilTool.DATE_FORMAT6.format(new Date(todayTime));
			roleIdSet.clear();
			saveFile = true;
		}

		for (Long roleId : KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds()) {
			if (roleIdSet.add(roleId)) {
				saveFile = true;
				KSupportFactory.getMailModuleSupport().sendAttMailBySystem(roleId, itemList, mailTitle, mailContent);
			}
		}

		if (saveFile) {
			// 记录名单
			List<Long> dialys = new ArrayList<Long>(roleIdSet);
			KGameUtilTool.saveSimpleDialy(dir, fileName, todayStr, dialys);
		}
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 内部唯一实例
	 * @author CamusHuang
	 * @creation 2014-2-21 上午9:44:51
	 * </pre>
	 */
	static class SendTask implements KGameTimerTask {

		private final static SendTask instance = new SendTask();
		private static boolean isRun = false;
		private final static long periodTime = 5 * Timer.ONE_SECOND;

		private SendTask() {
		}

		static SendTask getInstance() {
			return instance;
		}

		/**
		 * <pre>
		 * 启动入口
		 * 
		 * @author CamusHuang
		 * @creation 2014-2-21 上午10:35:34
		 * </pre>
		 */
		void start() {
			if (isRun) {
				return;
			}
			isRun = true;
			KGame.newTimeSignal(SendTask.instance, periodTime, TimeUnit.MILLISECONDS);
		}

		void stop() {
			isRun = false;
		}

		@Override
		public void done(KGameTimeSignal arg0) {
		}

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {

			if (!isRun) {
				return null;
			}

			try {
				doSomething();
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			} finally {
				arg0.getTimer().newTimeSignal(this, periodTime, TimeUnit.MILLISECONDS);
			}
			return null;
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}
	}
}
