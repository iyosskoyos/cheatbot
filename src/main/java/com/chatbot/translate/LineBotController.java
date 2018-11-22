
package com.chatbot.translate;

import com.google.gson.Gson;
import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import retrofit2.Response;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;

@RestController
@RequestMapping(value="/linebot")
public class LineBotController
{
    @Autowired
    @Qualifier("com.linecorp.channel_secret")
    String lChannelSecret;

    @Autowired
    @Qualifier("com.linecorp.channel_access_token")
    String lChannelAccessToken;

    private static final String CLIENT_ID = "FREE_TRIAL_ACCOUNT";
    private static final String CLIENT_SECRET = "PUBLIC_SECRET";
   // private static final String ENDPOINT = "http://api.whatsmate.net/v1/translation/translate";
    private boolean isBos = false;
    HashMap<String, String> hm = new HashMap<String, String>();
    @RequestMapping(value="/callback", method= RequestMethod.POST)

    public ResponseEntity<String> callback(
            @RequestHeader("X-Line-Signature") String aXLineSignature,
            @RequestBody String aPayload) throws IOException, URISyntaxException, SQLException {
        final String text=String.format("The Signature is: %s",
                (aXLineSignature!=null && aXLineSignature.length() > 0) ? aXLineSignature : "N/A");
        System.out.println(text);
        final boolean valid=new LineSignatureValidator(lChannelSecret.getBytes()).validateSignature(aPayload.getBytes(), aXLineSignature);
        System.out.println("The signature is: " + (valid ? "valid" : "tidak valid"));
        if(aPayload!=null && aPayload.length() > 0)
        {
            System.out.println("Payload: " + aPayload);
        }
        Gson gson = new Gson();
        Payload payload = gson.fromJson(aPayload, Payload.class);

        String msgText = " ";
        String idTarget = " ";
        String eventType = payload.events[0].type;

        if (eventType.equals("join")){
            if (payload.events[0].source.type.equals("group")){
                replyToUser(payload.events[0].replyToken, "Hello Group");
            }
            if (payload.events[0].source.type.equals("room")){
                replyToUser(payload.events[0].replyToken, "Hello Room");
            }
        } else if (eventType.equals("message")){
            if (payload.events[0].source.type.equals("group")){
                idTarget = payload.events[0].source.groupId;
            } else if (payload.events[0].source.type.equals("room")){
                idTarget = payload.events[0].source.roomId;
            } else if (payload.events[0].source.type.equals("user")){
                idTarget = payload.events[0].source.userId;
            }

            if (!payload.events[0].message.type.equals("text")){
                replyToUser(payload.events[0].replyToken, "Unknown message");
            } else {
                msgText = payload.events[0].message.text;
                //msgText = msgText.toLowerCase();

                if (!msgText.contains("bot leave")){
                    if (msgText.equalsIgnoreCase("noboss")){
                        isBos = false;
                        replyToUser(payload.events[0].replyToken, "OK");
                    }
                    else if(msgText.equalsIgnoreCase("boss")){
                        isBos = true;
                        replyToUser(payload.events[0].replyToken,randomText());
                    }

                    if(isBos == false){
                        if(msgText.contains("Save") || msgText.contains("save")){
                            saveMsg(msgText,payload);
                            replyToUser(payload.events[0].replyToken,"OK");
                        }
                        else if(msgText.contains("Load") || msgText.contains("load")){
                            String res = showMsg(msgText,payload);
                            if(res!=null){
                                replyToUser(payload.events[0].replyToken,res);
                            }
                        }
                        else{
                            replyToUser(payload.events[0].replyToken, "Value tidak ditemukan");
                        }
                    }else{

                    }

                    //String fromLang = "id";
                    //String toLang = "en";
                    //String tex = "Let's have some fun!";

                    //translate(fromLang, toLang, msgText, payload.events[0].replyToken);

                    //replyToUser(payload.events[0].replyToken, msgText);
                    /*try {
                        getMessageData(msgText, idTarget);
                    } catch (IOException e) {
                        System.out.println("Exception is raised ");
                        e.printStackTrace();
                    }
                    */
                } else {
                    if (payload.events[0].source.type.equals("group")){
                        leaveGR(payload.events[0].source.groupId, "group");
                    } else if (payload.events[0].source.type.equals("room")){
                        leaveGR(payload.events[0].source.roomId, "room");
                    }
                }

            }
        }

        return new ResponseEntity<String>(HttpStatus.OK);
    }

//    private void translate(String fromLang, String toLang, String text, String payload) throws IOException {
//        // TODO: Should have used a 3rd party library to make a JSON string from an object
//        String jsonPayload = new StringBuilder()
//                .append("{")
//                .append("\"fromLang\":\"")
//                .append(fromLang)
//                .append("\",")
//                .append("\"toLang\":\"")
//                .append(toLang)
//                .append("\",")
//                .append("\"text\":\"")
//                .append(text)
//                .append("\"")
//                .append("}")
//                .toString();
//
//        URL url = new URL(ENDPOINT);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setDoOutput(true);
//        conn.setRequestMethod("POST");
//        conn.setRequestProperty("X-WM-CLIENT-ID", CLIENT_ID);
//        conn.setRequestProperty("X-WM-CLIENT-SECRET", CLIENT_SECRET);
//        conn.setRequestProperty("Content-Type", "application/json");
//
//        OutputStream os = conn.getOutputStream();
//        os.write(jsonPayload.getBytes());
//        os.flush();
//        os.close();
//
//        int statusCode = conn.getResponseCode();
//        System.out.println("Status Code: " + statusCode);
//        BufferedReader br = new BufferedReader(new InputStreamReader(
//                (statusCode == 200) ? conn.getInputStream() : conn.getErrorStream()
//        ));
//        String output;
//        while ((output = br.readLine()) != null) {
//            replyToUser(payload, output);
//            //System.out.println(output);
//        }
//        conn.disconnect();
//    }

