package xiaoxiong.MyCloud.server.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import xiaoxiong.MyCloud.server.enumeration.AccountAuth;
import xiaoxiong.MyCloud.server.enumeration.LogLevel;
import xiaoxiong.MyCloud.server.enumeration.VCLevel;
import xiaoxiong.MyCloud.server.model.Folder;
import xiaoxiong.MyCloud.server.pojo.ExtendStores;

public class ConfigureReader {
	private static ConfigureReader cr;// 自体实体
	private Properties serverp;// 配置设置
	private Properties accountp;// 账户设置
	private int propertiesStatus;// 当前配置检查结果
	private String path;
	private String fileSystemPath; //文件系统路径
	private String confdir;
	private String mustLogin;
	private int port;
	private String log;
	private String vc;
	private String FSPath;
	private List<ExtendStores> extendStores;//扩展存储区路径
	private int bufferSize;
	private String fileBlockPath; //文件块存放区
	private String fileNodePath; //文件节点存放区
	private String TFPath; //临时文件存放区
	private String dbURL;
	private String dbDriver;
	private String dbUser;
	private String dbPwd;
	private final String ACCOUNT_PROPERTIES_FILE = "account.properties";
	private final String SERVER_PROPERTIES_FILE = "server.properties";
	private final int DEFAULT_BUFFER_SIZE = 1048576;
	private final int DEFAULT_PORT = 8080;
	private final String DEFAULT_LOG_LEVEL = "E";
	private final String DEFAULT_VC_LEVEL = "STANDARD";
	private final String DEFAULT_MUST_LOGIN = "O";
	private final String DEFAULT_FILE_SYSTEM_PATH;
	private final String DEFAULT_FILE_SYSTEM_PATH_SETTING = "DEFAULT";
	private final String DEFAULT_ACCOUNT_ID = "admin";
	private final String DEFAULT_ACCOUNT_PWD = "000000";
	private final String DEFAULT_ACCOUNT_AUTH = "cudrm";
	private final String DEFAULT_AUTH_OVERALL = "l";
	public static final int INVALID_PORT = 1;
	public static final int INVALID_LOG = 2;
	public static final int INVALID_FILE_SYSTEM_PATH = 3;
	public static final int INVALID_BUFFER_SIZE = 4;
	public static final int CANT_CREATE_FILE_BLOCK_PATH = 5;
	public static final int CANT_CREATE_FILE_NODE_PATH = 6;
	public static final int CANT_CREATE_TF_PATH = 7;
	public static final int CANT_CONNECT_DB = 8;
	public static final int HTTPS_SETTING_ERROR = 9;
	public static final int INVALID_VC = 10;
	public static final int LEGAL_PROPERTIES = 0;
	private static Thread accountPropertiesUpdateDaemonThread;
	private String timeZone;
	private boolean openHttps;
	private String httpsKeyFile;
	private String httpsKeyType;
	private String httpsKeyPass;
	private int httpsPort;
	
	private ConfigureReader() {
		this.propertiesStatus = -1;
		this.path = System.getProperty("user.dir");// 开发环境下,E:\yingzi\opensource\kiftd-source-master\kiftd-source-master
		String classPath = System.getProperty("java.class.path");
		if (classPath.indexOf(File.pathSeparator) < 0) {
			File f = new File(classPath);
			classPath = f.getAbsolutePath();
			if (classPath.endsWith(".jar")) {
				this.path = classPath.substring(0, classPath.lastIndexOf(File.separator));// 使用环境下
			}
		}
		this.DEFAULT_FILE_SYSTEM_PATH = this.path + File.separator + "filesystem" + File.separator;
		this.confdir = this.path + File.separator + "conf" + File.separator;
		this.serverp = new Properties();
		this.accountp = new Properties();
		extendStores = new ArrayList<>();
		final File serverProp = new File(this.confdir + SERVER_PROPERTIES_FILE);
		if (!serverProp.isFile()) {
			this.createDefaultServerPropertiesFile();
		}
		final File accountProp = new File(this.confdir + ACCOUNT_PROPERTIES_FILE);
		if (!accountProp.isFile()) {
			this.createDefaultAccountPropertiesFile();
		}
		try {
			print("正在载入配置文件...");
			final FileInputStream serverPropIn = new FileInputStream(serverProp);
			this.serverp.load(serverPropIn);
			final FileInputStream accountPropIn = new FileInputStream(accountProp);
			this.accountp.load(accountPropIn);
			print("配置文件载入完毕。正在检查配置...");
			this.propertiesStatus = this.testServerPropertiesAndEffect();
			if (this.propertiesStatus == LEGAL_PROPERTIES) {
				print("准备就绪。");
				//startAccountRealTimeUpdateListener();
			}
		} catch (Exception e) {
			print("错误：无法加载一个或多个配置文件（位于" + this.confdir + "路径下），请尝试删除旧的配置文件并重新启动本应用或查看安装路径的权限（必须可读写）。");
		}
	}

