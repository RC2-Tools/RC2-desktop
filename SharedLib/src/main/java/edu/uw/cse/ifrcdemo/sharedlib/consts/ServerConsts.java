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

package edu.uw.cse.ifrcdemo.sharedlib.consts;

public class ServerConsts {
    public static final String SERVER_ADDR_DEFAULT = "https://server-url.com";
    public static final String APP_ID_DEFAULT = "default";
    public static final String USERNAME_DEFAULT = GenConsts.EMPTY_STRING;
    public static final String PASSWORD_DEFAULT = GenConsts.EMPTY_STRING;

    public static final int RESET_FINISH_WAIT = 5000;
    public static final int PUSH_FINISH_WAIT = 5000;

    public static final int MAX_BATCH_SIZE = 500;

    public static final String FILES_KEY = "files";
    public static final String FILENAME_KEY = "filename";
    public static final String TABLE = "table";
    public static final String ETAG = "etag";
    public static final String SYNC_PROTOCOL_VERSION = "2";

    public static final String FORCE_UPDATE_OP = "FORCE_UPDATE";
    public static final String UPDATE_OP = "UPDATE";
    public static final String NEW_OP = "NEW";
    public static final String DELETE_OP = "DELETE";
    public static final String OP_STR = "operation";

    public static final String WRITER_ROW_ID = "row Id:";
    public static final String WRITER_HAD_OUTCOME = "had outcome";
    public static final String DEFAULT_OUTCOME_FILE_NAME = "outcomeFile.txt";
    }
