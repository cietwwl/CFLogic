package com.kola.kmp.logic.relationship.message;

import java.util.Collection;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.relationship.KRelationShipProtocol;

public class KPushIntercourseData {

	public static void pushMsg(long roleId, int count, int winCount, Collection<String> names) {

		/**
		 * <pre>
		 * 服务器推送被切磋战报
		 * 
		 * int 被切磋次数
		 * int 胜出次数
		 * byte 名单长度N
		 * for(0~N){
		 * 	String name
		 * }
		 * </pre>
		 */

		KGameMessage msg = KGame.newLogicMessage(KRelationShipProtocol.SM_PUSH_INTERCOURSE_DATA);
		msg.writeInt(count);
		msg.writeInt(winCount);
		msg.writeByte(names.size());
		for (String name : names) {
			msg.writeUtf8String(name);
		}
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
}
