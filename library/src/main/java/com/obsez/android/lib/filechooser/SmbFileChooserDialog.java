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
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.expeditors.gepha.expeditors.R;
import com.obsez.android.lib.filechooser.internals.ExtSmbFileFilter;
import com.obsez.android.lib.filechooser.internals.RegexSmbFileFilter;
import com.obsez.android.lib.filechooser.tool.SmbDirAdapter;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Pattern;

import jcifs.Config;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;

/**
 * Created by coco on 6/7/15.
 */
public class SmbFileChooserDialog extends ContextWrapper implements AdapterView.OnItemClickListener, DialogInterface.OnClickListener{
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor((ThreadFactory) runnable -> {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setName("SmbFileChooserDialog - Thread");
        thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    });

    @FunctionalInterface
    public interface OnChosenListener{
        void onChoosePath(@NonNull String path, @NonNull SmbFile file);
    }

    public SmbFileChooserDialog(@NonNull final Context context){
        super(context);
    }

    private SmbFileChooserDialog(@NonNull final Context context, @NonNull String serverIP) throws MalformedURLException{
        super(context);
        this._rootDirPath = "smb://" + serverIP + '/';
        this._rootDir = new SmbFile(this._rootDirPath);
        Config.setProperty("jcifs.netbios.wins", serverIP);
    }


    @NonNull public static SmbFileChooserDialog newDialog(@NonNull Context context){
        return new SmbFileChooserDialog(context);
    }

    @NonNull public static SmbFileChooserDialog newDialog(@NonNull Context context, @NonNull String serverIP) throws MalformedURLException{
        return new SmbFileChooserDialog(context, serverIP);
    }

    @NonNull public SmbFileChooserDialog setServer(@NonNull String serverIP) throws MalformedURLException{
        this._rootDirPath = "smb://" + serverIP + '/';
        this._rootDir = new SmbFile(this._rootDirPath);
        Config.setProperty("jcifs.netbios.wins", serverIP);
        return this;
    }

    @NonNull public SmbFileChooserDialog setFilter(@NonNull SmbFileFilter sff) {
        setFilter(false, false, (String[]) null);
        this._fileFilter = sff;
        return this;
    }

    @NonNull public SmbFileChooserDialog setFilter(boolean dirOnly, boolean allowHidden, @NonNull SmbFileFilter sff) {
        setFilter(dirOnly, allowHidden, (String[]) null);
        this._fileFilter = sff;
        return this;
    }

    @NonNull public SmbFileChooserDialog setFilter(boolean allowHidden, @Nullable String... suffixes) {
        return setFilter(false, allowHidden, suffixes);
    }

    @NonNull public SmbFileChooserDialog setFilter(boolean dirOnly, boolean allowHidden, @Nullable String... suffixes) {
        this._dirOnly = dirOnly;
        if (suffixes == null) {
            this._fileFilter = dirOnly ? filterDirectoriesOnly : filterFiles;
        } else {
            this._fileFilter = new ExtSmbFileFilter(_dirOnly, allowHidden, suffixes);
        }
        return this;
    }

    @NonNull public SmbFileChooserDialog setFilterRegex(boolean dirOnly, boolean allowHidden, @NonNull String pattern, int flags) {
        this._dirOnly = dirOnly;
        this._fileFilter = new RegexSmbFileFilter(_dirOnly, allowHidden, pattern, flags);
        return this;
    }

    @NonNull public SmbFileChooserDialog setFilterRegex(boolean dirOnly, boolean allowHidden, @NonNull String pattern) {
        this._dirOnly = dirOnly;
        this._fileFilter = new RegexSmbFileFilter(_dirOnly, allowHidden, pattern, Pattern.CASE_INSENSITIVE);
        return this;
    }

