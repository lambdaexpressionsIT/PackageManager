package apktool.brut.androlib;

/**
 * Created by steccothal
 * on Thursday 18 March 2021
 * at 10:22 AM
 */

import apktool.brut.androlib.err.InFileNotFoundException;
import apktool.brut.androlib.err.OutDirExistsException;
import apktool.brut.androlib.res.AndrolibResources;
import apktool.brut.androlib.res.data.ResTable;
import apktool.brut.common.BrutException;
import apktool.brut.directory.DirectoryException;
import apktool.brut.directory.ExtFile;
import apktool.brut.util.OS;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class ApkDecoder {
  public ApkDecoder() {
    this(new Androlib());
  }

  public ApkDecoder(Androlib androlib) {
    mAndrolib = androlib;
  }

  public void setApkFile(File apkFile) {
    if (mApkFile != null) {
      try {
        mApkFile.close();
      } catch (IOException ignored) {}
    }

    mApkFile = new ExtFile(apkFile);
    mResTable = null;
  }

  public void setOutDir(File outDir){
    mOutDir = outDir;
  }

  public void decode() throws AndrolibException, IOException, DirectoryException {
    try {
      File outDir = getOutDir();
      AndrolibResources.sKeepBroken = false;

      if (outDir.exists()) {
        throw new OutDirExistsException();
      }

      if (!mApkFile.isFile() || !mApkFile.canRead()) {
        throw new InFileNotFoundException();
      }

      try {
        OS.rmdir(outDir);
      } catch (BrutException ex) {
        throw new AndrolibException(ex);
      }
      outDir.mkdirs();

      LOGGER.info("Using Apktool on " + mApkFile.getName());

      if (hasResources()) {
        if (hasManifest()) {
          mAndrolib.decodeManifestWithResources(mApkFile, outDir, getResTable());
        }
        mAndrolib.decodeResourcesFull(mApkFile, outDir, getResTable());
      } else {
        // if there's no resources.arsc, decode the manifest without looking
        // up attribute references
        if (hasManifest()) {
          mAndrolib.decodeManifestFull(mApkFile, outDir, getResTable());
        }
      }
    } catch (Exception ex) {
      throw ex;
    } finally {
      try {
        mApkFile.close();
      } catch (IOException ignored) {}
    }
  }

  public ResTable getResTable() throws AndrolibException {
    if (mResTable == null) {
      boolean hasResources = hasResources();
      boolean hasManifest = hasManifest();
      if (! (hasManifest || hasResources)) {
        throw new AndrolibException(
            "Apk doesn't contain either AndroidManifest.xml file or resources.arsc file");
      }
      mResTable = mAndrolib.getResTable(mApkFile, hasResources);
    }
    return mResTable;
  }

  public boolean hasManifest() throws AndrolibException {
    try {
      return mApkFile.getDirectory().containsFile("AndroidManifest.xml");
    } catch (DirectoryException ex) {
      throw new AndrolibException(ex);
    }
  }

  public boolean hasResources() throws AndrolibException {
    try {
      return mApkFile.getDirectory().containsFile("resources.arsc");
    } catch (DirectoryException ex) {
      throw new AndrolibException(ex);
    }
  }

  public void close() throws IOException {
    if (mAndrolib != null) {
      mAndrolib.close();
    }
  }

  private File getOutDir() throws AndrolibException {
    if (mOutDir == null) {
      throw new AndrolibException("Out dir not set");
    }
    return mOutDir;
  }

  private final Androlib mAndrolib;

  private final static Logger LOGGER = Logger.getLogger(Androlib.class.getName());

  private ExtFile mApkFile;
  private File mOutDir;
  private ResTable mResTable;
}

