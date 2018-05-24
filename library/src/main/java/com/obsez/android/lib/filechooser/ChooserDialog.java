package com.obsez.android.lib.filechooser;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.obsez.android.lib.filechooser.internals.ExtFileFilter;
import com.obsez.android.lib.filechooser.internals.RegexFileFilter;
import com.obsez.android.lib.filechooser.tool.DirAdapter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

    public ChooserDialog(Context cxt) {
        this._context = cxt;
    }

    public ChooserDialog with(Context cxt) {
        this._context = cxt;
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
        if (suffixes == null) {
            this._fileFilter = dirOnly ? filterDirectoriesOnly : filterFiles;
        } else {
            this._fileFilter = new ExtFileFilter(_dirOnly, allowHidden, suffixes);
        }
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
        if (startFile != null) {
            _currentDir = new File(startFile);
        } else {
            _currentDir = Environment.getExternalStorageDirectory();
        }

        if (!_currentDir.isDirectory()) {
            _currentDir = _currentDir.getParentFile();
        }

        return this;
    }

    public ChooserDialog withChosenListener(Result r) {
        this._result = r;
        return this;
    }

    public ChooserDialog withResources(@StringRes int titleRes, @StringRes int okRes, @StringRes int cancelRes) {
        this._titleRes = titleRes;
        this._okRes = okRes;
        this._negativeRes = cancelRes;
        return this;
    }

    public ChooserDialog withIcon(@DrawableRes int iconId) {
        this._iconRes = iconId;
        return this;
    }

    public ChooserDialog withLayoutView(@LayoutRes int layoutResId) {
        this._layoutRes = layoutResId;
        return this;
    }

    public ChooserDialog withRowLayoutView(@LayoutRes int layoutResId) {
        this._rowLayoutRes = layoutResId;
        return this;
    }

    public ChooserDialog withDateFormat() {
        return this.withDateFormat("yyyy/MM/dd HH:mm:ss");
    }

    public ChooserDialog withDateFormat(String format) {
        this._dateFormat = format;
        return this;
    }

    public ChooserDialog withNegativeButton(@StringRes int cancelTitle,
            final DialogInterface.OnClickListener listener) {
        this._negativeRes = cancelTitle;
        this._negativeListener = listener;
        return this;
    }

    public ChooserDialog withNegativeButtonListener(final DialogInterface.OnClickListener listener) {
        this._negativeListener = listener;
        return this;
    }

    /**
     * it's NOT recommended to use the `withOnCancelListener`, replace with `withNegativeButtonListener` pls.
     */
    public ChooserDialog withOnCancelListener(final DialogInterface.OnCancelListener listener) {
        this._cancelListener2 = listener;
        return this;
    }

    public ChooserDialog withFileIcons(final boolean tryResolveFileTypeAndIcon, final Drawable fileIcon,
            final Drawable folderIcon) {
        _adapterSetter = new AdapterSetter() {
            @Override
            public void apply(DirAdapter adapter) {
                if (fileIcon != null) adapter.setDefaultFileIcon(fileIcon);
                if (folderIcon != null) adapter.setDefaultFolderIcon(folderIcon);
                adapter.setResolveFileType(tryResolveFileTypeAndIcon);
            }
        };
        return this;
    }

    public ChooserDialog withFileIconsRes(final boolean tryResolveFileTypeAndIcon, final int fileIcon,
            final int folderIcon) {
        _adapterSetter = new AdapterSetter() {
            @Override
            public void apply(DirAdapter adapter) {
                if (fileIcon != -1) adapter.setDefaultFileIcon(ContextCompat.getDrawable(_context, fileIcon));
                if (folderIcon != -1) {
                    adapter.setDefaultFolderIcon(
                            ContextCompat.getDrawable(_context, folderIcon));
                }
                adapter.setResolveFileType(tryResolveFileTypeAndIcon);
            }
        };
        return this;
    }

    /**
     *
     * @param setter you can customize the folder navi-adapter with `setter`
     * @return this
     */
    public ChooserDialog withAdapterSetter(AdapterSetter setter) {
        _adapterSetter = setter;
        return this;
    }

    /**
     *
     * @param cb give a hook at navigating up to a directory
     * @return this
     */
    public ChooserDialog withNavigateUpTo(CanNavigateUp cb) {
        _folderNavUpCB = cb;
        return this;
    }

    /**
     *
     * @param cb give a hook at navigating to a child directory
     * @return this
     */
    public ChooserDialog withNavigateTo(CanNavigateTo cb) {
        _folderNavToCB = cb;
        return this;
    }

    public ChooserDialog build() {
        if (_titleRes == 0 || _okRes == 0 || _negativeRes == 0) {
            throw new RuntimeException("withResources() should be called at first.");
        }

        DirAdapter adapter = refreshDirs();
        if (_adapterSetter != null) {
            _adapterSetter.apply(adapter);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        //builder.setTitle(R.string.dlg_choose dir_title);
        builder.setTitle(_titleRes);
        builder.setAdapter(adapter, this);

        if (this._iconRes != -1) {
            builder.setIcon(this._iconRes);
        }

        if (-1 != this._layoutRes) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                builder.setView(this._layoutRes);
            }
        }

        if (_dirOnly) {
            builder.setPositiveButton(_okRes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (_result != null) {
                        if (_dirOnly) {
                            _result.onChoosePath(_currentDir.getAbsolutePath(), _currentDir);
                        }
                    }
                    dialog.dismiss();
                }
            });
        }

        if (this._negativeListener == null) {
            this._negativeListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            };
        }
        builder.setNegativeButton(this._negativeRes, this._negativeListener);

        if (this._cancelListener2 != null) {
            builder.setOnCancelListener(_cancelListener2);
        }

        _alertDialog = builder.create();
        _list = _alertDialog.getListView();
        _list.setOnItemClickListener(this);
        return this;
    }

    public ChooserDialog show() {
        //if (_result == null)
        //    throw new RuntimeException("no chosenListener defined. use withChosenListener() at first.");
        if (_alertDialog == null || _list == null) {
            throw new RuntimeException("call build() before show().");
        }

        // Check for permissions if SDK version is >= 23
        if (Build.VERSION.SDK_INT >= 23) {
            final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 0;

            ActivityCompat.requestPermissions((Activity) _context,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);

            int permissionCheck = ContextCompat.checkSelfPermission(_context,
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                _alertDialog.show();
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
        if (_currentDir.getParent() != null) {
            _entries.add(new File(".."));
        }

        if (files != null) {
            _entries.addAll(Arrays.asList(files));
        }

        Collections.sort(_entries, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
            }
        });
    }

    private void listDirs2() {
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

        Collections.sort(_entries, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View list, int pos, long id) {
        if (pos < 0 || pos >= _entries.size()) {
            return;
        }

        File file = _entries.get(pos);
        if (file.getName().equals("..")) {
            File f = _currentDir.getParentFile();
            if (_folderNavUpCB == null) _folderNavUpCB = _defaultNavUpCB;
            if (_folderNavUpCB.canUpTo(f)) {
                _currentDir = f;
                //} else {
                //    Toast.makeText(this._context, "Couldn't go up level", Toast.LENGTH_SHORT).show();
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
        DirAdapter adapter = new DirAdapter(_context, _entries,
                _rowLayoutRes != -1 ? _rowLayoutRes : R.layout.li_row_textview, this._dateFormat);
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
    private Context _context;
    private AlertDialog _alertDialog;
    private ListView _list;
    private Result _result = null;
    private boolean _dirOnly;
    private FileFilter _fileFilter;
    private @StringRes
    int _titleRes = R.string.choose_file, _okRes = R.string.title_choose, _negativeRes = R.string.dialog_cancel;
    private @DrawableRes
    int _iconRes = -1;
    private @LayoutRes
    int _layoutRes = -1;
    private @LayoutRes
    int _rowLayoutRes = -1;
    private String _dateFormat;
    private DialogInterface.OnClickListener _negativeListener;
    private DialogInterface.OnCancelListener _cancelListener2;

    public interface AdapterSetter {
        void apply(DirAdapter adapter);
    }

    private AdapterSetter _adapterSetter = null;

    private final static FileFilter filterDirectoriesOnly = new FileFilter() {
        public boolean accept(File file) {
            return file.isDirectory();
        }
    };

    private final static FileFilter filterFiles = new FileFilter() {
        public boolean accept(File file) {
            return !file.isHidden();
        }
    };


    public interface CanNavigateUp {
        boolean canUpTo(File dir);
    }

    public interface CanNavigateTo {
        boolean canNavigate(File dir);
    }

    private CanNavigateUp _folderNavUpCB;
    private CanNavigateTo _folderNavToCB;

    private final static CanNavigateUp _defaultNavUpCB = new CanNavigateUp() {
        @Override
        public boolean canUpTo(File dir) {
            return dir != null && dir.canRead();
        }
    };

    private final static CanNavigateTo _defaultNavToCB = new CanNavigateTo() {
        @Override
        public boolean canNavigate(File dir) {
            return true;
        }
    };

}
