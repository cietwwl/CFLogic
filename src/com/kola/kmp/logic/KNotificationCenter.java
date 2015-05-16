package com.kola.kmp.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.koala.game.KGame;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.role.Role;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.role.KRole;

public class KNotificationCenter {
	
	private static final List<INotificationTemplate> _notificationList = new ArrayList<INotificationTemplate>();
	
	public static void loadNotification(String path) throws Exception {
		_notificationList.clear();
		KGameExcelFile file = new KGameExcelFile(path);
		KGameExcelRow[] allRows = file.getTable("推送配置", 2).getAllDataRows();
		KGameExcelRow row;
		byte type;
		INotificationTemplate notification;
		for (int i = 0; i < allRows.length; i++) {
			row = allRows[i];
			type = row.getByte("type");
			switch (type) {
			case INotificationTemplate.TYPE_PUSH_BY_REGISTER_TIME:
				notification = new KLoginNotificationTemplateImpl(row);
				break;
			case INotificationTemplate.TYPE_PUSH_BY_LEVEL:
				notification = new KPushNotificationByLevelImpl(row);
				break;
			default:
				notification = null;
			}
			if(notification != null) {
				_notificationList.add(notification);
			}
		}
	}
	
	public static List<INotification> checkNotificationList(KRole role) {
		if(_notificationList.size() > 0) {
			List<INotification> list = new ArrayList<INotification>();
			for(int i = 0; i < _notificationList.size(); i++) {
				list.addAll(_notificationList.get(i).getNotifications(role));
			}
			return list;
		}
		return Collections.emptyList();
	}
	
	public static abstract class KAbsNotificationTemplate implements INotificationTemplate {

		
		private long _startTime;
		private long _endTime;
		private String _content;
		
		protected KAbsNotificationTemplate(KGameExcelRow row) {
			String strStartTime = row.getData("startTime").trim();
			String strEndTime = row.getData("endTime").trim();
			if(strStartTime == null || strStartTime.length() == 0) {
				_startTime = 0;
			} else {
				_startTime = UtilTool.getTimeInMillis(strStartTime, UtilTool.DATE_FORMAT);
			}
			if(strEndTime == null || strEndTime.length() == 0) {
				_endTime = Long.MAX_VALUE;
			} else {
				_endTime = UtilTool.getTimeInMillis(strEndTime, UtilTool.DATE_FORMAT);
			}
			_content = row.getData("content").trim();
			if(_content.length() == 0) {
				throw new RuntimeException("推送内容字数为0，行数：" + row.getIndexInFile());
			}
			if(_startTime > _endTime) {
				throw new RuntimeException("startTime>endTime，行数：" + row.getIndexInFile());
			}
		}
		
		@Override
		public long getStartTime() {
			return _startTime;
		}

		@Override
		public long getEndTime() {
			return _endTime;
		}

		@Override
		public String getContent() {
			return _content;
		}
		
	}

	public static class KLoginNotificationTemplateImpl extends KAbsNotificationTemplate implements INotificationTemplate {

		private int _pushHour;
		private int _pushMin;
		private int[] _pushDate;
		
		public KLoginNotificationTemplateImpl(KGameExcelRow row) {
			super(row);
			String[] args = row.getData("script").split(";");
			if (args[0].contains(":")) {
				String[] time = args[0].split(":");
				this._pushHour = Integer.parseInt(time[0]);
				this._pushMin = Integer.parseInt(time[1]);
				if(_pushHour > 23 || _pushHour < 0) {
					throw new RuntimeException("参数[0]时间不合法，小时需小于24并且大于0，行数：" + row.getIndexInFile());
				}
				if(_pushMin > 59 || _pushMin < 0) {
					throw new RuntimeException("参数[0]时间不合法，分钟需小于60并且大于0，行数：" + row.getIndexInFile());
				}
			} else {
				throw new RuntimeException("参数[0]时间不合法，格式应该是HH:mm，行数：" + row.getIndexInFile());
			}
			_pushDate = new int[args.length - 1];
			for (int i = 1, index = 0; i < args.length; i++, index++) {
				_pushDate[index] = Integer.parseInt(args[i]);
				if (_pushDate[index] < 0) {
					throw new RuntimeException("推送日期不合法，必须大于0，行数：" + row.getIndexInFile());
				}
			}
			Arrays.sort(_pushDate);
		}	
		
