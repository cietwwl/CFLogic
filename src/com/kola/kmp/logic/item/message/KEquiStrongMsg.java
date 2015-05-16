package com.kola.kmp.logic.item.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.other.KItemTypeEnum;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.role.KRoleModuleConfig;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_EquiStrong;
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
public class KEquiStrongMsg implements GameMessageProcesser, KItemProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KEquiStrongMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_STRONG_EQUI;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long itemId = msg.readLong();
		int times = msg.readInt();
		if (times < 1){// || times > 20) {
			// 强制转成1次
			times = 1;
		}
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			ItemResult_EquiStrong result = new ItemResult_EquiStrong();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, itemId, result);
			return;
		}
		
		ItemResult_EquiStrong result = KItemLogic.dealMsg_strongEquipment(role, itemId, times);
		//

		doFinally(session, role, itemId, result);
	}

	private void doFinally(KGamePlayerSession session, KRole role, long itemId, ItemResult_EquiStrong result) {
		KGameMessage backmsg = KGame.newLogicMessage(SM_STRONG_EQUI_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeLong(itemId);
		backmsg.writeInt(result.strongAddLv);
		session.send(backmsg);

		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);

		if (result.isSucess) {
			KSupportFactory.getRewardModuleSupport().recordFuns(role, KVitalityTypeEnum.强化装备, result.successTime);
			KSupportFactory.getMissionSupport().notifyUseFunctionByCounts(role, KUseFunctionTypeEnum.装备强化, result.successTime);
			//
			if (!result.fullStrongEquiNames.isEmpty()) {
				KWordBroadcastType _broadcastType = KWordBroadcastType.装备_恭喜x角色将x装备强化到x级;
				if (result.fullStrongEquiNames.size() > 1) {
					String tips = StringUtil.format(_broadcastType.content, role.getExName(), KItemTypeEnum.装备.name, KRoleModuleConfig.getRoleMaxLv());
					KSupportFactory.getChatSupport().sendSystemChat(tips, _broadcastType);
				} else {
					for (String itemExtName : result.fullStrongEquiNames) {
						String tips = StringUtil.format(_broadcastType.content, role.getExName(), itemExtName, KRoleModuleConfig.getRoleMaxLv());
						KSupportFactory.getChatSupport().sendSystemChat(tips, _broadcastType);
					}
				}
			}
		}
	}
}
