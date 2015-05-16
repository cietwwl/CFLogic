package com.kola.kmp.logic.role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.EvictionListener;
import com.googlecode.concurrentlinkedhashmap.Weighers;
import com.koala.game.logging.KGameLogger;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.role.RoleBaseInfo;
import com.kola.kgame.cache.role.RoleModuleFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KRoleBaseInfoCacheManager implements IRoleEventListener {
	
	private static final Logger _LOGGER = KGameLogger.getLogger(KRoleBaseInfoCacheManager.class);
	
//	private static Map<String, IRoleBaseInfo> _roleNameMapping = new ConcurrentHashMap<String, IRoleBaseInfo>();
	private static Map<Long, Long> _roleIdToPlayerId = new ConcurrentHashMap<Long, Long>(2000, 0.75f);
	private static ConcurrentLinkedHashMap<Long, List<IRoleBaseInfo>> _roleListOfPlayers;
	
	static void init() {
		ConcurrentLinkedHashMap.Builder<Long, List<IRoleBaseInfo>> builder = new ConcurrentLinkedHashMap.Builder<Long, List<IRoleBaseInfo>>();
		builder.initialCapacity(1000);
		builder.maximumWeightedCapacity(2000);
		builder.concurrencyLevel(32);
		builder.weigher(Weighers.singleton());
		builder.listener(new EvictionListener<Long, List<IRoleBaseInfo>>() {

			@Override
			public void onEviction(Long playerId, List<IRoleBaseInfo> list) {
				IRoleBaseInfo baseInfo;
				for (int i = 0; i < list.size(); i++) {
					baseInfo = list.get(i);
					_roleIdToPlayerId.remove(baseInfo.getId());
//					_roleNameMapping.remove(baseInfo.getName());
				}
			}

		});
		_roleListOfPlayers = builder.build();
	}
	
	static RoleBaseInfo getRoleBaseInfo(long roleId) {
		Long playerId = _roleIdToPlayerId.get(roleId);
		if (playerId == null) {
			try {
				RoleBaseInfo baseInfo = RoleModuleFactory.getRoleModule().getRoleBaseInfo(roleId);
				return baseInfo;
			} catch (Exception e) {
				_LOGGER.error("获取单个角色基础数据出现异常，roleId：{}", roleId, e);
			}
		} else {
			List<IRoleBaseInfo> list = _roleListOfPlayers.get(playerId);
			RoleBaseInfo temp;
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					temp = list.get(i);
					if (temp.getId() == roleId) {
						return temp;
					}
				}
			}
		}
		return null;
	}

	static List<IRoleBaseInfo> getRoleListOfPlayer(long playerId, boolean loadIfAbsent) {
		List<IRoleBaseInfo> list = _roleListOfPlayers.get(playerId);
		if (list == null && loadIfAbsent) {
			synchronized ((Long) playerId) {
				// lock and double check
				list = _roleListOfPlayers.get(playerId);
				if(list != null) {
					return list;
				}
				// end
				try {
					list = new ArrayList<IRoleBaseInfo>();
					List<RoleBaseInfo> tempList = RoleModuleFactory.getRoleModule().getRoleList(playerId);

					_roleListOfPlayers.put(playerId, list);
					RoleBaseInfo baseInfo;
					for (int i = 0; i < tempList.size(); i++) {
						baseInfo = tempList.get(i);
						_roleIdToPlayerId.put(baseInfo.getId(), playerId);
						if (!(baseInfo instanceof KRoleBaseInfo) && (baseInfo instanceof KRole)) {
							// 因为逻辑要做缓存，所以把非KRoleBaseInfo对象的转为KRoleBaseInfo对象，以防缓存了引擎对象
							KRoleBaseInfo nBaseInfo = new KRoleBaseInfo();
							nBaseInfo.copyFrom((KRole) baseInfo);
							list.add(nBaseInfo);
						} else {
							list.add((KRoleBaseInfo) baseInfo);
						}
					}
				} catch (Exception e) {
					_LOGGER.error("获取角色列表时出现异常，账号id是：{}", playerId, e);
					list = new ArrayList<IRoleBaseInfo>();
				}
				Collections.sort(list, KRoleBaseInfo.CMP);
			}
		}
		return list;
	}
	
	static void updateEquipmentResMap(long roleId) {
		RoleBaseInfo baseInfo = getRoleBaseInfo(roleId);
		if(baseInfo != null) {
			((KRoleBaseInfo)baseInfo).updateEquipmentRes();
		}
	}
	
	static void updateFashionRes(long roleId) {
		RoleBaseInfo baseInfo = getRoleBaseInfo(roleId);
		if (baseInfo != null) {
			((KRoleBaseInfo) baseInfo).updateFashionResId();
		}
	}
	
	static void updateEquipSetRes(long roleId) {
		RoleBaseInfo baseInfo = getRoleBaseInfo(roleId);
		if (baseInfo != null) {
			((KRoleBaseInfo) baseInfo).updateEquipSetRes();
		}
	}

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
//		List<IRoleBaseInfo> list = _roleListOfPlayers.get(role.getPlayerId());
//		if(list != null) {
//			
//		}
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {
		
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		List<IRoleBaseInfo> list = _roleListOfPlayers.get(role.getPlayerId());
		if (list != null) {
			KRoleBaseInfo info = new KRoleBaseInfo();
			info.copyFrom(role);
			list.add(info);
			_roleIdToPlayerId.put(role.getId(), role.getPlayerId());
		}
	}

	@Override
	public void notifyRoleDeleted(long roleId) {
		Long playerId = _roleIdToPlayerId.get(roleId);
		if (playerId != null) {
			List<IRoleBaseInfo> list = _roleListOfPlayers.get(playerId);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getId() == roleId) {
					list.remove(i);
					_roleIdToPlayerId.remove(roleId);
					break;
				}
			}
		}
	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		List<IRoleBaseInfo> list = _roleListOfPlayers.get(role.getPlayerId());
		if (list != null) {
			RoleBaseInfo baseInfo;
			for (int i = 0; i < list.size(); i++) {
				baseInfo = list.get(i);
				if (baseInfo.getId() == role.getId()) {
					((KRoleBaseInfo) baseInfo).update(role);
				}
			}
		}
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		
	}
	
	public static void main(String[] args) {
		final long id = 1;
		final long temp = 0;
		System.out.println(((Long)id).hashCode());
		System.out.println(((Long)temp).hashCode());
		Thread t1 = new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized ((Long) id) {
					System.out.println("thread=" + Thread.currentThread().getName() + " get lock");
					try {
						TimeUnit.SECONDS.sleep(5);
					} catch (Exception e) {
						e.printStackTrace();
					}
					System.out.println("thread=" + Thread.currentThread().getName());
				}
			}
		});
		
		Thread t2 = new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized ((Long) temp) {
					System.out.println("thread=" + Thread.currentThread().getName() + " get lock");
					try {
						TimeUnit.SECONDS.sleep(5);
					} catch (Exception e) {
						e.printStackTrace();
					}
					System.out.println("thread=" + Thread.currentThread().getName());
				}
			}
		});
		t1.start();
		t2.start();
	}

}
