package com.kola.kmp.logic.reward.redress;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kmp.logic.RunTimeTask;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KGameLevelRecord;
import com.kola.kmp.logic.level.KGameLevelSet;
import com.kola.kmp.logic.mail.KMail;
import com.kola.kmp.logic.mail.KMailModuleExtension;
import com.kola.kmp.logic.mail.KMailSet;
import com.kola.kmp.logic.mail.attachment.MailAttachmentAbs;
import com.kola.kmp.logic.mail.attachment.MailAttachmentItemCode;
import com.kola.kmp.logic.mail.attachment.MailAttachmentTypeEnum;
import com.kola.kmp.logic.mount.KMount;
import com.kola.kmp.logic.mount.KMountDataManager;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountTemplate;
import com.kola.kmp.logic.mount.KMountModuleExtension;
import com.kola.kmp.logic.mount.KMountSet;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 机甲补丁
 * 原由：改版补偿，异形要塞补发奖励有误，错发左上一级补偿
 * 扫描在线人员，已经补偿过，未有指定机甲，背包不含指定物品，邮件不含指定附件
 * 
 * @author CamusHuang
 * @creation 2015-2-4 下午4:29:07
 * </pre>
 */
public class TWLevelMountPatch implements RunTimeTask {
	private static Logger _LOGGER = KGameLogger.getLogger(TWLevelMountPatch.class);

	public static TWLevelMountTask instance = new TWLevelMountTask();
	public static int MinLevelLv = 70030;
	public static int MountModelId = 100103;
	public static String ItemCode = "37801001";
	public static KMountTemplate mountTemp;

	static long TaskPeriod = 10;// 秒

	public String run(String param) {
		mountTemp = KMountDataManager.mMountTemplateManager.getTemplateByModel(MountModelId).get(1);
		if (mountTemp == null) {
			return ("异形要塞机甲补丁：机甲模板不存在");
		}

		if (param.equals("start")) {
			if (instance != null) {
				instance.isCancel = true;
			}
			instance = new TWLevelMountTask();
			KGame.newTimeSignal(instance, TaskPeriod, TimeUnit.SECONDS);
		} else {// stop
			if (instance != null) {
				instance.isCancel = true;
			}
		}
		return "执行完毕";
	}

	public static class TWLevelMountTask implements KGameTimerTask {

		boolean isCancel = false;

		private TWLevelMountTask() {
		}

		@Override
		public void done(KGameTimeSignal arg0) {
		}

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {
			if (isCancel) {
				return null;
			}
			try {
				List<Long> onLineRoleIds = KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds();
				for (long roleId : onLineRoleIds) {
					try {
						KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
						doWork(role);
					} catch (Exception ex) {
						_LOGGER.error(ex.getMessage(), ex);
					}
				}
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			} finally {
				arg0.getTimer().newTimeSignal(this, TaskPeriod, TimeUnit.SECONDS);
			}
			return null;
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}

		public void doWork(KRole role) {
			if (isCancel) {
				return;
			}

			if (role == null) {
				return;
			}

			long roleId = role.getId();
			//
			// 已经补偿过\关卡过30\机甲栏、背包、邮件未有机甲\
			KRoleRedress roleData = KRedressSonModule.instance.getRewardSon(role.getId());
			// 是否已经补偿过
			if (!roleData.isRunVer2Redress()) {
				return;
			}

			KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
			KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.爬塔副本关卡);
			if (record == null || record.towerCopyData == null) {
				return;
			}

			if (record.towerCopyData.nowLevelId < MinLevelLv) {
				return;
			}

			KMountSet set = KMountModuleExtension.getMountSet(role.getId());
			KMount mount = set.getMountByModel(MountModelId);
			if (mount != null) {
				return;
			}

			if (KItemLogic.searchItemFromBag(roleId, ItemCode) != null) {
				return;
			}

			KMailSet mailSet = KMailModuleExtension.getMailSet(role.getId());
			List<KMail> mailList = mailSet.getAllMailsCopy();
			for (KMail mail : mailList) {
				for (Entry<MailAttachmentTypeEnum, MailAttachmentAbs> eee : mail.getAllAttachmentsCache().entrySet()) {
					if (eee.getKey() == MailAttachmentTypeEnum.ITEMCODE) {
						MailAttachmentItemCode att = (MailAttachmentItemCode) eee.getValue();
						for (ItemCountStruct s : att.getDataStructCache()) {
							if (s.itemCode.equals(ItemCode)) {
								return;
							}
						}
					}
				}
			}

			KActionResult<KMount> result = KSupportFactory.getMountModuleSupport().presentMount(role, mountTemp, "异形要塞机甲补发");
			KRedressCenter.REDRESS_LOGGER.warn("{},{},{},成功,{},tips,{}", role.getId(), role.getName(), "异形要塞机甲补发", result.success, result.tips);
		}
	}
}
