package com.kola.kmp.logic.skill.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.skill.KSkillAttributeProvider;
import com.kola.kmp.logic.skill.KSkillLogic;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.SkillResult;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.skill.KSkillProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KSkillUplevelMsg implements GameMessageProcesser, KSkillProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KSkillUplevelMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_UPLEVEL_SKILL;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		boolean isInit = msg.readBoolean();
		int skillTempId = msg.readInt();
		// -------------
		SkillResult result = null;
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new SkillResult();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
		} else {
			result = KSkillLogic.dealMsg_uplvSkill(role, isInit, skillTempId);
		}
		// -------------
		KGameMessage backmsg = KGame.newLogicMessage(SM_UPLEVEL_SKILL_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		if (result.isSucess) {
			backmsg.writeBoolean(isInit);
			backmsg.writeInt(skillTempId);
			backmsg.writeInt(result.skill.getLv());
		}
		session.send(backmsg);

		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);

		if (result.isSucess) {
			// 通知日常任务
			KSupportFactory.getMissionSupport().notifyUseFunction(role, KUseFunctionTypeEnum.升级技能);

			if (!isInit) {
				// 刷新角色属性
				KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(role.getId(), KSkillAttributeProvider.getType());
			}
			KSupportFactory.getRoleModuleSupport().notifySkillUpgrade(role.getId(), result.skill);
		}
	}
}
