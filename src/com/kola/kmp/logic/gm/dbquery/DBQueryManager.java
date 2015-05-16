package com.kola.kmp.logic.gm.dbquery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.koala.game.dataaccess.dbconnectionpool.DBConnectionPoolAdapter;
import com.koala.game.dataaccess.dbconnectionpool.mysql.DefineDataSourceManagerIF;
import com.koala.game.dataaccess.dbobj.flowdata.DBChargeRecord;
import com.koala.game.dataaccess.dbobj.flowdata.DBFunPointConsumeRecord;
import com.koala.game.dataaccess.dbobj.flowdata.DBPresentPointRecord;
import com.koala.game.dataaccess.dbobj.flowdata.DBShopSellItemRecord;
import com.koala.game.logging.KGameLogger;

public class DBQueryManager {
	private static final KGameLogger _LOGGER = KGameLogger.getLogger(DBQueryManager.class);

	private static DBQueryManager instance;

	public static DBQueryManager getInstance() {
		if (instance == null) {
			instance = new DBQueryManager();
		}
		return instance;
	}

	// private int serverId;
	private DefineDataSourceManagerIF platformPool;
	private DefineDataSourceManagerIF gsPool;

	public DBQueryManager() {
		platformPool = DBConnectionPoolAdapter.getPlatformDBConnectionPool();
		gsPool = DBConnectionPoolAdapter.getLogicDBConnectionPool();
		// serverId = KGame.getGSID();
	}

	public List<DBChargeRecord> queryDBChargeRecord(long roleId, long startTime, long endTime) {
		List<DBChargeRecord> recordList = new ArrayList<DBChargeRecord>();
		String sql = "select * from charge_record where role_id = ? and charge_time>=? and charge_time<=?";
		Connection con = platformPool.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ps.setLong(1, roleId);
			ps.setTimestamp(2, new Timestamp(startTime));
			ps.setTimestamp(3, new Timestamp(endTime));

			rs = platformPool.executeQuery(ps);
			if (rs != null && rs.next()) {
				rs.beforeFirst();
				while (rs.next()) {
					long player_id = rs.getLong("player_id");
					long role_id = rs.getLong("role_id");
					String role_name = rs.getString("role_name");
					int role_level = rs.getInt("role_level");
					byte is_first_charge = (byte) (rs.getInt("is_first_charge"));
					int rmb = rs.getInt("rmb");
					int charge_point = rs.getInt("charge_point");
					String card_num = rs.getString("card_num");
					String card_password = rs.getString("card_password");
					long charge_time = rs.getTimestamp("charge_time") != null ? rs.getTimestamp("charge_time").getTime() : 0;
					int charge_type = rs.getInt("charge_type");
					int promo_id = rs.getInt("promo_id");
					int parent_promo_id = rs.getInt("parent_promo_id");
					int channel_id = rs.getInt("channel_id");
					int server_id = rs.getInt("server_id");
					String desc = rs.getString("descr");
					DBChargeRecord record = new DBChargeRecord(player_id, role_id, role_name, role_level, is_first_charge, rmb, charge_point, card_num, card_password, charge_time, charge_type,
							promo_id, parent_promo_id, channel_id, server_id, desc);
					recordList.add(record);
				}
			}

		} catch (SQLException ex) {
			_LOGGER.error("GM工具方法queryDBChargeRecord()查询充值流水失败，数据库发生未知异常", ex);
			// throw new KGameDBException("数据库发生未知异常", ex,
			// KGameDBException.CAUSE_DB_SHUTDOWN_WITH_UNKNOW_REASON,
			// "注册账号失败，数据库发生未知异常");
		} finally {
			platformPool.closeResultSet(rs);
			platformPool.closePreparedStatement(ps);
			platformPool.closeConnection(con);
		}

