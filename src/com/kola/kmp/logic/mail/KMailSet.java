package com.kola.kmp.logic.mail;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.management.timer.Timer;

import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.mail.impl.KAMailSet;
import com.kola.kgame.db.dataobject.DBMailData;

/**
 * <pre>
 * 客户端按从旧到新的原则，只显示警戒容量以内的邮件
 * 邮件具有有效期
 * 
 * 邮件自动清理规则
 * 1.达到警戒容量，则按从旧到新的原则，扫描清理警戒容量以内的、20%数量、无附件的邮件（登陆时检查）
 * 2.邮件达到有效期，（不管有无附件）则清理（登陆时检查）
 * 
 * @author CamusHuang
 * @creation 2014-2-21 下午6:30:40
 * </pre>
 */
public class KMailSet extends KAMailSet<KMail> {

	// 发送邮件CD结束时间
	private long sendMailCDEndTime;

	// /////////////////////////////////
	private static final String JSON_VER = "A";// 版本
	//
	private static final String JSON_CA_CDENDTIME = "1";

	KMailSet(long roleId, boolean isFirstNew) {
		super(roleId, isFirstNew);
	}

	protected Map<Long, KMail> initDBMails(List<DBMailData> dbdatas) {
		// 传入的参数必须是头旧尾新的方式排序
		Map<Long, KMail> result = new LinkedHashMap<Long, KMail>();
		for (DBMailData dbdata : dbdatas) {
			KMail data = new KMail(this, dbdata);
			result.put(data._id, data);
		}
		return result;
	}

	@Override
	protected void decodeCA(String jsonCA) {
		if (jsonCA == null || jsonCA.isEmpty() || jsonCA.length()==1) {
			return;
		}
		// 由底层调用,解释出逻辑层数据
		try {
			JSONObject obj = new JSONObject(jsonCA);
			int ver = obj.getInt(JSON_VER);// 默认版本
			// CEND 暂时只有版本0
			switch (ver) {
			case 0:
				sendMailCDEndTime = obj.optLong(JSON_CA_CDENDTIME) * Timer.ONE_MINUTE;
				break;
			}
		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误 roleId=" + _ownerId + " ----丢失数据，存在运行隐患！", ex);
		}
	}

	@Override
	protected String encodeCA() {
		rwLock.lock();
		// 构造一个数据对象给底层
		try {
			JSONObject obj = new JSONObject();
			obj.put(JSON_VER, 0);
			// CEND 暂时只有版本0
			obj.put(JSON_CA_CDENDTIME, sendMailCDEndTime / Timer.ONE_MINUTE);
			return obj.toString();
		} catch (Exception ex) {
			_LOGGER.error("encode数据时发生错误 roleId=" + _ownerId + " ----丢失数据！", ex);
			return "";
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 登陆清理邮件
	 * 1.邮件达到有效期，（不管有无附件）则清理（登陆时检查）
	 * 2.达到警戒容量，则按从旧到新的原则，扫描清理警戒容量以内的、20%数量、无附件的邮件（登陆时检查）
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-26 下午6:01:10
	 * </pre>
	 */
	void clearMailsForLogout() {
		rwLock.lock();
		try {
			List<KMail> elementList = getAllMailsCopy();
			{
				// 1.邮件达到有效期，（不管有无附件）则清理（登陆时检查）
				long nowTime = System.currentTimeMillis();
				long startTime = nowTime - KMailConfig.getInstance().OutDateTimeInMills;
				//
				for (Iterator<KMail> it = elementList.iterator(); it.hasNext();) {
					KMail mail = it.next();
					if (mail._createTime < startTime) {
						it.remove();
						super.notifyElementDelete(mail._id);
					}
				}
			}

			{
				// 2.达到警戒容量，则按从旧到新的原则，扫描清理警戒容量以内的、20%数量、无附件的邮件（登陆时检查）
				if (elementList.size() >= KMailConfig.getInstance().MailBoxWarnSize) {

					int scanCount = 0;// 扫描数量计数
					int deleteCount = 0;// 清理邮件计数
					for (KMail mail : elementList) {
						if (!mail.isContainAttachments()) {
							// 不包含附件
							super.notifyElementDelete(mail._id);
							//
							deleteCount++;
							if (deleteCount >= KMailConfig.getInstance().ClearOfWarnSize) {
								// 最多只清理20%警戒容量
								break;
							}
						}
						//
						scanCount++;
						if (scanCount >= KMailConfig.getInstance().MailBoxWarnSize) {
							// 最多只扫描警戒容量
							break;
						}
					}
				}
			}
		} finally {
			rwLock.unlock();
		}
	}

	void setSendCDEndTime(long endTime) {
		sendMailCDEndTime = endTime;
		super.notifyDB();
	}

	long getSendCDEndTime() {
		return sendMailCDEndTime;
	}

}
