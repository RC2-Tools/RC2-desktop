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

package edu.uw.cse.ifrcdemo.sharedlib.util;

import java.util.Map;

public final class Rc2SpecificColumnsUtil {
    public static final String[] GENDER_COLUMN_VALUES = new String[]{"sex","gender","genero"};
    public static final String[] DOB_COLUMN_VALUES = new String[]{"birth_date", "birthdate", "birthDate"};
    public static final String[] FIRST_NAME_COLUMN_VALUES = new String[]{"first_name", "firstName", "nombres"};
    public static final String[] LAST_NAME_COLUMN_VALUES = new String[]{"last_name", "lastName", "apellidos"};

    public static final String getDateOfBirth(Map<String, String> row) {
        return findColumn(row,DOB_COLUMN_VALUES);
    }

    public static final String getGender(Map<String, String> row) {
        return findColumn(row,GENDER_COLUMN_VALUES);
    }

    public static final String getFirstName(Map<String, String> row) {
        return findColumn(row,FIRST_NAME_COLUMN_VALUES);
    }

    public static final String getLastName(Map<String, String> row) {
        return findColumn(row,LAST_NAME_COLUMN_VALUES);
    }


    private static final String findColumn(Map<String, String> row, String[] possibleColumnNames) {
        for (String key : possibleColumnNames) {
            if (row.containsKey(key)) {
                return row.get(key);
            }
        }
       return null;
    }

}
