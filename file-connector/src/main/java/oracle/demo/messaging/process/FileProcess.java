package oracle.demo.messaging.process;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import oracle.demo.messaging.connector.InFileMessage;
import oracle.demo.messaging.connector.OutFileMessage;


@ApplicationScoped
public class FileProcess {

    private static Logger logger = Logger.getLogger(FileProcess.class.getSimpleName());

 
    /**
     * File Connectorからファイルを読み込む #1
     * pass-by-reference=trueにして、ファイルパスを渡す
     * メソッドの中でファイルにアクセスする必要があるが、auto ackが走るとファイルを
     * 処理する前にアーカイブされてしまうので、ack strategyをmanualにして、ackの処理を
     * 後続の処理に委譲している
     */
    @Incoming("file-in")
    @Outgoing("file-process")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public Message<byte[]> in(InFileMessage<Path> message) throws IOException{
        logger.info("--- in() ---");
        byte[] payload = Files.readAllBytes(message.getPayload()); // Pathから全バイト読み込み
        logger.info("[payload]\n" + new String(payload));
        return Message.of(payload, message::ack);
    }
 
    
    /**
     * File Connectorからファイルを読み込む #2
     * pass-by-reference=falseにして、ファイルのコンテンツをbyte[]で渡すケース
     * File Connectorがファイルの内容を読み込んでこのメソッドが呼ばれるので、
     * アーカイブのタイミングを計る必要がない、したがってauto ackにしている
     */
/*    
    @Incoming("file-in2")
    @Outgoing("file-process")
    public Message<byte[]> in2(InFileMessage<byte[]> message) throws IOException{
        logger.info("--- in2() ---");
        return Message.of(message.getPayload());
    }
*/    

    /**
     * File Connectorを経由してファイルを書き出す
     */
    @Incoming("file-process")
    @Outgoing("file-out")
    public OutFileMessage<String> out(Message<byte[]> message) throws IOException{
        logger.info("--- out() ---");
        return OutFileMessage.of(new String(message.getPayload()).toUpperCase());
    }

}