package com.piotrfilipowicz.aws;

import com.amazonaws.http.HttpMethodName;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class Runner {
    static final String AWS_IAM_ACCESS_KEY = "{YOUR_ACCESS_KEY}";
    static final String AWS_IAM_SECRET_ACCESS_KEY = "{YOUR_SECRET_ACCESS_KEY}";
    static final String AWS_REGION = "{AWS_REGION}"; //for example "eu-west-1"
    static final String AWS_API_GATEWAY_ENPOINT = "{YOUR_API_GATEWAY_ENDPOINT}"; //for example https://234n34k5678k.execute-api.eu-west-1.amazonaws.com/TEST

    static final String exampleJsonRequest = "{\n" +
            "  \"type\": \"dog\",\n" +
            "  \"price\": 249.99\n" +
            "}";

    public static void main(String... args) {
        try {
            JsonApiGatewayCaller caller = new JsonApiGatewayCaller(
                    AWS_IAM_ACCESS_KEY,
                    AWS_IAM_SECRET_ACCESS_KEY,
                    null,
                    AWS_REGION,
                    new URI(AWS_API_GATEWAY_ENPOINT)
            );

            ApiGatewayResponse response = caller.execute(HttpMethodName.POST, "/pets", new ByteArrayInputStream(exampleJsonRequest.getBytes()));

            System.out.println(response.getBody());

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

}
