package scw.zookeeper;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig.ConfigException;

import scw.core.Assert;
import scw.core.GlobalPropertyFactory;
import scw.json.JSONUtils;
import scw.logger.Logger;
import scw.logger.LoggerUtils;

public class ZooKeeperServerStart extends Thread {
	private Logger logger = LoggerUtils.getLogger(ZooKeeperServerStart.class);
	private static final String DATA_DIR = "dataDir";
	private static final String DATA_LOG_DIR = "dataLogDir";
	private static final String CLIENT_PORT = "clientPort";

	private final ServerConfig serverConfig;

	public ZooKeeperServerStart(int port) throws IOException, ConfigException {
		Properties properties = new Properties();
		properties.put(CLIENT_PORT, port);
		this.serverConfig = parse(properties);
	}

	public ZooKeeperServerStart(Properties properties) throws IOException,
			ConfigException {
		this.serverConfig = parse(properties);
	}

	protected ServerConfig parse(Properties properties) throws IOException,
			ConfigException {
		if (!properties.containsKey(DATA_DIR)) {
			properties.setProperty(DATA_DIR, GlobalPropertyFactory
					.getInstance().getWorkPath() + File.separator + "zk_data");
		}

		if (!properties.containsKey(DATA_LOG_DIR)) {
			properties.setProperty(DATA_LOG_DIR, GlobalPropertyFactory
					.getInstance().getWorkPath() + File.separator + "zk_logs");
		}

		if (!properties.containsKey(CLIENT_PORT)) {
			properties.setProperty(CLIENT_PORT, "2181");
		}

		QuorumPeerConfig quorumPeerConfig = new QuorumPeerConfig();
		quorumPeerConfig.parseProperties(properties);
		ServerConfig serverConfig = new ServerConfig();
		serverConfig.readFrom(quorumPeerConfig);
		return serverConfig;
	}

	public ZooKeeperServerStart(ServerConfig serverConfig) {
		Assert.requiredArgument(serverConfig != null, "serverConfig");
		this.serverConfig = serverConfig;
	}

	@Override
	public void run() {
		ZooKeeperServerMain zooKeeperServerMain = new ZooKeeperServerMain();
		logger.info("start zookeeper server: {}",
				JSONUtils.toJSONString(serverConfig));
		try {
			zooKeeperServerMain.runFromConfig(serverConfig);
		} catch (Exception e) {
			logger.error(e, "start zookeeper server error: {}",
					JSONUtils.toJSONString(serverConfig));
		}
		super.run();
	}
}
