package gov.nih.tbi.service.join;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import java.util.List;

import org.testng.annotations.Test;

import gov.nih.tbi.repository.model.InstancedRecord;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.service.cache.InstancedDataCache;
import gov.nih.tbi.service.cache.InstancedDataFormCache;

public class JoinerTest {

	private void debugPrintRecord(List<InstancedRecord> records) {

		for (InstancedRecord record : records) {
			String txt = "[";
			for (InstancedRow row : record.getSelectedRows()) {
				if (row != null) {
					txt += row.getRowUri();
				} else {
					txt += "null";
				}
				txt += ", ";
			}

			txt = txt.substring(0, txt.length() - 2);
			txt += "]";
			System.out.println(txt);
		}
	}

	@Test
	public void doJoinTest() {
		InstancedDataCache cache = new InstancedDataCache();
		InstancedDataFormCache formCache1 = new InstancedDataFormCache();
		InstancedRow form1row1 = mock(InstancedRow.class);
		when(form1row1.getRowUri()).thenReturn("form1row1");
		when(form1row1.getGuid()).thenReturn("guid1");
		formCache1.putRow(form1row1);
		cache.putFormCache("form1", formCache1);

		InstancedDataFormCache formCache2 = new InstancedDataFormCache();
		InstancedRow form2row1 = mock(InstancedRow.class);
		when(form2row1.getRowUri()).thenReturn("form2row1");
		when(form2row1.getGuid()).thenReturn("guid1");
		formCache2.putRow(form2row1);
		cache.putFormCache("form2", formCache2);

		String[] formList = {"form1", "form2"};
		Joiner joiner = new Joiner(formList, cache);
		List<InstancedRecord> records = joiner.doJoin();

		debugPrintRecord(records);
		assertEquals("guid1", records.get(0).getPrimaryKey());
	}

	@Test
	public void doJoinTest1() {
		InstancedDataCache cache = new InstancedDataCache();
		InstancedDataFormCache formCache1 = new InstancedDataFormCache();
		InstancedRow form1row1 = mock(InstancedRow.class);
		when(form1row1.getRowUri()).thenReturn("form1row1");
		when(form1row1.getGuid()).thenReturn("guid1");
		formCache1.putRow(form1row1);
		cache.putFormCache("form1", formCache1);

		InstancedDataFormCache formCache2 = new InstancedDataFormCache();
		InstancedRow form2row1 = mock(InstancedRow.class);
		when(form2row1.getRowUri()).thenReturn("form2row1");
		when(form2row1.getGuid()).thenReturn("guid1");
		formCache2.putRow(form2row1);
		cache.putFormCache("form2", formCache2);

		String[] formList = {"form1", "form2"};
		Joiner joiner = new Joiner(formList, cache);
		List<InstancedRecord> records = joiner.doJoin();

		assertEquals(1, records.size());
	}

	@Test
	public void doJoinTest3() {
		InstancedDataCache cache = new InstancedDataCache();
		InstancedDataFormCache formCache1 = new InstancedDataFormCache();
		InstancedRow form1row1 = mock(InstancedRow.class);
		when(form1row1.getRowUri()).thenReturn("form1row1");
		when(form1row1.getGuid()).thenReturn("guid1");
		formCache1.putRow(form1row1);


		InstancedRow form1row2 = mock(InstancedRow.class);
		when(form1row2.getRowUri()).thenReturn("form1row2");
		when(form1row2.getGuid()).thenReturn("guid2");
		formCache1.putRow(form1row2);

		cache.putFormCache("form1", formCache1);

		InstancedDataFormCache formCache2 = new InstancedDataFormCache();
		InstancedRow form2row1 = mock(InstancedRow.class);
		when(form2row1.getRowUri()).thenReturn("form2row1");
		when(form2row1.getGuid()).thenReturn("guid1");
		formCache2.putRow(form2row1);
		cache.putFormCache("form2", formCache2);

		String[] formList = {"form1", "form2"};
		Joiner joiner = new Joiner(formList, cache);
		List<InstancedRecord> records = joiner.doJoin();
		debugPrintRecord(records);
		assertEquals(records.size(), 2);
	}

