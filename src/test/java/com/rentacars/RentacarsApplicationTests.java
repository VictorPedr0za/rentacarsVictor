package com.rentacars;

import com.rentacars.support.DynamoLocalServer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** El contexto completo arranca contra un DynamoDB Local embebido (no necesita AWS ni Docker). */
@SpringBootTest
class RentacarsApplicationTests {

	@DynamicPropertySource
	static void dynamo(DynamicPropertyRegistry registry) {
		registry.add("app.dynamodb.endpoint", DynamoLocalServer::endpoint);
		registry.add("app.dynamodb.table-prefix", () -> "ctx_");
	}

	@Test
	void contextLoads() {
	}

}
