package com.kola.kmp.logic.gm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.dataaccess.KGameDBException;
import com.koala.game.dataaccess.dbobj.flowdata.DBChargeRecord;
import com.koala.game.dataaccess.dbobj.flowdata.DBFunPointConsumeRecord;
import com.koala.game.dataaccess.dbobj.flowdata.DBPresentPointRecord;
import com.koala.game.dataaccess.dbobj.flowdata.DBShopSellItemRecord;
import com.koala.game.player.KGamePlayer;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.player.KGameSimpleRoleInfo;
import com.koala.game.util.StringUtil;
import com.koala.paymentserver.PayExtParam;
import com.koala.paymentserver.PayOrder;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kgame.cache.role.RoleBaseInfo;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kgame.db.dataaccess.DataAccesserFactory;
import com.kola.kgame.db.dataobject.DBRoleData;
import com.kola.kmp.logic.competition.KCompetitor;
import com.kola.kmp.logic.currency.KPaymentListener;
import com.kola.kmp.logic.fashion.KFashionMsgPackCenter;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangMsgPackCenter;
import com.kola.kmp.logic.gm.dbquery.DBQueryManager;
import com.kola.kmp.logic.item.KItem;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.mail.KMail;
import com.kola.kmp.logic.mail.KMailConfig;
import com.kola.kmp.logic.mail.KMailMsgPackCenter;
import com.kola.kmp.logic.mount.KMountMsgPackCenter;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.other.KRankTypeEnum;
import com.kola.kmp.logic.pet.IPetSkill;
import com.kola.kmp.logic.pet.KPet;
import com.kola.kmp.logic.pet.KPetTemplate;
import com.kola.kmp.logic.rank.KRankMsgPackCenter;
import com.kola.kmp.logic.rank.gang.KGangRankMsgPackCenter;
import com.kola.kmp.logic.rank.gang.KGangRankTypeEnum;
import com.kola.kmp.logic.role.IRoleBaseInfo;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleIniSkillTemp;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRolePasSkillTemp;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.talent.ITalent;
import com.kola.kmp.logic.util.ResultStructs.GMResult;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;
import com.kola.kmp.protocol.gm.ProtocolGs;
//import com.kola.kmp.logic.gang.old.KGameFamily;
//import com.kola.kmp.logic.gang.old.rank.KFamilyRankMsgPackCenter;
//import com.kola.kmp.logic.gang.old.rank.KGangRankType;

class GMOrderCenter implements ProtocolGs {

	// 指令=chargeFlow;type;角色key;startTime;endTime 表示查充值流水
	private final static String order_chargeFlow = "chargeFlow";
	// 指令=precentFlow;type;角色key;startTime;endTime 表示查赠点流水
	private final static String order_presentFlow = "presentFlow";
	// 指令=consumeItem;type;角色key;startTime;endTime 表示查道具消费流水
	private final static String order_consumeItem = "consumeItem";
	// 指令=consumeFun;type;角色key;startTime;endTime 表示查功能消费流水
	private final static String order_consumeFun = "consumeFun";

	// 指令=rank;1 表示查类型1的排行榜
	private final static String order_rank = "rank";
	// 指令=familyList;10;2 表示以10个家族为一页，获取第2页的家族列表
	private final static String order_familyList = "familyList";
	// 指令=familyWarList;10;2 表示以10个家族为一页，获取第2页的家族战列表
	private final static String order_familyWarList = "familyWarList";
	// 指令=familySci;家族ID 表示获取家族的科技列表
	private final static String order_familySci = "familySci";
	// 指令=familyMem;家族ID 表示获取家族的成员列表
	private final static String order_familyMem = "familyMem";

	// 指令=searchRole;角色key 表示模糊搜索角色
	private final static String order_searchRole = "searchRole";
	// 指令=playerInfo;type;角色key 表示获取指定玩家的信息
	private final static String order_playerInfo = "playerInfo";
	private final static String order_playerInfo_roleId = "roleId";
	private final static String order_playerInfo_roleName = "roleNmae";
	private final static String order_playerInfo_playerId = "playerId";
	private final static String order_playerInfo_playerName = "playerName";
	// 指令=roleInfo;type;角色key 表示获取指定角色的信息
	private final static String order_roleInfo = "roleInfo";
	// 指令=petList;type;角色key 表示获取指定角色的道具列表
	private final static String order_itemList = "itemList";
	// 指令=mailList;type;角色key 表示获取指定角色的邮件列表
	public final static String order_mailList = "mailList";
	// 指令=petList;type;角色key 表示获取指定角色的神将列表
	private final static String order_petList = "petList";
	// 指令=petInfo;type;角色key;神将ID 表示获取指定角色的神将信息
	private final static String order_petInfo = "petInfo";
	// 指令=itemInfo;type;角色key;道具ID 表示获取指定角色的道具信息
	private final static String order_itemInfo = "itemInfo";
	// 指令=modifyRole;type;角色key;属性类型KEY;属性值 表示获取修改角色的属性信息
	private final static String order_modifyRole = "modifyRole";
	// 指令=addItem;type;角色key;ItemCode;数量 表示添加道具
	private final static String order_addItem = "addItem";
	// 指令=changeItemCount;type;角色key;ItemId;数量 表示修改道具数量
	private final static String order_changeItemCount = "changeItemCount";
	// 指令=itemTemplates 表示获取所有道具模板
	private final static String order_itemTemplates = "itemTemplates";
	// 指令=modifyEqui;type;角色key;ItemId;属性类型;属性值 表示修改装备属性
	private final static String order_modifyEqui = "modifyEqui";
	// 指令=closeSession;type;角色key 表示踢下线
	private final static String order_closeSession = "closeSession";
	// 指令=joinCompetition;type;角色key 表示加入到竞技场最后一名
	private final static String order_joinCompetition = "joinCompetition";
	
	// 指令=sendChat;type;角色key;发送内容 表示要向对应角色发送私聊
	public final static String order_sendChat = "sendChat";
	// 指令=sendWorldChat;GM所用名称;发送内容 表示要向世界频道发送聊天
	public final static String order_sendWorldChat = "sendWorldChat";
	// 指令=sendOnlineMail;最小等级;最大等级;邮件标题;发送内容 表示要全体在线玩家发送邮件
	public final static String order_sendOnlineMail = "sendOnlineMail";
	// 指令=sendOnlineMail;角色名单(,号分隔);邮件标题;发送内容 表示要指定玩家发送邮件
	public final static String order_sendMails = "sendMails";

	// 指令=moodPic;type;角色key;心情ID 表示获取指定心情图片
	private final static String order_moodPic = "moodPic";
	// 指令=replaceMoodPic;type;角色key;心情ID;替换图ID 表示替换指定心情图片（替换图ID=0表示解封，-1表示禁止显示）
	private final static String order_replaceMoodPic = "replaceMoodPic";
	
	private static final String _DATA_FORMAT = "{}\t{}";