	public static ConfigureReader instance() {
		if (ConfigureReader.cr == null) {
			ConfigureReader.cr = new ConfigureReader();
		}
		return ConfigureReader.cr;
	}
	
	public void createDefaultServerPropertiesFile() {
		print("正在生成初始服务器配置文件（" + this.confdir + SERVER_PROPERTIES_FILE + "）...");
		final Properties dsp = new Properties();
		dsp.setProperty("mustLogin", DEFAULT_MUST_LOGIN);
		dsp.setProperty("port", DEFAULT_PORT + "");
		dsp.setProperty("log", DEFAULT_LOG_LEVEL);
		dsp.setProperty("VC.level", DEFAULT_VC_LEVEL);
		dsp.setProperty("FS.path", DEFAULT_FILE_SYSTEM_PATH_SETTING);
		dsp.setProperty("buff.size", DEFAULT_BUFFER_SIZE + "");
		try {
			dsp.store(new FileOutputStream(this.confdir + SERVER_PROPERTIES_FILE),
					"<This is the default kiftd server setting file. >");
			print("初始服务器配置文件生成完毕。");
		} catch (FileNotFoundException e) {
			print("错误：无法生成初始服务器配置文件，存储路径不存在。");
		} catch (IOException e2) {
			print("错误：无法生成初始服务器配置文件，写入失败。");
		}
	}
	
	private void createDefaultAccountPropertiesFile() {
		print("正在生成初始账户配置文件（" + this.confdir + ACCOUNT_PROPERTIES_FILE + "）...");
		final Properties dap = new Properties();
		dap.setProperty(DEFAULT_ACCOUNT_ID + ".pwd", DEFAULT_ACCOUNT_PWD);
		dap.setProperty(DEFAULT_ACCOUNT_ID + ".auth", DEFAULT_ACCOUNT_AUTH);
		dap.setProperty("authOverall", DEFAULT_AUTH_OVERALL);
		try {
			dap.store(new FileOutputStream(this.confdir + ACCOUNT_PROPERTIES_FILE),
					"<This is the default kiftd account setting file. >");
			print("初始账户配置文件生成完毕。");
		} catch (FileNotFoundException e) {
			print("错误：无法生成初始账户配置文件，存储路径不存在。");
		} catch (IOException e2) {
			print("错误：无法生成初始账户配置文件，写入失败。");
		}
	}
	
	public void print(String message)
	{
		System.out.println(message);
	}
	
