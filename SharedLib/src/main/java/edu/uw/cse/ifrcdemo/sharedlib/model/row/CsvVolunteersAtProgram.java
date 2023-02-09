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

package edu.uw.cse.ifrcdemo.sharedlib.model.row;

import edu.uw.cse.ifrcdemo.sharedlib.model.stub.VolunteersAtProgram;

public class CsvVolunteersAtProgram extends BaseSyncRow implements VolunteersAtProgram {
  private String volunteerId;
  private String programId;

  @Override
  public String getVolunteerId() {
    return volunteerId;
  }

  @Override
  public void setVolunteerId(String volunteerId) {
    this.volunteerId = volunteerId;
  }

  @Override
  public String getProgramId() {
    return programId;
  }

  @Override
  public void setProgramId(String programId) {
    this.programId = programId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    CsvVolunteersAtProgram that = (CsvVolunteersAtProgram) o;

    if (volunteerId != null ? !volunteerId.equals(that.volunteerId) : that.volunteerId != null) return false;
    return programId != null ? programId.equals(that.programId) : that.programId == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (volunteerId != null ? volunteerId.hashCode() : 0);
    result = 31 * result + (programId != null ? programId.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "CsvVolunteersAtProgram{" +
        "volunteerId='" + volunteerId + '\'' +
        ", programId='" + programId + '\'' +
        "} " + super.toString();
  }
}
