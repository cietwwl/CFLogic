package com.kola.kmp.logic.map.duplicatemap;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import com.koala.game.exception.KGameServerException;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kmp.logic.map.KGameMapEntity;
import com.kola.kmp.logic.other.KMapEntityTypeEnum;

public class CollisionEventDataStateChangeSyncTask implements KGameTimerTask {
	public static final byte STATE_TYPE_BORN = 1;
	public static final byte STATE_TYPE_LEAVE = 2;
	private KDuplicateMap duplicateMap;
	private long eventInstanceId;
	private byte stateType;
	private float corX;
	private float corY;

	public CollisionEventDataStateChangeSyncTask(KDuplicateMap duplicateMap,
			long eventInstanceId, byte stateType, float corX, float corY) {
		this.duplicateMap = duplicateMap;
		this.eventInstanceId = eventInstanceId;
		this.stateType = stateType;
		this.corX = corX;
		this.corY = corY;
	}

	@Override
	public String getName() {
		return CollisionEventDataStateChangeSyncTask.class.getName();
	}

	@Override
	public Object onTimeSignal(KGameTimeSignal timeSignal)
			throws KGameServerException {
		if (stateType == STATE_TYPE_BORN) {
			List<KGameMapEntity> roleList = duplicateMap
					.getEntities(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE);
			for (KGameMapEntity roleE : roleList) {
				if (roleE != null) {
					roleE.sendOtherEntityBornIntoMapData(
							KMapEntityTypeEnum.ENTITY_TYPE_MONSTER,
							eventInstanceId, corX, corY, null, null);
				}
			}
		} else if (stateType == STATE_TYPE_LEAVE) {
			List<KGameMapEntity> roleList = duplicateMap
					.getEntities(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE);
			for (KGameMapEntity roleE : roleList) {
				if (roleE != null) {
					roleE.sendOtherEntityLeaveMapData(
							KMapEntityTypeEnum.ENTITY_TYPE_MONSTER,
							eventInstanceId, false, -1);
				}
			}
		}
		return null;
	}

	@Override
	public void done(KGameTimeSignal timeSignal) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rejected(RejectedExecutionException e) {
		// TODO Auto-generated method stub

	}

}
