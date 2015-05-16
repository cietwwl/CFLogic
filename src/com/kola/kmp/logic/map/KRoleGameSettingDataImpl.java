package com.kola.kmp.logic.map;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.KGameMessage;
import com.koala.game.player.KGamePlayerSession;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.DataStatus;
import com.kola.kmp.logic.gang.war.readme;
import com.kola.kmp.logic.role.IRoleGameSettingData;

public class KRoleGameSettingDataImpl implements IRoleGameSettingData {
	private final static String CLIENT_MEMORY_VOLUME_KEY = "systemMemory";
	private final static String CLIENT_GPU_TYPE_KEY = "graphicsDeviceVendor";
	private final static int CLIENT_MEMORY_LIMIT_VOLUME = 512;

	public final static byte PHONE_TYPE_IOS = 0;
	public final static byte PHONE_TYPE_ANDROID = 1;

	// public final static int SHOW_PLAYER_MIN_SIZE = 5;
	// public final static int SHOW_PLAYER_MAX_SIZE = 50;

	// // 苹果机型默认显示人数的条件列表，Key：内存大小，Value：显示人数
	// public static Map<Integer, Byte> _iosShowPlayerCountConditionMap = new
	// LinkedHashMap<Integer, Byte>();
	// // 安卓机型默认显示人数的条件列表，Key：内存大小，Value：显示人数
	// public static Map<Integer, Byte> _androidShowPlayerCountConditionMap =
	// new LinkedHashMap<Integer, Byte>();

	// 苹果机型默认显示人数的条件列表，Key：内存大小，Value：显示人数
	public static Map<String, List<MobileInfoShowMapConfig>> _iosShowPlayerCountConditionMap1 = new LinkedHashMap<String, List<MobileInfoShowMapConfig>>();
	// 安卓机型默认显示人数的条件列表，Key：内存大小，Value：显示人数
	public static Map<String, List<MobileInfoShowMapConfig>> _androidShowPlayerCountConditionMap1 = new LinkedHashMap<String, List<MobileInfoShowMapConfig>>();

	public static final String defaultKey = "default";

	// 是否屏蔽接受切磋（在线PVP请求）
	private boolean isBlockOnlinePVP = KGameMapManager.isBlockOnlinePVPDefaultValue;
	// 是否屏蔽世界聊天（世界频道）
	private boolean isBlockChat = KGameMapManager.isBlockWorldChatDefaultValue;

	// 角色设置中地图显示人数的等级
	private byte _mapShowPlayerLevel = KGameMapManager.mapShowPlayerLevelDefaultValue;
	// // 角色设置中地图显示人数
	// private int _mapShowPlayerSize = SHOW_PLAYER_MIN_SIZE;

	// 是否免打扰
	private boolean _isNotDisturb;

	private DataStatus _dataStatus;

	static final String JSON_VER = "0";// 版本
	//
	static final String JSON_PVP = "1";// 是否可以接受切磋（在线PVP请求）
	static final String JSON_CHAT = "2";// 是否开放世界聊天（世界频道）

	private static final String JSON_MAP_SHOW_PLAYER_LEVEL = "3";// 角色设置中地图显示人数的等级

	static final String JSON_LV_USE_TIME = "4";// 当前角色等级游戏时间

	static final String JSON_IS_SET_SHOW_LEVEL = "5";// 当前角色等级游戏时间

	private boolean isDebugOpenLevel = false;

	private long lastLoginTimeMillis = System.currentTimeMillis();
	private int nowRoleLvUseTimeSeconds = 0;

	private boolean isAlreadySetShowPlayerLevel = false;

	@Override
	public void decode(String attribute) throws Exception {
		JSONObject json = new JSONObject(attribute);

		if (json == null) {
			return;
		}
		int ver = json.getInt(JSON_VER);// 默认版本
		// CEND 暂时只有版本0
		switch (ver) {
		case 0:
			isBlockOnlinePVP = (json.optByte(JSON_PVP, (byte) (KGameMapManager.isBlockOnlinePVPDefaultValue ? 1 : 0)) == 1);
			isBlockChat = (json.optByte(JSON_CHAT, (byte) (KGameMapManager.isBlockWorldChatDefaultValue ? 1 : 0)) == 1);
			_mapShowPlayerLevel = json.optByte(JSON_MAP_SHOW_PLAYER_LEVEL, (KGameMapManager.mapShowPlayerLevelDefaultValue));
			this.nowRoleLvUseTimeSeconds = json.optInt(JSON_LV_USE_TIME, 0);
			this.isAlreadySetShowPlayerLevel = (json.optInt(JSON_IS_SET_SHOW_LEVEL, 0) == 1);
			break;
		}
	}

