package xiaoxiong.MyCloud.server.configation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.MultipartConfigElement;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import com.google.gson.Gson;

import xiaoxiong.MyCloud.server.pojo.ExtendStores;
import xiaoxiong.MyCloud.server.util.ConfigureReader;

@Configurable
@ComponentScan({ "xiaoxiong.MyCloud.server.controller", "xiaoxiong.MyCloud.service.impl" })
//@ServletComponentScan({ "xiaoxiong.MyCloud.server.listener", "xiaoxiong.MyCloud.server.filter" })
@ServletComponentScan({ "xiaoxiong.MyCloud.server.listener" })
@Import({ DataAccess.class })
public class MVC extends ResourceHttpRequestHandler implements WebMvcConfigurer{
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		// TODO 自动生成的方法存根
		configurer.enable();
	}

	/**
     * 静态资源文件映射配置
     * @param registry
     */
    @Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		// 将静态页面资源所在文件夹加入至资源路径中，功能类似于可以访问访问http://localhost:8083/1.png这样的静态文件了
		registry.addResourceHandler(new String[] { "/**" }).addResourceLocations(new String[] {
				"file:" + ConfigureReader.instance().getPath() + File.separator + "webContext" + File.separator });
		// 将所有文件块的保存路径加入至资源路径中（提供某些预览服务）
		List<String> paths = new ArrayList<>();
		paths.add("file:" + ConfigureReader.instance().getFileBlockPath());
		for (ExtendStores es : ConfigureReader.instance().getExtendStores()) {
			paths.add(
					"file:" + (es.getPath().getAbsolutePath().endsWith(File.separator) ? es.getPath().getAbsolutePath()
							: es.getPath().getAbsolutePath() + File.separator));
		}
		registry.addResourceHandler(new String[] { "/fileblocks/**" })
				.addResourceLocations(paths.toArray(new String[0]));
	}

	@Bean
	public MultipartConfigElement multipartConfigElement() {
		final MultipartConfigFactory factory = new MultipartConfigFactory(); //用于文件上传
		factory.setMaxFileSize(-1L);
		factory.setLocation(ConfigureReader.instance().getTemporaryfilePath());
		return factory.createMultipartConfig();
	}

	@Bean //Bean的id为gson
	public Gson gson() {
		return new Gson();
	}
}
