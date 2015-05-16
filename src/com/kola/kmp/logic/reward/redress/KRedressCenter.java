package com.kola.kmp.logic.reward.redress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.game.dataaccess.dbobj.flowdata.DBFunPointConsumeRecord;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kmp.logic.currency.KCurrencyAccountSet;
import com.kola.kmp.logic.currency.KCurrencyModuleExtension;
import com.kola.kmp.logic.fashion.KFashionLogic;
import com.kola.kmp.logic.gamble.KGambleRoleExtCACreator;
import com.kola.kmp.logic.gamble.wish.KRoleWishData;
import com.kola.kmp.logic.gm.dbquery.DBQueryManager;
import com.kola.kmp.logic.gm.dbquery.DBQueryManager.DBFunPointTotalConsume;
import com.kola.kmp.logic.item.KItem;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KGameLevelRecord;
import com.kola.kmp.logic.level.KGameLevelSet;
import com.kola.kmp.logic.mount.KMountLogic;
import com.kola.kmp.logic.mount.KMountModuleExtension;
import com.kola.kmp.logic.mount.KMountOld;
import com.kola.kmp.logic.mount.KMountSet;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.reward.redress.KRedressDataManagerV1.KEquiEnchanseRedressDataManager.EquiEnchanseRedressData;
import com.kola.kmp.logic.reward.redress.KRedressDataManagerV1.KFashionRedressDataManager.FashionRedressData;
import com.kola.kmp.logic.reward.redress.KRedressDataManagerV1.KSkillRedressDataManager;
import com.kola.kmp.logic.reward.redress.KRedressDataManagerV1.KSkillRedressDataManager.SkillRedressData;
import com.kola.kmp.logic.reward.redress.KRedressDataManagerV2.KMountRedressDataManager.MountRedressData;
import com.kola.kmp.logic.reward.redress.KRedressDataManagerV2.KMountStoneRedressDataManager.MountStoneRedressData;
import com.kola.kmp.logic.reward.redress.KRedressDataManagerV2.KVIPRedressDataManager.VIPRedressData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.skill.KSkillLogic;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;
import com.kola.kmp.logic.util.ResultStructs.MailResult;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_SendMail;
import com.kola.kmp.logic.vip.KRoleVIP;
import com.kola.kmp.logic.vip.KVIPDataManager;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;
import com.kola.kmp.logic.vip.KVIPLogic;
import com.kola.kmp.logic.vip.KVIPRoleExtCACreator;

/**
 * <pre>
 * 改版补偿
 * 
 * @author CamusHuang
 * @creation 2014-11-25 下午8:35:13
 * </pre>
 */
public class KRedressCenter {

	static final KGameLogger REDRESS_LOGGER = KGameLogger.getLogger("redress");
	// 检测数据库是否存在补偿统计的fun_point_consume_info表的标志位
	public static boolean hasDBFunPointTotalConsumeTable = false;

	/**
	 * <pre>
	 * 执行改版补偿操作
	 * 如果服务器未初始化，则忽略；如果服务器已初始化完成，则执行
	 * 
	 * @param role
	 * @author CamusHuang
	 * @creation 2014-11-18 下午6:59:01
	 * </pre>
	 */
	public static void notifyForLoadFinished(KRole role) {
		if (KRedressSonModule.instance.isNotifyCacheLoadComplete) {
			doRedress(role);
		}
	}

	static void notifyCacheLoadComplete() throws KGameServerException {

		hasDBFunPointTotalConsumeTable = DBQueryManager.getInstance().checkHasDBFunPointTotalConsumeTable();

		RoleModuleSupport mRoleModuleSupport = KSupportFactory.getRoleModuleSupport();
		for (long roleId : mRoleModuleSupport.getRoleIdCache()) {
			KRole role = mRoleModuleSupport.getRole(roleId);
			doRedress(role);
		}
	}

	public static void doRedress(KRole role) {
		if (role == null) {
			return;
		}
		//
		doRedressV1(role);
		//
		doRedressV2(role);
		//
		doRedressLCS(role);
	}

