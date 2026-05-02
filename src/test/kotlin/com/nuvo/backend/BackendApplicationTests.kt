package com.nuvo.backend

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class BackendApplicationTests {

	@Test
	fun applicationClassIsLoadableWithoutStartingSpring() {
		assertEquals("BackendApplication", BackendApplication::class.simpleName)
	}

}
