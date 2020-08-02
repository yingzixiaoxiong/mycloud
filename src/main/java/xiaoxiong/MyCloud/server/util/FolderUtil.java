package xiaoxiong.MyCloud.server.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import xiaoxiong.MyCloud.server.mapper.FolderMapper;
import xiaoxiong.MyCloud.server.mapper.NodeMapper;
import xiaoxiong.MyCloud.server.model.Folder;
import xiaoxiong.MyCloud.server.model.Node;

@Component
public class FolderUtil {
	@Resource
	private FolderMapper fm;
	@Resource
	private NodeMapper fim;
	@Resource
	private FileBlockUtil fbu;

	public List<Folder> getParentList(final String fid) {
		Folder f = this.fm.queryById(fid);
		final List<Folder> folderList = new ArrayList<Folder>();
		if (f != null) {
			while (!f.getFolderParent().equals("null")) {
				f = this.fm.queryById(f.getFolderParent());
				folderList.add(f);
			}
		}
		Collections.reverse(folderList);
		return folderList;
	}

	public int deleteAllChildFolder(final String folderId) {
		final Thread deleteChildFolderThread = new Thread(() -> this.iterationDeleteFolder(folderId));
		deleteChildFolderThread.start();
		return this.fm.deleteById(folderId);
	}

	private void iterationDeleteFolder(final String folderId) {
		final List<Folder> cf = (List<Folder>) this.fm.queryByParentId(folderId);
		if (cf.size() > 0) {
			for (final Folder f : cf) {
				this.iterationDeleteFolder(f.getFolderId());
			}
		}
		final List<Node> files = (List<Node>) this.fim.queryByParentFolderId(folderId);
		if (files.size() > 0) {
			this.fim.deleteByParentFolderId(folderId);
			for (final Node f2 : files) {
				this.fbu.deleteFromFileBlocks(f2);
			}
		}
		this.fm.deleteById(folderId);
	}
}
