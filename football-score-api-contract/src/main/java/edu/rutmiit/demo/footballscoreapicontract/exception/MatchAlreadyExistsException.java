package edu.rutmiit.demo.footballscoreapicontract.exception;

public class MatchAlreadyExistsException extends RuntimeException {

    public MatchAlreadyExistsException() {
        super("Match already exists");
    }
}