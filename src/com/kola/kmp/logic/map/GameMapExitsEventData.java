package com.kola.kmp.logic.map;



public class GameMapExitsEventData {
	// 事件触碰范围长度
	private final static float EVENT_RANGE_W = 50;
	// 事件触碰范围高度
	private final static float EVENT_RANGE_H = 50;

	/**
	 * 表示出口跳转类型为跳转到普通地图
	 */
	public final static int EXIT_EVENT_TYPE_JUMP_MAP = 1;

	/**
	 * 表示出口跳转类型为跳转到关卡选择界面
	 */
	public final static int EXIT_EVENT_TYPE_GAMELEVELS = 2;

	// 事件触发范围的x、y坐标
	private float _x, _y;

	private int _exitId;

//	// 出口跳转类型，参考EXIT_EVENT_TYPE_JUMP_MAP 或 EXIT_EVENT_TYPE_GAMELEVELS
	private int _exitType;

	/**
	 * 出口所属地图ID
	 */
	public int srcMapId;
	

	/**
	 * <pre>
	 * 出口对应目标ID,该ID可能为地图ID或者关卡副本ID。
	 * 根据表示出口事件跳转类型getExitEventType()判断
	 * </pre>
	 */
	public int targetId;
	
	private int npcOrExitInstanceId;

//	/**
//	 * 地图出口名称
//	 */
//	public String targetName;

//	/**
//	 * <pre>
//	 * 本地图出口的方向，该值参考{@ KGameMapData#DIR_UP}|| {@ KGameMapData#DIR_DOWN}。
//	 * </pre>
//	 */
//	public int srcMapExitDirection;

//	// 客户端对应的资源id
//	private int res_id;

	public GameMapExitsEventData(int exitId, int exitType, float x, float y) {
		this._exitId = exitId;
		this._x = x;
		this._y = y;
		this._exitType = exitType;
	}	

	public float getX() {
		return _x;
	}

	public float getY() {
		return _y;
	}

	/**
	 * 事件在地图触发起始X坐标的长度
	 * 
	 * @return
	 */
	public float getW() {
		return EVENT_RANGE_W;
	}

	/**
	 * 事件在地图触发起始X坐标的高度
	 * 
	 * @return
	 */
	public float getH() {
		return EVENT_RANGE_H;
	}

	public int getExitId() {
		return _exitId;
	}
	
	public int getExitType() {
		return _exitType;
	}
	
	public int getNpcOrExitInstanceId() {
		return npcOrExitInstanceId;
	}

	public void setNpcOrExitInstanceId(int npcOrExitInstanceId) {
		this.npcOrExitInstanceId = npcOrExitInstanceId;
	}

//	/**
//	 * 客户端对应的资源id
//	 * 
//	 * @return
//	 */
//	public int getRes_id() {
//		return res_id;
//	}
//
//	public void setRes_id(int res_id) {
//		this.res_id = res_id;
//	}

}
