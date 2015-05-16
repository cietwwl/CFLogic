package com.kola.kmp.logic.item.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.item.KItemAttributeProvider;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_Equi;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.item.KItemProtocol;

public class KInstallEquipmentMsg implements GameMessageProcesser, KItemProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KInstallEquipmentMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_INSTALL_EQUI;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long itemId = msg.readLong();
		long slotId = msg.readLong();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			ItemResult_Equi result = new ItemResult_Equi();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, itemId, slotId, result);
			return;
		}

		ItemResult_Equi result = KItemLogic.dealMsg_installEquipment(role, itemId, slotId);

		doFinally(session, role, itemId, slotId, result);
	}

	private void doFinally(KGamePlayerSession session, KRole role, long itemId, long slotId, ItemResult_Equi result) {
		KGameMessage backmsg = KGame.newLogicMessage(SM_INSTALL_EQUI_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeLong(itemId);
		backmsg.writeLong(slotId);
		session.send(backmsg);

		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);

		if (result.isSucess) {
			// 刷新UI
			KSupportFactory.getRoleModuleSupport().updateEquipmentRes(role.getId());
			KSupportFactory.getTeamPVPSupport().notifyRoleEquipmentResUpdate(role.getId());
		}
	}
}