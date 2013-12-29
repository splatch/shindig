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
package org.apache.shindig.auth;

import java.util.EnumSet;
import java.util.Map;

import org.apache.shindig.api.auth.SecurityToken;
import org.apache.shindig.api.auth.SecurityTokenCodec;
import org.apache.shindig.api.time.TimeSource;
import org.apache.shindig.common.crypto.BlobExpiredException;
import org.apache.shindig.common.util.DefaultTimeSource;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

/**
 * A base class for SecurityToken Implementations.
 * Currently provides an isExpired() method and getters/setters for nearly
 * every field of the token.
 *
 * @since 2.0.0
 */
public abstract class AbstractSecurityToken implements SecurityToken {
  /** allow three minutes for clock skew */
  private static final long CLOCK_SKEW_ALLOWANCE = 180;

  public static final int DEFAULT_MAX_TOKEN_TTL = 3600; // 1 hour

  private static final TimeSource TIME_SOURCE = new DefaultTimeSource();

  private String ownerId;
  private String viewerId;
  private String appId;
  private String domain;
  private String container;
  private String appUrl;
  private long moduleId = 0;
  private Long expiresAt;
  private String trustedJson;
  private String activeUrl;
  private TimeSource timeSource = AbstractSecurityToken.TIME_SOURCE;
  private int tokenTTL;

  /**
   * This method is mostly used for test code to test the expire methods.
   *
   * @param timeSource The new {@link TimeSource} for this token to use.
   * @return This object.
   */
  @VisibleForTesting
  protected AbstractSecurityToken setTimeSource(TimeSource timeSource) {
    this.timeSource = timeSource;
    return this;
  }

  protected TimeSource getTimeSource() {
    return timeSource;
  }

  public String getOwnerId() {
    return ownerId;
  }

  public AbstractSecurityToken setOwnerId(String ownerId) {
    this.ownerId = ownerId;
    return this;
  }

  public String getViewerId() {
    return viewerId;
  }

  public AbstractSecurityToken setViewerId(String viewerId) {
    this.viewerId = viewerId;
    return this;
  }

  public String getAppId() {
    return appId;
  }

  public AbstractSecurityToken setAppId(String appId) {
    this.appId = appId;
    return this;
  }

  public String getDomain() {
    return domain;
  }

  public AbstractSecurityToken setDomain(String domain) {
    this.domain = domain;
    return this;
  }

  public String getContainer() {
    return container;
  }

  public AbstractSecurityToken setContainer(String container) {
    this.container = container;
    return this;
  }

  public String getAppUrl() {
    return appUrl;
  }

  public AbstractSecurityToken setAppUrl(String appUrl) {
    this.appUrl = appUrl;
    return this;
  }

  public long getModuleId() {
    return moduleId;
  }

  public AbstractSecurityToken setModuleId(long moduleId) {
    this.moduleId = moduleId;
    return this;
  }

  public Long getExpiresAt() {
    return expiresAt;
  }

  /**
   * Compute and set the expiration time for this token using the default TTL.
   *
   * @return This security token.
   * @see #setExpires(int)
   */
  public AbstractSecurityToken setExpires() {
    return setExpires(DEFAULT_MAX_TOKEN_TTL);
  }

  /**
   * Compute and set the expiration time for this token using the provided TTL.
   *
   * @param tokenTTL the time to live (in seconds) of the token
   * @return This security token.
   */
  public AbstractSecurityToken setExpires(int tokenTTL) {
    this.tokenTTL = tokenTTL;
    return (AbstractSecurityToken) setExpiresAt((getTimeSource().currentTimeMillis() / 1000) + getMaxTokenTTL());
  }

  /**
   * Set the expiration time for this token.
   *
   * @param expiresAt When this token expires, in seconds since epoch.
   * @return This security token.
   */
  public AbstractSecurityToken setExpiresAt(long expiresAt) {
    this.expiresAt = expiresAt;
    return this;
  }

  public String getTrustedJson() {
    return trustedJson;
  }

  public AbstractSecurityToken setTrustedJson(String trustedJson) {
    this.trustedJson = trustedJson;
    return this;
  }

  public boolean isExpired() {
    try {
      enforceNotExpired();
    } catch (BlobExpiredException e) {
      return true;
    }
    return false;
  }

  public AbstractSecurityToken enforceNotExpired() throws BlobExpiredException {
    Long expiresAt = getExpiresAt();
    if (expiresAt != null) {
      long maxTime = expiresAt + CLOCK_SKEW_ALLOWANCE;
      long now = getTimeSource().currentTimeMillis() / 1000;

      if (!(now < maxTime)) {
        throw new BlobExpiredException(now, maxTime);
      }
    }
    return this;
  }

  public String getActiveUrl() {
    return activeUrl;
  }

  protected AbstractSecurityToken setActiveUrl(String activeUrl) {
    this.activeUrl = activeUrl;
    return this;
  }

  /**
   * A {@link Map} representation of this {@link SecurityToken}.  Implementors that
   * handle additional keys not contained in {@link Keys} should override and
   * supplement the functionality of this method.
   *
   * @return A map of serialized token values keyed according to {@link Keys}.
   * @see #getMapKeys()
   * @see #loadFromMap(Map)
   */
  public Map<String, String> toMap() {
    Map<String, String> map = Maps.newHashMap();
    for (Keys key : getMapKeys()) {
      String value = key.getValue(this);
      if (value != null) {
        map.put(key.getKey(), key.getValue(this));
      }
    }
    return map;
  }

  /**
   * Returns the maximum allowable time (in seconds) for this token to live. Override this method
   * only if you are internal token that doesn't get serialized via
   * {@link SecurityTokenCodec#encodeToken(SecurityToken)}, e.g., OAuth state tokens. For all other
   * cases, the SecurityTokenCodec will handle the time to live of the token.
   *
   * @return Maximum allowable time in seconds for a token to live.
   * @see SecurityTokenCodec#getTokenTimeToLive(String)
   */
  protected int getMaxTokenTTL() {
    return this.tokenTTL;
  }

  /**
   * A helper to help load known supported keys from a provided map.
   *
   * @param map The map of values.
   * @see #getMapKeys()
   * @see #toMap()
   */
  protected AbstractSecurityToken loadFromMap(Map<String, String> map) {
    for (Keys key : getMapKeys()) {
      key.loadFromMap(this, map);
    }
    return this;
  }

  /**
   * This method will govern the effectiveness of the protected {@link #toMap()} and
   * {@link #loadFromMap(SecurityToken, Map)} helper methods.
   * <br><br>
   * If your implementation throws an exception on any of the get methods, you
   * should not include the associated key here, and those values should be handled
   * in an overridden implementation of {@link #toMap()} if they might contain
   * useful information.
   *
   * @return An EnumSet of the Enums supported by the implementation of this
   * {@link AbstractSecurityToken}.
   */
  protected abstract EnumSet<Keys> getMapKeys();
}
