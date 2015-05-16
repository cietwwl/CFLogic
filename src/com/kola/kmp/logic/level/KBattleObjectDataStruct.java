package com.kola.kmp.logic.level;

import java.util.Comparator;

import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.npc.KNPCDataStructs.ObstructionTemplate;
import com.kola.kmp.logic.other.KBattleObjectTypeEnum;

public class KBattleObjectDataStruct {

	/**
	 * 战斗场景实体抽象类
	 * 
	 * @author Administrator
	 * 
	 */
	public static abstract class KBattleObject {
		/**
		 * 实体类型
		 */
		public KBattleObjectTypeEnum _objType;

		/**
		 * 战场中的实例ID
		 */
		public int _objInstanceId;

		/**
		 * x坐标
		 */
		public float _corX;
		/**
		 * y坐标
		 */
		public float _corY;

	}

	/**
	 * 表示战场上的怪物类型实体
	 * 
	 * @author Administrator
	 * 
	 */
	public static class MonsterData extends KBattleObject implements Comparable<MonsterData> {
		/**
		 * 怪物模版ID
		 */
		public KMonstTemplate _monsterTemplate;

		public MonsterData(int instanceId, KMonstTemplate monsterTemplate, float x, float y) {
			_objType = KBattleObjectTypeEnum.OBJ_TYPE_MONSTER;
			_monsterTemplate = monsterTemplate;
			_objInstanceId = instanceId;
			_corX = x;
			_corY = y;
		}

		@Override
		public int compareTo(MonsterData o) {
			if (o._corX < this._corX) {
				return 1;
			} else if (o._corX > this._corX) {
				return -1;
			} else {
				return 0;
			}
		}

	}

	/**
	 * 表示战场上的障碍物类型实体
	 * 
	 * @author Administrator
	 * 
	 */
	public static class ObstructionData extends KBattleObject {
		/**
		 * 障碍物模版ID
		 */
		public ObstructionTemplate _obsTemplate;

		public ObstructionData(int instanceId, ObstructionTemplate obsTemplate, float x, float y) {
			_objType = KBattleObjectTypeEnum.OBJ_TYPE_MONSTER;
			_obsTemplate = obsTemplate;
			_objInstanceId = instanceId;
			_corX = x;
			_corY = y;
		}
	}

	/**
	 * 表示战场上的出口连接点类型实体
	 * 
	 * @author Administrator
	 * 
	 */
	public static class ExitData extends KBattleObject {
		/**
		 * 出入口对应下个战场ID
		 */
		public int _nextBattlefieldId;

		public ExitData(int instanceId, int nextBattlefieldId, float x, float y) {
			_objType = KBattleObjectTypeEnum.OBJ_TYPE_MONSTER;
			_nextBattlefieldId = nextBattlefieldId;
			_objInstanceId = instanceId;
			_corX = x;
			_corY = y;
		}

	}

	/**
	 * 表示战场上的出生点类型实体
	 * 
	 * @author Administrator
	 * 
	 */
	public static class BornPointData extends KBattleObject implements Comparable<BornPointData> {

		public BornPointData(int instanceId, float x, float y) {
			_objType = KBattleObjectTypeEnum.OBJ_TYPE_BORN_POINT;
			_objInstanceId = instanceId;
			_corX = x;
			_corY = y;
		}

		@Override
		public int compareTo(BornPointData o) {
			if (o._corX < this._corX) {
				return 1;
			} else if (o._corX > this._corX) {
				return -1;
			} else {
				return 0;
			}
		}

	}

	/**
	 * 表示战场上的每一个分段的终点坐标信息
	 * 
	 * @author Administrator
	 * 
	 */
	public static class SectionPointData extends KBattleObject implements Comparable<SectionPointData> {

		public SectionPointData(int instanceId, float x, float y) {
			_objType = KBattleObjectTypeEnum.OBJ_TYPE_SECTION_POINT;
			_objInstanceId = instanceId;
			_corX = x;
			_corY = y;
		}

		@Override
		public int compareTo(SectionPointData o) {
			if (this._corX > o._corY) {
				return 1;
			} else if (this._corX < o._corY) {
				return -1;
			} else {
				return 0;
			}
		}
	}

}
