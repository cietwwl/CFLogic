package com.kola.kmp.logic.combat;

import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * 定义了战斗中一些常量
 * 错误代码均以ERR开头，例如：{@link ICombatConstant#ERR_CODE_ROLE_IS_OFFLINE}
 * </pre>
 * @author PERRY CHAN
 */
public interface ICombatConstant {

	/** 错误代号：角色不存在 */
	int ERR_CODE_ROLE_NOT_EXIST = 1;
	/** 错误代号：角色不在线 */
	int ERR_CODE_ROLE_IS_OFFLINE = 2;
	/** 错误代号：角色正在战斗中 */
	int ERR_CODE_ROLE_IS_FIGHTING = 3;
	/** 错误代号：目标不存在 */
	int ERR_CODE_TARGET_NOT_EXIST = 4;
	/** 错误代号：未知原因 */
	int ERR_CODE_UNKNOW = 404;
	
	int ONE_MINUTE_MILLIS = (int)TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
	
	int ONE_SECOND_MILLIS = (int)TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS);
	/** 同步时间的间隔，单位：秒 */
	int SYNC_TIME_ITERVAL = 5;
}
