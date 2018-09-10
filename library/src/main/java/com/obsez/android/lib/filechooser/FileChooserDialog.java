package com.obsez.android.lib.filechooser;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.expeditors.gepha.expeditors.R;
import com.obsez.android.lib.filechooser.internals.ExtFileFilter;
import com.obsez.android.lib.filechooser.internals.RegexFileFilter;
import com.obsez.android.lib.filechooser.tool.DirAdapter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by coco on 6/7/15. Edited by Guiorgy on 10/09/18.
 */
public class FileChooserDialog extends ContextWrapper implements AdapterView.OnItemClickListener, DialogInterface.OnClickListener {
    @FunctionalInterface
    public interface Result {
        void onChoosePath(@NonNull String dir, @NonNull File dirFile);
    }

    private FileChooserDialog(Context context) {
        super(context);
    }

    public static FileChooserDialog newDialog(Context context){
        return new FileChooserDialog(context);
    }

    public FileChooserDialog setFilter(FileFilter ff) {
        setFilter(false, false, (String[]) null);
        this._fileFilter = ff;
        return this;
    }

    public FileChooserDialog setFilter(boolean dirOnly, boolean allowHidden, FileFilter ff) {
        setFilter(dirOnly, allowHidden, (String[]) null);
        this._fileFilter = ff;
        return this;
    }

    public FileChooserDialog setFilter(boolean allowHidden, String... suffixes) {
        return setFilter(false, allowHidden, suffixes);
    }

    public FileChooserDialog setFilter(boolean dirOnly, boolean allowHidden, String... suffixes) {
        this._dirOnly = dirOnly;
        if (suffixes == null) {
            this._fileFilter = dirOnly ? filterDirectoriesOnly : filterFiles;
        } else {
            this._fileFilter = new ExtFileFilter(_dirOnly, allowHidden, suffixes);
        }
        return this;
    }

    public FileChooserDialog setFilterRegex(boolean dirOnly, boolean allowHidden, String pattern, int flags) {
        this._dirOnly = dirOnly;
        this._fileFilter = new RegexFileFilter(_dirOnly, allowHidden, pattern, flags);
        return this;
    }

    public FileChooserDialog setFilterRegex(boolean dirOnly, boolean allowHidden, String pattern) {
        this._dirOnly = dirOnly;
        this._fileFilter = new RegexFileFilter(_dirOnly, allowHidden, pattern, Pattern.CASE_INSENSITIVE);
        return this;
    }

    public FileChooserDialog setStartFile(String startFile) {
        if (startFile != null) {
            _currentDir = new File(startFile);
        } else {
            _currentDir = Environment.getExternalStorageDirectory();
        }

        if (!_currentDir.isDirectory()) {
            _currentDir = _currentDir.getParentFile();
        }

        if (_currentDir == null) {
            _currentDir = Environment.getExternalStorageDirectory();
        }

        return this;
    }

    public FileChooserDialog setChosenListener(Result r) {
        this._result = r;
        return this;
    }

    @NonNull public FileChooserDialog setResources(@StringRes int titleRes, @StringRes int okRes, @StringRes int cancelRes) {
        this._titleRes = titleRes;
        this._okRes = okRes;
        this._negativeRes = cancelRes;
        return this;
    }
    
    public FileChooserDialog setResources(@NonNull String title, @NonNull String ok, @NonNull String cancel) {
        this._title = title;
        this._ok = ok;
        this._negative = cancel;
        return this;
    }

    public FileChooserDialog setIcon(@DrawableRes int iconId) {
        this._iconRes = iconId;
        return this;
    }

    public FileChooserDialog setLayoutView(@LayoutRes int layoutResId) {
        this._layoutRes = layoutResId;
        return this;
    }

    public FileChooserDialog setRowLayoutView(@LayoutRes int layoutResId) {
        this._rowLayoutRes = layoutResId;
        return this;
    }

    public FileChooserDialog setDateFormat() {
        return this.setDateFormat("yyyy/MM/dd HH:mm:ss");
    }

    public FileChooserDialog setDateFormat(String format) {
        this._dateFormat = format;
        return this;
    }

    public FileChooserDialog setNegativeButton(@NonNull String cancelTitle,
                                                final DialogInterface.OnClickListener listener) {
        this._negative = cancelTitle;
        this._negativeListener = listener;
        return this;
    }

