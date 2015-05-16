package com.kola.kmp.logic.rank.gang;

import java.util.List;

import org.slf4j.Logger;

import com.koala.game.KGameMessage;
import com.koala.game.logging.KGameLogger;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.competition.KCompetitor;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangMember;
import com.kola.kmp.logic.gang.KGangPositionEnum;
import com.kola.kmp.logic.other.KRankTypeEnum;
import com.kola.kmp.logic.rank.KRankDataManager;
import com.kola.kmp.logic.rank.KRankDataStructs.KGangRankGoodReward;
import com.kola.kmp.logic.rank.KRankLogic;
import com.kola.kmp.logic.rank.KRankRoleExtCACreator;
import com.kola.kmp.logic.rank.KRoleRankData;
import com.kola.kmp.logic.rank.Rank;
import com.kola.kmp.logic.rank.RankElementAbs;
import com.kola.kmp.logic.rank.RankElementPet;
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
public class KGangRankMsgPackCenter {

	private static Logger _LOGGER = KGameLogger.getLogger(KGangRankMsgPackCenter.class);

	/**
	 * <pre>
	 * byte M排行榜数量
	 * for(0~M){
	 * 	参考{@link #角色排行榜数据}
	 * }
	 * 
	 * @param
	 * @author CamusHuang
	 * @creation 2013-6-11 上午10:53:32
	 * </pre>
	 */
	public static void packRanksForGM(List<String> infos, KGangRankTypeEnum rankType) {

		StringBuffer sbf = new StringBuffer();
		sbf.append("排名").append('\t').append("军团ID").append('\t').append("军团名称").append('\t').append("团长").append('\t').append("等级");
		List ranks = null;
		switch (rankType) {
		case 全部军团:
			ranks = KGangRankLogic.gangRank.getPublishData().getUnmodifiableElementList();
			break;
		case 军团战积分:
			sbf.append('\t').append("积分");
			ranks = KGangRankLogic.gangWarRank.getPublishData().getUnmodifiableElementList();
			break;
		case 军团战报名:
			sbf.append('\t').append("繁荣度");
			ranks = KGangRankLogic.gangWarSignUpRank.getPublishData().getUnmodifiableElementList();
			break;
		case 军团战力:
			sbf.append('\t').append("战力");
			ranks = KGangRankLogic.gangPowRank.getPublishData().getUnmodifiableElementList();
			break;
		}
		infos.add(sbf.toString());

		GangRankElementAbs rankE;
		for (Object obj : ranks) {
			rankE = (GangRankElementAbs) obj;
			sbf = rankElementToString(rankE);
			//
			switch (rankType) {
			case 全部军团:
				break;
			case 军团战积分:
				sbf.append('\t').append(((GangRankElementWar) rankE).getWarScore()).append('\n');
				break;
			case 军团战报名:
				sbf.append('\t').append(((GangRankElementWarSignUp) rankE).getFlourish()).append('\n');
				break;
			case 军团战力:
				sbf.append('\t').append(((GangRankElementPower) rankE).getBattlePow()).append('\n');
				break;
			}
			infos.add(sbf.toString());
		}
	}

	private static StringBuffer rankElementToString(GangRankElementAbs rankE) {
		// 排名，ID，名称，团长，等级
		StringBuffer sbf = new StringBuffer();
		KGang gang = KSupportFactory.getGangSupport().getGang(rankE.elementId);
		KGangMember mem = null;
		if (gang != null) {
			mem = gang.searchPosition(KGangPositionEnum.军团长);
		}
		sbf.append(rankE.getRank()).append('\t').append(rankE.elementId).append('\t').append(rankE.elementName).append('\t').append(mem == null ? "-" : mem.getRoleName()).append('\t')
				.append(rankE.getElementLv());

		return sbf;
	}

