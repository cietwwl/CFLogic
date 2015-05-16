package com.kola.kmp.logic.reward.activatecode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.koala.game.KGame;
import com.koala.game.communication.KGameHttpRequestSender;
import com.koala.game.communication.KGameHttpRequestSender.KGameHttpRequestResult;
import com.koala.game.logging.KGameLogger;
import com.koala.game.player.KGamePlayer;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.koala.promosupport.MD5;
import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.util.ThreadLocalDateFormat;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.item.KItem;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.reward.KRewardModule;
import com.kola.kmp.logic.reward.activatecode.ActivateCodeTask.HTTPFutureTask;
import com.kola.kmp.logic.reward.activatecode.KActivateCodeDataManager.ActivationRewardDataManager.ActivationRewardData;
import com.kola.kmp.logic.reward.activatecode.KActivateCodeDataManager.BigTypeDataManager.BigTypeData;
import com.kola.kmp.logic.reward.activatecode.db.DBActivation;
import com.kola.kmp.logic.reward.activatecode.db.DBActivationDataAccess;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.RewardTips;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-7-16 下午6:11:55
 * </pre>
 */
public class KActivateCodeCenter {

	static final KGameLogger _ACTIVATECODELOGGER = KGameLogger.getLogger("activateCodeLogger");

	/**
	 * <pre>
	 * 激活码奖励全局锁
	 * 考虑到领奖并不高频，加上激活码无角色归属，因此使用全局锁。
	 * </pre>
	 */
	private static AtomicBoolean islock = new AtomicBoolean();

	private KActivateCodeCenter() {
	}

