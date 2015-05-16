package com.kola.kmp.logic.gang.reswar;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.gang.GangIntegrateData;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.competition.KCompetitionBattlefield;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangExtCASet;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.gang.KGangModuleExtension;
import com.kola.kmp.logic.gang.reswar.KResWarDataManager.KCityTempManager.CityTemplate;
import com.kola.kmp.logic.gang.reswar.KResWarDataManager.KGangLvRewardBaseDataManager.GangLvData;
import com.kola.kmp.logic.gang.reswar.KResWarDataManager.KResPointTempManager.ResPointTemplate;
import com.kola.kmp.logic.gang.reswar.KResWarDataManager.KRewardDataManager.RewardData;
import com.kola.kmp.logic.gang.reswar.KResWarDataManager.KRoleLvRewardBaseDataManager.RoleLvData;
import com.kola.kmp.logic.gang.reswar.ResWarCity.CityWarData;
import com.kola.kmp.logic.gang.reswar.ResWarCity.CityWarData.GangData;
import com.kola.kmp.logic.gang.reswar.ResWarCity.ResPoint;
import com.kola.kmp.logic.gang.reswar.ResWarCityBidRank.BPElement;
import com.kola.kmp.logic.gang.reswar.message.KGrwSynMsg;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KXmlWriter;
import com.kola.kmp.logic.util.tips.GangResWarTips;

/**
 * <pre>
 * 军团资源战数据管理
 * 管理本周军团资源战的各项数据
 * 
 * @author CamusHuang
 * @creation 2013-8-29 下午6:29:22
 * </pre>
 */
public class ResWarDataCenter {

	public static final Logger RESWAR_LOGGER = KGameLogger.getLogger("gangResWar");

	// 读写锁
	public static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

	private static final String saveDirPath = "./res/output/gangResWar/";
	private static final String saveFileName = "gangResWarData";
	private static final String saveFileNameSuffix = ".xml";

	// 用于PVP的战斗地图
	static final KCompetitionBattlefield PVPBattlefield = new KCompetitionBattlefield();

	// <城市ID, City>
	final static ConcurrentHashMap<Integer, ResWarCity> allCityMap = new ConcurrentHashMap<Integer, ResWarCity>();

