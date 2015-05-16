package com.kola.kmp.logic.fashion.message;

import java.util.Set;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.fashion.KFashionMsgPackCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.util.tips.FashionTips;
import com.kola.kmp.protocol.fashion.KFashionProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KPushFashionsMsg implements KFashionProtocol {

	public static void pushAllFashions(KRole role) {
		if (role.isOnline()) {
			KGameMessage msg = KGame.newLogicMessage(SM_PUSH_ALLFASHION_LIST);
			KFashionMsgPackCenter.packAllFashions(msg, role);
			role.sendMsg(msg);
		}
	}
	
	
	
	public static void pushFashionBuyDialog(KRole role, Set<Integer> fashionTempIdSet) {
		if (role.isOnline()) {
			KGameMessage msg = KGame.newLogicMessage(SM_PUSH_FASHIONBUY_DIALOG);
			/**
			 * <pre>
			 * 推送时装续费对话框
			 * 
			 * String tips
			 * byte 时装数量N
			 * for(1~N){
			 * 	int 时装ID---如果客户端没有此模块，请忽略
			 * }
			 * </pre>
			 */
			msg.writeUtf8String(FashionTips.时装过期续费提示);
			msg.writeByte(fashionTempIdSet.size());
			for(int id:fashionTempIdSet){
				msg.writeInt(id);
			}
			role.sendMsg(msg);
		}
	}
}
