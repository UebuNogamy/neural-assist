package model;

public record Incoming( Type type, String payload )
{
    public enum Type
    {
        CONTENT,
        FUNCTION_CALL
    }
}
