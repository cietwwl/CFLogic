package com.kola.kmp.logic.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jxl.read.biff.BiffException;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.XmlUtil;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.map.AutoSearchRoadTrack.RoadPath;
import com.kola.kmp.logic.map.KGameMapEntity.PetMapEntityShowData;
import com.kola.kmp.logic.map.KGameMapEntity.RoleMapEntityShowData;
import com.kola.kmp.logic.map.KNormalMapActivityManager.KNormalMapActivityData;
import com.kola.kmp.logic.map.KRoleGameSettingDataImpl.MobileInfoShowMapConfig;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMapManager;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KMapTypeEnum;
import com.kola.kmp.logic.other.KMapEntityTypeEnum;
import com.kola.kmp.logic.pet.KPet;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.map.KMapProtocol;

public class KGameMapManager {
	private static final KGameLogger _LOGGER = KGameLogger.getLogger(KGameMapManager.class);
	/**
	 * 角色周围其他角色状态改变消息打包的最大长度
	 */
	public static int packetEntityStateChangeMsgMaxSize;

	/**
	 * 广播角色周围其他角色状态改变消息时间间隔（单位：秒）
	 */
	public static int broadcastEntityStateChangeMsgPackTimeSeconds;

	/**
	 * 广播角色周围其他角色状态改变消息时间间隔（单位：毫秒）
	 */
	public static int broadcastEntityStateChangeMsgPackTimeMillis;

	/**
	 * 每次获取周围玩家数量
	 */
	public static final int GET_AROUND_ROLE_PER_COUNT = 16;
	
	public static String mapDataExcelFilePath;

	// 普通地图集合
	// private final ReadWriteLock lockGameMaps = new ReentrantReadWriteLock();
	private final ConcurrentHashMap<Integer, KGameNormalMap> allGameMaps = new ConcurrentHashMap<Integer, KGameNormalMap>();
	private final ConcurrentHashMap<Integer, KGameMapArea> allAreas = new ConcurrentHashMap<Integer, KGameMapArea>();
	// NPC ID与地图的Mapping
	private final ConcurrentHashMap<Integer, KGameNormalMap> npcMappingGameMap = new ConcurrentHashMap<Integer, KGameNormalMap>();
	// 场景ID与地图的Mapping
	private final ConcurrentHashMap<Integer, KGameNormalMap> scenarioMappingGameMap = new ConcurrentHashMap<Integer, KGameNormalMap>();

	// public KGameNormalMap newGuideMap;

	public KGameNormalMap firstMap;

	private int sizeGameMaps = 0;

	// private static MapEventIdGenerator idGenerator = new MapEventIdGenerator(
	// 1000);
	//
	private List<PlayerRoleJoinLeaveMapEventListener> joinLeaveMapEventListenerList = new ArrayList<PlayerRoleJoinLeaveMapEventListener>();

	// 周围角色限制数量:
	private static int unitLimit;
	// 周围角色限制规则是否并发:
	static boolean unitLimitIsConcurrent;
	// 周围角色限制规则是否按照优先级处理:
	static boolean unitLimitIsPriority;

	private boolean inited = false;

	// 区域聊天广播监听器
	// private IChatChannelBroadcast mapIChatChannelBroadcast;

	/**
	 * 地图显示角色数量级别的Map，Key：级别，Value：显示人数
	 */
	public static HashMap<Byte, Integer> showPlayerLevelMap = new LinkedHashMap<Byte, Integer>();
	public static int default_show_size;

	public static String mapNpcXmlFilePath = "./res/gsconfig/mapModule/mapXmlData/";

	public static boolean isBlockOnlinePVPDefaultValue;
	public static boolean isBlockWorldChatDefaultValue;
	public static boolean isBlockMapChatDefaultValue;
	public static boolean isBlockFamilyChatDefaultValue;
	public static byte mapShowPlayerLevelDefaultValue;
	public static byte mapShowPlayerLevelMinValue = 1;
	public static boolean isShowForum;
	
	public static KNormalMapActivityManager normalMapActivityManager;

	public void init(String configPath) throws Exception {
		try {
			_LOGGER.info("！！！地图模块加载开始！！！");
			Document doc = XmlUtil.openXml(configPath);
			if (doc != null) {
				Element root = doc.getRootElement();
				mapDataExcelFilePath = root.getChildText("mapDataExcelFilePath");

				this.unitLimit = Integer.parseInt(root.getChildText("mapUnitLimit"));
				default_show_size = unitLimit;
				this.unitLimitIsConcurrent = (root.getChildText("unitLimitIsConcurrent")).equals("true");
				this.unitLimitIsPriority = (root.getChildText("unitLimitIsPriority")).equals("true");
				this.packetEntityStateChangeMsgMaxSize = Integer.parseInt(root.getChildText("packetEntityStateChangeMsgMaxSize"));
				this.broadcastEntityStateChangeMsgPackTimeSeconds = Integer.parseInt(root.getChildText("broadcastEntityStateChangeMsgPackTimeSeconds"));
				this.broadcastEntityStateChangeMsgPackTimeMillis = broadcastEntityStateChangeMsgPackTimeSeconds * 1000;

				this.mapNpcXmlFilePath = root.getChildText("mapNpcXmlFilePath");

				// List<Element> showLevelEList =
				// root.getChild("showPlayerInfo").getChildren("level");
				// for (Element infoE : showLevelEList) {
				// byte showLevel =
				// Byte.parseByte(infoE.getAttributeValue("lv"));
				// int showSize =
				// Integer.parseInt(infoE.getAttributeValue("showCount"));
				// showPlayerLevelMap.put(showLevel, showSize);
				// }

				// 读取角色客户端系统设置功能的默认配置
				isBlockOnlinePVPDefaultValue = root.getChild("gameSetting").getChildText("isBlockOnlinePVP").equals("true");
				isBlockWorldChatDefaultValue = root.getChild("gameSetting").getChildText("isBlockWorldChat").equals("true");
				isBlockMapChatDefaultValue = root.getChild("gameSetting").getChildText("isBlockMapChat").equals("true");
				isBlockFamilyChatDefaultValue = root.getChild("gameSetting").getChildText("isBlockFamilyChat").equals("true");
				mapShowPlayerLevelDefaultValue = Byte.parseByte(root.getChild("gameSetting").getChildText("mapShowPlayerLevel"));

				// 读取地图excel表头，初始化地图数据以及加载相关资源
				loadMapDataExcelFile(mapDataExcelFilePath);

				// 初始化地图聊天广播管理，并注册
				// this.mapIChatChannelBroadcast = new
				// KMapIChatChannelBroadcastImpl();
				// KChatModule.bindingChatBroadcast(mapIChatChannelBroadcast);

				// 初始化副本地图
				KDuplicateMapManager.getInstace().init(mapDataExcelFilePath);
				// 读取活动地图
				reloadNormalMapActivityData();
			} else {
				throw new NullPointerException("地图模块配置不存在！！");
			}
		} catch (Exception e) {
			throw new KGameServerException("读取地图excel表头发生错误！", e);
		}
		// testSearchRoad();

	}

	public void serverStartCompleted() {
		KGame.newTimeSignal(new BroadcastMapEntityStateTimerTask(), broadcastEntityStateChangeMsgPackTimeSeconds, TimeUnit.SECONDS);
	}
	
	public static void reloadNormalMapActivityData() throws Exception {
		KNormalMapActivityManager manager = new KNormalMapActivityManager();
		KGameExcelFile file = new KGameExcelFile(mapDataExcelFilePath);
		{
			KGameExcelTable table = file.getTable("活动时间主城地图配置", 2);
			KGameExcelRow[] rows = table.getAllDataRows();
			if(rows!=null&&rows.length>0){
				List<KNormalMapActivityData> list = new ArrayList<KNormalMapActivityData>();
				for (int i = 0; i < rows.length; i++) {
					KNormalMapActivityData data = new KNormalMapActivityData();
					data.mapId = rows[i].getInt("mapId");
					data.map_xml_path = rows[i].getData("map_data_file_name");
					data.musicId = rows[i].getInt("musicId");
					data.startTimeStr = rows[i].getData("startTimeStr");
					data.endTimeStr = rows[i].getData("endTimeStr");
					data.timeLimit = rows[i].getData("timeLimit");
					
					list.add(data);
					data.notifyCacheLoadComplete();
				}
				
				manager.initDatas(list);
				
			}
			
			KGameMapManager.normalMapActivityManager = manager;
		}
	}

	/**
	 * 新建{@link KGameNormalMap}实例的方法
	 * 
	 * @param id
	 * @param type
	 * @param name
	 * @param description
	 * @param data
	 * @return
	 */
	public KGameNormalMap newGameMap(int id, int type, String name, String description, KGameMapData data) {
		return new KGameNormalMap(this, id, type, name, description, data);
	}

	// /**
	// * 创建一个地图足迹记录器。产生后用于绑定到每个{@link GameMapEntity}。
	// * @param maxLength 最大记录长度
	// * @return
	// */
	// public GameMapTrack newGameMapTrack(int maxLength);

