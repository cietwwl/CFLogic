package com.kola.kmp.logic.mail.attachment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.koala.game.util.StringUtil;
import com.koala.thirdpart.json.JSONArray;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kmp.logic.KGameGlobalConfig;
import com.kola.kmp.logic.fashion.KFashionDataStructs.KFashionTemplate;
import com.kola.kmp.logic.flow.KPetFlowType;
import com.kola.kmp.logic.flow.KRoleAttrModifyType;
import com.kola.kmp.logic.mail.KMail;
import com.kola.kmp.logic.other.KColorFunEnum;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.pet.KPetTemplate;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;
import com.kola.kmp.logic.util.ResultStructs.MailResult_Ext;
import com.kola.kmp.logic.util.ResultStructs.MailResult_TakeAtt;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.ShopTips;

/**
 * <pre>
 * 邮件附件工厂
 * 
 * @author CamusHuang
 * @creation 2014-2-23 上午10:38:56
 * </pre>
 */
public final class MailAttachmentFactory {

	/**
	 * <pre>
	 * 本方法仅用于从DB加载邮件时间创建邮件附件
	 * 运行中新建邮件时请使用各附件类构造方法直接创建邮件附件
	 * 
	 * @param attachmentType
	 * @param attJsonCA
	 * @return
	 * @throws Exception
	 * @author CamusHuang
	 * @creation 2014-4-1 下午2:51:31
	 * </pre>
	 */
	public static MailAttachmentAbs createAndInitAttachmentForDB(MailAttachmentTypeEnum attachmentType, Object attJsonCA) throws Exception {
		switch (attachmentType) {
		case ITEMCODE: {
			MailAttachmentItemCode temp = new MailAttachmentItemCode();
			temp.decodeCA(attJsonCA);
			return temp;
		}
		case MONEY: {
			MailAttachmentMoney temp = new MailAttachmentMoney();
			temp.decodeCA(attJsonCA);
			return temp;
		}
		case ROLEATT: {
			MailAttachmentRoleAtt temp = new MailAttachmentRoleAtt();
			temp.decodeCA(attJsonCA);
			return temp;
		}
		case FASHION: {
			MailAttachmentFashion temp = new MailAttachmentFashion();
			temp.decodeCA(attJsonCA);
			return temp;
		}
		case PET: {
			MailAttachmentPet temp = new MailAttachmentPet();
			temp.decodeCA(attJsonCA);
			return temp;
		}
		}
		return null;
	}

	/**
	 * <pre>
	 * 循环提取此邮件的所有附件，直到失败为止
	 * 
	 * @param role
	 * @param mail
	 * @param result
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-1 下午4:53:05
	 * </pre>
	 */
	public static MailResult_TakeAtt takeAttachment(KRole role, KMail mail) {
		MailResult_TakeAtt result = new MailResult_TakeAtt();
		mail.rwLock.lock();
		try {

			for (Iterator<MailAttachmentAbs> it = mail.getAllAttachmentsCache().values().iterator();it.hasNext();) {
				MailAttachmentAbs mailAtt = it.next();
				MailResult_Ext tempResult = null;
				switch (mailAtt.type) {
				case ITEMCODE:
					tempResult = takeAttachment_itemCode(role, (MailAttachmentItemCode) mailAtt);
					break;
				case MONEY:
					tempResult = takeAttachment_money(role, (MailAttachmentMoney) mailAtt);
					break;
				case ROLEATT:
					tempResult = takeAttachment_roleAtt(role, mail, (MailAttachmentRoleAtt) mailAtt);
					break;
				case PET:
					tempResult = takeAttachment_pet(role, mail, (MailAttachmentPet) mailAtt);
					break;
				case FASHION:
					tempResult = takeAttachment_fashion(role, (MailAttachmentFashion) mailAtt);
					break;
				}
				//
				result.addUprisingTips(tempResult.getUprisingTips());
				result.addDataUprisingTips(tempResult.getDataUprisingTips());
				//
				if (tempResult.isSucess) {
					// 成功
					result.isTakePartAtts = true;
					it.remove();
				} else {
					// 失败
					result.isSucess = tempResult.isSucess;
					result.tips = tempResult.tips;
					return result;
				}
			}

			// 全部成功
			result.isSucess = true;
			result.isTakePartAtts = false;
			return result;
		} finally {
			mail.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 提取道具附件
	 * 
	 * @param role
	 * @param mailAtt
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-1 下午4:21:55
	 * </pre>
	 */
	private static MailResult_Ext takeAttachment_itemCode(KRole role, MailAttachmentItemCode mailAtt) {
		MailResult_Ext result = new MailResult_Ext();
		//
		List<ItemCountStruct> dataStructs = mailAtt.getDataStructCache();
		if (dataStructs.isEmpty()) {
			result.isSucess = true;
			return result;
		}
		//
		ItemResult_AddItem addResult = KSupportFactory.getItemModuleSupport().addItemsToBag(role, dataStructs, "邮件");
		//
		result.isSucess = addResult.isSucess;
		result.tips = addResult.tips;
		if (result.isSucess) {
			for (ItemCountStruct dataStruct : dataStructs) {
				result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, dataStruct.getItemTemplate().extItemName, dataStruct.itemCount));
			}
		}
		return result;
	}

