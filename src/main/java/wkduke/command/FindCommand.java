package wkduke.command;

import wkduke.exception.CommandOperationException;
import wkduke.exception.StorageOperationException;
import wkduke.storage.Storage;
import wkduke.task.Task;
import wkduke.task.TaskList;
import wkduke.ui.Ui;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a command to update the priority of a specified task in the task list.
 */
public class FindCommand extends Command {
    public static final String COMMAND_WORD = "find";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " {keywords} - specify one or more keywords separated by comma";
    public static final String MESSAGE_SUCCESS = "Here are the tasks in your list with the keyword '%s':";
    public static final String MESSAGE_FAILED = "No tasks found with the keyword: %s";
    private final List<String> keywords;


    public FindCommand(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FindCommand command)) {
            return false;
        }
        return keywords.equals(command.keywords);
    }

    @Override
    public void execute(TaskList taskList, Ui ui, Storage storage) throws StorageOperationException, CommandOperationException {
        List<Task> matchingTasks = taskList.findTasks(keywords);
        if (matchingTasks.isEmpty()) {
            ui.printMessages(String.format(MESSAGE_FAILED, keywords));
            return;
        }

        List<String> messages = new ArrayList<>();
        messages.add(String.format(MESSAGE_SUCCESS, keywords));
        for (Task task : matchingTasks) {
            messages.add(String.format("%d.%s", taskList.getTaskIndex(task) + 1, task));
        }
        ui.printMessages(messages.toArray(new String[0]));
    }
}