package com.kola.kmp.logic.support;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.koala.game.KGameMessage;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.role.RoleBaseInfo;
import com.kola.kgame.cache.role.RoleExtCA;
import com.kola.kmp.logic.combat.api.ICombatSkillData;
import com.kola.kmp.logic.flow.KRoleAttrModifyType;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KRoleExtTypeEnum;
import com.kola.kmp.logic.role.IRoleBaseInfo;
import com.kola.kmp.logic.role.IRoleMapData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.role.KRoleTemplate;
import com.kola.kmp.logic.support.ItemModuleSupport.ISecondWeapon;
import com.kola.kmp.logic.util.IRoleMapResInfo;

/**
 * 
 * @author PERRY CHAN
 */
public interface RoleModuleSupport {

	/**
	 * 
	 * <pre>
	 * 获取角色的地图相关的数据
	 * 如果角色不在缓存中，会通知缓存加载角色所有数据
	 * </pre>
	 * 
	 * @param roleId
	 * @return
	 */
	public IRoleMapData getRoleMapData(long roleId);

	/**
	 * 
	 * <pre>
	 * 获取session绑定的player正在使用的角色
	 * 如果角色不在缓存中，会通知缓存加载角色所有数据
	 * </pre>
	 * 
	 * @param session
	 * @return
	 */
	public KRole getRole(KGamePlayerSession session);

	/**
	 * 
	 * <pre>
	 * 获取角色实例
	 * 如果角色不在缓存中，会通知缓存，只加载<b><font color="ff0000">角色</font></b>数据，不加载其他数据
	 * </pre>
	 * 
	 * @param roleId
	 * @return
	 */
	public KRole getRole(long roleId);
	
	/**
	 * 
	 * <pre>
	 * 获取KRole，并且如果之前没有初始化数据的话，强制进行一次初始化。
	 * 一般情况下，角色只有在第一次上线之后才会进行初始化。所以，如果
	 * 角色从未上过线，那么，他的数据可能就永远没有初始化，这样，在某
	 * 些情况下可能会出现数据异常的情况，所以这里提供一个强制初始化的模式。
	 * <b><font color="ff0000">但是，不建议经常使用此方法，当对战斗数据敏感的时候，才建议
	 * 是用此方法获取。</font></b>
	 * </pre>
	 * @param roleId
	 * @return
	 */
	public KRole getRoleWithOfflineAttr(long roleId);
	
	/**
	 * 
	 * 获取所有在缓存中的roleId的集合
	 * 
	 * @return
	 */
	public Set<Long> getRoleIdCache();
	
	/**
	 * 
	 * <pre>
	 * 获取角色等级
	 * 如果角色不在缓存中，会通知缓存，只加载<b><font color="ff0000">角色</font></b>数据，不加载其他数据
	 * </pre>
	 * 
	 * @param roleId
	 * @return
	 */
	public int getLevel(long roleId);

	/**
	 * 
	 * @param roleId
	 * @param msg
	 * @return
	 */
	public boolean sendMsg(long roleId, KGameMessage msg);

	/**
	 * 
	 * @param roleId
	 * @param addAtt
	 */
	public void notifyUseConsumeItemEffect(long roleId, AttValueStruct addAtt, KRoleAttrModifyType type, Object... args);

//	/**
//	 * <pre>
//	 * 角色装备变化通知
//	 * 请通过{@link ItemModuleSupport#getEquipmentRoleEffects(long)}获取角色属性加成
//	 * 
//	 * @param roleId
//	 * @author CamusHuang
//	 * @creation 2014-3-4 上午10:53:35
//	 * </pre>
//	 */
//	public void notifyEquipmentChange(long roleId);
//	
//	/**
//	 * <pre>
//	 * 角色时装变化通知
//	 * 请通过{@link FashionModuleSupport#getRoleFashionEffect(long)}获取角色属性加成
//	 * 
//	 * @param roleId
//	 * @author CamusHuang
//	 * @creation 2014-3-17 上午10:42:04
//	 * </pre>
//	 */
//	public void notifyFashionChange(long roleId);
//	
//	/**
//	 * <pre>
//	 * 角色时装变化通知
//	 * 请通过{@link MountModuleSupport#getMountRoleEffects(long roleId, int roleLv)}获取角色属性加成
//	 * 请通过{@link MountModuleSupport#getMountRoleIniSkills(long)}获取角色主动技能数据
//	 * 请通过{@link MountModuleSupport#getMountRoleCommonAtkId(long)}获取角色普通攻击ID
//	 * 
//	 * @param roleId
//	 * @author CamusHuang
//	 * @creation 2014-3-17 上午10:42:04
//	 * </pre>
//	 */
//	public void notifyMountChange(long roleId);
	
//	/**
//	 * <pre>
//	 * 通知角色模块，随从影响的属性发生变化
//	 * 角色模块通过{@link PetModuleSupport#getEffectRoleAttribute(long)}获取加成的属性
//	 * </pre> 
//	 * @param roleId
//	 */
//	public void notifyPetEffectAttrChange(long roleId);
	
