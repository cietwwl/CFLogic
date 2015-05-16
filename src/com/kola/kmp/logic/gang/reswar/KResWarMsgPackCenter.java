package com.kola.kmp.logic.gang.reswar;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.timer.Timer;

import com.koala.game.KGameMessage;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.gang.KGangMsgPackCenter;
import com.kola.kmp.logic.gang.KGangRoleExtCACreator;
import com.kola.kmp.logic.gang.KRoleGangData;
import com.kola.kmp.logic.gang.reswar.KResWarDataManager.KCityTempManager.CityTemplate;
import com.kola.kmp.logic.gang.reswar.KResWarDataManager.KResPointTempManager.ResPointTemplate;
import com.kola.kmp.logic.gang.reswar.ResWarCity.CityWarData;
import com.kola.kmp.logic.gang.reswar.ResWarCity.CityWarData.GangData;
import com.kola.kmp.logic.gang.reswar.ResWarCity.ResPoint;
import com.kola.kmp.logic.gang.reswar.ResWarCityBidRank.BPElement;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.shop.KShopConfig;
import com.kola.kmp.logic.support.ChatModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GangResWarTips;

/**
 * <pre>
 * 由于在打包模块消息的过程中，涉及到访问及复杂的逻辑结构
 * 因此设立此类，用于打包消息
 * 
 * @author CamusHuang
 * @creation 2013-3-22 下午4:08:56
 * </pre>
 */
public class KResWarMsgPackCenter {
	/**
	 * <pre>
	 * 通知指定城市内的玩家
	 * 
	 * @param msg
	 * @param city
	 * @author CamusHuang
	 * @creation 2014-5-15 上午11:30:09
	 * </pre>
	 */
	public static void sendMsgToRoleInCity(KGameMessage msg, ResWarCity city) {
		Set<Long> set = new HashSet<Long>();
		city.getRoleIdsInWar(set);
		sendMsgToRoleIds(msg, set);
	}

	/**
	 * <pre>
	 * 通知所有城市内的玩家
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-28 下午9:26:49
	 * </pre>
	 */
	static void sendMsgToAllRoleInCitys(KGameMessage msg) {
		Set<Long> set = ResWarDataCenter.getRoleIdsInAllCityWars();
		sendMsgToRoleIds(msg, set);
	}

	private static void sendMsgToRoleIds(KGameMessage msg, Collection<Long> roleIds) {
		KGameMessage copy = msg.duplicate();
		for (Long roleId : roleIds) {
			if (KSupportFactory.getRoleModuleSupport().sendMsg(roleId, copy)) {
				copy = msg.duplicate();
			}
		}
	}

	public static void packTempDatas(KGameMessage msg) {
		/**
		 * <pre>
		 * 服务器返回结果，消息体内容如下：
		 * 
		 * byte 城市数量n
		 * for(0~n){
		 * 	参考{@link #CITY_TEMPDATA}
		 * }
		 * byte 资源点数量n
		 * for(0~n){
		 * 	参考{@link #RESPOINT_TEMPDATA}
		 * }
		 * </pre>
		 */
		{
			Map<Integer, CityTemplate> map = KResWarDataManager.mCityTempManager.getDataCache();
			msg.writeByte(map.size());
			for (CityTemplate temp : map.values()) {
				msg.writeByte(temp.ID);
				msg.writeUtf8String(temp.cityname);
				msg.writeByte(temp.citylv);
				msg.writeInt(temp.icon);
			}
		}
		//
		{
			Map<Integer, ResPointTemplate> map = KResWarDataManager.mResPointTempManager.getDataCache();
			msg.writeByte(map.size());
			for (ResPointTemplate temp : map.values()) {
				msg.writeByte(temp.ID);
				msg.writeUtf8String(temp.name);
				msg.writeInt(temp.icon);
				msg.writeUtf8String(temp.desc);
			}
		}
	}

