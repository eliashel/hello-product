package helloworld;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SNSApp implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static Logger logger = Logger.getLogger(SNSApp.class.getName());
    private APIGatewayProxyResponseEvent response;

    private SnsClient snsClient;

    private Gson gson;

    PublishRequest request;
    PublishResponse result;

    String time;
    String message;

    private long timeInMills = 0L;
    private long putEventsDuration = 0L;


    public SNSApp () {
        logger.info("CONSTRUCTOR:INI");


        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        logger.info("headers y APIGatewayProxyResponseEvent creados.");

        gson = new Gson();


        snsClient = SnsClient.builder()
                .region(Region.US_EAST_1)
                .build();


        logger.info("Client created.");

        logger.info("CONSTRUCTOR:FIN");
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

        logger.info("handleRequest():INI: request: " + input);



        // A través del queryParam "timeToSleep" se puede parametrizar el tiempo de espera. Si es null o "0", no se ejecuta el tiempo de espera.
        int timeToSleep = 0;
        if ( input.getQueryStringParameters() != null && input.getQueryStringParameters().get("timeToSleep") != null )
            try {
                timeToSleep = Integer.parseInt(input.getQueryStringParameters().get("timeToSleep"));
            } catch (Exception e) {
                logger.warning("No se pudo formatear el parámetro timeToSleep.");
                timeToSleep = 0;
            }

        // Para identificar una ejecución de prueba de carga de otra, en caso de que exista un delay muy largo entre las pruebas de carga se usa el "testId"
        int testId = 0;
        if ( input.getQueryStringParameters() != null && input.getQueryStringParameters().get("testId") != null )
            try {
                testId = Integer.parseInt(input.getQueryStringParameters().get("testId"));
            } catch (Exception e) {
                logger.warning("No se pudo formatear el parámetro testId.");
                testId = 0;
            }

        time = Calendar.getInstance().getTime().toString();
        message = String.format("{\"testId\": \"%d\", \"time\": \"%s\"}", testId, time);

        try {
            request = PublishRequest.builder()
                    .message(message)
                    .topicArn(topicArn)
                    .build();

            result = snsClient.publish(request);
            System.out.println(result.messageId() + " Message sent. Status was " + result.sdkHttpResponse().statusCode());

        } catch (SnsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }

        return null;
    }
}
