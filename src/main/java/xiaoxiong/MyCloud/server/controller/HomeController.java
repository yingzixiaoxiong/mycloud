package xiaoxiong.MyCloud.server.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import xiaoxiong.MyCloud.server.service.AccountService;
import xiaoxiong.MyCloud.server.service.FileService;
import xiaoxiong.MyCloud.server.service.FolderViewService;
import xiaoxiong.MyCloud.server.service.ServerInfoService;


@Controller
@RequestMapping({ "/homeController" })
public class HomeController {
	private static final String CHARSET_BY_AJAX = "text/html; charset=utf-8";
	@Resource
	private ServerInfoService si;
	
	@Resource
	private FolderViewService fvs;
	
	@Resource
	private AccountService as;
	
	@Resource
	private FileService fis;
	
	
	@RequestMapping({ "/getServerOS.ajax" })
	@ResponseBody
	public String getServerOS() {
		return this.si.getOSName();
	}
	
	@RequestMapping(value = { "/getFolderView.ajax" }, produces = { CHARSET_BY_AJAX }) //produces注解RequestMapping中produces属性可以设置返回数据的类型以及编码，可以是json或者xml
	@ResponseBody
	public String getFolderView(final String fid, final HttpSession session, final HttpServletRequest request) {
		return fvs.getFolderViewToJson(fid, session, request);
	}
	
	@RequestMapping(value = { "/getPublicKey.ajax" }, produces = { CHARSET_BY_AJAX })
	@ResponseBody
	public String getPublicKey() {
		return this.as.getPublicKey();
	}
	
	@RequestMapping({ "/doLogin.ajax" })
	@ResponseBody
	public String doLogin(final HttpServletRequest request, final HttpSession session) {
		return this.as.checkLoginRequest(request, session); //登录成功时session内会存储该用户的id，session.setAttribute("ACCOUNT", (Object) accountId);
	}
	
	@RequestMapping(value = { "/douploadFile.ajax" }, produces = { CHARSET_BY_AJAX })
	@ResponseBody
	public String douploadFile(final HttpServletRequest request, final HttpServletResponse response,
			final MultipartFile file) {
		return this.fis.doUploadFile(request, response, file); //file是吧request的file属性直接拿出来了
	}
	
	@RequestMapping(value = { "/checkUploadFile.ajax" }, produces = { CHARSET_BY_AJAX })
	@ResponseBody
	public String checkUploadFile(final HttpServletRequest request, final HttpServletResponse response) {
		return this.fis.checkUploadFile(request, response);
	}
	
	@RequestMapping({ "/downloadFile.do" })
	public void downloadFile(final HttpServletRequest request, final HttpServletResponse response) {
		this.fis.doDownloadFile(request, response);
	}

}
