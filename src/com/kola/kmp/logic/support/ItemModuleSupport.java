package com.kola.kmp.logic.support;

import java.util.List;
import java.util.Map;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.item.KItem;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.map.KGameMapEntity.RoleEquipShowData;
import com.kola.kmp.logic.other.KDataStructs.EquiSetStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;
import com.kola.kmp.protocol.item.KItemProtocol;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-2-21 下午4:58:49
 * </pre>
 */
public interface ItemModuleSupport {

	public KItemTempAbs getItemTemplate(String itemCode);

	/**
	 * <pre>
	 * 增加背包中的道具
	 * 内部会与客户端进行道具同步
	 * 
	 * @param role
	 * @param itemCode
	 * @param addCount 必须>0
	 * @param sourceTips 来源
	 * @return
	 * @author CamusHuang
	 * @creation 2012-11-14 下午7:07:01
	 * </pre>
	 */
	public ItemResult_AddItem addItemToBag(KRole role, String itemCode, long addCount, String sourceTips);

	/**
	 * <pre>
	 * 增加背包中的道具
	 * 要不就全加，要不就一个都不加
	 * 内部会与客户端进行道具同步
	 * 
	 * @param role
	 * @param itemCounts （道具数量必须>0）
	 * @param sourceTips 来源，可为null
	 * @return 
	 * @author CamusHuang
	 * @creation 2012-12-5 下午12:35:42
	 * </pre>
	 */
	public ItemResult_AddItem addItemsToBag(KRole role, List<ItemCountStruct> itemCounts, String sourceTips);	
	
	/**
	 * <pre>
	 * 
	 * 
	 * @param role
	 * @param itemCount
	 * @param sourceTips 来源
	 * @return
	 * @author CamusHuang
	 * @creation 2014-8-19 下午4:12:04
	 * </pre>
	 */
	public ItemResult_AddItem addItemToBag(KRole role, ItemCountStruct itemCount, String sourceTips);

	/**
	 * <pre>
	 * 删除角色<strong>背包</strong>指定itemCode的指定数量(count)
	 * 内部会与客户端进行道具同步
	 * 
	 * @param roleId
	 * @param itemCode
	 * @param count
	 * @return true=删除成功 false=删除失败
	 * </pre>
	 */
	public boolean removeItemFromBag(long roleId, String itemCode, long count);
	
	/**
	 * <pre>
	 * 删除角色<strong>背包</strong>指定itemId的指定数量(count)
	 * 内部会与客户端进行道具同步
	 * 
	 * @param roleId
	 * @param itemId
	 * @param count 只有可合并道具才允许>1
	 * @return true=删除成功 false=删除失败
	 * @author CamusHuang
	 * @creation 2014-3-8 下午5:00:41
	 * </pre>
	 */
	public boolean removeItemFromBag(long roleId, long itemId, long count);
	
	/**
	 * <pre>
	 * 检查背包是否能添加指定的道具
	 * 只检查，不实际添加进背包
	 * 
	 * @param roleId
	 * @param itemCounts （不允许包含重复的ItemCode）
	 * @return
	 * @author CamusHuang
	 * @creation 2012-12-7 上午10:28:21
	 * </pre>
	 */
	public boolean isCanAddItemsToBag(long roleId, List<ItemCountStruct> itemCounts);

	/**
	 * <pre>
	 * 检查背包空格数量
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2012-12-7 上午11:07:22
	 * </pre>
	 */
	public int checkEmptyVolumeInBag(long roleId);
	
	/**
	 * <pre>
	 * 根据道具模版ID检测角色背包存放该类型道具的数量，没有返回0
	 * 即：获取角色<strong>背包</strong>指定itemCode的道具的总数量
	 * 
	 * @param roleId
	 * @param itemCode
	 * @return
	 * </pre>
	 */
	public long checkItemCountInBag(long roleId, String itemCode);

	/**
	 * <pre>
	 * 
	 * 检查角色背包是否全部满足checkMap里的道具的数量，如果有一个不符合，立即返回false
	 * 
	 * </pre>
	 * 
	 * @param roleId
	 * @param checkMap <ItemCode,要求数量>
	 * @return 缺少道具则返回相应的道具ItemCode
	 */
	public String checkItemCountInBag(long roleId, Map<String, Integer> checkMap);

	/**
	 * <pre>
	 * 对道具集加锁
	 * 
	 * @deprecated 注意防止死锁，货币、角色模块慎用本方法
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2013-6-15 上午10:24:45
	 * </pre>
	 */
	public void lockItemSet(long roleId);

	/**
	 * <pre>
	 * 对道具集解锁
	 * 
	 * @deprecated 注意防止死锁，货币、角色模块慎用本方法
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2013-6-15 上午10:25:12
	 * </pre>
	 */
	public void unlockItemSet(long roleId);
	