	@Override
	public String encode() throws Exception {
		JSONObject json = new JSONObject();
		json.put(JSON_VER, 0);
		json.put(JSON_PVP, isBlockOnlinePVP ? 1 : 0);
		json.put(JSON_CHAT, isBlockChat ? 1 : 0);
		json.put(JSON_MAP_SHOW_PLAYER_LEVEL, _mapShowPlayerLevel);
		json.put(JSON_LV_USE_TIME, nowRoleLvUseTimeSeconds);
		json.put(JSON_IS_SET_SHOW_LEVEL, isAlreadySetShowPlayerLevel ? 1 : 0);
		return json.toString();
	}

	@Override
	public boolean isBlockOnlinePVP() {
		return isBlockOnlinePVP;
	}

	@Override
	public void setBlockOnlinePVP(boolean isBlockOnlinePVP) {
		this.isBlockOnlinePVP = isBlockOnlinePVP;
		_dataStatus.notifyUpdate();
	}

	@Override
	public boolean isBlockChat() {
		return isBlockChat;
	}

	@Override
	public void setBlockChat(boolean isBlockChat) {
		this.isBlockChat = isBlockChat;
		_dataStatus.notifyUpdate();
	}

	@Override
	public byte getMapShowPlayerLevel() {
		return _mapShowPlayerLevel;
	}

	@Override
	public void setMapShowPlayerLevel(byte _mapShowPlayerLevel) {
		this._mapShowPlayerLevel = _mapShowPlayerLevel;
		this.isAlreadySetShowPlayerLevel = true;
		_dataStatus.notifyUpdate();
	}

	@Override
	public boolean isNotDisturb() {
		return _isNotDisturb;
	}

	@Override
	public void setNotDisturb(boolean isNotDisturb) {
		this._isNotDisturb = isNotDisturb;
		_dataStatus.notifyUpdate();
	}

	@Override
	public void setDataStatusInstance(DataStatus entity) {
		_dataStatus = entity;
	}

	@Override
	public boolean isDebugOpenLevel() {
		return isDebugOpenLevel;
	}

	@Override
	public void setDebugOpenLevel(boolean isOpen) {
		this.isDebugOpenLevel = isOpen;
	}

	public int getNowRoleLvUseTimeSeconds() {
		return nowRoleLvUseTimeSeconds;
	}

	public void notifyLogin() {
		this.lastLoginTimeMillis = System.currentTimeMillis();
		_dataStatus.notifyUpdate();
	}

	public void notifyLogout() {
		int timeSecond = (int) ((System.currentTimeMillis() - this.lastLoginTimeMillis) / 1000);
		this.nowRoleLvUseTimeSeconds += timeSecond;
		_dataStatus.notifyUpdate();
	}

	public int notifyUpgradeLv() {
		int timeSecond = (int) ((System.currentTimeMillis() - this.lastLoginTimeMillis) / 1000) + this.nowRoleLvUseTimeSeconds;
		this.nowRoleLvUseTimeSeconds = 0;
		this.lastLoginTimeMillis = System.currentTimeMillis();
		_dataStatus.notifyUpdate();
		return timeSecond;
	}

	// public void checkAndSetMapShowPlayerLevel1(KGamePlayerSession session) {
	// if (session != null && session.getBoundPlayer() != null) {
	// String memSize =
	// session.getBoundPlayer().getAnalysisInfo(CLIENT_MEMORY_VOLUME_KEY);
	// if (memSize != null) {
	// try {
	// int size = Integer.parseInt(memSize);
	// if (size > CLIENT_MEMORY_LIMIT_VOLUME) {
	// return;
	// }
	// } catch (NumberFormatException e) {
	// }
	// }
	// }
	// this._mapShowPlayerLevel = KGameMapManager.mapShowPlayerLevelMinValue;
	// _dataStatus.notifyUpdate();
	// }

