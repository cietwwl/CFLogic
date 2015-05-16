package com.kola.kmp.logic.support;

import java.util.List;
import java.util.Set;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.chat.ChatDataAbs;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangMember;
import com.kola.kmp.logic.gang.KGangPositionEnum;
import com.kola.kmp.logic.other.KGangTecTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;

public interface GangModuleSupport {

	/**
	 * <pre>
	 * 获取角色所属的军团名称
	 * 不属于任何军团则返回""
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-1 上午11:58:46
	 * </pre>
	 */
	public String getGangNameByRoleId(long roleId);

	/**
	 * <pre>
	 * 获取角色所属的军团名称
	 * 不属于任何军团则返回0
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-1 上午11:58:46
	 * </pre>
	 */
	public long getGangIdByRoleId(long roleId);

	/**
	 * <pre>
	 * 根据发送者角色ID chatData.receiverId 找到相应军团，遍历军团成员发送聊天内容
	 * 
	 * @param chatData
	 * @param msg
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-12 上午11:05:33
	 * </pre>
	 */
	public int broadcastChatToGang(ChatDataAbs chatData, KGameMessage msg);

	public KGang getGang(long gangId);
	
	public KGang getGangByRoleId(long roleId);

	/**
	 * <pre>
	 * 为指定军团添加经验值
	 * 
	 * @param gangId
	 * @param addExp >0
	 * @param addRescouce >0
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-14 下午7:08:57
	 * </pre>
	 */
	public boolean addGangExp(long gangId, int addExp, int addRescouce);

	/**
	 * <pre>
	 * 角色增加活跃度时，请调用本方法通知军团模块
	 * 会根据公式为所属军团增加繁荣度
	 * 
	 * @param role
	 * @param addVitality >0
	 * @author CamusHuang
	 * @creation 2014-5-7 下午4:21:44
	 * </pre>
	 */
	public void notifyVitalityAdd(KRole role, int addVitality);
	
	/**
	 * <pre>
	 * 角色战力变化时通知
	 * 内部仅用于设置脏标志
	 * 
	 * @param role
	 * @param newBattlePow
	 * @author CamusHuang
	 * @creation 2014-10-30 下午5:43:21
	 * </pre>
	 */
	public void notifyRoleBattlePowerChange(KRole role, int newBattlePow);

	/**
	 * <pre>
	 * 增减军团奖金
	 * 内部负责向军团成员同步信息
	 * 
	 * @param gangId
	 * @param price 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-13 上午11:25:05
	 * </pre>
	 */
	public CommonResult changeGangResource(long gangId, int changeValue);
	
	/**
	 * <pre>
	 * 获取指定角色的勋章ICON
	 * 没有勋章则返回0
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-21 下午3:58:10
	 * </pre>
	 */
	public int getMedalIcon(long roleId);	
	
	/**
	 * 获取角色在地图中显示的军团信息
	 * 格式：<军团名>职位
	 * @param roleId
	 * @return
	 */
	public String getGangMapShowNameByRole(long roleId);
	
	/**
	 * <pre>
	 * 获取角色所属军团的科技效果
	 * 具体效果类型请参考{@link KGangTecTypeEnum}
	 * 
	 * @param type
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-10-8 下午12:06:34
	 * </pre>
	 */
	public int getGangEffect(KGangTecTypeEnum type, long roleId);
	
	/**
	 * <pre>
	 * 获取角色所属军团的科技效果描述
	 * 具体效果类型请参考{@link KGangTecTypeEnum}
	 * 
	 * @param type
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-10-8 下午12:06:34
	 * </pre>
	 */
	public String getGangEffectDescr(KGangTecTypeEnum type, long roleId);
	
	/**
	 * <pre>
	 * 返回指定军团指定职位的成员角色ID
	 * 
	 * @param gangId
	 * @param positions
	 * @return
	 * @author CamusHuang
	 * @creation 2014-11-27 下午9:47:02
	 * </pre>
	 */
	public List<Long> searchPositions(long gangId, Set<KGangPositionEnum> positions);
}