	/**
	 * <pre>
	 * 执行改版补偿操作
	 * 
	 * @param role
	 * @author CamusHuang
	 * @creation 2014-11-18 下午6:59:01
	 * </pre>
	 */
	private static void doRedressV1(KRole role) {

		// 指定时间之前创建的角色，才执行
		if (role.getCreateTime() > KRedressDataManagerV1.RoleCreateEndTime) {
			return;
		}

		KRoleRedress roleData = KRedressSonModule.instance.getRewardSon(role.getId());
		roleData.rwLock.lock();
		try {

			// 是否已经执行过
			if (roleData.isRunVer1Redress()) {
				return;
			}

			// 清理角色非法的镶嵌宝石
			try {
				int loseHoles = KItemLogic.clearIllegalEnchanse(role);
				if (loseHoles > 0) {
					EquiEnchanseRedressData data = KRedressDataManagerV1.mEquiEnchanseRedressDataManager.getData(loseHoles);
					if (data != null) {
						RewardResult_SendMail result = data.mailReward.sendReward(role, PresentPointTypeEnum.改版补偿, false);
						REDRESS_LOGGER.warn("{},{},{},{},{},{}", role.getId(), role.getName(), "镶嵌", loseHoles, result.isSucess, result.tips);
					}
				}
			} catch (Exception e) {
				REDRESS_LOGGER.warn(StringUtil.format("{},{},{},{},{},{}", role.getId(), role.getName(), "镶嵌", 0, false, "发生异常：" + e.getMessage()), e);
			}

			// 清理角色的指定时装
			try {
				Set<Integer> deleteFashions = KFashionLogic.clearIllegalFashion(role, KRedressDataManagerV1.mFashionRedressDataManager.getDataCache().keySet());
				if (!deleteFashions.isEmpty()) {
					for (int fashionTempId : deleteFashions) {
						FashionRedressData data = KRedressDataManagerV1.mFashionRedressDataManager.getData(fashionTempId);
						if (data != null) {
							RewardResult_SendMail result = data.mailReward.sendReward(role, PresentPointTypeEnum.改版补偿, false);
							REDRESS_LOGGER.warn("{},{},{},{},{},{}", role.getId(), role.getName(), "时装", fashionTempId, result.isSucess, result.tips);
						}
					}
				}
			} catch (Exception e) {
				REDRESS_LOGGER.warn(StringUtil.format("{},{},{},{},{},{}", role.getId(), role.getName(), "时装", 0, false, "发生异常：" + e.getMessage()), e);
			}

			// 清理主动技能
			try {
				boolean isInit = true;
				KSkillRedressDataManager skillManager = KRedressDataManagerV1.mInitSkillRedressDataManager;
				Map<Integer, Integer> deleteSkills = KSkillLogic.clearIllegalSkill(isInit, role, skillManager.getDataCache());
				if (!deleteSkills.isEmpty()) {
					BaseRewardData totalData = null;
					for (Entry<Integer, Integer> e : deleteSkills.entrySet()) {
						SkillRedressData data = skillManager.getData(e.getKey(), e.getValue());
						if (data != null) {
							if (totalData == null) {
								totalData = data.baseReward;
							} else {
								totalData = BaseRewardData.mergeReward(totalData, data.baseReward);
							}
						}
					}
					if (totalData.checkIsEffect()) {
						MailResult result = KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), skillManager.baseMailContent, totalData, PresentPointTypeEnum.改版补偿);
						REDRESS_LOGGER.warn("{},{},{},{},{},{}", role.getId(), role.getName(), "主动技能", "", result.isSucess, result.tips);
					}
				}
			} catch (Exception e) {
				REDRESS_LOGGER.warn(StringUtil.format("{},{},{},{},{},{}", role.getId(), role.getName(), "主动技能", 0, false, "发生异常：" + e.getMessage()), e);
			}

