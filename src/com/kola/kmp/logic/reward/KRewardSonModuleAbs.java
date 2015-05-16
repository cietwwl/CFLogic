package com.kola.kmp.logic.reward;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kmp.logic.reward.daylucky.KDayluckySonModule;
import com.kola.kmp.logic.reward.daylucky.KRoleRewardDaylucky;
import com.kola.kmp.logic.reward.login.KLoginSonModule;
import com.kola.kmp.logic.role.IRoleEventListener;

/**
 * <pre>
 * 奖励模块子模块
 * 
 * @author CamusHuang
 * @creation 2013-12-28 上午10:38:13
 * </pre>
 */
public abstract class KRewardSonModuleAbs<S extends KRoleRewardSonAbs> implements IRewardRoleEventListener {

	public final KRewardSonModuleType type;

	public KRewardSonModuleAbs(KRewardSonModuleType type) {
		this.type = type;
	}

	public abstract S newRewardSon(KRoleReward roleReward, boolean isFirstNew);

	public S getRewardSon(long roleId) {
		KRoleReward reward = KRewardRoleExtCACreator.getRoleReward(roleId);
		return (S) reward.getSon(type);
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 模块内调用
	 * @param e
	 * @throws KGameServerException
	 * @author CamusHuang
	 * @creation 2013-5-30 下午5:22:21
	 * </pre>
	 */
	public abstract void loadConfig(Element e) throws KGameServerException;

	/**
	 * <pre>
	 * 解释奖励总表中与本子模块相关的sheet
	 * 
	 * @param file
	 * @param HeaderIndex
	 * @throws Exception
	 * @author CamusHuang
	 * @creation 2014-10-10 上午10:54:47
	 * </pre>
	 */
	public abstract void goToParamsRewardData(KGameExcelFile file, int HeaderIndex) throws Exception;
	
	/**
	 * <pre>
	 * 加载独立的数据表
	 * 
	 * @param file
	 * @param HeaderIndex
	 * @throws Exception
	 * @author CamusHuang
	 * @creation 2014-10-10 上午10:55:14
	 * </pre>
	 */
	public abstract void goToLoadData(Element excelE) throws Exception;

	/**
	 * <pre>
	 * 
	 * @deprecated 模块内调用
	 * @throws KGameServerException
	 * @author CamusHuang
	 * @creation 2013-5-30 下午5:22:16
	 * </pre>
	 */
	public abstract void onGameWorldInitComplete() throws KGameServerException;

	/**
	 * <pre>
	 * 
	 * @deprecated 模块内调用
	 * @author CamusHuang
	 * @creation 2013-5-30 下午5:22:13
	 * </pre>
	 */
	public abstract void notifyCacheLoadComplete() throws KGameServerException;
	
	/**
	 * <pre>
	 * 
	 * @deprecated 模块内调用
	 * @author CamusHuang
	 * @creation 2013-5-30 下午5:22:13
	 * </pre>
	 */
	public void afterNotifyCacheLoadComplete() throws KGameServerException{
		
	}

	public abstract void serverShutdown() throws KGameServerException;

	/**
	 * <pre>
	 * 时效任务跨天通知
	 * 
	 * @author CamusHuang
	 * @creation 2014-4-24 下午3:12:03
	 * </pre>
	 */
	public abstract void notifyForDayChangeTask(long nowTime);

	/**
	 * <pre>
	 * 时效任务跨天重置后的通知
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2014-4-23 下午6:42:35
	 * </pre>
	 */
	public abstract void notifyAfterDayChangeTask(long roleId);

	/**
	 * <pre>
	 * 子模块类型
	 * 
	 * @author CamusHuang
	 * @creation 2014-4-26 下午6:04:23
	 * </pre>
	 */
	public static enum KRewardSonModuleType {
		每日幸运(1, true), //
		登陆奖励(2, true), //
		活跃度奖励(3, true), //
		僵尸庄园(4, false), //庄园数据复杂，独立一个角色扩展CA
		在线奖励(5, true), //
		激活码(6, true), //
		精彩活动(7, false), //数据复杂，独立一个角色扩展CA
		改版补偿(8, true), //
		;

		// 标识数值
		public final int sign;
		// 角色数据是否继承KRoleRewardSonAbs-即数据由KRoleReward负责管理
		public final boolean isSonOfReward;

		private KRewardSonModuleType(int sign, boolean isSonOfReward) {
			this.sign = sign;
			this.isSonOfReward = isSonOfReward;
		}
	}
}
