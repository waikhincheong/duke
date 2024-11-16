package wkduke.command.update;

import wkduke.exception.storage.StorageOperationException;
import wkduke.storage.Storage;
import wkduke.task.TaskList;
import wkduke.ui.Ui;
import wkduke.ui.UiTaskGroup;

/**
 * A command to sort tasks in the task list by task type.
 */
public class SortByTaskTypeCommand extends SortCommand {
    /**
     * Constructs a {@code SortByTaskTypeCommand} with the specified sort order.
     *
     * @param sortOrder The sort order (ascending or descending).
     */
    public SortByTaskTypeCommand(SortOrder sortOrder) {
        super(sortOrder);
    }

    /**
     * Checks if this {@code SortByTaskTypeCommand} is equal to another object.
     * Two commands are considered equal if they have the same sorting order.
     *
     * @param obj The object to compare with this command.
     * @return {@code true} if the specified object is a {@code SortByTaskTypeCommand} with the same sort order; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SortByTaskTypeCommand command)) {
            return false;
        }
        return sortOrder.equals(command.sortOrder);
    }

    /**
     * Executes the sort command by sorting tasks in the task list by task type, saving the updated list,
     * and displaying the sorted tasks.
     *
     * @param taskList The task list to sort.
     * @param ui       The user interface to display the sorted tasks.
     * @param storage  The storage to save the sorted task list.
     * @throws StorageOperationException If there is an error saving the task list.
     */
    @Override
    public void execute(TaskList taskList, Ui ui, Storage storage) throws StorageOperationException {
        taskList.sortTaskByType(sortOrder);
        storage.save(taskList);
        ui.printUiTaskGroup(taskList, new UiTaskGroup(
                String.format(MESSAGE_SUCCESS, "tasktype", sortOrder), "", taskList.getAllTask()));
    }
}