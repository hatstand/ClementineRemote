package com.purplehatstands.clementine.remote;

import java.io.IOException;

import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.sasl.SASLMechanism;

import android.util.Base64;
import android.util.Log;

public class GoogleTokenAuthenticator extends SASLMechanism {
  private static final String TAG = "GoogleTokenAuthenticator";

  public GoogleTokenAuthenticator(SASLAuthentication arg0) {
    super(arg0);
  }

  @Override
  protected String getName() {
    return "X-GOOGLE-TOKEN";
  }
  
  /*
   * X-GOOGLE-TOKEN Authentication
   * 1. Fetch a token from ClientLogin or AccountManager for service=mail.
   * 2. Append the username to a string containing a single null char.
   * 3. Append a null char and the auth token to that.
   * 4. Convert to base64 - this is the X-GOOGLE-TOKEN
   * 
   * eg.
   * <auth mechanism="X-GOOGLE-TOKEN" xmlns="urn:ietf:params:xml:ns:xmpp-sasl">
   *    AGpvaG4ubWFndWlyZUBnbWFpbC5jb20ARFFB...
   * </auth>
   */

  @Override
  public void authenticate(String username, String host, String auth)
      throws IOException, XMPPException {
    String token = '\0' + username + '\0' + auth;
    byte[] base64 = Base64.encode(token.getBytes(), Base64.NO_WRAP);
    
    AuthMechanism reply = new AuthMechanism("X-GOOGLE-TOKEN", new String(base64));  
    getSASLAuthentication().send(reply);
  }

}
