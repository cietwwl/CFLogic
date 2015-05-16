package com.kola.kmp.logic.support;

import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.vip.KVIPUpLvListener;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;

public interface VIPModuleSupport {

	/**
	 * <pre>
	 * 
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-2 下午6:15:31
	 * </pre>
	 */
	public VIPLevelData getVIPLevelData(long roleId);

	/**
	 * <pre>
	 * 
	 * @param viplv
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-2 下午6:15:31
	 * </pre>
	 */
	public VIPLevelData getVIPLevelData(int viplv);

	public int getVipLv(long roleId);

	/**
	 * <pre>
	 * 玩家总共充了多少钻石
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2013-12-20 下午2:34:03
	 * </pre>
	 */
	public int getTotalCharge(long roleId);
	
	/**
	 * <pre>
	 * 当前等级的VIP经验值
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-6-9 上午10:46:22
	 * </pre>
	 */
	public int getVipExp(long roleId);

	/**
	 * <pre>
	 * 角色充值时必须通知本方法
	 * 增加VIP经验
	 * 
	 * @param money 钻石数量
	 * @author CamusHuang
	 * @creation 2013-4-23 下午8:08:06
	 * </pre>
	 */
	public void notifyCharge(KRole role, int money);
	
	
	/**
	 * <pre>
	 * 添加VIP升级监听器
	 * 
	 * @param vip
	 * @param oldVipLv
	 * @author CamusHuang
	 * @creation 2013-11-8 下午12:04:41
	 * </pre>
	 */
	public void addVipUpLvListener(KVIPUpLvListener listener);

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
}
