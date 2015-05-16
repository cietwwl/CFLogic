package com.kola.kmp.logic.npc.message;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.npc.menu.KMenuService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.npc.KNpcProtocol;

/**
 * <pre>
 * 客户端点击功能NPC
 * 反馈有多种可能：
 * 1.如果有关卡，则直接进入关卡战斗（反馈由关卡模块负责）
 * 2.如果有可提交的主线任务，则直接进行主线任务提交流程（反馈由任务模块负责）
 * 3.如果有可接受的主线任务，则直接进行主线任务接受流程（反馈由任务模块负责）
 * 4.如果有菜单项，则显示一句话及菜单项列表
 * （菜单项列表包括按条件筛选出来显示的任务s+功能项s）(反馈由本模块负责)
 * --{@link #SM_SEND_NPC_MENU}
 * ---------------------
 * int npcTemplateId
 * </pre>
 */
public class KGetNpcMenusMsg implements GameMessageProcesser, KNpcProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGetNpcMenusMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GET_NPC_MENUS;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			KDialogService.sendUprisingDialog(session, GlobalTips.服务器繁忙请稍候再试);
			return;
		}
		KMenuService.sendAllNpcMenusInMap(role);
	}
}