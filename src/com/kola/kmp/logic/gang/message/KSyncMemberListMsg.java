package com.kola.kmp.logic.gang.message;

import java.util.Collection;
import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangMember;
import com.kola.kmp.logic.gang.KGangMsgPackCenter;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.gang.KGangProtocol;

/**
 * <pre>
 * 服务器主动推送军团成员列表，消息体内容如下：
 * 
 * @author CamusHuang
 * @creation 2013-1-30 上午11:34:04
 * </pre>
 */
public class KSyncMemberListMsg implements KGangProtocol {

	/**
	 * <pre>
	 * 同步给所有成员
	 * 
	 * @param gang 默认所有成员接收
	 * @param updateMembers 新增或更新的成员角色ID
	 * @param deleteMemberRoleIds 删除的成员角色ID
	 * @author CamusHuang
	 * @creation 2013-1-25 下午3:31:14
	 * </pre>
	 */
	public static void sendMsg(KGang gang, Collection<KGangMember> updateMembers, List<Long> deleteMemberRoleIds) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_SYNC_MEMBER_LIST);
		KGangMsgPackCenter.packMemberSyncList(msg, updateMembers, deleteMemberRoleIds);
		//
		KGangMsgPackCenter.sendMsgToMemebers(msg, gang);
	}

	/**
	 * <pre>
	 * 同步给指定成员
	 * 
	 * @param gang 默认所有成员接收
	 * @param memberRoleIdList 新增或更新的成员
	 * @param deleteMemberRoleIds 删除的成员角色ID
	 * @param receiverRoleId 接收者角色ID
	 * @author CamusHuang
	 * @creation 2013-8-28 下午2:42:16
	 * </pre>
	 */
	public static void sendMsg(KGang gang, Collection<KGangMember> updateMembers, List<Long> deleteMemberRoleIds, long receiverRoleId) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_SYNC_MEMBER_LIST);
		KGangMsgPackCenter.packMemberSyncList(msg, updateMembers, deleteMemberRoleIds);
		//
		KSupportFactory.getRoleModuleSupport().sendMsg(receiverRoleId, msg);
	}
}
