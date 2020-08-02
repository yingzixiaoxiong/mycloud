package xiaoxiong.MyCloud.server.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import xiaoxiong.MyCloud.server.util.FileNodeUtil;

@WebListener
public class H2DBinitListener implements ServletContextListener {
	public void contextInitialized(final ServletContextEvent sce) {
		FileNodeUtil.initNodeTableToDataBase();
	}

	public void contextDestroyed(final ServletContextEvent sce) {
	}
}