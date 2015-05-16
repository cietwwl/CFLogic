package com.kola.kmp.logic.role;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kgame.cache.role.RoleModuleFactory;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.RoleTips;

/**
 *
 * @author PERRY CHAN
 */
public class KRecoverPhyPowerTask implements KGameTimerTask{

	private static int _delay;
	static void submit() {
		KRecoverPhyPowerTask task = new KRecoverPhyPowerTask();
		_delay = (int)TimeUnit.MINUTES.convert(KRoleModuleConfig.getRecoverPhyPowerTimeItr(), TimeUnit.MILLISECONDS);
		if (_delay > 0) {
			KGame.getTimer().newTimeSignal(task, _delay, TimeUnit.MINUTES);
		} else {
			throw new RuntimeException("恢复体力间隔小于1分钟！毫秒数：" + KRoleModuleConfig.getRecoverPhyPowerTimeItr());
		}
	}
	
	@Override
	public String getName() {
		return "KRecoverPhyPowerTask";
	}

	@Override
	public Object onTimeSignal(KGameTimeSignal timeSignal) throws KGameServerException {
		List<Long> onlineIds = RoleModuleFactory.getRoleModule().getAllOnLineRoleIds();
		for (Iterator<Long> itr = onlineIds.iterator(); itr.hasNext();) {
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(itr.next());
			if (role != null) {
				role.increasePhyPower(KRoleModuleConfig.getPhyPowerRecoverItr(), true, false, RoleTips.getTipsFlowOnlineIncreasPhyPower());
			}
		}
		KRoleModuleConfig.setLastIncreasePhyPowerTime(System.currentTimeMillis());
		timeSignal.getTimer().newTimeSignal(this, _delay, TimeUnit.MINUTES);
		return "success";
	}

	@Override
	public void done(KGameTimeSignal timeSignal) {
		
	}

	@Override
	public void rejected(RejectedExecutionException e) {
		
	}

}
