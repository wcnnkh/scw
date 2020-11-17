package scw.zookeeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import scw.core.utils.CollectionUtils;
import scw.core.utils.StringUtils;

public final class ZooKeeperUtils {
	public static final String PATH_PREFIX = "/";

	public static String cleanPath(String... paths) {
		if (paths == null || paths.length == 0) {
			return "/";
		}

		String firstPath = paths[0];
		firstPath = firstPath == null ? PATH_PREFIX
				: ((firstPath.startsWith(PATH_PREFIX) ? "" : PATH_PREFIX) + firstPath);
		if (paths.length == 1) {
			return firstPath;
		}

		if (!firstPath.endsWith(PATH_PREFIX)) {
			firstPath = firstPath + PATH_PREFIX;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(firstPath);
		for (int i = 1; i < paths.length; i++) {
			String path = paths[i];
			if (StringUtils.isEmpty(path)) {
				continue;
			}

			if (i == paths.length) {
				// 最后一个
				if (path.endsWith(PATH_PREFIX)) {
					path = path.substring(0, path.length() - 2);
				}
			} else {
				if (!path.endsWith(PATH_PREFIX)) {
					path = path + PATH_PREFIX;
				}
			}

			sb.append(path);
		}

		firstPath = sb.toString();
		if (!firstPath.equals(PATH_PREFIX) && firstPath.endsWith(PATH_PREFIX)) {
			firstPath = firstPath.substring(0, firstPath.length() - 1);
		}
		return firstPath;
	}

	public static boolean isExist(ZooKeeper zooKeeper, String path) {
		try {
			return zooKeeper.exists(cleanPath(path), false) != null;
		} catch (KeeperException e) {
			throw new ZooKeeperException(path, e);
		} catch (InterruptedException e) {
			throw new ZooKeeperException(path, e);
		}
	}

	public static List<String> getChildren(ZooKeeper zooKeeper, String path) {
		String pathToUse = cleanPath(path);
		if (!isExist(zooKeeper, pathToUse)) {
			return Collections.emptyList();
		}

		List<String> paths;
		try {
			paths = zooKeeper.getChildren(pathToUse, false);
		} catch (KeeperException e) {
			throw new ZooKeeperException(path, e);
		} catch (InterruptedException e) {
			throw new ZooKeeperException(path, e);
		}

		if (CollectionUtils.isEmpty(paths)) {
			return Collections.emptyList();
		}

		List<String> list = new ArrayList<String>();
		for (String item : paths) {
			list.add(cleanPath(pathToUse, item));
		}
		return list;
	}

	public static List<ZooKeeperTreePath> getChildrenTreePaths(ZooKeeper zooKeeper, String parent) {
		String pathToUse = cleanPath(parent);
		List<String> childrens = getChildren(zooKeeper, pathToUse);
		if (CollectionUtils.isEmpty(childrens)) {
			return Collections.emptyList();
		}

		List<ZooKeeperTreePath> paths = new ArrayList<ZooKeeperTreePath>();
		for (String path : childrens) {
			ZooKeeperTreePath zooKeeperTreePath = new ZooKeeperTreePath(path);
			zooKeeperTreePath.setChildrens(getChildrenTreePaths(zooKeeper, path));
			paths.add(zooKeeperTreePath);
		}
		return paths;
	}

	public static byte[] getData(ZooKeeper zooKeeper, String path) {
		Stat stat;
		String pathToUse = cleanPath(path);
		try {
			stat = zooKeeper.exists(pathToUse, false);
			if (stat == null) {
				return null;
			}

			return zooKeeper.getData(pathToUse, false, stat);
		} catch (KeeperException e) {
			throw new ZooKeeperException(path, e);
		} catch (InterruptedException e) {
			throw new ZooKeeperException(path, e);
		}
	}

	/**
	 * 使用cas的特性插入数据，如果节点不存在就失败
	 * 
	 * @param zooKeeper
	 * @param path
	 * @param data
	 * @return
	 */
	public static boolean setData(ZooKeeper zooKeeper, String path, byte[] data) {
		try {
			Stat stat = zooKeeper.exists(path, true);
			if (stat == null) {
				return false;
			}

			while ((stat = zooKeeper.setData(path, data, stat.getVersion())) == null) {
				stat = zooKeeper.exists(path, true);
				if (stat == null) {
					return false;
				}
			}
			return true;
		} catch (KeeperException e) {
			throw new ZooKeeperException(path, e);
		} catch (InterruptedException e) {
			throw new ZooKeeperException(path, e);
		}
	}
	
	public static boolean delete(ZooKeeper zooKeeper, String path, int version){
		Stat stat;
		try {
			stat = zooKeeper.exists(path, true);
			if(stat == null){
				return false;
			}
			
			zooKeeper.delete(path, version);
			return true;
		} catch (KeeperException e) {
			throw new ZooKeeperException(path, e);
		} catch (InterruptedException e) {
			throw new ZooKeeperException(path, e);
		}
	}

	/**
	 * 创建节点(如果父节点不存在，会递归的创建父节点)，如果节点已经存在就返回false
	 * 
	 * @param zooKeeper
	 * @param path
	 * @param acl
	 * @param createMode
	 * @return
	 */
	public static boolean createNotExist(ZooKeeper zooKeeper, String path, List<ACL> acl, CreateMode createMode) {
		Stat stat;
		try {
			stat = zooKeeper.exists(path, true);
			if (stat != null) {
				return false;
			}

			StringBuilder sb = new StringBuilder();
			String[] paths = cleanPath(path).split(PATH_PREFIX);
			for (int i = 0; i < paths.length; i++) {
				String value = paths[i];
				if (StringUtils.isEmpty(value)) {
					continue;
				}

				sb.append(PATH_PREFIX).append(value);
				String tempPath = sb.toString();
				if (zooKeeper.exists(tempPath, false) != null) {
					continue;
				}

				zooKeeper.create(tempPath, null, acl, createMode);
			}
			return true;
		} catch (KeeperException e) {
			throw new ZooKeeperException(path, e);
		} catch (InterruptedException e) {
			throw new ZooKeeperException(path, e);
		}
	}
}
