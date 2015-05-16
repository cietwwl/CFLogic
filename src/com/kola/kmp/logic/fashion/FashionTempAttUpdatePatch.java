package com.kola.kmp.logic.fashion;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.RunTimeTask;
import com.kola.kmp.logic.fashion.KFashionDataStructs.KFashionTemplate;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.PropertyTypeEnum;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountTemplate;
import com.kola.kmp.logic.mount.message.KPushMountMsg;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;

/**
 * <pre>
 * 时装模板属性更新
 * 
 * @author CamusHuang
 * @creation 2014-12-23 下午12:24:55
 * </pre>
 */
public class FashionTempAttUpdatePatch implements RunTimeTask {
	private static Logger _LOGGER = KGameLogger.getLogger(FashionTempAttUpdatePatch.class);

	/**
	 * data=模板ID;属性类型（内部用-分隔）;属性值（内部用-分隔）
	 */
	public String run(String data) {

		String[][] datas = new String[][] { 
				{"15014", "106,110,111,115,116,129,130", "800,250,100,750,500,75000,1000" }, //
				{ "16014", "106,110,111,115,116,129,130", "800,250,100,750,500,75000,1000" }, //
				{ "17014", "106,110,111,115,116,129,130", "800,250,100,750,500,75000,1000" }// 
				};

		try {

			for (int i = 0; i < datas.length; i++) {
				String[] tempA = datas[i];
				int tempId = Integer.parseInt(tempA[0]);
				String[] attTyps = tempA[1].split(",");
				String[] attValues = tempA[2].split(",");

				KFashionTemplate temp = KFashionDataManager.mFashionTemplateManager.getFashionTemplate(tempId);
				if (temp == null) {
					return "模板不存在 id=" + tempId;
				}

				Map<KGameAttrType, Integer> tempEffects = new LinkedHashMap<KGameAttrType, Integer>(temp.allEffects);
				for (int k = 0; k < attTyps.length; k++) {
					KGameAttrType type = KGameAttrType.getAttrTypeEnum(Integer.parseInt(attTyps[k]));
					if (type == null) {
						return "属性加成类型不存在 type=" + attTyps[k];
					}

					int value = Integer.parseInt(attValues[k]);
					if (value <= 0) {
						return "属性加成值错误 =" + value;
					}

					if (!tempEffects.containsKey(type)) {
						return "原模板属性加成类型不存在 type=" + attTyps[k];
					}

					tempEffects.put(type, value);
				}

				temp.allEffects = Collections.unmodifiableMap(tempEffects);
			}
			return "执行完成";
		} catch (Exception e) {
			e.printStackTrace();
			return "发生异常：" + e.getMessage();
		}
	}
}
