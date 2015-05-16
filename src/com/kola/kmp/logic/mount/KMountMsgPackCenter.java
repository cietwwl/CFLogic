package com.kola.kmp.logic.mount;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;

import com.koala.game.KGameMessage;
import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.mount.KMount.KMountSkill;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountEquiTemp;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountLv;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountTemplate;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountUpLvData;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleIniSkillTemp;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.mount.KMountProtocol;

/**
 * <pre>
 * 由于在打包模块消息的过程中，涉及到访问及复杂的逻辑结构
 * 因此设立此类，用于打包消息
 * 
 * @author CamusHuang
 * @creation 2015-1-6 下午2:47:24
 * </pre>
 */
public class KMountMsgPackCenter {

	public static final Logger _LOGGER = KGameLogger.getLogger(KMountMsgPackCenter.class);

	/**
	 * <pre>
	 * 参考{@link KMountProtocol#SM_PUSH_MOUNTDATA}
	 * 
	 * @param msg
	 * @param role
	 * @author CamusHuang
	 * @creation 2015-1-8 上午11:06:05
	 * </pre>
	 */
	public static void pacAllMountDatas(KGameMessage msg, KRole role) {
		KMountSet set = KMountModuleExtension.getMountSet(role.getId());
		set.rwLock.lock();
		try {
			
			int writeIndex = msg.writerIndex();
			int count = 0;
			//
			msg.writeByte(count);
			for(Entry<Integer, LinkedHashMap<Integer, KMountTemplate>> e:KMountDataManager.mMountTemplateManager.getDataCache().entrySet()){
				int model = e.getKey();
				Map<Integer, KMountTemplate> tempMap = e.getValue();
				//
				KMountTemplate nowTemp = null;
				KMountTemplate frontTemp = null;
				KMountTemplate nextTemp = null;
				//
				KMount mount = set.getMountByModel(model);
				int bigLv = mount == null ? 1 : mount.getTemplate().bigLv;
				//
				nowTemp = tempMap.get(bigLv);
				if (nowTemp.isForNewRole) {
					continue;
				}
				frontTemp = tempMap.get(bigLv - 1);
				nextTemp = tempMap.get(bigLv + 1);
				//
				packMount(msg, role, mount, frontTemp, nowTemp, nextTemp);
				//
				count++;
			}
			msg.setByte(writeIndex, count);
		} finally {
			set.rwLock.unlock();
		}
	}
	
	public static void packMount(KGameMessage msg, KRole role, KMount mount) {
		
		KMountTemplate nowTemp = mount.getTemplate();
		Map<Integer, KMountTemplate> tempMap = KMountDataManager.mMountTemplateManager.getTemplateByModel(nowTemp.Model);
		//
		int bigLv = mount.getTemplate().bigLv;
		packMount(msg, role, mount, tempMap.get(bigLv-1), nowTemp, tempMap.get(bigLv+1));
	}

