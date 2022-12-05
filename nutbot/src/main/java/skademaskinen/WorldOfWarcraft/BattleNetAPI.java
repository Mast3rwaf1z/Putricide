package skademaskinen.WorldOfWarcraft;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import skademaskinen.Bot;
import skademaskinen.Utils.Shell;

public class BattleNetAPI {
    private static HttpClient httpClient = HttpClients.createDefault();
    private static String token;

    public Character getCharacter(String name, String realm){
        return new Character(name, realm);
    }

    public static void init(String OauthToken){
        token = OauthToken;
    }

    public static String getToken() {
        return token;
    }

    public static HttpClient getHttpClient(){
        return httpClient;
    }

    public static boolean verifyCharacter(String name, String server) {
        //if the json key 'code' = 404, then this method returns false
        //if its null then return true and handle the loss of a valid member
        String region = Bot.getConfig().get("region");
        String url = "https://"+region+".api.blizzard.com/profile/wow/character/"+server+"/"+name+"?namespace=profile-"+region+"&locale=en_GB&access_token="+token;
        //Shell.println(url);
        HttpGet request = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(request);
            request.releaseConnection();
            if(response.getStatusLine().getStatusCode() == 404){
                return false;
            }
            else{
                return true;
            }
        } catch (Exception e) {
            Shell.exceptionHandler(e);
        }
        
        return false;
    }

    public static JSONObject executeSubRequest(String url){
        String region = Bot.getConfig().get("region");
        HttpGet request = new HttpGet(url+"&access_token="+token);
        try {
            HttpResponse response = httpClient.execute(request);
            String responseData = EntityUtils.toString(response.getEntity());
            request.releaseConnection();
            return new JSONObject(responseData);
        } catch (Exception e) {
            Shell.exceptionHandler(e);
        }

        return null;

    }

    
    public static JSONObject getCharacterData(String name, String server){
        String region = Bot.getConfig().get("region");
        String url = "https://"+region+".api.blizzard.com/profile/wow/character/"+server+"/"+name+"?namespace=profile-"+region+"&locale=en_GB&access_token="+token;

        HttpGet request = new HttpGet(url);

        try {
            HttpResponse response = httpClient.execute(request);
            String responseData = EntityUtils.toString(response.getEntity());
            request.releaseConnection();
            return new JSONObject(responseData);
        } catch (Exception e) {
            Shell.exceptionHandler(e);
        }

        return null;

    }
}