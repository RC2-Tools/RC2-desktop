/*
 * Copyright (c) 2016-2022 University of Washington
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *  *  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *  * Neither the name of the University of Washington nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY OF WASHINGTON AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF WASHINGTON OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package edu.uw.cse.ifrcdemo.distplan.util;

import edu.uw.cse.ifrcdemo.distplan.consts.DbConsts;
import edu.uw.cse.ifrcdemo.translations.LogStr;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DbUtil {
  private static final Logger logger = LogManager.getLogger(DbUtil.class);

  public static Path copyDb() throws IOException {
    return copyDb(getDefaultDbPath());
  }

  public static Path copyDb(Path target) throws IOException {
    Objects.requireNonNull(target);

    if (Files.isDirectory(target)) {
      target = target.resolve(DbConsts.DB_EXTERNAL_FILENAME);
    }

    InputStream resource = DbUtil.class.getResourceAsStream(DbConsts.DB_IN_JAR_RESOURCE_NAME);

    if (resource != null) {
      logger.info(LogStr.LOG_FOUND_BUNDLED_DATABASE_COPYING);

      Files.copy(resource, target, StandardCopyOption.REPLACE_EXISTING);
    }

    return target;
  }

  public static EntityManagerFactory createEntityManagerFactory() {
    return createEntityManagerFactory(getDefaultDbPath().toAbsolutePath().toString());
  }

  public static EntityManagerFactory createEntityManagerFactory(String dbPath) {
    Objects.requireNonNull(dbPath);
    logger.info(LogStr.LOG_SETTING_DB_PATH_TO, dbPath);

    return Persistence.createEntityManagerFactory(
        DbConsts.DISTRIBUTION_DATABASE,
        buildPersistencePropertiesMap(dbPath)
    );
  }

  private static Map<String, String> buildPersistencePropertiesMap(String dbPath) {
    Objects.requireNonNull(dbPath);

    Map<String, String> props = new HashMap<>();

    props.put(
        DbConsts.PERSISTENCE_JDBC_URL_KEY,
        DbConsts.JDBC_SQLITE_PREFIX + dbPath
    );

    props.put("foreign_keys", "true");

    for (String key : DbConsts.PERSISTENCE_OVERRIDE_KEYS) {
      String value = System.getProperty(key);

      if (value != null) {
        logger.info(LogStr.LOG_PERSISTENCE_PROPERTY_OVERRIDE_SETTING_TO, key, value);
        props.put(key, value);
      }
    }

    return props;
  }

  private static Path getDefaultDbPath() {
    return PathUtil
        .getClassLocation(DbUtil.class)
        .resolve(DbConsts.DB_EXTERNAL_FILENAME);
  }
}