    public FileChooserDialog setNegativeButtonListener(final DialogInterface.OnClickListener listener) {
        this._negativeListener = listener;
        return this;
    }

    /**
     * it's NOT recommended to use the `setOnCancelListener`, replace set `setNegativeButtonListener` pls.
     *
     * @deprecated will be removed at v1.2
     */
    public FileChooserDialog setOnCancelListener(final DialogInterface.OnCancelListener listener) {
        this._cancelListener2 = listener;
        return this;
    }

    public FileChooserDialog setFileIcons(final boolean tryResolveFileTypeAndIcon, final Drawable fileIcon,
                                           final Drawable folderIcon) {
        _adapterSetter = adapter -> {
            if (fileIcon != null) adapter.setDefaultFileIcon(fileIcon);
            if (folderIcon != null) adapter.setDefaultFolderIcon(folderIcon);
            adapter.setResolveFileType(tryResolveFileTypeAndIcon);
        };
        return this;
    }

    public FileChooserDialog setFileIconsRes(final boolean tryResolveFileTypeAndIcon, final int fileIcon,
                                              final int folderIcon) {
        _adapterSetter = adapter -> {
            if (fileIcon != -1) {
                adapter.setDefaultFileIcon(ContextCompat.getDrawable(getBaseContext(), fileIcon));
            }
            if (folderIcon != -1) {
                adapter.setDefaultFolderIcon(
                        ContextCompat.getDrawable(getBaseContext(), folderIcon));
            }
            adapter.setResolveFileType(tryResolveFileTypeAndIcon);
        };
        return this;
    }

    /**
     * @param setter you can customize the folder navi-adapter set `setter`
     * @return this
     */
    public FileChooserDialog setAdapterSetter(AdapterSetter setter) {
        _adapterSetter = setter;
        return this;
    }

    /**
     * @param cb give a hook at navigating up to a directory
     * @return this
     */
    public FileChooserDialog setNavigateUpTo(CanNavigateUp cb) {
        _folderNavUpCB = cb;
        return this;
    }

    /**
     * @param cb give a hook at navigating to a child directory
     * @return this
     */
    public FileChooserDialog setNavigateTo(CanNavigateTo cb) {
        _folderNavToCB = cb;
        return this;
    }

    public FileChooserDialog disableTitle(boolean b) {
        _disableTitle = b;
        return this;
    }

    public FileChooserDialog build() {
        DirAdapter adapter = refreshDirs();
        if (_adapterSetter != null) {
            _adapterSetter.apply(adapter);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
        //builder.setTitle(R.string.dlg_choose dir_title);
        if (!_disableTitle) builder.setTitle(_title);
        builder.setAdapter(adapter, this);

        if (this._iconRes != -1) {
            builder.setIcon(this._iconRes);
        }

        if (-1 != this._layoutRes) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setView(this._layoutRes);
            }
        }

        if (_dirOnly) {
            builder.setPositiveButton(_ok, (dialog, id) -> {
                if (_result != null) {
                    if (_dirOnly) {
                        _result.onChoosePath(_currentDir.getAbsolutePath(), _currentDir);
                    }
                }
                dialog.dismiss();
            });
        }

        if (this._negativeListener == null) {
            this._negativeListener = (dialog, id) -> dialog.cancel();
        }
        builder.setNegativeButton(this._negative, this._negativeListener);

        if (this._cancelListener2 != null) {
            builder.setOnCancelListener(_cancelListener2);
        }