    @NonNull public SmbFileChooserDialog setStartFile(@Nullable String startFile) throws ExecutionException, InterruptedException{
        Future<SmbFileChooserDialog> ret = EXECUTOR.submit(() ->{
            if (startFile != null) {
                _currentDir = new SmbFile(startFile);
            } else {
                _currentDir = _rootDir;
            }

            if (!_currentDir.isDirectory()) {
                String parent = _currentDir.getParent();
                if(parent == null){
                    throw new MalformedURLException(startFile + " has no parent directory");
                }
                _currentDir = new SmbFile(parent);
            }

            if (_currentDir == null) {
                _currentDir = _rootDir;
            }
            return SmbFileChooserDialog.this;
        });
        return ret.get();
    }

    @NonNull public SmbFileChooserDialog setOnChosenListener(@NonNull OnChosenListener listener) {
        this._onChosenListener = listener;
        return this;
    }

    @NonNull public SmbFileChooserDialog setResources(@StringRes int titleRes, @StringRes int okRes, @StringRes int cancelRes) {
        this._titleRes = titleRes;
        this._okRes = okRes;
        this._negativeRes = cancelRes;
        return this;
    }

    @NonNull public SmbFileChooserDialog setResources(@NonNull String title, @NonNull String ok, @NonNull String cancel) {
        this._title = title;
        this._ok = ok;
        this._negative = cancel;
        return this;
    }

    @NonNull public SmbFileChooserDialog setIcon(@DrawableRes int iconId) {
        this._iconRes = iconId;
        return this;
    }

    // maybe... just maybe...
    /*@NonNull public SmbFileChooserDialog setIcon(@NonNull Drawable icon) {
        this._icon = icon;
        return this;
    }*/

    @NonNull public SmbFileChooserDialog setLayoutView(@LayoutRes int layoutResId) {
        this._layoutRes = layoutResId;
        return this;
    }

    // maybe... just maybe...
    /*@NonNull public SmbFileChooserDialog setLayoutView(@NonNull View layout) {
        this._layout = layout;
        return this;
    }*/

    @NonNull public SmbFileChooserDialog setRowLayoutView(@LayoutRes int layoutResId) {
        this._rowLayoutRes = layoutResId;
        return this;
    }

    // maybe... just maybe...
    /*@NonNull public SmbFileChooserDialog setRowLayoutView(@NonNull View layout) {
        this._rowLayout = layout;
        return this;
    }*/

    @NonNull public SmbFileChooserDialog setDefaultDateFormat() {
        return this.setDateFormat("yyyy/MM/dd HH:mm:ss");
    }

    @NonNull public SmbFileChooserDialog setDateFormat(@NonNull String format) {
        this._dateFormat = format;
        return this;
    }

    @NonNull public SmbFileChooserDialog setNegativeButton(@StringRes int cancelTitle, @NonNull final DialogInterface.OnClickListener listener) {
        this._negativeRes = cancelTitle;
        this._negativeListener = listener;
        return this;
    }

    @NonNull public SmbFileChooserDialog setNegativeButtonListener(final DialogInterface.OnClickListener listener) {
        this._negativeListener = listener;
        return this;
    }

    /**
     * it's NOT recommended to use the `setOnCancelListener`, replace with `setNegativeButtonListener` pls.
     *
     * @deprecated will be removed at v1.2
     */
    @NonNull public SmbFileChooserDialog setOnCancelListener(@NonNull final DialogInterface.OnCancelListener listener) {
        this._onCancelListener = listener;
        return this;
    }

    /**
     * @deprecated need to find a way to actually get the icons.
     */
    @Deprecated
    @NonNull public SmbFileChooserDialog setFileIcons(final boolean tryResolveFileTypeAndIcon, @Nullable final Drawable fileIcon, @Nullable final Drawable folderIcon) {
        _adapterSetter = adapter -> {
            if (fileIcon != null) adapter.setDefaultFileIcon(fileIcon);
            if (folderIcon != null) adapter.setDefaultFolderIcon(folderIcon);
            //noinspection deprecation
            adapter.setResolveFileType(tryResolveFileTypeAndIcon);
        };
        return this;
    }

