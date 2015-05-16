package com.kola.kmp.logic.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangExtCASet;
import com.kola.kmp.logic.gang.KGangMember;
import com.kola.kmp.logic.gang.KGangPositionEnum;
import com.kola.kmp.logic.gang.reswar.ResWarCity;
import com.kola.kmp.logic.gang.reswar.ResWarCity.ResPoint;
import com.kola.kmp.logic.item.KItem;
import com.kola.kmp.logic.item.KItemAttributeProvider;
import com.kola.kmp.logic.item.KItemConfig;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiBuyEnchansePrice;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.mail.KMail;
import com.kola.kmp.logic.mail.message.KPushMailsMsg;
import com.kola.kmp.logic.mount.KMount;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountTemplate;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.EquiSetStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGangTecTypeEnum;
import com.kola.kmp.logic.other.KRelationShipTypeEnum;
import com.kola.kmp.logic.reward.garden.KRoleGarden.TreeData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.shop.random.KRandomShopTypeEnum;
import com.kola.kmp.logic.skill.KSkill;
import com.kola.kmp.logic.util.tips.ShopTips;

/**
 * <pre>
 * 用于比较复杂的消息处理返回结果定义
 * 
 * @author camus
 * @creation 2012-12-14 下午11:45:43
 * </pre>
 */
public class ResultStructs {

	public static class CommonResult {
		public boolean isSucess;
		public String tips;
	}

	public static class CommonResult_Ext extends CommonResult {

		public boolean isGoVip;// 打开VIP界面
		public String showVipTips; // 打开界面提示
		
		
		public boolean isGoMoneyUI;// 打开货币界面
		public String showMoneyTips; // 打开界面提示
		public KCurrencyTypeEnum goMoneyUIType;// 钻石则打开充值界面、金币则打开兑换界面
		public long goMoneyUICount;// 缺少的货币数量
		//
		private List<String> uprisingTips;
		private List<String> dataUprisingTips;

		//
		public void addUprisingTips(String tips) {
			if (tips == null) {
				return;
			}
			getUprisingTips().add(tips);
		}

		public void addUprisingTips(Collection<String> tips) {
			if (tips == null) {
				return;
			}
			getUprisingTips().addAll(tips);
		}

		public void addDataUprisingTips(String tips) {
			if (tips == null) {
				return;
			}
			getDataUprisingTips().add(tips);
		}

		public void addDataUprisingTips(Collection<String> tips) {
			if (tips == null) {
				return;
			}
			getDataUprisingTips().addAll(tips);
		}

		public List<String> getUprisingTips() {
			if (uprisingTips == null) {
				uprisingTips = new ArrayList<String>(4);
			}
			return uprisingTips;
		}

		public List<String> getDataUprisingTips() {
			if (dataUprisingTips == null) {
				dataUprisingTips = new ArrayList<String>(4);
			}
			return dataUprisingTips;
		}

		public void doFinally(KRole role) {
			if (role == null) {
				return;
			}
			if (isGoMoneyUI) {
				
				if(showMoneyTips==null || showMoneyTips.isEmpty()){
					if (goMoneyUIType == KCurrencyTypeEnum.DIAMOND) {
						showMoneyTips = ShopTips.您的钻石不足是否前去充值;
					} else if (goMoneyUIType == KCurrencyTypeEnum.GOLD) {
						showMoneyTips = ShopTips.您的金币不足是否前去兑换;
					}
				}
				
				if (goMoneyUIType == KCurrencyTypeEnum.DIAMOND) {
					KDialogService.showChargeDialog(role.getId(), showMoneyTips);
				} else if (goMoneyUIType == KCurrencyTypeEnum.GOLD) {
					KDialogService.showExchangeDialog(role.getId(), showMoneyTips, goMoneyUICount);
				}
			}

			if (isGoVip) {
				KDialogService.showVIPDialog(role.getId(), showVipTips);
			}
			
			// 优化：如果tips数量较多，且没有显示其它DIALOG，则转成DIALOG显示
//			if(uprisingTips.size() < 3 || isGoMoneyUI || isGoVip){
//				KDialogService.sendUprisingDialog(role, uprisingTips);
//			} else {
//				StringBuffer sbf = new StringBuffer();
//				for(String str:uprisingTips){
//					sbf.append(str).append('\n');
//				}
//				sbf.deleteCharAt(sbf.length()-1);
//				KDialogService.sendSimpleDialog(role, "", sbf.toString());
//			}
			
			KDialogService.sendUprisingDialog(role, uprisingTips);
			KDialogService.sendDataUprisingDialog(role, dataUprisingTips);
		}
	}

	public static class ItemResult_Equi extends CommonResult_Ext {
		public Long notifySlotId;// !=null表示需要通知角色模块
		public EquiSetResult isSetChange;// 套装是否发生变化

