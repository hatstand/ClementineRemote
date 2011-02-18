package com.purplehatstands.clementine.remote;

import java.io.IOException;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

class GetAuthTokenCallback implements AccountManagerCallback<Bundle> {
  private final AuthTokenReceiver receiver_;
  private final String service_;
  private final Context context_;
  
  private static final String TAG = "GetAuthTokenCallback";
  
  public GetAuthTokenCallback(String service, AuthTokenReceiver receiver, Context context) {
    receiver_ = receiver;
    service_ = service;
    context_ = context;
  }
  
  public void run(AccountManagerFuture<Bundle> arg0) {
    try {
      Bundle bundle = arg0.getResult();
      if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
        String auth_token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
        Log.d(TAG, "Service: " + service_ + " Auth token:" + auth_token);
        receiver_.OnAuthTokenReceived(service_, auth_token);
      } else if (bundle.containsKey(AccountManager.KEY_INTENT)) {
        Intent intent = (Intent)bundle.get(AccountManager.KEY_INTENT);
        context_.startActivity(intent);
      }
    } catch (OperationCanceledException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (AuthenticatorException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }  
  }
}