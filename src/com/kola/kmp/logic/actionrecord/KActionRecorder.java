package com.kola.kmp.logic.actionrecord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.util.DayClearTask;
import com.kola.kmp.logic.util.tips.GlobalTips;

/**
 * 
 * @author PERRY CHAN
 */
public class KActionRecorder {

	private static final Map<Long, KActionRecordData> _actionRecordMap = new ConcurrentHashMap<Long, KActionRecordData>();
	private static final SimpleDateFormat _SDF = new SimpleDateFormat("yyyy_MMdd");
	private static final String _dirRoot = "./res/output/roleAction";
	private static final String _metaPath = _dirRoot + "/roleActionRecord.meta";
	private static final String _sheetName = "roleAction";
	private static final int _hourForSave = 3; // 开始时间，凌晨3点
	
	private static final int _colNumOfRoleId = 0;
	private static final int _colNumOfRoleName = _colNumOfRoleId + 1;
	private static final int _colNumOfRoleLv = _colNumOfRoleName + 1;
	private static final int _colNumOffset = _colNumOfRoleLv + 1;
	
	static {
		File file = new File(_dirRoot);
		if (!file.exists()) {
			file.mkdir();
		}
	}
	
	public static void init() {
		readTemporaryFile();
		Calendar instance = Calendar.getInstance();
		int nowHour = instance.get(Calendar.HOUR_OF_DAY);
		if (nowHour >= _hourForSave) {
			instance.roll(Calendar.DAY_OF_YEAR, true);
		}
		instance.set(Calendar.HOUR_OF_DAY, _hourForSave);
		instance.set(Calendar.MINUTE, 0);
		instance.set(Calendar.SECOND, 0);
		long delay = instance.getTimeInMillis() - System.currentTimeMillis();
		new DayClearTask(TimeUnit.MILLISECONDS.convert(_hourForSave, TimeUnit.HOURS)) {

			@Override
			public String getNameCN() {
				return "角色行为统计保存任务";
			}

			@Override
			public void doWork() throws KGameServerException {
				save(false);
			}
		}.start(delay);
	}
	
	static void notifyRoleJoinGame(KRole role) {
		KActionRecordData data = _actionRecordMap.get(role.getId());
		if (data == null) {
			synchronized (role) {
				data = _actionRecordMap.get(role.getId());
				if (data == null) {
					data = new KActionRecordData(role.getName());
					data.updateRoleLv(role.getLevel());
					_actionRecordMap.put(role.getId(), data);
				} else if (data.getRoleLv() != role.getLevel()) {
					data.updateRoleLv(role.getLevel());
				}
			}
		}
	}
	
	static void notifyRoleUpgrade(KRole role, int roleLv) {
		KActionRecordData data = _actionRecordMap.get(role.getId());
		if (data != null) {
			data.updateRoleLv(role.getLevel());
		}
	}
	
	public static void recordAction(long roleId, KActionType type, int count) {
		KActionRecordData data = _actionRecordMap.get(roleId);
		data.recordAction(type, count);
	}
	
	public static void shutdown() {
		save(true);
	}
	
