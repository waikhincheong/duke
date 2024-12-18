package wkduke.command.delete;

import wkduke.command.Command;
import wkduke.common.Messages;
import wkduke.common.Utils;
import wkduke.exception.command.CommandOperationException;
import wkduke.exception.storage.StorageOperationException;
import wkduke.storage.Storage;
import wkduke.task.Task;
import wkduke.task.TaskList;
import wkduke.ui.Ui;
import wkduke.ui.UiTaskGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static wkduke.ui.Ui.INDENT_HELP_MSG_NUM;

/**
 * Represents a command to delete a tasks from the task list.
 */
public class DeleteCommand extends Command {
    public static final String COMMAND_WORD = "delete";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " {task-numbers...}\n"
            + "Description:".indent(INDENT_HELP_MSG_NUM)
            + "  - Deletes the specified tasks from the task list.".indent(INDENT_HELP_MSG_NUM)
            + "  - You can provide multiple task numbers separated by commas.".indent(INDENT_HELP_MSG_NUM)
            + "Example:".indent(INDENT_HELP_MSG_NUM)
            + "  delete 1".indent(INDENT_HELP_MSG_NUM)
            + "  delete 1,3,5".indent(INDENT_HELP_MSG_NUM)
            + "Constraints:".indent(INDENT_HELP_MSG_NUM)
            + "  - Task numbers must be positive integers.".indent(INDENT_HELP_MSG_NUM)
            + "  - Task numbers must exist in the task list.".indent(INDENT_HELP_MSG_NUM)
            + "  - Duplicate task numbers will be ignored.".indent(INDENT_HELP_MSG_NUM);
    private static final String MESSAGE_SUCCESS_PRE = "Noted. I've removed these tasks:";
    private static final String MESSAGE_SUCCESS_POST = "Now you have %s tasks in the list.";
    private final Set<Integer> taskNumbers;

    /**
     * Constructs a DeleteCommand with the specified task numbers.
     *
     * @param taskNumbers A list of 1-based index representing the tasks to be marked as done (Duplicates will be ignored).
     */
    public DeleteCommand(List<Integer> taskNumbers) {
        this.taskNumbers = new HashSet<>(taskNumbers);
    }

    /**
     * Deletes tasks from the given task list based on the specified task numbers.
     *
     * @param taskList The task list from which tasks will be deleted.
     * @return A list of tasks that were successfully deleted.
     */
    private List<Task> deleteTasks(TaskList taskList) {
        List<Task> deletedTasks = new ArrayList<>();
        for (Integer taskNumber : taskNumbers) {
            int taskIndex = taskNumber - 1;
            Task task = taskList.getTask(taskIndex);
            deletedTasks.add(task);
        }

        for (Task task : deletedTasks) {
            taskList.deleteTask(task);
        }
        return deletedTasks;
    }

    /**
     * Checks if this DeleteCommand is equal to another object.
     * A DeleteCommand is considered equal if it is of the same type and has the same task number.
     *
     * @param obj The object to compare with this DeleteCommand.
     * @return {@code true} if the specified object is a DeleteCommand and has an equal task number; otherwise, {@code false}.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DeleteCommand command)) {
            return false;
        }
        return taskNumbers.equals(command.taskNumbers);
    }

    /**
     * Executes the delete command by removing the specified task from the task list,
     * saving the updated list to storage, and displaying a success message.
     *
     * @param taskList The task list from which the task will be deleted.
     * @param ui       The user interface for displaying messages to the user.
     * @param storage  The storage where the updated task list will be saved.
     * @throws StorageOperationException if there is an error with saving the task list to storage.
     * @throws CommandOperationException if the specified task number is invalid.
     */
    @Override
    public void execute(TaskList taskList, Ui ui, Storage storage) throws StorageOperationException, CommandOperationException {
        assert taskList != null : "Precondition failed: 'taskList' cannot be null";
        assert ui != null : "Precondition failed: 'ui' cannot be null";
        assert storage != null : "Precondition failed: 'storage' cannot be null";
        assert taskNumbers != null : "Precondition failed: 'taskNumbers' cannot be null";
        try {
            // Validate task numbers
            Utils.validateTaskNumbers(taskList, taskNumbers);

            // Delete task
            List<Task> deletedTasks = deleteTasks(taskList);

            // Save taskList to storage
            if (!deletedTasks.isEmpty()) {
                storage.save(taskList);
            }

            // Display success messages
            ui.printUiTaskGroup(taskList, new UiTaskGroup(MESSAGE_SUCCESS_PRE,
                    String.format(MESSAGE_SUCCESS_POST, taskList.size()), deletedTasks)
            );
        } catch (IndexOutOfBoundsException e) {
            throw new CommandOperationException(
                    Messages.MESSAGE_INVALID_TASK_NUMBER,
                    String.format("Command='delete', TaskNumber='%s'", taskNumbers),
                    Messages.MESSAGE_INVALID_TASK_NUMBER_HELP
            );
        }
    }
}
