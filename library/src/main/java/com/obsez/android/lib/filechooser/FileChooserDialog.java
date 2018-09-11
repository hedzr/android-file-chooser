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
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.obsez.android.lib.filechooser.internals.ExtFileFilter;
import com.obsez.android.lib.filechooser.internals.RegexFileFilter;
import com.obsez.android.lib.filechooser.tool.DirAdapter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by coco on 6/7/15. Edited by Guiorgy on 10/09/18.
 */
public class FileChooserDialog extends ContextWrapper implements AdapterView.OnItemClickListener, DialogInterface.OnClickListener {
    @FunctionalInterface
    public interface OnChosenListener{
        void onChoosePath(@NonNull String dir, @NonNull File dirFile);
    }

    private FileChooserDialog(@NonNull Context context) {
        super(context);
    }

    @NonNull public static FileChooserDialog newDialog(@NonNull Context context){
        return new FileChooserDialog(context);
    }

    @NonNull public FileChooserDialog setFilter(@NonNull FileFilter ff) {
        setFilter(false, false, (String[]) null);
        this._fileFilter = ff;
        return this;
    }

    @NonNull public FileChooserDialog setFilter(boolean dirOnly, boolean allowHidden, @NonNull FileFilter ff) {
        setFilter(dirOnly, allowHidden, (String[]) null);
        this._fileFilter = ff;
        return this;
    }

    @NonNull public FileChooserDialog setFilter(boolean allowHidden, @Nullable String... suffixes) {
        return setFilter(false, allowHidden, suffixes);
    }

    @NonNull public FileChooserDialog setFilter(boolean dirOnly, boolean allowHidden, @Nullable String... suffixes) {
        this._dirOnly = dirOnly;
        if (suffixes == null) {
            this._fileFilter = dirOnly ? filterDirectoriesOnly : filterFiles;
        } else {
            this._fileFilter = new ExtFileFilter(_dirOnly, allowHidden, suffixes);
        }
        return this;
    }

    @NonNull public FileChooserDialog setFilterRegex(boolean dirOnly, boolean allowHidden, @NonNull String pattern, int flags) {
        this._dirOnly = dirOnly;
        this._fileFilter = new RegexFileFilter(_dirOnly, allowHidden, pattern, flags);
        return this;
    }

    @NonNull public FileChooserDialog setFilterRegex(boolean dirOnly, boolean allowHidden, @NonNull String pattern) {
        this._dirOnly = dirOnly;
        this._fileFilter = new RegexFileFilter(_dirOnly, allowHidden, pattern, Pattern.CASE_INSENSITIVE);
        return this;
    }

