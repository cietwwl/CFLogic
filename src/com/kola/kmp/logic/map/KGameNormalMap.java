package com.kola.kmp.logic.map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.kola.kmp.logic.other.KMapTypeEnum;
import com.kola.kmp.logic.other.KMapEntityTypeEnum;
import com.kola.kmp.logic.role.KRole;

public class KGameNormalMap implements KMap<KGameMapEntity> {

	private KGameMapManager _manager;

	// －－地图上的实体
	private static final int INIT_MAX_ENTITIES = 2000;
	// private final ReadWriteLock lockGameMapEntities = new
	// ReentrantReadWriteLock();
	final ConcurrentHashMap<GameMapEntityCacheKey, KGameMapEntity> chm_GameMapEntities = new ConcurrentHashMap<GameMapEntityCacheKey, KGameMapEntity>(
			INIT_MAX_ENTITIES);

	// 角色进入离开地图事件监听器集合
	private final Set<PlayerRoleJoinLeaveMapEventListener> joinleavelisteners = new HashSet<PlayerRoleJoinLeaveMapEventListener>();

	private int _mapId;// 地图Id
	private String _name;// 地图名称
	private int _mapType;// 地图类型
	private String _desc;// 地图描述
	private int _areaId;// 地图所属区域ID
	private String _areaName;// 地图所属区域名称
	private String _mapDataFileName;// 地图数据文件名（不带后缀）
	private int _mapResId;// 地图客户端资源ID
	private int bgMusicResId;// 地图背景音乐
	private byte specialEfficiencyType;// 地图特效类型
	private int specialEfficiencyId;// 地图特效资源ID
	private int particleResId;// 地图粒子效果资源ID
	private int joinMapEfficiencyId;// 进城显示特效ID

	private boolean _isOpen;// 地图是否开放

	private KGameMapData _mapData;// 地图数据
	// private final Lock lockGameMapData = new ReentrantLock();

	private int _entitySize;// 地图实体数量

	public KMapTypeEnum mapType;

	protected KGameNormalMap(KGameMapManager manager, int id, int type,
			String name, String description) {
		this(manager, id, type, name, description, null);
	}

	protected KGameNormalMap(KGameMapManager manager, int id, int type,
			String name, String description, KGameMapData data) {
		this._manager = manager;
		this._mapId = id;
		this._mapType = type;
		this._name = name;
		this._desc = description;
		this._mapData = data;
		this.mapType = KMapTypeEnum.普通主城地图;
		// this.registerPlayerRoleJoinLeaveListener((PlayerRoleJoinLeaveMapEventListener)
		// PlayerRoleServiceModuleFactory.getPlayerRoleServiceModule());
	}

