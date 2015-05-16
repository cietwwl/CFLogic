package com.kola.kmp.logic.gang;

import java.text.ParseException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kgame.cache.DataCacheAccesserFactory;
import com.kola.kgame.cache.gang.GangIntegrateData;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.RunTimeTask;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.OtherFlowTypeEnum;
import com.kola.kmp.logic.rank.gang.KGangRankLogic;
import com.kola.kmp.logic.rank.teampvp.KTeamPVPRankLogic;

/**
 * <pre>
 * 这是一个即时保存排行榜的补丁
 * 
 * @author CamusHuang
 * @creation 2013-7-24 上午2:58:19
 * </pre>
 */
public class DismissGangPatch implements RunTimeTask {
	private static Logger _LOGGER = KGameLogger.getLogger(DismissGangPatch.class);

//	public String run(String gangIdStr) {
//
//		long gangId = Long.parseLong(gangIdStr);
//
//		GangIntegrateData gangData = KGangModuleExtension.getGangAndSet(gangId);
	public String run(String gangName) {

		GangIntegrateData gangData = DataCacheAccesserFactory.getGangEntireDataCacheAccesser().getGangIntegrateData(gangName);
		if (gangData == null) {
			return "军团数据不存在";
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();

		gang.rwLock.lock();
		try {

			if (gang.memberSize() > 0) {
				return "军团成员数量=" + gang.memberSize();
			}
			// 人数为0，解散军团
			dismissGang(gang);
		} finally {
			gang.rwLock.unlock();
		}

		return "执行完毕";
	}

	private static void dismissGang(KGang gang) {
		// 取消角色ID与军团ID映射
		for (long roleId : gang.getAllElementRoleIds()) {
			KGangLogic.mGangMappingDataManager.removeRoleIdToGangId(roleId);
		}

		// 执行删除军团DB数据
		try {
			DataCacheAccesserFactory.getGangEntireDataCacheAccesser().deleteGang(gang.getId());
		} catch (KGameServerException e) {
			_LOGGER.error(e.getMessage(), e);
		}
		// 刷新军团排行榜
		KGangRankLogic.notifyGangDelete(gang.getId());

		FlowManager.logOther(gang.getId(), OtherFlowTypeEnum.军团解散, "军团名称:" + gang.getName());
	}
}
