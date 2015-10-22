import java.sql.Timestamp;

/**
 * Created by Jonathan Sterling on 21/10/2015.
 */
public class MessageWrapper {
    private final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    private String dataType;
    private String message;

    public MessageWrapper(){
        // No args constructor for Jackson
    }

    public MessageWrapper(String dataType, String message){
        this.dataType = dataType;
        this.message = message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getDataType() {
        return dataType;
    }
}
