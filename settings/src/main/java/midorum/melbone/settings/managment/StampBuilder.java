package midorum.melbone.settings.managment;

import com.midorum.win32api.facade.Rectangle;
import dma.validation.Validator;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.model.settings.key.StampKey;
import midorum.melbone.settings.internal.dto.StampImpl;


public class StampBuilder {

    private StampKey key;
    private String description;
    private int[] wholeData;
    private int[] firstLine;
    private Rectangle location;
    private Rectangle windowRect;
    private Rectangle windowClientRect;
    private Rectangle windowClientToScreenRect;

    public StampBuilder key(final StampKey key) {
        this.key = key;
        return this;
    }

    public StampBuilder description(final String description) {
        this.description = description;
        return this;
    }

    public StampBuilder wholeData(final int[] wholeData) {
        this.wholeData = wholeData;
        return this;
    }

    public StampBuilder firstLine(final int[] firstLine) {
        this.firstLine = firstLine;
        return this;
    }

    public StampBuilder location(final Rectangle location) {
        this.location = location;
        return this;
    }

    public StampBuilder windowRect(final Rectangle windowRect) {
        this.windowRect = windowRect;
        return this;
    }

    public StampBuilder windowClientRect(final Rectangle windowClientRect) {
        this.windowClientRect = windowClientRect;
        return this;
    }

    public StampBuilder windowClientToScreenRect(final Rectangle windowClientToScreenRect) {
        this.windowClientToScreenRect = windowClientToScreenRect;
        return this;
    }

    public Stamp build() {
        return new StampImpl(Validator.checkNotNull(key).orThrowForSymbol("stamp key"),
                Validator.checkNotNull(description).andCheckNot(String::isBlank).orThrow("stamp description cannot be null or empty"),
                Validator.checkNotNull(wholeData).orThrowForSymbol("stamp whole data array"),
                Validator.checkNotNull(firstLine).orThrowForSymbol("stamp first line data array"),
                Validator.checkNotNull(location).orThrowForSymbol("stamp location"),
                Validator.checkNotNull(windowRect).orThrowForSymbol("stamp target window rectangle"),
                Validator.checkNotNull(windowClientRect).orThrowForSymbol("stamp target window client rectangle"),
                Validator.checkNotNull(windowClientToScreenRect).orThrowForSymbol("stamp target window client rectangle in screen coordinates"));
    }
}