			// 清理被动技能
			try {
				boolean isInit = false;
				KSkillRedressDataManager skillManager = KRedressDataManagerV1.mPasSkillRedressDataManager;
				Map<Integer, Integer> deleteSkills = KSkillLogic.clearIllegalSkill(isInit, role, skillManager.getDataCache());
				if (!deleteSkills.isEmpty()) {
					BaseRewardData totalData = null;
					for (Entry<Integer, Integer> e : deleteSkills.entrySet()) {
						SkillRedressData data = skillManager.getData(e.getKey(), e.getValue());
						if (data != null) {
							if (totalData == null) {
								totalData = data.baseReward;
							} else {
								totalData = BaseRewardData.mergeReward(totalData, data.baseReward);
							}
						}
					}
					if (totalData.checkIsEffect()) {
						MailResult result = KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), skillManager.baseMailContent, totalData, PresentPointTypeEnum.改版补偿);
						REDRESS_LOGGER.warn("{},{},{},{},{},{}", role.getId(), role.getName(), "被动技能", "", result.isSucess, result.tips);
					}
				}
			} catch (Exception e) {
				REDRESS_LOGGER.warn(StringUtil.format("{},{},{},{},{},{}", role.getId(), role.getName(), "被动技能", 0, false, "发生异常：" + e.getMessage()), e);
			}

			// 全局补偿
			try {
				if (KRedressDataManagerV1.redressForAllRole != null) {
					RewardResult_SendMail result = KRedressDataManagerV1.redressForAllRole.sendReward(role, PresentPointTypeEnum.改版补偿, false);
					REDRESS_LOGGER.warn("{},{},{},{},{},{}", role.getId(), role.getName(), "全局", "", result.isSucess, result.tips);
				}
			} catch (Exception e) {
				REDRESS_LOGGER.warn(StringUtil.format("{},{},{},{},{},{}", role.getId(), role.getName(), "全局", 0, false, "发生异常：" + e.getMessage()), e);
			}

			// 首充重置
			try {
				if (KRedressDataManagerV1.isResetFirstCharge) {
					KCurrencyAccountSet set = KCurrencyModuleExtension.getCurrencyAccountSet(role.getId());
					if (set.resetAndGetFirstCharge()) {
						// 重置首充
						REDRESS_LOGGER.warn("{},{},{},{},{},{}", role.getId(), role.getName(), "首充重置", "", true, "成功");
					}
				}
			} catch (Exception e) {
				REDRESS_LOGGER.warn(StringUtil.format("{},{},{},{},{},{}", role.getId(), role.getName(), "首充重置", 0, false, "发生异常：" + e.getMessage()), e);
			}

			//
			roleData.setRunVer1Redress(true);

		} finally {
			roleData.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 执行改版补偿操作
	 * 
	 * @param role
	 * @author CamusHuang
	 * @creation 2014-11-18 下午6:59:01
	 * </pre>
	 */
	private static void doRedressV2(KRole role) {

		// 指定时间之前创建的角色，才执行
		if (role.getCreateTime() > KRedressDataManagerV2.RoleCreateEndTime) {
			return;
		}

		KRoleRedress roleData = KRedressSonModule.instance.getRewardSon(role.getId());
		roleData.rwLock.lock();
		try {

			// 是否已经执行过
			if (roleData.isRunVer2Redress()) {
				return;
			}

			// 异能要塞补偿
			try {
				KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
				KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.爬塔副本关卡);
				if (record == null || record.towerCopyData == null) {
					REDRESS_LOGGER.warn("{},{},{},关卡ID,{},成功,{},tips,{}", role.getId(), role.getName(), "异能要塞", 0, false, "没有数据");
				} else {
					int nowLevelId = record.towerCopyData.nowLevelId;
					if (nowLevelId == 0) {
						// 无需补偿
					} else {
						BaseMailRewardData reawrdData = KRedressDataManagerV2.mLadderRedressDataManager.getData(nowLevelId);
						if (reawrdData == null) {
							REDRESS_LOGGER.warn("{},{},{},关卡ID,{},成功,{},tips,{}", role.getId(), role.getName(), "异能要塞", nowLevelId, true, "无补偿数据");
						} else {
							RewardResult_SendMail result = reawrdData.sendReward(role, PresentPointTypeEnum.改版补偿V2, false);
							REDRESS_LOGGER.warn("{},{},{},关卡ID,{},成功,{},tips,{}", role.getId(), role.getName(), "异能要塞", nowLevelId, result.isSucess, result.tips);
						}
					}
				}
			} catch (Exception e) {
				REDRESS_LOGGER.warn(StringUtil.format("{},{},{},关卡ID,{},成功,{},tips,{}", role.getId(), role.getName(), "异能要塞", 0, false, "发生异常：" + e.getMessage()), e);
			}

			// 欢乐送次数补偿
			try {
				KRoleWishData data = KGambleRoleExtCACreator.getGambleRoleExtData(role.getId()).getWishData();
				if (data == null) {
					REDRESS_LOGGER.warn("{},{},{},次数,{},钻石,{},成功,{},tips,{}", role.getId(), role.getName(), "欢乐送", 0, 0, false, "没有数据");
				} else {
					int diceTimes = data.diceTimes.get();
					if (diceTimes < 1) {
						// 无需补偿，REDRESS_LOGGER.warn("{},{},{},次数,{},金币,{},成功,{},tips,{}",
						// role.getId(), role.getName(), "欢乐送", 0, 0, true,
						// "无需补偿");
					} else {
						long money = diceTimes * KRedressDataManagerV2.mHappyTimeRedressDataManager.SingleDiamonds;
						if (money < 1) {
							REDRESS_LOGGER.warn("{},{},{},次数,{},钻石,{},成功,{},tips,{}", role.getId(), role.getName(), "欢乐送", 0, money, true, "无需补偿");
						} else {
							BaseMailContent mailContent = KRedressDataManagerV2.mHappyTimeRedressDataManager.mailContent;
							MailResult result = KSupportFactory.getMailModuleSupport().sendMoneyMailBySystem(role.getId(), Arrays.asList(new KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND, money)),
									PresentPointTypeEnum.改版补偿V2, mailContent.getMailTitle(), mailContent.getMailContent());
							REDRESS_LOGGER.warn("{},{},{},次数,{},钻石,{},成功,{},tips,{}", role.getId(), role.getName(), "欢乐送", diceTimes, money, result.isSucess, result.tips);
						}
					}
				}
			} catch (Exception e) {
				REDRESS_LOGGER.warn(StringUtil.format("{},{},{},次数,{},金币,{},成功,{},tips,{}", role.getId(), role.getName(), "欢乐送", 0, 0, false, "发生异常：" + e.getMessage()), e);
			}

			// VIP等级礼包补偿
			try {
				KRoleVIP data = KVIPRoleExtCACreator.getRoleVIP(role.getId());
				if (data == null) {
					REDRESS_LOGGER.warn("{},{},{},VIP,{},成功,{},tips,{}", role.getId(), role.getName(), "VIP礼包补偿", 0, false, "没有数据");
				} else {
					data.rwLock.lock();
					try {
						int nowLv = data.getLv();
						for (int lv = KVIPDataManager.mVIPLevelDataManager.getMinLevel().lvl; lv <= nowLv; lv++) {
							if (!data.isCollectedLvReward(lv)) {
								continue;
							}

							VIPRedressData oldData = KRedressDataManagerV2.mVIPRedressDataManager.getData(lv);
							if (oldData == null) {
								REDRESS_LOGGER.warn("{},{},{},VIP,{},成功,{},tips,{}", role.getId(), role.getName(), "VIP礼包补偿", lv, false, "无补偿数据");
							} else if (oldData.mailReward == null) {
								// 无需补偿，REDRESS_LOGGER.warn("{},{},{},VIP,{},成功,{},tips,{}",
								// role.getId(), role.getName(), "VIP礼包补偿", lv,
								// false, "无需补偿");
							} else {
								RewardResult_SendMail result = oldData.mailReward.sendReward(role, PresentPointTypeEnum.改版补偿V2, false);
								REDRESS_LOGGER.warn("{},{},{},VIP,{},成功,{},tips,{}", role.getId(), role.getName(), "VIP礼包补偿", lv, result.isSucess, result.tips);
							}
						}
					} finally {
						data.rwLock.unlock();
					}
				}
			} catch (Exception e) {
				REDRESS_LOGGER.warn(StringUtil.format("{},{},{},VIP,{},成功,{},tips,{}", role.getId(), role.getName(), "VIP礼包补偿", 0, false, "发生异常：" + e.getMessage()), e);
			}

			// VIP等级刷新
			try {
				KRoleVIP data = KVIPRoleExtCACreator.getRoleVIP(role.getId());
				if (data == null) {
					REDRESS_LOGGER.warn("{},{},{},原VIP,{},原经验,{},新等级,{},新经验,{},成功,{},tips,{}", role.getId(), role.getName(), "VIP等级刷新", 0, 0, 0, 0, false, "没有数据");
				} else {
					boolean isUpLv = false;
					int oldVip = 0;
					data.rwLock.lock();
					try {
						oldVip = data.getLv();
						int oldExp = data.getExp();
						if (oldVip < 1) {
							// 无需补偿，REDRESS_LOGGER.warn("{},{},{},原VIP,{},原经验,{},新等级,{},新经验,{},成功,{},tips,{}",
							// role.getId(), role.getName(), "VIP等级刷新", oldVip,
							// oldExp, 0, 0, false, "无需刷新");
						} else {
							VIPRedressData oldData = KRedressDataManagerV2.mVIPRedressDataManager.getData(oldVip);
							if (oldData == null) {
								REDRESS_LOGGER.warn("{},{},{},原VIP,{},原经验,{},新等级,{},新经验,{},成功,{},tips,{}", role.getId(), role.getName(), "VIP等级刷新", oldVip, oldExp, 0, 0, false, "无补偿数据");
							} else {
								VIPLevelData newData = KVIPDataManager.mVIPLevelDataManager.getLevelData(oldVip);
								if (newData.totalCharge == oldData.totalOldCharge) {
									// 无需补偿，REDRESS_LOGGER.warn("{},{},{},原VIP,{},原经验,{},新等级,{},新经验,{},成功,{},tips,{}",
									// role.getId(), role.getName(), "VIP等级刷新",
									// oldVip, oldExp, 0, 0, false, "无需刷新");
								} else {
									// 新旧VIP所需经验不同，重算VIP等级
									int totalCharge = oldData.totalOldCharge + oldExp;
									data.setLv(0);
									data.setExp(totalCharge);
									//
									KVIPLogic.tryToUpVIPLv(data);
									if (data.getLv() > oldVip) {
										isUpLv = true;
									}

									REDRESS_LOGGER.warn("{},{},{},原VIP,{},原经验,{},新等级,{},新经验,{},成功,{},tips,{}", role.getId(), role.getName(), "VIP等级刷新", oldVip, oldExp, data.getLv(), data.getExp(),
											true, "成功");
								}
							}
						}
					} finally {
						data.rwLock.unlock();
						if (isUpLv) {
							// vip礼包自动领取
							KVIPLogic.autoCollectVipLvReward(role);

							// 将VIP升级通知到各相关模块
							KVIPLogic.vipUpLvNotify(data, oldVip);
						}
					}
				}
			} catch (Exception e) {
				REDRESS_LOGGER.warn(StringUtil.format("{},{},{},原VIP,{},原经验,{},新等级,{},新经验,{},成功,{},tips,{}", role.getId(), role.getName(), "VIP等级刷新", 0, 0, 0, 0, false, "发生异常：" + e.getMessage()), e);
			}

			// 机甲培养补偿
			try {
				KMountSet set = KMountModuleExtension.getMountSet(role.getId());
				set.rwLock.lock();
				try {
					// 3、 原额返还，原机甲培养的钻石
					// 金币补偿值=培养生命值/9.54*0.7*33400
					List<KCurrencyCountStruct> moneys = new ArrayList<KCurrencyCountStruct>();
					long addDiamond = 0;
					long addGold = 0;
					{
						if (!hasDBFunPointTotalConsumeTable) {
							List<DBFunPointConsumeRecord> list = DBQueryManager.getInstance().queryDBFunPointConsumeRecord(role.getId(), KRedressDataManagerV2.RoleCreateStartTime,
									KRedressDataManagerV2.RoleCreateEndTime);
							for (DBFunPointConsumeRecord record : list) {
								if (record.funType == UsePointFunctionTypeEnum.坐驾培养.funType) {
									addDiamond += record.consumePoint;
								}
							}
						} else {
							DBFunPointTotalConsume record = DBQueryManager.getInstance().caculateDBFunPointTotalConsumeByType(role.getId(), UsePointFunctionTypeEnum.坐驾培养.funType);
							if (record != null) {
								addDiamond += record.consumePoint;
							}
						}
						if (addDiamond > 0) {
							moneys.add(new KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND, addDiamond));
						}
					}
					{
						for (KMountOld oldMount : set.getOldMountMap().values()) {
							AtomicInteger avalue = oldMount.getTrainAttsMapCache().get(KGameAttrType.MAX_HP);
							int value = avalue == null ? 0 : avalue.get();
							addGold += (value * 33400 * 0.7 / 9.54);
						}
						if (addGold > 0) {
							moneys.add(new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, addGold));
						}
					}

					if (moneys.isEmpty()) {
						// 无需补偿，REDRESS_LOGGER.warn("{},{},{},钻石,{},金币,{},成功,{},tips,{}",
						// role.getId(), role.getName(), "机甲培养", addDiamond,
						// addGold, true, "无需补偿");
					} else {
						MailResult result = KSupportFactory.getMailModuleSupport().sendMoneyMailBySystem(role.getId(), moneys, PresentPointTypeEnum.改版补偿V2,
								KRedressDataManagerV2.mMountTrainRedressMail.getMailTitle(), KRedressDataManagerV2.mMountTrainRedressMail.getMailContent());
						REDRESS_LOGGER.warn("{},{},{},钻石,{},金币,{},成功,{},tips,{}", role.getId(), role.getName(), "机甲培养", addDiamond, addGold, result.isSucess, result.tips);
					}
				} finally {
					set.rwLock.unlock();
				}
			} catch (Exception e) {
				REDRESS_LOGGER.warn(StringUtil.format("{},{},{},钻石,{},金币,{},成功,{},tips,{}", role.getId(), role.getName(), "机甲培养", 0, 0, false, "发生异常：" + e.getMessage()), e);
			}

			// 机甲养成补偿
			try {
				KMountSet set = KMountModuleExtension.getMountSet(role.getId());
				set.rwLock.lock();
				try {
					// 机甲模板ID--基础材料、货币
					// 额外材料数量=原经验/原经验最大值*额外材料基数
					// 额外金币数量=原经验/原经验最大值*额外金币基数
					List<ItemCountStruct> addItems = new ArrayList<ItemCountStruct>();
					List<KCurrencyCountStruct> addMoneys = new ArrayList<KCurrencyCountStruct>();
					for (KMountOld oldMount : set.getOldMountMap().values()) {
						addItems.clear();
						addMoneys.clear();
						//
						MountRedressData redressData = KRedressDataManagerV2.mMountRedressDataManager.getData(oldMount.getTemplateId());
						if (redressData == null) {
							REDRESS_LOGGER.warn("{},{},{},机甲,{},成功,{},tips,{}", role.getId(), role.getName(), "机甲养成", oldMount.getTemplateId(), false, "无补偿数据");
							continue;
						}
						addItems.addAll(redressData.baseItems);
						addMoneys.add(redressData.baseMoney);
						//
						if (redressData.oldMaxExp > 0) {
							float rate = (float) oldMount.getExp() / redressData.oldMaxExp;
							int itemCount = (int) (rate * redressData.additionalItem.itemCount);
							if (itemCount > 0) {
								addItems.add(new ItemCountStruct(redressData.additionalItem.getItemTemplate(), itemCount));
							}
							long moneyCount = (int) (rate * redressData.additionalMoney.currencyCount);
							if (moneyCount > 0) {
								addMoneys.add(new KCurrencyCountStruct(redressData.additionalMoney.currencyType, moneyCount));
							}
						}
						set.notifyElementDelete(oldMount._id);
						//
						BaseRewardData baseReward = new BaseRewardData(null, addMoneys, addItems, null, null, true, true);
						MailResult result = KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), redressData.mailContent, baseReward, PresentPointTypeEnum.改版补偿V2);
						//
						REDRESS_LOGGER.warn("{},{},{},机甲,{},成功,{},tips,{}", role.getId(), role.getName(), "机甲养成", oldMount.getTemplateId(), result.isSucess, result.tips);
					}
				} finally {
					set.rwLock.unlock();
				}
			} catch (Exception e) {
				REDRESS_LOGGER.warn(StringUtil.format("{},{},{},机甲,{},成功,{},tips,{}", role.getId(), role.getName(), "机甲养成", 0, false, "发生异常：" + e.getMessage()), e);
			}

			// 机甲石替换
			try {
				for (MountStoneRedressData data : KRedressDataManagerV2.mMountStoneRedressDataManager.dataMap.values()) {
					KItem item = KItemLogic.searchItemFromBag(role.getId(), data.oldItemId);
					if (item == null) {
						continue;
					}
					long count = item.getCount() * data.newItemCount;
					if (!KSupportFactory.getItemModuleSupport().removeItemFromBag(role.getId(), item.getId(), item.getCount())) {
						// 清除失败
						REDRESS_LOGGER.warn("{},{},{},旧材料ID,{},数量,{},成功,{},tips,{}", role.getId(), role.getName(), "机甲石替换", data.oldItemId, item.getCount(), false, "删除旧物品失败");
						continue;
					}
					if (count > 0) {
						ItemResult_AddItem result = KSupportFactory.getItemModuleSupport().addItemToBag(role, data.newItemId, count, "改版替换");
						REDRESS_LOGGER.warn("{},{},{},新材料ID,{},数量{},成功,{},tips,{}", role.getId(), role.getName(), "机甲石替换", data.newItemId, count, result.isSucess, result.tips);
					}
				}
			} catch (Exception e) {
				REDRESS_LOGGER.warn(StringUtil.format("{},{},{},旧材料ID,{},数量,{},成功,{},tips,{}", role.getId(), role.getName(), "机甲石替换", 0, 0, false, "发生异常：" + e.getMessage()), e);
			}

			// 全局补偿
			try {
				if (KRedressDataManagerV2.redressForAllRole != null) {
					RewardResult_SendMail result = KRedressDataManagerV2.redressForAllRole.sendReward(role, PresentPointTypeEnum.改版补偿V2, false);
					REDRESS_LOGGER.warn("{},{},{},成功,{},tips,{}", role.getId(), role.getName(), "全局", result.isSucess, result.tips);
				}
			} catch (Exception e) {
				REDRESS_LOGGER.warn(StringUtil.format("{},{},{},成功,{},tips,{}", role.getId(), role.getName(), "全局", false, "发生异常：" + e.getMessage()), e);
			}

			// 机甲按等级开放一次
			try {
				KMountLogic.presentMountForLv(role);
			} catch (Exception e) {
				REDRESS_LOGGER.warn(StringUtil.format("{},{},{},成功,{},tips,{}", role.getId(), role.getName(), "机甲按等级开放", false, "发生异常：" + e.getMessage()), e);
			}
			// 技能按等级开放一次
			try {
				KSkillLogic.newSkillsForUplv(role, false);
			} catch (Exception e) {
				REDRESS_LOGGER.warn(StringUtil.format("{},{},{},成功,{},tips,{}", role.getId(), role.getName(), "技能按等级开放", false, "发生异常：" + e.getMessage()), e);
			}
			//
			roleData.setRunVer2Redress(true);
		} finally {
			roleData.rwLock.unlock();
		}
	}
	
	/**
	 * <pre>
	 * 执行合区补偿操作
	 * 
	 * @param role
	 * @author CamusHuang
	 * @creation 2015-2-9 下午4:24:55
	 * </pre>
	 */
	private static void doRedressLCS(KRole role) {

		// 新角色，不执行
		if (role.getCreateTime() >= KRedressDataManagerLCS.LastCombimeServerTime) {
			return;
		}

		KRoleRedress roleData = KRedressSonModule.instance.getRewardSon(role.getId());
		roleData.rwLock.lock();
		try {
			// 是否已经执行过
			if(roleData.getLastCSRedress()>=KRedressDataManagerLCS.LastCombimeServerTime){
				return;
			}
			try {
				BaseMailRewardData rewardData = KRedressDataManagerLCS.countRedress(role.getGSId());
				if(rewardData==null){
					// 无需补偿
				} else {
					RewardResult_SendMail mailResult = rewardData.sendReward(role, PresentPointTypeEnum.合区补偿, false);
					REDRESS_LOGGER.warn("{},{},{},成功,{},tips,{}", role.getId(), role.getName(), "合区", mailResult.isSucess, mailResult.tips);
				}
			} catch (Exception e) {
				REDRESS_LOGGER.warn(StringUtil.format("{},{},{},成功,{},tips,{}", role.getId(), role.getName(), "合区", false, "发生异常：" + e.getMessage()), e);
			}
			//
			roleData.setLastCSRedress(System.currentTimeMillis());
		} finally {
			roleData.rwLock.unlock();
		}
	}	
}
