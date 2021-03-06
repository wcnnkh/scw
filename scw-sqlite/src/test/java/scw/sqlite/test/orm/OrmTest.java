package scw.sqlite.test.orm;

import org.junit.Test;

import scw.db.DB;
import scw.env.Sys;
import scw.json.JSONUtils;
import scw.sqlite.SQLiteDB;
import scw.util.XUtils;

public class OrmTest {
	private DB db = new SQLiteDB(Sys.env.getWorkPath() + "/orm_test.db");

	{
		db.createTable(TestTable1.class);
	}

	private void initData() {
		for (int i = 0; i < 5; i++) {
			TestTable1 table1 = db.getById(TestTable1.class, i);
			if (table1 == null) {
				table1 = new TestTable1();
				table1.setId(i);
				table1.setKey(XUtils.getUUID());
				table1.setValue(i);
				boolean b = db.save(table1);
				System.out.println("添加数据" + (b ? "成功" : "失败"));
			}
		}
	}

	private void saveOrUpdate() {
		for (int i = 0; i < 5; i++) {
			TestTable1 table1 = new TestTable1();
			table1.setId(i);
			table1.setKey(XUtils.getUUID());
			table1.setValue(i);
			boolean b = db.saveOrUpdate(table1);
			System.out.println("saveOrUpdate数据" + (b ? "成功" : "失败"));
		}
	}

	@Test
	public void test() {
		initData();
		System.out.println(JSONUtils.getJsonSupport().toJSONString(db.getByIdList(TestTable1.class, 1)));
		saveOrUpdate();
		System.out.println(JSONUtils.getJsonSupport().toJSONString(db.getByIdList(TestTable1.class, 1)));
	}
}
