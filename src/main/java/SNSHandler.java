import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SNSHandler implements RequestHandler<SNSEvent, String> {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private DynamoDB dynamoDb;
    String DYNAMODB_TABLE_NAME = "csye6225";
    Regions REGION = Regions.US_EAST_1;

    @Override
    public String handleRequest(SNSEvent event, Context context)
    {
        LambdaLogger logger = context.getLogger();
        String response = new String("200 OK");
        // log execution details
        Util.logEnvironment(event, context, gson);
      //  logger.log("messageType : "+ event);

        String message=  event.getRecords().get(0).getSNS().getMessage();

        String[] Messages = message.split("\\|");

        String messageType = Messages[0];

        String answerId,questionId,username,questionText,answertext,HTMLBODY;
        logger.log("messageType : "+ messageType);
        answerId = Messages[1];
        questionId = Messages[2];
        questionText=Messages[3];
        answertext = Messages[4];
        username = Messages[5];

        switch(messageType) {
            case "addAnswer":

                logger.log("answerId : "+ answerId);
                logger.log("questionId : "+ questionId);
                logger.log("questionText : "+ questionText);
                logger.log("answertext : "+ answertext);
                logger.log("username : "+ username);

                String url ="http://prod.dhavalpandya.me/webapp/v1/question/"+questionId+"/answer/"+answerId;
                 HTMLBODY = "<h1>New Answer is Added </h1>"
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
                 HTMLBODY = "<h1>Answer is updated for "+questionText +"</h1>"
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
                HTMLBODY = "<h1>File uploaded  </h1>"
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

               // url ="http://prod.dhavalpandya.me/v1/question/"+questionId+"/answerId/"+answerId;
                HTMLBODY = "<h1>Answer is deleted"+"</h1>"
                        + "<p><b>Question Text:</b> "+questionText;



                logger.log("HTML body : "+ HTMLBODY);


                Util.sendEmail(username,"noreply@prod.dhavalpandya.me",
                        "Answer deleted",HTMLBODY,"");
                logger.log("Email sent to "+username);
                break;
            default:
                // code block
        }



        logger.log("message value  : " +message);
        Util.logEnvironment(event, context, gson);

        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        client.setRegion(Region.getRegion(REGION));
        this.dynamoDb = new DynamoDB(client);

        this.dynamoDb.getTable(DYNAMODB_TABLE_NAME)
                .putItem(
                        new PutItemSpec().withItem(new Item()
                                .withString("id", "kjshdfjksgfjkgh")
                                .withString("firstName", "dhaval")
                                .withString("lastName", "pandya")));

        return response;
    }
}
