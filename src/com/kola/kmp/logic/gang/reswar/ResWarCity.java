package com.kola.kmp.logic.gang.reswar;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.timer.Timer;

import org.jdom.Element;

import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.gang.reswar.KResWarDataManager.KCityTempManager.CityTemplate;
import com.kola.kmp.logic.gang.reswar.KResWarDataManager.KResPointTempManager.ResPointTemplate;
import com.kola.kmp.logic.gang.reswar.ResWarCity.CityWarData.GangData;
import com.kola.kmp.logic.gang.reswar.ResWarCityBidRank.BPElement;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 城市数据
 * 
 * @author CamusHuang
 * @creation 2014-5-7 下午6:10:36
 * </pre>
 */
public class ResWarCity {
	public final int id;
	// ConcurrentHashMap<资源点ID, ResPoint>
	public final Map<Integer, ResPoint> resPointManager;
	// 竞价排行榜
	public final ResWarCityBidRank bidRank;
	//
	private GangData occGangData;// 占领此城市的军团ID，null表示未占领
	private CityWarData cityWarData;// null表示没有开战军团
	//
	private boolean isWarEnd;
	private GangData[] result;// null表示没有开战军团，[胜出军团、战败军团]

	ResWarCity(int id, ConcurrentHashMap<Integer, ResPoint> resPoints) {
		this.id = id;
		resPointManager = Collections.unmodifiableMap(resPoints);
		bidRank = new ResWarCityBidRank(id);
	}

	boolean isWarEnd() {
		return isWarEnd;
	}

	GangData[] getResult() {
		return result;
	}

	public CityTemplate getTemplate() {
		return KResWarDataManager.mCityTempManager.getData(id);
	}

	GangData getOccGangData() {
		return occGangData;
	}

	CityWarData getCityWarData() {
		return cityWarData;
	}

	void getRoleIdsInWar(Set<Long> set) {
		if (cityWarData == null) {
			return;
		}
		if (cityWarData.gangA != null) {
			set.addAll(cityWarData.gangA.getAllMemSetCache());
		}
		if (cityWarData.gangB != null) {
			set.addAll(cityWarData.gangB.getAllMemSetCache());
		}
	}

	/**
	 * <pre>
	 * 选出各城市对战军团
	 * 不入围军团退还竞价费用，清空竞价榜
	 * 
	 * @author CamusHuang
	 * @creation 2014-5-12 下午3:03:48
	 * </pre>
	 */
	void notifyForBidEnd() {

		// 竞价资金前2的军团可以入围军团资源战
		List<BPElement> elist = bidRank.getCopyElementList();
		if (elist.isEmpty()) {
			return;
		}

		{
			GangData gangA = null;
			GangData gangB = null;
			{
				BPElement e = elist.remove(0);
				gangA = new GangData(e.elementId, e.elementName, e.price);
			}
			if (!elist.isEmpty()) {
				BPElement e = elist.remove(0);
				gangB = new GangData(e.elementId, e.elementName, e.price);
			}

			cityWarData = new CityWarData(gangA, gangB);
		}

		ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战：城市{}：竞价成功", id);

		// 清空竞价，未上榜军团的竞价费用返还
		clearBidRank(true);

		// 入围通报
		{
			if (cityWarData != null) {
				if (cityWarData.gangA != null) {
					ResWarSystemBrocast.onBidEnd_JoinList(id, cityWarData.gangA.gangId);
				}
				if (cityWarData.gangB != null) {
					ResWarSystemBrocast.onBidEnd_JoinList(id, cityWarData.gangB.gangId);
				}
			}
		}
	}

	/**
	 * <pre>
	 * 出产积分
	 * 
	 * @author CamusHuang
	 * @creation 2014-5-13 下午5:18:57
	 * </pre>
	 */
	void notifyForProduceScore() {
		if (isWarEnd) {
			return;
		}
		if (cityWarData != null) {
			for (ResPoint point : resPointManager.values()) {
				if (point.occGangId > 0) {
					ResPointTemplate temp = point.getTemplate();
					if (cityWarData.gangA != null && cityWarData.gangA.gangId == point.occGangId) {
						cityWarData.gangA.score += temp.Integral;
					} else if (cityWarData.gangB != null && cityWarData.gangB.gangId == point.occGangId) {
						cityWarData.gangB.score += temp.Integral;
					}
				}
			}
		}
	}

