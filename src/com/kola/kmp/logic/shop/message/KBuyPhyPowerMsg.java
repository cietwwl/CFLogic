package com.kola.kmp.logic.shop.message;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.shop.PhyPowerShopCenter;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.shop.KShopProtocol;

/**
 * <pre>
 * 购买体力流程：
 * CM请求-->次数不足-->提示次数不足-->确认则跳转到VIP界面
 * CM请求-->元宝不足-->提示元宝不足-->确认则跳转到充值界面
 * CM请求-->购买二次确认-->确认则购买-->结果提示
 * 
 * @author CamusHuang
 * @creation 2013-6-27 下午3:19:24
 * </pre>
 */
public class KBuyPhyPowerMsg implements GameMessageProcesser, KShopProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KBuyPhyPowerMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_BUY_PHYPOWER;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
//		KGameMessage msg = msgEvent.getMessage();
		// -------------
//		String itemCode = msg.readUtf8String();
//		int buyCount = msg.readInt();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			KDialogService.sendUprisingDialog(session, GlobalTips.服务器繁忙请稍候再试);
			return;
		}

		PhyPowerShopCenter.showBuyPhyPowerDialog(role);
	}

}
