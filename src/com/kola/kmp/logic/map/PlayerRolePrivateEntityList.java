package com.kola.kmp.logic.map;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.koala.game.logging.KGameLogger;
import com.kola.kgame.cache.util.FixedPriorityList;
import com.kola.kmp.logic.role.KRole;

/**
 * <b>角色类型的KGameMapEntity私有的其他实体的列表（就是角色类型的KGameMapEntity能感知的范围）</b><br>
 * － 也就是一个角色类型的KGameMapEntity周围能交互的其他GameMapEntity数量限制，因为这个数量并非无限的。<br>
 * 假设同地图上有1000角色，那只能感知100人，而这能感知的100人又有它各自的策略：<br>
 * <i> 列表显示获取优先级从高到低为（夫妻另一方）、（师徒另一方）、队友、好友、（家族成员）、陌生人。 </i>
 * <p>
 * 注：只要游戏设置有地图人数限制，则每个角色类型的GameMapEntity身上都携带一个本类实例。
 * </p>
 * <p>
 * 生命周期管理：<br>
 * >PlayerRole第一次进入地图时候创建本实例并被引擎绑定到GameMapEntity对象；<br>
 * >所在地图有其他Entity进出时会自动更新此列表；<br>
 * >当角色跳转地图时会被重置，即重新更新全部Entity；<br>
 * >当角色退出游戏时此列表同时销毁。
 * </p>
 * <p>
 * 名词解析：<b>Holder</b>－GameMapEntity B的私有列表包含GameMapEntity A，则我们称B是A的Holder
 * </p>
 */
public class PlayerRolePrivateEntityList {
	private static final KGameLogger _LOGGER = KGameLogger
			.getLogger(PlayerRolePrivateEntityList.class);
	/**
	 * 列表拥有者
	 */
	private final KGameMapEntity _owner;
	private final FixedPriorityList<GameMapEntityInListWithPriority> myList;// 我的列表
	// 记录其他人保存了我到他的列表－－－－－－－－－－－－－－－－－－－－－－－－－－－－
	private static final Object PRESENT = new Object();
	private final ConcurrentHashMap<KGameMapEntity, Object> myHolders = new ConcurrentHashMap<KGameMapEntity, Object>(
			100);

	private final ConcurrentHashMap<KGameMapEntity, Boolean> myShowMap = new ConcurrentHashMap<KGameMapEntity, Boolean>();
	private final LinkedList<KGameMapEntity> myShowQueue = new LinkedList<KGameMapEntity>();

	public PlayerRolePrivateEntityList(KGameMapEntity ownerPlayerRole,
			int fixedCapacity, boolean concurrent, boolean priority) {
		this._owner = ownerPlayerRole;
		this.myList = new FixedPriorityList<GameMapEntityInListWithPriority>(
				concurrent, fixedCapacity,
				priority ? (new Comparator<GameMapEntityInListWithPriority>() {

					public int compare(GameMapEntityInListWithPriority o1,
							GameMapEntityInListWithPriority o2) {
						if (o1 == null && o2 == null) {
							return 0;
						} else {
							if (o1 == null) {
								return -1;
							} else if (o2 == null) {
								return 1;
							}
						}
						return o1.priority - o2.priority;
					}
				}) : null);
	}

	/**
	 * 私有列表的拥有者：角色类型的KGameMapEntity
	 * 
	 * @return
	 */
	public KGameMapEntity getOwner() {
		return _owner;
	}

