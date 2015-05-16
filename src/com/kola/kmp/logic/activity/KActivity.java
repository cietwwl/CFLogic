package com.kola.kmp.logic.activity;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KActivityTimeTypeEnum;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.ItemModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;

public abstract class KActivity {
	// 日常活动类型
	public static final byte ACTIVITY_TYPE_DAILY = 1;
	// 定时活动类型
	public static final byte ACTIVITY_TYPE_TIMED = 2;
	// 活动编号Id
	protected int activityId;
	// 活动时间
	protected String activityName;
	// 活动描述
	protected String desc;
	// 活动图标资源ID
	protected int iconId;
	// 活动类型
	protected byte activityType;
	// 活动时间类型参数
	protected KActivityTimeStruct timeStruct;
	// 活动主要产出描述
	protected String mainProduceTips;
	// 活动开放时间描述(短提示)
	protected String openTimeShortTips;
	// 活动开放时间描述
	protected String openTimeTips;
	// 活动开放的角色等级
	protected int openRoleLv;
	// 活动排序编号
	protected int serialNum;
	// 活动是否开放的状态位
	protected boolean isOpened;
	// 活动自定义配置文件路径
	protected String configFilePath;
	// 是否已进行活动总表的配置数据加载
	protected boolean isloadExcelConfig = false;
	// 是否公会（军团）活动
	protected boolean isGangActivity;
	// 公会（军团）等级限制
	protected int gangLevelLimit;
	// 活动允许进入次数（0：表示不限制）
	protected int canJoinCount;
	// 是否有进入活动次数限制
	protected boolean isLimitJointCount;
	// 是否有进入CD冷却时间限制（受isLimitJointCount影响，当isLimitJointCount==true，该值才生效）
	protected boolean isCdTimeLimit;
	// 进入CD冷却时间（单位：秒，当isCdTimeLimit==true时才生效）
	protected int cdTimeSeconds;
	// 活动是否开启（true开启并显示在活动列表，false关闭不显示）
	protected boolean isStart;
	// 活动列表界面显示的通用奖励数据
	protected BaseRewardData activityShowReward;
	// 活动列表界面显示的军团资源奖励图标数据
	protected List<ActivityGangRewardLogoStruct> gangLogoList = new ArrayList<ActivityGangRewardLogoStruct>();

	public void loadExcelConfig(KGameExcelRow row) throws KGameServerException {
		this.activityId = row.getInt("id");
		this.activityName = row.getData("name");
		int type = row.getInt("type");
		KActivityTimeTypeEnum typeEnum = KActivityTimeTypeEnum.getEnum(type);
		String dayStr = null;
		String weekStr = null;
		String timeStr = null;
		if (typeEnum == null) {
			throw new KGameServerException("初始化《活动配置》表的参数type出错，找不到的类型，type=" + type + ",活动ID：" + activityId + "，excel行数：" + row.getIndexInFile());
		}
		if (typeEnum == KActivityTimeTypeEnum.ACTIVITY_TYPE_ANY_TIME) {
			activityType = ACTIVITY_TYPE_DAILY;
		} else {
			activityType = ACTIVITY_TYPE_TIMED;
		}
		switch (typeEnum) {
		case ACTIVITY_TYPE_SPECIAL_TIME:
			break;
		case ACTIVITY_TYPE_ANY_TIME:
			break;
		case ACTIVITY_TYPE_EVERYDAY_TIMED:
			timeStr = row.getData("time");
			break;
		case ACTIVITY_TYPE_APPOINTED_DAY:
			dayStr = row.getData("day");
			timeStr = row.getData("time");
			break;
		case ACTIVITY_TYPE_WEEKDAY_TIMED:
			weekStr = row.getData("week");
			timeStr = row.getData("time");
			break;
		default:
			break;
		}
		this.timeStruct = new KActivityTimeStruct(type, dayStr, weekStr, timeStr);
		this.timeStruct.initTimeStruct();

		this.openRoleLv = row.getInt("lvl");
		this.openTimeShortTips = row.getData("time_tips");
		this.openTimeTips = row.getData("time_info");
		this.desc = row.getData("content_info");
		this.mainProduceTips = row.getData("award_info");
		this.iconId = row.getInt("icon");
		this.serialNum = row.getInt("sort");

		this.isGangActivity = row.getBoolean("is_guild");
		this.gangLevelLimit = row.getInt("guildlvl");
		this.canJoinCount = row.getInt("is_num");
		this.isLimitJointCount = (this.canJoinCount > 0);
		this.isStart = row.getBoolean("isStart");
		if (this.isLimitJointCount) {
			this.cdTimeSeconds = row.getInt("cd_time");
			if (this.cdTimeSeconds > 0) {
				this.isCdTimeLimit = true;
			}
		}

		boolean isHasExp = row.getBoolean("isExp");
		List<AttValueStruct> attrlist = new ArrayList<AttValueStruct>();
		if (isHasExp) {
			attrlist.add(new AttValueStruct(KGameAttrType.EXPERIENCE, 0));
		}
		List<KCurrencyCountStruct> curList = new ArrayList<KCurrencyCountStruct>();
		String currencyTypeInfoStr = row.getData("currencyType");
		if (currencyTypeInfoStr != null && currencyTypeInfoStr.length() > 0 && !currencyTypeInfoStr.equals("")) {
			String[] currencyTypeInfo = currencyTypeInfoStr.split(",");
			if (currencyTypeInfo != null && currencyTypeInfo.length > 0) {
				for (int i = 0; i < currencyTypeInfo.length; i++) {
					byte curType = Byte.parseByte(currencyTypeInfo[i]);
					KCurrencyTypeEnum currencyType = KCurrencyTypeEnum.getEnum(curType);
					if (currencyType == null) {
						throw new KGameServerException("初始化《活动配置》表的参数currencyType出错，找不到的货币类型，type=" + curType + ",活动ID：" + activityId + "，excel行数：" + row.getIndexInFile());
					}
					curList.add(new KCurrencyCountStruct(currencyType, 0));
				}
			}
		}
		List<ItemCountStruct> itemList = new ArrayList<ItemCountStruct>();
		String itemInfoStr = row.getData("itemList");
		if (itemInfoStr != null && itemInfoStr.length() > 0 && !itemInfoStr.equals("")) {
			String[] itemInfo = itemInfoStr.split(",");
			if (itemInfo != null && itemInfo.length > 0) {
				ItemModuleSupport itemSupport = KSupportFactory.getItemModuleSupport();
				for (int i = 0; i < itemInfo.length; i++) {
					if (itemSupport.getItemTemplate(itemInfo[i]) == null) {
						throw new KGameServerException("初始化《活动配置》表的参数itemList出错，找不到的道具类型，code=" + itemInfo[i] + ",活动ID：" + activityId + "，excel行数：" + row.getIndexInFile());
					}
					itemList.add(new ItemCountStruct(itemInfo[i], 0));
				}
			}
		}
		this.activityShowReward = new BaseRewardData(attrlist, curList, itemList, null, null, false, false);

		int gangExpRewardLogo = row.getInt("gangExp");
		if (gangExpRewardLogo != 0) {
			gangLogoList.add(new ActivityGangRewardLogoStruct(ActivityGangRewardLogoStruct.LOGO_TYPE_GANG_EXP, gangExpRewardLogo));
		}

		int gangGoldRewardLogo = row.getInt("gangGold");
		if (gangGoldRewardLogo != 0) {
			gangLogoList.add(new ActivityGangRewardLogoStruct(ActivityGangRewardLogoStruct.LOGO_TYPE_GANG_GOLD, gangGoldRewardLogo));
		}

		isloadExcelConfig = true;
	}

