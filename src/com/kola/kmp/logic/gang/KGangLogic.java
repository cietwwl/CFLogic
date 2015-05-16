package com.kola.kmp.logic.gang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;

import com.koala.game.KGameMessage;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.DataCacheAccesserFactory;
import com.kola.kgame.cache.GangEntireDataCacheAccesser;
import com.kola.kgame.cache.flowdata.FlowDataModuleFactory;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kgame.cache.gang.Gang;
import com.kola.kgame.cache.gang.GangIntegrateData;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.OtherFlowTypeEnum;
import com.kola.kmp.logic.gang.KGangDataStruct.GangContributionData;
import com.kola.kmp.logic.gang.KGangDataStruct.GangGoodsData;
import com.kola.kmp.logic.gang.KGangDataStruct.GangLevelData;
import com.kola.kmp.logic.gang.KGangDataStruct.GangTechTemplate;
import com.kola.kmp.logic.gang.KGangDataStruct.GangTechTemplate.TechLevelData;
import com.kola.kmp.logic.gang.KGangExtCASet.ApplicationCache;
import com.kola.kmp.logic.gang.KGangExtCASet.GangDialyCache;
import com.kola.kmp.logic.gang.KGangExtCASet.GangDialyCache.GangDialy;
import com.kola.kmp.logic.gang.KGangModuleExtension.CreateGangDataImpl;
import com.kola.kmp.logic.gang.message.KSyncAppChangeCountMsg;
import com.kola.kmp.logic.gang.message.KSyncContributionChangeCountMsg;
import com.kola.kmp.logic.gang.message.KSyncDialyMsg;
import com.kola.kmp.logic.gang.message.KSyncGangDataMsg;
import com.kola.kmp.logic.gang.message.KSyncMemberListMsg;
import com.kola.kmp.logic.gang.war.GangWarLogic;
import com.kola.kmp.logic.gang.war.KGangWarDataManager;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KGangMedalDataManager.GangMedalData;
import com.kola.kmp.logic.gang.war.message.KGWPushMsg;
import com.kola.kmp.logic.mail.KMailConfig;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.npc.message.KShowDialogMsg;
import com.kola.kmp.logic.other.KChatChannelTypeEnum;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KFunctionTypeEnum;
import com.kola.kmp.logic.other.KGangTecTypeEnum;
import com.kola.kmp.logic.other.KShopTypeEnum;
import com.kola.kmp.logic.rank.KRankDataManager;
import com.kola.kmp.logic.rank.KRankDataStructs.KGangRankGoodReward;
import com.kola.kmp.logic.rank.gang.GangRank;
import com.kola.kmp.logic.rank.gang.GangRankElementPower;
import com.kola.kmp.logic.rank.gang.KGangRankLogic;
import com.kola.kmp.logic.rank.gang.KGangRankTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.KGameUtilTool;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.ResultStructs.GangResult;
import com.kola.kmp.logic.util.ResultStructs.GangResultExt;
import com.kola.kmp.logic.util.ResultStructs.GangResult_AcceptInvite;
import com.kola.kmp.logic.util.ResultStructs.GangResult_AllowApp;
import com.kola.kmp.logic.util.ResultStructs.GangResult_DoContribution;
import com.kola.kmp.logic.util.ResultStructs.GangResult_Exit;
import com.kola.kmp.logic.util.ResultStructs.GangResult_SetPosition;
import com.kola.kmp.logic.util.ResultStructs.GangResult_UplvTech;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;
import com.kola.kmp.logic.util.tips.GangTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.ItemTips;
import com.kola.kmp.logic.util.tips.ShopTips;

/**
 * <pre>
 * 本类作为本模块的管理器,负责主要的逻辑功能
 * 1.军团列表（有排名）
 * 1.1 列表翻页
 * 1.2 查找军团
 * 1.3 查看（列表）军团信息
 * 2.申请加入军团（可以同时申请加入N个军团，已加入则返回失败提示）
 * 3.创建军团（扣钱，等级限制，一个角色只能加入一个军团，命名规则）
 * 3.1 军团频道（收听，发言）
 * 4. 军团等级系统
 * 4.1 增加经验
 * 4.2 自动升级（增加成员数量上限等）
 * 5.所属军团详细信息（基础信息，成员列表，动态日志，申请列表）
 * 5.1 设置军团公告（文字过滤规则）
 * 5.2 退出军团
 * 5.3 成员列表
 * 5.3.1 成员在线状态等刷新
 * 5.3.2 成员离线时间计算规则
 * 5.3.3 转让团长，任命/革除副团长，开除成员
 * 5.4 申请列表刷新
 * 5.4.1 批准加入军团
 * 5.4.2 拒绝申请
 * 5.4.3 自动清理规则？？
 * 5.5 动态日志刷新
 * 5.5.1 任名副团长日志
 * 5.6 军团科技实现
 * 5.7 军团信息刷新
 * 6. 捐献（个人贡献，军团资金）
 * 7. 个人贡献值增加
 * 8. 军团商店
 * 
 * (等级、战力、在线)变化时， 成员列表的更新处理
 * 1.等级变，刷新
 * 2.上线、下线，刷新
 * 3.战力变，忽略（等级和上下线已经同时更新了战力）
 * 
 * (等级、战力、在线)变化时， APP列表的更新处理
 * 1.等级变，忽略
 * 2.上线、下线，忽略
 * 3.战力变，忽略
 * 
 * @author camus
 * @creation 2012-12-30 下午2:50:58
 * </pre>
 */
public class KGangLogic {

	private static Logger _LOGGER = KGameLogger.getLogger(KGangLogic.class);

	/**
	 * <pre>
	 * 模糊搜索军团CD锁
	 * 本模块中，对于模糊搜索军团，需要对每个角色进行CD时间控制
	 * </pre>
	 */
	final static GangSearchCDManager mGangSearchCDManager = new GangSearchCDManager();

	/**
	 * <pre>
	 * 军团全局映射数据管理
	 * 例如：角色ID-》军团ID映射
	 * </pre>
	 */
	final static GangMappingDataManager mGangMappingDataManager = new GangMappingDataManager();

	// 用于军团全局保护：例如创建军团时避免命名冲突等
	final static ReentrantLock GangLock = new ReentrantLock();

	/**
	 * <pre>
	 * 
	 * 
	 * @param roleId
	 * @return 不存在则返回-1
	 * @author CamusHuang
	 * @creation 2013-2-1 上午11:39:18
	 * </pre>
	 */
	public static long getGangIdByRoleId(long roleId) {
		// 军团--通过角色ID获取所属的军团ID，是否建立军团全局角色ID-军团ID映射
		return mGangMappingDataManager.getGangId(roleId);
	}

	public static KGang getGangByRoleId(long roleId) {
		long gangId = getGangIdByRoleId(roleId);
		if (gangId < 1) {
			return null;
		}
		return KGangModuleExtension.getGang(gangId);
	}

	public static KGangExtCASet getGangExtCASetByRoleId(long roleId) {
		long gangId = getGangIdByRoleId(roleId);
		if (gangId < 1) {
			return null;
		}
		return KGangModuleExtension.getGangExtCASet(gangId);
	}

	public static GangIntegrateData getGangAndSetByRoleId(long roleId) {
		long gangId = getGangIdByRoleId(roleId);
		if (gangId < 1) {
			return null;
		}
		return KGangModuleExtension.getGangAndSet(gangId);
	}

	/**
	 * <pre>
	 * 清理指定角色的所有申请书
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2013-8-12 下午8:02:22
	 * </pre>
	 */
	public static void clearAppFromAllGangs(long roleId) {
		GangEntireDataCacheAccesser cache = DataCacheAccesserFactory.getGangEntireDataCacheAccesser();

		List<Gang> copyGangList = cache.getAllGangsCopy();
		for (Gang temp : copyGangList) {
			KGang gang = (KGang) temp;
			KGangExtCASet extCASet = KGangModuleExtension.getGangExtCASet(gang.getId());
			ApplicationCache appCache = extCASet.getAppCache();
			if (appCache.deleteApp(roleId)) {
				// 提示申请列表有变化
				KSyncAppChangeCountMsg.sendMsg(gang, -1);
			}
		}
	}

	/**
	 * <pre>
	 * 搜索已被指定角色申请的军团
	 * 
	 * @param roleId
	 * @return 返回军团ID
	 * @author CamusHuang
	 * @creation 2014-4-10 下午2:33:43
	 * </pre>
	 */
	static Set<Long> searchAppGangs(long roleId) {
		Set<Long> result = new HashSet<Long>();
		//
		GangEntireDataCacheAccesser cache = DataCacheAccesserFactory.getGangEntireDataCacheAccesser();
		List<Gang> copyGangList = cache.getAllGangsCopy();
		for (Gang temp : copyGangList) {
			KGang gang = (KGang) temp;
			KGangExtCASet extCASet = KGangModuleExtension.getGangExtCASet(gang.getId());
			ApplicationCache appCache = extCASet.getAppCache();
			if (appCache.containApp(roleId)) {
				result.add(temp.getId());
			}
		}
		return result;
	}

	static Set<Gang> searchGangs(String nameKey) {
		Set<Gang> result = new HashSet<Gang>();
		//
		GangEntireDataCacheAccesser cache = DataCacheAccesserFactory.getGangEntireDataCacheAccesser();
		List<Gang> copyGangList = cache.getAllGangsCopy();
		for (Gang temp : copyGangList) {
			if (temp.getName().contains(nameKey)) {
				result.add(temp);
			}
		}
		return result;
	}
	
