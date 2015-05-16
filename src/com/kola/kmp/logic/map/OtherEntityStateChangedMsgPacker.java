package com.kola.kmp.logic.map;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.map.KGameMapEntity.KGameMapEntityState;
import com.kola.kmp.logic.map.KGameMapEntity.PetMapEntityShowData;
import com.kola.kmp.logic.map.KGameMapEntity.RoleEquipShowData;
import com.kola.kmp.logic.map.KGameMapEntity.RoleMapEntityShowData;
import com.kola.kmp.logic.other.KEquipmentTypeEnum;
import com.kola.kmp.logic.other.KItemQualityEnum;
import com.kola.kmp.logic.other.KMapEntityTypeEnum;
import com.kola.kmp.protocol.map.KMapProtocol;

/**
 * <pre>
 * 周围角色状态改变的消息封装器，用于打包封装角色实体在地图中状态变化的消息。
 * 每个类型为角色的地图实体都会持有一个消息封装器。
 * </pre>
 */
public class OtherEntityStateChangedMsgPacker {

	public final static int PACK_MSG_TYPE_GAME_MAP = 1;

	public final static int PACK_MSG_TYPE_FAMILY_WAR_MAP = 2;

	private final ConcurrentLinkedQueue<OtherRoleStateChangedData> queue = new ConcurrentLinkedQueue<OtherRoleStateChangedData>();

	private long lastPackTimeMillis;

	// private long _debugTimeMillis;
	// private int _debugCounter1, _debugCounter2;
	long roleID;

	// public boolean isJoinMap = false;

	// private final LinkedList<OtherRoleStateChangedData> queue = new
	// LinkedList<OtherRoleStateChangedData>();

	OtherEntityStateChangedMsgPacker(long roleID) {
		this.roleID = roleID;
	}

	/**
	 * 增加一个走路状态信息
	 * 
	 * @param entityType
	 * @param entityID
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean addEntityResetState(int entityType, long entityID, float x,
			float y) {

		OtherRoleStateChangedData wd = new OtherRoleStateChangedData(
				entityType, entityID,
				KGameMapEntityState.ENTITY_STATE_RESET_XY, x, y);
		OtherRoleStateChangedData updateXyWd = new OtherRoleStateChangedData(
				entityType, entityID,
				KGameMapEntityState.ENTITY_STATE_UPDATE_XY, -1, -1);

		// 如果之前包含相同角色的状态信息那就直接删除在添加最新的，即用最新的状态覆盖旧的状态
		if (queue.contains(wd)) {
			queue.remove(wd);
		}
		if (queue.contains(updateXyWd)) {
			queue.remove(updateXyWd);
		}

		// 加入最新状态
		boolean offered = queue.offer(wd);

		return offered;
	}

	/**
	 * 增加一个走路状态信息
	 * 
	 * @param entityType
	 * @param entityID
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean addEntityWalkState(int entityType, long entityID, float x,
			float y) {

		OtherRoleStateChangedData wd = new OtherRoleStateChangedData(
				entityType, entityID,
				KGameMapEntityState.ENTITY_STATE_UPDATE_XY, x, y);

		// 如果之前包含相同角色的状态信息那就直接删除在添加最新的，即用最新的状态覆盖旧的状态
		if (queue.contains(wd)) {
			queue.remove(wd);
		}

		// 加入最新状态
		boolean offered = queue.offer(wd);

		return offered;
	}

	/**
	 * 增加一个进入地图状态信息
	 * 
	 * @param entityType
	 * @param entityID
	 * @param x
	 * @param y
	 * @param entityName
	 * @param entityResId
	 * @param level
	 * @return
	 */
	public boolean addEntityBornState(int entityType, long entityID, float x,
			float y, RoleMapEntityShowData data, PetMapEntityShowData petData) {
		OtherRoleStateChangedData wd = new OtherRoleStateChangedData(
				entityType, entityID,
				KGameMapEntityState.ENTITY_STATE_JOIN_MAP, x, y);
		// wd.entityName = data.roleName;
		// wd.entityResId = data.roleResId;
		// wd.level = level;
		// wd.familyName = familyName;
		// wd.gameTitleIconId = gameTitleIconId;
		// wd.mountResId = mountResId;
		// wd.equipResId = equipResId;
		wd.data = data;

		// 如果之前包含相同角色的状态信息那就直接删除在添加最新的，即用最新的状态覆盖旧的状态
		if (queue.contains(wd)) {
			queue.remove(wd);
		}

		// 加入最新状态
		boolean offered = queue.offer(wd);

		if (petData != null) {
			OtherRoleStateChangedData petWd = new OtherRoleStateChangedData(
					KMapEntityTypeEnum.ENTITY_TYPE_PET.entityType, petData.petId,
					KGameMapEntityState.ENTITY_STATE_JOIN_MAP, -1, -1);
			petWd.petData = petData;
			if (queue.contains(petWd)) {
				queue.remove(petWd);
			}
			queue.offer(petWd);
		}

		return offered;
	}

