package com.purplehatstands.clementine.remote;

public interface AuthTokenReceiver {
  void OnAuthTokenReceived(String service, String token);
}
