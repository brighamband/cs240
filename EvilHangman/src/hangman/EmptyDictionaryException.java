package hangman;

public class EmptyDictionaryException extends Exception {
    String errorMsg;

    //Thrown when dictionary file is empty or no words in dictionary match the length asked for
    public EmptyDictionaryException(String errorMsg) {
        super(errorMsg);
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return errorMsg;
    }
}