	/**
	 * 增加一个离开地图状态信息
	 * 
	 * @param entityType
	 * @param entityID
	 * @return
	 */
	public boolean addEntityLeaveMapState(int entityType, long entityID,
			boolean isHasPet, long petId) {
		// 如果本次添加的实体状态为ENTITY_STATE_LEAVE_MAP，则检测队列中是否存在状态为ENTITY_STATE_JOIN_MAP，
		// 如果存在则本次状态不添加到队列中
		OtherRoleStateChangedData tempJoinMapWd = new OtherRoleStateChangedData(
				entityType, entityID,
				KGameMapEntityState.ENTITY_STATE_JOIN_MAP, -1, -1);
		OtherRoleStateChangedData updateXyWd = new OtherRoleStateChangedData(
				entityType, entityID,
				KGameMapEntityState.ENTITY_STATE_UPDATE_XY, -1, -1);
		if (queue.contains(updateXyWd)) {
			queue.remove(updateXyWd);
		}

		if (queue.contains(tempJoinMapWd)) {
			queue.remove(tempJoinMapWd);
			if (isHasPet) {
				OtherRoleStateChangedData tempPetJoinMapWd = new OtherRoleStateChangedData(
						KMapEntityTypeEnum.ENTITY_TYPE_PET.entityType, petId,
						KGameMapEntityState.ENTITY_STATE_JOIN_MAP, -1, -1);
				if (queue.contains(tempPetJoinMapWd)) {
					queue.remove(tempPetJoinMapWd);
				}
			}
			return true;
		}

		// 构建entity离开地图状态数据
		OtherRoleStateChangedData wd = new OtherRoleStateChangedData(
				entityType, entityID,
				KGameMapEntityState.ENTITY_STATE_LEAVE_MAP, -1, -1);
		OtherRoleStateChangedData petWd = new OtherRoleStateChangedData(
				KMapEntityTypeEnum.ENTITY_TYPE_PET.entityType, petId,
				KGameMapEntityState.ENTITY_STATE_LEAVE_MAP, -1, -1);

		// 如果之前包含相同角色的状态信息那就直接删除在添加最新的，即用最新的状态覆盖旧的状态
		if (queue.contains(wd)) {
			queue.remove(wd);
		}
		if (isHasPet && queue.contains(petWd)) {
			queue.remove(petWd);
		}

		// 加入最新状态
		boolean offered = queue.offer(wd);
		if (isHasPet) {
			queue.offer(petWd);
		}

		return offered;
	}