	/**
	 * <pre>
	 * 搜索已加入军团的在线玩家
	 * 
	 * @return <军团ID,<角色ID>>
	 * @author CamusHuang
	 * @creation 2014-5-23 下午6:37:36
	 * </pre>
	 */
	public static Map<Long,List<Long>> searchOnlineRoleOfGang() {
		
		Map<Long,List<Long>> map = new HashMap<Long, List<Long>>();
		
		for (Long roleId : KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds()) {
			long gangId = KGangLogic.getGangIdByRoleId(roleId);
			if (gangId > 0) {
				List<Long> l = map.get(gangId);
				if(l==null){
					l = new ArrayList<Long>();
					map.put(gangId, l);
				}
				l.add(roleId);
			}
		}
		return map;
	}

	/**
	 * <pre>
	 * 添加日志
	 * 内部会同步日志给成员、浮动提示、军团频道发送
	 * 
	 * @param content
	 * @param isSyn 是否将日志同步给所有成员
	 * @param isUpTips 是否显示浮动提示
	 * @param isChannel 是否军团频道发送
	 * @return
	 * @author CamusHuang
	 * @creation 2013-10-18 下午7:07:54
	 * </pre>
	 */
	public static GangDialy addDialy(KGang gang, KGangExtCASet extCASet, String content, boolean isSyn, boolean isUpTips, boolean isChannel) {
		GangDialy dialy = null;
		gang.rwLock.lock();
		try {
			GangDialyCache dialyCache = extCASet.getDialyCache();
			dialy = dialyCache.addDialy(content);
			return dialy;
		} finally {
			gang.rwLock.unlock();

			// 同步日志给所有成员
			if (isSyn) {
				KSyncDialyMsg.sendMsg(gang, dialy);
			}

			// 通知现存成员：上浮提示
			if (isUpTips) {
				KGameMessage msg = KShowDialogMsg.createUprisingDialogMsg(content);
				KGangMsgPackCenter.sendMsgToMemebers(msg, gang);
			}
			// 通知现存成员：军团频道
			if (isChannel) {
				KSupportFactory.getChatSupport().sendChatToAnyChannel(KChatChannelTypeEnum.军团, content, gang.getId());
			}
		}
	}