	/**
	 * <pre>
	 * 参考{@link KMountProtocol#MSG_STRUCT_MOUNT}
	 * 
	 * @param msg
	 * @param role
	 * @param mount
	 * @param frontTemp
	 * @param nextTemp
	 * @author CamusHuang
	 * @creation 2015-1-8 上午11:06:14
	 * </pre>
	 */
	private static void packMount(KGameMessage msg, KRole role, KMount mount, KMountTemplate frontTemp, KMountTemplate nowTemp, KMountTemplate nextTemp) {
		
		int nowLv = mount==null?1:mount.getLevel();
		//
//		 * boolean 是否已经获得此机甲
//		 * int 机甲型号
//		 * String 名称
//		 * int 头像资源ID
//		 * int 形象资源ID
//		 * short 阶级
//		 * short 等级
//		 * float 主城移动速率
//		 * float 战斗移动速率
//		 * float 移动速率
//		 * byte 品质
//		 * String 获得界面描述
//		 * int 获得界面ID
//		 * String 型号类型描述
//		 * String 机甲特长

		msg.writeBoolean(mount!=null);
		msg.writeInt(nowTemp.Model);
		msg.writeUtf8String(nowTemp.Name);
		msg.writeInt(nowTemp.HeadID);
		msg.writeInt(nowTemp.res_id);
		msg.writeShort(nowTemp.bigLv);
		msg.writeShort(nowLv);
		msg.writeShort(KMountDataManager.mMountUpBigLvDataManager.getData(nowTemp.bigLv).lv);
		msg.writeFloat(nowTemp.cityMoveSpeedup);
		msg.writeFloat(nowTemp.fightMoveSpeed);
		msg.writeByte(nowTemp.quaEnum.sign);
		msg.writeUtf8String(nowTemp.Description);
		msg.writeInt(nowTemp.functionId);
		msg.writeUtf8String(nowTemp.ModelDescription);
		msg.writeUtf8String(nowTemp.SpecialtyDescription);
		msg.writeInt(nowTemp.beanTimeMap.get(1));
		
//		 * 
//		 * byte 装备数量n
//		 * for(0~n){
//		 * 	本阶装备参考{@link #MSG_STRUCT_MOUNT_EQUI}
//		 * 	boolean 是否已经获得本阶装备
//		 * 	if(true){
//		 * 		boolean 是否存在下一阶装备
//		 * 		if(true){
//		 * 			下一阶装备参考{@link #MSG_STRUCT_MOUNT_EQUI}
//		 * 			byte 合成下一阶装备需要的其它材料数量n
//		 * 			for(0~n){
//		 * 				材料参考{@link KItemProtocol#MSG_STRUCT_ITEM_DETAILS}
//		 * 			}
//		 * 		}
//		 * 	} else {
//		 * 		boolean 是否存在上一阶装备
//		 * 		if(true){
//		 * 			上一级装备参考{@link #MSG_STRUCT_MOUNT_EQUI}
//		 * 		}
//		 * 		byte 合成本阶装备需要的其它材料数量n
//		 * 		for(0~n){
//		 * 			材料参考{@link KItemProtocol#MSG_STRUCT_ITEM_DETAILS}
//		 * 		}
//		 * 	}
//		 * }
		msg.writeByte(nowTemp.equiIdList.size());
		for(int index=0;index<nowTemp.equiIdList.size();index++){
			
			int equiId = nowTemp.equiIdList.get(index);
			KMountEquiTemp equiTemp = KMountDataManager.mMountEquiDataManager.getData(equiId);
			packMountEqui(msg, equiTemp);
			boolean get = mount==null?false:mount.checkEqui(equiId);
			msg.writeBoolean(get);
			if(get){
				msg.writeBoolean(nextTemp!=null);
				if(nextTemp!=null){
					int nextEquiId = nextTemp.equiIdList.get(index);
					KMountEquiTemp nextEquiTemp = KMountDataManager.mMountEquiDataManager.getData(nextEquiId);
					packMountEqui(msg, nextEquiTemp);
					packMountEquiMetrails(msg, nextEquiTemp);
				}
			} else {
				msg.writeBoolean(frontTemp!=null);
				if(frontTemp!=null){
					int frontEquiId = frontTemp.equiIdList.get(index);
					KMountEquiTemp frontEquiTemp = KMountDataManager.mMountEquiDataManager.getData(frontEquiId);
					packMountEqui(msg, frontEquiTemp);
				}
				packMountEquiMetrails(msg, equiTemp);
			}
		}
		

//		 * byte 基础属性数量n
//		 * for(0~n){
//		 * 	int 属性类型
//		 * 	int 基础属性值
//		 * }
//		 * 
//		 * byte 升级属性数量n
//		 * for(0~n){
//		 * 	int 属性类型
//		 *  int 升级属性值
//		 * }
		msg.writeByte(nowTemp.allEffects.size());
		for(Entry<KGameAttrType, Integer> e:nowTemp.allEffects.entrySet()){
			msg.writeInt(e.getKey().sign);
			msg.writeInt(e.getValue());
		}
		KMountLv lvData = KMountDataManager.mMountLvDataManager.getData(nowLv);
		Map<KGameAttrType, Integer> lvAttsMap = lvData.allEffects.get(nowTemp.Model);
		msg.writeByte(lvAttsMap.size());
		for(Entry<KGameAttrType, Integer> e:lvAttsMap.entrySet()){
			msg.writeInt(e.getKey().sign);
			msg.writeInt(e.getValue());
		}
		
//		 * boolean 是否存在一下级
//		 * if(true){
//		 * 	int 等级经验分子
//		 * 	int 等级经验分母
//		 * 	byte 属性数量n
//		 * 	for(0~n){
//		 * 		int 属性类型
//		 * 		int 升级属性值
//		 * 	}
//		 * 	int SP加点
//		 * }
//		 * int 剩余SP点
		{
			KMountLv nextLvData = KMountDataManager.mMountLvDataManager.getData(nowLv+1);
			msg.writeBoolean(nextLvData!=null);
			if(nextLvData!=null){
				msg.writeInt(mount==null?0:mount.getExp());
				msg.writeInt(lvData.exp);
				//
				Map<KGameAttrType, Integer> nextLvAttsMap = nextLvData.allEffects.get(nowTemp.Model);
				msg.writeByte(nextLvAttsMap.size());
				for(Entry<KGameAttrType, Integer> e:nextLvAttsMap.entrySet()){
					msg.writeInt(e.getKey().sign);
					msg.writeInt(e.getValue());
				}
				msg.writeInt(nextLvData.spPoint - lvData.spPoint);
			}
		}
		
		msg.writeInt(lvData.spPoint-(mount==null?0:mount.getUsedSP()));

//		 * byte 技能数量n
//		 * for(0~n){
//		 * 	int 技能模板ID
//		 * 	int 技能等级
//		 * }
//		 * boolean 是否骑乘中
//		 * 
//		 * long 战斗力
//		 * 
//		 * boolean 是否存在下一阶机甲
//		 * if(true){
//		 * 	下一阶机甲参考{@link #MSG_STRUCT_NEXTMOUNT}
//		 * }
		msg.writeByte(nowTemp.skillIdList.size());
		for(int skillId:nowTemp.skillIdList){
			msg.writeInt(skillId);
			KMountSkill mKMountSkill = mount==null?null:mount.getSkillCache().get(skillId);
			msg.writeInt(mKMountSkill==null?1:mKMountSkill.getLv());
		}
		msg.writeBoolean(mount==null?false:mount.isUsed());
		
		long battlePower = mount==null?nowTemp.getBattlePower(role.getId()):KSupportFactory.getRoleModuleSupport().calculateBattlePower(mount.getAttsForAll(), role.getId());
		msg.writeLong(battlePower);
		
		msg.writeBoolean(nextTemp!=null);
		if(nextTemp!=null){
			packNextMount(msg, role, nextTemp);
		}
	}
	
