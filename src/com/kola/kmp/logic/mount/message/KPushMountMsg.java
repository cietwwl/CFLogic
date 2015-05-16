package com.kola.kmp.logic.mount.message;

import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.mount.KMount;
import com.kola.kmp.logic.mount.KMountMsgPackCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.protocol.mount.KMountProtocol;

public class KPushMountMsg implements KMountProtocol {

	public static void SM_PUSH_MOUNTDATA(KRole role) {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_MOUNTDATA);
		//
		KMountMsgPackCenter.pacAllMountDatas(msg, role);
		role.sendMsg(msg);
	}

	public static void SM_PUSH_MOUNT_CONSTANCE(KRole role) {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_MOUNT_CONSTANCE);
		//
		KMountMsgPackCenter.packMountConstance(msg);
		role.sendMsg(msg);
	}
	
	public static void SM_SYN_MOUNT(KRole role, List<KMount> mountList){
		/**
		 * <pre>
		 * 同步机甲数据（覆盖或新增）
		 * 
		 * byte 机甲数量n
		 * for(0~n){
		 * 	参考{@link KMountProtocol#MSG_STRUCT_MOUNT}
		 * }
		 * </pre>
		 */
		if(mountList.isEmpty()){
			return;
		}
		
		KGameMessage msg = KGame.newLogicMessage(SM_SYN_MOUNT);
		//
		msg.writeByte(mountList.size());
		for(KMount mount:mountList){
			KMountMsgPackCenter.packMount(msg, role, mount);
		}
		role.sendMsg(msg);
	}
	
	public static void SM_SYN_MOUNT(KRole role, KMount mount){
		/**
		 * <pre>
		 * 同步机甲数据（覆盖或新增）
		 * 
		 * byte 机甲数量n
		 * for(0~n){
		 * 	参考{@link KMountProtocol#MSG_STRUCT_MOUNT}
		 * }
		 * </pre>
		 */
		if(mount==null){
			return;
		}
		
		KGameMessage msg = KGame.newLogicMessage(SM_SYN_MOUNT);
		//
		msg.writeByte(1);
		KMountMsgPackCenter.packMount(msg, role, mount);
		role.sendMsg(msg);
	}	
	
	public static void SM_SYN_MOUNT_UPLV(KRole role, KMount mount){
		/**
		 * <pre>
		 * 同步机甲数据（覆盖或新增）
		 * 
		 * byte 机甲数量n
		 * for(0~n){
		 * 	参考{@link KMountProtocol#MSG_STRUCT_MOUNT}
		 * }
		 * </pre>
		 */
		if(mount==null){
			return;
		}
		
		KGameMessage msg = KGame.newLogicMessage(SM_SYN_MOUNT_UPLV);
		//
		KMountMsgPackCenter.packMountFoUpLv(msg, role, mount);
		role.sendMsg(msg);
	}	

}
