import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SNSHandler implements RequestHandler<SNSEvent, String> {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static Map<String, String> env =System.getenv();
    private DynamoDB dynamoDb;
    String DYNAMODB_TABLE_NAME = env.get("dynamodbTable");
    Regions REGION = Regions.US_EAST_1;

    @Override
    public String handleRequest(SNSEvent event, Context context)
    {
        LambdaLogger logger = context.getLogger();
        String response = new String("200 OK");
        // log execution details
        Util.logEnvironment(event, context, gson);
      //  logger.log("messageType : "+ event);

        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        client.setRegion(Region.getRegion(REGION));
        this.dynamoDb = new DynamoDB(client);

        String message=  event.getRecords().get(0).getSNS().getMessage();
        logger.log("message value  : " +message);
        String[] Messages = message.split("\\|");

        String messageType = Messages[0];

        String answerId,questionId,username,questionText,answertext,HTMLBODY;
        logger.log("messageType : "+ messageType);
        answerId = Messages[1];
        questionId = Messages[2];
        questionText=Messages[3];
        answertext = Messages[4];
        username = Messages[5];

        String shaString =  answerId+questionId+answertext+username;



        String sha256hex = DigestUtils.sha256Hex(shaString);
        GetItemRequest request = null;
        HashMap<String, AttributeValue> key_to_get =
                new HashMap<String,AttributeValue>();

        key_to_get.put("id", new AttributeValue(sha256hex));

        request = new GetItemRequest()
                .withKey(key_to_get)
                .withTableName(DYNAMODB_TABLE_NAME);

        final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();

//    if(!messageType.equals("deleteAnswer")) {
        try {
            Map<String, AttributeValue> returned_item =
                    ddb.getItem(request).getItem();
            if (returned_item != null) {
                logger.log("sha is already exists-->" + sha256hex);
                return response;
            } else {
                logger.log("sha is not already exists-->" + sha256hex);

                this.dynamoDb.getTable(DYNAMODB_TABLE_NAME)
                        .putItem(
                                new PutItemSpec().withItem(new Item()
                                        .withString("id", sha256hex)
                                        .withString("requesttype", messageType)
                                        .withString("questionid", questionId)
                                        .withString("answerid", answerId)
                                        .withString("answertext", answertext)
                                        .withString("emailsendto", username)
                                ));

            }
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
//    }
        String url="";
        String questionUrl="";
        switch(messageType) {
            case "addAnswer":

                logger.log("answerId : "+ answerId);
                logger.log("questionId : "+ questionId);
                logger.log("questionText : "+ questionText);
                logger.log("answertext : "+ answertext);
                logger.log("username : "+ username);

                 url ="http://prod.dhavalpandya.me/webapp/v1/question/"+questionId+"/answer/"+answerId;
                 questionUrl ="http://prod.dhavalpandya.me/webapp/v1/question/"+questionId;

                HTMLBODY = "<h1>New Answer is Added </h1>"
                        + "<p><b>Question URL:</b> "+questionUrl +" <a href='"+questionUrl+"'>"
                        + "<p><b>Question Text:</b> "+questionText
                        + "<p><b>Answer Text:</b> "+answertext
                        + "<p><b>Answer URL:</b>"+url +" <a href='"+url+"'>" ;

                logger.log("HTML body : "+ HTMLBODY);


                Util.sendEmail(username,"noreply@prod.dhavalpandya.me",
                        "New Answer Added",HTMLBODY,"");
                logger.log("Email sent to "+username);
                break;
            case "updateAnswer":

                logger.log("answerId : "+ answerId);
                logger.log("questionId : "+ questionId);
                logger.log("questionText : "+ questionText);
                logger.log("answertext : "+ answertext);
                logger.log("username : "+ username);

                 url ="http://prod.dhavalpandya.me/webapp/v1/question/"+questionId+"/answer/"+answerId;
                 questionUrl ="http://prod.dhavalpandya.me/webapp/v1/question/"+questionId;

                HTMLBODY = "<h1>Answer is updated for "+questionText +"</h1>"
                        + "<p><b>Question URL:</b> "+questionUrl +" <a href='"+questionUrl+"'>"
                        + "<p><b>Question Text:</b> "+questionText
                        + "<p><b>Answer Text:</b> "+answertext
                        + "<p><b>Answer URL:</b>"+url +" <a href='"+url+"'>" ;

                logger.log("HTML body : "+ HTMLBODY);


                Util.sendEmail(username,"noreply@prod.dhavalpandya.me",
                        "Answer updated",HTMLBODY,"");
                logger.log("Email sent to "+username);

                break;
            case "uploadFileAnswer":

                logger.log("answerId : "+ answerId);
                logger.log("questionId : "+ questionId);
                logger.log("questionText : "+ questionText);
                logger.log("answertext : "+ answertext);
                logger.log("username : "+ username);

                url ="http://prod.dhavalpandya.me/webapp/v1/question/"+questionId+"/answer/"+answerId;
                questionUrl ="http://prod.dhavalpandya.me/webapp/v1/question/"+questionId;

                HTMLBODY = "<h1>File uploaded  </h1>"
                        + "<p><b>Question URL:</b> "+questionUrl +" <a href='"+questionUrl+"'>"
                        + "<p><b>Question Text:</b> "+questionText
                        + "<p><b>Answer Text:</b> "+answertext
                        + "<p><b>Answer URL:</b>"+url +" <a href='"+url+"'>" ;

                logger.log("HTML body : "+ HTMLBODY);


                Util.sendEmail(username,"noreply@prod.dhavalpandya.me",
                        "File upload",HTMLBODY,"");
                logger.log("Email sent to "+username);

                break;
            case "deleteAnswer":
                logger.log("answerId : "+ answerId);
                logger.log("questionId : "+ questionId);
                logger.log("questionText : "+ questionText);
                logger.log("answertext : "+ answertext);
                logger.log("username : "+ username);
                questionUrl ="http://prod.dhavalpandya.me/webapp/v1/question/"+questionId;

               // url ="http://prod.dhavalpandya.me/v1/question/"+questionId+"/answerId/"+answerId;
                HTMLBODY = "<h1>Answer is deleted"+"</h1>"
                        + "<p><b>Question URL:</b> "+questionUrl +" <a href='"+questionUrl+"'>"
                        + "<p><b>Question Text:</b> "+questionText
                        + "<p><b>Answer text was:</b> "+answertext;



                logger.log("HTML body : "+ HTMLBODY);


                Util.sendEmail(username,"noreply@prod.dhavalpandya.me",
                        "Answer deleted",HTMLBODY,"");
                logger.log("Email sent to "+username);
                this.dynamoDb.getTable(DYNAMODB_TABLE_NAME)
                        .putItem(
                                new PutItemSpec().withItem(new Item()
                                        .withString("id", sha256hex)
                                        .withString("requesttype", messageType)
                                        .withString("questionid", questionId)
                                        .withString("answerid", answerId)
                                        .withString("answertext", answertext)
                                        .withString("emailsendto", username)
                                ));

                break;
            default:
                // code block
        }

        return response;
    }
}
