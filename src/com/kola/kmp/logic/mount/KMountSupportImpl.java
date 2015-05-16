package com.kola.kmp.logic.mount;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.combat.api.ICombatMount;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountTemplate;
import com.kola.kmp.logic.mount.message.KPushMountMsg;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.MountModuleSupport;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-2-21 下午4:59:04
 * </pre>
 */
public class KMountSupportImpl implements MountModuleSupport {

	public List<Integer> getMountRoleIniSkills(long roleId) {
		KMountSet set = KMountModuleExtension.getMountSet(roleId);
		set.rwLock.lock();
		try {
			KMount mount = set.getMountInUsed();
			if (mount == null) {
				return Collections.emptyList();
			}

			KMountTemplate temp = mount.getTemplate();
			if (temp == null) {
				return Collections.emptyList();
			}
			return temp.equiIdList;
		} finally {
			set.rwLock.unlock();
		}
	}

	@Override
	public int getRoleMountResId(KRole role) {
		KMountSet set = KMountModuleExtension.getMountSet(role.getId());
		set.rwLock.lock();
		try {
			KMount mount = set.getMountInUsed();
			if (mount == null) {
				return -1;
			}

			return mount.getInMapResId();
		} finally {
			set.rwLock.unlock();
		}
	}

	@Override
	public ICombatMount getMountCanWarOfRole(long roleId) {
		KMountSet set = KMountModuleExtension.getMountSet(roleId);
		if (set == null) {
			return null;
		}
		return set.getMountInUsed();
	}
	
	@Override
	public KMount getMountOfRole(long roleId) {
		KMountSet set = KMountModuleExtension.getMountSet(roleId);
		if (set == null) {
			return null;
		}
		return set.getMountInUsed();
	}
	
	public void notifyStartMountForNewRole(long roleId) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		if (role != null) {
			KMountLogic.giveMountToNewRole(role);
		}
	}

	public void notifyCancelMountFromNewRole(long roleId) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		if (role != null) {
			KMountLogic.cancelMountFromNewRole(role);
		}
	}

	public void notifyStartMount(long roleId) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		if (role != null) {
			KMountLogic.presentMountForLv(role);
		}
	}

	@Override
	public void packMountDataToMsgForOtherRole(KGameMessage msg, KRole role) {
		KMountMsgPackCenter.packMountForOtherRole(msg, role);
	}

	@Override
	public float getRoleMountMoveSpeedup(KRole role) {
		KMountSet set = KMountModuleExtension.getMountSet(role.getId());
		KMount mount = set.getMountInUsed();
		if (mount == null) {
			return 1f;
		}

		KMountTemplate temp = mount.getTemplate();
		if (temp == null) {
			return 1f;
		}
		return temp.cityMoveSpeedup;
	}

	public KActionResult<KMount> presentMount(KRole role, KMountTemplate temp, String sourceTips){
		return KMountLogic.presentMount(role, temp, sourceTips);
	}

	@Override
	public void cloneRoleByCamus(KRole myRole, KRole srcRole) {

		KMountSet srcset = KMountModuleExtension.getMountSet(srcRole.getId());
		KMountSet myset = KMountModuleExtension.getMountSet(myRole.getId());

		{
			for(KMount srcmount:srcset.getMountCache().values()){
				KMount mymount = myset.getMountByModel(srcmount.getTemplate().Model);
				if(mymount==null){
					mymount = new KMount(myset, srcmount.getTemplate());
					myset.addMount(mymount);
				}
				mymount.cloneByCamus(srcmount);
			}
		}
		
		myset.setUsedModelId(srcset.getUsedModelId());

		// 同步机甲数据
		KPushMountMsg.SM_PUSH_MOUNTDATA(myRole);

		// 刷新角色属性
		KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(myRole.getId(), KMountAttributeProvider.getType());

		// 通知地图模块
		KSupportFactory.getMapSupport().notifyMountStatus(myRole.getId(), myset.getMountInUsed()!=null, myset.getMountInUsed().getInMapResId());
	}
	
	public KMountTemplate getMountTemplate(int mountTempId){
		return KMountDataManager.mMountTemplateManager.getTemplate(mountTempId);
	}
}
