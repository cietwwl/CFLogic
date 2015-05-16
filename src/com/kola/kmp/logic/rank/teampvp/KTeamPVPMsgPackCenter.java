package com.kola.kmp.logic.rank.teampvp;

import java.util.List;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPManager;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.rank.KRankProtocol;

/**
 * <pre>
 * 由于在打包模块消息的过程中，涉及到访问及复杂的逻辑结构
 * 因此设立此类，用于打包消息
 * 
 * @author CamusHuang
 * @creation 2013-1-5 下午12:03:37
 * </pre>
 */
public class KTeamPVPMsgPackCenter {

	/**
	 * <pre>
	 * 打包天梯榜最强队伍数据
	 * {@link KRankProtocol#天梯冠军榜元素数据}
	 * 
	 * @param msg
	 * @author CamusHuang
	 * @creation 2014-9-3 下午5:01:18
	 * </pre>
	 */
	public static void packTopElement(KGameMessage msg) {

		Object[] objs = KTeamPVPRankLogic.searchTopElement();
		if(objs == null){
			msg.writeBoolean(false);
			return;
		}
		
		KTeamPVPRankTypeEnum enuma = (KTeamPVPRankTypeEnum)objs[0];
		TeamPVPRankElement e = (TeamPVPRankElement)objs[1];
		
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(e.getLeaderRoleId());
		if(role == null){
			msg.writeBoolean(false);
			return;
		}
			
		{
			msg.writeBoolean(true);

			msg.writeLong(e.elementId);
			{				
				msg.writeLong(e.getLeaderRoleId());
				msg.writeUtf8String(e.getLeaderRoleName());
				msg.writeInt(role.getInMapResId());
				msg.writeShort(role.getLevel());
				msg.writeByte(e.getLeaderRoleVip());
				int[] setResIds = KSupportFactory.getItemModuleSupport().getEquiSetMapResIds(e.getLeaderRoleId());
				msg.writeInt(setResIds[1]);
				msg.writeInt(setResIds[0]);
				msg.writeBoolean(KSupportFactory.getItemModuleSupport().isRedWepond(role.getId()));
				//
				KSupportFactory.getRoleModuleSupport().packRoleResToMsg(role, role.getJob(), msg);
			}

			{
				role = KSupportFactory.getRoleModuleSupport().getRole(e.getMemRoleId());
				if (role == null) {
					msg.writeBoolean(false);
				} else {
					msg.writeBoolean(true);
					msg.writeLong(e.getMemRoleId());
					msg.writeUtf8String(e.getMemRoleName());
					msg.writeInt(role.getInMapResId());
					msg.writeShort(role.getLevel());
					msg.writeByte(e.getMemRoleVip());
					int[] setResIds = KSupportFactory.getItemModuleSupport().getEquiSetMapResIds(e.getMemRoleId());
					msg.writeInt(setResIds[1]);
					msg.writeInt(setResIds[0]);
					msg.writeBoolean(KSupportFactory.getItemModuleSupport().isRedWepond(role.getId()));
					//
					KSupportFactory.getRoleModuleSupport().packRoleResToMsg(role, role.getJob(), msg);
				}
			}
			
			msg.writeUtf8String(enuma.name);
			msg.writeInt(e.getBattlePow());
			
			// 被点赞次数
			int count = KSupportFactory.getTeamPVPSupport().getTeamPVPGoodCount(e.elementId);
			msg.writeInt(count);
		}
	}

	/**
	 * <pre>
	 * 参考{@link KRankProtocol#SM_GET_INI_RANKS_RESULT}
	 * 
	 * @param msg
	 * @param role
	 * @param numPerPage
	 * @param pageNum
	 * @author CamusHuang
	 * @creation 2014-4-1 上午10:50:19
	 * </pre>
	 */
	public static void packIniRanks(KGameMessage msg, KRole role, int numPerPage, int pageNum) {

		long tempId = KTeamPVPManager.getTeamIdByRoleId(role.getId());

		msg.writeByte(KTeamPVPRankTypeEnum.size());
		for (KTeamPVPRankTypeEnum RType : KTeamPVPRankTypeEnum.values()) {
			packRank(msg, tempId, RType, 1, numPerPage, pageNum);
		}
	}