	/**
	 * 向列表增加或更新一个成员GameMapEntity，已经存在就是更新，不存在根据优先级添加进去。 <br>
	 * 例如解散组队时，那优先级发生了变化则需要更新原队友在我列表中的位置
	 * 
	 * @param currentMap
	 *            当前发生的地图（本来可以在GameMapEntity拿到所属地图的，但比较麻烦）
	 * @param entity
	 *            更新或要增加的对象
	 * @return 是否增加或更新成功
	 */
	public boolean addOrUpdateMyList(KMap currentMap, KGameMapEntity entity) {
		if (entity == null
				|| (!_owner.shouldAddToPrivateEntityList(currentMap, entity))) {
			return false;
		}
		if (myList.remainingCapacity() < 0) {
			return false;
		}
		GameMapEntityInListWithPriority element = new GameMapEntityInListWithPriority(
				entity);
		element.judgePriorityInPrivateEntityList(_owner);
		if (myList.addOrUpdate(element)) {
			// System.out.print(owner + ".pList.add(" + entity + ") OK;");
			// 加入成功后，将我加入到对方的Holder集合
			PlayerRolePrivateEntityList otherlist = (PlayerRolePrivateEntityList) entity
					.getPrivateEntityList();
			if (otherlist != null) {
				otherlist.addHolder(_owner);
				// System.out.println(entity + ".hList.add(" + owner + ") OK!");
			} else {
				// System.out.println(entity + ".hList.add(" + owner +
				// ") Fail");
			}
			// if(_owner.getSourceObjectID()==103){
			// _LOGGER.debug("### 有角色："+element.entity.getSourceObjectID()+
			// "进入地图，加入本人角色："+_owner.getSourceObjectID()+"的私有列表。");
			// }
			checkIfPutToShowMap(element.entity);

			return true;
		}
		return false;
	}

	/**
	 * 移除一个GameMapEntity，如果存在的话。
	 * 
	 * @param entity
	 *            待移除的对象
	 * @return 是否移除成功
	 */
	public boolean removeFromMyList(KGameMapEntity entity) {
		if (entity == null) {
			return false;
		}
		return removeFromMyList0(new GameMapEntityInListWithPriority(entity));
	}

	/**
	 * 清空我的列表，并同时通知所有对我的持有者
	 */
	public void clearMyListAndNotifyHolders() {
		for (Iterator<GameMapEntityInListWithPriority> it = myList.iterator(); it
				.hasNext();) {
			GameMapEntityInListWithPriority en = it.next();
			this.removeFromMyList0(en);
		}
		myList.clear();
		myHolders.clear();
		myShowMap.clear();
		myShowQueue.clear();
	}