	/**
	 * 增加一个进入地图状态信息
	 * 
	 * @param entityType
	 * @param entityID
	 * @param x
	 * @param y
	 * @param entityName
	 * @param entityResId
	 * @param level
	 * @return
	 */
	public boolean addEntityPetBornState(PetMapEntityShowData petData) {
		OtherRoleStateChangedData wd = new OtherRoleStateChangedData(
				KMapEntityTypeEnum.ENTITY_TYPE_PET.entityType, petData.petId,
				KGameMapEntityState.ENTITY_STATE_JOIN_MAP, -1, -1);
		// wd.entityName = data.roleName;
		// wd.entityResId = data.roleResId;
		// wd.level = level;
		// wd.familyName = familyName;
		// wd.gameTitleIconId = gameTitleIconId;
		// wd.mountResId = mountResId;
		// wd.equipResId = equipResId;
		wd.petData = petData;

		// 如果之前包含相同角色的状态信息那就直接删除在添加最新的，即用最新的状态覆盖旧的状态
		if (queue.contains(wd)) {
			queue.remove(wd);
		}

		// 加入最新状态
		boolean offered = queue.offer(wd);

		return offered;
	}

	/**
	 * 增加一个离开地图状态信息
	 * 
	 * @param entityType
	 * @param entityID
	 * @return
	 */
	public boolean addEntityPetLeaveMapState(long petId) {
		// 如果本次添加的实体状态为ENTITY_STATE_LEAVE_MAP，则检测队列中是否存在状态为ENTITY_STATE_JOIN_MAP，
		// 如果存在则本次状态不添加到队列中
		OtherRoleStateChangedData tempJoinMapWd = new OtherRoleStateChangedData(
				KMapEntityTypeEnum.ENTITY_TYPE_PET.entityType, petId,
				KGameMapEntityState.ENTITY_STATE_JOIN_MAP, -1, -1);

		if (queue.contains(tempJoinMapWd)) {
			queue.remove(tempJoinMapWd);
			return true;
		}

		// 构建entity离开地图状态数据
		OtherRoleStateChangedData petWd = new OtherRoleStateChangedData(
				KMapEntityTypeEnum.ENTITY_TYPE_PET.entityType, petId,
				KGameMapEntityState.ENTITY_STATE_LEAVE_MAP, -1, -1);

		// 如果之前包含相同角色的状态信息那就直接删除在添加最新的，即用最新的状态覆盖旧的状态
		if (queue.contains(petWd)) {
			queue.remove(petWd);
		}

		// 加入最新状态
		boolean offered = queue.offer(petWd);
		return offered;
	}

	/**
	 * 增加一个机甲改变状态信息
	 * 
	 * @param entityId
	 * @param mountResId
	 * @return
	 */
	public boolean sendOtherEntityMountStatus(long entityId, int mountResId) {
		OtherRoleStateChangedData wd = new OtherRoleStateChangedData(
				KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE.entityType, entityId,
				KGameMapEntityState.ENTITY_STATE_MOUNT_STATUS, -1, -1);
		wd.data = new RoleMapEntityShowData();
		wd.data.mountResId = mountResId;

		// 如果之前包含相同角色的状态信息那就直接删除在添加最新的，即用最新的状态覆盖旧的状态
		if (queue.contains(wd)) {
			queue.remove(wd);
		}

		// 加入最新状态
		boolean offered = queue.offer(wd);

		return offered;
	}
	
	/**
	 * 增加一个机甲改变状态信息
	 * 
	 * @param entityId
	 * @param mountResId
	 * @return
	 */
	public boolean sendOtherEntityFightStatus(long entityId, boolean isFighting) {
		OtherRoleStateChangedData wd = new OtherRoleStateChangedData(
				KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE.entityType, entityId,
				KGameMapEntityState.ENTITY_STATE_FIGHTING_STATUS, -1, -1);
		wd.data = new RoleMapEntityShowData();
		wd.data.isFighting = isFighting;

		// 如果之前包含相同角色的状态信息那就直接删除在添加最新的，即用最新的状态覆盖旧的状态
		if (queue.contains(wd)) {
			queue.remove(wd);
		}

		// 加入最新状态
		boolean offered = queue.offer(wd);

		return offered;
	}
	
