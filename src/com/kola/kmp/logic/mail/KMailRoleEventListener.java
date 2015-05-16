package com.kola.kmp.logic.mail;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.mail.message.KPushMailsMsg;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2013-1-29 下午5:50:54
 * </pre>
 */
public class KMailRoleEventListener implements IRoleEventListener {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		// 主动推送邮件列表
		KPushMailsMsg.pushAllMails(role);
		// 发送每日邮件
		KSupportFactory.getRewardModuleSupport().notifyForDayMail(role);
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {
		// 邮件清理
		KMailSet set = KMailModuleExtension.getMailSet(role.getId());
		set.clearMailsForLogout();
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		KMailLogic.initDefaultMails(role);
	}

	@Override
	public void notifyRoleDeleted(long roleId) {
		// 忽略
	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		// 忽略
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		
	}

}