	/**
	 * 
	 * @param roleId
	 * @param providerType
	 */
	public void notifyEffectAttrChange(long roleId, int providerType);
	
	/**
	 * 
	 * @param roleId
	 */
	public void notifySecondWeaponUpdate(long roleId, ISecondWeapon weapon);
	
	/**
	 * 
	 * @param roleId
	 * @param exp
	 * @return
	 */
	public int addExp(long roleId, int exp, KRoleAttrModifyType type, Object... args);

	/**
	 * <pre>
	 * 获取所有在线角色ID列表
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-4 下午8:20:03
	 * </pre>
	 */
	public List<Long> getAllOnLineRoleIds();
	
	/**
	 * <pre>
	 * 获取所有在线角色数量
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-4 下午8:20:03
	 * </pre>
	 */
	public int getAllOnLineRoleNum();

	/**
	 * <pre>
	 * 
	 * 
	 * @param roleName
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-5 下午7:21:11
	 * </pre>
	 */
	public KRole getRole(String roleName);
	
	/**
	 * 
	 * 获取playerId所有的角色的基本信息
	 * 
	 * @param session
	 * @return
	 */
	public List<IRoleBaseInfo> getRoleList(long playerId);
	
	/**
	 * 
	 * @param roleId
	 * @return
	 */
	public RoleBaseInfo getRoleBaseInfo(long roleId);

	/**
	 * <pre>
	 * 获取角色名称
	 * 若不存在则返回""
	 * 
	 * @param receiverId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-7 下午6:25:26
	 * </pre>
	 */
	public String getRoleName(long roleId);
	
	/**
	 * <pre>
	 * 获取染色的角色名称
	 * 若不存在则返回""
	 * 
	 * @param receiverId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-7 下午6:25:26
	 * </pre>
	 */
	public String getRoleExtName(long roleId);

	/**
	 * <pre>
	 * 发送奖励
	 * {@link AttValueStruct#usetime}字段无效，请忽略此字段
	 * 
	 * @param role
	 * @param attList
	 * @author CamusHuang
	 * @creation 2014-3-12 下午3:51:06
	 * </pre>
	 */
	public void addAttsFromBaseReward(KRole role, List<AttValueStruct> attList, KRoleAttrModifyType type, Object... args);
	
	/**
	 * 
	 * 计算属性所能产生的战斗力
	 * 
	 * @param attrMap
	 * @param roleId 角色id。由于部分属性是与角色等级挂钩，所以需要传入角色id以获取角色等级，如果<=0，则默认等级为1
	 * @return
	 */
	public int calculateBattlePower(Map<KGameAttrType, Integer> attrMap, long roleId);
	
	/**
	 * 
	 * 根据指定的等级，计算属性所能产生的战斗力
	 * 
	 * @param attrMap
	 * @param lv
	 * @return
	 */
	public int calculateBattlePower(Map<KGameAttrType, Integer> attrMap, int lv);

	/**
	 * 
	 * 角色所附属于的账号是否首次充值
	 * 
	 * @param roleId
	 * @return
	 */
	public boolean isPlayerFirstCharge(long roleId);

	/**
	 * 
	 * 设置角色所附属于的账号的首充标志位
	 * 
	 * @param roleId
	 * @param value
	 */
	public void setPlayerFirstCharge(long roleId, boolean flag);
	
	/**
	 * 
	 * 获取角色的附加属性集合
	 * 
	 * @param roleId
	 * @return
	 */
	public RoleExtCA getRoleExtCA(long roleId, KRoleExtTypeEnum type, boolean addIfAbsent);
	