	private int testServerPropertiesAndEffect() {
		print("正在检查服务器配置...");
		this.mustLogin = this.serverp.getProperty("mustLogin");
		if (this.mustLogin == null) {
			print("警告：未找到是否必须登录配置，将采用默认值（O）。");
			this.mustLogin = "O";
		}
		final String ports = this.serverp.getProperty("port");
		if (ports == null) {
			print("警告：未找到端口配置，将采用默认值（8080）。");
			this.port = 8080;
		} else {
			try {
				this.port = Integer.parseInt(ports);
				if (this.port <= 0 || this.port > 65535) {
					print("错误：端口号配置不正确，必须使用1-65535之间的整数。");
					return 1;
				}
			} catch (Exception e) {
				print("错误：端口号配置不正确，必须使用1-65535之间的整数。");
				return 1;
			}
		}
		final String logs = this.serverp.getProperty("log");
		if (logs == null) {
			print("警告：未找到日志等级配置，将采用默认值（E）。");
			this.log = "E";
		} else {
			if (!logs.equals("N") && !logs.equals("R") && !logs.equals("E")) {
				return 2;
			}
			this.log = logs;
		}
		final String vcl = this.serverp.getProperty("VC.level");
		if (vcl == null) {
			print("警告：未找到登录验证码配置，将采用默认值（STANDARD）。");
			this.vc = DEFAULT_VC_LEVEL;
		} else {
			switch (vcl) {
			case "STANDARD":
			case "SIMP":
			case "CLOSE":
				this.vc = vcl;
				break;
			default:
				return INVALID_VC;
			}
		}
		final String bufferSizes = this.serverp.getProperty("buff.size");
		if (bufferSizes == null) {
			print("警告：未找到缓冲大小配置，将采用默认值（1048576）。");
			this.bufferSize = 1048576;
		} else {
			try {
				this.bufferSize = Integer.parseInt(bufferSizes);
				if (this.bufferSize <= 0) {
					print("错误：缓冲区大小设置无效。");
					return 4;
				}
			} catch (Exception e2) {
				print("错误：缓冲区大小设置无效。");
				return 4;
			}
		}
		this.FSPath = this.serverp.getProperty("FS.path");
		if (this.FSPath == null) {
			print("警告：未找到文件系统配置，将采用默认值。");
			this.fileSystemPath = this.DEFAULT_FILE_SYSTEM_PATH; //this.DEFAULT_FILE_SYSTEM_PATH初始化时有默认路径
		} else if (this.FSPath.equals("DEFAULT")) {
			this.fileSystemPath = this.DEFAULT_FILE_SYSTEM_PATH;
		} else {
			this.fileSystemPath = this.FSPath;
		}
		if (!fileSystemPath.endsWith(File.separator)) {
			fileSystemPath = fileSystemPath + File.separator;
		}
		extendStores.clear();
		for (short i = 1; i < 32; i++) {
			if (serverp.getProperty("FS.extend." + i) != null) {
				ExtendStores es = new ExtendStores();
				es.setPath(new File(serverp.getProperty("FS.extend." + i)));
				es.setIndex(i);
				extendStores.add(es);
			}
		}
		final File fsFile = new File(this.fileSystemPath);
		if (!fsFile.isDirectory() || !fsFile.canRead() || !fsFile.canWrite()) {
			print("错误：文件系统路径[" + this.fileSystemPath + "]无效，该路径必须指向一个具备读写权限的文件夹。");
			return 3;
		}
		for (ExtendStores es : extendStores) {
			if (!es.getPath().isDirectory() || !es.getPath().canRead() || !es.getPath().canWrite()) {
				print("错误：扩展存储区路径[" + es.getPath().getAbsolutePath() + "]无效，该路径必须指向一个具备读写权限的文件夹。");
				return 3;
			}
		}
		for (int i = 0; i < extendStores.size() - 1; i++) {
			for (int j = i + 1; j < extendStores.size(); j++) {
				if (extendStores.get(i).getPath().equals(extendStores.get(j).getPath())) {
					print(
							"错误：扩展存储区路径[" + extendStores.get(j).getPath().getAbsolutePath() + "]无效，该路径已被其他扩展存储区占用。");
					return 3;
				}
			}
		}
		this.fileBlockPath = this.fileSystemPath + "fileblocks" + File.separator;
		final File fbFile = new File(this.fileBlockPath);
		if (!fbFile.isDirectory() && !fbFile.mkdirs()) {
			print("错误：无法创建文件块存放区[" + this.fileBlockPath + "]。");
			return 5;
		}
		this.fileNodePath = this.fileSystemPath + "filenodes" + File.separator;
		final File fnFile = new File(this.fileNodePath);
		if (!fnFile.isDirectory() && !fnFile.mkdirs()) {
			print("错误：无法创建文件节点存放区[" + this.fileNodePath + "]。");
			return 6;
		}
		this.TFPath = this.fileSystemPath + "temporaryfiles" + File.separator;
		final File tfFile = new File(this.TFPath);
		if (!tfFile.isDirectory() && !tfFile.mkdirs()) {
			print("错误：无法创建临时文件存放区[" + this.TFPath + "]。");
			return 7;
		}

		if ("true".equals(serverp.getProperty("mysql.enable"))) {
			dbDriver = "com.mysql.cj.jdbc.Driver";
			String url = serverp.getProperty("mysql.url", "127.0.0.1/kift");
			if (url.indexOf("/") <= 0 || url.substring(url.indexOf("/")).length() == 1) {
				print("错误：自定义数据库的URL中必须指定数据库名称。");
				return 8;
			}
			dbURL = "jdbc:mysql://" + url + "?useUnicode=true&characterEncoding=utf8";
			dbUser = serverp.getProperty("mysql.user", "root");
			dbPwd = serverp.getProperty("mysql.password", "");
			timeZone = serverp.getProperty("mysql.timezone");
			if (timeZone != null) {
				dbURL = dbURL + "&serverTimezone=" + timeZone;
			}
			try {
				Class.forName(dbDriver).newInstance();
				Connection testConn = DriverManager.getConnection(dbURL, dbUser, dbPwd);
				testConn.close();
			} catch (Exception e) {
				print(
						"错误：无法连接至自定义数据库：" + dbURL + "（user=" + dbUser + ",password=" + dbPwd + "），请确重新配置MySQL数据库相关项。");
				return 8;
			}
		} else {
			dbDriver = "org.h2.Driver";
			dbURL = "jdbc:h2:file:" + fileNodePath + File.separator + "kift";
			dbUser = "root";
			dbPwd = "301537gY";
		}
		// https支持检查及生效处理
		if ("true".equals(serverp.getProperty("https.enable"))) {
			File keyFile = new File(path, "https.p12");
			if (keyFile.isFile()) {
				httpsKeyType = "PKCS12";
			} else {
				keyFile = new File(path, "https.jks");
				if (keyFile.isFile()) {
					httpsKeyType = "JKS";
				} else {
					print(
							"错误：无法启用https支持，因为kiftd未能找到https证书文件。您必须在应用主目录内放置PKCS12（必须命名为https.p12）或JKS（必须命名为https.jks）证书。");
					return HTTPS_SETTING_ERROR;
				}
			}
			httpsKeyFile = keyFile.getAbsolutePath();
			httpsKeyPass = serverp.getProperty("https.keypass", "");
			String httpsports = serverp.getProperty("https.port");
			if (httpsports == null) {
				print("警告：未找到https端口配置，将采用默认值（443）。");
				httpsPort = 443;
			} else {
				try {
					this.httpsPort = Integer.parseInt(httpsports);
					if (httpsPort <= 0 || httpsPort > 65535) {
						print("错误：无法启用https支持，https访问端口号配置不正确。");
						return HTTPS_SETTING_ERROR;
					}
				} catch (Exception e) {
					print("错误：无法启用https支持，https访问端口号配置不正确。");
					return HTTPS_SETTING_ERROR;
				}
			}
			openHttps = true;
		}
		print("检查完毕。");
		return 0;
	}
	