	static void save(boolean shutdown) {
		Calendar instance = Calendar.getInstance();
		String fileName = null;
		if (shutdown) {
			if(instance.get(Calendar.HOUR_OF_DAY) < _hourForSave) {
				instance.roll(Calendar.DAY_OF_YEAR, false);
			}
			fileName = StringUtil.format("{}/{}_{}_temp.xls", _dirRoot, KGame.getGSID(), _SDF.format(instance.getTime()));
		} else {
			instance.roll(Calendar.DAY_OF_YEAR, false);
			fileName = StringUtil.format("{}/{}_{}.xls", _dirRoot, KGame.getGSID(), _SDF.format(instance.getTime()));
		}
		try {
			writeExcelFile(fileName, _sheetName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(shutdown) {
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(_metaPath));
				bw.write(fileName);
				bw.write("\n");
				bw.write(String.valueOf(System.currentTimeMillis()));
				bw.write("\n");
				bw.flush();
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				File metaFile = new File(_metaPath);
				if (metaFile.exists()) {
					BufferedReader br = new BufferedReader(new FileReader(metaFile));
					File target = new File(br.readLine());
					if(target.exists()) {
						target.delete();
					}
					br.close();
					metaFile.delete();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void readTemporaryFile() {
		File meta = new File(_metaPath);
		if (meta.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(meta));
				String path = br.readLine();
				long saveTime = Long.parseLong(br.readLine());
				br.close();
				File file = new File(path);
				if (file.exists()) {
					Calendar instance = Calendar.getInstance();
					int nowYear = instance.get(Calendar.YEAR);
					int nowDay = instance.get(Calendar.DAY_OF_YEAR);
					int nowHour = instance.get(Calendar.HOUR_OF_DAY);
					instance.setTimeInMillis(saveTime);
					int saveDay = instance.get(Calendar.DAY_OF_YEAR);
					int saveHour = instance.get(Calendar.HOUR_OF_DAY);
					if (saveDay == nowDay) {
						// 停机保存时间与开机时间是在同一日
						if ((saveHour >= _hourForSave || nowHour < _hourForSave) && nowYear == instance.get(Calendar.YEAR)) {
							// 保存的小时是在凌晨3点之前或者开机时间是在凌晨3点之前，并且年份是一样。
							readTemporaryRecord(path);
						} else {
							instance.roll(Calendar.DAY_OF_YEAR, false);
							moveFile(path, StringUtil.format("{}/{}_{}.xls", _dirRoot, KGame.getGSID(), _SDF.format(instance.getTime())));
						}
					} else {
						if (nowHour >= _hourForSave || nowDay - saveDay > 1) {
							moveFile(path, StringUtil.format("{}/{}_{}.xls", _dirRoot, KGame.getGSID(), _SDF.format(instance.getTime())));
						} else {
							// 停机保存时间是前一天，并且开机时间在凌晨3点之前
							readTemporaryRecord(path);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void readTemporaryRecord(String path) throws Exception {
		KGameExcelFile file = new KGameExcelFile(path);
		KGameExcelTable table = file.getTable(_sheetName, 1);
		KGameExcelRow[] allRows = table.getAllDataRows();
		KActionRecordData data;
		KGameExcelRow row;
		KActionType[] allTypes = KActionType.values();
		KActionType currentType;
		for (int i = 0; i < allRows.length; i++) {
			row = allRows[i];
			data = new KActionRecordData(row.getData(GlobalTips.getTipsRoleNameLabel()));
			if(row.containsCol(GlobalTips.getTipsRoleLvLabel())) {
				data.updateRoleLv(row.getInt(GlobalTips.getTipsRoleLvLabel()));
			} else {
				data.updateRoleLv(1);
			}
			for (int k = 0; k < allTypes.length; k++) {
				currentType = allTypes[k];
				if (row.containsCol(currentType.name)) {
					data.recordAction(currentType, row.getInt(currentType.name));
				} else {
					data.recordAction(currentType, 0);
				}
			}
			_actionRecordMap.put(row.getLong(GlobalTips.getTipsRoleIdLabel()), data);
		}
	}
	
	private static void moveFile(String src, String target) {
		try {
			File file = new File(src);
			if (file.exists()) {
				FileInputStream in = new FileInputStream(file);
				FileOutputStream fos = new FileOutputStream(target);
				byte[] buf = new byte[1024];
				int readLength;
				while ((readLength = in.read(buf)) > 0) {
					fos.write(buf, 0, readLength);
				}
				in.close();
				fos.flush();
				fos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void writeExcelFile(String path, String sheetName) throws IOException, RowsExceededException, WriteException {
		FileOutputStream fos = new FileOutputStream(new File(path));
		WritableWorkbook workBook = Workbook.createWorkbook(fos);
		WritableSheet sheet = workBook.createSheet(sheetName, 0);
		int rowNum = 0;
		WritableCell cell;
		Map.Entry<Long, KActionRecordData> entry;
		KActionRecordData record;
		KActionType[] allTypes = KActionType.values();
		sheet.addCell(new Label(_colNumOfRoleId, rowNum, GlobalTips.getTipsRoleIdLabel()));
		sheet.addCell(new Label(_colNumOfRoleName, rowNum, GlobalTips.getTipsRoleNameLabel()));
		sheet.addCell(new Label(_colNumOfRoleLv, rowNum, GlobalTips.getTipsRoleLvLabel()));
		for (int i = 0, colNum = _colNumOffset; i < KActionType.ALL_ACTION_NAME.size(); i++, colNum++) {
			cell = new Label(colNum, rowNum, KActionType.ALL_ACTION_NAME.get(i));
			sheet.addCell(cell);
		}
		for (Iterator<Map.Entry<Long, KActionRecordData>> itr = _actionRecordMap.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			record = entry.getValue();
			rowNum++;
			sheet.addCell(new Label(_colNumOfRoleId, rowNum, String.valueOf(entry.getKey())));
			sheet.addCell(new Label(_colNumOfRoleName, rowNum, record.getRoleName()));
			sheet.addCell(new Label(_colNumOfRoleLv, rowNum, String.valueOf(record.getRoleLv())));
			for (int i = 0, colNum = _colNumOffset; i < allTypes.length; i++, colNum++) {
				sheet.addCell(new Label(colNum, rowNum, record.getActionCount(allTypes[i]).toString()));
			}
			record.reset();
		}
		workBook.write();
		workBook.close();
		fos.close();
	}
 }
