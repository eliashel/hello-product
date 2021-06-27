package helloworld;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class AppTest {

  @Test
  public void successfulResponse() {
    App app = new App();
    APIGatewayProxyResponseEvent result = app.handleRequest(null, null);

    Assertions.assertEquals(result.getStatusCode().intValue(), 200);
    Assertions.assertEquals(result.getHeaders().get("Content-Type"), "application/json");
    String content = result.getBody();
    Assertions.assertNotNull(content);
    Assertions.assertTrue(content.contains("\"message\""));
    Assertions.assertTrue(content.contains("hello world"));
    Assertions.assertTrue(content.contains("\"location\""));
  }
}
