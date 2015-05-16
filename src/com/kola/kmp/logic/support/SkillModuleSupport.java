package com.kola.kmp.logic.support;

import java.util.List;

import com.kola.kmp.logic.combat.api.ICombatMinionTemplateData;
import com.kola.kmp.logic.combat.api.ICombatSkillData;
import com.kola.kmp.logic.combat.api.ICombatSkillTemplateData;
import com.kola.kmp.logic.combat.state.ICombatStateTemplate;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleIniSkillTemp;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRolePasSkillTemp;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-2-21 下午4:58:49
 * </pre>
 */
public interface SkillModuleSupport {
	
	/**
	 * 
	 * <pre>
	 * 获取所有主动技能的模板数据
	 * 本方法仅供服务器初始化的时候，给战斗模块使用
	 * </pre>
	 * @deprecated 本方法仅供战斗模块初始化时调用，反复调用，会返回一个empty的list
	 * @return
	 */
	public List<ICombatSkillTemplateData> getAllSkillTemplateData();
	
	/**
	 * 
	 * <pre>
	 * 获取所有被动技能的模板数据
	 * 本方法仅供服务器初始化的时候，给战斗模块使用
	 * </pre>
	 * @deprecated 本方法仅供战斗模块初始化时调用，反复调用，会返回一个empty的list
	 * @return
	 */
	public List<ICombatSkillTemplateData> getAllPassiveSkillTemplateData();
	
	/**
	 * <pre>
	 * 获取所有状态数据
	 * </pre>
	 * @deprecated 本方法仅供战斗模块初始化时调用，反复调用，会返回一个empty的list
	 * @return
	 */
	public List<ICombatStateTemplate> getAllStateTemplateData();

	/**
	 * <pre>
	 * 为角色添加技能，主要由境界、道具模块调用
	 * 本方法内部会发送最新技能列表给客户端
	 * 
	 * @deprecated 主要由境界、道具模块调用
	 * @param roleId
	 * @param isInitiative true:主动技能，false:被动技能
	 * @param skillTemplateId
	 * @param sourceTips 来源
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-7 下午4:28:33
	 * </pre>
	 */
	public boolean addSkillToRole(long roleId, boolean isInitiative, int skillTemplateId, String sourceTips);

	/**
	 * <pre>
	 * 获取角色主动技能模板
	 * 
	 * @param templateId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-7 下午4:25:20
	 * </pre>
	 */
	public KRoleIniSkillTemp getRoleIniSkillTemplate(int templateId);

	/**
	 * <pre>
	 * 获取角色被动技能模板
	 * 
	 * @param templateId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-26 上午11:28:21
	 * </pre>
	 */
	public KRolePasSkillTemp getRolePasSkillTemplate(int templateId);
	
	/**
	 * <pre>
	 * 获取随从被动技能模板
	 * 
	 * </pre>
	 * @param templateId
	 * @return
	 */
	public KRolePasSkillTemp getPetPasSkillTemplate(int templateId);
	
	/**
	 * <pre>
	 * 根据模板id获取被动技能模板
	 * 不区分角色还是随从
	 * </pre>
	 * @param templateId
	 * @return
	 */
	public KRolePasSkillTemp getPasSkillTemplate(int templateId);

	/**
	 * <pre>
	 * 获取机甲技能模板
	 * 
	 * @param templateId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-7 下午4:25:20
	 * </pre>
	 */
	public KRoleIniSkillTemp getMountSkillTemplate(int templateId);
	
	/**
	 * <pre>
	 * 获取怪物技能模板
	 * 
	 * </pre>
	 * @param templateId
	 * @return
	 */
	public KRoleIniSkillTemp getMonsterSkillTemplate(int templateId);

	/**
	 * <pre>
	 * 获取宠物技能模板
	 * 
	 * @param templateId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-7 下午4:25:20
	 * </pre>
	 */
	public KRoleIniSkillTemp getPetSkillTemplate(int templateId);

	/**
	 * 
	 * 获取角色所有已经掌握的主动技能模板ID
	 * 
	 * @param roleId
	 * @return
	 */
	public List<Integer> getRoleAllIniSkills(long roleId);

	/**
	 * <pre>
	 * 获取角色所有已经掌握的被动技能模板ID
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-26 上午11:27:49
	 * </pre>
	 */
	public List<Integer> getRoleAllPasSkills(long roleId);

	/**
	 * 
	 * 获取角色正在使用的主动技能模板ID
	 * 
	 * @param roleId
	 * @return
	 */
	public List<Integer> getRoleInUseIniSkills(long roleId);
	
	/**
	 * 
	 * @param roleId
	 * @return
	 */
	public List<ICombatSkillData> getRoleInUseIniSkillInstances(long roleId);
	
	/**
	 * 
	 * @param roleId
	 * @return
	 */
	public List<ICombatSkillData> getRolePasSkillInstances(long roleId);

	/**
	 * 
	 * @param stateTemplateId
	 * @return
	 */
	public ICombatStateTemplate getStateTemplate(int stateTemplateId);
	
	/**
	 * 
	 * @param templateId
	 * @return
	 */
	public ICombatMinionTemplateData getMinionTemplateData(int templateId);
	
	/**
	 * 
	 * @param roleId
	 * @return
	 */
	public int getSkillBattlePower(long roleId, int roleLv);
	
	/**
	 * 
	 * 获取游戏中角色所能携带的最大技能数量
	 * 
	 * @return
	 */
	public int getMaxSkillSlotCount();

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
	 * 
	 * 判断某个技能模板id是不是角色技能
	 * 
	 * @param templateId
	 * @return
	 */
	public boolean isRoleSkill(int templateId);
}
