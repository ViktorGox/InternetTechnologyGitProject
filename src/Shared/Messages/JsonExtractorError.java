package Shared.Messages;

public class JsonExtractorError extends RuntimeException{
    public JsonExtractorError(String error){
        super(error);
    }
}
