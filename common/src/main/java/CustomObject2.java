import java.util.UUID;

/**
 * Created by anon on 22/10/2015.
 */
public class CustomObject2 {
    private String aThirdField;
    private String message;
    private UUID id;

    public CustomObject2(){
        // No args constructor for jackson...
    }

    public CustomObject2(String message, UUID id, String aThirdField){
        this.message = message;
        this.id = id;
        this.aThirdField = aThirdField;
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

    public String getaThirdField() {
        return aThirdField;
    }

    public void setaThirdField(String aThirdField) {
        this.aThirdField = aThirdField;
    }
}