	/**
	 * <pre>
	 * 打包战力榜冠军
	 * 参考{@link KRankProtocol#SM_GET_TOPRANK_RESULT}
	 * 
	 * @param msg
	 * @param role
	 * @author CamusHuang
	 * @creation 2014-3-27 下午4:57:36
	 * </pre>
	 */
	public static void packTopElement(KGameMessage msg) {
		
		GangRankElementPower e = KGangRankLogic.gangPowRank.getPublishData().getElementByRank(1);
		
		if (e == null) {
			msg.writeBoolean(false);
			return;
		}
		/**
		 * <pre>
		 * long 角色ID
		 * String 角色昵称
		 * int 角色形象资源ID
		 * short 角色LV
		 * byte 角色VIP等级
		 * int 战力
		 * int 被赞次数
		 * int 宝石套装地图资源ID（0表示没有）
		 * int 升星套装地图资源ID（0表示没有）
		 * {@link com.kola.kmp.protocol.fight.KFightProtocol#BATTLE_EQUIPMENT_INFO}
		 * </pre>
		 */
		long gangId = e.elementId;
		KGang gang = KSupportFactory.getGangSupport().getGang(gangId); 
		if(gang==null){
			msg.writeBoolean(false);
			return;
		}
		KGangMember mem = gang.searchPosition(KGangPositionEnum.军团长);
		if(mem==null){
			msg.writeBoolean(false);
			return;
		}
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(mem._roleId);
		if (role == null) {
			msg.writeBoolean(false);
			return;
		}
		KRoleRankData rankData = KRankRoleExtCACreator.getRoleRankData(mem._roleId);
		if (rankData == null) {
			msg.writeBoolean(false);
			return;
		}
		msg.writeBoolean(true);
		msg.writeLong(mem._roleId);
		msg.writeUtf8String(role.getName());
		msg.writeInt(role.getInMapResId());
		msg.writeShort(role.getLevel());
		msg.writeByte(KSupportFactory.getVIPModuleSupport().getVipLv(mem._roleId));
		msg.writeInt(role.getBattlePower());
		msg.writeInt(rankData.getGood());
		int[] setResIds = KSupportFactory.getItemModuleSupport().getEquiSetMapResIds(mem._roleId);
		msg.writeInt(setResIds[1]);
		msg.writeInt(setResIds[0]);
		msg.writeBoolean(KSupportFactory.getItemModuleSupport().isRedWepond(role.getId()));
		//
		KSupportFactory.getRoleModuleSupport().packRoleResToMsg(role, role.getJob(), msg);
		//
		msg.writeUtf8String(gang.getName());
	}

	public static void packRank(KGameMessage msg, KRole role, KGangRankTypeEnum type, int numPerPage, int startPage, int pageNum) {
		int startIndex = (startPage - 1) * numPerPage;// 起始位置
		int endIndex = startIndex + pageNum * numPerPage;// 结束位置（不包含）
		//
		/**
		 * <pre>
		 * byte 排行榜类型（13军团战力榜）
		 * {
		 *  short 我的排名（-1表示未上榜）
		 *  short 排行榜元素数量N
		 *  for(0~N){
		 *  	参考{@link KRankProtocol#军团战力榜元素基本数据}
		 *  }
		 * }
		 * </pre>
		 */
		long myGangId = KSupportFactory.getGangSupport().getGangIdByRoleId(role.getId());
		
		msg.writeByte(type.sign);
		{
			GangRank<GangRankElementPower> rank = KGangRankLogic.getRank(type);
			rank.rwlock.readLock().lock();
			try {
				GangRankElementPower rankE = rank.getPublishData().getElement(myGangId);
				int myRank = rankE == null ? -1 : rankE.getRank();
				//
				List<GangRankElementPower> list = rank.getPublishData().getUnmodifiableElementList();
				packElementList(msg, myRank, list, startIndex, endIndex);
			} finally {
				rank.rwlock.readLock().unlock();
			}
		}
	}

	public static void packIniRanks(KGameMessage msg, KRole role, int numPerPage, int pageNum) {
		msg.writeByte(1);
		packRank(msg, role, KGangRankTypeEnum.军团战力, numPerPage, 1, pageNum);
	}
	
	private static void packElementList(KGameMessage msg, int myRank, List<GangRankElementPower> elementList, int startIndex, int endIndex) {
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
			packElement(msg, elementList.get(index));
		}
	}

	/**
	 * <pre>
	 * 参考{@link KRankProtocol#军团战力榜元素基本数据}
	 * 
	 * @param msg
	 * @param e
	 * @author CamusHuang
	 * @creation 2014-4-1 上午10:47:12
	 * </pre>
	 */
	private static void packElement(KGameMessage msg, GangRankElementPower e) {
		/**
		 * <pre>
		 * short 排名
		 * long 军团ID
		 * String　军团名称
		 * long 军团长角色ID
		 * String 军团长角色昵称
		 * short 军团等级
		 * long 总战力
		 * int 每日奖励军团资金
		 * </pre>
		 */
		msg.writeShort(e.getRank());
		msg.writeLong(e.elementId);
		msg.writeUtf8String(e.elementName);
		{
			long sirRoleId = 0;
			String sirName = UtilTool.getNotNullString(null);
			KGang gang = KSupportFactory.getGangSupport().getGang(e.elementId); 
			if(gang!=null){
				KGangMember mem = gang.searchPosition(KGangPositionEnum.军团长);
				if(mem!=null){
					sirRoleId = mem._roleId;
					sirName = mem.getRoleName();
				}
			}
			msg.writeLong(sirRoleId);
			msg.writeUtf8String(sirName);
		}
		
		msg.writeShort(e.getElementLv());
		msg.writeLong(e.getBattlePow());
		{
			int reward = 0;
			KGangRankGoodReward data = KRankDataManager.mGangGoodRewardManager.getData(e.getRank());
			if(data!=null){
				reward = data.Funds;
			}
			msg.writeInt(reward);
		}
		
	}	
}
