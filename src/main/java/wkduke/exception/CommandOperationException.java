package wkduke.exception;

/**
 * Represents an exception that is thrown when an error occurs during the execution of a command.
 * This exception is used to signal issues that prevent successful command operations.
 */
public class CommandOperationException extends WKDukeException {
    /**
     * Constructs a CommandOperationException with the specified error message.
     *
     * @param message The error message describing the command operation issue.
     */
    public CommandOperationException(String message) {
        super(message);
    }

    /**
     * Constructs a CommandOperationException with the specified error message and detailed information.
     *
     * @param message The error message describing the command operation issue.
     * @param detail  Additional detail about the command operation issue.
     */
    public CommandOperationException(String message, String detail) {
        super(message, detail);
    }

    /**
     * Constructs a CommandOperationException with the specified error message, detailed information, and help text.
     *
     * @param message The error message describing the command operation issue.
     * @param detail  Additional detail about the command operation issue.
     * @param help    Suggested help or guidance for resolving the issue.
     */
    public CommandOperationException(String message, String detail, String help) {
        super(message, detail, help);
    }
}