	private boolean removeFromMyList0(GameMapEntityInListWithPriority element) {
		if (element != null) {
			if (myList.remove(element)) {
				// System.out.print(owner + ".pList.remove(" + entity +
				// ") OK;");
				// 移出成功后，将我移出到对方的Holder集合
				PlayerRolePrivateEntityList otherlist = (PlayerRolePrivateEntityList) element.entity
						.getPrivateEntityList();
				if (otherlist != null && otherlist.removeHolder(_owner)) {
//					 System.err.println(element.entity.getSourceObjectID() + ".hList.remove(" + _owner.getSourceObjectID() +
//					 ") OK!");
				} else {
//					System.err.println(element.entity.getSourceObjectID() + ".hList.remove(" + _owner.getSourceObjectID() +
//							 ") Fail!");
					 
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * 私有列表当前Entity数量
	 * 
	 * @return
	 */
	public int sizeOfMyList() {
		return myList.size();
	}

	/**
	 * 对我的持有者数量
	 * 
	 * @return
	 */
	public int sizeOfMyHolder() {
		return myHolders.size();
	}

	/**
	 * 私有列表的容量（固定）
	 * 
	 * @return
	 */
	public int capacityOfMyList() {
		return myList.capacity();
	}

	/**
	 * 私有列表的剩余容量
	 * 
	 * @return
	 */
	public int remainingCapacityOfMyList() {
		return myList.remainingCapacity();
	}

	/**
	 * 检测私有列表是否包含指定的Entity
	 * 
	 * @param entity
	 *            被检测的Entity
	 * @return
	 */
	public boolean containsInMyList(KGameMapEntity entity) {
		return myList.indexOf(new GameMapEntityInListWithPriority(entity)) != -1;
	}

	/**
	 * 将私有列表中的所有Entity转化为普通列表的形式返回
	 * 
	 * @return
	 */
	public List<KGameMapEntity> getMyListEntities() {
		List<KGameMapEntity> entities = new ArrayList<KGameMapEntity>(
				myList.size());
		Object[] objs = myList.toArray();
		for (Object object : objs) {
			if (object != null) {
				entities.add(((GameMapEntityInListWithPriority) object).entity);
			}
		}
		return entities;
	}

	/**
	 * 检测某个Entity的私有队列是否包含我在其中
	 * 
	 * @param entity
	 *            被检测的实体
	 * @return
	 */
	public boolean isMyHolder(KGameMapEntity entity) {
		if (entity != null) {
			PlayerRolePrivateEntityList otherlist = (PlayerRolePrivateEntityList) entity
					.getPrivateEntityList();
			if (otherlist != null) {
				return otherlist.containsInMyHolderSet(_owner);
			}
		}
		return false;
	}

	/**
	 * 取得我的所有Holder（Holder的解析请看本接口文档）
	 * 
	 * @return
	 */
	public List<KGameMapEntity> getMyHolderEntities() {
		List<KGameMapEntity> entities = new ArrayList<KGameMapEntity>(
				myHolders.size());

		entities.addAll(myHolders.keySet());

		return entities;
	}

	/**
	 * 检查其它Entity在我列表中的优先级，如果不存在则返回defaultReturnValue
	 * 
	 * @param e
	 *            被检查的Entity
	 * @param defaultReturnValue
	 *            如果不在队列那默认返回的值（此值可以是调用者用于判断是否有优先级，可以设一个跟预定优先级不相同的值）
	 * @return 优先级，如果不存在则返回defaultReturnValue
	 */
	public int checkOtherEntityPriorityIfInList(KGameMapEntity entity,
			int defaultReturnValue) {
		GameMapEntityInListWithPriority element = new GameMapEntityInListWithPriority(
				entity);
		GameMapEntityInListWithPriority elementGot = myList.contains(element);
		return elementGot != null ? elementGot.priority : defaultReturnValue;

	}

	// /////////////////////////////////////////////////
	void addHolder(KGameMapEntity holder) {
		myHolders.put(holder, PRESENT);
	}

	boolean removeHolder(KGameMapEntity holder) {
		return myHolders.remove(holder) == PRESENT;
	}

	boolean containsInMyHolderSet(KGameMapEntity entity) {
		return myHolders.containsKey(entity);
	}

	public<T extends KMap<KGameMapEntity>> void dealOnJoinGameMap(T map) {

		// 重组我的队列、以及更新别人的队列
		// List<GameMapEntity> entities = map.getEntities();

		if (map != null) {
			for (KGameMapEntity gameMapEntity : map.getEntitysMap().values()) {
				if (gameMapEntity != null) {
					// －－检测是否应该加入我的私有队列
					if (this.addOrUpdateMyList(map, gameMapEntity)) {
						// System.out.println("<<<" + owner + ".pList.add(" +
						// gameMapEntity + ")OK");
					}
					// －－询问其他人看是否应该把我加入他的队列
					PlayerRolePrivateEntityList otherlist = (PlayerRolePrivateEntityList) gameMapEntity
							.getPrivateEntityList();
					if ((otherlist) != null) {
						boolean bAddToOtherList = otherlist.addOrUpdateMyList(
								map, _owner);
						// System.out.println("<<<" + gameMapEntity +
						// ".pList.add(" + gameMapEntity + ")" +
						// bAddToOtherList);

					}
				}
			}

		}

	}

	public<T extends KMap<KGameMapEntity>> void dealOnLeaveGameMap(T map) {
		// 遍历我在本地图实体对象的所有持有者，如果他们的私有列表中存在我的地图实体对象，则清除掉
		for (KGameMapEntity gameMapEntity : myHolders.keySet()) {
			if (gameMapEntity != null && gameMapEntity.isPlayerRoleType()
					&& gameMapEntity.getPrivateEntityList() != null) {
				// －－把我移出队列吧
				boolean removeSucceed = gameMapEntity.getPrivateEntityList()
						.removeFromMyList(_owner);
				if (gameMapEntity.getPrivateEntityList().myShowMap
						.containsKey(_owner)) {
					Object removeShowSucceed = gameMapEntity
							.getPrivateEntityList().myShowMap.remove(_owner);
					if (removeShowSucceed !=null) {
						gameMapEntity.getPrivateEntityList().myShowQueue
								.remove(_owner);
					}
				}
				// System.out.println(">>>" + gameMapEntity + ".pList.remove(" +
				// owner + ")" + removeSucceed);
				if (removeSucceed) {

					// 如果移除成功，则该持有者的私有列表再补充其他一个角色地图实体对象
					PlayerRolePrivateEntityList otherplist = (PlayerRolePrivateEntityList) gameMapEntity
							.getPrivateEntityList();
					if (otherplist != null) {
						for (KGameMapEntity gameMapEntity1 : map.getEntitysMap()
								.values()) {
							if (gameMapEntity1.getSourceObjectID() != gameMapEntity
									.getSourceObjectID()) {
								if (gameMapEntity1 != null
										&& otherplist.addOrUpdateMyList(map,
												gameMapEntity1)) {
									break;
								}
							}
						}
					}
				}
			}
		}
		// 然后再遍历自己的私有列表，将自己持有的地图实体对象移除
		for (Iterator<GameMapEntityInListWithPriority> it = myList.iterator(); it
				.hasNext();) {
			GameMapEntityInListWithPriority en = it.next();
			removeFromMyList0(en);
		}
		
		myShowMap.clear();
	}

	private boolean checkIfPutToShowMap(KGameMapEntity otherEntity) {
		if (_owner.isPlayerRoleType()) {
			int showSize;
			if (KGameMapManager.showPlayerLevelMap
					.containsKey(((KRole) _owner.getSourceObject())
							.getRoleGameSettingData().getMapShowPlayerLevel())) {
				showSize = KGameMapManager.showPlayerLevelMap
						.get(((KRole) _owner.getSourceObject())
								.getRoleGameSettingData().getMapShowPlayerLevel());
			} else {
				showSize = KGameMapManager.default_show_size;
			}

			if (!myShowMap.containsKey(otherEntity)
					&& myShowMap.size() < showSize) {
				myShowQueue.add(otherEntity);
				myShowMap.put(otherEntity, true);
				return true;
			}
		}
		return false;
	}

	public boolean isShowEntity(KGameMapEntity otherEntity) {
		return myShowMap.containsKey(otherEntity);
	}

	public List<KGameMapEntity> decreaseShowEntitySize(int showSize) {
		List<KGameMapEntity> subList = new ArrayList<KGameMapEntity>();

		while (myShowQueue.size() > showSize) {
			KGameMapEntity removeEntity = myShowQueue.removeLast();
			if (removeEntity != null) {
				myShowMap.remove(removeEntity);
				subList.add(removeEntity);
			}
		}

		return subList;
	}

	public List<KGameMapEntity> increaseShowEntitySize(int showSize) {
		List<KGameMapEntity> addList = new ArrayList<KGameMapEntity>();

		int myListIndex = 0;
		while (myShowQueue.size() < showSize
				&& myShowQueue.size() < myList.size()
				&& myListIndex < myList.size()) {
			GameMapEntityInListWithPriority addEntityWithPriority = myList
					.getElement(myListIndex);
			if (addEntityWithPriority != null
					&& addEntityWithPriority.entity != null) {
				if (!myShowMap.containsKey(addEntityWithPriority.entity)) {
					myShowQueue.add(addEntityWithPriority.entity);
					myShowMap.put(addEntityWithPriority.entity, true);
					addList.add(addEntityWithPriority.entity);
				}
			}
			myListIndex++;
		}

		return addList;
	}
	
	public List<KGameMapEntity> getMyShowRoleList(){
		List<KGameMapEntity> roleList = new ArrayList<KGameMapEntity>();
		if(myShowQueue!=null && !myShowQueue.isEmpty()){
			roleList.addAll(myShowQueue);			
		}
		return roleList;
	}

	// //////////////////////////////////////////////////

	/**
	 * inner class: 放到我的私有列表后的GameMapEntity，携带优先级的
	 */
	private final class GameMapEntityInListWithPriority {

		KGameMapEntity entity;
		int priority;

		GameMapEntityInListWithPriority(KGameMapEntity entity) {
			this.entity = entity;
		}

		GameMapEntityInListWithPriority(KGameMapEntity entity, int priority) {
			this.priority = priority;
			this.entity = entity;
		}

		void judgePriorityInPrivateEntityList(KGameMapEntity listowner) {
			if (listowner == null) {
				return;
			}
			this.priority = listowner.judgePriorityInPrivateEntityList(entity);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final GameMapEntityInListWithPriority other = (GameMapEntityInListWithPriority) obj;
			if (this.entity != other.entity
					&& (this.entity == null || !this.entity
							.equals(other.entity))) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 89 * hash
					+ (this.entity != null ? this.entity.hashCode() : 0);
			return hash;
		}
	}

}
