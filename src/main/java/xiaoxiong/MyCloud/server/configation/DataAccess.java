package xiaoxiong.MyCloud.server.configation;

import java.io.File;

import javax.sql.DataSource;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import xiaoxiong.MyCloud.server.util.ConfigureReader;


@Configurable
public class DataAccess {
	private static Resource[] mapperFiles;
    private static Resource mybatisConfg;
    
    @Bean
    public DataSource dataSource() {
        final DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(ConfigureReader.instance().getFileNodePathDriver());
        ds.setUrl(ConfigureReader.instance().getFileNodePathURL());
        ds.setUsername(ConfigureReader.instance().getFileNodePathUserName());
        ds.setPassword(ConfigureReader.instance().getFileNodePathPassWord());
        return (DataSource)ds;
    }
    
    @Bean(name = { "sqlSessionFactory" })
    @Autowired //由于DataSource只有一个bean所以@Autowired把这个bean自动装填进去
    public SqlSessionFactoryBean sqlSessionFactoryBean(final DataSource ds) {
        final SqlSessionFactoryBean ssf = new SqlSessionFactoryBean();
        ssf.setDataSource(ds);
        ssf.setConfigLocation(DataAccess.mybatisConfg);
        ssf.setMapperLocations(DataAccess.mapperFiles);
        return ssf;
    }
    
    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        final MapperScannerConfigurer msf = new MapperScannerConfigurer();
        msf.setBasePackage("xiaoxiong.MyCloud.server.mapper");
        msf.setSqlSessionFactoryBeanName("sqlSessionFactory");
        return msf;
    }
    
    static {
        final String mybatisResourceFolder = ConfigureReader.instance().getPath() + File.separator + "mybatisResource" + File.separator;
        final String mapperFilesFolder = mybatisResourceFolder + "mapperXML" + File.separator;
        DataAccess.mapperFiles = new Resource[] { new FileSystemResource(mapperFilesFolder + "NodeMapper.xml"), new FileSystemResource(mapperFilesFolder + "FolderMapper.xml") };
        DataAccess.mybatisConfg = (Resource)new FileSystemResource(mybatisResourceFolder + "mybatis.xml");
    }
}