	static void notifyCacheLoadComplete() throws KGameServerException {
		rwLock.writeLock().lock();
		try {

			PVPBattlefield.initBattlefield(KResWarConfig.资源战PVP地图文件名, KResWarConfig.资源战PVP地图背景音乐);

			for (CityTemplate city : KResWarDataManager.mCityTempManager.getDataCache().values()) {
				ConcurrentHashMap<Integer, ResPoint> resPoints = new ConcurrentHashMap<Integer, ResWarCity.ResPoint>();
				for (ResPointTemplate point : KResWarDataManager.mResPointTempManager.getDataCache().values()) {
					resPoints.put(point.ID, new ResPoint(point.ID));
				}
				ResWarCity warCity = new ResWarCity(city.ID, resPoints);
				allCityMap.put(city.ID, warCity);
				warCity.bidRank.loadFromDB();
			}
		} finally {
			rwLock.writeLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 选出各城市对战军团
	 * 不入围军团退还竞价费用，清空竞价榜
	 * 
	 * @author CamusHuang
	 * @creation 2014-5-12 下午3:02:43
	 * </pre>
	 */
	static void notifyForBidEnd() {
		rwLock.writeLock().lock();
		try {
			for (ResWarCity city : allCityMap.values()) {
				city.notifyForBidEnd();
			}
		} finally {
			rwLock.writeLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 搜索玩家占领的资源点
	 * 
	 * @param gangId
	 * @param roleId
	 * @return Object[] { 城市, 资源点 }
	 * @author CamusHuang
	 * @creation 2014-5-13 下午4:29:47
	 * </pre>
	 */
	static Object[] searchOccedResPoint(long gangId, long roleId) {
		rwLock.readLock().lock();
		try {
			for (ResWarCity city : allCityMap.values()) {
				if (city.isWarEnd()) {
					continue;
				}
				CityWarData warData = city.getCityWarData();
				if (warData != null) {
					if (warData.isGangJoined(gangId)) {
						for (ResPoint point : city.resPointManager.values()) {
							if (point.getOccGangId() == gangId && point.getOccRoleId() == roleId) {
								return new Object[] { city, point };
							}
						}
					}
				}
			}
			return null;
		} finally {
			rwLock.readLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 清理玩家占领的资源点
	 * 
	 * @param gangId
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2014-5-13 下午4:29:47
	 * </pre>
	 */
	static Set<ResWarCity> clearOccedResPoint(long gangId, long roleId) {
		rwLock.writeLock().lock();
		try {
			Set<ResWarCity> result = new HashSet<ResWarCity>();
			for (ResWarCity city : allCityMap.values()) {
				if (city.isWarEnd()) {
					continue;
				}
				CityWarData warData = city.getCityWarData();
				if (warData != null) {
					if (warData.isGangJoined(gangId)) {
						for (ResPoint point : city.resPointManager.values()) {
							if (point.getOccGangId() == gangId && point.getOccRoleId() == roleId) {
								point.clearOcc();
								result.add(city);
							}
						}
					}
				}
			}
			return result;
		} finally {
			rwLock.writeLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 获取所有城市中记录的在城市内的角色ID
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-15 上午11:04:34
	 * </pre>
	 */
	static Set<Long> getRoleIdsInAllCityWars() {
		Set<Long> set = new HashSet<Long>();
		ResWarDataCenter.rwLock.readLock().lock();
		try {
			for (ResWarCity city : ResWarDataCenter.allCityMap.values()) {
				if (city.isWarEnd()) {
					continue;
				}
				city.getRoleIdsInWar(set);
			}
		} finally {
			ResWarDataCenter.rwLock.readLock().unlock();
		}
		return set;
	}

	/**
	 * <pre>
	 * 获取所有城市竞价榜中的军团ID
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-15 上午11:04:52
	 * </pre>
	 */
	static Set<Long> getGangIdsInAllCityRanks() {
		Set<Long> set = new HashSet<Long>();
		rwLock.readLock().lock();
		try {
			for (ResWarCity city : allCityMap.values()) {
				for (BPElement e : city.bidRank.getCopyElementList()) {
					set.add(e.elementId);
				}
			}
		} finally {
			rwLock.readLock().unlock();
		}
		return set;
	}

	/**
	 * <pre>
	 * 获取所有城市对战军团ID
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-15 上午11:04:52
	 * </pre>
	 */
	static Set<Long> getGangIdsInAllCityWars() {
		Set<Long> set = new HashSet<Long>();
		rwLock.readLock().lock();
		try {
			for (ResWarCity city : allCityMap.values()) {
				CityWarData war = city.getCityWarData();
				if (war == null) {
					continue;
				}
				if (war.gangA != null) {
					set.add(war.gangA.gangId);
				}
				if (war.gangB != null) {
					set.add(war.gangB.gangId);
				}
			}
		} finally {
			rwLock.readLock().unlock();
		}
		return set;
	}

	static boolean isRoleJoinedCity(long roleId) {
		rwLock.readLock().lock();
		try {
			long gangId = KSupportFactory.getGangSupport().getGangIdByRoleId(roleId);

			for (ResWarCity city : allCityMap.values()) {
				if (city.isWarEnd()) {
					continue;
				}
				CityWarData warData = city.getCityWarData();
				if (warData != null) {
					if (warData.isRoleJoined(gangId, roleId)) {
						return true;
					}
				}
			}
			return false;
		} finally {
			rwLock.readLock().unlock();
		}
	}

	static boolean isCityInWar(int cityId) {
		rwLock.readLock().lock();
		try {
			ResWarCity city = allCityMap.get(cityId);
			if (city == null) {
				return false;
			}

			return !city.isWarEnd();
		} finally {
			rwLock.readLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 活动进行中，进行裁决，确定胜负
	 * 
	 * @author CamusHuang
	 * @creation 2014-5-14 下午5:17:41
	 * </pre>
	 */
	static boolean judgeInWar() {
		boolean isChange = false;
		rwLock.writeLock().lock();
		try {
			boolean isAllEnd = true;
			for (ResWarCity city : allCityMap.values()) {
				if (city.isWarEnd()) {
					continue;
				}
				city.notifyJudgeInWar();
				//
				if (city.isWarEnd()) {
					isChange = true;
					sendWarEndReward(city);
				} else {
					isAllEnd = false;
				}
			}
			return isAllEnd;
		} finally {
			rwLock.writeLock().unlock();

			// 通知客户端刷新城市列表
			if (isChange) {
				// 收集所有城市对战军团名单
				Set<Long> pkGangIds = ResWarDataCenter.getGangIdsInAllCityWars();
				KGrwSynMsg.sendCityListStatusChangeMsg(pkGangIds);
			}
		}
	}

	/**
	 * <pre>
	 * 活动结束，进行裁决
	 * 确定胜负，发送最终奖励，清理现场
	 * 
	 * @author CamusHuang
	 * @creation 2014-5-14 下午5:17:41
	 * </pre>
	 */
	static void judgeForWarEnd() {
		rwLock.writeLock().lock();
		try {
			for (ResWarCity city : allCityMap.values()) {
				if (city.isWarEnd()) {
					continue;
				}
				//
				city.notifyJudgeForEnd();
				//
				sendWarEndReward(city);
			}
		} finally {
			rwLock.writeLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 确定胜负，发送最终奖励，清理现场
	 * 
	 * @param city
	 * @author CamusHuang
	 * @creation 2014-5-14 下午6:09:31
	 * </pre>
	 */
	private static void sendWarEndReward(ResWarCity city) {

		GangData[] temp = city.getResult();

		if (temp == null || (temp[0] == null && temp[1] == null)) {
			return;
		}

		GangData winGangData = temp[0];
		GangData loseGangData = temp[1];

		// 战斗失败的一方，系统会返还参与竞价的60%资金
		if (loseGangData != null) {
			int backPrice = Math.max(1, loseGangData.bidPrice * KResWarConfig.SeizeFailBackRate);
			// 返还军团资金
			KGangLogic.backResourceForBidResWar(ResWarDataCenter.RESWAR_LOGGER, loseGangData.gangId, backPrice);
			// 系统通知：本届军团争夺失败，系统返还竞价资金XXX
			ResWarSystemBrocast.onWarEnd_Lose_BackBidResource(city.id, loseGangData.gangId, backPrice);
		}

		// 发放奖励
		sendWarEndReward(city, true, winGangData);
		sendWarEndReward(city, false, loseGangData);

		// 系统通知
		ResWarSystemBrocast.onWarEnd_City(city, winGangData, loseGangData);
	}

	/**
	 * <pre>
	 * 发放奖励
	 * 玩家奖励通过邮件形式发放，发放对象为：
	 * 			参与过资源争夺活动的军团成员
	 * 			奖励结算时未参与活动但处于在线状态的成员
	 * 
	 * @author CamusHuang
	 * @creation 2014-5-14 下午6:25:47
	 * </pre>
	 */
	private static void sendWarEndReward(ResWarCity city, boolean isWin, GangData gangData) {
		if (gangData == null) {
			return;
		}
		GangIntegrateData dbGangData = KGangModuleExtension.getGangAndSet(gangData.gangId);
		if (dbGangData == null) {
			return;
		}
		KGang gang = (KGang) dbGangData.getGang();
		KGangExtCASet gangCASet = (KGangExtCASet) dbGangData.getGangExtCASet();

		CityTemplate temp = city.getTemplate();
		RewardData reward = KResWarDataManager.mRewardDataManager.getData(temp.citylv, isWin);

		{// 军团经验&资金=军团等级对应经验*领地胜负军团经验奖励系数
			GangLvData gangLvData = KResWarDataManager.mGangLvDataManager.getData(gang.getLevel());
			int addExp = reward.LegionExp * gangLvData.exp;
			KSupportFactory.getGangSupport().addGangExp(gangData.gangId, addExp, addExp);
			// 争夺战获胜利时，军团动态内记录信息：军团成功在本次争夺战中占领了XXX城市，获得XXX军团经验
			// 争夺战失败时，军团动态内记录信息：军团未能在本次争夺战中占领了XXX城市，获得XXX军团经验
			ResWarSystemBrocast.onWarEnd_Gang(temp, isWin, gang, gangCASet, addExp);
		}

		// 个人奖励通过邮件发送
		String mailTitle = StringUtil.format(GangResWarTips.军团资源战x城市奖励发放邮件标题, temp.cityname);
		String mailContent = isWin ? GangResWarTips.军团资源战x城市胜出奖励发放邮件内容 : GangResWarTips.军团资源战x城市战败奖励发放邮件内容;
		mailContent = StringUtil.format(mailContent, temp.cityname);
		BaseMailContent baseMail = new BaseMailContent(mailTitle, mailContent, null, null);

		Set<Long> allMems = new HashSet<Long>(gangData.getAllMemSetCache());
		for (Long roleId : gang.getAllElementRoleIds()) {
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role.isOnline() || allMems.contains(roleId)) {
				// 个人贡献=（1+int(人物等级/20))*领地胜负贡献奖励系数
				// 个人荣誉=人物等级对应荣誉*领地胜负荣誉奖励系数
				// 道具礼包奖励
				List<KCurrencyCountStruct> moneys = new ArrayList<KCurrencyCountStruct>();
				int roleLv = role.getLevel();
				RoleLvData roleLvData = KResWarDataManager.mRoleLvDataManager.getData(roleLv);
				int con = (1 + (roleLv / 20)) * reward.Contribution;
				int honor = roleLvData.honor * reward.honor;
				moneys.add(new KCurrencyCountStruct(KCurrencyTypeEnum.GANG_CONTRIBUTION, con));
				moneys.add(new KCurrencyCountStruct(KCurrencyTypeEnum.SCORE, honor));
				//
				BaseRewardData rewardData = new BaseRewardData(null, moneys, Arrays.asList(reward.addItem), null, null);
				BaseMailRewardData mailReward = new BaseMailRewardData(0, baseMail, rewardData);
				KSupportFactory.getMailModuleSupport().sendAttMailBySystem(roleId, mailReward, PresentPointTypeEnum.城市占领);
			}
		}

	}

	/**
	 * <pre>
	 * 清理所有数据
	 * 
	 * @param isForce
	 * @author CamusHuang
	 * @creation 2013-10-11 下午11:18:12
	 * </pre>
	 */
	static void clearData() {
		rwLock.writeLock().lock();
		try {
			for (ResWarCity city : allCityMap.values()) {
				city.clearData();
			}
		} finally {
			rwLock.writeLock().unlock();
		}
	}

	static void saveData(String tips) {
		rwLock.readLock().lock();
		try {
			String saveFileName = ResWarDataCenter.saveFileName + tips;

			String url = saveDirPath + saveFileName + saveFileNameSuffix;
			KXmlWriter writer = null;
			try {
				File file = new File(saveDirPath);
				file.mkdirs();

				writer = new KXmlWriter(url, true);

				Element root = writer.getRoot();
				root.setAttribute("保存时间", UtilTool.DATE_FORMAT.format(new Date()));
				root = new Element("停服缓存数据");
				writer.addElement(root);

				{
					ResWarStatusEnum status = ResWarStatusManager.getNowStatus();
					Element elementA = new Element("当前状态");
					root.addContent(elementA);
					elementA.setAttribute("id", status.sign + "");
					elementA.setAttribute("name", status.name);
				}
				{
					Element elementA = new Element("城市列表");
					root.addContent(elementA);
					for (ResWarCity city : allCityMap.values()) {
						Element elementB = new Element("城市");
						elementA.addContent(elementB);
						//
						elementB.setAttribute("id", city.id + "");
						{
							Element elementC = new Element("占领");
							elementB.addContent(elementC);
							GangData occGangData = city.getOccGangData();

							elementC.setAttribute("状态", (occGangData != null) + "");
							if (occGangData != null) {
								elementC.setAttribute("id", occGangData.gangId + "");
								elementC.setAttribute("name", UtilTool.getNotNullString(occGangData.gangName));
								elementC.setAttribute("price", occGangData.bidPrice + "");
							}
						}
						{
							CityWarData warData = city.getCityWarData();
							Element elementC = new Element("对战");
							elementB.addContent(elementC);

							elementC.setAttribute("状态", (warData != null) + "");
							if (warData != null) {
								//
								{
									Element elementD = new Element("军团A");
									elementC.addContent(elementD);

									elementD.setAttribute("状态", (warData.gangA != null) + "");
									if (warData.gangA != null) {
										GangData gang = warData.gangA;
										//
										elementD.setAttribute("id", gang.gangId + "");
										elementD.setAttribute("name", UtilTool.getNotNullString(gang.gangName));
										elementD.setAttribute("price", gang.bidPrice + "");
										elementD.setAttribute("score", gang.getScore() + "");
									}
								}
								{
									Element elementD = new Element("军团B");
									elementC.addContent(elementD);

									elementD.setAttribute("状态", (warData.gangB != null) + "");
									if (warData.gangB != null) {
										GangData gang = warData.gangB;
										//
										elementD.setAttribute("id", gang.gangId + "");
										elementD.setAttribute("name", UtilTool.getNotNullString(gang.gangName));
										elementD.setAttribute("price", gang.bidPrice + "");
										elementD.setAttribute("score", gang.getScore() + "");
									}
								}
							}
						}
						{
							Element elementD = new Element("资源点");
							elementB.addContent(elementD);

							for (ResPoint point : city.resPointManager.values()) {
								Element elementC = new Element("资源点");
								elementD.addContent(elementC);
								//
								elementC.setAttribute("id", point.id + "");

								elementC.setAttribute("occGangId", point.getOccGangId() + "");
								elementC.setAttribute("occRoleId", point.getOccRoleId() + "");
								elementC.setAttribute("occRoleName", UtilTool.getNotNullString(point.getOccRoleName()));
								elementC.setAttribute("occRoleLv", point.getOccRoleLv() + "");
								elementC.setAttribute("occEndTime", point.getOccEndTime() + "");
								//
								elementC.setAttribute("pkGangId", point.getPkGangId() + "");
								elementC.setAttribute("pkRoleId", point.getPkRoleId() + "");
								elementC.setAttribute("pkRoleName", UtilTool.getNotNullString(point.getPkRoleName()));
							}
						}
					}
				}

				writer.output();
				// COPY一份备份
				UtilTool.copyFile(url, url + ".bak");

				ResWarDataCenter.RESWAR_LOGGER.error("保存成功！路径={}", url);

			} catch (Exception e) {
				ResWarDataCenter.RESWAR_LOGGER.error("保存出错！路径={}", url);
				ResWarDataCenter.RESWAR_LOGGER.error(e.getMessage(), e);
				return;
			}
		} finally {
			rwLock.readLock().unlock();
		}
	}

	static CacheData loadData() {

		CacheData cacheData = null;
		try {

			String url = saveDirPath + saveFileName + saveFileNameSuffix;
			File file = new File(url);
			if (file.exists()) {
				if (file.isDirectory()) {
					throw new Exception("文件路径不能是目录！path=" + url);
				}
			} else {
				ResWarDataCenter.RESWAR_LOGGER.warn("不存在数据文件！");
				return cacheData;
			}
			Document doc = XmlUtil.openXml(file);
			Element root = doc.getRootElement();

			cacheData = new CacheData(root.getChild("停服缓存数据"));

		} catch (Exception e) {
			ResWarDataCenter.RESWAR_LOGGER.error(e.getMessage(), e);
		}

		return cacheData;
	}

	/**
	 * <pre>
	 * 从外部文件加载的数据
	 * 
	 * @author CamusHuang
	 * @creation 2013-10-29 下午12:16:16
	 * </pre>
	 */
	static class CacheData {
		ResWarStatusEnum nowStatus;

		CacheData(Element root) {

			{
				nowStatus = ResWarStatusEnum.getEnum(Byte.parseByte(root.getChild("当前状态").getAttributeValue("id")));
			}
			{
				List<Element> tempEs = root.getChild("城市列表").getChildren("城市");
				for (Element temp : tempEs) {
					int cityId = Integer.parseInt(temp.getAttributeValue("id"));
					ResWarCity city = allCityMap.get(cityId);
					//
					{
						Element elementC = temp.getChild("占领");
						city.paramsOccGang(elementC);
					}
					{
						Element elementC = temp.getChild("对战");
						city.paramsWarGangs(elementC);
					}
					{
						Element elementD = temp.getChild("资源点");
						for (Object obj : elementD.getChildren("资源点")) {
							Element elementC = (Element) obj;
							int id = Integer.parseInt(elementC.getAttributeValue("id"));
							//
							ResPoint point = city.resPointManager.get(id);
							point.params(elementC);
						}
					}
				}
			}
		}
	}

}