    @NonNull public SmbFileChooserDialog setFileIcons(@Nullable final Drawable fileIcon, @Nullable final Drawable folderIcon) {
        _adapterSetter = adapter -> {
            if (fileIcon != null) adapter.setDefaultFileIcon(fileIcon);
            if (folderIcon != null) adapter.setDefaultFolderIcon(folderIcon);
        };
        return this;
    }

    /**
     * @deprecated need to find a way to actually get the icons.
     */
    @Deprecated
    @NonNull public SmbFileChooserDialog setFileIcons(final boolean tryResolveFileTypeAndIcon, final int fileIconResId, final int folderIconResId) {
        _adapterSetter = adapter -> {
            if (fileIconResId != -1) adapter.setDefaultFileIcon(ContextCompat.getDrawable(getBaseContext(), fileIconResId));
            if (folderIconResId != -1) adapter.setDefaultFolderIcon(ContextCompat.getDrawable(getBaseContext(), folderIconResId));
            //noinspection deprecation
            adapter.setResolveFileType(tryResolveFileTypeAndIcon);
        };
        return this;
    }

    @NonNull public SmbFileChooserDialog setFileIcons(final int fileIconResId, final int folderIconResId) {
        _adapterSetter = adapter -> {
            if (fileIconResId != -1) adapter.setDefaultFileIcon(ContextCompat.getDrawable(getBaseContext(), fileIconResId));
            if (folderIconResId != -1) adapter.setDefaultFolderIcon(ContextCompat.getDrawable(getBaseContext(), folderIconResId));
        };
        return this;
    }

    /**
     * @param setter you can customize the folder navi-adapter with `setter`
     * @return this
     */
    @NonNull public SmbFileChooserDialog setAdapterSetter(@NonNull AdapterSetter setter) {
        _adapterSetter = setter;
        return this;
    }

    /**
     * @param cb give a hook at navigating up to a directory
     * @return this
     */
    @NonNull public SmbFileChooserDialog setNavigateUpTo(@NonNull CanNavigateUp cb) {
        _folderNavUpCB = cb;
        return this;
    }

    /**
     * @param cb give a hook at navigating to a child directory
     * @return this
     */
    @NonNull public SmbFileChooserDialog setNavigateTo(@NonNull CanNavigateTo cb) {
        _folderNavToCB = cb;
        return this;
    }

    @NonNull public SmbFileChooserDialog disableTitle(boolean b) {
        _disableTitle = b;
        return this;
    }

    @NonNull public SmbFileChooserDialog build() throws ExecutionException, InterruptedException{
        AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());

        Future<Void> thread = EXECUTOR.submit(() ->{
            if (_titleRes != 0) SmbFileChooserDialog.this._title = getBaseContext().getResources().getString(SmbFileChooserDialog.this._titleRes);
            if (_okRes != 0) SmbFileChooserDialog.this._ok = getBaseContext().getResources().getString(SmbFileChooserDialog.this._okRes);
            if (_negativeRes != 0) SmbFileChooserDialog.this._negative = getBaseContext().getResources().getString(SmbFileChooserDialog.this._negativeRes);

            SmbDirAdapter adapter = refreshDirs();
            if (_adapterSetter != null) {
                _adapterSetter.apply(adapter);
            }

            if (!_disableTitle) builder.setTitle(_title);
            builder.setAdapter(adapter, SmbFileChooserDialog.this);

            if (SmbFileChooserDialog.this._iconRes != -1) {
                builder.setIcon(SmbFileChooserDialog.this._iconRes);
            }

            if (-1 != SmbFileChooserDialog.this._layoutRes) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder.setView(SmbFileChooserDialog.this._layoutRes);
                }
            }

            if (_dirOnly) {
                builder.setPositiveButton(_ok, (dialog, id) -> {
                    if (_onChosenListener != null) {
                        if (_dirOnly) {
                            _onChosenListener.onChoosePath(_currentDir.getPath(), _currentDir);
                        }
                    }
                    dialog.dismiss();
                });
            }