	static void dealGMOrder(KGamePlayerSession session, String gmc, String order) {
		String[] orders = order.split(";");

		if (orders[0].equals(order_moodPic)) {
			GMResult result = getMoodPic(orders[1], orders[2], orders[3]);
			KGameMessage outmsg = KGame.newLogicMessage(GS_GMS_TCP_QUERY_UNION_RESULT);
			outmsg.writeUtf8String(gmc);
			outmsg.writeInt(KGame.getGSID());
			outmsg.writeUtf8String(order);
			if (result.data == null) {
				outmsg.writeBoolean(false);
				outmsg.writeUtf8String(result.tips);
			} else {
				outmsg.writeBoolean(true);
				outmsg.writeBoolean(result.tips != null);// 是否被封
				outmsg.writeBytes(result.data);
			}
			session.send(outmsg);
		} else if (orders[0].equals(order_itemTemplates)) {
			Map<String, List<String>> result = getAllItemTemplates();
			KGameMessage outMsg = KGame.newLogicMessage(GS_GMS_TCP_QUERY_UNION_RESULT);
			outMsg.writeUtf8String(gmc);
			outMsg.writeInt(KGame.getGSID());
			outMsg.writeUtf8String(order);
			{
				if (result == null || result.isEmpty()) {
					outMsg.writeInt(0);
				} else {
					outMsg.writeInt(result.size());
					for (Entry<String, List<String>> entry : result.entrySet()) {
						outMsg.writeUtf8String(entry.getKey());
						outMsg.writeInt(entry.getValue().size());
						for (String str : entry.getValue()) {
							outMsg.writeUtf8String(str);
						}
					}
				}
			}
			session.send(outMsg);
		} else {

			List<String> result = null;
			if (orders[0].equals(order_rank)) {
				int rankType = Integer.parseInt(orders[1]);
				result = getRankInfo(rankType);
			} else if (orders[0].equals(order_familyList)) {
				int numPerPage = Integer.parseInt(orders[1]);
				int page = Integer.parseInt(orders[2]);
				result = getFamilyList(numPerPage, page);
			} else if (orders[0].equals(order_familyWarList)) {
				int numPerPage = Integer.parseInt(orders[1]);
				int page = Integer.parseInt(orders[2]);
				result = getFamilyWarList(numPerPage, page);
			} else if (orders[0].equals(order_familySci)) {
				long familyId = Long.parseLong(orders[1]);
				result = getFamilySecAndTec(familyId);
			} else if (orders[0].equals(order_familyMem)) {
				long familyId = Long.parseLong(orders[1]);
				result = getFamilyMem(familyId);
			} else if (orders[0].equals(order_playerInfo)) {
				result = getPlayerInfo(orders[1], orders[2]);
			} else if (orders[0].equals(order_chargeFlow)) {
				result = getChargeFlow(orders[1], orders[2], orders[3], orders[4]);
			} else if (orders[0].equals(order_presentFlow)) {
				result = getPresentFlow(orders[1], orders[2], orders[3], orders[4]);
			} else if (orders[0].equals(order_consumeItem)) {
				result = getItemConsumeFlow(orders[1], orders[2], orders[3], orders[4]);
			} else if (orders[0].equals(order_consumeFun)) {
				result = getFunConsumeFlow(orders[1], orders[2], orders[3], orders[4]);
			} else if (orders[0].equals(order_roleInfo)) {
				result = getRoleInfo(orders[1], orders[2]);
			} else if (orders[0].equals(order_itemList)) {
				result = getItemList(orders[1], orders[2]);
			} else if (orders[0].equals(order_mailList)) {
				result = getMailList(orders[1], orders[2]);
			} else if (orders[0].equals(order_petList)) {
				result = getPetList(orders[1], orders[2]);
			} else if (orders[0].equals(order_petInfo)) {
				result = getPetInfo(orders[1], orders[2], orders[3]);
			} else if (orders[0].equals(order_itemInfo)) {
				result = getItemInfo(orders[1], orders[2], orders[3]);
			} else if (orders[0].equals(order_replaceMoodPic)) {
				result = replaceMoodPic(orders[1], orders[2], orders[3], orders[4]);
			} else if (orders[0].equals(order_modifyRole)) {
				result = modifyRole(orders[1], orders[2], orders[3], orders[4]);
			} else if (orders[0].equals(order_addItem)) {
				result = addItem(orders[1], orders[2], orders[3], orders[4]);
			} else if (orders[0].equals(order_changeItemCount)) {
				result = changeItemCount(orders[1], orders[2], orders[3], orders[4]);
			} else if (orders[0].equals(order_modifyEqui)) {
				result = modifyEqui(orders[1], orders[2], orders[3], orders[4], orders[5]);
			} else if (orders[0].equals(order_closeSession)) {
				result = closeSession(orders[1], orders[2]);
			} else if (orders[0].equals(order_joinCompetition)) {
				result = joinCompetition(orders[1], orders[2]);
			} else if (orders[0].equals(order_sendChat)) {
				result = sendPrivateChat(orders[1], orders[2], orders[3]);	
			} else if (orders[0].equals(order_sendWorldChat)) {
				result = sendChat(orders[1], orders[2]);	
			} else if (orders[0].equals(order_sendOnlineMail)) {
				result = sendOnlineMail(orders[1], orders[2], orders[3], orders[4]);	
			} else if (orders[0].equals(order_sendMails)) {
				result = sendMails(orders[1], orders[2], orders[3]);	
			} else if (orders[0].equals(order_searchRole)) {
				result = searchRoles(orders[1]);
			} else {
				result = Collections.emptyList();
			}
			sendBackQueryData(session, gmc, order, result);
		}
	}

	private static void sendBackQueryData(KGamePlayerSession session, String gmc, String order, List<String> result) {
		KGameMessage msg = KGame.newLogicMessage(GS_GMS_TCP_QUERY_UNION_RESULT);

		msg.writeUtf8String(gmc);
		msg.writeInt(KGame.getGSID());
		msg.writeUtf8String(order);
		//
		msg.writeInt(result.size());
		for (String info : result) {
			msg.writeUtf8String(info);
		}
		session.send(msg);
	}

