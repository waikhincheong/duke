package wkduke.parser;

import wkduke.command.Command;
import wkduke.command.ExitCommand;
import wkduke.command.HelpCommand;
import wkduke.command.create.AddCommand;
import wkduke.command.create.AddDeadlineCommand;
import wkduke.command.create.AddEventCommand;
import wkduke.command.create.AddTodoCommand;
import wkduke.command.delete.DeleteCommand;
import wkduke.command.read.FindCommand;
import wkduke.command.read.ListCommand;
import wkduke.command.read.ListOnCommand;
import wkduke.command.update.MarkCommand;
import wkduke.command.update.SortByDateTimeCommand;
import wkduke.command.update.SortByPriorityCommand;
import wkduke.command.update.SortByTaskTypeCommand;
import wkduke.command.update.SortCommand;
import wkduke.command.update.SortField;
import wkduke.command.update.SortOrder;
import wkduke.command.update.UnmarkCommand;
import wkduke.command.update.UpdatePriorityCommand;
import wkduke.common.Messages;
import wkduke.exception.TaskFormatException;
import wkduke.exception.command.CommandFormatException;
import wkduke.task.TaskPriority;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static wkduke.common.Utils.validateDateTimeRange;

/**
 * Parses user input into command objects for execution.
 * Uses regular expressions to match and extract command keywords and arguments.
 */
