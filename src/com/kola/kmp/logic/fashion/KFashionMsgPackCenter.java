package com.kola.kmp.logic.fashion;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.KGameMessage;
import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.fashion.KFashionDataStructs.KFashionTemplate;
import com.kola.kmp.logic.fashion.KRoleFashion.FashionData;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.fashion.KFashionProtocol;

/**
 * <pre>
 * 由于在打包模块消息的过程中，涉及到访问及复杂的逻辑结构
 * 因此设立此类，用于打包消息
 * 
 * @author CamusHuang
 * @creation 2013-1-5 下午12:03:37
 * </pre>
 */
public class KFashionMsgPackCenter {

	public static final Logger _LOGGER = KGameLogger.getLogger(KFashionMsgPackCenter.class);

	public static void packAllFashions(KGameMessage msg, KRole role) {
		KRoleFashion set = KFasionRoleExtCACreator.getRoleFashion(role.getId());
		set.rwLock.lock();
		try {
			HashMap<Integer, FashionData> datas = set.getAllFashionsCacha();
			List<KFashionTemplate> temps = KFashionDataManager.mFashionTemplateManager.getFashionTemplateList();

			msg.writeInt(set.getSelectedFashionId());
			//
			int writeIndex = msg.writerIndex();
			msg.writeShort(temps.size());
			int count = 0;
			byte job = role.getJob();
			long nowTime = System.currentTimeMillis();
			// 先打包身上已有时装（未过期）
			for (KFashionTemplate temp : temps) {
				// 只发送不限职业或相同职业的时装
				if (temp.jobEnum == null || temp.job == job) {
					FashionData fashion = datas.get(temp.id);
					if (fashion == null) {
						// 未有
						continue;
					} else {
						packFashion(msg, role.getId(), temp, fashion, nowTime);
						count++;
					}
				}
			}
			// 打包身上未有或已过期的时装
			for (KFashionTemplate temp : temps) {
				if (!temp.isShowInList) {
					continue;
				}
				// 只发送不限职业或相同职业的时装
				if (temp.jobEnum != null && temp.job != job) {
					continue;
				}

				FashionData fashion = datas.get(temp.id);
				if (fashion != null) {
					// 已有
					continue;
				}
				// 未有
				packFashion(msg, role.getId(), temp, null, nowTime);
				count++;
			}

			msg.setShort(writeIndex, count);
		} finally {
			set.rwLock.unlock();
		}
	}

	public static boolean packFashion(KGameMessage msg, long roleId, int fashionTempId) {
		KFashionTemplate temp = KFashionDataManager.mFashionTemplateManager.getFashionTemplate(fashionTempId);
		if (temp == null) {
			return false;
		}
		long nowTime = System.currentTimeMillis();
		packFashion(msg, roleId, temp, null, nowTime);
		return true;
	}

	/**
	 * <pre>
	 * 参考{@link KFashionProtocol#MSG_STRUCT_FASHION_DETAILS}
	 * 
	 * @param msg
	 * @param temp
	 * @param fashion
	 * @author CamusHuang
	 * @creation 2014-3-26 下午5:22:34
	 * </pre>
	 */
	private static void packFashion(KGameMessage msg, long roleId, KFashionTemplate temp, FashionData fashionData, long nowTime) {

		msg.writeInt(temp.id);
		msg.writeInt(temp.icon);
		msg.writeByte(temp.job);
		msg.writeUtf8String(temp.ItemQuality.name);
		// msg.writeInt(temp.res_id);
		msg.writeUtf8String(temp.res_id);
		msg.writeUtf8String(temp.name);
		msg.writeBoolean(temp.buyMoney!=null);
		if(temp.buyMoney!=null){
			msg.writeByte(temp.buyMoney.currencyType.sign);
			msg.writeLong(temp.buyMoney.currencyCount);
		} else {
			msg.writeUtf8String(temp.catchDesc);
		}
		msg.writeByte(temp.allEffects.size());
		for (Entry<KGameAttrType, Integer> e : temp.allEffects.entrySet()) {
			msg.writeInt(e.getKey().sign);
			msg.writeInt(e.getValue());
		}

		//
//		 * long 每次购买有效时间（毫秒，<=0表示永久）
//		 * long 剩余时间（毫秒，0表示永久,-1表示未购买）
		msg.writeLong(temp.effectTime);
		 
		long endTime = KFashionLogic.ExpressionForFashionReleaseTime(fashionData, temp);
		msg.writeLong(endTime);

		// 战斗力
		int power = FashionData.countPower(temp, roleId);
		msg.writeInt(power);
	}

	public static void packFashionDataForGM(KRole role, List<String> infos) {

		infos.add("【时装名称】" + '\t' + "【剩余时长（分）】" + '\t' + "【战斗力】" + '\t' + "【是否穿戴】");

		KRoleFashion set = KFasionRoleExtCACreator.getRoleFashion(role.getId());
		set.rwLock.lock();
		try {
			HashMap<Integer, FashionData> datas = set.getAllFashionsCacha();

			for (FashionData data : datas.values()) {
				KFashionTemplate temp = KFashionDataManager.mFashionTemplateManager.getFashionTemplate(data.tempId);
				if (temp == null) {
					continue;
				}
				long releaseTime = KFashionLogic.ExpressionForFashionReleaseTime(data, temp);
				if (releaseTime == -1) {
					continue;
				}

				String releaseTimeStr = "";// （0表示永久,-1表示未购买）
				if (releaseTime == 0) {
					releaseTimeStr = "永久";
				} else {
					releaseTimeStr = releaseTime / Timer.ONE_MINUTE + "";
				}

				int power = FashionData.countPower(temp, role.getId());

				infos.add(temp.name + '\t' + releaseTimeStr + '\t' + power + '\t' + (data.tempId == set.getSelectedFashionId()));
			}
		} finally {
			set.rwLock.unlock();
		}
	}

}