	@Test
	public void doJoinTest4() {
		InstancedDataCache cache = new InstancedDataCache();
		InstancedDataFormCache formCache1 = new InstancedDataFormCache();
		InstancedRow form1row1 = mock(InstancedRow.class);
		when(form1row1.getRowUri()).thenReturn("form1row1");
		when(form1row1.getGuid()).thenReturn("guid1");
		formCache1.putRow(form1row1);


		InstancedRow form1row2 = mock(InstancedRow.class);
		when(form1row2.getRowUri()).thenReturn("form1row2");
		when(form1row2.getGuid()).thenReturn("guid1");
		formCache1.putRow(form1row2);

		InstancedRow form1row3 = mock(InstancedRow.class);
		when(form1row3.getRowUri()).thenReturn("form1row3");
		when(form1row3.getGuid()).thenReturn("guid2");
		formCache1.putRow(form1row3);

		InstancedRow form1row4 = mock(InstancedRow.class);
		when(form1row4.getRowUri()).thenReturn("form1row4");
		when(form1row4.getGuid()).thenReturn("guid3");
		formCache1.putRow(form1row4);

		cache.putFormCache("form1", formCache1);

		InstancedDataFormCache formCache2 = new InstancedDataFormCache();
		InstancedRow form2row1 = mock(InstancedRow.class);
		when(form2row1.getRowUri()).thenReturn("form2row1");
		when(form2row1.getGuid()).thenReturn("guid1");
		formCache2.putRow(form2row1);

		InstancedRow form2row2 = mock(InstancedRow.class);
		when(form2row2.getRowUri()).thenReturn("form2row2");
		when(form2row2.getGuid()).thenReturn("guid1");
		formCache2.putRow(form2row2);

		InstancedRow form2row3 = mock(InstancedRow.class);
		when(form2row3.getRowUri()).thenReturn("form2row3");
		when(form2row3.getGuid()).thenReturn("guid1");
		formCache2.putRow(form2row3);

		InstancedRow form2row4 = mock(InstancedRow.class);
		when(form2row4.getRowUri()).thenReturn("form2row4");
		when(form2row4.getGuid()).thenReturn("guid1");
		formCache2.putRow(form2row4);

		InstancedRow form2row5 = mock(InstancedRow.class);
		when(form2row5.getRowUri()).thenReturn("form2row5");
		when(form2row5.getGuid()).thenReturn("guid2");
		formCache2.putRow(form2row5);

		cache.putFormCache("form2", formCache2);

		String[] formList = {"form1", "form2"};
		Joiner joiner = new Joiner(formList, cache);
		List<InstancedRecord> records = joiner.doJoin();
		assertEquals(10, records.size());
	}

	@Test
	public void doJoinTest5() {
		InstancedDataCache cache = new InstancedDataCache();
		InstancedDataFormCache formCache1 = new InstancedDataFormCache();
		InstancedRow form1row1 = mock(InstancedRow.class);
		when(form1row1.getRowUri()).thenReturn("form1row1");
		when(form1row1.getGuid()).thenReturn("guid1");
		formCache1.putRow(form1row1);


		InstancedRow form1row2 = mock(InstancedRow.class);
		when(form1row2.getRowUri()).thenReturn("form1row2");
		when(form1row2.getGuid()).thenReturn("guid1");
		formCache1.putRow(form1row2);

		cache.putFormCache("form1", formCache1);

		InstancedDataFormCache formCache2 = new InstancedDataFormCache();
		InstancedRow form2row1 = mock(InstancedRow.class);
		when(form2row1.getRowUri()).thenReturn("form2row1");
		when(form2row1.getGuid()).thenReturn("guid1");
		formCache2.putRow(form2row1);

		InstancedRow form2row2 = mock(InstancedRow.class);
		when(form2row2.getRowUri()).thenReturn("form2row2");
		when(form2row2.getGuid()).thenReturn("guid1");
		formCache2.putRow(form2row2);

		InstancedRow form2row3 = mock(InstancedRow.class);
		when(form2row3.getRowUri()).thenReturn("form2row3");
		when(form2row3.getGuid()).thenReturn("guid1");
		formCache2.putRow(form2row3);

		InstancedRow form2row4 = mock(InstancedRow.class);
		when(form2row4.getRowUri()).thenReturn("form2row4");
		when(form2row4.getGuid()).thenReturn("guid1");
		formCache2.putRow(form2row4);


		cache.putFormCache("form2", formCache2);

		InstancedDataFormCache formCache3 = new InstancedDataFormCache();
		InstancedRow form3row1 = mock(InstancedRow.class);
		when(form3row1.getRowUri()).thenReturn("form3row1");
		when(form3row1.getGuid()).thenReturn("guid1");
		formCache3.putRow(form3row1);

		InstancedRow form3row2 = mock(InstancedRow.class);
		when(form3row2.getRowUri()).thenReturn("form3row2");
		when(form3row2.getGuid()).thenReturn("guid1");
		formCache3.putRow(form3row2);


		cache.putFormCache("form3", formCache3);

		String[] formList = {"form1", "form2", "form3"};
		Joiner joiner = new Joiner(formList, cache);
		List<InstancedRecord> records = joiner.doJoin();
		debugPrintRecord(records);
		assertEquals(records.size(), 16);
	}

