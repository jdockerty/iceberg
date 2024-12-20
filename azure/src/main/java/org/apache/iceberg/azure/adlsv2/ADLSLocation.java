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
package org.apache.iceberg.azure.adlsv2;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.iceberg.exceptions.ValidationException;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

/**
 * This class represents a fully qualified location to a file or directory in Azure Data Lake
 * Storage Gen2 storage.
 *
 * <p>Locations follow a URI like structure to identify resources
 *
 * <pre>{@code abfs[s]://[<container>@]<storage account host>/<file path>}</pre>
 *
 * <p>See <a
 * href="https://learn.microsoft.com/en-us/azure/storage/blobs/data-lake-storage-introduction-abfs-uri#uri-syntax">Azure
 * Data Lake Storage URI</a>
 */
class ADLSLocation {
  private static final Pattern URI_PATTERN = Pattern.compile("^abfss?://([^/?#]+)(.*)?$");

  private final String storageAccount;
  private final String container;
  private final String path;

  /**
   * Creates a new ADLSLocation from a fully qualified URI.
   *
   * @param location fully qualified URI
   */
  ADLSLocation(String location) {
    Preconditions.checkArgument(location != null, "Invalid location: null");

    Matcher matcher = URI_PATTERN.matcher(location);

    ValidationException.check(matcher.matches(), "Invalid ADLS URI: %s", location);

    String authority = matcher.group(1);
    String[] parts = authority.split("@", -1);
    if (parts.length > 1) {
      this.container = parts[0];
      this.storageAccount = parts[1];
    } else {
      this.container = null;
      this.storageAccount = authority;
    }

    String uriPath = matcher.group(2);
    this.path = uriPath == null ? "" : uriPath.startsWith("/") ? uriPath.substring(1) : uriPath;
  }

  /** Returns Azure storage account. */
  public String storageAccount() {
    return storageAccount;
  }

  /** Returns Azure container name. */
  public Optional<String> container() {
    return Optional.ofNullable(container);
  }

  /** Returns ADLS path. */
  public String path() {
    return path;
  }
}