	/**
	 * <pre>
	 * {@link KMountProtocol#MSG_STRUCT_ITEM_DETAILS}
	 * 
	 * @param msg
	 * @param nextEquiTemp
	 * @author CamusHuang
	 * @creation 2015-1-8 上午11:41:28
	 * </pre>
	 */
	private static void packMountEquiMetrails(KGameMessage msg, KMountEquiTemp equiTemp) {
//		 * 			byte 合成下一阶装备需要的其它材料数量n
//		 * 			for(0~n){
//		 * 				材料参考{@link KItemProtocol#MSG_STRUCT_ITEM_DETAILS}
//		 * 			}
		msg.writeByte(equiTemp.itemList.size());
		for(ItemCountStruct struct:equiTemp.itemList){
			KItemMsgPackCenter.packItem(msg, struct.getItemTemplate(), struct.itemCount);
		}
	}

	/**
	 * <pre>
	 * {@link KMountProtocol#MSG_STRUCT_MOUNT_EQUI}
	 * 
	 * @param msg
	 * @param equiTemp
	 * @author CamusHuang
	 * @creation 2015-1-8 上午11:30:50
	 * </pre>
	 */
	private static void packMountEqui(KGameMessage msg, KMountEquiTemp equiTemp) {
//		 * int 装备ID
//		 * int icon
//		 * String　名称
//		 * byte 属性数量n
//		 * for(0~n){
//		 * 	int 属性类型
//		 * 	int 属性值
//		 * }
		
		msg.writeInt(equiTemp.equipID);
		msg.writeInt(equiTemp.icon);
		msg.writeUtf8String(equiTemp.name);
		msg.writeByte(equiTemp.allEffects.size());
		for(Entry<KGameAttrType, Integer> e:equiTemp.allEffects.entrySet()){
			msg.writeInt(e.getKey().sign);
			msg.writeInt(e.getValue());
		}
	}
	
