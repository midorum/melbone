package midorum.melbone.ui.context.experimental;

import midorum.melbone.model.experimental.task.Action;
import midorum.melbone.model.experimental.task.ActionType;
import midorum.melbone.model.experimental.task.Task;
import midorum.melbone.model.experimental.task.TaskStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MockedTaskStorage implements TaskStorage {

    private final Logger logger = LogManager.getLogger();

    private final Map<Integer, Task> tasks = new ConcurrentHashMap<>();
    private final Map<Integer, Action> actions = new ConcurrentHashMap<>();

    @Override
    public Collection<Task> listAllTasks() {
        final Collection<Task> result = List.copyOf(tasks.values());
        logger.trace("get all tasks: {}", result);
        return result;
    }

    @Override
    public int createTask(final String name, final int period) {
        final Task task = new Task(tasks.size() + 1, name, period, true);
        logger.trace("store task: {})", task);
        tasks.put(task.id(), task);
        return task.id();
    }

    @Override
    public void updateTask(final int id, final String name, final int period) {
        if (!tasks.containsKey(id)) throw new IllegalStateException("Task with id=" + id + " not found to update");
        final Task old = tasks.get(id);
        final Task task = new Task(id, name, period, old.enabled());
        logger.trace("update task: {} => {})", old, task);
        tasks.put(task.id(), task);
    }

    @Override
    public void updateTask(final int id, final boolean enabled) {
        if (!tasks.containsKey(id)) throw new IllegalStateException("Task with id=" + id + " not found to update");
        final Task old = tasks.get(id);
        final Task task = new Task(id, old.name(), old.period(), enabled);
        logger.trace("update task: {} => {})", old, task);
        tasks.put(task.id(), task);
    }

    @Override
    public Optional<Task> getTaskById(final int id) {
        final Optional<Task> result = Optional.ofNullable(tasks.get(id));
        logger.trace("get task id={}: {}", id, result);
        return result;
    }

    @Override
    public Collection<Action> listAllActions() {
        final Collection<Action> result = List.copyOf(actions.values());
        logger.trace("get all actions: {}", result);
        return result;
    }

    @Override
    public Optional<Action> getActionById(final int id) {
        final Optional<Action> result = Optional.ofNullable(actions.get(id));
        logger.trace("get action id={}: {}", id, result);
        return result;
    }

    @Override
    public int createAction(final String name, final ActionType type) {
        final Action action = new Action(actions.size() + 1, name, type, true);
        logger.trace("store action: {})", action);
        actions.put(action.id(), action);
        return action.id();
    }

    @Override
    public void updateAction(final int id, final String name, final ActionType type) {
        if (!actions.containsKey(id)) throw new IllegalStateException("Action with id=" + id + " not found to update");
        final Action old = actions.get(id);
        final Action action = new Action(id, name, type, old.enabled());
        logger.trace("update action: {} => {})", old, action);
        actions.put(action.id(), action);
    }
}
