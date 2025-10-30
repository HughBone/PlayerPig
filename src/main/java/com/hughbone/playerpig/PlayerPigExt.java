package com.hughbone.playerpig;

public interface PlayerPigExt {

  void setPlayerName(String name);

  void setPlayerUUID(String uuid);

  String getPlayerName();

  String getPlayerUUID();

  boolean isPlayerPig();

  void setPlayerPig(boolean setPP);

}
