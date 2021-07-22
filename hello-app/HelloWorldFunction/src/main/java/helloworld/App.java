package helloworld;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;

import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeAsyncClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeAsyncClientBuilder;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClientBuilder;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.http.apache.ApacheHttpClient;


/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    
    private static Logger logger = Logger.getLogger(App.class.getName());
    private APIGatewayProxyResponseEvent response;

    private Gson gson;

    private EventBridgeAsyncClient eventBridgeAsyncClient;
    private EventBridgeClient eventBridgeClient;

    String time;
    String message;

    private long timeInMills = 0L;
    private long putEventsDuration = 0L;


    public App () {
        logger.info("CONSTRUCTOR:INI");


        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        logger.info("headers y APIGatewayProxyResponseEvent creados.");

        gson = new Gson();


        /*
        eventBridgeAsyncClient = EventBridgeAsyncClient.builder()
                .region(Region.US_EAST_1)
                .httpClient(NettyNioAsyncHttpClient.builder().build())
                .build();
        */
        eventBridgeClient = EventBridgeClient.builder()
            .region(Region.US_EAST_1)
            .httpClient(ApacheHttpClient.builder().build())
            .build();
            
        logger.info("Client created.");

        logger.info("CONSTRUCTOR:FIN");
    }



    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, final Context context) {

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

        PutEventsRequestEntry requestEntry = PutEventsRequestEntry.builder()
                .eventBusName("ees-test-eventasync")
                .resources("resource1", "resource2")
                .source("com.eliashel.myapp")
                .detailType("myDetailType")
                .detail(message)
                .build();
        logger.info("PutEventsRequestEntry creado.");


        List<PutEventsRequestEntry> requestEntries = new ArrayList<>();
        requestEntries.add(requestEntry);

        PutEventsRequest eventsRequest = PutEventsRequest.builder()
                .entries(requestEntries)
                .build();
        logger.info("PutEventsRequest creado.");

        timeInMills = Calendar.getInstance().getTimeInMillis();
        //eventBridgeAsyncClient.putEvents(eventsRequest);
        eventBridgeClient.putEvents(eventsRequest);
        putEventsDuration = Calendar.getInstance().getTimeInMillis() - timeInMills;
        logger.info("Eventos enviados: putEvents(eventsRequest): " 
        + String.format("{\"testId\": \"%d\", \"time\": \"%s\", \"putEventsDuration\": \"%d\"}", testId, time, putEventsDuration) );

        // Si el tiempo de espera es mayor a cero, duerme el hilo principal por el tiempo indicado.
        if ( timeToSleep > 0 ) {
                try {

                        logger.info("Sleep: "+ timeToSleep);
                        Thread.sleep(timeToSleep);
                } catch ( Exception e ) {
                        logger.severe("Error sleep." );
                        e.printStackTrace();
                }
        }
        

        try {

            final String pageContents = this.getPageContents("https://checkip.amazonaws.com");
            String output = String.format("{ \"message\": \"hello world v2.3\", \"location\": \"%s\", \"testId\": \"%d\", \"time\": \"%s\" }", 
                pageContents, testId, time);

            return response
                    .withStatusCode(200)
                    .withBody(output);
        } catch (IOException e) {
            return response
                    .withBody("{}")
                    .withStatusCode(500);
        } finally {
            logger.info("handleRequest():FIN: response: " + response.toString());
        }
    }

    private String getPageContents(String address) throws IOException{
        URL url = new URL(address);
        try(BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
