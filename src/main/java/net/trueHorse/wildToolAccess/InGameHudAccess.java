package net.trueHorse.wildToolAccess;

public interface InGameHudAccess {

    public AccessBar getOpenAccessBar();

    public void closeOpenAccessbar(boolean select);

    public void openAccessbar(int num);
}
