package vopen.download;

import java.util.List;

import vopen.db.DBApi.DownLoadInfo;
import vopen.db.DBApi.EDownloadStatus;

/**
 * 该类用来管理Service进程
 * 便于在UI上进行显示
 * @author user
 *
 */
public interface DownloadListener {
	//添加到下载列表
	public void onAddDownloadBean(List<DownLoadInfo> list);
	//完成下载，成功或失败
	public void onFinishDownload(EDownloadStatus status, int id, int down, int total);
	//开始新的下载
	public void onStartDownload(int id, int downSize, int total);
	//反应下载的过程
	public void onDownloadProgeress(int offset);
}
