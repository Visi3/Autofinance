package com.fag.Autofinance;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AutofinanceApplicationTests {

	@Test
	void contextLoads() {
		Assumptions.assumeTrue(System.getenv("CI") == null);
	}

}