	public int getActivityId() {
		return activityId;
	}

	public String getActivityName() {
		return activityName;
	}

	public String getDesc() {
		return desc;
	}

	public int getIconId() {
		return iconId;
	}

	public KActivityTimeStruct getKActivityTimeStruct() {
		return timeStruct;
	}

	public String getMainProduceTips() {
		return mainProduceTips;
	}

	public String getOpenTimeShortTips() {
		return openTimeShortTips;
	}

	public String getOpenTimeTips() {
		return openTimeTips;
	}

	public int getOpenRoleLv() {
		return openRoleLv;
	}

	public boolean isOpened() {
		return isOpened;
	}

	public boolean isGangActivity() {
		return isGangActivity;
	}

	public int getGangLevelLimit() {
		return gangLevelLimit;
	}

	public int getCanJoinCount() {
		return canJoinCount;
	}

	public boolean isLimitJointCount() {
		return isLimitJointCount;
	}

	public boolean isStart() {
		return isStart;
	}

	public boolean isCdTimeLimit() {
		return isCdTimeLimit;
	}

	public int getCdTimeSeconds() {
		return cdTimeSeconds;
	}

	/**
	 * 通知活动管理器该活动当前的开启状态，当开启状态发生改变时调用
	 * 
	 * @param isOpenOrClose
	 */
	public void notifyActivityOpenStatus(boolean isOpenOrClose) {
		this.isOpened = isOpenOrClose;
		KActivityManager.getInstance().notifyActivityOpenStatus(activityId);
	}

	/**
	 * <pre>
	 * 通知角色登录游戏
	 * </pre>
	 */
	public void notifyRoleJoinedGame(KRole role) {

	}

	/**
	 * <pre>
	 * 通知角色离开游戏
	 * </pre>
	 */
	public void notifyRoleLeavedGame(KRole role) {

	}

	/**
	 * <pre>
	 * 初始化活动
	 * 
	 * @param activityConfigPath
	 * @throws KGameServerException
	 * </pre>
	 */
	public abstract void init(String activityConfigPath) throws KGameServerException;

	/**
	 * 通知角色从活动列表进入活动
	 * 
	 * @param role
	 * @return
	 */
	public abstract KActionResult playerRoleJoinActivity(KRole role);

	/**
	 * <pre>
	 * 游戏世界加载完毕的通知，此时，游戏的缓存数据仍未加载完毕
	 * </pre>
	 */
	public abstract void onGameWorldInitComplete() throws KGameServerException;

	/**
	 * <pre>
	 * 通知缓存数据加载完毕
	 * </pre>
	 */
	public abstract void notifyCacheLoadComplete() throws KGameServerException;

	/**
	 * 服务器关闭前的通知
	 */
	public abstract void serverShutdown() throws KGameServerException;

	/**
	 * 剩余进入次数
	 */
	public abstract int getRestJoinActivityCount(KRole role);

	/**
	 * 活动列表军团资源奖励logo数据
	 * 
	 * @author Administrator
	 * 
	 */
	public static class ActivityGangRewardLogoStruct {
		public static final byte LOGO_TYPE_GANG_EXP = 1;
		public static final byte LOGO_TYPE_GANG_GOLD = 2;

		public byte logoType;
		public int logoId;

		private ActivityGangRewardLogoStruct(byte logoType, int logoId) {
			this.logoType = logoType;
			this.logoId = logoId;
		}

	}
}
