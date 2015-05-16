package com.kola.kmp.logic.reward;

import java.text.ParseException;
import java.util.ArrayList;
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
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.RunTimeTask;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;

/**
 * <pre>
 * 重大补偿
 * 执行补丁后，每10秒扫描一次在线人员，创角时间小于指定时间的，且未发奖励的，发送奖励
 * 
 * @author CamusHuang
 * @creation 2013-10-25 下午2:26:36
 * </pre>
 */
public class ShutDownPatch implements RunTimeTask {
	private static Logger _LOGGER = KGameLogger.getLogger(DayRewardMailPatch.class);
	
	public String run(String value) {
		
		String[] params = value.split(";");

		if (params[0].equalsIgnoreCase("start")) {
			long maxTime;
			try {
				maxTime = UtilTool.DATE_FORMAT12.parse(params[1]).getTime();
			} catch (ParseException e) {
				_LOGGER.error(e.getMessage(), e);
				return e.getMessage();
			}
			
			String mailTitle ="标题：1月16日登录异常补偿";
			String mailContent ="正文：亲爱的友友，由于网络问题，导致1月16日13：00左右部分玩家无法登录游戏，现补偿100万金币，50体力，给您带来不便万分抱歉，感谢您对超凡特工的支持！";

			BaseMailContent baseMail = new BaseMailContent(mailTitle, mailContent, null, null);
			//物品：323001*5，货币类型2 1000000
			List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
			moneyList.add(new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, 1000000));
			List<ItemCountStruct> itemStructs = new ArrayList<ItemCountStruct>();
			itemStructs.add(new ItemCountStruct("323001", 5));
			BaseRewardData baseRewardData = new BaseRewardData(null, moneyList, itemStructs, null, null);
			
			BaseMailRewardData mailReward = new BaseMailRewardData(1, baseMail, baseRewardData);

			SendTask.getInstance().start(maxTime, mailReward);
			return "任务已开始，请等待5秒";
		} else {
			SendTask.getInstance().stop();
			return "任务已停止，请等待5秒";
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
		private final static long periodTime = 10 * Timer.ONE_SECOND;
		
		
		long maxTime;
		BaseMailRewardData mailReward;

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
		void start(long maxTime, BaseMailRewardData mailReward) {
			if (isRun) {
				return;
			}
			isRun = true;
			this.maxTime = maxTime;
			this.mailReward = mailReward;
			doRoles.clear();
			KGame.newTimeSignal(this, periodTime, TimeUnit.MILLISECONDS);
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
		
		Set<Long> doRoles = new HashSet<Long>();
		
		void doSomething() {

			RoleModuleSupport mRoleModuleSupport= KSupportFactory.getRoleModuleSupport();
			
			for (Long roleId : mRoleModuleSupport.getAllOnLineRoleIds()) {
				if (doRoles.contains(roleId)) {
					continue;
				}
				KRole role = mRoleModuleSupport.getRole(roleId);
				if(role==null){
					_LOGGER.error("------------------角色ID="+roleId+" 不存在！");
				} else if(role.getCreateTime()<maxTime){
					KSupportFactory.getMailModuleSupport().sendAttMailBySystem(roleId, mailReward, PresentPointTypeEnum.其它);
					doRoles.add(roleId);
				}
			}
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}
	}
}