	/**
	 * <pre>
	 * 参考{@link KMountProtocol#MSG_STRUCT_MOUNT_FORUPLV}
	 * 
	 * @param msg
	 * @param role
	 * @param mount
	 * @param frontTemp
	 * @param nextTemp
	 * @author CamusHuang
	 * @creation 2015-1-8 上午11:06:14
	 * </pre>
	 */
	public static void packMountFoUpLv(KGameMessage msg, KRole role, KMount mount) {
		KMountTemplate nowTemp = mount.getTemplate();
		Map<Integer, KMountTemplate> tempMap = KMountDataManager.mMountTemplateManager.getTemplateByModel(nowTemp.Model);
		//
		int bigLv = mount.getTemplate().bigLv;
		packMountFoUpLv(msg, role, mount, tempMap.get(bigLv-1), nowTemp, tempMap.get(bigLv+1));
	}
	private static void packMountFoUpLv(KGameMessage msg, KRole role, KMount mount, KMountTemplate frontTemp, KMountTemplate nowTemp, KMountTemplate nextTemp) {
		
		int nowLv = mount==null?1:mount.getLevel();
		//
//		 * int 机甲型号
//		 * short 等级
//		 * 
//		 * byte 属性数量n
//		 * for(0~n){
//		 * 	int 属性类型
//		 *  int 升级属性值
//		 * }
//		 * 
//		 * boolean 是否存在一下级
//		 * if(true){
//		 * 	int 等级经验分子
//		 * 	int 等级经验分母
//		 * 	byte 属性数量n
//		 * 	for(0~n){
//		 * 		int 属性类型
//		 * 		int 基础属性值
//		 * 	}
//		 * 	int SP加点
//		 * }
//		 * 
//		 * int 剩余SP点
//		 * int 战斗力

		msg.writeInt(nowTemp.Model);
		msg.writeShort(nowLv);
		//
		KMountLv lvData = KMountDataManager.mMountLvDataManager.getData(nowLv);
		Map<KGameAttrType, Integer> lvAttsMap = lvData.allEffects.get(nowTemp.Model);
		msg.writeByte(lvAttsMap.size());
		for(Entry<KGameAttrType, Integer> e:lvAttsMap.entrySet()){
			msg.writeInt(e.getKey().sign);
			msg.writeInt(e.getValue());
		}
		//
		{
			KMountLv nextLvData = KMountDataManager.mMountLvDataManager.getData(nowLv+1);
			msg.writeBoolean(nextLvData!=null);
			if(nextLvData!=null){
				msg.writeInt(mount==null?0:mount.getExp());
				msg.writeInt(lvData.exp);
				//
				Map<KGameAttrType, Integer> nextLvAttsMap = nextLvData.allEffects.get(nowTemp.Model);
				msg.writeByte(nextLvAttsMap.size());
				for(Entry<KGameAttrType, Integer> e:nextLvAttsMap.entrySet()){
					msg.writeInt(e.getKey().sign);
					msg.writeInt(e.getValue());
				}
				msg.writeInt(nextLvData.spPoint - lvData.spPoint);
			}
		}
		msg.writeInt(lvData.spPoint-(mount==null?0:mount.getUsedSP()));
		
		// 战斗力计算方式=培养属性+机甲基础属性+机甲装备属性
		int battlePower = mount == null ? nowTemp.getBattlePower(role.getId()) : KSupportFactory.getRoleModuleSupport().calculateBattlePower(mount.getAttsForAll(), role.getId());
		msg.writeInt(battlePower);
	}	

