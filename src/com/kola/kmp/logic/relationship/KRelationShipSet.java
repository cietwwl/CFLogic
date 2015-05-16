package com.kola.kmp.logic.relationship;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.relationship.impl.KARelationShipSet;
import com.kola.kgame.db.dataobject.DBRelationShipData;
import com.kola.kmp.logic.other.KRelationShipTypeEnum;
import com.kola.kmp.logic.relationship.IntercourseCenter.MemStatusEnum;
import com.kola.kmp.logic.util.tips.RelationShipTips;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-5 下午8:14:02
 * </pre>
 */
public class KRelationShipSet extends KARelationShipSet<KRelationShip> {

	KRelationShipSet(long roleId, boolean isFirstNew) {
		super(roleId, isFirstNew);

		//
		Map<Integer, LinkedHashMap<Long, KRelationShip>> _elementMap = getRelationShipsCache();
		for (KRelationShipTypeEnum type : KRelationShipTypeEnum.values()) {
			if (!_elementMap.containsKey(type.sign)) {
				LinkedHashMap<Long, KRelationShip> temp = new LinkedHashMap<Long, KRelationShip>();
				_elementMap.put(type.sign, temp);
			}
		}
	}

	protected List<KRelationShip> initDBRelationShips(List<DBRelationShipData> dbdatas) {
		List<KRelationShip> result = new ArrayList<KRelationShip>();
		for (DBRelationShipData dbdata : dbdatas) {
			KRelationShip data = new KRelationShip(this, dbdata);
			result.add(data);
		}
		return result;
	}

	@Override
	protected void decodeCA(String jsonStr) {
		// CTODO
	}

	@Override
	protected String encodeCA() {
		// CTODO
		return null;
	}

	public boolean isRelationShipFull(KRelationShipTypeEnum type) {
		int size = countRelationShipSize(type.sign);
		//
		if (type == KRelationShipTypeEnum.好友) {
			return size >= KRelationShipLogic.getMaxFriendCount(_roleId);
		} else {
			return size >= type.getMaxNum();
		}
	}

	// 切磋数据
	private PVPData mPVPData = new PVPData();

	MemStatusEnum getPVPStatus() {
		return mPVPData.status;
	}

	void setPVPStatus(MemStatusEnum status) {
		mPVPData.status = status;
	}

	void addPVPScore(int addScore) {
		if(addScore<1){
			return;
		}
		mPVPData.score.addAndGet(addScore);
		notifyDB();
	}

	public int getPVPScore() {
		return mPVPData.score.get();
	}

	void notifyPVPResult(boolean isWin, String oppName) {
		rwLock.lock();
		try {
			mPVPData.timeCount.incrementAndGet();
			if (isWin) {
				mPVPData.winCount.incrementAndGet();
				oppName=StringUtil.format(RelationShipTips.x挑战了你已被打跑, oppName);
			} else {
				oppName=StringUtil.format(RelationShipTips.x挑战你取得胜利, oppName);
			}
			//
			mPVPData.oppNameList.addLast(oppName);
			if (mPVPData.oppNameList.size() > KRelationShipConfig.getInstance().切磋名单最大数量) {
				mPVPData.oppNameList.removeFirst();
			}
		} finally {
			rwLock.unlock();
		}
	}

	int getPVPCount() {
		return mPVPData.timeCount.get();
	}

	int getPVPWinCount() {
		return mPVPData.winCount.get();
	}

	List<String> getPVPNameList() {
		return mPVPData.oppNameList;
	}

	void clearPVPData() {
		rwLock.lock();
		try {
			mPVPData.timeCount.set(0);
			mPVPData.winCount.set(0);
			mPVPData.oppNameList.clear();
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 切磋数据 
	 * 
	 * @author CamusHuang
	 * @creation 2014-12-3 下午2:59:56
	 * </pre>
	 */
	private static class PVPData {
		MemStatusEnum status = MemStatusEnum.IN;
		AtomicInteger score = new AtomicInteger();
		//
		AtomicInteger timeCount = new AtomicInteger();
		AtomicInteger winCount = new AtomicInteger();
		LinkedList<String> oppNameList = new LinkedList<String>();
	}
}
