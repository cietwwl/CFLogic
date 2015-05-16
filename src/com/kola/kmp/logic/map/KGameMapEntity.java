package com.kola.kmp.logic.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMapImpl;
import com.kola.kmp.logic.other.KItemQualityEnum;
import com.kola.kmp.logic.other.KMapEntityTypeEnum;
import com.kola.kmp.logic.pet.KPet;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.util.IRoleEquipShowData;

public class KGameMapEntity<T> implements KMapEntityData, Cloneable {
	// /**
	// * 表示类型为玩家角色的地图实体
	// */
	// public static final int ENTITY_TYPE_DEFAULT = 1<<1;
	// /**
	// * 表示类型为NPC的地图实体
	// */
	// public static final int ENTITY_TYPE_NPC = 1<<2;
	// /**
	// * 表示类型为默认地图出生点的地图实体
	// */
	// public static final int ENTITY_TYPE_BORN_POINT = 1<<4;
	// /**
	// * 表示类型为玩家角色的地图实体
	// */
	// public static final int ENTITY_TYPE_PLAYERROLE = 1<<6;
	// /**
	// * 表示类型为出口的地图实体
	// */
	// public static final int ENTITY_TYPE_MAP_EXIT = 1<<8;
	// /**
	// * 表示类型为出口的地图实体
	// */
	// public static final int ENTITY_TYPE_LEVEL_EXIT = 1<<9;
	// /**
	// * 表示类型为玩家角色的宠物实体（客户端需要，服务器上没有具体地图实体）
	// */
	// public static final int ENTITY_TYPE_PET = 1<<15;

	/**
	 * 地图实体显示的优先级别为称号级别
	 */
	public static final int ENTITY_PRIORITY_GAME_TITLE = 3;

	/**
	 * 地图实体显示的优先级别为好友级别
	 */
	public static final int ENTITY_PRIORITY_FRIEND = 2;
	/**
	 * 地图实体显示的优先级别为家族成员级别
	 */
	public static final int ENTITY_PRIORITY_GANG_MEMBER = 1;
	/**
	 * 地图实体显示的优先级别为陌生人级别
	 */
	public static final int ENTITY_PRIORITY_STRANGER = 16;

	/**
	 * <pre>
	 * ENTITY自定义数据类型的KEY，表示为NPC或地图出口位置信息
	 * </pre>
	 */
	public static final String ENTITY_ATT_TYPE_NPC_OR_EXITS_POSITION = "NPC_OR_EXITS_POSITION";

	// /**
	// * <pre>
	// * ENTITY自定义数据类型的KEY，表示为地图实体ID
	// * </pre>
	// */
	// public static final String ENTITY_ATT_TYPE_INSTANCE_ID = "INSTANCE_ID";

	// // 地图实体类型值
	// private int _entityType;
	// 地图实体类型枚举
	private KMapEntityTypeEnum _entityTypeEnum;
	// 是否为角色的实体类型
	private boolean _isPlayerRoleType;
	// 该物体的对象实例ID，例如playerId、itemId等等
	private long _sourceObjId;
	// 实体对应取得“源对象”。
	private T _sourceObj;
	// 实体在地图上的坐标
	private KCoordinate _entityCoordinate;
	// 角色在地图附近的需要给客户端显示的角色，有数量限制，按照一定的优先级规则排序
	private PlayerRolePrivateEntityList _privateEntityList;
	// Entity的自定义数据
	private Map<String, Object> entityAttributes = new HashMap<String, Object>(10);
	// 角色在地图中的周围其他地图实体状态改变的消息打包器,用于组装打包角色周围实体状态改变的消息
	private OtherEntityStateChangedMsgPacker msgPacker;

	private HashSet<Long> getAroundRoleIdSet = new HashSet<Long>();
	// 编辑器在地图中的实体id
	private int editorInstanceId;

	/**
	 * 
	 * @param type
	 *            地图实体类型
	 * @param srcObjId
	 *            该地图实体的对象实例ID
	 * @param srcObj
	 *            该地图实体的对象实例
	 */
	public KGameMapEntity(KMapEntityTypeEnum type, long srcObjId, T srcObj) {
		init(type, srcObjId, srcObj);
	}

	/**
	 * 初始化该实体
	 * 
	 * @param type
	 *            地图实体类型
	 * @param srcObjId
	 *            该地图实体的对象实例ID
	 * @param srcObj
	 *            该地图实体的对象实例
	 */
	public void init(KMapEntityTypeEnum type, long srcObjId, T srcObj) {
		// this._entityType = type;
		this._entityTypeEnum = type;
		if (type == KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE) {
			this._isPlayerRoleType = true;
		} else {
			this._isPlayerRoleType = false;
		}
		this._sourceObjId = srcObjId;
		this._sourceObj = srcObj;
		this._entityCoordinate = new KCoordinate(0, 0);
		if (_isPlayerRoleType) {
			msgPacker = new OtherEntityStateChangedMsgPacker(srcObjId);
		}
	}