	/**
	 * 通过地图ID取得地图实例
	 * 
	 * @param mapId
	 * @return
	 */
	public KGameNormalMap getGameMap(int mapId) {
		// lockGameMaps.readLock().lock();
		// try {
		return allGameMaps.get(mapId);
		// } finally {
		// lockGameMaps.readLock().unlock();
		// }
	}

	/**
	 * 取得某个类型的所有地图实例
	 * 
	 * @param mapType
	 * @return
	 */
	public Map<Integer, KGameNormalMap> getGameMaps(int mapType) {
		Map<Integer, KGameNormalMap> gms = new HashMap<Integer, KGameNormalMap>(getGameMapNum(), 1.0f);
		// lockGameMaps.readLock().lock();
		// try {
		for (KGameNormalMap gameMap : allGameMaps.values()) {
			if (gameMap != null && gameMap.getType() == mapType) {
				gms.put(gameMap.getMapId(), gameMap);
			}
		}
		// } finally {
		// lockGameMaps.readLock().unlock();
		// }
		return gms;
	}

	/**
	 * 取得整个游戏世界的所有地图
	 * 
	 * @return
	 */
	public Map<Integer, KGameNormalMap> getGameMaps() {
		Map<Integer, KGameNormalMap> gms = new HashMap<Integer, KGameNormalMap>(getGameMapNum(), 1.0f);
		// lockGameMaps.readLock().lock();
		// try {
		gms.putAll(allGameMaps);
		// } finally {
		// lockGameMaps.readLock().unlock();
		// }
		return gms;
	}

	/**
	 * 地图总数量
	 * 
	 * @return
	 */
	public int getGameMapNum() {
		// lockGameMaps.readLock().lock();
		// try {
		return sizeGameMaps;
		// } finally {
		// lockGameMaps.readLock().unlock();
		// }
	}

	/**
	 * 某种类型地图的总数量
	 * 
	 * @param mapType
	 * @return
	 */
	public int getGameMapNum(int mapType) {
		int n = 0;
		// lockGameMaps.readLock().lock();
		// try {
		for (KGameNormalMap gameMap : allGameMaps.values()) {

			if (gameMap != null && gameMap.getType() == mapType) {
				n++;
			}
		}
		// } finally {
		// lockGameMaps.readLock().unlock();
		// }
		return n;
	}

	public KGameMapArea getGameMapArea(int areaId) {
		return allAreas.get(areaId);
	}

	// /**
	// * 服务器运行时关闭某个地图
	// * @param mapId
	// * @return
	// * @throws
	// com.yz.gamexp.engine.mmorpg.server.mapsystem.GameMapServiceModuleException
	// */
	// public boolean closeGameMapOnRuntime(long mapId) throws
	// GameMapServiceModuleException;
	//
	// /**
	// * 服务器运行时开放某个地图
	// * @param mapId
	// * @return
	// * @throws
	// com.yz.gamexp.engine.mmorpg.server.mapsystem.GameMapServiceModuleException
	// */
	// public boolean openGameMapOnRuntime(long mapId) throws
	// GameMapServiceModuleException;

	/**
	 * 获取NPC ID与地图的Mapping
	 * 
	 * @return
	 */
	public ConcurrentHashMap<Integer, KGameNormalMap> getNpcMappingGameMap() {
		return npcMappingGameMap;
	}

	/**
	 * 获取场景ID与地图的Mapping
	 * 
	 * @return
	 */
	public ConcurrentHashMap<Integer, KGameNormalMap> getScenarioMappingGameMap() {
		return scenarioMappingGameMap;
	}

	/**
	 * 是否有对每个GameMapEntity（属于角色类型的）可以感知周围GameMapEntity的数量限制。<br>
	 * 如果有限制，则引擎会为每个角色类型的GameMapEntity建立一个{@link PlayerRolePrivateEntityList}实例。
	 * 
	 * @return 限制数量（如果为负数或0则无限制，列表也不会被创建）
	 * @see PlayerRolePrivateEntityList
	 */
	public static int getUnitLimitForGameMapEntity() {
		return unitLimit;
	}

	// /**
	// * 获取地图事件ID生成器
	// *
	// * @return
	// */
	// public static MapEventIdGenerator getMapEventIdGenerator() {
	// return idGenerator;
	// }

	public void registerPlayerRoleJoinLeaveMapEventListener(PlayerRoleJoinLeaveMapEventListener listener) {
		if (listener != null && !this.joinLeaveMapEventListenerList.contains(listener)) {
			this.joinLeaveMapEventListenerList.add(listener);
		}
	}

	public List<PlayerRoleJoinLeaveMapEventListener> getJoinLeaveMapEventListenerList() {
		return joinLeaveMapEventListenerList;
	}

	private void loadMapDataExcelFile(String path) throws Exception {
		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(path);
		} catch (BiffException e) {
			throw new KGameServerException("读取地图excel表头发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取地图excel表头发生错误！", e);
		}