	public boolean accessFolder(Folder f, String account) {
		if (f == null) {
			return false;// 访问不存在的文件夹肯定是没权限
		}
		return true;
		/*int cl = f.getFolderConstraint();
		if (cl == 0) {
			return true;
		} else {
			if (account != null) {
				if (cl == 1) {
					if (f.getFolderCreator().equals(account)) {
						return true;
					}
					String vGroup = accountp.getProperty(account + ".group");
					String cGroup = accountp.getProperty(f.getFolderCreator() + ".group");
					if (vGroup != null && cGroup != null) {
						if ("*".equals(vGroup) || "*".equals(cGroup)) {
							return true;
						}
						String[] vgs = vGroup.split(";");
						String[] cgs = cGroup.split(";");
						for (String vs : vgs) {
							for (String cs : cgs) {
								if (vs.equals(cs)) {
									return true;
								}
							}
						}
					}
				}
				if (cl == 2) {
					if (f.getFolderCreator().equals(account)) {
						return true;
					}
				}
			}
			return false;
		}*/
	}
	
	public boolean authorized(final String account, final AccountAuth auth) {
		if (account != null && account.length() > 0) {
			final StringBuffer auths = new StringBuffer();
			final String accauth = this.accountp.getProperty(account + ".auth");
			final String overall = this.accountp.getProperty("authOverall");
			if (accauth != null) {
				auths.append(accauth);
			}
			if (overall != null) {
				auths.append(overall);
			}
			switch (auth) {
			case CREATE_NEW_FOLDER: {
				return auths.indexOf("c") >= 0;
			}
			case UPLOAD_FILES: {
				return auths.indexOf("u") >= 0;
			}
			case DELETE_FILE_OR_FOLDER: {
				return auths.indexOf("d") >= 0;
			}
			case RENAME_FILE_OR_FOLDER: {
				return auths.indexOf("r") >= 0;
			}
			case DOWNLOAD_FILES: {
				return auths.indexOf("l") >= 0;
			}
			case MOVE_FILES: {
				return auths.indexOf("m") >= 0;
			}
			default: {
				return false;
			}
			}
		} else {
			final String overall2 = this.accountp.getProperty("authOverall");
			if (overall2 == null) {
				return false;
			}
			switch (auth) {
			case CREATE_NEW_FOLDER: {
				return overall2.indexOf("c") >= 0;
			}
			case UPLOAD_FILES: {
				return overall2.indexOf("u") >= 0;
			}
			case DELETE_FILE_OR_FOLDER: {
				return overall2.indexOf("d") >= 0;
			}
			case RENAME_FILE_OR_FOLDER: {
				return overall2.indexOf("r") >= 0;
			}
			case DOWNLOAD_FILES: {
				return overall2.indexOf("l") >= 0;
			}
			case MOVE_FILES: {
				return overall2.indexOf("m") >= 0;
			}
			default: {
				return false;
			}
			}
		}
	}
	