	/**
	 * <pre>
	 * 定时扫描处理PVP玩家非战斗、离线等错误状态
	 * 处理占领超时的资源点
	 * 不干预PVP流程，目的在于释放资源点
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-15 上午9:34:15
	 * </pre>
	 */
	boolean notifyForWarErrorScan() {
		// 清理资源点
		boolean isChange = false;
		long nowTime = System.currentTimeMillis();
		for (ResPoint point : resPointManager.values()) {
			if (point.occEndTime > 0 && nowTime >= point.occEndTime) {
				// 通知原占领者
				ResPointBeSeizedManager.newEvent(point.occRoleId, id, point.id, null);
				// 占领超时
				point.clearOcc();
				isChange = true;
			}

			if (point.pkRoleId < 1) {
				// 非PK中
				continue;
			}

			KRole role = KSupportFactory.getRoleModuleSupport().getRole(point.pkRoleId);
			if (role == null || !role.isOnline() || !role.isFighting()) {
				// 角色已离线 或 非PK中
				point.clearPk();
				isChange = true;
				continue;
			}
		}
		return isChange;
	}

	/**
	 * <pre>
	 * 处理在线人数
	 * 处理PK信息
	 * 不干预PVP流程，目的在于释放资源点
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-15 上午9:34:15
	 * </pre>
	 */
	boolean[] notifyForRoleLeave(long roleId) {
		// 清理人数
		boolean isMemCountChange = false;
		if (cityWarData != null) {
			if (cityWarData.leave(roleId)) {
				isMemCountChange = true;
			}
		}
		// 清理资源点
		boolean isPointChange = false;
		for (ResPoint point : resPointManager.values()) {
			// 释放PK
			if (point.pkRoleId == roleId) {
				point.clearPk();
				isPointChange = true;
			}
		}
		return new boolean[] { isMemCountChange, isPointChange };
	}

	/**
	 * <pre>
	 * 处理在线人数
	 * 处理PK信息
	 * 不干预PVP流程，目的在于释放资源点
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-15 上午9:34:15
	 * </pre>
	 */
	boolean[] notifyForRoleDelete(long roleId) {
		// 清理人数
		boolean isMemCountChange = false;
		if (cityWarData != null) {
			if (cityWarData.leave(roleId)) {
				isMemCountChange = true;
			}
		}
		// 清理资源点
		boolean isPointChange = false;
		for (ResPoint point : resPointManager.values()) {
			// 释放占领
			if (point.occRoleId == roleId) {
				point.clearOcc();
				isPointChange = true;
			}

			// 释放PK
			if (point.pkRoleId == roleId) {
				point.clearPk();
				isPointChange = true;
			}
		}
		return new boolean[] { isMemCountChange, isPointChange };
	}

	/**
	 * <pre>
	 * 活动中，按照双方的积分值，积分值先达到上限的一方胜出
	 * 若积分同时达到上限，则按积分高的一方胜出
	 * 若积分相同，则按照竞价高的一方获得胜利
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-14 下午6:19:55
	 * </pre>
	 */
	void notifyJudgeInWar() {

		if (isWarEnd) {
			return;
		}

		clearData();

		if (cityWarData == null) {
			isWarEnd = true;
			return;
		}

		{
			// 某一方不存在的情况
			if (cityWarData.gangA == null || cityWarData.gangB == null) {
				if (cityWarData.gangA == null) {
					result = new GangData[] { cityWarData.gangB, null };
				} else if (cityWarData.gangB == null) {
					result = new GangData[] { cityWarData.gangA, null };
				}
				isWarEnd = true;
				if (result[0] != null) {
					occGangData = new GangData(result[0].gangId, result[0].gangName, result[0].bidPrice);
				}
				return;
			}

			// 双方均过线的情况
			if (cityWarData.gangA.score >= KResWarConfig.WinScoreValue && cityWarData.gangB.score >= KResWarConfig.WinScoreValue) {
				if (cityWarData.gangA.score >= cityWarData.gangB.score) {
					result = new GangData[] { cityWarData.gangA, cityWarData.gangB };
				} else if (cityWarData.gangA.score < cityWarData.gangB.score) {
					result = new GangData[] { cityWarData.gangB, cityWarData.gangA };
				}
				isWarEnd = true;
				if (result[0] != null) {
					occGangData = new GangData(result[0].gangId, result[0].gangName, result[0].bidPrice);
				}
				return;
			}

			// 单方过线的情况
			if (cityWarData.gangA.score >= KResWarConfig.WinScoreValue || cityWarData.gangB.score >= KResWarConfig.WinScoreValue) {
				if (cityWarData.gangA.score >= cityWarData.gangB.score) {
					result = new GangData[] { cityWarData.gangA, cityWarData.gangB };
				} else if (cityWarData.gangA.score < cityWarData.gangB.score) {
					result = new GangData[] { cityWarData.gangB, cityWarData.gangA };
				}
				isWarEnd = true;
				if (result[0] != null) {
					occGangData = new GangData(result[0].gangId, result[0].gangName, result[0].bidPrice);
				}
				return;
			}

			// 均未过线
			return;
		}
	}

