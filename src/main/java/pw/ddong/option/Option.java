package pw.ddong.option;

import java.util.Optional;

/**
 * Used for startup arguments.
 *
 * @author Daniel
 * @since Jun 26, 2015
 */
public class Option {
    private String label;
    private boolean required;
    private String description = "No description given.";

    public Option(String label, boolean required, Optional<String> description) {
        this.label = label;
        this.required = required;
        if (description.isPresent()) {
            this.description = description.get();
        }
    }

    public String getLabel() {
        return label;
    }

    public boolean isRequired() {
        return required;
    }

    public String getDescription() {
        return description;
    }
}
