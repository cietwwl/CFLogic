package com.kola.kmp.logic.combat.event;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatEvent;
import com.kola.kmp.logic.combat.ICombatEventListener;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.fight.KFightProtocol;

/**
 * 
 * @author PERRY CHAN
 */
public class KGoldCombatAfterAtkEvent implements ICombatEventListener  {

	private int _para;
	private KGameMessage _msg;
	private int _writerIndex;
	private long _totalHp;
	
	public KGoldCombatAfterAtkEvent(int para, long totalHp) {
		_para = para;
		_msg = KGame.newLogicMessage(KFightProtocol.SM_SYNC_GOLD_COMBAT_MONEY);
		_writerIndex = _msg.writerIndex();
		_totalHp = totalHp;
		_msg.writeInt(0);
	}
	
	@Override
	public int getEventId() {
		return ICombatEvent.EVENT_AFTER_ATTACK;
	}

	@Override
	public void run(ICombat combat, ICombatMember operator, long happenTime) {
		// 角色金币基数，战斗中显示获得金币的公式：我的伤害 / BOSS总血量 * 金币基数 = 可获得金币
//		ICombatMember src = operator;
		if (operator.getMemberType() == ICombatMember.MEMBER_TYPE_PET) {
			ICombatMember master = combat.getCombatMember(combat.getMasterShadowIdOfPet(operator.getShadowId()));
			master.getCombatRecorder().recordAccompanyDm(operator.getCombatRecorder().getLastDm());
			operator = master;
		} else if (operator.getMemberType() == ICombatMember.MEMBER_TYPE_VEHICLE) {
			if(operator.isAlive()) {
				operator = combat.getMasterOfMount(operator);
			} else {
				// 有可能通知的时候，坐骑已经超时，原因是客户端播放动画需要时间，击中的时间发生在坐骑超时之后
				operator = combat.getCombatMemberByType(ICombatMember.MEMBER_TYPE_ROLE).get(0);
			}
		} else if (operator.getMemberType() == ICombatMember.MEMBER_TYPE_MINION) {
			ICombatMember master = combat.getMasterOfMinion(operator);
			if (master.getMemberType() == ICombatMember.MEMBER_TYPE_PET) {
				short masterIdOfPet = combat.getMasterShadowIdOfPet(master.getShadowId());
				master = combat.getCombatMember(masterIdOfPet);
			}
			master.getCombatRecorder().recordAccompanyDm(operator.getCombatRecorder().getLastDm());
			operator = master;
		}
//		ICombat.LOGGER.info("打金伤害结算，src=[{},{}]，operator=[{},{},{}]", src.getName(), src.getCombatRecorder().getLastDm(), operator.getName(), operator.getCombatRecorder().getLastDm(), operator.getCombatRecorder().getTotalDm());
		long dm = operator.getCombatRecorder().getTotalDmIncludingAccompany();
//		KGameMessage msg = _msg.duplicate(); // duplicate之后不能修改数据
		KGameMessage msg = _msg.copy();
		msg.setInt(_writerIndex, Math.round((float)dm / _totalHp * _para));
		KSupportFactory.getRoleModuleSupport().sendMsg(operator.getSrcObjId(), msg);
	}

	@Override
	public boolean isEffective(long happenTime) {
		return true;
	}

}
