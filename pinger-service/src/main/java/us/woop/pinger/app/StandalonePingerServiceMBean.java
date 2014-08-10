package us.woop.pinger.app;

public interface StandalonePingerServiceMBean {
    public void shutdown();
    public void restartHazelcast();
}