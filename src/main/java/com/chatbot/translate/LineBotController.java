
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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
    //private static final String ENDPOINT = "http://api.whatsmate.net/v1/translation/translate";
    private boolean statusBos = false;

    @RequestMapping(value="/callback", method=RequestMethod.POST)
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

        if (eventType.equals("message")){
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
                    if(msgText.equalsIgnoreCase("no boss")){
                        statusBos = false;
                        replyToUser(payload.events[0].replyToken, "OK");
                    }
                    else if(msgText.equalsIgnoreCase("Boss")){
                        statusBos = true;
                        replyToUser(payload.events[0].replyToken, randomText());;
                    }

                    if(statusBos==false){
                        if(msgText.contains("Save")||msgText.contains("save")){
                            saveMsg(msgText, payload);
                            replyToUser(payload.events[0].replyToken, "Ok");
                        }
                        else if(msgText.contains("Load")||msgText.contains("load")){
                            String hasil = loadMsg(msgText, payload);
                            if(hasil!= null){
                                replyToUser(payload.events[0].replyToken, hasil);
                            }
                            else{
                                replyToUser(payload.events[0].replyToken, "Value tidak ditemukan");
                            }
                        }
                        else{


                        }
                    }
                }
                else {
                    replyToUser(payload.events[0].replyToken, "Dadah!");
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

    private String randomText() {
        return "A short story is a piece of prose fiction that typically can be read in one sitting and focuses on a self-contained incident or series of linked incidents, with the intent of evoking a \"single effect\" or mood, however there are many exceptions to this.\n" +
                "\n" +
                "A dictionary definition is \"an invented prose narrative shorter than a novel usually dealing with a few characters and aiming at unity of effect and often concentrating on the creation of mood rather than plot.\"[1]\n" +
                "\n" +
                "The short story is a crafted form in its own right. Short stories make use of plot, resonance, and other dynamic components as in a novel, but typically to a lesser degree. While the short story is largely distinct from the novel or novella (a shorter novel), authors generally draw from a common pool of literary techniques.\n" +
                "\n" +
                "Short story writers may define their works as part of the artistic and personal expression of the form. They may also attempt to resist categorization by genre and fixed formation.\n" +
                "\n" +
                "Short stories have deep roots and the power of short fiction has been recognised in modern society for hundreds of years. The short form is, conceivably, more natural to us than longer forms. We are drawn to short stories as the well-told story, and as William Boyd, the award-winning British author and short story writer has said:";
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

    public static void insertData(String id, String key, String value) throws URISyntaxException, SQLException {

        String query = "INSERT INTO dataSimpanan (id_person,key,value) VALUES(?,?,?);";
        PreparedStatement st = getConnection().prepareStatement(query);
        st.setString(1, id);
        st.setString(2, key);
        st.setString(3, value);
        st.executeUpdate();
        st.close();
        System.out.println("haha");
    }

    private String loadMsg(String perintah, Payload payload) throws URISyntaxException, SQLException {
        String[] data = perintah.split(" ");
        if(data.length<2){
            replyToUser(payload.events[0].replyToken, "Error input!");
        }
        String id = payload.events[0].source.userId;
        String key=data[1];
        String val = getData(id,key);
        return val;
    }


    public static String getData(String id, String value) throws URISyntaxException, SQLException {
        PreparedStatement st = getConnection().prepareStatement("select value from dataSimpanan where id_person = ? AND key = ? order by id desc limit 1;");
        st.setString(1, id);
        st.setString(2, value);
        String tmp = "";
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            tmp = rs.getString(1);
        }
        String hasil = tmp;
        //System.out.println(rs.getObject(1));
        st.close();
        return hasil;
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

    private static Connection getConnection() throws URISyntaxException, SQLException {

        Connection connection=null;
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://ec2-54-197-249-140.compute-1.amazonaws.com:5432/dap2hgf18m4g59", "qsrokubscletbs", "a8c2f03d1c14ca1291b5e6cf38f3f8a4a551bb32cc5895f4a2c085ade07726aa");

            System.out.println("Connected to PostgreSQL database!");
            return connection;
        }  catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }

        return connection;
    }
}