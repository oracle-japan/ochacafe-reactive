package oracle.demo.reactive;

import java.util.concurrent.CompletableFuture;

public class FlowMessage {
    private final String request;
    private final CompletableFuture<String> response = new CompletableFuture<>();

    public FlowMessage(String request){
        this.request = request;
    }

    public String getRequest(){
        return request;
    }

    public void complete(String s){
        response.complete(s);
    }

    public void completeExceptionally(Throwable t){
        response.completeExceptionally(t);
    }

    public CompletableFuture<String> getResponse(){
        return response;
    }

    public String toString(){
        return request;
    }

}