		return recordList;
	}

	public List<DBPresentPointRecord> queryDBPresentPointRecord(long roleId, long startTime, long endTime) {
		List<DBPresentPointRecord> recordList = new ArrayList<DBPresentPointRecord>();
		String sql = "select * from present_game_point_record where role_id = ? and present_time>=? and present_time<=?";
		Connection con = platformPool.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ps.setLong(1, roleId);
			ps.setTimestamp(2, new Timestamp(startTime));
			ps.setTimestamp(3, new Timestamp(endTime));

			rs = platformPool.executeQuery(ps);
			if (rs != null && rs.next()) {
				rs.beforeFirst();
				while (rs.next()) {
					long player_id = rs.getLong("player_id");
					long role_id = rs.getLong("role_id");
					String role_name = rs.getString("role_name");
					int present_point = rs.getInt("present_point");
					int type = rs.getInt("type");
					long present_time = rs.getTimestamp("present_time") != null ? rs.getTimestamp("present_time").getTime() : 0;
					int promo_id = rs.getInt("promo_id");
					int parent_promo_id = rs.getInt("parent_promo_id");
					int server_id = rs.getInt("server_id");
					String desc = rs.getString("descr");
					DBPresentPointRecord record = new DBPresentPointRecord(player_id, role_id, role_name, present_point, type, desc, present_time, promo_id, parent_promo_id, server_id);
					recordList.add(record);
				}
			}

		} catch (SQLException ex) {
			_LOGGER.error("GM工具方法queryDBPresentPointRecord()查询赠送点数流水失败，数据库发生未知异常", ex);
			// throw new KGameDBException("数据库发生未知异常", ex,
			// KGameDBException.CAUSE_DB_SHUTDOWN_WITH_UNKNOW_REASON,
			// "注册账号失败，数据库发生未知异常");
		} finally {
			platformPool.closeResultSet(rs);
			platformPool.closePreparedStatement(ps);
			platformPool.closeConnection(con);
		}

		return recordList;
	}

	public List<DBFunPointConsumeRecord> queryDBFunPointConsumeRecord(long roleId, long startTime, long endTime) {
		List<DBFunPointConsumeRecord> recordList = new ArrayList<DBFunPointConsumeRecord>();
		String sql = "select * from fun_point_consume_record where role_id = ? and consume_time>=? and consume_time<=?";
		Connection con = gsPool.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ps.setLong(1, roleId);
			ps.setTimestamp(2, new Timestamp(startTime));
			ps.setTimestamp(3, new Timestamp(endTime));

			rs = gsPool.executeQuery(ps);
			if (rs != null && rs.next()) {
				rs.beforeFirst();
				while (rs.next()) {
					long playerId = rs.getLong("player_id");
					long role_id = rs.getLong("role_id");
					int funType = rs.getInt("fun_type");
					int consumePoint = rs.getInt("consume_point");
					long consumeTime = rs.getTimestamp("consume_time") != null ? rs.getTimestamp("consume_time").getTime() : 0;
					int promoId = rs.getInt("promo_id");
					int parentPromoId = rs.getInt("parent_promo_id");
					String desc = rs.getString("descr");
					boolean isFirst = (rs.getInt("is_first") == 1);
					DBFunPointConsumeRecord record = new DBFunPointConsumeRecord(playerId, roleId, funType, consumePoint, consumeTime, promoId, parentPromoId, desc, isFirst);
					recordList.add(record);
				}
			}

		} catch (SQLException ex) {
			_LOGGER.error("GM工具方法queryDBFunPointConsumeRecord()查询功能消费流水失败，数据库发生未知异常", ex);
			// throw new KGameDBException("数据库发生未知异常", ex,
			// KGameDBException.CAUSE_DB_SHUTDOWN_WITH_UNKNOW_REASON,
			// "注册账号失败，数据库发生未知异常");
		} finally {
			gsPool.closeResultSet(rs);
			gsPool.closePreparedStatement(ps);
			gsPool.closeConnection(con);
		}

		return recordList;
	}

	public List<DBShopSellItemRecord> queryDBShopSellItemRecord(long roleId, long startTime, long endTime) {
		List<DBShopSellItemRecord> recordList = new ArrayList<DBShopSellItemRecord>();
		String sql = "select * from shop_point_consume_record where role_id = ? and consume_time>=? and consume_time<=?";
		Connection con = gsPool.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ps.setLong(1, roleId);
			ps.setTimestamp(2, new Timestamp(startTime));
			ps.setTimestamp(3, new Timestamp(endTime));

			rs = gsPool.executeQuery(ps);
			if (rs != null && rs.next()) {
				rs.beforeFirst();
				while (rs.next()) {
					long playerId = rs.getLong("player_id");
					long role_id = rs.getLong("role_id");
					long itemId = rs.getLong("item_id");
					String itemCode = rs.getString("code");
					int consumePoint = rs.getInt("consume_point");
					int count = rs.getInt("count");
					int shopType = rs.getInt("shop_type");
					long consumeTime = rs.getTimestamp("consume_time") != null ? rs.getTimestamp("consume_time").getTime() : 0;
					int promoId = rs.getInt("promo_id");
					int parentPromoId = rs.getInt("parent_promo_id");
					String desc = rs.getString("descr");

					DBShopSellItemRecord record = new DBShopSellItemRecord(playerId, roleId, itemId, itemCode, count, consumePoint, consumeTime, promoId, parentPromoId, desc, shopType);
					recordList.add(record);
				}
			}

		} catch (SQLException ex) {
			_LOGGER.error("GM工具方法queryDBShopSellItemRecord()查询功能消费流水失败，数据库发生未知异常", ex);
			// throw new KGameDBException("数据库发生未知异常", ex,
			// KGameDBException.CAUSE_DB_SHUTDOWN_WITH_UNKNOW_REASON,
			// "注册账号失败，数据库发生未知异常");
		} finally {
			gsPool.closeResultSet(rs);
			gsPool.closePreparedStatement(ps);
			gsPool.closeConnection(con);
		}

		return recordList;
	}

	public List<DBFunPointTotalConsume> caculateDBFunPointTotalConsumeByAllTypes(long roleId) {
		List<DBFunPointTotalConsume> recordList = new ArrayList<DBFunPointTotalConsume>();
		String sql = "select * from fun_point_consume_info where role_id = ?";
		Connection con = gsPool.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ps.setLong(1, roleId);

			rs = gsPool.executeQuery(ps);
			if (rs != null && rs.next()) {
				rs.beforeFirst();
				while (rs.next()) {
					long role_id = rs.getLong("role_id");
					int consumePoint = rs.getInt("total_consume_point");
					int funType = rs.getInt("fun_type");
					String desc = rs.getString("descr");

					DBFunPointTotalConsume record = new DBFunPointTotalConsume(roleId, funType, consumePoint, desc);
					recordList.add(record);
				}
			}

		} catch (SQLException ex) {
			_LOGGER.error("GM工具方法caculateDBFunPointTotalConsumeByAllTypes()查询功能消费流水失败，数据库发生未知异常", ex);
			// throw new KGameDBException("数据库发生未知异常", ex,
			// KGameDBException.CAUSE_DB_SHUTDOWN_WITH_UNKNOW_REASON,
			// "注册账号失败，数据库发生未知异常");
		} finally {
			gsPool.closeResultSet(rs);
			gsPool.closePreparedStatement(ps);
			gsPool.closeConnection(con);
		}
		return recordList;
	}

	public DBFunPointTotalConsume caculateDBFunPointTotalConsumeByType(long roleId, int funType) {
		
		String sql = "select * from fun_point_consume_info where role_id = ? and fun_type = ?";
		Connection con = gsPool.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {			
			ps = con.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ps.setLong(1, roleId);
			ps.setInt(2, funType);

			rs = gsPool.executeQuery(ps);
			if (rs != null && rs.next()) {
				long role_id = rs.getLong("role_id");
				int consumePoint = rs.getInt("total_consume_point");
				int fun_type = rs.getInt("fun_type");
				String desc = rs.getString("descr");

				DBFunPointTotalConsume record = new DBFunPointTotalConsume(roleId, fun_type, consumePoint, desc);
				return record;
			} else {
				return null;
			}
		} catch (SQLException ex) {
			_LOGGER.error("GM工具方法caculateDBFunPointTotalConsumeByAllTypes()查询功能消费流水失败，数据库发生未知异常", ex);
			return null;
			// throw new KGameDBException("数据库发生未知异常", ex,
			// KGameDBException.CAUSE_DB_SHUTDOWN_WITH_UNKNOW_REASON,
			// "注册账号失败，数据库发生未知异常");
		} finally {
			gsPool.closeResultSet(rs);
			gsPool.closePreparedStatement(ps);
			gsPool.closeConnection(con);
		}
	}
	
	public boolean checkHasDBFunPointTotalConsumeTable(){
		String dbName = gsPool.getDBName();
		String selectSql = "SELECT table_name FROM information_schema.TABLES WHERE TABLE_SCHEMA=? and table_name ='fun_point_consume_info'";
		
		Connection con = gsPool.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(selectSql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ps.setString(1, dbName);
			rs = gsPool.executeQuery(ps);
			if (rs != null && rs.next()) {
				return true;
			}
		} catch (SQLException ex) {
			_LOGGER.error("GM工具方法caculateDBFunPointTotalConsumeByAllTypes()查询功能消费流水失败，数据库发生未知异常", ex);			
			// throw new KGameDBException("数据库发生未知异常", ex,
			// KGameDBException.CAUSE_DB_SHUTDOWN_WITH_UNKNOW_REASON,
			// "注册账号失败，数据库发生未知异常");
		} finally {
			gsPool.closeResultSet(rs);
			gsPool.closePreparedStatement(ps);
			gsPool.closeConnection(con);
		}
		return false;
	}

	public static class DBFunPointTotalConsume {
		/** 角色ID */
		public long roleId;
		/** 功能使用类型 */
		public int funType;
		/** 消费点数 */
		public int consumePoint;
		/** 功能描述 */
		public String desc;

		public DBFunPointTotalConsume(long roleId, int funType, int consumePoint, String desc) {
			super();
			this.roleId = roleId;
			this.funType = funType;
			this.consumePoint = consumePoint;
			this.desc = desc;
		}

	}

	public static void main(String[] a) {
		try {
			DBConnectionPoolAdapter.initPlatformDbPool();
			DBConnectionPoolAdapter.initLogicDbPool(1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// List<DBChargeRecord> list =
		// DBQueryManager.getInstance().queryDBChargeRecord(10001, new
		// Date().getTime()-3600000, new Date().getTime());
		// for (DBChargeRecord record:list) {
		// _LOGGER.error("充值流水：角色ID：{},角色名:{}，充值金额：{}，充值时间：{}",record.role_id,record.role_name,record.rmb,record.charge_time);
		// }

		// List<DBFunPointConsumeRecord> list1 =
		// DBQueryManager.getInstance().queryDBFunPointConsumeRecord(10, new
		// Date().getTime()-3600000000l, new Date().getTime());
		// for (DBFunPointConsumeRecord record:list1) {
		// _LOGGER.error("功能消费流水：角色ID：{},功能名:{}，消费点数：{}，充值时间：{}",record.roleId,record.desc,record.consumePoint,record.consumeTime);
		// }

		// List<DBShopSellItemRecord> list2 =
		// DBQueryManager.getInstance().queryDBShopSellItemRecord(10, new
		// Date().getTime()-3600000000l, new Date().getTime());
		// for (DBShopSellItemRecord record:list2) {
		// _LOGGER.error("道具消费流水：角色ID：{},道具名:{}，消费点数：{}，充值时间：{}",record.roleId,record.desc,record.consumePoint,record.consumeTime);
		// }

		// List<DBPresentPointRecord> list3 =
		// DBQueryManager.getInstance().queryDBPresentPointRecord(20, new
		// Date().getTime()-3600000000l, new Date().getTime());
		// for (DBPresentPointRecord record:list3) {
		// _LOGGER.error("赠送点数流水：角色ID：{},赠送功能名:{}，赠送点数：{}，赠送时间：{}",record.role_id,record.desc,record.present_point,record.present_time);
		// }
	}

}
