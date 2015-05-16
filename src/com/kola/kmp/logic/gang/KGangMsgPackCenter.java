package com.kola.kmp.logic.gang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.gang.Gang;
import com.kola.kgame.cache.gang.GangIntegrateData;
import com.kola.kgame.cache.mail.impl.KAMail;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.gang.KGangDataStruct.GangContributionData;
import com.kola.kmp.logic.gang.KGangDataStruct.GangGoodsData;
import com.kola.kmp.logic.gang.KGangDataStruct.GangLevelData;
import com.kola.kmp.logic.gang.KGangDataStruct.GangTechTemplate;
import com.kola.kmp.logic.gang.KGangDataStruct.GangTechTemplate.TechLevelData;
import com.kola.kmp.logic.gang.KGangExtCASet.ApplicationCache;
import com.kola.kmp.logic.gang.KGangExtCASet.GangDialyCache.GangDialy;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.mail.KMailConfig;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.rank.gang.GangRank;
import com.kola.kmp.logic.rank.gang.GangRankElementAbs;
import com.kola.kmp.logic.rank.gang.KGangRankLogic;
import com.kola.kmp.logic.rank.gang.KGangRankTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.ChatModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.tips.GangTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.KGangProtocol;

/**
 * <pre>
 * 由于在打包模块消息的过程中，涉及到访问及复杂的逻辑结构
 * 因此设立此类，用于打包消息
 * 
 * @author CamusHuang
 * @creation 2013-1-5 下午12:03:37
 * </pre>
 */
public class KGangMsgPackCenter {

	private static Logger _LOGGER = KGameLogger.getLogger(KGangMsgPackCenter.class);

	public static void packConstance(KGameMessage msg) {
		/**
		 * <pre>
		 * 角色登陆时，服务器主动推送的军团模块常量
		 * 
		 * byte 创建军团要支付的货币类型
		 * int 创建军团要支付的货币数量
		 * short 创建军团要求的角色等级
		 * </pre>
		 */
		msg.writeByte(KGangConfig.getInstance().PayForCreateGang.currencyType.sign);
		msg.writeInt((int) KGangConfig.getInstance().PayForCreateGang.currencyCount);
		msg.writeShort(KGangConfig.getInstance().CreateGangMinRoleLevel);
	}

	/**
	 * <pre>
	 * 参考{@link KGangProtocol#SM_GET_GANG_LIST_RESULT}
	 * 
	 * @param msg
	 * @param role
	 * @param numPerPage
	 * @param startPage
	 * @param pageNum
	 * @author CamusHuang
	 * @creation 2014-4-13 下午5:13:35
	 * </pre>
	 */
	public static void packGangList(KGameMessage msg, KRole role, int numPerPage, int startPage, int pageNum) {
		// 军团--客户端请求获取军团列表
		/**
		 * <pre>
		 * short 军团数量N
		 * for(0~N){
		 * 	参考{@link #ListFamilData}
		 * }
		 * </pre>
		 */
		// 要求本人申请过的排在军团列表前面
		Set<Long> frontGangs = KGangLogic.searchAppGangs(role.getId());
		long ownGangId = KGangLogic.getGangIdByRoleId(role.getId());// 当前所属的帮会
		frontGangs.add(ownGangId);
		//
		int startIndex = (startPage - 1) * numPerPage;// 起始位置
		int endIndex = startIndex + pageNum * numPerPage;// 结束位置（不包含）
		//
		ArrayList<GangRankElementAbs> list = new ArrayList<GangRankElementAbs>();
		{
			GangRank rank = KGangRankLogic.getRank(KGangRankTypeEnum.全部军团);
			List<GangRankElementAbs> tempList = new ArrayList<GangRankElementAbs>(rank.getPublishData().getUnmodifiableElementList());
			// 提取指定军团
			for (Iterator<GangRankElementAbs> it = tempList.iterator(); it.hasNext();) {
				GangRankElementAbs e = it.next();
				if (frontGangs.contains(e.elementId)) {
					it.remove();
					if (e.elementId == ownGangId) {
						list.add(0, e);// 所属军团放在最前面面
					} else {
						list.add(e);// 已申请的军团按顺序放入
					}
				}
			}
			// 放入剩余的军团
			list.addAll(tempList);
		}

		{
			int writeIndex = msg.writerIndex();
			msg.writeShort(0);
			int count = 0;
			if (startIndex < list.size()) {
				if (endIndex > list.size()) {
					endIndex = list.size();
				}

				for (int index = startIndex; index < endIndex; index++) {
					if (packGangDataForList(msg, role.getId(), list.get(index))) {
						count++;
					}
				}
			}
			msg.setShort(writeIndex, count);
		}
	}