		if (xlsFile != null) {
			// 读取区域（主城）配置工作表
			int areaDataRowIndex = 2;
			KGameExcelTable areaDataTable = xlsFile.getTable("主城配置表", areaDataRowIndex);
			KGameExcelRow[] allAreaRows = areaDataTable.getAllDataRows();
			if (allAreaRows != null) {
				for (int i = 0; i < allAreaRows.length; i++) {
					int areaId = allAreaRows[i].getInt("areaId");
					String areaName = allAreaRows[i].getData("areaName");
					int openRoleLevel = allAreaRows[i].getInt("openRoleLevel");
					int openFrontMissionTemplateId = allAreaRows[i].getInt("frontMissionTemplateId");

					KGameMapArea area = new KGameMapArea();
					area.setAreaId(areaId);
					area.setAreaName(areaName);
					area.setOpenRoleLevel(openRoleLevel);
					area.setOpenFrontMissionTemplateId(openFrontMissionTemplateId);
					this.allAreas.put(areaId, area);
				}
			}

			// 读取地图数据配置工作表
			int mapDataRowIndex = 3;
			KGameExcelTable mapDataTable = null;

			mapDataTable = xlsFile.getTable("地图配置表", mapDataRowIndex);

			if (mapDataTable != null) {
				KGameExcelRow[] allRows = mapDataTable.getAllDataRows();
				if (allRows != null) {
					for (int i = 0; i < allRows.length; i++) {
						// 先加载地图基础数据
						int mapId = allRows[i].getInt("mapId");
						_LOGGER.info("加载地图数据，地图ID" + mapId);
						String mapName = allRows[i].getData("mapName");
						int mapType = allRows[i].getInt("mapType");
						int mapResId = allRows[i].getInt("map_res_id");
						String mapDataFileName = allRows[i].getData("map_data_file_name");
						if (/* mapResId == 0 || */mapDataFileName == null) {
							throw new KGameServerException("读取地图的map_res_id为：" + mapResId + ",map_data_file_name=" + mapDataFileName + "错误，这两个值不能为空或0！！，Row=" + allRows[i].getIndexInFile());
						}
						String mapDesc = allRows[i].getData("description");
						int mapAreaId = allRows[i].getInt("areaId");
						if (!allAreas.containsKey(mapAreaId)) {
							throw new KGameServerException("读取地图的areaId为：" + mapAreaId + "找不到这个主城ID，Row=" + allRows[i].getIndexInFile());
						}
						int bgMusicResId = allRows[i].getInt("musicId");
						int joinMapEfficiencyId = allRows[i].getInt("joinMapEfficiencyId");

						KGameNormalMap map = new KGameNormalMap(this, mapId, mapType, mapName, mapDesc);
						allGameMaps.put(mapId, map);
						map.setAreaId(mapAreaId);
						map.setAreaName(allAreas.get(mapAreaId).getAreaName());
						map.setMapResId(mapResId);
						map.setBgMusicResId(bgMusicResId);
						map.setMapDataFileName(mapDataFileName);
						map.setJoinMapEfficiencyId(joinMapEfficiencyId);

						// 将地图放入区域的Map结构，并判断设置地图区域的第一层
						if (this.allAreas.containsKey(mapAreaId)) {
							KGameMapArea area = this.allAreas.get(mapAreaId);
							area.addMap(map);
							if (allRows[i].getInt("isFirstLevel") == 1) {

								area.setFirstLevelMap(map);
							}
						}

						// 初始化地图具体数据，如出入口、NPC信息等
						KGameMapData data = new KGameMapData();
						map.setGameMapData(data);
						String filePath = this.mapNpcXmlFilePath + mapDataFileName + ".assetbundle";
						data.initMapData(map, filePath, allRows[i], mapDataRowIndex + i + 1);

						if (i == 0) {
							firstMap = map;
						}

						_LOGGER.info("地图加载完毕,地图：" + allGameMaps.get(mapId).getName());

					}
				}
			}

			// 地图显示人数配置
			int dataRowIndex = 2;
			KGameExcelTable dataTable = xlsFile.getTable("地图显示人数配置", dataRowIndex);
			KGameExcelRow[] allRows = dataTable.getAllDataRows();
			if (allRows != null) {
				for (int i = 0; i < allRows.length; i++) {
					byte phoneType = allRows[i].getByte("phoneType");
					int memSize = allRows[i].getInt("memSize");
					String gpuType = allRows[i].getData("gpuType");
					byte showLevel = allRows[i].getByte("showLevel");
					
//					if (phoneType == KRoleGameSettingDataImpl.PHONE_TYPE_IOS) {
//						KRoleGameSettingDataImpl._iosShowPlayerCountConditionMap.put(memSize, showLevel);
//					} else if (phoneType == KRoleGameSettingDataImpl.PHONE_TYPE_ANDROID) {
//						KRoleGameSettingDataImpl._androidShowPlayerCountConditionMap.put(memSize, showLevel);
//					} else {
//						throw new KGameServerException("读取地图excel<地图显示人数配置>表头发生错误，phoneType只能为0或1！");
//					}
					
					if (phoneType == KRoleGameSettingDataImpl.PHONE_TYPE_IOS) {
						if(!KRoleGameSettingDataImpl._iosShowPlayerCountConditionMap1.containsKey(gpuType)){
							KRoleGameSettingDataImpl._iosShowPlayerCountConditionMap1.put(gpuType, new ArrayList<MobileInfoShowMapConfig>());
						}
						KRoleGameSettingDataImpl._iosShowPlayerCountConditionMap1.get(gpuType).add(new MobileInfoShowMapConfig(phoneType, gpuType, memSize, showLevel));
//						KRoleGameSettingDataImpl._iosShowPlayerCountConditionMap.put(memSize, showLevel);
					} else if (phoneType == KRoleGameSettingDataImpl.PHONE_TYPE_ANDROID) {
//						KRoleGameSettingDataImpl._androidShowPlayerCountConditionMap.put(memSize, showLevel);
						if(!KRoleGameSettingDataImpl._androidShowPlayerCountConditionMap1.containsKey(gpuType)){
							KRoleGameSettingDataImpl._androidShowPlayerCountConditionMap1.put(gpuType, new ArrayList<MobileInfoShowMapConfig>());
						}
						KRoleGameSettingDataImpl._androidShowPlayerCountConditionMap1.get(gpuType).add(new MobileInfoShowMapConfig(phoneType, gpuType, memSize, showLevel));
					} else {
						throw new KGameServerException("读取地图excel<地图显示人数配置>表头发生错误，phoneType只能为0或1！");
					}
				}
			}

			// 地图显示人数级别
			dataTable = xlsFile.getTable("地图显示人数级别", dataRowIndex);
			allRows = dataTable.getAllDataRows();
			if (allRows != null) {
				for (int i = 0; i < allRows.length; i++) {
					byte showLevel = allRows[i].getByte("showLevel");
					int showSize = allRows[i].getInt("showSize");
					showPlayerLevelMap.put(showLevel, showSize);
				}
			}
		}
	}

	public boolean processPlayerRoleJumpMap(KRole role, int srcMapID, int exitId) {
		if (role == null) {
			_LOGGER.error("跳转地图时发生错误，角色为null。");
			return false;
		}
		KGameNormalMap srcMap = allGameMaps.get(srcMapID);

		if (srcMap == null) {
			_LOGGER.error("跳转地图时发生错误，源地图数据为null，角色ID：{}，地图ID：{}", +role.getId(), srcMapID);
			return false;
		}
		GameMapExitsEventData srcExitData = srcMap.getGameMapData().getGameMapExitsEventData(exitId);
		// if(srcExitData ==
		// null||srcExitData.getExitType()!=GameMapExitsEventData.EXIT_EVENT_TYPE_JUMP_MAP){
		// _LOGGER.error("跳转地图时发生错误，源地图数据的出口数据为null，角色ID：{}，地图ID：{}，出口ID：{}", +
		// role.getId(),srcMapID,exitId);
		// return false;
		// }
		int targetMapId = srcExitData.targetId;
		KGameNormalMap targetMap = allGameMaps.get(targetMapId);
		// if (targetMap == null) {
		// _LOGGER.error("跳转地图时发生错误，源地图数据为null，角色ID：{}，地图ID：{}", +
		// role.getId(),srcMapID);
		// return false;
		// }
		GameMapExitsEventData targetExitData = targetMap.getGameMapData().getGameMapExitsEventDataByTargetMapId(srcMapID);
		// if(targetExitData ==
		// null||targetExitData.getExitType()!=GameMapExitsEventData.EXIT_EVENT_TYPE_JUMP_MAP){
		// _LOGGER.error("跳转地图时发生错误，源地图数据的出口数据为null，角色ID：{}，地图ID：{}，出口ID：{}", +
		// role.getId(),srcMapID,exitId);
		// return false;
		// }

		KCoordinate targerCoor = targetMap.getGameMapData().getPlayerRoleJumpMapCoordinate(targetExitData.getExitId());

		return playerRoleJumpMap(role, srcMapID, targetMapId, targerCoor.getX(), targerCoor.getY(), true);
	}

	/**
	 * 角色跳转地图
	 * 
	 * @param role
	 *            ，需要跳转地图的角色
	 * @param srcMapID
	 *            ，跳转前所在的地图ID
	 * @param targetMapID
	 *            ，跳转目标地图ID
	 * @param target_x
	 *            ,跳转目标地图出生点X坐标
	 * @param target_y
	 *            ，跳转目标地图出生点Y坐标
	 * @param isNotifyClient
	 * @return
	 */
	public boolean playerRoleJumpMap(KRole role, int srcMapID, int targetMapID, float target_x, float target_y, boolean isNotifyClient) {

		return jumpMap(role, srcMapID, targetMapID, target_x, target_y);

	}

	/**
	 * 角色跳转地图的私有处理方法
	 * 
	 * @param role
	 * @param srcMapID
	 * @param targetMapID
	 * @param target_x
	 * @param target_y
	 * @return
	 */
	private boolean jumpMap(KRole role, int srcMapID, int targetMapID, float target_x, float target_y) {
		if (role == null) {
			_LOGGER.error("跳转地图时发生错误，角色为null。");
			return false;
		}

		KGameNormalMap srcMap = allGameMaps.get(srcMapID);

		if (srcMap == null) {
			_LOGGER.error("跳转地图时发生错误，源地图数据为null，角色ID：" + role.getId() + "。");
			return false;
		}

		KGameNormalMap targetMap = allGameMaps.get(targetMapID);

		if (targetMap == null) {
			_LOGGER.error("跳转地图时发生错误，目标图数据为null，角色ID：" + role.getId() + "。");
			return false;
		}

		// _LOGGER.debug("/*/*/*/*/*/*/ 角色id：" + role.getRoleId() + "跳转地图，源地图："
		// + srcMapID + ",目标地图：" + targetMapID + ",出生坐标(" + target_x + ","
		// + target_y + ")");

		// 发送跳转地图提示
		// sendJumpMapTips(role, targetMap.getName());

		KGameMapEntity playerEntity = null;

		if ((playerEntity = srcMap.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId())) != null) {
			// 处理玩家离开源地图
			playerRoleEntityLeaveMap(srcMap, playerEntity);
		} else {
			_LOGGER.warn("地图系统：jumpMap 过地图的时候，在地图里找不到玩家角色：" + role.getId() + ", 角色名：" + role.getName() + ",地图ID： " + srcMap.getMapId());
			playerEntity = new KGameMapEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId(), role);
		}

		// 通知所有地图监听器角色离开地图成功
		notifyPlayerRoleLeavedMapEvent(role, srcMap);

		// 处理角色进入地图流程，以及广播周围其他角色该玩家角色进入地图
		return playerRoleJoinInMap(role, playerEntity, targetMap, target_x, target_y, srcMap.getMapId());

	}

	public void notifyPlayerRoleLeavedMapEvent(KRole role, KMap currentMap) {
		for (PlayerRoleJoinLeaveMapEventListener listener : joinLeaveMapEventListenerList) {
			listener.notifyPlayerRoleLeavedMap(role, currentMap);
		}
	}

	/**
	 * 角色进入地图处理
	 * 
	 * @param targetMap
	 * @param roleEntity
	 * @param target_x
	 * @param target_y
	 * @param srcMapId
	 * @return
	 */
	private boolean playerRoleJoinInMap(KRole myRole, KGameMapEntity roleEntity, KGameNormalMap targetMap, float target_x, float target_y, int srcMapId) {

		// 将角色放入地图
		try {
			targetMap.putEntityTo(roleEntity, target_x, target_y);
		} catch (KGameMapModuleException e) {
			// TODO 处理将玩家角色放入地图失败的情况，需要通知客户端
			_LOGGER.error("地图系统：jumpMap 过地图的时候，将角色放入地图失败，角色ID：" + myRole.getId() + ", 角色名：" + myRole.getName() + ",地图ID： " + targetMap.getMapId(), e);
			return false;
		}

		KGameNormalMap srcMap = this.getGameMap(srcMapId);

		boolean isJumpArea = false;
		// 设置角色属性的坐标信息
		if (targetMap.mapType == KMapTypeEnum.普通主城地图) {
			myRole.getRoleMapData().setCurrentMapId(targetMap.getMapId());
		}
		if (srcMap != null && srcMap.mapType == KMapTypeEnum.普通主城地图) {
			myRole.getRoleMapData().setLastMapId(srcMapId);
			if (srcMap.getAreaId() != targetMap.getAreaId()) {
				isJumpArea = true;
			}
		} else {
			myRole.getRoleMapData().setLastMapId(firstMap.getMapId());
		}

		myRole.getRoleMapData().setCorX(target_x);
		myRole.getRoleMapData().setCorY(target_y);

		// 发送地图数据给客户端
		sendGameMapDataToClient(roleEntity, targetMap, isJumpArea);

		// 通知所有地图监听器角色跳转地图成功

		for (PlayerRoleJoinLeaveMapEventListener listener : joinLeaveMapEventListenerList) {
			try {
				listener.notifyPlayerRoleJoinedMap(myRole, targetMap);
			} catch (Exception e) {
				_LOGGER.error("角色：" + myRole.getId() + "跳转地图通知PlayerRoleJoinLeaveMapEventListener发生异常。", e);
			}
		}

		// 以下是处理发送周围角色数据

		// 获取角色周围地图实体私有列表
		PlayerRolePrivateEntityList privateList = roleEntity.getPrivateEntityList();
		try {
			if (privateList != null) {
				// 取得我私有列表中的地图实体
				List<KGameMapEntity> myEntityList = privateList.getMyListEntities();

				if (myEntityList != null) {
					for (KGameMapEntity myOtherEntity : myEntityList) {
						if (myOtherEntity != null && privateList.isShowEntity(myOtherEntity)) {
							// 向持有自己实体的其他角色发送自己在地图中生成的消息数据（这些角色存在于我的私有列表）
							KRole holderEntityRole = (KRole) myOtherEntity.getSourceObject();

							RoleMapEntityShowData data = getRoleMapEntityShowData(holderEntityRole, false);

							if (data != null) {
								PetMapEntityShowData petData = getPetMapEntityShowData(holderEntityRole.getId());
								boolean result = roleEntity.sendOtherEntityBornIntoMapData(myOtherEntity.getEntityType(), myOtherEntity.getSourceObjectID(), myOtherEntity.getCoordinate().getX(),
										myOtherEntity.getCoordinate().getY(), data, petData);
							}
						}
						// 向自己添加我的私有列表的其他角色在地图出生的

					}
				}

				// 取得我的地图实体对象持有者（持有我地图实体对象的其他角色地图实体对象集合）
				List<KGameMapEntity> holderList = privateList.getMyHolderEntities();
				RoleMapEntityShowData data = getRoleMapEntityShowData(myRole, false);
				PetMapEntityShowData petData = getPetMapEntityShowData(myRole.getId());
				if (petData != null) {
					roleEntity.sendOtherEntityPetBornIntoMapData(petData);
				}
				if (holderList != null) {
					// 向其他对象持有者打包组装我的角色进入地图状态消息（这里并不是真正发送消息，该消息由时效任务发送）
					for (KGameMapEntity holderEntity : holderList) {
						if (holderEntity != null && holderEntity.getPrivateEntityList() != null && holderEntity.getPrivateEntityList().isShowEntity(roleEntity)) {

							if (data != null) {
								holderEntity.sendOtherEntityBornIntoMapData(roleEntity.getEntityType(), roleEntity.getSourceObjectID(), target_x, target_y, data, petData);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			_LOGGER.error("角色：" + myRole.getId() + "跳转地图时处理通知周围角色时发生异常。", e);
		}

		return true;
	}

	/**
	 * 角色离开地图处理
	 * 
	 * @param map
	 * @param roleEntity
	 */
	public void playerRoleEntityLeaveMap(KGameNormalMap map, KGameMapEntity roleEntity) {
		// 获取角色周围地图实体列表
		PlayerRolePrivateEntityList privateList = roleEntity.getPrivateEntityList();
		if (privateList != null) {
			// 取得我的实体对象持有者
			List<KGameMapEntity> holderList = privateList.getMyHolderEntities();
			boolean isHasPet = false;
			long petId = 0;
			PetMapEntityShowData petData = getPetMapEntityShowData(roleEntity.getSourceObjectID());
			if (petData != null) {
				isHasPet = true;
				petId = petData.petId;
			}

			if (holderList != null) {
				// 向其他对象持有者打包组装角色离开地图状态消息（这里并不是真正发送消息，该消息由时效任务发送）
				for (KGameMapEntity holderEntity : holderList) {
					if (holderEntity != null && holderEntity.getPrivateEntityList() != null && holderEntity.getPrivateEntityList().isShowEntity(roleEntity)) {

						holderEntity.sendOtherEntityLeaveMapData(roleEntity.getEntityType(), roleEntity.getSourceObjectID(), isHasPet, petId);
					}
				}
			}
		}

		// 移除地图角色实体
		map.removeEntity(roleEntity);

		roleEntity.clearOtherRoleStateChangedMsgs();

		// TODO 以后可能会有更多角色离开地图事情要做，待定
	}

	/**
	 * 处理角色登录后进入地图dd
	 * 
	 * @param role
	 * @return
	 */
	public boolean playerRoleLoginJumpMap(KRole role) {
		if (role == null) {
			_LOGGER.error("角色登录跳转地图时发生错误，角色为null。");
			return false;
		}
		_LOGGER.debug("地图模块收到角色登录游戏消息！角色id是：" + role.getId());
		// 获取角色上次登录所在的地图ID
		int roleLastMapId = role.getRoleMapData().getCurrentMapId();
		float targetMap_x = role.getRoleMapData().getCorX();
		float targetMap_y = role.getRoleMapData().getCorY();

		KGameNormalMap targetMap = null;
		KCoordinate roleInitPoint = null;

		// if (isNoviceGuideJoin) {
		// // 如果是新手地图信息，则选择新手登录地图
		// targetMap = newGuideMap;
		// roleInitPoint = newGuideMap.getGameMapData()
		// .getPlayerRoleJumpMapCoordinateWithNoDirection();
		// } else {
		if (this.allGameMaps.containsKey(roleLastMapId)) {
			// 如果角色有上次登录的最后地图信息，则获取上次登录所在地图的坐标作为出生点
			targetMap = allGameMaps.get(roleLastMapId);
			roleInitPoint = new KCoordinate(targetMap_x, targetMap_y);
		} else {
			// 如果没有上次登录地图信息，则选择默认登录地图
			targetMap = firstMap;
			KGameMapData targetMapData = targetMap.getGameMapData();
			roleInitPoint = targetMapData.getPlayerRoleJumpMapCoordinateWithNoDirection();
			targetMap_x = roleInitPoint.getX();
			targetMap_y = roleInitPoint.getY();
		}
		// }

		// if (targetMap == null) {
		//
		// targetMap = firstMap;
		// KGameMapData targetMapData = targetMap.getGameMapData();
		// // 选择默认地图行走范围中心区域随机的出生点坐标
		// roleInitPoint = targetMapData
		// .getPlayerRoleJumpMapCoordinateWithNoDirection();
		// targetMap_x = roleInitPoint.getX();
		// targetMap_y = roleInitPoint.getY();
		// } else if (targetMap != newGuideMap) {
		//
		// roleInitPoint = new KCoordinate(targetMap_x, targetMap_y);
		// } else {
		// if (roleInitPoint == null) {
		// KGameMapData targetMapData = targetMap.getGameMapData();
		// roleInitPoint = targetMapData
		// .getPlayerRoleJumpMapCoordinateWithNoDirection();
		// targetMap_x = roleInitPoint.getX();
		// targetMap_y = roleInitPoint.getY();
		// }
		// }

		// 创建角色地图实体
		KGameMapEntity playerEntity = new KGameMapEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId(), role);

		// 处理角色进入地图流程，以及广播周围其他角色该玩家角色进入地图
		return playerRoleJoinInMap(role, playerEntity, targetMap, roleInitPoint.getX(), roleInitPoint.getY(), targetMap.getMapId());
	}

	/**
	 * 角色退出游戏时处理离开地图
	 * 
	 * @param role
	 */
	public void playerRoleLogoutGameLeaveMap(KRole role) {
		if (role == null) {
			_LOGGER.error("角色登录跳转地图时发生错误，角色为null。");
			return;
		}
		_LOGGER.debug("地图模块收到角色退出游戏离开地图通知！角色id是：" + role.getId());

		// 获取当前角色所在的地图
		int roleLastMapId = role.getRoleMapData().getCurrentMapId();

		KGameNormalMap targetMap = allGameMaps.get(roleLastMapId);

		if (targetMap != null && targetMap.isRoleEntityInMap(role.getId())) {
			KGameMapEntity playerEntity = targetMap.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId());

			// 设置角色坐标
			if (targetMap.mapType == KMapTypeEnum.普通主城地图) {
				role.getRoleMapData().setLastMapId(roleLastMapId);
			}
			role.getRoleMapData().setCorX(playerEntity.getCoordinate().getX());
			role.getRoleMapData().setCorY(playerEntity.getCoordinate().getY());

			// 处理角色离开地图流程，以及广播周围其他角色该玩家角色离开地图
			if (playerEntity != null) {
				playerRoleEntityLeaveMap(targetMap, playerEntity);
				// 释放entity数据
				if (playerEntity != null)
					playerEntity.dispose();
			}
		} else {
			roleLastMapId = role.getRoleMapData().getLastMapId();
			targetMap = allGameMaps.get(roleLastMapId);
			if (targetMap != null && targetMap.isRoleEntityInMap(role.getId())) {
				KGameMapEntity playerEntity = targetMap.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId());

				// 设置角色坐标
				if (targetMap.mapType == KMapTypeEnum.普通主城地图) {
					role.getRoleMapData().setLastMapId(roleLastMapId);
				}
				role.getRoleMapData().setCorX(playerEntity.getCoordinate().getX());
				role.getRoleMapData().setCorY(playerEntity.getCoordinate().getY());

				// 处理角色离开地图流程，以及广播周围其他角色该玩家角色离开地图
				if (playerEntity != null) {
					playerRoleEntityLeaveMap(targetMap, playerEntity);
					// 释放entity数据
					if (playerEntity != null)
						playerEntity.dispose();
				}
			}
		}
	}

	public void processRoleJoinNormalGameLevelAndLeaveMap(KRole role) {
		if (role == null) {
			_LOGGER.error("跳转地图时发生错误，角色为null。");
			return;
		}

		int mapId = role.getRoleMapData().getCurrentMapId();
		KGameNormalMap srcMap = KMapModule.getGameMapManager().getGameMap(mapId);

		if (srcMap == null) {
			_LOGGER.error("跳转地图时发生错误，源地图数据为null，角色ID：" + role.getId() + "。");
			return;
		}

		KGameMapEntity playerEntity = null;

		if ((playerEntity = srcMap.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId())) != null) {
			// 通知其他角色本角色离开源地图
			// 获取角色周围地图实体列表
			PlayerRolePrivateEntityList privateList = playerEntity.getPrivateEntityList();
			if (privateList != null) {
				// 取得我的实体对象持有者
				List<KGameMapEntity> holderList = privateList.getMyHolderEntities();
				boolean isHasPet = false;
				long petId = 0;
				PetMapEntityShowData petData = getPetMapEntityShowData(playerEntity.getSourceObjectID());
				if (petData != null) {
					isHasPet = true;
					petId = petData.petId;
				}

				if (holderList != null) {
					// 向其他对象持有者打包组装角色离开地图状态消息（这里并不是真正发送消息，该消息由时效任务发送）
					for (KGameMapEntity holderEntity : holderList) {
						if (holderEntity != null && holderEntity.getPrivateEntityList() != null && holderEntity.getPrivateEntityList().isShowEntity(playerEntity)) {

							holderEntity.sendOtherEntityLeaveMapData(playerEntity.getEntityType(), playerEntity.getSourceObjectID(), isHasPet, petId);
						}
					}
				}
			}
			
			playerEntity.clearOtherRoleStateChangedMsgs();
		}

	}

	public void processRoleFinishNormalGameLevelAndReturnToMap(KRole role) {
		if (role == null) {
			_LOGGER.error("跳转地图时发生错误，角色为null。");
			return;
		}

		playerRoleLoginJumpMap(role);
		KDialogService.sendNullDialog(role);
	}

	/**
	 * 处理角色跳转区域（世界地图跳转）
	 * 
	 * @param role
	 * @param srcMapID
	 * @param targetAreaId
	 * @return
	 */
	public boolean playerRoleJumpArea(KRole role, int srcMapID, int targetAreaId) {
		if (role == null) {
			_LOGGER.error("角色跳转区域发生异常，角色为null。地图ID：" + srcMapID + "，区域ID：" + targetAreaId);
			return false;
		}

		if (!this.allAreas.containsKey(targetAreaId)) {
			_LOGGER.error("角色跳转区域发生异常，找不到该区域。角色ID：" + role.getId() + "，地图ID：" + srcMapID + "，区域ID：" + targetAreaId);
			return false;
		}

		KGameMapArea area = this.allAreas.get(targetAreaId);

		KGameNormalMap firstLevelMap = area.getFirstLevelMap();

		if (area.getFirstLevelMap() == null) {
			_LOGGER.error("角色跳转区域发生异常，找不到该区域的目标地图。角色ID：" + role.getId() + "，地图ID：" + srcMapID + "，区域ID：" + targetAreaId);
			return false;
		}

		KCoordinate coor = firstLevelMap.getGameMapData().getPlayerRoleJumpMapCoordinateWithNoDirection();

		return playerRoleJumpMap(role, srcMapID, firstLevelMap.getMapId(), coor.getX(), coor.getY(), true);
	}

	public static RoleMapEntityShowData getRoleMapEntityShowData(KRole role, boolean isDuplicated) {
		RoleMapEntityShowData data = new RoleMapEntityShowData();
		data.roleName = role.getName();

		data.familyName = KSupportFactory.getGangSupport().getGangMapShowNameByRole(role.getId());
		data.familyIconId = KSupportFactory.getGangSupport().getMedalIcon(role.getId());

		data.vipLv = KSupportFactory.getVIPModuleSupport().getVipLv((role.getId()));
		data.job = role.getJob();
		data.roleResId = role.getInMapResId();
		data.equipList = KSupportFactory.getItemModuleSupport().getRoleEquipShowDataList(role.getId());
		data.mountResId = KSupportFactory.getMountModuleSupport().getRoleMountResId(role);
		data.fashionResData = KSupportFactory.getFashionModuleSupport().getFashingResId(role.getId());
		if (isDuplicated) {
			data.isFighting = role.isFighting();
		} else {
			data.isFighting = false;
		}

		float mountSpeedUp = KSupportFactory.getMountModuleSupport().getRoleMountMoveSpeedup(role);
		data.moveSpeedX = role.getMoveSpeedX() * (mountSpeedUp);
		data.moveSpeedY = role.getMoveSpeedY() * (mountSpeedUp);

		int[] itemEffectResId = KSupportFactory.getItemModuleSupport().getEquiSetMapResIds(role.getId());
		if (itemEffectResId != null && itemEffectResId.length == 2) {
			data.starItemResId = itemEffectResId[0];
			data.stoneItemResId = itemEffectResId[1];
		}

		return data;
	}

	public static PetMapEntityShowData getPetMapEntityShowData(long roleId) {
		PetMapEntityShowData data = null;
		KPet pet = KSupportFactory.getPetModuleSupport().getFightingPet(roleId);
		if (pet != null) {
			data = new PetMapEntityShowData();
			data.petId = pet.getId();
			data.roleId = pet.getOwnerId();
			data.petResId = pet.getInMapResId();
			data.petName = pet.getName();
			data.qualityColor = pet.getQuality().getColor();
		}
		return data;
	}

	// /**
	// * 发送世界地图数据
	// *
	// * @param role
	// */
	// public void sendWorldMapData(KRole role) {
	// KGameMessage sendMsg = KGame
	// .newLogicMessage(KGameMapProtocol.SM_SEND_WORLD_MAP_DATA);
	// sendMsg.writeByte(allAreas.size());
	// for (KGameMapArea area : allAreas.values()) {
	// sendMsg.writeInt(area.getAreaId());
	// sendMsg.writeBoolean(area.checkAreaIsOpen(role));
	// }
	// KPlayerRoleModuleSupport support = KSupportFactory.getRoleSupport();
	//
	// support.sendMsg(role, sendMsg);
	// }

	// /**
	// * 通过角色等级检测是否需要更新世界地图信息
	// *
	// * @param role
	// */
	// public void checkAndUpdateWorldMapData(KRole role) {
	// for (KGameMapArea area : allAreas.values()) {
	// if (area.getOpenRoleLevel() == role.getLevel()
	// && area.checkAreaIsOpen(role)) {
	// KGameMessage sendMsg = KGame
	// .newLogicMessage(KGameMapProtocol.SM_UPDATE_WORLD_MAP_DATA);
	// sendMsg.writeInt(area.getAreaId());
	// sendMsg.writeBoolean(true);
	// KSupportFactory.getRoleSupport().sendMsg(role, sendMsg);
	// }
	// }
	// }

	// /**
	// * 通过前置任务ID检测是否需要更新世界地图信息
	// *
	// * @param role
	// */
	// public void checkAndUpdateWorldMapDataByMissionTemplateId(KRole role,
	// int missionTemplateId) {
	// for (KGameMapArea area : allAreas.values()) {
	// if (area.getOpenFrontMissionTemplateId() == missionTemplateId
	// && area.checkAreaIsOpen(role)) {
	// KGameMessage sendMsg = KGame
	// .newLogicMessage(KGameMapProtocol.SM_UPDATE_WORLD_MAP_DATA);
	// sendMsg.writeInt(area.getAreaId());
	// sendMsg.writeBoolean(true);
	// KSupportFactory.getRoleSupport().sendMsg(role, sendMsg);
	// }
	// }
	// }

	/**
	 * 处理发送地图数据消息给客户端
	 */
	public void sendGameMapDataToClient(KGameMapEntity playerEntity, KGameNormalMap targetMap, boolean isSendJumpAreaEffect) {
		if (playerEntity == null || targetMap == null) {
			return;
		}
		if (!playerEntity.isPlayerRoleType()) {
			return;
		}

		KRole role = (KRole) playerEntity.getSourceObject();

		KGameMapData mapData = targetMap.getGameMapData();
		
		KNormalMapActivityData activityMapData = null;
		if(normalMapActivityManager!=null){
			activityMapData = normalMapActivityManager.getNormalMapActivityData(targetMap.getMapId());
		}
		String map_file_name = targetMap.getMapDataFileName();
		int musicId = targetMap.getBgMusicResId();
		if(activityMapData!=null && activityMapData.isActivityTakeEffectNow()){
			map_file_name = activityMapData.map_xml_path;
			musicId = activityMapData.musicId;
		}

		KGameMessage sendMsg = KGame.newLogicMessage(KMapProtocol.SM_SEND_MAP_DATA);

		sendMsg.writeByte(targetMap.mapType.type);

		// 发送地图基本数据，及主角色出生点坐标
		sendMsg.writeInt(targetMap.getMapId());
		sendMsg.writeUtf8String(targetMap.getName());
		sendMsg.writeUtf8String(map_file_name);
		sendMsg.writeInt(targetMap.getAreaId());
		sendMsg.writeInt(targetMap.getMapResId());
		sendMsg.writeInt(musicId);
		sendMsg.writeFloat(playerEntity.getCoordinate().getX());
		sendMsg.writeFloat(playerEntity.getCoordinate().getY());

		role.sendMsg(sendMsg);

		// _LOGGER.info("发送地图模块数据消息！角色id是：" + role.getRoleId());
	}

	public void reflashMapOtherRoleToClient(KRole role) {
		if (role == null) {
			_LOGGER.error("###EXCEPTION-----地图模块reflashMapOtherRoleToClient方法发送地图其他角色位置数据消息出现异常，角色为NULL！");
			return;
		}
		int roleMapId = role.getRoleMapData().getCurrentMapId();

		KGameNormalMap map = allGameMaps.get(roleMapId);

		if (map == null) {
			_LOGGER.error("###EXCEPTION-----地图模块reflashMapOtherRoleToClient方法发送地图其他角色位置数据消息出现异常，地图为NULL！地图id是：" + roleMapId);
			return;
		}

		KGameMapEntity roleEntity = map.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId());

		if (roleEntity == null) {
			_LOGGER.error("###EXCEPTION-----地图模块reflashMapOtherRoleToClient方法发送地图其他角色位置数据消息出现异常，roleEntity为NULL！地图id是：" + roleMapId);
			return;
		}

		// 获取角色周围地图实体私有列表
		PlayerRolePrivateEntityList privateList = roleEntity.getPrivateEntityList();
		if (privateList != null) {
			// 取得我私有列表中的地图实体
			List<KGameMapEntity> myEntityList = privateList.getMyListEntities();
			// 向自己发送地图中其他角色数据（这些角色存在于我的私有列表）
			if (myEntityList != null) {
				for (KGameMapEntity myOtherEntity : myEntityList) {
					if (myOtherEntity != null && privateList.isShowEntity(myOtherEntity)) {
						KRole holderEntityRole = (KRole) myOtherEntity.getSourceObject();
						RoleMapEntityShowData data = getRoleMapEntityShowData(holderEntityRole, false);
						if (data != null) {
							PetMapEntityShowData petData = getPetMapEntityShowData(holderEntityRole.getId());
							roleEntity.sendOtherEntityBornIntoMapData(myOtherEntity.getEntityType(), myOtherEntity.getSourceObjectID(), myOtherEntity.getCoordinate().getX(), myOtherEntity
									.getCoordinate().getY(), data, petData);
						}
					}
				}
				// // 发送周围角色地图实体数据
				// KGameMessage msg = roleEntity
				// .getOtherEntityStateChangeDataMessageImmediately();
				// if (msg != null) {
				// KPlayerRoleModuleSupport support = KSupportFactory
				// .getRoleSupport();
				//
				// support.sendMsg(role, msg);
				//
				// _LOGGER.info("发送周围角色地图实体数据消息！角色id是：" + role.getRoleId());
				// }
			}
		}
		for (int i = 0; i < joinLeaveMapEventListenerList.size(); i++) {
			joinLeaveMapEventListenerList.get(i).notifyPlayerRoleRejoinedMap(role);
		}
	}

	// public void sendJumpMapTips(KRole role, String targetMapName) {
	// KGameMessage sendMsg = KGame
	// .newLogicMessage(KGameMapProtocol.SM_NOTIFY_JUMP_MAP);
	// // sendMsg.writeUtf8String("正在进入地图：" + targetMapName +
	// // ",正在加载地图数据，请稍后。");
	// sendMsg.writeUtf8String(MapTips
	// .getTipsJumpMapTips(targetMapName));
	// KPlayerRoleModuleSupport support = KSupportFactory.getRoleSupport();
	//
	// support.sendMsg(role, sendMsg);
	// }

	/**
	 * 处理角色在地图中更新坐标位置
	 * 
	 * @param role
	 *            更新坐标的角色
	 * @param mapId
	 *            角色所在地图ID
	 * @param x
	 *            要更新的X坐标
	 * @param y
	 *            要更新的Y坐标
	 */
	public void processRoleUpdateCoordinate(KRole role, int mapId, float x, float y) {
		if (role == null) {
			_LOGGER.error("角色更新地图坐标时发生错误，角色为null。");
			return;
		}

		// _LOGGER.debug("角色更新地图坐标，角色：" + role.getRoleId() +
		// "，地图:"+mapId+"，坐标：(" + x + "," + y
		// + ")。");

		KGameNormalMap map = allGameMaps.get(mapId);

		if (map == null) {
			_LOGGER.error("角色更新地图坐标时发生错误，源地图数据为null，角色ID：" + role.getId() + "。");
			return;
		}
		KGameMapEntity roleEntity = map.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId());

		if (roleEntity == null) {
			_LOGGER.error("角色更新地图坐标时发生错误，角色对应的地图实体为null。角色ID：" + role.getId());
			return;
		}

		// 以下是处理地图中本角色实体的持有者，对其通知本角色坐标的改变

		// 获取角色周围地图实体私有列表
		PlayerRolePrivateEntityList privateList = roleEntity.getPrivateEntityList();
		if (privateList != null) {
			// 取得我的地图实体对象持有者（持有我地图实体对象的其他角色地图实体对象集合）
			List<KGameMapEntity> holderList = privateList.getMyHolderEntities();

			if (holderList != null) {
				// 向其他对象持有者打包组装我的角色更新坐标消息（这里并不是真正发送消息，该消息由时效任务发送）

				for (KGameMapEntity holderEntity : holderList) {
					if (holderEntity != null && holderEntity.getPrivateEntityList() != null && holderEntity.getPrivateEntityList().isShowEntity(roleEntity)) {
						if (holderEntity.getSourceObjectID() == roleEntity.getSourceObjectID()) {
							_LOGGER.debug("***************** 自己的Holder中存在自己角色。角色ID：" + role.getId());
							continue;
						} else {
							holderEntity.sendOtherEntityWalkStateData(roleEntity.getEntityType(), roleEntity.getSourceObjectID(), x, y);
						}
					}
				}
			}
		}

		// 设置角色属性的坐标信息
		role.getRoleMapData().setCorX(x);
		role.getRoleMapData().setCorY(y);
		roleEntity.getCoordinate().setX(x);
		roleEntity.getCoordinate().setY(y);
	}

	public void processPlayerRoleResetCoordinate(KRole role, float x, float y) {
		if (role == null) {
			_LOGGER.error("角色更新地图坐标时发生错误，角色为null。");
			return;
		}

		// _LOGGER.debug("角色更新地图坐标，角色：" + role.getRoleId() +
		// "，地图:"+mapId+"，坐标：(" + x + "," + y
		// + ")。");

		KGameNormalMap map = allGameMaps.get(role.getRoleMapData().getCurrentMapId());
		if (map == null) {
			_LOGGER.error("角色更新地图坐标时发生错误，源地图数据为null，角色ID：" + role.getId() + "。");
			return;
		}

		KGameMapEntity roleEntity = map.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId());

		if (roleEntity == null) {
			_LOGGER.error("角色更新地图坐标时发生错误，角色对应的地图实体为null。角色ID：" + role.getId());
			return;
		}

		// 设置角色属性的坐标信息
		role.getRoleMapData().setCorX(x);
		role.getRoleMapData().setCorY(y);
		roleEntity.getCoordinate().setX(x);
		roleEntity.getCoordinate().setY(y);

		KGameMessage sendMsg = KGame.newLogicMessage(KMapProtocol.SM_RESET_SPRITE_XY);
		sendMsg.writeFloat(x);
		sendMsg.writeFloat(y);

		role.sendMsg(sendMsg);

		// 获取角色周围地图实体私有列表
		PlayerRolePrivateEntityList privateList = roleEntity.getPrivateEntityList();
		if (privateList != null) {
			// 取得我的地图实体对象持有者（持有我地图实体对象的其他角色地图实体对象集合）
			List<KGameMapEntity> holderList = privateList.getMyHolderEntities();

			if (holderList != null) {
				// 向其他对象持有者打包组装我的角色更新坐标消息（这里并不是真正发送消息，该消息由时效任务发送）

				for (KGameMapEntity holderEntity : holderList) {
					if (holderEntity != null && holderEntity.getPrivateEntityList() != null && holderEntity.getPrivateEntityList().isShowEntity(roleEntity)) {
						if (holderEntity.getSourceObjectID() == roleEntity.getSourceObjectID()) {
							_LOGGER.debug("***************** 自己的Holder中存在自己角色。角色ID：" + role.getId());
						}
						holderEntity.sendOtherEntityResetStateData(roleEntity.getEntityType(), roleEntity.getSourceObjectID(), x, y);
					}
				}
			}
		}
	}

	public void processRoleFinishBattleReturnToMap(KRole role) {
		KGameNormalMap targetMap = allGameMaps.get(role.getRoleMapData().getCurrentMapId());

		KGameMapEntity roleEntity = targetMap.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId());

		if (roleEntity == null) {
			_LOGGER.error("角色：" + role.getId() + "完成战斗返回地图时处理通知周围角色时发生错误。找不到地图实体。重新创建新地图实体。");
			roleEntity = new KGameMapEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId(), role);
		}

		// 发送地图数据给客户端
		sendGameMapDataToClient(roleEntity, targetMap, false);

		// 以下是处理发送周围角色数据

		// 获取角色周围地图实体私有列表
		// 获取角色周围地图实体私有列表
		PlayerRolePrivateEntityList privateList = roleEntity.getPrivateEntityList();
		try {
			if (privateList != null) {
				// 取得我私有列表中的地图实体
				List<KGameMapEntity> myEntityList = privateList.getMyListEntities();

				if (myEntityList != null) {
					for (KGameMapEntity myOtherEntity : myEntityList) {
						if (myOtherEntity != null && privateList.isShowEntity(myOtherEntity)) {
							// 向持有自己实体的其他角色发送自己在地图中生成的消息数据（这些角色存在于我的私有列表）
							KRole holderEntityRole = (KRole) myOtherEntity.getSourceObject();

							RoleMapEntityShowData data = getRoleMapEntityShowData(holderEntityRole, false);

							if (data != null) {
								PetMapEntityShowData petData = getPetMapEntityShowData(holderEntityRole.getId());
								boolean result = roleEntity.sendOtherEntityBornIntoMapData(myOtherEntity.getEntityType(), myOtherEntity.getSourceObjectID(), myOtherEntity.getCoordinate().getX(),
										myOtherEntity.getCoordinate().getY(), data, petData);
							}
						}
						// 向自己添加我的私有列表的其他角色在地图出生的

					}
				}

				// 取得我的地图实体对象持有者（持有我地图实体对象的其他角色地图实体对象集合）
				List<KGameMapEntity> holderList = privateList.getMyHolderEntities();
				RoleMapEntityShowData data = getRoleMapEntityShowData(role, false);
				PetMapEntityShowData petData = getPetMapEntityShowData(role.getId());
				if (petData != null) {
					roleEntity.sendOtherEntityPetBornIntoMapData(petData);
				}
				if (holderList != null) {
					// 向其他对象持有者打包组装我的角色进入地图状态消息（这里并不是真正发送消息，该消息由时效任务发送）
					for (KGameMapEntity holderEntity : holderList) {
						if (holderEntity != null && holderEntity.getPrivateEntityList() != null && holderEntity.getPrivateEntityList().isShowEntity(roleEntity)) {

							if (data != null) {
								holderEntity.sendOtherEntityBornIntoMapData(roleEntity.getEntityType(), roleEntity.getSourceObjectID(), role.getRoleMapData().getCorX(), role.getRoleMapData()
										.getCorY(), data, petData);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			_LOGGER.error("角色：" + role.getId() + "完成战斗返回地图时处理通知周围角色时发生异常。", e);
		}
	}

	/**
	 * 处理角色获取本地图周围玩家角色列表
	 * 
	 * @param role
	 * @param srcMapId
	 */
	public void processGetMapAroundRoleList(KRole role, int srcMapId, boolean isNewGet) {
		if (role == null) {
			_LOGGER.error("获取地图周围玩家角色列表发生错误，角色为null。");
			return;
		}

		KGameNormalMap map = allGameMaps.get(srcMapId);

		if (map == null) {
			_LOGGER.warn("取地图周围玩家角色列表发生错误，源地图数据为null，角色ID：" + role.getId() + "。");
			sendAroundRoleInfoMsg(role, null);
			return;
		}

		KGameMapEntity myEntity = map.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId());

		if (myEntity == null) {
			_LOGGER.warn("取地图周围玩家角色列表发生错误，源地图数据为null，角色ID：" + role.getId() + "。");
			sendAroundRoleInfoMsg(role, null);
			return;
		}
		if (isNewGet) {
			myEntity.getAroundRoleIdSet().clear();
		}

		List<AroundRoleStruct> structList = new ArrayList<KGameMapManager.AroundRoleStruct>();
		int mapRoleSize = map.getEntitiesSize(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE);
		if (mapRoleSize > 0) {
			int writeSize = 0;
			for (KGameMapEntity roleEntity : myEntity.getPrivateEntityList().getMyListEntities()) {
				if (!myEntity.getAroundRoleIdSet().contains(roleEntity.getSourceObjectID())) {
					KRole otherRole = (KRole) roleEntity.getSourceObject();
					structList.add(new AroundRoleStruct(otherRole.getId(), otherRole.getName(), 0,// otherRole.getHeadResId(),
							otherRole.getLevel()));
					myEntity.getAroundRoleIdSet().add(otherRole.getId());
					writeSize++;
					if (writeSize >= GET_AROUND_ROLE_PER_COUNT) {
						break;
					}
				}
			}
			if (writeSize < GET_AROUND_ROLE_PER_COUNT) {
				Set<GameMapEntityCacheKey> keySet = map.getEntitysMap().keySet();
				for (GameMapEntityCacheKey key : keySet) {
					if (key.type == KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE.entityType && key.objId != role.getId() && !myEntity.getAroundRoleIdSet().contains(key.objId)) {

						KRole otherRole = (KRole) (map.getEntity(KMapEntityTypeEnum.getEnum(key.type), key.objId).getSourceObject());
						structList.add(new AroundRoleStruct(otherRole.getId(), otherRole.getName(), 0,// otherRole.getHeadResId(),
								otherRole.getLevel()));
						myEntity.getAroundRoleIdSet().add(otherRole.getId());
						writeSize++;
						if (writeSize >= GET_AROUND_ROLE_PER_COUNT) {
							break;
						}
					}
				}
			}
		}
		sendAroundRoleInfoMsg(role, structList);
	}

	private void sendAroundRoleInfoMsg(KRole role, List<AroundRoleStruct> list) {

		KGameMessage sendMsg = KGame.newLogicMessage(KMapProtocol.SM_GET_AROUND_PLAYERROLE_LIST);

		if (list != null) {
			sendMsg.writeInt(list.size());
			for (AroundRoleStruct struct : list) {
				sendMsg.writeLong(struct.roleId);
				sendMsg.writeUtf8String(struct.roleName);
				sendMsg.writeInt(struct.headId);
				sendMsg.writeInt(struct.lv);
			}
		} else {
			sendMsg.writeInt(0);
		}

		role.sendMsg(sendMsg);
	}

	/**
	 * 自动寻路引导至目标NPC，获取其寻路路径
	 * 
	 * @param role
	 *            角色Id
	 * @param npcTemplateId
	 *            npc模版ID
	 * @return
	 */
	public AutoSearchRoadTrack autoDirectToNpc(KRole role, int npcTemplateId) {
		AutoSearchRoadTrack track = new AutoSearchRoadTrack();

		KGameNormalMap currentMap = this.getGameMap(role.getRoleMapData().getCurrentMapId());
		KGameNormalMap targetMap = this.npcMappingGameMap.get(npcTemplateId);
		if (targetMap != null) {

			autoDirectToTarget(currentMap, null, targetMap, npcTemplateId, track, true);
		}

		return track;
	}

	/**
	 * 自动寻路引导至目标关卡，获取其寻路路径
	 * 
	 * @param role
	 *            橘色ID
	 * @param sccnarioId
	 *            目标关卡对应的场景ID
	 * @param levelId
	 *            目标关卡ID
	 * @return
	 */
	public AutoSearchRoadTrack autoDirectToGameLevel(KRole role, int sccnarioId) {

		AutoSearchRoadTrack track = new AutoSearchRoadTrack();

		KGameNormalMap currentMap = this.getGameMap(role.getRoleMapData().getCurrentMapId());
		KGameNormalMap targetMap = this.scenarioMappingGameMap.get(sccnarioId);
		if (targetMap != null) {
			int exitId = 0;
			for (GameMapExitsEventData exitData : targetMap.getGameMapData().getAllGameMapExitsEventData()) {
				if (exitData.getExitType() == GameMapExitsEventData.EXIT_EVENT_TYPE_GAMELEVELS && exitData.targetId == sccnarioId) {
					exitId = exitData.getExitId();
				}
			}
			autoDirectToTarget(currentMap, null, targetMap, exitId, track, false);
		}

		return track;
	}

	public boolean autoDirectToTarget(KGameNormalMap currentMap, KGameNormalMap lastMap, KGameNormalMap targetMap, int targetId, AutoSearchRoadTrack track, boolean isNpcDirect) {
		// System.out.println("###### currentMap ID:"+currentMap.getId());
		if (currentMap.getMapId() == targetMap.getMapId()) {
			if (isNpcDirect) {
				track.addRoadPath(new AutoSearchRoadTrack.RoadPath(RoadPath.PATH_TYPE_WALK_TO_NPC, getMapInstanceId(targetMap, targetId, isNpcDirect)));
			} else {
				track.addRoadPath(new AutoSearchRoadTrack.RoadPath(RoadPath.PATH_TYPE_WALK_TO_EXITS, getMapInstanceId(targetMap, targetId, isNpcDirect)));
			}
			return true;
		} else {
			for (GameMapExitsEventData exitData : currentMap.getGameMapData().getAllGameMapExitsEventData()) {
				if (exitData.getExitType() == GameMapExitsEventData.EXIT_EVENT_TYPE_JUMP_MAP) {
					if (lastMap == null || exitData.targetId != lastMap.getMapId()) {
						int upMapId = exitData.targetId;
						int exitId = exitData.getExitId();
						KGameNormalMap upMap = this.getGameMap(upMapId);
						boolean result = autoDirectToTarget(upMap, currentMap, targetMap, targetId, track, isNpcDirect);
						if (result) {
							RoadPath path = new AutoSearchRoadTrack.RoadPath(RoadPath.PATH_TYPE_WALK_TO_EXITS, getMapInstanceId(currentMap, exitId, false));
							track.addRoadPath(path);
							// System.out.println("**********寻路路径,出入口名称："+
							// currentMap.getGameMapData().getGameMapExitsEventData(direction).targetName+
							// "，type:"+path.pathType+",id:"+path.targetId);
							return true;
						}
					}
					// else {
					// return false;
					// }
				}
			}
		}

		return false;
	}

	private int getMapInstanceId(KGameNormalMap map, int targetId, boolean isNpcDirect) {
		int instanceId = 0;
		if (isNpcDirect) {
			KGameMapEntity entity = map.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_NPC, targetId);
			if (entity != null) {
				instanceId = entity.getEditorInstanceId();
			}
		} else {
			KGameMapData data = map.getGameMapData();
			if (data != null) {
				GameMapExitsEventData exitData = data.getGameMapExitsEventData(targetId);
				if (exitData != null) {
					instanceId = exitData.getNpcOrExitInstanceId();
				}
			}
		}
		return instanceId;
	}

	public void changeRoleSetting(KRole role, boolean isChallengeDeny, byte showLevel, boolean isBlockChat) {
		/**
		 * boolean isChallengeDeny; byte showLevel; boolean isBlockWorldChat;
		 * boolean isBlockAreaChat; boolean isBlockFamilyChat;
		 */
		if (role != null) {
			changeRoleMapShowPlayers(role, showLevel);

			if (role.getRoleGameSettingData().isBlockOnlinePVP() != isChallengeDeny) {
				role.getRoleGameSettingData().setBlockOnlinePVP(isChallengeDeny);
			}
			if (role.getRoleGameSettingData().isBlockChat() != isBlockChat) {
				role.getRoleGameSettingData().setBlockChat(isBlockChat);
			}
			sendRoleSettingMsg(role, false);
		}
		_LOGGER.warn("changeRoleSetting:::::isBlockOnlinePVP=" + isChallengeDeny + ",showLevel=" + showLevel + ",isBlockChat:" + isBlockChat);
	}

	private void changeRoleMapShowPlayers(KRole role, byte showLevel) {
		int mapId = role.getRoleMapData().getCurrentMapId();
		KGameNormalMap map = allGameMaps.get(mapId);
		if (map == null) {
			_LOGGER.error("角色地图显示人数时changeRoleMapShowPlayers()发生错误，源地图数据为null，角色ID：" + role.getId() + "。");
			return;
		}
		KGameMapEntity roleEntity = map.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId());

		if (roleEntity == null) {
			_LOGGER.error("角色地图显示人数时changeRoleMapShowPlayers()发生错误，角色对应的地图实体为null。角色ID：" + role.getId());
			return;
		}

		if (role.getRoleGameSettingData().getMapShowPlayerLevel() == showLevel) {
			return;
		} else if (role.getRoleGameSettingData().getMapShowPlayerLevel() < showLevel) {
			role.getRoleGameSettingData().setMapShowPlayerLevel(showLevel);
			PlayerRolePrivateEntityList privateList = roleEntity.getPrivateEntityList();
			if (privateList != null) {
				int showSize = this.showPlayerLevelMap.get(showLevel);
				List<KGameMapEntity> addEntityList = privateList.increaseShowEntitySize(showSize);

				for (KGameMapEntity myOtherEntity : addEntityList) {
					// 向持有自己实体的其他角色发送自己在地图中生成的消息数据（这些角色存在于我的私有列表）
					KRole holderEntityRole = (KRole) myOtherEntity.getSourceObject();
					RoleMapEntityShowData data = getRoleMapEntityShowData(holderEntityRole, false);
					if (data != null) {
						PetMapEntityShowData petData = getPetMapEntityShowData(holderEntityRole.getId());
						boolean result = roleEntity.sendOtherEntityBornIntoMapData(myOtherEntity.getEntityType(), myOtherEntity.getSourceObjectID(), myOtherEntity.getCoordinate().getX(),
								myOtherEntity.getCoordinate().getY(), data, petData);
					}
					// 向自己添加我的私有列表的其他角色在地图出生的

				}

				// sendRoleSettingMsg(role, false);
			}
		} else if (role.getRoleGameSettingData().getMapShowPlayerLevel() > showLevel) {
			role.getRoleGameSettingData().setMapShowPlayerLevel(showLevel);
			PlayerRolePrivateEntityList privateList = roleEntity.getPrivateEntityList();
			if (privateList != null) {
				int showSize = this.showPlayerLevelMap.get(showLevel);
				List<KGameMapEntity> subEntityList = privateList.decreaseShowEntitySize(showSize);
				for (KGameMapEntity holderEntity : subEntityList) {
					if (holderEntity != null) {
						boolean isHasPet = false;
						long petId = 0;
						PetMapEntityShowData petData = getPetMapEntityShowData(holderEntity.getSourceObjectID());
						if (petData != null) {
							isHasPet = true;
							petId = petData.petId;
						}
						roleEntity.sendOtherEntityLeaveMapData(holderEntity.getEntityType(), holderEntity.getSourceObjectID(), isHasPet, petId);
					}
				}
			}

			// sendRoleSettingMsg(role, false);
		}
	}

	public void sendRoleSettingMsg(KRole role, boolean isSendShowLevelData) {
		KGameMessage sendMsg = KGame.newLogicMessage(KMapProtocol.SM_SEND_ROLE_SETTING);
		sendMsg.writeBoolean(role.getRoleGameSettingData().isBlockOnlinePVP());
		sendMsg.writeByte(role.getRoleGameSettingData().getMapShowPlayerLevel());
		sendMsg.writeBoolean(role.getRoleGameSettingData().isBlockChat());
		sendMsg.writeBoolean(isSendShowLevelData);
		if (isSendShowLevelData) {
			sendMsg.writeByte(showPlayerLevelMap.size());
			for (Byte lv : showPlayerLevelMap.keySet()) {
				sendMsg.writeByte(lv);
				sendMsg.writeShort(showPlayerLevelMap.get(lv));
			}
		}
		role.sendMsg(sendMsg);
	}

	// /**
	// * 地图事件ID生成器
	// *
	// * @author Administrator
	// *
	// */
	// public static class MapEventIdGenerator {
	// private AtomicLong id;
	//
	// public MapEventIdGenerator(long initialValue) {
	// id = new AtomicLong(initialValue);
	// }
	//
	// public long currentEventId() {
	// return id.get();
	// }
	//
	// public long nextEventId() {
	// return id.incrementAndGet();
	// }
	// }

	public static class AroundRoleStruct {
		public long roleId;
		public String roleName;
		public int headId;
		public int lv;

		public AroundRoleStruct(long roleId, String roleName, int headId, int lv) {
			super();
			this.roleId = roleId;
			this.roleName = roleName;
			this.headId = headId;
			this.lv = lv;
		}

	}

}