    @NonNull public FileChooserDialog setStartFile(@Nullable String startFile) {
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

    @NonNull public FileChooserDialog setOnChosenListener(@NonNull OnChosenListener r) {
        this._result = r;
        return this;
    }

    @NonNull public FileChooserDialog setCancelable(boolean cancelable){
        this._cancelable = cancelable;
        return this;
    }

    @NonNull public FileChooserDialog setOnDismissListener(@NonNull DialogInterface.OnDismissListener listener) {
        if(Build.VERSION.SDK_INT >= 17){
            this._onDismissListener = listener;
        }
        return this;
    }

    @NonNull public FileChooserDialog setOnBackPressedListener(OnBackPressedListener listener){
        this._onBackPressed = listener;
        return this;
    }

    @NonNull public FileChooserDialog setResources(@StringRes int titleRes, @StringRes int okRes, @StringRes int cancelRes) {
        this._titleRes = titleRes;
        this._okRes = okRes;
        this._negativeRes = cancelRes;
        return this;
    }

    @NonNull public FileChooserDialog setResources(@Nullable String title, @Nullable String ok, @Nullable String cancel) {
        if(title != null){
            this._title = title;
            this._titleRes = 0;
        }
        if(ok != null){
            this._ok = ok;
            this._okRes = 0;
        }
        if(cancel != null){
            this._negative = cancel;
            this._negativeRes = 0;
        }
        return this;
    }

    @NonNull public FileChooserDialog setIcon(@DrawableRes int iconId) {
        this._iconRes = iconId;
        return this;
    }

    @NonNull public FileChooserDialog setLayoutView(@LayoutRes int layoutResId) {
        this._layoutRes = layoutResId;
        return this;
    }

    @NonNull public FileChooserDialog setRowLayoutView(@LayoutRes int layoutResId) {
        this._rowLayoutRes = layoutResId;
        return this;
    }

    @NonNull public FileChooserDialog setDateFormat() {
        return this.setDateFormat("yyyy/MM/dd HH:mm:ss");
    }

    @NonNull public FileChooserDialog setDateFormat(@NonNull String format) {
        this._dateFormat = format;
        return this;
    }

    @NonNull public FileChooserDialog setNegativeButton(@StringRes int cancelTitle, @NonNull final DialogInterface.OnClickListener listener) {
        this._negativeRes = cancelTitle;
        this._negativeListener = listener;
        return this;
    }

    @NonNull public FileChooserDialog setNegativeButton(@NonNull String cancelTitle, @NonNull final DialogInterface.OnClickListener listener) {
        this._negative = cancelTitle;
        this._negativeRes = 0;
        this._negativeListener = listener;
        return this;
    }

    @NonNull public FileChooserDialog setNegativeButtonListener(@NonNull final DialogInterface.OnClickListener listener) {
        this._negativeListener = listener;
        return this;
    }

    /**
     * it's NOT recommended to use the `setOnCancelListener`, replace with `setNegativeButtonListener` pls.
     *
     * @deprecated will be removed at v1.2
     */
    @NonNull public FileChooserDialog setOnCancelListener(@NonNull final DialogInterface.OnCancelListener listener) {
        this._onCancelListener = listener;
        return this;
    }

    @NonNull public FileChooserDialog setFileIcons(final boolean tryResolveFileTypeAndIcon, @Nullable final Drawable fileIcon, @Nullable final Drawable folderIcon) {
        _adapterSetter = new AdapterSetter(){
            @Override
            public void apply(@NonNull final DirAdapter adapter){
                if(fileIcon != null)
                    adapter.setDefaultFileIcon(fileIcon);
                if(folderIcon != null)
                    adapter.setDefaultFolderIcon(folderIcon);
                adapter.setResolveFileType(tryResolveFileTypeAndIcon);
            }
        };
        return this;
    }

    @NonNull public FileChooserDialog setFileIconsRes(final boolean tryResolveFileTypeAndIcon, final int fileIcon, final int folderIcon) {
        _adapterSetter = new AdapterSetter(){
            @Override
            public void apply(@NonNull final DirAdapter adapter){
                if(fileIcon != -1){
                    adapter.setDefaultFileIcon(ContextCompat.getDrawable(FileChooserDialog.this.getBaseContext(), fileIcon));
                }
                if(folderIcon != -1){
                    adapter.setDefaultFolderIcon(
                            ContextCompat.getDrawable(FileChooserDialog.this.getBaseContext(), folderIcon));
                }
                adapter.setResolveFileType(tryResolveFileTypeAndIcon);
            }
        };
        return this;
    }

    /**
     * @param setter you can customize the folder navi-adapter set `setter`
     * @return this
     */
    @NonNull public FileChooserDialog setAdapterSetter(@NonNull AdapterSetter setter) {
        _adapterSetter = setter;
        return this;
    }

    /**
     * @param cb give a hook at navigating up to a directory
     * @return this
     */
    @NonNull public FileChooserDialog setNavigateUpTo(@NonNull CanNavigateUp cb) {
        _folderNavUpCB = cb;
        return this;
    }

    /**
     * @param cb give a hook at navigating to a child directory
     * @return this
     */
    @NonNull public FileChooserDialog setNavigateTo(@NonNull CanNavigateTo cb) {
        _folderNavToCB = cb;
        return this;
    }

    @NonNull public FileChooserDialog disableTitle(boolean b) {
        _disableTitle = b;
        return this;
    }

    @NonNull public FileChooserDialog build() {
        DirAdapter adapter = refreshDirs();
        if (this._adapterSetter != null) {
            this._adapterSetter.apply(adapter);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
        if (!this._disableTitle){
            if(this._titleRes == 0) builder.setTitle(this._title);
              else builder.setTitle(this._titleRes);
        }
        builder.setAdapter(adapter, this);

        if (this._iconRes != -1) {
            builder.setIcon(this._iconRes);
        }

        if (-1 != this._layoutRes) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setView(this._layoutRes);
            }
        }

        if (this._dirOnly) {
            final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener(){
                @Override
                public void onClick(final DialogInterface dialog, final int which){
                    if(FileChooserDialog.this._result != null){
                        if(FileChooserDialog.this._dirOnly){
                            FileChooserDialog.this._result.onChoosePath(FileChooserDialog.this._currentDir.getAbsolutePath(),FileChooserDialog.this. _currentDir);
                        }
                    }
                    dialog.dismiss();
                }
            };

            if(this._okRes == 0) builder.setPositiveButton(this._ok, listener);
              else builder.setPositiveButton(this._okRes, listener);
        }

        if (this._negativeListener == null) {
            this._negativeListener = new DialogInterface.OnClickListener(){
                @Override
                public void onClick(final DialogInterface dialog, final int id){
                    dialog.cancel();
                }
            };
        }

        if(this._negativeRes == 0) builder.setNegativeButton(this._negative, this._negativeListener);
          else builder.setNegativeButton(this._negativeRes, this._negativeListener);

        if (this._onCancelListener != null) {
            builder.setOnCancelListener(_onCancelListener);
        }

        if(Build.VERSION.SDK_INT >= 17){
            if(this._onDismissListener != null){
                builder.setOnDismissListener(this._onDismissListener);
            }
        }

        builder.setCancelable(this._cancelable)
               .setOnKeyListener(new DialogInterface.OnKeyListener(){
                   @Override
                   public boolean onKey(final DialogInterface dialog, final int keyCode, final KeyEvent event){
                       if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
                           FileChooserDialog.this._onBackPressed.onBackPressed((AlertDialog) dialog);
                       }
                       return true;
                   }
               });

