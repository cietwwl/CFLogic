package com.kola.kmp.logic.gang;

import java.text.SimpleDateFormat;

import javax.management.timer.Timer;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.util.tips.GangTips;

/**
 * <pre>
 * 
 * @author camus
 * @creation 2012-12-30 下午2:49:48
 * </pre>
 */
public class KGangConfig {
	private static KGangConfig instance;

	public final int MAX_APP_GANG_NUM;// 一个角色最多只能向N个军团申请加入

	// =========================创建军团
	/** 创建军团的价格 */
	final KCurrencyCountStruct PayForCreateGang;
	/** 创建军团的最小等级 */
	public final int CreateGangMinRoleLevel;
	/** 军团名称的最小字符数量 */
	public final int GangNameMinLen;
	/** 军团名称的最大字符数量 */
	public final int GangNameMaxLen;
	/** 军团创建初始资金 */
	final int initResource;

	// =========================军团公告
	/** 军团公告的最大字符数量 */
	final int GangNoticeMaxLen = 100;// 客户端写死，不能配置

	// =========================军团时效任务
	/** 军团数据（日志，APP）清理任务周期时间(毫秒) */
	final long DATA_CLEARTASK_PERIOD;
	/** 军团成员及APP数据同步周期时间(毫秒) */
	final long GangSyncTaskPeroid;

	// =========================军团日志
	/** 日志最大数量 */
	final int DAILY_MAXLEN;
	/** 日志时间日期格式 本值涉及到数据加载，不允许配置 */
	final SimpleDateFormat DAILY_DATEFORMATE = new SimpleDateFormat(GangTips.dd日HHmm);

	// =========================军团申请
	/** APP最大数量 */
	final int APP_MAXLEN;
	/** 角色发出APP的最大数量 */
	final int APP_MAXCOUNT;
	
	/** 离开军团24小时内无法重新加入 */
	final long JoinGangCDHour;
	
	/** 角色邀请CD时间*/
	final long InviteCDTimeForRole;
	
	/** 世界邀请CD时间*/
	final long InviteCDTimeForWorld;

	// =========================模糊搜索军团
	/** 模糊搜索军团时，返回给客户端的列表最大长度 */
	final long GangSearchMaxLen;
	/** 模糊搜索军团的CD时间 */
	public long GangSearchCD;

	// =========================自动禅让
	/** 自动禅让给多少时长内登陆的成员 */
	final long AutoMoveOver_TargetTime;
	/** 团长多少时长不上线时，自动禅让 */
	final long AutoMoveOver_OutTime;
	/** 团长多少天不上线时，自动禅让 */
	final long AutoMoveOver_OutDay;

