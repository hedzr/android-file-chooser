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
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.obsez.android.lib.filechooser.internals.ExtSmbFileFilter;
import com.obsez.android.lib.filechooser.internals.RegexSmbFileFilter;
import com.obsez.android.lib.filechooser.tool.SmbDirAdapter;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
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
 * Created by coco on 6/7/15. Edited by Guiorgy on 10/09/18.
 */
public class SmbFileChooserDialog extends ContextWrapper implements AdapterView.OnItemClickListener, DialogInterface.OnClickListener{
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactory(){
        @Override
        public Thread newThread(@NonNull final Runnable runnable){
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            thread.setName("SmbFileChooserDialog - Thread");
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
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

    @NonNull public SmbFileChooserDialog setStartFile(@Nullable final String startFile) throws ExecutionException, InterruptedException{
        Future<SmbFileChooserDialog> ret = EXECUTOR.submit(new Callable<SmbFileChooserDialog>(){
            @Override
            public SmbFileChooserDialog call() throws Exception{
                if(startFile != null){
                    _currentDir = new SmbFile(startFile);
                } else{
                    _currentDir = _rootDir;
                }

                if(!_currentDir.isDirectory()){
                    String parent = _currentDir.getParent();
                    if(parent == null){
                        throw new MalformedURLException(startFile + " has no parent directory");
                    }
                    _currentDir = new SmbFile(parent);
                }

                if(_currentDir == null){
                    _currentDir = _rootDir;
                }
                return SmbFileChooserDialog.this;
            }
        });
        return ret.get();
    }

    @NonNull public SmbFileChooserDialog setOnChosenListener(@NonNull OnChosenListener listener) {
        this._onChosenListener = listener;
        return this;
    }

    @NonNull public SmbFileChooserDialog setCancelable(boolean cancelable){
        this._cancelable = cancelable;
        return this;
    }

    @NonNull public SmbFileChooserDialog setOnDismissListener(@NonNull DialogInterface.OnDismissListener listener){
        this._onDismissListener = listener;
        return this;
    }

    @NonNull public SmbFileChooserDialog setOnBackPressedListener(OnBackPressedListener listener){
        this._onBackPressed = listener;
        return this;
    }

    @NonNull public SmbFileChooserDialog setResources(@StringRes int titleRes, @StringRes int okRes, @StringRes int cancelRes) {
        this._titleRes = titleRes;
        this._okRes = okRes;
        this._negativeRes = cancelRes;
        return this;
    }

    @NonNull public SmbFileChooserDialog setResources(@Nullable String title, @Nullable String ok, @Nullable String cancel) {
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

    @NonNull public SmbFileChooserDialog setNegativeButton(@NonNull String cancelTitle, @NonNull final DialogInterface.OnClickListener listener) {
        this._negative = cancelTitle;
        this._negativeRes = 0;
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
        _adapterSetter = new AdapterSetter(){
            @Override
            public void apply(final SmbDirAdapter adapter){
                if(fileIcon != null)
                    adapter.setDefaultFileIcon(fileIcon);
                if(folderIcon != null)
                    adapter.setDefaultFolderIcon(folderIcon);
                //noinspection deprecation
                adapter.setResolveFileType(tryResolveFileTypeAndIcon);
            }
        };
        return this;
    }

    @NonNull public SmbFileChooserDialog setFileIcons(@Nullable final Drawable fileIcon, @Nullable final Drawable folderIcon) {
        _adapterSetter = new AdapterSetter(){
            @Override
            public void apply(final SmbDirAdapter adapter){
                if(fileIcon != null)
                    adapter.setDefaultFileIcon(fileIcon);
                if(folderIcon != null)
                    adapter.setDefaultFolderIcon(folderIcon);
            }
        };
        return this;
    }

    /**
     * @deprecated need to find a way to actually get the icons.
     */
    @Deprecated
    @NonNull public SmbFileChooserDialog setFileIcons(final boolean tryResolveFileTypeAndIcon, final int fileIconResId, final int folderIconResId) {
        _adapterSetter = new AdapterSetter(){
            @Override
            public void apply(final SmbDirAdapter adapter){
                if(fileIconResId != -1)
                    adapter.setDefaultFileIcon(ContextCompat.getDrawable(SmbFileChooserDialog.this.getBaseContext(), fileIconResId));
                if(folderIconResId != -1)
                    adapter.setDefaultFolderIcon(ContextCompat.getDrawable(SmbFileChooserDialog.this.getBaseContext(), folderIconResId));
                //noinspection deprecation
                adapter.setResolveFileType(tryResolveFileTypeAndIcon);
            }
        };
        return this;
    }

    @NonNull public SmbFileChooserDialog setFileIcons(final int fileIconResId, final int folderIconResId) {
        _adapterSetter = new AdapterSetter(){
            @Override
            public void apply(final SmbDirAdapter adapter){
                if(fileIconResId != -1)
                    adapter.setDefaultFileIcon(ContextCompat.getDrawable(SmbFileChooserDialog.this.getBaseContext(), fileIconResId));
                if(folderIconResId != -1)
                    adapter.setDefaultFolderIcon(ContextCompat.getDrawable(SmbFileChooserDialog.this.getBaseContext(), folderIconResId));
            }
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());

        Future<Void> thread = EXECUTOR.submit(new Callable<Void>(){
            @Override
            public Void call() throws Exception{
                SmbDirAdapter adapter = SmbFileChooserDialog.this.refreshDirs();
                if(SmbFileChooserDialog.this._adapterSetter != null){
                    SmbFileChooserDialog.this._adapterSetter.apply(adapter);
                }

                if(!SmbFileChooserDialog.this._disableTitle){
                    if(SmbFileChooserDialog.this._titleRes == 0)
                        builder.setTitle(SmbFileChooserDialog.this._title);
                    else
                        builder.setTitle(SmbFileChooserDialog.this._titleRes);
                }
                builder.setAdapter(adapter, SmbFileChooserDialog.this);
                return null;
            }
        });
        thread.get();

        if(this._iconRes != -1){
            builder.setIcon(this._iconRes);
        }

        if(-1 != this._layoutRes){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                builder.setView(this._layoutRes);
            }
        }

        if(this._dirOnly){
            final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener(){
                @Override
                public void onClick(final DialogInterface dialog, final int which){
                    if(SmbFileChooserDialog.this._onChosenListener != null){
                        if(SmbFileChooserDialog.this._dirOnly){
                            SmbFileChooserDialog.this._onChosenListener.onChoosePath(SmbFileChooserDialog.this._currentDir.getPath(), SmbFileChooserDialog.this._currentDir);
                        }
                    }
                    dialog.dismiss();
                }
            };

            if(this._okRes == 0)
                builder.setPositiveButton(this._ok, listener);
            else
                builder.setPositiveButton(this._okRes, listener);
        }

        if(this._negativeListener == null){
            this._negativeListener = new DialogInterface.OnClickListener(){
                @Override
                public void onClick(final DialogInterface dialog, final int id){
                    dialog.cancel();
                }
            };
        }

        if(this._negativeRes == 0)
            builder.setNegativeButton(this._negative, this._negativeListener);
        else
            builder.setNegativeButton(this._negativeRes, this._negativeListener);

        if(this._onCancelListener != null){
            builder.setOnCancelListener(this._onCancelListener);
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
                            SmbFileChooserDialog.this._onBackPressed.onBackPressed((AlertDialog) dialog);
                        }
                        return true;
                    }
                });

