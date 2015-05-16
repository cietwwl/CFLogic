package com.kola.kmp.logic.gamble.wish;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import jxl.read.biff.BiffException;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;
import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.XmlUtil;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kgame.cache.item.Item;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.actionrecord.KActionRecorder;
import com.kola.kmp.logic.actionrecord.KActionType;
import com.kola.kmp.logic.competition.KCompetitionModule;
import com.kola.kmp.logic.competition.KCompetitionModuleConfig;
import com.kola.kmp.logic.competition.KCompetitionServerMsgSender;
import com.kola.kmp.logic.gamble.KGambleRoleExtCACreator;
import com.kola.kmp.logic.gamble.KGambleRoleExtData;
import com.kola.kmp.logic.gamble.wish.KWishItemPool.KDropableItem;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.mission.daily.KDailyMissionManager;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.pet.KPetTemplate;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KXmlWriter;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.CompetitionTips;
import com.kola.kmp.logic.util.tips.GambleTips;
import com.kola.kmp.logic.util.tips.LevelTips;
import com.kola.kmp.protocol.competition.KCompetitionProtocol;
import com.kola.kmp.protocol.gamble.KGambleProtocol;

/**
 * 许愿系统管理器
 * 
 * @author zhaizl
 * 
 */
public class KWishSystemManager {

	private static final Logger WISH_LOGGER = KGameLogger.getLogger("wishLogger");

	static final Logger _LOGGER = KGameLogger.getLogger(KWishSystemManager.class);

	private final String _savePath = "./res/output/wishdata.xml";

	public static final byte WISH_TYPE_POOR = 0;// 屌丝扭蛋类型
	public static final byte WISH_TYPE_RICH = 1;// 高富帅扭蛋类型

	public static final byte FREE_WISH_STATUS_CLOSE = 0;// 免费许愿状态：未开启
	public static final byte FREE_WISH_STATUS_OPEN = 1;// 免费许愿状态：开启
	public static final byte FREE_WISH_STATUS_COMPLETE = 2;// 免费许愿状态：已完成

	private KWishItemPool wishPool = new KWishItemPool();

	public List<GirdReward> girdRewardList = new ArrayList<GirdReward>();

	public static Map<Integer, CircleReward> circleRewardMap = new LinkedHashMap<Integer, CircleReward>();

	// public static Map<Integer, WishShowInfoData> showWishPriceMapByType = new
	// LinkedHashMap<Integer, WishShowInfoData>();

	public static Map<Integer, List<WishShowInfoData>> poorShowWishPriceMap = new LinkedHashMap<Integer, List<WishShowInfoData>>();
	public static Map<Integer, List<WishShowInfoData>> richShowWishPriceMap = new LinkedHashMap<Integer, List<WishShowInfoData>>();

	public static List<WishShowInfoData> defaultPoorWishShowDataList = new ArrayList<WishShowInfoData>();
	public static List<WishShowInfoData> defaultRichWishShowDataList = new ArrayList<WishShowInfoData>();

	private static Map<Integer, LuckyGirdCounter> luckyGirdMap = new HashMap<Integer, LuckyGirdCounter>();

	public static AtomicLong diamondLotteryPool;

	public ConcurrentLinkedHashMap<Long, WishBroadcastInfo> broadcastMap;

	public AtomicLong broadcastInfoIdGen = new AtomicLong(0);

	public long nowMaxId = 0;

	private static int TOTAL_GIRD_COUNT;

	private static final int MAX_BROADCAST_COUNT = 10;
	// 欢乐送个人中奖间隔天数的中奖概率，Key：距离上次中奖的间隔天数，Value：概率（万分比）
	public static Map<Integer, Integer> luckyPriceRateMap = new LinkedHashMap<Integer, Integer>();
	public static int MIN_LUCKY_PRICE_PASS_DAY;
	public static int MAX_LUCKY_PRICE_PASS_DAY;

	public static int _poorWishUsePoint;// 单次屌丝许愿使用点数
	public static int _poorWishUseTicket;// 单次屌丝许愿使用扭蛋券
	public static int _poorWish10UsePoint;// 10次屌丝许愿使用点数
	public static int _poorWish10UseTicket;// 10次屌丝许愿使用扭蛋券
	public static int _richWishUsePoint;// 单次高富帅许愿使用点数
	public static int _richWishUseTicket;// 单次高富帅许愿使用扭蛋券
	public static int _richWish10UsePoint;// 10次高富帅许愿使用点数
	public static int _richWish10UseTicket;// 10次高富帅许愿使用扭蛋券
	public static int _wishTakeRate;// 许愿提取钻石比例
	public static int _initPoolDiamondCount;// 初始化彩池钻石数
	public static int _freeWishTimeSecond;// 免费扭蛋时间间隔(单位：秒)
	public static int _poorWishAddDiceCount;// 屌丝许愿增加掷点次数
	public static int _richWishAddDiceCount;// 高富帅许愿增加掷点次数
	public static int _guideWishPresentTicket;// 首次开放许愿赠送扭蛋券数量
	public static KPetTemplate _guideWishPresentPetTemplate;// //首次开放许愿赠送宠物模版
	public static KItemTempAbs _guideWishPresentPetItemTemplate;// //首次开放许愿赠送宠物对应的道具模版

	public void init(String configPath) throws KGameServerException {
		// WISH_LOGGER.info("$$$$$$$$$$$$$$$$$  启动许愿  #######################");
		Document doc = XmlUtil.openXml(configPath);
		if (doc != null) {
			Element root = doc.getRootElement();

			// ReflectPaser.parse(KWishSystemManager.class, doc.getRootElement()
			// .getChildren("config"));
			//
			// _initPoolDiamondCount = Integer.parseInt(root
			// .getChild("poolConfig").getAttributeValue("initValue"));
			//
			// List<Element> takeRateEList = root.getChild("poolConfig")
			// .getChildren("takeRate");
			// for (Element takeRateE : takeRateEList) {
			// int girdIndex = Integer.parseInt(takeRateE
			// .getAttributeValue("girdIndex"));
			// if (!(girdIndex == 9 || girdIndex == 13 || girdIndex == 22)) {
			// throw new KGameServerException("初始化许愿系统配置文件:" + configPath
			// + "的takeRate字段<girdIndex>错误，该值只能为9、13、22。当前值="
			// + girdIndex);
			//
			// }
			// int targetCount = Integer.parseInt(takeRateE
			// .getAttributeValue("targetCount"));
			// int rate = Integer
			// .parseInt(takeRateE.getAttributeValue("rate"));
			//
			// LuckyGirdCounter counter = new LuckyGirdCounter(girdIndex,
			// targetCount, rate);
			// luckyGirdMap.put(counter.girdId, counter);
			// }

			// diamondLotteryPool = new AtomicLong(_initPoolDiamondCount);

			ConcurrentLinkedHashMap.Builder<Long, WishBroadcastInfo> builder = new ConcurrentLinkedHashMap.Builder<Long, WishBroadcastInfo>();
			builder.initialCapacity(100);
			builder.maximumWeightedCapacity(200);
			builder.concurrencyLevel(32);
			builder.weigher(Weighers.singleton());
			broadcastMap = builder.build();

			String wishExcelFilePath = root.getChildText("wishExcelFilePath");
			loadExcelData(wishExcelFilePath);

			initNext6GirdWeight();

			diamondLotteryPool = new AtomicLong(_initPoolDiamondCount);

			readWishData();
		} else {
			throw new NullPointerException("许愿系统配置不存在！！");
		}

		// WISH_LOGGER.info("$$$$$$$$$$$$$$$$$  启动许愿成功！！！  #######################");
	}

	private void loadExcelData(String xlsPath) throws KGameServerException {
		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(xlsPath);
		} catch (BiffException e) {
			throw new KGameServerException("读取许愿系统excel表头发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取许愿系统excel表头发生错误！", e);
		}

