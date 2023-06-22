package midorum.melbone.executor.internal.processor;

import midorum.melbone.model.exception.ControlledInterruptedException;
import midorum.melbone.model.window.WindowConsumer;
import midorum.melbone.model.window.baseapp.BaseAppWindow;
import midorum.melbone.model.window.baseapp.InGameBaseAppWindow;
import midorum.melbone.window.WindowFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class OnRunningAccountActionTest {

    private final WindowFactory windowFactory = Mockito.mock(WindowFactory.class);

    @BeforeAll
    public static void beforeAll() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @AfterEach
    public void afterEach() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @Test
    void checkInterruptedExceptionHandling() throws InterruptedException {
        System.out.println("checkInterruptedExceptionHandling");
        final List<BaseAppWindow> baseAppWindows = mockBaseAppWindows(getBaseAppWindowsWithThrowing());
        final OnRunningAccountAction instance = new OnRunningAccountAction(windowFactory);
        Assertions.assertThrows(ControlledInterruptedException.class, instance::perform);
        verifyBaseAppMethodsCall(baseAppWindows);
    }

    @Test
    void checkNormalProcessingAllWindows() throws InterruptedException {
        System.out.println("checkNormalProcessingAllWindows");
        final List<BaseAppWindow> baseAppWindows = mockBaseAppWindows(getBaseAppWindows());
        final OnRunningAccountAction instance = new OnRunningAccountAction(windowFactory);
        instance.perform();
        Mockito.verify(windowFactory, Mockito.only()).getAllBaseAppWindows();
        verifyBaseAppMethodsCall(baseAppWindows);
    }

    private List<BaseAppWindow> getBaseAppWindows() throws InterruptedException {
        return Arrays.asList(mockBaseAppWindowThatShouldProcessedNormally("test_account_1"),
                mockBaseAppWindowThatShouldProcessedNormally("test_account_2"));
    }

    private List<BaseAppWindow> getBaseAppWindowsWithThrowing() throws InterruptedException {
        return Arrays.asList(mockBaseAppWindowThatShouldProcessedNormally("test_account_1"),
                mockBaseAppWindowThrowing("test_account_2"));
    }

    private List<BaseAppWindow> mockBaseAppWindows(final List<BaseAppWindow> testWindows) {
        Mockito.when(windowFactory.getAllBaseAppWindows()).thenReturn(testWindows);
        return testWindows;
    }

    private BaseAppWindow mockBaseAppWindow(final String characterName) {
        final BaseAppWindow mock = Mockito.mock(BaseAppWindow.class);
        Mockito.when(mock.getCharacterName()).thenReturn(Optional.ofNullable(characterName));
        return mock;
    }

    @SuppressWarnings("unchecked")
    private BaseAppWindow mockBaseAppWindowThatShouldProcessedNormally(final String characterName) throws InterruptedException {
        final BaseAppWindow mock = mockBaseAppWindow(characterName);
        Mockito.doAnswer(invocation -> {
            final Object[] arguments = invocation.getArguments();
            Assertions.assertEquals(1, arguments.length);
            final WindowConsumer<InGameBaseAppWindow> consumer = (WindowConsumer<InGameBaseAppWindow>) arguments[0];
            final InGameBaseAppWindow inGameBaseAppWindow = Mockito.mock(InGameBaseAppWindow.class);
            consumer.accept(inGameBaseAppWindow);
            Mockito.verify(inGameBaseAppWindow, Mockito.times(1)).checkInLoginTracker();
            Mockito.verify(inGameBaseAppWindow, Mockito.times(1)).checkInAction();
            return null;
        }).when(mock).doInGameWindow(Mockito.any(WindowConsumer.class));
        return mock;
    }

    @SuppressWarnings("unchecked")
    private BaseAppWindow mockBaseAppWindowThrowing(final String characterName) throws InterruptedException {
        final BaseAppWindow mock = mockBaseAppWindow(characterName);
        Mockito.doThrow(InterruptedException.class).when(mock).doInGameWindow(Mockito.any(WindowConsumer.class));
        return mock;
    }

    @SuppressWarnings("unchecked")
    private void verifyBaseAppMethodsCall(final List<BaseAppWindow> baseAppWindows) {
        baseAppWindows.forEach(baseAppWindow -> {
            Mockito.verify(baseAppWindow, Mockito.times(1)).getCharacterName();
            try {
                Mockito.verify(baseAppWindow, Mockito.times(1)).doInGameWindow(Mockito.any(WindowConsumer.class));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

}