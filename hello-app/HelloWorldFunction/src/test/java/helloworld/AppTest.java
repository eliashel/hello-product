package helloworld;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class AppTest {

  @Test
  void successfulResponse() {
    App app = new App();
    APIGatewayProxyResponseEvent result = app.handleRequest(null, null);

    Assertions.assertEquals(200, result.getStatusCode().intValue());
    Assertions.assertEquals("application/json", result.getHeaders().get("Content-Type"));
    String content = result.getBody();
    Assertions.assertNotNull(content);
    Assertions.assertTrue(content.contains("\"message\""));
    Assertions.assertTrue(content.contains("hello world"));
    Assertions.assertTrue(content.contains("\"location\""));
  }
}