	// public void checkAndSetMapShowPlayerLevel(KGamePlayerSession session) {
	// if (session != null && session.getBoundPlayer() != null) {
	// int memSize = 0;
	// byte showLevel = KGameMapManager.mapShowPlayerLevelMinValue;
	// String memSizeInfo =
	// session.getBoundPlayer().getAnalysisInfo(CLIENT_MEMORY_VOLUME_KEY);
	// if (memSizeInfo != null) {
	// try {
	// memSize = Integer.parseInt(memSizeInfo);
	// } catch (NumberFormatException e) {
	// }
	// }
	//
	// if (memSize >= 0) {
	// if (session.getClientType() == KGameMessage.CTYPE_ANDROID) {
	// for (Integer mSize : _androidShowPlayerCountConditionMap.keySet()) {
	// if (memSize >= mSize) {
	// showLevel = _androidShowPlayerCountConditionMap.get(mSize);
	// }
	// }
	// } else {
	// for (Integer mSize : _iosShowPlayerCountConditionMap.keySet()) {
	// if (memSize >= mSize) {
	// showLevel = _iosShowPlayerCountConditionMap.get(mSize);
	// }
	// }
	// }
	// }
	// this._mapShowPlayerLevel = showLevel;
	//
	// _dataStatus.notifyUpdate();
	// }
	//
	// }

	public void checkAndSetMapShowPlayerLevel(KGamePlayerSession session) {
		if (!isAlreadySetShowPlayerLevel && session != null && session.getBoundPlayer() != null) {
			int memSize = 0;
			byte showLevel = KGameMapManager.mapShowPlayerLevelMinValue;
			String memSizeInfo = session.getBoundPlayer().getAnalysisInfo(CLIENT_MEMORY_VOLUME_KEY);
			String gpuInfo = session.getBoundPlayer().getAnalysisInfo(CLIENT_GPU_TYPE_KEY);
			if (memSizeInfo != null) {
				try {
					memSize = Integer.parseInt(memSizeInfo);
				} catch (NumberFormatException e) {
				}
			}

			if (gpuInfo != null) {
				gpuInfo = gpuInfo.toLowerCase();
				if (session.getClientType() == KGameMessage.CTYPE_ANDROID) {
					checkAndSetMapShowPlayerLevelNative(_androidShowPlayerCountConditionMap1, gpuInfo, memSize);
				} else {
					checkAndSetMapShowPlayerLevelNative(_iosShowPlayerCountConditionMap1, gpuInfo, memSize);
				}
			} else {
				if (session.getClientType() == KGameMessage.CTYPE_ANDROID) {
					checkAndSetMapShowPlayerLevelNative(_androidShowPlayerCountConditionMap1, defaultKey, memSize);
				} else {
					checkAndSetMapShowPlayerLevelNative(_iosShowPlayerCountConditionMap1, defaultKey, memSize);
				}
			}			
		}
	}

	public void checkAndSetMapShowPlayerLevelNative(Map<String, List<MobileInfoShowMapConfig>> checkMap, String gpuInfo, int memSize) {

		byte showLevel = KGameMapManager.mapShowPlayerLevelMinValue;

		if (gpuInfo.equals(defaultKey)) {
			for (MobileInfoShowMapConfig config : checkMap.get(defaultKey)) {
				if (memSize >= config.memSize) {
					showLevel = config.showPlayerLevel;
				}
			}
		} else {
			boolean isMatch = false;
			for (String gpuKey : checkMap.keySet()) {
				if (!gpuKey.equals(defaultKey) && gpuInfo.indexOf(gpuKey.toLowerCase()) != -1) {
					for (MobileInfoShowMapConfig config : checkMap.get(gpuKey)) {
						if (memSize >= config.memSize) {
							showLevel = config.showPlayerLevel;
							isMatch = true;
						}
					}
				}
			}
			if (!isMatch) {
				for (MobileInfoShowMapConfig config : checkMap.get(defaultKey)) {
					if (memSize >= config.memSize) {
						showLevel = config.showPlayerLevel;
					}
				}
			}
		}
		this._mapShowPlayerLevel = showLevel;

		this.isAlreadySetShowPlayerLevel = true;
		_dataStatus.notifyUpdate();
	}

	public static class MobileInfoShowMapConfig {
		public byte mType;
		public String gpuInfo;
		public int memSize;
		public byte showPlayerLevel;

		public MobileInfoShowMapConfig(byte mType, String gpuInfo, int memSize, byte showPlayerLevel) {
			super();
			this.mType = mType;
			this.gpuInfo = gpuInfo;
			this.memSize = memSize;
			this.showPlayerLevel = showPlayerLevel;
		}

	}
}