	// /**
	// * 取得该物体的类型
	// *
	// * @return 类型
	// */
	// public int getEntityType() {
	// return _entityType;
	// }

	/**
	 * 取得该物体的类型枚举
	 * 
	 * @return 类型
	 */
	public KMapEntityTypeEnum getEntityType() {
		return _entityTypeEnum;
	}

	/**
	 * 判断是否角色类型的Entity
	 * 
	 * @return
	 */
	public boolean isPlayerRoleType() {
		return _isPlayerRoleType;
	}

	/**
	 * 取得该物体的对象实例ID，例如playerId、itemId等等
	 * 
	 * @return
	 */
	public long getSourceObjectID() {
		return _sourceObjId;
	}

	/**
	 * 取得“源对象”。<br>
	 * 例如：PlayerRole可以是一个GameMapEntity，则此方法返回的就是PlayerRole对象实例。
	 * 
	 * @return
	 */
	public T getSourceObject() {
		return _sourceObj;
	}

	/**
	 * 取得本地图实体的地图坐标<br>
	 * 
	 * @return KCoordinate 地图坐标（X、Y）
	 */
	public KCoordinate getCoordinate() {
		return _entityCoordinate;
	}

	/**
	 * 设置本地图实体的地图坐标<br>
	 * 
	 * @return KCoordinate 地图坐标（X、Y）
	 */
	public void setCoordinate(KCoordinate coor) {
		this._entityCoordinate = coor;
	}

	/**
	 * 放入Entity自定义数据
	 * 
	 * @param key
	 * @param value
	 */
	public void putAttribute(String key, Object value) {
		entityAttributes.put(key, value);
	}

