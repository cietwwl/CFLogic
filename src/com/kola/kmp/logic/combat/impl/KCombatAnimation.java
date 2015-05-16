package com.kola.kmp.logic.combat.impl;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.level.gamestory.Animation;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatAnimation {

	private Animation _animationData;
	
	/**
	 * 
	 */
	KCombatAnimation(Animation pAnimationData) {
		this._animationData = pAnimationData;
	}
	
	public void packDataToMsg(KGameMessage msg) {
		msg.writeInt(this._animationData.animationResId);
		msg.writeByte(_animationData.animationStartType);
		msg.writeInt(_animationData.battlefieldSerialNumber);
		msg.writeInt(_animationData.waveId);
	}
}