	/**
	 * <pre>
	 * 参考{@link KMountProtocol#MSG_STRUCT_NEXTMOUNT}
	 * 
	 * @param msg
	 * @param role
	 * @param nexTemp
	 * @param uplvData
	 * @author CamusHuang
	 * @creation 2015-1-8 下午12:23:00
	 * </pre>
	 */
	private static void packNextMount(KGameMessage msg, KRole role, KMountTemplate nextTemp) {
//		 * int 机甲型号
//		 * String 名称
//		 * int 头像资源ID
//		 * int 形象资源ID
//		 * short 阶级
//		 * short 等级
//		 * short 本阶最大等级
//		 * float 主城移动速率
//		 * float 战斗移动速率
//		 * byte 品质		
		msg.writeInt(nextTemp.Model);
		msg.writeUtf8String(nextTemp.Name);
		msg.writeInt(nextTemp.HeadID);
		msg.writeInt(nextTemp.res_id);
		msg.writeShort(nextTemp.bigLv);
		msg.writeShort(KMountDataManager.mMountUpBigLvDataManager.getData(nextTemp.bigLv-1).lv);
		msg.writeShort(KMountDataManager.mMountUpBigLvDataManager.getData(nextTemp.bigLv).lv);
		msg.writeFloat(nextTemp.cityMoveSpeedup);
		msg.writeFloat(nextTemp.fightMoveSpeed);
		msg.writeByte(nextTemp.quaEnum.sign);
		
//		 * 
//		 * byte 基础属性数量n
//		 * for(0~n){
//		 * 	int 属性类型
//		 * 	int 基础属性值
//		 * }
//		 * 
//		 * byte 技能数量n
//		 * for(0~n){
//		 * 	int 技能模板ID
//		 * }
//		 * int 战斗力
		
		msg.writeByte(nextTemp.allEffects.size());
		for(Entry<KGameAttrType, Integer> e:nextTemp.allEffects.entrySet()){
			msg.writeInt(e.getKey().sign);
			msg.writeInt(e.getValue());
		}
		
		msg.writeByte(nextTemp.skillIdList.size());
		for(int skillId:nextTemp.skillIdList){
			msg.writeInt(skillId);
		}
		
		long battlePower = nextTemp.getBattlePower(role.getId());
		msg.writeLong(battlePower);
	}

	/**
	 * <pre>
	 * 参考{@link KMountProtocol#SM_PUSH_MOUNT_CONSTANCE}
	 * 
	 * @param msg
	 * @author CamusHuang
	 * @creation 2015-1-8 下午5:34:25
	 * </pre>
	 */
	public static void packMountConstance(KGameMessage msg) {
//		 * byte 材料数量n
//		 * for(0~n){
//		 * 	材料参考{@link KItemProtocol#MSG_STRUCT_ITEM_DETAILS}
//		 * 	byte 货币类型
//		 * 	long 货币数量
//		 * 	int 加经验值
//		 * }
		Map<String, KMountUpLvData> map=KMountDataManager.mMountUpLvDataManager.getDataCache();
		msg.writeByte(map.size());
		for (KMountUpLvData data : map.values()) {
			KItemMsgPackCenter.packItem(msg, data.itemStruct.getItemTemplate(), data.itemStruct.itemCount);
			msg.writeByte(data.moneyStruct.currencyType.sign);
			msg.writeLong(data.moneyStruct.currencyCount);
			msg.writeInt(data.addExp);
		}
	}
	
	public static void packMountDataForGM(KRole role, List<String> infos) {
		infos.add("【名称】" + '\t' +"【型号】" + '\t' + "【阶级】" + '\t' + "【等级】" + '\t' + "【战斗力】");
		//
		KMountSet mountSet = KMountModuleExtension.getMountSet(role.getId());
		for(KMount mount:mountSet.getMountCache().values()){
			KMountTemplate temp = mount.getTemplate();
			
			long battlePower = KSupportFactory.getRoleModuleSupport().calculateBattlePower(mount.getAttsForAll(), role.getId());
			
			infos.add(temp.Name + '\t' + temp.Model + '\t' + temp.bigLv + '\t' + mount.getLevel() + '\t' + battlePower);
			{
				for(int skillId : temp.skillIdList){
					KRoleIniSkillTemp skillTemp = KSupportFactory.getSkillModuleSupport().getMountSkillTemplate(skillId);
					KMountSkill skillLv = mount.getSkillCache().get(skillId);
					infos.add("【技能】" + '\t' +skillTemp.name + '\t' +"【lv】" + '\t' +(skillLv==null?1:skillLv.getLv()));
				}
			}

			{
				for(int equipID : mount.getEquiCache()){
					KMountEquiTemp equiTemp = KMountDataManager.mMountEquiDataManager.getData(equipID);
					infos.add("【装备】" + '\t' +equiTemp.name);
				}
			}
		}
	}