	/**
	 * 增加一个时装改变状态信息
	 * 
	 * @param entityId
	 * @param mountResId
	 * @return
	 */
	public boolean sendOtherEntityFashionStatus(long entityId, String fashionResData) {
		OtherRoleStateChangedData wd = new OtherRoleStateChangedData(
				KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE.entityType, entityId,
				KGameMapEntityState.ENTITY_STATE_FASHION_STATUS, -1, -1);
		wd.data = new RoleMapEntityShowData();
		wd.data.fashionResData = fashionResData;

		// 如果之前包含相同角色的状态信息那就直接删除在添加最新的，即用最新的状态覆盖旧的状态
		if (queue.contains(wd)) {
			queue.remove(wd);
		}

		// 加入最新状态
		boolean offered = queue.offer(wd);

		return offered;
	}

	/**
	 * 打包一条消息
	 * 
	 * @param currentTimeMillis
	 * @return
	 */
	KGameMessage pack(long currentTimeMillis, int type) {
		KGameMessage msg = null;
		int qsize = queue.size();
		currentTimeMillis = System.currentTimeMillis();
		if (type == OtherEntityStateChangedMsgPacker.PACK_MSG_TYPE_GAME_MAP) {
			// 如果队列状态数量大于打包消息的最大长度，或者本次与上次打包时间间隔大于每次打包的时间间隔值，则进行打包
			if ((qsize >= KGameMapManager.packetEntityStateChangeMsgMaxSize)
					|| (currentTimeMillis - lastPackTimeMillis >= KGameMapManager.broadcastEntityStateChangeMsgPackTimeMillis)) {
				int ssize = qsize > KGameMapManager.packetEntityStateChangeMsgMaxSize ? KGameMapManager.packetEntityStateChangeMsgMaxSize
						: qsize;
				if (ssize > 0) {
					msg = packMessage(ssize);
					// System.out.println("###send msg packet（Role " + roleID +
					// "）: " + ssize);
					// if (queue.size() >= 100) {
					// System.out.println("----------PackerQueue(" + roleID
					// + ").RemainingSize = " + qsize);
					// }
					// if(roleID==103){
					// System.err.println("$$$$$$ 打包数据:"+ssize);
					// }
				}
				lastPackTimeMillis = System.currentTimeMillis();
			}
		} else {
			if (currentTimeMillis - lastPackTimeMillis >= KGameMapManager.broadcastEntityStateChangeMsgPackTimeMillis) {

				msg = packMessage(qsize);

				lastPackTimeMillis = System.currentTimeMillis();
			}
		}
		return msg;
	}

	// /**
	// * 立即打包一条消息
	// * @return
	// */
	// public KGameMessage packImmediately() {
	// KGameMessage msg = null;
	// int qsize = queue.size();
	// if (qsize > 0) {
	// msg = packMessage(qsize);
	// lastPackTimeMillis = System.currentTimeMillis();
	// }
	//
	// return msg;
	//
	// }

