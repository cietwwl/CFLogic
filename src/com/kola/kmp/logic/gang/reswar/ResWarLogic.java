package com.kola.kmp.logic.gang.reswar;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.KGameCheatCenter.CheatResult;
import com.kola.kmp.logic.combat.ICombatCommonResult;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.competition.KCompetitionManager;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.OtherFlowTypeEnum;
import com.kola.kmp.logic.flow.KRoleAttrModifyType;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.gang.KGangMember;
import com.kola.kmp.logic.gang.KGangRoleExtCACreator;
import com.kola.kmp.logic.gang.KRoleGangData;
import com.kola.kmp.logic.gang.reswar.KResWarDataManager.KLevyRewardDataManager.LevyRewardData;
import com.kola.kmp.logic.gang.reswar.KResWarDataManager.KRoleLvRewardBaseDataManager.RoleLvData;
import com.kola.kmp.logic.gang.reswar.ResWarCity.CityWarData;
import com.kola.kmp.logic.gang.reswar.ResWarCity.CityWarData.GangData;
import com.kola.kmp.logic.gang.reswar.ResWarCity.ResPoint;
import com.kola.kmp.logic.gang.reswar.ResWarCityBidRank.BPElement;
import com.kola.kmp.logic.gang.reswar.message.KGrwSynMsg;
import com.kola.kmp.logic.gang.reswar.message.KGrwSynResPointsMsg;
import com.kola.kmp.logic.gang.reswar.message.KGrwSynWarInfosMsg;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.ResultStructs.GangResWarResult_Bid;
import com.kola.kmp.logic.util.ResultStructs.GangResWarResult_Join;
import com.kola.kmp.logic.util.ResultStructs.GangResWarResult_Occ;
import com.kola.kmp.logic.util.ResultStructs.GangResultExt;
import com.kola.kmp.logic.util.tips.GangResWarTips;
import com.kola.kmp.logic.util.tips.GangTips;
import com.kola.kmp.logic.util.tips.ShopTips;

public class ResWarLogic {

	/**
	 * <pre>
	 * 停止当前军团资源战、重新加载配置，且重新开启军团资源战
	 * 不允许在军团资源战在进入、开战状态下进行重启操作
	 * 
	 * @deprecated 一般在测试环境中使用GM指令调用
	 * @author CamusHuang
	 * @creation 2013-10-11 下午10:57:08
	 * </pre>
	 */
	public static CheatResult stopAndRestartWar(boolean isReloadXmlConfig, boolean isForce) {

		// 保存数据
		ResWarDataCenter.saveData("-" + UtilTool.DATE_FORMAT7.format(new Date()) + "-动态重启军团资源战前备份");

		ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战：开始重启...");
		ResWarDataCenter.rwLock.writeLock().lock();
		try {
			// 重新加载配置
			if (isReloadXmlConfig) {
				ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战：>>>>>> 重新加载配置");
				try {
					KResWarConfig.init(null);
				} catch (KGameServerException e) {
					CheatResult result = new CheatResult();
					result.tips = e.getMessage();
					return result;
				}
			}

			// 停止当前军团资源战
			ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战：>>>>>> 停止当前军团资源战");
			CheatResult result = ResWarStatusManager.stopGangWar(isForce);
			if (!result.isSuccess) {
				return result;
			}

			// 初始化军团资源战的状态
			ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战：>>>>>> 重启军团资源战");
			ResWarStatusManager.notifyCacheLoadComplete();

			result = new CheatResult();
			result.isSuccess = true;
			return result;
		} finally {
			ResWarDataCenter.rwLock.writeLock().unlock();
		}
	}

