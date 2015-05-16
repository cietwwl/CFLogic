package com.kola.kmp.logic.reward.activatecode;

import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.communication.KGameHttpRequestSender.KGameHttpRequestResult;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kmp.logic.item.KItemLogic;

public class ActivateCodeTask {

	private static final Logger _LOGGER = KGameLogger.getLogger(ActivateCodeTask.class);
	
	/**
	 * <pre>
	 * HTTP结果日志记录
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-12 上午10:37:35
	 * </pre>
	 */
	static class HTTPFutureTask implements KGameTimerTask {
		private KActivateCodePromoTypeEnum promoTypeEnum;
		private String tips;
		private Future<KGameHttpRequestResult> httpResult;
		
		private HTTPFutureTask(KActivateCodePromoTypeEnum promoTypeEnum, String tips, Future<KGameHttpRequestResult> httpResult) {
			this.promoTypeEnum = promoTypeEnum;
			this.tips = tips;
			this.httpResult = httpResult;
		}
		
		static void submitTask(KActivateCodePromoTypeEnum promoTypeEnum, String tips, Future<KGameHttpRequestResult> httpResult){
			KGame.newTimeSignal(new HTTPFutureTask(promoTypeEnum, tips, httpResult), 1, TimeUnit.SECONDS);
		}

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {
			try {
				if (promoTypeEnum == KActivateCodePromoTypeEnum.梦想) {
					// 梦想
					if (httpResult.get().content.equals("0")) {
						KItemLogic._OPEN_FIXEDBOX_LOGGER.warn(tips, "成功", "");
					} else {
						KItemLogic._OPEN_FIXEDBOX_LOGGER.warn(tips, "失败", httpResult.get().content);
					}
					return null;
				}

				if (promoTypeEnum == KActivateCodePromoTypeEnum.YY) {
					// YY
					JSONObject json = new JSONObject(httpResult.get().content);
					if (json.getInt("result")==0) {
						KItemLogic._OPEN_FIXEDBOX_LOGGER.warn(tips, "成功", "");
					} else {
						KItemLogic._OPEN_FIXEDBOX_LOGGER.warn(tips, "失败", httpResult.get().content);
					}
					return null;
				}
				
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				KItemLogic._OPEN_FIXEDBOX_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			}
			return null;
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}

		@Override
		public void done(KGameTimeSignal arg0) {
		}
	}	
}