        _alertDialog = builder.create();
        _list = _alertDialog.getListView();
        _list.setOnItemClickListener(this);
        return this;
    }

    public FileChooserDialog show() {
        //if (_result == null)
        //    throw new RuntimeException("no chosenListener defined. use setChosenListener() at first.");
        if (_alertDialog == null || _list == null) {
            throw new RuntimeException("call build() before show().");
        }

        // Check for permissions if SDK version is >= 23
        if (Build.VERSION.SDK_INT >= 23) {
            final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 0;
            int permissionCheck = ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            //if = permission granted
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                _alertDialog.show();
            } else {
                ActivityCompat.requestPermissions((Activity) getBaseContext(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
            }
        } else {
            _alertDialog.show();
        }

        return this;
    }

    private void listDirs() {
        _entries.clear();

        // Get files
        File[] files = _currentDir.listFiles(_fileFilter);

        // Add the ".." entry
        if (_currentDir.getParentFile() != null && !_currentDir.getParent().equals("/storage/emulated")) {
            _entries.add(new File("../"));
        }

        if (files != null) {
            List<File> dirList = new LinkedList<>();
            for (File f : files) {
                if (f.isDirectory()) {
                    if (!f.getName().startsWith(".")) {
                        dirList.add(f);
                    }
                }
            }
            sortByName(dirList);
            _entries.addAll(dirList);

            List<File> fileList = new LinkedList<>();
            for (File f : files) {
                if (!f.isDirectory()) {
                    if (!f.getName().startsWith(".")) {
                        fileList.add(f);
                    }
                }
            }
            sortByName(fileList);
            _entries.addAll(fileList);
        }
    }

    void sortByName(List<File> list) {
        Collections.sort(list, (f1, f2) -> f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase()));
    }
    
    /**
     * @deprecated better use listDirs as it sorts directories and files separately
     */
    @Deprecated
    private void listDirsUncategorised() {
        _entries.clear();

        // Get files
        File[] files = _currentDir.listFiles();

        // Add the ".." entry
        if (_currentDir.getParent() != null) {
            _entries.add(new File(".."));
        }

        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory()) {
                    continue;
                }

                _entries.add(file);
            }
        }

        sortByName(_entries);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View list, int pos, long id) {
        if (pos < 0 || pos >= _entries.size()) {
            return;
        }

        File file = _entries.get(pos);
        if (file.getName().equals("../") || file.getName().equals("..")) {
            File f = _currentDir.getParentFile();
            if (_folderNavUpCB == null) _folderNavUpCB = _defaultNavUpCB;
            if (_folderNavUpCB.canUpTo(f)) {
                _currentDir = f;
            }
        } else {
            if (file.isDirectory()) {
                if (_folderNavToCB == null) _folderNavToCB = _defaultNavToCB;
                if (_folderNavToCB.canNavigate(file)) {
                    _currentDir = file;
                }
            } else {
                if (!_dirOnly) {
                    if (_result != null) {
                        _result.onChoosePath(file.getAbsolutePath(), file);
                        _alertDialog.dismiss();
                        return;
                    }
                }
            }
        }
        refreshDirs();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        //
    }

    private DirAdapter refreshDirs() {
        listDirs();
        DirAdapter adapter = new DirAdapter(getBaseContext(), _entries, _rowLayoutRes != -1 ? _rowLayoutRes : R.layout.li_row_textview, this._dateFormat);
        if (_adapterSetter != null) {
            _adapterSetter.apply(adapter);
        }
        if (_list != null) {
            _list.setAdapter(adapter);
        }
        return adapter;
    }

    private List<File> _entries = new ArrayList<>();
    private File _currentDir;
    private AlertDialog _alertDialog;
    private ListView _list;
    private Result _result = null;
    private boolean _dirOnly;
    private FileFilter _fileFilter;
    private @StringRes int _titleRes = 0, _okRes = 0, _negativeRes = 0;
    private @NonNull
    String _title = "Select a file", _ok = "Choose", _negative = "Cancel";
    private @DrawableRes
    int _iconRes = -1;
    private @LayoutRes
    int _layoutRes = -1;
    private @LayoutRes
    int _rowLayoutRes = -1;
    private String _dateFormat;
    private DialogInterface.OnClickListener _negativeListener;
    private DialogInterface.OnCancelListener _cancelListener2;
    private boolean _disableTitle;

    @FunctionalInterface
    public interface AdapterSetter {
        void apply(DirAdapter adapter);
    }

    private AdapterSetter _adapterSetter = null;

    private final static FileFilter filterDirectoriesOnly = File::isDirectory;

    private final static FileFilter filterFiles = file -> !file.isHidden();

    @FunctionalInterface
    public interface CanNavigateUp {
        boolean canUpTo(File dir);
    }

    @FunctionalInterface
    public interface CanNavigateTo {
        boolean canNavigate(File dir);
    }

    private CanNavigateUp _folderNavUpCB;
    private CanNavigateTo _folderNavToCB;

    private final static CanNavigateUp _defaultNavUpCB = dir -> dir != null && dir.canRead();

    private final static CanNavigateTo _defaultNavToCB = dir -> true;

}
