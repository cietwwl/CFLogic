package com.kola.kmp.logic.mission;

import java.util.ArrayList;
import java.util.List;

import com.kola.kmp.logic.npc.menu.IMissionMenu;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;


public class IMissionMenuImpl implements IMissionMenu{
	
	private String missionName;
	private int missionTemplateId;
	private byte missionStatus;
	private List<IMissionConversation> missionConversationList = new ArrayList<IMissionMenu.IMissionConversation>();
	boolean isMainMission;
	private byte clickEvent;
	private boolean isShowMissionReward;
	private BaseRewardData reward;
	

	public IMissionMenuImpl() {
	}

	@Override
	public String getMissionName() {
		return missionName;
	}

	@Override
	public int getMissionTemplateId() {
		return missionTemplateId;
	}

	@Override
	public byte getMissionStatus() {
		return missionStatus;
	}

	@Override
	public List<IMissionConversation> getConversations() {
		
		return missionConversationList;
	}
	
	

	public void setMissionName(String missionName) {
		this.missionName = missionName;
	}

	public void setMissionTemplateId(int missionTemplateId) {
		this.missionTemplateId = missionTemplateId;
	}

	public void setMissionStatus(byte missionStatus) {
		this.missionStatus = missionStatus;
	}

	public void setMainMission(boolean isMainMission) {
		this.isMainMission = isMainMission;
	}
	
	public void addMissionConversation(IMissionConversation conversation){
		this.missionConversationList.add(conversation);
	}

	@Override
	public boolean isMainMission() {
		return isMainMission;
	}
	
	@Override
	public byte getActionAfterTalk() {
		return clickEvent;
	}
	
	public void setActionAfterTalk(byte clickEvent) {
		this.clickEvent = clickEvent;
	}
	
	@Override
	public boolean isShowMissionReward() {
		return isShowMissionReward;
	}
	

	public void setShowMissionReward(boolean isShowMissionReward) {
		this.isShowMissionReward = isShowMissionReward;
	}

	@Override
	public BaseRewardData missionReward() {
		return reward;
	}
	
	
	public void setMissionReward(BaseRewardData reward) {
		this.reward = reward;
	}



	public static class IMissionConversationImpl implements IMissionConversation{
		
		private String  conversationConten;
		private String menuText;

		public IMissionConversationImpl(String conversationConten,
				String menuText) {
			this.conversationConten = conversationConten;
			this.menuText = menuText;
		}

		@Override
		public String getConversationConten() {
			return conversationConten;
		}

		@Override
		public String getMenuText() {
			return menuText;
		}		
	}

	
}
