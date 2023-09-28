package com.mirrorfly.gateway.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mirrorfly.gateway.model.ConnectionConfig;
import com.mirrorfly.gateway.util.DataSourceUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InstantRefresh {

	@Autowired
	private DataSourceUtil dataSourceUtil;

	@Autowired
	private DataStore dataStore;

	public Map<String, DataSource> refresh(DataSource defaultDS, Set<String> keys) {
		Map<String, DataSource> dataSources = new HashMap<>();
		List<ConnectionConfig> connectionConfigs = getConnectionConfigs(defaultDS);

		dataStore.setConnectionConfig(connectionConfigs);

		for (ConnectionConfig connectionConfig : connectionConfigs) {
			if (!keys.contains(connectionConfig.getApiDomain())) {
				dataSources.put(connectionConfig.getApiDomain(), dataSourceUtil.createDataSource(connectionConfig));
			}
		}
		return dataSources;
	}

	private List<ConnectionConfig> getConnectionConfigs(DataSource defaultDS) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ConnectionConfig> list = new ArrayList<>();
		try {
			connection = defaultDS.getConnection();
			String sql = "SELECT * FROM connection_config";
			ps = connection.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				ConnectionConfig config = new ConnectionConfig();
				config.setId(rs.getInt("id"));
				config.setMysql(rs.getString("mysql"));
				config.setApiDomain(rs.getString("api_domain"));
				config.setLicenseKey(rs.getString("license_key"));
				config.setStorage(rs.getString("storage"));
				config.setXmppDomain(rs.getString("xmpp_domain"));
				config.setSignalDomain(rs.getString("signal_domain"));
				config.setAwsDetails(rs.getString("aws_details"));
				config.setOciDetails(rs.getString("oci_details"));
				config.setXmppConfig(rs.getString("xmpp_config"));
				config.setAndroidPushDetails(rs.getString("android_push_details"));
				config.setIosPushDetails(rs.getString("ios_push_details"));
				config.setUserMaxSessions(rs.getInt("user_max_session"));
				list.add(config);
			}
		} catch (SQLException e) {
			log.error("SQLException occured while fetching configured connection details from connection_config ::: "
					+ e);
		} finally {
			releaseResources(connection, ps, rs);
		}
		return list;
	}

	private void releaseResources(Connection con, PreparedStatement ps, ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			log.error("SQLException occured while closing ResultSet ::: Supressing this exception ::: " + e);
		}

		try {
			if (ps != null) {
				ps.close();
			}
		} catch (SQLException e) {
			log.error("SQLException occured while closing PreparedStatement ::: Supressing this exception ::: " + e);
		}

		try {
			if (con != null && !con.isClosed()) {
				con.close();
			}
		} catch (SQLException e) {
			log.error("SQLException occured while closing Connection ::: Supressing this exception ::: " + e);
		}
	}

}
