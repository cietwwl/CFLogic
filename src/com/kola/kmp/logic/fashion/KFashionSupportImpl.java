package com.kola.kmp.logic.fashion;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.kola.kmp.logic.fashion.KFashionDataStructs.KFashionTemplate;
import com.kola.kmp.logic.fashion.KRoleFashion.FashionData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.FashionModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-2-21 下午4:59:04
 * </pre>
 */
public class KFashionSupportImpl implements FashionModuleSupport {

	@Override
	public KFashionTemplate getFashionTemplate(int fashionTempId) {
		return KFashionDataManager.mFashionTemplateManager.getFashionTemplate(fashionTempId);
	}

	@Override
	public CommonResult addFashions(KRole role, List<Integer> fashionTempIds, String sourceTips) {
		return KFashionLogic.addFashions(role, fashionTempIds, sourceTips);
	}

	@Override
	public String getFashingResId(long roleId) {
		KRoleFashion set = KFasionRoleExtCACreator.getRoleFashion(roleId);
		if (set != null) {
			if (set.getSelectedFashionId() > 0) {
				KFashionTemplate temp = KFashionDataManager.mFashionTemplateManager.getFashionTemplate(set.getSelectedFashionId());
				if (temp != null) {
					return temp.res_id;
				}
			}
		}
		return "";
	}

	@Override
	public List<KFashionTemplate> getAllFashionTemplate() {
		return KFashionDataManager.mFashionTemplateManager.getFashionTemplateList();
	}

	@Override
	public void cloneRoleByCamus(KRole myRole, KRole srcRole) {
		KRoleFashion myset = KFasionRoleExtCACreator.getRoleFashion(myRole.getId());
		HashMap<Integer, FashionData> myMap = myset.getAllFashionsCacha();

		KRoleFashion srcset = KFasionRoleExtCACreator.getRoleFashion(srcRole.getId());
		HashMap<Integer, FashionData> srcMap = srcset.getAllFashionsCacha();

		// 剔除多余的时装
		for (Iterator<FashionData> it = myMap.values().iterator(); it.hasNext();) {
			FashionData data = it.next();
			if (!srcMap.containsKey(data.tempId)) {
				it.remove();
			}
		}

		for (FashionData data : srcMap.values()) {
			FashionData my = myMap.get(data.tempId);
			if (my == null) {
				KFashionLogic.addFashion(myRole, data.tempId, "复制角色");
				my = myMap.get(data.tempId);

			}
			my.setEndTime(data.getEndTime());
		}
		
		myset.setSelectedFashionId(srcset.getSelectedFashionId());
		
		myset.notifyDB();
		
		
		// 刷新角色属性
		KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(myRole.getId(), KFashionAttributeProvider.getType());
		// 刷新UI
		KSupportFactory.getMapSupport().notifyFashionStatus(myRole.getId(), KSupportFactory.getFashionModuleSupport().getFashingResId(myRole.getId()));
		// 刷新UI
		KSupportFactory.getRoleModuleSupport().updateFashionRes(myRole.getId());
		KSupportFactory.getTeamPVPSupport().notifyRoleFashionUpdate(myRole.getId());
	}

}
