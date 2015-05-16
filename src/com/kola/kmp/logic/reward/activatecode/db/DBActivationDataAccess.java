package com.kola.kmp.logic.reward.activatecode.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.koala.game.KGame;
import com.koala.game.dataaccess.dbconnectionpool.DBConnectionPoolAdapter;
import com.koala.game.dataaccess.dbconnectionpool.mysql.DefineDataSourceManagerIF;
import com.koala.game.logging.KGameLogger;

/**
 * <pre>
 * 访问物理数据库
 * 
 * @author CamusHuang
 * @creation 2013-6-7 下午8:26:38
 * </pre>
 */
public class DBActivationDataAccess {
	private static final KGameLogger _LOGGER = KGameLogger.getLogger(DBActivationDataAccess.class);

	private static DBActivationDataAccess instance;

	public static DBActivationDataAccess getInstance() {
		if (instance == null) {
			instance = new DBActivationDataAccess();
		}
		return instance;
	}

	private int serverId;
	private DefineDataSourceManagerIF platformPool;

	public DBActivationDataAccess() {
		platformPool = DBConnectionPoolAdapter.getPlatformDBConnectionPool();
		serverId = KGame.getGSID();
	}

	/**
	 * <pre>
	 * 获取激活码数据
	 * 
	 * @param activationCode
	 * @return
	 * @author CamusHuang
	 * @creation 2013-6-1 下午4:49:44
	 * </pre>
	 */
	public DBActivation getActivation(String activationCode) {

		String sql = "select * from activation_code where code = ?";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		DBActivation data = null;
		try {
			con = platformPool.getConnection();
			ps = con.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ps.setString(1, activationCode.toLowerCase());

			rs = platformPool.executeQuery(ps);
			if (rs != null && rs.next()) {
				int type = rs.getInt("type");
				long effectEndTime = (rs.getTimestamp("effect_time") != null ? rs.getTimestamp("effect_time").getTime() : 0);
				int useGSId = rs.getInt("gsid");
				long playerId = rs.getLong("player_id");
				long useRoleId = rs.getLong("role_id");
				String useRoleName = rs.getString("role_name");
				long useTime = (rs.getTimestamp("use_time") != null ? rs.getTimestamp("use_time").getTime() : 0);
				data = new DBActivation(activationCode, type, effectEndTime, useGSId, playerId, useRoleId, useRoleName, useTime);

			}

		} catch (SQLException ex) {
			_LOGGER.error("方法getActivation()获取激活码失败，数据库发生未知异常", ex);
			// throw new KGameDBException("数据库发生未知异常", ex,
			// KGameDBException.CAUSE_DB_SHUTDOWN_WITH_UNKNOW_REASON,
			// "注册账号失败，数据库发生未知异常");
		} finally {
			platformPool.closeResultSet(rs);
			platformPool.closePreparedStatement(ps);
			platformPool.closeConnection(con);
		}

		return data;
	}

	/**
	 * <pre>
	 * 检查玩家是否已领取过指定类型的激活礼包
	 * 
	 * @param roleId 角色ID
	 * @param type 激活礼包类型
	 * @return
	 * @author CamusHuang
	 * @creation 2013-6-1 下午4:52:10
	 * </pre>
	 */
	public boolean isPlayerReceivedActivation(long roleId, int type) {
		String sql = "select * from activation_code where role_id = ? and type = ? and gsid = ?";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		DBActivation data = null;
		try {
			con = platformPool.getConnection();
			
			ps = con.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ps.setLong(1, roleId);
			ps.setInt(2, type);
			ps.setInt(3, serverId);

			rs = platformPool.executeQuery(ps);
			if (rs != null && rs.next()) {
				// int t= rs.getInt("player_id");
				// long r = rs.getLong("role_id");
				// int g = rs.getInt("gsid");
				// System.out.println("g::::"+g+"::"+t+"::"+r);
				// if (rs.getInt("player_id") == 0 && rs.getLong("role_id") == 0
				// && rs.getInt("gsid") == 0) {
				// return false;
				// }
				return true;
			} else {
				return false;
			}

		} catch (SQLException ex) {
			_LOGGER.error("方法isPlayerReceivedActivation()检查玩家是否已领取过指定类型的激活礼包失败，数据库发生未知异常", ex);
			// throw new KGameDBException("数据库发生未知异常", ex,
			// KGameDBException.CAUSE_DB_SHUTDOWN_WITH_UNKNOW_REASON,
			// "注册账号失败，数据库发生未知异常");
		} finally {
			platformPool.closeResultSet(rs);
			platformPool.closePreparedStatement(ps);
			platformPool.closeConnection(con);
		}

		return true;

	}

	/**
	 * <pre>
	 * 记录玩家已领取指定类型的激活礼包
	 * 
	 * @param activationCode
	 * @return
	 * @author CamusHuang
	 * @creation 2013-6-1 下午4:49:44
	 * </pre>
	 */
	public void recordPlayerReceiveActivation(String activationCode, long playerId, long roleId, String roleName) {
		String sql = "update activation_code set gsid=?,player_id=?,role_id=?,role_name=?,use_time=? where code=?";
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = platformPool.getConnection();

			ps = con.prepareStatement(sql);

			ps.setInt(1, serverId);
			ps.setLong(2, playerId);
			ps.setLong(3, roleId);
			ps.setString(4, roleName);
			ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
			ps.setString(6, activationCode);

			platformPool.executeUpdate(ps);
		} catch (SQLException ex) {
			_LOGGER.error("方法recordPlayerReceiveActivation()记录玩家已领取指定类型的激活礼包失败，数据库发生未知异常", ex);
		} finally {
			platformPool.closePreparedStatement(ps);
			platformPool.closeConnection(con);
		}

	}

	public static void main(String[] a) {
		try {
			DBConnectionPoolAdapter.initPlatformDbPool();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DBActivationDataAccess da = new DBActivationDataAccess();
		DBActivation data = da.getActivation("4520546658");
		System.out.println("data::::" + data.activationCode + "::" + data.effectEndTime + "::" + data.type + "::" + data.playerId
				+ "::" + data.useRoleId);

		boolean isPlayerReceivedActivation = da.isPlayerReceivedActivation(1001, 1);
		System.out.println("isPlayerReceivedActivation:::" + isPlayerReceivedActivation);
		//
		da.recordPlayerReceiveActivation("4520546658", 10001, 1001, "AAA");

		isPlayerReceivedActivation = da.isPlayerReceivedActivation(1001, 1);
		System.out.println("isPlayerReceivedActivation:::" + isPlayerReceivedActivation);
	}

}