	static void packMountForOtherRole(KGameMessage msg, KRole role) {
		KMountSet set = KMountModuleExtension.getMountSet(role.getId());
		set.rwLock.lock();
		try {
			int writeIndex=msg.writerIndex();
			msg.writeByte(0);
			int count = 0;
			for(Entry<Integer, LinkedHashMap<Integer, KMountTemplate>> e:KMountDataManager.mMountTemplateManager.getDataCache().entrySet()){
				int model = e.getKey();
				KMount mount = set.getMountByModel(model);
				if(mount==null){
					continue;
				} else {
					packMountForOtherRole(msg, role, mount);
					count++;
				}
			}
			msg.setByte(writeIndex, count);
		} finally {
			set.rwLock.unlock();
		}
	}
	
	private static void packMountForOtherRole(KGameMessage msg, KRole role, KMount mount) {
		KMountTemplate nowTemp = mount.getTemplate();
		//
		int nowLv = mount==null?1:mount.getLevel();
		//
//		 * boolean 是否已经获得此机甲
//		 * int 机甲型号
//		 * String 名称
//		 * int 头像资源ID
//		 * int 形象资源ID
//		 * short 阶级
//		 * short 等级
//		 * byte 品质
//		 * String 型号类型描述
//		 * String 机甲特长

		msg.writeBoolean(mount!=null);
		msg.writeInt(nowTemp.Model);
		msg.writeUtf8String(nowTemp.Name);
		msg.writeInt(nowTemp.HeadID);
		msg.writeInt(nowTemp.res_id);
		msg.writeShort(nowTemp.bigLv);
		msg.writeShort(nowLv);
		msg.writeByte(nowTemp.quaEnum.sign);
		msg.writeUtf8String(nowTemp.ModelDescription);
		msg.writeUtf8String(nowTemp.SpecialtyDescription);
		
//		 * 
//		 * byte 装备数量n
//		 * for(0~n){
//		 * 	本阶装备参考{@link #MSG_STRUCT_MOUNT_EQUI}
//		 * 	boolean 是否已经获得本阶装备
//		 * }
		msg.writeByte(nowTemp.equiIdList.size());
		for(int index=0;index<nowTemp.equiIdList.size();index++){
			int equiId = nowTemp.equiIdList.get(index);
			KMountEquiTemp equiTemp = KMountDataManager.mMountEquiDataManager.getData(equiId);
			packMountEqui(msg, equiTemp);
			boolean get = mount==null?false:mount.checkEqui(equiId);
			msg.writeBoolean(get);
		}
		

//		 * byte 基础属性数量n
//		 * for(0~n){
//		 * 	int 属性类型
//		 * 	int 基础属性值
//		 * }
//		 * 
//		 * byte 升级属性数量n
//		 * for(0~n){
//		 * 	int 属性类型
//		 *  int 升级属性值
//		 * }
		msg.writeByte(nowTemp.allEffects.size());
		for(Entry<KGameAttrType, Integer> e:nowTemp.allEffects.entrySet()){
			msg.writeInt(e.getKey().sign);
			msg.writeInt(e.getValue());
		}
		KMountLv lvData = KMountDataManager.mMountLvDataManager.getData(nowLv);
		Map<KGameAttrType, Integer> lvAttsMap = lvData.allEffects.get(nowTemp.Model);
		msg.writeByte(lvAttsMap.size());
		for(Entry<KGameAttrType, Integer> e:lvAttsMap.entrySet()){
			msg.writeInt(e.getKey().sign);
			msg.writeInt(e.getValue());
		}
		
//		 * byte 技能数量n
//		 * for(0~n){
//		 * 	int 技能模板ID
//		 * 	int 技能等级
//		 * }
//		 * boolean 是否骑乘中
//		 * 
//		 * long 战斗力
		msg.writeByte(nowTemp.skillIdList.size());
		for(int skillId:nowTemp.skillIdList){
			msg.writeInt(skillId);
			KMountSkill mKMountSkill = mount==null?null:mount.getSkillCache().get(skillId);
			msg.writeInt(mKMountSkill==null?1:mKMountSkill.getLv());
		}
		msg.writeBoolean(mount==null?false:mount.isUsed());
		
		long battlePower = KSupportFactory.getRoleModuleSupport().calculateBattlePower(mount.getAttsForAll(), role.getId());
		msg.writeLong(battlePower);
	}	
}
