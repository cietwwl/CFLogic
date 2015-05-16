package com.kola.kmp.logic.fashion.message;

import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.fashion.KFashionAttributeProvider;
import com.kola.kmp.logic.fashion.KFashionLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.tips.FashionTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.fashion.KFashionProtocol;

public class KSelectFashionMsg implements GameMessageProcesser, KFashionProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KSelectFashionMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_SELECTED_FASHION;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int fashionId = msg.readInt();
		boolean isSelected = msg.readBoolean();
		// -------------
		CommonResult_Ext result = null;
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new CommonResult_Ext();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
		} else {
			result = KFashionLogic.dealMsg_selecteFashion(role, fashionId, isSelected);
		}
		// -------------
		doFinally(session, role, fashionId, isSelected, result);

		// 需要通知角色模块时装参数变更
		if (result.isSucess) {
			KFashionLogic.updateForSelectFashionChange(role);
		}
	}

	private static void doFinally(KGamePlayerSession session, KRole role, int fashionId, boolean isSelected, CommonResult_Ext result) {
		KGameMessage backmsg = KGame.newLogicMessage(SM_SELECTED_FASHION_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeInt(fashionId);
		backmsg.writeBoolean(isSelected);
		session.send(backmsg);
	}

	public static void synSelectFashionToClient(KRole role, int fashionTempId, boolean isIn) {
		KGameMessage backmsg = KGame.newLogicMessage(SM_SELECTED_FASHION_RESULT);
		backmsg.writeBoolean(true);
		backmsg.writeUtf8String(isIn?FashionTips.自动穿戴成功:FashionTips.时装已过期);
		backmsg.writeInt(fashionTempId);
		backmsg.writeBoolean(isIn);
		role.sendMsg(backmsg);
	}
}
