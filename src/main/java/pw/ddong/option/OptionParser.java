package pw.ddong.option;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Daniel
 * @since Jun 26, 2015
 */
public class OptionParser {
    private Map<String, Option> options = new HashMap<>();

    /*
     * Since we'll never parse more than once with the same parser there
     * shouldn't be any issue to have a cache.
     */
    private Map<String, Option> cache = new HashMap<>();

    /*
     * The initial string with given arguments.
     */
    private String[] initial;

    /**
     * Parses the given arguments to see which options are present. Caches all
     * given arguments for later use.
     *
     * @param arguments
     */
    public void parse(String[] arguments) {
        this.initial = arguments;
        if (this.options.isEmpty()) {
            return;
        }

        String command = compile(arguments);
        for (Option option : this.options.values()) {
            if (!this.isPresent(option.getLabel(), Optional.of(command))) {
                continue;
            }
            this.cache.put(option.getLabel(), option);
        }
    }

    /**
     * Get the following argument after an option.
     *
     * @param option
     * @return
     */
    public String getFollowingParam(String option) {
        String following = "null";
        for (int index = 0; index < this.initial.length; index++) {
            String label = this.initial[index];
            if (!option.equals(label)) {
                continue;
            }

            following = this.initial[index + 1];
            break;
        }

        return following;
    }

    /**
     * Gets a specific option if it's present in the parsed string.
     *
     * @param option
     * @return option
     */
    public Option getIfPresent(String option) {
        if (!this.cache.containsKey(option)) {
            return null;
        }

        return cache.get(option);
    }

    /**
     * Compiles the arguments into a single string.
     *
     * @param arguments
     * @return The arguments as a single string separated by a space.
     */
    public String compile(String[] arguments) {
        return String.join(" ", arguments);
    }

    /**
     * Add an option to the parser.
     *
     * @param option
     * @param require
     */
    public void register(String option, boolean require, String description) {
        this.options.put(option, new Option(option, require, Optional.of(description)));
    }

    /**
     * Does the given command contain the option.
     *
     * @param option
     * @param command
     * @return true or false depending on command
     */
    public boolean isPresent(String option, Optional<String> command) {
        return command.map(s -> s.contains(option)).orElseGet(() -> cache.containsKey(option));

    }

    /**
     * All available options.
     *
     * @return options
     */
    public Collection<Option> getAvailable() {
        return options.values();
    }
}
