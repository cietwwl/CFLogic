package com.kola.kmp.logic.combat.state;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.IOperationResult;
import com.kola.kmp.logic.combat.operation.IOperation;

/**
 * 
 * @author PERRY CHAN
 */
public class KStateDurationEffectOperation implements IOperation {

	private long _happenTime;
	private ICombatMember _target;
	private ICombatState _state;
	private ICombatMember _srcTarget;
	
	
	public KStateDurationEffectOperation(long pHappenTime, ICombatMember pTarget, ICombatState pState) {
		this._happenTime = pHappenTime;
		this._target = pTarget;
		this._srcTarget = pTarget;
		this._state = pState;
	}
	
	@Override
	public IOperationResult executeOperation(ICombat combat) {
		if (_state.isEffective() && !_state.isTimeOut(_happenTime) && !_target.isHang()) {
//			_LOGGER.info("周期状态影响，shadowId：{}，状态id：{}，时间：{}", _target.getShadowId(), _state.getStateTemplateId(), _happenTime);
			_state.durationEffect(combat, _target.getSkillActor(), _happenTime);
		}
		return null;
	}
	
	@Override
	public long getOperationTime() {
		return _happenTime;
	}
	
	@Override
	public int getPriority() {
		return PRIORITY_URGENT;
	}
	
	@Override
	public void notifyMountAdded(ICombatMember master, ICombatMember mount) {
		if (this._target == master) {
			this._target = mount;
		}
	}

	@Override
	public void notifyMountReleased(ICombatMember master, ICombatMember mount) {
		if(this._target == mount && this._srcTarget == master) {
			this._target = master;
		}
	}
}
