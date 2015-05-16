package com.kola.kmp.logic.role;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KGameGender;
import com.kola.kmp.logic.support.DirtyWordSupport;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * 名字管理器
 * 
 * </pre>
 * @author PERRY CHAN
 */
public class KRandomNameManager {

	private static final Logger _LOGGER = KGameLogger.getLogger(KRandomNameManager.class);
	private static final int BOTTOM_LINE = 5000;
	
	private static List<String> _allNamesMale = new ArrayList<String>();
	private static List<String> _allNamesFemale = new ArrayList<String>();
	private static final Queue<String> _usableNamesMale = new ConcurrentLinkedQueue<String>();
	private static final Queue<String> _usableNamesFemale = new ConcurrentLinkedQueue<String>();
	private static final Queue<String> _usedNamesMale = new ConcurrentLinkedQueue<String>();
	private static final Queue<String> _usedNamesFemale = new ConcurrentLinkedQueue<String>();
	private static final Map<Long, Object[]> _playerCurrentNames = new ConcurrentHashMap<Long, Object[]>();
	private static AtomicInteger _currentIdMale;
	private static AtomicInteger _currentIdFemale;
	private static String _usableNamePathMale;
	private static String _usableNamePathFemale;
	private static String _allNamePathMale;
	private static String _allNamePathFemale;
	private static String _idFilePath;
	private static boolean _hasRemoveSome = false;
	
	public static void loadNames(Element element) throws Exception {
		_usableNamePathMale = element.getChildTextTrim("usableNameFilePathMale");
		_usableNamePathFemale = element.getChildTextTrim("usableNameFilePathFemale");
		
		_allNamePathMale = element.getChildTextTrim("allNameFilePathMale");
		loadAllNames(_allNamePathMale, _allNamesMale);
		
		_allNamePathFemale = element.getChildTextTrim("allNameFilePathFemale");
		loadAllNames(_allNamePathFemale, _allNamesFemale);
		
		_idFilePath = element.getChildTextTrim("idFilePath");
		Properties pr = new Properties();
		pr.load(new FileInputStream(_idFilePath));
		
		_currentIdMale = new AtomicInteger(Integer.parseInt(pr.getProperty("currentIdMale")));
		_currentIdFemale = new AtomicInteger(Integer.parseInt(pr.getProperty("currentIdFemale")));
		
		int usingIdMale = Integer.parseInt(pr.getProperty("usingIdMale"));
		int usingIdFemale = Integer.parseInt(pr.getProperty("usingIdFemale"));
		
		loadNames(_currentIdMale, usingIdMale, _usableNamePathMale, _allNamesMale, _usableNamesMale);
		loadNames(_currentIdFemale, usingIdFemale, _usableNamePathFemale, _allNamesFemale, _usableNamesFemale);
	}
	