	@Test
	public void doJoinTest6() {
		InstancedDataCache cache = new InstancedDataCache();
		InstancedDataFormCache formCache1 = new InstancedDataFormCache();
		InstancedRow form1row1 = mock(InstancedRow.class);
		when(form1row1.getRowUri()).thenReturn("form1row1");
		when(form1row1.getGuid()).thenReturn("guid1");
		formCache1.putRow(form1row1);


		InstancedRow form1row2 = mock(InstancedRow.class);
		when(form1row2.getRowUri()).thenReturn("form1row2");
		when(form1row2.getGuid()).thenReturn("guid1");
		formCache1.putRow(form1row2);

		cache.putFormCache("form1", formCache1);

		InstancedDataFormCache formCache2 = new InstancedDataFormCache();
		InstancedRow form2row1 = mock(InstancedRow.class);
		when(form2row1.getRowUri()).thenReturn("form2row1");
		when(form2row1.getGuid()).thenReturn("guid2");
		formCache2.putRow(form2row1);

		InstancedRow form2row2 = mock(InstancedRow.class);
		when(form2row2.getRowUri()).thenReturn("form2row2");
		when(form2row2.getGuid()).thenReturn("guid2");
		formCache2.putRow(form2row2);

		InstancedRow form2row3 = mock(InstancedRow.class);
		when(form2row3.getRowUri()).thenReturn("form2row3");
		when(form2row3.getGuid()).thenReturn("guid2");
		formCache2.putRow(form2row3);

		InstancedRow form2row4 = mock(InstancedRow.class);
		when(form2row4.getRowUri()).thenReturn("form2row4");
		when(form2row4.getGuid()).thenReturn("guid2");
		formCache2.putRow(form2row4);


		cache.putFormCache("form2", formCache2);

		InstancedDataFormCache formCache3 = new InstancedDataFormCache();
		InstancedRow form3row1 = mock(InstancedRow.class);
		when(form3row1.getRowUri()).thenReturn("form3row1");
		when(form3row1.getGuid()).thenReturn("guid1");
		formCache3.putRow(form3row1);

		InstancedRow form3row2 = mock(InstancedRow.class);
		when(form3row2.getRowUri()).thenReturn("form3row2");
		when(form3row2.getGuid()).thenReturn("guid1");
		formCache3.putRow(form3row2);


		cache.putFormCache("form3", formCache3);

		String[] formList = {"form1", "form2", "form3"};
		Joiner joiner = new Joiner(formList, cache);
		List<InstancedRecord> records = joiner.doJoin();
		// debugPrintRecord(records);
		assertEquals(records.size(), 8);
	}

	@Test
	public void doJoinTest7() {
		InstancedDataCache cache = new InstancedDataCache();
		InstancedDataFormCache formCache1 = new InstancedDataFormCache();
		InstancedRow form1row1 = mock(InstancedRow.class);
		when(form1row1.getRowUri()).thenReturn("form1row1");
		when(form1row1.getGuid()).thenReturn("guid1");
		formCache1.putRow(form1row1);


		InstancedRow form1row2 = mock(InstancedRow.class);
		when(form1row2.getRowUri()).thenReturn("form1row2");
		when(form1row2.getGuid()).thenReturn("guid1");
		formCache1.putRow(form1row2);

		cache.putFormCache("form1", formCache1);

		InstancedDataFormCache formCache2 = new InstancedDataFormCache();
		InstancedRow form2row1 = mock(InstancedRow.class);
		when(form2row1.getRowUri()).thenReturn("form2row1");
		when(form2row1.getGuid()).thenReturn("guid1");
		formCache2.putRow(form2row1);

		InstancedRow form2row2 = mock(InstancedRow.class);
		when(form2row2.getRowUri()).thenReturn("form2row2");
		when(form2row2.getGuid()).thenReturn("guid1");
		formCache2.putRow(form2row2);

		InstancedRow form2row3 = mock(InstancedRow.class);
		when(form2row3.getRowUri()).thenReturn("form2row3");
		when(form2row3.getGuid()).thenReturn("guid1");
		formCache2.putRow(form2row3);

		InstancedRow form2row4 = mock(InstancedRow.class);
		when(form2row4.getRowUri()).thenReturn("form2row4");
		when(form2row4.getGuid()).thenReturn("guid1");
		formCache2.putRow(form2row4);


		cache.putFormCache("form2", formCache2);

		InstancedDataFormCache formCache3 = new InstancedDataFormCache();
		InstancedRow form3row1 = mock(InstancedRow.class);
		when(form3row1.getRowUri()).thenReturn("form3row1");
		when(form3row1.getGuid()).thenReturn("guid2");
		formCache3.putRow(form3row1);

		InstancedRow form3row2 = mock(InstancedRow.class);
		when(form3row2.getRowUri()).thenReturn("form3row2");
		when(form3row2.getGuid()).thenReturn("guid2");
		formCache3.putRow(form3row2);


		cache.putFormCache("form3", formCache3);

		String[] formList = {"form1", "form2", "form3"};
		Joiner joiner = new Joiner(formList, cache);
		List<InstancedRecord> records = joiner.doJoin();
		// debugPrintRecord(records);
		assertEquals(records.size(), 10);
	}
}
