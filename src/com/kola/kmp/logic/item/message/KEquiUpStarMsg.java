package com.kola.kmp.logic.item.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.item.KItemDataManager;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_EquiUpStar;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.item.KItemProtocol;

/**
 * <pre>
 * 装备强化消息
 * 
 * @author CamusHuang
 * @creation 2012-12-10 下午4:35:52
 * </pre>
 */
public class KEquiUpStarMsg implements GameMessageProcesser, KItemProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KEquiUpStarMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_UPSTAR_EQUI;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long itemId = msg.readLong();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			ItemResult_EquiUpStar result = new ItemResult_EquiUpStar();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, itemId, result);
			return;
		}

		ItemResult_EquiUpStar result = KItemLogic.dealMsg_upEquipmentStar(role, itemId);
		// -------------
		doFinally(session, role, itemId, result);
	}

	private void doFinally(KGamePlayerSession session, KRole role, long itemId, ItemResult_EquiUpStar result) {

		KGameMessage backmsg = KGame.newLogicMessage(SM_UPSTAR_EQUI_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeLong(itemId);
		backmsg.writeInt(result.starChange);
		session.send(backmsg);

		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);
		
		if(result.isSucess){
			// 通知日常任务
			KSupportFactory.getMissionSupport().notifyUseFunction(role, KUseFunctionTypeEnum.装备升星);
			//
			if (result.orgStarLv < KItemDataManager.mEquiStarRateManager.getMaxStarLv() 
					&& result.nowStarLv >= KItemDataManager.mEquiStarRateManager.getMaxStarLv()) {
				KWordBroadcastType _broadcastType = KWordBroadcastType.装备_恭喜x角色将x装备升至x星;
				String tips = StringUtil.format(_broadcastType.content, role.getExName(), result.itemName, KItemDataManager.mEquiStarRateManager.getMaxStarLv());
				KSupportFactory.getChatSupport().sendSystemChat(tips, _broadcastType);
			}
		}
	}
}