	public static CommonResult_Ext collectActivationReward(KGamePlayerSession session, KRole role, String activationCode) {
		CommonResult_Ext result = new CommonResult_Ext();

		DBActivation dbdata = DBActivationDataAccess.getInstance().getActivation(activationCode);
		if (dbdata == null) {
			result.tips = RewardTips.此激活码不存在;
			return result;
		}

		long nowTime = System.currentTimeMillis();
		if (nowTime > dbdata.effectEndTime) {
			result.tips = RewardTips.此激活码已过期;
			return result;
		}

		ActivationRewardData rewardData = KActivateCodeDataManager.mActivationRewardDataManager.getData(dbdata.type);
		if (rewardData == null) {
			result.tips = RewardTips.对不起此礼包未开放;
			return result;
		}

		if (nowTime > rewardData.endTime) {
			result.tips = RewardTips.此激活码已过期;
			return result;
		}

		BigTypeData bigType = KActivateCodeDataManager.mBigTypeDataManager.getData(rewardData.type);

		if (!islock.compareAndSet(false, true)) {
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			return result;
		}

		ItemResult_AddItem addRewardResult = null;
		try {
			if (!bigType.oneCodeMuilUse) {
				// 不允许一码多用
				if (dbdata.useRoleId > 0) {
					result.tips = RewardTips.此激活码已被使用;
					return result;
				}
			}

			KRoleRewardActivatecode roleData = KActivateCodeSonModule.instance.getRewardSon(role.getId());
			roleData.rwLock.lock();
			try {
				// 大类型内，小类型数量限制：同种类型礼包你已达使用上限，请领取其他类型礼包
				int usedSonTypeCount = roleData.getUsedSonTypeCount(bigType.type);
				if (usedSonTypeCount >= bigType.useSonTypeCount) {
					result.tips = RewardTips.同种类型礼包你已达使用上限请领取其他类型礼包;
					return result;
				}

				if (roleData.isUsedSonType(rewardData.type, rewardData.sonType)) {
					result.tips = RewardTips.同种类型礼包你已达使用上限请领取其他类型礼包;
					return result;
				}

				{// 可领取
					addRewardResult = rewardData.baseRewardData.sendReward2(role, PresentPointTypeEnum.激活码奖励);
					if (!addRewardResult.isSucess) {
						result.tips = addRewardResult.tips;
						return result;
					}

					// 登记
					roleData.recordUsedCode(rewardData.type, rewardData.sonType);
					// 修改数据
					if (dbdata.useRoleId < 1) {
						DBActivationDataAccess.getInstance().recordPlayerReceiveActivation(activationCode, role.getPlayerId(), role.getId(), role.getName());
					}

					result.isSucess = true;
					result.tips = RewardTips.成功领取激活码礼包;
					return result;
				}
			} finally {
				roleData.rwLock.unlock();
			}
		} finally {
			islock.set(false);

			if (result.isSucess) {
				result.addDataUprisingTips(rewardData.baseRewardData.dataUprisingTips);

				// 记录进日志
				_ACTIVATECODELOGGER.warn(",角色ID=,{},角色名=,{},code=,{},type=,{},sonType=,{}", role.getId(), role.getName(), activationCode, rewardData.type, rewardData.sonType);

				// 如果是线下活动礼包，则记录激活码信息
				{
					TimeLimieProduceActivity dreamActivity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.话费礼包活动);
					if (dreamActivity != null) {
						if (addRewardResult.newItemList != null) {
							for (KItem item : addRewardResult.newItemList) {
								if (dreamActivity.dreamGiftItemTemp.itemCode.equals(item.getItemCode())) {
									item.getCA().setActiviteCode(activationCode);
								}
							}
						}
						if (addRewardResult.updateItemCountList != null) {
							for (KItem item : addRewardResult.updateItemCountList) {
								if (dreamActivity.dreamGiftItemTemp.itemCode.equals(item.getItemCode())) {
									item.getCA().setActiviteCode(activationCode);
								}
							}
						}
					}
				}

				// 通知合作方
				notifyPromo(bigType, session, role, activationCode);
			}
		}
	}

	/**
	 * <pre>
	 * 使用激活码后通知合作方
	 * 
	 * @param bigType
	 * @param session
	 * @param role
	 * @param activationCode
	 * @author CamusHuang
	 * @creation 2014-12-29 下午4:10:36
	 * </pre>
	 */
	private static void notifyPromo(BigTypeData bigType, KGamePlayerSession session, KRole role, String activationCode) {

		if (bigType.promoTypeEnum == KActivateCodePromoTypeEnum.通用) {
			return;
		}

		if (bigType.promoTypeEnum == KActivateCodePromoTypeEnum.梦想) {
			// 通知给梦想
			notifyDreamByHttp(session, role, activationCode, bigType.type);
			return;
		}

		if (bigType.promoTypeEnum == KActivateCodePromoTypeEnum.YY) {
			// 通知给YY
			notifyYYByHttp(session, role, activationCode, bigType.type);
			return;
		}
	}
	
	/**
	 * <pre>
	 * 通知YY相关接口
	 * 
	 * @param session
	 * @param role
	 * @param activationCode
	 * @param giftType 参考【礼包类型】定义
	 * @author CamusHuang
	 * @creation 2014-12-22 下午3:05:22
	 * </pre>
	 */
	private static void notifyYYByHttp(KGamePlayerSession session, KRole role, String activationCode, String giftType) {
		// 反馈给YY信息：
//		“passport” : “my_good_game”,  // 合作游戏厂商在YY注册的通行证，不区分大小写。（获取方式见附录）， “timestamp”: “2012-01-02 03:04:05”,  // 时间戳- 格式：YYYY-MM-DD hh:mm:ss, 防重放用
//		“tid”: 1321,  //  task id - 任务标识 – 因游戏厂商名下可能有多个任务，需要明确指定是操作哪个任务
//		“data”: [  // 玩家已经完成的任务码（玩家进入游戏任务时输入的值）
//		“RHT781723JF70GO3C”, 
//		“Y03A8K8562X72Q84J”, 
//		“JX1A3O5G484Y02KJ9”,
//		………………………………….,
//		………………………………….,
//		“9F0Q5HU7OQ832GGE4”
//		]

		KGamePlayer player = session.getBoundPlayer();
		long nowTime = System.currentTimeMillis();
		//
		
		//
		String json;
		String code;
		try {
			JSONObject josnO = new JSONObject();
			josnO.put("passport", KActivateCodeDataManager.YYPassport);
			josnO.put("timestamp", UtilTool.DATE_FORMAT2.format(new Date(nowTime)));
			josnO.put("tid", KActivateCodeDataManager.YYTid);
			
			JSONArray jsonA = new JSONArray();
			jsonA.put(activationCode);
			
			josnO.put("data", jsonA);
			//
			json = josnO.toString();
			code = MD5.MD5Encode( json + ";" + KActivateCodeDataManager.YYKey);
		} catch(Exception e){
			KItemLogic._OPEN_FIXEDBOX_LOGGER.error(",yyHttp,gsId=,{},playerId=,{},角色ID=,{},角色名=,{},giftType=,{},giftCode=,{},result=,{},exception=,{}", KGame.getGSID(), player.getID(),
					role.getId(), role.getName(), giftType, activationCode, "json失败", e.getMessage());
			
			KRewardModule._LOGGER.error(e.getMessage(), e);
			return;
		}
		
		Map<String, Object> paraMap = new HashMap<String, Object>();
		paraMap.put("code", code);
		paraMap.put("json", json);
		
		List<String> needEncodeKeys = Collections.emptyList();

		try {
			Future<KGameHttpRequestResult> httpResult = KGame.sendPostRequestUseJSON(KActivateCodeDataManager.YYHTTPAddress, paraMap, needEncodeKeys, null);
			
			String tips = StringUtil.format(",yyHttp,gsId=,{},playerId=,{},角色ID=,{},角色名=,{},giftType=,{},giftCode=,{},result=,{},http=,{}", KGame.getGSID(), player.getID(), role.getId(),
					role.getName(), giftType, activationCode);//成功或失败,失败原因,留给时效任务处理
			
			HTTPFutureTask.submitTask(KActivateCodePromoTypeEnum.YY, tips, httpResult);
		} catch (Exception e) {
			KItemLogic._OPEN_FIXEDBOX_LOGGER.error(",yyHttp,gsId=,{},playerId=,{},角色ID=,{},角色名=,{},giftType=,{},giftCode=,{},result=,{},exception=,{}", KGame.getGSID(), player.getID(),
					role.getId(), role.getName(), giftType, activationCode, "失败", e.getMessage());

			KRewardModule._LOGGER.error(e.getMessage(), e);
		}
	}
	
	public static void main(String[] strs) throws Exception{
		long nowTime = System.currentTimeMillis();
		//
		ThreadLocalDateFormat DATE_FORMAT2 = new ThreadLocalDateFormat("yyyy-MM-dd HH:mm:ss");
		//
		String json;
		String code;
		{
			JSONObject josnO = new JSONObject();
			josnO.put("passport", KActivateCodeDataManager.YYPassport);
			josnO.put("timestamp", DATE_FORMAT2.format(new Date(nowTime)));
			josnO.put("tid", KActivateCodeDataManager.YYTid);
			
			JSONArray jsonA = new JSONArray();
			jsonA.put("BBB001");
			jsonA.put("BBB002");
			jsonA.put("BBB003");
			jsonA.put("BBB004");
			jsonA.put("BBB005");
			
			josnO.put("data", jsonA);
			//
			json = josnO.toString();
			code = MD5.MD5Encode( json + ";" + KActivateCodeDataManager.YYKey);
		}
		
		Map<String, Object> paraMap = new HashMap<String, Object>();
		paraMap.put("code", code);
		paraMap.put("json", json);
		
		System.err.println(code);
		System.err.println(json);
		
		List<String> needEncodeKeys = new ArrayList<String>();
		
		try {
			KGameHttpRequestSender sender = new KGameHttpRequestSender();
			Future<KGameHttpRequestResult> httpResult = sender.sendPostRequestUseJSON(KActivateCodeDataManager.YYHTTPAddress, paraMap, needEncodeKeys, null);
			System.err.println(httpResult.get().content);
		} catch (Exception e) {
			KRewardModule._LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * <pre>
	 * 通知梦想相关接口
	 * 1.使用主播礼包、话费礼包激活码时
	 * 2.开启话费礼包选择钻石或话费奖励时
	 * 
	 * 礼包类型：F 表示使用CPS主播码，获得【CPS礼包】，一码多用
	 * 礼包类型：G 表示使用话费礼包码，获得【话费礼包】，一码一用
	 * 礼包类型：-1 表示开启【话费礼包】，选择话费奖励
	 * 礼包类型：-2 表示开启【话费礼包】，选择钻石礼包
	 * 
	 * @param session
	 * @param role
	 * @param activationCode
	 * @param giftType 参考【礼包类型】定义
	 * @author CamusHuang
	 * @creation 2014-12-22 下午3:05:22
	 * </pre>
	 */
	public static void notifyDreamByHttp(KGamePlayerSession session, KRole role, String activationCode, String giftType) {
		// 反馈给梦想信息：
		// 1、玩家账号(没有账号就只要是唯一标识即可) player.getID();
		// 2、使用的礼包码 activationCode;
		// 3、注册游戏时间 player.getCreateTimeMillis();
		// 4、所在平台 player.getPromoID();
		// 5、所在区 KGameServer.getInstance().getGSID();
		// 6、角色名 role.getName();
		// PS：(同一账号多个区、多个角色、无论N个角色使用N个码，只记录发送首次使用该礼包的角色名、所在区)
		long nowTime = System.currentTimeMillis();
		KGamePlayer player = session.getBoundPlayer();
		//
		Map<String, Object> paraMap = new HashMap<String, Object>();
		// key，giftType,userId ,giftCode 其中 一个不同 就行了
		paraMap.put("userId", player.getID() + "");
		paraMap.put("giftType", giftType);
		paraMap.put("giftCode", activationCode);
		//
		paraMap.put("key", KActivateCodeDataManager.DreamKey);
		String uuid = player.getAnalysisInfo("uid");
		uuid = (uuid == null || uuid.isEmpty()) ? (player.getID() + "") : uuid;
		paraMap.put("uuid", uuid);
		// System.err.println(player.getAnalysisInfo("analysis_uid"));
		paraMap.put("userAccount", player.getPromoMask());
		paraMap.put("serverName", KGame.getGSID() + "");
		paraMap.put("role", role.getName());
		paraMap.put("createUserTime", UtilTool.DATE_FORMAT12.format(new Date(player.getCreateTimeMillis())));
		paraMap.put("useGiftTime", UtilTool.DATE_FORMAT12.format(new Date(nowTime)));
		paraMap.put("platformId", player.getPromoID() + "");
		//
		List<String> needEncodeKeys = new ArrayList<String>();
		needEncodeKeys.add("serverName");
		needEncodeKeys.add("role");

		try {
			Future<KGameHttpRequestResult> httpResult = KGame.sendPostRequestUseJSON(KActivateCodeDataManager.DreamHTTPAddress, paraMap, needEncodeKeys, null);
			
			String tips = StringUtil.format(",dreamHttp,gsId=,{},playerId=,{},角色ID=,{},角色名=,{},giftType=,{},giftCode=,{},result=,{},http=,{}", KGame.getGSID(), player.getID(), role.getId(),
					role.getName(), giftType, activationCode);//成功或失败,失败原因,留给时效任务处理
			
			HTTPFutureTask.submitTask(KActivateCodePromoTypeEnum.梦想, tips, httpResult);
		} catch (Exception e) {
			KItemLogic._OPEN_FIXEDBOX_LOGGER.error(",dreamHttp,gsId=,{},playerId=,{},角色ID=,{},角色名=,{},giftType=,{},giftCode=,{},result=,{},exception=,{}", KGame.getGSID(), player.getID(),
					role.getId(), role.getName(), giftType, activationCode, "失败", e.getMessage());

			KRewardModule._LOGGER.error(e.getMessage(), e);
		}
	}
}