	/**
	 * <pre>
	 * 活动时间结束时，按照双方的积分值，积分值高的一方胜出
	 * 若积分相同时，则按照竞价高的一方获得胜利
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-14 下午6:19:55
	 * </pre>
	 */
	void notifyJudgeForEnd() {

		if (isWarEnd) {
			return;
		}

		clearData();

		if (cityWarData == null) {
			isWarEnd = true;
			return;
		}

		{
			if (cityWarData.gangA != null && cityWarData.gangB == null) {
				result = new GangData[] { cityWarData.gangA, null };
			} else if (cityWarData.gangA == null && cityWarData.gangB != null) {
				result = new GangData[] { cityWarData.gangB, null };
			} else {
				if (cityWarData.gangA.score >= cityWarData.gangB.score) {
					result = new GangData[] { cityWarData.gangA, cityWarData.gangB };
				} else {
					result = new GangData[] { cityWarData.gangB, cityWarData.gangA };
				}
			}

			isWarEnd = true;
			if (result[0] != null) {
				occGangData = new GangData(result[0].gangId, result[0].gangName, result[0].bidPrice);
			}
		}
	}

	/**
	 * <pre>
	 * 清空竞价表
	 * 返还资金
	 * 
	 * @param isSuccess 是否如常进行
	 * @author CamusHuang
	 * @creation 2014-5-12 下午3:20:13
	 * </pre>
	 */
	void clearBidRank(boolean isSuccess) {
		// 竞价表清空前备份
		bidRank.save(false, true, true);
		ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战：城市{}： 清空竞价表", id);

		List<BPElement> list = bidRank.getCopyElementList();
		for (BPElement element : list) {
			// 返还军团资金
			KGangLogic.backResourceForBidResWar(ResWarDataCenter.RESWAR_LOGGER, element.elementId, element.price);
			// 系统通知：本届军团争夺失败，系统返还竞价资金XXX
			ResWarSystemBrocast.onBidEnd_OutList_BackBidResource(isSuccess, id, element.elementId, element.price);
		}
		// 清空排行榜
		bidRank.clear();
		// 保存到DB
		bidRank.save(true, false, false);
	}

	/**
	 * <pre>
	 * 清空所有数据
	 * 
	 * @author CamusHuang
	 * @creation 2014-5-14 下午9:09:58
	 * </pre>
	 */
	void clearData() {
		// 竞价表清空前备份
		bidRank.save(false, true, true);
		// 清空排行榜
		bidRank.clear();
		// 保存到DB
		bidRank.save(true, false, false);

		occGangData = null;
		for (ResPoint point : resPointManager.values()) {
			point.clearOcc();
			point.clearPk();
		}
	}

	/**
	 * <pre>
	 * 清空数据，清空竞价，竞价费用全返还
	 * 
	 * @author CamusHuang
	 * @creation 2014-5-14 下午9:11:37
	 * </pre>
	 */
	void clearDataForResetFail() {
		clearBidRank(false);

		occGangData = null;
		for (ResPoint point : resPointManager.values()) {
			point.clearOcc();
			point.clearPk();
		}
	}

	void paramsOccGang(Element elementC) {
		if (Boolean.getBoolean(elementC.getAttributeValue("状态"))) {
			occGangData = new GangData(Integer.parseInt(elementC.getAttributeValue("id")), elementC.getAttributeValue("name"), Integer.parseInt(elementC.getAttributeValue("price")));
		}
	}

	void paramsWarGangs(Element elementC) {
		if (Boolean.getBoolean(elementC.getAttributeValue("状态"))) {
			GangData gangA = null;
			GangData gangB = null;
			Element elementD = elementC.getChild("军团A");
			if (Boolean.getBoolean(elementD.getAttributeValue("状态"))) {
				gangA = new GangData(Integer.parseInt(elementD.getAttributeValue("id")), elementD.getAttributeValue("name"), Integer.parseInt(elementD.getAttributeValue("price")));
				gangA.params(elementD);

			}
			//
			elementD = elementC.getChild("军团B");
			if (Boolean.getBoolean(elementD.getAttributeValue("状态"))) {
				if (elementD != null) {
					gangB = new GangData(Integer.parseInt(elementD.getAttributeValue("id")), elementD.getAttributeValue("name"), Integer.parseInt(elementD.getAttributeValue("price")));
					gangB.params(elementD);
				}
			}
			cityWarData = new CityWarData(gangA, gangB);
		}
	}

	/**
	 * <pre>
	 * 开战的双方军团ID 
	 * 
	 * @author CamusHuang
	 * @creation 2014-5-7 下午6:02:32
	 * </pre>
	 */
	static class CityWarData {
		final GangData gangA;
		final GangData gangB;

		CityWarData(GangData gangA, GangData gangB) {
			this.gangA = gangA;
			this.gangB = gangB;
		}

		boolean isGangJoined(long gangId) {
			if (gangA != null && gangA.gangId == gangId) {
				return true;
			}
			if (gangB != null && gangB.gangId == gangId) {
				return true;
			}
			return false;
		}

