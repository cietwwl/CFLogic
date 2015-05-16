package com.kola.kmp.logic.rank;

import java.util.List;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.KGameMessage;
import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.competition.KCompetitor;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.other.KRankTypeEnum;
import com.kola.kmp.logic.pet.KPet;
import com.kola.kmp.logic.rank.abs.ElementAbs;
import com.kola.kmp.logic.rank.teampvp.KTeamPVPMsgPackCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.rank.KRankProtocol;

/**
 * <pre>
 * 由于在打包模块消息的过程中，涉及到访问及复杂的逻辑结构
 * 因此设立此类，用于打包消息
 * 
 * @author CamusHuang
 * @creation 2013-1-5 下午12:03:37
 * </pre>
 */
public class KRankMsgPackCenter {

	public static final Logger _LOGGER = KGameLogger.getLogger(KRankMsgPackCenter.class);

	/**
	 * <pre>
	 * 打包冠军榜
	 * 参考{@link KRankProtocol#SM_GET_TOPRANK_RESULT}
	 * 
	 * @param msg
	 * @param role
	 * @author CamusHuang
	 * @creation 2014-3-27 下午4:57:36
	 * </pre>
	 */
	public static void packTopRank(KGameMessage msg) {
		{
			long delayTime = (RankTaskManager.RankPublishTask.getInstance().getNextTime() - System.currentTimeMillis()) / Timer.ONE_SECOND;
			if (delayTime < 1) {
				delayTime = 1;
			}
			msg.writeInt((int) delayTime);
		}
		packTopElement(msg, KSupportFactory.getCompetitionModuleSupport().getCompetitor(1));
		packTopElement(msg, KRankLogic.aresRank.getPublishData().getElementByRank(1));
		packTopElement(msg, KRankLogic.levelRank.getPublishData().getElementByRank(1));
		packTopElement(msg, KRankLogic.petRank.getPublishData().getElementByRank(1));
	}

	private static void packTopElement(KGameMessage msg, Object e) {
		if (e == null) {
			msg.writeBoolean(false);
			return;
		}

		if (e instanceof KCompetitor) {
			packTopElement(msg, ((KCompetitor) e).getRoleId(), (byte)((KCompetitor) e).getVipLv(), false);
		} else if (e instanceof RankElementPet){
			packTopElement(msg, ((ElementAbs) e).elementId, ((RankElementAbs) e).getVipLv(), true);
		} else {
			packTopElement(msg, ((ElementAbs) e).elementId, ((RankElementAbs) e).getVipLv(), false);
		}
	}