	private static List<String> getRoleInfo(String type, String key) {
		List<String> infos = new ArrayList<String>();
		infos.add("项目" + '\t' + "数据1" + '\t' + "数据2" + '\t' + "数据3");

		StringBuffer sbf = new StringBuffer();

		KRole role = null;
		if (type.equals(order_playerInfo_roleId)) {
			long roleId = Long.parseLong(key);
			role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else if (type.equals(order_playerInfo_roleName)) {
			role = KSupportFactory.getRoleModuleSupport().getRole(key);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else {
			infos.add("type错误！type=" + type);
			return infos;
		}

		// 角色列表：角色名，性别，等级，经验，职业，主角战斗力，VIP，VIP经验，竞技场排名，所属家族名称，角色创建时间、最后登录登出时间
		// 角色详细属性，技能列表， 机甲列表，时装列表，天赋列表，货币列表（可修改）
		// 独立查询：道具、装备、邮件、随从
		{
			infos.add("帐号ID" + '\t' + role.getPlayerId());
			infos.add("角色ID" + '\t' + role.getId());
			infos.add("角色名" + '\t' + role.getName());
			infos.add("等级" + '\t' + role.getLevel());
			infos.add("等级经验" + '\t' + role.getCurrentExp());
			infos.add("职业" + '\t' + KJobTypeEnum.getJobName(role.getJob()));
			infos.add("主角战力" + '\t' + role.getBattlePower());
			infos.add("VIP" + '\t' + KSupportFactory.getVIPModuleSupport().getVipLv(role.getId()));
			infos.add("VIP经验" + '\t' + KSupportFactory.getVIPModuleSupport().getVipExp(role.getId()));
			infos.add("");
			KGang gang = KSupportFactory.getGangSupport().getGangByRoleId(role.getId());
			if (gang == null) {
				infos.add("军团" + '\t' + "");
			} else {
				infos.add("军团" + '\t' + gang.getName());
			}
			infos.add("");
			infos.add("创建时间" + '\t' + UtilTool.DATE_FORMAT2.format(new Date(role.getCreateTime())));
			infos.add("最后登录时间" + '\t' + UtilTool.DATE_FORMAT2.format(new Date(role.getLastJoinGameTime())));
			infos.add("最后登出时间" + '\t' + UtilTool.DATE_FORMAT2.format(new Date(role.getLastLeaveGameTime())));
		}
		{
			infos.add("");
			KCompetitor competitor = KSupportFactory.getCompetitionModuleSupport().getCompetitor(role.getId());
			if (competitor == null) {
				infos.add("竞技排名" + '\t' + "未上榜");
			} else {
				sbf.append("竞技排名" + '\t' + competitor.getRanking());
			}
		}

		{// 角色详细属性
			infos.add("");
			infos.add("角色详细属性");
			infos.add("【属性名称】" + '\t' + "【属性值】" + '\t' + "【属性名称】" + '\t' + "【属性值】");
			String temp = null;
			for (KGameAttrType att : KGameAttrType.values()) {
				int value = role.getAttributeByType(att);
				if (value > 0) {
					if (temp == null) {
						temp = att.getName() + '\t' + value;
					} else {
						temp += ('\t' + att.getName() + '\t' + value);
						infos.add(temp);
						temp = null;
					}
				}
			}
			if (temp != null) {
				infos.add(temp);
			}
		}
		{// 技能列表
			infos.add("");
			infos.add("主动技能");
			infos.add("【技能ID】" + '\t' + "【技能名称】" + '\t' + "【描述】" + '\t' + "【使用中】");
			List<Integer> skillIdList = KSupportFactory.getSkillModuleSupport().getRoleAllIniSkills(role.getId());
			Set<Integer> inUsedId = new HashSet<Integer>(KSupportFactory.getSkillModuleSupport().getRoleInUseIniSkills(role.getId()));
			for (int skillId : skillIdList) {
				KRoleIniSkillTemp temp = KSupportFactory.getSkillModuleSupport().getRoleIniSkillTemplate(skillId);
				infos.add(skillId + "" + '\t' + temp.name + '\t' + temp.state + '\t' + (inUsedId.contains(skillId) ? "是" : ""));
			}
			infos.add("被动技能");
			infos.add("【技能ID】" + '\t' + "【技能名称】" + '\t' + "【描述】");
			skillIdList = KSupportFactory.getSkillModuleSupport().getRoleAllPasSkills(role.getId());
			for (int skillId : skillIdList) {
				KRolePasSkillTemp temp = KSupportFactory.getSkillModuleSupport().getRolePasSkillTemplate(skillId);
				infos.add(skillId + "" + '\t' + temp.name + '\t' + temp.state);
			}
		}
		{// 机甲
			infos.add("");
			infos.add("机甲");
			KMountMsgPackCenter.packMountDataForGM(role, infos);
		}
		{// 时装列表
			infos.add("");
			infos.add("时装列表");
			KFashionMsgPackCenter.packFashionDataForGM(role, infos);
		}
		{// 天赋列表
			infos.add("");
			infos.add("天赋列表");
			infos.add("【天赋树】" + '\t' + "【天赋点】" + '\t' + "【天赋值】");
			Map<String, List<ITalent>> map = KSupportFactory.getTalentSupport().getAllTalentData(role.getId());
			for(Entry<String, List<ITalent>> entry:map.entrySet()){
				infos.add(entry.getKey());
				for(ITalent t:entry.getValue()){
					for(Entry<KGameAttrType, Integer> e:t.getEffectAttr().entrySet()){
						infos.add('\t' + e.getKey().getName() + '\t' + e.getValue());
					}
				}
			}
		}
		{// 货币列表
			// 货币一定要放在最后
			infos.add("");
			infos.add("【货币类型】" + '\t' + "【货币名称】" + '\t' + "【货币数量】");
			for (KCurrencyTypeEnum currencyType : KCurrencyTypeEnum.values()) {
				infos.add(currencyType.sign + "" + '\t' + currencyType.name + '\t' + KSupportFactory.getCurrencySupport().getMoney(role.getId(), currencyType));
			}
		}

		return infos;
	}

	/**
	 * <pre>
	 * 【账号信息】：
	 * 账号ID，渠道号、渠道识别码、注册时间、最后登录登出时间、在线时长
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-24 下午3:55:32
	 * </pre>
	 */
	private static List<String> getPlayerInfo(String type, String key) {
		List<String> infos = new ArrayList<String>();
		infos.add("项目" + '\t' + "数据1" + '\t' + "数据2" + '\t' + "数据3");

		KGamePlayer player = null;
		if (type.equals(order_playerInfo_roleId)) {
			long roleId = Long.parseLong(key);
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
			player = KGame.getPlayerManager().loadPlayerData(role.getPlayerId());

		} else if (type.equals(order_playerInfo_roleName)) {
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(key);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
			player = KGame.getPlayerManager().loadPlayerData(role.getPlayerId());
		} else if (type.equals(order_playerInfo_playerId)) {
			player = KGame.getPlayerManager().loadPlayerData(Long.parseLong(key));
		} else if (type.equals(order_playerInfo_playerName)) {
			player = KGame.getPlayerManager().loadPlayerData(key);
		} else {
			infos.add("type错误！type=" + type);
			return infos;
		}

		if (player == null) {
			infos.add("找不到此帐号:" + key);
			return infos;
		}

		infos.add("帐号ID" + '\t' + player.getID());
		infos.add("帐号名" + '\t' + player.getPlayerName());
		infos.add("Password" + '\t' + player.getPassword());
		infos.add("");
		infos.add("PromoID" + '\t' + player.getPromoID());
		infos.add("ParentPromoID" + '\t' + player.getParentPromoID());
		infos.add("PromoMask" + '\t' + player.getPromoMask());
		infos.add("PromoRemark" + '\t' + player.getPromoRemark());
		infos.add("");
		infos.add("登陆总次数" + '\t' + player.getTotalLoginCount());
		infos.add("最后登陆时间" + '\t' + UtilTool.DATE_FORMAT2.format(new Date(player.getLastestLoginTimeMillis())));
		infos.add("最后登出时间" + '\t' + UtilTool.DATE_FORMAT2.format(new Date(player.getLastestLogoutTimeMillis())));
		infos.add("");
		infos.add("最后登陆区号" + '\t' + player.getLastLoginedGSID());
		infos.add("最后登陆机型" + '\t' + player.getLastLoginDeviceModel());
		infos.add("");
		infos.add("封号解封时间" + '\t' + UtilTool.DATE_FORMAT2.format(new Date(player.getBanEndtime())));
		infos.add("禁言解封时间" + '\t' + UtilTool.DATE_FORMAT2.format(new Date(player.getGagEndtime())));
		infos.add("是否已充值" + '\t' + !player.isFirstCharge());

		List<IRoleBaseInfo> dbRoleList = KSupportFactory.getRoleModuleSupport().getRoleList(player.getID());
		infos.add("");
		infos.add("现有角色数量" + '\t' + dbRoleList.size());
		infos.add("【角色ID】" + '\t' + "【角色名】" + '\t' + "【职业】" + '\t' + "【等级】");
		if (dbRoleList != null || !dbRoleList.isEmpty()) {
			for (RoleBaseInfo role : dbRoleList) {
				infos.add(role.getId() + "" + '\t' + role.getName() + '\t' + KJobTypeEnum.getJobName((byte) role.getType()) + '\t' + role.getLevel());
			}
		}

		infos.add("");
		infos.add("创建角色总量" + '\t' + player.getCreateRoleSize());
		infos.add("本区历史角色列表" + '\t' + "【职业】" + '\t' + "【等级】");
		List<KGameSimpleRoleInfo> roleList = player.getRoleSimpleInfo4GsListShow(KGame.getGSID());
		for (KGameSimpleRoleInfo info : roleList) {
			infos.add('\t' + KJobTypeEnum.getJobName((byte) info.roleJobType) + '\t' + info.roleLV);
		}
		// 缺少 注册时间、在线时长
		return infos;
	}

	/**
	 * <pre>
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-24 下午3:55:32
	 * </pre>
	 */
	private static List<String> sendPrivateChat(String type, String key, String content) {
		List<String> infos = new ArrayList<String>();

		KRole role = null;
		if (type.equals(order_playerInfo_roleId)) {
			long roleId = Long.parseLong(key);
			role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		} else if (type.equals(order_playerInfo_roleName)) {
			role = KSupportFactory.getRoleModuleSupport().getRole(key);
		}
		
		if (role == null) {
			infos.add("找不到此角色！key=" + key);
			return infos;
		}
		
		if(content.isEmpty()){
			infos.add("请输入内容");
			return infos;
		}
		
		
		KSupportFactory.getChatSupport().sendChatToRole(content, role.getId());
		
		if(role.isOnline()){
			infos.add("发送成功");
		} else {
			infos.add("发送成功（角色不在线，可能收不到）");
		}
		return infos;
	}
	
	private static List<String> sendChat(String gmName, String content) {
		List<String> infos = new ArrayList<String>();

		if(content.isEmpty()){
			infos.add("请输入内容");
			return infos;
		}
		
		
		int count = KSupportFactory.getChatSupport().sendChatToWorldChannel(gmName, content);
		
		infos.add("发送成功（接收人数=" + count + "）");
		return infos;
	}
	
	/**
	 * <pre>
	 * 
	 * 
	 * @param minLvStr
	 * @param maxLvStr
	 * @param title
	 * @param content
	 * @return
	 * @author CamusHuang
	 * @creation 2014-11-3 上午10:32:06
	 * </pre>
	 */
	private static List<String> sendOnlineMail(String minLvStr, String maxLvStr, String title, String content) {
		List<String> infos = new ArrayList<String>();

		int minLv = 0;
		int maxLv = 0;
		{
			try{
				minLv = Integer.parseInt(minLvStr);
			} catch(Exception e){
				infos.add("最小等级输入错误!");
				return infos;
			}
			if(minLv<1){
				infos.add("最小等级输入错误!");
				return infos;
			}
			
			try{
				maxLv = Integer.parseInt(maxLvStr);
			} catch(Exception e){
				infos.add("最大等级输入错误!");
				return infos;
			}
			if(maxLv>100){
				infos.add("最大等级输入错误!");
				return infos;
			}
			if(maxLv<minLv){
				infos.add("最大等级输入错误!");
				return infos;
			}
		}
		
		if(title.isEmpty()){
			infos.add("请输入标题");
			return infos;
		}
		
		if(content.isEmpty()){
			infos.add("请输入内容");
			return infos;
		}
		
		
		Map<Long, KMail> sendResult = KSupportFactory
				.getMailModuleSupport().sendSimpleMailToOnlineBySystem(minLv, maxLv, title, content);
		
		infos.add("发送成功（接收人数=" + sendResult.size() + "）");
		return infos;
	}
	
	/**
	 * <pre>
	 * 角色名单(,号分隔);邮件标题;发送内容
	 * 
	 * @param gmName
	 * @param content
	 * @return
	 * @author CamusHuang
	 * @creation 2014-11-3 上午10:32:16
	 * </pre>
	 */
	private static List<String> sendMails(String nameList, String title, String content) {
		List<String> infos = new ArrayList<String>();

		if(nameList.isEmpty()){
			infos.add("请输入名单");
			return infos;
		}
		RoleModuleSupport roleSupport = KSupportFactory
				.getRoleModuleSupport();
		
		Set<Long> receiverRoleIds = new HashSet<Long>();
		
		StringBuffer sbf = new StringBuffer();
		for(String name:nameList.split(",")){
			KRole  role  = roleSupport.getRole(name);
			if(role==null){
				sbf.append(name).append('、');
				continue;
			}
			receiverRoleIds.add(role.getId());
		}
		
		if(sbf.length()>0){
			infos.add("以下角色不存在:"+sbf.toString());
			return infos;
		}
		
		if(receiverRoleIds.isEmpty()){
			infos.add("请输入名单");
			return infos;
		}
		
		
		if(title.isEmpty()){
			infos.add("请输入标题");
			return infos;
		}
		
		if(content.isEmpty()){
			infos.add("请输入内容");
			return infos;
		}
		

		Map<Long, KMail> sendResult = KSupportFactory.getMailModuleSupport().sendGroupSimpleMail(
						KMailConfig.SYS_MAIL_SENDER_ID,
						KMailConfig.SYS_MAIL_SENDER_NAME, receiverRoleIds,
						KMail.TYPE_SYSTEM, title, content);
		
		infos.add("发送成功（接收人数=" + sendResult.size() + "）");
		return infos;
	}	
	
	/**
	 * <pre>
	 * 【排行榜】
	 * 查看各排行榜数据
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-24 下午3:55:32
	 * </pre>
	 */
	private static List<String> getRankInfo(int type) {

		List<String> infos = new ArrayList<String>();

		if (type >= 0) {
			// 角色榜
			KRankTypeEnum rankType = KRankTypeEnum.getEnum(type);
			if (rankType == null) {
				infos.add("找不到此排行榜:" + type);
				return infos;
			}
			KRankMsgPackCenter.packRanksForGM(infos, rankType);
		} else {
			// 军团榜
			type = Math.abs(type);
			KGangRankTypeEnum rankType = KGangRankTypeEnum.getEnum(type);
			if (rankType == null) {
				infos.add("找不到此排行榜:" + type);
				return infos;
			}
			KGangRankMsgPackCenter.packRanksForGM(infos, rankType);
		}
		return infos;
	}

	/**
	 * <pre>
	 * 【家族】
	 * 家族列表
	 * 
	 * 排名，名称，族长，等级，人数，经验，资金
	 * 
	 * @param numPerPage
	 * @param page
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-24 下午6:03:35
	 * </pre>
	 */
	private static List<String> getFamilyList(int numPerPage, int page) {
		List<String> infos = new ArrayList<String>();
		KGangMsgPackCenter.packGangListForGM(infos, (short) numPerPage, (short) page);
		return infos;
	}

	/**
	 * <pre>
	 * 家族科技列表
	 * 
	 * @param familyId
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-24 下午6:16:37
	 * </pre>
	 */
	private static List<String> getFamilySecAndTec(long familyId) {
		List<String> infos = new ArrayList<String>();
		KGangMsgPackCenter.packGangSecAndTecForGM(infos, familyId);
		return infos;
	}

	private static List<String> getFamilyMem(long familyId) {
		List<String> infos = new ArrayList<String>();
		KGangMsgPackCenter.packGangMemListForGM(infos, familyId);
		return infos;
	}

	private static List<String> getFamilyWarList(int numPerPage, int page) {
		List<String> infos = new ArrayList<String>();
		// KGangMsgPackCenter.packGangWarListForGM(infos, numPerPage, page);
		return infos;
	}

	private static List<String> getChargeFlow(String type, String key, String startTime, String endTime) {
		List<String> infos = new ArrayList<String>();

		StringBuffer sbf = new StringBuffer();
		sbf.append("时间").append('\t').append("充值点数").append('\t').append("首充").append('\t').append("Lv").append('\t').append("卡号").append('\t').append("密码").append('\t').append("充值类型").append('\t')
				.append("子渠道").append('\t').append("父渠道").append('\t').append("通道").append('\t').append("描述");
		infos.add(sbf.toString());

		KRole role = null;
		if (type.equals(order_playerInfo_roleId)) {
			long roleId = Long.parseLong(key);
			role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else if (type.equals(order_playerInfo_roleName)) {
			role = KSupportFactory.getRoleModuleSupport().getRole(key);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else {
			infos.add("type错误！type=" + type);
			return infos;
		}

		long startTime1 = Long.parseLong(startTime);
		long endTime1 = Long.parseLong(endTime);
		List<DBChargeRecord> list = DBQueryManager.getInstance().queryDBChargeRecord(role.getId(), startTime1, endTime1);

		if (list == null || list.isEmpty()) {
			infos.add("没有相关流水");
			return infos;
		}

		int gsId = KGame.getGSID();
		for (DBChargeRecord record : list) {
//			if (record.server_id != gsId) {
//				continue;
//			}
			sbf = new StringBuffer();
			sbf.append(UtilTool.DATE_FORMAT2.format(new Date(record.charge_time))).append('\t').append(record.charge_point).append('\t').append(record.is_first_charge == 1).append('\t')
					.append(record.role_level).append('\t').append(record.card_num).append('\t').append(record.card_password).append('\t').append(record.charge_type).append('\t')
					.append(record.promo_id).append('\t').append(record.parent_promo_id).append('\t').append(record.channel_id).append('\t').append(record.desc);

			infos.add(sbf.toString());
		}

		return infos;
	}

	private static List<String> getPresentFlow(String type, String key, String startTime, String endTime) {
		List<String> infos = new ArrayList<String>();
		StringBuffer sbf = new StringBuffer();
		sbf.append("时间").append('\t').append("赠送点数").append('\t').append("描述").append('\t').append("类型ID").append('\t').append("类型名称");
		infos.add(sbf.toString());

		KRole role = null;
		if (type.equals(order_playerInfo_roleId)) {
			long roleId = Long.parseLong(key);
			role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else if (type.equals(order_playerInfo_roleName)) {
			role = KSupportFactory.getRoleModuleSupport().getRole(key);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else {
			infos.add("type错误！type=" + type);
			return infos;
		}

		long startTime1 = Long.parseLong(startTime);
		long endTime1 = Long.parseLong(endTime);
		List<DBPresentPointRecord> list = DBQueryManager.getInstance().queryDBPresentPointRecord(role.getId(), startTime1, endTime1);

		if (list == null || list.isEmpty()) {
			infos.add("没有相关流水");
			return infos;
		}

		int gsId = KGame.getGSID();
		for (DBPresentPointRecord record : list) {
			if (record.server_id != gsId) {
				continue;
			}

			PresentPointTypeEnum enuma = PresentPointTypeEnum.getEnum(record.type);
			sbf = new StringBuffer();
			sbf.append(UtilTool.DATE_FORMAT2.format(new Date(record.present_time))).append('\t').append(record.present_point).append('\t').append(record.desc).append('\t').append(record.type)
					.append('\t').append(enuma == null ? "-" : enuma.name());

			infos.add(sbf.toString());
		}

		return infos;
	}

	private static List<String> getItemConsumeFlow(String type, String key, String startTime, String endTime) {
		List<String> infos = new ArrayList<String>();

		StringBuffer sbf = new StringBuffer();
		sbf.append("流水ID").append('\t').append("时间").append('\t').append("点数").append('\t').append("道具ID").append('\t').append("ItemCode").append('\t').append("道具名称").append('\t').append("数量")
				.append('\t').append("子渠道").append('\t').append("父渠道").append('\t').append("描述");
		infos.add(sbf.toString());

		KRole role = null;
		if (type.equals(order_playerInfo_roleId)) {
			long roleId = Long.parseLong(key);
			role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else if (type.equals(order_playerInfo_roleName)) {
			role = KSupportFactory.getRoleModuleSupport().getRole(key);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else {
			infos.add("type错误！type=" + type);
			return infos;
		}

		long startTime1 = Long.parseLong(startTime);
		long endTime1 = Long.parseLong(endTime);
		List<DBShopSellItemRecord> list = DBQueryManager.getInstance().queryDBShopSellItemRecord(role.getId(), startTime1, endTime1);

		if (list == null || list.isEmpty()) {
			infos.add("没有相关流水");
			return infos;
		}

		for (DBShopSellItemRecord record : list) {
			KItemTempAbs temp = KSupportFactory.getItemModuleSupport().getItemTemplate(record.itemCode);
			sbf = new StringBuffer();
			sbf.append(record.id).append('\t').append(UtilTool.DATE_FORMAT2.format(new Date(record.consumeTime))).append('\t').append(record.consumePoint).append('\t').append(record.itemId)
					.append('\t').append(record.itemCode).append('\t').append(temp == null ? "" : temp.name).append('\t').append(record.count).append('\t').append(record.promoId).append('\t')
					.append(record.parentPromoId).append('\t').append(record.desc);

			infos.add(sbf.toString());
		}

		return infos;
	}

	private static List<String> getFunConsumeFlow(String type, String key, String startTime, String endTime) {
		List<String> infos = new ArrayList<String>();

		StringBuffer sbf = new StringBuffer();
		sbf.append("流水ID").append('\t').append("时间").append('\t').append("点数").append('\t').append("描述").append('\t').append("类型ID").append('\t').append("类型名称").append('\t').append("子渠道")
				.append('\t').append("父渠道");
		infos.add(sbf.toString());

		KRole role = null;
		if (type.equals(order_playerInfo_roleId)) {
			long roleId = Long.parseLong(key);
			role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else if (type.equals(order_playerInfo_roleName)) {
			role = KSupportFactory.getRoleModuleSupport().getRole(key);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else {
			infos.add("type错误！type=" + type);
			return infos;
		}

		long startTime1 = Long.parseLong(startTime);
		long endTime1 = Long.parseLong(endTime);
		List<DBFunPointConsumeRecord> list = DBQueryManager.getInstance().queryDBFunPointConsumeRecord(role.getId(), startTime1, endTime1);

		if (list == null || list.isEmpty()) {
			infos.add("没有相关流水");
			return infos;
		}

		for (DBFunPointConsumeRecord record : list) {
			sbf = new StringBuffer();
			sbf.append(record.id).append('\t').append(UtilTool.DATE_FORMAT2.format(new Date(record.consumeTime))).append('\t').append(record.consumePoint).append('\t').append(record.desc)
					.append('\t').append(record.funType).append('\t');

			UsePointFunctionTypeEnum enuma = UsePointFunctionTypeEnum.getEnum(record.funType);
			if (enuma == null) {
				sbf.append("-");
			} else {
				sbf.append(enuma.name());
			}

			sbf.append('\t').append(record.promoId).append('\t').append(record.parentPromoId);

			infos.add(sbf.toString());
		}

		return infos;
	}

	private static List<String> getItemList(String type, String key) {
		List<String> infos = new ArrayList<String>();

		KRole role = null;
		if (type.equals(order_playerInfo_roleId)) {
			long roleId = Long.parseLong(key);
			role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else if (type.equals(order_playerInfo_roleName)) {
			role = KSupportFactory.getRoleModuleSupport().getRole(key);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else {
			infos.add("type错误！type=" + type);
			return infos;
		}

		KItemMsgPackCenter.packItemListForGM(infos, role.getId());
		return infos;
	}
	
	private static List<String> getMailList(String type, String key) {
		List<String> infos = new ArrayList<String>();

		KRole role = null;
		if (type.equals(order_playerInfo_roleId)) {
			long roleId = Long.parseLong(key);
			role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else if (type.equals(order_playerInfo_roleName)) {
			role = KSupportFactory.getRoleModuleSupport().getRole(key);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else {
			infos.add("type错误！type=" + type);
			return infos;
		}

		KMailMsgPackCenter.packAllMailsForGM(infos, role.getId());
		return infos;
	}	

	private static List<String> getPetList(String type, String key) {
		List<String> infos = new ArrayList<String>();
		infos.add("模板ID" + '\t' + "模板名称" + '\t' +"ID" + '\t' + "显示名称" + '\t' + "等级" + '\t' + "出战");

		KRole role = null;
		if (type.equals(order_playerInfo_roleId)) {
			long roleId = Long.parseLong(key);
			role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else if (type.equals(order_playerInfo_roleName)) {
			role = KSupportFactory.getRoleModuleSupport().getRole(key);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else {
			infos.add("type错误！type=" + type);
			return infos;
		}

		List<KPet> pets = KSupportFactory.getPetModuleSupport().getAllPets(role.getId());
		if (pets == null || pets.isEmpty()) {
			return infos;
		}
		long warPetId = -1;
		{
			KPet pet = KSupportFactory.getPetModuleSupport().getFightingPet(role.getId());
			if (pet != null) {
				warPetId = pet.getId();
			}
		}

		for (KPet pet : pets) {
			KPetTemplate petTemplate = KSupportFactory.getPetModuleSupport().getPetTemplate(pet.getTemplateId());
			infos.add((petTemplate==null?-1:petTemplate.templateId)+ "" + '\t' + (petTemplate==null?"":petTemplate.defaultName) + '\t' + pet.getId() + "" + '\t' + pet.getName() + '\t' + pet.getLevel() + '\t' + (warPetId == pet.getId() ? "出战" : "休息"));
		}

		return infos;
	}

	private static List<String> getPetInfo(String type, String roleKey, String petKey) {
		List<String> infos = new ArrayList<String>();
		infos.add("项目" + '\t' + "数据1" + '\t' + "数据2" + '\t' + "数据3");

		KRole role = null;
		if (type.equals(order_playerInfo_roleId)) {
			long roleId = Long.parseLong(roleKey);
			role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role == null) {
				infos.add(StringUtil.format("找不到此角色！key={}",roleKey));
				return infos;
			}
		} else if (type.equals(order_playerInfo_roleName)) {
			role = KSupportFactory.getRoleModuleSupport().getRole(roleKey);
			if (role == null) {
				infos.add(StringUtil.format("找不到此角色！key={}", roleKey));
				return infos;
			}
		} else {
			infos.add(StringUtil.format("type错误！type={}", type));
			return infos;
		}
//
		long petId = Long.parseLong(petKey);

		KPet pet = KSupportFactory.getPetModuleSupport().getPet(role.getId(), petId);
		if (pet == null) {
			infos.add("随从不存在！");
			return infos;
		}
		
		KPetTemplate petTemplate = KSupportFactory.getPetModuleSupport().getPetTemplate(pet.getTemplateId());
//
		infos.add(StringUtil.format(_DATA_FORMAT, "模板ID", petTemplate==null?-1:petTemplate.templateId));
		infos.add(StringUtil.format(_DATA_FORMAT, "模板名称", petTemplate==null?"":petTemplate.defaultName));
		infos.add(StringUtil.format(_DATA_FORMAT, "ID", pet.getId()));
		infos.add(StringUtil.format(_DATA_FORMAT, "显示名称", pet.getName()));
		infos.add(StringUtil.format(_DATA_FORMAT, "等级", pet.getLevel()));
		infos.add(StringUtil.format(_DATA_FORMAT, "类型", pet.getPetType().getTypeName()));
		infos.add(StringUtil.format(_DATA_FORMAT, "攻击", pet.getAtkType().getName()));
//		infos.add("进阶" + '\t' + "");
//		infos.add("轮回" + '\t' + pet.getSamsaraName());
//		infos.add("战力" + '\t' + pet.getBattlePower());
//		//
		infos.add("【增加主人属性】");
		infos.add(StringUtil.format(_DATA_FORMAT, KGameAttrType.MAX_HP.getName(), pet.getAttributeByType(KGameAttrType.MAX_HP)));
		infos.add(StringUtil.format(_DATA_FORMAT, KGameAttrType.ATK.getName(), pet.getAttributeByType(KGameAttrType.ATK)));
		infos.add(StringUtil.format(_DATA_FORMAT, KGameAttrType.DEF.getName(), pet.getAttributeByType(KGameAttrType.DEF)));
		infos.add(StringUtil.format(_DATA_FORMAT, KGameAttrType.HIT_RATING.getName(), pet.getAttributeByType(KGameAttrType.HIT_RATING)));
		infos.add(StringUtil.format(_DATA_FORMAT, KGameAttrType.DODGE_RATING.getName(), pet.getAttributeByType(KGameAttrType.DODGE_RATING)));
		infos.add(StringUtil.format(_DATA_FORMAT, KGameAttrType.CRIT_RATING.getName(), pet.getAttributeByType(KGameAttrType.DODGE_RATING)));
		infos.add(StringUtil.format(_DATA_FORMAT, KGameAttrType.RESILIENCE_RATING.getName(), pet.getAttributeByType(KGameAttrType.RESILIENCE_RATING)));
//		if (pet.isPhysical()) {
//			infos.add("法击" + '\t' + pet.getPhyDamage());
//		} else {
//			infos.add("法击" + '\t' + pet.getMagicDamage());
//		}
//		infos.add("物防" + '\t' + pet.getPhyDefence());
//		infos.add("法防" + '\t' + pet.getMagicDefence());
//
//		infos.add("闪避" + '\t' + pet.getDodge());
//		infos.add("暴击" + '\t' + pet.getCritical());
//		infos.add("命中" + '\t' + pet.getHitrate());
//		infos.add("韧性" + '\t' + pet.getTenacity());
//		//
//		infos.add("【资质】");
//		infos.add("生命" + '\t' + pet.getAptHp() + "/100");
//		infos.add("攻击" + '\t' + pet.getAptDm() + "/100");
//		infos.add("物防" + '\t' + pet.getAptPhyDef() + "/100");
//		infos.add("法防" + '\t' + pet.getAptMagicDef() + "/100");
//
//		infos.add("闪避" + '\t' + pet.getAptDodge() + "/100");
//		infos.add("暴击" + '\t' + pet.getAptCritical() + "/100");
//		infos.add("命中" + '\t' + pet.getAptHitrate() + "/100");
//		infos.add("韧性" + '\t' + pet.getAptTenacity() + "/100");
//
//		infos.add("【技能】");
//		List<IPetSkill> skills = pet.getAllSkills();
//		for (IPetSkill skill : skills) {
//			KGameSkillTemplate temp = KSupportFactory.getSkillSupport().getSkillTemplate(skill.getSkillTemplateId());
//			if (temp != null) {
//				infos.add(skill.getSkillTemplateId() + "" + '\t' + skill.getSkillName() + '\t' + temp.desc);
//				continue;
//			}
//
//			KGamePassiveSkillTemplate ptemp = KSupportFactory.getSkillSupport().getPassiveSkillTemplate(skill.getSkillTemplateId());
//			if (ptemp != null) {
//				infos.add(skill.getSkillTemplateId() + "" + '\t' + skill.getSkillName() + '\t' + ptemp.desc);
//				continue;
//			}
//
//			infos.add(skill.getSkillTemplateId() + "" + '\t' + skill.getSkillName() + '\t' + "");
//		}
		infos.add("");
		
		List<IPetSkill> list = pet.getSkillList();
		infos.add("【主动技能】");
		for(IPetSkill petSkill: list) {
			if(petSkill.isActiveSkill()){
				KRoleIniSkillTemp skillTemp = KSupportFactory.getSkillModuleSupport().getPetSkillTemplate(petSkill.getSkillTemplateId());
				infos.add(petSkill.getSkillTemplateId() + "" + '\t' + skillTemp.name + '\t' + skillTemp.state + '\t' + petSkill.getLv() );
			}
		}
		infos.add("");
		infos.add("【被动技能】");
		for(IPetSkill petSkill: list) {
			if(!petSkill.isActiveSkill()){
				KRolePasSkillTemp skillTemp = KSupportFactory.getSkillModuleSupport().getPetPasSkillTemplate(petSkill.getSkillTemplateId());
				infos.add(petSkill.getSkillTemplateId() + "" + '\t' + skillTemp.name + '\t' + skillTemp.state + '\t' + petSkill.getLv() + "级");
			}
		}
		
		return infos;
	}

	private static List<String> getItemInfo(String type, String key, String key2) {
		List<String> infos = new ArrayList<String>();

		KRole role = null;
		if (type.equals(order_playerInfo_roleId)) {
			long roleId = Long.parseLong(key);
			role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else if (type.equals(order_playerInfo_roleName)) {
			role = KSupportFactory.getRoleModuleSupport().getRole(key);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else {
			infos.add("type错误！type=" + type);
			return infos;
		}

		KItemMsgPackCenter.packItemForGM(infos, role.getId(), Long.parseLong(key2));
		return infos;
	}

	private static GMResult getMoodPic(String type, String key, String moodId) {
		// GMResult result = new GMResult();
		//
		// KRole role = null;
		// if (type.equals(order_playerInfo_roleId)) {
		// long roleId = Long.parseLong(key);
		// role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		// if (role == null) {
		// result.tips = "找不到此角色！key=" + key;
		// return result;
		// }
		// } else if (type.equals(order_playerInfo_roleName)) {
		// role = KSupportFactory.getRoleModuleSupport().getRole(key);
		// if (role == null) {
		// result.tips = "找不到此角色！key=" + key;
		// return result;
		// }
		// } else {
		// result.tips = "type错误！type=" + type;
		// return result;
		// }
		// // long ownerRoleId, byte imageType, long imageId

		// return KGameKZoneModuleManager.getMoodPicData(role.getId(),
		// Long.parseLong(moodId));
		return null;
	}

	/**
	 * <pre>
	 * type;角色key;心情ID;替换图ID
	 * 
	 * @param type
	 * @param key
	 * @param picId
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-27 下午2:53:43
	 * </pre>
	 */
	private static List<String> replaceMoodPic(String type, String key, String moodId, String picId) {

		List<String> infos = new ArrayList<String>();

		// KRole role = null;
		// if (type.equals(order_playerInfo_roleId)) {
		// long roleId = Long.parseLong(key);
		// role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		// if (role == null) {
		// infos.add("找不到此角色！key=" + key);
		// return infos;
		// }
		// } else if (type.equals(order_playerInfo_roleName)) {
		// role = KSupportFactory.getRoleModuleSupport().getRole(key);
		// if (role == null) {
		// infos.add("找不到此角色！key=" + key);
		// return infos;
		// }
		// } else {
		// infos.add("type错误！type=" + type);
		// return infos;
		// }
		//
		// infos.add(KGameKZoneModuleManager.replaceMoodPic(role.getId(),
		// Long.parseLong(moodId), Integer.parseInt(picId)));
		return infos;
	}

	private static List<String> modifyRole(String type, String key, String modifyType, String data) {
		// 指令=modifyRole;type;角色key;属性类型KEY;属性值 表示获取修改角色的属性信息
		// private final static String order_modifyRole = "modifyRole";
		List<String> infos = new ArrayList<String>();

		KRole role = null;
		if (type.equals(order_playerInfo_roleId)) {
			long roleId = Long.parseLong(key);
			role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else if (type.equals(order_playerInfo_roleName)) {
			role = KSupportFactory.getRoleModuleSupport().getRole(key);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else {
			infos.add("type错误！type=" + type);
			return infos;
		}

		if (modifyType.equals("charge")) {
			PayExtParam payExt = new PayExtParam(-1, 1, role.getId());
			int fen = Integer.parseInt(data) * 100;
			PayOrder payOrder = new PayOrder(payExt, "orderId", fen + "", "promoMask", "GM", "2013-07-01 12:59", "GM工具");
			KPaymentListener.instance.dealPayOrderForTest(payOrder);
			infos.add("");
			infos.add(KCurrencyTypeEnum.DIAMOND.name);
			infos.add(KSupportFactory.getCurrencySupport().getMoney(role.getId(), KCurrencyTypeEnum.DIAMOND) + "");
			return infos;
		} else {
			String[] datas = data.split(",");
			KCurrencyTypeEnum moneytype = KCurrencyTypeEnum.getEnum(Byte.parseByte(datas[0]));
			if (moneytype == null) {
				infos.add("修改类型错误！");
				return infos;
			} else {
				long value = Long.parseLong(datas[1]);
				if (value < 0) {
					if (KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), moneytype, -value, UsePointFunctionTypeEnum.GM指令, true) < 0) {
						infos.add("修改" + moneytype.name + "失败！");
						return infos;
					}
				} else {
					KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), moneytype, value, PresentPointTypeEnum.GM或指令操作, true);
				}

				infos.add("");// ""表示成功
				infos.add(moneytype.name);
				infos.add(KSupportFactory.getCurrencySupport().getMoney(role.getId(), moneytype) + "");
				return infos;
			}
		}
	}

	private static List<String> addItem(String type, String key, String itemCode, String count) {
		List<String> infos = new ArrayList<String>();

		KRole role = null;
		if (type.equals(order_playerInfo_roleId)) {
			long roleId = Long.parseLong(key);
			role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else if (type.equals(order_playerInfo_roleName)) {
			role = KSupportFactory.getRoleModuleSupport().getRole(key);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else {
			infos.add("type错误！type=" + type);
			return infos;
		}

		ItemResult_AddItem result = KSupportFactory.getItemModuleSupport().addItemToBag(role, itemCode, Integer.parseInt(count), "GM");
		if (result.isSucess) {
			infos.add("");
			return infos;
		} else {
			infos.add(result.tips);
			return infos;
		}
	}

	private static List<String> changeItemCount(String type, String key, String itemId, String count) {
		List<String> infos = new ArrayList<String>();

		KRole role = null;
		if (type.equals(order_playerInfo_roleId)) {
			long roleId = Long.parseLong(key);
			role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else if (type.equals(order_playerInfo_roleName)) {
			role = KSupportFactory.getRoleModuleSupport().getRole(key);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else {
			infos.add("type错误！type=" + type);
			return infos;
		}

		int countInt = Integer.parseInt(count);
		if (countInt == 0) {
			infos.add("不能输入0！");
			return infos;
		}

		KItem item = KItemLogic.getItem(role.getId(), Long.parseLong(itemId));
		if (item == null) {
			infos.add("道具不存在！");
			return infos;
		}

		long orgCount = item.getCount();
		if (countInt > 0) {
			// 增加
			ItemCountStruct itemStruct = new ItemCountStruct(item.getItemTemplate(), countInt);
			ItemResult_AddItem result = KSupportFactory.getItemModuleSupport().addItemToBag(role, itemStruct, "GM");
			if (result.isSucess) {
				infos.add("修改成功");
				infos.add(itemId);
				infos.add(item.getCount() + "");
				return infos;
			} else {
				infos.add(result.tips);
				return infos;
			}
		} else {
			// 减少
			countInt = -countInt;
			boolean isSuccess = KSupportFactory.getItemModuleSupport().removeItemFromBag(role.getId(), item.getId(), countInt);
			if (!isSuccess) {
				infos.add("修改失败");
				return infos;
			} else {
				infos.add("修改成功");
				infos.add(itemId);
				infos.add(orgCount + countInt + "");
				return infos;
			}
		}
	}

	private static Map<String, List<String>> getAllItemTemplates() {
		return KItemMsgPackCenter.packAllItemTemplatesForGM();
	}

	private static List<String> modifyEqui(String type, String key, String itemId, String modifyType, String data) {
		List<String> infos = new ArrayList<String>();

		KRole role = null;
		if (type.equals(order_playerInfo_roleId)) {
			long roleId = Long.parseLong(key);
			role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else if (type.equals(order_playerInfo_roleName)) {
			role = KSupportFactory.getRoleModuleSupport().getRole(key);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else {
			infos.add("type错误！type=" + type);
			return infos;
		}

		if (modifyType.equals("strong")) {
			String tips = KItemLogic.setStrongLvForGM(role, Long.parseLong(itemId), Integer.parseInt(data));
			infos.add(tips);
			return infos;
		} else {
			infos.add("修改类型错误！");
			return infos;
		}
	}

	private static List<String> closeSession(String type, String key) {
		List<String> infos = new ArrayList<String>();

		KRole role = null;
		if (type.equals(order_playerInfo_roleId)) {
			long roleId = Long.parseLong(key);
			role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else if (type.equals(order_playerInfo_roleName)) {
			role = KSupportFactory.getRoleModuleSupport().getRole(key);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else {
			infos.add("type错误！type=" + type);
			return infos;
		}

		KGamePlayerSession session = KGame.getPlayerSession(role.getPlayerId());
		if (session == null) {
			infos.add("玩家不在线！key=" + key);
			return infos;
		}

		session.close();
		infos.add("操作完成！key=" + key);
		return infos;
	}

	private static List<String> joinCompetition(String type, String key) {
		List<String> infos = new ArrayList<String>();

		KRole role = null;
		if (type.equals(order_playerInfo_roleId)) {
			long roleId = Long.parseLong(key);
			role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else if (type.equals(order_playerInfo_roleName)) {
			role = KSupportFactory.getRoleModuleSupport().getRole(key);
			if (role == null) {
				infos.add("找不到此角色！key=" + key);
				return infos;
			}
		} else {
			infos.add("type错误！type=" + type);
			return infos;
		}

		infos.add(KSupportFactory.getCompetitionModuleSupport().joinCompetitionForGM(role.getId()));
		return infos;
	}

	/**
	 * <pre>
	 * 模糊搜索角色名称
	 * 
	 * @param roleName
	 * @return
	 * @author CamusHuang
	 * @creation 2013-10-23 下午8:40:49
	 * </pre>
	 */
	private static List<String> searchRoles(String roleName) {
		List<String> infos = new ArrayList<String>();

		StringBuffer sbf = new StringBuffer();
		sbf.append("playerId").append('\t').append("roleId").append('\t').append("角色名").append('\t').append("等级");
		infos.add(sbf.toString());

		// 进行模糊搜索
		try {
			List<DBRoleData> roles = DataAccesserFactory.getRoleDataAccesser().fuzzySearchPlayerRoles(roleName);
			for (DBRoleData role2 : roles) {
				if (role2 == null) {
					continue;
				}
				sbf = new StringBuffer();
				sbf.append(role2.getPlayerId()).append('\t').append(role2.getId()).append('\t').append(role2.getName()).append('\t').append(role2.getLevel());
				infos.add(sbf.toString());
			}

		} catch (KGameDBException e) {
			infos.add(e.getMessage());
		}

		return infos;
	}
}
