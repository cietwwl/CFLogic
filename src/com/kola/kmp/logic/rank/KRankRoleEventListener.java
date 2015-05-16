package com.kola.kmp.logic.rank;

import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.competition.KCompetitor;
import com.kola.kmp.logic.other.KRankTypeEnum;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.text.HyperTextTool;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2013-1-29 下午5:50:54
 * </pre>
 */
public class KRankRoleEventListener implements IRoleEventListener {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		KWordBroadcastType _boradcastType = KWordBroadcastType.排行榜_我们的x榜冠军x角色上线了大家快去膜拜吧;

		RankElementAbs e = KRankLogic.aresRank.getPublishData().getElementByRank(1);
		if (e != null && e.elementId == role.getId()) {
			String tips = StringUtil.format(_boradcastType.content, KRankTypeEnum.战力榜.name, HyperTextTool.extRoleNameWithMenu(e.elementId, e.elementName));
			KSupportFactory.getChatSupport().sendSystemChat(tips, _boradcastType);
		}
		e = KRankLogic.levelRank.getPublishData().getElementByRank(1);
		if (e != null && e.elementId == role.getId()) {
			String tips = StringUtil.format(_boradcastType.content, KRankTypeEnum.等级榜.name, HyperTextTool.extRoleNameWithMenu(e.elementId, e.elementName));
			KSupportFactory.getChatSupport().sendSystemChat(tips, _boradcastType);
		}
		e = KRankLogic.petRank.getPublishData().getElementByRank(1);
		if (e != null && e.elementId == role.getId()) {
			String tips = StringUtil.format(_boradcastType.content, KRankTypeEnum.随从榜.name, HyperTextTool.extRoleNameWithMenu(e.elementId, e.elementName));
			KSupportFactory.getChatSupport().sendSystemChat(tips, _boradcastType);
		}
		KCompetitor competitor = KSupportFactory.getCompetitionModuleSupport().getCompetitor(1);
		if (competitor != null && competitor.getRoleId() == role.getId()) {
			String tips = StringUtil.format(_boradcastType.content, KRankTypeEnum.竞技榜.name, HyperTextTool.extRoleNameWithMenu(competitor.getRoleId(), competitor.getRoleName()));
			KSupportFactory.getChatSupport().sendSystemChat(tips, _boradcastType);
		}

		// 点赞玩家数据
		KRoleRankData doRoleData = KRankRoleExtCACreator.getRoleRankData(role.getId());
		// 尝试跨天数据重置
		doRoleData.notifyForLogin(System.currentTimeMillis());
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {
		// 忽略
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		// 忽略
	}

	@Override
	public void notifyRoleDeleted(long roleId) {
		KRankLogic.notifyRoleDeleted(roleId);
	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		KRankLogic.notifyPlayerRoleLevelUp(role, role.getLevel(), role.getCurrentExp());
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		
	}

}