	private static void loadAllNames(String namePath, List<String> allNames)throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(namePath), "UTF-8"));
		String name;
		while ((name = br.readLine()) != null && (name = name.trim()).length() > 0) {
			allNames.add(name);
		}
		br.close();
		Collections.shuffle(allNames, new Random());
	}
	
	private static void loadNames(AtomicInteger currentId, int usingId, String currentNamePath, List<String> allnames, Queue<String> currentNames)throws IOException {
		List<String> tempList;
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(currentNamePath), "UTF-8"));
		String name = br.readLine();
		if (currentId.get() == usingId) {
			tempList = new ArrayList<String>();
			do {
				if(UtilTool.getStringLength(name) > 12) {
//					_LOGGER.info(">>>>字符长度大于12：{}<<<<", name);
					continue;
				} else if (name.indexOf(0xfffd) >= 0) {
//					_LOGGER.error("！！！！！！！！名字：{}，存在乱码！！！！！！！！", name);
					continue;
				} else if (name.indexOf(12288) >= 0) {
//					_LOGGER.error("！！！！！！！！名字：{}，存在全角空格！！！！！！！！", name);
					continue;
				}
				tempList.add(name);
			} while((name = br.readLine()) != null && (name = name.trim()).length() > 0);
		} else {
			tempList = generateNames(allnames, currentId);
		}
		br.close();
		Collections.shuffle(tempList, new Random());
		currentNames.addAll(tempList);
		tempList.clear();
	}
	
	private static boolean processDirtyWord(Collection<String> names) {

		boolean hasRemoveSome = false;
		
		DirtyWordSupport dirtySupport = KSupportFactory.getDirtyWordSupport();

		for (Iterator<String> it = names.iterator(); it.hasNext();) {
			String name = it.next();
			String dirword = dirtySupport.containDirtyWord(name);
			if (dirword != null) {
				it.remove();
				hasRemoveSome = true;
				_LOGGER.error("！！！！！！！！名字：" + name + " 已被剔除，包含非法字符" + dirword + "！！！！！！！！");
			}
		}
		
		return hasRemoveSome;
	}
	
	public static void onGameWorldInitComplete() throws KGameServerException {
		_hasRemoveSome = _hasRemoveSome | processDirtyWord(_allNamesMale);
		_hasRemoveSome = _hasRemoveSome | processDirtyWord(_allNamesFemale);
		processDirtyWord(_usableNamesMale);
		processDirtyWord(_usableNamesFemale);
	}
	
	public static String getRandomName(long playerId, int gender){
		Queue<String> tempUsableNames;
		Queue<String> preUsableNames = null;
		List<String> allNames = null;
		AtomicInteger currentId = null;
		switch (KGameGender.getGender(gender)) {
		case FEMALE:
			tempUsableNames = _usableNamesFemale;
			allNames = _allNamesFemale;
			currentId = _currentIdFemale;
			break;
		default:
		case MALE:
			tempUsableNames = _usableNamesMale;
			allNames = _allNamesMale;
			currentId = _currentIdMale;
			break;
		}
		String name = tempUsableNames.poll();
		if(name == null) {
			name = "";
		}
		Object[] objArray = _playerCurrentNames.put(playerId, new Object[] { gender, name });
		String previous = null;
		if (objArray != null) {
			previous = (String) objArray[1];
			KGameGender preGender = KGameGender.getGender((Integer) objArray[0]);
			switch (preGender) {
			case FEMALE:
				preUsableNames = _usableNamesFemale;
				break;
			case MALE:
				preUsableNames = _usableNamesMale;
				break;
			}
			if (previous != null && previous.length() > 0) {
				preUsableNames.add(previous);
			}
		}
		if(tempUsableNames.size() < BOTTOM_LINE) {
			KGame.newTimeSignal(new KRandomNameTask(allNames, currentId, tempUsableNames), 1, TimeUnit.SECONDS);
		}
		return name;
	}
	
	public static void roleCreated(long playerId, String name) {
		destroyName(playerId, name);
	}
	
	public static void nameUsed(long playerId, String name) {
		destroyName(playerId, name);
	}
	
	private static void destroyName(long playerId, String name) {
		Object[] objArray = _playerCurrentNames.remove(playerId);
		if (objArray != null) {
			String tempName = (String) objArray[1];
			if (tempName == null || tempName.length() == 0) {
				return;
			}
			int gender = (Integer) objArray[0];
			Queue<String> usedNames = null;
			Queue<String> usableNames = null;
			switch(KGameGender.getGender(gender)) {
			case FEMALE:
				usedNames = _usedNamesFemale;
				usableNames = _usableNamesFemale;
				break;
			case MALE:
				usedNames = _usedNamesMale;
				usableNames = _usableNamesMale;
				break;
			}
			if (usedNames != null) {
				if (tempName.equals(name)) {
					usedNames.add(tempName);
				} else {
					usableNames.add(tempName);
				}
			}
		}
	}
	
	public static void shutdown() {
		try {
			Charset cs = Charset.forName("UTF-8");
			
			if (_hasRemoveSome) {
				saveName(cs, _allNamePathMale, _allNamesMale);
				saveName(cs, _allNamePathFemale, _allNamesFemale);
			}
			
			saveName(cs, _usableNamePathMale, _usableNamesMale);
			
			saveName(cs, _usableNamePathFemale, _usableNamesFemale);
			
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(_idFilePath)), cs);
			Properties pr = new Properties();
			pr.put("currentIdFemale", String.valueOf(_currentIdFemale));
			pr.put("usingIdFemale", String.valueOf(_currentIdFemale));
			pr.put("currentIdMale", String.valueOf(_currentIdMale));
			pr.put("usingIdMale", String.valueOf(_currentIdMale));
			pr.store(writer, null);
			writer.flush();
			writer.close();
		} catch (Exception e) {
			_LOGGER.error("随机名字数据回写时出现异常！！", e);
		}
	}
	
	private static void saveName(Charset cs, String path, Collection<String> nameList) throws Exception {
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(path)), cs);
		for (Iterator<String> itr = nameList.iterator(); itr.hasNext();) {
			writer.write(itr.next());
			writer.write("\n");
		}
		writer.flush();
		writer.close();
	}
	
	private static List<String> generateNames(List<String> allNames, AtomicInteger currentId) {
		List<String> tempList = new ArrayList<String>();
		String tempName;
		for (int i = 0; i < allNames.size(); i++) {
			tempName = allNames.get(i) + currentId;
			if (tempName.length() > KRoleModuleConfig.getRoleNameLengthMax()) {
				continue;
			}
			tempList.add(tempName);
		}
		currentId.incrementAndGet();
		return tempList;
	}
	
	private static class KRandomNameTask implements KGameTimerTask {
		
		private List<String> _allNames;
		private AtomicInteger _currentId;
		private Queue<String> _usableNames;
		
		private KRandomNameTask(List<String> pAllNames, AtomicInteger pCurrentId, Queue<String> pUsableNames) {
			this._allNames = pAllNames;
			this._currentId = pCurrentId;
			this._usableNames = pUsableNames;
		}
		
		@Override
		public String getName() {
			return "KRandomNameTask" + this.hashCode();
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal timeSignal) throws KGameServerException {
			List<String> names = generateNames(_allNames, _currentId);
			Collections.shuffle(names, new Random());
			_usableNames.addAll(names);
			return "finished";
		}

		@Override
		public void done(KGameTimeSignal timeSignal) {
			
		}

		@Override
		public void rejected(RejectedExecutionException e) {
			
		}
		
	}
	
	/**
	 * 
	 * @param file
	 * @throws Exception
	 */
	static void replaceFullSpaceChar(String path) throws Exception {
		char c12288 = 12288; // 全角空格
		String strC = String.valueOf(c12288);
		char c65279 = 65279; // 不知名空格
//		String str65279 = String.valueOf(c65279);
		byte[] enter = "\n".getBytes();
		File file = new File(path);
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
		String name = br.readLine();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while (name != null && (name = name.trim()).length() > 0) {
			if (name.indexOf(c12288) >= 0) {
				_LOGGER.info("包含全角空格名字：{}", name);
				name = name.replace(strC, "");
			}
			if(name.indexOf(c65279) >= 0) {
				_LOGGER.info("以空格开头的名字：{}", name);
				char[] array = name.toCharArray();
				name = String.valueOf(Arrays.copyOfRange(array, 1, array.length));
			}
			bos.write(name.getBytes());
			bos.write(enter);
			name = br.readLine();
		}
		br.close();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(bos.toByteArray());
		fos.flush();
		fos.close();
		bos.close();
	}
	
	public static void main(String[] args) throws Exception {
//		char c = 12288;
//		String strC = String.valueOf(c);
//		char c65279 = 65279;
//		KGameExcelFile file = new KGameExcelFile("./res/randomName/temp.xls");
//		KGameExcelRow[] allRows = file.getTable("sheet1", 1).getAllDataRows();
//		OutputStreamWriter fos = new OutputStreamWriter(new FileOutputStream(new File("./res/randomName/temp.txt")), Charset.forName("UTF-8"));
//		String name;
//		for(int i = 0; i < allRows.length; i++) {
//			name = allRows[i].getData("名字");
//			if (name.indexOf(c) >= 0) {
//				name = name.replace(strC, "");
//			}
//			if(name.indexOf(c65279) >= 0) {
//				char[] array = name.toCharArray();
//				name = String.valueOf(Arrays.copyOfRange(array, 1, array.length));
//			}
//			fos.write(allRows[i].getData("名字").trim());
//			fos.write("\n");
//		}
//		fos.flush();
//		fos.close();
//		replaceFullSpaceChar("./res/randomName/allMaleNames.txt");
//		replaceFullSpaceChar("./res/randomName/allFemaleNames.txt");
//		replaceFullSpaceChar("./res/randomName/usableMaleNames.txt");
//		replaceFullSpaceChar("./res/randomName/usableFemaleNames.txt");
//		loadNames(new AtomicInteger(), 0, "./res/randomName/usableMaleNames.txt", null, new ArrayList<String>());
	}
	
}