	private KGameMessage packMessage(int size) {
		KGameMessage msg = KGame
				.newLogicMessage(KMapProtocol.SM_SYNC_MAP_ENTITY);

		List<OtherRoleStateChangedData> wdList = new ArrayList<OtherRoleStateChangedData>();
		for (int i = 0; i < size; i++) {
			OtherRoleStateChangedData wd = queue.poll();
			if (wd != null) {
				wdList.add(wd);
			}
		}
		int wdSize = 0;
		int wdSizeMsgIndex = msg.writerIndex();
		msg.writeInt(wdSize);// 数量
		for (int i = 0; i < wdList.size(); i++) {
			OtherRoleStateChangedData wd = wdList.get(i);
			if (wd != null) {
				if (wd.stateType == KGameMapEntityState.ENTITY_STATE_JOIN_MAP) {
					if (wd.entityType == KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE.entityType) {
						if (wd.data != null) {
							msg.writeInt(wd.entityType);
							msg.writeLong(wd.entityID);
							msg.writeByte(wd.stateType);
							msg.writeFloat(wd.x);
							msg.writeFloat(wd.y);

							msg.writeUtf8String((wd.data.roleName != null) ? wd.data.roleName
									: "");
							msg.writeInt(wd.data.roleResId);
							msg.writeByte(wd.data.job);
							boolean hasResEquip = false;
							if(wd.data.equipList!=null){
								msg.writeByte(wd.data.equipList.size());
								for (RoleEquipShowData equipData:wd.data.equipList) {
									msg.writeByte(equipData.equipType);
									msg.writeUtf8String(equipData.equipResData);
									if(equipData.equipType == KEquipmentTypeEnum.主武器.sign){
										if(equipData.getQuality() == KItemQualityEnum.无敌的){
											hasResEquip = true;
										}
									}
								}
							}else{
								msg.writeByte(0);
							}
							msg.writeUtf8String((wd.data.fashionResData != null) ? wd.data.fashionResData
									: "");							
							msg.writeBoolean(hasResEquip);
							
							msg.writeInt(wd.data.mountResId);
							msg.writeByte(wd.data.vipLv);
							msg.writeUtf8String((wd.data.familyName != null) ? wd.data.familyName
									: "");
							msg.writeInt(wd.data.familyIconId);
							msg.writeByte(wd.data.rankResIdList.size());
							for (Integer rankResId : wd.data.rankResIdList) {
								msg.writeInt(rankResId);
							}
							msg.writeByte(wd.data.titleResIdList.size());
							for (Integer titleResId : wd.data.titleResIdList) {
								msg.writeInt(titleResId);
							}
							
							msg.writeBoolean(wd.data.isFighting);
							msg.writeFloat(wd.data.moveSpeedX);
							msg.writeFloat(wd.data.moveSpeedY);
							
							msg.writeInt(wd.data.starItemResId);
							msg.writeInt(wd.data.stoneItemResId);
							wdSize++;
						}
					} else if (wd.entityType == KMapEntityTypeEnum.ENTITY_TYPE_PET.entityType) {
						if (wd.petData != null) {
							msg.writeInt(wd.entityType);
							msg.writeLong(wd.entityID);
							msg.writeByte(wd.stateType);

							msg.writeLong(wd.petData.roleId);
							msg.writeInt(wd.petData.petResId);
							msg.writeUtf8String((wd.petData.petName != null) ? wd.petData.petName
									: "");
							msg.writeInt(wd.petData.qualityColor);
							wdSize++;
						}

					} else if (wd.entityType == KMapEntityTypeEnum.ENTITY_TYPE_MONSTER.entityType) {
						msg.writeInt(wd.entityType);
						msg.writeLong(wd.entityID);
						msg.writeByte(wd.stateType);
						msg.writeFloat(wd.x);
						msg.writeFloat(wd.y);
						wdSize++;
					}
				} else if (wd.stateType == KGameMapEntityState.ENTITY_STATE_UPDATE_XY) {
					msg.writeInt(wd.entityType);
					msg.writeLong(wd.entityID);
					msg.writeByte(wd.stateType);
					msg.writeFloat(wd.x);
					msg.writeFloat(wd.y);
					wdSize++;
				} else if (wd.stateType == KGameMapEntityState.ENTITY_STATE_LEAVE_MAP) {
					msg.writeInt(wd.entityType);
					msg.writeLong(wd.entityID);
					msg.writeByte(wd.stateType);
					wdSize++;
				} else if (wd.stateType == KGameMapEntityState.ENTITY_STATE_RESET_XY) {
					msg.writeInt(wd.entityType);
					msg.writeLong(wd.entityID);
					msg.writeByte(wd.stateType);
					msg.writeFloat(wd.x);
					msg.writeFloat(wd.y);
					wdSize++;
				} else if (wd.stateType == KGameMapEntityState.ENTITY_STATE_MOUNT_STATUS) {
					msg.writeInt(wd.entityType);
					msg.writeLong(wd.entityID);
					msg.writeByte(wd.stateType);
					if (wd.data == null) {
						msg.writeInt(-1);
					} else {
						msg.writeInt(wd.data.mountResId);
					}
					wdSize++;
				} else if (wd.stateType == KGameMapEntityState.ENTITY_STATE_FASHION_STATUS) {
					msg.writeInt(wd.entityType);
					msg.writeLong(wd.entityID);
					msg.writeByte(wd.stateType);
					if (wd.data == null) {
						msg.writeUtf8String("");
					} else {
						msg.writeUtf8String(wd.data.fashionResData);
					}
					wdSize++;
				} 
				else if (wd.stateType == KGameMapEntityState.ENTITY_STATE_FIGHTING_STATUS) {
					msg.writeInt(wd.entityType);
					msg.writeLong(wd.entityID);
					msg.writeByte(wd.stateType);
					if (wd.data != null) {
						msg.writeBoolean(wd.data.isFighting);
					}else {
						msg.writeBoolean(false);
					}
					wdSize++;
				}
				wd = null;
			}
		}
		msg.setInt(wdSizeMsgIndex, wdSize);
		return msg;
	}

