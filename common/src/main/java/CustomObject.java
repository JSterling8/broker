import java.io.Serializable;
import java.util.UUID;

/**
 * Created by anon on 21/10/2015.
 */
public class CustomObject implements Serializable{
    private String message;
    private UUID id;

    public CustomObject(){
        // No args constructor for jackson...
    }

    public CustomObject(String message, UUID id){
        this.message = message;
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
