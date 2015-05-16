package com.kola.kmp.logic.map;


import java.util.Stack;

/**
 * 表示地图自动寻路路径数据
 * @author zhaizl
 *
 */
public class AutoSearchRoadTrack {
	
	private Stack<RoadPath> roadPathStack = new Stack<RoadPath>();
    /**
     * 添加一个路径
     * @param path
     */
    public void addRoadPath(RoadPath path){
    	this.roadPathStack.push(path);
    }
	/**
	 * 获取路径集合
	 * @return
	 */
	public Stack<RoadPath> getRoadPathStack() {
		return roadPathStack;
	}
    
	/**
	 * 单个路径的数据
	 * @author zhaizl
	 *
	 */
	public static class RoadPath{
		/**
		 * 表示该段寻路路径类型为跳转区域
		 */
		public static final byte PATH_TYPE_JUMP_AREA = 1;
		/**
		 * 表示该段寻路路径类型为行走到本地图的出入口
		 */
		public static final byte PATH_TYPE_WALK_TO_EXITS = 2;
		/**
		 * 表示该段寻路路径类型为行走到本地图的NPC
		 */
		public static final byte PATH_TYPE_WALK_TO_NPC = 3;
		
		/**
		 * <pre>
		 * 表示该段寻路路径类型，该值参考{@link RoadPath#PATH_TYPE_JUMP_AREA}||
		 * {@link RoadPath#PATH_TYPE_WALK_TO_EXITS}||{@link RoadPath#PATH_TYPE_WALK_TO_NPC}
		 * </pre>
		 */
		public byte pathType;
		
		/**
		 * <pre>
		 * 表示该段寻路路径的目标ID。
		 * 当pathType=={@link RoadPath#PATH_TYPE_JUMP_AREA}时，目标ID为跳转区域的ID；
		 * 当pathType=={@link RoadPath#PATH_TYPE_WALK_TO_EXITS}时，目标ID为本地图的出入口的ID；
		 * 当pathType=={@link RoadPath#PATH_TYPE_WALK_TO_NPC}时，目标ID为本地图的NPC的ID；
		 * </pre>
		 */
		public int targetId;

		public RoadPath(byte pathType, int targetId) {
			super();
			this.pathType = pathType;
			this.targetId = targetId;
		}
		
		
		
	}

}
