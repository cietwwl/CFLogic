package com.kola.kmp.logic.reward.garden.message;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.npc.dialog.KDefaultDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.reward.garden.KGardenCenter;
import com.kola.kmp.logic.reward.garden.KGardenDataManager;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_GardenCollectTop;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.reward.KRewardProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KGardenCollectTopMsg implements GameMessageProcesser, KRewardProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGardenCollectTopMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GARDEN_COLLECT_TOP;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		byte type = msg.readByte();
		// -------------
		RewardResult_GardenCollectTop result = null;
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new RewardResult_GardenCollectTop();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, type, result);
			return;
		} 
		
		result = KGardenCenter.dealMsg_CollectTop(role, type, false);
		
		if (!result.isGoConfirm) {
			KDialogService.sendNullDialog(session);
			doFinally(session, role, type, result);
			return;
		}
		// -------------

		// 发送菜单，消费二次确认
		List<KDialogButton> buttons = new ArrayList<KDialogButton>();
		buttons.add(KDialogButton.CANCEL_BUTTON);
		buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_PAY_GARDEN, type+"", KDialogButton.CONFIRM_DISPLAY_TEXT));
		KDialogService.sendFunDialog(role, "", result.tips, buttons, true, (byte) -1);

	}
	
	public static void confirmByDialog(KGamePlayerSession session, String script) {
		RewardResult_GardenCollectTop result = null;
		
		byte type = Byte.parseByte(script);

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new RewardResult_GardenCollectTop();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, type, result);
			return;
		}
		
		result = KGardenCenter.dealMsg_CollectTop(role, type, true);
		
		//
		KDialogService.sendNullDialog(session);
		doFinally(session, role, type, result);
		return;
	}	
		
	private static void doFinally(KGamePlayerSession session, KRole role, byte type, RewardResult_GardenCollectTop result) {
		
		if (result.isSucess) {
			KGardenSynMsg.sendTreeData(role, role.getId(), result.treeData);
		}

		KGameMessage backmsg = KGame.newLogicMessage(SM_GARDEN_COLLECT_TOP_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeByte(type);
		session.send(backmsg);
		//
		result.doFinally(role);

		if (result.isSucess) {
			KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.收获庄园植物);
			//
			if (type == KGardenDataManager.TYPE_TOP_MIN && result.addItem.getItemTemplate().ItemQuality.sign >= KGardenDataManager.TOP_BROCAST_QUALITY.sign) {
				KWordBroadcastType _broadccastType = KWordBroadcastType.庄园_x角色在庄园内收获金色果实获得了x物品x数量;
				String tips = StringUtil.format(_broadccastType.content, role.getExName(), result.addItem.getItemTemplate().extItemName, result.addItem.itemCount);
				KSupportFactory.getChatSupport().sendSystemChat(tips, _broadccastType);
			}
		}
	}
}
