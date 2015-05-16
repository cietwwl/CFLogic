package com.kola.kmp.logic.map.duplicatemap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.XmlUtil;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.map.GameMapEntityCacheKey;
import com.kola.kmp.logic.map.GameMapExitsEventData;
import com.kola.kmp.logic.map.KCoordinate;
import com.kola.kmp.logic.map.KGameMapData;
import com.kola.kmp.logic.map.KGameMapEntity;
import com.kola.kmp.logic.map.KGameMapModuleException;
import com.kola.kmp.logic.map.KGameNormalMap;
import com.kola.kmp.logic.map.KMap;
import com.kola.kmp.logic.map.KMapEntityData;
import com.kola.kmp.logic.map.KMapModule;
import com.kola.kmp.logic.map.PlayerRoleJoinLeaveMapEventListener;
import com.kola.kmp.logic.map.PlayerRolePrivateEntityList;
import com.kola.kmp.logic.npc.KNPCDataStructs.KNPCTemplate;
import com.kola.kmp.logic.other.KMapDuplicateTypeEnum;
import com.kola.kmp.logic.other.KMapEntityTypeEnum;
import com.kola.kmp.logic.other.KMapTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KDuplicateMapImpl implements KDuplicateMap<KGameMapEntity>,
		Cloneable {
	// private KDuplicateMapManager _manager;
	private static final KGameLogger _LOGGER = KGameLogger
			.getLogger(KDuplicateMapImpl.class);
	/**
	 * 周围角色限制类型：不限制（全部角色显示）
	 */
	public final static byte UNIT_LIMIT_TYPE_UNLIMIT = 0;
	/**
	 * 周围角色限制类型：不显示（不显示周围角色）
	 */
	public final static byte UNIT_LIMIT_TYPE_NOT_SHOW = 1;
	/**
	 * 周围角色限制类型：限制人数显示（显示n个周围角色）
	 */
	public final static byte UNIT_LIMIT_TYPE_LIMIT = 2;
	// 周围角色限制类型
	public byte unitLimitType;
	// 周围角色限制数量:
	public int unitLimit;
	// 周围角色限制规则是否并发:
	public boolean unitLimitIsConcurrent = true;
	// 周围角色限制规则是否按照优先级处理:
	public boolean unitLimitIsPriority = true;

	// －－地图上的实体
	private static final int INIT_MAX_ENTITIES = 2000;
	// private final ReadWriteLock lockGameMapEntities = new
	// ReentrantReadWriteLock();
	private ConcurrentHashMap<GameMapEntityCacheKey, KGameMapEntity> chm_GameMapEntities = new ConcurrentHashMap<GameMapEntityCacheKey, KGameMapEntity>(
			INIT_MAX_ENTITIES);

	// // 角色进入离开地图事件监听器集合
	// private final Set<PlayerRoleJoinLeaveMapEventListener> joinleavelisteners
	// = new HashSet<PlayerRoleJoinLeaveMapEventListener>();

	private int _mapId;// 地图Id
	private int _duplicateId; // 副本ID
	private int _mapGroupId;// 地图组ID
	private String _name;// 地图名称
	private int _mapType;// 地图类型
	private String _desc;// 地图描述
	private String _mapDataFileName;// 地图数据文件名（不带后缀）
	private int _mapResId;// 地图客户端资源ID
	private int bgMusicResId;// 地图背景音乐
	private byte specialEfficiencyType;// 地图特效类型
	private int specialEfficiencyId;// 地图特效资源ID
	private int particleResId;// 地图粒子效果资源ID
	private int joinMapEfficiencyId;// 进城显示特效ID
	//是否显示随从
	public boolean isShowPet = true;

	private boolean _isOpen;// 地图是否开放

	// private KGameMapData _mapData;// 地图数据
	// private final Lock lockGameMapData = new ReentrantLock();

	private int _entitySize;// 地图实体数量

	public KMapTypeEnum mapType;

	public KMapDuplicateTypeEnum duplicateType;

	private Map<Integer, KDuplicateMapBornPoint> bornPointDatas;

	private Map<Integer, CollisionEventObjectData> collisionDatas;

	private CollisionEventListener collisionEventListener = null;

	protected KDuplicateMapImpl(int id, int groupId, int type,
			byte duplicateType, byte unitLimitType, int unitLimit,boolean isShowPet, String name,
			String description) {
		this._mapId = id;
		this._mapGroupId = groupId;
		this._mapType = type;
		this.unitLimitType = unitLimitType;
		this.unitLimit = unitLimit;
		this.isShowPet = isShowPet;
		this._name = name;
		this._desc = description;
		// this._mapData = data;
		this.mapType = KMapTypeEnum.副本地图;
		this.duplicateType = KMapDuplicateTypeEnum.getEnum(duplicateType);
		// this.registerPlayerRoleJoinLeaveListener((PlayerRoleJoinLeaveMapEventListener)
		// PlayerRoleServiceModuleFactory.getPlayerRoleServiceModule());
		this.collisionDatas = new HashMap<Integer, CollisionEventObjectData>();
		this.bornPointDatas = new HashMap<Integer, KDuplicateMapBornPoint>();
	}

	protected void init(String flashXmlPath, KGameExcelRow dataRow)
			throws KGameServerException {
		try {
			_LOGGER.info("！！！读取地图ID为" + getMapId() + "的地图XML数据！！！");

			// _exitsIfExist = new boolean[] { false, false, false, false };

			// _exitsEventData = new GameMapExitsEventData[4];

			Document doc = XmlUtil.openXml(flashXmlPath);
			if (doc == null) {
				throw new KGameServerException(
						"加载主城配置表的地图xml数据错误，不存在此路径xml文件，值=" + flashXmlPath
								+ "，Row=" + dataRow.getIndexInFile());
			} else {
				Element root = doc.getRootElement();

				// 读取NPC位置
				List<Element> mapObjEList = root.getChildren("gameObject");

				for (Element mapObjE : mapObjEList) {

					int objType = Integer.parseInt(mapObjE
							.getAttributeValue("type"));
					if (objType == KMapEntityTypeEnum.ENTITY_TYPE_NPC.entityType) {
						initNpcEntityAndPutIntoMap(mapObjE,
								dataRow.getIndexInFile());
					} else if (objType == KMapEntityTypeEnum.ENTITY_TYPE_MAP_EXIT.entityType) {
						initExitsEntityAndPutIntoMap(mapObjE, dataRow,
								dataRow.getIndexInFile(),
								GameMapExitsEventData.EXIT_EVENT_TYPE_JUMP_MAP);
					} else if (objType == KMapEntityTypeEnum.ENTITY_TYPE_BORN_POINT.entityType) {
						initBornPointAndPutIntoMap(mapObjE);
					} else if (objType == KMapEntityTypeEnum.ENTITY_TYPE_LEVEL_EXIT.entityType) {
						initExitsEntityAndPutIntoMap(
								mapObjE,
								dataRow,
								dataRow.getIndexInFile(),
								GameMapExitsEventData.EXIT_EVENT_TYPE_GAMELEVELS);
					} else if (objType == KMapEntityTypeEnum.ENTITY_TYPE_LEVEL_EXIT.entityType) {

					} else if (objType == KMapEntityTypeEnum.ENTITY_TYPE_MONSTER.entityType) {
						initCollisionEventData(mapObjE);
					}

				}

			}
		} catch (Exception e) {
			throw new KGameServerException("读取地图id为：" + getMapId()
					+ "的地图XML数据发生错误！", e);
		}
	}

	/**
	 * 初始化NPC地图实体,并将其放入地图
	 */
	private void initNpcEntityAndPutIntoMap(Element e, int rowIndex)
			throws Exception {
		// 读取xml的npc信息，并初始化NpcOrExitsMapViewInfo的数据
		int npcId = Integer.parseInt(e.getAttributeValue("id"));
		float[] result = getPosition(e);
		int instance_id = Integer.parseInt(e.getAttributeValue("instanceId"));

		// 根据NPC ID获取NPC信息
		KNPCTemplate npc = KSupportFactory.getNpcModuleSupport()
				.getNPCTemplate((int) npcId);
		if (npc == null) {
			// throw new KGameServerException("读取地图的NPCID为：" + npcId
			// + "的NPC模版实体对象为null！！，Row=" + rowIndex);
		}

		if (npc != null) {
			npc.instanceId = instance_id;
			KGameMapEntity npcEntity = new KGameMapEntity(
					KMapEntityTypeEnum.ENTITY_TYPE_NPC, npcId, npc);
			npcEntity.setCoordinate(new KCoordinate(result[0], result[1]));
			npcEntity.setEditorInstanceId(instance_id);

			try {
				putEntityTo(npcEntity, result[0], result[1]);
			} catch (KGameMapModuleException ex) {
				_LOGGER.error("初始化NPC发生错误，NpcID：" + npcId, ex);
				throw ex;
			}
		}
	}

	/**
	 * 初始化出入口地图实体及其事件数据,并将其放入地图
	 */
	private void initExitsEntityAndPutIntoMap(Element e,
			KGameExcelRow excelDataRow, int rowIndex, int eventType)
			throws Exception {
		// 读取xml的npc信息，并初始化NpcOrExitsMapViewInfo的数据
		int exitId = Integer.parseInt(e.getAttributeValue("id"));
		int targetId = excelDataRow.getInt("targetId" + exitId);
		if (targetId == 0) {
			throw new KGameServerException(
					"读取地图的出入口信息错误，xml与excel表数据不匹配，targetId" + exitId
							+ "的目标ID为0，Row=" + rowIndex);
		}

		float[] result = getPosition(e);

		int instance_id = Integer.parseInt(e.getAttributeValue("instanceId"));

		// 处理地图出入口事件数据

		GameMapExitsEventData eventData = new GameMapExitsEventData(exitId,
				eventType, result[0], result[1]);
		eventData.srcMapId = getMapId();

		eventData.targetId = targetId;
		eventData.setNpcOrExitInstanceId(instance_id);
		KMapEntityTypeEnum entityType;
		if (eventType == GameMapExitsEventData.EXIT_EVENT_TYPE_JUMP_MAP) {
			entityType = KMapEntityTypeEnum.ENTITY_TYPE_MAP_EXIT;
		} else {
			entityType = KMapEntityTypeEnum.ENTITY_TYPE_LEVEL_EXIT;
		}
		try {
			putEntity(eventData.getExitId(), eventData, entityType, result[0],
					result[1]);
		} catch (KGameMapModuleException ex) {
			_LOGGER.error("初始化副本出入口发生错误，exitID：" + eventData.getExitId()
					+ ",出口类型：" + eventType, ex);
			throw ex;
		}
	}

	/**
	 * 初始化出入口地图实体及其事件数据,并将其放入地图
	 */
	private void initBornPointAndPutIntoMap(Element e) throws Exception {
		float[] result = getPosition(e);
		int instance_id = Integer.parseInt(e.getAttributeValue("instanceId"));
		int exitsId = Integer.parseInt(e.getAttributeValue("id"));

		KDuplicateMapBornPoint bornPoint = new KDuplicateMapBornPoint(
				instance_id, exitsId, result[0], result[1]);

		bornPointDatas.put(instance_id, bornPoint);

		// KGameMapEntity bornPointEntity = new KGameMapEntity(
		// KMapEntityTypeEnum.ENTITY_TYPE_BORN_POINT,
		// bornPoint._objInstanceId, bornPoint);
		// try {
		// putEntityTo(bornPointEntity, result[0], result[1]);
		// } catch (KGameMapModuleException ex) {
		// _LOGGER.error("初始化副本出生点发生错误，InstanceId：" + instance_id + ",exitID："
		// + exitsId, ex);
		// throw ex;
		// }
	}

	private float[] getPosition(Element e) throws Exception {
		float[] result = new float[] { 0f, 0f };

		if ((e.getChild("transform").getChild("position")) != null) {
			result[0] = Float.parseFloat(e.getChild("transform")
					.getChild("position").getAttributeValue("x"));
			result[1] = Float.parseFloat(e.getChild("transform")
					.getChild("position").getAttributeValue("y"));
		}

		return result;
	}

	/**
	 * 初始化地图碰撞检测事件数据,并将其放入地图
	 */
	private void initCollisionEventData(Element e) throws Exception {
		float[] result = getPosition(e);
		int instance_id = Integer.parseInt(e.getAttributeValue("instanceId"));

		KCollisionEventData event = new KCollisionEventData(instance_id,
				result[0], result[1]);

		this.collisionDatas.put(instance_id, event);
	}

	/**
	 * 在同一个游戏世界中地图的ID是唯一的<br>
	 * 
	 * @return 地图ID<br>
	 */
	public int getMapId() {
		return _mapId;
	}

	public int getDuplicateId() {
		return _duplicateId;
	}

	/**
	 * 获取副本地图组Id
	 */
	public int getMapGroupId() {
		return _mapGroupId;
	}

	/**
	 * 在同一个游戏世界中地图名称可以有相同<br>
	 * 
	 * @return 游戏名称<br>
	 */
	public String getName() {
		return _name;
	}

	/**
	 * 设置地图名称<br>
	 * 
	 * @param name
	 *            新的地图名称<br>
	 */
	public void setName(String name) {
		this._name = name;
	}

	/**
	 * 地图类型可根据具体游戏再定使用方法，甚至可以用来表达多种属性<br>
	 * 
	 * @return 地图的类型<br>
	 */
	public int getType() {
		return _mapType;
	}

	public KMapTypeEnum getMapType() {
		return mapType;
	}

	@Override
	public KMapDuplicateTypeEnum getDuplicateTypeEnum() {
		return this.duplicateType;
	}

	// /**
	// * 表达“是否能产生副本地图？”。也就是本地图是否是具体产生副本地图能力的母体（母鸡）。<br>
	// * ("capable of being duplicated." OR "the quality of being reproducible."
	// )
	// * @return true－能生地图仔，false－不能（两种可能：1是本来就不能生；2是子地图）
	// */
	// public boolean isDuplicable();
	//
	// /**
	// * 表达“是否产生出来的副本地图？”。也就是本地图是否是由母体地图生出来的（小鸡）。
	// * @return ture－是生出来的副本地图（子地图），false－不是子地图
	// */
	// public boolean isDuplication();

	/**
	 * 地图描述文字，可以用来告诉玩家关于本地图的描述，也可以作为一个脚本字符串。<br>
	 * 
	 * @return 地图描述文字<br>
	 */
	public String getDescription() {
		return _desc;
	}

	/**
	 * 设置地图描述信息。<br>
	 * 
	 * @param description
	 *            描述<br>
	 */
	public void setDescription(String description) {
		this._desc = description;
	}

	/**
	 * 获取地图客户端资源ID
	 * 
	 * @return
	 */
	public int getMapResId() {
		return _mapResId;
	}

	/**
	 * 设置地图客户端资源ID
	 * 
	 * @param _mapResId
	 */
	public void setMapResId(int _mapResId) {
		this._mapResId = _mapResId;
	}

	/**
	 * 如果不是开启状态，很多操作是无法执行的（这时会抛出{@link KGameMapModuleException}）<br>
	 * 
	 * @return 地图是否开启？<br>
	 */
	public boolean isOpen() {
		return _isOpen;
	}

	/**
	 * 地图特效资源ID
	 * 
	 * @return
	 */
	public int getSpecialEfficiencyId() {
		return specialEfficiencyId;
	}

	/**
	 * 设置地图特效资源ID
	 * 
	 * @param specialEfficiencyId
	 */
	public void setSpecialEfficiencyId(int specialEfficiencyId) {
		this.specialEfficiencyId = specialEfficiencyId;
	}

	/**
	 * 地图特效类型
	 * 
	 * @return
	 */
	public byte getSpecialEfficiencyType() {
		return specialEfficiencyType;
	}

	/**
	 * 设置地图特效类型
	 * 
	 * @return
	 */
	public void setSpecialEfficiencyType(byte specialEfficiencyType) {
		this.specialEfficiencyType = specialEfficiencyType;
	}

	/**
	 * 地图粒子效果资源ID
	 * 
	 * @return
	 */
	public int getParticleResId() {
		return particleResId;
	}

	/**
	 * 设置地图粒子效果资源ID
	 * 
	 * @return
	 */
	public void setParticleResId(int particleResId) {
		this.particleResId = particleResId;
	}

	public int getJoinMapEfficiencyId() {
		return joinMapEfficiencyId;
	}

	public void setJoinMapEfficiencyId(int joinMapEfficiencyId) {
		this.joinMapEfficiencyId = joinMapEfficiencyId;
	}

	/**
	 * 获取地图背景资源音乐
	 * 
	 * @return
	 */
	public int getBgMusicResId() {
		return bgMusicResId;
	}

	/**
	 * 设置地图背景资源音乐
	 * 
	 * @param bgMusicResId
	 */
	public void setBgMusicResId(int bgMusicResId) {
		this.bgMusicResId = bgMusicResId;
	}

	public String getMapDataFileName() {
		return _mapDataFileName;
	}

	public void setMapDataFileName(String mapDataFileName) {
		this._mapDataFileName = mapDataFileName;
	}

	// /**
	// * 地图数据，一般从地图编辑器编辑所得，根据细节数据来部署地图的。<br>
	// *
	// * @return 对应的地图数据-{@link KGameMapData}
	// */
	// public KGameMapData getGameMapData() {
	// // lockGameMapData.lock();
	// // try {
	// return _mapData;
	// // } finally {
	// // lockGameMapData.unlock();
	// // }
	// }
	//
	// /**
	// * 设置地图数据描述类。
	// *
	// * @param data
	// * {@link KGameMapData}
	// */
	// public void setGameMapData(KGameMapData data) {
	// // lockGameMapData.lock();
	// // try {
	// this._mapData = data;
	// // } finally {
	// // lockGameMapData.unlock();
	// // }
	// }

	@Override
	public List<KDuplicateMapBornPoint> getAllBornPointEntity() {
		List<KDuplicateMapBornPoint> list = new ArrayList<KDuplicateMapBornPoint>();
		// List<KGameMapEntity> eList = this
		// .getEntities(KMapEntityTypeEnum.ENTITY_TYPE_BORN_POINT);
		// for (KGameMapEntity entity : eList) {
		// list.add((KDuplicateMapBornPoint) (entity.getSourceObject()));
		// }
		list.addAll(bornPointDatas.values());
		Collections.sort(list);

		return list;
	}

	/**
	 * 把一个地图实体（如角色、镖车等等）放入地图。不一定都是成功的。<br>
	 * 
	 * @param entity
	 *            实体<br>
	 * @return 成功放入地图后的实体坐标，失败则null。<br>
	 * @throws GameMapServiceModuleException
	 * <br>
	 */
	public KCoordinate putEntity(KGameMapEntity entity)
			throws KGameMapModuleException {
		if (entity == null) {
			throw new IllegalArgumentException(
					"#putEntity(entity).entity can not be null.");
		}
		return putEntityTo(entity, entity.getCoordinate());
	}

	/**
	 * 把一个地图实体（如NPC、角色、怪物、物品等等）放入地图某个坐标。<br>
	 * 
	 * @param entity
	 *            实体<br>
	 * @param coordinate
	 *            目标坐标<br>
	 * @return 成功放入地图后的实体坐标，失败则null。<br>
	 * @throws GameMapServiceModuleException
	 * <br>
	 */
	public KCoordinate putEntityTo(KGameMapEntity entity, KCoordinate coordinate)
			throws KGameMapModuleException {
		if (entity == null) {
			throw new IllegalArgumentException(
					"#putEntityTo(entity,coordinate).entity can not be null.");
		}
		if (coordinate == null) {
			throw new IllegalArgumentException(
					"#putEntityTo(entity,coordinate).coordinate can not be null.");
		}
		return putEntityAndGetCoordinate(entity, coordinate.getX(),
				coordinate.getY());
	}

	/**
	 * 把一个地图实体（如NPC、角色、怪物、物品等等）放入地图某个坐标。<br>
	 * 
	 * @param entity
	 *            实体<br>
	 * @param x
	 *            目标坐标X
	 * @param y
	 *            目标坐标Y
	 * @return 成功放入地图后的实体坐标，失败则null。<br>
	 * @throws GameMapServiceModuleException
	 *             ;
	 */
	public KCoordinate putEntityAndGetCoordinate(KGameMapEntity entity,
			float x, float y) throws KGameMapModuleException {
		if (entity == null) {
			throw new IllegalArgumentException(
					"#putEntityTo(entity,x,y).entity can not be null.");
		}
		// if (_mapData == null) {
		// throw new KGameMapModuleException("#No KGameMapData in KGameMap("
		// + this.getMapId() + ").");
		// }

		// 检测可否站立
		KCoordinate end = new KCoordinate(x, y);
		if (end != null) {
			// 更新Entity的坐标
			entity.getCoordinate().setX(end.getX());
			entity.getCoordinate().setY(end.getY());

			// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			// ！！！针对角色类型的GameMapEntity进入地图时的特殊处理！！！
			if (entity.isPlayerRoleType()) {
				KRole role = (KRole) entity.getSourceObject();
				if (role.isOnline()) {

					// 私有列表的维护
					if (this.unitLimit > 0) {
						PlayerRolePrivateEntityList privateList = (PlayerRolePrivateEntityList) entity
								.getPrivateEntityList();
						// 如果第一次进入则创建一个私有列表
						if (privateList == null) {
							privateList = new PlayerRolePrivateEntityList(
									entity, this.unitLimit,
									this.unitLimitIsConcurrent,
									this.unitLimitIsPriority);
							entity.setPrivateEntityList(privateList);
						}
						// 加入地图时的处理
						privateList.dealOnJoinGameMap(this);
					}

					// 加入地图时的通知
					// joinNotify((KRole) entity.getSourceObject());

					if (chm_GameMapEntities.put(
							new GameMapEntityCacheKey(
									entity.getEntityType().entityType, entity
											.getSourceObjectID()), entity) == null) {
						_entitySize++;
					}
				}
			} else {
				if (chm_GameMapEntities.put(
						new GameMapEntityCacheKey(
								entity.getEntityType().entityType, entity
										.getSourceObjectID()), entity) == null) {
					_entitySize++;
				}
			}

			// 放入地图Entity缓存
			// lockGameMapEntities.writeLock().lock();
			// try {

			// } finally {
			// lockGameMapEntities.writeLock().unlock();
			// }
		}
		return end;
	}

	/**
	 * 移除一个地图对象实体。<br>
	 * 
	 * @param entity
	 *            将被移除的实体。<br>
	 * @return 刚被移除的对象<br>
	 */
	public KGameMapEntity removeEntity(KGameMapEntity entity) {
		KGameMapEntity old = null;
		if (entity != null) {
			old = removeEntity(entity.getEntityType(),
					entity.getSourceObjectID());
		}
		return old;
	}

	/**
	 * 移除一个地图对象实体。<br>
	 * 
	 * @param entityType
	 *            类型<br>
	 * @param entityObjectID
	 *            对象ID<br>
	 * @return 刚被移除的对象<br>
	 */
	public KGameMapEntity removeEntity(KMapEntityTypeEnum entityType,
			long entityObjectID) {

		KGameMapEntity theEntityJustLeaved = chm_GameMapEntities
				.remove(new GameMapEntityCacheKey(entityType.entityType,
						entityObjectID));
		if (theEntityJustLeaved != null) {
			_entitySize--;
			_entitySize = _entitySize < 0 ? 0 : _entitySize;
			if (theEntityJustLeaved.isPlayerRoleType()) {

				// 私有列表的维护>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
				if (this.unitLimit > 0) {
					PlayerRolePrivateEntityList privateList = (PlayerRolePrivateEntityList) theEntityJustLeaved
							.getPrivateEntityList();
					if (privateList != null) {
						privateList.dealOnLeaveGameMap(this);
						// System.out.println("privateList holder size: "+privateList.sizeOfMyHolder()+",mylist size:"+privateList.sizeOfMyList());
					}
				}

				// 离开通知>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
				// leaveNotify((KRole) theEntityJustLeaved.getSourceObject());
			}
		}
		return theEntityJustLeaved;

	}

	/**
	 * 移除地图上某种类型的所有实体。<br>
	 * 
	 * @param entityType
	 *            类型<br>
	 */
	public void removeEntities(KMapEntityTypeEnum entityType) {
		for (KGameMapEntity gameMapEntity : chm_GameMapEntities.values()) {
			if (gameMapEntity != null
					&& gameMapEntity.getEntityType() == entityType) {
				this.removeEntity(gameMapEntity.getEntityType(),
						gameMapEntity.getSourceObjectID());
			}
		}
	}

	/**
	 * 移除地图上的所有实体。<br>
	 */
	public void removeAllEntities() {
		for (KGameMapEntity gameMapEntity : chm_GameMapEntities.values()) {
			if (gameMapEntity != null) {
				this.removeEntity(gameMapEntity.getEntityType(),
						gameMapEntity.getSourceObjectID());
			}
		}
	}

	/**
	 * 根据类型和ID取得某个地图实体。<br>
	 * 
	 * @param entityType
	 *            类型<br>
	 * @param entityObjectID
	 *            对象ID<br>
	 * @return 地图实体<br>
	 */
	public KGameMapEntity getEntity(KMapEntityTypeEnum entityType,
			long entityObjectID) {
		return chm_GameMapEntities.get(new GameMapEntityCacheKey(
				entityType.entityType, entityObjectID));
	}

	/**
	 * 取得某类型的所有地图实体。<br>
	 * 
	 * @param entityType
	 *            类型<br>
	 * @return 实体集合<br>
	 */
	public List<KGameMapEntity> getEntities(KMapEntityTypeEnum entityType) {
		List<KGameMapEntity> list = new ArrayList<KGameMapEntity>(
				_entitySize < 0 ? 0 : _entitySize);

		for (KGameMapEntity ge : chm_GameMapEntities.values()) {

			if (ge != null && ge.getEntityType() == entityType) {
				list.add(ge);
			}
		}

		return list;
	}

	/**
	 * 取得某类型的所有地图实体。<br>
	 * 
	 * @param entityType
	 *            类型<br>
	 * @return 实体集合<br>
	 */
	public List<Long> getEntitieIds(KMapEntityTypeEnum entityType) {
		List<Long> list = new ArrayList<Long>(_entitySize < 0 ? 0 : _entitySize);

		for (KGameMapEntity ge : chm_GameMapEntities.values()) {

			if (ge != null && ge.getEntityType() == entityType) {
				list.add(ge.getSourceObjectID());
			}
		}

		return list;
	}

	public ConcurrentHashMap<GameMapEntityCacheKey, KGameMapEntity> getEntitysMap() {
		return chm_GameMapEntities;
	}

	/**
	 * 取得所有地图实体。<br>
	 * 
	 * @return 实体集合<br>
	 */
	public List<KGameMapEntity> getEntities() {
		List<KGameMapEntity> list = new ArrayList<KGameMapEntity>(
				_entitySize < 0 ? 0 : _entitySize);

		list.addAll(chm_GameMapEntities.values());

		return list;
	}

	/**
	 * 取得除了自己角色实体外的其他所有地图角色实体。<br>
	 * 
	 * @param myRoleId
	 *            自己角色的ID
	 * @return
	 */
	public List<KGameMapEntity> getOtherRoleEntities(long myRoleId) {
		List<KGameMapEntity> list = new ArrayList<KGameMapEntity>(
				_entitySize < 0 ? 0 : _entitySize);

		for (KGameMapEntity ge : chm_GameMapEntities.values()) {

			if (ge != null
					&& ge.getEntityType() == KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE
					&& ge.getSourceObjectID() != myRoleId) {
				list.add(ge);
			}
		}

		return list;
	}

	/**
	 * 检测某个坐标为中心的矩形范围的当前的实体
	 * 
	 * @param coordinate
	 *            坐标
	 * @param w
	 *            检测坐标左右延伸的宽度
	 * @param h
	 *            检测坐标上下延伸的宽度
	 * @return 该坐标为中心的矩形范围上的实体集合
	 */
	public List<KGameMapEntity> checkEntities(KCoordinate coordinate, float w,
			float h) {

		List<KGameMapEntity> list = new ArrayList<KGameMapEntity>(
				_entitySize < 0 ? 0 : _entitySize);
		if (coordinate == null) {
			return list;
		}
		float x = coordinate.getX();
		float y = coordinate.getY();

		for (KGameMapEntity gameMapEntity : chm_GameMapEntities.values()) {

			KCoordinate coor;
			if (gameMapEntity != null
					&& (coor = gameMapEntity.getCoordinate()) != null) {
				if ((coor.getX() >= x && coor.getX() <= x + w)
						&& (coor.getY() >= y && coor.getY() <= y + h)) {
					list.add(gameMapEntity);
				}
			}
		}

		return list;
	}

	/**
	 * 检测某个坐标为中心的矩形范围的当前的实体
	 * 
	 * @param x
	 *            坐标x
	 * @param y
	 *            坐标y
	 * @param w
	 *            检测坐标左右延伸的宽度
	 * @param h
	 *            检测坐标上下延伸的宽度
	 * @return 该坐标为中心的矩形范围上的实体集合
	 */
	public List<KGameMapEntity> checkEntities(float x, float y, float w, float h) {
		List<KGameMapEntity> list = new ArrayList<KGameMapEntity>(
				_entitySize < 0 ? 0 : _entitySize);

		for (KGameMapEntity gameMapEntity : chm_GameMapEntities.values()) {

			KCoordinate coor;
			if (gameMapEntity != null
					&& (coor = gameMapEntity.getCoordinate()) != null) {
				if ((coor.getX() >= x && coor.getX() <= x + w)
						&& (coor.getY() >= y && coor.getY() <= y + h)) {
					list.add(gameMapEntity);
				}
			}
		}

		return list;
	}

	/**
	 * 接口：在地图中由远到近搜索一批Entities的搜索规则
	 */
	public interface FromNearToFarEntitiesSearchRule {

		/**
		 * 搜索中心起点坐标
		 * 
		 * @return
		 */
		public KCoordinate centerCoordinate();

		/**
		 * 搜索中心起点最大宽度半径范围（若要全地图搜索可返回一个很大的值或-1）
		 * 
		 * @return
		 */
		public int upperLimitWidth();

		/**
		 * 搜索中心起点最大高度半径范围（若要全地图搜索可返回一个很大的值或-1）
		 * 
		 * @return
		 */
		public int upperLimitHeight();

		/**
		 * 某种类型的Entity是否在此次搜索条件内
		 * 
		 * @param entityType
		 *            实体类型
		 * @return
		 */
		public boolean isMatchEntityType(int entityType);

		/**
		 * 此次搜索的实体上限（若要全地图搜索可返回一个很大的值或-1）
		 * 
		 * @return
		 */
		public int upperLimitEntityCount();
	}

	// /**
	// * 高级搜索：传入搜索规则由远到近地搜索一批符合条件的Entities
	// * @param rule 搜索规则
	// * @return 符合条件的Entity集合
	// * @see FromNearToFarEntitiesSearchRule
	// */
	// public List<KGameMapEntity>
	// searchEntitiesFromNearToFar(FromNearToFarEntitiesSearchRule rule);

	/**
	 * 取得该地图上实体的数量<br>
	 * 
	 * @return 数量<br>
	 */
	public int getEntitiesSize() {
		return _entitySize;
	}

	/**
	 * 取得该地图上某种类型的实体数量<br>
	 * 
	 * @param entityType
	 *            类型<br>
	 * @return 数量<br>
	 */
	public int getEntitiesSize(int entityType) {
		int count = 0;
		// lockGameMapEntities.readLock().lock();
		// try {
		for (Iterator<GameMapEntityCacheKey> it = chm_GameMapEntities.keySet()
				.iterator(); it.hasNext();) {
			GameMapEntityCacheKey entityKey = it.next();
			if (entityKey != null && entityKey.type == entityType) {
				count++;
			}
		}
		// } finally {
		// lockGameMapEntities.readLock().unlock();
		// }
		return count;
	}

	// /**
	// * 将地图事件脚本绑到本地图。<br>
	// * @param script 脚本<br>
	// */
	// public void bindScriptTo(GameMapScript script);
	//
	//
	// /**
	// * 将地图事件脚本绑到本地图某个坐标上。<br>
	// * @param script 脚本<br>
	// * @param x 坐标X<br>
	// * @param y 坐标Y<br>
	// * @param z 坐标Z<br>
	// */
	// public void bindScriptTo(GameMapScript script, int x, int y);
	//
	// /**
	// * 移除一个事件脚本<br>
	// * @param scriptId 脚本ID
	// * @return 刚被移除的脚本
	// */
	// public GameMapScript removeScript(long scriptId);
	//
	// /**
	// * 取得绑定了地图的事件<br>
	// * @return 地图事件集合<br>
	// */
	// public List<GameMapScript> checkMapBindingScripts();
	//
	//
	// /**
	// * 取得绑定了某个地图格子坐标的事件<br>
	// * @param x 坐标X<br>
	// * @param y 坐标Y<br>
	// * @param z 坐标Z<br>
	// * @return 地图事件集合<br>
	// */
	// public List<GameMapScript> checkCoordinateBindingScripts(int x, int y,
	// int z);

	// final void registerPlayerRoleJoinLeaveListener(
	// PlayerRoleJoinLeaveMapEventListener lis) {
	// joinleavelisteners.add(lis);
	// }
	//
	// private void joinNotify(KRole role) {
	// for (PlayerRoleJoinLeaveMapEventListener
	// playerRoleJoinLeaveMapEventListener : joinleavelisteners) {
	// if (playerRoleJoinLeaveMapEventListener != null) {
	// playerRoleJoinLeaveMapEventListener.notifyPlayerRoleJoinedMap(
	// role, this);
	// }
	// }
	// }
	//
	// private void leaveNotify(KRole role) {
	// for (PlayerRoleJoinLeaveMapEventListener
	// playerRoleJoinLeaveMapEventListener : joinleavelisteners) {
	// if (playerRoleJoinLeaveMapEventListener != null) {
	// playerRoleJoinLeaveMapEventListener.notifyPlayerRoleLeavedMap(
	// role, this);
	// }
	// }
	// }

	/**
	 * 根据Npc模版ID检测这个NPC是否位于本地图
	 * 
	 * @param npcTemplateId
	 * @return
	 */
	public boolean isNpcEntityInMap(int npcTemplateId) {
		if (chm_GameMapEntities.containsKey(new GameMapEntityCacheKey(
				KMapEntityTypeEnum.ENTITY_TYPE_NPC.entityType, npcTemplateId))) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 根据角色ID检测这个角色是否位于本地图
	 * 
	 * @param npcTemplateId
	 * @return
	 */
	public boolean isRoleEntityInMap(long roleId) {
		if (chm_GameMapEntities.containsKey(new GameMapEntityCacheKey(
				KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE.entityType, roleId))) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 地图消失时清楚地图数据
	 */
	public void dispose() {
		_name = null;
		_desc = null;
		// _mapData = null;
		chm_GameMapEntities.clear();
		this._entitySize = 0;
		// joinleavelisteners.clear();
	}

	@Override
	public Object clone() {
		KDuplicateMapImpl o = null;
		try {
			o = (KDuplicateMapImpl) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		o._duplicateId = KDuplicateMapManager.getDuplcateMapIdGenerator()
				.nextDuplcateMapId();
		o.chm_GameMapEntities = new ConcurrentHashMap<GameMapEntityCacheKey, KGameMapEntity>();
		o.chm_GameMapEntities.putAll(chm_GameMapEntities);
		o.collisionDatas = new HashMap<Integer, CollisionEventObjectData>();
		for (CollisionEventObjectData data : this.collisionDatas.values()) {
			KCollisionEventData newData = new KCollisionEventData();
			newData.setX(data.getX());
			newData.setY(data.getY());
			newData.setMapInstanceId(data.getMapInstanceId());
			o.collisionDatas.put(newData.getMapInstanceId(), newData);
		}
		return o;
	}

	@Override
	public <T> KGameMapEntity putEntity(long sourceId, T sourceObject,
			KMapEntityTypeEnum entityType, float x, float y)
			throws KGameMapModuleException {
		if (sourceObject == null) {
			throw new IllegalArgumentException(
					"#putEntityTo(sourceId,sourceObject,entityType,x,y).entity can not be null.");
		}
		// if (_mapData == null) {
		// throw new KGameMapModuleException("#No KGameMapData in KGameMap("
		// + this.getMapId() + ").");
		// }

		// 检测可否站立
		KCoordinate end = new KCoordinate(x, y);
		KGameMapEntity entity = new KGameMapEntity(entityType, sourceId,
				sourceObject);

		if (end != null) {
			// 更新Entity的坐标
			entity.getCoordinate().setX(end.getX());
			entity.getCoordinate().setY(end.getY());

			// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			// ！！！针对角色类型的GameMapEntity进入地图时的特殊处理！！！
			if (entity.isPlayerRoleType() && (sourceObject instanceof KRole)) {
				KRole role = (KRole) entity.getSourceObject();
				if (role.isOnline()) {

					// 私有列表的维护
					if (this.unitLimit > 0) {
						PlayerRolePrivateEntityList privateList = (PlayerRolePrivateEntityList) entity
								.getPrivateEntityList();
						// 如果第一次进入则创建一个私有列表
						if (privateList == null) {
							privateList = new PlayerRolePrivateEntityList(
									entity, this.unitLimit,
									this.unitLimitIsConcurrent,
									this.unitLimitIsPriority);
							entity.setPrivateEntityList(privateList);
						}
						// 加入地图时的处理
						privateList.dealOnJoinGameMap(this);
					}

					// 加入地图时的通知
					// joinNotify((KRole) entity.getSourceObject());

					if (chm_GameMapEntities.put(
							new GameMapEntityCacheKey(
									entity.getEntityType().entityType, entity
											.getSourceObjectID()), entity) == null) {
						_entitySize++;
					}
				}
			} else {
				if (chm_GameMapEntities.put(
						new GameMapEntityCacheKey(
								entity.getEntityType().entityType, entity
										.getSourceObjectID()), entity) == null) {
					_entitySize++;
				}
			}
		}
		return entity;
	}

	@Override
	public KGameMapEntity putEntityTo(KGameMapEntity entity, float x, float y)
			throws KGameMapModuleException {
		if (entity == null) {
			throw new IllegalArgumentException(
					"#putEntityTo(sourceId,sourceObject,entityType,x,y).entity can not be null.");
		}
		// if (_mapData == null) {
		// throw new KGameMapModuleException("#No KGameMapData in KGameMap("
		// + this.getMapId() + ").");
		// }

		// 检测可否站立
		KCoordinate end = new KCoordinate(x, y);

		if (end != null) {
			// 更新Entity的坐标
			entity.getCoordinate().setX(end.getX());
			entity.getCoordinate().setY(end.getY());

			// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			// ！！！针对角色类型的GameMapEntity进入地图时的特殊处理！！！
			if (entity.isPlayerRoleType() && (entity.getSourceObject() != null)
					&& (entity.getSourceObject() instanceof KRole)) {
				KRole role = (KRole) entity.getSourceObject();
				if (role.isOnline()) {

					// 私有列表的维护
					if (this.unitLimit > 0) {
						PlayerRolePrivateEntityList privateList = (PlayerRolePrivateEntityList) entity
								.getPrivateEntityList();
						// 如果第一次进入则创建一个私有列表
						if (privateList == null) {
							privateList = new PlayerRolePrivateEntityList(
									entity, this.unitLimit,
									this.unitLimitIsConcurrent,
									this.unitLimitIsPriority);
							entity.setPrivateEntityList(privateList);
						}
						// 加入地图时的处理
						privateList.dealOnJoinGameMap(this);
					}

					// 加入地图时的通知
					// joinNotify((KRole) entity.getSourceObject());

					if (chm_GameMapEntities.put(
							new GameMapEntityCacheKey(
									entity.getEntityType().entityType, entity
											.getSourceObjectID()), entity) == null) {
						_entitySize++;
					}
				}
			} else {
				if (chm_GameMapEntities.put(
						new GameMapEntityCacheKey(
								entity.getEntityType().entityType, entity
										.getSourceObjectID()), entity) == null) {
					_entitySize++;
				}
			}
		}
		return entity;
	}

	@Override
	public List<CollisionEventObjectData> getAllCollisionEventObject() {
		List<CollisionEventObjectData> list = new ArrayList<CollisionEventObjectData>();

		// List<KGameMapEntity> entityList =
		// this.getEntities(KMapEntityTypeEnum.ENTITY_TYPE_COLLISION_EVENT_OBJ);
		list.addAll(collisionDatas.values());
		Collections.sort(list);
		return list;
	}

	@Override
	public void setCollisionEventListener(CollisionEventListener listener) {
		this.collisionEventListener = listener;
	}

	public CollisionEventListener getCollisionEventListener() {
		return this.collisionEventListener;
	}

	@Override
	public CollisionEventObjectData removeCollisionEventObjectData(
			int mapInstanceId) {
		CollisionEventObjectData data = collisionDatas.remove(mapInstanceId);
		if (data != null) {
			// List<KGameMapEntity> roleList =
			// getEntities(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE);
			// for (KGameMapEntity roleE : roleList) {
			// if (roleE != null) {
			// roleE.sendOtherEntityLeaveMapData(
			// KMapEntityTypeEnum.ENTITY_TYPE_MONSTER,
			// (long) (data.getMapInstanceId()), false, -1);
			// }
			// }

			CollisionEventDataStateChangeSyncTask task = new CollisionEventDataStateChangeSyncTask(
					this, mapInstanceId,
					CollisionEventDataStateChangeSyncTask.STATE_TYPE_LEAVE, -1,
					-1);
			KGame.getTimer().newTimeSignal(task, 0, TimeUnit.SECONDS);
		}
		return data;
	}

	@Override
	public CollisionEventObjectData putCollisionEventObjectData(
			int mapInstanceId, Object attachment) {
		if (collisionDatas.containsKey(mapInstanceId)) {
			return collisionDatas.get(mapInstanceId);
		} else {
			KDuplicateMap structMap = KDuplicateMapManager
					.getStructMap(this._mapId);
			CollisionEventObjectData structData = structMap
					.getCollisionEventObject(mapInstanceId);
			if (structData != null) {
				CollisionEventObjectData data = new KCollisionEventData(
						mapInstanceId, structData.getX(), structData.getY());
				data.setAttachment(attachment);
				this.collisionDatas.put(data.getMapInstanceId(), data);
				// List<KGameMapEntity> roleList =
				// getEntities(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE);
				// for (KGameMapEntity roleE : roleList) {
				// if (roleE != null) {
				// roleE.sendOtherEntityLeaveMapData(
				// KMapEntityTypeEnum.ENTITY_TYPE_MONSTER,
				// (long) (data.getMapInstanceId()), false, -1);
				// }
				// }
				CollisionEventDataStateChangeSyncTask task = new CollisionEventDataStateChangeSyncTask(
						this, mapInstanceId,
						CollisionEventDataStateChangeSyncTask.STATE_TYPE_BORN,
						data.getX(), data.getY());
				KGame.getTimer().newTimeSignal(task, 0, TimeUnit.SECONDS);

				return data;
			}
		}
		return null;
	}

	@Override
	public CollisionEventObjectData getCollisionEventObject(int mapInstanceId) {

		return collisionDatas.get(mapInstanceId);
	}

	public static void main(String[] args) {
		KDuplicateMapImpl sMap = new KDuplicateMapImpl(1, 1,
				KMapTypeEnum.副本地图.type,
				KMapDuplicateTypeEnum.世界BOSS副本地图类型.type,
				UNIT_LIMIT_TYPE_UNLIMIT, 0, true,"aaa", "ccc");
		KGameMapEntity<AtomicInteger> e1 = new KGameMapEntity<AtomicInteger>(
				KMapEntityTypeEnum.ENTITY_TYPE_OBSTRUCTION, 10000l,
				new AtomicInteger(10000));
		KGameMapEntity<AtomicInteger> e2 = new KGameMapEntity<AtomicInteger>(
				KMapEntityTypeEnum.ENTITY_TYPE_OBSTRUCTION, 20000l,
				new AtomicInteger(20000));
		KGameMapEntity<AtomicInteger> e3 = new KGameMapEntity<AtomicInteger>(
				KMapEntityTypeEnum.ENTITY_TYPE_OBSTRUCTION, 30000l,
				new AtomicInteger(30000));
		try {
			sMap.putEntityAndGetCoordinate(e1, 1f, 2f);
			sMap.putEntityAndGetCoordinate(e2, 10f, 20f);

			sMap.collisionDatas.put(1, new KCollisionEventData(1, 10f, 10f));
		} catch (KGameMapModuleException e) {
			e.printStackTrace();
		}

		KDuplicateMapImpl cMap = (KDuplicateMapImpl) sMap.clone();

		// sMap.chm_GameMapEntities.clear();

		try {
			cMap.putEntityAndGetCoordinate(e3, 100f, 200f);
		} catch (KGameMapModuleException e) {
			e.printStackTrace();
		}

		System.out.println(cMap._name + "," + cMap._desc + ",type:"
				+ cMap.mapType);

		System.out.println(cMap.chm_GameMapEntities.size());

		for (KGameMapEntity entity : cMap.chm_GameMapEntities.values()) {
			System.out.println("cMap entity::" + entity.getSourceObject() + ","
					+ entity.getSourceObjectID());
		}

		for (KGameMapEntity entity : sMap.chm_GameMapEntities.values()) {
			System.out.println("sMap entity::" + entity.getSourceObject() + ","
					+ entity.getSourceObjectID());
		}

		System.out.println("sMap objE:" + sMap.collisionDatas.get(1));
		System.out.println("cMap objE:" + cMap.collisionDatas.get(1));

	}

}
