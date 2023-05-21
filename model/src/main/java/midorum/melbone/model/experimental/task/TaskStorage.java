package midorum.melbone.model.experimental.task;

import java.util.Collection;
import java.util.Optional;

public interface TaskStorage {

    Collection<Task> listAllTasks();

    int createTask(final String name, final int period);

    void updateTask(final int id, final String name, final int period);

    void updateTask(final int id, final boolean enabled);

    Optional<Task> getTaskById(final int id);

    Collection<Action> listAllActions();

    Optional<Action> getActionById(final int id);

    int createAction(final String name, final ActionType type);

    void updateAction(final int id, final String name, final ActionType type);

}