	public static void packCityListStatus(KGameMessage msg, KRole role) {
		/**
		 * <pre>
		 * 服务器返回结果，消息体内容如下：
		 * 玩家点击城市图标时，若非【开战中】（竞价中、准备中、战后）则为查看城市信息，请使用（{@link #CM_GANGRW_GET_CITYDIALOG_DATA}）
		 * 玩家点击城市图标时，若【开战中】则为进入城市内部，请使用{@link #CM_GANGRW_JOIN_CITY}
		 * 
		 * boolean 是否【开战中】------用于在玩家点击城市图标时进行判断
		 * int 距离开战倒计时（秒）------如果>0，则显示倒计时
		 * byte 城市数量n
		 * for(0~n){
		 * 	byte 城市ID
		 * 	boolean 是否已占领
		 * 	if(true){
		 * 		long 占领军团ID----据此判断是否显示【我方军团占领】
		 * 		String 占领军团名称
		 * 		boolean 是否显示【可征收标记】---非开战状态，是占领方军团成员，且当天未征收
		 * 	}
		 * 
		 * 	boolean 是否有参战军团A的数据
		 * 	if(true){
		 * 		long A军团ID
		 * 		String A军团名称
		 * 	}
		 * 	boolean 是否有参战军团B的数据
		 * 	if(true){
		 * 		long B军团ID
		 * 		String B军团名称
		 * 	}
		 * }
		 * </pre>
		 */

		KRoleGangData roleData = KGangRoleExtCACreator.getData(role.getId(), true);
		long gangId = KSupportFactory.getGangSupport().getGangIdByRoleId(role.getId());

		ResWarDataCenter.rwLock.readLock().lock();
		try {
			msg.writeBoolean(ResWarStatusManager.getNowStatus() == ResWarStatusEnum.WAR_START);
			if (ResWarStatusManager.getNowStatus() == ResWarStatusEnum.READY_START) {
				msg.writeInt((int) ((ResWarStatusManager.getWarTime().warStartTime - System.currentTimeMillis()) / Timer.ONE_SECOND));
			} else {
				msg.writeInt(0);
			}
			msg.writeByte(ResWarDataCenter.allCityMap.size());
			for (ResWarCity city : ResWarDataCenter.allCityMap.values()) {
				// 休息或报名期间，是占领方军团成员，且当天未征收
				boolean isCanLevy = false;
				if (ResWarStatusManager.getNowStatus() == ResWarStatusEnum.BID_START || ResWarStatusManager.getNowStatus() == ResWarStatusEnum.REST_START) {
					GangData gangData = city.getOccGangData();
					if (gangData != null && gangId == gangData.gangId && !roleData.isLevyedCity(city.id)) {
						isCanLevy = true;
					}
				}
				packCityListStatus(msg, city, isCanLevy);
			}
		} finally {
			ResWarDataCenter.rwLock.readLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param msg
	 * @param city
	 * @param isCanLevy 是否是占领方军团成员，且当天未征收
	 * @author CamusHuang
	 * @creation 2014-5-12 下午5:58:59
	 * </pre>
	 */
	private static void packCityListStatus(KGameMessage msg, ResWarCity city, boolean isCanLevy) {
		msg.writeByte(city.id);
		GangData occGang = city.getOccGangData();
		msg.writeBoolean(occGang != null);
		if (occGang != null) {
			msg.writeLong(occGang.gangId);
			msg.writeUtf8String(occGang.gangName);
			msg.writeBoolean(isCanLevy);// 是否是占领方军团成员，且当天未征收
		}
		CityWarData warData = city.getCityWarData();
		if (warData == null) {
			msg.writeBoolean(false);
			msg.writeBoolean(false);
		} else {
			msg.writeBoolean(warData.gangA != null);
			if (warData.gangA != null) {
				msg.writeLong(warData.gangA.gangId);
				msg.writeUtf8String(warData.gangA.gangName);
			}
			msg.writeBoolean(warData.gangB != null);
			if (warData.gangB != null) {
				msg.writeLong(warData.gangB.gangId);
				msg.writeUtf8String(warData.gangB.gangName);
			}
		}
	}

	public static void packCityDialogDatas(KGameMessage backMsg, KRole role, int cityId) {
		/**
		 * <pre>
		 * 服务器返回结果，消息体内容如下：
		 * 
		 * boolean 是否成功
		 * String 结果提示
		 * byte 城市ID
		 * if(成功){
		 * 	int 我的竞价
		 * 	int 追加或竞价规定输入的金额
		 * 	byte 竞价榜数量N
		 * 	for(0~N){
		 * 		byte 排名
		 * 		String 名称
		 * 		int 竞价
		 * 	}
		 * }
		 * </pre>
		 */
		ResWarCity warCity = ResWarDataCenter.allCityMap.get(cityId);
		if (warCity == null) {
			backMsg.writeBoolean(false);
			backMsg.writeUtf8String(GangResWarTips.不存在此城市);
			backMsg.writeByte(cityId);
			return;
		}

		long gangId = KSupportFactory.getGangSupport().getGangIdByRoleId(role.getId());

		ResWarDataCenter.rwLock.readLock().lock();
		try {

			if (ResWarStatusManager.getNowStatus() == ResWarStatusEnum.WAR_START) {
				backMsg.writeBoolean(false);
				backMsg.writeUtf8String(GangResWarTips.对战已开始请刷新后进入城市);
				backMsg.writeByte(cityId);
				return;
			}

			backMsg.writeBoolean(true);
			backMsg.writeUtf8String(UtilTool.getNotNullString(null));
			backMsg.writeByte(cityId);
			{
				BPElement element = warCity.bidRank.getElement(gangId);
				int price = element == null ? 0 : element.price;
				backMsg.writeInt(price);
				backMsg.writeInt(price < 1 ? KResWarConfig.FirstBidPrice : KResWarConfig.OtherBidPrice);
				{
					List<BPElement> list = warCity.bidRank.getCopyElementList();
					int toIndex = Math.min(list.size(), KResWarConfig.BID_RANK_SHOWCOUNT);
					list = list.subList(0, toIndex);
					
					backMsg.writeByte(list.size());
					for (BPElement e : list) {
						backMsg.writeByte(e.rank);
						backMsg.writeUtf8String(e.elementName);
						backMsg.writeInt(e.price);
					}
				}
			}
		} finally {
			ResWarDataCenter.rwLock.readLock().unlock();
		}
	}

	public static void packResPoints(KGameMessage backMsg, ResWarCity warCity) {
		ResWarDataCenter.rwLock.readLock().lock();
		try {
			Map<Integer, ResPoint> resPointMap = warCity.resPointManager;
			backMsg.writeByte(resPointMap.size());
			for (ResPoint point : resPointMap.values()) {
				packResPoint(backMsg, point);
			}
		} finally {
			ResWarDataCenter.rwLock.readLock().unlock();
		}
	}

	public static void packResPoint(KGameMessage backMsg, ResPoint point) {
		/**
		 * <pre>
		 * 一个资源点的数据
		 * 
		 * byte 资源点ID
		 * boolean 是否已占领
		 * if(true){
		 * 	long 占领军团ID--判断是否己方军团占领
		 * 	long 占领角色ID--判断是否我的领地--弹框显示【占领】或【放弃】
		 * 	String 占领角色名称
		 * 	short 占领角色等级
		 *  int 占领剩余时间（秒）
		 * 	boolean 是否开战中
		 * 	if(true){
		 * 		String 挑战角色名称
		 * 	}
		 * }
		 * </pre>
		 */
		ResPointTemplate temp = point.getTemplate();
		backMsg.writeByte(point.id);
		backMsg.writeBoolean(point.getOccGangId() > 0);
		if (point.getOccGangId() > 0) {
			backMsg.writeLong(point.getOccGangId());
			backMsg.writeLong(point.getOccRoleId());
			backMsg.writeUtf8String(point.getOccRoleName());
			backMsg.writeShort(point.getOccRoleLv());
			int releaseTime = Math.max((int) ((point.getOccEndTime() - System.currentTimeMillis()) / Timer.ONE_SECOND), 1);
			backMsg.writeInt(releaseTime);
			backMsg.writeBoolean(point.getPkGangId() > 0);
			if (point.getPkGangId() > 0) {
				backMsg.writeUtf8String(point.getPkRoleName());
			}
		}
	}

	public static void packCityWarInfo(KGameMessage msg, ResWarCity city) {
		/**
		 * <pre>
		 * 服务器主动推送城市内的对战数据（人数、积分）
		 * 
		 * byte 城市ID----判断当前城市是否一致，不同则忽略本消息
		 * int 距离对战结束倒计时（秒）
		 * boolean 是否有参战军团A的数据
		 * if(true){
		 * 	short 参战人数
		 * 	int 积分
		 * }
		 * boolean 是否有参战军团B的数据
		 * if(true){
		 * 	short 参战人数
		 * 	int 积分
		 * }
		 * </pre>
		 */
		msg.writeByte(city.id);
		msg.writeInt(ResWarStatusManager.getWarReleaseTime());

		CityWarData warData = city.getCityWarData();
		if (warData == null) {
			msg.writeBoolean(false);
			msg.writeBoolean(false);
			return;
		}

		{
			GangData gang = warData.gangA;
			msg.writeBoolean(gang != null);
			if (gang != null) {
				msg.writeShort(gang.getMemCountInCity());
				msg.writeInt(gang.getScore());
			}
		}
		{
			GangData gang = warData.gangB;
			msg.writeBoolean(gang != null);
			if (gang != null) {
				msg.writeShort(gang.getMemCountInCity());
				msg.writeInt(gang.getScore());
			}
		}
	}
}