	/**
	 * <pre>
	 * 参考{@link KRankProtocol#角色冠军榜元素数据}
	 * 
	 * @param msg
	 * @param e
	 * @author CamusHuang
	 * @creation 2014-4-1 上午10:46:47
	 * </pre>
	 */
	private static void packTopElement(KGameMessage msg, long roleId, byte vipLv, boolean isUsePetResId) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		if (role == null) {
			msg.writeBoolean(false);
			return;
		}
		KRoleRankData rankData = KRankRoleExtCACreator.getRoleRankData(roleId);
		if (rankData == null) {
			msg.writeBoolean(false);
			return;
		}
		msg.writeBoolean(true);
		msg.writeLong(roleId);
		msg.writeUtf8String(role.getName());
		{
			//主形象ID
			int mapResId = role.getInMapResId();
			if (isUsePetResId) {
				KPet pet = KSupportFactory.getPetModuleSupport().getFightingPet(roleId);
				if (pet != null) {
					mapResId = pet.getInMapResId();
				}
			}
			msg.writeInt(mapResId);
		}
		msg.writeShort(role.getLevel());
		msg.writeByte(vipLv);
		msg.writeInt(role.getBattlePower());
		msg.writeInt(rankData.getGood());
		int[] setResIds = KSupportFactory.getItemModuleSupport().getEquiSetMapResIds(roleId);
		msg.writeInt(setResIds[1]);
		msg.writeInt(setResIds[0]);
		msg.writeBoolean(KSupportFactory.getItemModuleSupport().isRedWepond(roleId));
		//
		KSupportFactory.getRoleModuleSupport().packRoleResToMsg(role, role.getJob(), msg);
	}

	/**
	 * <pre>
	 * 参考{@link KRankProtocol#SM_GET_INI_RANKS_RESULT}
	 * 
	 * @param msg
	 * @param role
	 * @param numPerPage
	 * @param pageNum
	 * @author CamusHuang
	 * @creation 2014-4-1 上午10:50:19
	 * </pre>
	 */
	public static void packIniRanks(KGameMessage msg, KRole role, int numPerPage, int pageNum) {
		msg.writeByte(KRankTypeEnum.size());
		for (KRankTypeEnum RType : KRankTypeEnum.values()) {
			packRank(msg, role, RType, 1, numPerPage, pageNum);
		}
	}

	/**
	 * <pre>
	 * 打包排行榜数据
	 * 参考{@link KRankProtocol#角色排行榜数据}
	 * 
	 * @param msg
	 * @param role
	 * @param rankType
	 * @param startPage
	 * @param numPerPage
	 * @param pageNum
	 * @author CamusHuang
	 * @creation 2014-3-27 下午4:53:55
	 * </pre>
	 */
	public static void packRank(KGameMessage msg, KRole role, KRankTypeEnum rankType, int startPage, int numPerPage, int pageNum) {
		int startIndex = (startPage - 1) * numPerPage;// 起始位置
		int endIndex = startIndex + pageNum * numPerPage;// 结束位置（不包含）
		//
		msg.writeByte(rankType.sign);
		if (rankType == KRankTypeEnum.竞技榜) {
			KCompetitor rankE = KSupportFactory.getCompetitionModuleSupport().getCompetitor(role.getId());
			int myRank = rankE == null ? -1 : rankE.getRanking();
			//
			List<KCompetitor> list = KSupportFactory.getCompetitionModuleSupport().getCurrentRanks(rankType.getMaxLen());
			packElementList(msg, myRank, list, startIndex, endIndex);
		} else {
			Rank rank = KRankLogic.getRank(rankType);
			rank.rwlock.readLock().lock();
			try {
				RankElementAbs rankE = (RankElementAbs) rank.getPublishData().getElement(role.getId());
				int myRank = rankE == null ? -1 : rankE.getRank();
				//
				List<RankElementAbs> list = rank.getPublishData().getUnmodifiableElementList();
				packElementList(msg, myRank, list, startIndex, endIndex);
			} finally {
				rank.rwlock.readLock().unlock();
			}
		}
	}

	private static void packElementList(KGameMessage msg, int myRank, List elementList, int startIndex, int endIndex) {
		msg.writeShort(myRank);
		//
		if (startIndex >= elementList.size()) {
			msg.writeShort(0);
			return;
		}
		if (endIndex > elementList.size()) {
			endIndex = elementList.size();
		}
		msg.writeShort(endIndex - startIndex);
		for (int index = startIndex; index < endIndex; index++) {
			Object e = elementList.get(index);
			if (e instanceof RankElementAbs) {
				packElement(msg, (RankElementAbs) e);
			} else {
				packElement(msg, (KCompetitor) e);
			}
		}
	}

	/**
	 * <pre>
	 * 参考{@link KRankProtocol#角色排行榜元素基本数据}
	 * 
	 * @param msg
	 * @param e
	 * @author CamusHuang
	 * @creation 2014-4-1 上午10:47:12
	 * </pre>
	 */
	private static void packElement(KGameMessage msg, RankElementAbs e) {
		msg.writeShort(e.getRank());
		msg.writeLong(e.elementId);
		msg.writeUtf8String(e.elementName);
		msg.writeByte(e.getVipLv());
		msg.writeByte(e.job);
		
		if (e instanceof RankElementPet) {
			msg.writeShort(((RankElementPet)e).getPetLv());
			msg.writeInt(((RankElementPet)e).getPetPow());
			msg.writeUtf8String(((RankElementPet)e).getPetName());
		} else {
			msg.writeShort(e.getElementLv());
			msg.writeInt(e.getBattlePower());
			msg.writeUtf8String(e.getGangName());
		}
	}

	private static void packElement(KGameMessage msg, KCompetitor e) {
		msg.writeShort(e.getRanking());
		msg.writeLong(e.getRoleId());
		msg.writeUtf8String(e.getRoleName());
		msg.writeByte(e.getVipLv());
		msg.writeByte(e.getOccupation());
		msg.writeShort(e.getRoleLevel());
		msg.writeInt(e.getFightPower());
		msg.writeUtf8String(KSupportFactory.getGangSupport().getGangNameByRoleId(e.getRoleId()));
	}

	public static void packRanksForGM(List<String> infos, KRankTypeEnum rankType) {
		
		int showGMLen = 100;
		
		if (rankType == KRankTypeEnum.竞技榜) {
			StringBuffer sbf = new StringBuffer();
			sbf.append("排名").append('\t').append("角色ID").append('\t').append("角色名").append('\t').append("职业").append('\t').append("等级").append('\t').append("战力");
			infos.add(sbf.toString());

			// 竞技场
			List<KCompetitor> ranks = KSupportFactory.getCompetitionModuleSupport().getCurrentRanks(showGMLen);

			for (KCompetitor rankE : ranks) {
				sbf = new StringBuffer();
				sbf.append(rankE.getRanking()).append('\t').append(rankE.getRoleId()).append('\t').append(rankE.getRoleName()).append('\t')
						.append(KJobTypeEnum.getJobName(rankE.getOccupation())).append('\t').append(rankE.getRoleLevel()).append('\t')
						.append(rankE.getFightPower());
				infos.add(sbf.toString());
			}

		} else if (rankType == KRankTypeEnum.随从榜) {
			StringBuffer sbf = new StringBuffer();
			sbf.append("排名").append('\t').append("角色ID").append('\t').append("角色名").append('\t').append("随从").append('\t').append("随从战力").append('\t').append("随从等级");
			List ranks = KRankLogic.petRank.getPublishData().getUnmodifiableElementList();
			infos.add(sbf.toString());
			//
			int toIndex = Math.min(showGMLen, ranks.size());
			ranks = ranks.subList(0, toIndex);

			RankElementAbs rankE;
			for (Object obj : ranks) {
				rankE = (RankElementAbs) obj;
				sbf = petRankElementToStringForGM(rankE);
				sbf.append('\t').append(((RankElementPet)rankE).getPetName());
				sbf.append('\t').append(((RankElementPet)rankE).getPetPow());
				sbf.append('\t').append(((RankElementPet)rankE).getPetLv());
				infos.add(sbf.toString());
			}
		} else {

			StringBuffer sbf = new StringBuffer();
			sbf.append("排名").append('\t').append("角色ID").append('\t').append("角色名").append('\t').append("职业").append('\t').append("战力").append('\t').append("等级");
			List ranks = null;
			switch (rankType) {
			case 战力榜:
				ranks = KRankLogic.aresRank.getPublishData().getUnmodifiableElementList();
				break;
			case 等级榜:
				sbf.append('\t').append("经验");
				ranks = KRankLogic.levelRank.getPublishData().getUnmodifiableElementList();
				break;
			}
			infos.add(sbf.toString());
			//
			int toIndex = Math.min(showGMLen, ranks.size());
			ranks = ranks.subList(0, toIndex);

			RankElementAbs rankE;
			for (Object obj : ranks) {
				rankE = (RankElementAbs) obj;
				sbf = rankElementToStringForGM(rankE);
				switch (rankType) {
				case 战力榜:
					break;
				case 等级榜:
					sbf.append('\t').append(((RankElementLevel)rankE).getExp());
					break;
				}
				infos.add(sbf.toString());
			}
		}
	}

	private static StringBuffer petRankElementToStringForGM(RankElementAbs rankE) {
		// 排名，角色ID，角色
		StringBuffer sbf = new StringBuffer();
		sbf.append(rankE.getRank()).append('\t').append(rankE.elementId).append('\t').append(rankE.elementName);

		return sbf;
	}
	
	private static StringBuffer rankElementToStringForGM(RankElementAbs rankE) {
		// 排名，角色ID，角色，职业，战力，等级，
		StringBuffer sbf = new StringBuffer();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(rankE.elementId);
		sbf.append(rankE.getRank()).append('\t').append(rankE.elementId).append('\t').append(rankE.elementName).append('\t').append(role == null ? "-" : KJobTypeEnum.getJobName((byte)role.getType()))
				.append('\t').append(rankE.getBattlePower()).append('\t').append(rankE.getElementLv());

		return sbf;
	}
}
