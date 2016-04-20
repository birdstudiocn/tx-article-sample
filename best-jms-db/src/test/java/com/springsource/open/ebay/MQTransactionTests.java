package com.springsource.open.ebay;

import static org.junit.Assert.assertEquals;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/META-INF/spring/data-source-context.xml")
public class MQTransactionTests {
	private SimpleJdbcTemplate jdbcTemplate;
	private SimpleJdbcTemplate statjdbcTemplate;

	private int totalCount = 0;

	@Autowired
	public void setDataSource(@Qualifier("dataSource") DataSource dataSource,
			@Qualifier("statDataSource") DataSource statDataSource) {
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
		this.statjdbcTemplate = new SimpleJdbcTemplate(statDataSource);
	}

	@BeforeTransaction
	public void clearData() {
		totalCount = jdbcTemplate
				.queryForInt("select count(*) from T_TRANSACTION");
	}

	@AfterTransaction
	public void checkPostConditions() {

		int count = jdbcTemplate
				.queryForInt("select count(*) from T_TRANSACTION");
		// This change was rolled back by the test framework
		assertEquals(totalCount + 1, count);

		count = statjdbcTemplate.queryForInt("select count(*) from T_USER");
		// This rolls back as well if the connections are managed together
		// assertEquals(2, count);

	}

	/**
	 * Vanilla test case for two inserts into two data sources. Both should roll
	 * back.
	 * 
	 * @throws Exception
	 */
	@Transactional
	@Test
	public void testInsertIntoTwoDataSources() throws Exception {

		int count = jdbcTemplate
				.update("INSERT into T_TRANSACTION (SELLER_ID,BUYER_ID,AMOUNT) values (?,?,?)",
						1, 2, 1);
		assertEquals(1, count);

		/*
		 * count = statjdbcTemplate.update(
		 * "UPDATE T_USER SET amt_sold = amt_sold + ? WHERE id = ?", 1, 1);
		 * assertEquals(1, count);
		 * 
		 * count = statjdbcTemplate.update(
		 * "UPDATE T_USER SET amt_bought = amt_bought + ? WHERE id = ?", 1, 2);
		 * assertEquals(1, count);
		 */
	}
}
