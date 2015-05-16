package com.kola.kmp.logic.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.shop.random.KRandomShopDataManager;

/**
 * <pre>
 * 本类用于为【简单日志管理】提供支持
 * 目前用于随机商店购买日志、每日幸运大奖日志
 * 
 * @author CamusHuang
 * @creation 2014-4-24 上午11:43:42
 * </pre>
 */
public class KSimpleDialyManager {
	public static final Logger _LOGGER = KGameLogger.getLogger(KSimpleDialyManager.class);
	//

	// 读写锁
	public final ReentrantLock rwLock = new ReentrantLock();
	//
	private String logFileDir;
	private String logFileName;
	//
	private AtomicInteger dialyIDGen = new AtomicInteger(1);

	// 购买日志:xxx购买了xxx x件 xx元宝
	private LinkedList<Dialy> dialys = new LinkedList<Dialy>();
	private List<Dialy> dialysCopy = Collections.emptyList();// 提供给外部访问
	private int maxDialyId;

	public KSimpleDialyManager(String logFileDir, String logFileName) {
		this.logFileDir = logFileDir;
		this.logFileName = logFileName;
	}

	public void addDialy(String log) {
		rwLock.lock();
		try {
			Dialy temp = new Dialy(dialyIDGen.getAndIncrement(), log);
			maxDialyId = temp.id;
			dialys.add(temp);
			if (dialys.size() > KRandomShopDataManager.RandomLogMaxCount) {
				dialys.removeFirst();
			}
			dialysCopy = Collections.unmodifiableList(new ArrayList<Dialy>(dialys));
		} finally {
			rwLock.unlock();
		}
	}

	public List<Dialy> getAllDialysCopy() {
		rwLock.lock();
		try {
			return dialysCopy;
		} finally {
			rwLock.unlock();
		}
	}

	public int getMaxDialyId() {
		return maxDialyId;
	}

	public void saveDialys() {
		List<Dialy> dialys = getAllDialysCopy();
		if (dialys.isEmpty()) {
			return;
		}

		KGameUtilTool.saveSimpleDialy(logFileDir, logFileName, null, dialys);
	}

	public void loadDialys() {
		List<String> result = KGameUtilTool.loadSimpleDialy(logFileDir, logFileName, null);
		int toIndex = Math.min(result.size(), KRandomShopDataManager.RandomLogMaxCount);
		result = result.subList(0, toIndex);

		for (String log : result) {
			Dialy temp = new Dialy(dialyIDGen.getAndIncrement(), log);
			maxDialyId = temp.id;
			dialys.add(temp);
		}
		dialysCopy = Collections.unmodifiableList(new ArrayList<Dialy>(dialys));
	}

	public static class Dialy {
		public final int id;
		public final String dialy;

		private Dialy(int id, String dialy) {
			this.id = id;
			this.dialy = dialy;
		}

		public String toString() {
			return dialy;
		}
	}
}