	/**
	 * 在同一个游戏世界中地图的ID是唯一的<br>
	 * 
	 * @return 地图ID<br>
	 */
	public int getMapId() {
		return _mapId;
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
	 * 设置地图所属地区ID
	 * 
	 * @return
	 */
	public int getAreaId() {
		return _areaId;
	}

	/**
	 * 设置地图所属地区ID
	 * 
	 * @return
	 */
	public void setAreaId(int areaId) {
		this._areaId = areaId;
	}

	/**
	 * 获取地图所属地区名称
	 * 
	 * @return
	 */
	public String getAreaName() {
		return _areaName;
	}

	/**
	 * 设置地图所属地区名称
	 * 
	 * @return
	 */
	public void setAreaName(String areaName) {
		this._areaName = areaName;
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

	/**
	 * 地图数据，一般从地图编辑器编辑所得，根据细节数据来部署地图的。<br>
	 * 
	 * @return 对应的地图数据-{@link KGameMapData}
	 */
	public KGameMapData getGameMapData() {
		// lockGameMapData.lock();
		// try {
		return _mapData;
		// } finally {
		// lockGameMapData.unlock();
		// }
	}

	/**
	 * 设置地图数据描述类。
	 * 
	 * @param data
	 *            {@link KGameMapData}
	 */
	public void setGameMapData(KGameMapData data) {
		// lockGameMapData.lock();
		// try {
		this._mapData = data;
		// } finally {
		// lockGameMapData.unlock();
		// }
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
		if (_mapData == null) {
			throw new KGameMapModuleException("#No KGameMapData in KGameMap("
					+ this.getMapId() + ").");
		}

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
					int limit = _manager.getUnitLimitForGameMapEntity();
					if (limit > 0) {
						PlayerRolePrivateEntityList privateList = (PlayerRolePrivateEntityList) entity
								.getPrivateEntityList();
						// 如果第一次进入则创建一个私有列表
						if (privateList == null) {
							privateList = new PlayerRolePrivateEntityList(
									entity, limit,
									_manager.unitLimitIsConcurrent,
									_manager.unitLimitIsPriority);
							entity.setPrivateEntityList(privateList);
						}
						// 加入地图时的处理
						privateList.dealOnJoinGameMap(this);
					}

					// 加入地图时的通知
					joinNotify((KRole) entity.getSourceObject());

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
				if (_manager.getUnitLimitForGameMapEntity() > 0) {
					PlayerRolePrivateEntityList privateList = (PlayerRolePrivateEntityList) theEntityJustLeaved
							.getPrivateEntityList();
					if (privateList != null) {
						privateList.dealOnLeaveGameMap(this);
						// System.out.println("privateList holder size: "+privateList.sizeOfMyHolder()+",mylist size:"+privateList.sizeOfMyList());
					}
				}

				// 离开通知>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
				leaveNotify((KRole) theEntityJustLeaved.getSourceObject());
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

	public Map<GameMapEntityCacheKey, KGameMapEntity> getEntitysMap() {
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
	public int getEntitiesSize(KMapEntityTypeEnum entityType) {
		int count = 0;
		// lockGameMapEntities.readLock().lock();
		// try {
		for (Iterator<GameMapEntityCacheKey> it = chm_GameMapEntities.keySet()
				.iterator(); it.hasNext();) {
			GameMapEntityCacheKey entityKey = it.next();
			if (entityKey != null && entityKey.type == entityType.entityType) {
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

	final void registerPlayerRoleJoinLeaveListener(
			PlayerRoleJoinLeaveMapEventListener lis) {
		joinleavelisteners.add(lis);
	}

	private void joinNotify(KRole role) {
		for (PlayerRoleJoinLeaveMapEventListener playerRoleJoinLeaveMapEventListener : joinleavelisteners) {
			if (playerRoleJoinLeaveMapEventListener != null) {
				playerRoleJoinLeaveMapEventListener.notifyPlayerRoleJoinedMap(
						role, this);
			}
		}
	}

	private void leaveNotify(KRole role) {
		for (PlayerRoleJoinLeaveMapEventListener playerRoleJoinLeaveMapEventListener : joinleavelisteners) {
			if (playerRoleJoinLeaveMapEventListener != null) {
				playerRoleJoinLeaveMapEventListener.notifyPlayerRoleLeavedMap(
						role, this);
			}
		}
	}

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
		_mapData = null;
		chm_GameMapEntities.clear();
		this._entitySize = 0;
		joinleavelisteners.clear();
		_manager = null;
	}

	@Override
	public <T> KGameMapEntity putEntity(long sourceId, T sourceObject,
			KMapEntityTypeEnum entityType, float x, float y)
			throws KGameMapModuleException {
		if (sourceObject == null) {
			throw new IllegalArgumentException(
					"#putEntityTo(sourceId,sourceObject,entityType,x,y).entity can not be null.");
		}
		if (_mapData == null) {
			throw new KGameMapModuleException("#No KGameMapData in KGameMap("
					+ this.getMapId() + ").");
		}

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
					int limit = _manager.getUnitLimitForGameMapEntity();
					if (limit > 0) {
						PlayerRolePrivateEntityList privateList = (PlayerRolePrivateEntityList) entity
								.getPrivateEntityList();
						// 如果第一次进入则创建一个私有列表
						if (privateList == null) {
							privateList = new PlayerRolePrivateEntityList(
									entity, limit,
									_manager.unitLimitIsConcurrent,
									_manager.unitLimitIsPriority);
							entity.setPrivateEntityList(privateList);
						}
						// 加入地图时的处理
						privateList.dealOnJoinGameMap(this);
					}

					// 加入地图时的通知
					joinNotify((KRole) entity.getSourceObject());

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
		if (_mapData == null) {
			throw new KGameMapModuleException("#No KGameMapData in KGameMap("
					+ this.getMapId() + ").");
		}

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
					int limit = _manager.getUnitLimitForGameMapEntity();
					if (limit > 0) {
						PlayerRolePrivateEntityList privateList = (PlayerRolePrivateEntityList) entity
								.getPrivateEntityList();
						// 如果第一次进入则创建一个私有列表
						if (privateList == null) {
							privateList = new PlayerRolePrivateEntityList(
									entity, limit,
									_manager.unitLimitIsConcurrent,
									_manager.unitLimitIsPriority);
							entity.setPrivateEntityList(privateList);
						}
						// 加入地图时的处理
						privateList.dealOnJoinGameMap(this);
					}

					// 加入地图时的通知
					joinNotify((KRole) entity.getSourceObject());

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

}