public class CommandParser {
    private static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)");
    private static final Pattern TASK_TODO_DATA_ARGS_FORMAT = Pattern.compile("(?<description>.+)");
    private static final Pattern TASK_DEADLINE_DATA_ARGS_FORMAT = Pattern.compile("(?<description>.+) /by (?<by>.+)");
    // Solution below inspired by https://perlancar.wordpress.com/2018/10/05/matching-several-things-in-no-particular-order-using-a-single-regex/
    private static final Pattern TASK_EVENT_DATA_ARGS_FORMAT = Pattern.compile("(?<description>[^/]+)(?=.*?/from\\s+(?<from>(?:(?!/to|$).)+))(?=.*?/to\\s+(?<to>(?:(?!/from|$).)+))");

    private static final Pattern LIST_TASK_ARGS_FORMAT = Pattern.compile("/on (?<on>.+)");
    private static final Pattern UPDATE_TASK_PRIORITY_ARGS_FORMAT = Pattern.compile("^(?<taskNumber>\\d.*) (?<priority>[LMH])$");
    private static final Pattern SORT_TASK_ARGS_FORMAT = Pattern.compile("(?=.*?/by\\s+(?<by>priority|tasktype|datetime))(?=.*?/order\\s+(?<order>asc|desc))");
    private static final Pattern FIND_TASK_ARGS_FORMAT = Pattern.compile("([^,]+)");

    /**
     * Parses the user input into a command.
     *
     * @param userInput The full user input to be parsed.
     * @return The corresponding {@code Command} based on the user input.
     * @throws CommandFormatException If the command format is invalid.
     * @throws TaskFormatException    If the task format within the command is invalid.
     */
    public static Command parseCommand(String userInput) throws CommandFormatException, TaskFormatException {
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            throw new AssertionError("Empty command scenario is already handled earlier");
        }

        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments").trim();

        return switch (commandWord) {
            case ExitCommand.COMMAND_WORD -> new ExitCommand();
            case HelpCommand.COMMAND_WORD -> new HelpCommand();
            case ListCommand.COMMAND_WORD -> prepareList(arguments);
            case AddCommand.COMMAND_WORD_TODO -> prepareAddToDo(arguments);
            case AddCommand.COMMAND_WORD_DEADLINE -> prepareAddDeadline(arguments);
            case AddCommand.COMMAND_WORD_EVENT -> prepareAddEvent(arguments);
            case MarkCommand.COMMAND_WORD -> prepareMark(arguments);
            case UnmarkCommand.COMMAND_WORD -> prepareUnmark(arguments);
            case DeleteCommand.COMMAND_WORD -> prepareDelete(arguments);
            case UpdatePriorityCommand.COMMAND_WORD -> prepareUpdatePriority(arguments);
            case FindCommand.COMMAND_WORD -> prepareFind(arguments);
            case SortCommand.COMMAND_WORD -> prepareSort(arguments);
            default -> throw new CommandFormatException(
                    Messages.MESSAGE_UNKNOWN_COMMAND,
                    String.format("Input='%s'", userInput),
                    System.lineSeparator() + Messages.MESSAGE_AVAILABLE_COMMAND
            );
        };
    }

    /**
     * Prepares an AddDeadline command from the given arguments.
     *
     * @param arguments The arguments provided for the Deadline task.
     * @return A new {@code AddDeadlineCommand} with the specified description and due date.
     * @throws TaskFormatException If the arguments format is invalid.
     */
    private static Command prepareAddDeadline(String arguments) throws TaskFormatException {
        final Matcher matcher = TASK_DEADLINE_DATA_ARGS_FORMAT.matcher(arguments.trim());
        if (!matcher.matches()) {
            throw new TaskFormatException(
                    Messages.MESSAGE_INVALID_TASK_FORMAT,
                    String.format("Command='deadline', Arguments='%s'", arguments),
                    AddDeadlineCommand.MESSAGE_USAGE
            );
        }
        LocalDateTime byDateTime = TimeParser.parseDateTime(matcher.group("by"));
        return new AddDeadlineCommand(matcher.group("description"), byDateTime);
    }

    /**
     * Prepares an AddEvent command from the given arguments.
     *
     * @param arguments The arguments provided for the Event task.
     * @return A new {@code AddEventCommand} with the specified description, start date, and end date.
     * @throws TaskFormatException If the arguments format is invalid.
     */
    private static Command prepareAddEvent(String arguments) throws TaskFormatException {
        final Matcher matcher = TASK_EVENT_DATA_ARGS_FORMAT.matcher(arguments.trim());
        if (!matcher.find()) {
            throw new TaskFormatException(
                    Messages.MESSAGE_INVALID_TASK_FORMAT,
                    String.format("Command='event', Arguments='%s'", arguments),
                    AddEventCommand.MESSAGE_USAGE
            );
        }
        LocalDateTime fromDateTime = TimeParser.parseDateTime(matcher.group("from").trim());
        LocalDateTime toDateTime = TimeParser.parseDateTime(matcher.group("to").trim());
        validateDateTimeRange(fromDateTime, toDateTime, arguments);
        return new AddEventCommand(matcher.group("description").trim(), fromDateTime, toDateTime);
    }

    /**
     * Prepares an AddToDo command from the given arguments.
     *
     * @param arguments The arguments provided for the Todo task.
     * @return A new {@code AddTodoCommand} with the specified task description.
     * @throws TaskFormatException If the arguments format is invalid.
     */
    private static Command prepareAddToDo(String arguments) throws TaskFormatException {
        final Matcher matcher = TASK_TODO_DATA_ARGS_FORMAT.matcher(arguments.trim());
        if (!matcher.matches()) {
            throw new TaskFormatException(
                    Messages.MESSAGE_INVALID_TASK_FORMAT,
                    String.format("Command='todo', Arguments='%s'", arguments),
                    AddTodoCommand.MESSAGE_USAGE
            );
        }
        return new AddTodoCommand(matcher.group("description"));
    }

    /**
     * Prepares a DeleteCommand from the given arguments.
     *
     * @param arguments The arguments provided to specify which tasks to delete.
     * @return A new {@code DeleteCommand} with the specified task numbers.
     * @throws CommandFormatException If the arguments format is invalid.
     */
    private static Command prepareDelete(String arguments) throws CommandFormatException {
        try {
            List<Integer> taskNumbers = TaskNumberParser.parseTaskNumbers(arguments, ",");
            return new DeleteCommand(taskNumbers);
        } catch (NumberFormatException e) {
            throw new CommandFormatException(
                    String.format(Messages.MESSAGE_INVALID_TASK_NUMBERS_FORMAT, e.getMessage()),
                    String.format("Command='delete', Arguments='%s'", arguments),
                    DeleteCommand.MESSAGE_USAGE
            );
        }
    }

    /**
     * Prepares a FindCommand based on the specified arguments.
     *
     * @param arguments The arguments containing keywords to search for in task descriptions.
     * @return A {@code FindCommand} with the specified keywords.
     * @throws CommandFormatException If the arguments format is invalid.
     */
    private static Command prepareFind(String arguments) throws CommandFormatException {
        final Matcher matcher = FIND_TASK_ARGS_FORMAT.matcher(arguments.trim());
        List<String> keywords = new ArrayList<>();
        while (matcher.find()) {
            keywords.add(matcher.group(1).trim());
        }

        if (keywords.isEmpty()) {
            throw new CommandFormatException(
                    Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                    String.format("Command='find', Arguments='%s'", arguments),
                    FindCommand.MESSAGE_USAGE
            );
        }
        return new FindCommand(keywords);
    }

    /**
     * Prepares a ListCommand or ListOnCommand based on the arguments.
     *
     * @param arguments The arguments specifying a date for filtering, if provided.
     * @return A {@code ListCommand} if no date is provided, or a {@code ListOnCommand} if a date is specified.
     * @throws CommandFormatException If the arguments format is invalid.
     */
    private static Command prepareList(String arguments) throws CommandFormatException {
        if (arguments.isEmpty()) {
            return new ListCommand();
        }

        final Matcher matcher = LIST_TASK_ARGS_FORMAT.matcher(arguments.trim());
        if (!matcher.matches()) {
            throw new CommandFormatException(
                    Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                    String.format("Command='list', Arguments='%s'", arguments),
                    ListOnCommand.MESSAGE_USAGE
            );
        }
        try {
            LocalDateTime onDateTime = TimeParser.parseDateTime(matcher.group("on"));
            return new ListOnCommand(onDateTime);
        } catch (TaskFormatException e) {
            throw new CommandFormatException(
                    e.getMessage(),
                    e.getDetail(),
                    e.getHelp()
            );
        }
    }

    /**
     * Prepares a MarkCommand from the given arguments.
     *
     * @param arguments The arguments provided to specify which tasks to mark as done.
     * @return A new {@code MarkCommand} with the specified task numbers.
     * @throws CommandFormatException If the arguments format is invalid.
     */
    private static Command prepareMark(String arguments) throws CommandFormatException {
        try {
            List<Integer> taskNumbers = TaskNumberParser.parseTaskNumbers(arguments, ",");
            return new MarkCommand(taskNumbers);
        } catch (NumberFormatException e) {
            throw new CommandFormatException(
                    String.format(Messages.MESSAGE_INVALID_TASK_NUMBERS_FORMAT, e.getMessage()),
                    String.format("Command='mark', Arguments='%s'", arguments),
                    MarkCommand.MESSAGE_USAGE
            );
        }
    }

    /**
     * Prepares a SortCommand from the given arguments.
     *
     * @param arguments The arguments provided to specify the sort field and order.
     * @return A specific {@code SortCommand} for the given sort field and order.
     * @throws CommandFormatException If the arguments format is invalid.
     */
    private static Command prepareSort(String arguments) throws CommandFormatException {
        final Matcher matcher = SORT_TASK_ARGS_FORMAT.matcher(arguments.trim());
        if (!matcher.find()) {
            throw new CommandFormatException(
                    Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                    String.format("Command='sort', Arguments='%s'", arguments),
                    SortCommand.MESSAGE_USAGE
            );
        }
        SortField sortField = SortField.fromFieldName(matcher.group("by"));
        SortOrder sortOrder = SortOrder.fromCode(matcher.group("order"));
        return switch (sortField) {
            case PRIORITY -> new SortByPriorityCommand(sortOrder);
            case TASKTYPE -> new SortByTaskTypeCommand(sortOrder);
            case DATETIME -> new SortByDateTimeCommand(sortOrder);
            default -> throw new AssertionError("Invalid sort field scenario is already handled earlier");
        };
    }

    /**
     * Prepares an UnmarkCommand from the given arguments.
     *
     * @param arguments The arguments provided to specify which tasks to unmark.
     * @return A new {@code UnmarkCommand} with the specified task numbers.
     * @throws CommandFormatException If the arguments format is invalid.
     */
    private static Command prepareUnmark(String arguments) throws CommandFormatException {
        try {
            List<Integer> taskNumbers = TaskNumberParser.parseTaskNumbers(arguments, ",");
            return new UnmarkCommand(taskNumbers);
        } catch (NumberFormatException e) {
            throw new CommandFormatException(
                    String.format(Messages.MESSAGE_INVALID_TASK_NUMBERS_FORMAT, e.getMessage()),
                    String.format("Command='unmark', Arguments='%s'", arguments),
                    UnmarkCommand.MESSAGE_USAGE
            );
        }
    }

    /**
     * Prepares an UpdatePriorityCommand from the given arguments.
     *
     * @param arguments The arguments specifying the task number and the new priority level.
     * @return A new {@code UpdatePriorityCommand} with the specified task number and priority.
     * @throws CommandFormatException If the arguments format is invalid.
     */
    private static Command prepareUpdatePriority(String arguments) throws CommandFormatException {
        final Matcher matcher = UPDATE_TASK_PRIORITY_ARGS_FORMAT.matcher(arguments.trim());
        if (!matcher.matches()) {
            throw new CommandFormatException(
                    Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                    String.format("Command='update-priority', Arguments='%s'", arguments),
                    UpdatePriorityCommand.MESSAGE_USAGE
            );
        }
        return new UpdatePriorityCommand(
                Integer.parseInt(matcher.group("taskNumber")),
                TaskPriority.fromCode(matcher.group("priority"))
        );
    }
}