		public void doFinally(KRole role) {
			if (role == null) {
				return;
			}
			super.doFinally(role);
			// 通知角色模块
			if (notifySlotId != null && notifySlotId == KItemConfig.MAIN_BODYSLOT_ID) {
				// 同步套装数据
				KItemLogic.synEquiSetData(role, isSetChange);
				// 刷新角色属性
				KItemAttributeProvider.notifyEffectAttrChange(role);
			}
		}
	}

	/**
	 * <pre>
	 * 
	 * @deprecated
	 * @author CamusHuang
	 * @creation 2015-1-27 下午4:38:28
	 * </pre>
	 */
	public static class ItemResult_EquiStrongOld extends ItemResult_Equi {
		public int strongAddLv;
		public int successTime;
		public KCurrencyTypeEnum payType;
		public long payCount;
		//
		public String itemExtName;
		public int orgStrongLv;
		public int nowStrongLv;
	}
	
	public static class ItemResult_EquiStrongIn extends CommonResult_Ext {
		public boolean isBreak;// 是否需要跳过所有强化
		public int strongAddLv;
		public KCurrencyTypeEnum payType;
		public long payCount;
	}
	
	public static class ItemResult_EquiStrong extends ItemResult_Equi {
		public int strongAddLv;
		public int successTime;
		public Set<String> fullStrongEquiNames=new HashSet<String>();
	}

	public static class ItemResult_EquiUpStar extends ItemResult_Equi {
		public int starChange;// 0表示等级不变，1表示升级，-1表示降级
		//
		public String itemName;
		public int orgStarLv;
		public int nowStarLv;
	}

	public static class EquiSetResult {
		public EquiSetStruct oldSet;// {升星套装等级,宝石套装等级,强化套装等级}
		public EquiSetStruct newSet;// {升星套装等级,宝石套装等级,强化套装等级}
		public int[] setForBroast;// 已经世界广播的等级{升星套装等级,宝石套装等级,强化套装等级}
		public boolean isSetChange;
		public boolean isStarSetUp;
		public boolean isStoneSetUp;
		public boolean isStrongSetUp;
		public boolean isQuaSetChange;

		public EquiSetResult() {
		}

		public EquiSetResult(EquiSetStruct oldSet, EquiSetStruct newSet, int[] setForBroast) {
			this.oldSet = oldSet;
			this.newSet = newSet;
			this.setForBroast = setForBroast;
			if (newSet.starSetLv > oldSet.starSetLv) {
				isStarSetUp = true;
			}
			if (newSet.stoneSetLv > oldSet.stoneSetLv) {
				isStoneSetUp = true;
			}
			if (newSet.strongSetLv > oldSet.strongSetLv) {
				isStrongSetUp = true;
			}
			if (newSet.quaSetIds.size() != oldSet.quaSetIds.size()) {
				isQuaSetChange = true;
			} else {
				for(int id:newSet.quaSetIds){
					if(!oldSet.quaSetIds.contains(id)){
						isQuaSetChange = true;
						break;
					}
				}
			}
			this.isSetChange = isStarSetUp || isStoneSetUp || isStrongSetUp || isQuaSetChange;
		}

	}

	public static class ItemResult_ExtPack extends CommonResult_Ext {
		public int newVolume;
	}

	public static class ItemResult_Item extends CommonResult_Ext {
		public KItem item;
	}

	public static class ItemResult_Use extends CommonResult_Ext {
		public KItem item;
		public int useCount;
	}

	public static class ItemResult_Compose extends CommonResult_Ext {
		public boolean isGoConfirmPay;
		public KItemTempAbs itemTemp;
		public int successTime;
	}

	public static class ItemResult_Enchase extends ItemResult_Equi {
		public boolean isGoConfirmMuil;
	}
	
	public static class ItemResult_BuyEnchase extends CommonResult_Ext {
		public KEquiBuyEnchansePrice priceData;
	}

	public static class ItemResult_AddItem extends CommonResult {
		//
		public List<KItem> newItemList;// 新创建的道具，可能为NULL
		public List<KItem> updateItemCountList;// 修改了道具数量的原有道具，可能为NULL

		public KItem getItem() {
			if (newItemList != null && newItemList.size() > 0) {
				return newItemList.get(0);
			}
			if (updateItemCountList != null && updateItemCountList.size() > 0) {
				return updateItemCountList.get(0);
			}
			return null;
		}
	}

	public static class ItemResult_InheritEquiPrice extends CommonResult {
		public KCurrencyCountStruct commonPayGold;
	}

	public static class ItemResult_GetExtBagPrice extends CommonResult {
		public List<KCurrencyCountStruct> price;
	}

	public static class GMResult extends CommonResult {
		public byte[] data;
	}

