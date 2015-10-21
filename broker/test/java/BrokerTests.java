import org.junit.Before;
import org.junit.Test;

/**
 * Created by anon on 21/10/2015.
 */
public class BrokerTests {
    private MessageBroker messageBroker;

    @Before
    public void setup(){
        messageBroker = new MessageBroker();
    }

    @Test
    public void testSubscriberFirst(){
        Subscriber subscriber = new Subscriber();
        Publisher publisher = new Publisher();
    }

    @Test
    public void testPublisherFirst(){
        Publisher publisher = new Publisher();

        Subscriber subscriber = new Subscriber();
    }

    @Test
    public void testMultipleSubscribers(){
        Subscriber subscriber = new Subscriber();
        Subscriber subscriber1 = new Subscriber();
        Subscriber subscriber2 = new Subscriber();
        Subscriber subscriber3 = new Subscriber();
        Subscriber subscriber4 = new Subscriber();

        Publisher publisher = new Publisher();
    }

    @Test
    public void testMultiplePublishers(){
        Publisher publisher = new Publisher();
        Publisher publisher1 = new Publisher();
        Publisher publisher2 = new Publisher();
        Publisher publisher3 = new Publisher();
        Publisher publisher4 = new Publisher();

        Subscriber subscriber = new Subscriber();
    }

    // Subs joining, receiving, all leaving, new sub, receiving
    @Test
    public void testSubsJoiningReceivingLeavingRejoining(){
        Subscriber subscriber = new Subscriber();
        Subscriber subscriber1 = new Subscriber();
        Publisher publisher = new Publisher();

        subscriber = null;
        subscriber1 = null;

        Publisher publisher1 = new Publisher();
        Publisher publisher2 = new Publisher();
        Publisher publisher3 = new Publisher();
        subscriber = new Subscriber();
    }

    // Heavy load
    @Test
    public void testThousandsOfSubscribers(){
        for(int i = 0; i < 5000; i++){
            new Subscriber();
        }

        Publisher publisher = new Publisher();
    }
}