	/**
	 * 
	 * @param roleId
	 * @param type
	 * @return
	 */
	public RoleExtCA addExtCAToRole(long roleId, KRoleExtTypeEnum type);
	
	/**
	 * 
	 * 扣减角色体力
	 * 
	 * @param role
	 * @param value <b><font color="ff0000">正整数</font></b>
	 * @return
	 */
	public boolean decreasePhyPower(KRole role, int value, String reason);
	
	/**
	 * 获取指定角色等级的经验上限（即升级所需经验）
	 * @param level
	 * @return
	 */
	public int getUpgradeExp(int level);

	/**
	 * <pre>
	 * 查询角色是否在线，不要求加载数据
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-15 上午9:37:56
	 * </pre>
	 */
	public boolean isRoleOnline(long roleId);
	
	/**
	 * 
	 * 添加体力
	 * 
	 * @param roleId
	 * @param count 添加的数量
	 * @param allowOverflow 是否允许溢出（即是否允许添加之后超出体力上限）。
	 * 如果不允许，假设count+当前体力，会超出体力上限，那么最后的值也只是体力上限，
	 * 多余的会被舍弃
	 */
	public void addPhyPower(long roleId, int count, boolean allowOverflow, String reason);

	/**
	 * <pre>
	 * 体力购买通知，加到满
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2014-5-22 下午12:21:19
	 * </pre>
	 */
	public void notifyPhyPowerBuyFull(long roleId, String reason);
	
	/**
	 * <pre>
	 * 查询角色的体力是否已经到达上限
	 * 
	 * </pre>
	 * @param role
	 * @return
	 */
	public boolean isPhyPowerFull(KRole role);
	
	/**
	 * <pre>
	 * 查询角色的体力已经使用了多少
	 * 
	 * </pre>
	 * @param role
	 * @return
	 */
	public int checkPhyPowerUsed(KRole role);
	
	/**
	 * 
	 * @param roleId
	 * @param energy
	 * @param energyBeans
	 */
	public void syncEnergy(long roleId, int energy, int energyBeans);
	
	/**
	 * 
	 * 通知更新角色装备资源
	 * 
	 * @param roleId
	 */
	public void updateEquipmentRes(long roleId);
	
	/**
	 * 
	 * @param roleId
	 */
	public void updateEquipmentSetRes(long roleId);
	
	/**
	 * 
	 * 通知更新时装资源
	 * 
	 * @param roleId
	 */
	public void updateFashionRes(long roleId);
	
	/**
	 * <pre>
	 * 打包角色的地图显示资源
	 * 消息结构：
	 * byte 职业
	 * {@link com.kola.kmp.protocol.fight.KFightProtocol#BATTLE_EQUIPMENT_INFO}
	 * </pre>
	 * @param msg
	 */
	public void packRoleResToMsg(IRoleMapResInfo info, byte job, KGameMessage msg);
	
	/**
	 * <pre>
	 * 打包角色的地图显示资源
	 * 消息结构：
	 * {@link com.kola.kmp.protocol.fight.KFightProtocol#BATTLE_EQUIPMENT_INFO}
	 * 跟{@link #packRoleResToMsg(IRoleMapResInfo, byte, KGameMessage)的区别是不包含职业}
	 * </pre>
	 * @param msg
	 */
	public void packRoleResToMsg(IRoleMapResInfo info, KGameMessage msg);
	
	/**
	 * <pre>
	 * 检测指定角色ID的角色数据是否在缓存中
	 * </pre> 
	 * @param roleId
	 * @return
	 */
	public boolean isRoleDataInCache(long roleId);
	
	/**
	 * 
	 * @param roleId
	 * @param skill
	 */
	public void notifySkillUpgrade(long roleId, ICombatSkillData skill);
	
	/**
	 * 
	 * @param roleId
	 */
	public void notifySkillListChange(long roleId);
	
	/**
	 * 
	 * @param job
	 * @return
	 */
	public KRoleTemplate getRoleTemplateByJob(byte job);

	/**
	 * <pre>
	 * 获取角色正常满载的体力上限
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-9-15 上午10:43:45
	 * </pre>
	 */
	public int getPhyPowerFullSize();
	
	/**
	 * 
	 * @param lv
	 * @return
	 */
	public int getHpMultiple(int lv);
}