		if (xlsFile != null) {
			// 屌丝扭蛋数据表
			int dataRowIndex = 5;
			KGameExcelTable dataTable;
			KGameExcelRow[] allDataRows;

			String[] tableName = { "屌丝扭蛋", "屌丝扭蛋必得物", "高富帅扭蛋", "高富帅扭蛋必得物", "免费屌丝扭蛋", "高富帅扭蛋3件必得物" };
			byte[] poolType = { KWishItemPool.POOR_NORMAL_POOL, KWishItemPool.POOR_SPECIAL_POOL, KWishItemPool.RICH_NORMAL_POOL, KWishItemPool.RICH_SPECIAL_POOL, KWishItemPool.FREE_NORMAL_POOL,
					KWishItemPool.RICH_SPECIAL_ITEM_POOL };
			for (int k = 0; k < tableName.length; k++) {

				dataTable = xlsFile.getTable(tableName[k], dataRowIndex);
				allDataRows = dataTable.getAllDataRows();

				if (allDataRows != null) {
					for (int i = 0; i < allDataRows.length; i++) {
						int dropId = allDataRows[i].getInt("index");
						String itemCode = allDataRows[i].getData("id");
						int count = allDataRows[i].getInt("quantity");
						int openLv = allDataRows[i].getInt("mixlvl");
						int closeLv = allDataRows[i].getInt("maxlvl");
						int weight = allDataRows[i].getInt("pro");
						boolean isShow = true;// (allDataRows[i].getInt("news")
												// == 1);
						KItemTempAbs itemTemp = KSupportFactory.getItemModuleSupport().getItemTemplate(itemCode);
						if (itemTemp == null) {
							throw new KGameServerException("初始化许愿系统表<" + tableName[k] + ">的字段<id>错误，找不到对应的道具：" + itemCode + "，excel行数：" + allDataRows[i].getIndexInFile());
						}
						KDropableItem dItem = new KDropableItem(dropId, openLv, closeLv, weight, itemCode, itemTemp.extItemName, count, isShow);
						wishPool.addDropableItem(poolType[k], dItem);
					}
				}
			}

			// 掷点表数据
			dataTable = xlsFile.getTable("掷点", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {

				for (int i = 0; i < allDataRows.length; i++) {
					int index = allDataRows[i].getInt("index");
					if (index == 0) {
						GirdReward startGird = new GirdReward();
						startGird.isStartGird = true;
						startGird.girdCount = 0;
						startGird.weight = allDataRows[i].getInt("weight");
						girdRewardList.add(startGird);
					} else {
						boolean isLuckyPrice = (allDataRows[i].getInt("news") == 1);
						if ((index == 9 || index == 13 || index == 22)) {
							if (!isLuckyPrice) {
								throw new KGameServerException("初始化许愿系统表<掷点>的字段<index>错误，第" + index + "格必须配置为幸运大奖，" + "，excel行数：" + allDataRows[i].getIndexInFile());
							}
						} else {
							if (isLuckyPrice) {
								throw new KGameServerException("初始化许愿系统表<掷点>的字段<index>错误，第" + index + "格不能配置为幸运大奖，" + "，excel行数：" + allDataRows[i].getIndexInFile());
							}
						}
						GirdReward reward;
						if (isLuckyPrice) {
							int weight = allDataRows[i].getInt("weight");
							reward = new GirdReward(index, isLuckyPrice, null, weight);
							int targetCount = allDataRows[i].getInt("IntervalTimes");
							int rate = allDataRows[i].getInt("percent");
							int roleRunStepLimit = allDataRows[i].getInt("StartTimes");
							LuckyGirdCounter counter = new LuckyGirdCounter(index, targetCount, rate, roleRunStepLimit);
							luckyGirdMap.put(counter.girdId, counter);
						} else {
							String itemCode = allDataRows[i].getData("id");
							int itemCount = allDataRows[i].getInt("quantity");
							int weight = allDataRows[i].getInt("weight");
							if (KSupportFactory.getItemModuleSupport().getItemTemplate(itemCode) == null) {
								throw new KGameServerException("初始化许愿系统表<掷点>的字段<id>错误，找不到对应的道具：" + itemCode + "，excel行数：" + allDataRows[i].getIndexInFile());
							}
							reward = new GirdReward(index, isLuckyPrice, new ItemCountStruct(itemCode, itemCount), weight);
						}
						girdRewardList.add(reward);
					}
				}
				TOTAL_GIRD_COUNT = girdRewardList.size();
			}

			// 掷点表数据
			dataTable = xlsFile.getTable("圈数奖励", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					int circle = allDataRows[i].getInt("RingReward");
					String itemCode = allDataRows[i].getData("id");
					if (KSupportFactory.getItemModuleSupport().getItemTemplate(itemCode) == null) {
						throw new KGameServerException("初始化许愿系统表<圈数奖励>的字段<id>错误，找不到对应的道具：" + itemCode + "，excel行数：" + allDataRows[i].getIndexInFile());
					}
					CircleReward reward = new CircleReward(circle, new ItemCountStruct(itemCode, 1));
					this.circleRewardMap.put(circle, reward);
				}
			}

			// 屌丝许愿展示表数据
			dataTable = xlsFile.getTable("屌丝许愿展示", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {
				int maxLv = 0;
				for (int i = 0; i < allDataRows.length; i++) {
					int index = allDataRows[i].getInt("index");
					byte type = allDataRows[i].getByte("type");
					int maxlvl = allDataRows[i].getInt("maxlvl");
					String itemCode = allDataRows[i].getData("id");
					KItemTempAbs itemTemp = KSupportFactory.getItemModuleSupport().getItemTemplate(itemCode);
					if (itemTemp == null) {
						throw new KGameServerException("初始化许愿系统表<屌丝许愿展示>的字段<id>错误，找不到对应的道具：" + itemCode + "，excel行数：" + allDataRows[i].getIndexInFile());
					}
					WishShowInfoData data = new WishShowInfoData(index, type, maxlvl, itemTemp);
					if (!this.poorShowWishPriceMap.containsKey(maxlvl)) {
						this.poorShowWishPriceMap.put(maxlvl, new ArrayList<WishShowInfoData>());
					}
					this.poorShowWishPriceMap.get(maxlvl).add(data);
					if (maxlvl > maxLv) {
						maxLv = maxlvl;
					}
				}
				if (maxLv > 0) {
					this.defaultPoorWishShowDataList = this.poorShowWishPriceMap.get(maxLv);
				} else {
					throw new KGameServerException("初始化许愿系统表<屌丝许愿展示>表错误，此表必须有数据存在！");
				}
			}

			// 高富帅许愿展示表数据
			dataTable = xlsFile.getTable("高富帅许愿展示", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {
				int maxLv = 0;
				for (int i = 0; i < allDataRows.length; i++) {
					int index = allDataRows[i].getInt("index");
					byte type = allDataRows[i].getByte("type");
					int maxlvl = allDataRows[i].getInt("maxlvl");
					String itemCode = allDataRows[i].getData("id");
					KItemTempAbs itemTemp = KSupportFactory.getItemModuleSupport().getItemTemplate(itemCode);
					if (itemTemp == null) {
						throw new KGameServerException("初始化许愿系统表<高富帅许愿展示>的字段<id>错误，找不到对应的道具：" + itemCode + "，excel行数：" + allDataRows[i].getIndexInFile());
					}
					WishShowInfoData data = new WishShowInfoData(index, type, maxlvl, itemTemp);
					if (!this.richShowWishPriceMap.containsKey(maxlvl)) {
						this.richShowWishPriceMap.put(maxlvl, new ArrayList<WishShowInfoData>());
					}
					this.richShowWishPriceMap.get(maxlvl).add(data);
					if (maxlvl > maxLv) {
						maxLv = maxlvl;
					}
				}
				if (maxLv > 0) {
					this.defaultRichWishShowDataList = this.richShowWishPriceMap.get(maxLv);
				} else {
					throw new KGameServerException("初始化许愿系统表<高富帅许愿展示>表错误，此表必须有数据存在！");
				}
			}

			// 许愿参数表数据
			dataTable = xlsFile.getTable("许愿参数", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					_poorWishUsePoint = allDataRows[i].getInt("poorWishUsePoint");
					_poorWish10UsePoint = allDataRows[i].getInt("poorWish10UsePoint");
					_richWishUsePoint = allDataRows[i].getInt("richWishUsePoint");
					_richWish10UsePoint = allDataRows[i].getInt("richWish10UsePoint");
					_wishTakeRate = allDataRows[i].getInt("wishTakeRate");
					_initPoolDiamondCount = allDataRows[i].getInt("initPoolDiamondCount");
					_poorWishAddDiceCount = allDataRows[i].getInt("poorWishAddTime");
					_richWishAddDiceCount = allDataRows[i].getInt("richWishAddTime");
					_freeWishTimeSecond = allDataRows[i].getInt("FreeTime");
					_poorWishUseTicket = allDataRows[i].getInt("poorWishTicketCount");
					_poorWish10UseTicket = _poorWishUseTicket * 10;
					_richWishUseTicket = allDataRows[i].getInt("richWishTicketCount");
					_richWish10UseTicket = _richWishUseTicket * 10;
					_guideWishPresentTicket = allDataRows[i].getInt("GiveWishTicketCount");
					int petTempId = allDataRows[i].getInt("guidePetID");
					String petItemTempId = allDataRows[i].getData("guidePetItemID");
					_guideWishPresentPetTemplate = KSupportFactory.getPetModuleSupport().getPetTemplate(petTempId);
					if (_guideWishPresentPetTemplate == null) {
						throw new KGameServerException("初始化许愿系统表<许愿参数>表guidePetID错误，不存在宠物模版=" + petTempId);
					}
					_guideWishPresentPetItemTemplate = KSupportFactory.getItemModuleSupport().getItemTemplate(petItemTempId);
					if (_guideWishPresentPetItemTemplate == null) {
						throw new KGameServerException("初始化许愿系统表<许愿参数>表guidePetItemID错误，不存在宠物对应的道具模版=" + petItemTempId);
					}
				}
			}

			// 欢乐送中奖时间保护数据
			dataTable = xlsFile.getTable("欢乐送中奖时间保护", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					int day = allDataRows[i].getInt("day");
					int zoomRate = allDataRows[i].getInt("ZoomRate");
					luckyPriceRateMap.put(day, zoomRate);
					if (MIN_LUCKY_PRICE_PASS_DAY == 0 || day < MIN_LUCKY_PRICE_PASS_DAY) {
						MIN_LUCKY_PRICE_PASS_DAY = day;
					}
					if (day > MAX_LUCKY_PRICE_PASS_DAY) {
						MAX_LUCKY_PRICE_PASS_DAY = day;
					}
				}
			}
		}
	}

	private void initNext6GirdWeight() {
		for (GirdReward girdData : girdRewardList) {
			int totalWeight = 0;
			for (int i = 1; i <= 6; i++) {
				int nextGirdIndex = caculateDiceIndex(girdData.girdCount, i);
				GirdReward nextGirdData = girdRewardList.get(nextGirdIndex);
				totalWeight += nextGirdData.weight;
				girdData.next6GirdWeightMap.put(nextGirdIndex, nextGirdData.weight);
				girdData.next6GirdTotalWeight = totalWeight;
			}
		}
	}

	public void serverStartCompleted() {

	}

	public void serverShutdown() {
		saveWishData();
	}

	private void readWishData() throws KGameServerException {
		try {
			boolean isReload = true;
			File file = new File(_savePath);
			if (!file.exists()) {
				_LOGGER.warn("上次【许愿系统】保存的数据不存在，走开服初始化流程...");
				isReload = false;
			} else {
				if (file.isDirectory()) {
					_LOGGER.warn("【许愿系统】文件路径不能是目录！path=" + _savePath);
					isReload = false;
				}
			}
			if (isReload) {
				Document doc = XmlUtil.openXml(_savePath);
				if (doc != null) {
					Element root = doc.getRootElement();

					diamondLotteryPool.set(Integer.parseInt(root.getChildText("poolDiamondCount")));
					List<Element> luckyPriceInfoEList = root.getChild("luckyPriceInfo").getChildren("luckyCounter");
					for (Element luckyInfoE : luckyPriceInfoEList) {
						int girdId = Integer.parseInt(luckyInfoE.getAttributeValue("girdId"));
						int count = Integer.parseInt(luckyInfoE.getAttributeValue("count"));
						if (luckyGirdMap.containsKey(girdId)) {
							luckyGirdMap.get(girdId).initCounter(count);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new KGameServerException("####  读取【许愿系统】彩池数据出现异常。", e);
		}
	}

	private void saveWishData() {
		try {
			KXmlWriter writer = new KXmlWriter(_savePath, true);

			Element poolE = new Element("poolDiamondCount");
			poolE.setText("" + diamondLotteryPool.get());
			writer.addElement(poolE);

			Element luckyInfoE = new Element("luckyPriceInfo");
			for (Integer girdId : luckyGirdMap.keySet()) {
				LuckyGirdCounter counter = luckyGirdMap.get(girdId);
				Element luckE = new Element("luckyCounter");
				luckE.setAttribute("girdId", String.valueOf(girdId));
				luckE.setAttribute("count", String.valueOf(counter.counter.get()));
				luckyInfoE.addContent(luckE);
			}
			writer.addElement(luckyInfoE);

			writer.output();
		} catch (IOException e) {
			_LOGGER.error("##### 保存许愿系统彩池信息出错", e);
		}
	}

	/**
	 * 发送许愿数据
	 * 
	 * @param role
	 */
	public void sendWishData(KRole role, List<String> addInfo) {
		KGambleRoleExtData extData = KGambleRoleExtCACreator.getGambleRoleExtData(role.getId());
		extData.checkAndRestWishData();
		KRoleWishData data = KGambleRoleExtCACreator.getGambleRoleExtData(role.getId()).getWishData();
		List<WishShowInfoData> poorShowList = getWishShowInfoDataList(WISH_TYPE_POOR, role.getLevel());
		List<WishShowInfoData> richShowList = getWishShowInfoDataList(WISH_TYPE_RICH, role.getLevel());

		KGameMessage msg = KGame.newLogicMessage(KGambleProtocol.SM_GET_WISH_DATA);
		boolean isFreeWish = extData.checkIsFreeWish();
		msg.writeBoolean(isFreeWish);
		msg.writeInt(getNextFreeWishRestTimeSeconds(data));

		msg.writeInt(_poorWishUsePoint);
		msg.writeInt(getPoor10WishUsePoint(data.poorWishCount));
		msg.writeInt(_richWishUsePoint);
		msg.writeInt(getRich10WishUsePoint(data.richWishCount));
		msg.writeInt(_poorWishAddDiceCount);
		msg.writeInt(_richWishAddDiceCount);
		msg.writeInt(poorShowList.size());
		for (WishShowInfoData showData : poorShowList) {
			KItemTempAbs itemTemp = showData.itemTemplate;
			msg.writeInt(showData.index);
			msg.writeByte(showData.type);
			KItemMsgPackCenter.packItem(msg, itemTemp, -1);
		}
		msg.writeInt(richShowList.size());
		for (WishShowInfoData showData : richShowList) {
			KItemTempAbs itemTemp = showData.itemTemplate;
			msg.writeInt(showData.index);
			msg.writeByte(showData.type);
			KItemMsgPackCenter.packItem(msg, itemTemp, -1);
		}
		// List<WishBroadcastInfo> infoList = getWishBroadcastInfo(data);
		// msg.writeInt(infoList.size());
		// for (WishBroadcastInfo info : infoList) {
		// msg.writeUtf8String(info.info);
		// }
		if (addInfo != null) {
			msg.writeInt(addInfo.size());
			for (int i = 0; i < addInfo.size(); i++) {
				msg.writeUtf8String(addInfo.get(i));
			}
		} else {
			List<WishBroadcastInfo> infoList = getWishBroadcastInfo(data);
			msg.writeInt(infoList.size());
			for (WishBroadcastInfo info : infoList) {
				msg.writeUtf8String(info.info);
			}
		}
		// -----------以下是骰子数据------------//
		msg.writeInt(data.diceTimes.get());
		msg.writeInt(data.circle.get());
		msg.writeLong(diamondLotteryPool.get());
		msg.writeInt(data.nowGirlIndex.get());
		msg.writeInt(girdRewardList.size());
		for (GirdReward gird : girdRewardList) {
			msg.writeInt(gird.girdCount);
			if (gird.isStartGird) {
				msg.writeBoolean(true);
			} else {
				msg.writeBoolean(gird.isLuckyPrice);
				if (!gird.isLuckyPrice) {
					KItemMsgPackCenter.packItem(msg, gird.item.getItemTemplate(), gird.item.itemCount);
				}
			}
		}

		msg.writeInt(circleRewardMap.size());
		for (CircleReward circle : circleRewardMap.values()) {
			msg.writeInt(circle.circleCount);
			KItemMsgPackCenter.packItem(msg, circle.item.getItemTemplate(), circle.item.itemCount);
		}

		role.sendMsg(msg);
	}

	public void sendUpdateWishData(KRole role, List<String> addInfo) {
		KGambleRoleExtData extData = KGambleRoleExtCACreator.getGambleRoleExtData(role.getId());
		KRoleWishData data = extData.getWishData();

		KGameMessage msg = KGame.newLogicMessage(KGambleProtocol.SM_UPDATE_WISH_DATA);
		msg.writeInt(data.diceTimes.get());
		if (addInfo != null) {
			msg.writeInt(addInfo.size());
			for (int i = 0; i < addInfo.size(); i++) {
				msg.writeUtf8String(addInfo.get(i));
			}
		} else {
			List<WishBroadcastInfo> infoList = getWishBroadcastInfo(data);
			msg.writeInt(infoList.size());
			for (WishBroadcastInfo info : infoList) {
				msg.writeUtf8String(info.info);
			}
		}
		boolean isFreeWish = extData.checkIsFreeWish();
		msg.writeBoolean(isFreeWish);
		msg.writeInt(getNextFreeWishRestTimeSeconds(data));

		role.sendMsg(msg);
	}

	public void sendUpdateDiceData(KRole role) {
		KRoleWishData data = KGambleRoleExtCACreator.getGambleRoleExtData(role.getId()).getWishData();

		KGameMessage msg = KGame.newLogicMessage(KGambleProtocol.SM_UPDATE_DICE_DATA);
		msg.writeInt(data.diceTimes.get());
		msg.writeInt(data.circle.get());
		msg.writeLong(diamondLotteryPool.get());
		role.sendMsg(msg);
	}

	private int getNextFreeWishRestTimeSeconds(KRoleWishData data) {
		if (data.freeWishStatus == FREE_WISH_STATUS_OPEN) {
			return 0;
		} else if (data.freeWishStatus == FREE_WISH_STATUS_COMPLETE) {
			return -1;
		} else {
			long nowTime = System.currentTimeMillis();
			if (nowTime >= data.freeCheckTime + _freeWishTimeSecond * 1000) {
				return 0;
			} else {
				return (int) (((data.freeCheckTime + _freeWishTimeSecond * 1000) - nowTime) / 1000);
			}
		}
	}

	/**
	 * 处理角色许愿
	 * 
	 * @param role
	 * @param wishCount
	 */
	public void processRoleWish(KRole role, byte wishType, boolean isFree, boolean isUse10Count) {
		KGambleRoleExtCACreator.getGambleRoleExtData(role.getId()).checkAndRestWishData();
		KGambleRoleExtData extData = KGambleRoleExtCACreator.getGambleRoleExtData(role.getId());

		KRoleWishData data = extData.getWishData();

		if (!isFree && !isUse10Count) {
			if (extData.checkIsFreeWish()) {
				isFree = true;
			}
		}

		int point, wishCount;
		byte poolType;
		byte specialPoolType;
		int addDiceCount;
		if (isFree) {
			if (!extData.checkIsFreeWish()) {
				KDialogService.sendUprisingDialog(role, GambleTips.getTipsNotFreeWish());
				sendUpdateWishData(role, null);
				return;
			} else {
				poolType = KWishItemPool.FREE_NORMAL_POOL;
				specialPoolType = KWishItemPool.FREE_NORMAL_POOL;
				wishCount = 1;
				point = 0;
				addDiceCount = 0;
			}
		} else {
			if (wishType == WISH_TYPE_POOR) {
				if (isUse10Count) {
					point = getPoor10WishUsePoint(data.poorWishCount);
					wishCount = 10;
				} else {
					point = _poorWishUsePoint;
					wishCount = 1;
				}
				addDiceCount = wishCount * _poorWishAddDiceCount;
				poolType = KWishItemPool.POOR_NORMAL_POOL;
				specialPoolType = KWishItemPool.POOR_SPECIAL_POOL;
			} else {
				if (isUse10Count) {
					point = getRich10WishUsePoint(data.richWishCount);
					wishCount = 10;
				} else {
					point = _richWishUsePoint;
					wishCount = 1;
				}
				addDiceCount = wishCount * _richWishAddDiceCount;
				poolType = KWishItemPool.RICH_NORMAL_POOL;
				specialPoolType = KWishItemPool.RICH_SPECIAL_POOL;
			}
		}

		if (KSupportFactory.getItemModuleSupport().checkEmptyVolumeInBag(role.getId()) < 1) {
			KDialogService.sendUprisingDialog(role, LevelTips.getTipsBagCapacityNotEnough());
			return;
		}

		if (isFree) {
			extData.freeWish();
		} else {
			long result = KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), KCurrencyTypeEnum.DIAMOND, point, UsePointFunctionTypeEnum.许愿, true);
			// 元宝不足，发送提示
			if (result == -1) {
				KDialogService.sendUprisingDialog(role, GambleTips.getTipsWishNotEnoughIgot(wishCount, point));
				return;
			}
			// 增加投骰子次数
			extData.addWishDiceCount(addDiceCount, (wishType == WISH_TYPE_POOR), isUse10Count);

			// 不是免费，抽取对应比例的钻石放入彩池
			diamondLotteryPool.addAndGet((point * _wishTakeRate) / LuckyGirdCounter.TAKE_RATE_MOD);
		}

		List<String> addInfo = new ArrayList<String>();
		StringBuffer buffer = new StringBuffer();
		List<ItemCountStruct> priceList = new ArrayList<ItemCountStruct>();
		KDropableItem dropableItem;

		for (int i = 0; i < wishCount; i++) {
			if (isUse10Count && i == 0) {
				dropableItem = wishPool.getDropableItem(specialPoolType, role.getLevel());
			} else if (isUse10Count && i >= 1 && i <= 3 && wishType == WISH_TYPE_RICH) {
				dropableItem = wishPool.getDropableItem(KWishItemPool.RICH_SPECIAL_ITEM_POOL, role.getLevel());
			} else {
				dropableItem = wishPool.getDropableItem(poolType, role.getLevel());
			}
			if (dropableItem != null) {
				priceList.add(new ItemCountStruct(dropableItem.itemCode, dropableItem.dropCount));
				String info = GambleTips.getTipsWishBigPriceInfo(role.getExName(), dropableItem.extItemName, dropableItem.dropCount);
				addInfo.add(info);
				if (dropableItem.isShow) {
					long broadcastId = addNewBroadcastInfo(info);
					data.setBroadcastIndex(broadcastId);
				}
				buffer.append(dropableItem.itemCode + "x" + dropableItem.dropCount + ",");
			}
		}

		KGameMessage msg = KGame.newLogicMessage(KGambleProtocol.SM_WISH);
		msg.writeInt(priceList.size());
		for (ItemCountStruct price : priceList) {
			KItemMsgPackCenter.packItem(msg, price.getItemTemplate(), price.itemCount);
			msg.writeUtf8String(GambleTips.getTipsWishPriceInfo(price.getItemTemplate().extItemName, price.itemCount));
		}

		role.sendMsg(msg);
		// 更新许愿数据
		boolean isFullUpdate = false;
		TimeLimieProduceActivity activity = null;
		if (wishType == WISH_TYPE_POOR && isUse10Count) {
			activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.初级扭蛋10连抽打折);
			if (activity != null && activity.isActivityTakeEffectNow() && data.poorWishCount == activity.discountTimes) {
				isFullUpdate = true;
			}
		} else if (wishType == WISH_TYPE_RICH && isUse10Count) {
			activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.高级扭蛋10连抽打折);
			if (activity != null && activity.isActivityTakeEffectNow() && data.richWishCount == activity.discountTimes) {
				isFullUpdate = true;
			}
		}
		if (isFullUpdate) {
			sendWishData(role, addInfo);
		} else {
			sendUpdateWishData(role, addInfo);
		}

		KDialogService.sendNullDialog(role);

		if (!isFree) {
			KDialogService.sendUprisingDialog(role, GambleTips.getTipsWishAddDiceCount(wishCount, addDiceCount));
		} else {
			KDialogService.sendUprisingDialog(role, GambleTips.getTipsFreeWishNotAddDiceCount());
		}

		List<ItemCountStruct> mailPriceList = ItemCountStruct.mergeItemCountStructs(priceList);

		ItemResult_AddItem addResult = KSupportFactory.getItemModuleSupport().addItemsToBag(role, mailPriceList, "processRoleWish()");
		if (!addResult.isSucess) {
			BaseRewardData priceData = new BaseRewardData(Collections.<AttValueStruct> emptyList(), Collections.<KCurrencyCountStruct> emptyList(), mailPriceList, Collections.<Integer> emptyList(),
					Collections.<Integer> emptyList());
			BaseMailContent mailContent = new BaseMailContent(GambleTips.getTipsWishPriceMailTitle(), GambleTips.getTipsWishPriceMailContent(), null, null);
			BaseMailRewardData mailData = new BaseMailRewardData(1, mailContent, priceData);

			mailData.sendReward(role, PresentPointTypeEnum.许愿奖励, true);
			KDialogService.sendUprisingDialog(role, GambleTips.getTipsWishPriceMailContent());
			// System.err.println("￥￥￥ 角色背包满，许愿邮件发奖");
		} else {
			// System.err.println("%%%% 角色背包满，许愿背包发奖");
		}

		KSupportFactory.getMissionSupport().notifyUseFunction(role, KUseFunctionTypeEnum.完成许愿);

		// 角色行为统计
		KActionRecorder.recordAction(role.getId(), KActionType.ACTION_TYPE_WISH, wishCount);

		WISH_LOGGER.info("角色ID={},许愿={},钻石={},掷点次数={},彩池={},获得道具={}", role.getId(), wishCount, point, data.diceTimes.get(), diamondLotteryPool.get(), buffer.toString());

		if (wishType == WISH_TYPE_POOR && isUse10Count) {
			TimeLimieProduceActivity presentActivity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.初级扭蛋10连抽送道具);
			if (presentActivity != null && presentActivity.poorWish10PresentItems.size() > 0 && presentActivity.isActivityTakeEffectNow()) {
				KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), presentActivity.poorWish10PresentItems, presentActivity.mailTitle, presentActivity.mailContent);
			}
		} else if (wishType == WISH_TYPE_RICH && isUse10Count) {
			TimeLimieProduceActivity presentActivity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.高级扭蛋10连抽送道具);
			if (presentActivity != null && presentActivity.richWish10PresentItems.size() > 0 && presentActivity.isActivityTakeEffectNow()) {
				KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), presentActivity.richWish10PresentItems, presentActivity.mailTitle, presentActivity.mailContent);
			}
		}
	}

	/**
	 * 处理角色许愿
	 * 
	 * @param role
	 * @param wishCount
	 */
	public void processRoleGuideWish(KRole role) {
		KGambleRoleExtCACreator.getGambleRoleExtData(role.getId()).checkAndRestWishData();
		KGambleRoleExtData extData = KGambleRoleExtCACreator.getGambleRoleExtData(role.getId());

		KRoleWishData data = extData.getWishData();

		int wishCount = 10;
		byte poolType = KWishItemPool.FREE_NORMAL_POOL;
		int addDiceCount = 10;

		if (KSupportFactory.getItemModuleSupport().checkEmptyVolumeInBag(role.getId()) < 1) {
			KDialogService.sendUprisingDialog(role, LevelTips.getTipsBagCapacityNotEnough());
			return;
		}

		// 增加投骰子次数
		extData.addWishDiceCount(addDiceCount, true, true);

		List<String> addInfo = new ArrayList<String>();
		StringBuffer buffer = new StringBuffer();
		List<ItemCountStruct> priceList = new ArrayList<ItemCountStruct>();
		KDropableItem dropableItem;
		int index = 0;

		for (int i = 0; i < (wishCount - 1); i++) {
			dropableItem = wishPool.getDropableItem(poolType, role.getLevel());
			if (dropableItem != null) {
				priceList.add(new ItemCountStruct(dropableItem.itemCode, dropableItem.dropCount));
				String info = GambleTips.getTipsWishBigPriceInfo(role.getExName(), dropableItem.extItemName, dropableItem.dropCount);
				addInfo.add(info);
				buffer.append(dropableItem.itemCode + "x" + dropableItem.dropCount + ",");
			}
		}
		List<Integer> petIds = new ArrayList<Integer>();
		petIds.add(_guideWishPresentPetTemplate.templateId);

		List<ItemCountStruct> msgPriceList = new ArrayList<ItemCountStruct>();
		msgPriceList.addAll(priceList);
		msgPriceList.add(new ItemCountStruct(_guideWishPresentPetItemTemplate, 1));

		KGameMessage msg = KGame.newLogicMessage(KGambleProtocol.SM_WISH);
		msg.writeInt(msgPriceList.size());
		for (ItemCountStruct price : msgPriceList) {
			KItemMsgPackCenter.packItem(msg, price.getItemTemplate(), price.itemCount);
			msg.writeUtf8String(GambleTips.getTipsWishPriceInfo(price.getItemTemplate().extItemName, price.itemCount));
		}

		role.sendMsg(msg);
		// 更新许愿数据
		sendUpdateWishData(role, addInfo);

		KDialogService.sendNullDialog(role);

		KDialogService.sendUprisingDialog(role, GambleTips.getTipsWishAddDiceCount(wishCount, addDiceCount));

		List<ItemCountStruct> mailPriceList = ItemCountStruct.mergeItemCountStructs(priceList);

		ItemResult_AddItem addResult = KSupportFactory.getItemModuleSupport().addItemsToBag(role, mailPriceList, "processRoleWish()");
		if (!addResult.isSucess) {
			BaseRewardData priceData = new BaseRewardData(Collections.<AttValueStruct> emptyList(), Collections.<KCurrencyCountStruct> emptyList(), mailPriceList, Collections.<Integer> emptyList(),
					Collections.<Integer> emptyList());
			BaseMailContent mailContent = new BaseMailContent(GambleTips.getTipsWishPriceMailTitle(), GambleTips.getTipsWishPriceMailContent(), null, null);
			BaseMailRewardData mailData = new BaseMailRewardData(1, mailContent, priceData);

			mailData.sendReward(role, PresentPointTypeEnum.许愿奖励, true);
			KDialogService.sendUprisingDialog(role, GambleTips.getTipsWishPriceMailContent());
			// System.err.println("￥￥￥ 角色背包满，许愿邮件发奖");
		} else {
			// System.err.println("%%%% 角色背包满，许愿背包发奖");
		}
		KSupportFactory.getPetModuleSupport().createPetToRole(role.getId(), _guideWishPresentPetTemplate.templateId, "许愿引导创建宠物");
		// 记录许愿引导状态
		extData.completeGuideWish();

		KSupportFactory.getMissionSupport().notifyUseFunction(role, KUseFunctionTypeEnum.完成许愿);

		// 角色行为统计
		KActionRecorder.recordAction(role.getId(), KActionType.ACTION_TYPE_WISH, wishCount);

		WISH_LOGGER.info("角色ID={},引导许愿={},掷点次数={},彩池={},获得道具={}", role.getId(), wishCount, data.diceTimes.get(), diamondLotteryPool.get(), buffer.toString());

	}

	public int getPoor10WishUsePoint(int poor10WishCount) {
		TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.初级扭蛋10连抽打折);
		int discount = 100;
		if (activity != null && activity.isActivityTakeEffectNow()) {
			if ((poor10WishCount < activity.discountTimes || activity.discountTimes == 0)) {
				discount = activity.discount;
			}
		}
		return _poorWish10UsePoint * discount / 100;
	}

	public int getRich10WishUsePoint(int rich10WishCount) {
		TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.高级扭蛋10连抽打折);
		int discount = 100;
		if (activity != null && activity.isActivityTakeEffectNow()) {
			if ((rich10WishCount < activity.discountTimes || activity.discountTimes == 0)) {
				discount = activity.discount;
			}
		}
		return _richWish10UsePoint * discount / 100;
	}

	// public void processThrowDice(KRole role, boolean isUseAllCount) {
	// KGambleRoleExtCACreator.getGambleRoleExtData(role.getId())
	// .checkAndRestWishData();
	// KGambleRoleExtData extData = KGambleRoleExtCACreator
	// .getGambleRoleExtData(role.getId());
	//
	// KRoleWishData data = extData.getWishData();
	//
	// int diceTimes = data.diceTimes.get();
	// if (diceTimes <= 0) {
	// KDialogService.sendUprisingDialog(role,
	// GambleTips.getTipsDiceNotEnoughCount());
	// return;
	// }
	// int diceCount = 1;
	// if (isUseAllCount) {
	// diceCount = diceTimes;
	// }
	//
	// Map<Integer, ItemCountStruct> circlePriceMap = new LinkedHashMap<Integer,
	// ItemCountStruct>();
	// Map<Integer, String> circleCountMap = new LinkedHashMap<Integer,
	// String>();
	// List<Integer> diceStepList = new ArrayList<Integer>();
	// List<String> priceInfoList = new ArrayList<String>();
	// List<String> luckyPriceBroadcastList = new ArrayList<String>();
	// List<String> luckyPriceMailList = new ArrayList<String>();
	// Map<Integer, Long> luckyDiamondCountMap = new LinkedHashMap<Integer,
	// Long>();
	// for (int i = 0; i < diceCount; i++) {
	// addAllLuckyGirdCounter();
	// int preGirdIndex = data.nowGirlIndex.get();
	// int diceNum = UtilTool.random(1, 6);
	// int caculateIndex = caculateDiceIndex(preGirdIndex, diceNum);
	//
	// if (luckyGirdMap.containsKey(caculateIndex)) {
	// // System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$");
	// }
	// boolean isLucky = false;
	// int checkIndex = checkAndAddLuckyGirdCounter(caculateIndex, data);
	// if (caculateIndex == checkIndex) {
	// diceStepList.add(checkIndex);
	// long luckyDiamondCount = caculateLuckyDiamond(checkIndex);
	// luckyDiamondCountMap.put(i, luckyDiamondCount);
	// isLucky = true;
	// } else {
	// while (luckyGirdMap.containsKey(caculateIndex)) {
	// // System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	// diceNum = UtilTool.random(1, 6);
	// caculateIndex = caculateDiceIndex(preGirdIndex, diceNum);
	// }
	// diceStepList.add(caculateIndex);
	// }
	// boolean isRunCircle = extData.updateDiceIndex(caculateIndex,
	// isLucky);
	// if (isRunCircle) {
	// int nowCircle = data.circle.get();
	// if (circleRewardMap.containsKey(nowCircle)) {
	// CircleReward circleReward = circleRewardMap.get(nowCircle);
	// circlePriceMap.put(i, new ItemCountStruct(
	// circleReward.item.itemCode,
	// circleReward.item.itemCount));
	// String circleTips = GambleTips
	// .getTipsCirclePriceMailContent(
	// nowCircle,
	// circleReward.item.getItemTemplate().extItemName,
	// (int) circleReward.item.itemCount);
	// circleCountMap.put(i, circleTips);
	// }
	// }
	// }
	//
	// List<ItemCountStruct> dicePriceList = new ArrayList<ItemCountStruct>();
	//
	// for (int i = 0; i < diceStepList.size(); i++) {
	// Integer girdId = diceStepList.get(i);
	// if (girdId < TOTAL_GIRD_COUNT) {
	// GirdReward girdReward = girdRewardList.get(girdId);
	// if (!girdReward.isLuckyPrice) {
	// if (girdReward.isStartGird) {
	// priceInfoList.add(GambleTips.getTipsDicePriceNone());
	// WISH_LOGGER.info(
	// "角色ID={},掷点格子={},掷点次数={},圈数={},彩池={},没有中奖",
	// role.getId(), girdId, data.diceTimes.get(),
	// data.circle.get(), diamondLotteryPool.get());
	// } else {
	// if (girdReward.item != null) {
	// dicePriceList.add(new ItemCountStruct(
	// girdReward.item.itemCode,
	// girdReward.item.itemCount));
	// priceInfoList
	// .add(girdReward.item.getItemTemplate().extItemName
	// + " +" + girdReward.item.itemCount);
	// WISH_LOGGER
	// .info("角色ID={},格子={},掷点次数={},圈数={},彩池={},道具={}",
	// role.getId(),
	// girdId,
	// data.diceTimes.get(),
	// data.circle.get(),
	// diamondLotteryPool.get(),
	// (girdReward.item.itemCode + "x" + girdReward.item.itemCount));
	// } else {
	// priceInfoList
	// .add(GambleTips.getTipsDicePriceNone());
	// WISH_LOGGER
	// .info("角色ID={},掷点格子={},掷点次数={},圈数={},彩池={},没有中奖",
	// role.getId(), girdId,
	// data.diceTimes.get(),
	// data.circle.get(),
	// diamondLotteryPool.get());
	// }
	// }
	// } else {
	// long diamondCount = luckyDiamondCountMap.get(i);
	// if (luckyGirdMap.containsKey(girdId)) {
	// LuckyGirdCounter counter = luckyGirdMap.get(girdId);
	// luckyPriceMailList.add(GambleTips
	// .getTipsDiceLuckyPriceMailContent(
	// counter.poolTakeRate, diamondCount));
	// }
	// String luckyTips = GambleTips
	// .getTipsDiceLuckyPriceWorldBroadcast(
	// role.getExName(), diamondCount);
	// luckyPriceBroadcastList.add(luckyTips);
	// priceInfoList.add(luckyTips);
	// WISH_LOGGER.info("角色ID={},格子={},掷点次数={},圈数={},彩池={},钻石={}",
	// role.getId(), girdId, data.diceTimes.get(),
	// data.circle.get(), diamondLotteryPool.get(),
	// diamondCount);
	// }
	//
	// } else {
	// // System.out.println("########################");
	// }
	// }
	// if (dicePriceList.size() > 0) {
	// dicePriceList = ItemCountStruct
	// .mergeItemCountStructs(dicePriceList);
	// }
	// List<KCurrencyCountStruct> diamondList = new
	// ArrayList<KCurrencyCountStruct>();
	//
	// long luckyDiamondCount = 0;
	// if (luckyDiamondCountMap.size() > 0) {
	// for (long count : luckyDiamondCountMap.values()) {
	// luckyDiamondCount += count;
	// }
	// diamondList.add(new KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND,
	// luckyDiamondCount));
	// }
	// BaseRewardData priceData = new BaseRewardData(
	// Collections.<AttValueStruct> emptyList(), diamondList,
	// dicePriceList, Collections.<Integer> emptyList(),
	// Collections.<Integer> emptyList());
	//
	// // 发送掷点路径信息
	// KGameMessage msg = KGame.newLogicMessage(KGambleProtocol.SM_THROW_DICE);
	// msg.writeInt(diceStepList.size());
	// for (int i = 0; i < diceStepList.size(); i++) {
	// int girdIndex = diceStepList.get(i);
	// String priceInfo = priceInfoList.get(i);
	// msg.writeInt(girdIndex);
	// msg.writeUtf8String(priceInfo);
	// if (circlePriceMap.containsKey(i) && circleCountMap.containsKey(i)) {
	// msg.writeBoolean(true);
	// String circleTips = circleCountMap.get(i);
	// msg.writeUtf8String(circleTips);
	// } else {
	// msg.writeBoolean(false);
	// }
	// }
	// role.sendMsg(msg);
	// // 添加道具进背包，若添加失败则发送附件邮件
	// if (dicePriceList.size() > 0) {
	// ItemResult_AddItem addResult = KSupportFactory
	// .getItemModuleSupport().addItemsToBag(role, dicePriceList,
	// "processThrowDice()");
	// if (!addResult.isSucess) {
	// BaseMailContent mailContent = new BaseMailContent(
	// GambleTips.getTipsDicePriceMailTitle(),
	// GambleTips.getTipsDicePriceMailContent(
	// diceStepList.size(),
	// luckyDiamondCountMap.size(),
	// (int) luckyDiamondCount), null, null);
	// BaseMailRewardData mailData = new BaseMailRewardData(1,
	// mailContent, priceData);
	// mailData.sendReward(role, PresentPointTypeEnum.欢乐送普通奖励, true);
	// KDialogService.sendUprisingDialog(role,
	// GambleTips.getTipsDicePriceBagFull());
	// } else {
	// if (diamondList.size() > 0) {
	// KSupportFactory.getCurrencySupport().increaseMoneys(
	// role.getId(), diamondList,
	// PresentPointTypeEnum.欢乐送幸运大奖, true);
	// }
	// // 发送获奖浮动tips
	// // KDialogService.sendDataUprisingDialog(role,
	// // priceData.dataUprisingTips);
	// }
	// } else if (diamondList.size() > 0) {
	// KSupportFactory.getCurrencySupport().increaseMoneys(role.getId(),
	// diamondList, PresentPointTypeEnum.欢乐送幸运大奖, true);
	// }
	//
	// if (circlePriceMap.size() > 0 && circleCountMap.size() > 0) {
	//
	// for (Integer key : circlePriceMap.keySet()) {
	// ItemCountStruct itemStruct = circlePriceMap.get(key);
	// ItemResult_AddItem addCircleResult = KSupportFactory
	// .getItemModuleSupport().addItemToBag(role, itemStruct,
	// this.getClass().getSimpleName());
	// if (!addCircleResult.isSucess) {
	// String info = circleCountMap.get(key);
	// KSupportFactory.getMailModuleSupport().sendAttMailBySystem(
	// role.getId(), itemStruct,
	// GambleTips.getTipsDicePriceMailTitle(), info);
	// }
	// }
	// }
	// KDialogService.sendNullDialog(role);
	//
	// // 更新掷点信息
	// sendUpdateDiceData(role);
	//
	// // 更新许愿信息
	// sendUpdateWishData(role, null);
	//
	// // 世界播报
	// if (luckyPriceBroadcastList.size() > 0) {
	// for (String tips : luckyPriceBroadcastList) {
	// KSupportFactory.getChatSupport().sendSystemChat(tips, true,
	// false);
	// }
	//
	// }
	//
	// // 大奖发系统邮件通知
	// if (luckyPriceMailList.size() > 0) {
	// for (String tips : luckyPriceMailList) {
	// KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(
	// role.getId(), GambleTips.getTipsDicePriceMailTitle(),
	// tips);
	// }
	// }
	//
	// }

	public void processThrowDice(KRole role, boolean isUseAllCount) {
		KGambleRoleExtCACreator.getGambleRoleExtData(role.getId()).checkAndRestWishData();
		KGambleRoleExtData extData = KGambleRoleExtCACreator.getGambleRoleExtData(role.getId());

		KRoleWishData data = extData.getWishData();

		int diceTimes = data.diceTimes.get();
		if (diceTimes <= 0) {
			KDialogService.sendUprisingDialog(role, GambleTips.getTipsDiceNotEnoughCount());
			return;
		}
		int diceCount = 1;
		if (isUseAllCount) {
			diceCount = diceTimes;
		}

		Map<Integer, ItemCountStruct> circlePriceMap = new LinkedHashMap<Integer, ItemCountStruct>();
		Map<Integer, String> circleCountMap = new LinkedHashMap<Integer, String>();
		List<Integer> diceStepList = new ArrayList<Integer>();
		List<String> priceInfoList = new ArrayList<String>();
		List<String> luckyPriceBroadcastList = new ArrayList<String>();
		List<String> luckyPriceMailList = new ArrayList<String>();
		Map<Integer, Long> luckyDiamondCountMap = new LinkedHashMap<Integer, Long>();
		for (int i = 0; i < diceCount; i++) {
			addAllLuckyGirdCounter();
			int preGirdIndex = data.nowGirlIndex.get();
			int caculateIndex = randomNextDiceIndex(preGirdIndex);

			if (luckyGirdMap.containsKey(caculateIndex)) {
				// System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$");
			}
			boolean isLucky = false;
			int checkIndex = checkAndAddLuckyGirdCounter(caculateIndex, data);
			if (caculateIndex == checkIndex) {
				diceStepList.add(checkIndex);
				long luckyDiamondCount = caculateLuckyDiamond(checkIndex);
				luckyDiamondCountMap.put(i, luckyDiamondCount);
				isLucky = true;
			} else {
				while (luckyGirdMap.containsKey(caculateIndex)) {
					// System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");

					caculateIndex = randomNextDiceIndex(preGirdIndex);
				}
				diceStepList.add(caculateIndex);
			}
			boolean isRunCircle = extData.updateDiceIndex(caculateIndex, isLucky);
			if (isRunCircle) {
				int nowCircle = data.circle.get();
				if (circleRewardMap.containsKey(nowCircle)) {
					CircleReward circleReward = circleRewardMap.get(nowCircle);
					circlePriceMap.put(i, new ItemCountStruct(circleReward.item.itemCode, circleReward.item.itemCount));
					String circleTips = GambleTips.getTipsCirclePriceMailContent(nowCircle, circleReward.item.getItemTemplate().extItemName, (int) circleReward.item.itemCount);
					circleCountMap.put(i, circleTips);
				}
			}
		}

		List<ItemCountStruct> dicePriceList = new ArrayList<ItemCountStruct>();

		for (int i = 0; i < diceStepList.size(); i++) {
			Integer girdId = diceStepList.get(i);
			if (girdId < TOTAL_GIRD_COUNT) {
				GirdReward girdReward = girdRewardList.get(girdId);
				if (!girdReward.isLuckyPrice) {
					if (girdReward.isStartGird) {
						priceInfoList.add(GambleTips.getTipsDicePriceNone());
						WISH_LOGGER.info("角色ID={},掷点格子={},掷点次数={},圈数={},彩池={},没有中奖", role.getId(), girdId, data.diceTimes.get(), data.circle.get(), diamondLotteryPool.get());
					} else {
						if (girdReward.item != null) {
							dicePriceList.add(new ItemCountStruct(girdReward.item.itemCode, girdReward.item.itemCount));
							priceInfoList.add(girdReward.item.getItemTemplate().extItemName + " +" + girdReward.item.itemCount);
							WISH_LOGGER.info("角色ID={},格子={},掷点次数={},圈数={},彩池={},道具={}", role.getId(), girdId, data.diceTimes.get(), data.circle.get(), diamondLotteryPool.get(),
									(girdReward.item.itemCode + "x" + girdReward.item.itemCount));
						} else {
							priceInfoList.add(GambleTips.getTipsDicePriceNone());
							WISH_LOGGER.info("角色ID={},掷点格子={},掷点次数={},圈数={},彩池={},没有中奖", role.getId(), girdId, data.diceTimes.get(), data.circle.get(), diamondLotteryPool.get());
						}
					}
				} else {
					long diamondCount = luckyDiamondCountMap.get(i);
					if (luckyGirdMap.containsKey(girdId)) {
						LuckyGirdCounter counter = luckyGirdMap.get(girdId);
						luckyPriceMailList.add(GambleTips.getTipsDiceLuckyPriceMailContent(counter.poolTakeRate, diamondCount));
					}
					String luckyTips = GambleTips.getTipsDiceLuckyPriceWorldBroadcast(role.getExName(), diamondCount);
					luckyPriceBroadcastList.add(luckyTips);
					priceInfoList.add(luckyTips);
					WISH_LOGGER.info("角色ID={},格子={},掷点次数={},圈数={},彩池={},钻石={}", role.getId(), girdId, data.diceTimes.get(), data.circle.get(), diamondLotteryPool.get(), diamondCount);
				}

			} else {
				// System.out.println("########################");
			}
		}
		if (dicePriceList.size() > 0) {
			dicePriceList = ItemCountStruct.mergeItemCountStructs(dicePriceList);
		}
		List<KCurrencyCountStruct> diamondList = new ArrayList<KCurrencyCountStruct>();

		long luckyDiamondCount = 0;
		if (luckyDiamondCountMap.size() > 0) {
			for (long count : luckyDiamondCountMap.values()) {
				luckyDiamondCount += count;
			}
			diamondList.add(new KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND, luckyDiamondCount));
		}
		BaseRewardData priceData = new BaseRewardData(Collections.<AttValueStruct> emptyList(), diamondList, dicePriceList, Collections.<Integer> emptyList(), Collections.<Integer> emptyList());

		// 发送掷点路径信息
		KGameMessage msg = KGame.newLogicMessage(KGambleProtocol.SM_THROW_DICE);
		msg.writeInt(diceStepList.size());
		for (int i = 0; i < diceStepList.size(); i++) {
			int girdIndex = diceStepList.get(i);
			String priceInfo = priceInfoList.get(i);
			msg.writeInt(girdIndex);
			msg.writeUtf8String(priceInfo);
			if (circlePriceMap.containsKey(i) && circleCountMap.containsKey(i)) {
				msg.writeBoolean(true);
				String circleTips = circleCountMap.get(i);
				msg.writeUtf8String(circleTips);
			} else {
				msg.writeBoolean(false);
			}
		}
		role.sendMsg(msg);
		// 添加道具进背包，若添加失败则发送附件邮件
		if (dicePriceList.size() > 0) {
			ItemResult_AddItem addResult = KSupportFactory.getItemModuleSupport().addItemsToBag(role, dicePriceList, "processThrowDice()");
			if (!addResult.isSucess) {
				BaseMailContent mailContent = new BaseMailContent(GambleTips.getTipsDicePriceMailTitle(), GambleTips.getTipsDicePriceMailContent(diceStepList.size(), luckyDiamondCountMap.size(),
						(int) luckyDiamondCount), null, null);
				BaseMailRewardData mailData = new BaseMailRewardData(1, mailContent, priceData);
				mailData.sendReward(role, PresentPointTypeEnum.欢乐送普通奖励, true);
				KDialogService.sendUprisingDialog(role, GambleTips.getTipsDicePriceBagFull());
			} else {
				if (diamondList.size() > 0) {
					KSupportFactory.getCurrencySupport().increaseMoneys(role.getId(), diamondList, PresentPointTypeEnum.欢乐送幸运大奖, true);
				}
				// 发送获奖浮动tips
				// KDialogService.sendDataUprisingDialog(role,
				// priceData.dataUprisingTips);
			}
		} else if (diamondList.size() > 0) {
			KSupportFactory.getCurrencySupport().increaseMoneys(role.getId(), diamondList, PresentPointTypeEnum.欢乐送幸运大奖, true);
		}

		if (circlePriceMap.size() > 0 && circleCountMap.size() > 0) {

			for (Integer key : circlePriceMap.keySet()) {
				ItemCountStruct itemStruct = circlePriceMap.get(key);
				ItemResult_AddItem addCircleResult = KSupportFactory.getItemModuleSupport().addItemToBag(role, itemStruct, this.getClass().getSimpleName());
				if (!addCircleResult.isSucess) {
					String info = circleCountMap.get(key);
					KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), itemStruct, GambleTips.getTipsDicePriceMailTitle(), info);
				}
			}
		}
		KDialogService.sendNullDialog(role);

		// 更新掷点信息
		sendUpdateDiceData(role);

		// 更新许愿信息
		sendUpdateWishData(role, null);

		// 世界播报
		if (luckyPriceBroadcastList.size() > 0) {
			for (String tips : luckyPriceBroadcastList) {
				KSupportFactory.getChatSupport().sendSystemChat(tips, true, false);
			}

		}

		// 大奖发系统邮件通知
		if (luckyPriceMailList.size() > 0) {
			for (String tips : luckyPriceMailList) {
				KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(role.getId(), GambleTips.getTipsDicePriceMailTitle(), tips);
			}
		}

	}

	public boolean processThrowDiceDubugTest(KRole role, boolean isUseAllCount, int dTime) {
		boolean isLuckyPrice = false;
		KGambleRoleExtData extData = KGambleRoleExtCACreator.getGambleRoleExtData(role.getId());

		KRoleWishData data = extData.getWishData();

		int diceTimes = data.diceTimes.get();
		if (diceTimes <= 0) {
			KDialogService.sendUprisingDialog(role, GambleTips.getTipsDiceNotEnoughCount());
			return false;
		}
		int diceCount = 1;
		if (isUseAllCount) {
			diceCount = diceTimes;
		}

		Map<Integer, ItemCountStruct> circlePriceMap = new LinkedHashMap<Integer, ItemCountStruct>();
		Map<Integer, String> circleCountMap = new LinkedHashMap<Integer, String>();
		List<Integer> diceStepList = new ArrayList<Integer>();
		List<Integer> diceRunStepList = new ArrayList<Integer>();
		List<String> priceInfoList = new ArrayList<String>();
		List<String> luckyPriceBroadcastList = new ArrayList<String>();
		Map<Integer, Long> luckyDiamondCountMap = new LinkedHashMap<Integer, Long>();
		for (int i = 0; i < diceCount; i++) {
			addAllLuckyGirdCounter();
			int preGirdIndex = data.nowGirlIndex.get();
			// int diceNum = UtilTool.random(1, 6);
			// int caculateIndex = caculateDiceIndex(preGirdIndex, diceNum);
			int caculateIndex = randomNextDiceIndex(preGirdIndex);

			if (luckyGirdMap.containsKey(caculateIndex)) {
				// System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$");
			}
			int checkIndex = checkAndAddLuckyGirdCounterDebug(role.getId(), caculateIndex, data);
			boolean isLucky = false;
			if (caculateIndex == checkIndex) {
				diceStepList.add(checkIndex);
				diceRunStepList.add(data.runDiceCount);
				long luckyDiamondCount = caculateLuckyDiamond(checkIndex);
				luckyDiamondCountMap.put(i, luckyDiamondCount);
				isLucky = true;
				isLuckyPrice = true;
			} else {
				while (luckyGirdMap.containsKey(caculateIndex)) {
					// System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
					// diceNum = UtilTool.random(1, 6);
					// caculateIndex = caculateDiceIndex(preGirdIndex, diceNum);
					caculateIndex = randomNextDiceIndex(preGirdIndex);
				}
				diceStepList.add(caculateIndex);
				diceRunStepList.add(data.runDiceCount);
			}
			boolean isRunCircle = extData.updateDiceIndex(caculateIndex, isLucky);
			if (isRunCircle) {
				int nowCircle = data.circle.get();
				if (circleRewardMap.containsKey(nowCircle)) {
					CircleReward circleReward = circleRewardMap.get(nowCircle);
					circlePriceMap.put(i, new ItemCountStruct(circleReward.item.itemCode, circleReward.item.itemCount));
					String circleTips = GambleTips.getTipsCirclePriceMailContent(nowCircle, circleReward.item.getItemTemplate().extItemName, (int) circleReward.item.itemCount);
					circleCountMap.put(i, circleTips);
				}
			}
		}

		List<ItemCountStruct> dicePriceList = new ArrayList<ItemCountStruct>();

		for (int i = 0; i < diceStepList.size(); i++) {
			Integer girdId = diceStepList.get(i);
			if (girdId < TOTAL_GIRD_COUNT) {
				GirdReward girdReward = girdRewardList.get(girdId);
				if (!girdReward.isLuckyPrice) {
					if (girdReward.isStartGird) {
						priceInfoList.add(GambleTips.getTipsDicePriceNone());
						WISH_LOGGER.info("角色ID={},格子={},次数={},当前行走次数={},彩池={},20%={},40%={},80%={},没有中奖", role.getId(), girdId, dTime, diceRunStepList.get(i), diamondLotteryPool.get(),
								luckyGirdMap.get(9).counter.get(), luckyGirdMap.get(13).counter.get(), luckyGirdMap.get(22).counter.get());
					} else {
						if (girdReward.item != null) {
							dicePriceList.add(new ItemCountStruct(girdReward.item.itemCode, girdReward.item.itemCount));
							priceInfoList.add(girdReward.item.getItemTemplate().extItemName + " +" + girdReward.item.itemCount);
							WISH_LOGGER.info("角色ID={},格子={},次数={},当前行走次数={},彩池={},20%={},40%={},80%={},道具={}", role.getId(), girdId, dTime, diceRunStepList.get(i), diamondLotteryPool.get(),
									luckyGirdMap.get(9).counter.get(), luckyGirdMap.get(13).counter.get(), luckyGirdMap.get(22).counter.get(), girdReward.item.itemCode);
						} else {
							priceInfoList.add(GambleTips.getTipsDicePriceNone());
							WISH_LOGGER.info("角色ID={},格子={},次数={},当前行走次数={},彩池={},20%={},40%={},80%={},没有中奖", role.getId(), girdId, dTime, diceRunStepList.get(i), diamondLotteryPool.get(),
									luckyGirdMap.get(9).counter.get(), luckyGirdMap.get(13).counter.get(), luckyGirdMap.get(22).counter.get());
						}
					}
				} else {
					long diamondCount = luckyDiamondCountMap.get(i);
					String luckyTips = GambleTips.getTipsDiceLuckyPriceWorldBroadcast(role.getExName(), diamondCount);
					luckyPriceBroadcastList.add(luckyTips);
					priceInfoList.add(luckyTips);
					WISH_LOGGER.info("角色ID={},格子={},次数={},当前行走次数={},彩池={},20%={},40%={},80%={},钻石={}", role.getId(), girdId, dTime, diceRunStepList.get(i), diamondLotteryPool.get(),
							luckyGirdMap.get(9).counter.get(), luckyGirdMap.get(13).counter.get(), luckyGirdMap.get(22).counter.get(), diamondCount);
				}

			} else {
				// System.out.println("########################");
			}
		}

		return isLuckyPrice;
	}

	private int randomNextDiceIndex(int currentIndex) {
		GirdReward nowGird = girdRewardList.get(currentIndex);
		int randomWeight = UtilTool.random(nowGird.next6GirdTotalWeight);
		int targetIndex = caculateDiceIndex(currentIndex, 1);
		int tempRate = 0;
		for (Integer nextGirdIndex : nowGird.next6GirdWeightMap.keySet()) {
			int nextGirdWeight = nowGird.next6GirdWeightMap.get(nextGirdIndex);
			if (tempRate < randomWeight && randomWeight <= (tempRate + nextGirdWeight)) {
				targetIndex = nextGirdIndex;
				break;
			} else {
				tempRate += nextGirdWeight;
			}
		}
		return targetIndex;
	}

	private int caculateDiceIndex(int currentIndex, int diceNum) {
		int nowIndex = currentIndex + diceNum;
		if (nowIndex >= TOTAL_GIRD_COUNT) {
			nowIndex = nowIndex % TOTAL_GIRD_COUNT;
		}
		return nowIndex;
	}

	private int checkAndAddLuckyGirdCounter(int checkIndex, KRoleWishData data) {
		int hitIndex = -1;

		for (Integer girdId : luckyGirdMap.keySet()) {
			boolean isEqualsIndex = (girdId == checkIndex);
			if (isEqualsIndex) {
				LuckyGirdCounter counter = luckyGirdMap.get(girdId);
				if (data.runDiceCount + 1 < counter.roleRunStepCountLimit) {
					return hitIndex;
				}
				int luckyPriceRate;
				if (KWishSystemManager.luckyPriceRateMap.containsKey(data.luckyPricePassDay)) {
					luckyPriceRate = KWishSystemManager.luckyPriceRateMap.get(data.luckyPricePassDay);
				} else {
					luckyPriceRate = KWishSystemManager.luckyPriceRateMap.get(MIN_LUCKY_PRICE_PASS_DAY);
				}
				int rate = UtilTool.random(UtilTool.TEN_THOUSAND_RATIO_UNIT);
				if (rate >= luckyPriceRate) {
					return hitIndex;
				}
				boolean isHit = counter.addAndCheckCounter(isEqualsIndex);
				if (isHit && hitIndex == -1) {
					hitIndex = checkIndex;
				}
				break;
			}
		}
		return hitIndex;
	}

	private int checkAndAddLuckyGirdCounterDebug(long roleId, int checkIndex, KRoleWishData data) {
		int hitIndex = -1;
		for (Integer girdId : luckyGirdMap.keySet()) {
			boolean isEqualsIndex = (girdId == checkIndex);
			if (isEqualsIndex) {
				LuckyGirdCounter counter = luckyGirdMap.get(girdId);
				if (data.runDiceCount + 1 < counter.roleRunStepCountLimit) {
					WISH_LOGGER.info("角色={},行走次数={},格子={}，中奖彩池百分比={},行走次数未达到目标值={}，不能获取奖励", roleId, (data.runDiceCount + 1), checkIndex, (counter.poolTakeRate + "%"), counter.roleRunStepCountLimit);
					return hitIndex;
				}
				int luckyPriceRate;
				if (KWishSystemManager.luckyPriceRateMap.containsKey(data.luckyPricePassDay)) {
					luckyPriceRate = KWishSystemManager.luckyPriceRateMap.get(data.luckyPricePassDay);
				} else {
					luckyPriceRate = KWishSystemManager.luckyPriceRateMap.get(MIN_LUCKY_PRICE_PASS_DAY);
				}
				int rate = UtilTool.random(UtilTool.TEN_THOUSAND_RATIO_UNIT);
				if (rate >= luckyPriceRate) {
					WISH_LOGGER.info("角色={},行走次数={},格子={}，中奖彩池百分比={},当前个人中奖概率={}，随机出来概率={}，没有命中中奖概率不能获取奖励", roleId, (data.runDiceCount + 1), checkIndex, (counter.poolTakeRate + "%"), luckyPriceRate,
							rate);
					return hitIndex;
				}
				boolean isHit = counter.addAndCheckCounterDebug(roleId, isEqualsIndex, data);
				if (isHit && hitIndex == -1) {
					hitIndex = checkIndex;
				}
				break;
			}
		}
		return hitIndex;
	}

	public void addAllLuckyGirdCounter() {
		for (LuckyGirdCounter counter : luckyGirdMap.values()) {
			counter.counter.incrementAndGet();
		}
	}

	private long caculateLuckyDiamond(int checkIndex) {
		long luckyDiamondCount = 0;
		if (luckyGirdMap.containsKey(checkIndex)) {
			LuckyGirdCounter counter = luckyGirdMap.get(checkIndex);
			synchronized (diamondLotteryPool) {
				long nowdiamondLotteryPoolCount = diamondLotteryPool.get();
				luckyDiamondCount = (nowdiamondLotteryPoolCount * counter.poolTakeRate) / LuckyGirdCounter.TAKE_RATE_MOD;
				diamondLotteryPool.addAndGet(-luckyDiamondCount);
			}
		}
		return luckyDiamondCount;
	}

	public List<WishBroadcastInfo> getWishBroadcastInfo(KRoleWishData data) {
		List<WishBroadcastInfo> list = new ArrayList<WishBroadcastInfo>();
		if (broadcastMap.size() > 0) {
			if (data.broadcastIndex < nowMaxId) {
				long getCount = nowMaxId - data.broadcastIndex;
				for (long i = nowMaxId, j = 0; i > 0 && j < getCount && j < MAX_BROADCAST_COUNT; i--, j++) {
					WishBroadcastInfo info = broadcastMap.getQuietly(i);
					if (info != null) {
						list.add(info);
					}
				}
				data.broadcastIndex = nowMaxId;
			}
		}
		return list;
	}

	public long addNewBroadcastInfo(String infoStr) {
		long id = broadcastInfoIdGen.incrementAndGet();
		nowMaxId = id;
		WishBroadcastInfo info = new WishBroadcastInfo(id, infoStr);
		this.broadcastMap.put(id, info);
		return id;
	}

	public List<WishShowInfoData> getWishShowInfoDataList(byte poolType, int roleLv) {
		Map<Integer, List<WishShowInfoData>> map;
		if (poolType == WISH_TYPE_POOR) {
			map = poorShowWishPriceMap;
		} else {
			map = richShowWishPriceMap;
		}
		for (Integer lv : map.keySet()) {
			if (roleLv <= lv) {
				return map.get(lv);
			}
		}
		if (poolType == WISH_TYPE_POOR) {
			return defaultPoorWishShowDataList;
		} else {
			return defaultRichWishShowDataList;
		}

	}

	public static class CircleReward {
		public int circleCount;
		public ItemCountStruct item;

		public CircleReward(int circleCount, ItemCountStruct item) {
			super();
			this.circleCount = circleCount;
			this.item = item;
		}
	}

	public static class GirdReward {
		public int girdCount;
		public boolean isLuckyPrice;
		public boolean isStartGird = false;
		public ItemCountStruct item;
		public int weight;// 权重值

		public Map<Integer, Integer> next6GirdWeightMap = new LinkedHashMap<Integer, Integer>();
		public int next6GirdTotalWeight = 0;

		public GirdReward() {

		}

		public GirdReward(int girdCount, boolean isLuckyPrice, ItemCountStruct item, int weight) {
			super();
			this.girdCount = girdCount;
			this.isLuckyPrice = isLuckyPrice;
			this.item = item;
			this.weight = weight;
		}
	}

	public static class LuckyGirdCounter {
		public final static int TAKE_RATE_MOD = 100;

		public int girdId;
		public int targetCount;
		public AtomicInteger counter = new AtomicInteger(0);
		public int poolTakeRate;
		public int roleRunStepCountLimit;

		public LuckyGirdCounter(int girdId, int targetCount, int poolTakeRate, int roleRunStepCountLimit) {
			super();
			this.girdId = girdId;
			this.targetCount = targetCount;
			this.poolTakeRate = poolTakeRate;
			this.roleRunStepCountLimit = roleRunStepCountLimit;
		}

		public void initCounter(int value) {
			counter.set(value);
		}

		public boolean addAndCheckCounter(boolean isHit) {
			int nowCount = counter.get();
			if (nowCount >= targetCount && isHit) {
				counter.set(0);
				return true;
			} else {

				return false;
			}
		}

		public boolean addAndCheckCounterDebug(long roleId, boolean isHit, KRoleWishData data) {
			int nowCount = counter.get();
			if (nowCount >= targetCount && isHit) {
				counter.set(0);
				return true;
			} else {
				WISH_LOGGER.info("角色={},行走次数={},格子={}，中奖彩池百分比={},系统间隔次数={},未达到目标值={}，不能获取奖励", roleId, (data.runDiceCount + 1), this.girdId, (poolTakeRate + "%"), nowCount, targetCount);
				return false;
			}
		}
	}

	public static class WishBroadcastInfo {
		public final long infoId;
		public final String info;

		public WishBroadcastInfo(long infoId, String info) {
			super();
			this.infoId = infoId;
			this.info = info;
		}
	}

	public static class WishShowInfoData {
		public int index;
		public byte type;
		public int maxRoleLv;
		public KItemTempAbs itemTemplate;

		public WishShowInfoData(int index, byte type, int maxRoleLv, KItemTempAbs itemTemplate) {
			super();
			this.index = index;
			this.type = type;
			this.maxRoleLv = maxRoleLv;
			this.itemTemplate = itemTemplate;
		}

	}

}
