import java.sql.Timestamp;

/**
 * Created by anon on 21/10/2015.
 */
public class MessageWrapper {
    private final Timestamp timestamp;
    private final String dataType;
    private final String message;

    public MessageWrapper(String dataType, String message){
        this.dataType = dataType;
        this.message = message;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
}