	/**
	 * <pre>
	 * 打包排行榜数据
	 * 参考{@link KRankProtocol#天梯排行榜数据}
	 * 
	 * @param msg
	 * @param role
	 * @param rankType
	 * @param startPage
	 * @param numPerPage
	 * @param pageNum
	 * @author CamusHuang
	 * @creation 2014-3-27 下午4:53:55
	 * </pre>
	 */
	private static void packRank(KGameMessage msg, long tempId, KTeamPVPRankTypeEnum rankType, int startPage, int numPerPage, int pageNum) {
		int startIndex = (startPage - 1) * numPerPage;// 起始位置
		int endIndex = startIndex + pageNum * numPerPage;// 结束位置（不包含）
		//
		msg.writeByte(rankType.sign);
		{
			TeamPVPRank<TeamPVPRankElement> rank = KTeamPVPRankLogic.getRank(rankType);
			rank.rwlock.readLock().lock();
			try {
				TeamPVPRankElement rankE = rank.getPublishData().getElement(tempId);
				int myRank = rankE == null ? -1 : rankE.getRank();
				//
				List<TeamPVPRankElement> list = rank.getPublishData().getUnmodifiableElementList();
				packElementList(msg, myRank, list, startIndex, endIndex);
			} finally {
				rank.rwlock.readLock().unlock();
			}
		}
	}

	private static void packElementList(KGameMessage msg, int myRank, List<TeamPVPRankElement> elementList, int startIndex, int endIndex) {
		msg.writeShort(myRank);
		//
		if (startIndex >= elementList.size()) {
			msg.writeShort(0);
			return;
		}
		if (endIndex > elementList.size()) {
			endIndex = elementList.size();
		}
		msg.writeShort(endIndex - startIndex);
		for (int index = startIndex; index < endIndex; index++) {
			packElement(msg, elementList.get(index));
		}
	}

	/**
	 * <pre>
	 * 参考{@link KRankProtocol#天梯排行榜元素基本数据}
	 * 
	 * @param msg
	 * @param e
	 * @author CamusHuang
	 * @creation 2014-4-1 上午10:47:12
	 * </pre>
	 */
	private static void packElement(KGameMessage msg, TeamPVPRankElement e) {
		msg.writeShort(e.getRank());
		msg.writeLong(e.elementId);
		msg.writeUtf8String(e.elementName);
		msg.writeInt(e.getBattlePow());
		msg.writeByte(e.getElementLv());
		msg.writeInt(e.getExp());

		msg.writeLong(e.getLeaderRoleId());
		msg.writeUtf8String(e.getLeaderRoleName());
		msg.writeByte(e.getLeaderRoleVip());
		msg.writeBoolean(e.getMemRoleId() > 0);
		if (e.getMemRoleId() > 0) {
			msg.writeLong(e.getMemRoleId());
			msg.writeUtf8String(e.getMemRoleName());
			msg.writeByte(e.getMemRoleVip());
		}
	}
	
	/**
	 * <pre>
	 * 打包排行榜数据
	 * 参考{@link KRankProtocol#角色排行榜数据}
	 * 
	 * @param msg
	 * @param role
	 * @param rankType
	 * @param startPage
	 * @param numPerPage
	 * @param pageNum
	 * @author CamusHuang
	 * @creation 2014-3-27 下午4:53:55
	 * </pre>
	 */
	public static void packRank(KGameMessage msg, KRole role, KTeamPVPRankTypeEnum rankType, int startPage, int numPerPage, int pageNum) {
		
		long tempId = KTeamPVPManager.getTeamIdByRoleId(role.getId());
		
		packRank(msg, tempId, rankType, startPage, numPerPage, pageNum);
	}	
}