	/**
	 * 清理全部
	 */
	/* synchronized */void clear() {
		queue.clear();
	}

	int sizeOfQueue() {
		return queue.size();
	}

	/**
	 * 周围角色状态更新的数据
	 * 
	 * @author Ahong
	 * @create 2009-11-25 22:05:40
	 */
	private final class OtherRoleStateChangedData {

		int entityType;
		long entityID;
		byte stateType;
		float x;
		float y;
		RoleMapEntityShowData data;
		PetMapEntityShowData petData;

		// byte lvRP;//善恶值等级

		public OtherRoleStateChangedData(int entityType, long entityID,
				byte stateType, float x, float y) {
			this.entityType = entityType;
			this.entityID = entityID;
			this.stateType = stateType;
			this.x = x;
			this.y = y;
		}

		// @Override
		// public String toString() {
		// return "[WalkData]" +
		// this.entityType + "," +
		// this.entityID + "," +
		// this.stateType + "," +
		// this.x + "," +
		// this.y + "," +
		// this.lvRP;
		// }
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final OtherRoleStateChangedData other = (OtherRoleStateChangedData) obj;
			if (this.entityType != other.entityType) {
				return false;
			}
			if (this.entityID != other.entityID) {
				return false;
			}
			if (this.stateType != other.stateType) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 29 * hash + this.entityType;
			hash = 29 * hash + (int) (this.entityID ^ (this.entityID >>> 32));
			hash = 29 * hash + this.stateType;
			return hash;
		}
	}

	public static void main(String[] args) {
		OtherEntityStateChangedMsgPacker packer = new OtherEntityStateChangedMsgPacker(
				10000);

		// packer.addEntityBornState(KGameMapEntity.ENTITY_TYPE_PLAYERROLE,
		// 1000,
		// -1, -1, "A1", 1, 100, null, null, -1,-1);
		System.out.println("queueSize:" + packer.sizeOfQueue());
		packer.addEntityWalkState(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE.entityType, 1001,
				-1, -1);
		System.out.println("queueSize:" + packer.sizeOfQueue());
		packer.addEntityWalkState(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE.entityType, 1000,
				100, 100);
		System.out.println("queueSize:" + packer.sizeOfQueue());
		// packer.addEntityBornState(KGameMapEntity.ENTITY_TYPE_PLAYERROLE,
		// 1003,
		// -1, -1, "A3", 1, 100, null, null, -1,-1);
		System.out.println("queueSize:" + packer.sizeOfQueue());
		packer.addEntityLeaveMapState(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE.entityType,
				1001, false, 0);
		System.out.println("queueSize:" + packer.sizeOfQueue());
		packer.addEntityLeaveMapState(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE.entityType,
				1000, false, 0);
		System.out.println("queueSize:" + packer.sizeOfQueue());
		packer.addEntityWalkState(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE.entityType, 1003,
				-1, -1);
		System.out.println("queueSize:" + packer.sizeOfQueue());
		packer.addEntityWalkState(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE.entityType, 1003,
				-1, -1);
		// System.out.println("queueSize:"+packer.sizeOfQueue());

		int size = packer.sizeOfQueue();
		System.out.println("queueSize:" + size);

		for (int i = 0; i < size; i++) {
			OtherRoleStateChangedData data = packer.queue.poll();
			// System.out.println("entityId:" + data.entityID + ",state:"
			// + data.stateType + ",name:" + data.entityName);
		}
	}

}
