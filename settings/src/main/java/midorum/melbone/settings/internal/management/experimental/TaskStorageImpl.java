package midorum.melbone.settings.internal.management.experimental;

import midorum.melbone.model.experimental.task.Action;
import midorum.melbone.model.experimental.task.ActionType;
import midorum.melbone.model.experimental.task.Task;
import midorum.melbone.model.experimental.task.TaskStorage;

import java.util.Collection;
import java.util.Optional;

public class TaskStorageImpl implements TaskStorage {

    public TaskStorageImpl() {
    }

    @Override
    public Collection<Task> listAllTasks() {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public int createTask(final String name, final int period) {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public void updateTask(final int id, final String name, final int period) {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public void updateTask(final int id, final boolean enabled) {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public Optional<Task> getTaskById(final int id) {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public Collection<Action> listAllActions() {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public Optional<Action> getActionById(final int id) {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public int createAction(final String name, final ActionType type) {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public void updateAction(final int id, final String name, final ActionType type) {
        throw new UnsupportedOperationException("Not supported yet");
    }
}
