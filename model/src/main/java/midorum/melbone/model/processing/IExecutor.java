package midorum.melbone.model.processing;

public interface IExecutor {

    void sendRoutineTask(final AccountsProcessingRequest request);

    void cancelCurrentTask();
}