	/**
	 * <pre>
	 * 创建军团
	 * 
	 * @param role
	 * @param gangName
	 * @return
	 * @author CamusHuang
	 * @creation 2013-1-30 下午6:23:04
	 * </pre>
	 */
	public static GangResult_UplvTech dealMsg_createGang(KRole role, String gangName) {
		GangResult_UplvTech result = new GangResult_UplvTech();
		
		// 检查功能是否开放
		String tempResult = KGameUtilTool.checkFunctionTypeOpen(role, KFunctionTypeEnum.军团);
		if (tempResult != null) {
			result.tips = tempResult;
			return result;
		}
		
		// 名称检查（上限为12个字符，6个汉字）
		int len = UtilTool.getStringLength(gangName);
		if (len < KGangConfig.getInstance().GangNameMinLen || len > KGangConfig.getInstance().GangNameMaxLen) {
			result.tips = StringUtil.format(GangTips.军团名称长度不合法限x至x个字符, KGangConfig.getInstance().GangNameMinLen, KGangConfig.getInstance().GangNameMaxLen);
			return result;
		}

		// 军团--军团名称敏感字检查
		String dirtyWords = KSupportFactory.getDirtyWordSupport().containDirtyWord(gangName);
		if (dirtyWords != null) {
			result.tips = StringUtil.format(GangTips.军团名称包含敏感字x, dirtyWords);
			return result;
		}

		GangLock.lock();
		try {

			// 军团名称同名检查
			GangEntireDataCacheAccesser cache = DataCacheAccesserFactory.getGangEntireDataCacheAccesser();
			if (!cache.checkGangNameUseable(gangName)) {
				// 命名冲突
				result.tips = GangTips.此名称已被抢注;
				return result;
			}

			int vipLv = KSupportFactory.getVIPModuleSupport().getVipLv(role.getId());

			// 角色等级
			if (role.getLevel() < KGangConfig.getInstance().CreateGangMinRoleLevel && vipLv < 1) {
				result.tips = StringUtil.format(GangTips.等级必须达到x级才能创建军团, KGangConfig.getInstance().CreateGangMinRoleLevel);
				return result;
			}

			// 军团--针对未加入军团的角色，建立角色锁，防止同时加入不同的军团
			if (!mGangMappingDataManager.lockRoleId(role.getId())) {
				result.tips = GlobalTips.您的操作太频繁了请歇一歇;
				return result;
			}
			try {
				KGang gang = getGangByRoleId(role.getId());
				if (gang != null) {
					result.tips = StringUtil.format(GangTips.您已加入军团x, gang.getExtName());
					return result;
				}

				// 货币数量
				if (KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), KGangConfig.getInstance().PayForCreateGang, UsePointFunctionTypeEnum.军团创建, true) < 0) {
					result.isGoMoneyUI = true;
					result.goMoneyUIType = KGangConfig.getInstance().PayForCreateGang.currencyType;
					result.goMoneyUICount = KGangConfig.getInstance().PayForCreateGang.currencyCount-KSupportFactory.getCurrencySupport().getMoney(role.getId(), KGangConfig.getInstance().PayForCreateGang.currencyType);
					result.tips = StringUtil.format(ShopTips.x货币数量不足x, KGangConfig.getInstance().PayForCreateGang.currencyType.extName, KGangConfig.getInstance().PayForCreateGang.currencyCount);
					return result;
				}

				// 军团--建立军团对象，放入缓存
				gang = new KGang(gangName, role.getId());
				KGangExtCASet set = new KGangExtCASet(gang);
				CreateGangDataImpl data = new CreateGangDataImpl(gang, set);
				cache.addGangToCache(data);

				// 向军团增加创建者作为团长
				gang.rwLock.lock();
				try {
					KGangMember member = new KGangMember(gang, role.getId(), KGangPositionEnum.军团长);
					gang.addMember(member);
				} finally {
					gang.rwLock.unlock();
				}

				//
				result.isSucess = true;
				result.tips = GangTips.军团创建成功;
				result.gang = gang;
				result.extCASet = set;
				result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, KGangConfig.getInstance().PayForCreateGang.currencyType.extName, KGangConfig.getInstance().PayForCreateGang.currencyCount));
				return result;
			} finally {
				mGangMappingDataManager.unlockRoleId(role.getId());
			}
		} finally {
			GangLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 申请加入军团
	 * 
	 * @param role
	 * @param gangId
	 * @return
	 * @author CamusHuang
	 * @creation 2013-1-30 下午6:22:02
	 * </pre>
	 */
	public static GangResultExt dealMsg_appGang(KRole role, long gangId) {

		GangResultExt result = new GangResultExt();
		// 军团--针对未加入军团的角色，建立角色锁，防止同时加入不同的军团
		if (!mGangMappingDataManager.lockRoleId(role.getId())) {
			result.tips = GlobalTips.您的操作太频繁了请歇一歇;
			return result;
		}
		
//		// 检查功能是否开放
//		String tempResult = KGameUtilTool.checkFunctionTypeOpen(role, KFunctionTypeEnum.军团);
//		if(tempResult!=null){
//			result.tips = tempResult;
//			return result;
//		}
		
		try {
			// 检查功能是否开放
			String tempResult = KGameUtilTool.checkFunctionTypeOpen(role, KFunctionTypeEnum.军团);
			if(tempResult!=null){
				result.tips = tempResult;
				return result;
			}
			
			GangIntegrateData gangData = getGangAndSetByRoleId(role.getId());
			if (gangData != null) {
				result.tips = StringUtil.format(GangTips.您已加入军团x, ((KGang) gangData.getGang()).getExtName());
				return result;
			}
			
			KRoleGangData roleData = KGangRoleExtCACreator.getData(role.getId(), false);
			if (roleData != null) {
				long time = roleData.getReleaseJoinGangTime();
				if (time > 0) {
					// 离开军团24小时内无法重新加入，限制剩余时间 00：00
					result.tips = StringUtil.format(GangTips.离开军团x小时内无法重新加入限制剩余时间x, KGangConfig.getInstance().JoinGangCDHour, UtilTool.genReleaseCDTimeString2(time));
					return result;
				}
			}
			
			Set<Long> appGangIds = searchAppGangs(role.getId());
			if (appGangIds.size() >= KGangConfig.getInstance().APP_MAXCOUNT) {
				result.tips = StringUtil.format(GangTips.最多只能同时申请x个军团, KGangConfig.getInstance().APP_MAXCOUNT);
				return result;
			}

			if (appGangIds.contains(gangId)) {
				result.tips = GangTips.您的申请还没审批请耐心等候;
				return result;
			}

			gangData = KGangModuleExtension.getGangAndSet(gangId);
			if (gangData == null) {
				result.tips = GangTips.不存在此军团;
				return result;
			}

			KGang gang = (KGang) gangData.getGang();
			KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();

			gang.rwLock.lock();
			try {

				ApplicationCache appCache = set.getAppCache();
				appCache.addApp(role.getId());

				result.isSucess = true;
				result.tips = GangTips.申请已成功发出;
				result.gang = gang;
				return result;
			} finally {
				gang.rwLock.unlock();
			}
		} finally {
			mGangMappingDataManager.unlockRoleId(role.getId());
		}
	}

	/**
	 * <pre>
	 * 取消申请加入军团
	 * 
	 * @param role
	 * @param gangId
	 * @return
	 * @author CamusHuang
	 * @creation 2013-1-30 下午6:22:02
	 * </pre>
	 */
	public static GangResultExt dealMsg_cancelAppGang(KRole role, long gangId) {

		GangResultExt result = new GangResultExt();

		GangIntegrateData gangData = KGangModuleExtension.getGangAndSet(gangId);
		if (gangData == null) {
			result.tips = GangTips.不存在此军团;
			return result;
		}

		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();

		gang.rwLock.lock();
		try {

			ApplicationCache appCache = set.getAppCache();
			appCache.deleteApp(role.getId());

			result.isSucess = true;
			result.tips = GangTips.申请已成功取消;
			result.gang = gang;
			return result;
		} finally {
			gang.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 批准申请
	 * 
	 * @param role
	 * @param appRoleId
	 * @return
	 * @author CamusHuang
	 * @creation 2013-1-30 下午6:22:37
	 * </pre>
	 */
	public static GangResult_AllowApp dealMsg_arrowApp(KRole role, KRole appRole) {
		// 权限，申请书，满员，已有军团，
		GangResult_AllowApp result = new GangResult_AllowApp();

		// 检查功能是否开放
		String tempResult = KGameUtilTool.checkFunctionTypeOpen(appRole, KFunctionTypeEnum.军团);
		if (tempResult != null) {
			result.tips = GlobalTips.此角色功能未开放;
			return result;
		}

		long appRoleId = appRole.getId();
		KRoleGangData roleData = KGangRoleExtCACreator.getData(appRoleId, false);
		if (roleData != null) {
			long time = roleData.getReleaseJoinGangTime();
			if (time > 0) {
				// 离开军团24小时内无法重新加入，限制剩余时间 00：00
				result.tips = StringUtil.format(GangTips.离开军团x小时内无法重新加入限制剩余时间x, KGangConfig.getInstance().JoinGangCDHour, UtilTool.genReleaseCDTimeString2(time));
				return result;
			}
		}

		GangIntegrateData gangData = getGangAndSetByRoleId(role.getId());
		if (gangData == null) {
			result.tips = GangTips.对不起您不属于任何军团;
			return result;
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();

		gang.rwLock.lock();
		try {

			KGangMember sirmem = gang.getMember(role.getId());
			if (sirmem == null || sirmem.getType() == KGangPositionEnum.成员.sign) {
				result.tips = GangTips.普通成员不具有此权限;
				return result;
			}

			GangLevelData lvData = KGangDataManager.mGangLevelDataManager.getLevelData(gang.getLevel());
			if (gang.memberSize() >= lvData.maxuser) {
				result.tips = GangTips.军团已满员;
				return result;
			}

			// 军团--针对未加入军团的角色，建立角色锁，防止同时加入不同的军团
			if (!mGangMappingDataManager.lockRoleId(appRoleId)) {
				result.tips = GlobalTips.服务器繁忙请稍候再试;
				return result;
			}
			try {

				ApplicationCache appCache = set.getAppCache();
				if (!appCache.deleteApp(appRoleId)) {
					result.tips = GangTips.此玩家没有申请加入你的军团;
					return result;
				}

				if (getGangIdByRoleId(appRoleId) > 0) {
					result.tips = GangTips.此玩家已加入其它军团;
					return result;
				}

				// 军团--DB加成员，放入军团内
				KGangMember newMember = new KGangMember(gang, appRoleId, KGangPositionEnum.成员);
				gang.addMember(newMember);
				//
				result.isSucess = true;
				result.tips = GangTips.审批成功;
				result.gang = gang;
				result.extCASet = set;
				result.targetMember = newMember;
				return result;
			} finally {
				mGangMappingDataManager.unlockRoleId(appRoleId);
			}
		} finally {
			gang.rwLock.unlock();
		}
	}

	public static GangResult_AcceptInvite dealMsg_acceptInvite(KRole oppRole, long gangId) {
		// 满员，已有军团，
		GangResult_AcceptInvite result = new GangResult_AcceptInvite();

		{
			KRoleGangData oppRoleData = KGangRoleExtCACreator.getData(oppRole.getId(), false);
			if (oppRoleData != null) {
				long time = oppRoleData.getReleaseJoinGangTime();
				if (time > 0) {
					// 离开军团24小时内无法重新加入，限制剩余时间 00：00
					result.tips = StringUtil.format(GangTips.离开军团x小时内无法重新加入限制剩余时间x, KGangConfig.getInstance().JoinGangCDHour, UtilTool.genReleaseCDTimeString2(time));
					return result;
				}
			}

			GangIntegrateData oppGangData = getGangAndSetByRoleId(oppRole.getId());
			if (oppGangData != null) {
				if (oppGangData.getGang().getId() == gangId) {
					result.tips = GangTips.您已加入此军团;
					return result;
				}
				result.tips = StringUtil.format(GangTips.您已加入军团x, ((KGang) oppGangData.getGang()).getExtName());
				return result;
			}
		}

		GangIntegrateData gangData = KGangModuleExtension.getGangAndSet(gangId);
		if (gangData == null) {
			result.tips = GangTips.不存在此军团;
			return result;
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();

		gang.rwLock.lock();
		try {

			GangLevelData lvData = KGangDataManager.mGangLevelDataManager.getLevelData(gang.getLevel());
			if (gang.memberSize() >= lvData.maxuser) {
				result.tips = GangTips.军团已满员;
				return result;
			}
			
			if(!gang.isInviteEffect(oppRole.getId())){
				result.tips = GangTips.此邀请已失效;
				return result;
			}

			// 军团--针对未加入军团的角色，建立角色锁，防止同时加入不同的军团
			if (!mGangMappingDataManager.lockRoleId(oppRole.getId())) {
				result.tips = GlobalTips.服务器繁忙请稍候再试;
				return result;
			}
			try {
				if (getGangIdByRoleId(oppRole.getId()) > 0) {
					result.tips = GangTips.您已加入其它军团;
					return result;
				}

				ApplicationCache appCache = set.getAppCache();
				if (appCache.deleteApp(oppRole.getId())) {
					result.isApp = true;
				}

				// 军团--DB加成员，放入军团内
				KGangMember newMember = new KGangMember(gang, oppRole.getId(), KGangPositionEnum.成员);
				gang.addMember(newMember);
				//
				result.isSucess = true;
				result.tips = StringUtil.format(GangTips.您已加入军团x, gang.getExtName());
				result.gang = gang;
				result.extCASet = set;
				result.opMember = newMember;
				return result;
			} finally {
				mGangMappingDataManager.unlockRoleId(oppRole.getId());
			}
		} finally {
			gang.rwLock.unlock();
		}
	}

	public static GangResultExt dealMsg_inviteForGang(KRole role, KRole oppRole) {
		// 已有军团，权限，邀请CD，满员，
		GangResultExt result = new GangResultExt();
		
		// 检查功能是否开放
		String tempResult = KGameUtilTool.checkFunctionTypeOpen(oppRole, KFunctionTypeEnum.军团);
		if (tempResult != null) {
			result.tips = GlobalTips.此角色功能未开放;
			return result;
		}

		GangIntegrateData gangData = getGangAndSetByRoleId(role.getId());
		if (gangData == null) {
			result.tips = GangTips.对不起您不属于任何军团;
			return result;
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();

		{
			GangIntegrateData oppGangData = getGangAndSetByRoleId(oppRole.getId());
			if (oppGangData != null) {
				if (oppGangData.getGang().getId() == gang.getId()) {
					result.tips = GangTips.此玩家属于您所在的军团;
					return result;
				}
				result.tips = GangTips.此玩家已加入其它军团;
				return result;
			}
		}

		gang.rwLock.lock();
		try {

			KGangMember sirmem = gang.getMember(role.getId());
			if (sirmem == null || sirmem.getType() == KGangPositionEnum.成员.sign) {
				result.tips = GangTips.普通成员不具有此权限;
				return result;
			}

			GangLevelData lvData = KGangDataManager.mGangLevelDataManager.getLevelData(gang.getLevel());
			if (gang.memberSize() >= lvData.maxuser) {
				result.tips = GangTips.军团已满员;
				return result;
			}

			// CD
			if (!gang.addInvite(oppRole.getId())) {
				result.tips = GangTips.请不要重复进行邀请;
				return result;
			}

			result.isSucess = true;
			result.tips = GangTips.邀请信息已发送成功;
			result.gang = gang;
			return result;
		} finally {
			gang.rwLock.unlock();
		}
	}

	public static CommonResult dealMsg_inviteForWorld(KRole role, String link) {
		// 已有军团，权限，邀请CD，满员，
		CommonResult result = new CommonResult();

		GangIntegrateData gangData = getGangAndSetByRoleId(role.getId());
		if (gangData == null) {
			result.tips = GangTips.对不起您不属于任何军团;
			return result;
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();

		gang.rwLock.lock();
		try {

			KGangMember sirmem = gang.getMember(role.getId());
			if (sirmem == null || !sirmem.isSirs()) {
				result.tips = GangTips.只有团长和副团长才有此权限;
				return result;
			}
			
			// 每个军团发送消息链接间隔时间不能少于30秒，冷却时间未到时发送链接时系统提示：“每次发送间隔不能少于30秒”发送失败
			long releaseTime = gang.checkInviteForWoldCDTime();
			if(releaseTime > 0){
				String timeStr = UtilTool.genReleaseCDTimeString(releaseTime);
				result.tips = StringUtil.format(GangTips.世界广播邀请CD剩余x时间, timeStr);
				return result;
			}

			GangLevelData lvData = KGangDataManager.mGangLevelDataManager.getLevelData(gang.getLevel());
			if (gang.memberSize() >= lvData.maxuser) {
				result.tips = GangTips.军团已满员;
				return result;
			}

			result.isSucess = true;
			result.tips = GangTips.邀请信息已发送成功;
			return result;
		} finally {
			gang.rwLock.unlock();

			if (result.isSucess) {
				// 发到世界广播
				// 【军团名称】（X级）军团诚邀各位加入【加入军团】
				link = StringUtil.format(link,GangTips.军团邀请链接名称);
				String content = StringUtil.format(GangTips.x军团名称x级军团诚邀各位x, gang.getExtName(),gang.getLevel(), link);
				KSupportFactory.getChatSupport().sendChatToWorldChannel(KMailConfig.SYS_MAIL_SENDER_NAME, content);
			}
		}
	}

	/**
	 * <pre>
	 * 计算捐献得到的军团经验
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-11 下午6:01:48
	 * </pre>
	 */
	private static int ExpressionForContribution(KCurrencyTypeEnum type, int roleLv, int expRate) {
		// 金币捐献：round(（1+人物等级/20）*金币捐献军团经验系数,0)=最终军团获得经验
		// 潜能捐献：round(（1+人物等级/20）*潜能捐献军团经验系数,0)=最终军团获得经验
		// 钻石捐献：钻石捐献军团经验系数*1

		switch (type) {
		case DIAMOND:
			return expRate;
		case GOLD:
		case POTENTIAL:
			return (1 + roleLv / 20) * expRate;
		case EMBLEM:
		case GANG_CONTRIBUTION:
		case SCORE:
		default:
			return expRate;
		}
	}

	/**
	 * <pre>
	 * 计算角色捐献、增加活跃度得到的军团繁荣度
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-11 下午6:01:48
	 * </pre>
	 */
	static int ExpressionForFlourish(KCurrencyTypeEnum type, long moneyCount, float roleLv) {
		//  金币捐献获得繁荣度：round(（1+人物等级/20）*金币繁荣度基数（待定）,0)
		//  潜能捐献获得繁荣度：round(（1+人物等级/20）*金币繁荣度基数（待定）,0)
		//  钻石捐献获得繁荣度：钻石数量*钻石繁荣度基数
		//  获得活跃度转换繁荣度：round(活跃度军团繁荣基数（待定）*人物等级/20,0)

		if (type == null) {
			// 活跃度
			return Math.round(KGangDataManager.mGangProsperityData.liveness_boom * roleLv / 20);
		}

		int baseRate = KGangDataManager.mGangProsperityData.moneyBase.get(type);

		switch (type) {
		case DIAMOND:
			return (int) moneyCount * baseRate;
		case GOLD:
		case POTENTIAL:
			return Math.round((1 + roleLv / 20) * baseRate);
		case EMBLEM:
		case GANG_CONTRIBUTION:
		case SCORE:
		default:
			return 0;
		}
	}

	/**
	 * <pre>
	 * 修改军团繁荣度
	 * 军团战报名期间可修改、报名结束时清0
	 * 
	 * @author CamusHuang
	 * @creation 2014-5-18 上午11:40:45
	 * </pre>
	 */
	static boolean addGangFlourish(KRole role, KGang gang, int addValue, boolean isShowTips) {
		// 条件检查
		if (!GangWarLogic.isCanSignUp()) {
			return false;
		}

		// 增加军团繁荣度
		gang.setFlourish(gang.getFlourish() + addValue);
		// 通知报名榜
		KGangRankLogic.notifyGangFlourish(gang, gang.getFlourish());

		if (isShowTips) {
			KDialogService.sendUprisingDialog(role, StringUtil.format(GangTips.军团活跃度加x, addValue));
		}

		return true;
	}

	/**
	 * <pre>
	 * 军团--捐献
	 * 
	 * @param role
	 * @param currencyType
	 * @param money
	 * @return
	 * @author CamusHuang
	 * @creation 2013-1-30 下午6:23:13
	 * </pre>
	 */
	public static GangResult_DoContribution dealMsg_doContribution(KRole role, KCurrencyTypeEnum moneyEnum) {
		long roleId = role.getId();
		GangResult_DoContribution result = new GangResult_DoContribution();

		if (!KGangDataManager.mGangContributionDataManager.containType(moneyEnum)) {
			result.tips = GangTips.不能捐献此类型的货币;
			return result;
		}

		// 非VIP玩家进行捐献时系统提示：“VIP1级以上成员才能进行钻石捐献”
		if (moneyEnum == KCurrencyTypeEnum.DIAMOND) {
			if (KSupportFactory.getVIPModuleSupport().getVipLv(role.getId()) < 1) {
				result.tips = GangTips.VIP1级以上成员才能进行钻石捐献;
				return result;
			}
		}

		GangIntegrateData gangData = getGangAndSetByRoleId(role.getId());
		if (gangData == null) {
			result.tips = GangTips.对不起您不属于任何军团;
			return result;
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();

		int addGangProsperity = 0;
		gang.rwLock.lock();
		try {
			KGangMember mem = gang.getMember(roleId);
			if (mem == null) {
				result.tips = GangTips.对不起您不属于任何军团;
				return result;
			}

			if (moneyEnum != KCurrencyTypeEnum.DIAMOND) {
				GangLevelData lvData = KGangDataManager.mGangLevelDataManager.getLevelData(gang.getLevel());
				// 每日捐献次数有限制
				int nowGangTime = gang.getContributionTime();
				if (nowGangTime >= lvData.donatetime) {
					result.tips = GangTips.军团今天接受捐献的次数已达极限;
					return result;
				}
			}

			int nowMemTime = mem.getContributionTime(moneyEnum.sign);
			GangContributionData gcData = KGangDataManager.mGangContributionDataManager.getData(moneyEnum.sign, nowMemTime + 1);
			if (gcData == null) {
				result.tips = StringUtil.format(GangTips.x类型捐献今日已达到次数上限, moneyEnum.extName);
				return result;
			}

			// 扣货币
			if (KSupportFactory.getCurrencySupport().decreaseMoney(roleId, gcData.price, UsePointFunctionTypeEnum.军团捐献, true) < 0) {
				result.isGoMoneyUI = true;
				result.goMoneyUIType = moneyEnum;
				result.goMoneyUICount = gcData.price.currencyCount-KSupportFactory.getCurrencySupport().getMoney(role.getId(), gcData.price.currencyType);
				result.tips = StringUtil.format(ShopTips.x货币数量不足x, gcData.price.currencyType.extName, gcData.price.currencyCount);
				return result;
			}

			// 全局次数+1
			if (moneyEnum != KCurrencyTypeEnum.DIAMOND) {
				gang.setContributionTime(gang.getContributionTime() + 1);
			}
			// 个人指定货币的次数加1
			mem.setContributionTime(moneyEnum.sign, nowMemTime + 1);

			//
			int addGangValue = ExpressionForContribution(moneyEnum, role.getLevel(), gcData.exp);// 军团得到的经验和资金
			addGangProsperity = ExpressionForFlourish(gcData.price.currencyType, gcData.price.currencyCount, (float) role.getLevel());
			int addMemberValue = gcData.GainContribution;// 成员得到的贡献值
			//
			// 增加军团资金、经验、繁荣度
			gang.changeResource(addGangValue);
			gang.setExp(gang.getExp() + addGangValue);
			// 增加个人贡献值
			KSupportFactory.getCurrencySupport().increaseMoney(roleId, KCurrencyTypeEnum.GANG_CONTRIBUTION, addMemberValue, PresentPointTypeEnum.军团捐献, true);
			// 记录对军团的贡献
			mem.addContribution(addMemberValue);

			//
			set.setContributionLastChangeTime(System.currentTimeMillis());
			//

			result.isSucess = true;
			result.tips = GangTips.捐献成功;
			result.gang = gang;
			result.member = mem;
			result.price = gcData.price;
			result.addUprisingTips(StringUtil.format(GangTips.x捐献x数量x货币, "", gcData.price.currencyCount, gcData.price.currencyType.extName));
			result.addDataUprisingTips(StringUtil.format(GangTips.个人贡献加x, addMemberValue));
			result.addDataUprisingTips(StringUtil.format(GangTips.军团经验加x, addGangValue));
			result.addDataUprisingTips(StringUtil.format(GangTips.军团资金加x, addGangValue));
			return result;
		} finally {
			gang.rwLock.unlock();

			if (result.isSucess) {
				// 增加军团资金、经验、繁荣度
				if (addGangFlourish(role, gang, addGangProsperity, false)) {
					result.addDataUprisingTips(StringUtil.format(GangTips.军团活跃度加x, addGangProsperity));
				}
			}
		}
	}

	/**
	 * <pre>
	 * 军团--任命职务
	 * 
	 * @param role
	 * @param newVicesirRoleId
	 * @return
	 * @author CamusHuang
	 * @creation 2013-1-30 下午6:22:23
	 * </pre>
	 */
	public static GangResult_SetPosition dealMsg_setPosition(KRole role, long targetRoleId, KGangPositionEnum position) {
		// 权限
		GangResult_SetPosition result = new GangResult_SetPosition();

		if (role.getId() == targetRoleId) {
			result.tips = GangTips.不能任命自己;
			return result;
		}

		GangIntegrateData gangData = getGangAndSetByRoleId(role.getId());
		if (gangData == null) {
			result.tips = GangTips.对不起您不属于任何军团;
			return result;
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();

		gang.rwLock.lock();
		try {
			// 权限
			KGangMember sirmem = gang.getMember(role.getId());
			if (sirmem == null || sirmem.getType() != KGangPositionEnum.军团长.sign) {
				result.tips = GangTips.只有团长才有此权限;
				return result;
			}

			KGangMember mem = gang.getMember(targetRoleId);
			if (mem == null) {
				result.tips = GangTips.此玩家不属于您所在的军团;
				return result;
			}

			KGangPositionEnum orgPosition = mem.getPositionEnum();

			if (orgPosition == position) {
				result.tips = GangTips.任命成功;
				return result;
			}

			if (gang.countPosition(position) >= position.size) {
				result.tips = GangTips.此职务人数已达极限;
				return result;
			}

			mem.setType(position.sign);
			//
			result.isSucess = true;
			result.tips = GangTips.任命成功;
			result.gang = gang;
			result.extCASet = set;
			result.opMember = sirmem;
			result.targetMember = mem;
			result.targetOrgPosition = orgPosition;
			return result;
		} finally {
			gang.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 开除成员
	 * 
	 * @param role
	 * @param fireRoleId
	 * @return
	 * @author CamusHuang
	 * @creation 2013-1-30 下午6:23:29
	 * </pre>
	 */
	public static GangResult_SetPosition dealMsg_fireMember(KRole role, long fireRoleId) {
		GangResult_SetPosition result = new GangResult_SetPosition();

		GangIntegrateData gangData = getGangAndSetByRoleId(role.getId());
		if (gangData == null) {
			result.tips = GangTips.对不起您不属于任何军团;
			return result;
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();

		gang.rwLock.lock();
		try {
			// 权限
			KGangMember sirmem = gang.getMember(role.getId());
			if (sirmem == null || !sirmem.isSirs()) {
				result.tips = GangTips.只有团长和副团长才有此权限;
				return result;
			}

			KGangMember mem = gang.getMember(fireRoleId);
			if (mem == null) {
				result.tips = GangTips.此玩家不属于您所在的军团;
				return result;
			}

			if (mem.isSirs()) {
				result.tips = GangTips.不能开除团长或副团长;
				return result;
			}

			result.targetOrgPosition = mem.getPositionEnum();
			// 军团--DB删成员，从军团内删除
			gang.notifyElementDelete(fireRoleId);
			//
			result.isSucess = true;
			result.tips = GangTips.开除成员成功;
			result.gang = gang;
			result.extCASet = set;
			result.opMember = sirmem;
			result.targetMember = mem;
			return result;
		} finally {
			gang.rwLock.unlock();

			if (result.isSucess) {
				FlowManager.logOther(gang.getId(), OtherFlowTypeEnum.被开除出军团, "军团名称:" + gang.getName() + ";执行角色ID:" + role.getId() + ";被开角色ID:" + fireRoleId);
			}
		}
	}

	public static GangResultExt dealMsg_ingoreApp(KRole role, long appRoleId) {
		// 权限，申请书
		GangResultExt result = new GangResultExt();

		GangIntegrateData gangData = getGangAndSetByRoleId(role.getId());
		if (gangData == null) {
			result.tips = GangTips.对不起您不属于任何军团;
			return result;
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();

		gang.rwLock.lock();
		try {
			KGangMember sirmem = gang.getMember(role.getId());
			if (sirmem == null || sirmem.getType() == KGangPositionEnum.成员.sign) {
				result.tips = GangTips.普通成员不具有此权限;
				return result;
			}

			ApplicationCache appCache = set.getAppCache();
			if (!appCache.deleteApp(appRoleId)) {
				result.tips = GangTips.此玩家没有申请加入你的军团;
				return result;
			}

			result.isSucess = true;
			result.tips = GangTips.成功忽略申请;
			result.gang = gang;
			return result;
		} finally {
			gang.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 转让团长职位
	 * 
	 * @param role
	 * @param nextSirRoleId
	 * @return
	 * @author CamusHuang
	 * @creation 2013-1-30 下午6:23:52
	 * </pre>
	 */
	public static GangResult_SetPosition dealMsg_makeOverSir(KRole role, KRole nextSirRole) {
		GangResult_SetPosition result = new GangResult_SetPosition();

		if (role.getId() == nextSirRole.getId()) {
			result.tips = GangTips.不能任命自己;
			return result;
		}

		// 角色等级
		if (nextSirRole.getLevel() < KGangConfig.getInstance().CreateGangMinRoleLevel) {
			result.tips = StringUtil.format(GangTips.等级必须达到x级才能接任军团长, KGangConfig.getInstance().CreateGangMinRoleLevel);
			return result;
		}

		GangIntegrateData gangData = getGangAndSetByRoleId(role.getId());
		if (gangData == null) {
			result.tips = GangTips.对不起您不属于任何军团;
			return result;
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();

		//
		gang.rwLock.lock();
		try {
			// 权限
			KGangMember sirmem = gang.getMember(role.getId());
			if (sirmem == null || sirmem.getType() != KGangPositionEnum.军团长.sign) {
				result.tips = GangTips.只有团长才有此权限;
				return result;
			}

			KGangMember mem = gang.getMember(nextSirRole.getId());
			if (mem == null) {
				result.tips = GangTips.此玩家不属于您所在的军团;
				return result;
			}

			// if (mem.getType() != KGangPositionEnum.副团长.sign) {
			// result.tips = GangTips.此玩家不是副团长;
			// return result;
			// }

			result.targetOrgPosition = mem.getPositionEnum();
			sirmem.setType(KGangPositionEnum.成员.sign);
			mem.setType(KGangPositionEnum.军团长.sign);
			//
			result.isSucess = true;
			result.tips = GangTips.任命成功;
			result.gang = gang;
			result.extCASet = set;
			result.opMember = sirmem;
			result.targetMember = mem;
			return result;
		} finally {
			gang.rwLock.unlock();

			if (result.isSucess) {
				FlowManager.logOther(gang.getId(), OtherFlowTypeEnum.转让团长, "军团名称:" + gang.getName() + ";原团长角色ID:" + role.getId() + ";新团长角色ID:" + nextSirRole);
			}
		}
	}

	/**
	 * <pre>
	 * 修改公告
	 * 
	 * @param roleId
	 * @param newNotice
	 * @return
	 * @author CamusHuang
	 * @creation 2013-1-30 下午6:24:03
	 * </pre>
	 */
	public static GangResult dealMsg_modifyNotice(long roleId, String newNotice) {
		GangResult result = new GangResult();

		KGang gang = getGangByRoleId(roleId);
		if (gang == null) {
			result.tips = GangTips.对不起您不属于任何军团;
			return result;
		}

		// 超长
		if (newNotice.length() > KGangConfig.getInstance().GangNoticeMaxLen) {
			result.tips = StringUtil.format(GangTips.军团公告限x个字, KGangConfig.getInstance().GangNoticeMaxLen);
			return result;
		}

		gang.rwLock.lock();
		try {
			KGangMember sirmem = gang.getMember(roleId);
			if (sirmem == null || !sirmem.isSirs()) {
				result.tips = GangTips.只有团长和副团长才有此权限;
				return result;
			}

			// 军团--以*号替换公告敏感字
			newNotice = KSupportFactory.getDirtyWordSupport().clearDirtyWords(newNotice, true);
			//
			gang.setNotice(newNotice);
			//
			result.isSucess = true;
			result.tips = GangTips.修改公告成功;
			result.gang = gang;
			return result;
		} finally {
			gang.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 退出军团
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2013-1-30 下午6:23:21
	 * </pre>
	 */
	public static GangResult_Exit dealMsg_exitGang(long roleId, boolean isDialogConfirm) {

		GangResult_Exit result = new GangResult_Exit();

		GangIntegrateData gangData = getGangAndSetByRoleId(roleId);
		if (gangData == null) {
			result.tips = GangTips.对不起您不属于任何军团;
			return result;
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();
		//
		gang.rwLock.lock();
		try {

			KGangMember mem = gang.getMember(roleId);
			if (mem == null) {
				result.tips = GangTips.对不起您不属于任何军团;
				return result;
			}

			if (mem.getType() == KGangPositionEnum.军团长.sign) {
				// 军团--团长欲退团-军团有2人以上时，不允许团长退团
				if (gang.memberSize() > 1) {
					result.tips = GangTips.请先删除其他成员再解散军团;
					return result;
				}

				if (!isDialogConfirm) {
					// 未进行二次确认
					result.isDismiss = true;
					return result;
				}

				// 普通成员离开军团
				gang.notifyElementDelete(roleId);
				// 军团--执行解散军团
				dismissGang(gang);
				result.isDismiss = true;
			} else {
				if (!isDialogConfirm) {
					// 未进行二次确认
					result.isGoConfirm = true;
					return result;
				}
				// 普通成员离开军团
				gang.notifyElementDelete(roleId);
			}

			result.isSucess = true;
			result.tips = GangTips.退出军团成功;
			result.gang = gang;
			result.extCASet = set;
			result.opMember = mem;
			return result;
		} finally {
			gang.rwLock.unlock();

			if (result.isSucess) {
				FlowManager.logOther(gang.getId(), OtherFlowTypeEnum.退出军团, "军团名称:" + gang.getName() + ";角色id:" + gang.getId());
			}
		}
	}

	/**
	 * <pre>
	 * 角色删除，退出军团
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2013-1-30 下午5:34:21
	 * </pre>
	 */
	static void notifyRoleDeleted(long roleId) {

		GangIntegrateData gangData = getGangAndSetByRoleId(roleId);
		if (gangData == null) {
			return;
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();

		KGangMember mem = null;

		gang.rwLock.lock();
		try {

			// 成员离开军团
			mem = gang.notifyElementDelete(roleId);
			if (mem == null) {
				return;
			}
			if (gang.memberSize() < 1) {
				// 人数为0，解散军团
				dismissGang(gang);
			} else {
				// 人数不为0
				// 离团后续操作
				actionAfterMemExit(gang, set, mem);
				//
				if (mem.getType() == KGangPositionEnum.军团长.sign) {
					// 军团--团长删角色-禅让
					autoMoveOverSire_delete(gang, set, mem);
				}
			}
		} finally {
			gang.rwLock.unlock();

			if (mem != null) {
				FlowManager.logOther(gang.getId(), OtherFlowTypeEnum.退出军团, "军团名称:" + gang.getName() + ";角色id:" + roleId + ";删除角色");
			}
		}
	}

	/**
	 * <pre>
	 * 军团跨天触发任务
	 * 
	 * @author CamusHuang
	 * @creation 2014-4-14 上午11:50:55
	 * </pre>
	 */
	static void notifyForDayTask() {

		long nowTime = System.currentTimeMillis();

		GangEntireDataCacheAccesser cache = DataCacheAccesserFactory.getGangEntireDataCacheAccesser();
		List<Gang> copyGangList = cache.getAllGangsCopy();
		for (Gang tempGang : copyGangList) {
			KGang gang = (KGang) tempGang;
			gang.rwLock.lock();
			try {
				// 帮会当天捐献次数清0
				boolean isGangChange = false;
				if (gang.getContributionTime() > 0) {
					gang.setContributionTime(0);
					isGangChange = true;
				}

				// 成员当天捐献次数清0
				for (KGangMember mem : gang.getAllElementsCache().values()) {
					if (mem.notifyForDayChange() || isGangChange) {
						KSyncContributionChangeCountMsg.sendMsg(mem._roleId, 1);
					}
				}

				((KGangExtCASet) KGangModuleExtension.getGangExtCASet(gang.getId())).setContributionLastChangeTime(nowTime);
			} finally {
				gang.rwLock.unlock();
			}
		}
		
		// 军团战力榜每日奖励资金
		GangRank<GangRankElementPower> rank = KGangRankLogic.getRank(KGangRankTypeEnum.军团战力);
		for(KGangRankGoodReward data:KRankDataManager.mGangGoodRewardManager.getDataCache().values()){
			if(data.Funds<1){
				continue;
			}
			GangRankElementPower e = rank.getPublishData().getElementByRank(data.start);
			if(e==null){
				continue;
			}
			KGang gang = KGangModuleExtension.getGang(e.elementId);
			if(gang==null){
				continue;
			}
			
			gang.rwLock.lock();
			try {
				// 增加军团资金
				gang.changeResource(data.Funds);
			} finally {
				gang.rwLock.unlock();
				
				// 发邮件给军团长
				{
					KGangMember sir = gang.searchPosition(KGangPositionEnum.军团长);
					// 您好，您所统率军团XXX，排名为XXX，获得XXX军团资金奖励，已经发放。军团排名越高，所获取的军团资金奖励越高。
					if (sir != null) {
						String content = StringUtil.format(GangTips.军团x排名x获得x军团资金奖励, gang.getExtName(), data.start, data.Funds);
						KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(sir._roleId, GangTips.军团榜排名奖励邮件标题, content);
					}
				}
				
				// 更新全体成员：军团基础信息
				KSyncGangDataMsg.sendMsg(gang);
			}
		}

	}

	/**
	 * <pre>
	 * 军团小时触发任务
	 * 
	 * @author CamusHuang
	 * @creation 2014-4-14 上午11:56:28
	 * </pre>
	 */
	static void notifyForHourTask() {
		KGangLogic.autoMoveOverSire_outtime();
	}

	/**
	 * <pre>
	 * 玩家主动离团的后续操作
	 * 包含删角色、退团
	 * 不包含被开除
	 * 
	 * @param gang
	 * @param extCASet
	 * @param exitMem
	 * @author CamusHuang
	 * @creation 2014-4-14 下午4:06:18
	 * </pre>
	 */
	public static void actionAfterMemExit(KGang gang, KGangExtCASet extCASet, KGangMember exitMem) {
		// 军团频道；上浮提示；日志
		KGangLogic.addDialy(gang, extCASet, StringUtil.format(GangTips.x离开了军团, exitMem.getExtRoleName()), true, true, true);

		// 更新成员列表
		KSyncMemberListMsg.sendMsg(gang, null, Arrays.asList(exitMem._roleId));
		KSyncGangDataMsg.sendMsg(gang);

		// 通知角色刷新属性值
		notifyEffectAttrChange(exitMem._roleId, null);

		// 通知称号模块
		// KSupportFactory.getGameTitleSupport().notifyGangLeaveMember(gang.getId(),
		// roleId, false);
	}

	/**
	 * <pre>
	 * 军团--团长删角色时，自动禅让
	 * 
	 * @param gang
	 * @author CamusHuang
	 * @creation 2013-2-1 下午5:10:59
	 * </pre>
	 */
	private static void autoMoveOverSire_delete(KGang gang, KGangExtCASet extCASet, KGangMember sirMem) {

		//  团长删除角色系统自动将团长转让给副团长，无副团长时，则转让给最近3天内在线且贡献最高的玩家，并给与邮件提示：
		//  标题：军团管理
		//  内容：军团战XXX退出了军团，系统将军团职位自动转让给了XXX
		long minTime = System.currentTimeMillis() - KGangConfig.getInstance().AutoMoveOver_TargetTime;
		KGangMember mem = searchMemForAutoMoveOver(gang, minTime, sirMem._roleId);

		if (mem == null || mem.getType() == KGangPositionEnum.军团长.sign) {
			// 找不到合适的继任者
			return;
		}

		mem.setType(KGangPositionEnum.军团长.sign);

		// 军团频道；上浮提示；日志
		String tips = StringUtil.format(GangTips.军团长x退出了军团系统将其职位自动转让给了x, "", mem.getExtRoleName());
		KGangLogic.addDialy(gang, extCASet, tips, true, true, true);

		// 更新成员列表
		KSyncMemberListMsg.sendMsg(gang, Arrays.asList(mem), null);
		KSyncGangDataMsg.sendMsg(gang);

		// 群发邮件
		KGangMsgPackCenter.sendMailToAllMems(gang, GangTips.军团管理邮件标题, tips);

		FlowManager.logOther(gang.getId(), OtherFlowTypeEnum.删除角色转让团长, "军团名称:" + gang.getName() + ";新团长角色ID:" + mem._roleId);
	}

	/**
	 * <pre>
	 * 军团--团长长时间不上线时，自动禅让
	 * 由时效任务扫描并通知
	 * 周期可以每小时一次
	 * 
	 * @param gang
	 * @author CamusHuang
	 * @creation 2013-2-1 下午5:10:59
	 * </pre>
	 */
	private static void autoMoveOverSire_outtime() {

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

			KGangMember sirMem = gang.searchPosition(KGangPositionEnum.军团长);
			if (sirMem == null || sirMem.getLastLoginTime() < minTime_Out) {
				KGangMember mem = searchMemForAutoMoveOver(gang, minTime_Login, sirMem == null ? 0 : sirMem._roleId);
				if (mem == null || mem.getType() == KGangPositionEnum.军团长.sign) {
					// 找不到合适的继任者
					continue;
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
			}
		}
	}

	/**
	 * <pre>
	 * 团长删除角色系统自动将团长转让给副团长，无副团长时，则转让给最近3天内在线且贡献最高的玩家，并给与邮件提示：
	 * 
	 * @param gang
	 * @param minTime
	 * @return
	 * @author CamusHuang
	 * @creation 2014-8-19 下午5:53:45
	 * </pre>
	 */
	private static KGangMember searchMemForAutoMoveOver(KGang gang, long minTime, long excuteRoleId) {
		List<KGangMember> mems = new ArrayList<KGangMember>(gang.getAllElementsCache().values());

		KGangMember mem = null;
		for (KGangMember temp : mems) {
			if (temp._roleId == excuteRoleId) {
				continue;
			}
			if (temp.isSirs()) {
				// 找到团长或副团长
				return temp;
			}

			if (temp.getLastLoginTime() > minTime) {
				if (mem == null || temp.getTotalContribution() > mem.getTotalContribution()) {
					mem = temp;
				}
			}
		}
		return mem;
	}

	/**
	 * <pre>
	 * 军团--执行无条件解散军团
	 * 调用前已保证军团内没有有效成员，可以直接从DB删除
	 * 
	 * @param gang
	 * @author CamusHuang
	 * @creation 2013-2-1 下午5:10:59
	 * </pre>
	 */
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

	/**
	 * <pre>
	 * 军团--升级军团科技
	 * 
	 * @param role
	 * @param scitecId
	 * @return
	 * @author CamusHuang
	 * @creation 2013-1-30 下午6:24:41
	 * </pre>
	 */
	public static GangResult_UplvTech dealMsg_uplvGangTech(KRole role, int techId) {

		GangResult_UplvTech result = new GangResult_UplvTech();

		GangIntegrateData gangData = getGangAndSetByRoleId(role.getId());
		if (gangData == null) {
			result.tips = GangTips.对不起您不属于任何军团;
			return result;
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();

		GangTechTemplate techTemp = KGangDataManager.mGangTechDataManager.getData(techId);
		if (techTemp == null) {
			result.tips = GangTips.不存在此科技;
			return result;
		}
		result.tecType = techTemp.type;

		int nextLv = 0;

		gang.rwLock.lock();
		try {

			KGangMember sirmem = gang.getMember(role.getId());
			if (sirmem == null || !sirmem.isSirs()) {
				result.tips = GangTips.只有团长和副团长才有此权限;
				return result;
			}

			Map<Integer, Integer> techDataMap = set.getTechCache().getDataCache();
			int nowLv = techDataMap.containsKey(techId) ? techDataMap.get(techId) : 0;
			nextLv = nowLv + 1;
			TechLevelData nextLvData = techTemp.getLevelData(nextLv);
			if (nextLvData == null) {
				result.tips = StringUtil.format(GangTips.x科技等级已达极限, techTemp.getExtName());
				return result;
			}

			if (gang.getLevel() < nextLvData.needLv) {
				result.tips = StringUtil.format(GangTips.军团必须达到x级才能升级x科技, nextLvData.needLv, techTemp.getExtName());
				return result;
			}

			long resource = gang.getResource();
			if (resource < nextLvData.Legion_MoneyCount) {
				result.tips = StringUtil.format(GangTips.军团资金不足x, nextLvData.Legion_MoneyCount);
				return result;
			}

			// 扣资金
			gang.changeResource(-nextLvData.Legion_MoneyCount);
			// 升级
			techDataMap.put(techId, nextLv);
			set.getTechCache().notifyDB();

			result.isSucess = true;
			result.tips = StringUtil.format(GangTips.x科技成功提升到x级, techTemp.getExtName(), nextLv);
			result.gang = gang;
			result.extCASet = set;
			result.addDataUprisingTips(StringUtil.format(GangTips.军团资金减x, nextLvData.Legion_MoneyCount));
			return result;
		} finally {
			gang.rwLock.unlock();

			if (result.isSucess) {
				FlowManager.logOther(gang.getId(), OtherFlowTypeEnum.军团科技升级, "军团名称:" + gang.getName() + ";科技:" + techTemp.name + ";升级到:" + nextLv);
			}
		}
	}

	/**
	 * <pre>
	 * 通知在线军团成员刷新属性值
	 * 
	 * @param gang
	 * @author CamusHuang
	 * @creation 2014-4-11 下午12:12:05
	 * </pre>
	 */
	public static void notifyEffectAttrChange(KGang gang, KGangTecTypeEnum tecType) {
		// RoleModuleSupport support = KSupportFactory.getRoleModuleSupport();
		// for (long roleId : gang.getAllElementRoleIds()) {
		// support.notifyEffectAttrChange(roleId,
		// KGangAttributeProvider.getType());
		// }
		// 2014-10-13 添加
		if (tecType == null || tecType == KGangTecTypeEnum.随从合成经验加成) {
			for (long roleId : gang.getAllElementRoleIds()) {
				KSupportFactory.getPetModuleSupport().notifyPetComposeIncChange(roleId);
			}
		}
	}

	public static void notifyEffectAttrChange(long roleId, KGangTecTypeEnum tecType) {
		if (tecType == null || tecType == KGangTecTypeEnum.随从合成经验加成) {
			KSupportFactory.getPetModuleSupport().notifyPetComposeIncChange(roleId);
		}
	}

	public static CommonResult_Ext dealMsg_buyItem(KRole role, int gangGoodsId) {

		CommonResult_Ext result = new CommonResult_Ext();

		KGang gang = getGangByRoleId(role.getId());
		if (gang == null) {
			result.tips = GangTips.对不起您不属于任何军团;
			return result;
		}

		//
		GangGoodsData goods = null;
		gang.rwLock.lock();
		try {

			goods = KGangDataManager.mGangGoodsDataManager.getGoodsData(gangGoodsId);
			if (goods == null) {
				result.tips = ItemTips.物品不存在;
				return result;
			}

			if (gang.getLevel() < goods.LegionLv) {
				result.tips = StringUtil.format(GangTips.军团必须达到x级才能购买此商品, goods.LegionLv);
				return result;
			}

			KSupportFactory.getItemModuleSupport().lockItemSet(role.getId());
			try {

				List<ItemCountStruct> addItems = Arrays.asList(goods.item);
				if (!KSupportFactory.getItemModuleSupport().isCanAddItemsToBag(role.getId(), addItems)) {
					result.tips = ItemTips.背包容量不足;
					return result;
				}

				if (KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), goods.price, UsePointFunctionTypeEnum.购买军团道具, true) < 0) {
					result.isGoMoneyUI = true;
					result.goMoneyUIType = goods.price.currencyType;
					result.goMoneyUICount = goods.price.currencyCount-KSupportFactory.getCurrencySupport().getMoney(role.getId(), goods.price.currencyType);
					result.tips = StringUtil.format(ShopTips.x货币数量不足x, goods.price.currencyType.extName, goods.price.currencyCount);
					return result;
				}

				ItemResult_AddItem addResult = KSupportFactory.getItemModuleSupport().addItemToBag(role, goods.item, KShopTypeEnum.军团商店.name());
				if (!addResult.isSucess) {
					// 回滚货币
					KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), goods.price, PresentPointTypeEnum.回滚, true);
					//
					result.tips = addResult.tips;
					return result;
				}

				// 记录流水
				if (goods.price.currencyType == KCurrencyTypeEnum.DIAMOND) {
					long itemId = 0;
					if (addResult.newItemList != null && !addResult.newItemList.isEmpty()) {
						itemId = addResult.newItemList.get(0).getId();
					} else if (addResult.updateItemCountList != null && !addResult.updateItemCountList.isEmpty()) {
						itemId = addResult.updateItemCountList.get(0).getId();
					}
					FlowDataModuleFactory.getModule().recordBuyItemUsePoint(role, goods.item.itemCode, goods.item.getItemTemplate().name, itemId, (int) goods.item.itemCount,
							(int) goods.price.currencyCount, KShopTypeEnum.军团商店.sign);
				}

				result.isSucess = true;
				result.tips = ShopTips.购买成功;
				result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, goods.price.currencyType.extName, goods.price.currencyCount));
				return result;
			} finally {
				KSupportFactory.getItemModuleSupport().unlockItemSet(role.getId());
			}
		} finally {
			gang.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 加经验后，尝试对指定军团进行升级
	 * 
	 * @param gang
	 * @author CamusHuang
	 * @creation 2014-4-13 下午4:21:04
	 * </pre>
	 */
	public static boolean tryToUplvGang(KGang gang) {
		boolean isUp = false;
		gang.rwLock.lock();
		try {
			while (true) {
				int nowLv = gang.getLevel();
				int nowExp = gang.getExp();
				int nextLv = nowLv + 1;
				GangLevelData nextData = KGangDataManager.mGangLevelDataManager.getLevelData(nextLv);
				{
					if (nextData == null) {
						// 没有下一级
						// gang.setExp(0);
						return isUp;
					}
				}

				if (nowExp < nextData.exp) {
					return isUp;
				}
				// 升级
				gang.setLevel(nextLv);
				gang.setExp(nowExp - nextData.exp);
				isUp = true;
			}
		} finally {
			gang.rwLock.unlock();
			if (isUp) {
				FlowManager.logOther(gang.getId(), OtherFlowTypeEnum.军团升级, "军团名称:" + gang.getName() + ";升级到:" + gang.getLevel());
				
				// 通知报名榜
				KGangRankLogic.notifyGangLevelUp(gang, gang.getLevel(), gang.getExp());
			}
		}
	}

	/**
	 * <pre>
	 * 军团资源战报名结束后，对未入围的军团进行报名费返还
	 * 
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-27 下午7:30:18
	 * </pre>
	 */
	public static void backResourceForBidResWar(Logger reswarLogger, long gangId, int resource) {

		KGang gang = KGangModuleExtension.getGang(gangId);
		if (gang != null) {
			gang.changeResource(resource);
			//
			KGangMsgPackCenter.sendMsgToMemebers(KShowDialogMsg.createDataUprisingDialogMsg(StringUtil.format(GangTips.军团资金加x, resource)), gang);
			//
			reswarLogger.warn("【返还资金】,军团ID={},军团名称={},返还资金={}", gang.getId(), gang.getName(), resource);
		}
	}

	/**
	 * <pre>
	 * 	 	对应的玩家获得勋章奖励时，会弹出获得勋章奖励界面
	 * 	若对应玩家不在线时，则会在其上线后打开军团功能界面时再弹出：
	 * 
	 * @param gangId
	 * @param medal
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-21 下午12:18:15
	 * </pre>
	 */
	public static KGang sendGangWarFinalReward(long gangId, GangMedalData medal) {

		KGang gang = KSupportFactory.getGangSupport().getGang(gangId);
		if (gang == null) {
			return null;
		}

		List<Long> roleIds = new ArrayList<Long>();
		gang.rwLock.lock();
		try {
			// 设置勋章(含：是否已弹框)，下次军团战报名结束时取消勋章
			KGangMember sir = gang.searchPosition(KGangPositionEnum.军团长);
			KGangMember viceSir = gang.searchPosition(KGangPositionEnum.副团长);
			//
			if (sir != null) {
				sir.setMedal(medal.rank, false);
				roleIds.add(sir._roleId);
			}
			if (viceSir != null) {
				viceSir.setMedal(medal.rank, false);
				roleIds.add(viceSir._roleId);
			}
			//
		} finally {
			gang.rwLock.unlock();
		}

		for (Long roleId : roleIds) {
			KGWPushMsg.syncMedal(roleId, medal, false);
			// 刷新角色属性
			KGangAttributeProvider.notifyEffectAttrChange(roleId);
		}

		return gang;
	}

	public static void recordShowMedalDialog(KRole role) {
		KGang gang = getGangByRoleId(role.getId());
		if (gang == null) {
			return;
		}

		//
		gang.rwLock.lock();
		try {
			KGangMember mem = gang.getMember(role.getId());
			mem.setMedal(mem.getMedal(), true);
		} finally {
			gang.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 军团战报名结束时，清理所有勋章
	 * 	 	每次军团报名结束时，系统会自动回收上一届军团战所发放的军团勋章
	 * 	在勋章回收时，系统会发送文件给被回收的玩家:
	 * 	邮件标题：勋章回收通知
	 * 	邮件内容：新一届的军团争霸已经开始，您在上届军团战中所获得的【XX勋章】已被回收，请在本届军团战中再接再厉、再创辉煌！
	 * 	获得勋章的成员，进行职位禅让、解除职位，其勋章保留
	 * 	军团成员离开军团、军团解散时，系统回收拥有的勋章
	 * 
	 * @author CamusHuang
	 * @creation 2014-5-21 下午12:20:58
	 * </pre>
	 */
	public static void clearGangWarMedalForSignUpEnd() {
		GangEntireDataCacheAccesser cache = DataCacheAccesserFactory.getGangEntireDataCacheAccesser();
		List<Gang> copyGangList = cache.getAllGangsCopy();
		for (Gang temp : copyGangList) {
			KGang gang = (KGang) temp;
			clearGangWarMedalForSignUpEnd(gang);
		}
	}
	
	public static void clearGangWarMedalForSignUpEnd(KGang gang) {
		if (gang == null) {
			return;
		}
		RoleModuleSupport support = KSupportFactory.getRoleModuleSupport();
		for (Long roleId : gang.getAllElementRoleIds()) {
			KGangMember mem = gang.getMember(roleId);
			int medelRank = mem.getMedal();
			if (medelRank > 0) {
				mem.clearMedal();
				//
				KRole role = support.getRole(mem._roleId);
				if (role.isOnline()) {
					KGWPushMsg.syncMedal(mem._roleId, null, false);
				}
				// 刷新角色属性
				KGangAttributeProvider.notifyEffectAttrChange(mem._roleId);
				//
				GangMedalData medal = KGangWarDataManager.mGangMedalDataManager.getDataByRank(medelRank);
				KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(mem._roleId, GangTips.勋章回收邮件标题, StringUtil.format(GangTips.上届军团战中所获得的x勋章已被回收邮件内容, medal == null ? "" : medal.name));
				//
				KDialogService.sendUprisingDialog(mem._roleId, StringUtil.format(GangTips.勋章x回收通知, medal == null ? "" : medal.name));
			}
		}
	}

	/**
	 * <pre>
	 * 将所有军团的繁荣度清0
	 * 
	 * @author CamusHuang
	 * @creation 2014-5-18 下午12:24:33
	 * </pre>
	 */
	public static void clearAllGangFlourish() {
		GangEntireDataCacheAccesser cache = DataCacheAccesserFactory.getGangEntireDataCacheAccesser();
		List<Gang> copyGangList = cache.getAllGangsCopy();
		for (Gang temp : copyGangList) {
			KGang gang = (KGang) temp;
			gang.setFlourish(0);
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 模糊搜索军团CD锁
	 * 本模块中，对于模糊搜索军团，需要对每个角色进行CD时间控制
	 * 
	 * @author CamusHuang
	 * @creation 2013-3-27 下午12:25:51
	 * </pre>
	 */
	public static class GangSearchCDManager {
		// ////读写锁
		private final ReentrantLock lock = new ReentrantLock();
		/**
		 * <pre>
		 * KEY=角色ID
		 * VALUE=CD结束时间
		 * </pre>
		 */
		private final HashMap<Long, Long> cdLockMap = new HashMap<Long, Long>();

		public boolean addCD(long roleId, long cdEndTime) {
			lock.lock();
			try {
				Long oldCD = cdLockMap.get(roleId);
				if (oldCD == null) {
					cdLockMap.put(roleId, cdEndTime);
					return true;
				}

				long nowTime = System.currentTimeMillis();
				if (nowTime > oldCD) {
					cdLockMap.put(roleId, cdEndTime);
					return true;
				}
				return false;
			} finally {
				lock.unlock();
			}
		}

		public void removeCD(long roleId) {
			lock.lock();
			try {
				cdLockMap.remove(roleId);
			} finally {
				lock.unlock();
			}
		}

		public boolean isInCD(long roleId) {
			lock.lock();
			try {
				Long oldCD = cdLockMap.get(roleId);
				if (oldCD == null) {
					return false;
				}

				long nowTime = System.currentTimeMillis();
				if (nowTime > oldCD) {
					cdLockMap.remove(roleId);
					return false;
				}
				return true;
			} finally {
				lock.unlock();
			}
		}

		// void onTimeSingnal() {
		// lock.lock();
		// try {
		// long nowTime = System.currentTimeMillis();
		// for (Iterator<Entry<Long, Long>> it =
		// cdLockMap.entrySet().iterator(); it.hasNext();) {
		// Entry<Long, Long> entry = it.next();
		// if (nowTime > entry.getValue()) {
		// it.remove();
		// }
		// }
		// } finally {
		// lock.unlock();
		// }
		// }
	}

	int 移植分界线 = 0;

	// /**
	// * <pre>
	// * 新建军团对象
	// *
	// * @return
	// * @author camus
	// * @creation 2012-1-11 下午3:50:47
	// * </pre>
	// */
	// static KGang createKGangForDB(String gangName, long
	// createRoleId, List<KGangMember> memberList,
	// KDataObserver observer) {
	// KGang mKGang = new KGang(gangName, createRoleId,
	// memberList, observer);
	// // 返回
	// return mKGang;
	// }

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 军团全局映射数据管理
	 * 例如：角色ID-》军团ID映射
	 * 
	 * @author CamusHuang
	 * @creation 2013-2-1 上午11:19:24
	 * </pre>
	 */
	static class GangMappingDataManager {

		/**
		 * <pre>
		 * 角色锁
		 * <角色ID>
		 * 本模块中，对于需要保证多线程安全的角色，由于没有角色锁，故采取以下替代方式：
		 * 操作前将角色ID加入集合，若加入成功，则允许操作；若加入不成功，则提示繁忙
		 * 主要目的是提高性能
		 * </pre>
		 */
		private ConcurrentHashMap<Long, Long> roleIdLockMap = new ConcurrentHashMap<Long, Long>();

		/**
		 * <pre>
		 * 全部成员的角色ID 与 所属军团ID映射
		 * <角色ID, 所属军团ID>
		 * 主要目的是提高性能
		 * 
		 * CEND 军团--开服、军团所有成员增减时，注意同步本类数据--OK
		 * </pre>
		 */
		private ConcurrentHashMap<Long, Long> roleIdToGangId = new ConcurrentHashMap<Long, Long>();

		/**
		 * <pre>
		 * 军团--当开服、军团成员增加时，调用此方法
		 * 
		 * @param roleId
		 * @param gangId
		 * @author CamusHuang
		 * @creation 2013-2-2 上午10:51:31
		 * </pre>
		 */
		void putRoleIdToGangId(long roleId, long gangId) {
			roleIdToGangId.put(roleId, gangId);
		}

		/**
		 * <pre>
		 * 军团--当军团成员减少时，调用此方法
		 * 
		 * @param roleId
		 * @author CamusHuang
		 * @creation 2013-2-2 上午11:02:56
		 * </pre>
		 */
		void removeRoleIdToGangId(long roleId) {
			roleIdToGangId.remove(roleId);
		}

		/**
		 * <pre>
		 * 
		 * @param roleId
		 * @return 不存在则返回-1
		 * @author CamusHuang
		 * @creation 2013-2-1 上午11:22:44
		 * </pre>
		 */
		long getGangId(long roleId) {
			Long result = roleIdToGangId.get(roleId);
			if (result == null) {
				return -1;
			}
			return result;
		}

		boolean lockRoleId(long roleId) {
			return roleIdLockMap.putIfAbsent(roleId, roleId) == null;
		}

		void unlockRoleId(long roleId) {
			roleIdLockMap.remove(roleId);
		}
	}
}
