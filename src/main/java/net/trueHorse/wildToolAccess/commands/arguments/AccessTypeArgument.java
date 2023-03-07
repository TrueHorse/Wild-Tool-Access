package net.trueHorse.wildToolAccess.commands.arguments;

import java.util.function.Predicate;

public class AccessTypeArgument implements Predicate<Class<?>> {

    private final Class<?> type;

    public AccessTypeArgument(Class<?> type) {
        this.type = type;
    }

    @Override
    public boolean test(Class<?> aClass) {
        return type.equals(aClass);
    }

    public Class<?> getType() {
        return type;
    }
}
