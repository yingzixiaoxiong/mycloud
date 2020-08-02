package xiaoxiong.MyCloud.server.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
	String checkUploadFile(final HttpServletRequest request, final HttpServletResponse response);

	String doUploadFile(final HttpServletRequest request,final HttpServletResponse response, final MultipartFile file);

	String deleteFile(final HttpServletRequest request);

	void doDownloadFile(final HttpServletRequest request, final HttpServletResponse response);

	String doRenameFile(final HttpServletRequest request);

	String deleteCheckedFiles(final HttpServletRequest request);

	String getPackTime(final HttpServletRequest request);

	String downloadCheckedFiles(final HttpServletRequest request);

	void downloadCheckedFilesZip(final HttpServletRequest request, final HttpServletResponse response) throws Exception;

	String confirmMoveFiles(final HttpServletRequest request);

	String doMoveFiles(final HttpServletRequest request);
}
