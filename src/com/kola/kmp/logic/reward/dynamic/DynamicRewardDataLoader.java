package com.kola.kmp.logic.reward.dynamic;

import org.slf4j.Logger;

import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.dynamic.DynamicRewardDataStruct.GangRewardData;
import com.kola.kmp.logic.reward.dynamic.DynamicRewardDataStruct.RewardElement;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 本类负责加载数据
 * 
 * @author camus
 * @creation 2012-12-30 下午2:52:15
 * </pre>
 */
public class DynamicRewardDataLoader {

	private static Logger _LOGGER = KGameLogger.getLogger(DynamicRewardDataLoader.class);

	// 奖励数据参数
	private static String RoleRewardDataPath="./res/gamedata/rewardModule/dynamicReward/roleRewardData.xls";
	private static String GangRewardDataPath="./res/gamedata/rewardModule/dynamicReward/gangRewardData.xls";
	private static int HeaderIndex = 5;

	private final static String 奖励名单 = "奖励名单";
	private final static String 动态奖励规则表 = "动态奖励规则表";

	private DynamicRewardDataLoader() {
	}

	/**
	 * <pre>
	 * 开始加载文件
	 * 
	 * @deprecated 模块内调用
	 * @param configPath
	 * @author CamusHuang
	 * @creation 2012-11-9 下午7:38:27
	 * </pre>
	 */
	static DynamicRewardDataManager goToLoadData(boolean isRole) throws Exception {
		DynamicRewardDataManager dataManager = new DynamicRewardDataManager();
		// 加载数据
		KGameExcelFile file = new KGameExcelFile(isRole ? RoleRewardDataPath : GangRewardDataPath);
		{// 加载奖励项
			KGameExcelTable table = file.getTable(动态奖励规则表, HeaderIndex);
			KGameExcelRow[] rows = table.getAllDataRows();
			for (KGameExcelRow row : rows) {
				BaseMailRewardData mail = BaseMailRewardData.loadData(row, true);
				int gangResource = isRole?0:row.getInt("gangResource");
				GangRewardData data = new GangRewardData(mail, gangResource);
				dataManager.mRewardDataManager.addData(data);
			}
		}

		{// 加载奖励名单
			KGameExcelTable table = file.getTable(奖励名单, HeaderIndex);
			KGameExcelRow[] rows = table.getAllDataRows();
			if (isRole) {
				loadRewardRoleList(dataManager, HeaderIndex, rows, _LOGGER);
			} else {
				loadRewardGangList(dataManager, HeaderIndex, rows, _LOGGER);
			}
		}
		return dataManager;
	}

	/**
	 * <pre>
	 * 加载角色奖励名单
	 * 
	 * @param headerIndex
	 * @param rows
	 * @throws KGameServerException
	 * @throws Exception
	 * @author camus
	 * @creation 2012-12-30 下午11:46:22
	 * </pre>
	 */
	private static void loadRewardRoleList(DynamicRewardDataManager dataManager, int headerIndex, KGameExcelRow[] rows, Logger _LOGGER) throws KGameServerException, Exception {

		if (rows.length < 1) {
			throw new KGameServerException("加载奖励名单错误：有效行数为0！");
		}

		for (KGameExcelRow row : rows) {
			try {
				KRole role = null;
				long roleId = Long.parseLong(row.getData("id"));
				String roleName = null;
				if (roleId >= 0) {
					role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
				} else {
					roleName = row.getData("roleName");
					if (roleName == null || roleName.length() < 1) {
						throw new KGameServerException("加载奖励名单错误：roleId 且 roleName 无效 ,Row=" + row.getIndexInFile());
					}
					role = KSupportFactory.getRoleModuleSupport().getRole(roleName);
					roleId = role.getId();
				}

				if (role == null) {
					throw new KGameServerException("加载奖励名单错误：不存在的角色ID=" + roleId + " NAME=" + roleName + ",Row=" + row.getIndexInFile());
				}

				int rewardId = row.getInt("rewardId");
				if (dataManager.mRewardDataManager.getData(rewardId) == null) {
					throw new KGameServerException("加载奖励名单错误：未经定义的奖励规则=" + rewardId + ",Row=" + row.getIndexInFile());
				}

				String error = dataManager.mRewardRoleDataManager.addData(new RewardElement(roleId, role.getName(), rewardId));
				if (error != null) {
					throw new KGameServerException("加载奖励名单错误：" + error + ",Row=" + row.getIndexInFile());
				}
				_LOGGER.warn("动态奖励名单:ROLE{ID:{} NAME:{}} 奖励ID:{}", roleId, role.getName(), rewardId);
			} catch (KGameServerException e) {
				throw e;
			} catch (Exception e) {
				throw new KGameServerException("加载奖励名单错误：Row=" + row.getIndexInFile(), e);
			}
		}
		_LOGGER.warn("动态奖励名单加载完毕，共加载{}个角色", dataManager.mRewardRoleDataManager.getAllDatas().size());
	}

	/**
	 * <pre>
	 * 加载军团奖励名单
	 * 
	 * @param headerIndex
	 * @param rows
	 * @throws KGameServerException
	 * @throws Exception
	 * @author camus
	 * @creation 2012-12-30 下午11:46:22
	 * </pre>
	 */
	private static void loadRewardGangList(DynamicRewardDataManager dataManager, int headerIndex, KGameExcelRow[] rows, Logger _LOGGER) throws KGameServerException, Exception {

		if (rows.length < 1) {
			throw new KGameServerException("加载奖励名单错误：有效行数为0！");
		}

		for (KGameExcelRow row : rows) {
			try {
				KGang gang = null;
				long gangId = Long.parseLong(row.getData("id"));
				gang = KSupportFactory.getGangSupport().getGang(gangId);

				if (gang == null) {
					throw new KGameServerException("加载奖励名单错误：不存在的军团ID=" + gangId + ",Row=" + row.getIndexInFile());
				}

				int rewardId = row.getInt("rewardId");
				if (dataManager.mRewardDataManager.getData(rewardId) == null) {
					throw new KGameServerException("加载奖励名单错误：未经定义的奖励规则=" + rewardId + ",Row=" + row.getIndexInFile());
				}

				String error = dataManager.mRewardRoleDataManager.addData(new RewardElement(gangId, null, rewardId));
				if (error != null) {
					throw new KGameServerException("加载奖励名单错误：" + error + ",Row=" + row.getIndexInFile());
				}
				_LOGGER.warn("动态奖励名单:GangID:{} 奖励ID:{}", gangId, rewardId);
			} catch (KGameServerException e) {
				throw e;
			} catch (Exception e) {
				throw new KGameServerException("加载奖励名单错误：Row=" + row.getIndexInFile(), e);
			}
		}
		_LOGGER.warn("动态奖励名单加载完毕，共加载{}个军团", dataManager.mRewardRoleDataManager.getAllDatas().size());
	}

}
