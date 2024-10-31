package wkduke.command;

import wkduke.storage.Storage;
import wkduke.task.Task;
import wkduke.task.TaskList;
import wkduke.ui.Ui;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a command to list all tasks in the task list.
 */
public class ListCommand extends Command {
    public static final String COMMAND_WORD = "list";
    public static final String MESSAGE_SUCCESS = "Here are the tasks in your list:";
    public static final String MESSAGE_FAILED = "Your task list is currently empty.";

    /**
     * Executes the list command by retrieving all tasks from the task list
     * and displaying them to the user. If the task list is empty, a message
     * indicating this is displayed.
     *
     * @param taskList The task list containing all tasks.
     * @param ui       The user interface for displaying messages to the user.
     * @param storage  The storage being used (not used in this command).
     */
    @Override
    public void execute(TaskList taskList, Ui ui, Storage storage) {
        List<Task> tasks = taskList.getAllTask();
        if (tasks.isEmpty()) {
            ui.printMessages(MESSAGE_FAILED);
            return;
        }

        List<String> messages = new ArrayList<>();
        messages.add(MESSAGE_SUCCESS);
        for (Task task : tasks) {
            messages.add(String.format("%d.%s", taskList.getTaskIndex(task) + 1, task));
        }
        ui.printMessages(messages.toArray(new String[0]));
    }
}