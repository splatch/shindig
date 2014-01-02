/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.shindig.api.auth;

import java.util.Map;

/**
 * An abstract representation of a signing token.
 * Use in conjunction with @code SecurityTokenCodec.
 */
public interface SecurityToken {

  /**
   * @return the owner from the token, or null if there is none.
   */
  String getOwnerId();

  /**
   * @return the viewer from the token, or null if there is none.
   */
  String getViewerId();

  /**
   * @return the application id from the token, or null if there is none.
   */
  String getAppId();

  /**
   * @return the domain from the token, or null if there is none.
   */
  String getDomain();

  /**
   * @return The container.
   */
  String getContainer();

  /**
   * @return the URL of the application
   */
  String getAppUrl();

  /**
   * @return the module ID of the application
   */
  long getModuleId();

  /**
   * @return The time in seconds since epoc that this token expires or
   *   <code>null</code> if unknown or indeterminate.
   */
  Long getExpiresAt();

  /**
   * @return true if the token is no longer valid.
   */
  boolean isExpired();

  /**
   * @return an updated version of the token to return to the gadget, or null
   * if there is no need to update the token.
   */
  String getUpdatedToken();

  /**
   * @return the authentication mechanism used to generate this security token
   * @see AuthenticationMode
   */
  String getAuthenticationMode();

  /**
   * @return a string formatted JSON object from the container, or null if there
   * is no JSON from the container.
   */
  String getTrustedJson();

  /**
   * @return true if the token is for an anonymous viewer/owner
   */
  boolean isAnonymous();

  /**
   * @return the URL being used by the current request and null if not specified.
   *
   * The returned URL must contain at least protocol, host, and port.
   *
   * The returned URL may contain path or query parameters.
   *
   */
  String getActiveUrl();


  public enum Keys {
    OWNER("o") {
      public String getValue(SecurityToken token) {
        return token.getOwnerId();
      }
      public void loadFromMap(SecurityToken token, Map<String, String> map) {
        token.setOwnerId(map.get(key));
      }
    },
    VIEWER("v") {
      public String getValue(SecurityToken token) {
        return token.getViewerId();
      }
      public void loadFromMap(SecurityToken token, Map<String, String> map) {
        token.setViewerId(map.get(key));
      }
    },
    APP_ID("i") {
      public String getValue(SecurityToken token) {
        return token.getAppId();
      }
      public void loadFromMap(SecurityToken token, Map<String, String> map) {
        token.setAppId(map.get(key));
      }
    },
    DOMAIN("d") {
      public String getValue(SecurityToken token) {
        return token.getDomain();
      }
      public void loadFromMap(SecurityToken token, Map<String, String> map) {
        token.setDomain(map.get(key));
      }
    },
    CONTAINER("c") {
      public String getValue(SecurityToken token) {
        return token.getContainer();
      }
      public void loadFromMap(SecurityToken token, Map<String, String> map) {
        token.setContainer(map.get(key));
      }
    },
    APP_URL("u") {
      public String getValue(SecurityToken token) {
        return token.getAppUrl();
      }
      public void loadFromMap(SecurityToken token, Map<String, String> map) {
        token.setAppUrl(map.get(key));
      }
    },
    MODULE_ID("m") {
      public String getValue(SecurityToken token) {
        long value = token.getModuleId();
        if (value == 0) {
          return null;
        }
        return Long.toString(token.getModuleId(), 10);
      }
      public void loadFromMap(SecurityToken token, Map<String, String> map) {
        String value = map.get(key);
        if (value != null) {
          token.setModuleId(Long.parseLong(value, 10));
        }
      }
    },
    EXPIRES("x") {
      public String getValue(SecurityToken token) {
        Long value = token.getExpiresAt();
        if (value == null) {
          return null;
        }
        return Long.toString(token.getExpiresAt(), 10);
      }
      public void loadFromMap(SecurityToken token, Map<String, String> map) {
        String value = map.get(key);
        if (value != null) {
          token.setExpiresAt(Long.parseLong(value, 10));
        }
      }
    },
    TRUSTED_JSON("j") {
      public String getValue(SecurityToken token) {
        return token.getTrustedJson();
      }
      public void loadFromMap(SecurityToken token, Map<String, String> map) {
        token.setTrustedJson(map.get(key));
      }
    };

    protected String key;
    private Keys(String key) {
      this.key = key;
    }

    /**
     * @return The key this {@link Keys} is bound to.
     */
    public String getKey() {
      return key;
    }

    /**
     * Gets the {@link String} value from the {@link SecurityToken} using the getter that
     * this {@link Keys} is bound to.
     *
     * @param token The token to get the value from.
     * @return The value
     */
    public abstract String getValue(SecurityToken token);

    /**
     * Loads from the map the value bound to this {@link Keys} and sets it on the
     * {@link SecurityToken}
     *
     * @param token The token to insert set the value on.
     * @param map The map to read the value from.
     */
    public abstract void loadFromMap(SecurityToken token, Map<String, String> map);
  }


SecurityToken setOwnerId(String ownerId);

SecurityToken setTrustedJson(String trustedJson);

SecurityToken setExpiresAt(Long expiresAt);

SecurityToken setModuleId(Long moduleId);

SecurityToken setAppUrl(String appUrl);

SecurityToken setContainer(String container);

SecurityToken setDomain(String domain);

SecurityToken setAppId(String appId);

SecurityToken setViewerId(String string);

SecurityToken setAuthenticationMode(AuthenticationMode mode);

}
