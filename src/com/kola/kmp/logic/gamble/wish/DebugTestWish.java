package com.kola.kmp.logic.gamble.wish;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kmp.logic.gamble.KGambleModule;
import com.kola.kmp.logic.gamble.KGambleRoleExtCACreator;
import com.kola.kmp.logic.gamble.KGambleRoleExtData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class DebugTestWish {

	public static int testDebugDiceCount = 1500;

	public static void debug() {
		KGambleModule.getWishSystemManager().diamondLotteryPool.set(10000);
		long minRoleId = 238000000001l;
		long maxRoleId = 238000001052l;
		List<KRole> roleList = new ArrayList<KRole>();
		for (long roleId = minRoleId; roleId <= maxRoleId; roleId++) {
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role != null && role.getLevel() >= 20) {
				roleList.add(role);
			}
		}

		System.err.println("接受许愿测试的角色数量：" + roleList.size());

		for (KRole role : roleList) {
			TestWishTask task = new TestWishTask(role);
			KGame.getTimer().newTimeSignal(task, 1, TimeUnit.SECONDS);
		}
	}

	public static class TestWishTask implements KGameTimerTask {
		KRole role;
		int diceTime = 1;
		boolean isLuckyPrice = false;
		int luckyPriceCount = 0;

		public TestWishTask(KRole role) {
			super();
			this.role = role;
			KGambleRoleExtData extData = KGambleRoleExtCACreator.getGambleRoleExtData(role.getId());
			if (extData != null) {
				KRoleWishData data = extData.getWishData();
				if (data != null) {
					data.luckyPricePassDay = KWishSystemManager.MAX_LUCKY_PRICE_PASS_DAY;
					extData.addWishDiceCount(testDebugDiceCount, false, false);
				}
			}
			System.err.println("$$$ 角色=" + role.getId() + "开始投掷测试！");
		}

		@Override
		public String getName() {
			return TestWishTask.class.getName();
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal timeSignal) throws KGameServerException {
			if (KGambleModule.getWishSystemManager().processThrowDiceDubugTest(role, false, diceTime)) {
				isLuckyPrice = true;
				luckyPriceCount++;
			}
			return null;
		}

		@Override
		public void done(KGameTimeSignal timeSignal) {
			if (diceTime <= testDebugDiceCount) {
				if (diceTime % 100 == 0) {
					System.err.println("$$$ 角色=" + role.getId() + "完成第" + diceTime + "次投掷测试！");
				}
				diceTime++;
				KGame.getTimer().newTimeSignal(this, 1, TimeUnit.SECONDS);
			} else {
				System.err.println("$$$ 角色=" + role.getId() + "投掷测试完成！是否中奖=" + isLuckyPrice + ",中奖次数：" + luckyPriceCount);
			}
		}

		@Override
		public void rejected(RejectedExecutionException e) {

		}

	}

}
