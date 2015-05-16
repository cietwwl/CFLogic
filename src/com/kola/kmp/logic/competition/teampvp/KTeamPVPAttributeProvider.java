package com.kola.kmp.logic.competition.teampvp;

import java.util.Collections;
import java.util.Map;

import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.role.IRoleAttributeProvider;
import com.kola.kmp.logic.role.KRole;

/**
 * 
 * @author PERRY CHAN
 */
public class KTeamPVPAttributeProvider implements IRoleAttributeProvider {

	private static int _type;
	
	public static final int getType() {
		return _type;
	}
	
	@Override
	public Map<KGameAttrType, Integer> getEffectAttr(KRole role) {
		KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(role.getId());
		if (team != null) {
			return team.getDanStageInfo().attribute;
		}
		return Collections.emptyMap();
	}

	@Override
	public void notifyTypeAssigned(int type) {
		_type = type;
	}

}