        this._alertDialog = builder.create();
        this._list = this._alertDialog.getListView();
        this._list.setOnItemClickListener(this);
        return SmbFileChooserDialog.this;
    }

    @NonNull public SmbFileChooserDialog show() throws ExecutionException, InterruptedException{
        Future<SmbFileChooserDialog> ret = EXECUTOR.submit(new Callable<SmbFileChooserDialog>(){
            @Override
            public SmbFileChooserDialog call(){
                if(_alertDialog == null || _list == null){
                    throw new RuntimeException("Dialog has not been built yet! (call .build() before .show())");
                }

                // Check for permissions if SDK version is >= 23
                if(Build.VERSION.SDK_INT >= 23){
                    final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 0;
                    int permissionCheck = ContextCompat.checkSelfPermission(SmbFileChooserDialog.this.getBaseContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                    //if = permission granted
                    if(permissionCheck == PackageManager.PERMISSION_GRANTED){
                        ((Activity) SmbFileChooserDialog.this.getBaseContext()).runOnUiThread(new Runnable(){
                            @Override
                            public void run(){
                                _alertDialog.show();
                            }
                        });
                    } else{
                        ActivityCompat.requestPermissions((Activity) SmbFileChooserDialog.this.getBaseContext(),
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                } else{
                    ((Activity) SmbFileChooserDialog.this.getBaseContext()).runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            _alertDialog.show();
                        }
                    });
                }

                return SmbFileChooserDialog.this;
            }
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
        Collections.sort(list, new Comparator<SmbFile>(){
            @Override
            public int compare(final SmbFile f1, final SmbFile f2){
                return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
            }
        });
    }

    /**
     * @deprecated better use listDirs as it sorts directories and files separately
     */
    @Deprecated
    private void listDirsUncategorised() throws ExecutionException, InterruptedException{
        Future ret = EXECUTOR.submit(new Runnable(){
            @Override
            public void run(){
                try{
                    _entries.clear();

                    // Get files
                    SmbFile[] files = _currentDir.listFiles();

                    // Add the ".." entry
                    final String parent = _currentDir.getParent();
                /*if (parent != null && !parent.equalsIgnoreCase("smb://")) {
                    _entries.add(new SmbFile(parent));
                }*/
                    if(parent != null && !parent.equalsIgnoreCase("smb://")){
                        _entries.add(new SmbFile(".."));
                    }

                    if(files != null){
                        for(SmbFile file : files){
                            if(!file.isDirectory()){
                                continue;
                            }

                            _entries.add(file);
                        }
                    }

                    SmbFileChooserDialog.this.sortByName(_entries);
                } catch(MalformedURLException | SmbException e){
                    e.printStackTrace();
                }
            }
        });
        ret.get();
    }

    @Override
    public void onItemClick(@Nullable AdapterView<?> parent, @NonNull View list, final int pos, final long id) {
        Future ret = EXECUTOR.submit(new Runnable(){
            @Override
            public void run(){
                try{
                    if(pos < 0 || pos >= _entries.size()){
                        return;
                    }

                    SmbFile file = _entries.get(pos);
                    //Log.d("TAG", file.getName(),)
                    if(file.getName().equals("../") || file.getName().equals("..")){
                        SmbFile f;
                        final String parentPath = _currentDir.getParent();
                        f = new SmbFile(parentPath);
                        if(_folderNavUpCB == null)
                            _folderNavUpCB = _defaultNavUpCB;
                        if(_folderNavUpCB.canUpTo(f))
                            _currentDir = f;
                    } else{
                        if(file.isDirectory()){
                            if(_folderNavToCB == null)
                                _folderNavToCB = _defaultNavToCB;
                            if(_folderNavToCB.canNavigate(file))
                                _currentDir = file;
                        } else{
                            if(!_dirOnly){
                                if(_onChosenListener != null){
                                    _onChosenListener.onChoosePath(file.getPath(), file);
                                    _alertDialog.dismiss();
                                    return;
                                }
                            }
                        }
                    }
                    SmbFileChooserDialog.this.refreshDirs();
                } catch(MalformedURLException | SmbException e){
                    e.printStackTrace();
                }
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

        final SmbDirAdapter adapter = new SmbDirAdapter(getBaseContext(), _entries, _rowLayoutRes != -1 ? _rowLayoutRes : R.layout.li_row_textview, _dateFormat);
        if (_adapterSetter != null) {
            _adapterSetter.apply(adapter);
        }

        if (_list != null) {
            ((Activity) getBaseContext()).runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    _list.setAdapter(adapter);
                }
            });
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
    private boolean _cancelable = true;
    private DialogInterface.OnDismissListener _onDismissListener;

    @FunctionalInterface
    public interface AdapterSetter {
        void apply(SmbDirAdapter adapter);
    }

    private AdapterSetter _adapterSetter = null;

    private final static SmbFileFilter filterDirectoriesOnly = new SmbFileFilter(){
        @Override
        public boolean accept(final SmbFile smbFile) throws SmbException{
            return smbFile.isDirectory();
        }
    };

    private final static SmbFileFilter filterFiles = new SmbFileFilter(){
        @Override
        public boolean accept(final SmbFile file) throws SmbException{
            return !file.isHidden();
        }
    };

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

    private final static CanNavigateUp _defaultNavUpCB = new CanNavigateUp(){
        @Override
        public boolean canUpTo(final SmbFile dir) throws SmbException{
            return dir != null && dir.canRead();
        }
    };

    private final static CanNavigateTo _defaultNavToCB = new CanNavigateTo(){
        @Override
        public boolean canNavigate(final SmbFile dir){
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
            if(SmbFileChooserDialog.this._entries.size() > 0
                    && (SmbFileChooserDialog.this._entries.get(0).getName().equals("../") || SmbFileChooserDialog.this._entries.get(0).getName().equals(".."))){
                SmbFileChooserDialog.this.onItemClick(null, SmbFileChooserDialog.this._list, 0, 0);
            } else{
                dialog.dismiss();
            }
        }
    });
}