		boolean isRoleJoined(long gangId, long roleId) {
			if (gangA != null && gangA.gangId == gangId) {
				return gangA.allMemSet.contains(roleId);
			}
			if (gangB != null && gangB.gangId == gangId) {
				return gangB.allMemSet.contains(roleId);
			}
			return false;
		}

		void join(long gangId, long roleId) {
			if (gangA != null && gangA.gangId == gangId) {
				gangA.allMemSet.add(roleId);
				return;
			}
			if (gangB != null && gangB.gangId == gangId) {
				gangB.allMemSet.add(roleId);
				return;
			}
		}

		boolean leave(long roleId) {
			if (gangA != null) {
				return gangA.allMemSet.remove(roleId);
			}
			if (gangB != null) {
				return gangB.allMemSet.remove(roleId);
			}
			return false;
		}

		static class GangData {
			final long gangId;
			final String gangName;
			final int bidPrice;
			// -------当场对战的数据
			private int score;// 积分
			// 所有参战的成员，key=角色ID
			private Set<Long> allMemSet = new HashSet<Long>();

			GangData(long gangId, String gangName, int bidPrice) {
				this.gangId = gangId;
				this.gangName = gangName;
				this.bidPrice = bidPrice;
			}

			int getMemCountInCity() {
				return allMemSet.size();
			}

			/**
			 * <pre>
			 * 
			 * @deprecated
			 * @return
			 * @author CamusHuang
			 * @creation 2014-5-14 下午7:40:43
			 * </pre>
			 */
			Set<Long> getAllMemSetCache() {
				return allMemSet;
			}

			int getScore() {
				return score;
			}

			void params(Element elementD) {
				score = Integer.parseInt(elementD.getAttributeValue("score"));
			}
		}
	}

	/**
	 * <pre>
	 * 资源点数据
	 * 
	 * @author CamusHuang
	 * @creation 2014-5-7 下午6:10:26
	 * </pre>
	 */
	public static class ResPoint {
		public final int id;
		//
		private long occGangId;// 占领此资源点的军团ID，0表示未占领
		private long occRoleId;// 占领此资源点的角色ID，0表示未占领
		private String occRoleName;// 占领角色名称
		private int occRoleLv;// 占领角色等级
		private long occEndTime;// 占领结束时间
		//
		private long pkGangId;// 争夺此资源点的军团ID，0表示无人争夺
		private long pkRoleId;// 争夺此资源点的角色ID，0表示无人争夺
		private String pkRoleName;// 争夺此资源点的角色名称

		ResPoint(int id) {
			this.id = id;
		}

		public ResPointTemplate getTemplate() {
			return KResWarDataManager.mResPointTempManager.getData(id);
		}

		void clearOcc() {
			occGangId = 0;
			occRoleId = 0;
			occRoleLv = 0;
			occRoleName = null;
			occEndTime = 0;
		}

		void setOcc(long gangId, KRole role) {
			ResPointTemplate temp = getTemplate();
			// 未有人占领->直接占领
			occGangId = gangId;
			occRoleId = role.getId();
			occRoleLv = role.getLevel();
			occRoleName = role.getName();
			occEndTime = (System.currentTimeMillis() + temp.OccupyTime * Timer.ONE_SECOND) / Timer.ONE_SECOND;
		}

		void setPK(long gangId, KRole role) {
			pkGangId = gangId;
			pkRoleId = role.getId();
			pkRoleName = role.getName();
		}

		void clearPk() {
			pkGangId = 0;
			pkRoleId = 0;
			pkRoleName = null;
		}

		long getOccGangId() {
			return occGangId;
		}

		long getOccRoleId() {
			return occRoleId;
		}

		String getOccRoleName() {
			return occRoleName;
		}

		int getOccRoleLv() {
			return occRoleLv;
		}

		long getOccEndTime() {
			return occEndTime;
		}

		long getPkGangId() {
			return pkGangId;
		}

		long getPkRoleId() {
			return pkRoleId;
		}

		String getPkRoleName() {
			return pkRoleName;
		}

		void params(Element elementC) {
			occGangId = Long.parseLong(elementC.getAttributeValue("occGangId"));
			occRoleId = Long.parseLong(elementC.getAttributeValue("occRoleId"));
			occRoleName = elementC.getAttributeValue("occRoleName");
			occRoleLv = Integer.parseInt(elementC.getAttributeValue("occRoleLv"));
			occEndTime = Long.parseLong(elementC.getAttributeValue("occEndTime"));
			pkGangId = Long.parseLong(elementC.getAttributeValue("pkGangId"));
			pkRoleId = Long.parseLong(elementC.getAttributeValue("pkRoleId"));
			pkRoleName = elementC.getAttributeValue("pkRoleName");
		}
	}

}
