package com.obsez.android.lib.filechooser.tool;

import java.io.File;

public final class RootFile extends File {

    public RootFile(String pathname) {
        super(pathname);
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public long lastModified() {
        return 0L;
    }
}
