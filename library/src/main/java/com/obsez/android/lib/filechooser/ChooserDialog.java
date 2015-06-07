package com.obsez.android.lib.filechooser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.obsez.android.lib.filechooser.internals.DirAdapter;
import com.obsez.android.lib.filechooser.internals.ExtFileFilter;
import com.obsez.android.lib.filechooser.internals.FileUtil;
import com.obsez.android.lib.filechooser.internals.RegexFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by coco on 6/7/15.
 */
public class ChooserDialog implements AdapterView.OnItemClickListener, DialogInterface.OnClickListener {

    public interface Result {
        void onChoosePath(String dir, File dirFile);
    }

    public ChooserDialog() {

    }

    public ChooserDialog with(Context cxt) {
        this.m_context = cxt;
        return this;
    }

    public ChooserDialog withFilter(FileFilter ff) {
        withFilter(false, false, (String[]) null);
        this._fileFilter = ff;
        return this;
    }

    public ChooserDialog withFilter(boolean dirOnly, boolean allowHidden, FileFilter ff) {
        withFilter(dirOnly, allowHidden, (String[]) null);
        this._fileFilter = ff;
        return this;
    }

    public ChooserDialog withFilter(boolean allowHidden, String... suffixes) {
        return withFilter(false, allowHidden, suffixes);
    }

    public ChooserDialog withFilter(boolean dirOnly, boolean allowHidden, String... suffixes) {
        this._dirOnly = dirOnly;
        if (suffixes == null)
            this._fileFilter = dirOnly ? filterDirectoriesOnly : filterFiles;
        else
            this._fileFilter = new ExtFileFilter(_dirOnly, allowHidden, suffixes);
        return this;
    }

    public ChooserDialog withFilterRegex(boolean dirOnly, boolean allowHidden, String pattern, int flags) {
        this._dirOnly = dirOnly;
        this._fileFilter = new RegexFileFilter(_dirOnly, allowHidden, pattern, flags);
        return this;
    }

    public ChooserDialog withFilterRegex(boolean dirOnly, boolean allowHidden, String pattern) {
        this._dirOnly = dirOnly;
        this._fileFilter = new RegexFileFilter(_dirOnly, allowHidden, pattern, Pattern.CASE_INSENSITIVE);
        return this;
    }

    public ChooserDialog withStartFile(String startFile) {
        if (startFile != null)
            m_currentDir = new File(startFile);
        else
            m_currentDir = Environment.getExternalStorageDirectory();
        return this;
    }

    public ChooserDialog withChosenListener(Result r) {
        this.m_result = r;
        return this;
    }

    public ChooserDialog withResources(int titleRes, int okRes, int cancelRes) {
        this._titleRes = titleRes;
        this._okRes = okRes;
        this._cancelRes = cancelRes;
        return this;
    }

    public ChooserDialog build() {
        if (_titleRes == 0 || _okRes == 0 || _cancelRes == 0)
            throw new RuntimeException("withResources() should be called at first.");

        DirAdapter adapter = refreshDirs();

        AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
        //builder.setTitle(R.string.dlg_choose dir_title);
        builder.setTitle(_titleRes);
        builder.setAdapter(adapter, this);

        if (_dirOnly) {
            builder.setPositiveButton(_okRes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (m_result != null) {
                        if (_dirOnly)
                            m_result.onChoosePath(m_currentDir.getAbsolutePath(), m_currentDir);
                    }
                    dialog.dismiss();
                }
            });
        }

        builder.setNegativeButton(_cancelRes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        m_alertDialog = builder.create();
        m_list = m_alertDialog.getListView();
        m_list.setOnItemClickListener(this);
        return this;
    }

    public ChooserDialog show() {
        //if (m_result == null)
        //    throw new RuntimeException("no chosenListener defined. use withChosenListener() at first.");
        if (m_alertDialog == null || m_list == null)
            throw new RuntimeException("call build() before show().");
        m_alertDialog.show();
        return this;
    }




    private void listDirs() {
        m_entries.clear();

        // Get files
        File[] files = m_currentDir.listFiles(_fileFilter);

        // Add the ".." entry
        if (m_currentDir.getParent() != null)
            m_entries.add(new File(".."));

        if (files != null) {
            for (File file : files) {
                m_entries.add(file);
            }
        }

        Collections.sort(m_entries, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
            }
        });
    }

    private void listDirs2() {
        m_entries.clear();

        // Get files
        File[] files = m_currentDir.listFiles();

        // Add the ".." entry
        if (m_currentDir.getParent() != null)
            m_entries.add(new File(".."));

        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory())
                    continue;

                m_entries.add(file);
            }
        }

        Collections.sort(m_entries, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View list, int pos, long id) {
        if (pos < 0 || pos >= m_entries.size())
            return;

        File file = m_entries.get(pos);
        if (file.getName().equals(".."))
            m_currentDir = m_currentDir.getParentFile();
        else
            m_currentDir = file;

        if (!file.isDirectory()) {
            if (!_dirOnly) {
                if (m_result != null) {
                    m_result.onChoosePath(file.getAbsolutePath(), file);
                    m_alertDialog.dismiss();
                    return;
                }
            }
        }

        refreshDirs();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        //
    }

    DirAdapter refreshDirs(){
        listDirs();
        DirAdapter adapter = new DirAdapter(m_context, m_entries, R.layout.li_row_textview);
        if(m_list!=null)
            m_list.setAdapter(adapter);
        return adapter;
    }




    List<File> m_entries = new ArrayList<File>();
    File m_currentDir;
    Context m_context;
    AlertDialog m_alertDialog;
    ListView m_list;
    Result m_result = null;
    boolean _dirOnly;
    FileFilter _fileFilter;
    int _titleRes = R.string.choose_file, _okRes = R.string.title_choose, _cancelRes = R.string.dialog_cancel;

    static FileFilter filterDirectoriesOnly = new FileFilter() {
        public boolean accept(File file) {
            return file.isDirectory();
        }
    };

    static FileFilter filterFiles = new FileFilter() {
        public boolean accept(File file) {
            return !file.isHidden();
        }
    };



}
