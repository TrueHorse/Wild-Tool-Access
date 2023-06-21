package net.trueHorse.wildToolAccess;

public interface InGameHudAccess {

    AccessBar getOpenAccessBar();

    void closeOpenAccessbar(boolean select);

    void openAccessbar(int num);

    boolean isBarWithNumberOpen(int number);

    void refreshAccessbars();
}