	public String getPath() {
		return this.path;
	}
	
	public String getFileBlockPath() {
		return this.fileBlockPath;
	}
	
	public List<ExtendStores> getExtendStores() {
		return extendStores;
	}
	
	public String getTemporaryfilePath() {
		return this.TFPath;
	}
	
	public String getFileNodePathURL() {
		return dbURL;
	}

	public String getFileNodePathDriver() {
		return dbDriver;
	}

	public String getFileNodePathUserName() {
		return dbUser;
	}

	public String getFileNodePathPassWord() {
		return dbPwd;
	}
	
	/**
	 * 
	 * <h2>获得验证码等级</h2>
	 * <p>
	 * 返回设置的验证码等级枚举类（kohgylw.kiftd.server.enumeration.VCLevel），包括：关闭（CLOSE）、简单（Simplified）、标准（Standard）
	 * </p>
	 * 
	 * @author 青阳龙野(kohgylw)
	 * @return kohgylw.kiftd.server.enumeration.VCLevel 验证码等级
	 */
	public VCLevel getVCLevel() {
		if (this.vc == null) {
			this.vc = "";
		}
		final String vc = this.vc;
		switch (vc) {
		case "STANDARD": {
			return VCLevel.Standard;
		}
		case "SIMP": {
			return VCLevel.Simplified;
		}
		case "CLOSE": {
			return VCLevel.Close;
		}
		default: {
			return null;
		}
		}
	}
	
	public boolean foundAccount(final String account) {
		final String accountPwd = this.accountp.getProperty(account + ".pwd");
		return accountPwd != null && accountPwd.length() > 0;
	}

	public boolean checkAccountPwd(final String account, final String pwd) {
		final String apwd = this.accountp.getProperty(account + ".pwd");
		return apwd != null && apwd.equals(pwd);
	}
	
