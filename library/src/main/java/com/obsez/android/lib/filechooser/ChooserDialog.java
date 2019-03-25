package com.obsez.android.lib.filechooser;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.obsez.android.lib.filechooser.internals.ExtFileFilter;
import com.obsez.android.lib.filechooser.internals.FileUtil;
import com.obsez.android.lib.filechooser.internals.RegexFileFilter;
import com.obsez.android.lib.filechooser.internals.UiUtil;
import com.obsez.android.lib.filechooser.permissions.PermissionsUtil;
import com.obsez.android.lib.filechooser.tool.DirAdapter;
import com.obsez.android.lib.filechooser.tool.RootFile;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.obsez.android.lib.filechooser.internals.FileUtil.NewFolderFilter;

/**
 * Created by coco on 6/7/15.
 */
public class ChooserDialog implements AdapterView.OnItemClickListener, DialogInterface.OnClickListener,
    AdapterView.OnItemLongClickListener {
    @FunctionalInterface
    public interface Result {
        void onChoosePath(String dir, File dirFile);
    }

    /**
     * @deprecated will be removed at v1.2
     */
    public ChooserDialog() {
    }

    public ChooserDialog(Context cxt, @StyleRes int fileChooserTheme) {
        this._context = cxt;
        init(fileChooserTheme);
    }

    public ChooserDialog(Activity activity, @StyleRes int fileChooserTheme) {
        this._context = activity;
        init(fileChooserTheme);
    }

    public ChooserDialog(Fragment fragment, @StyleRes int fileChooserTheme) {
        this._context = fragment.getActivity();
        init(fileChooserTheme);
    }

    public ChooserDialog(Context cxt) {
        this._context = cxt;
        init();
    }

    public ChooserDialog(Activity activity) {
        this._context = activity;
        init();
    }

    public ChooserDialog(Fragment fragment) {
        this._context = fragment.getActivity();
        init();
    }

    /**
     * @param cxt android context
     * @return `this` reference
     * @deprecated will be removed at v1.2
     */
    public ChooserDialog with(Context cxt) {
        this._context = cxt;
        init();
        return this;
    }

    private void init() {
        init(null);
    }

    private void init(@Nullable @StyleRes Integer fileChooserTheme) {
        _onBackPressed = new defBackPressed(this);

        if (fileChooserTheme == null) {
            TypedValue typedValue = new TypedValue();
            if (!this._context.getTheme().resolveAttribute(
                R.attr.fileChooserStyle, typedValue, true))
                this._context = new ContextThemeWrapper(this._context, R.style.FileChooserStyle);
        } else {
            //noinspection UnnecessaryUnboxing
            this._context = new ContextThemeWrapper(this._context, fileChooserTheme.intValue());
        }
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

    public ChooserDialog withFilter(boolean dirOnly, final boolean allowHidden, String... suffixes) {
        this._dirOnly = dirOnly;
        if (suffixes == null || suffixes.length == 0) {
            this._fileFilter = dirOnly ?
                file -> file.isDirectory() && (!file.isHidden() || allowHidden) : file ->
                !file.isHidden() || allowHidden;
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

        if (_currentDir == null) {
            _currentDir = Environment.getExternalStorageDirectory();
        }

        return this;
    }

    public ChooserDialog dismissOnButtonClick(final boolean dismissOnButtonClick) {
        this._dismissOnButtonClick = dismissOnButtonClick;
        if (this._onBackPressed instanceof defBackPressed) {
            defBackPressed dbp = ((defBackPressed) this._onBackPressed);
            if (dismissOnButtonClick) {
                dbp._defaultLastBack = Dialog::dismiss;
            } else {
                dbp._defaultLastBack = dialog -> {
                };
            }
        }
        return this;
    }

    public ChooserDialog withChosenListener(Result r) {
        this._result = r;
        return this;
    }

    /**
     * @deprecated by {@link #withNegativeButtonListener(DialogInterface.OnClickListener)}
     */
    public ChooserDialog withOnBackPressedListener(OnBackPressedListener listener) {
        this._onBackPressed = listener;
        return this;
    }

    public ChooserDialog withOnLastBackPressedListener(OnBackPressedListener listener) {
        if (this._onBackPressed instanceof defBackPressed) {
            ((defBackPressed) this._onBackPressed)._onLastBackPressed = listener;
        }
        return this;
    }

    public ChooserDialog withResources(@StringRes int titleRes, @StringRes int okRes, @StringRes int cancelRes) {
        this._titleRes = titleRes;
        this._okRes = okRes;
        this._negativeRes = cancelRes;
        return this;
    }

    public ChooserDialog withStringResources(@Nullable String titleRes, @Nullable String okRes, @Nullable String cancelRes) {
        this._title = titleRes;
        this._ok = okRes;
        this._negative = cancelRes;
        return this;
    }

    /**
     * To enable the option pane with create/delete folder on the fly.
     * When u set it true, you may need WRITE_EXTERNAL_STORAGE declaration too.
     *
     * @param enableOptions true/false
     * @return this
     */
    public ChooserDialog enableOptions(boolean enableOptions) {
        this._enableOptions = enableOptions;
        return this;
    }

    public ChooserDialog withOptionResources(@StringRes int createDirRes, @StringRes int deleteRes,
        @StringRes int newFolderCancelRes, @StringRes int newFolderOkRes) {
        this._createDirRes = createDirRes;
        this._deleteRes = deleteRes;
        this._newFolderCancelRes = newFolderCancelRes;
        this._newFolderOkRes = newFolderOkRes;
        return this;
    }

    public ChooserDialog withOptionStringResources(@Nullable String createDir, @Nullable String delete,
        @Nullable String newFolderCancel, @Nullable String newFolderOk) {
        this._createDir = createDir;
        this._delete = delete;
        this._newFolderCancel = newFolderCancel;
        this._newFolderOk = newFolderOk;
        return this;
    }

    public ChooserDialog withOptionIcons(@DrawableRes int optionsIconRes, @DrawableRes int createDirIconRes,
        @DrawableRes int deleteRes) {
        this._optionsIconRes = optionsIconRes;
        this._createDirIconRes = createDirIconRes;
        this._deleteIconRes = deleteRes;
        return this;
    }

    public ChooserDialog withOptionIcons(@Nullable Drawable optionsIcon, @Nullable Drawable createDirIcon,
        @Nullable Drawable delete) {
        this._optionsIcon = optionsIcon;
        this._createDirIcon = createDirIcon;
        this._deleteIcon = delete;
        return this;
    }

    public ChooserDialog withNewFolderFilter(NewFolderFilter filter) {
        this._newFolderFilter = filter;
        return this;
    }

    public ChooserDialog withIcon(@DrawableRes int iconId) {
        this._iconRes = iconId;
        return this;
    }

    public ChooserDialog withIcon(@Nullable Drawable icon) {
        this._icon = icon;
        return this;
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public ChooserDialog withLayoutView(@LayoutRes int layoutResId) {
        this._layoutRes = layoutResId;
        return this;
    }

    /**
     * @deprecated use {@link AdapterSetter#apply(DirAdapter)}
     *             and then {@link DirAdapter.GetView#getView(File, boolean, boolean, View, ViewGroup, LayoutInflater)} instead
     */
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

    public ChooserDialog withNegativeButton(@Nullable String cancelTitle,
        final DialogInterface.OnClickListener listener) {
        this._negative = cancelTitle;
        if (cancelTitle != null) this._negativeRes = -1;
        this._negativeListener = listener;
        return this;
    }

    public ChooserDialog withNegativeButtonListener(final DialogInterface.OnClickListener listener) {
        this._negativeListener = listener;
        return this;
    }

    /**
     * onCancelListener will be triggered on back pressed or clicked outside of dialog
     */
    public ChooserDialog withOnCancelListener(final DialogInterface.OnCancelListener listener) {
        this._cancelListener2 = listener;
        return this;
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public ChooserDialog withOnDismissListener(final DialogInterface.OnDismissListener listener) {
        _onDismissListener = listener;
        return this;
    }

    public ChooserDialog withFileIcons(final boolean tryResolveFileTypeAndIcon, final Drawable fileIcon,
        final Drawable folderIcon) {
        _adapterSetter = adapter -> {
            if (fileIcon != null) adapter.setDefaultFileIcon(fileIcon);
            if (folderIcon != null) adapter.setDefaultFolderIcon(folderIcon);
            adapter.setResolveFileType(tryResolveFileTypeAndIcon);
        };
        return this;
    }

    public ChooserDialog withFileIconsRes(final boolean tryResolveFileTypeAndIcon, final int fileIcon,
        final int folderIcon) {
        _adapterSetter = adapter -> {
            if (fileIcon != -1) {
                adapter.setDefaultFileIcon(ContextCompat.getDrawable(_context, fileIcon));
            }
            if (folderIcon != -1) {
                adapter.setDefaultFolderIcon(
                    ContextCompat.getDrawable(_context, folderIcon));
            }
            adapter.setResolveFileType(tryResolveFileTypeAndIcon);
        };
        return this;
    }

    /**
     * @param setter you can override {@link DirAdapter#getView(int, View, ViewGroup)}
     *               see {@link AdapterSetter} for more information
     * @return this
     */
    public ChooserDialog withAdapterSetter(AdapterSetter setter) {
        _adapterSetter = setter;
        return this;
    }

    /**
     * @param cb give a hook at navigating up to a directory
     * @return this
     */
    public ChooserDialog withNavigateUpTo(CanNavigateUp cb) {
        _folderNavUpCB = cb;
        return this;
    }

    /**
     * @param cb give a hook at navigating to a child directory
     * @return this
     */
    public ChooserDialog withNavigateTo(CanNavigateTo cb) {
        _folderNavToCB = cb;
        return this;
    }

    public ChooserDialog disableTitle(boolean disableTitle) {
        _disableTitle = disableTitle;
        return this;
    }

    /**
     * allows dialog title follows the current folder name
     *
     * @param followDir dialog title will follow the changing of directory
     * @return this
     */
    public ChooserDialog titleFollowsDir(boolean followDir) {
        _followDir = followDir;
        return this;
    }

    /**
     * @param followDir deprecated
     * @return this
     * @deprecated at 1.1.17 or 1.2.0
     */
    public ChooserDialog followDir(boolean followDir) {
        _followDir = followDir;
        return this;
    }

    public ChooserDialog displayPath(boolean displayPath) {
        _displayPath = displayPath;
        return this;
    }

    public ChooserDialog customizePathView(CustomizePathView callback) {
        _customizePathView = callback;
        return this;
    }

    public ChooserDialog enableMultiple(boolean enableMultiple) {
        this._enableMultiple = enableMultiple;
        return this;
    }

    public ChooserDialog build() {
        TypedArray ta = _context.obtainStyledAttributes(R.styleable.FileChooser);
        int style = ta.getResourceId(R.styleable.FileChooser_fileChooserDialogStyle,
            R.style.FileChooserDialogStyle);
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(_context, style),
            ta.getResourceId(R.styleable.FileChooser_fileChooserDialogStyle, R.style.FileChooserDialogStyle));
        ta.recycle();

        if (_rowLayoutRes != -1) {
            _adapter = new DirAdapter(_context,
                new ArrayList<>(), _rowLayoutRes, this._dateFormat);
        } else {
            _adapter = new DirAdapter(_context, this._dateFormat);
        }
        if (_adapterSetter != null) _adapterSetter.apply(_adapter);

        refreshDirs();
        builder.setAdapter(_adapter, this);

        if (!_disableTitle) {
            if (_titleRes != -1)
                builder.setTitle(_titleRes);
            else if (_title != null)
                builder.setTitle(_title);
            else
                builder.setTitle(R.string.choose_file);
        }

        if (_iconRes != -1) {
            builder.setIcon(_iconRes);
        } else if (_icon != null) {
            builder.setIcon(_icon);
        }

        if (_layoutRes != -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setView(_layoutRes);
            }
        }

        if (_dirOnly || _enableMultiple) {
            // choosing folder, or multiple files picker
            DialogInterface.OnClickListener listener = (dialog, which) -> {
                if (_result != null) {
                    _result.onChoosePath(_currentDir.getAbsolutePath(), _currentDir);
                }
            };
            if (_okRes != -1)
                builder.setPositiveButton(_okRes, listener);
            else if (_ok != null)
                builder.setPositiveButton(_ok, listener);
            else
                builder.setPositiveButton(R.string.title_choose, listener);
        }

        if (_negativeRes != -1)
            builder.setNegativeButton(_negativeRes, _negativeListener);
        else if (_negative != null)
            builder.setNegativeButton(_negative, _negativeListener);
        else
            builder.setNegativeButton(R.string.dialog_cancel, _negativeListener);

        if (_cancelListener2 != null) {
            builder.setOnCancelListener(_cancelListener2);
        } else {
            builder.setOnCancelListener(dialog -> {
                Log.v("Cancel", "Cancel");
                dialog.cancel();
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && _onDismissListener != null) {
            builder.setOnDismissListener(_onDismissListener);
        }

        builder.setOnKeyListener(new keyListener(this));

        _alertDialog = builder.create();

        _alertDialog.setOnShowListener(new onShowListener(this));
        _list = _alertDialog.getListView();
        _list.setOnItemClickListener(this);
        if (_enableMultiple) {
            _list.setOnItemLongClickListener(this);
        }
        return this;
    }

    private void showDialog() {
        Window window = _alertDialog.getWindow();
        if (window != null) {
            TypedArray ta = _context.obtainStyledAttributes(R.styleable.FileChooser);
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(ta.getInt(R.styleable.FileChooser_fileChooserDialogGravity, Gravity.CENTER));
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.dimAmount = ta.getFloat(R.styleable.FileChooser_fileChooserDialogBackgroundDimAmount, 0.3f);
            lp.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(lp);
            ta.recycle();
        }
        _alertDialog.show();
    }

    public ChooserDialog show() {
        if (_alertDialog == null || _list == null) {
            build();
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            showDialog();
            return this;
        }

        if (_permissionListener == null) {
            _permissionListener = new PermissionsUtil.OnPermissionListener() {
                @Override
                public void onPermissionGranted(String[] permissions) {
                    boolean show = false;
                    for (String permission : permissions) {
                        if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            show = true;
                            break;
                        }
                    }
                    if (!show) return;
                    if (_enableOptions) {
                        show = false;
                        for (String permission : permissions) {
                            if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                show = true;
                                break;
                            }
                        }
                    }
                    if (!show) return;
                    if (_adapter.isEmpty()) refreshDirs();
                    showDialog();
                }

                @Override
                public void onPermissionDenied(String[] permissions) {
                    //
                }

                @Override
                public void onShouldShowRequestPermissionRationale(final String[] permissions) {
                    Toast.makeText(_context, "You denied the Read/Write permissions on SDCard.",
                        Toast.LENGTH_LONG).show();
                }
            };
        }


        final String[] permissions =
            _enableOptions ? new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE }
            : new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE };

        PermissionsUtil.checkPermissions(_context, _permissionListener, permissions);

        return this;
    }

    private void displayPath(String path) {
        if (_pathView == null) {
            final int rootId = _context.getResources().getIdentifier("contentPanel", "id", "android");
            final ViewGroup root = ((AlertDialog) _alertDialog).findViewById(rootId);
            // In case the id was changed or not found.
            if (root == null) return;

            ViewGroup.MarginLayoutParams params;
            if (root instanceof LinearLayout) {
                params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            } else {
                params = new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, Gravity.TOP);
            }

            TypedArray ta = _context.obtainStyledAttributes(R.styleable.FileChooser);
            int style = ta.getResourceId(R.styleable.FileChooser_fileChooserPathViewStyle,
                R.style.FileChooserPathViewStyle);
            final Context context = new ContextThemeWrapper(_context, style);

            _pathView = new TextView(context);
            root.addView(_pathView, 0, params);

            int elevation = ta.getInt(R.styleable.FileChooser_fileChooserPathViewElevation, 2);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                _pathView.setElevation(elevation);
            } else {
                ViewCompat.setElevation(_pathView, elevation);
            }
            ta.recycle();

            if (_customizePathView != null) {
                _customizePathView.customize(_pathView);
            }
        }

        if (path == null) {
            _pathView.setVisibility(View.GONE);

            ViewGroup.MarginLayoutParams param = ((ViewGroup.MarginLayoutParams) _list.getLayoutParams());
            if (_pathView.getParent() instanceof FrameLayout) {
                param.topMargin = _pathView.getHeight();
            }
            _list.setLayoutParams(param);
        } else {
            if (removableRoot == null || primaryRoot == null) {
                removableRoot = FileUtil.getStoragePath(_context, true);
                primaryRoot = FileUtil.getStoragePath(_context, false);
            }
            if (path.contains(removableRoot))
                path = path.substring(removableRoot.lastIndexOf('/') + 1);
            if (path.contains(primaryRoot))
                path = path.substring(primaryRoot.lastIndexOf('/') + 1);
            _pathView.setText(path);

            while (_pathView.getLineCount() > 1) {
                int i = path.indexOf("/");
                i = path.indexOf("/", i + 1);
                if (i == -1) break;
                path = "..." + path.substring(i);
                _pathView.setText(path);
            }

            _pathView.setVisibility(View.VISIBLE);

            ViewGroup.MarginLayoutParams param = ((ViewGroup.MarginLayoutParams) _list.getLayoutParams());
            if (_pathView.getHeight() == 0) {
                ViewTreeObserver viewTreeObserver = _pathView.getViewTreeObserver();
                viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        if (_pathView.getHeight() <= 0) {
                            return false;
                        }
                        _pathView.getViewTreeObserver().removeOnPreDrawListener(this);
                        if (_pathView.getParent() instanceof FrameLayout) {
                            param.topMargin = _pathView.getHeight();
                        }
                        _list.setLayoutParams(param);
                        return true;
                    }
                });
            } else {
                if (_pathView.getParent() instanceof FrameLayout) {
                    param.topMargin = _pathView.getHeight();
                }
                _list.setLayoutParams(param);
            }
        }
    }

    private String removableRoot = null;
    private String primaryRoot = null;
    private void listDirs() {
        _entries.clear();

        if (_currentDir == null) {
            _currentDir = new File(FileUtil.getStoragePath(_context, false));
        }

        // Get files
        File[] files = _currentDir.listFiles(_fileFilter);

        // Add the ".." entry
        boolean up = false;
        if (removableRoot == null || primaryRoot == null) {
            removableRoot = FileUtil.getStoragePath(_context, true);
            primaryRoot = FileUtil.getStoragePath(_context, false);
        }
        if (!removableRoot.equals(primaryRoot)) {
            //File f = Environment.getExternalStorageDirectory();
            //File newRoot = _currentDir.getParentFile();
            if (_currentDir.getAbsolutePath().equals(primaryRoot)) {
                _entries.add(new RootFile(sSdcardStorage)); //⇠
                up = true;
            } else if (_currentDir.getAbsolutePath().equals(removableRoot)) {
                _entries.add(new RootFile(sPrimaryStorage)); //⇽
                up = true;
            }
        }
        boolean displayPath = false;
        if (!up && _currentDir.getParentFile() != null && _currentDir.getParentFile().canRead()) {
            _entries.add(new RootFile(".."));
            displayPath = true;
        }

        if (files == null) return;

        List<File> dirList = new LinkedList<>();
        List<File> fileList = new LinkedList<>();

        for (File f : files) {
            if (f.isDirectory()) {
                dirList.add(f);
            } else {
                fileList.add(f);
            }
        }

        sortByName(dirList);
        sortByName(fileList);
        _entries.addAll(dirList);
        _entries.addAll(fileList);

        // #45: setup dialog title too
        if (_alertDialog != null && !_disableTitle) {
            if (_followDir) {
                if (displayPath) {
                    _alertDialog.setTitle(_currentDir.getName());
                } else {
                    if (_titleRes != -1)
                        _alertDialog.setTitle(_titleRes);
                    else if (_title != null)
                        _alertDialog.setTitle(_title);
                    else
                        _alertDialog.setTitle(R.string.choose_file);
                }

            }
        }

        if (_alertDialog != null && _displayPath) {
            if (displayPath) {
                displayPath(_currentDir.getPath());
            } else {
                displayPath(null);
            }
        }
        //_hoverIndex = -1;
    }

    private void sortByName(List<File> list) {
        Collections.sort(list, (f1, f2) -> f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase()));
    }

    void createNewDirectory(String name) {
        if (FileUtil.createNewDirectory(name, _currentDir)) {
            refreshDirs();
            return;
        }

        final File newDir = new File(_currentDir, name);
        Toast.makeText(_context,
            "Couldn't create folder " + newDir.getName() + " at " + newDir.getAbsolutePath(),
            Toast.LENGTH_LONG).show();
    }

    Runnable _deleteModeIndicator;

    @Override
    public void onItemClick(AdapterView<?> parent_, View list_, int position, long id_) {
        if (position < 0 || position >= _entries.size()) return;

        boolean scrollToTop = false;
        File file = _entries.get(position);
        if (file.getName().equals("..")) {
            if (!_list.hasFocus()) _list.requestFocus();
            doGoBack();
            return;
        } else if (file.getName().contains(sSdcardStorage)) {
            if (removableRoot == null) {
                removableRoot = FileUtil.getStoragePath(_context, true);
            }
            if (Environment.MEDIA_MOUNTED.equals(
                Environment.getExternalStorageState())) {
                _currentDir = new File(removableRoot);
                _chooseMode = _chooseMode == CHOOSE_MODE_DELETE ? CHOOSE_MODE_NORMAL : _chooseMode;
                if (_deleteModeIndicator != null) _deleteModeIndicator.run();
                _adapter.popAll();
            }
        } else if (file.getName().contains(sPrimaryStorage)) {
            if (primaryRoot == null) {
                primaryRoot = FileUtil.getStoragePath(_context, false);
            }
            _currentDir = new File(primaryRoot);
            _chooseMode = _chooseMode == CHOOSE_MODE_DELETE ? CHOOSE_MODE_NORMAL : _chooseMode;
            if (_deleteModeIndicator != null) _deleteModeIndicator.run();
            _adapter.popAll();
        } else {
            switch (_chooseMode) {
                case CHOOSE_MODE_NORMAL:
                    if (file.isDirectory()) {
                        if (_folderNavToCB == null) _folderNavToCB = _defaultNavToCB;
                        if (_folderNavToCB.canNavigate(file)) {
                            _currentDir = file;
                            scrollToTop = true;
                            _adapter.push(position);
                        }
                    } else if ((!_dirOnly) && _result != null) {
                        if (_dismissOnButtonClick) _alertDialog.dismiss();
                        _result.onChoosePath(file.getAbsolutePath(), file);
                        if (_enableMultiple) {
                            _result.onChoosePath(_currentDir.getAbsolutePath(), _currentDir);
                        }
                        return;
                    }
                    break;
                case CHOOSE_MODE_SELECT_MULTIPLE:
                    if (file.isDirectory()) {
                        if (_folderNavToCB == null) _folderNavToCB = _defaultNavToCB;
                        if (_folderNavToCB.canNavigate(file)) {
                            _currentDir = file;
                            scrollToTop = true;
                            _adapter.push();
                        }
                    } else {
                        _adapter.selectItem(position);
                        if (!_adapter.isAnySelected()) {
                            _chooseMode = CHOOSE_MODE_NORMAL;
                            _alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);
                        }
                        _result.onChoosePath(file.getAbsolutePath(), file);
                    }
                    break;
                case CHOOSE_MODE_DELETE:
                    try {
                        FileUtil.deleteFileRecursively(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(_context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    _chooseMode = CHOOSE_MODE_NORMAL;
                    if (_deleteModeIndicator != null) _deleteModeIndicator.run();
                    break;
                default:
                    // ERROR! It shouldn't get here...
                    break;
            }
        }
        refreshDirs();
        if (scrollToTop) _list.setSelection(0);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View list, int position, long id) {
        File file = _entries.get(position);
        if (file instanceof RootFile || file.isDirectory()) {
            return true;
        }
        if (_adapter.isSelected(position)) return true;
        _result.onChoosePath(file.getAbsolutePath(), file);
        _adapter.selectItem(position);
        _chooseMode = CHOOSE_MODE_SELECT_MULTIPLE;
        _alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
        if (_deleteModeIndicator != null) _deleteModeIndicator.run();
        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        //
    }

    void refreshDirs() {
        listDirs();
        _adapter.setEntries(_entries);
    }

    public void dismiss() {
        _alertDialog.dismiss();
    }

    boolean doMoveUp() {
        if (_list.hasFocus()) {
            Log.d("z", "move up at " + _adapter.getHoveredIndex());
            int indexOld = _adapter.getHoveredIndex();
            int index = _adapter.decreaseHoveredIndex();
            if (indexOld >= 0 && indexOld != index) {
                UiUtil.ensureVisible(_list, index);
                _list.requestFocus();
            } else {
                _list.setSelection(index); // to prevent the list scroll to top.
                //moveFocusToButtons();
            }
        } else if (buttonsHasFocus()) {
            _list.requestFocus();
        }
        return true;
    }

    boolean doMoveDown() {
        if (_list.hasFocus()) {
            Log.d("z", "move down at " + _adapter.getHoveredIndex());
            int indexOld = _adapter.getHoveredIndex();
            int index = _adapter.increaseHoveredIndex();
            if (indexOld >= 0 && indexOld != index) {
                UiUtil.ensureVisible(_list, index);
                _list.requestFocus();
            } else {
                _list.setSelection(index); // to prevent the list scroll to top.
                moveFocusToButtons();
            }
            //} else if (buttonsHasFocus()) {
            //    _list.requestFocus();
        }
        return true;
    }

    boolean doGoBack() {
        if (_list.hasFocus()) {
            //Log.d("z", "go back at " + _adapter.getHoveredIndex());
            //int position = _adapter.getHoveredIndex();

            //boolean scrollToTop = false;
            //File file = _entries.get(position);

            File f = _currentDir.getParentFile();
            Log.d("z", "go back at " + _adapter.getHoveredIndex() + ", go up level: " + f.getAbsolutePath());
            if (_folderNavUpCB == null) _folderNavUpCB = _defaultNavUpCB;
            if (_folderNavUpCB.canUpTo(f)) {
                _currentDir = f;
                _chooseMode = _chooseMode == CHOOSE_MODE_DELETE ? CHOOSE_MODE_NORMAL : _chooseMode;
                if (_deleteModeIndicator != null) _deleteModeIndicator.run();
                //scrollToTop = true;
                _adapter.pop();

                refreshDirs();
                //if (scrollToTop) _list.setSelection(0);
                //_list.requestFocus();
                _list.setSelection(_adapter.getHoveredIndex());
            }
        }
        return true;
    }

    boolean doEnter() {
        if (_list.hasFocus()) {
            Log.d("z", "enter at " + _adapter.getHoveredIndex());
            int position = _adapter.getHoveredIndex();
            onItemClick(_list, _list, position, -1);
            //_list.requestFocus();
        } else if (buttonsHasFocus()) {
            _alertDialog.getCurrentFocus().performClick();
        }
        return true;
    }

    boolean buttonsHasFocus() {
        View v = _alertDialog.getCurrentFocus();
        return v instanceof Button;
        //return v == _alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL) ||
        //    v == _alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE) ||
        //    v == _alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
    }

    private boolean moveFocusToButtons() {
        View v = null;
        if (_alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).getVisibility() == View.VISIBLE) {
            v = _alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        }
        if (v == null && _alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).getVisibility() == View.VISIBLE) {
            v = _alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        }
        if (v == null && _alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).getVisibility() == View.VISIBLE) {
            v = _alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        }
        if (v != null) {
            v.requestFocus();
            return true;
        }
        return false;
    }

    boolean cancelFolderViewOrBack(DialogInterface dialog) {
        if (_newFolderView != null && _newFolderView.getVisibility() == View.VISIBLE) {
            _newFolderView.setVisibility(View.GONE);
            return true;
        } else {
            _onBackPressed.onBackPressed((AlertDialog) dialog);
            return true;
        }
    }

    //private int _hoverIndex = -1;
    List<File> _entries = new ArrayList<>();
    DirAdapter _adapter;
    File _currentDir;
    Context _context;
    AlertDialog _alertDialog;
    ListView _list;
    Result _result = null;
    boolean _dirOnly;
    private FileFilter _fileFilter;
    private @StringRes
    int _titleRes = -1, _okRes = -1, _negativeRes = -1;
    private @Nullable
    String _title, _ok, _negative;
    private @DrawableRes
    int _iconRes = -1;
    private @Nullable
    Drawable _icon;
    private @LayoutRes
    int _layoutRes = -1;
    private @LayoutRes @Deprecated
    int _rowLayoutRes = -1;
    private String _dateFormat;
    DialogInterface.OnClickListener _negativeListener;
    private DialogInterface.OnCancelListener _cancelListener2;
    private DialogInterface.OnDismissListener _onDismissListener;
    private boolean _disableTitle;
    boolean _enableOptions;
    private boolean _followDir;
    private boolean _displayPath = true;
    private TextView _pathView;
    private CustomizePathView _customizePathView;
    View _options;
    @StringRes
    int _createDirRes = -1, _deleteRes = -1, _newFolderCancelRes = -1, _newFolderOkRes = -1;
    @Nullable
    String _createDir, _delete, _newFolderCancel, _newFolderOk;
    @DrawableRes
    int _optionsIconRes = -1, _createDirIconRes = -1, _deleteIconRes = -1;
    @Nullable
    Drawable _optionsIcon, _createDirIcon, _deleteIcon;
    @Nullable
    View _newFolderView;
    boolean _dismissOnButtonClick = true;
    boolean _enableMultiple;
    private PermissionsUtil.OnPermissionListener _permissionListener;

    @FunctionalInterface
    public interface AdapterSetter {
        void apply(DirAdapter adapter);
    }

    private AdapterSetter _adapterSetter = null;

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

    @FunctionalInterface
    public interface OnBackPressedListener {
        void onBackPressed(AlertDialog dialog);
    }

    private OnBackPressedListener _onBackPressed = null;

    final static String sSdcardStorage = ".. SDCard Storage";
    final static String sPrimaryStorage = ".. Primary Storage";

    static final int CHOOSE_MODE_NORMAL = 0;
    static final int CHOOSE_MODE_DELETE = 1;
    static final int CHOOSE_MODE_SELECT_MULTIPLE = 2;

    int _chooseMode = CHOOSE_MODE_NORMAL;

    NewFolderFilter _newFolderFilter;

    @FunctionalInterface
    public interface CustomizePathView {
        void customize(TextView pathView);
    }
}
