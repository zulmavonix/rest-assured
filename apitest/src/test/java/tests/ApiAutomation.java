package tests;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;

public class ApiAutomation {

    private static final String BASE_URI = "https://apidev2.vonix.id";
    private static final String EMAIL = "zulma.irzamsyah+2@vonix.id";
    private static final String PASSWORD = "Vonix@2023";
    private static final String CALLBACK_TOKEN = "8Dmba1gwWSA09jSAhvX6l128ld07L0XeWJtESYdDKCOSd4Zq";

    public static void main(String[] args) {
        String token = generateBearerToken();
        createVirtualAccount(token);
        createCallbackXenditVirtualAccount(token);
    }

    private static String generateBearerToken() {
        RestAssured.baseURI = BASE_URI;

        RequestSpecification request = RestAssured.given();
        String payload = "{\r\n" +
                " \"email\": \"" + EMAIL + "\",\r\n" +
                " \"password\": \"" + PASSWORD + "\"\r\n" +
                "}";

        request.header("Content-Type", "application/json");
        Response response = request.body(payload).post("/v1/auth/login");
        response.prettyPrint();

        String jsonString = response.getBody().asString();
        return JsonPath.from(jsonString).get("accessToken");
    }

    private static void createVirtualAccount(String token) {
        RequestSpecification request = RestAssured.given();
        request.header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json");

        String payload = "{\n" +
                "    \"bankCode\": \"SAHABAT_SAMPOERNA\"\n" +
                "}";

        Response response = request.body(payload).post("/v2/portfolio/deposit/virtual-account");
        Assert.assertEquals(201, response.getStatusCode());
        response.prettyPrint();
    }

    private static void createCallbackXenditVirtualAccount(String token) {
        RequestSpecification request = RestAssured.given();
        request.header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("x-callback-token", CALLBACK_TOKEN);

        String payload = "{\n" +
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

        Response response = request.body(payload).post("/v2/portfolio/deposit/callbacks/virtual-account");
        response.prettyPrint();
    }
}