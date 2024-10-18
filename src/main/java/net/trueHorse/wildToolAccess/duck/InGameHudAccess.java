package net.trueHorse.wildToolAccess.duck;

import net.trueHorse.wildToolAccess.AccessBar;

public interface InGameHudAccess {

    AccessBar getOpenAccessBar();

    void closeOpenAccessbar(boolean select);

    void openAccessbar(int num);

    boolean isBarWithNumberOpen(int number);

    void refreshAccessbars();
}
