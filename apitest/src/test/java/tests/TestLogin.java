package tests;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
public class TestLogin {

    @Test
    public void bearerTokenAuthentication() {

        baseURI = "https://apidev2.vonix.id";

        // Step 1: Login and get the token
        RequestSpecification loginRequest = given();
        String loginPayload = "{\r\n" +
                " \"email\": \"zulma.irzamsyah+2@vonix.id\",\r\n" +
                " \"password\": \"Vonix@2023\"\r\n" +
                "}";

        loginRequest.header("Content-Type", "application/json");
        Response responseFromGenerateToken = loginRequest.body(loginPayload).post("/v1/auth/login");
        responseFromGenerateToken.prettyPrint();

        String jsonString = responseFromGenerateToken.getBody().asString();
        String tokenGenerated = JsonPath.from(jsonString).get("accessToken");

        // Step 2: Create Virtual Account
        RequestSpecification createVARequest = given();
        createVARequest.header("Authorization", "Bearer " + tokenGenerated)
                .header("Content-Type", "application/json");

        String createVAPayload = "{\n" +
                "    \"bankCode\": \"SAHABAT_SAMPOERNA\"\n" +
                "}";

        Response createVAResponse = createVARequest.body(createVAPayload).post("/v2/portfolio/deposit/virtual-account");
        Assert.assertEquals(201, createVAResponse.getStatusCode());
        createVAResponse.prettyPrint();

        String jsonStringVA = createVAResponse.getBody().asString();
        String VANumber = JsonPath.from(jsonStringVA).get("virtualAccountNumber");
        String XenditID = JsonPath.from(jsonStringVA).get("xenditVirtualAccountId");
        String externalId = JsonPath.from(jsonStringVA).get("externalId");

        // Step 3: Create Callback Xendit Virtual Account
        RequestSpecification callbackRequest = given();
        callbackRequest.header("Authorization", "Bearer " + tokenGenerated)
                .header("Content-Type", "application/json")
                .header("x-callback-token", "8Dmba1gwWSA09jSAhvX6l128ld07L0XeWJtESYdDKCOSd4Zq");

        String callbackPayload = "{\n" +
                "    \"id\": \"609114073314562\",\n" +
                "    \"amount\": 10000,\n" +
                "    \"created\": \"2022-10-03T07:24:37.294Z\",\n" +
                "    \"updated\": \"2022-10-03T07:24:40.251Z\",\n" +
                "    \"currency\": \"IDR\",\n" +
                "    \"owner_id\": \"62ce42a5c028e86059a1d8f3\",\n" +
                "    \"bank_code\": \"SAHABAT_SAMPOERNA\",\n" +
                "    \"payment_id\": \"1026392071889567744_1664781877048\",\n" +
                "    \"external_id\": \"614033974691072\",\n" +
                "    \"merchant_code\": \"8808\",\n" +
                "    \"account_number\": \"4010299993645415\",\n" +
                "    \"transaction_timestamp\": \"2022-10-03T07:24:37.048Z\",\n" +
                "    \"callback_virtual_account_id\": \"584e6bef-9ab9-4b3f-a9a1-62bacf1e165e\"\n" +
                "}";

        Response createCallbackXenditResponse = callbackRequest.body(callbackPayload).post("/v2/portfolio/deposit/callbacks/virtual-account");
        createCallbackXenditResponse.prettyPrint();
    }

}
