package com.kola.kmp.logic.reward.dynamic;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.gang.GangIntegrateData;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangExtCASet;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.gang.KGangMember;
import com.kola.kmp.logic.gang.KGangModuleExtension;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.reward.dynamic.DynamicRewardDataStruct.GangRewardData;
import com.kola.kmp.logic.reward.dynamic.DynamicRewardDataStruct.RewardElement;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.MailResult;
import com.kola.kmp.logic.util.tips.GangResWarTips;
import com.kola.kmp.logic.util.tips.GangTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.RewardTips;

/**
 * <pre>
 * 动态奖励
 * 在EXCEL中配置奖励和名单
 * 接受GM指令，加载奖励数据执行奖励操作
 * 
 * 定时任务扫描开关执行奖励操作
 * @author CamusHuang
 * @creation 2013-5-21 下午12:24:52
 * </pre>
 */
public class DynamicRewardCenter {

	public static final Logger _LOGGER = KGameLogger.getLogger(DynamicRewardCenter.class);

	/**
	 * <pre>
	 * 
	 * 
	 * @param psw 执行密码
	 * @param type role表示角色，family表示军团
	 * @author CamusHuang
	 * @creation 2013-10-11 下午10:41:25
	 * </pre>
	 */
	public static String runRewardOnGMOrder(String psw, String type) {
		if (!psw.equals("45636$#$wqroll")) {
			_LOGGER.error("执行【动态奖励】失败！原因=密码错误");
			return "执行【动态奖励】失败！原因=密码错误";
		}

		if (type.equals("role")) {
			return runRoleReward();
		}
		if (type.equals("family")) {
			return runGangReward();
		}

		_LOGGER.error("执行【动态奖励】失败！原因=类型错误");
		return "执行【动态奖励】失败！原因=类型错误";
	}

	/**
	 * <pre>
	 * 执行奖励流程
	 * 
	 * 加载数据--检查数据--发送邮件
	 * 
	 * @author CamusHuang
	 * @creation 2013-6-25 上午11:14:57
	 * </pre>
	 */
	private static String runRoleReward() {
		_LOGGER.warn("开始加载角色动态奖励数据");
		try {
			DynamicRewardDataManager dataManager = DynamicRewardDataLoader.goToLoadData(true);
			dataManager.mRewardDataManager.serverStartCompleted();
			_LOGGER.warn("加载角色动态奖励数据结束");

			for (RewardElement data : dataManager.mRewardRoleDataManager.getAllDatas().values()) {
				// 插入邮件
				MailResult result = KSupportFactory.getMailModuleSupport().sendAttMailBySystem(data.id, dataManager.mRewardDataManager.getData(data.rewardId).baseMailRewardData,
						PresentPointTypeEnum.动态奖励);
				if (result.isSucess) {
					// 浮动提示通知
					KDialogService.sendUprisingDialog(data.id, RewardTips.系统奖励已发送请查看邮件);
				} else {
					_LOGGER.warn("添加角色奖励邮件 出错：ROLE{ID:{} NAME:{}} TIPS={}", data.id, data.name, result.tips);
				}
			}
			_LOGGER.warn("发送角色动态奖励邮件结束");
			return null;
		} catch (Exception e) {
			_LOGGER.error("加载角色动态奖励数据错误：" + e.getMessage(), e);
			return "加载角色动态奖励数据错误：" + e.getMessage();
		}
	}

	/**
	 * <pre>
	 * 执行奖励流程
	 * 
	 * 加载数据--检查数据--发送邮件
	 * 
	 * @author CamusHuang
	 * @creation 2013-6-25 上午11:14:57
	 * </pre>
	 */
	private static String runGangReward() {
		_LOGGER.warn("开始加载军团动态奖励数据");
		try {
			DynamicRewardDataManager dataManager = DynamicRewardDataLoader.goToLoadData(false);
			dataManager.mRewardDataManager.serverStartCompleted();
			_LOGGER.warn("加载军团动态奖励数据结束");

			for (RewardElement data : dataManager.mRewardRoleDataManager.getAllDatas().values()) {
				GangIntegrateData gangData = KGangModuleExtension.getGangAndSet(data.id);
				if (gangData == null) {
					_LOGGER.warn("发送军团动态奖励时找不到军团：Gang_ID:{}", data.id);
					continue;
				}
				KGang gang = (KGang) gangData.getGang();
				KGangExtCASet gangExtSet = (KGangExtCASet) gangData.getGangExtCASet();

				gang.rwLock.lock();
				try {
					GangRewardData reward = dataManager.mRewardDataManager.getData(data.rewardId);
					{
						if (reward.gangResource > 0) {
							gang.changeResource(reward.gangResource);
							String dialy = GlobalTips.系统发送奖励 + StringUtil.format(GangTips.军团资金加x, reward.gangResource);
							KGangLogic.addDialy(gang, gangExtSet, dialy, true, true, true);
						}
					}

					{// 遍历成员插入邮件
						for (KGangMember mem : gang.getAllElementsCache().values()) {
							MailResult result = KSupportFactory.getMailModuleSupport().sendAttMailBySystem(mem._roleId, reward.baseMailRewardData, PresentPointTypeEnum.动态奖励);
							if (result.isSucess) {
								// 浮动提示通知
								KDialogService.sendUprisingDialog(mem._roleId, RewardTips.系统奖励已发送请查看邮件);
							} else {
								_LOGGER.warn("添加军团奖励邮件 出错：gang_ID:{} role_ID:{} TIPS={}", data.id, mem._roleId, result.tips);
							}
						}
					}
				} finally {
					gang.rwLock.unlock();
				}
			}
			_LOGGER.warn("发送军团动态奖励邮件结束");
			return null;
		} catch (Exception e) {
			_LOGGER.error("加载军团动态奖励数据错误：" + e.getMessage(), e);
			return "加载军团动态奖励数据错误：" + e.getMessage(); 
		}
	}

}
