package com.purplehatstands.clementine.remote;

import java.io.IOException;

import javax.security.auth.callback.CallbackHandler;

import org.apache.harmony.javax.security.auth.callback.Callback;
import org.apache.harmony.javax.security.auth.callback.UnsupportedCallbackException;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Authentication;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.sasl.SASLMechanism;

import android.util.Base64;
import android.util.Log;

public class GoogleTokenAuthenticator extends SASLMechanism {
  private static final String TAG = "GoogleTokenAuthenticator";

  public GoogleTokenAuthenticator(SASLAuthentication arg0) {
    super(arg0);
    // TODO Auto-generated constructor stub
  }

  @Override
  protected String getName() {
    // TODO Auto-generated method stub
    return "X-GOOGLE-TOKEN";
  }
  
  
  @Override
  public void authenticate(String username, String host,
      org.apache.harmony.javax.security.auth.callback.CallbackHandler cbh)
      throws IOException, XMPPException {
    Log.d(TAG, "authenticate:" + username + "/" + host);
    super.authenticate(username, host, cbh);
  }

  @Override
  public void authenticate(String username, String host, String auth)
      throws IOException, XMPPException {

    //auth = "DQAAALYAAACGaG2h7O_LFvwgPhwPP1auZMzs36LuzwigwYQ8B_OMi2vN_fmL8pJ976xgoJZw_zZ4VYgMjMAIRPRRa8Ducpq0TWqmkA84-IAK0o48izA5yXmGuLWVwhYNXHL2M0bZfO9eat2ocRe3LUnFZ7ogz1p2ydmv9ctWLSxhaedUkgytH5CRySrVCusROR5_GI202SyS-ErV-CPfWn-eZEIG8bNeAEEz4NuLt5rzMtuGfyglK3lbDjcRhwSlE2H_ZMcDr5I";
    
    String token = '\0' + username + '\0' + auth;
    Log.d(TAG, token);
    
    byte[] base64 = Base64.encode(token.getBytes(), Base64.NO_WRAP);
    
    Log.d(TAG, "Token:" + new String(base64));
    
    AuthMechanism reply = new AuthMechanism("X-GOOGLE-TOKEN", new String(base64));
    Log.d(TAG, reply.toXML());
    
    getSASLAuthentication().send(reply);
  
    /*
    GoogleTokenPacket stanza = new GoogleTokenPacket(base64);
    
    getSASLAuthentication().send(stanza);
    */
  }

  @Override
  public void challengeReceived(String arg0) throws IOException {
    Log.d(TAG, "Challenged!");
    super.challengeReceived(arg0);
  }

  @Override
  public void handle(Callback[] arg0) throws IOException,
      UnsupportedCallbackException {
    Log.d(TAG, "handle");
    super.handle(arg0);
  }

 
}
