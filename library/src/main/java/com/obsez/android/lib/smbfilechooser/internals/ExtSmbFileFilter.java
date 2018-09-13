package com.obsez.android.lib.smbfilechooser.internals;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;

/**
 * Created by coco on 6/7/15. Edited by Guiorgy on 10/09/18.
 */
public class ExtSmbFileFilter implements SmbFileFilter{
    boolean m_allowHidden;
    boolean m_onlyDirectory;
    String[] m_ext;

    public ExtSmbFileFilter() {
        this(false, false);
    }

    public ExtSmbFileFilter(String... ext_list) {
        this(false, false, ext_list);
    }

    public ExtSmbFileFilter(boolean dirOnly, boolean hidden, String... ext_list) {
        m_allowHidden = hidden;
        m_onlyDirectory = dirOnly;
        m_ext = ext_list;
    }

    @Override
    public boolean accept(SmbFile pathname) throws SmbException{
        if (!m_allowHidden) {
            if (pathname.isHidden()) {
                return false;
            }
        }

        if (m_onlyDirectory) {
            if (!pathname.isDirectory()) {
                return false;
            }
        }

        if (m_ext == null) {
            return true;
        }

        if (pathname.isDirectory()) {
            return true;
        }

        String ext = FileUtil.getExtensionWithoutDot(pathname);
        for (String e : m_ext) {
            if (ext.equalsIgnoreCase(e)) {
                return true;
            }
        }
        return false;
    }

}
