package scw.microsoft;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import scw.aop.annotation.AopEnable;

@AopEnable(false)
public interface ExcelOperations extends ExcelReader {
	Excel create(InputStream inputStream) throws IOException, ExcelException;

	WritableExcel create(OutputStream outputStream) throws IOException, ExcelException;
	
	WritableExcel create(OutputStream outputStream, ExcelVersion excelVersion) throws IOException, ExcelException;

	Excel create(File file) throws IOException, ExcelException;

	WritableExcel createWritableExcel(File file) throws IOException, ExcelException;
	
	ExcelExport createExcelExport(OutputStream outputStream) throws IOException, ExcelException;
	
	ExcelExport createExcelExport(OutputStream outputStream, ExcelVersion excelVersion) throws IOException, ExcelException;
	
	ExcelExport createExcelExport(File file) throws IOException, ExcelException;
}
