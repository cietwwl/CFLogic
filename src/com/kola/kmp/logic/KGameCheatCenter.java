package com.kola.kmp.logic;

import static com.kola.kmp.protocol.pet.KPetProtocol.SM_NOTIFY_PET_FLOW_SUCCESS;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.KGameModule;
import com.koala.game.dataaccess.KGameDataAccessFactory;
import com.koala.game.exception.KGameServerException;
import com.koala.game.gameserver.KGameServer;
import com.koala.game.logging.KGameLogger;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimer;
import com.koala.game.timer.KGameTimerTask;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.StringUtil;
import com.koala.paymentserver.PayExtParam;
import com.koala.paymentserver.PayOrder;
import com.kola.kgame.cache.KGameEngineModule;
import com.kola.kgame.cache.flowdata.FlowDataModuleFactory;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kgame.cache.pet.Pet;
import com.kola.kgame.cache.role.Role;
import com.kola.kgame.cache.role.RoleModuleFactory;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.KActivityManager;
import com.kola.kmp.logic.activity.KActivityRoleExtCaCreator;
import com.kola.kmp.logic.activity.KActivityRoleExtData;
import com.kola.kmp.logic.activity.goldact.KGoldActivityManager;
import com.kola.kmp.logic.activity.newglodact.KNewGoldActivity;
import com.kola.kmp.logic.activity.newglodact.NewGoldActivityRoleRecordData;
import com.kola.kmp.logic.activity.transport.KTransportData;
import com.kola.kmp.logic.activity.worldboss.KWorldBossActivityField.KWorldBossMonsterInfo;
import com.kola.kmp.logic.activity.worldboss.KWorldBossActivityMain;
import com.kola.kmp.logic.activity.worldboss.KWorldBossActivityMonitor;
import com.kola.kmp.logic.activity.worldboss.KWorldBossFieldData;
import com.kola.kmp.logic.activity.worldboss.KWorldBossManager;
import com.kola.kmp.logic.chat.KChatLogic;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatEnhanceInfo;
import com.kola.kmp.logic.combat.ICombatForce;
import com.kola.kmp.logic.combat.ICombatGround;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.combat.impl.KCombatGroundBaseImpl;
import com.kola.kmp.logic.combat.impl.KCombatImpl;
import com.kola.kmp.logic.combat.impl.KCombatManager;
import com.kola.kmp.logic.combat.operation.IOperationMsgHandler;
import com.kola.kmp.logic.combat.resulthandler.ICombatResultHandler;
import com.kola.kmp.logic.competition.KCompetitionManager;
import com.kola.kmp.logic.competition.KCompetitionModule;
import com.kola.kmp.logic.competition.KCompetitor;
import com.kola.kmp.logic.competition.KHallOfFrameData;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPManager;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPTeam;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPTeam.KTeamPVPTeamMember;
import com.kola.kmp.logic.currency.KPaymentListener;
import com.kola.kmp.logic.flow.KPetFlowType;
import com.kola.kmp.logic.flow.KRoleAttrModifyType;
import com.kola.kmp.logic.gamble.KGambleModule;
import com.kola.kmp.logic.gamble.KGambleRoleExtCACreator;
import com.kola.kmp.logic.gamble.KGambleRoleExtData;
import com.kola.kmp.logic.gamble.wish.DebugTestWish;
import com.kola.kmp.logic.gamble.wish.KRoleWishData;
import com.kola.kmp.logic.gamble.wish.KWishSystemManager;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.gang.KGangPositionEnum;
import com.kola.kmp.logic.gang.reswar.KResWarConfig;
import com.kola.kmp.logic.gang.reswar.ResWarLogic;
import com.kola.kmp.logic.gang.war.GangWarLogic;
import com.kola.kmp.logic.gang.war.KGangWarConfig;
import com.kola.kmp.logic.gang.war.KGangWarDataManager;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KGangMedalDataManager.GangMedalData;
import com.kola.kmp.logic.item.KItem;
import com.kola.kmp.logic.item.KItemDataLoader;
import com.kola.kmp.logic.item.KItemDataManager;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempEqui;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.item.KItemModuleExtension;
import com.kola.kmp.logic.item.KItemSet;
import com.kola.kmp.logic.item.message.KPushItemsMsg;
import com.kola.kmp.logic.level.BattlefieldWaveViewInfo;
import com.kola.kmp.logic.level.FightEvaluateData;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.MonsterData;
import com.kola.kmp.logic.level.KEnterLevelCondition;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KGameLevelRecord;
import com.kola.kmp.logic.level.KGameLevelRecord.PlayerRoleGamelevelData;
import com.kola.kmp.logic.level.KGameLevelSet;
import com.kola.kmp.logic.level.KGameScenario;
import com.kola.kmp.logic.level.KLevelTemplate;
import com.kola.kmp.logic.level.copys.KFriendCopyManager;
import com.kola.kmp.logic.level.copys.KPetChallengeCopyManager;
import com.kola.kmp.logic.level.copys.KSeniorPetChallengeCopyManager;
import com.kola.kmp.logic.level.copys.KTowerCopyManager;
import com.kola.kmp.logic.level.tower.KTowerBattlefield;
import com.kola.kmp.logic.level.tower.KTowerLevelTemplate;
import com.kola.kmp.logic.mail.KMail;
import com.kola.kmp.logic.mail.KMailLogic;
import com.kola.kmp.logic.mail.KMailModuleExtension;
import com.kola.kmp.logic.mail.KMailSet;
import com.kola.kmp.logic.map.KGameMapEntity;
import com.kola.kmp.logic.map.KGameNormalMap;
import com.kola.kmp.logic.map.KMapModule;
import com.kola.kmp.logic.mission.IMissionMenuImpl;
import com.kola.kmp.logic.mission.KMission;
import com.kola.kmp.logic.mission.KMissionCompleteRecordSet;
import com.kola.kmp.logic.mission.KMissionEventListener;
import com.kola.kmp.logic.mission.KMissionModuleExtension;
import com.kola.kmp.logic.mission.KMissionModuleSupportImpl;
import com.kola.kmp.logic.mission.KMissionSet;
import com.kola.kmp.logic.mission.KMissionTemplate;
import com.kola.kmp.logic.mission.guide.FunctionOpenRecord;
import com.kola.kmp.logic.mission.guide.KGuideManager;
import com.kola.kmp.logic.mission.guide.MainMenuFunction;
import com.kola.kmp.logic.mount.KMount;
import com.kola.kmp.logic.mount.KMountDataManager;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountTemplate;
import com.kola.kmp.logic.mount.KMountLogic;
import com.kola.kmp.logic.npc.KNPCDataStructs.KNPCTemplate;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.npc.menu.IMissionMenu;
import com.kola.kmp.logic.npc.menu.KMenuService;
import com.kola.kmp.logic.npc.message.KNPCOrderMsg;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KColorFunEnum;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KGameMissionFunTypeEnum;
import com.kola.kmp.logic.other.KGameMissionStatusEnum;
import com.kola.kmp.logic.other.KItemQualityEnum;
import com.kola.kmp.logic.other.KItemTypeEnum;
import com.kola.kmp.logic.other.KMapEntityTypeEnum;
import com.kola.kmp.logic.other.KNPCOrderEnum;
import com.kola.kmp.logic.other.KPetQuality;
import com.kola.kmp.logic.other.KRoleExtTypeEnum;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.pet.KPet;
import com.kola.kmp.logic.pet.KPetModuleConfig;
import com.kola.kmp.logic.pet.KPetModuleManager;
import com.kola.kmp.logic.pet.KPetSet;
import com.kola.kmp.logic.pet.KPetTemplate;
import com.kola.kmp.logic.pet.KPetTemplateHandbookModel;
import com.kola.kmp.logic.reward.KRewardDataManager;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.reward.activatecode.KActivateCodeDataManager;
import com.kola.kmp.logic.reward.dynamic.DynamicRewardCenter;
import com.kola.kmp.logic.reward.garden.KGardenCenter;
import com.kola.kmp.logic.role.IRoleBaseInfo;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.role.KRoleBaseInfoCacheManager;
import com.kola.kmp.logic.role.KRoleModuleConfig;
import com.kola.kmp.logic.role.message.KRoleServerMsgPusher;
import com.kola.kmp.logic.shop.timehot.KHotShopCenter;
import com.kola.kmp.logic.support.CombatModuleSupport.ICombatMonsterUpdateInfo;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.IRoleEquipShowData;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;
import com.kola.kmp.logic.util.ResultStructs.MailResult_Sync;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.PetTips;
import com.kola.kmp.logic.vip.KVIPDataManager;
import com.kola.kmp.logic.vip.KVIPLogic;
import com.kola.kmp.protocol.mission.KMissionProtocol;

/**
 * <pre>
 * 【----------------------以下是技术专用指令--------------------】
 * GM通道前缀：gsorder-区号:角色名:指令
 * 执行时效任务【@010622,com.koala.kgamelogic.util.AddDirtyWordPatch,脏词1;脏词2】
 * 替换指定消息的处理器【@replace,消息id,消息处理类路径】
 * 替换战斗指令处理类【@replaceOpMsgHandler,2wsxCDE#,新的路径】
 * 
 * 查指定角色的玩家ID、角色ID【@roleid】
 * 断开链接【@closeSession】
 * 
 * 【卡机补偿】重新加载【@kajiRewardReload,45636$#$wqroll】
 * 【体力免费领取】重新加载【@phypowerRewardReload,45636$#$wqroll】
 * 【每日邮件】重新加载【@reloadDailyMail,45636$#$wqroll】
 * 【精彩活动、限时活动、排行榜奖励】重新加载【@ExcitingReload,45636$#$wqroll,是否推送一次精彩活动】
 * 【限时热购商品表】重新加载【@HotGoodsReload,45636$#$wqroll】
 * 【新礼包道具模板】加载【@reloadNewGifTemplates,45636$#$wqroll】
 * 【激活码类型表】重新加载【@reloadActiviCode,45636$#$wqroll】
 * 【动态奖励】执行【@DynReward,45636$#$wqroll,role/family】
 * 
 * 【军团战】重新加载配置【@reloadGangWarConfig】
 * 【军团战】重启【@restartGangWar,isReloadXmlConfig,isForce】
 * 【军团战】重启【@startGangWar,1,201411171900】指定具体报名结束时间
 * 【军团战】重启【@startGangWar,2,20】指定N分钟后报名结束
 * 
 * 【军团[资源]战】重新加载配置【@reloadGangResWarConfig】
 * 【军团[资源]战】重启【@restartGangResWar,isReloadXmlConfig,isForce】
 * 
 * 【----------------------以下是内部公开指令--------------------】
 * 加时装【@fashion,时装模板Id】
 * 加机甲【@mount,机甲模板Id】
 * 加道具 【@additem,道具模板id,道具数量】
 * 清空背包 【@clearbag】
 * 加一堆物品【@et】
 * 加货币 【@addmoney,货币类型,货币数量】----1钻石2金币3潜能4荣誉6贡献
 * 加军团资金 【@addGangMoney,数量】
 * 加军团经验 【@addGangExp,数量】
 * 加技能【@addSkill,是否主动技能,技能模板ID】
 * 完成活跃度【@vitality,类型,次数】
 * 完成全部活跃度【@vitality】
 * 测试发送附件邮件【@testMail】
 * 清空邮件【@clearmail】
 * 庄园植物全部加速N分钟【@garden,N】
 * 庄园指定植物M加速N分钟【@garden,M,N】
 * 设置VIP等级 【@vip,vip等级】
 * 模拟充值【@charge,人民币数量(元)】
 * 仿真充值(记录帐号首充)【@hscharge,人民币数量(元)】
 * 
 * 发送在线玩家邮件【@onlineMail,最小等级,最大等级,邮件标题,邮件内容】
 * 复制指定角色【@cloneRole,srcRoleName】
 * 设置聊天等级限制【@chatlv,世界频道等级,区域频道等级】-1表示不变
 * 设置军团战勋章【@setMedal,1001】-2表示查询，-1表示取消，1，2，3分别表示名次
 * 
 * 提升等级【@lv,需要提升到的等级】
 * 添加随从【@addPet,模板id,模板id...】
 * 开启所有关卡【@openLevel,1】
 * 世界boss指令【@worldBoss,指令类型（1=按照正常开启一次世界boss，2=加入世界boss，3=结束世界boss，4,id=结束单场世界boss，5=进入战斗，6=离开世界boss，7=鼓舞，8=马上开始世界boss，9=纪录世界boss数据）】
 * 开启指定功能【@openFunc,功能id。如果后面不接参数，则表示开启所有功能】
 * 扣减体力【@dphy,体力值】
 * 重置好友副本【@clearFdCopy】
 * 重置打金活动次数【@clearBarrel】
 * 清除打金活动cd【@clearBarrelCD】
 * 重置随从副本【@clearPetCopy】
 * 怒气满格【@fullEnergy】
 * 提升指定随从等级【@petlv,角色id,随从id,等级】
 * 设定属性【@setAttr,属性类型,属性值】
 * 完成指定任务【@mission,任务id】
 * 接受指定任务【@accept,任务id】
 * 增加竞技场挑战次数【@incCmpChallengeTimes,roleId,次数】
 * 派发战队竞技每日奖励【@teamPVPDaily】
 * 模拟战队的每日重置【@teamPVPReset】
 * 提升战队段位到某个段位【@teampvpPromote,段位id】
 * 将战队降到某个段位【@teampvpDemote,段位id】
 * 
 * @author CamusHuang
 * @creation 2014-3-7 下午9:04:31
 * </pre>
 */
public class KGameCheatCenter {

	private static Logger _LOGGER = KGameLogger.getLogger(KGameCheatCenter.class);

	public static class CheatResult {
		public boolean isOrder;// 是否是指令
		public boolean isSuccess;// 指令是否处理成功
		public String tips;// 反馈信息
	}

	/**
	 * <pre>
	 * 处理来自角色聊天面板的指令
	 * 
	 * @param senderRole
	 * @param chatStr
	 * @return true表示是指令；false表示不是指令
	 * @author CamusHuang
	 * @creation 2014-3-5 上午11:42:42
	 * </pre>
	 */
	public static boolean processCheatFromRole(KRole role, String content) {
		if (KGameGlobalConfig.isOpenCheatCmd()) {
			CheatResult result = processCheat(content, role);
			if (result.isOrder) {
				if (result.tips == null || result.tips.isEmpty()) {
					if (result.isSuccess) {
						result.tips = "指令处理成功！";
					} else {
						result.tips = "指令处理失败！";
					}
				}
				KSupportFactory.getChatSupport().sendChatToRole(result.tips, role.getId());
			}
			return result.isOrder;
		} else {
			if (content.startsWith("@")) {
				return true;
			}
			return false;
		}
	}

	/**
	 * <pre>
	 * 处理来自GM的指令
	 * 
	 * @param content
	 * @param roleName
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-7 下午8:19:50
	 * </pre>
	 */
	public static CheatResult processCheatFromGM(String content, String roleName) {

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleName);
		if (role == null) {
			CheatResult result = new CheatResult();
			result.tips = "角色不存在！";
			return result;
		}