	/**
	 * <pre>
	 * 提取货币附件
	 * 
	 * @param role
	 * @param mailAtt
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-1 下午4:25:19
	 * </pre>
	 */
	private static MailResult_Ext takeAttachment_money(KRole role, MailAttachmentMoney mailAtt) {
		MailResult_Ext result = new MailResult_Ext();
		//
		List<KCurrencyCountStruct> dataStructs = mailAtt.getDataStructCache();
		if (dataStructs.isEmpty()) {
			result.isSucess = true;
			return result;
		}
		//
		KSupportFactory.getCurrencySupport().increaseMoneys(role.getId(), dataStructs, mailAtt.getPresentType(), true);
		//
		result.isSucess = true;
		for (KCurrencyCountStruct dataStruct : dataStructs) {
			result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, dataStruct.currencyType.extName, dataStruct.currencyCount));
		}
		return result;
	}

	/**
	 * <pre>
	 * 提取角色属性附件
	 * 
	 * @param role
	 * @param mailAtt
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-1 下午6:02:46
	 * </pre>
	 */
	private static MailResult_Ext takeAttachment_roleAtt(KRole role, KMail mail, MailAttachmentRoleAtt mailAtt) {
		MailResult_Ext result = new MailResult_Ext();
		//
		List<AttValueStruct> dataStructs = mailAtt.getDataStructCache();
		if (dataStructs.isEmpty()) {
			result.isSucess = true;
			return result;
		}
		//
		KSupportFactory.getRoleModuleSupport().addAttsFromBaseReward(role, dataStructs, KRoleAttrModifyType.邮件附件, mail._title);
		//
		result.isSucess = true;
		for (AttValueStruct dataStruct : dataStructs) {
			result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, dataStruct.roleAttType.getExtName(), dataStruct.addValue));
		}
		return result;
	}

	/**
	 * <pre>
	 * 提取宠物附件
	 * 
	 * @param roleId
	 * @param mail
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-2 上午10:33:44
	 * </pre>
	 */
	private static MailResult_Ext takeAttachment_pet(KRole role, KMail mail, MailAttachmentPet mailAtt) {
		MailResult_Ext result = new MailResult_Ext();
		//
		List<Integer> dataStructs = mailAtt.getDataStructCache();
		if (dataStructs.isEmpty()) {
			result.isSucess = true;
			return result;
		}
		//
		CommonResult addResult = KSupportFactory.getPetModuleSupport().createPetsToRole(role.getId(), dataStructs, KPetFlowType.邮件附件.name()+":"+mail._title);
		//
		result.isSucess = addResult.isSucess;
		result.tips = addResult.tips;
		if (result.isSucess) {
			for (int tempId : dataStructs) {
				KPetTemplate temp = KSupportFactory.getPetModuleSupport().getPetTemplate(tempId);
				result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, temp.getNameEx(), 1));
			}
		}
		return result;
	}

	/**
	 * <pre>
	 * 提取时装附件
	 * 
	 * @param roleId
	 * @param mail
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-2 上午10:38:24
	 * </pre>
	 */
	private static MailResult_Ext takeAttachment_fashion(KRole role, MailAttachmentFashion mailAtt) {
		MailResult_Ext result = new MailResult_Ext();
		//
		List<Integer> dataStructs = mailAtt.getDataStructCache();
		if (dataStructs.isEmpty()) {
			result.isSucess = true;
			return result;
		}
		//
		CommonResult addResult = KSupportFactory.getFashionModuleSupport().addFashions(role, dataStructs, PresentPointTypeEnum.邮件附件.name());
		//
		result.isSucess = addResult.isSucess;
		result.tips = addResult.tips;
		if (result.isSucess) {
			for (int tempId : dataStructs) {
				KFashionTemplate temp = KSupportFactory.getFashionModuleSupport().getFashionTemplate(tempId);
				if (temp != null) {
					result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, temp.extName, 1));
				}
			}
		}
		return result;
	}
}