	/**
	 * <pre>
	 * 参考{@link KGangProtocol#ListGangData}
	 * 
	 * @param msg
	 * @param gang
	 * @author CamusHuang
	 * @creation 2013-3-27 上午11:08:17
	 * </pre>
	 */
	private static boolean packGangDataForList(KGameMessage msg, long roleId, GangRankElementAbs element) {
		GangIntegrateData gangData = KGangModuleExtension.getGangAndSet(element.elementId);
		if (gangData == null) {
			return false;
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();

		packGangDataForList(msg, roleId, element.getRank(), gang, set);
		return true;
	}

	private static void packGangDataForList(KGameMessage msg, long roleId, int rank, KGang gang, KGangExtCASet set) {

		if (set == null) {
			set = KGangModuleExtension.getGangExtCASet(gang.getId());
		}

		/**
		 * <pre>
		 * 一个军团的列表数据消息结构
		 * int 排名
		 * long 军团ID
		 * String 军团名称 
		 * String 团长名称 
		 * short 等级
		 * String 人数（10/15）
		 * boolean 本人是否已申请过加入此军团且未被审批、或已属于此军团
		 * </pre>
		 */
		msg.writeInt(rank);
		msg.writeLong(gang.getId());
		msg.writeUtf8String(gang.getName());
		KGangMember sirmem = gang.searchPosition(KGangPositionEnum.军团长);
		msg.writeUtf8String(sirmem == null ? "" : sirmem.getRoleName());
		msg.writeShort(gang.getLevel());
		GangLevelData lvData = KGangDataManager.mGangLevelDataManager.getLevelData(gang.getLevel());
		msg.writeUtf8String(gang.memberSize() + "/" + lvData.maxuser);
		msg.writeBoolean(gang.getMember(roleId) != null || set.getAppCache().containApp(roleId));
	}

	public static void searchAndPackGangList(KGameMessage msg, KRole role, String key) {

		// * boolean 是否成功
		// * if(false){
		// * 失败原因
		// * } else {
		// * short 军团数量
		// * for(0~n){
		// * 参考{@link #ListGangData}
		// * }
		// * }
		// -------------
		//
		if (role == null) {
			msg.writeBoolean(false);
			msg.writeUtf8String(GlobalTips.服务器繁忙请稍候再试);
			return;
		}
		if (key == null || key.length() < 1) {
			msg.writeBoolean(false);
			msg.writeUtf8String(GangTips.请输入合法的字符);
			return;
		}

		long cdEndTime = System.currentTimeMillis() + KGangConfig.getInstance().GangSearchCD;
		if (!KGangLogic.mGangSearchCDManager.addCD(role.getId(), cdEndTime)) {
			msg.writeBoolean(false);
			msg.writeUtf8String(GlobalTips.您的操作太频繁了请歇一歇);
			return;
		}

		msg.writeBoolean(true);
		Set<Gang> gangs = KGangLogic.searchGangs(key);
		msg.writeShort(gangs.size());
		for (Gang gang : gangs) {
			int rank = KGangRankLogic.checkRank(KGangRankTypeEnum.全部军团, gang.getId());
			packGangDataForList(msg, role.getId(), rank, (KGang) gang, null);
		}

	}

	/**
	 * <pre>
	 * 参考 {@link KGangProtocol#OwnGangDetails}
	 * 
	 * 
	 * @param gang
	 * @author CamusHuang
	 * @creation 2013-1-26 下午3:25:10
	 * </pre>
	 */
	public static void packOwnGangData(KGameMessage msg, long roleId, KGang gang, KGangExtCASet set) {
		/**
		 * <pre>
		 * 角色所属军团的详细数据消息结构
		 * 
		 * long 军团ID
		 * String 军团名称
		 * String 染色的军团名称
		 * short 等级
		 * int 排名
		 * short 成员人数上限
		 * String 经验（10/15）
		 * long 军团资金
		 * String 军团公告
		 * 
		 * short 军团成员数量
		 * for(0~n){
		 * 	参考{@link #MemStruct}
		 * }
		 * short 军团科技数量
		 * for(0~n){
		 * 	参考{@link #TechStruct}
		 * }
		 * short 日志数量
		 * for(0~n){
		 * 	String dialy日志内容
		 *  String 日志产生日期
		 * }
		 * </pre>
		 */
		int rank = KGangRankLogic.checkRank(KGangRankTypeEnum.全部军团, gang.getId());
		gang.rwLock.lock();
		try {
			// 军团--打包所属军团的详细数据
			msg.writeLong(gang.getId());
			msg.writeUtf8String(gang.getName());
			KGangMember mem = gang.getMember(roleId);
			msg.writeUtf8String(StringUtil.format(GangTips.军团名x职位x, gang.getExtName(), mem == null ? UtilTool.getNotNullString(null) : mem.getPositionEnum().name));
			msg.writeShort(gang.getLevel());
			msg.writeInt(rank);
			GangLevelData lvData = KGangDataManager.mGangLevelDataManager.getLevelData(gang.getLevel());
			msg.writeShort(lvData.maxuser);
			GangLevelData nextLvData = KGangDataManager.mGangLevelDataManager.getLevelData(gang.getLevel() + 1);
			if (nextLvData == null) {
				msg.writeUtf8String("-/-");
			} else {
				msg.writeUtf8String(gang.getExp() + "/" + nextLvData.exp);
			}
			msg.writeLong(gang.getResource());
			msg.writeUtf8String(gang.getNotice());
			// 成员
			packMemberList(msg, gang.getAllElementsCache().values());
			// 科技
			packTechList(msg, set);
			// 日志列表
			packDialyList(msg, set);
		} finally {
			gang.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 参考 {@link KGangProtocol#SM_GANG_SYNC_GANGDATA}
	 * 
	 * @param msg
	 * @param gang
	 * @author CamusHuang
	 * @creation 2013-1-26 下午12:18:38
	 * </pre>
	 */
	public static void packGangUpdataData(KGameMessage msg, KGang gang) {
		/**
		 * <pre>
		 * 服务器推送最新的军团数据，消息体内容如下：
		 * 
		 * short 等级
		 * int 排名
		 * short 成员人数上限
		 * String 经验（10/15）
		 * long 军团资金
		 * </pre>
		 */
		int rank = KGangRankLogic.checkRank(KGangRankTypeEnum.全部军团, gang.getId());
		gang.rwLock.lock();
		try {
			// 军团--打包所属军团的详细数据
			msg.writeShort(gang.getLevel());
			msg.writeInt(rank);
			GangLevelData lvData = KGangDataManager.mGangLevelDataManager.getLevelData(gang.getLevel());
			msg.writeShort(lvData.maxuser);
			GangLevelData nextLvData = KGangDataManager.mGangLevelDataManager.getLevelData(gang.getLevel() + 1);
			if (nextLvData == null) {
				msg.writeUtf8String("-/-");
			} else {
				msg.writeUtf8String(gang.getExp() + "/" + nextLvData.exp);
			}
			msg.writeLong(gang.getResource());
		} finally {
			gang.rwLock.unlock();
		}
	}

	public static void packContributionDatas(long roleId, KGang gang, KGangMember nowMem, KGameMessage msg) {
		if (nowMem == null) {
			nowMem = gang.getMember(roleId);
		}
		GangLevelData lvData = KGangDataManager.mGangLevelDataManager.getLevelData(gang.getLevel());
		int releaseTime = lvData.donatetime - gang.getContributionTime();
		msg.writeShort(releaseTime < 1 ? 0 : releaseTime);
		//
		{
			Map<Integer, Map<Integer, GangContributionData>> dataMap = KGangDataManager.mGangContributionDataManager.getCache();
			/**
			 * <pre>
			 * short 军团可捐献次数
			 * byte 捐献类型数量N
			 * for(0~N){
			 * 	byte 捐献类型
			 *  byte 剩余次数(-1表示无限)
			 *  if(剩余次数!=0){
			 *  	int 捐献数量
			 *  	int 得到的贡献数量
			 *  }
			 * }
			 * </pre>
			 */
			msg.writeByte(dataMap.size());
			for (Entry<Integer, Map<Integer, GangContributionData>> entry : dataMap.entrySet()) {
				msg.writeByte(entry.getKey());
				//
				int nowMemTime = nowMem.getContributionTime(entry.getKey());
				int memReleaseTime = KGangDataManager.mGangContributionDataManager.getReleaseTime(entry.getKey(), nowMemTime);
				msg.writeByte(memReleaseTime);
				
				GangContributionData gcData = KGangDataManager.mGangContributionDataManager.getData(entry.getKey(), nowMemTime + 1);

				if (gcData != null) {
					msg.writeInt((int) gcData.price.currencyCount);
					msg.writeInt(gcData.GainContribution);
				}
			}
		}
	}

	/**
	 * <pre>
	 * 参考 {@link KGangProtocol#SM_GANG_GET_CONTRIBUTION_LIST_RESULT}
	 * 
	 * @param msg
	 * @param gang
	 * @author CamusHuang
	 * @creation 2013-1-26 下午2:26:56
	 * </pre>
	 */
	public static KGameMessage genContributionListMsg(int msgId, KRole role) {

		if (role == null) {
			return null;
		}

		GangIntegrateData gangData = KGangLogic.getGangAndSetByRoleId(role.getId());
		if (gangData == null) {
			return null;
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();

		gang.rwLock.lock();
		try {

			// 对比较：列表最后变化时间点以及玩家上次获取列表的时间点
			long lastChangeTime = set.getContributionLastChangeTime();
			KGangMember nowMem = gang.getMember(role.getId());
			long lastSynTime = nowMem.getLastSynContributionDataTime();
			if (lastSynTime > 0 && lastSynTime > lastChangeTime) {
				return null;
			}

			nowMem.setLastSynContributionDataTime(System.currentTimeMillis());
			//
			KGameMessage msg = KGame.newLogicMessage(msgId);

			packContributionDatas(role.getId(), gang, nowMem, msg);
			// 今日捐献表
			{
				int index = msg.writerIndex();
				msg.writeShort(0);
				int count = 0;
				for (KGangMember mem : gang.getAllElementsCache().values()) {
					if (mem.getTodayContribution() > 0) {
						msg.writeLong(mem._roleId);
						msg.writeLong(mem.getTodayContribution());
						count++;
					}
				}
				msg.setShort(index, count);
			}
			// 历史捐献表
			{
				int index = msg.writerIndex();
				msg.writeShort(0);
				int count = 0;
				for (KGangMember mem : gang.getAllElementsCache().values()) {
					if (mem.getTotalContribution() > 0) {
						msg.writeLong(mem._roleId);
						msg.writeLong(mem.getTotalContribution());
						count++;
					}
				}
				msg.setShort(index, count);
			}
			return msg;
		} finally {
			gang.rwLock.unlock();
		}

	}

	public static KGameMessage genAppListMsg(int msgId, KRole role) {
		if (role == null) {
			return null;
		}

		GangIntegrateData gangData = KGangLogic.getGangAndSetByRoleId(role.getId());
		if (gangData == null) {
			return null;
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();

		set.rwLock.lock();
		try {
			ApplicationCache appCache = set.getAppCache();

			// 未曾同步过或有更新-打包申请更新列表
			KGameMessage msg = KGame.newLogicMessage(msgId);
			packAppList(msg, appCache.getDataCache());
			return msg;
		} finally {
			set.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 
	 * @param msg
	 * @param gang
	 * @param appRoleIdList
	 * @param deleteAppRoldIds
	 * @author CamusHuang
	 * @creation 2013-1-26 上午11:21:18
	 * </pre>
	 */
	private static void packAppList(KGameMessage msg, Map<Long, Long> apps) {

		// System.err.println("apps="+(apps==null?0:apps.size()));
		// 军团--打包申请列表
		if (apps == null || apps.isEmpty()) {
			msg.writeShort(0);
			return;
		}

		int index = msg.writerIndex();
		msg.writeShort(0);
		int count = 0;
		for (Entry<Long, Long> entry : apps.entrySet()) {
			if (packApp(msg, entry.getKey(), entry.getValue())) {
				count++;
			}
		}
		msg.setShort(index, count);
	}

	/**
	 * <pre>
	 * 参考{@link KGangProtocol#AppStruct}
	 * 
	 * @param msg
	 * @param appRoleId
	 * @param appTime
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-11 下午8:38:09
	 * </pre>
	 */
	private static boolean packApp(KGameMessage msg, long appRoleId, long appTime) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(appRoleId);
		if (role == null) {
			// 如果要更新的APP对应的角色不存在，则从军团中删除
			return false;
		}
		/**
		 * <pre>
		 * 一个军团申请书的消息结构
		 * long 角色ID
		 * String 角色名称 
		 * short 等级
		 * String 职业
		 * int 战力
		 * long 申请时间
		 * boolean 是否在线（获取列表时的是否在线，不动态更新）
		 * </pre>
		 */
		//
		msg.writeLong(appRoleId);
		msg.writeUtf8String(role.getName());
		msg.writeShort(role.getLevel());
		msg.writeUtf8String(KJobTypeEnum.getJobName(role.getJob()));
		msg.writeInt(role.getBattlePower());
		msg.writeLong(appTime);
		msg.writeBoolean(role.isOnline());
		return true;
	}

	/**
	 * <pre>
	 * 参考{@link KGangProtocol#SM_GANG_SYNC_TECH}
	 * 
	 * @param msg
	 * @param gang
	 * @param scitecList
	 * @author CamusHuang
	 * @creation 2013-1-26 下午4:46:04
	 * </pre>
	 */
	public static void packTechForSyn(KGameMessage msg, KGangExtCASet set, int techId) {
		// 军团--打包科技更新列表
		GangTechTemplate temp = KGangDataManager.mGangTechDataManager.getData(techId);
		Integer lv = set.getTechCache().getDataCache().get(temp.ID);
		packTech(msg, temp, lv == null ? 0 : lv);
	}

	private static void packTechList(KGameMessage msg, KGangExtCASet set) {
		// 军团--打包科技更新列表
		Map<Integer, GangTechTemplate> techTempList = KGangDataManager.mGangTechDataManager.getCache();
		Map<Integer, Integer> techLvMap = set.getTechCache().getDataCache();
		msg.writeShort(techTempList.size());
		for (GangTechTemplate temp : techTempList.values()) {
			Integer lv = techLvMap.get(temp.ID);
			packTech(msg, temp, lv == null ? 0 : lv);
		}
	}

	/**
	 * <pre>
	 * 参考{@link KGangProtocol#TechStruct}
	 * 
	 * @param msg
	 * @param techTemp
	 * @param lv
	 * @author CamusHuang
	 * @creation 2013-1-28 上午11:08:39
	 * </pre>
	 */
	private static void packTech(KGameMessage msg, GangTechTemplate techTemp, int lv) {
		/**
		 * <pre>
		 * 一项军团科技的数据消息结构
		 * short 科技ID
		 * String 名称
		 * int icon
		 * int 等级
		 * String 描述
		 * boolean 是否存在下一等级
		 * if(true){
		 * 	int 要求的最小军团等级
		 * 	int 要求消耗的军团资金
		 *  String 下一级的描述
		 * }
		 * </pre>
		 */
		// 打包单个科技数据
		msg.writeShort(techTemp.ID);
		msg.writeUtf8String(techTemp.name);
		msg.writeInt(techTemp.icon);
		msg.writeInt(lv);
		TechLevelData data = techTemp.getLevelData(lv);
		msg.writeUtf8String(data.desc);

		TechLevelData nextLvData = techTemp.getLevelData(lv + 1);
		msg.writeBoolean(nextLvData != null);
		if (nextLvData != null) {
			msg.writeInt(nextLvData.needLv);
			msg.writeInt(nextLvData.Legion_MoneyCount);
			msg.writeUtf8String(nextLvData.desc);
		}
	}

	/**
	 * <pre>
	 * 参考{@link KGangProtocol#SM_GANG_SYNC_DIALY}
	 * 
	 * @param msg
	 * @param gang
	 * @param dialy
	 * @author CamusHuang
	 * @creation 2014-4-12 上午10:46:09
	 * </pre>
	 */
	public static void packDialySync(KGameMessage msg, KGang gang, GangDialy dialy) {
		msg.writeShort(1);
		msg.writeUtf8String(dialy.getContent());
		msg.writeUtf8String(dialy.getDateTime());
	}

	/**
	 * <pre>
	 * 参考 {@link KGangProtocol#SM_GANG_SYNC_DIALY}
	 * 
	 * @param msg
	 * @param gang
	 * @author CamusHuang
	 * @creation 2013-1-26 下午4:35:38
	 * </pre>
	 */
	private static void packDialyList(KGameMessage msg, KGangExtCASet set) {
		List<GangDialy> list = set.getDialyCache().getDataCache();
		msg.writeShort(list.size());
		for (GangDialy dialy : list) {
			msg.writeUtf8String(dialy.getContent());
			msg.writeUtf8String(dialy.getDateTime());
		}
	}

	public static int sendMsgToMemebers(KGameMessage msg, KGang gang, long exceptRoleId) {
		int count = 0;
		// 将消息克隆发送给所有成员
		KGameMessage dupMsg = msg.duplicate();
		RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
		for (long roleId : gang.getAllElementRoleIds()) {
			if (roleId == exceptRoleId) {
				continue;
			}
			if (roleSupport.sendMsg(roleId, dupMsg)) {
				dupMsg = msg.duplicate();
				count++;
			}
		}
		return count;
	}

	public static int sendMsgToMemebers(KGameMessage msg, long gangId) {
		
		KGang gang = KGangModuleExtension.getGang(gangId);
		if(gang==null){
			return 0;
		}
		
		return sendChatMsgToMemebers(msg, gang);
	}
	
	public static int sendMsgToMemebers(KGameMessage msg, KGang gang) {
		int count = 0;
		// 将消息克隆发送给所有成员
		KGameMessage dupMsg = msg.duplicate();
		RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
		for (long roleId : gang.getAllElementRoleIds()) {
			if (roleSupport.sendMsg(roleId, dupMsg)) {
				dupMsg = msg.duplicate();
				count++;
			}
		}
		return count;
	}

	/**
	 * <pre>
	 * 聊天消息专用，需要判断角色是否屏蔽军团频道
	 * 
	 * @param msg
	 * @param gang
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-15 上午10:07:10
	 * </pre>
	 */
	public static int sendChatMsgToMemebers(KGameMessage msg, KGang gang) {
		int count = 0;
		// 将消息克隆发送给所有成员
		KGameMessage dupMsg = msg.duplicate();
		KRole role = null;
		RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
		for (long roleId : gang.getAllElementRoleIds()) {
			role = roleSupport.getRole(roleId);
			if (role == null) {
				continue;
			}
			if (!role.isOnline()) {
				continue;
			}

			// if (role.getRoleGameSettingData().isBlockFamilyChat()) {
			// continue;
			// }

			if (role.sendMsg(dupMsg)) {
				dupMsg = msg.duplicate();
				count++;
			}
		}
		return count;
	}

	/**
	 * <pre>
	 * 发送私聊给军团不在线的成员
	 * 
	 * @author CamusHuang
	 * @creation 2013-9-26 上午11:08:08
	 * </pre>
	 */
	public static void sendPrivateChatToNotOnlineRoles(KGang gang, String content) {
		if (gang == null) {
			return;
		}
		ChatModuleSupport chatSupport = KSupportFactory.getChatSupport();
		KRole role;
		for (Long roleId : gang.getAllElementRoleIds()) {
			role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role != null && !role.isOnline()) {
				chatSupport.sendChatToRole(content, roleId);
			}
		}
	}

	public static void sendMsgToMembers(KGameMessage msg, Set<Long> gangIds) {
		if (gangIds == null || gangIds.isEmpty()) {
			return;
		}
		KGang gang;
		for (Long gangId : gangIds) {
			gang = KSupportFactory.getGangSupport().getGang(gangId);
			if (gang != null) {
				sendMsgToMemebers(msg, gang);
			}
		}
	}

	public static int sendMsgToSirs(KGameMessage msg, KGang gang) {
		int count = 0;
		// 军团--将消息克隆发送给所有副团长和军团长
		RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
		gang.rwLock.lock();
		try {
			KGameMessage dupMsg = msg.duplicate();
			for (KGangMember mem : gang.getAllElementsCache().values()) {
				if (mem.isSirs()) {
					if (roleSupport.sendMsg(mem._roleId, dupMsg)) {
						dupMsg = msg.duplicate();
						count++;
					}
				}
			}
		} finally {
			gang.rwLock.unlock();
		}
		return count;
	}

	/**
	 * <pre>
	 * 发送给非普通成员
	 * 
	 * @param msg
	 * @param gang
	 * @return
	 * @author CamusHuang
	 * @creation 2014-7-25 下午3:33:33
	 * </pre>
	 */
	public static int sendMsgToNotCommonMems(KGameMessage msg, KGang gang) {
		int count = 0;
		// 军团--将消息克隆发送给非普通成员
		KGameMessage dupMsg = msg.duplicate();
		RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
		gang.rwLock.lock();
		try {
			for (KGangMember mem : gang.getAllElementsCache().values()) {
				if (mem.getPositionEnum() == KGangPositionEnum.成员) {
					continue;
				}

				if (roleSupport.sendMsg(mem._roleId, dupMsg)) {
					dupMsg = msg.duplicate();
					count++;
				}
			}
		} finally {
			gang.rwLock.unlock();
		}
		return count;
	}

	/**
	 * <pre>
	 * 发送邮件给全体成员
	 * 
	 * @param title
	 * @param content
	 * @author CamusHuang
	 * @creation 2013-8-23 下午7:16:21
	 * </pre>
	 */
	static void sendMailToAllMems(KGang gang, String title, String content) {
		KSupportFactory.getMailModuleSupport().sendGroupSimpleMail(KMailConfig.SYS_MAIL_SENDER_ID, KMailConfig.SYS_MAIL_SENDER_NAME, gang.getAllElementRoleIds(), KAMail.TYPE_SYSTEM, title, content);
	}

	/**
	 * <pre>
	 * 发送邮件给团长和副团长
	 * 
	 * @param title
	 * @param content
	 * @author CamusHuang
	 * @creation 2013-8-23 下午7:16:10
	 * </pre>
	 */
	public static void sendMailToSirs(KGang gang, String title, String content) {
		gang.rwLock.lock();
		try {
			for (KGangMember mem : gang.getAllElementsCache().values()) {
				if (mem.isSirs()) {
					KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(mem._roleId, title, content);
				}
			}
		} finally {
			gang.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 参考{@link KGangProtocol#SM_GANG_SYNC_MEMBER_LIST}
	 * 
	 * @param msg
	 * @param gang
	 * @param memberList
	 * @param deleteMemberRoleIds
	 * @author CamusHuang
	 * @creation 2013-1-28 上午11:09:50
	 * </pre>
	 */
	public static void packMemberSyncList(KGameMessage msg, Collection<KGangMember> updateMembers, List<Long> deleteMemberRoleIds) {
		// 军团--打包成员更新列表
		packMemberList(msg, updateMembers);
		//
		if (deleteMemberRoleIds == null || deleteMemberRoleIds.isEmpty()) {
			msg.writeShort(0);
			return;
		}

		int index = msg.writerIndex();
		msg.writeShort(0);
		int count = 0;
		for (Long roleId : deleteMemberRoleIds) {
			msg.writeLong(roleId);
			count++;
		}
		msg.setShort(index, count);
	}

	/**
	 * <pre>
	 * 参考{@link KGangProtocol#SM_GANG_SYNC_MEMBER_LIST}
	 * 
	 * @param msg
	 * @param gang
	 * @param memberList
	 * @author CamusHuang
	 * @creation 2013-1-28 上午11:09:50
	 * </pre>
	 */
	private static void packMemberList(KGameMessage msg, Collection<KGangMember> updateMembers) {
		// 军团--打包成员列表
		if (updateMembers == null || updateMembers.isEmpty()) {
			msg.writeShort(0);
			return;
		}
		RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
		int index = msg.writerIndex();
		msg.writeShort(0);
		int count = 0;
		for (KGangMember mem : updateMembers) {
			KRole role = roleSupport.getRole(mem._roleId);
			if (role == null) {
				continue;
			}
			packMember(msg, role, mem);
			count++;
		}
		msg.setShort(index, count);
	}

	/**
	 * <pre>
	 * 参考{@link KGangProtocol#MemStruct}
	 * 
	 * @param msg
	 * @param role
	 * @param member
	 * @author CamusHuang
	 * @creation 2013-1-28 上午11:09:20
	 * </pre>
	 */
	private static void packMember(KGameMessage msg, KRole role, KGangMember member) {
		/**
		 * <pre>
		 * 一个军团成员的消息结构
		 * long 角色ID
		 * String 角色名称
		 * byte 军团职位：军团长(1, 1), 副团长(2, 1), 监察长(3, 4), 成员(4, 300);
		 * short 等级
		 * int 竞技场排名
		 * long 历史贡献值
		 * boolean 是否在线
		 * String 在线状态
		 * byte 职业
		 * long 离线时长
		 * int 战力
		 * </pre>
		 */
		// 军团--打包成员的消息
		msg.writeLong(role.getId());
		msg.writeUtf8String(role.getName());
		msg.writeByte(member.getType());
		msg.writeShort(role.getLevel());
		msg.writeInt(KSupportFactory.getCompetitionModuleSupport().getCurrentRankOfRole(role.getId()));
		msg.writeLong(member.getTotalContribution());
		boolean isOnline = role.isOnline();
		msg.writeBoolean(isOnline);
		
		long lastLeaveGameTime = role.getLastLeaveGameTime();
		if (lastLeaveGameTime == 0) {
			lastLeaveGameTime = role.getLastJoinGameTime();
		}
		if (isOnline) {
			msg.writeUtf8String(GlobalTips.在线);
		} else {
			msg.writeUtf8String(UtilTool.genLeaveTimeString(lastLeaveGameTime));
		}
		msg.writeByte(role.getJob());
		if (isOnline) {
			msg.writeLong(0);
		} else {
			msg.writeLong(System.currentTimeMillis() - lastLeaveGameTime);
		}
		msg.writeInt(role.getBattlePower());
	}

	/**
	 * <pre>
	 * {@link KGangProtocol#SM_GANG_GET_GOODS_LIST_RESULT}
	 * 
	 * @param backmsg
	 * @param role
	 * @author CamusHuang
	 * @creation 2014-4-12 下午3:40:51
	 * </pre>
	 */
	public static void packGoodsList(KGameMessage msg) {
		Map<Integer, GangGoodsData> datas = KGangDataManager.mGangGoodsDataManager.getCache();
		/**
		 * <pre>
		 * 服务器返回军团商品列表，消息体内容如下：
		 * 
		 * short 商品数量
		 * for(0~n){
		 * 	int 商品ID
		 *  short 要求最小军团等级
		 *  参考{@link com.kola.kmp.protocol.item.KItemProtocol#MSG_STRUCT_ITEM_DETAILS}
		 * }
		 * </pre>
		 */
		msg.writeShort(datas.size());
		for (GangGoodsData data : datas.values()) {
			msg.writeInt(data.ID);
			msg.writeShort(data.LegionLv);
			KItemMsgPackCenter.packItem(msg, data.item.getItemTemplate(), data.item.itemCount, data.price);
		}
	}

	/**
	 * <pre>
	 * 排名，ID，名称，军团长，等级，经验，人数，资金
	 * 
	 * @param infos
	 * @param countPerPage
	 * @param pageNum
	 * @author CamusHuang
	 * @creation 2013-7-24 下午6:04:39
	 * </pre>
	 */
	public static void packGangListForGM(List<String> infos, short numPerPage, short startPage) {
		StringBuffer sbf = new StringBuffer();
		sbf.append("排名").append('\t').append("ID").append('\t').append("名称").append('\t').append("军团长角色ID").append('\t').append("军团长角色名").append('\t').append("等级").append('\t').append("人数")
				.append('\t').append("资金").append('\t').append("繁荣度");
		infos.add(sbf.toString());

		//
		int startIndex = (startPage - 1) * numPerPage;// 起始位置
		int endIndex = startIndex + 1 * numPerPage;// 结束位置（不包含）
		//
		GangRank rank = KGangRankLogic.getRank(KGangRankTypeEnum.全部军团);
		List<GangRankElementAbs> list = new ArrayList<GangRankElementAbs>(rank.getPublishData().getUnmodifiableElementList());
		{
			if (startIndex < list.size()) {
				if (endIndex > list.size()) {
					endIndex = list.size();
				}

				for (int index = startIndex; index < endIndex; index++) {
					GangRankElementAbs e = list.get(index);
					KGang gang = KGangModuleExtension.getGang(e.elementId);
					if (gang != null) {
						infos.add(gangToString(gang, e.getRank()).toString());
					}
				}
			}
		}
	}

	/**
	 * <pre>
	 * 科技名称，科技ID
	 * 
	 * @param infos
	 * @param gangId
	 * @author CamusHuang
	 * @creation 2013-7-24 下午6:24:39
	 * </pre>
	 */
	public static void packGangSecAndTecForGM(List<String> infos, long gangId) {

		StringBuffer sbf = new StringBuffer();
		sbf.append("科技名称").append('\t').append("等级").append('\t').append("描述").append("").append('\t');
		infos.add(sbf.toString());

		GangIntegrateData gangData = KGangModuleExtension.getGangAndSet(gangId);
		if (gangData == null) {
			infos.add("找不到数据=" + gangId);
			return;
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();

		Map<Integer, Integer> techDatas = set.getTechCache().getDataCache();
		for (GangTechTemplate tech : KGangDataManager.mGangTechDataManager.getCache().values()) {
			int lv = 0;
			Integer LV = techDatas.get(tech.ID);
			if (LV != null) {
				lv = LV;
			}

			sbf = new StringBuffer();
			sbf.append(tech.name).append('\t').append(lv).append('\t').append(tech.getLevelData(lv).desc).append('\t');
			infos.add(sbf.toString());
		}

		infos.add("");

		sbf = new StringBuffer();
		sbf.append("【军团公告】").append('\t').append(gang.getNotice());
		infos.add(sbf.toString());
	}

	private static StringBuffer gangToString(KGang gang, int rank) {
		StringBuffer sbf = new StringBuffer();
		sbf.append(rank).append('\t').append(gang.getId()).append('\t').append(gang.getName()).append('\t');
		KGangMember mem = gang.searchPosition(KGangPositionEnum.军团长);
		sbf.append(mem == null ? "-" : mem._roleId).append('\t');
		sbf.append(mem == null ? "-" : mem.getRoleName()).append('\t');
		GangLevelData lvData = KGangDataManager.mGangLevelDataManager.getLevelData(gang.getLevel());
		sbf.append(gang.getLevel()).append('\t').append(gang.memberSize() + "/" + lvData.maxuser).append('\t').append(gang.getResource()).append('\t').append(gang.getFlourish());
		return sbf;
	}

	public static void packGangMemListForGM(List<String> infos, long gangId) {
		StringBuffer sbf = new StringBuffer();
		sbf.append("roleId").append('\t').append("角色").append('\t').append("职业").append('\t').append("等级").append('\t').append("战力").append('\t').append("职位").append('\t').append("今天贡献").append('\t')
				.append("历史贡献").append('\t').append("入会时间");
		infos.add(sbf.toString());

		KGang gang = KGangModuleExtension.getGang(gangId);
		if (gang == null) {
			infos.add("找不到数据=" + gangId);
		}

		//
		List<Long> memberRoleIdList = gang.getAllElementRoleIds();
		if (memberRoleIdList == null || memberRoleIdList.isEmpty()) {
			return;
		}

		for (long memId : memberRoleIdList) {
			KGangMember member = gang.getMember(memId);
			if (member == null) {
				continue;
			}
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(memId);
			if (role == null) {
				continue;
			}

			sbf = new StringBuffer();
			sbf.append(role.getId()).append('\t').append(role.getName()).append('\t').append(KJobTypeEnum.getJobName(role.getJob())).append('\t').append(role.getLevel()).append('\t')
					.append(role.getBattlePower());

			sbf.append('\t').append(member.getPositionEnum().name);
			sbf.append('\t').append(member.getTodayContribution());
			sbf.append('\t').append(member.getTotalContribution());
			sbf.append('\t').append(UtilTool.DATE_FORMAT.format(new Date(member._createTime)));

			infos.add(sbf.toString());
		}
	}
}
