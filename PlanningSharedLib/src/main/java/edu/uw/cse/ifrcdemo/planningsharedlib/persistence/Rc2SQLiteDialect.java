/*
 * Copyright (c) 2016-2022 University of Washington
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *  Neither the name of the University of Washington nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY OF WASHINGTON AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF WASHINGTON OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package edu.uw.cse.ifrcdemo.planningsharedlib.persistence;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.SQLiteDialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.dialect.identity.IdentityColumnSupportImpl;
import org.hibernate.dialect.unique.DefaultUniqueDelegate;
import org.hibernate.dialect.unique.UniqueDelegate;
import org.hibernate.mapping.Column;

public class Rc2SQLiteDialect extends SQLiteDialect {
  private final UniqueDelegate uniqueDelegate;
  private final IdentityColumnSupport identityColumnSupport;

  public Rc2SQLiteDialect() {
    super();

    uniqueDelegate = new SQLiteUniqueDelegate(this);
    identityColumnSupport = new SQLiteIdentityColumnSupport();
  }

  @Override
  public UniqueDelegate getUniqueDelegate() {
    return uniqueDelegate;
  }

  @Override
  public IdentityColumnSupport getIdentityColumnSupport() {
    return identityColumnSupport;
  }

  private static class SQLiteUniqueDelegate extends DefaultUniqueDelegate {
    private SQLiteUniqueDelegate(Dialect dialect) {
      super( dialect );
    }
    @Override
    public String getColumnDefinitionUniquenessFragment(Column column) {
      return " unique";
    }
  }

  private static class SQLiteIdentityColumnSupport extends IdentityColumnSupportImpl {
    @Override
    public boolean supportsIdentityColumns() {
      return true;
    }

    @Override
    public boolean hasDataTypeInIdentityColumn() {
      return false;
    }

    @Override
    public String getIdentitySelectString(String table, String column, int type) {
      return "select last_insert_rowid()";
    }

    @Override
    public String getIdentityColumnString(int type) {
      return "integer";
    }
  }
}