	private KGangConfig(Element root) throws KGameServerException {

		MAX_APP_GANG_NUM = Integer.parseInt(root.getChildTextTrim("MAX_APP_GANG_NUM"));

		// =========================创建军团
		{
			Element tempE = root.getChild("createGang");

			KCurrencyTypeEnum currencyType = KCurrencyTypeEnum.getEnum(Byte.parseByte(tempE.getChildTextTrim("PayForCreateGang_CURRENCYTYPE")));
			int moneyCount = Integer.parseInt(tempE.getChildTextTrim("PayForCreateGang_MONEYCOUNT"));
			if (currencyType == null || moneyCount < 1) {
				throw new KGameServerException("加载模块配置文件错误：PayForCreateGang 有误！");
			}
			PayForCreateGang = new KCurrencyCountStruct(currencyType, moneyCount);
			CreateGangMinRoleLevel = Integer.parseInt(tempE.getChildTextTrim("CreateGangMinRoleLevel"));
			if (CreateGangMinRoleLevel <= 0) {
				throw new KGameServerException("加载模块配置文件错误：CreateGangMinRoleLevel 有误！");
			}
			GangNameMinLen = Integer.parseInt(tempE.getChildTextTrim("GangNameMinLen"));
			if (GangNameMinLen <= 0) {
				throw new KGameServerException("加载模块配置文件错误：GangNameMinLen 有误！");
			}
			GangNameMaxLen = Integer.parseInt(tempE.getChildTextTrim("GangNameMaxLen"));
			if (GangNameMaxLen <= 0) {
				throw new KGameServerException("加载模块配置文件错误：GangNameMaxLen 有误！");
			}

			/** 军团创建初始资金 */
			initResource = Integer.parseInt(tempE.getChildTextTrim("initResource"));
		}

		// =========================军团时效任务
		{
			DATA_CLEARTASK_PERIOD = UtilTool.parseDHMS(root.getChildTextTrim("DATA_CLEARTASK_PERIOD"));
			if (DATA_CLEARTASK_PERIOD <= 0) {
				throw new KGameServerException("加载模块配置文件错误：DATA_CLEARTASK_PERIOD 有误！");
			}
			GangSyncTaskPeroid = UtilTool.parseDHMS(root.getChildTextTrim("GangSyncTaskPeroid"));
			if (GangSyncTaskPeroid <= 0) {
				throw new KGameServerException("加载模块配置文件错误：GangSyncTaskPeroid 有误！");
			}
		}

		// =========================军团日志
		{
			DAILY_MAXLEN = Integer.parseInt(root.getChildTextTrim("DAILY_MAXLEN"));
			if (DAILY_MAXLEN <= 0) {
				throw new KGameServerException("加载模块配置文件错误：DAILY_MAXLEN 有误！");
			}
		}

		// =========================军团申请
		{
			APP_MAXLEN = Integer.parseInt(root.getChildTextTrim("APP_MAXLEN"));
			if (APP_MAXLEN <= 0) {
				throw new KGameServerException("加载模块配置文件错误：APP_MAXLEN 有误！");
			}
			APP_MAXCOUNT = Integer.parseInt(root.getChildTextTrim("APP_MAXCOUNT"));
			if (APP_MAXCOUNT <= 0) {
				throw new KGameServerException("加载模块配置文件错误：APP_MAXCOUNT 有误！");
			}
			JoinGangCDHour = Integer.parseInt(root.getChildTextTrim("JoinGangCDHour"));
			if (JoinGangCDHour <= 0) {
				throw new KGameServerException("加载模块配置文件错误：JoinGangCDHour 有误！");
			}
			
			InviteCDTimeForRole = UtilTool.parseDHMS(root.getChildTextTrim("InviteCDTimeForRole"));
			if (InviteCDTimeForRole <= 30*Timer.ONE_SECOND) {
				throw new KGameServerException("加载模块配置文件错误：InviteCDTimeForRole 有误！");
			}
			
			InviteCDTimeForWorld = UtilTool.parseDHMS(root.getChildTextTrim("InviteCDTimeForWorld"));
			if (InviteCDTimeForWorld <= 30*Timer.ONE_SECOND) {
				throw new KGameServerException("加载模块配置文件错误：InviteCDTimeForWorld 有误！");
			}
		}

		// =========================模糊搜索军团
		{
			GangSearchMaxLen = Integer.parseInt(root.getChildTextTrim("GangSearchMaxLen"));
			if (GangSearchMaxLen <= 0) {
				throw new KGameServerException("加载模块配置文件错误：GangSearchMaxLen 有误！");
			}

			GangSearchCD = UtilTool.parseDHMS(root.getChildTextTrim("GangSearchCD"));
			if (GangSearchCD <= 0) {
				throw new KGameServerException("加载模块配置文件错误：GangSearchCD 有误！");
			}
		}

		// =========================自动禅让
		{
			AutoMoveOver_TargetTime = UtilTool.parseDHMS(root.getChildTextTrim("AutoMoveOver_TargetDay"));
			if (AutoMoveOver_TargetTime <= 0) {
				throw new KGameServerException("加载模块配置文件错误：AutoMoveOver_TargetDay 有误！");
			}

			AutoMoveOver_OutTime = UtilTool.parseDHMS(root.getChildTextTrim("AutoMoveOver_OutDay"));
			if (AutoMoveOver_OutTime <= 0) {
				throw new KGameServerException("加载模块配置文件错误：AutoMoveOver_OutDay 有误！");
			}
			int day = (int) (AutoMoveOver_OutTime / Timer.ONE_DAY);
			if (day < 1) {
				day = 1;
			}

			AutoMoveOver_OutDay = day;
		}
	}

	public static void init(Element root) throws KGameServerException {
		instance = new KGangConfig(root);
	}

	public static KGangConfig getInstance() {
		return instance;
	}
}
