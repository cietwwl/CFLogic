package com.kola.kmp.logic.rank;

import java.util.Collections;
import java.util.List;

import com.kola.kmp.logic.other.KRankTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RankModuleSupport;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-2-21 下午4:59:15
 * </pre>
 */
public class KRankSupportImpl implements RankModuleSupport {

	public void notifyBattlePowerChange(KRole role, int battlePower) {
		if (role == null) {
			return;
		}
		KRankLogic.notifyBattlePowerChange(role, battlePower);
		KSupportFactory.getCompetitionModuleSupport().notifyBattlePowerChange(role, battlePower);
	}
	
	public void notifyPetInfoChange(KRole role, String petName, int petLv, int petPow) {
		if (role == null) {
			return;
		}
		KRankLogic.notifyPetInfoChange(role, petName, petLv, petPow);
	}

	/**
	 * <pre>
	 * 
	 * @param ranType
	 * @param roleId
	 * @return 0表示未上榜
	 * @author CamusHuang
	 * @creation 2014-2-21 上午11:11:44
	 * </pre>
	 */
	public int checkRank(KRankTypeEnum ranType, long roleId) {
		RankElementAbs element = null;
		switch (ranType) {
		case 战力榜:
			element = KRankLogic.aresRank.getPublishData().getElement(roleId);
			break;
		case 等级榜:
			element = KRankLogic.levelRank.getPublishData().getElement(roleId);
			break;
		case 随从榜:
			element = KRankLogic.petRank.getPublishData().getElement(roleId);
			break;
		case 竞技榜:
			return KSupportFactory.getCompetitionModuleSupport().getCurrentRankOfRole(roleId);
		}
		if (element == null) {
			return 0;
		}
		return element.getRank();
	}
	
	/**
	 * <pre>
	 * 获取等级排行榜
	 * 
	 * @param ranType
	 * @param page 第几页：从1开始算
	 * @param pageSize 一页的长度
	 * @return 一定不为null
	 * @author CamusHuang
	 * @creation 2014-2-21 下午5:06:39
	 * </pre>
	 */
	public List<RankElementLevel> getRankElements_Level(int page, int pageSize) {
		return (List<RankElementLevel>)getRankElements(KRankTypeEnum.等级榜, page, pageSize);
	}	

	/**
	 * <pre>
	 * 获取排行榜
	 * 
	 * @param ranType
	 * @param page 第几页：从1开始算
	 * @param pageSize 一页的长度
	 * @return 一定不为null
	 * @author CamusHuang
	 * @creation 2014-2-21 下午5:06:39
	 * </pre>
	 */
	public List getRankElements(KRankTypeEnum ranType, int page, int pageSize) {
		if (page < 1 || pageSize < 1) {
			return Collections.emptyList();
		}
		//
		List temp = null;
		switch (ranType) {
		case 战力榜:
			temp = KRankLogic.aresRank.getPublishData().getUnmodifiableElementList();
			break;
		case 等级榜:
			temp = KRankLogic.levelRank.getPublishData().getUnmodifiableElementList();
			break;
		case 随从榜:
			temp = KRankLogic.petRank.getPublishData().getUnmodifiableElementList();
			break;
		case 竞技榜:
			temp = KSupportFactory.getCompetitionModuleSupport().getCurrentRanks(KRankTypeEnum.竞技榜.getMaxLen());
			break;
		}

		if (temp.isEmpty()) {
			return Collections.emptyList();
		}

		// 2页，10个:10-20
		int fromIndex = pageSize * (page - 1);
		int toIndex = pageSize * page;

		int size = temp.size();
		if (fromIndex >= size) {
			return Collections.emptyList();
		}
		toIndex = Math.min(toIndex, size);
		return temp.subList(fromIndex, toIndex);
	}
}
