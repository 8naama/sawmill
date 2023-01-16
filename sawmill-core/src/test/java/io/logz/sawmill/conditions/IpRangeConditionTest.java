package io.logz.sawmill.conditions;

import io.logz.sawmill.Doc;
import org.junit.Test;

import static io.logz.sawmill.utils.DocUtils.createDoc;
import static org.assertj.core.api.Assertions.assertThat;

public class IPRangeConditionTest {
	
	@Test
	public void testFieldNotExists() {}

	@Test
	public void testNotIPValue() {}

	@Test
	public void testvalidPublicIPValue() {}

	@Test
	public void testvalidPrivateIPValue() {}

	@Test
	public void testvEmptyValue() {}
}