	public static GangResultExt dealMsg_levyCity(KRole role, int cityId) {
		// 休息或报名期间，是占领方军团成员，且当天未征收

		GangResultExt result = new GangResultExt();

		if (ResWarStatusManager.getNowStatus() != ResWarStatusEnum.BID_START || ResWarStatusManager.getNowStatus() != ResWarStatusEnum.REST_START) {
			result.tips = GangResWarTips.对不起现阶段不能征收;
			return result;
		}

		long gangId = KSupportFactory.getGangSupport().getGangIdByRoleId(role.getId());
		if (gangId < 1) {
			result.tips = GangTips.对不起您不属于任何军团;
			return result;
		}

		long occGangId = 0;
		ResWarDataCenter.rwLock.readLock().lock();
		try {
			ResWarCity city = ResWarDataCenter.allCityMap.get(cityId);
			if (city == null) {
				result.tips = GangResWarTips.不存在此城市;
				return result;
			}
			GangData gangData = city.getOccGangData();
			if (gangData != null) {
				occGangId = gangData.gangId;
			}
		} finally {
			ResWarDataCenter.rwLock.readLock().unlock();
		}

		if (occGangId < 1 || gangId != occGangId) {
			result.tips = GangResWarTips.你的军团未占领此城市;
			return result;
		}

		KRoleGangData roleData = KGangRoleExtCACreator.getData(role.getId(), true);
		roleData.rwLock.lock();
		try {
			if (roleData.isLevyedCity(cityId)) {
				result.tips = GangResWarTips.你今天已征收过此城市;
				return result;
			}

			// 发送征收奖励
			// 每日获取个人贡献=（1+int（人物等级/20))*占领贡献基数
			// 经验奖励=等级对应经验奖励基数*领地奖励级别经验系数
			// 金币奖励=等级对应金币奖励基数*领地奖励级别金币系数

			int cityLv = KResWarDataManager.mCityTempManager.getData(cityId).citylv;
			int contribution = ExpressionForLevy(KCurrencyTypeEnum.GANG_CONTRIBUTION, role.getLevel(), cityLv);
			KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), KCurrencyTypeEnum.GANG_CONTRIBUTION, contribution, PresentPointTypeEnum.城市征收, false);
			int gold = ExpressionForLevy(KCurrencyTypeEnum.GOLD, role.getLevel(), cityLv);
			KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), KCurrencyTypeEnum.GOLD, gold, PresentPointTypeEnum.城市征收, true);
			int exp = ExpressionForLevy(null, role.getLevel(), cityLv);
			KSupportFactory.getRoleModuleSupport().addExp(role.getId(), exp, KRoleAttrModifyType.军团资源战);

			roleData.recordLevyCity(cityId);
			result.isSucess = true;
			result.tips = GangResWarTips.征收成功;
			//
			result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, KCurrencyTypeEnum.GANG_CONTRIBUTION.extName, contribution));
			result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, KCurrencyTypeEnum.GOLD.extName, gold));
			result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, KGameAttrType.EXPERIENCE.getExtName(), exp));
			return result;
		} finally {
			roleData.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 城市征收公式
	 * 
	 * @param type
	 * @param roleLv
	 * @param cityLv
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-12 下午4:47:35
	 * </pre>
	 */
	private static int ExpressionForLevy(KCurrencyTypeEnum type, int roleLv, int cityLv) {

		// 每日获取个人贡献=（1+int（人物等级/20))*占领贡献基数
		// 经验奖励=等级对应经验奖励基数*领地奖励级别经验系数
		// 金币奖励=等级对应金币奖励基数*领地奖励级别金币系数

		if (type == KCurrencyTypeEnum.GANG_CONTRIBUTION) {
			LevyRewardData data = KResWarDataManager.mLevyRewardDataManager.getData(cityLv);
			return (1 + (roleLv / 20)) * data.contribution;
		}
		if (type == KCurrencyTypeEnum.GOLD) {
			LevyRewardData data = KResWarDataManager.mLevyRewardDataManager.getData(cityLv);
			RoleLvData data2 = KResWarDataManager.mRoleLvDataManager.getData(roleLv);
			return data2.gold * data.gold;
		}

		// 经验
		LevyRewardData data = KResWarDataManager.mLevyRewardDataManager.getData(cityLv);
		RoleLvData data2 = KResWarDataManager.mRoleLvDataManager.getData(roleLv);
		return data2.exp * data.exp;
	}

	public static GangResWarResult_Bid dealMsg_bidCity(KRole role, int cityId, int value) {

		GangResWarResult_Bid result = new GangResWarResult_Bid();

		KGang gang = KGangLogic.getGangByRoleId(role.getId());
		if (gang == null) {
			result.tips = GangTips.对不起您不属于任何军团;
			return result;
		}
		KGangMember mem = gang.getMember(role.getId());
		if (mem == null || !mem.isSirs()) {
			result.tips = GangTips.只有团长和副团长才有此权限;
			return result;
		}

		long gangId = gang.getId();
		ResWarDataCenter.rwLock.writeLock().lock();
		try {

			if (ResWarStatusManager.getNowStatus() != ResWarStatusEnum.BID_START) {
				result.tips = GangResWarTips.对不起现阶段不能竞价;
				return result;
			}

			ResWarCity city = ResWarDataCenter.allCityMap.get(cityId);
			if (city == null) {
				result.tips = GangResWarTips.不存在此城市;
				return result;
			}

			BPElement element = city.bidRank.getElement(gangId);
			int existPrice = element == null ? 0 : element.price;
			int minPrice = 0;
			if (existPrice < 1) {
				minPrice = KResWarConfig.FirstBidPrice;
			} else {
				minPrice = KResWarConfig.OtherBidPrice;
			}
			if (value < minPrice) {
				result.tips = StringUtil.format(GangResWarTips.至少输入x资金, minPrice);
				return result;
			}

			CommonResult deleteResult = KSupportFactory.getGangSupport().changeGangResource(gangId, -value);
			if (!deleteResult.isSucess) {
				result.tips = deleteResult.tips;
				return result;
			}

			city.bidRank.notifyBid(gang, value);

			result.isSucess = true;
			result.tips = GangResWarTips.竞价成功;
			result.addDataUprisingTips(deleteResult.tips);

			//
			element = city.bidRank.getElement(gangId);
			result.我的竞价 = element == null ? 0 : element.price;
			result.追加或竞价规定输入的金额 = result.我的竞价 < 1 ? KResWarConfig.FirstBidPrice : KResWarConfig.OtherBidPrice;
			return result;
		} finally {
			ResWarDataCenter.rwLock.writeLock().unlock();

			if (result.isSucess) {
				// 财产日志
				String tips = StringUtil.format("资源战竞价;城市ID:{};增加金额:{};最终金额:{}", cityId, value, result.我的竞价);
				FlowManager.logOther(role.getId(), OtherFlowTypeEnum.资源战竞价, tips);
			}
		}
	}

	public static GangResWarResult_Occ dealMsg_occResPoint(KRole role, int cityId, int resPointId, boolean isConfirm) {
		GangResWarResult_Occ result = new GangResWarResult_Occ();

		long gangId = KSupportFactory.getGangSupport().getGangIdByRoleId(role.getId());
		if (gangId < 1) {
			result.tips = GangTips.对不起您不属于任何军团;
			return result;
		}

		ResWarDataCenter.rwLock.writeLock().lock();
		try {

			if (ResWarStatusManager.getNowStatus() != ResWarStatusEnum.WAR_START) {
				result.tips = GangResWarTips.对不起现阶段不能进行此操作;
				return result;
			}

			ResWarCity city = ResWarDataCenter.allCityMap.get(cityId);
			if (city == null) {
				result.tips = GangResWarTips.不存在此城市;
				return result;
			}

			if (city.isWarEnd()) {
				result.tips = GangResWarTips.此城市争夺胜负已分;
				return result;
			}

			// boolean 是否成功(失败：PK中、自己占领中、需要PK)
			CityWarData warData = city.getCityWarData();
			if (warData == null || !warData.isGangJoined(gangId)) {
				result.tips = GangResWarTips.您的军团未参与此处争夺;
				return result;
			}

			ResPoint resPoint = city.resPointManager.get(resPointId);
			if (resPoint == null) {
				result.tips = GangResWarTips.不存在此资源点;
				return result;
			}

			// 已经被我方占领
			if (resPoint.getOccGangId() == gangId) {
				result.tips = GangResWarTips.此资源点已被我方占领;
				return result;
			}

			// 先看有没有人争夺中（由于争夺中，原占领人可以放弃，所以要优先进行争夺检查）
			if (resPoint.getPkGangId() > 0) {
				result.tips = GangResWarTips.此资源点正在争夺中;
				return result;
			}

			result.syncCitys = new HashSet<ResWarCity>();
			// 二次确认
			if (isConfirm) {
				// 清理玩家的占领点
				result.syncCitys.addAll(ResWarDataCenter.clearOccedResPoint(gangId, role.getId()));
			} else {
				// 未进行二次确认，检查是否有占领点
				Object[] objs = ResWarDataCenter.searchOccedResPoint(gangId, role.getId());
				if (objs != null) {
					result.confirmOccCity = (ResWarCity) objs[0];
					result.confirmOccPoint = (ResPoint) objs[1];
					return result;
				}
			}

			// 被对方占领中
			if (resPoint.getOccGangId() > 0) {
				// 需要PK
				resPoint.setPK(gangId, role);
				result.syncCitys.add(city);
				//
				result.isGoPVP = true;
				result.resPoint = resPoint;
				result.tips = UtilTool.getNotNullString(null);
				return result;
			}

			// 未有人占领->直接占领
			resPoint.setOcc(gangId, role);
			result.syncCitys.add(city);
			//
			result.isSucess = true;
			result.tips = GangResWarTips.占领成功;
			result.resPoint = resPoint;
			return result;
		} finally {
			ResWarDataCenter.rwLock.writeLock().unlock();
		}
	}

	public static GangResWarResult_Join dealMsg_giveUpResPoint(KRole role, int cityId, int resPointId) {
		GangResWarResult_Join result = new GangResWarResult_Join();

		long gangId = KSupportFactory.getGangSupport().getGangIdByRoleId(role.getId());
		if (gangId < 1) {
			result.tips = GangTips.对不起您不属于任何军团;
			return result;
		}

		ResWarDataCenter.rwLock.writeLock().lock();
		try {

			if (ResWarStatusManager.getNowStatus() != ResWarStatusEnum.WAR_START) {
				result.tips = GangResWarTips.对不起现阶段不能进行此操作;
				return result;
			}

			ResWarCity city = ResWarDataCenter.allCityMap.get(cityId);
			if (city == null) {
				result.tips = GangResWarTips.不存在此城市;
				return result;
			}

			if (city.isWarEnd()) {
				result.tips = GangResWarTips.此城市争夺胜负已分;
				return result;
			}

			CityWarData warData = city.getCityWarData();
			if (warData == null || !warData.isGangJoined(gangId)) {
				result.tips = GangResWarTips.您的军团未参与此处争夺;
				return result;
			}

			ResPoint resPoint = city.resPointManager.get(resPointId);
			if (resPoint == null) {
				result.tips = GangResWarTips.不存在此资源点;
				return result;
			}

			if (resPoint.getOccGangId() != gangId) {
				result.tips = GangResWarTips.此资源点未被我方占领;
				return result;
			}

			// 执行放弃
			resPoint.clearOcc();

			result.isSucess = true;
			result.tips = GangResWarTips.成功放弃资源点;
			result.city = city;
			return result;
		} finally {
			ResWarDataCenter.rwLock.writeLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 出产积分
	 * 
	 * @author CamusHuang
	 * @creation 2013-9-24 下午9:16:23
	 * </pre>
	 */
	static void onTimeSignalForProduceScore() {
		ResWarDataCenter.rwLock.writeLock().lock();
		try {
			if (ResWarStatusManager.getNowStatus() != ResWarStatusEnum.WAR_START) {
				return;
			}
			for (ResWarCity city : ResWarDataCenter.allCityMap.values()) {
				if (city.isWarEnd()) {
					continue;
				}

				city.notifyForProduceScore();

				// 同步人数、积分
				KGrwSynWarInfosMsg.pushMsg(city);
			}
		} finally {
			ResWarDataCenter.rwLock.writeLock().unlock();
		}
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
	static boolean onTimeSignalForJudgeInWar() {
		ResWarDataCenter.rwLock.writeLock().lock();
		try {
			if (ResWarStatusManager.getNowStatus() != ResWarStatusEnum.WAR_START) {
				return true;
			}

			return ResWarDataCenter.judgeInWar();
		} finally {
			ResWarDataCenter.rwLock.writeLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 定时扫描处理PVP玩家非战斗、离线等错误状态
	 * 处理占领超时的资源点
	 * 不干预PVP流程，目的在于释放资源点
	 * 
	 * @author CamusHuang
	 * @creation 2013-9-24 下午9:16:23
	 * </pre>
	 */
	static void onTimeSignalForWarErrorScan() {
		ResWarDataCenter.rwLock.writeLock().lock();
		try {
			if (ResWarStatusManager.getNowStatus() != ResWarStatusEnum.WAR_START) {
				return;
			}
			for (ResWarCity city : ResWarDataCenter.allCityMap.values()) {
				if (city.notifyForWarErrorScan()) {
					// 同步资源点数据
					KGrwSynResPointsMsg.pushMsg(city);
				}
			}
		} finally {
			ResWarDataCenter.rwLock.writeLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 处理在线人数
	 * 处理PK信息
	 * 不干预PVP流程，目的在于释放资源点
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2014-5-15 上午10:06:38
	 * </pre>
	 */
	static void notifyRoleLeave(long roleId) {
		ResWarDataCenter.rwLock.writeLock().lock();
		try {
			if (ResWarStatusManager.getNowStatus() != ResWarStatusEnum.WAR_START) {
				return;
			}
			for (ResWarCity city : ResWarDataCenter.allCityMap.values()) {
				if (city.isWarEnd()) {
					continue;
				}
				boolean[] bools = city.notifyForRoleLeave(roleId);
				if (bools[0]) {
					// 同步人数、积分
					KGrwSynWarInfosMsg.pushMsg(city);
				}
				if (bools[1]) {
					// 同步资源点数据
					KGrwSynResPointsMsg.pushMsg(city);
				}
			}
		} finally {
			ResWarDataCenter.rwLock.writeLock().unlock();
		}
	}

	public static void dealMsg_existCity(KRole role, int cityId) {

		long gangId = KSupportFactory.getGangSupport().getGangIdByRoleId(role.getId());
		if (gangId < 1) {
			return;
		}

		ResWarCity city = null;

		ResWarDataCenter.rwLock.writeLock().lock();
		try {

			if (ResWarStatusManager.getNowStatus() != ResWarStatusEnum.WAR_START) {
				return;
			}

			city = ResWarDataCenter.allCityMap.get(cityId);
			if (city == null) {
				return;
			}

			if (city.isWarEnd()) {
				return;
			}

			CityWarData warData = city.getCityWarData();
			if (warData == null || !warData.isGangJoined(gangId)) {
				return;
			}

			warData.leave(role.getId());

		} finally {
			ResWarDataCenter.rwLock.writeLock().unlock();
		}

		// 同步人数、积分
		KGrwSynWarInfosMsg.pushMsg(city);
	}

	public static GangResWarResult_Join dealMsg_joinCity(KRole role, int cityId) {

		// 开战中、参战人员才能获取

		GangResWarResult_Join result = new GangResWarResult_Join();

		ResWarCity warCity = ResWarDataCenter.allCityMap.get(cityId);
		if (warCity == null) {
			result.tips = GangResWarTips.不存在此城市;
			return result;
		}

		long gangId = KSupportFactory.getGangSupport().getGangIdByRoleId(role.getId());

		ResWarDataCenter.rwLock.writeLock().lock();
		try {

			if (ResWarStatusManager.getNowStatus() != ResWarStatusEnum.WAR_START) {
				result.tips = GangResWarTips.军团资源战未开始;
				return result;
			}

			if (warCity.isWarEnd()) {
				result.tips = GangResWarTips.此城市争夺胜负已分;
				return result;
			}

			CityWarData warData = warCity.getCityWarData();
			if (warData == null || !warData.isGangJoined(gangId)) {
				result.tips = GangResWarTips.您的军团未参与此处争夺;
				return result;
			}

			//
			warData.join(gangId, role.getId());

			result.isSucess = true;
			result.tips = UtilTool.getNotNullString(null);
			result.city = warCity;
			return result;
		} finally {
			ResWarDataCenter.rwLock.writeLock().unlock();
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
	public static void gotoPVPMap(KRole role, int cityId, ResPoint point) {

		// 飞入关卡，在不同的出生点出生
		GangResWarAtt grwAtt = new GangResWarAtt(cityId, point.id, point.getOccRoleId());
		KActionResult<Integer> result = KSupportFactory.getCombatModuleSupport().fightWithAI(role, point.getOccRoleId(), KCombatType.GANG_RESWAR, KCompetitionManager.battlefield, grwAtt);
		if (!result.success) {
			// 不成功，清理PK标志
			point.clearPk();
			KDialogService.sendUprisingDialog(role, result.tips);
		}
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
	public static void notifyBattleFinished(KRole role, ICombatCommonResult result) {

		GangResWarAtt grwAtt = null;
		try {
			grwAtt = (GangResWarAtt) result.getAttachment();
		} catch (Exception e) {
			ResWarDataCenter.RESWAR_LOGGER.error("响应军团资源战PVP（AI）结果时发生异常！", e);
			return;
		}

		if (grwAtt == null) {
			return;
		}

		boolean isWin = result.isWin();

		ResWarCity warCity = ResWarDataCenter.allCityMap.get(grwAtt.cityId);
		if (warCity == null) {
			// 不存在此城市，无奖励
			KGrwSynMsg.sendBattleResult(role, isWin);
			return;
		}

		long gangId = KSupportFactory.getGangSupport().getGangIdByRoleId(role.getId());

		long orgRoleId = grwAtt.oppRoleId;
		boolean isOccSuccess = false;

		ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战：PVP（AI）结果通知：角色{} vs 角色{} {}！", role.getId(), orgRoleId, isWin ? "胜出" : "战败");
		ResWarDataCenter.rwLock.writeLock().lock();
		try {
			if (ResWarStatusManager.getNowStatus() != ResWarStatusEnum.WAR_START) {
				// 对战已结束，无奖励
				KGrwSynMsg.sendBattleResult(role, isWin);
				return;
			}

			if (warCity.isWarEnd()) {
				// 对战已结束，无奖励
				KGrwSynMsg.sendBattleResult(role, isWin);
				return;
			}

			CityWarData warData = warCity.getCityWarData();
			if (warData == null || !warData.isGangJoined(gangId)) {
				// 您的军团未参与此处争夺，无奖励
				KGrwSynMsg.sendBattleResult(role, isWin);
				return;
			}

			ResPoint point = warCity.resPointManager.get(grwAtt.pointId);
			if (point == null) {
				// 不存在此资源点，无奖励
				KGrwSynMsg.sendBattleResult(role, isWin);
				return;
			}

			if (point.getPkRoleId() != role.getId()) {
				// 挑战者与资源点记录不符，无奖励
				KGrwSynMsg.sendBattleResult(role, isWin);
				return;
			}

			ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战：PVP（AI）结果通知：角色{} vs 角色{} {}！----【有效】", role.getId(), orgRoleId, isWin ? "胜出" : "战败");

			// 结算以及发送结算界面消息
			// 胜出或资源点未被占领则成功占领
			if (isWin || point.getOccGangId() < 1) {
				isOccSuccess = true;
				// 设置占领信息
				point.setOcc(gangId, role);
				// 无奖励
			} else {
				// 无奖励
			}
			// 清除PK信息
			point.clearPk();

			KGrwSynMsg.sendBattleResult(role, isWin);
		} finally {
			ResWarDataCenter.rwLock.writeLock().unlock();
		}

		if (isOccSuccess) {
			// 通知原占领者
			ResPointBeSeizedManager.newEvent(orgRoleId, grwAtt.cityId, grwAtt.pointId, role);

			// 系统通知
			ResWarSystemBrocast.onPVPWin();

			// 同步资源点数据
			KGrwSynResPointsMsg.pushMsg(warCity);
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
	static class GangResWarAtt {
		final int cityId;
		final int pointId;
		final long oppRoleId;

		private GangResWarAtt(int cityId, int pointId, long oppRoleId) {
			this.cityId = cityId;
			this.pointId = pointId;
			this.oppRoleId = oppRoleId;
		}
	}

}
