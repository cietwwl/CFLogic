package com.kola.kmp.logic.gang;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.DataCacheAccesserFactory;
import com.kola.kgame.cache.GangEntireDataCacheAccesser;
import com.kola.kgame.cache.gang.Gang;
import com.kola.kmp.logic.RunTimeTask;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.OtherFlowTypeEnum;
import com.kola.kmp.logic.gang.message.KSyncGangDataMsg;
import com.kola.kmp.logic.gang.message.KSyncMemberListMsg;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GangTips;


/**
 * 执行一次超时禅让逻辑
 */
public class RunMakeOverTaskPatch implements RunTimeTask {
	private static Logger _LOGGER = KGameLogger.getLogger(RunMakeOverTaskPatch.class);

	public String run(String gangId) {
		try {
			if(gangId==null){
				autoMoveOverSire_outtime();
			} else {
				return autoMoveOverSire(Long.parseLong(gangId));
			}
			return "执行完毕";
		} catch (Exception e) {
			_LOGGER.error(e.getMessage(), e);
			return "发生异常："+e.getMessage();
		}
	}
	
	private static void autoMoveOverSire_outtime() throws Exception {

		//  团长的离线时间超过5天时，系统会自动将团长职位转让给最近3天内在线且贡献最高的玩家
		//  团长被系统自动转让时，会发送邮件给与相关提示给团长与任职的玩家：
		//  标题：军团管理
		//  内容：由于XXX团长的不在线时间超过5天，系统将团长职位自动转让给了XXX。

		long minTime_Out = System.currentTimeMillis() - KGangConfig.getInstance().AutoMoveOver_OutTime;
		long minTime_Login = System.currentTimeMillis() - KGangConfig.getInstance().AutoMoveOver_TargetTime;

		GangEntireDataCacheAccesser cache = DataCacheAccesserFactory.getGangEntireDataCacheAccesser();
		List<Gang> copyGangList = cache.getAllGangsCopy();
		for (Gang tempGang : copyGangList) {
			KGang gang = (KGang) tempGang;
			KGangExtCASet extCASet = KGangModuleExtension.getGangExtCASet(gang.getId());
			//
			doWork(gang, extCASet, minTime_Out, minTime_Login);
		}
	}
	
	private static String autoMoveOverSire(long gangId) throws Exception {

		//  团长的离线时间超过5天时，系统会自动将团长职位转让给最近3天内在线且贡献最高的玩家
		//  团长被系统自动转让时，会发送邮件给与相关提示给团长与任职的玩家：
		//  标题：军团管理
		//  内容：由于XXX团长的不在线时间超过5天，系统将团长职位自动转让给了XXX。

		long minTime_Out = System.currentTimeMillis() - 1*Timer.ONE_MINUTE;
		long minTime_Login = System.currentTimeMillis() - 1*Timer.ONE_DAY;

		{
			KGang gang = KSupportFactory.getGangSupport().getGang(gangId);
			if (gang == null) {
				throw new Exception("军团不存在");
			}
			KGangExtCASet extCASet = KGangModuleExtension.getGangExtCASet(gang.getId());
			//
			return doWork(gang, extCASet, minTime_Out, minTime_Login);
		}
	}
	
	private static String doWork(KGang gang, KGangExtCASet extCASet, long minTime_Out, long minTime_Login) throws Exception {

		KGangMember sirMem = gang.searchPosition(KGangPositionEnum.军团长);
		if (sirMem == null || sirMem.getLastLoginTime() < minTime_Out) {
			
			Method method = KGangLogic.class.getDeclaredMethod("searchMemForAutoMoveOver", KGang.class, long.class, long.class);
			method.setAccessible(true);
			//KGang gang, long minTime, long excuteRoleId
			KGangMember mem = (KGangMember)method.invoke(null, gang, minTime_Login, sirMem == null ? 0L : sirMem._roleId);
			method.setAccessible(false);
			if (mem == null || mem.getType() == KGangPositionEnum.军团长.sign) {
				// 找不到合适的继任者
				return "找不到合适的继任者";
			}

			mem.setType(KGangPositionEnum.军团长.sign);
			if (sirMem != null) {
				sirMem.setType(KGangPositionEnum.成员.sign);
			}

			// 军团频道；上浮提示；日志
			String tips = StringUtil.format(GangTips.军团长xx天内未登陆系统其职位自动转让给了x, sirMem == null ? "" : sirMem.getExtRoleName(), KGangConfig.getInstance().AutoMoveOver_OutDay, mem.getExtRoleName());
			KGangLogic.addDialy(gang, extCASet, tips, true, true, true);

			// 更新成员列表
			KSyncMemberListMsg.sendMsg(gang, Arrays.asList(mem), null);
			KSyncGangDataMsg.sendMsg(gang);

			// 群发邮件
			KGangMsgPackCenter.sendMailToAllMems(gang, GangTips.军团管理邮件标题, tips);

			FlowManager.logOther(gang.getId(), OtherFlowTypeEnum.自动转让团长, "军团名称:" + gang.getName() + ";新团长角色ID:" + mem._roleId);
			return "军团名称:" + gang.getName() + ";新团长角色ID:" + mem._roleId;
		} else {
			return "无须禅让";
		}
	}
	
	
	
//	private static KGangMember searchMemForAutoMoveOver(KGang gang, long minTime, long excuteRoleId) {
//		List<KGangMember> mems = new ArrayList<KGangMember>(gang.getAllElementsCache().values());
//
//		KGangMember mem = null;
//		for (KGangMember temp : mems) {
//			if (temp._roleId == excuteRoleId) {
//				continue;
//			}
//			if (temp.isSirs()) {
//				// 找到团长或副团长
//				return temp;
//			}
//
//			if (temp.getLastLoginTime() > minTime) {
//				if (mem == null || temp.getTotalContribution() > mem.getTotalContribution()) {
//					mem = temp;
//				}
//			}
//		}
//		return mem;
//	}	
}
