package com.kola.kmp.logic.item.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempMaterial;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_Compose;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.item.KItemProtocol;

/**
 * <pre>
 * 合成物品（包含材料和宝石）
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KComposeListSelectMsg implements GameMessageProcesser, KItemProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KComposeListSelectMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_COMPOSE2_SELECT;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long itemId = msg.readLong();
		String selectItem = msg.readUtf8String();
		boolean isComposeAll = false;
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			ItemResult_Compose result = new ItemResult_Compose();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			KCompose2Msg.doFinally(session, role, itemId, result);
			return;
		}

		ItemResult_Compose result = null;
		// 处理消息的过程
		result = KItemLogic.dealMsg_compose2(role, itemId, false, isComposeAll, selectItem);

		if (result.isGoConfirmPay) {
			// 需要付费确认
			KCompose2Msg.showConfirmPayDialog(session, result.tips, itemId, selectItem, isComposeAll);
			return;
		}
		// -------------
		KCompose2Msg.doFinally(session, role, itemId, result);
	}
	
	public static void pushComposeList(KRole role, long itemId, KItemTempMaterial temp){
//		 * long 使用的道具ID
//		 * byte 可选结果数量n
//		 * for(int i=0;i<n;i++){
//		 * 	参考{@link #MSG_STRUCT_ITEM_DETAILS}
//		 * }
		KGameMessage backmsg = KGame.newLogicMessage(SM_COMPOSE2_LIST);
		backmsg.writeLong(itemId);
		backmsg.writeByte(temp.composeTargetList.size());
		for(ItemCountStruct struct:temp.composeTargetList){
			KCurrencyCountStruct price = temp.composeTargetPrice.get(struct.itemCode);
			if(price==null){
				price = new KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND, 0);
			} 
			KItemMsgPackCenter.packItem(backmsg, struct.getItemTemplate(), struct.itemCount, price);
		}
		role.sendMsg(backmsg);
	}
}
