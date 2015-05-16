package com.kola.kmp.logic.gang;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.koala.game.KGameMessage;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.gang.GangIntegrateData;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.chat.ChatDataAbs;
import com.kola.kmp.logic.gang.KGangDataStruct.GangTechTemplate;
import com.kola.kmp.logic.gang.message.KSyncGangDataMsg;
import com.kola.kmp.logic.gang.war.KGangWarDataManager;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KGangMedalDataManager.GangMedalData;
import com.kola.kmp.logic.other.KGangTecTypeEnum;
import com.kola.kmp.logic.rank.gang.KGangRankLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.GangModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.GangTips;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-2-21 下午4:59:04
 * </pre>
 */
public class KGangSupportImpl implements GangModuleSupport {

	@Override
	public KGang getGang(long gangId) {
		return KGangModuleExtension.getGang(gangId);
	}

	@Override
	public KGang getGangByRoleId(long roleId) {
		return KGangLogic.getGangByRoleId(roleId);
	}

	@Override
	public String getGangNameByRoleId(long roleId) {
		KGang gang = KGangLogic.getGangByRoleId(roleId);
		if (gang == null) {
			return UtilTool.getNotNullString(null);
		}
		return gang.getName();
	}

	@Override
	public long getGangIdByRoleId(long roleId) {
		return KGangLogic.getGangIdByRoleId(roleId);
	}

	@Override
	public int broadcastChatToGang(ChatDataAbs chatData, KGameMessage msg) {
		// 根据发送者角色ID chatData.receiverId 找到相应军团，遍历军团成员发送聊天内容
		KGang gang = KGangModuleExtension.getGang(chatData.receiverId);
		if (gang == null) {
			return 0;
		}
		int count = KGangMsgPackCenter.sendChatMsgToMemebers(msg, gang);
		// 通知GM
		KSupportFactory.getGMSupport().onChat(chatData, gang.getName());
		return count;
	}

	@Override
	public boolean addGangExp(long gangId, int addExp, int addResource) {
		if (addExp < 1) {
			return true;
		}
		KGang gang = KGangModuleExtension.getGang(gangId);
		if (gang == null) {
			return false;
		}
		gang.rwLock.lock();
		try {
			// 增加军团资金、经验
			gang.changeResource(addResource);
			gang.setExp(gang.getExp() + addExp);

			return true;
		} finally {
			gang.rwLock.unlock();

			// 尝试进行升级
			KGangLogic.tryToUplvGang(gang);

			// 更新全体成员：军团基础信息
			KSyncGangDataMsg.sendMsg(gang);
		}
	}

	public void notifyVitalityAdd(KRole role, int addVitality) {
		KGang gang = KGangLogic.getGangByRoleId(role.getId());
		if (gang == null) {
			return;
		}
		KGangLogic.addGangFlourish(role, gang, KGangLogic.ExpressionForFlourish(null, addVitality, role.getLevel()), true);
	}
	
	public void notifyRoleBattlePowerChange(KRole role, int newBattlePow) {
		KGang gang = KGangLogic.getGangByRoleId(role.getId());
		if (gang == null) {
			return;
		}
		// 通知军团战榜
		KGangRankLogic.notifyGangBattlePow(gang, -1);
	}

	public CommonResult changeGangResource(long gangId, int changeValue) {

		CommonResult result = new CommonResult();

		KGang gang = KGangModuleExtension.getGang(gangId);
		if (gang == null) {
			result.tips = GangTips.不存在此军团;
			return result;
		}
		gang.rwLock.lock();
		try {
			int temp = Math.abs(changeValue);
			if (changeValue < 0) {
				if (gang.getResource() < temp) {
					result.tips = StringUtil.format(GangTips.军团资金不足x, temp);
					return result;
				}
			}
			// 增减军团资金
			gang.changeResource(changeValue);

			result.isSucess = true;
			if (changeValue < 0) {
				result.tips = StringUtil.format(GangTips.军团资金减x, temp);
			} else {
				result.tips = StringUtil.format(GangTips.军团资金加x, temp);
			}
			return result;
		} finally {
			gang.rwLock.unlock();

			if (result.isSucess) {
				// 更新全体成员：军团基础信息
				KSyncGangDataMsg.sendMsg(gang);
			}
		}
	}

	public int getMedalIcon(long roleId) {
		KGang gang = KGangLogic.getGangByRoleId(roleId);
		if (gang == null) {
			return 0;
		}
		
		int medalId = -1;
		gang.rwLock.lock();
		try {
			KGangMember mem = gang.getMember(roleId);
			if (mem == null) {
				return 0;
			}
			
			medalId = mem.getMedal();
		} finally {
			gang.rwLock.unlock();
		}
		
		if (medalId < 0) {
			return 0;
		}
		
		GangMedalData medal = KGangWarDataManager.mGangMedalDataManager.getDataByRank(medalId);
		return medal == null ? 0 : medal.icon;
	}

	@Override
	public String getGangMapShowNameByRole(long roleId) {
		// <军团名>职位
		KGang gang = KGangLogic.getGangByRoleId(roleId);
		if (gang == null) {
			return UtilTool.getNotNullString(null);
		}
		gang.rwLock.lock();
		try {
			KGangMember mem = gang.getMember(roleId);
			if (mem == null) {
				return UtilTool.getNotNullString(null);
			}
			return StringUtil.format(GangTips.军团名x职位x, gang.getExtName(), mem.getPositionEnum().name);
		} finally {
			gang.rwLock.unlock();
		}
	}

	public int getGangEffect(KGangTecTypeEnum type, long roleId) {
		GangIntegrateData gangData = KGangLogic.getGangAndSetByRoleId(roleId);
		if (gangData == null) {
			return 0;
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();

		gang.rwLock.lock();
		try {

			GangTechTemplate temp = KGangDataManager.mGangTechDataManager.getData(type);
			if (temp == null) {
				return 0;
			}

			Map<Integer, Integer> techLvMap = set.getTechCache().getDataCache();
			Integer lv = techLvMap.get(temp.ID);
			int lvl = lv == null ? 0 : lv;

			return temp.getLevelData(lvl).effectValue;
		} finally {
			gang.rwLock.unlock();
		}
	}
	
	public String getGangEffectDescr(KGangTecTypeEnum type, long roleId) {
		GangIntegrateData gangData = KGangLogic.getGangAndSetByRoleId(roleId);
		if (gangData == null) {
			return "";
		}
		KGang gang = (KGang) gangData.getGang();
		KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();

		gang.rwLock.lock();
		try {

			GangTechTemplate temp = KGangDataManager.mGangTechDataManager.getData(type);
			if (temp == null) {
				return "";
			}

			Map<Integer, Integer> techLvMap = set.getTechCache().getDataCache();
			Integer lv = techLvMap.get(temp.ID);
			int lvl = lv == null ? 0 : lv;

			return temp.getLevelData(lvl).effectValueStr;
		} finally {
			gang.rwLock.unlock();
		}
	}
	
	public List<Long> searchPositions(long gangId, Set<KGangPositionEnum> positions) {
		KGang gang  = KGangModuleExtension.getGang(gangId);
		if(gang==null){
			return Collections.emptyList();
		}
		if(positions.isEmpty()){
			return gang.getAllElementRoleIds();
		} 
		
		return gang.searchPositions(positions);
	}
}