            if (SmbFileChooserDialog.this._negativeListener == null) {
                SmbFileChooserDialog.this._negativeListener = (dialog, id) -> dialog.cancel();
            }
            builder.setNegativeButton(SmbFileChooserDialog.this._negative, SmbFileChooserDialog.this._negativeListener);

            if (SmbFileChooserDialog.this._onCancelListener != null) {
                builder.setOnCancelListener(_onCancelListener);
            }

            return null;
        });
        thread.get();
        _alertDialog = builder.create();
        _list = _alertDialog.getListView();
        _list.setOnItemClickListener(SmbFileChooserDialog.this);
        return SmbFileChooserDialog.this;
    }

    @NonNull public SmbFileChooserDialog show() throws ExecutionException, InterruptedException{
        Future<SmbFileChooserDialog> ret = EXECUTOR.submit(() ->{
            if (_alertDialog == null || _list == null) {
                throw new RuntimeException("Dialog has not been built yet! (call .build() before .show())");
            }

            // Check for permissions if SDK version is >= 23
            if (Build.VERSION.SDK_INT >= 23) {
                final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 0;
                int permissionCheck = ContextCompat.checkSelfPermission(getBaseContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE);

                //if = permission granted
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    ((Activity) getBaseContext()).runOnUiThread(() -> _alertDialog.show());
                } else {
                    ActivityCompat.requestPermissions((Activity) getBaseContext(),
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
                }
            } else {
                ((Activity) getBaseContext()).runOnUiThread(() -> _alertDialog.show());
            }

            return SmbFileChooserDialog.this;
        });
        return ret.get();
    }

    private void listDirs() throws SmbException, MalformedURLException{
        /*Future ret = EXECUTOR.submit(() ->{
            try{*/
                _entries.clear();

                // Get files
                SmbFile[] files = _currentDir.listFiles(_fileFilter);

                // Add the ".." entry
                final String parent = _currentDir.getParent();
                /*if (parent != null && !parent.equalsIgnoreCase("smb://")) {
                    _entries.add(new SmbFile(parent));
                }*/
                if (parent != null && !parent.equalsIgnoreCase("smb://")) {
                    _entries.add(new SmbFile("smb://.."));
                }

                if (files != null) {
                    List<SmbFile> dirList = new LinkedList<SmbFile>();
                    for (SmbFile f : files) {
                        if (f.isDirectory()) {
                            if (!f.getName().startsWith(".")) {
                                dirList.add(f);
                            }
                        }
                    }

                    sortByName(dirList);
                    _entries.addAll(dirList);

                    List<SmbFile> fileList = new LinkedList<>();
                    for (SmbFile f : files) {
                        if (!f.isDirectory()) {
                            if (!f.getName().startsWith(".")) {
                                fileList.add(f);
                            }
                        }
                    }
                    sortByName(fileList);
                    _entries.addAll(fileList);
                }
            /*} catch(SmbException | MalformedURLException e){
                e.printStackTrace();
            }
        });
        ret.get();*/
    }

    void sortByName(@NonNull List<SmbFile> list) {
        Collections.sort(list, (f1, f2) -> f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase()));
    }

    /**
     * @deprecated better use listDirs as it sorts directories and files separately
     */
    @Deprecated
    private void listDirsUncategorised() throws ExecutionException, InterruptedException{
        Future ret = EXECUTOR.submit(() ->{
            try{
                _entries.clear();

                // Get files
                SmbFile[] files = _currentDir.listFiles();

                // Add the ".." entry
                final String parent = _currentDir.getParent();
                /*if (parent != null && !parent.equalsIgnoreCase("smb://")) {
                    _entries.add(new SmbFile(parent));
                }*/
                if (parent != null && !parent.equalsIgnoreCase("smb://")) {
                    _entries.add(new SmbFile(".."));
                }

                if (files != null) {
                    for (SmbFile file : files) {
                        if (!file.isDirectory()) {
                            continue;
                        }

                        _entries.add(file);
                    }
                }

                sortByName(_entries);
            } catch(MalformedURLException | SmbException e){
                e.printStackTrace();
            }
        });
        ret.get();
    }

    @Override
    public void onItemClick(@NonNull AdapterView<?> parent, @NonNull View list, int pos, long id) {
        Future ret = EXECUTOR.submit(() ->{
            try{
                if (pos < 0 || pos >= _entries.size()) {
                    return;
                }

                SmbFile file = _entries.get(pos);
                //Log.d("TAG", file.getName(),)
                if (file.getName().equals("../") || file.getName().equals("..")) {
                    SmbFile f;
                    final String parentPath = _currentDir.getParent();
                    f = new SmbFile(parentPath);
                    if (_folderNavUpCB == null) _folderNavUpCB = _defaultNavUpCB;
                    if (_folderNavUpCB.canUpTo(f)) _currentDir = f;
                } else {
                    if (file.isDirectory()) {
                        if (_folderNavToCB == null) _folderNavToCB = _defaultNavToCB;
                        if (_folderNavToCB.canNavigate(file)) _currentDir = file;
                    } else {
                        if (!_dirOnly) {
                            if (_onChosenListener != null) {
                                _onChosenListener.onChoosePath(file.getPath(), file);
                                _alertDialog.dismiss();
                                return;
                            }
                        }
                    }
                }
                refreshDirs();
            } catch(MalformedURLException | SmbException e){
                e.printStackTrace();
            }
        });
        try{
            ret.get();
        } catch(InterruptedException | ExecutionException e){
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        //
    }

    private SmbDirAdapter refreshDirs() throws MalformedURLException, SmbException{
        listDirs();

        SmbDirAdapter adapter = new SmbDirAdapter(getBaseContext(), _entries, _rowLayoutRes != -1 ? _rowLayoutRes : R.layout.li_row_textview, _dateFormat);
        if (_adapterSetter != null) {
            _adapterSetter.apply(adapter);
        }

        if (_list != null) {
            ((Activity) getBaseContext()).runOnUiThread(() -> _list.setAdapter(adapter));
        }

        return adapter;
    }

    private List<SmbFile> _entries = new ArrayList<>();
    private SmbFile _currentDir;
    private String _rootDirPath;
    private SmbFile _rootDir;
    private AlertDialog _alertDialog;
    private ListView _list;
    private OnChosenListener _onChosenListener = null;
    private boolean _dirOnly;
    private SmbFileFilter _fileFilter;
    private @StringRes int _titleRes = 0, _okRes = 0, _negativeRes = 0;
    private @NonNull String _title = "Select a file", _ok = "Choose", _negative = "Cancel";
    private @DrawableRes int _iconRes = -1;
    //private Drawable _icon = null;
    private @LayoutRes int _layoutRes = -1;
    //private View _layout = null;
    private @LayoutRes int _rowLayoutRes = -1;
    //private View _rowLayout = null;
    private String _dateFormat;
    private DialogInterface.OnClickListener _negativeListener;
    private DialogInterface.OnCancelListener _onCancelListener;
    private boolean _disableTitle;

    @FunctionalInterface
    public interface AdapterSetter {
        void apply(SmbDirAdapter adapter);
    }

    private AdapterSetter _adapterSetter = null;

    private final static SmbFileFilter filterDirectoriesOnly = SmbFile::isDirectory;

    private final static SmbFileFilter filterFiles = file -> !file.isHidden();

    @FunctionalInterface
    public interface CanNavigateUp {
        boolean canUpTo(SmbFile dir) throws SmbException;
    }

    @FunctionalInterface
    public interface CanNavigateTo {
        boolean canNavigate(SmbFile dir);
    }

    private CanNavigateUp _folderNavUpCB;
    private CanNavigateTo _folderNavToCB;

    private final static CanNavigateUp _defaultNavUpCB = dir -> dir != null && dir.canRead();

    private final static CanNavigateTo _defaultNavToCB = dir -> true;

}
