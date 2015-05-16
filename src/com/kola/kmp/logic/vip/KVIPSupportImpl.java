package com.kola.kmp.logic.vip;

import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.OtherFlowTypeEnum;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.VIPModuleSupport;
import com.kola.kmp.logic.util.tips.VIPTips;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;
import com.kola.kmp.logic.vip.message.KSyncVipDataMsg;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-2-21 下午4:59:04
 * </pre>
 */
public class KVIPSupportImpl implements VIPModuleSupport {

	@Override
	public VIPLevelData getVIPLevelData(long roleId) {
		int viplv = getVipLv(roleId);
		return KVIPDataManager.mVIPLevelDataManager.getLevelData(viplv);
	}

	@Override
	public VIPLevelData getVIPLevelData(int viplv) {
		return KVIPDataManager.mVIPLevelDataManager.getLevelData(viplv);
	}

	@Override
	public int getVipLv(long roleId) {
		KRoleVIP vip = KVIPRoleExtCACreator.getRoleVIP(roleId);
		if(vip==null){
			return 0;
		}
		return vip.getLv();
	}

	public int getTotalCharge(long roleId) {
		KRoleVIP vip = KVIPRoleExtCACreator.getRoleVIP(roleId);
		if (vip == null) {
			return 0;
		}

		return vip.getTotalExp();
	}

	public int getVipExp(long roleId) {
		KRoleVIP vip = KVIPRoleExtCACreator.getRoleVIP(roleId);
		if (vip == null) {
			return 0;
		}

		return vip.getExp();
	}

	public void notifyCharge(KRole role, int money) {
		if (money < 1) {
			return;
		}

		boolean isUpLv = false;
		KRoleVIP vip = KVIPRoleExtCACreator.getRoleVIP(role.getId());
		//
		int oldLv = vip.getLv();
		int oldExp = vip.getExp();
		//
		vip.rwLock.lock();
		try {
			vip.setExp(oldExp + money);// 1:1
			vip.setTotalExp(vip.getTotalExp() + money);
			//
			isUpLv = KVIPLogic.tryToUpVIPLv(vip);
		} finally {
			vip.rwLock.unlock();

			// vip礼包自动领取
			if (isUpLv) {
				KVIPLogic.autoCollectVipLvReward(role);
			}

			// 同步给客户端
			KSyncVipDataMsg.sendMsg(role, vip);

			if (isUpLv) {
				// KSupportFactory.getFlowDataSupport().recrodVipInfo(oldLv,
				// vip.getLv());

				// 将VIP升级通知到各相关模块
				KVIPLogic.vipUpLvNotify(vip, oldLv);
				KDialogService.sendDataUprisingDialog(role.getId(), StringUtil.format(VIPTips.VIP升级到第x级, vip.getLv()));
			}

			// 财产流水
			String tips = StringUtil.format("充值:{};原等级:{};原经验:{};现等级:{};现经验:{}", money, oldLv, oldExp, vip.getLv(), vip.getExp());
			FlowManager.logOther(role.getId(), OtherFlowTypeEnum.VIP充值处理, tips);
		}
	}

	public void addVipUpLvListener(KVIPUpLvListener listener) {
		KVIPLogic.addVipUpLvListener(listener);
	}

	@Override
	public void cloneRoleByCamus(KRole myRole, KRole srcRole) {

		KRoleVIP myvip = KVIPRoleExtCACreator.getRoleVIP(myRole.getId());
		KRoleVIP srcvip = KVIPRoleExtCACreator.getRoleVIP(srcRole.getId());

		int oldLv = myvip.getLv();
		myvip.setLv(srcvip.getLv());
		myvip.setExp(srcvip.getExp());
		myvip.setTotalExp(srcvip.getTotalExp());
		myvip.clearLvReward();

		int maxLv = KVIPDataManager.mVIPLevelDataManager.getMaxLevel().lvl;
		for(int lv = KVIPDataManager.mVIPLevelDataManager.getMinLevel().lvl;lv<=maxLv;lv++){
			if(srcvip.isCollectedLvReward(lv)){
				myvip.collectedLvReward(lv);
			}
		}

		// vip礼包自动领取
		KVIPLogic.autoCollectVipLvReward(myRole);

		// 同步给客户端
		KSyncVipDataMsg.sendMsg(myRole, myvip);

		// 将VIP升级通知到各相关模块
		KVIPLogic.vipUpLvNotify(myvip, oldLv);
		KDialogService.sendDataUprisingDialog(myRole.getId(), StringUtil.format(VIPTips.VIP升级到第x级, myvip.getLv()));
	}

}