        this._alertDialog = builder.create();
        this._list = this._alertDialog.getListView();
        this._list.setOnItemClickListener(this);
        return this;
    }

    @NonNull public FileChooserDialog show() {
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

    void sortByName(@NonNull List<File> list) {
        Collections.sort(list, new Comparator<File>(){
            @Override
            public int compare(final File f1, final File f2){
                return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
            }
        });
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
    public void onItemClick(@Nullable AdapterView<?> parent, @NonNull View list, int pos, long id) {
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
    public void onClick(@NonNull DialogInterface dialog, int which) {
        //
    }

    @NonNull private DirAdapter refreshDirs() {
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

    public void dismiss(){
        if(_alertDialog == null) return;
        _alertDialog.dismiss();
    }

    public void cancel(){
        if(_alertDialog == null) return;
        _alertDialog.cancel();
    }

    private List<File> _entries = new ArrayList<>();
    private File _currentDir;
    private AlertDialog _alertDialog;
    private ListView _list;
    private OnChosenListener _result = null;
    private boolean _dirOnly;
    private FileFilter _fileFilter;
    private @StringRes int _titleRes = 0, _okRes = 0, _negativeRes = 0;
    private @NonNull String _title = "Select a file", _ok = "Choose", _negative = "Cancel";
    private @DrawableRes int _iconRes = -1;
    private @LayoutRes int _layoutRes = -1;
    private @LayoutRes int _rowLayoutRes = -1;
    private String _dateFormat;
    private DialogInterface.OnClickListener _negativeListener;
    private DialogInterface.OnCancelListener _onCancelListener;
    private boolean _disableTitle;
    private boolean _cancelable = true;
    private DialogInterface.OnDismissListener _onDismissListener;

    @FunctionalInterface
    public interface AdapterSetter {
        void apply(@NonNull DirAdapter adapter);
    }

    private AdapterSetter _adapterSetter = null;

    private final static FileFilter filterDirectoriesOnly = new FileFilter(){
        @Override
        public boolean accept(final File file){
            return file.isDirectory();
        }
    };

    private final static FileFilter filterFiles = new FileFilter(){
        @Override
        public boolean accept(final File file){
            return !file.isHidden();
        }
    };

    @FunctionalInterface
    public interface CanNavigateUp {
        boolean canUpTo(@Nullable File dir);
    }

    @FunctionalInterface
    public interface CanNavigateTo {
        boolean canNavigate(@NonNull File dir);
    }

    private CanNavigateUp _folderNavUpCB;
    private CanNavigateTo _folderNavToCB;

    private final static CanNavigateUp _defaultNavUpCB = new CanNavigateUp(){
        @Override
        public boolean canUpTo(@Nullable final File dir){
            return dir != null && dir.canRead();
        }
    };

    private final static CanNavigateTo _defaultNavToCB = new CanNavigateTo(){
        @Override
        public boolean canNavigate(@NonNull final File dir){
            return true;
        }
    };

    @FunctionalInterface
    public interface OnBackPressedListener{
        void onBackPressed(@NonNull AlertDialog dialog);
    }

    private OnBackPressedListener _onBackPressed = (new OnBackPressedListener(){
        @Override
        public void onBackPressed(@NonNull final AlertDialog dialog){
            if(FileChooserDialog.this._entries.size() > 0
                    && (FileChooserDialog.this._entries.get(0).getName().equals("../") || FileChooserDialog.this._entries.get(0).getName().equals(".."))){
                FileChooserDialog.this.onItemClick(null, FileChooserDialog.this._list, 0, 0);
            } else{
                dialog.dismiss();
            }
        }
    });
}
