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

package edu.uw.cse.ifrcdemo.sharedlib.model;

import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.Gender;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GenderTest {


    @Test
    void testMale() {
        assertEquals(Gender.MALE, Gender.valueOf("MALE"));
        assertEquals(Gender.MALE, Gender.RC2GenderValueOf("male"));
        assertEquals(Gender.MALE, Gender.RC2GenderValueOf("MALE"));
        assertEquals(Gender.MALE, Gender.RC2GenderValueOf("MAlE"));
        assertEquals(Gender.MALE, Gender.RC2GenderValueOf("mAlE"));
    }

    @Test
    void testFemale() {
        assertEquals(Gender.FEMALE, Gender.valueOf("FEMALE"));
        assertEquals(Gender.FEMALE, Gender.RC2GenderValueOf("FEmale"));
        assertEquals(Gender.FEMALE, Gender.RC2GenderValueOf("FEMALE"));
        assertEquals(Gender.FEMALE, Gender.RC2GenderValueOf("FEMAlE"));
        assertEquals(Gender.FEMALE, Gender.RC2GenderValueOf("femAlE"));
    }

    @Test
    void testNA() {
        assertEquals(Gender.NA, Gender.valueOf("NA"));
        assertEquals(Gender.NA, Gender.RC2GenderValueOf("NA"));
        assertEquals(Gender.NA, Gender.RC2GenderValueOf("na"));
        assertEquals(Gender.NA, Gender.RC2GenderValueOf("n/a"));
        assertEquals(Gender.NA, Gender.RC2GenderValueOf("N/A"));
        assertEquals(Gender.NA, Gender.RC2GenderValueOf("other"));
        assertEquals(Gender.NA, Gender.RC2GenderValueOf("NO_ANSWER"));
    }

}