	/**
	 * 获取Entity自定义数据
	 * 
	 * @param key
	 * @return
	 */
	public Object getAttribute(String key) {
		return entityAttributes.get(key);
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	public HashSet<Long> getAroundRoleIdSet() {
		return getAroundRoleIdSet;
	}

	public int getEditorInstanceId() {
		return editorInstanceId;
	}

	public void setEditorInstanceId(int npcOrExitInstanceId) {
		this.editorInstanceId = npcOrExitInstanceId;
	}

	/**
	 * <pre>
	 * 取得私有Entity实体列表。即所谓的“地图玩家列表”，此列表是针对角色个人的即每个角色可能拥有不同内容的这种列表。
	 * <p><i>注意，必须是isPlayerRoleType才可以拥有此队列。否返回null。</i></p>
	 * 此列表实例只是绑定到KGameMapEntity中，KGameMapEntity的实现类无需对此做任何创建的操作。
	 * @return PlayerRolePrivateEntityList
	 * </pre>
	 */
	public PlayerRolePrivateEntityList getPrivateEntityList() {
		if (this._entityTypeEnum == KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE) {
			return _privateEntityList;
		} else {
			return null;
		}

	}

	/**
	 * 设置PlayerRolePrivateEntityList对象实例。
	 * 
	 * @param privateEntityList
	 *            传入的对象实例，注意，此实例有可能为null，无需检查直接赋值即可。
	 */
	public void setPrivateEntityList(PlayerRolePrivateEntityList privateEntityList) {
		this._privateEntityList = privateEntityList;
	}

	/**
	 * 判定参数中的GameMapEntity“other”相对于本GameMapEntity在私有列表中的优先级
	 * 
	 * @param other
	 *            另外一个对象
	 * @return 优先级
	 */
	public int judgePriorityInPrivateEntityList(KGameMapEntity other) {
		// TODO ￥￥￥￥￥处理方法judgePriorityInPrivateEntityList();
		if (!other._isPlayerRoleType) {
			return -1;
		}

		// if (GameTitleManager.checkRoleIsUseGameTitle(other._sourceObjId)) {
		// return ENTITY_PRIORITY_GAME_TITLE;
		// }
		//
		// // 判断该实体是否关注了我或者被我关注
		// if (KSupportFactory.getRelationshipSupport().checkRolesRelationship(
		// _sourceObjId, other._sourceObjId, KRelationshipType.KCONCERN)) {
		// return ENTITY_PRIORITY_FRIEND;
		// }
		//
		// if (KSupportFactory.getRelationshipSupport().checkRolesRelationship(
		// other._sourceObjId, _sourceObjId, KRelationshipType.KCONCERN)) {
		// return ENTITY_PRIORITY_FRIEND;
		// }
		//
		// // 判断该实体角色是否与我角色在同一家族
		// if (KSupportFactory.getFamilySupport().checkRolesInSameFamily(
		// _sourceObjId, other._sourceObjId)) {
		// return ENTITY_PRIORITY_GANG_MEMBER;
		// }

		return ENTITY_PRIORITY_STRANGER;
	}

	/**
	 * 是否应该加入本GameMapEntity的私有队列？
	 * 
	 * @param currentMap
	 *            当前发生的地图
	 * @param other
	 *            另外一个对象
	 * @return true－应该加入（之后是根据优先级来判端在队列的位置），false－不应加入（连优先级都不用判断了）
	 */
	public boolean shouldAddToPrivateEntityList(KMap currentMap, KGameMapEntity other) {
		if (currentMap == null) {
			return false;
		}

		if (!other._isPlayerRoleType) {
			return false;
		}

		if (this._sourceObjId == other._sourceObjId) {
			return false;
		}
		// TODO ￥￥￥￥￥ 处理方法shouldAddToPrivateEntityList();
		// 判断角色在线
		if (!((KRole) (other._sourceObj)).isOnline()) {
			currentMap.removeEntity(other._entityTypeEnum, other._sourceObjId);
			return false;
		}
		//
		// 判断角色是否处于免打扰状态
		if (((KRole) (other._sourceObj)).getRoleGameSettingData().isNotDisturb()) {
			return false;
		}

		return true;
	}

	/**
	 * 添加角色在地图周围其他实体的行走状态改变的数据到消息打包器中
	 * 
	 * @param entityType
	 *            ，地图实体类型
	 * @param entityID
	 *            ，实体ID
	 * @param x
	 *            ，当stateType为走路时，实体需要改变的X坐标
	 * @param y
	 *            ，当stateType为走路时，实体需要改变的Y坐标
	 * @return
	 */
	public boolean sendOtherEntityWalkStateData(KMapEntityTypeEnum entityType, long entityID, float x, float y) {

		if (msgPacker != null) {
			return msgPacker.addEntityWalkState(entityType.entityType, entityID, x, y);
		}

		return false;
	}

	/**
	 * 添加角色在地图周围其他实体的行走状态改变的数据到消息打包器中
	 * 
	 * @param entityType
	 *            ，地图实体类型
	 * @param entityID
	 *            ，实体ID
	 * @param x
	 *            ，当stateType为走路时，实体需要改变的X坐标
	 * @param y
	 *            ，当stateType为走路时，实体需要改变的Y坐标
	 * @return
	 */
	public boolean sendOtherEntityResetStateData(KMapEntityTypeEnum entityType, long entityID, float x, float y) {

		if (msgPacker != null) {
			return msgPacker.addEntityResetState(entityType.entityType, entityID, x, y);
		}

		return false;
	}

	/**
	 * 添加角色在地图周围其他实体的出生状态的数据到消息打包器中
	 * 
	 * @param entityType
	 *            ，地图实体类型
	 * @param entityID
	 *            ，实体ID
	 * @param x
	 *            ，当stateType为出生时，实体所在的X坐标
	 * @param y
	 *            ，当stateType为出生时，实体所在的Y坐标
	 * @param entityName
	 *            ，实体名称
	 * @param entityResId
	 *            ，实体资源Id
	 * @param level
	 *            ，当实体为角色时，显示该角色的等级
	 * @return
	 */
	public boolean sendOtherEntityBornIntoMapData(KMapEntityTypeEnum entityType, long entityId, float x, float y, RoleMapEntityShowData data, PetMapEntityShowData petData) {

		if (msgPacker != null) {
			boolean result = msgPacker.addEntityBornState(entityType.entityType, entityId, x, y, data, petData);
			return true;
		}

		return false;
	}

	/**
	 * 添加角色在地图周围其他实体的离开地图状态的数据到消息打包器中
	 * 
	 * @param entityType
	 *            ，地图实体类型
	 * @param entityID
	 *            ，实体ID
	 * @return
	 */
	public boolean sendOtherEntityLeaveMapData(KMapEntityTypeEnum entityType, long entityId, boolean isHasPet, long petId) {

		if (msgPacker != null) {
			return msgPacker.addEntityLeaveMapState(entityType.entityType, entityId, isHasPet, petId);
		}
		return false;
	}

	/**
	 * 获取该地图角色的周围实体状态改变打包消息，
	 * 
	 * @param currentTimeMillis
	 *            ，当前的时间（毫秒）
	 * @return
	 */
	public KGameMessage getOtherEntityStateChangeDataMessage(long currentTimeMillis, int packType) {
		try {

			if (msgPacker != null) {
				return msgPacker.pack(currentTimeMillis, packType);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// /**
	// * 立即获取该地图角色的周围实体状态改变打包消息
	// * @param currentTimeMillis，当前的时间（毫秒）
	// * @return
	// */
	// public KGameMessage getOtherEntityStateChangeDataMessageImmediately() {
	// try {
	//
	// if (msgPacker != null) {
	// System.out.println("Immediately msgPackerSize:"+msgPacker.sizeOfQueue()+",roleId:"+_sourceObjId);
	// KGameMessage msg = msgPacker.packImmediately();
	//
	// return msg;
	// }
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return null;
	// }

	/**
	 * 添加角色在地图周围其他实体的出生状态的数据到消息打包器中
	 * 
	 * @param entityType
	 *            ，地图实体类型
	 * @param entityID
	 *            ，实体ID
	 * @param x
	 *            ，当stateType为出生时，实体所在的X坐标
	 * @param y
	 *            ，当stateType为出生时，实体所在的Y坐标
	 * @param entityName
	 *            ，实体名称
	 * @param entityResId
	 *            ，实体资源Id
	 * @param level
	 *            ，当实体为角色时，显示该角色的等级
	 * @return
	 */
	public boolean sendOtherEntityPetBornIntoMapData(PetMapEntityShowData petData) {

		if (msgPacker != null) {
			boolean result = msgPacker.addEntityPetBornState(petData);
			return true;
		}

		return false;
	}

	/**
	 * 添加角色在地图周围其他实体的离开地图状态的数据到消息打包器中
	 * 
	 * @param entityType
	 *            ，地图实体类型
	 * @param entityID
	 *            ，实体ID
	 * @return
	 */
	public boolean sendOtherEntityPetLeaveMapData(long entityId) {

		if (msgPacker != null) {
			return msgPacker.addEntityPetLeaveMapState(entityId);
		}
		return false;
	}

	/**
	 * 添加角色在地图周围其他实体的机甲状态改变的数据到消息打包器中
	 * 
	 * @param entityID
	 *            ，实体ID
	 * @param mountResId
	 *            ，机甲资源Id，当为-1时，表示没有机甲
	 * @return
	 */
	public boolean sendOtherEntityMountStatus(long entityId, int mountResId) {

		if (msgPacker != null) {
			return msgPacker.sendOtherEntityMountStatus(entityId, mountResId);
		}
		return false;
	}

	/**
	 * 添加角色在地图周围其他实体的战斗状态改变的数据到消息打包器中
	 * 
	 * @param entityID
	 *            ，实体ID
	 * @param isFighting
	 *            ，战斗状态，当为true时，表示在战斗中
	 * @return
	 */
	public boolean sendOtherEntityFightStatus(long entityId, boolean isFighting) {

		if (msgPacker != null) {
			return msgPacker.sendOtherEntityFightStatus(entityId, isFighting);
		}
		return false;
	}

	/**
	 * 添加角色在地图周围其他实体的机甲状态改变的数据到消息打包器中
	 * 
	 * @param entityID
	 *            ，实体ID
	 * @param mountResId
	 *            ，机甲资源Id，当为-1时，表示没有机甲
	 * @return
	 */
	public boolean sendOtherEntityFashionStatus(long entityId, String fashionData) {

		if (msgPacker != null) {
			return msgPacker.sendOtherEntityFashionStatus(entityId, fashionData);
		}
		return false;
	}

	public void clearOtherRoleStateChangedMsgs() {
		if (msgPacker != null) {
			msgPacker.clear();
		}
		this.getAroundRoleIdSet.clear();
	}

	// /**
	// * 当某个GameMapEntity离开某个地图时需要广播给地图角色时的消息
	// * @param map 要离开的地图
	// * @return 待广播的消息
	// */
	// public GameXpMessage getBroadcastMessageOnLeavedGameMap(GameMap map);
	//
	// /**
	// * 当某个GameMapEntity进入某个地图时需要广播给地图角色时的消息
	// * @param map 目标地图
	// * @return 待广播的消息
	// */
	// public GameXpMessage getBroadcastMessageOnJoinedGameMap(GameMap map);

	/**
	 * 销毁一个GameMapEntity在内存中的对象，目的是回收内存。<br>
	 * 实现者应该把本类内的所有对象置null方便GC回收。
	 */
	public void dispose() {
		_sourceObj = null;
		_entityCoordinate = null;
		entityAttributes.clear();
		entityAttributes = null;
		if(_privateEntityList!=null){
			_privateEntityList.clearMyListAndNotifyHolders();
			_privateEntityList = null;
		}
		getAroundRoleIdSet.clear();
		if (msgPacker != null) {
			msgPacker.clear();
			msgPacker = null;
		}
	}

	@Override
	public Object clone() {
		KGameMapEntity o = null;
		try {
			o = (KGameMapEntity) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		o._entityTypeEnum = this._entityTypeEnum;
		// o._sourceObj = this._sourceObj;
		// o._entityCoordinate = new KCoordinate(this._entityCoordinate.getX(),
		// this._entityCoordinate.getY());

		return o;
	}

	@Override
	public List<Long> getMyHolderRoleIds() {
		List<Long> roleIds = new ArrayList<Long>();
		if (this._privateEntityList != null && this._privateEntityList.getMyHolderEntities() != null) {
			for (KGameMapEntity entity : this._privateEntityList.getMyHolderEntities()) {
				if (entity.isPlayerRoleType() && entity.getSourceObjectID() > 0) {
					roleIds.add(entity.getSourceObjectID());
				}
			}
		}
		return roleIds;
	}

	@Override
	public List<Long> getMyShowRoleIds() {
		List<Long> roleIds = new ArrayList<Long>();
		if (this._privateEntityList != null) {
			List<KGameMapEntity> list = this._privateEntityList.getMyShowRoleList();
			if (list != null) {
				for (KGameMapEntity entity : this._privateEntityList.getMyHolderEntities()) {
					roleIds.add(entity.getSourceObjectID());
				}
			}
		}
		return roleIds;
	}

	public static interface KGameMapEntityState {
		/**
		 * 该状态表示 重置坐标 int x坐标 int y坐标
		 */
		public static final byte ENTITY_STATE_RESET_XY = 4;
		/**
		 * 该状态表示 更新坐标 int x坐标 int y坐标
		 */
		public static final byte ENTITY_STATE_UPDATE_XY = 3;

		/**
		 * entity 离开地图
		 */
		public static final byte ENTITY_STATE_LEAVE_MAP = 2;

		/**
		 * entity 加入地图
		 */
		public static final byte ENTITY_STATE_JOIN_MAP = 1;

		/**
		 * role entity的角色机甲状态
		 */
		public static final byte ENTITY_STATE_MOUNT_STATUS = 5;

		/**
		 * role entity的角色时装状态
		 */
		public static final byte ENTITY_STATE_FASHION_STATUS = 6;

		/**
		 * role entity的角色战斗状态
		 */
		public static final byte ENTITY_STATE_FIGHTING_STATUS = 7;
	}

	public static class RoleMapEntityShowData {
		public String roleName;
		public int roleResId;
		public byte job;
		public List<RoleEquipShowData> equipList;
		public String fashionResData;
		public int mountResId;
		public int vipLv;
		public String familyName;
		public int familyIconId;
		public List<Integer> rankResIdList = new ArrayList<Integer>();
		public List<Integer> titleResIdList = new ArrayList<Integer>();
		public boolean isFighting;
		public float moveSpeedX;
		public float moveSpeedY;

		public int starItemResId;// 升星套装地图特效ID
		public int stoneItemResId;// 宝石套装地图特效ID
	}

	public static class RoleEquipShowData implements IRoleEquipShowData {
		public byte equipType;
		public String equipResData;
		public KItemQualityEnum quality;
		@Override
		public byte getPart() {
			return equipType;
		}
		@Override
		public String getRes() {
			return equipResData;
		}
		@Override
		public KItemQualityEnum getQuality() {
			return quality;
		}
	}

	public static class PetMapEntityShowData {
		public long petId;
		public long roleId;
		public int petResId;
		public String petName;
		public int qualityColor;
	}

	public static void main(String[] args) {
		KGameMapEntity<AtomicInteger> e = new KGameMapEntity<AtomicInteger>(KMapEntityTypeEnum.ENTITY_TYPE_OBSTRUCTION, 10000l, new AtomicInteger(10000));
		e.setCoordinate(new KCoordinate(20f, 30f));
		KGameMapEntity<AtomicInteger> c = (KGameMapEntity<AtomicInteger>) e.clone();

		e.getSourceObject().set(999999);

		System.out.println(c.getSourceObjectID());

		System.out.println(c.getSourceObject().get());

		System.out.println(c._isPlayerRoleType);

		e.setCoordinate(new KCoordinate(60f, 70f));

		System.out.println(c.getCoordinate().getX() + "," + c.getCoordinate().getY());
	}
}