		CheatResult result = processCheat(content, role);
		if (result.tips == null || result.tips.isEmpty()) {
			if (result.isSuccess) {
				result.tips = "指令处理成功！";
			} else {
				result.tips = "指令处理失败！";
			}
		}
		return result;
	}

	private static CheatResult processCheat(String content, KRole role) {
		CheatResult result = new CheatResult();

		if (!content.startsWith("@")) {
			result.tips = "非指令！";
			return result;
		}

		// 有可能是指令，具体判断
		String[] args = content.split(",");
		String cmd = args[0];

//		if (cmd.equalsIgnoreCase("@gangWar")) {
//			// @gangWar
//			result.isOrder = true;
//			result.tips = GangWarLogic.dealGMOrder(role, args);
//			result.isSuccess = true;
//			return result;
//		}
		
		if (cmd.equalsIgnoreCase("@setMedal")) {
			// @setMedal
			result.isOrder = true;
			int rank = Integer.parseInt(args[1]);
			KGang gang = KSupportFactory.getGangSupport().getGangByRoleId(role.getId());
			if (gang == null) {
				result.tips = "军团不存在";
				return result;
			}

			if (rank == -2) {
				int medalRank = gang.searchPosition(KGangPositionEnum.军团长).getMedal();
				result.tips = "勋章=" + medalRank;
				return result;
			}

			if (rank == -1) {
				KGangLogic.clearGangWarMedalForSignUpEnd(gang);
				result.tips = "取消勋章成功";
				result.isSuccess = true;
				return result;
			}

			GangMedalData medalData = KGangWarDataManager.mGangMedalDataManager.getDataByRank(rank);
			if (medalData == null) {
				result.tips = "排名勋章奖励不存在";
				return result;
			}

			KGangLogic.sendGangWarFinalReward(gang.getId(), medalData);
			result.tips = "设置勋章执行成功";
			result.isSuccess = true;
			return result;
		}
		
		if (cmd.equalsIgnoreCase("@closeSession")) {
			// @closeSession
			result.isOrder = true;
			KGamePlayerSession playerSession = KGameServer.getInstance().getPlayerManager().getPlayerSession(role.getPlayerId());
			if (playerSession == null) {
				result.tips = "连接不存在";
				return result;
			}
			playerSession.close();
			//
			result.isSuccess = true;
			return result;
		}

		if (cmd.equalsIgnoreCase("@replace")) {
			result.isOrder = true;
			// 替换消息处理器
			GameMessageProcesser now = null;
			int id = Integer.parseInt(args[1]);
			try {
				KGameModule module = null;
				Field gameModuleField = KGameServer.class.getDeclaredField("modules");
				gameModuleField.setAccessible(true);
				Map<String, KGameModule> moduleMap = (Map<String, KGameModule>) gameModuleField.get(KGameServer.getInstance());
				for (Iterator<Map.Entry<String, KGameModule>> itr = moduleMap.entrySet().iterator(); itr.hasNext();) {
					module = itr.next().getValue();
					if (module instanceof KGameEngineModule) {
						break;
					} else {
						module = null;
						continue;
					}
				}
				gameModuleField.setAccessible(false);
				if (module != null) {
					String clazz = args[2];
					Field field = KGameEngineModule.class.getDeclaredField("_msgProcessers");
					field.setAccessible(true);
					@SuppressWarnings("unchecked")
					Map<Integer, GameMessageProcesser> map = (Map<Integer, GameMessageProcesser>) field.get(module);
					field.setAccessible(false);

					GameMessageProcesser prc = (GameMessageProcesser) Class.forName(clazz).newInstance();
					map.put(id, prc);
					now = map.get(id);
					result.isSuccess = true;
				} else {
					result.tips = "找不到指定的模块!";
				}
			} catch (Exception e) {
				e.printStackTrace();
				result.tips = "出异常啦！！！" + e.getMessage();
			}
			if (result.isSuccess) {
				result.tips = "替换成功！" + id + "当前的处理类：" + now.getClass().getName();
			}
			return result;
		}
		
		else if ("@testReplace".equalsIgnoreCase(cmd)) {
			return processCheat("@replace,23104,com.kola.kmp.logic.competition.message.KRequestMatchChallengerMsgProcesser", role);
		}
		
		else if ("@testReplaceHandler".equalsIgnoreCase(cmd)) {
//			return processCheat("@replaceOpMsgHandler,2wsxCDE#,com.kola.kmp.logic.combat.operation.KOperationMsgHandlerImplV2", role);
//			result.isOrder = true;
//			result.isSuccess = true;
//			String path = args[1];
			return processCheat("@010622,com.kola.kmp.logic.KReplaceOpMsgHandlerPro,com.kola.kmp.logic.combat.operation.KOperationMsgHandlerImplV3", role);
		}
		
		else if ("@replaceHandler".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				int combatType = Integer.parseInt(args[1]);
				String preClazzName = args[2];
				String nowClazzName = args[3];
				KCombatType type = KCombatType.getCombatType(combatType);
				@SuppressWarnings("rawtypes")
				Class preClazz = Class.forName(preClazzName);
				Field field = KCombatManager.class.getDeclaredField("_combatResultHandlers");
				field.setAccessible(true);
				@SuppressWarnings("unchecked")
				Map<Integer, List<ICombatResultHandler>> map = (Map<Integer, List<ICombatResultHandler>>) field.get(null);
				field.setAccessible(false);
				ICombatResultHandler handler = (ICombatResultHandler) Class.forName(nowClazzName).newInstance();
				if (type != null) {
					List<ICombatResultHandler> list = map.get(type);
					int count = replaceCombatResultHandler(list, preClazz, handler);
					result.tips = "共替换：" + count;
				} else if (combatType <= -1) {
					int count = 0;
					for (Iterator<List<ICombatResultHandler>> itr = map.values().iterator(); itr.hasNext();) {
						count += replaceCombatResultHandler(itr.next(), preClazz, handler);
					}
					result.tips = "共替换：" + count;
				} else {
					result.tips = "找不到战斗类型：" + combatType;
				}
			} catch (Exception e) {
				result.tips = "异常情况：" + e.getMessage();
			}
			return result;
		}

		if (cmd.equalsIgnoreCase("@garden")) {
			// 庄园植物全部加速N分钟【@garden,N】
			// 庄园指定植物M加速N分钟【@garden,M,N】
			result.isOrder = true;
			int treeId = 0;
			int minute = 0;
			if (args.length == 2) {
				minute = Integer.parseInt(args[1]);
			} else {
				treeId = Integer.parseInt(args[1]);
				minute = Integer.parseInt(args[2]);
			}
			CommonResult temp = KGardenCenter.speedByGM(role, treeId, minute);
			result.tips = temp.tips;
			result.isSuccess = temp.isSucess;
			return result;
		}

		if (cmd.equalsIgnoreCase("@addGangMoney")) {
			// 加军团资金 @addGangMoney,count
			result.isOrder = true;
			long gangId = KSupportFactory.getGangSupport().getGangIdByRoleId(role.getId());
			if (gangId < 1) {
				result.tips = "你还没加入军团";
				result.isSuccess = true;
				return result;
			}
			int count = Integer.parseInt(args[1]);

			result.isSuccess = KSupportFactory.getGangSupport().addGangExp(gangId, 1, count);
			if(result.isSuccess){
				result.tips = "指令执行成功";
			} else {
				result.tips = "指令执行失败";
			}
			return result;
		}
		
		if (cmd.equalsIgnoreCase("@addGangExp")) {
			// 加军团经验 @addGangExp,count
			result.isOrder = true;
			long gangId = KSupportFactory.getGangSupport().getGangIdByRoleId(role.getId());
			if (gangId < 1) {
				result.tips = "你还没加入军团";
				result.isSuccess = true;
				return result;
			}
			int count = Integer.parseInt(args[1]);
			result.isSuccess =  KSupportFactory.getGangSupport().addGangExp(gangId, count, 0);
			if(result.isSuccess){
				result.tips = "指令执行成功";
			} else {
				result.tips = "指令执行失败";
			}
			return result;
		}

		if (cmd.equalsIgnoreCase("@roleId")) {
			// 查玩家ID和角色ID @roleid
			result.isOrder = true;
			result.tips = new StringBuffer().append("playerId=").append(role.getPlayerId()).append(" roleId=").append(role.getId()).toString();
			result.isSuccess = true;
			return result;
		}
		
		else if(cmd.equalsIgnoreCase("@addExp")) {
			result.isOrder = true;
			result.isSuccess = true;
			result.tips = "本次增加经验：" + KSupportFactory.getRoleModuleSupport().addExp(role.getId(), Integer.parseInt(args[1]), KRoleAttrModifyType.GM操作, null);
			return result;
		}

		if (cmd.equalsIgnoreCase("@chatLv")) {
			// 设置聊天等级限制
			// @chatlv,世界频道等级,区域频道等级
			result.isOrder = true;
			short[] results = KChatLogic.notifyChatLvOrder(Short.parseShort(args[1]), Short.parseShort(args[2]));
			result.tips = "执行成功，原世界频道等级=" + results[0] + "，原区域频道等级=" + results[1];
			result.isSuccess = true;
			return result;
		}

		if (cmd.equalsIgnoreCase("@DynReward")) {
			// 执行动态奖励
			result.isOrder = true;
			String tempResult = DynamicRewardCenter.runRewardOnGMOrder(args[1], args[2]);
			if (tempResult != null) {
				result.isSuccess = false;
				result.tips = tempResult;
				return result;
			}
			result.isSuccess = true;
			result.tips = "指令处理完毕，请查看GS日志！";
			return result;
		}

		if (cmd.equalsIgnoreCase("@phypowerRewardReload")) {
			// 执行体力免费领取重新加载
			result.isOrder = true;
			if (!args[1].equals("45636$#$wqroll")) {
				result.tips = "执行【体力免费领取重新加载】失败！原因=密码错误";
				return result;
			}
			String tempResult = KRewardDataManager.reloadPhyPowerRewardDatas();
			if (tempResult != null) {
				result.isSuccess = false;
				result.tips = tempResult;
				return result;
			}
			result.isSuccess = true;
			result.tips = "指令处理完毕";
			return result;
		}
		
		if (cmd.equalsIgnoreCase("@kajiRewardReload")) {
			// 执行卡机补偿重新加载
			result.isOrder = true;
			if (!args[1].equals("45636$#$wqroll")) {
				result.tips = "执行【卡机补偿重新加载】失败！原因=密码错误";
				return result;
			}
			String tempResult = KRewardDataManager.reloadKAJIRewardDatas();
			if (tempResult != null) {
				result.isSuccess = false;
				result.tips = tempResult;
				return result;
			}
			result.isSuccess = true;
			result.tips = "指令处理完毕";
			return result;
		}
		 
		if (cmd.equalsIgnoreCase("@reloadDailyMail")) {
			// 执行每日邮件重新加载
			result.isOrder = true;
			if (!args[1].equals("45636$#$wqroll")) {
				result.tips = "执行【每日邮件重新加载】失败！原因=密码错误";
				return result;
			}
			String tempResult = KRewardDataManager.reloadDialyMailRewardDatas();
			if (tempResult != null) {
				result.isSuccess = false;
				result.tips = tempResult;
				return result;
			}
			result.isSuccess = true;
			result.tips = "指令处理完毕";
			return result;
		}

		if (cmd.equalsIgnoreCase("@ExcitingReload")) {
			// @ExcitingReload,45636$#$wqroll
			result.isOrder = true;
			if (!args[1].equals("45636$#$wqroll")) {
				result.tips = "执行【精彩活动重新加载】失败！原因=密码错误";
				return result;
			}
			boolean isPushExciting = true;
			if (args.length > 2) {
				if (args[2].equalsIgnoreCase("false")) {
					isPushExciting = false;
				}
			}

			String error = KSupportFactory.getExcitingRewardSupport().reloadExcitionData(isPushExciting);
			result.isSuccess = true;
			result.tips = error == null ? "指令处理完毕，请查看GS日志！" : error;
			return result;
		}

		if (cmd.equalsIgnoreCase("@HotGoodsReload")) {
			// @HotGoodsReload,45636$#$wqroll
			result.isOrder = true;
			if (!args[1].equals("45636$#$wqroll")) {
				result.tips = "执行【限时热购重新加载】失败！原因=密码错误";
				return result;
			}
			String error = KHotShopCenter.reloadData();
			result.isSuccess = true;
			result.tips = error == null ? "指令处理完毕，请查看GS日志！" : error;
			return result;
		}

		if (cmd.equalsIgnoreCase("@reloadNewGifTemplates")) {
			// @reloadNewGifTemplates,45636$#$wqroll
			result.isOrder = true;
			if (!args[1].equals("45636$#$wqroll")) {
				result.tips = "执行【新礼包道具模板重新加载】失败！原因=密码错误";
				return result;
			}
			String error = KItemDataLoader.reloadNewItemTemplates();
			result.isSuccess = true;
			result.tips = error == null ? "指令处理完毕，请查看GS日志！" : error;
			return result;
		}

		if (cmd.equalsIgnoreCase("@reloadActiviCode")) {
			// @reloadActiviCode,45636$#$wqroll
			result.isOrder = true;
			if (!args[1].equals("45636$#$wqroll")) {
				result.tips = "执行【激活码类型表】失败！原因=密码错误";
				return result;
			}
			String error = KActivateCodeDataManager.reloadData();
			result.isSuccess = true;
			result.tips = error == null ? "指令处理完毕，请查看GS日志！" : error;
			return result;
		}

		if (cmd.equalsIgnoreCase("@addItem")) {
			// 加道具 @additem,itemCode,count
			result.isOrder = true;
			ItemResult_AddItem addResult = KSupportFactory.getItemModuleSupport().addItemToBag(role, args[1], Integer.parseInt(args[2]), "指令");
			result.isSuccess = addResult.isSucess;
			result.tips = addResult.tips;
			return result;
		}

		if (cmd.equalsIgnoreCase("@clearBag")) {
			// 清空背包 @clearbag
			result.isOrder = true;
			KItemLogic.clearBagForTestOrder(role.getId());
			result.isSuccess = true;
			return result;
		}
		
		if(cmd.equalsIgnoreCase("@startItemPatch")) {
			return processCheat("@010622,com.kola.kmp.logic.item.NewRoleGiftPatch,start;340100#340101#340102#340103#340104#340105#340106#340107#340108;340100", role);
		}

		if (cmd.equalsIgnoreCase("@vip")) {
			// 设置VIP等级@vip,1
			result.isOrder = true;
			int lv = Integer.parseInt(args[1]);
			if (lv < 0 || lv > KVIPDataManager.mVIPLevelDataManager.getMaxLevel().lvl) {
				result.tips = "指令参数错误";
				return result;
			}
			KVIPLogic.presentVIPLv(role, lv);
			result.isSuccess = true;
			return result;
		}

		if (cmd.equalsIgnoreCase("@vitality")) {
			// 完成活跃度【@vitality,类型,次数】
			result.isOrder = true;
			if (args.length == 1) {
				for (KVitalityTypeEnum type : KVitalityTypeEnum.values()) {
					KSupportFactory.getRewardModuleSupport().recordFuns(role, type, 100);
				}
				result.isSuccess = true;
				return result;
			}

			KVitalityTypeEnum type = KVitalityTypeEnum.getEnum(Integer.parseInt(args[1]));
			if (type == null) {
				result.tips = "活跃度类型不存在";
				return result;
			}
			int addTime = Integer.parseInt(args[2]);
			if (addTime < 1) {
				result.tips = "次数错误";
				return result;
			}
			KSupportFactory.getRewardModuleSupport().recordFuns(role, type, addTime);
			result.isSuccess = true;
			return result;
		}

		if (cmd.equalsIgnoreCase("@addMoney")) {
			// 加货币 @addmoney,moneyType,count
			result.isOrder = true;
			KCurrencyTypeEnum type = KCurrencyTypeEnum.getEnum(Byte.parseByte(args[1]));
			if (type == null) {
				result.tips = "指令参数错误";
				return result;
			}
			long count = Long.parseLong(args[2]);
			if (count == 0) {
				result.tips = "指令参数错误";
				return result;
			}
			long addResult = 0;
			if (count > 0) {
				addResult = KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), type, count, PresentPointTypeEnum.GM或指令操作, true);
			} else if (count < 0) {
				addResult = KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), type, -count, UsePointFunctionTypeEnum.GM指令, true);
			}
			if (addResult < 0) {
				result.tips = "执行失败";
				return result;
			}

			result.isSuccess = true;
			return result;
		}

		if (cmd.equalsIgnoreCase("@charge")) {
			return goCharge(role, args, result, false);
		}

		if (cmd.equalsIgnoreCase("@hscharge")) {
			return goCharge(role, args, result, true);
		}

		if (cmd.equalsIgnoreCase("@addSkill")) {
			result.isOrder = true;
			boolean isInit = Boolean.parseBoolean(args[1]);
			int skillId = Integer.parseInt(args[2]);
			if (KSupportFactory.getSkillModuleSupport().addSkillToRole(role.getId(), isInit, skillId, "指令")) {
				result.tips = "添加技能成功";
			} else {
				result.tips = "添加技能失败";
			}
			result.isSuccess = true;
			return result;

		}

		if (cmd.equalsIgnoreCase("@mount")) {
			result.isOrder = true;
			int mountTempId = Integer.parseInt(args[1]);
			KMountTemplate temp = KMountDataManager.mMountTemplateManager.getTemplate(mountTempId);
			KActionResult<KMount> tempResult = KMountLogic.presentMount(role, temp, "GM指令");
			result.tips = tempResult.tips;
			result.isSuccess = true;
			return result;
		}
		
		if (cmd.equalsIgnoreCase("@fashion")) {
			result.isOrder = true;
			int tempId = Integer.parseInt(args[1]);
			CommonResult tempResult = KSupportFactory.getFashionModuleSupport().addFashions(role, Arrays.asList(tempId), "GM指令");
			result.tips = tempResult.tips;
			result.isSuccess = tempResult.isSucess;
			return result;
		}
		
		if (cmd.equalsIgnoreCase("@et")) {
			// 全套 @et
			result.isOrder = true;
			KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), KCurrencyTypeEnum.GOLD, 1000000000, PresentPointTypeEnum.GM或指令操作, false);
			KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), KCurrencyTypeEnum.DIAMOND, 10000000, PresentPointTypeEnum.GM或指令操作, false);
			KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), KCurrencyTypeEnum.POTENTIAL, 10000000, PresentPointTypeEnum.GM或指令操作, false);
			KSupportFactory.getCurrencySupport().synCurrencyDataToClient(role.getId());

			for (int i = 0; i < 10; i++) {
				KItemLogic.dealMsg_extendBagVolume(role, 1);
			}
			int roleLv = role.getLevel();
			for (KItemTempAbs itemT : KItemDataManager.mItemTemplateManager.getItemTemplateList()) {
				if (itemT.ItemType == KItemTypeEnum.装备) {
					KItemTempEqui itemT2 = (KItemTempEqui) itemT;
					if (itemT2.jobEnum == null || itemT2.job == role.getJob()) {
						if (roleLv >= itemT2.lvl && (roleLv - itemT2.lvl) < 10 && itemT2.ItemQuality == KItemQualityEnum.精良的) {
							KSupportFactory.getItemModuleSupport().addItemToBag(role, itemT.itemCode, 1, "指令");
						}
					}
				}
			}
			KSupportFactory.getItemModuleSupport().addItemToBag(role, "330000", 10, "指令");
			KSupportFactory.getItemModuleSupport().addItemToBag(role, "330001", 10, "指令");
			KSupportFactory.getItemModuleSupport().addItemToBag(role, "330002", 10, "指令");
			KSupportFactory.getItemModuleSupport().addItemToBag(role, "330003", 10, "指令");
			KSupportFactory.getItemModuleSupport().addItemToBag(role, "330004", 10, "指令");
			KSupportFactory.getItemModuleSupport().addItemToBag(role, "350000", 10, "指令");
			KSupportFactory.getItemModuleSupport().addItemToBag(role, "350100", 10, "指令");

			// 发送消息给客户端
			KPushItemsMsg.pushAllItems(role);

			// KSupportFactory.getSkillModuleSupport().addSkillToRole(role.getId(),
			// true, 410100);
			// KSupportFactory.getSkillModuleSupport().addSkillToRole(role.getId(),
			// true, 410101);
			// KSupportFactory.getSkillModuleSupport().addSkillToRole(role.getId(),
			// true, 410102);
			// KSupportFactory.getSkillModuleSupport().addSkillToRole(role.getId(),
			// true, 410103);
			// KSupportFactory.getSkillModuleSupport().addSkillToRole(role.getId(),
			// true, 410104);
			// KSupportFactory.getSkillModuleSupport().addSkillToRole(role.getId(),
			// true, 410105);

			result.isSuccess = true;
			return result;
		}

		if (cmd.equalsIgnoreCase("@lv")) {
			result.isOrder = true;
			int toLv = Integer.parseInt(args[1]);
			if (toLv < role.getLevel()) {
				result.tips = "不能降级！";
			} else {
				if (toLv > KRoleModuleConfig.getRoleMaxLv()) {
					toLv = KRoleModuleConfig.getRoleMaxLv();
				}
				try {
					Method m = role.getClass().getDeclaredMethod("onLevelUp", int.class);
					m.setAccessible(true);
					m.invoke(role, toLv);
					m.setAccessible(false);
					result.tips = "您的等级已经提升至：" + toLv;
				} catch (Exception e) {
					e.printStackTrace();
					result.tips = "出异常啦！！" + e.getMessage();
				}
			}
			return result;
		}

		if (cmd.equalsIgnoreCase("@addPet")) {
			result.isOrder = true;
			if (args.length > 2) {
				List<Integer> templateIds = new ArrayList<Integer>();
				for (int i = 1; i < args.length; i++) {
					templateIds.add(Integer.parseInt(args[i]));
				}
				CommonResult addResult = KSupportFactory.getPetModuleSupport().createPetsToRole(role.getId(), templateIds, KPetFlowType.测试.name());
				result.isSuccess = addResult.isSucess;
				result.tips = addResult.tips;
			} else {
				int templateId = Integer.parseInt(args[1]);
				KActionResult<Pet> addResult = KSupportFactory.getPetModuleSupport().createPetToRole(role.getId(), templateId, KPetFlowType.测试.name());
				result.isSuccess = addResult.success;
				result.tips = addResult.tips;
			}
			return result;
		}
		
		if (cmd.equalsIgnoreCase("@deletepet")) {
			long roleId = Long.parseLong(args[1]);
			List<Long> list = new ArrayList<Long>();
			for (int i = 2; i < args.length; i++) {
				list.add(Long.parseLong(args[i]));
			}
			boolean success = KSupportFactory.getPetModuleSupport().deletePets(roleId, list, "GM删除");
			result.tips = "删除以下随从：" + list.toString() + "，是否成功：" + success;
			result.isOrder = true;
			result.isSuccess = true;
			return result;
		}

		if (cmd.equalsIgnoreCase("@teampvprank")) {
			result.isOrder = true;
			//
			{
				long teamId = 199000000019L;
				// KSupportFactory.getTeamPVPRankSupport().notifyTempChange(teamId,
				// "DDD", KTeamPVPRankTypeEnum.最强王者, 1, 3001, 3333,
				// 199000000003L, "弗恩霍金", 1, 0, null, 0);

				// KSupportFactory.getTeamPVPRankSupport().notifyTempChange(teamId,
				// "DDD", KTeamPVPRankTypeEnum.最强王者, 1, 3001, 3333,
				// 199000000003L, "弗恩霍金", 1, 199000000002L, "弗恩霍金2", 3);

				// teamId = 199000000034L;
				// TeamPVPRankElement element =
				// KSupportFactory.getTeamPVPRankSupport().getRankElement(
				// KTeamPVPRankTypeEnum.钻石, teamId);
				// KSupportFactory.getTeamPVPRankSupport().notifyTempChange(teamId,
				// element.elementName, KTeamPVPRankTypeEnum.最强王者,
				// element.getElementLv(), element.getExp(),
				// element.getBattlePow(), element.getLeaderRoleId(),
				// element.getLeaderRoleName(), element.getLeaderRoleVip(),
				// element.getMemRoleId(), element.getMemRoleName(),
				// element.getMemRoleVip());

				// KCompetitionTeam team =
				// KCompetitionTeamPVPManager.getTeamByTeamId(teamId);
				// TeamPVPRankElement element =
				// KSupportFactory.getTeamPVPRankSupport().getRankElement(
				// KTeamPVPRankTypeEnum.青铜, teamId);
			}
			//
			{
				// 批量插入天梯榜
				long teamId = 199000000020L;
				// for (int i = 1; i < 20; i++) {
				// teamId++;
				// KSupportFactory.getTeamPVPRankSupport().notifyTempChange(teamId,
				// "DDD" + i, KTeamPVPRankTypeEnum.getEnum(UtilTool.random(21,
				// 26)), UtilTool.random(1, 10), UtilTool.random(1, 90),
				// UtilTool.random(1, 100) * 30, 199000000003L + i, "弗恩霍金" + i,
				// 1, 0, null, 0);
				// }
			}
			{
				// 测试删除部分天梯榜元素，DB保存是否正常
				long teamId = 199000000020L;
				// for (int i = 1; i < 10; i++) {
				// teamId++;
				// KSupportFactory.getTeamPVPRankSupport().notifyTeampDelete(teamId);
				// }
			}
			//
			result.isSuccess = true;
			result.tips = "执行成功";
			return result;
		}

		if (cmd.equalsIgnoreCase("@testmail")) {
			result.isOrder = true;
			KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), "330000", 10, "保存排行榜成功", "保存排行榜成功！排行榜类型=全部军团，路径=./res/output/gangRank/allGang2014-04-22_11-00-19.xml");
			{
				BaseMailContent baseMail = new BaseMailContent("保存排行榜成功！", "排行榜类型=全部军团，路径=./res/output/gangRank/allGang2014-04-22_11-00-19.xml", new int[] { 150001, 150002, 150003 }, new String[] {
						"[url=http://www.baidu.com]百度一下[/url]", "[url=http://www.163.com]网易有态度[/url]" });
				List<AttValueStruct> attList = Arrays.asList(new AttValueStruct(KGameAttrType.HP, 50), new AttValueStruct(KGameAttrType.PHY_POWER, 100));
				List<KCurrencyCountStruct> moneyList = Arrays.asList(new KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND, 1), new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, 100));
				List<ItemCountStruct> itemStructs = Arrays.asList(new ItemCountStruct("330000", 10), new ItemCountStruct("330001", 10));
				List<Integer> fashionTempIdList = Arrays.asList(15000, 15003);
				List<Integer> petTempIdList = Arrays.asList(700001, 700007);

				BaseRewardData baseRewardData = new BaseRewardData(attList, moneyList, itemStructs, fashionTempIdList, petTempIdList);
				BaseMailRewardData mailReward = new BaseMailRewardData(1, baseMail, baseRewardData);
				KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), mailReward, PresentPointTypeEnum.GM或指令操作);
				KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), itemStructs, baseMail.getMailTitle(), baseMail.getMailContent());
			}
			result.isSuccess = true;
			result.tips = "执行成功";
			return result;
		}

		// 发送在线玩家邮件【@onlineMail,最小等级,最大等级,邮件标题,邮件内容】
		if (cmd.equalsIgnoreCase("@onlineMail")) {
			result.isOrder = true;
			if (args.length != 5) {
				result.tips = "参数错误";
				return result;
			}
			int minLv = Integer.parseInt(args[1]);
			int maxLv = Integer.parseInt(args[2]);
			String mailTitle = args[3];
			String mailContent = args[4];

			Map<Long, KMail> sendResult = KSupportFactory.getMailModuleSupport().sendSimpleMailToOnlineBySystem(minLv, maxLv, mailTitle, mailContent);

			result.isSuccess = true;
			result.tips = "执行成功,发送角色数量=" + sendResult.size();
			return result;
		}

		if (cmd.equalsIgnoreCase("@clearmail")) {
			result.isOrder = true;
			MailResult_Sync deleteResult = KMailLogic.deleteAllMailsByGM(role);
			deleteResult.doFinally(role);
			result.isSuccess = deleteResult.isSucess;
			result.tips = deleteResult.tips;
			return result;
		}

		if (cmd.equalsIgnoreCase("@unlock")) {
			result.isOrder = true;
			role.setNotFighting();
			return result;
		}

		if (cmd.equalsIgnoreCase("@010622")) {
			result.isOrder = true;
			String path = args[1];
			try {
				result.tips = ((RunTimeTask) Class.forName(path).newInstance()).run(args[2]);
				result.isSuccess = true;
			} catch (Exception e) {
				result.tips = e.getMessage();
				e.printStackTrace();
			}
			return result;
		}

		if (cmd.equalsIgnoreCase("@friendCopy")) {
			result.isOrder = true;
			try {
				String fdName = args[1];
				KRole friend = KSupportFactory.getRoleModuleSupport().getRole(fdName);
				if (friend != null) {
					Method m = KCombatManager.class.getDeclaredMethod("startTowerCombat", KRole.class, long.class, KTowerBattlefield.class);
					Field friendCopyLevelMapField = KGameLevelModuleExtension.getManager().getFriendCopyManager().getClass().getDeclaredField("friendCopyLevelMap");
					friendCopyLevelMapField.setAccessible(true);
					@SuppressWarnings("unchecked")
					Map<Integer, KTowerLevelTemplate> friendCopyLevelMap = (Map<Integer, KTowerLevelTemplate>) friendCopyLevelMapField.get(KGameLevelModuleExtension.getManager()
							.getFriendCopyManager());
					m.setAccessible(true);
					m.invoke(null, role, friend.getId(), friendCopyLevelMap.get(10001).getTowerBattlefield());
					friendCopyLevelMapField.setAccessible(false);
					m.setAccessible(false);
					result.isSuccess = true;
				} else {
					result.tips = "朋友不存在！";
					result.isSuccess = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				result.tips = e.getMessage();
			}
			return result;
		}

		// * 复制指定角色【@cloneRole,srcName】
		if (cmd.equalsIgnoreCase("@cloneRole")) {
			result.isOrder = true;
			try {

				if (args.length != 2) {
					result.tips = "指令格式错误！";
					return result;
				}

				KRole srcRole = KSupportFactory.getRoleModuleSupport().getRole(args[1]);

				if (srcRole == null) {
					result.tips = "源角色不存在！";
					return result;
				}

				result.tips = cloneRole(role, srcRole);
				if (result.tips == null) {
					result.tips = "执行成功";
					result.isSuccess = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
				result.tips = e.getMessage();
			}
			return result;
		}

		if (cmd.equalsIgnoreCase("@dupmap")) {
			result.isOrder = true;
			try {
				int order = Integer.parseInt(args[1]);
				int duplicateMapId = Integer.parseInt(args[2]);
				if (order == 1) {
					KSupportFactory.getDuplicateMapSupport().playerRoleJoinDuplicateMap(role, duplicateMapId);
				} else if (order == 2) {
					KSupportFactory.getDuplicateMapSupport().playerRoleLeaveDuplicateMap(role, duplicateMapId);
				}
				result.isSuccess = true;
				result.tips = "执行成功";
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}

		if (cmd.equalsIgnoreCase("@openlevel")) {
			result.isOrder = true;
			try {
				int order = Integer.parseInt(args[1]);
				boolean isOpen = (order == 1);
				if (isOpen) {
					role.getRoleGameSettingData().setDebugOpenLevel(isOpen);
				} else {
					KLevelTemplate level = KGameLevelModuleExtension.getManager().allKGameLevel.get(order);
					// 关卡开启条件
					KEnterLevelCondition condition = level.getEnterCondition();

					// 获取关卡记录
					KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());

					PlayerRoleGamelevelData levelData = levelSet.getPlayerRoleNormalGamelevelData(level.getLevelId());

					// 处理个人关卡记录，计算关卡评价

					boolean isLevelDataChange = false;
					boolean isCompletedAndTriggerOpenHinderLevel = false;
					// 是否检测星级评定并发送更新信息

					byte fightLv = 5;
					if (levelData == null) {
						// 找不到关卡记录，则添加记录并将状态改为完成状态，设入关卡评价
						levelSet.addOrModifyNormalGameLevelData(level.getLevelId(), condition.isLimitJoinCountLevel() ? condition.getLevelLimitJoinCount() : 0, fightLv, true);

						isLevelDataChange = true;
						if (level.getHinderGameLevelList().size() > 0) {
							isCompletedAndTriggerOpenHinderLevel = true;
						}

						// // 记录第一次进入关卡行为数据
						FlowDataModuleFactory.getModule().recordFirstCompleteGameLevel(level.getLevelId(), role.getLevel());
					} else {

						if (!levelData.isCompleted()) {
							if (level.getHinderGameLevelList().size() > 0) {
								isCompletedAndTriggerOpenHinderLevel = true;
							}

							// // 记录第一次进入关卡行为数据
							FlowDataModuleFactory.getModule().recordFirstCompleteGameLevel(level.getLevelId(), role.getLevel());
						}

						levelSet.addOrModifyNormalGameLevelData(level.getLevelId(), levelData.getRemainJoinLevelCount(),
								((levelData.getLevelEvaluate() < fightLv) ? fightLv : levelData.getLevelEvaluate()), true);
						isLevelDataChange = true;
					}

					// 发消息通知用户该关卡记录有发生改变
					if (isLevelDataChange) {
						KGameLevelModuleExtension.getManager().sendUpdateGameLevelInfoMsg(role, level, levelData);
					}

					// 处理后置关卡开放状态
					if (isCompletedAndTriggerOpenHinderLevel) {
						for (KLevelTemplate hinderLevel : level.getHinderGameLevelList()) {
							if (role.getLevel() >= hinderLevel.getEnterCondition().getOpenRoleLevel()) {
								KGameLevelModuleExtension.getManager().sendUpdateGameLevelOpenedOrGrayState(role, hinderLevel.getScenarioId(), hinderLevel.getLevelId(),
										KLevelTemplate.GAME_LEVEL_STATE_OPEN, hinderLevel.getEnterCondition().getLevelLimitJoinCount());
							}
						}
					}

					// 通知活跃度模块
					KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.通关普通副本);
				}

				result.isSuccess = true;
				result.tips = "执行成功";
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}if (cmd.equalsIgnoreCase("@openAlllevel")) {
			result.isOrder = true;
			// 获取关卡记录
			KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
			for (KLevelTemplate level : KGameLevelModuleExtension.getManager().allKGameLevel.values()) {
				levelSet.addOrModifyNormalGameLevelData(level.getLevelId(), 0, (byte)5, true);
			}
			result.isSuccess = true;
			result.tips = "执行成功";
			return result;
		}

		if (cmd.equalsIgnoreCase("@worldboss")) {
			result = worldBossAction(role, args);
			return result;
		}
		
		if (cmd.equalsIgnoreCase("@loadPetTemplate")) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				Field field = KPetModuleManager.class.getDeclaredField("_allTemplates");
				Field handBookField = KPetModuleManager.class.getDeclaredField("_petHandbookMap");
				Method m = KPetTemplate.class.getDeclaredMethod("onGameWorldInitComplete");
				m.setAccessible(true);
				field.setAccessible(true);
				handBookField.setAccessible(true);
				Map<Integer, KPetTemplate> allTemplates = (Map<Integer, KPetTemplate>) field.get(null);
				Map<KPetQuality, List<KPetTemplateHandbookModel>> petHandbookMap = new LinkedHashMap<KPetQuality, List<KPetTemplateHandbookModel>>((Map<KPetQuality, List<KPetTemplateHandbookModel>>)handBookField.get(null));
				for (Iterator<Map.Entry<KPetQuality, List<KPetTemplateHandbookModel>>> itr = petHandbookMap.entrySet().iterator(); itr.hasNext();) {
					Map.Entry<KPetQuality, List<KPetTemplateHandbookModel>> tempEntry = itr.next();
					tempEntry.setValue(new ArrayList<KPetTemplateHandbookModel>(tempEntry.getValue()));
				}
				StringBuilder strBld = new StringBuilder("新增随从：\n");
				field.setAccessible(false);
				KGameExcelFile file = new KGameExcelFile("./res/gamedata/petModule/petData.xls");
				KGameExcelRow[] allRows = file.getTable("随从数据表", 5).getAllDataRows();
				KPetTemplate template;
				List<KPetTemplateHandbookModel> tempList;
				KGameExcelRow row;
				for (int i = 0; i < allRows.length; i++) {
					row = allRows[i];
					if (allTemplates.containsKey(row.getInt("templateId"))) {
						continue;
					}
					template = new KPetTemplate(allRows[i]);
					m.invoke(template);
					strBld.append(template.templateId).append(":").append(template.defaultName).append("\n");
					allTemplates.put(template.templateId, template);
					tempList = petHandbookMap.get(template.quality);
					if (tempList == null) {
						tempList = new ArrayList<KPetTemplateHandbookModel>();
						petHandbookMap.put(template.quality, tempList);
					}
					tempList.add(new KPetTemplateHandbookModel(template));
				}
				for (Iterator<Map.Entry<KPetQuality, List<KPetTemplateHandbookModel>>> itr = petHandbookMap.entrySet().iterator(); itr.hasNext();) {
					Map.Entry<KPetQuality, List<KPetTemplateHandbookModel>> tempEntry = itr.next();
					Collections.sort(tempEntry.getValue());
					tempEntry.setValue(Collections.unmodifiableList(tempEntry.getValue()));
				}
				handBookField.set(null, Collections.unmodifiableMap(petHandbookMap));
				handBookField.setAccessible(false);
				m.setAccessible(false);
				result.tips = strBld.toString();
			} catch (Exception e) {
				result.tips = e.getMessage();
			}
			return result;
		}

		if (cmd.equalsIgnoreCase("@reloadGangResWarConfig")) {
			result.isOrder = true;
			// 重新加载军团资源战配置【@reloadGangResWarConfig】
			try {
				KResWarConfig.init(null);
				result.tips = "指令执行完成";
			} catch (Exception e) {
				result.tips = e.getMessage();
			}
			return result;
		}

		if (cmd.equalsIgnoreCase("@restartGangResWar")) {
			result.isOrder = true;
			// 重启军团资源战【@restartGangResWar,isReloadXmlConfig,isForce】
			result = ResWarLogic.stopAndRestartWar(Boolean.parseBoolean(args[1]), Boolean.parseBoolean(args[2]));
			if (result.isSuccess) {
				result.tips = "指令执行完成";
			}
			return result;
		}

		if (cmd.equalsIgnoreCase("@reloadGangWarConfig")) {
			result.isOrder = true;
			// 重新加载军团战配置【@reloadGangWarConfig】
			try {
				KGangWarConfig.init(null);
				result.tips = "指令执行完成";
			} catch (Exception e) {
				result.tips = e.getMessage();
			}
			return result;
		}

		if (cmd.equalsIgnoreCase("@restartGangWar")) {
			result.isOrder = true;
			// 重启军团战【@restartGangWar,isReloadXmlConfig,isForce】
			result = GangWarLogic.stopAndRestartGangWarByGM(Boolean.parseBoolean(args[1]), Boolean.parseBoolean(args[2]));
			result.isOrder = true;
			if (result.isSuccess) {
				result.tips = "指令执行完成";
			}
			return result;
		}

		if (cmd.equalsIgnoreCase("@startGangWar")) {
			result.isOrder = true;
			// * 【军团战】重启【@startGangWar,1,201411171900】指定具体报名结束时间
			// * 【军团战】重启【@startGangWar,2,20】指定N分钟后报名结束

			int type = Integer.parseInt(args[1]);
			if (type != 1 && type != 2) {
				result.tips = "指令格式错误";
				return result;
			}

			long nowTime = System.currentTimeMillis();

			long signupEndTime = nowTime;
			try {
				if (type == 1) {
					signupEndTime = UtilTool.DATE_FORMAT3.parse(args[2]).getTime();
				} else {
					signupEndTime = nowTime + Integer.parseInt(args[2]) * Timer.ONE_MINUTE;
				}
			} catch (Exception e) {
				result.tips = "指令格式错误";
				return result;
			}

			result = GangWarLogic.startGangWarByGM(signupEndTime);
			result.isOrder = true;

			if (result.isSuccess) {
				result.tips = "指令执行完成";
			}
			return result;
		}

		if (cmd.equalsIgnoreCase("@openfunc")) {
			result.isOrder = true;
			result.isSuccess = true;
			result.tips = processOpenFunc(role, args);
			return result;
		}

		if (cmd.equalsIgnoreCase("@reflashcompetition")) {
			result.isOrder = true;
			result.isSuccess = true;
			result.tips = "提交成功！";
			KCompetitionModule.getCompetitionManager().reflashCompetitionData(role, false, true);
			return result;
		}

		if (cmd.equalsIgnoreCase("@press")) {
			result.isOrder = true;
			result.isSuccess = true;
			result.tips = "提交成功！";
			KCreateRoleTask.testCreateRole(Long.parseLong(args[1]));
			return result;
		}

		if (cmd.equalsIgnoreCase("@thread")) {
			result.isOrder = true;
			result.isSuccess = true;
			Thread t;
			int count = 0;
			for (Iterator<Thread> itr = Thread.getAllStackTraces().keySet().iterator(); itr.hasNext();) {
				t = itr.next();
				if (t.getName().contains("pool-8")) {
					count++;
				}
			}
			result.tips = "当前线程数量：" + Thread.getAllStackTraces().size() + ", pool-8的线程数量：" + count;
			return result;
		}
		if (cmd.equalsIgnoreCase("@unExceptedClose")) {
			role.sendMsg(null);
			result.isOrder = true;
			result.isSuccess = true;
			result.tips = "执行成功！";
			return result;
		}

		if (cmd.equalsIgnoreCase("@dphy")) {
			int value = Integer.parseInt(args[1]);
			KSupportFactory.getRoleModuleSupport().decreasePhyPower(role, value, "测试");
			result.isOrder = true;
			result.isSuccess = true;
			result.tips = "执行成功！";
			return result;
		}

		else if (cmd.equalsIgnoreCase("@resetCmp")) {
			long beginRoleId = Long.parseLong(args[1]);
			long endRoleId = Long.parseLong(args[2]);
			StringBuilder strBld = new StringBuilder();
			for (long id = beginRoleId; id <= endRoleId; id++) {
				strBld.append(KCompetitionModule.getCompetitionManager().joinCompetitionForGM(id)).append("\n");
			}
			result.isOrder = true;
			result.isSuccess = true;
			result.tips = strBld.toString();
			return result;
		}

		else if (cmd.equalsIgnoreCase("fightMonster")) {
			result.isOrder = true;
			result.isSuccess = true;
			// KSupportFactory.getCombatModuleSupport().fightByUpdateInfo(role,
			// battlefield, null, null, KCombatType., attachment)
		}

		else if (cmd.equalsIgnoreCase("@clearFdCopy")) {
			result.isOrder = true;
			result.isSuccess = true;
			KGameLevelSet levelSet = KGameLevelModuleExtension.getGameLevelSet(role.getId());
			KGameLevelRecord fRecord = levelSet.getCopyRecord(KGameLevelTypeEnum.好友副本关卡);
			fRecord.friendCopyData.friendCoolingTimeMap.clear();
			fRecord.friendCopyData.strangerCoolingTimeMap.clear();
			fRecord.friendCopyData.challengeCount = 0;
			fRecord.friendCopyData.remainChallengeCount = KFriendCopyManager.free_challenge_count;
			fRecord.friendCopyData.todayBuyCount = 0;
			result.tips = "执行成功";
			return result;
		}

		else if (cmd.equalsIgnoreCase("@clearBarrel")) {
			NewGoldActivityRoleRecordData record = KActivityRoleExtCaCreator.getNewGoldActivityRoleRecordData(role.getId());
			record.checkAndRestData(true);
			record.restChallengeCount = KGoldActivityManager.max_can_challenge_count;
			result.isOrder = true;
			result.isSuccess = true;
			result.tips = "执行成功";
			return result;
		}

		else if (cmd.equalsIgnoreCase("@clearBarrelCD")) {
			KActivityRoleExtData actData = KActivityRoleExtCaCreator.getActivityRoleExtData(role.getId());
			result.isOrder = true;
			result.isSuccess = true;
			try {
				Field field = actData.getClass().getDeclaredField("activityCdTimeMap");
				field.setAccessible(true);
				@SuppressWarnings("unchecked")
				Map<Integer, Long> map = (Map<Integer, Long>) field.get(actData);
				field.setAccessible(false);
				map.put(KNewGoldActivity.getInstance().getActivityId(), 0l);
				KNewGoldActivity.getInstance().sendUpdateActivity(role);
				result.tips = "执行成功";
			} catch (Exception e) {
				result.tips = e.getMessage();
			}
			return result;
		}

		else if (cmd.equalsIgnoreCase("@clearPetCopy")) {
			KGameLevelModuleExtension.getManager().getPetCopyManager().checkAndResetPetCopyDatas(role, false);
			result.isOrder = true;
			result.isSuccess = true;
			result.tips = "执行成功";
			return result;
		}
		
		else if (cmd.equalsIgnoreCase("@clearTwCopy")) {
			result.isOrder = true;
			result.isSuccess = true;
			KGameLevelSet levelSet = KGameLevelModuleExtension.getGameLevelSet(role.getId());
			KGameLevelRecord fRecord = levelSet.getCopyRecord(KGameLevelTypeEnum.爬塔副本关卡);
			fRecord.towerCopyData.challengeCount = 0;
			fRecord.towerCopyData.todayGetReward = false;
			fRecord.notifyDB();
			result.tips = "执行成功";
			return result;
		}
		else if (cmd.equalsIgnoreCase("@resetTwCopy")) {
			result.isOrder = true;
			result.isSuccess = true;
			KGameLevelSet levelSet = KGameLevelModuleExtension.getGameLevelSet(role.getId());
			KGameLevelRecord fRecord = levelSet.getCopyRecord(KGameLevelTypeEnum.爬塔副本关卡);
			fRecord.towerCopyData.nowLevelId = 0;
			KGameLevelModuleExtension.getManager().getTowerCopyManager().sendCopyData(role);
			fRecord.notifyDB();
			result.tips = "执行成功";
			return result;
		}else if (cmd.equalsIgnoreCase("@setTwCopy")) {
			result.isOrder = true;
			result.isSuccess = true;
			int levelId = Integer.parseInt(args[1]);
			KLevelTemplate levelTemplate = KTowerCopyManager.towerCopyLevelMap.get(levelId);
			if (levelTemplate == null) {
//				_LOGGER.error("### Exctpeion----战斗结束爬塔副本管理模块找不到关卡模版数据，" + "关卡ID:" + levelId + "，战场类型：" + result.getBattlefieldType().battlefieldType);
				result.isOrder = true;
				result.isSuccess = false;
				result.tips = "找不到关卡模版数据。关卡ID:"+levelId;
				return result;
			}
			KGameLevelSet levelSet = KGameLevelModuleExtension.getGameLevelSet(role.getId());
			KGameLevelRecord fRecord = levelSet.getCopyRecord(KGameLevelTypeEnum.爬塔副本关卡);			
			fRecord.towerCopyData.nowLevelId = levelId;
			KGameLevelModuleExtension.getManager().getTowerCopyManager().sendCopyData(role);
			fRecord.notifyDB();
			result.tips = "执行成功";
			return result;
		}
		else if (cmd.equalsIgnoreCase("@setPetCopyLevel")) {
			result.isOrder = true;
			result.isSuccess = true;
			int levelId = Integer.parseInt(args[1]);
			KLevelTemplate levelTemplate = KPetChallengeCopyManager.petChallengeCopyLevelMap.get(levelId);
			if (levelTemplate == null) {
//				_LOGGER.error("### Exctpeion----战斗结束爬塔副本管理模块找不到关卡模版数据，" + "关卡ID:" + levelId + "，战场类型：" + result.getBattlefieldType().battlefieldType);
				result.isOrder = true;
				result.isSuccess = false;
				result.tips = "找不到关卡模版数据。关卡ID:"+levelId;
				return result;
			}
			KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
			levelSet.recordCompletePetChallengeCopy(levelId, -1, -1);
			KGameLevelModuleExtension.getManager().getPetChallengeCopyManager().completeOrUpdateCopyInfo(role, levelId);
			result.tips = "执行成功";
			return result;
		}
		else if (cmd.equalsIgnoreCase("@saodangptc")) {
			KGameLevelModuleExtension.getManager().getPetChallengeCopyManager().processSaodangCopy(role);
			result.isOrder = true;
			result.isSuccess = true;
			result.tips = "执行成功";
			return result;
		}
		else if (cmd.equalsIgnoreCase("@petcopytest")) {
			KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
			KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.随从挑战副本关卡);
			int levelId = record.petChallengeCopyData.nowLevelId;
			KGameLevelModuleExtension.getManager().getPetChallengeCopyManager().processPlayerRoleJoinLevel(role, levelId);
		}
		else if (cmd.equalsIgnoreCase("@setSPetCopyLevel")) {
			result.isOrder = true;
			result.isSuccess = true;
			int levelId = Integer.parseInt(args[1]);
			KLevelTemplate levelTemplate = KSeniorPetChallengeCopyManager.seniorPetChallengeCopyLevelMap.get(levelId);
			if (levelTemplate == null) {
//				_LOGGER.error("### Exctpeion----战斗结束爬塔副本管理模块找不到关卡模版数据，" + "关卡ID:" + levelId + "，战场类型：" + result.getBattlefieldType().battlefieldType);
				result.isOrder = true;
				result.isSuccess = false;
				result.tips = "找不到关卡模版数据。关卡ID:"+levelId;
				return result;
			}
			KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
			KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.高级随从挑战副本关卡);
			
			record.seniorPetChallengeCopyData.recordCompleteSeniorPetChallengeCopy(levelId, -1, -1);
			KGameLevelModuleExtension.getManager().getKSeniorPetChallengeCopyManager().completeOrUpdateCopyInfo(role, levelId);
			result.tips = "执行成功";
			return result;
		}
		else if (cmd.equalsIgnoreCase("@completeSPetCopy")) {
			result.isOrder = true;
			result.isSuccess = true;
			KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
			KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.高级随从挑战副本关卡);
			record.seniorPetChallengeCopyData.recordCompleteSeniorPetChallengeCopyLastLevel(KSeniorPetChallengeCopyManager.endLevel.getLevelId());
			KGameLevelModuleExtension.getManager().getKSeniorPetChallengeCopyManager().completeOrUpdateCopyInfo(role, KSeniorPetChallengeCopyManager.endLevel.getLevelId());
			result.tips = "执行成功";
			return result;
		}

		else if (cmd.equalsIgnoreCase("@clearTransport")) {
			result.isOrder = true;
			result.isSuccess = true;
			KActivityRoleExtData activityData = (KActivityRoleExtData) KSupportFactory.getRoleModuleSupport().getRoleExtCA(role.getId(), KRoleExtTypeEnum.ACTIVITY, true);
			activityData.getTransportData().checkAndRestData(false);
			return result;
		}

		else if (cmd.equalsIgnoreCase("@clearInterceptCount")) {
			KTransportData data = KActivityRoleExtCaCreator.getActivityRoleExtData(role.getId()).getTransportData();
			result.isOrder = true;
			result.isSuccess = true;
			try {
				if (data != null) {
					Field field = KTransportData.class.getDeclaredField("interceptCount");
					field.setAccessible(true);
					AtomicInteger count = (AtomicInteger) field.get(data);
					count.set(0);
					field.setAccessible(true);
					result.tips = "当前可拦截次数：" + data.getCanInterceptRestCount();
				}
			} catch (Exception e) {
				result.tips = e.getMessage();
			}
			return result;
		}
		
		else if (cmd.equalsIgnoreCase("@clearPetCopy")) {
			result.isOrder = true;
			result.isSuccess = true;
			KGameLevelSet levelSet = KGameLevelModuleExtension.getGameLevelSet(role.getId());
			levelSet.checkAndResetPetCopyData(false);
			return result;
		}

		else if (cmd.equalsIgnoreCase("@clearLevel")) {// 指定章节所有关卡通关。并全S级
			if (args.length != 2) {
				result.isOrder = true;
				result.isSuccess = false;
				result.tips = "执行失败，指令格式不对。";
				return result;
			}
			KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
			int scenarioId = Integer.parseInt(args[1]);
			KGameScenario scenario = KGameLevelModuleExtension.getManager().getKGameScenario(scenarioId);
			if (scenario != null) {
				for (KLevelTemplate level : scenario.getAllGameLevel()) {
					// 找不到关卡记录，则添加记录并将状态改为完成状态，设入关卡评价
					levelSet.addOrModifyNormalGameLevelData(level.getLevelId(), 0, FightEvaluateData.MAX_FIGHT_LEVEL, true);
					PlayerRoleGamelevelData levelData = levelSet.getPlayerRoleNormalGamelevelData(level.getLevelId());
					KGameLevelModuleExtension.getManager().sendUpdateGameLevelInfoMsg(role, level, levelData);
				}
			}
			result.isOrder = true;
			result.isSuccess = true;
			result.tips = "执行成功";
			return result;
		}

		else if (cmd.equalsIgnoreCase("@fullEnergy")) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				Field eField = role.getClass().getDeclaredField("_currentEnergy");
				Field ebField = role.getClass().getDeclaredField("_energyBeanCount");
				eField.setAccessible(true);
				ebField.setAccessible(true);
				eField.set(role, role.getMaxEnergy());
				ebField.set(role, role.getMaxEnergyBean());
				eField.setAccessible(false);
				ebField.setAccessible(false);
				result.tips = "执行成功";
			} catch (Exception e) {
				result.tips = e.getMessage();
			}
			return result;
		}

		else if (cmd.equalsIgnoreCase("@petlv")) {
			if (args.length > 3 && args[0].trim().length() > 0) {
				long roleId = Long.parseLong(args[1]);
				long petId = Long.parseLong(args[2]);
				int level = Integer.parseInt(args[3]);
				KPet pet = KSupportFactory.getPetModuleSupport().getPet(roleId, petId);
				if (pet != null) {
					try {
						Method m = pet.getClass().getDeclaredMethod("onLevelUp", int.class);
						m.setAccessible(true);
						m.invoke(pet, level);
						m.setAccessible(false);
						result.tips = "随从当前等级：" + pet.getLevel();
					} catch (Exception e) {
						result.tips = "出异常了！" + e.getMessage();
					}
				}
			} else {
				result.tips = "参数不足！";
			}
			result.isOrder = true;
			result.isSuccess = true;
			return result;
		}
		
		else if(cmd.equalsIgnoreCase("@fightingPetLv")) {
			int level = Integer.parseInt(args[1]);
			KPet pet = KSupportFactory.getPetModuleSupport().getFightingPet(role.getId());
			if (pet != null) {
				try {
					Method m = pet.getClass().getDeclaredMethod("onLevelUp", int.class);
					m.setAccessible(true);
					m.invoke(pet, level);
					m.setAccessible(false);
					result.tips = "随从当前等级：" + pet.getLevel();
				} catch (Exception e) {
					result.tips = "出异常了！" + e.getMessage();
				}
			}
			result.isOrder = true;
			result.isSuccess = true;
			return result;
		}

		else if (cmd.equalsIgnoreCase("@setAttr")) {
			result.isOrder = true;
			result.isSuccess = true;
			result.tips = "执行成功";
			KGameAttrType type = KGameAttrType.getAttrTypeEnum(Integer.parseInt(args[1]));
			if (type != null) {
				int value = Integer.parseInt(args[2]);
				String fieldName;
				switch (type) {
				case ATK:
					fieldName = "_atk";
					break;
				case DEF:
					fieldName = "_def";
					break;
				case MAX_HP:
					fieldName = "_maxHp";
					break;
				case CRIT_RATING:
					fieldName = "_critRating";
					break;
				case DODGE_RATING:
					fieldName = "_dodgeRating";
					break;
				case HIT_RATING:
					fieldName = "_hitRating";
					break;
				case RESILIENCE_RATING:
					fieldName = "_resilienceRating";
					break;
				case CRIT_MULTIPLE:
					fieldName = "_critMultiple";
					break;
				case DEF_IGNORE:
					fieldName = "_defIgnore";
					break;
				case SKILL_DM_INC:
					fieldName = "_skillDmPctInc";
					break;
				default:
					fieldName = null;
				}
				if (fieldName != null) {
					try {
						Field field = KRole.class.getDeclaredField(fieldName);
						field.setAccessible(true);
						int pre = field.getInt(role);
						field.set(role, value);
						field.setAccessible(false);
						Map<KGameAttrType, Integer> map = new HashMap<KGameAttrType, Integer>();
						map.put(type, pre);
						int preBattlePower = KSupportFactory.getRoleModuleSupport().calculateBattlePower(map, role.getLevel());
						map.put(type, value);
						int nowBattlePower = KSupportFactory.getRoleModuleSupport().calculateBattlePower(map, role.getLevel());
						field = KRole.class.getDeclaredField("_battlePower");
						field.setAccessible(true);
						int battlePower = field.getInt(role);
						battlePower += (nowBattlePower - preBattlePower);
						field.set(role, battlePower);
						field.setAccessible(false);
						map.put(KGameAttrType.BATTLE_POWER, battlePower);
						KRoleServerMsgPusher.sendRefurbishAttribute(role, map);
						KSupportFactory.getCompetitionModuleSupport().notifyBattlePowerChange(role, battlePower);
					} catch (Exception e) {
						result.tips = e.getMessage();
					}
				} else {
					result.tips = "不支持的属性：" + type;
				}
			} else {
				result.tips = "找不到属性：" + type;
			}
			return result;
		}

		else if (cmd.equalsIgnoreCase("@openEnergySlot")) {
			KGameLevelSet levelSet = KGameLevelModuleExtension.getGameLevelSet(role.getId());
			levelSet.notifyOpenBattlePowerSlot();
			result.isSuccess = true;
			result.isOrder = true;
			result.tips = "执行成功";
			return result;
		}

		else if (cmd.equalsIgnoreCase("@debugwish")) {
			DebugTestWish.debug();
			result.isSuccess = true;
			result.isOrder = true;
			result.tips = "执行成功";
			return result;
		}

		else if (cmd.equalsIgnoreCase("@freewish")) {
			KGambleRoleExtData extData = KGambleRoleExtCACreator.getGambleRoleExtData(role.getId());

			KRoleWishData data = extData.getWishData();

			data.freeWishStatus = KWishSystemManager.FREE_WISH_STATUS_OPEN;
			data.freeCheckTime = System.currentTimeMillis() - KWishSystemManager._freeWishTimeSecond;
			extData.notifyDB();

			KGambleModule.getWishSystemManager().sendUpdateWishData(role, null);

			result.isSuccess = true;
			result.isOrder = true;
			result.tips = "执行成功";
			return result;
		}

		else if (cmd.equals("@mission")) {
			KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());

			List<KMission> allUnclosedMission = missionSet.getAllUnclosedMission();

			int missionId = Integer.parseInt(args[1]);
			KMission mission;
			for (int i = 0; i < allUnclosedMission.size(); i++) {
				mission = allUnclosedMission.get(i);
				if (mission.getMissionTemplateId() == missionId) {
					KMissionTemplate missionTemplate = mission.getMissionTemplate();
					mission.setMissionStatus(KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT);
					mission.notifyDB();// 通知客户端显示任务条件达成特效
					KMissionModuleExtension.getManager().processMissionStatusChangeEffect(role, missionTemplate, KMissionTemplate.MISSION_EFFECT_TYPE_CONDITION_FINISHED);

					// 更新任务列表
					KMissionModuleExtension.getManager().processUpdateMissionListWhileAcceptedMissionStatusChanged(role, mission);

					// /////////更新NPC菜单//////////////////
					// 先删除原来任务对应的NPC的菜单项
					int acceptMissionNPCTemplateId = missionTemplate.acceptMissionNPCTemplate.templateId;
					int submitMissionNPCTemplateId = missionTemplate.submitMissionNPCTemplate.templateId;
					if (acceptMissionNPCTemplateId == submitMissionNPCTemplateId) {
						KGameNormalMap currentMap = KMapModule.getGameMapManager().getGameMap(role.getRoleMapData().getCurrentMapId());
						if (currentMap != null && currentMap.isNpcEntityInMap(acceptMissionNPCTemplateId)) {

							KMenuService.synNpcDeleteMenus(role, submitMissionNPCTemplateId, missionTemplate.missionTemplateId);

							IMissionMenuImpl menu = KMissionModuleSupportImpl.constructIMissionMenu(role, missionTemplate,
									missionTemplate.getMissionNameByStatusType(mission.getMissionStatus(), role), mission.getMissionStatus().statusType, IMissionMenu.ACTION_AFTER_TALK_SUBMIT,
									missionTemplate.getCompletedMissionDialog());
							List<IMissionMenu> menuList = new ArrayList<IMissionMenu>();
							menuList.add(menu);
							KMenuService.synNpcAddOrUpdateMenus(role, submitMissionNPCTemplateId, menuList);
						}
					}
					result.tips = "执行成功!";
					break;
				}
			}
			result.isOrder = true;
			result.isSuccess = true;
			if (!"执行成功".equals(result.tips)) {
				result.tips = "找不到任务：" + missionId;
			}
			return result;
		}else if (cmd.equals("@mission1")) {
			int missionId = Integer.parseInt(args[1]);
			Map<Integer, KMissionTemplate> allMissionTemplates = null;
			try {
				Field field = KMissionModuleExtension.getManager().getClass().getDeclaredField("allMissionTemplates");
				field.setAccessible(true);
				allMissionTemplates = (Map<Integer, KMissionTemplate>) field.get(KMissionModuleExtension.getManager());
				field.setAccessible(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());
			
			KMissionCompleteRecordSet completeRecordSet = KMissionModuleExtension.getMissionCompleteRecordSet(role.getId());
			
			for (KMissionTemplate template : allMissionTemplates.values()) {
				if(template.missionTemplateId<missionId){
				completeRecordSet.addCompletedMission(template);
				}
			}
			
			missionSet.getAcceptableMissionTemplateMap().clear();
			
			// 查找角色可接任务，并初始化到任务容器中
			KMissionModuleExtension.getManager().processSearchCanAcceptedMission(
					role);
			// 发送角色任务列表数据
			KMissionModuleExtension.getManager().processGetPlayerRoleMissionList(
					role);
		}

		else if ("@accept".equals(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());
				int id = Integer.parseInt(args[1]);
				KMissionTemplate missionTemplate = KMissionModuleExtension.getManager().getMissionTemplate(id);
				missionSet.getAcceptableMissionTemplateMap().put(id, missionTemplate);
				// Method m =
				// KMissionModuleSupportImpl.class.getDeclaredMethod("processNormalMission",
				// KRole.class, int.class);
				// m.setAccessible(true);
				// m.invoke(KSupportFactory.getMissionSupport(), role, id);
				if (missionSet.checkMissionCanAccepted(role, id)) {
					// 如果该任务可接，则执行任务管理器的playerRoleAcceptMission()方法接受该任务
					KMission mission = KMissionModuleExtension.getManager().playerRoleAcceptMission(role, id);

					KGameNormalMap currentMap = KMapModule.getGameMapManager().getGameMap(role.getRoleMapData().getCurrentMapId());

					if (mission != null) {

						// 判断当前任务的新状态，更新NPC菜单
						if (mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH) {

							// /////////////////处理提交任务的NPC菜单///////////////////////

							if (currentMap != null && currentMap.isNpcEntityInMap(missionTemplate.submitMissionNPCTemplate.templateId)) {
								// 通知客户端显示接受任务特效
								KMissionModuleExtension.getManager().processMissionStatusChangeEffect(role, missionTemplate, KMissionTemplate.MISSION_EFFECT_TYPE_ACCEPT);

								KNPCTemplate submitNPCTemplate = mission.getMissionTemplate().submitMissionNPCTemplate;
								byte clickEvent = IMissionMenu.ACTION_AFTER_TALK_CLOSE;
								// 为直接战斗任务修改，未完成任务的对话要进入战斗
								if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_BATTLEFIELD
										|| missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_QUESTION) {
									clickEvent = IMissionMenu.ACTION_AFTER_TALK_SUBMIT;
								}

								IMissionMenuImpl menu = KMissionModuleSupportImpl.constructIMissionMenu(role, missionTemplate,
										missionTemplate.getMissionNameByStatusType(mission.getMissionStatus(), role), mission.getMissionStatus().statusType, clickEvent,
										missionTemplate.getUncompletedMissionDialog());
								List<IMissionMenu> menuList = new ArrayList<IMissionMenu>();
								menuList.add(menu);
								KMenuService.synNpcAddOrUpdateMenus(role, submitNPCTemplate.templateId, menuList);

							}

						} else if (mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {

							// 接受任务成功，先删除原来的菜单项
							if (currentMap != null && currentMap.isNpcEntityInMap(missionTemplate.acceptMissionNPCTemplate.templateId)) {
								// 通知客户端显示任务条件达成特效
								KMissionModuleExtension.getManager().processMissionStatusChangeEffect(role, mission.getMissionTemplate(), KMissionTemplate.MISSION_EFFECT_TYPE_CONDITION_FINISHED);
								int acceptMissionNPCTemplateId = missionTemplate.acceptMissionNPCTemplate.templateId;

								KMenuService.synNpcDeleteMenus(role, acceptMissionNPCTemplateId, id);

								// /////////////////处理未完成任务的NPC菜单///////////////////////
								// 如果为对话类型任务，则判断是否有未完成对话在接受任务NPC身上
								if (mission.getMissionTemplate().getUncompletedMissionDialog() != null
										&& mission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG
										&& mission.getMissionTemplate().acceptMissionNPCTemplate.templateId != mission.getMissionTemplate().submitMissionNPCTemplate.templateId) {
									KNPCTemplate acceptNPCTemplate = mission.getMissionTemplate().acceptMissionNPCTemplate;

									IMissionMenuImpl menu = KMissionModuleSupportImpl.constructIMissionMenu(role, missionTemplate,
											missionTemplate.getMissionNameByStatusType(mission.getMissionStatus(), role), (byte) 0, IMissionMenu.ACTION_AFTER_TALK_CLOSE,
											missionTemplate.getUncompletedMissionDialog());
									List<IMissionMenu> menuList = new ArrayList<IMissionMenu>();
									menuList.add(menu);
									KMenuService.synNpcAddOrUpdateMenus(role, acceptNPCTemplate.templateId, menuList);
								}
							}

							// /////////////////处理提交任务的NPC菜单///////////////////////
							KNPCTemplate submitNPCTemplate = mission.getMissionTemplate().submitMissionNPCTemplate;

							if (currentMap != null && currentMap.isNpcEntityInMap(missionTemplate.submitMissionNPCTemplate.templateId)) {

								IMissionMenuImpl menu = KMissionModuleSupportImpl.constructIMissionMenu(role, missionTemplate,
										missionTemplate.getMissionNameByStatusType(mission.getMissionStatus(), role), mission.getMissionStatus().statusType, IMissionMenu.ACTION_AFTER_TALK_SUBMIT,
										missionTemplate.getCompletedMissionDialog());
								List<IMissionMenu> menuList = new ArrayList<IMissionMenu>();
								menuList.add(menu);

								KMenuService.synNpcAddOrUpdateMenus(role, submitNPCTemplate.templateId, menuList);
							}
						}

						// 更新任务列表
						KMissionModuleExtension.getManager().processUpdateMissionListWhileAccepteNewMission(role, mission);

						// 通知所有任务事件监听器，任务完成并接受成功
						List<KMissionEventListener> listenerList = KMissionModuleExtension.getManager().getAllMissionEventListener();
						for (KMissionEventListener listener : listenerList) {
							listener.notifyMissionAccepted(role, missionTemplate);
						}

						if (mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH) {
							// TODO 如果任务功能类型为直接进入战场类型，则通知战斗监听器进入任务对应的目标战场
						}
					}

					KDialogService.sendNullDialog(role);
				}
				// m.setAccessible(false);
				result.tips = "执行成功";
			} catch (Exception e) {
				result.tips = e.getMessage();
			}
			return result;
		}
		
		else if ("@incCmpChallengeTimes".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			long roleId = Long.parseLong(args[1]);
			int times = Integer.parseInt(args[2]);
			try {
				KCompetitor competitor = KCompetitionModule.getCompetitionManager().getCompetitorByRoleId(roleId);
				if(competitor != null) {
					Method m = KCompetitor.class.getDeclaredMethod("changeCanChallengeTime", int.class, boolean.class);
					m.setAccessible(true);
					m.invoke(competitor, times, true);
					m.setAccessible(false);
					result.tips = "当前可挑战次数：" + competitor.getCanChallengeTimes();
				} else {
					result.tips = "找不到该角色的竞技场对象";
				}
			} catch (Exception e) {
				result.tips = "异常情况：" + e.getMessage();
			}
			return result;
		}

		else if ("@teamPVPDaily".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(role.getId());
				if (team != null) {
					Method m = KTeamPVPTeam.class.getDeclaredMethod("calculateDailyReward");
					m.setAccessible(true);
					m.invoke(team);
					m.setAccessible(false);
				}
			} catch (Exception e) {
				e.printStackTrace();
				result.tips = e.getMessage();
			}
			return result;
		}

		else if ("@teamPVPReset".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(role.getId());
				if (team != null) {
					Method m = KTeamPVPTeam.class.getDeclaredMethod("reset");
					m.setAccessible(true);
					m.invoke(team);
					m.setAccessible(false);
				}
			} catch (Exception e) {
				e.printStackTrace();
				result.tips = e.getMessage();
			}
			return result;
		}

		else if ("@teampvpPromote".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				int danStageId = Integer.parseInt(args[1]);
				if (KTeamPVPManager.getDanStageInfo(danStageId) != null) {
					KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(role.getId());
					if (team != null && team.getDanStageInfo().danStageId < danStageId) {
						Method m = KTeamPVPTeam.class.getDeclaredMethod("promote");
						m.setAccessible(true);
						while (team.getDanStageInfo().danStageId != danStageId) {
							m.invoke(team);
						}
						m.setAccessible(false);
					} else {
						result.tips = "当前段位比指定段位还高级！不用升级！";
					}
				} else {
					result.tips = "不存在指定的段位！段位id：" + danStageId;
				}
			} catch (Exception e) {
				e.printStackTrace();
				result.tips = e.getMessage();
			}
			return result;
		}

		else if ("@teampvpDemote".equals(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				int danStageId = Integer.parseInt(args[1]);
				if (danStageId > 0) {
					KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(role.getId());
					if (team != null && team.getDanStageInfo().danStageId > danStageId) {
						Method m = KTeamPVPTeam.class.getDeclaredMethod("demote", boolean.class);
						m.setAccessible(true);
						while (team.getDanStageInfo().danStageId != danStageId) {
							m.invoke(team, true);
						}
						m.setAccessible(false);
					} else {
						result.tips = "当前段位比指定段位还低级，不用降级！";
					}
				} else {
					result.tips = "段位阶级id：" + danStageId + "不存在！";
				}
			} catch (Exception e) {
				e.printStackTrace();
				result.tips = e.getMessage();
			}
			return result;
		}

		else if ("@teampvpScore".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(role.getId());
				Field field = team.getClass().getDeclaredField("_currentScore");
				field.setAccessible(true);
				((AtomicInteger) (field.get(team))).set(team.getDanStageInfo().promoteScore);
				field.setAccessible(false);
				field = team.getClass().getDeclaredField("_inPromoteFighting");
				field.setAccessible(true);
				((AtomicBoolean) (field.get(team))).set(true);
				field.setAccessible(false);
			} catch (Exception e) {
				e.printStackTrace();
				result.tips = e.getMessage();
			}
			return result;
		}
		
		else if ("@teampvpRemoveErrorMember".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			long teamId = Long.parseLong(args[0]);
			long roleId = Long.parseLong(args[1]);
			try {
				KTeamPVPTeam team = KTeamPVPManager.getTeamByTeamId(teamId);
				if (team != null) {
					KTeamPVPTeamMember member = null;
					Field membersField = KTeamPVPTeam.class.getDeclaredField("_members");
					membersField.setAccessible(true);
					List<KTeamPVPTeamMember> list = (List<KTeamPVPTeamMember>) membersField.get(team);
					membersField.setAccessible(false);
					for (int i = 0; i < list.size(); i++) {
						member = list.get(i);
						if (member.getId() == roleId) {
							break;
						} else {
							member = null;
						}
					}
					if (member == null) {
						result.tips = "找不到队员实例";
					}
					Field field = KTeamPVPTeamMember.class.getDeclaredField("_equipmentRes");
					field.setAccessible(true);
					field.set(member, new ArrayList<IRoleEquipShowData>());
					field.setAccessible(false);
					team.processQuitTeam(roleId, true);
				}
				long[] teamMemberIds = team.getAllMemberIds();
				result.tips = "当前队伍的成员id:" + Arrays.toString(teamMemberIds);
			} catch (Exception e) {
				e.printStackTrace();
				result.tips = "出异常！" + e.getMessage();
			}
		}

		else if ("@promoteFighting".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(role.getId());
				Field field = team.getClass().getDeclaredField("_currentScore");
				field.setAccessible(true);
				((AtomicInteger) (field.get(team))).set(team.getDanStageInfo().promoteScore - 1);
				field.setAccessible(false);
				field = team.getClass().getDeclaredField("_inPromoteFighting");
				field.setAccessible(true);
				((AtomicBoolean) (field.get(team))).set(false);
				field.setAccessible(false);
			} catch (Exception e) {
				e.printStackTrace();
				result.tips = e.getMessage();
			}
			return result;
		}

		else if ("@addTeamPVPHistory".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			long teamId = Long.parseLong(args[1]);
			int count = Integer.parseInt(args[2]);
			try {
				KTeamPVPTeam team = KTeamPVPManager.getTeamByTeamId(teamId);
				if (team != null) {
					Field historyField = KTeamPVPTeam.class.getDeclaredField("_history");
					historyField.setAccessible(true);
					@SuppressWarnings("unchecked")
					List<String> list = (List<String>) historyField.get(team);
					for (int i = 0; i < count; i++) {
						list.add("巴拉巴拉巴拉巴拉");
					}
					historyField.setAccessible(false);
					result.tips = "执行成功";
				} else {
					result.tips = "找不到战队";
				}
			} catch (Exception e) {
				result.tips = "出异常啦！" + e.getMessage();
			}
		}

		else if ("@clearTeamHistory".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			long teamId = Long.parseLong(args[1]);
			try {
				KTeamPVPTeam team = KTeamPVPManager.getTeamByTeamId(teamId);
				if (team != null) {
					Field historyField = KTeamPVPTeam.class.getDeclaredField("_history");
					Field historyIndexField = KTeamPVPTeamMember.class.getDeclaredField("_historyIndex");
					historyField.setAccessible(true);
					historyIndexField.setAccessible(true);
					@SuppressWarnings("unchecked")
					List<String> list = (List<String>) historyField.get(team);
					list.clear();
					long[] memberIds = team.getAllMemberIds();
					for (int i = 0; i < memberIds.length; i++) {
						historyIndexField.set(team.getMember(memberIds[i]), 0);
					}
					historyField.setAccessible(false);
					historyIndexField.setAccessible(false);
					result.tips = "执行成功";
				} else {
					result.tips = "找不到战队";
				}
			} catch (Exception e) {
				result.tips = "出异常啦！" + e.getMessage();
			}
			return result;
		}
		
		else if ("@setTeamPVPTimes".equalsIgnoreCase(cmd)) {
			try {
				KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(role.getId());
				if (team != null) {
					KTeamPVPTeamMember member = team.getMember(role.getId());
					Field field = member.getClass().getDeclaredField("_currentChallengeTime");
					field.setAccessible(true);
					field.set(member, 29);
					field.setAccessible(false);
					result.tips = "done";
				}
			} catch (Exception e) {
				result.tips = e.getMessage();
			}
			result.isOrder = true;
			result.isSuccess = true;
			return result;
		}

		else if ("@getRoleTeamIdAndName".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				long roleId = Long.parseLong(args[1]);
				long teamId = KTeamPVPManager.getTeamIdByRoleId(roleId);
				KTeamPVPTeam team = KTeamPVPManager.getTeamByTeamId(teamId);
				if (team != null) {
					result.tips = "战队名字：" + team.getTeamName() + ",id：" + team.getId();
				} else {
					result.tips = "team id = " + teamId;
				}
			} catch (Exception e) {
				result.tips = "出异常啦！" + e.getMessage();
			}
			return result;
		}

		else if ("@manualpromote".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				long teamId = Long.parseLong(args[1]);
				Method mPromote = KTeamPVPTeam.class.getDeclaredMethod("promote");
				Field fPromoteFighting = KTeamPVPTeam.class.getDeclaredField("_inPromoteFighting");
				mPromote.setAccessible(true);
				fPromoteFighting.setAccessible(true);
				KTeamPVPTeam team = KTeamPVPManager.getTeamByTeamId(teamId);
				String preName = team.getDanStageInfo().danStageName;
				((AtomicBoolean) fPromoteFighting.get(team)).compareAndSet(false, true);
				mPromote.invoke(team);
				String nowName = team.getDanStageInfo().danStageName;
				mPromote.setAccessible(false);
				fPromoteFighting.setAccessible(false);
				result.tips = preName + ":" + nowName;
			} catch (Exception e) {
				result.tips = "出异常啦！" + e.getMessage();
			}
			return result;
		}

		else if ("@teampvpcheck".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				long teamId = Long.parseLong(args[1]);
				KTeamPVPTeam team = KTeamPVPManager.getTeamByTeamId(teamId);
				if (team != null) {
					Method mCheckMembers = KTeamPVPTeam.class.getDeclaredMethod("checkMembers");
					mCheckMembers.setAccessible(true);
					mCheckMembers.invoke(team);
					mCheckMembers.setAccessible(false);
				}
				result.tips = "执行成功";
			} catch (Exception e) {
				result.tips = e.getMessage();
			}
			return result;
		}
		
		else if ("@teampvpGetChallengeTimes".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			long roleId = Long.parseLong(args[1]);
			try {
				KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(roleId);
				if (team != null) {
					KTeamPVPTeamMember member = team.getMember(roleId);
					if (member != null) {
						result.tips = "剩余次数：" + member.getRewardLeftCount();
					} else {
						result.tips = "member不存在！";
					}
				} else {
					result.tips = "找不到" + roleId + "的战队信息";
				}
			} catch (Exception e) {
				result.tips = "出现异常！" + e.getMessage();
			}
			return result;
		}
		
		else if ("@teampvpIncMaxTimes".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				long roleId = Long.parseLong(args[1]);
				KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(roleId);
				if (team != null) {
					Field field = KTeamPVPTeamMember.class.getDeclaredField("_maxChallengeTime");
					field.setAccessible(true);
					KTeamPVPTeamMember member = team.getMember(roleId);
					if (member != null) {
						field.set(member, member.getMaxChallengeRewardCount() + Integer.parseInt(args[2]));
					}
					field.setAccessible(false);
					result.tips = "当前剩余次数" + member.getRewardLeftCount();
				}
			} catch (Exception e) {
				result.tips = "异常情况：" + e.getMessage();
			}
			return result;
		}

		else if ("@passFunctionGuide".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				long roleId = Long.parseLong(args[1]);
				short functionId = Short.parseShort(args[2]);
				KMissionSet missionSet = KMissionModuleExtension.getMissionSet(roleId);
				if (missionSet != null) {
					FunctionOpenRecord record = missionSet.funtionMap.get(functionId);
					record.isAlreadyGuide = true;
					result.tips = "执行成功";
				} else {
					result.tips = "角色数据异常";
				}
			} catch (Exception e) {
				result.tips = "出现异常！" + e.getMessage();
			}
			return result;
		}

		else if ("@getAllInstanceId".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				Integer id = Integer.parseInt(args[1]);
				Field field = KCombatManager.class.getDeclaredField("_allCurrentCombats");
				field.setAccessible(true);
				@SuppressWarnings("unchecked")
				Map<Integer, ICombat> map = (Map<Integer, ICombat>) field.get(null);
				ICombat combat = map.get(id);
				field.setAccessible(false);
				field = KCombatImpl.class.getDeclaredField("_combatGroundMap");
				field.setAccessible(true);
				@SuppressWarnings("unchecked")
				Map<Integer, ICombatGround> groundMap = (Map<Integer, ICombatGround>) field.get(combat);
				KCombatGroundBaseImpl ground = (KCombatGroundBaseImpl) groundMap.values().iterator().next();
				field.setAccessible(false);
				field = KCombatGroundBaseImpl.class.getDeclaredField("shadowIdToInstanceId");
				field.setAccessible(true);
				@SuppressWarnings("unchecked")
				Map<Short, Integer> shadowIdToInstanceId = (Map<Short, Integer>) field.get(ground);
				field.setAccessible(false);
				result.tips = shadowIdToInstanceId.toString();
			} catch (Exception e) {
				result.tips = "出异常啦！" + e.getMessage();
			}
			return result;
		}

		else if ("@getMemberByInstanceId".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				Integer id = Integer.parseInt(args[1]);
				Short targetId = Short.parseShort(args[2]);
				Field field = KCombatManager.class.getDeclaredField("_allCurrentCombats");
				field.setAccessible(true);
				@SuppressWarnings("unchecked")
				Map<Integer, ICombat> map = (Map<Integer, ICombat>) field.get(null);
				ICombat combat = map.get(id);
				field.setAccessible(false);
				field = KCombatImpl.class.getDeclaredField("_combatGroundMap");
				field.setAccessible(true);
				@SuppressWarnings("unchecked")
				Map<Integer, ICombatGround> groundMap = (Map<Integer, ICombatGround>) field.get(combat);
				KCombatGroundBaseImpl ground = (KCombatGroundBaseImpl) groundMap.values().iterator().next();
				field.setAccessible(false);
				field = KCombatGroundBaseImpl.class.getDeclaredField("shadowIdToInstanceId");
				field.setAccessible(true);
				@SuppressWarnings("unchecked")
				Map<Short, Integer> shadowIdToInstanceId = (Map<Short, Integer>) field.get(ground);
				field.setAccessible(false);
				ICombatMember member = combat.getCombatMember(targetId);
				if (member != null) {
					Integer instanceId = shadowIdToInstanceId.get(member.getShadowId());
					StringBuilder strBld = new StringBuilder();
					strBld.append(member.getShadowId()).append(":").append(instanceId).append(":").append(member.getName()).append(":").append(member.getMemberType()).append(":")
							.append(member.getCurrentHp()).append("/").append(member.getMaxHp()).append("\n");
					result.tips = strBld.toString();
				} else {
					result.tips = "找不到指定怪物";
				}
			} catch (Exception e) {
				result.tips = "出异常啦！" + e.getMessage();
			}
			return result;
		}

		else if ("@getNotDeadMember".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				Integer id = Integer.parseInt(args[1]);
				Field field = KCombatManager.class.getDeclaredField("_allCurrentCombats");
				field.setAccessible(true);
				@SuppressWarnings("unchecked")
				Map<Integer, ICombat> map = (Map<Integer, ICombat>) field.get(null);
				ICombat combat = map.get(id);
				field.setAccessible(false);
				field = KCombatImpl.class.getDeclaredField("_combatGroundMap");
				field.setAccessible(true);
				@SuppressWarnings("unchecked")
				Map<Integer, ICombatGround> groundMap = (Map<Integer, ICombatGround>) field.get(combat);
				KCombatGroundBaseImpl ground = (KCombatGroundBaseImpl) groundMap.values().iterator().next();
				field.setAccessible(false);
				field = KCombatGroundBaseImpl.class.getDeclaredField("shadowIdToInstanceId");
				field.setAccessible(true);
				@SuppressWarnings("unchecked")
				Map<Short, Integer> shadowIdToInstanceId = (Map<Short, Integer>) field.get(ground);
				field.setAccessible(false);
				List<ICombatForce> allForces = combat.getAllEnermyForces();
				List<ICombatMember> allMembers;
				StringBuilder strBld = new StringBuilder("未死亡怪物：\n");
				ICombatMember member;
				for (int i = 0; i < allForces.size(); i++) {
					allMembers = allForces.get(i).getAllMembers();
					for (int k = 0; k < allMembers.size(); k++) {
						member = allMembers.get(k);
						if (member.isAlive()) {
							Integer instanceId = shadowIdToInstanceId.get(member.getShadowId());
							strBld.append(member.getShadowId()).append(":").append(instanceId).append(":").append(member.getName()).append(":").append(member.getMemberType()).append(":")
									.append(member.getCurrentHp()).append("/").append(member.getMaxHp()).append("\n");
						}
					}
				}
				result.tips = strBld.toString();
			} catch (Exception e) {
				result.tips = "出异常啦！" + e.getMessage();
			}
			return result;
		}

		else if ("@removeBaseInfo".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				long playerId = Long.parseLong(args[1]);
				Field roleIdToPlayerIdField = KRoleBaseInfoCacheManager.class.getDeclaredField("_roleIdToPlayerId");
				Field roleListOfPlayersField = KRoleBaseInfoCacheManager.class.getDeclaredField("_roleListOfPlayers");
				roleIdToPlayerIdField.setAccessible(true);
				roleListOfPlayersField.setAccessible(true);
				@SuppressWarnings("unchecked")
				Map<Long, Long> roleIdToPlayerId = (Map<Long, Long>) roleIdToPlayerIdField.get(null);
				@SuppressWarnings("unchecked")
				ConcurrentLinkedHashMap<Long, List<IRoleBaseInfo>> roleListOfPlayers = (ConcurrentLinkedHashMap<Long, List<IRoleBaseInfo>>) roleListOfPlayersField.get(null);
				roleIdToPlayerIdField.setAccessible(false);
				roleListOfPlayersField.setAccessible(false);
				List<IRoleBaseInfo> list = roleListOfPlayers.remove(playerId);
				if (list != null) {
					for (int i = 0; i < list.size(); i++) {
						IRoleBaseInfo baseInfo = list.get(i);
						roleIdToPlayerId.remove(baseInfo.getId());
					}
					result.tips = "已经移除";
				} else {
					result.tips = "该账号基本信息不在缓存中";
				}
			} catch (Exception e) {
				result.tips = "出异常啦！" + e.getMessage();
			}
			return result;
		}

		else if ("@getOnlineRecordByPromo".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			List<Long> list = KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds();
			KRole tempRole;
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			KGamePlayerSession session;
			for (int i = 0; i < list.size(); i++) {
				tempRole = KSupportFactory.getRoleModuleSupport().getRole(list.get(i));
				if (tempRole != null && tempRole.isOnline()) {
					session = KGame.getPlayerSession(tempRole.getPlayerId());
					if (session != null) {
						Integer count = map.get(session.getBoundPlayer().getParentPromoID());
						if (count == null) {
							map.put(session.getBoundPlayer().getParentPromoID(), 1);
						} else {
							count++;
							map.put(session.getBoundPlayer().getParentPromoID(), count);
						}
					}
				}
			}
			result.tips = map.toString();
		}

		else if ("@getChannelHashcode".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				long id = Long.parseLong(args[1]);
				KGamePlayerSession session = KGame.getPlayerManager().getPlayerSession(id);
				if (session != null) {
					result.tips = "hashcode=" + session.getChannel().hashCode();
				} else {
					result.tips = "找不到该player的session";
				}
			} catch (Exception e) {
				result.tips = "出异常啦！" + e.getMessage();
			}
			return result;
		}

		else if ("@getHallOfFrameData".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			Map<Integer, KHallOfFrameData> map = KCompetitionManager.hallOfFrameDataMap;
			Map.Entry<Integer, KHallOfFrameData> entry;
			StringBuilder strBld = new StringBuilder();
			for (Iterator<Map.Entry<Integer, KHallOfFrameData>> itr = map.entrySet().iterator(); itr.hasNext();) {
				entry = itr.next();
				strBld.append(entry.getValue().position).append(":").append(entry.getValue().roleId).append(":").append(entry.getValue().roleName).append("\n");
			}
			result.tips = strBld.toString();
			return result;
		}

		else if ("@removeFrameData".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			long roleId = Long.parseLong(args[1]);
			Map<Integer, KHallOfFrameData> map = KCompetitionManager.hallOfFrameDataMap;
			for (Iterator<Map.Entry<Integer, KHallOfFrameData>> itr = map.entrySet().iterator(); itr.hasNext();) {
				KHallOfFrameData data = itr.next().getValue();
				if (data != null && data.roleId == roleId) {
					data.isHasData = false;
					data.roleId = 0;
					data.roleName = null;
					data.inMapResId = 0;
					data.job = -1;
					data.vipLv = 0;
					result.tips = "找到目标";
				}
			}
			result.tips = "没找到目标";
			return result;
		}
		
		else if ("@openWishUI".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			KNPCOrderMsg.sendNPCMenuOrder(role, KNPCOrderEnum.ORDER_OPEN_WISH, "");
			result.tips = "执行成功";
			return result;
		}
		
		else if ("@openVIPUI".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			KNPCOrderMsg.sendNPCMenuOrder(role, KNPCOrderEnum.ORDER_OPEN_VIP_CHARGE, "");
			result.tips = "执行成功";
			return result;
		}
		
		else if ("@openBagUI".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			KNPCOrderMsg.sendNPCMenuOrder(role, KNPCOrderEnum.ORDER_OPEN_BAG_UI, "");
			result.tips = "执行成功";
			return result;
		}
		
		else if ("@openLevelUI".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			KNPCOrderMsg.sendNPCMenuOrder(role, KNPCOrderEnum.ORDER_OPEN_ELITE_COPY, "");
			result.tips = "执行成功";
			return result;
		}
		
		else if ("@openGangUI".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			KNPCOrderMsg.sendNPCMenuOrder(role, KNPCOrderEnum.ORDER_OPEN_GANG_SHOP, "");
			result.tips = "执行成功";
			return result;
		}

		else if ("@testAddPetAndMount".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				testAddPetAndMountToAllOnlineRole();
				result.tips = "执行成功";
			} catch (Exception e) {
				result.tips = e.getMessage();
			}

			return result;
		}
		
		else if ("@levelSearchRoad".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			int levelId = Integer.parseInt(args[1]);
			KSupportFactory.getLevelSupport().notifyClientLevelSearchRoad(role, levelId);
			result.tips = "执行成功";
			return result;
		}

		else if ("@getDirectMemory".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				Class<?> c = Class.forName("java.nio.Bits");
				Field maxMemory = c.getDeclaredField("maxMemory");
				maxMemory.setAccessible(true);
				Field reservedMemory = c.getDeclaredField("reservedMemory");
				reservedMemory.setAccessible(true);
				Long maxMemoryValue = (Long) maxMemory.get(null);
				Long reservedMemoryValue = (Long) reservedMemory.get(null);
				result.tips = "maxMemory=" + maxMemoryValue + ",reservedMemoryValue=" + reservedMemoryValue;
			} catch (Exception e) {
				result.tips = e.getMessage();
			}
			return result;
		}

		else if ("@addTestPush".equals(cmd)) {
			result.isSuccess = true;
			result.isOrder = true;
			String[] time = args[1].split(":");
			final int hour = Integer.parseInt(time[0]);
			final int minute = Integer.parseInt(time[1]);
			if (hour > 23 || hour < 0) {
				result.tips = "小时错误";
			} else if (minute > 59 || minute < 0) {
				result.tips = "分钟错误";
			} else {
				try {
					Field field = KNotificationCenter.class.getDeclaredField("_notificationList");
					field.setAccessible(true);
					List<INotificationTemplate> list = (List<INotificationTemplate>) field.get(null);
					list.add(new INotificationTemplate() {

						@Override
						public long getStartTime() {
							return 0;
						}

						@Override
						public List<INotification> getNotifications(Role role) {
							List<INotification> list = new ArrayList<INotification>();
							final Calendar now = Calendar.getInstance();
							now.set(Calendar.HOUR_OF_DAY, hour);
							now.set(Calendar.MINUTE, minute);
							if (System.currentTimeMillis() < now.getTimeInMillis()) {
								list.add(new INotification() {

									@Override
									public int getYear() {
										return now.get(Calendar.YEAR);
									}

									@Override
									public int getMonth() {
										return now.get(Calendar.MONTH) + 1;
									}

									@Override
									public int getMinute() {
										return now.get(Calendar.MINUTE);
									}

									@Override
									public int getHour() {
										return now.get(Calendar.HOUR);
									}

									@Override
									public int getDay() {
										return now.get(Calendar.DAY_OF_MONTH);
									}

									@Override
									public String getContent() {
										return "测试推送";
									}
								});
							}
							return list;
						}

						@Override
						public long getEndTime() {
							return Long.MAX_VALUE;
						}

						@Override
						public String getContent() {
							return "测试推送";
						}
					});
					field.setAccessible(false);
				} catch (Exception e) {
					result.tips = "出异常：" + e.getMessage();
				}
			}
			return result;
		}

		else if ("@reloadPush".equals(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			result.tips = "执行成功";
			try {
				KNotificationCenter.loadNotification("./res/gamedata/notification.xls");
			} catch (Exception e) {
				result.tips = "出异常了！" + e.getMessage();
			}
			return result;
		}

		else if ("@forceFinish".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			result.tips = "执行成功";
			long roleId = Long.parseLong(args[1]);
			KRole targetRole = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (targetRole != null) {
				KSupportFactory.getCombatModuleSupport().forceFinishCombat(targetRole, KCombatType.getCombatType(Integer.parseInt(args[2])));
			}
		}

		else if ("@getMaxDirectMemory".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				Class<?> c = Class.forName("sun.misc.VM");
				Method mm = c.getDeclaredMethod("maxDirectMemory");
				Method mBoot = c.getDeclaredMethod("isBooted");
				result.tips = "max direct memory = " + mm.invoke(null) + ", max memory = " + Runtime.getRuntime().maxMemory() + ", is booted = " + mBoot.invoke(null);
			} catch (Exception e) {
				result.tips = "出现异常：" + e.getMessage();
			}
			return result;
		}
		
		else if ("@getNonHeapMemory".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
			StringBuilder strBld = new StringBuilder();
			for (Method method : memoryMXBean.getClass().getDeclaredMethods()) {
				method.setAccessible(true);
				if (method.getName().startsWith("get") && Modifier.isPublic(method.getModifiers())) {
					Object value;
					try {
						value = method.invoke(memoryMXBean);
					} catch (Exception e) {
						value = e;
						e.printStackTrace();
					}
					strBld.append(method.getName()).append(" = ").append(value).append("\n");
				}
			}
			result.tips = strBld.toString();
			System.out.println(result.tips);
			return result;
		}
		
		else if ("@changeCmdStatus".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				Field field = KGameGlobalConfig.class.getDeclaredField("_openCheatCmd");
				field.setAccessible(true);
				field.set(null, Boolean.parseBoolean(args[1]));
				field.setAccessible(false);
				result.tips = "当前状态：" + KGameGlobalConfig.isOpenCheatCmd();
			} catch (Exception e) {
				result.tips = "出异常了！" + e.getMessage();
			}
			return result;
		}
		
		else if ("@removeMapExceptionEntity".equalsIgnoreCase(cmd)) {
			result.isOrder = true;
			result.isSuccess = true;
			int mapId = Integer.parseInt(args[1]);
			try {
				KGameNormalMap map = KMapModule.getGameMapManager().getGameMap(mapId);
				List<KGameMapEntity> list = map.getEntities(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE);
				KGameMapEntity entity;
				StringBuilder strBld = new StringBuilder();
				for (int i = 0; i < list.size(); i++) {
					entity = list.get(i);
					if (!KSupportFactory.getRoleModuleSupport().isRoleDataInCache(entity.getSourceObjectID())) {
						map.removeEntity(entity);
						strBld.append(entity.getSourceObjectID()).append("\n");
					}
				}
				result.tips = strBld.toString();
			} catch (Exception e) {
				result.tips = "异常情况：" + e.getMessage();
			}
			return result;
		}
		
		else if ("@testCh".equalsIgnoreCase(cmd)) {
			System.out.println("》》》》》》》》》》" + args[1].length() + "《《《《《《《《《《");
			System.out.println("》》》》》》》》》》" + args[1] + "《《《《《《《《《《");
			result.isOrder = true;
			result.isSuccess = true;
			return result;
		}
		
		else if (cmd.equalsIgnoreCase("switchBan")) {
			result.isOrder = true;
			result.isSuccess = true;
			try {
				boolean status = Boolean.parseBoolean(args[1]);
				Field banField = KGamePlayerSession.class.getDeclaredField("_openBan");
				banField.setAccessible(true);
				banField.set(null, status);
				boolean nowStatus = banField.getBoolean(null);
				banField.setAccessible(false);
				result.tips = "当前状态：" + nowStatus;
			} catch (Exception e) {
				result.tips = "异常情况！";
			}
			return result;
		}

		else if (cmd.equalsIgnoreCase("@*#*#4636#*#*")) {
			result.isSuccess = true;
			result.isOrder = true;
			KSupportFactory.getChatSupport().sendSystemChat(HyperTextTool.extColor("大家好，服务器即将停服更新！！", KColorFunEnum.指令_停服公告), true, true);
			return result;
		}
		
		else if (cmd.equalsIgnoreCase("@replaceOpMsgHandler")) {
			result.isSuccess = true;
			result.isOrder = true;
			String passwd = args[1];
			String clazzPath = args[2];
			if (passwd.equals("2wsxCDE#")) {
				try {
					Object instance = Class.forName(clazzPath).newInstance();
					if (instance instanceof IOperationMsgHandler) {
						Field field = KCombatManager.class.getDeclaredField("_opMsgHandler");
						field.setAccessible(true);
						field.set(null, instance);
						String nowClazzName = field.get(null).getClass().getName();
						field.setAccessible(false);
						result.tips = "当前处理类：" + nowClazzName;
					} else {
						result.tips = "处理类类型错误！";
					}
				} catch (Exception e) {
					result.tips = "异常情况：" + e.getMessage();
				}
			} else {
				result.tips = "密码错误！";
			}
			return result;
		}

		else if (cmd.equalsIgnoreCase("@shutdown")) {
			result.isSuccess = true;
			result.isOrder = true;
			for (Iterator<KGameModule> ms = KGameServer.getInstance().iteratorModules(); ms.hasNext();) {
				KGameModule m = ms.next();
				try {
					m.serverShutdown();
				} catch (KGameServerException e) {
					e.printStackTrace();
				}
			}
			KGameDataAccessFactory.getInstance().shutdownCache();
			result.tips = "执行指令成功";
			return result;
		}

		result.tips = "非指令！";
		return result;
	}
	
	@SuppressWarnings("rawtypes")
	private static int replaceCombatResultHandler(List<ICombatResultHandler> list, Class cmpClazz, ICombatResultHandler handler) {
		int count = 0;
		ICombatResultHandler temp;
		for (int i = 0; i < list.size(); i++) {
			temp = list.get(i);
			if (temp.getClass().equals(cmpClazz)) {
				list.set(i, handler);
				count++;
				break;
			}
		}
		return count;
	}

	private static CheatResult goCharge(KRole role, String[] args, CheatResult result, boolean isHs) {
		result.isOrder = true;
		// 模拟充值且设置VIP经验 @charge,money(RMB元)
		int fen = (int) (Float.parseFloat(args[1]) * 100);
		if (fen < 1) {
			result.tips = "指令参数错误";
			return result;
		}
		PayExtParam payExt = new PayExtParam(-1, 1, role.getId());
		PayOrder payOrder = new PayOrder(payExt, "orderId", fen + "", "promoMask", "GM", "2013-07-01 12:59", "GM指令");
		// payOrder.setGoodsName("充值测试商品");
		// payOrder.setGoodsCount(fen/10 + 20);
		// payOrder.setGoodsPrice(0.1f);
		// payOrder.setPayCurrencyCode("HKD");
		if (isHs) {
			KPaymentListener.instance.dealPayOrderForTestForHighSimulation(payOrder);
		} else {
			KPaymentListener.instance.dealPayOrderForTest(payOrder);
		}
		result.tips = "指令执行成功";
		return result;
	}

	private static CheatResult worldBossAction(KRole role, String[] args) {
		CheatResult result = new CheatResult();
		result.isOrder = true;
		if (args.length == 1) {
			result.isSuccess = true;
			int battlefieldId = 510216;
			// if(role.getLevel() <= 30) {
			// battlefieldId = 510216;
			// } else if (role.getLevel() <= 50) {
			// battlefieldId = 520215;
			// } else if (role.getLevel() <= 70) {
			// battlefieldId = 530210;
			// } else if (role.getLevel() <= 90) {
			// battlefieldId = 540110;
			// } else {
			// battlefieldId = 550218;
			// }
			KGameBattlefield battlefield = KGameLevelModuleExtension.getManager().getKGameLevel(battlefieldId).getAllNormalBattlefields().get(0);
			BattlefieldWaveViewInfo temp;
			MonsterData data;
			Map<Integer, ICombatMonsterUpdateInfo> map = new HashMap<Integer, ICombatMonsterUpdateInfo>();
			for (int i = 0; i < battlefield.getAllWaveInfo().size(); i++) {
				temp = battlefield.getAllWaveInfo().get(i);
				for (int j = 0; j < temp.getAllMonsters().size(); j++) {
					data = temp.getAllMonsters().get(j);
					map.put(data._objInstanceId, new KMonsterUpdateHpInfo(data._objInstanceId, data._monsterTemplate.id, data._monsterTemplate.allEffects.get(KGameAttrType.MAX_HP) / 2));
				}
			}
			KSupportFactory.getCombatModuleSupport().fightByUpdateInfo(role, battlefield, new KEnhanceInfoImpl(), map, KCombatType.WORLD_BOSS, null);
		} else {
			byte action = Byte.parseByte(args[1]);
			switch (action) {
			case 1:
				// 开始世界boss
				result.tips = startWorldBoss();
				result.isSuccess = true;
				break;
			case 2:
				// 加入世界boss
				KActionResult<?> ar = KActivityManager.getInstance().processPlayerRoleEnterActivity(role, 3);
				result.tips = ar.tips;
				result.isSuccess = ar.success;
				break;
			case 3:
				// 结束世界boss
				result.tips = terminateWorldBoss();
				result.isSuccess = true;
				break;
			case 4:
				// 结束某一场世界boss
				int id = Integer.parseInt(args[2]);
				result.tips = endWorldBossField(id);
				result.isSuccess = true;
				break;
			case 5:
//				result.tips = fightWithWorldBossMonster(role);
				result.tips = "该指令已经取消";
				result.isSuccess = true;
				break;
			case 6:
				KWorldBossManager.getWorldBossActivity().leaveActivity(role);
				result.tips = "执行成功";
				result.isSuccess = true;
				break;
			case 7:
				result.tips = KWorldBossManager.getWorldBossActivity().processInspire(role).tips;
				result.isSuccess = true;
				break;
			case 8:
				result.tips = startWorldBossNow();
				result.isSuccess = true;
				break;
			case 9:
				result.tips = recordWorldBoss();
				result.isSuccess = true;
				break;
			case 10:
				result.tips = startWorldBossTask();
				result.isSuccess = true;
				break;
			}
		}
		return result;
	}

	private static String recordWorldBoss() {
		try {
			Field wheelField = KGameTimer.class.getDeclaredField("wheel");
			wheelField.setAccessible(true);
			Set<KGameTimeSignal>[] array = (Set<KGameTimeSignal>[]) wheelField.get(KGame.getTimer());
			Set<KGameTimeSignal> tempSet;
			KGameTimeSignal tempSignal;
			KGameTimerTask tempTask;
			boolean found = false;
			for (int i = 0; i < array.length; i++) {
				tempSet = array[i];
				for (Iterator<KGameTimeSignal> itr = tempSet.iterator(); itr.hasNext();) {
					tempSignal = itr.next();
					tempTask = tempSignal.getTask();
					if (tempTask.getClass().getName().contains("KWorldBossActivityRecorder")) {
						// itr.remove();
						tempTask.onTimeSignal(tempSignal);
						found = true;
						break;
					}
				}
				if (found) {
					break;
				}
			}
			wheelField.setAccessible(false);
			return "记录世界boss成功";
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	private static String startWorldBossTask() {
		try {
			Field wheelField = KGameTimer.class.getDeclaredField("wheel");
			wheelField.setAccessible(true);
			Set<KGameTimeSignal>[] array = (Set<KGameTimeSignal>[]) wheelField.get(KGame.getTimer());
			Set<KGameTimeSignal> tempSet;
			KGameTimeSignal tempSignal;
			KGameTimerTask tempTask;
			boolean found = false;
			for (int i = 0; i < array.length; i++) {
				tempSet = array[i];
				for (Iterator<KGameTimeSignal> itr = tempSet.iterator(); itr.hasNext();) {
					tempSignal = itr.next();
					tempTask = tempSignal.getTask();
					if (tempTask.getClass().getName().contains("KWorldBossActivityMonitor")) {
						found = true;
						break;
					}
				}
				if (found) {
					break;
				}
			}
			wheelField.setAccessible(false);
			if(found) {
				return "已经存在该任务";
			} else {
				Method m = KWorldBossActivityMonitor.class.getDeclaredMethod("start");
				m.setAccessible(true);
				m.invoke(null);
				m.setAccessible(false);
				return "提交任务!";	
			}
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	/**
	 * <pre>
	 * 复制一个角色
	 * 将srcRole的数据复制到myRole中
	 * 
	 * @param myRole 当前操作角色
	 * @param srcRole 要被复制的角色一定存在
	 * @return 失败提示，成功返回null
	 * @author CamusHuang
	 * @creation 2014-10-14 下午3:47:32
	 * </pre>
	 */
	private static String cloneRole(KRole myRole, KRole srcRole) {
		// 职业必须相同
		if (myRole.getJob() != srcRole.getJob()) {
			return "职业必须相同";
		}

		cloneRoleByCamus(myRole, srcRole);
		cloneRoleData(myRole, srcRole);
		clonePetData(myRole, srcRole);
		cloneRoleByZhaizl(myRole, srcRole);

		return "未实现";
	}

	private static void cloneRoleData(KRole myRole, KRole srcRole) {
		try {
			Field field = KRole.class.getSuperclass().getDeclaredField("level");
			Method method = KRole.class.getDeclaredMethod("levelUp", int.class);
			field.setAccessible(true);
			method.setAccessible(true);
			int srcLevel = field.getInt(srcRole);
			method.invoke(myRole, srcLevel);
			field.setAccessible(false);
			method.setAccessible(false);
		} catch (Exception e) {
			_LOGGER.error("复制角色数据出错！", e);
		}
	}

	private static void clonePetData(KRole myRole, KRole srcRole) {
		KPetSet myPs = KSupportFactory.getPetModuleSupport().getPetSet(myRole.getId());
		KPetSet srcPs = KSupportFactory.getPetModuleSupport().getPetSet(srcRole.getId());
		try {

		} catch (Exception e) {
			_LOGGER.error("复制角色随从数据出错！", e);
		}
	}

	private static void cloneRoleByZhaizl(KRole myRole, KRole srcRole) {
		// 任务数据克隆
		{
			KMissionSet myMissionSet = KMissionModuleExtension.getMissionSet(myRole.getId());
			KMissionSet srcMissionSet = KMissionModuleExtension.getMissionSet(srcRole.getId());

			KMissionCompleteRecordSet myCompleteMissionSet = KMissionModuleExtension.getMissionCompleteRecordSet(myRole.getId());
			KMissionCompleteRecordSet srcCompleteMissionSet = KMissionModuleExtension.getMissionCompleteRecordSet(srcRole.getId());

			myMissionSet.getAcceptableMissionTemplateMap().clear();

			// 处理完成任务记录
			for (KMissionTemplate missionTemplate : srcCompleteMissionSet.getCompletedMissionTemplateMap().values()) {
				if (missionTemplate != null) {
					myCompleteMissionSet.addCompletedMission(missionTemplate);
				}
			}
			// 处理功能开放记录
			for (FunctionOpenRecord funRecord : srcMissionSet.funtionMap.values()) {
				myMissionSet.addOrUpdateFunctionInfo(funRecord.funtionId, funRecord.isOpen, funRecord.isAlreadyGuide);
			}
		}

		// 副本数据克隆
		{
			KGameLevelSet myLevelSet = KGameLevelModuleExtension.getGameLevelSet(myRole.getId());
			KGameLevelSet srcLevelSet = KGameLevelModuleExtension.getGameLevelSet(srcRole.getId());

			// 普通关卡数据
			for (PlayerRoleGamelevelData normalGameLevelData : srcLevelSet.normalLevelDataMap.values()) {
				myLevelSet.addOrModifyNormalGameLevelData(normalGameLevelData.getLevelId(), normalGameLevelData.getRemainJoinLevelCount(), normalGameLevelData.getLevelEvaluate(),
						normalGameLevelData.isCompleted());
			}

			// 普通关卡全通奖励数据
			myLevelSet.scenarioPriceRecordSet.addAll(srcLevelSet.scenarioPriceRecordSet);
			// 普通关卡全S奖励数据
			myLevelSet.scenarioSLevelPriceRecordSet.addAll(srcLevelSet.scenarioSLevelPriceRecordSet);

			// 是否开放战斗怒气槽
			if (srcLevelSet.isOpenBattlePowerSlot()) {
				myLevelSet.notifyOpenBattlePowerSlot();
			}

			myLevelSet.notifyUpdateLevelSet();
		}
	}

	private static void cloneRoleByCamus(KRole myRole, KRole srcRole) {
		// 货币、时装、物品、邮件、机甲、技能、VIP
		KSupportFactory.getCurrencySupport().cloneRoleByCamus(myRole, srcRole);
		KSupportFactory.getFashionModuleSupport().cloneRoleByCamus(myRole, srcRole);
		KSupportFactory.getItemModuleSupport().cloneRoleByCamus(myRole, srcRole);
		KSupportFactory.getMailModuleSupport().cloneRoleByCamus(myRole, srcRole);
		KSupportFactory.getMountModuleSupport().cloneRoleByCamus(myRole, srcRole);
		KSupportFactory.getSkillModuleSupport().cloneRoleByCamus(myRole, srcRole);
		KSupportFactory.getVIPModuleSupport().cloneRoleByCamus(myRole, srcRole);
	}

	private static String startWorldBoss() {
		try {
			Field field = KWorldBossActivityMain.class.getDeclaredField("_start");
			field.setAccessible(true);
			boolean start = field.getBoolean(KWorldBossManager.getWorldBossActivity());
			field.setAccessible(false);
			if (start) {
				return "世界boss正在进行中";
			}
			Calendar instance = Calendar.getInstance();
			instance.set(Calendar.HOUR_OF_DAY, 13);
			instance.set(Calendar.MINUTE, 0);
			instance.set(Calendar.SECOND, 0);
			instance.set(Calendar.MILLISECOND, 0);
			long timeA = instance.getTimeInMillis();
			instance.add(Calendar.HOUR_OF_DAY, 7);
			long timeB = instance.getTimeInMillis();
			long[][] times = new long[][] { { timeA, timeA + 1800000 }, { timeB, timeB + 1800000 } };
			long current = System.currentTimeMillis();
			boolean normalTime = false;
			for (int i = 0; i < times.length; i++) {
				if (current >= times[i][0] && current <= times[i][1]) {
					return "世界boss正在进行中";
				} else if (current < times[i][0] && current > (times[i][0] - 1800)) {
					normalTime = true;
					break;
				}
			}
			if (normalTime) {
				Field wheelField = KGameTimer.class.getDeclaredField("wheel");
				wheelField.setAccessible(true);
				Set<KGameTimeSignal>[] array = (Set<KGameTimeSignal>[]) wheelField.get(KGame.getTimer());
				Set<KGameTimeSignal> tempSet;
				KGameTimeSignal tempSignal;
				KGameTimerTask tempTask;
				boolean found = false;
				for (int i = 0; i < array.length; i++) {
					tempSet = array[i];
					for (Iterator<KGameTimeSignal> itr = tempSet.iterator(); itr.hasNext();) {
						tempSignal = itr.next();
						tempTask = tempSignal.getTask();
						if (tempTask.getClass().getName().contains("KWorldBossActivityMonitor")) {
							itr.remove();
							tempTask.onTimeSignal(tempSignal);
							found = true;
							break;
						}
					}
					if (found) {
						break;
					}
				}
				wheelField.setAccessible(false);
			} else {
				KGame.newTimeSignal(new KWorldBossTask(), 1, TimeUnit.SECONDS);
			}
			return "启动世界boss成功";
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	private static String startWorldBossNow() {
		try {
			Field field = KWorldBossActivityMain.class.getDeclaredField("_start");
			field.setAccessible(true);
			if (field.getBoolean(KWorldBossManager.getWorldBossActivity())) {
				field.setAccessible(false);
				return "世界boss正在进行中";
			} else {
				field.setAccessible(false);
				Method m = KWorldBossActivityMain.class.getDeclaredMethod("start", int.class);
				m.setAccessible(true);
				m.invoke(KWorldBossManager.getWorldBossActivity(), 0);
				m.setAccessible(false);
				return "启动世界boss成功";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	private static String terminateWorldBoss() {
		try {
			Field field = KWorldBossActivityMain.class.getDeclaredField("_leftSeconds");
			field.setAccessible(true);
			field.set(KWorldBossManager.getWorldBossActivity(), 0);
			field.setAccessible(false);
			return "终结世界boss活动成功！";
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static String endWorldBossField(int fieldId) {
		try {
			Field fHp = KWorldBossMonsterInfo.class.getDeclaredField("_currentHp");
			Class[] clazzs = KWorldBossActivityMain.class.getDeclaredClasses();
			Field mapField = null;
			Field dataField = null;
			Field fActivityList = KWorldBossActivityMain.class.getDeclaredField("_allActivityField");
			fActivityList.setAccessible(true);
			List list = (List) fActivityList.get(KWorldBossManager.getWorldBossActivity());
			for (int i = 0; i < clazzs.length; i++) {
				if (clazzs[i].getName().contains("KWorldBossActivityField")) {
					mapField = clazzs[i].getDeclaredField("_monsterInfos");
					dataField = clazzs[i].getDeclaredField("_worldBossFieldData");
					break;
				}
			}
			if (mapField != null) {
				fHp.setAccessible(true);
				mapField.setAccessible(true);
				dataField.setAccessible(true);
				KWorldBossFieldData fieldData;
				Object temp;
				for (int i = 0; i < list.size(); i++) {
					temp = list.get(i);
					fieldData = (KWorldBossFieldData) dataField.get(temp);
					if (fieldData.templateId == fieldId) {
						KWorldBossMonsterInfo tempInfo;
						AtomicInteger hp;
						Map<Integer, Map<Integer, KWorldBossMonsterInfo>> map = (Map<Integer, Map<Integer, KWorldBossMonsterInfo>>) mapField.get(temp);
						for (Iterator<Map<Integer, KWorldBossMonsterInfo>> itr = map.values().iterator(); itr.hasNext();) {
							for (Iterator<KWorldBossMonsterInfo> itr2 = itr.next().values().iterator(); itr2.hasNext();) {
								tempInfo = itr2.next();
								hp = (AtomicInteger) fHp.get(tempInfo);
								hp.set(0);
							}
						}
					}
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	private static String processOpenFunc(KRole role, String[] cmdArgs) {
		try {
			KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());
			Field field = KGuideManager.class.getDeclaredField("mainMenuFunctionInfoMap");
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			HashMap<Short, MainMenuFunction> map = (HashMap<Short, MainMenuFunction>) field.get(null);
			if (cmdArgs.length > 1) {
				int funcId = Integer.parseInt(cmdArgs[1]);
				List<MainMenuFunction> openFunList = new ArrayList<MainMenuFunction>();
				for (MainMenuFunction info : map.values()) {
					if (info.getFunctionId() == funcId) {
						if (missionSet.funtionMap.containsKey(info.getFunctionId()) && !missionSet.funtionMap.get(info.getFunctionId()).isOpen) {
							openFunList.add(info);

						} else if (!missionSet.funtionMap.containsKey(info.getFunctionId())) {
							openFunList.add(info);
						}
					}
				}
				if (openFunList.size() > 0) {
					KGameMessage sendMsg = KGame.newLogicMessage(KMissionProtocol.SM_OPEN_NEW_FUNCTION);
					sendMsg.writeInt(openFunList.size());
					for (MainMenuFunction info : openFunList) {
						sendMsg.writeShort(info.getFunctionId());
						sendMsg.writeBoolean(info.isOpenShowIcon());
					}
					sendMsg.writeBoolean(false);
					role.sendMsg(sendMsg);

					for (MainMenuFunction info : openFunList) {
						missionSet.addOrUpdateFunctionInfo(info.getFunctionId(), true, false);
					}
				}
			} else {
				List<MainMenuFunction> openFunList = new ArrayList<MainMenuFunction>();
				for (MainMenuFunction info : map.values()) {
					if (missionSet.funtionMap.containsKey(info.getFunctionId()) && !missionSet.funtionMap.get(info.getFunctionId()).isOpen) {
						openFunList.add(info);

					} else if (!missionSet.funtionMap.containsKey(info.getFunctionId())) {
						openFunList.add(info);
					}
				}
				if (openFunList.size() > 0) {
					KGameMessage sendMsg = KGame.newLogicMessage(KMissionProtocol.SM_OPEN_NEW_FUNCTION);
					sendMsg.writeInt(openFunList.size());
					for (MainMenuFunction info : openFunList) {
						sendMsg.writeShort(info.getFunctionId());
						sendMsg.writeBoolean(info.isOpenShowIcon());
					}
					sendMsg.writeBoolean(false);
					role.sendMsg(sendMsg);

					for (MainMenuFunction info : openFunList) {
						missionSet.addOrUpdateFunctionInfo(info.getFunctionId(), true, false);
					}
				}
			}
			field.setAccessible(false);
			return "执行成功";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	private static void testAddPetAndMountToAllOnlineRole() {
		List<KPetTemplate> petTemplateList = new ArrayList<KPetTemplate>();
		petTemplateList.addAll(KPetModuleManager.getAllPetTemplates().values());
		int petTemplateSize = petTemplateList.size();

		List<Long> onlineRoleIds = KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds();
		KRole role;
		for (Long roleId : onlineRoleIds) {
			role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (KSupportFactory.getPetModuleSupport().getFightingPet(roleId) == null) {
				KActionResult<Pet> result = KSupportFactory.getPetModuleSupport().createPetToRole(roleId, petTemplateList.get(UtilTool.random(petTemplateSize)).templateId, "宠物" + roleId);
				if (result != null && result.success && result.attachment != null) {
					Pet pet = result.attachment;
					setPetFollow(role, pet);
				}
			}
			
			KMountLogic.presentMountForLv(role);
		}
	}

	private static void setPetFollow(KRole role, Pet pet) {
		KPet targetPet = null;
		if (role != null) {
			String result = null;
			long lastFightingPetId = 0;
			KPetSet petSet = KSupportFactory.getPetModuleSupport().getPetSet(role.getId());
			long petId = 0;
			if (pet != null) {
				targetPet = (KPet) pet;
				petId = pet.getId();
				int subLv = targetPet.getLevel() - role.getLevel();
				if (subLv > KPetModuleConfig.getPetMaxLvGreatThanRole()) {
					result = PetTips.getTipsPetIsStrongerThanRole();
				} else if (!targetPet.isFighting()) {
					KPet currentPet = petSet.getFightingPet();
					if (currentPet != null) {
						lastFightingPetId = currentPet.getId();
						currentPet.setFightingStatus(false);
					}
					targetPet.setFightingStatus(true);
					petSet.updateFightingPet(targetPet.getId());

				}
			} else {
				result = PetTips.getTipsNoSuchPet();
			}
			if (result != null) {
				KDialogService.sendDataUprisingDialog(role, result);
			} else {
				KGameMessage msg = KGame.newLogicMessage(SM_NOTIFY_PET_FLOW_SUCCESS);
				msg.writeLong(petId);
				msg.writeLong(lastFightingPetId);
				role.sendMsg(msg);
				KDialogService.sendUprisingDialog(role, PetTips.getTipsPetFlowSuccess());
				KSupportFactory.getRankModuleSupport().notifyPetInfoChange(role, targetPet.getName(), targetPet.getLevel(), targetPet.getAttributeByType(KGameAttrType.BATTLE_POWER));
			}
		}
	}

	static class KMonsterUpdateHpInfo implements ICombatMonsterUpdateInfo {

		int instanceId;
		int templateId;
		long currentHp;

		KMonsterUpdateHpInfo(int pInstanceId, int pTemplateId, long pCurrentHp) {
			this.instanceId = pInstanceId;
			this.templateId = pTemplateId;
			this.currentHp = pCurrentHp;
		}

		@Override
		public int getInstanceId() {
			return instanceId;
		}

		@Override
		public int getTemplateId() {
			return templateId;
		}

		@Override
		public long getCurrentHp() {
			return currentHp;
		}

	}

	static class KEnhanceInfoImpl implements ICombatEnhanceInfo {

		@Override
		public int getAtkInc() {
			return 100;
		}

		@Override
		public int getDefInc() {
			return 100;
		}

		@Override
		public int getAtkPctInc() {
			return 6000;
		}

		@Override
		public int getDefPctInc() {
			return 6000;
		}

	}

	static class KCreateRoleTask implements KGameTimerTask {

		private static final Logger _FLOW_LOGGER = KGameLogger.getLogger("pressLogger");
		
		private static final int PlayerTask = 10;//任务数量15
		private static final int PEROID_MIN = 1;//任务周期，秒
		private static final int PEROID_MAX = 3;//任务周期，秒
		
		private static final int PlayerCountPerTask = 1000;//每个任务跑多少个帐号100
		private static final int MAX_ROLE_PER_PERIOD= 100;//每次执行任务最多随机处理多少个角色
		
		static void testCreateRole(long beginId) {
			for (int i = 0; i < PlayerTask; i++) {
				KGame.getTimer().newTimeSignal(new KCreateRoleTask(beginId, i), 5, TimeUnit.SECONDS);
			}
		}

		private long _beginPlayerId;
		private long _endPlayerId;
		private boolean _creating = true;
		private final List<Long> _roleIds = new ArrayList<Long>();
		private List<KItemTempAbs> itemTemps;
		private final int[] petTemplateIds = new int[] { 700001, 700002, 700003, 700004, 700005, 700007 };
		// private static String _nameFormat = "momoko{}";

		KCreateRoleTask(long pBeginPlayerId, int taskIndex) {
			this._beginPlayerId = pBeginPlayerId + PlayerCountPerTask*taskIndex;
			this._endPlayerId = this._beginPlayerId+PlayerCountPerTask;
			_LOGGER.info("提交时效任务，beginPlayerId={}，endPlayerId={}", _beginPlayerId, _endPlayerId);
			//
			itemTemps = KItemDataManager.mItemTemplateManager.getItemTemplateList();
		}

		@Override
		public String getName() {
			return "KCreateRoleTask";
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal timeSignal) throws KGameServerException {
			if (_creating) {
				for (long playerId = _beginPlayerId; playerId < _endPlayerId; playerId++) {
					int ran = UtilTool.random(1, 3);
					try {
						if(RoleModuleFactory.getRoleModule().getRoleList(playerId).size()>0){
							continue;
						}
						String name = "test_"+playerId;//KRandomNameManager.getRandomName(playerId, ran == 2 ? KGameGender.FEMALE.sign : KGameGender.MALE.sign);
						Role role = RoleModuleFactory.getRoleModule().createRole(playerId, name, ran, ran);
						KMissionModuleExtension.getMissionCompleteRecordSet(role.getId()).getNoviceGuideRecord();
						_roleIds.add(role.getId());
						_FLOW_LOGGER.info("创建角色成功，playerId:{}, roleId:{}, roleName:{}", playerId, role.getId(), role.getName());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				_creating = false;
			} else {
				try {
					List<Long> copy = new ArrayList<Long>(_roleIds);
					for (int i = 0; i < MAX_ROLE_PER_PERIOD && copy.size() > 0; i++) {
						Long roleId = copy.remove(UtilTool.randomWithoutMax(0, copy.size()));
						KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
						int action = UtilTool.random(7);
						switch (action) {
						case 0:
							int money = UtilTool.random(50, 100);
							long result = KSupportFactory.getCurrencySupport().increaseMoney(roleId, KCurrencyTypeEnum.GOLD, money, PresentPointTypeEnum.GM或指令操作, false);
							_FLOW_LOGGER.info("角色：{}，加金币，数量：{}，剩余货币：{}", role.getName(), money, result);
							break;
						case 1:
							KItemLogic.dealMsg_extendBagVolume(role, 1);
							
							KItemTempAbs temp = itemTemps.get(UtilTool.random(itemTemps.size()));
							int addCount = temp.isCanStack()?UtilTool.randomWithoutMax(1, 5):UtilTool.random(2);
							if(addCount>0){
								ItemResult_AddItem addResult = KSupportFactory.getItemModuleSupport().addItemToBag(role, temp.itemCode, addCount, "指令");
								_FLOW_LOGGER.info("角色：{}，加道具：{}，数量：{}，是否成功：{}", role.getName(), temp.name, addCount, addResult.isSucess);
							}
							break;
						case 2:
							KItemSet set = KItemModuleExtension.getItemSet(role.getId());
							List<KItem> items=set.getBag().copyAllItems();
							if(!items.isEmpty()){
								int index = UtilTool.random(items.size()*2);
								if(index<items.size()){
									KItem item = items.get(index);
									boolean isSuccess = KSupportFactory.getItemModuleSupport().removeItemFromBag(roleId, item.getId(), 1);
									_FLOW_LOGGER.info("角色：{}，减道具：{}，数量：{}，是否成功：{}", role.getName(), item.getItemTemplate().name, 1, isSuccess);
								}
							}
							break;
						case 3:
							int value = KSupportFactory.getRoleModuleSupport().getUpgradeExp(role.getLevel()) / 20;
							if (value < 20) {
								value = 20;
							}
							int addExp = role.addExp(value, KRoleAttrModifyType.其他);
							_FLOW_LOGGER.info("角色：{}，加经验，数量：{}，最后加的数量：{}", role.getName(), value, addExp);
							break;
						case 4:
							List<KPet> allPets = KSupportFactory.getPetModuleSupport().getAllPets(role.getId());
							int actionRandom = UtilTool.random(100);
							if (allPets.size() > 1) {
								actionRandom -= allPets.size() * 5;
							} else {
								actionRandom += 51;
							}
							if (actionRandom > 50) {
								int templateId = petTemplateIds[UtilTool.randomWithoutMax(0, petTemplateIds.length)];
								KActionResult<Pet> addPetResult = KSupportFactory.getPetModuleSupport().createPetToRole(roleId, templateId, KPetFlowType.测试.name());
								_FLOW_LOGGER.info("角色：{}，加随从，模板：{}，是否成功：{}", role.getName(), templateId, addPetResult.success);
							} else {
								KPet pet = allPets.get(0);
								KPet target = allPets.get(1);
								KPetSet ps = KSupportFactory.getPetModuleSupport().getPetSet(role.getId());
								ps.deletePet(target.getId());
								pet.addExp(target.getBeComposedExp(), true, role.getLevel());
								_FLOW_LOGGER.info("角色：{}，吞噬随从，srcId：{}，targetId:{}，uuid：{}，exp：{}", role.getName(), pet.getId(), target.getId(), target.getUUID(), target.getBeComposedExp());
							}
							break;
						case 5:
							KMailSet mailSet = KMailModuleExtension.getMailSet(role.getId());
							List<KMail> mailList = mailSet.getAllMailsCopy();
							boolean addOrDelete = true;
							int mailSize = mailList.size();
							if(mailSize>0 && mailSize<10){
								addOrDelete = UtilTool.randomNextBoolean();
							}else if(mailSize>=10){
								addOrDelete = false;
							}
							if(addOrDelete){
								KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(role.getId(), "测试邮件", "测试邮件内容...");
							} else{
								long mailId = mailList.get(UtilTool.random(mailSize))._id;
								KMailLogic.dealMsg_deleteMail(role, mailId);
							}
							break;	
						case 6:
							KGameLevelSet levelSet = KGameLevelModuleExtension.getGameLevelSet(role.getId());
							
							List<Integer> levelIdList = new ArrayList<Integer>();
							levelIdList.addAll(KGameLevelModuleExtension.getManager().allKGameLevel.keySet());
							int levelId = levelIdList.get(UtilTool.random(levelIdList.size()));
							KLevelTemplate level = KGameLevelModuleExtension.getManager().allKGameLevel.get(levelId);
							
							levelSet.addOrModifyNormalGameLevelData(levelId, 0, (byte)UtilTool.random(1,5), true);	
							break;	
						}
					}
				} catch (Exception e) {
					_LOGGER.error("执行出现异常！", e);
				}
			}
			timeSignal.getTimer().newTimeSignal(this, UtilTool.random(PEROID_MIN, PEROID_MAX), TimeUnit.SECONDS);
			return Boolean.TRUE;
		}

		@Override
		public void done(KGameTimeSignal timeSignal) {

		}

		@Override
		public void rejected(RejectedExecutionException e) {

		}

	}

	static class KWorldBossTask implements KGameTimerTask {

		private boolean _warnUp = true;

		@Override
		public String getName() {
			return "KWorldBossTask";
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal timeSignal) throws KGameServerException {
			if (_warnUp) {
				System.out.println("！！！！世界boss预热开始！！！！");
				try {
					_warnUp = false;
					Method m = KWorldBossManager.getWorldBossActivity().getClass().getDeclaredMethod("warnUp", int.class, int.class);
					m.setAccessible(true);
					m.invoke(KWorldBossManager.getWorldBossActivity(), 180, 0);
					m.setAccessible(false);
					timeSignal.getTimer().newTimeSignal(this, 180, TimeUnit.SECONDS);
					System.out.println(StringUtil.format("！！！！世界boss将于{}秒后开始！！！！", 180));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("！！！！世界boss正式开始！！！！");
				try {
					Method m = KWorldBossManager.getWorldBossActivity().getClass().getDeclaredMethod("start", int.class);
					m.setAccessible(true);
					m.invoke(KWorldBossManager.getWorldBossActivity(), 0);
					m.setAccessible(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return true;
		}

		@Override
		public void done(KGameTimeSignal timeSignal) {

		}

		@Override
		public void rejected(RejectedExecutionException e) {

		}

	}
}
