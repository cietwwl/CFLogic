package com.kola.kmp.logic.gang.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangExtCASet;
import com.kola.kmp.logic.gang.KGangMsgPackCenter;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.gang.KGangProtocol;

/**
 * <pre>
 * 服务器主动推送(创建军团，成员登陆时)所属军团的详细数据
 * 创建、加入、登陆
 * 
 * @author CamusHuang
 * @creation 2013-1-30 上午11:33:52
 * </pre>
 */
public class KSyncOwnGangDataMsg implements KGangProtocol {
	/**
	 * <pre>
	 * 
	 * @param roleId
	 * @param gang
	 * @author CamusHuang
	 * @creation 2013-3-30 上午11:45:44
	 * </pre>
	 */
	public static void sendMsg(long roleId, KGang gang, KGangExtCASet set) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_SYNC_OWNGANG_DATA);
		KGangMsgPackCenter.packOwnGangData(msg, roleId, gang, set);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
		
//		KSupportFactory.getMissionSupport().notifyUseFunction(roleId, FunctionTypeEnum.我的军团);
	}
}
