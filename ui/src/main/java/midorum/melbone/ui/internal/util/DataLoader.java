package midorum.melbone.ui.internal.util;

import dma.validation.Validator;
import midorum.melbone.model.exception.ControlledInterruptedException;
import midorum.melbone.model.exception.CriticalErrorException;

import javax.swing.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DataLoader {

    private final static DataLoader INSTANCE = new DataLoader();

    private DataLoader() {
    }

    public static DataLoader getInstance() {
        return INSTANCE;
    }

    public <T> void loadGuiData(final Supplier<T> dataSupplier, final Consumer<T> dataConsumer) {
        final Supplier<T> dataSupplierChecked = Validator.checkNotNull(dataSupplier).orThrowForSymbol("dataSupplier");
        final Consumer<T> dataConsumerChecked = Validator.checkNotNull(dataConsumer).orThrowForSymbol("dataConsumer");
        (new SwingWorker<T, Object>() {
            @Override
            protected T doInBackground() throws Exception {
                return dataSupplierChecked.get();
            }

            @Override
            protected void done() {
                try {
                    dataConsumerChecked.accept(get());
                } catch (InterruptedException e) {
                    throw new ControlledInterruptedException(e);
                } catch (ExecutionException e) {
                    throw new CriticalErrorException(e);
                }
            }
        }).execute();
    }
}
