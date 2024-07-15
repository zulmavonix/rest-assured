package tests;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class ApiAutomation {

    private static final String BASE_URI = "https://apidev2.vonix.id";
    private static final String EMAIL = "zulma.irzamsyah+foreigner@vonix.id";
    private static final String PASSWORD = "Vonix@2023";
    private static final String CALLBACK_TOKEN = "8Dmba1gwWSA09jSAhvX6l128ld07L0XeWJtESYdDKCOSd4Zq";
    private static final String BANK_CODE_VA = "MANDIRI";

    private static final Integer AMOUNT_BANK = 2000000;

    public static void main(String[] args) {
        String token = generateBearerToken();
        Map<String, String> virtualAccountDetails = createVirtualAccount(token);
        createCallbackXenditVirtualAccount(token, virtualAccountDetails);
    }
    @Test
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

    @Test
    private static Map<String, String> createVirtualAccount(String token) {
        RequestSpecification request = RestAssured.given();
        request.header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json");

        String payload = "{\n" +
                "    \"bankCode\": \"" + BANK_CODE_VA + "\"\n" +
                "}";

        Response response = request.body(payload).post("/v2/portfolio/deposit/virtual-account");
        Assert.assertEquals(201, response.getStatusCode());
        response.prettyPrint();

        String jsonString = response.getBody().asString();
        Map<String, String> accountDetails = new HashMap<>();
        accountDetails.put("callbackXenditID", (String) JsonPath.from(jsonString).get("xenditVirtualAccountId"));
        accountDetails.put("externalId", (String) JsonPath.from(jsonString).get("externalId"));
        accountDetails.put("accountNumber", (String) JsonPath.from(jsonString).get("virtualAccountNumber"));

        return accountDetails;
    }

    @Test
    private static void createCallbackXenditVirtualAccount(String token, Map<String, String> accountDetails) {
        RequestSpecification request = RestAssured.given();
        request.header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("x-callback-token", CALLBACK_TOKEN);

        String payload = "{\n" +
                "    \"id\": \"609114073314562\",\n" +
                "    \"amount\": " + AMOUNT_BANK + ",\n" +
                "    \"created\": \"2022-10-03T07:24:37.294Z\",\n" +
                "    \"updated\": \"2022-10-03T07:24:40.251Z\",\n" +
                "    \"currency\": \"IDR\",\n" +
                "    \"owner_id\": \"62ce42a5c028e86059a1d8f3\",\n" +
                "    \"bank_code\": \"" + BANK_CODE_VA + "\",\n" +
                "    \"payment_id\": \"1026392071889567744_1664781877048\",\n" +
                "    \"external_id\": \"" + accountDetails.get("externalId") + "\",\n" +
                "    \"merchant_code\": \"8808\",\n" +
                "    \"account_number\": \"" + accountDetails.get("accountNumber") + "\",\n" +
                "    \"transaction_timestamp\": \"2022-10-03T07:24:37.048Z\",\n" +
                "    \"callback_virtual_account_id\": \"" + accountDetails.get("callbackXenditID") + "\"\n" +
                "}";

        Response response = request.body(payload).post("/v2/portfolio/deposit/callbacks/virtual-account");
        response.prettyPrint();
    }
}
