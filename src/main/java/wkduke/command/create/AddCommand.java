package wkduke.command.create;

import wkduke.command.Command;
import wkduke.exception.command.CommandOperationException;
import wkduke.exception.storage.StorageOperationException;
import wkduke.storage.Storage;
import wkduke.task.Task;
import wkduke.task.TaskList;
import wkduke.ui.Ui;
import wkduke.ui.UiTaskGroup;

import java.util.List;

import static wkduke.common.Messages.MESSAGE_DUPLICATE_TASK;
import static wkduke.common.Messages.MESSAGE_DUPLICATE_TASK_HELP;

/**
 * Represents a command to add a task to the task list.
 * This is an abstract class to be extended by specific task-adding commands, such as Todo, Deadline, and Event.
 */
public abstract class AddCommand extends Command {
    public static final String COMMAND_WORD_TODO = "todo";
    public static final String COMMAND_WORD_DEADLINE = "deadline";
    public static final String COMMAND_WORD_EVENT = "event";
    private static final String MESSAGE_SUCCESS_PRE = "Got it. I've added this task:";
    private static final String MESSAGE_SUCCESS_POST = "Now you have %s tasks in the list.";
    Task task;

    /**
     * Validates that the task is not already in the task list.
     *
     * @param taskList The task list to validate against.
     * @throws CommandOperationException if the task already exists.
     */
    private void validateTaskUniqueness(TaskList taskList) throws CommandOperationException {
        List<Task> tasks = taskList.getTasks();
        if (tasks.contains(task)) {
            throw new CommandOperationException(MESSAGE_DUPLICATE_TASK,
                    String.format("Task='%s'", task), MESSAGE_DUPLICATE_TASK_HELP);
        }
    }

    /**
     * Checks if this AddCommand is equal to another object.
     * An AddCommand is considered equal if it is of the same type and has the same task.
     *
     * @param obj The object to compare with this AddCommand.
     * @return {@code true} if the specified object is an AddCommand and has an equal task; otherwise, {@code false}.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AddCommand command)) {
            return false;
        }
        return task.equals(command.task);
    }

    /**
     * Executes the add command by adding a task to the task list, saving it to storage, and displaying a success message.
     *
     * @param taskList The task list to which the task will be added.
     * @param ui       The user interface for displaying messages to the user.
     * @param storage  The storage where the updated task list will be saved.
     * @throws StorageOperationException if there is an error with saving the task list to storage.
     */
    @Override
    public void execute(TaskList taskList, Ui ui, Storage storage) throws StorageOperationException, CommandOperationException {
        assert taskList != null : "Precondition failed: 'taskList' cannot be null";
        assert ui != null : "Precondition failed: 'ui' cannot be null";
        assert storage != null : "Precondition failed: 'storage' cannot be null";
        assert task != null : "Precondition failed: 'task' cannot be null";

        validateTaskUniqueness(taskList);
        taskList.addTask(task);
        storage.save(taskList);
        ui.printUiTaskGroup(taskList, new UiTaskGroup(String.format(MESSAGE_SUCCESS_PRE),
                String.format(MESSAGE_SUCCESS_POST, taskList.size()), List.of(task)));
    }
}
