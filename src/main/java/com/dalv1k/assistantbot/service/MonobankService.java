package com.dalv1k.assistantbot.service;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MonobankService {

    @Value("${monobank.token}")
    private String monobankToken;

    public String getMonobankBalance() {
        HttpResponse<JsonNode> response = Unirest.get("https://api.monobank.ua/personal/client-info")
                .header("X-Token", monobankToken).asJson();
        if (response.getStatus() == 200) {
            JSONObject object = response.getBody().getObject();
            JSONArray array = object.getJSONArray("accounts");
            for (int i = 0; i < array.length(); i++) {
                JSONObject card = array.getJSONObject(i);
                if (card.getString("id").equals("ooO7kWe53i_vDCPhrjv64g")) {
                    return "Card balance: ".concat(dot(card.getString("balance")));
                }
            }
            return "Card not found";
        } else {
            return "Error in response from Monobank: "
                    .concat(response.getBody().getObject().getString("errorDescription"));
        }
    }

    private String dot(String originalString) {
        StringBuilder newString = new StringBuilder(originalString);
        newString.insert(originalString.length() - 2, ".");
        newString.append(" UAH");
        return newString.toString();
    }

}
