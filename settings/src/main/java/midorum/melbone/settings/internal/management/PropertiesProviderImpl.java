package midorum.melbone.settings.internal.management;

import dma.validation.Validator;
import midorum.melbone.model.exception.CriticalErrorException;
import midorum.melbone.model.settings.PropertiesProvider;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Properties;

public class PropertiesProviderImpl implements PropertiesProvider {

    private final String modeKey = "app.mode";
    private final String nameKey = "app.name";
    private final Properties properties;

    public PropertiesProviderImpl(final String fileName) {
        this.properties = new Properties();
        if (fileName == null) return;
        final File file = new File(fileName);
        if (file.exists()) {
            try (final Reader reader = new FileReader(file)) {
                this.properties.load(reader);
            } catch (IOException e) {
                throw new CriticalErrorException(e);
            }
        }
    }

    @Override
    public String mode() {
        return properties.getProperty(modeKey);
    }

    @Override
    public boolean isModeSet(final String mode) {
        return Optional.ofNullable(properties.getProperty(modeKey))
                .filter(s -> s.contains(Validator.checkNotNull(mode).andCheckNot(String::isBlank).orThrow()))
                .isPresent();
    }

    @Override
    public String appName() {
        return Validator.checkNotNull(properties.getProperty(nameKey)).orDefault(this::getExecutableName);
    }

    @Override
    public String storageName(final String storageFilePropertyName) {
        return properties.getProperty(Validator.checkNotNull(storageFilePropertyName).andCheckNot(String::isBlank).orThrow());
    }

    private String getExecutableName() {
        try {
            final File file = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            return file.isFile() ? file.getName() : "app";
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return "app";
    }
}
