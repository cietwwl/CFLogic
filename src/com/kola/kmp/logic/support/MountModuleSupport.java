package com.kola.kmp.logic.support;

import java.util.List;
import java.util.Map;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.combat.api.ICombatMount;
import com.kola.kmp.logic.mount.KMount;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountTemplate;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.role.KRole;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-2-21 下午4:58:49
 * </pre>
 */
public interface MountModuleSupport {

	/**
	 * <pre>
	 * 获取出战机甲的（角色主动）技能
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-25 下午6:06:54
	 * </pre>
	 */
	public List<Integer> getMountRoleIniSkills(long roleId);
	
	/**
	 * <pre>
	 * 获取出战机甲资源ID，如果角色没有装备机甲，返回-1。
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-8 上午9:55:58
	 * </pre>
	 */
	public int getRoleMountResId(KRole role);
	
	/**
	 * <pre>
	 * 获取出战机甲速度加成，如果角色没有出战机甲，返回0。
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-8 上午9:55:58
	 * </pre>
	 */
	public float getRoleMountMoveSpeedup(KRole role);
	
	/**
	 * <pre>
	 * 获取角色可以召唤战斗的机甲
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-10-16 下午12:09:08
	 * </pre>
	 */
	public ICombatMount getMountCanWarOfRole(long roleId);
	
	/**
	 * <pre>
	 * 获取角色出战机甲
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-10-16 下午12:09:30
	 * </pre>
	 */
	public KMount getMountOfRole(long roleId);
	
	/**
	 * <pre>
	 * 如果存在出战机甲，则把数据打包到消息中，参考{@link com.kola.kmp.protocol.mount.KMountProtocol#MSG_STRUCT_MOUNT_DETAILS}
	 * 如果不存在，则不用做任何事情
	 * 
	 * @param msg
	 * @param role
	 * @author CamusHuang
	 * @creation 2014-7-14 下午5:05:10
	 * </pre>
	 */
	public void packMountDataToMsgForOtherRole(KGameMessage msg, KRole role);
	
	/**
	 * <pre>
	 * 给予角色新手体验机甲
	 * 
	 * @deprecated 已失效，空实现
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2014-7-17 上午11:34:10
	 * </pre>
	 */
	public void notifyStartMountForNewRole(long roleId);
	
	/**
	 * <pre>
	 * 取消角色新手体验机甲
	 * 
	 * @deprecated 已失效，空实现
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2014-7-17 上午11:34:25
	 * </pre>
	 */
	public void notifyCancelMountFromNewRole(long roleId);
	
	/**
	 * <pre>
	 * 开启角色的机甲
	 * 
	 * @deprecated 已失效，空实现
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2014-4-18 下午4:40:32
	 * </pre>
	 */
	public void notifyStartMount(long roleId);
	
	/**
	 * <pre>
	 * 赠送机甲给玩家
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2014-4-18 下午4:40:32
	 * </pre>
	 */
	public KActionResult<KMount> presentMount(KRole role, KMountTemplate temp, String srouceTips);
	
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
	 * 按机甲模板ID(区别于型号)获取模板
	 * 
	 * 
	 * @param mountTempId
	 * @return
	 * @author CamusHuang
	 * @creation 2015-1-31 下午3:09:32
	 * </pre>
	 */
	public KMountTemplate getMountTemplate(int mountTempId);
}
