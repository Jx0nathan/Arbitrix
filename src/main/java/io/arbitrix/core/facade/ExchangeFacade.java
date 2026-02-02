package io.arbitrix.core.facade;

public interface ExchangeFacade {
    /**
     * functionPrefix-exchange
     */
    String ACTION_NAME_FORMAT = "%s-%s";

    default String getActionName(String exchangeName) {
        return String.format(ACTION_NAME_FORMAT, getPrefix(), exchangeName.toUpperCase());
    }

    String getPrefix();
}