	public static class MailResult extends CommonResult {
		public KMail mail;
	}

	public static class MailResult_Ext extends MailResult {
		private List<String> uprisingTips;
		private List<String> dataUprisingTips;

		//
		public void addUprisingTips(String tips) {
			if (tips == null) {
				return;
			}
			getUprisingTips().add(tips);
		}

		public void addUprisingTips(Collection<String> tips) {
			if (tips == null) {
				return;
			}
			getUprisingTips().addAll(tips);
		}

		public void addDataUprisingTips(String tips) {
			if (tips == null) {
				return;
			}
			getDataUprisingTips().add(tips);
		}

		public void addDataUprisingTips(Collection<String> tips) {
			if (tips == null) {
				return;
			}
			getDataUprisingTips().addAll(tips);
		}

		public List<String> getUprisingTips() {
			if (dataUprisingTips == null) {
				dataUprisingTips = new ArrayList<String>(4);
			}
			return uprisingTips;
		}

		public List<String> getDataUprisingTips() {
			if (dataUprisingTips == null) {
				dataUprisingTips = new ArrayList<String>(4);
			}
			return dataUprisingTips;
		}

		//
		public void doFinally(KRole role) {
			if (role == null) {
				return;
			}
			KDialogService.sendUprisingDialog(role, uprisingTips);
			KDialogService.sendDataUprisingDialog(role, dataUprisingTips);
		}
	}

	public static class MailResult_Sync extends MailResult_Ext {
		// 需要删除的邮件
		public List<Long> deleteMails = new ArrayList<Long>();
		// 需要更新的邮件(包含由于警戒容量内的邮件被删除而上浮的邮件)
		public List<KMail> updateMails = new ArrayList<KMail>();

		public void doFinally(KRole role) {
			if (role == null) {
				return;
			}
			if (!deleteMails.isEmpty() || !updateMails.isEmpty()) {
				KPushMailsMsg.synMails(role, deleteMails, updateMails);
			}
			super.doFinally(role);
		}
	}

	public static class MailResult_TakeAtt extends MailResult_Ext {
		// 是否只提取了部分附件
		public boolean isTakePartAtts;
	}

	public static class SkillResult extends CommonResult_Ext {
		public KSkill skill;
	}

	public static class MoneyResult_ExchangeGold extends CommonResult_Ext {
		public long addGold;
	}

	public static class RSResult_AddFriend extends CommonResult_Ext {
		// KEY=角色ID
		public Map<Long, RSSynStruct> rsSynDatas;

		public void addDeleteRoleId(long owner, KRelationShipTypeEnum type, long oppRoleId) {
			if (rsSynDatas == null) {
				rsSynDatas = new HashMap<Long, RSSynStruct>();
			}
			RSSynStruct mRSSynStruct = rsSynDatas.get(owner);
			if (mRSSynStruct == null) {
				mRSSynStruct = new RSSynStruct(owner);
				rsSynDatas.put(mRSSynStruct.roleId, mRSSynStruct);
			}
			List<Long> temp = mRSSynStruct.deletes.get(type);
			if (temp == null) {
				temp = new ArrayList<Long>();
				mRSSynStruct.deletes.put(type, temp);
			}
			temp.add(oppRoleId);
		}

		public void addUpdateRoleId(long owner, KRelationShipTypeEnum type, long oppRoleId) {
			if (rsSynDatas == null) {
				rsSynDatas = new HashMap<Long, RSSynStruct>();
			}
			RSSynStruct mRSSynStruct = rsSynDatas.get(owner);
			if (mRSSynStruct == null) {
				mRSSynStruct = new RSSynStruct(owner);
				rsSynDatas.put(mRSSynStruct.roleId, mRSSynStruct);
			}

			List<Long> temp = mRSSynStruct.addOrUpdates.get(type);
			if (temp == null) {
				temp = new ArrayList<Long>();
				mRSSynStruct.addOrUpdates.put(type, temp);
			}
			temp.add(oppRoleId);
		}

		public static class RSSynStruct {
			public long roleId;
			public Map<KRelationShipTypeEnum, List<Long>> addOrUpdates = new HashMap<KRelationShipTypeEnum, List<Long>>();
			public Map<KRelationShipTypeEnum, List<Long>> deletes = new HashMap<KRelationShipTypeEnum, List<Long>>();

			RSSynStruct(long roleId) {
				this.roleId = roleId;
			}
		}
	}

	public static class MountResult_Use extends CommonResult_Ext {
		public KMountTemplate mountTemplate;
	}

	public static class MountResult_UpLv extends CommonResult_Ext {
		public boolean isLvUp;
		public int oldLv;
		public int newLv;
		public KMount mount;
	}
	
