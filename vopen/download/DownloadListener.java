package vopen.download;

import vopen.db.DBApi.EDownloadStatus;

/**
 * 该接口用于下载线程通知UI，便于在UI上进行显示
 * @author user
 */
public interface DownloadListener {
	/**
	 * 开始一个任务的下载
	 * @param id 任务的id
	 * @param downSize 已下载大小
	 * @param total 文件的总大小
	 */
	public void onStartDownload(int id, int downSize, int total);

	/**
	 * 下载任务进行中
	 * @param id 任务的id
	 * @param offset 当前已下载的大小
	 * @param total 文件的总大小
	 */
	public void onDownloadProgeress(int id, int offset, int total);

	/**
	 * 任务结束，成功或失败
	 * @param id 任务的id
	 * @param status 任务状态
	 * @param down 当前下载的大小
	 * @param total 文件的总大小
	 */
	public void onFinishDownload( int id, EDownloadStatus status, int down,
			int total);
}