	/**
	 * <pre>
	 * 打包装备的简单数据
	 * {@link com.kola.kmp.protocol.fight.KFightProtocol#BATTLE_EQUIPMENT_INFO}
	 * </pre>
	 * @param roleId
	 */
	public void packSimpleEquipmentInfo(KGameMessage msg, long roleId);
	
	
	/**
	 * 获取装备的地图打包数据
	 * @param roleId
	 * @return
	 */
	public List<RoleEquipShowData> getRoleEquipShowDataList(long roleId);
	
	/**
	 * 
	 * 获取副武器参数，如果没有副武器，则返回null
	 * 
	 * @param roleId
	 * @return
	 */
	public ISecondWeapon getSecondWeaponArgs(long roleId);
	
	/**
	 * 
	 * 获取角色武器的icon资源id
	 * 
	 * @param roleId
	 * @return 武器的icon资源id，[0]=主武器，[1]=副武器
	 */
	public int[] getWeaponIcons(long roleId);
	
	/**
	 * 
	 * 打包装备栏数据
	 * 
	 * @param roleId
	 * @param msg
	 */
	public List<KItem> getRoleEquipmentList(long roleId);
	
	/**
	 * 
	 * 将一个道具（属于角色）的数据打包到消息中
	 * 参考{@link KItemProtocol#MSG_STRUCT_ITEM_DETAILS}
	 * 
	 * @param item
	 * @param roleLv
	 * @param msg
	 */
	public void packItemDataToMsg(KItem item, int roleLv, KGameMessage msg);
	
	/**
	 * <pre>
	 * 打包装备套装数据
	 * 参考{@link KItemProtocol#SM_PUSH_EQUISET_DATA}
	 * 
	 * @param role
	 * @param msg
	 * @author CamusHuang
	 * @creation 2014-7-16 下午3:09:15
	 * </pre>
	 */
	public void packEquiSetDataToMsg(KRole role, KGameMessage msg);
	
	/**
	 * <pre>
	 * 获取指定角色的套装地图资源ID
	 * 
	 * @param roleId
	 * @return []{升星套装地图资源ID,宝石套装地图资源ID}
	 * @author CamusHuang
	 * @creation 2014-8-15 上午10:45:31
	 * </pre>
	 */
	public int[] getEquiSetMapResIds(long roleId);	
	
	/**
	 * <pre>
	 * 获取装备套装等级
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-9-2 上午11:48:26
	 * </pre>
	 */
	public EquiSetStruct getEquiSets(long roleId);

	/**
	 * 
	 * 服务器数据结构
	 * 
	 * @author PERRY CHAN
	 */
	public static interface ISecondWeapon {
		
		/**
		 * 副武器类型：盾牌
		 */
		public static final byte SECOND_WEAPON_TYPE_SHIELD = 1;
		
		/**
		 * 副武器类型：大刀
		 */
		public static final byte SECOND_WEAPON_TYPE_BROADSWORD = 2;
		
		/**
		 * 副武器类型：机枪
		 */
		public static final byte SECOND_WEAPON_TYPE_MACHINE_GUN = 3;
		
		/**
		 * 
		 * 获取副武器的类型
		 * 
		 * @return
		 */
		public byte getType();
		
		/**
		 * 
		 * 获取格挡值
		 * 
		 * @return
		 */
		public int getBlock();
		
		/**
		 * 
		 * 获取聚力伤害上限（万分比）
		 * 
		 * @return
		 */
		public int getCohesionFixedDm();
		
		/**
		 * 
		 * 获取聚力时间
		 * 
		 * @return
		 */
		public int getCohesionPct();
		
		/**
		 * 
		 * 获取弹夹容量
		 * 
		 * @return
		 */
		public int getClip();
		
		/**
		 * 
		 * 获取机枪过热的冷却时间
		 * 
		 * @return
		 */
		public long getMachineGunCD();
		
		/**
		 * 
		 * 获取机枪增加的伤害万分比
		 * 
		 * @return
		 */
		public int getMachineGunDmPct();
	}

	/**
	 * <pre>
	 * 克隆角色数据
	 * 
	 * @param myRole
	 * @param srcRole
	 * @author CamusHuang
	 * @creation 2014-10-15 上午11:50:31
	 * </pre>
	 */
	public void cloneRoleByCamus(KRole myRole, KRole srcRole);

	/**
	 * <pre>
	 * 是否有红色武器
	 * 
	 * @param id
	 * @return
	 * @author CamusHuang
	 * @creation 2015-1-26 下午6:14:53
	 * </pre>
	 */
	public boolean isRedWepond(long id);
}
