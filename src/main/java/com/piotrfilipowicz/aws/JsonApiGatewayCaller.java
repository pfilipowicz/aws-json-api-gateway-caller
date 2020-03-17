package com.piotrfilipowicz.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.DefaultRequest;
import com.amazonaws.RequestConfig;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.http.ExecutionContext;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.http.JsonErrorResponseHandler;
import com.amazonaws.http.JsonResponseHandler;
import com.amazonaws.internal.AmazonWebServiceRequestAdapter;
import com.amazonaws.internal.auth.DefaultSignerProvider;
import com.amazonaws.protocol.json.JsonOperationMetadata;
import com.amazonaws.protocol.json.SdkStructuredPlainJsonFactory;
import com.amazonaws.transform.JsonErrorUnmarshaller;
import com.amazonaws.transform.JsonUnmarshallerContext;
import com.amazonaws.transform.Unmarshaller;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;

public class JsonApiGatewayCaller extends AmazonWebServiceClient {
    private static final String API_GATEWAY_SERVICE_NAME = "execute-api";

    private final AWSCredentialsProvider credentials;
    private final String apiKey;
    private final AWS4Signer signer;

    private final JsonResponseHandler<ApiGatewayResponse> responseHandler;
    private final JsonErrorResponseHandler errorResponseHandler;

    public JsonApiGatewayCaller(String accessKey, String secretAccessKey, String apiKey, String region, URI endpoint) {

        super(new ClientConfiguration());

        this.credentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretAccessKey));
        this.apiKey = apiKey;
        this.endpoint = endpoint;

        this.signer = new AWS4Signer();
        this.signer.setServiceName(API_GATEWAY_SERVICE_NAME);
        this.signer.setRegionName(region);

        final JsonOperationMetadata metadata = new JsonOperationMetadata().withHasStreamingSuccessResponse(false).withPayloadJson(false);
        final Unmarshaller<ApiGatewayResponse, JsonUnmarshallerContext> responseUnmarshaller = in -> new ApiGatewayResponse(in.getHttpResponse());
        this.responseHandler = SdkStructuredPlainJsonFactory.SDK_JSON_FACTORY.createResponseHandler(metadata, responseUnmarshaller);

        JsonErrorUnmarshaller defaultErrorUnmarshaller = new JsonErrorUnmarshaller(ApiGatewayException.class, null) {
            @Override
            public AmazonServiceException unmarshall(JsonNode jsonContent) throws Exception {
                return new ApiGatewayException(jsonContent.toString());
            }
        };

        this.errorResponseHandler = SdkStructuredPlainJsonFactory.SDK_JSON_FACTORY.createErrorResponseHandler(
                Collections.singletonList(defaultErrorUnmarshaller), null);
    }

    public ApiGatewayResponse execute(HttpMethodName method, String resourcePath, InputStream content) {
        final ExecutionContext executionContext = createExecutionContext();

        DefaultRequest request = prepareRequest(method, resourcePath, content);

        RequestConfig requestConfig = new AmazonWebServiceRequestAdapter(request.getOriginalRequest());

        return this.client.execute(request, responseHandler, errorResponseHandler, executionContext, requestConfig).getAwsResponse();
    }

    private DefaultRequest prepareRequest(HttpMethodName method, String resourcePath, InputStream content) {
        DefaultRequest request = new DefaultRequest(API_GATEWAY_SERVICE_NAME);
        request.setHttpMethod(method);
        request.setContent(content);
        request.setEndpoint(this.endpoint);
        request.setResourcePath(resourcePath);
        request.setHeaders(Collections.singletonMap("Content-type", "application/json"));
        return request;
    }

    private ExecutionContext createExecutionContext() {
        final ExecutionContext executionContext = ExecutionContext.builder().withSignerProvider(
                new DefaultSignerProvider(this, signer)).build();
        executionContext.setCredentialsProvider(credentials);
        return executionContext;
    }

}
