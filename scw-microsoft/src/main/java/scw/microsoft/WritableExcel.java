package scw.microsoft;

import java.io.Flushable;

/**
 * 可写的excel
 * @author shuchaowen
 *
 */
public interface WritableExcel extends Excel, Flushable {
	WritableSheet getSheet(int sheetIndex);

	WritableSheet getSheet(String sheetName);

	WritableSheet createSheet();
	
	WritableSheet createSheet(String sheetName);
	
	void removeSheet(int sheetIndex);
}
