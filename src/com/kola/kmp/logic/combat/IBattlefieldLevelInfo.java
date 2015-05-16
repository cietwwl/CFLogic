package com.kola.kmp.logic.combat;

import java.util.List;

import com.kola.kmp.logic.level.BattlefieldWaveViewInfo;
import com.kola.kmp.logic.other.KGameBattlefieldTypeEnum;

/**
 * 统一接口，集成IBattlefieldBaseInfo
 * 有些不需要实现这里的方法，但是战斗中需要使用两个接口的数据
 * 所以集成数据
 * @author PERRY CHAN
 *
 */
public interface IBattlefieldLevelInfo extends IBattlefieldBaseInfo {

	/**
	 * 
	 * @return
	 */
	public boolean isFirstBattlefield();
	
	/**
	 * 
	 * @return
	 */
	public KGameBattlefieldTypeEnum getBattlefieldType();
	
	/**
	 * 
	 * @return
	 */
	public int getLevelId();
	
	/**
	 * 
	 * @return
	 */
	public List<BattlefieldWaveViewInfo> getAllWaveInfo();
}