		@Override
		public List<INotification> getNotifications(Role role) {
			KGamePlayerSession session = KGame.getPlayerSession(role.getPlayerId());
			if(session.getBoundPlayer() != null) {
				long createTime = session.getBoundPlayer().getCreateTimeMillis();
				List<Integer> pushDays = new ArrayList<Integer>();
				long now = System.currentTimeMillis();
				long sub = now - session.getBoundPlayer().getCreateTimeMillis();
				int subDays = (int) TimeUnit.DAYS.convert(sub, TimeUnit.MILLISECONDS);
				if (UtilTool.isBetweenDay(session.getBoundPlayer().getCreateTimeMillis(), now)) {
					subDays++;
				}
				int tempDay;
				for (int i = 0; i < _pushDate.length; i++) {
					tempDay = _pushDate[i];
					if (tempDay > subDays) {
						pushDays.add(tempDay);
					}
				}
				if(pushDays.size() > 0) {
					List<INotification> rtnList = new ArrayList<INotification>();
					Calendar createCalendar = Calendar.getInstance();
					createCalendar.setTimeInMillis(createTime);
					createCalendar.set(Calendar.HOUR_OF_DAY, _pushHour);
					createCalendar.set(Calendar.MINUTE, _pushMin);
					createCalendar.set(Calendar.SECOND, 0);
					int pre = 0;
					int current = 0;
					for(int i = 0; i < pushDays.size(); i++) {
						current = pushDays.get(i);
						createCalendar.add(Calendar.DAY_OF_YEAR, current - pre);
						pre = current;
						rtnList.add(new KNotificationImpl(createCalendar, getContent()));
					}
					return rtnList;
				}
			}
			return Collections.emptyList();
		}
		
	}
	
	public static class KPushNotificationByLevelImpl extends KAbsNotificationTemplate implements INotificationTemplate {

		private int _pushHour;
		private int _pushMin;
		private int _pushLevel;
		
		public KPushNotificationByLevelImpl (KGameExcelRow row) {
			super(row);
			String[] args = row.getData("script").split(";");
			if (args[0].contains(":")) {
				String[] time = args[0].split(":");
				this._pushHour = Integer.parseInt(time[0]);
				this._pushMin = Integer.parseInt(time[1]);
				if (_pushHour > 23 || _pushHour < 0) {
					throw new RuntimeException("参数[0]时间不合法，小时需小于24并且大于0，行数：" + row.getIndexInFile());
				}
				if (_pushMin > 59 || _pushMin < 0) {
					throw new RuntimeException("参数[0]时间不合法，分钟需小于60并且大于0，行数：" + row.getIndexInFile());
				}
			} else {
				throw new RuntimeException("参数[0]时间不合法，格式应该是HH:mm，行数：" + row.getIndexInFile());
			}
			_pushLevel = Integer.parseInt(args[1]);
		}
		
		@Override
		public List<INotification> getNotifications(Role role) {
			if (role.getLevel() >= _pushLevel) {
				Calendar pushTime = Calendar.getInstance();
				pushTime.set(Calendar.HOUR_OF_DAY, _pushHour);
				pushTime.set(Calendar.MINUTE, _pushMin);
				if(System.currentTimeMillis() + 600000 < pushTime.getTimeInMillis()) {
					List<INotification> list = new ArrayList<INotification>();
					list.add(new KNotificationImpl(pushTime, this.getContent()));
					return list;
				}
			}
			return Collections.emptyList();
		}
		
	}
	
	public static class KNotificationImpl implements INotification {

		private int _year;
		private int _month;
		private int _day;
		private int _hour;
		private int _minute;
		private String _content;
		
		public KNotificationImpl(Calendar time, String content) {
			_year = time.get(Calendar.YEAR);
			_month = time.get(Calendar.MONTH) + 1;
			_day = time.get(Calendar.DAY_OF_MONTH);
			_hour = time.get(Calendar.HOUR_OF_DAY);
			_minute = time.get(Calendar.MINUTE);
			_content = content;
		}
		
		@Override
		public int getYear() {
			return _year;
		}

		@Override
		public int getMonth() {
			return _month;
		}
		
		@Override
		public int getDay() {
			return _day;
		}

		@Override
		public int getHour() {
			return _hour;
		}

		@Override
		public int getMinute() {
			return _minute;
		}

		@Override
		public String getContent() {
			return _content;
		}

		@Override
		public String toString() {
			return "KNotificationImpl [year=" + _year + ", month=" + _month + ", day=" + _day + ", hour=" + _hour + ", minute=" + _minute + ", content=" + _content + "]";
		}
		
	}
}