    private void getMessageData(String message, String targetID) throws IOException{
        if (message!=null){
            pushMessage(targetID, message);
        }
    }

    private void replyToUser(String rToken, String messageToUser){
        TextMessage textMessage = new TextMessage(messageToUser);
        ReplyMessage replyMessage = new ReplyMessage(rToken, textMessage);
        try {
            Response<BotApiResponse> response = LineMessagingServiceBuilder
                    .create(lChannelAccessToken)
                    .build()
                    .replyMessage(replyMessage)
                    .execute();
            System.out.println("Reply Message: " + response.code() + " " + response.message());
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }
    }

    private void pushMessage(String sourceId, String txt){
        TextMessage textMessage = new TextMessage(txt);
        PushMessage pushMessage = new PushMessage(sourceId,textMessage);
        try {
            Response<BotApiResponse> response = LineMessagingServiceBuilder
                    .create(lChannelAccessToken)
                    .build()
                    .pushMessage(pushMessage)
                    .execute();
            System.out.println(response.code() + " " + response.message());
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }
    }

    private void leaveGR(String id, String type){
        try {
            if (type.equals("group")){
                Response<BotApiResponse> response = LineMessagingServiceBuilder
                        .create(lChannelAccessToken)
                        .build()
                        .leaveGroup(id)
                        .execute();
                System.out.println(response.code() + " " + response.message());
            } else if (type.equals("room")){
                Response<BotApiResponse> response = LineMessagingServiceBuilder
                        .create(lChannelAccessToken)
                        .build()
                        .leaveRoom(id)
                        .execute();
                System.out.println(response.code() + " " + response.message());
            }
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }
    }

    public String randomText(){
        return "What is Lorem Ipsum?Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.Why do we use it?It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors now use Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites still in their infancy. Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like).";
    }

    private void saveMsg(String perintah, Payload payload) throws URISyntaxException, SQLException {
        String[] data = perintah.split(" ");
        if(data.length<3){
            replyToUser(payload.events[0].replyToken, "Harap masukan pesan degan format '[save] [key] [value]'");
        }
        else{
            String id = payload.events[0].source.userId;
            String key = data[1];
            String value = data[2];
            insertData(id, key, value);
        }
    }

    public void insertData(String id, String key, String value) throws URISyntaxException, SQLException {
        String temp = getData(id, key);
        if(temp!=null || temp!= ""){
            PreparedStatement st = getConnection().prepareStatement("UPDATE simpanan SET value = ? WHERE id_person = ? AND key = ?;");
            st.setString(1, value);
            st.setString(2, id);
            st.setString(3, key);
            st.executeUpdate();
            st.close();
        }
        else{
            PreparedStatement st = getConnection().prepareStatement("INSERT INTO simpanan (id_person,key,value)" + "\n" + " VALUES(?,?,?);");
            st.setString(1, id);
            st.setString(2, key);
            st.setString(3, value);
            st.executeUpdate();
            st.close();
        }
    }

    public String getData(String id, String value) throws URISyntaxException, SQLException{
        PreparedStatement st = getConnection().prepareStatement("select value from simpanan where id_person = ? AND key = ?;");
        st.setString(1, id);
        st.setString(2, value);
        ResultSet rs= st.executeQuery();
        rs.next();
        String hasil = (String)rs.getObject(1);
        //System.out.println(rs.getObject(1));
        st.close();
        return hasil;
    }

    private String showMsg(String perintah, Payload payload) throws URISyntaxException, SQLException {
        String[] data = perintah.split(" ");
        if(data.length<2){
            replyToUser(payload.events[0].replyToken, "Harap masukan pesan degan format '[load] [value]'");
        }
        String id = payload.events[0].source.userId;
        String key=data[1];
        //String val = hmap.get(data[1]+id);
        String val = getData(id,key);
        return val;
    }

    private static Connection getConnection() throws URISyntaxException, SQLException {
        Connection connection=null;

        try {
            connection = DriverManager.getConnection("jdbc:postgresql://ec2-54-197-249-140.compute-1.amazonaws.com:5432/dap2hgf18m4g59", "qsrokubscletbs", "a8c2f03d1c14ca1291b5e6cf38f3f8a4a551bb32cc5895f4a2c085ade07726aa");
            System.out.println("Java JDBC PostgreSQL Example");
            // When this class first attempts to establish a connection, it automatically loads any JDBC 4.0 drivers found within
            // the class path. Note that your application must manually load any JDBC drivers prior to version 4.0.
//			Class.forName("org.postgresql.Driver");

            System.out.println("Connected to PostgreSQL database!");
            return connection;
        } /*catch (ClassNotFoundException e) {
			System.out.println("PostgreSQL JDBC driver not found.");
			e.printStackTrace();
		}*/ catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }
        String dbUrl = "postgres://qsrokubscletbs:a8c2f03d1c14ca1291b5e6cf38f3f8a4a551bb32cc5895f4a2c085ade07726aa@ec2-54-197-249-140.compute-1.amazonaws.com:5432/dap2hgf18m4g59";

        String username ="qsrokubscletbs";

        String password="a8c2f03d1c14ca1291b5e6cf38f3f8a4a551bb32cc5895f4a2c085ade07726aa";

        return connection;
    }
}
