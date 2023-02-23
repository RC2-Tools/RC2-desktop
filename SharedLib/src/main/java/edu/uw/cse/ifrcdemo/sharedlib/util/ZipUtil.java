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

import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

  public static boolean zipDirectory(Path pathToDirectoryToZip, Path zipOutputLocation) {
    FileOutputStream outputStream = null;
    ZipOutputStream zipOutputStream = null;
    try {
      outputStream = new FileOutputStream(zipOutputLocation.toFile());
      zipOutputStream = new ZipOutputStream(outputStream);

      File directoryToZip = pathToDirectoryToZip.toFile();
      zipFile(directoryToZip, directoryToZip.getName(), zipOutputStream);

      return true;
    } catch (IOException e) {
      // TODO: add error tracking

      e.printStackTrace();
      return false;
    } finally {
      if(zipOutputStream != null) {
        try {
          zipOutputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if(outputStream != null) {
        try {
          outputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOutputStream) throws IOException {
    if (fileToZip.isHidden()) {
      return;
    }

    if (fileToZip.isDirectory()) {
      if (fileName.endsWith(GenConsts.PATH_SEPARATOR)) {
        zipOutputStream.putNextEntry(new ZipEntry(fileName));
        zipOutputStream.closeEntry();
      } else {
        zipOutputStream.putNextEntry(new ZipEntry(fileName + GenConsts.PATH_SEPARATOR));
        zipOutputStream.closeEntry();
      }
      File[] children = fileToZip.listFiles();
      for (File childFile : children) {
        zipFile(childFile, fileName + GenConsts.PATH_SEPARATOR + childFile.getName(), zipOutputStream);
      }
      return;
    }

    FileInputStream fileInputStream = null;

    try {
      fileInputStream = new FileInputStream(fileToZip);
      ZipEntry zipEntry = new ZipEntry(fileName);
      zipOutputStream.putNextEntry(zipEntry);
      byte[] bytes = new byte[1024];
      int length;
      while ((length = fileInputStream.read(bytes)) >= 0) {
        zipOutputStream.write(bytes, 0, length);
      }
    } catch (IOException e) {
      throw e;
    } finally {
      if(fileInputStream != null) {
        fileInputStream.close();
      }
    }
  }

  public static void extractZip(Path pathToZip, Path outputPath) throws IOException {

    try (FileSystem zipFs = FileSystems.newFileSystem(pathToZip, ZipUtil.class.getClassLoader())) {
      Files.walkFileTree(zipFs.getPath(GenConsts.ROOT), new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          super.preVisitDirectory(dir, attrs);

          // concat string because dir has a leading slash
          Files.createDirectories(Paths.get(outputPath.toString() + dir.toString()));
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          super.visitFile(file, attrs);

          // concat string because dir has a leading slash
          Files.copy(file, Paths.get(outputPath.toString() + file.toString()), StandardCopyOption.REPLACE_EXISTING);
          return FileVisitResult.CONTINUE;
        }
      });
    }
  }

  public static void extractZipFromResource(String name, Path outputPath) throws IOException {
    FileAttribute<?>[] attrs = {};
    Path temp = Files.createTempFile(name, GenConsts.ZIP_FILE_EXTENSION, attrs);
    try {
      Files.copy(ZipUtil.class.getClassLoader().getResourceAsStream(name), temp, StandardCopyOption.REPLACE_EXISTING);

      extractZip(temp, outputPath);
    } finally {
      Files.deleteIfExists(temp);
    }
  }
}
