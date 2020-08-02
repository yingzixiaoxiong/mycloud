package xiaoxiong.MyCloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import xiaoxiong.MyCloud.server.configation.MVC;


@SpringBootApplication
@Import({ MVC.class })
public class App 
{
    public static void main( String[] args )
    {
    	SpringApplication.run(App.class, args);
    }
}
