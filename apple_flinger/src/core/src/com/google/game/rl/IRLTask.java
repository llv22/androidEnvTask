package com.google.game.rl;


@SuppressWarnings("unused")
public interface IRLTask {

  void nativeLogDebug(String tag, String msg);
  void logReady();
  boolean isEnabled();
  boolean get(String key, boolean defaultValue);
  String get(String key, String defaultValue);
  @Deprecated
  void logExtra(Object extra);
  void logExtra(String name, Object value);
  void logScore(Object score);
  void logEpisodeEnd();

  IRLTask EMPTY_TASK = new IRLTask(){

    @Override
    public void nativeLogDebug(String tag, String msg) {
      System.out.println(tag + ": " + msg);
    }

    @Override
    public void logReady() {
      // DO NOTHING
    }
    @Override
    public boolean isEnabled() {
      return false;
    }

    @Override
    public String get(String key, String defaultValue) {
      return defaultValue;
    }

    @Override
    public boolean get(String key, boolean defaultValue) {
      return defaultValue;
    }

    @Deprecated
    @Override
    public void logExtra(Object extra) {
      // DO NOTHING
    }

    @Override
    public void logExtra(String name, Object value) {
      // DO NOTHING
    }

    @Override
    public void logScore(Object score) {
      // DO NOTHING
  }

    @Override
    public void logEpisodeEnd() {
      // DO NOTHING
    }
  };
}