	public int getBuffSize() {
		return this.bufferSize;
	}
	
	public boolean inspectLogLevel(final LogLevel l) {
		int o = 0;
		int m = 0;
		if (l == null) {
			return false;
		}
		switch (l) {
		case None: {
			m = 0;
			break;
		}
		case Runtime_Exception: {
			m = 1;
		}
		case Event: {
			m = 2;
			break;
		}
		default: {
			m = 0;
			break;
		}
		}
		if (this.log == null) {
			this.log = "";
		}
		final String log = this.log;
		switch (log) {
		case "N": {
			o = 0;
			break;
		}
		case "R": {
			o = 1;
			break;
		}
		case "E": {
			o = 2;
			break;
		}
		default: {
			o = 1;
			break;
		}
		}
		return o >= m;
	}
	
	/**
	 * 
	 * <h2>获得某一账户的上传文件体积限制</h2>
	 * <p>
	 * 该方法用于判断指定用户上传的文件是否超过规定值。使用时需传入用户账户名字符串，返回该用户的最大上传限制。
	 * </p>
	 * 
	 * @author 青阳龙野(kohgylw)
	 * @param account
	 *            java.lang.String 需要检查的账户名
	 * @return long 以byte为单位的最大阈值，若返回0则设置错误，若小于0则不限制。
	 */
	public long getUploadFileSize(String account) {
		String defaultMaxSizeP = accountp.getProperty("defaultMaxSize");
		if (account == null) {
			return getMaxSizeByString(defaultMaxSizeP);
		} else {
			String accountMaxSizeP = accountp.getProperty(account + ".maxSize");
			return accountMaxSizeP == null ? getMaxSizeByString(defaultMaxSizeP) : getMaxSizeByString(accountMaxSizeP);
		}
	}
	
	/**
	 * 
	 * <h2>上传文件大小设置值转化方法</h2>
	 * <p>
	 * 该方法用于将配置文件中的设置值转化为long类型的数值，例如当输入字符串“1 KB”时，输出1024，输入“5GB”时，输出5368709120。
	 * </p>
	 * <p>
	 * 输入字符串格式规则：{数值}{存储单位（可选）}。其中，存储单位可使用下列字符串之一指代（不区分大小写）：
	 * </p>
	 * <ul>
	 * <li>KB 或 K</li>
	 * <li>MB 或 M</li>
	 * <li>GB 或 G</li>
	 * </ul>
	 * <p>
	 * 当不写存储单位时，则以“B”（byte）为单位进行转换。
	 * </p>
	 * 
	 * @author 青阳龙野(kohgylw)
	 * @param in
	 *            java.lang.String 要转换的字符串内容，格式应为“{数值}{存储单位（可选）}”，例如“1024KB”或“10mb”。
	 * @return long 以Byte为单位计算的体积值，若为0则代表设置错误，若为负数则代表无限制
	 */
	private long getMaxSizeByString(String in) {
		long r = 0L;
		// 首先，判断是否为null，若是，则直接返回-1。
		if (in == null || in.length() <= 0) {
			return -1L;
		}
		// 接下来判断是否有单位，若字符串总长小于1，则必无单位，否则可能有单位。
		try {
			if (in.length() > 1) {
				String value = in.substring(0, in.length() - 1).trim();
				String unit = in.substring(in.length() - 1).toLowerCase();
				if (in.length() > 2) {
					if (in.toLowerCase().charAt(in.length() - 1) == 'b') {
						unit = in.substring(in.length() - 2, in.length() - 1).toLowerCase();
						value = in.substring(0, in.length() - 2).trim();
					}
				}
				switch (unit) {
				case "k":
					r = Integer.parseInt(value) * 1024L;
					break;
				case "m":
					r = Integer.parseInt(value) * 1048576L;
					break;
				case "g":
					r = Integer.parseInt(value) * 1073741824L;
					break;
				default:
					r = Integer.parseInt(in.trim());
					break;
				}
			} else {
				r = Integer.parseInt(in.trim());
			}
		} catch (Exception e) {
		}
		return r;
	}
}