	public static class MountResult_BuildEqui extends CommonResult_Ext {
		public KMount mount;
		public boolean isMountInUsed;
	}
	
	public static class MountResult_ResetSP extends CommonResult_Ext {
		public boolean isGoConfirm;
		//
		public int releaseSP;
		public KMountTemplate mountTemplate;
	}
	
	public static class MountResult_UpBigLv extends CommonResult_Ext {
		public int oldBigLv;
		public int newBigLv;
		public KMount mount;
		public boolean isMountInUsed;
		
	}

	public static class MountResult_UpLvSkill extends CommonResult_Ext {
		public int releaseSP;
	}

	public static class FashionResult_Buy extends CommonResult_Ext {
		public boolean isGoConfirm;
		public long effectTime = -1;// 购买后的时长
	}
	
	public static class FashionResult_Buys extends CommonResult_Ext {
		public Map<Integer,Long> effectTimes = new HashMap<Integer, Long>();// 购买后的时长
	}

	public static class GangResult extends CommonResult {
		public KGang gang;
		public KGangExtCASet extCASet;
		public KGangMember opMember;
	}

	public static class GangResult_AllowApp extends GangResult {
		public KGangMember targetMember;
	}
	
	public static class GangResult_AcceptInvite extends GangResult {
		public boolean isApp;
	}

	public static class GangResult_SetPosition extends GangResult {
		public KGangMember targetMember;
		public KGangPositionEnum targetOrgPosition;
	}

	public static class GangResult_Exit extends GangResult {
		public boolean isGoConfirm;
		public boolean isDismiss;
	}

	public static class GangResultExt extends CommonResult_Ext {
		public KGang gang;
	}

	public static class GangResult_DoContribution extends GangResultExt {
		public KGangMember member;
		public KCurrencyCountStruct price;
	}

	public static class GangResult_UplvTech extends GangResultExt {
		public KGangExtCASet extCASet;
		public KGangTecTypeEnum tecType;
	}

	public static class GangResWarResult_Bid extends CommonResult_Ext {
		public int 我的竞价;
		public int 追加或竞价规定输入的金额;
	}

	public static class GangResWarResult_Join extends CommonResult {
		public ResWarCity city;
	}

	public static class GangResWarResult_Occ extends CommonResult_Ext {
		public ResPoint resPoint;
		public ResWarCity confirmOccCity;// 需要二次确认，放弃玩家当前占领的资源点
		public ResPoint confirmOccPoint;// 需要二次确认，放弃玩家当前占领的资源点
		public Set<ResWarCity> syncCitys;// 需要同步资源点数据的城市：放弃占领点的城市和已占领点的城市
		public boolean isGoPVP;
	}

	public static class RandomShopResultExt extends CommonResult_Ext {
		public boolean isGoConfirm;
		public KCurrencyCountStruct goConfirmPrice;
		public KRandomShopTypeEnum freeRefreshTypeEnum;
	}
	
	public static class HotShopResultExt extends CommonResult_Ext {
		public boolean isFind;
		public int releaseTime;
		public int releaseWorldTime;
	}	

	public static class RewardResult_DayluckOpenNum extends CommonResult_Ext {
		public boolean isSynLogs;
	}

	public static class RewardResult_SendMail extends CommonResult_Ext {
		public boolean isSendByMail;
	}

	public static class RewardResult_GoodLuck extends CommonResult {
		public int[] newNum;
		public int nextActiveValue;
	}

	public static class RewardResult_Garden extends CommonResult_Ext {
		public TreeData treeData;
		public ItemCountStruct specialRwardItem;
	}

	public static class RewardResult_GardenCollect extends RewardResult_Garden {
		public int diamondCount;
	}

	public static class RewardResult_GardenCollectTop extends RewardResult_Garden {
		public boolean isGoConfirm;
		public ItemCountStruct addItem;
	}

	public static class RewardResult_GardenOneKeyCollect extends CommonResult_Ext {
		public int collectedCount;
	}

	public static class RewardResult_Online extends CommonResult_Ext {
		public boolean isSync;
	}

	public static class RankResult_GoodJob extends CommonResult_Ext {
		public boolean isGoConfirm;
	}

//	public static class RankResult_LoginReward extends CommonResult_Ext {
//		public byte dayState;
//	}

	public static class SkillResul extends CommonResult {
		public boolean isChange;
	}
	
	public static class AddCheckResult extends CommonResult_Ext {
		public boolean isGoConfirm;
		public int day;
		
	}
	
	public static class BuyExcitingActivityResult extends CommonResult_Ext {
		public boolean isGoConfirm;
		
	}
	
	public static class DigRequestResult extends CommonResult{
		public boolean isShowNameList;
	}
	
	public static class VatalityRewardResult extends CommonResult_Ext{
		public int rewardLv;
	}
}
