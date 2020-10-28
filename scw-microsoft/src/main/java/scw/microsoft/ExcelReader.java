package scw.microsoft;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import scw.aop.annotation.AopEnable;

@AopEnable(false)
public interface ExcelReader {
	/**
	 * 逐行读取(内存消耗小)
	 * 
	 * @param inputStream
	 * @param rowCallback
	 * @throws IOException
	 * @throws ExcelException
	 */
	void read(InputStream inputStream, RowCallback rowCallback) throws IOException, ExcelException;

	void read(File file, RowCallback rowCallback) throws IOException, ExcelException;
}
