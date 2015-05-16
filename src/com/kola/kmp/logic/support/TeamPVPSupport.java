package com.kola.kmp.logic.support;

import java.util.Map;

import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.role.KRole;

/**
 * 
 * @author PERRY CHAN
 */
public interface TeamPVPSupport {

	/**
	 * <pre>
	 * 
	 * 
	 * @param elementId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-9-15 下午3:31:46
	 * </pre>
	 */
	public int getTeamPVPGoodCount(long elementId);
	
	/**
	 * 
	 * @param teamId
	 * @param count
	 */
	public void increaseTeamPVPGoodCount(long teamId, int count);
	
	/**
	 * 
	 * 好友添加通知
	 * 
	 * @param hostRoleId 主人id
	 * @param friendId 好友id
	 */
	public void notifyFriendAdded(long hostRoleId, long friendId);
	
	/**
	 * 
	 * @param hostRoleId
	 * @param friendId
	 */
	public void notifyFriendRemoved(long hostRoleId, long friendId);
	
	/**
	 * 
	 * @param robotId
	 * @return
	 */
	public String getRobotFashion(long robotId);
	
	/**
	 * 
	 * @param roleId
	 */
	public void notifyRoleEquipmentResUpdate(long roleId);

	/**
	 * 
	 * @param roleId
	 */
	public void notifyRoleEquipmentSetResUpdate(long roleId);

	/**
	 * 
	 * @param roleId
	 */
	public void notifyRoleFashionUpdate(long roleId);
	
	/**
	 * 
	 * 获取队伍竞技的套装属性加成
	 * 
	 * @param roleId
	 * @return 数组，[0]表示当前，[1]表示下一级，如果没有下一级，则为null
	 */
	public ITeamPVPAttrSetInfo[] getTeamPVPAttrSetInfo(long roleId);
	
	/**
	 * 退队、加入队伍、创建队伍、队伍段位变化时通知
	 * 
	 * @param role
	 */
	public void notifyRoleTeamDataChange(KRole role);
	
	public interface ITeamPVPAttrSetInfo {
		
		/**
		 * 
		 * @return
		 */
		public String getSetName();
		
		/**
		 * 
		 * @return
		 */
		public int getLevel();
		
		/**
		 * 
		 * @return
		 */
		public boolean isActivate();
		
		/**
		 * 
		 * @return
		 */
		public Map<KGameAttrType, Integer> getAttrMap();
	}
}
