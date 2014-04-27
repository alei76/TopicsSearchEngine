package infrascructure.data.email;

import infrascructure.data.util.Trace;

import javax.mail.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Properties;

import static infrascructure.data.email.EmailConfig.*;

/**
 * Created with IntelliJ IDEA.
 * User: shredinger
 * Date: 2/2/14
 * Time: 9:49 PM
 * Project: IntelligentSearch
 */
public class EmailReaderImpl implements EmailReader{

    private final Properties properties;
    private Folder inbox;
    private int messageCount;
    private int currentId;
    private volatile boolean emailInitialized;

    public EmailReaderImpl(Properties properties) throws IOException, MessagingException {
        this.properties = properties;
    }

    public Iterator<Message> getMessagesIterator(){
        return new Iterator<Message>() {
            @Override
            public boolean hasNext() {
                if(!emailInitialized){
                    init();
                }
                return currentId > 1;
            }

            @Override
            public Message next() {
                return readNextMessage();
            }
        };
    }


    private Message readNextMessage(){
        try {
            if(!emailInitialized){
                init();
            }
            return inbox.getMessage(--currentId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private void init() throws RuntimeException {
        try{
            String emailsDir = properties.getProperty(EMAIL_STORE_DIRECTORY);
            createSourceDirectory(emailsDir);

            Session session = Session.getInstance(properties, null);
            Store store = session.getStore();


            String host = properties.getProperty(EMAIL_HOST);
            String password = properties.getProperty(EMAIL_PASSWORD);
            String account = properties.getProperty(EMAIL_ACCOUNT);

            store.connect(host, account, password);
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            messageCount = inbox.getMessageCount();
            currentId = messageCount - 1;
            emailInitialized = true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void createSourceDirectory(String emailsDir) throws IOException {
        Path path = Paths.get(emailsDir);
        if(!Files.exists(path)){
            Files.createDirectory(path);
        }
    }

    public static void main(String[] args) throws IOException, MessagingException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("IntelligentSearch/src/main/resources/emails/email.properties"));
        EmailReaderImpl emailReader = new EmailReaderImpl(properties);
        Iterator<Message> messagesIterator = emailReader.getMessagesIterator();
        while (messagesIterator.hasNext()){
            Message message = messagesIterator.next();
            Trace.trace(message.getSubject());
        }

    }
}
