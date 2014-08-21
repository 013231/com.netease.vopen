package common.pal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IFile
{
	
	/**
	 *  创建此抽象路径名指定的目录。
	 */
	public boolean mkdir();
	
	/**
	 * 创建此抽象路径名指定的目录，包括创建必需但不存在的父目录。
	 */
	public boolean mkdirs() ;

	
	/**
	 * 重新命名此抽象路径名表示的文件。
	 * @param newName
	 * @return
	 */
	public boolean renameTo(String newName); 

	/**
	 *  返回由此抽象路径名表示的文件或目录的名称。
	 * @return
	 */
	public String getName();
	
	/**
	 * 测试此抽象路径名表示的文件或目录是否存在。
	 * @return
	 */
	public boolean exists(); 

	/**
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException;
	
	/**
	 *   返回由此抽象路径名表示的文件的长度。
	 * @return
	 * @throws IOException
	 */
	public long getSize() throws IOException;
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public OutputStream openOutputStream() throws IOException;
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public InputStream openInputStream() throws IOException;

	
	
}
