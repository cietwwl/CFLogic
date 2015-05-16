package com.kola.kmp.logic.npc.menu;

import java.util.List;

import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;

/**
 *
 * @author PERRY CHAN
 */
public interface IMissionMenu {

	/**
	 * 点击事件：继续文本
	 */
	public final byte ACTION_AFTER_TALK_CONTINUE = 0;
	/**
	 * 点击事件：直接关闭对话框
	 */
	public final byte ACTION_AFTER_TALK_CLOSE = 1;
	/**
	 * 点击事件：提交服务器
	 */
	public final byte ACTION_AFTER_TALK_SUBMIT = 2;

	/**
	 * 
	 * 获取任务的模板id
	 * 
	 * @return
	 */
	int getMissionTemplateId();

	/**
	 * 
	 * 获取任务的名字
	 * 
	 * @return
	 */
	String getMissionName();
	
	/**
	 * 
	 * 获取任务的状态
	 * 
	 * @return
	 */
	byte getMissionStatus();
	
	/**
	 * 
	 * 获取任务的对话内容
	 * 
	 * @return
	 */
	List<IMissionConversation> getConversations();
	
	/**
	 * 
	 * 获取点击菜单后的事件
	 * 0=继续对话内容,1=对话结束关闭对话框,2=请求服务器
	 * 
	 * @return
	 */
	byte getActionAfterTalk();

	/**
	 * 
	 * 是不是主线任务
	 * 
	 * @return
	 */
	boolean isMainMission();
	
	/**
	 * 是否在任务对话结束时显示任务奖励
	 * @return
	 */
	boolean isShowMissionReward();
	
	/**
	 * 任务对话结束时显示的任务奖励数据
	 * @return
	 */
	BaseRewardData missionReward();
	
	
	public static interface IMissionConversation{
		

		
		/**
		 * 
		 * 获取对话内容显示在NPC头像旁边的那段话
		 * 
		 * @return
		 */
		String getConversationConten();
		
		/**
		 * 
		 * 获取菜单的文本
		 * 
		 * @return
		 */
		String getMenuText();
	}
}
